package com.mycompany.ticketingsystem;

import com.mycompany.ticketingsystem.config.FirebaseConfig;
import com.mycompany.ticketingsystem.mqtt.MqttPublisher;
import com.mycompany.ticketingsystem.mqtt.MqttSubscriber;
import com.mycompany.ticketingsystem.model.Ticket;
import com.mycompany.ticketingsystem.web.WebServer;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;

import java.time.LocalDate;

public class CombinedMain {
    public static void main(String[] args) throws Exception {
        // 1) Init Firebase & clear tickets
        FirebaseConfig.init();
        Firestore db = FirebaseConfig.getDb();
        for (DocumentReference doc : db.collection("tickets").listDocuments()) {
            doc.delete().get();
        }
        System.out.println("Cleared tickets collection on startup.");

        // 2) Start web server in a daemon thread
        Thread webThread = new Thread(() -> {
            try {
                WebServer.main(new String[]{});
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, "WebServer-Thread");
        webThread.setDaemon(true);
        webThread.start();

        // 3) Give Spark 2 seconds to bind its port and set up routes
        Thread.sleep(2000);
        System.out.println("Web server should now be up on port 4567.");

        // 4) Start MQTT subscriber & publisher
        MqttSubscriber subscriber = new MqttSubscriber();
        MqttPublisher  publisher  = new MqttPublisher();

        // 5) Publish 10 tickets, one every 3s
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

        // 6) Cleanup
        publisher.disconnect();
        System.out.println("Simulation complete, exiting.");
        System.exit(0);
    }
}
