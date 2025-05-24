package com.mycompany.ticketingsystem.web;

import static spark.Spark.*;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.gson.Gson;

import com.google.firebase.auth.*;
import com.mycompany.ticketingsystem.auth.AuthService;
import com.mycompany.ticketingsystem.auth.Role;
import com.mycompany.ticketingsystem.auth.User;
import com.mycompany.ticketingsystem.config.FirebaseConfig;
import com.mycompany.ticketingsystem.model.JourneyPlan;
import com.mycompany.ticketingsystem.model.PaymentTransaction;
import com.mycompany.ticketingsystem.model.Ticket;

import java.util.List;
import java.util.Map;

/** Minimal HTTP layer: JSON API + auto-refreshing HTML dashboard. */
public class WebServer {

    private static final Gson gson = new Gson();
    private static Firestore  db;
    private static FirebaseAuth fbAuth;
    private static CollectionReference usersColl;

    public static void main(String[] args) throws Exception {

        /* ---------- bootstrap Firebase ---------- */
        FirebaseConfig.init();
        db        = FirebaseConfig.getDb();
        fbAuth    = FirebaseConfig.getAuth();
        usersColl = db.collection("users");

        AuthService authSvc = new AuthService();

        /* ---------- Spark setup ---------- */
        port(4567);

        /* ---------  CORS for local testing (optional) -------- */
        options("/*", (req, res) -> {
            String acrHeaders = req.headers("Access-Control-Request-Headers");
            if (acrHeaders != null) res.header("Access-Control-Allow-Headers", acrHeaders);
            String acrMethod = req.headers("Access-Control-Request-Method");
            if (acrMethod != null)  res.header("Access-Control-Allow-Methods", acrMethod);
            return "OK";
        });
        before((req, res) -> res.header("Access-Control-Allow-Origin", "*"));

        /* ---------- PUBLIC: sign-up & sign-in ---------- */

        post("/api/register", (req, res) -> {
            Map<?,?> payload = gson.fromJson(req.body(), Map.class);
            String email    = (String) payload.get("email");
            String password = (String) payload.get("password");
            String roleStr  = (String) payload.get("role");
            Role role       = Role.valueOf(roleStr.toUpperCase());

            String token = (role == Role.OPERATOR)
                    ? authSvc.registerOperator(email, password)
                    : authSvc.registerPassenger(email, password);

            res.type("application/json");
            return "{\"token\":\"" + token + "\"}";
        });

        post("/api/login", (req, res) -> {
            Map<?,?> payload = gson.fromJson(req.body(), Map.class);
            String email    = (String) payload.get("email");
            String password = (String) payload.get("password");     // kept for parity; not re-checked server-side
            String token = authSvc.login(email, password);                // AuthService verifies user exists

            res.type("application/json");
            return "{\"token\":\"" + token + "\"}";
        });

        /* ---------- PROTECTED JSON endpoints ---------- */

        before("/tickets",  requireRole(Role.PASSENGER));   // only passengers list their tickets
        before("/plans",    requireRole(Role.PASSENGER));
        before("/payments", requireRole(Role.OPERATOR));    // only operators audit payments

        get("/tickets",  (req, res) -> toJson("tickets",  Ticket.class,  res));
        get("/payments", (req, res) -> toJson("payments", PaymentTransaction.class, res));
        get("/plans",    (req, res) -> toJson("plans",    JourneyPlan.class, res));

        /* ---------- auto-refreshing HTML dashboard ---------- */

        get("/", (req, res) -> {
            res.type("text/html");
            StringBuilder html = new StringBuilder()
                    .append("<!DOCTYPE html><html><head>")
                    .append("<meta http-equiv=\"refresh\" content=\"10\">")
                    .append("<title>Ticketing System Status</title>")
                    .append("</head><body>")
                    .append("<h1>System Dashboard (updates every 10 s)</h1>");

            appendList(html, "Issued Tickets",  "tickets",  Ticket.class);
            appendList(html, "Payments",        "payments", PaymentTransaction.class);
            appendList(html, "Journey Plans",   "plans",    JourneyPlan.class);

            html.append("</body></html>");
            return html.toString();
        });
    }

    /* ---------- helpers ---------- */

    private static <T> String toJson(String coll, Class<T> cls, spark.Response res) throws Exception {
        List<T> list = db.collection(coll).get().get().toObjects(cls);
        res.type("application/json");
        return gson.toJson(list);
    }

    /** Small Spark 'before' filter factory that enforces a single role. */
    private static spark.Filter requireRole(Role required) {
        return (req, res) -> {
            String hdr = req.headers("Authorization");
            if (hdr == null || !hdr.startsWith("Bearer "))
                halt(401, "Missing Bearer token");

            String token = hdr.substring(7);
            FirebaseToken decoded;
            try {
                decoded = fbAuth.verifyIdToken(token);
            } catch (Exception e) {
                halt(401, "Invalid token: " + e.getMessage());
                return;
            }

            String uid = decoded.getUid();
            ApiFuture<DocumentSnapshot> snap = usersColl.document(uid).get();
            DocumentSnapshot doc = snap.get();
            if (!doc.exists()) halt(403, "User not found");

            Role role = Role.valueOf(doc.getString("role"));
            if (role != required)
                halt(403, "Forbidden for role " + role);

            /* stash uid/role so downstream handlers can use it if needed */
            req.attribute("uid",  uid);
            req.attribute("role", role);
        };
    }

    private static <T> void appendList(StringBuilder html,
                                       String title,
                                       String coll,
                                       Class<T> cls) {
        html.append("<h2>").append(title).append("</h2><ul>");
        try {
            for (T obj : db.collection(coll).get().get().toObjects(cls)) {
                html.append("<li>").append(gson.toJson(obj)).append("</li>");
            }
        } catch (Exception e) {
            html.append("<li>Error loading ").append(coll).append(": ")
                    .append(e.getMessage()).append("</li>");
        }
        html.append("</ul>");
    }
}
