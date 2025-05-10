package com.mycompany.ticketingsystem.web;

import static spark.Spark.*;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.gson.Gson;
import com.mycompany.ticketingsystem.config.FirebaseConfig;
import com.mycompany.ticketingsystem.model.JourneyPlan;
import com.mycompany.ticketingsystem.model.PaymentTransaction;
import com.mycompany.ticketingsystem.model.Ticket;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class WebServer {
    public static void main(String[] args) throws Exception {
        // Initialize Firebase & get DB
        FirebaseConfig.init();
        Firestore db = FirebaseConfig.getDb();
        Gson gson = new Gson();

        port(4567);

        // JSON endpoints

        get("/tickets", (req, res) -> {
            ApiFuture<QuerySnapshot> q = db.collection("tickets").get();
            List<Ticket> list = q.get().toObjects(Ticket.class);
            res.type("application/json");
            return gson.toJson(list);
        });

        get("/payments", (req, res) -> {
            ApiFuture<QuerySnapshot> q = db.collection("payments").get();
            List<PaymentTransaction> list = q.get().toObjects(PaymentTransaction.class);
            res.type("application/json");
            return gson.toJson(list);
        });

        get("/plans", (req, res) -> {
            ApiFuture<QuerySnapshot> q = db.collection("plans").get();
            List<JourneyPlan> list = q.get().toObjects(JourneyPlan.class);
            res.type("application/json");
            return gson.toJson(list);
        });

        // Auto-refreshing HTML dashboard

        get("/", (req, res) -> {
            res.type("text/html");
            StringBuilder html = new StringBuilder()
                    .append("<!DOCTYPE html><html><head>")
                    .append("<meta http-equiv=\"refresh\" content=\"10\">")
                    .append("<title>Ticketing System Status</title>")
                    .append("</head><body>")
                    .append("<h1>System Dashboard (updates every 10s)</h1>");

            // Tickets
            html.append("<h2>Issued Tickets</h2><ul>");
            try {
                for (Ticket t : db.collection("tickets").get().get().toObjects(Ticket.class)) {
                    html.append("<li>")
                            .append("ID=").append(t.getTicketID())
                            .append(", Type=").append(t.getTicketType())
                            .append(", Price=").append(t.getPrice())
                            .append(", Issued=").append(t.getIssueDate())
                            .append(", Expires=").append(t.getExpirationDate())
                            .append("</li>");
                }
            } catch (Exception e) {
                html.append("<li>Error loading tickets: ").append(e.getMessage()).append("</li>");
            }
            html.append("</ul>");

            // Payments
            html.append("<h2>Payments</h2><ul>");
            try {
                for (PaymentTransaction p : db.collection("payments").get().get().toObjects(PaymentTransaction.class)) {
                    html.append("<li>")
                            .append("TxnID=").append(p.getTransactionID())
                            .append(", Amount=").append(p.getAmount())
                            .append(", Status=").append(p.getStatus())
                            .append(", Date=").append(p.getDate())
                            .append("</li>");
                }
            } catch (Exception e) {
                html.append("<li>Error loading payments: ").append(e.getMessage()).append("</li>");
            }
            html.append("</ul>");

            // Journey Plans
            html.append("<h2>Journey Plans</h2><ul>");
            try {
                for (JourneyPlan j : db.collection("plans").get().get().toObjects(JourneyPlan.class)) {
                    html.append("<li>")
                            .append("PlanID=").append(j.getPlanID())
                            .append(", Passenger=").append(j.getPassengerID())
                            .append(", ETA=").append(j.getEstimatedTravelTime())
                            .append(", Departs=").append(j.getDepartureTime())
                            .append(", Arrives=").append(j.getArrivalTime())
                            .append("</li>");
                }
            } catch (Exception e) {
                html.append("<li>Error loading plans: ").append(e.getMessage()).append("</li>");
            }
            html.append("</ul>");

            html.append("</body></html>");
            return html.toString();
        });
    }
}
