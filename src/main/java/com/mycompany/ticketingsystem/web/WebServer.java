package com.mycompany.ticketingsystem.web;

import static spark.Spark.*;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.gson.Gson;

import com.mycompany.ticketingsystem.auth.AuthService;
import com.mycompany.ticketingsystem.auth.Role;
import com.mycompany.ticketingsystem.config.FirebaseConfig;
import com.mycompany.ticketingsystem.model.JourneyPlan;
import com.mycompany.ticketingsystem.model.PaymentTransaction;
import com.mycompany.ticketingsystem.model.Ticket;

import java.util.List;
import java.util.Map;

/** JSON API + static UI + auto-refreshing HTML dashboard. */
public class WebServer {

    private static final Gson gson = new Gson();
    private static Firestore  db;
    private static FirebaseAuth fbAuth;
    private static CollectionReference usersColl;

    public static void main(String[] args) throws Exception {

        /* ─── Firebase ───────────────────────────────────────────────── */
        FirebaseConfig.init();
        db        = FirebaseConfig.getDb();
        fbAuth    = FirebaseConfig.getAuth();
        usersColl = db.collection("users");
        AuthService authSvc = new AuthService();

        /* ─── Spark ──────────────────────────────────────────────────── */
        port(4567);
        staticFileLocation("/public");               // login.html, dashboards…

        options("/*", (req,res)->{
            String h=req.headers("Access-Control-Request-Headers");
            if(h!=null) res.header("Access-Control-Allow-Headers",h);
            String m=req.headers("Access-Control-Request-Method");
            if(m!=null) res.header("Access-Control-Allow-Methods",m);
            return "OK";
        });
        before((req,res)-> res.header("Access-Control-Allow-Origin","*"));

        /* ─── PUBLIC: register + login ──────────────────────────────── */
        post("/api/register", (req,res)->{
            Map<?,?> b=gson.fromJson(req.body(),Map.class);
            String email=(String)b.get("email"), pwd=(String)b.get("password");
            Role role=Role.valueOf(((String)b.get("role")).toUpperCase());

            String tok = (role==Role.OPERATOR)
                    ? authSvc.registerOperator(email,pwd)
                    : authSvc.registerPassenger(email,pwd);

            res.type("application/json"); return "{\"token\":\""+tok+"\"}";
        });

        post("/api/login", (req,res)->{
            Map<?,?> b=gson.fromJson(req.body(),Map.class);
            String tok=authSvc.login((String)b.get("email"),(String)b.get("password"));
            res.type("application/json"); return "{\"token\":\""+tok+"\"}";
        });

        /* ─── PUBLIC: who-am-I (UID + role) ─────────────────────────── */
        get("/api/me", (req,res)->{
            String hdr=req.headers("Authorization");
            if(hdr==null||!hdr.startsWith("Bearer ")) halt(401,"Missing token");
            FirebaseToken tok=fbAuth.verifyIdToken(hdr.substring(7));

            String role=(String)tok.getClaims().get("role");
            if(role==null) role=usersColl.document(tok.getUid()).get().get().getString("role");

            res.type("application/json");
            return gson.toJson(Map.of("uid",tok.getUid(),"role",role));
        });

        /* ─── PROTECTED JSON endpoints ──────────────────────────────── */
        before("/tickets",      requireRole(Role.PASSENGER));        // passenger owns tickets
        before("/tickets/all",  requireRole(Role.OPERATOR));         // operator audit
        before("/plans",        requireRole(Role.PASSENGER));
        before("/payments",     requireRole(Role.OPERATOR));

        get("/tickets",      (rq,rs)-> toJson("tickets",  Ticket.class,  rs));
        get("/tickets/all",  (rq,rs)-> toJson("tickets",  Ticket.class,  rs));
        get("/payments",     (rq,rs)-> toJson("payments", PaymentTransaction.class, rs));
        get("/plans",        (rq,rs)-> toJson("plans",    JourneyPlan.class, rs));

        /* ─── Legacy auto-refresh dashboard ─────────────────────────── */
        get("/", (req,res)->{
            res.type("text/html");
            StringBuilder h=new StringBuilder()
                    .append("<!DOCTYPE html><html><head>")
                    .append("<meta http-equiv=\"refresh\" content=\"10\">")
                    .append("<title>Ticketing System Status</title></head><body>")
                    .append("<h1>System Dashboard (updates every 10 s)</h1>");

            appendList(h,"Issued Tickets","tickets",Ticket.class);
            appendList(h,"Payments","payments",PaymentTransaction.class);
            appendList(h,"Journey Plans","plans",JourneyPlan.class);

            return h.append("</body></html>").toString();
        });
    }

    /* ─── util: read collection & JSON serialise ────────────────────── */
    private static <T> String toJson(String coll, Class<T> cls, spark.Response res) throws Exception{
        List<T> list=db.collection(coll).get().get().toObjects(cls);
        res.type("application/json"); return gson.toJson(list);
    }

    /* ─── role filter (accepts multiple roles) ──────────────────────── */
    private static spark.Filter requireRole(Role... allowed){
        return (req,res)->{
            String hdr=req.headers("Authorization");
            if(hdr==null||!hdr.startsWith("Bearer ")) halt(401,"Missing Bearer token");

            FirebaseToken tok=fbAuth.verifyIdToken(hdr.substring(7));
            DocumentSnapshot doc=usersColl.document(tok.getUid()).get().get();
            if(!doc.exists()) halt(403,"User not found");

            Role caller=Role.valueOf(doc.getString("role"));
            for(Role r:allowed) if(caller==r) { req.attribute("uid",tok.getUid()); return; }
            halt(403,"Forbidden for role "+caller);
        };
    }

    /* ─── util: append list to legacy HTML page ─────────────────────── */
    private static <T> void appendList(StringBuilder html,String title,
                                       String coll,Class<T> cls){
        html.append("<h2>").append(title).append("</h2><ul>");
        try{
            for(T obj: db.collection(coll).get().get().toObjects(cls)){
                html.append("<li>").append(gson.toJson(obj)).append("</li>");
            }
        }catch(Exception e){
            html.append("<li>Error loading ").append(coll).append(": ")
                    .append(e.getMessage()).append("</li>");
        }
        html.append("</ul>");
    }
}
