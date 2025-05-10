package com.mycompany.ticketingsystem.web;

import static spark.Spark.*;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.gson.Gson;
import com.mycompany.ticketingsystem.config.FirebaseConfig;
import com.mycompany.ticketingsystem.model.Ticket;

import java.util.concurrent.ExecutionException;

public class WebServer {
    public static void main(String[] args) throws Exception {
        FirebaseConfig.init();
        Firestore db = FirebaseConfig.getDb();
        Gson gson = new Gson();

        port(4567);

        // JSON list of all tickets
        get("/tickets", (req, res) -> {
            ApiFuture<QuerySnapshot> query = db.collection("tickets").get();
            res.type("application/json");
            return gson.toJson(query.get().toObjects(Ticket.class));
        });

        // HTML view, auto-refresh every 10s
        get("/", (req, res) -> {
            ApiFuture<QuerySnapshot> query = db.collection("tickets").get();
            StringBuilder html = new StringBuilder()
                    .append("<!DOCTYPE html><html><head>")
                    .append("<meta http-equiv=\"refresh\" content=\"10\">")
                    .append("<title>Issued Tickets</title>")
                    .append("</head><body>")
                    .append("<h1>Issued Tickets (refresh every 10s)</h1><ul>");
            try {
                for (Ticket t : query.get().toObjects(Ticket.class)) {
                    html.append("<li>")
                            .append("ID: ").append(t.getTicketID())
                            .append(", Type: ").append(t.getTicketType())
                            .append(", Price: ").append(t.getPrice())
                            .append(", Issued: ").append(t.getIssueDate())
                            .append(", Expires: ").append(t.getExpirationDate())
                            .append("</li>");
                }
            } catch (InterruptedException | ExecutionException e) {
                halt(500, "Error fetching tickets: " + e.getMessage());
            }
            html.append("</ul></body></html>");
            res.type("text/html");
            return html.toString();
        });

        init();            // start Spark
        awaitInitialization();  // block until routes are bound
    }
}
