package com.mycompany.ticketingsystem;

import com.mycompany.ticketingsystem.config.FirebaseConfig;
import com.mycompany.ticketingsystem.mqtt.MqttPublisher;
import com.mycompany.ticketingsystem.mqtt.MqttSubscriber;
import com.mycompany.ticketingsystem.model.Ticket;
import com.mycompany.ticketingsystem.service.PaymentService;
import com.mycompany.ticketingsystem.service.JourneyPlanner;
import com.mycompany.ticketingsystem.web.WebServer;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;

import java.time.LocalDate;

public class CombinedMain {
    public static void main(String[] args) throws Exception {
        // 1) Initialize Firebase & clear the "tickets" collection
        FirebaseConfig.init();
        Firestore db = FirebaseConfig.getDb();
        for (DocumentReference docRef : db.collection("tickets").listDocuments()) {
            docRef.delete().get();
        }
        System.out.println("Cleared tickets collection on startup.");

        // 2) Launch the web server in a daemon thread
        Thread webThread = new Thread(() -> {
            try {
                WebServer.main(new String[]{});
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, "WebServer-Thread");
        webThread.setDaemon(true);
        webThread.start();

        // 3) Give Spark a moment to bind its port and set up routes
        Thread.sleep(2000);
        System.out.println("Web server should now be up on port 4567.");

        // 4) Start MQTT subscriber & publisher (shared)
        MqttSubscriber subscriber = new MqttSubscriber();
        MqttPublisher  publisher  = new MqttPublisher();

        // 5) Create services with shared publisher
        PaymentService paymentService   = new PaymentService();
        JourneyPlanner journeyPlanner   = new JourneyPlanner();

        // 6) Simulate issuing 10 tickets, one every 3 seconds
        LocalDate today    = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        for (int i = 1; i <= 10; i++) {
            Ticket ticket = new Ticket(
                    "TCKT" + i,
                    "single-ride",
                    2.50,
                    today.toString(),
                    tomorrow.toString()
            );
            publisher.publishTicketInfo(ticket);
            Thread.sleep(3000);
        }

        // 7) Simulate Payments: initiate and persist 5 payment requests
        for (int i = 1; i <= 5; i++) {
            paymentService.initiatePayment(2.50, "TCKT" + i);
            Thread.sleep(2000);
        }

        // 8) Simulate Journey Plans: generate and persist 5 plans
        for (int i = 1; i <= 5; i++) {
            journeyPlanner.generatePlan("PAX" + i, "Route-" + i + ": A→B→C");
            Thread.sleep(2000);
        }

        // 9) Done publishing—keep JVM alive for inspection
        System.out.println("Simulation complete; entering idle mode. Press Ctrl+C to quit.");
        Thread.currentThread().join();
    }
}
