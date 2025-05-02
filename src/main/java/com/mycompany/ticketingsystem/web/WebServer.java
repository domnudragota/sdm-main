package com.mycompany.ticketingsystem.web;

import static spark.Spark.*;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.cloud.firestore.WriteBatch;
import com.google.gson.Gson;
import com.mycompany.ticketingsystem.config.FirebaseConfig;
import com.mycompany.ticketingsystem.model.Ticket;

import java.util.concurrent.ExecutionException;

public class WebServer {
    public static void main(String[] args) throws Exception {
        // 1) Initialize Firebase
        FirebaseConfig.init();
        Firestore db = FirebaseConfig.getDb();
        Gson gson = new Gson();

        // 2) Cleanup: delete all existing tickets on startup
        {
            WriteBatch batch = db.batch();
            Iterable<DocumentReference> docs = db.collection("tickets").listDocuments();
            for (DocumentReference docRef : docs) {
                batch.delete(docRef);
            }
            // commit and wait
            batch.commit().get();
            System.out.println("Cleared tickets collection at startup.");
        }

        // 3) Start server
        port(4567);

        // JSON API
        get("/tickets", (req, res) -> {
            ApiFuture<QuerySnapshot> q = db.collection("tickets").get();
            res.type("application/json");
            return gson.toJson(q.get().toObjects(Ticket.class));
        });

        // HTML UI with auto-refresh every 10s
        get("/", (req, res) -> {
            ApiFuture<QuerySnapshot> q = db.collection("tickets").get();
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><head>")
                    .append("<meta http-equiv=\"refresh\" content=\"10\">")
                    .append("<title>Ticketing System Status</title>")
                    .append("</head><body>")
                    .append("<h1>Issued Tickets (updates every 10s)</h1>")
                    .append("<ul>");
            for (DocumentSnapshot doc : q.get().getDocuments()) {
                Ticket t = doc.toObject(Ticket.class);
                html.append("<li>")
                        .append("ID: ").append(t.getTicketID())
                        .append(", Type: ").append(t.getTicketType())
                        .append(", Price: ").append(t.getPrice())
                        .append(", Issued: ").append(t.getIssueDate())
                        .append(", Expires: ").append(t.getExpirationDate())
                        .append("</li>");
            }
            html.append("</ul></body></html>");
            res.type("text/html");
            return html.toString();
        });
    }
}
