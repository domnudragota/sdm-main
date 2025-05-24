package com.mycompany.ticketingsystem;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;

import com.mycompany.ticketingsystem.auth.AuthService;
import com.mycompany.ticketingsystem.config.FirebaseConfig;
import com.mycompany.ticketingsystem.mqtt.MqttPublisher;
import com.mycompany.ticketingsystem.mqtt.MqttSubscriber;
import com.mycompany.ticketingsystem.model.JourneyPlan;
import com.mycompany.ticketingsystem.model.PaymentTransaction;
import com.mycompany.ticketingsystem.model.Ticket;
import com.mycompany.ticketingsystem.service.JourneyPlanner;
import com.mycompany.ticketingsystem.service.PaymentService;
import com.mycompany.ticketingsystem.web.WebServer;

import io.github.cdimascio.dotenv.Dotenv;

import java.time.LocalDate;

public class CombinedMain {

    public static void main(String[] args) throws Exception {

        /* ─── load .env so FIREBASE_API_KEY is visible ─────────────────── */
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().systemProperties().load();

        /* ─── 1. Init Firebase & clear demo collections ────────────────── */
        FirebaseConfig.init();
        Firestore db = FirebaseConfig.getDb();
        for (String coll : new String[]{"tickets", "payments", "plans"}) {
            for (DocumentReference d : db.collection(coll).listDocuments()) d.delete();
        }
        System.out.println("Cleared tickets, payments, plans on startup.");

        /* ─── 2. Get demo passenger & operator ID-tokens ───────────────── */
        AuthService auth = new AuthService();
        String paxToken = auth.login("demo.passenger@example.com", "pass123");
        String opToken  = auth.login("demo.operator@example.com",   "op12345");

        /* ─── 3. Start Spark web server on a daemon thread ─────────────── */
        Thread webThread = new Thread(() -> {
            try { WebServer.main(new String[]{}); }
            catch (Exception ex) { throw new RuntimeException(ex); }
        }, "WebServer");
        webThread.setDaemon(true);
        webThread.start();
        Thread.sleep(1500);
        System.out.println("Web portal:  http://localhost:4567  (auto-refresh)");

        /* ─── 4. MQTT endpoints (passenger JWT) ────────────────────────── */
        MqttSubscriber subPassenger = new MqttSubscriber(paxToken);
        MqttPublisher  pubPassenger = new MqttPublisher(paxToken);

        /* ─── 5. Domain services (role-correct tokens) ─────────────────── */
        PaymentService paymentSvc   = new PaymentService(opToken);
        JourneyPlanner journeyPlan  = new JourneyPlanner(paxToken);

        /* ─── 6. Issue 10 demo tickets ─────────────────────────────────── */
        LocalDate today = LocalDate.now(), tomorrow = today.plusDays(1);
        for (int i = 1; i <= 10; i++) {
            Ticket t = new Ticket("TCKT"+i, "single-ride", 2.50,
                    today.toString(), tomorrow.toString());
            pubPassenger.publishTicketInfo(t);
            System.out.println("Ticket inserted:  " + t.getTicketID());
            Thread.sleep(1000);
        }

        /* ─── 7. Insert 5 payments (prints to console) ─────────────────── */
        for (int i = 1; i <= 5; i++) {
            PaymentTransaction tx =
                    paymentSvc.initiatePayment(opToken, 2.50, "PAX"+i);
            System.out.println("Payment inserted: " + tx.getTransactionID());
            Thread.sleep(1000);
        }

        /* ─── 8. Insert 5 journey plans (prints to console) ────────────── */
        for (int i = 1; i <= 5; i++) {
            JourneyPlan jp = journeyPlan.generatePlan(
                    paxToken, "PAX"+i, "Route-"+i+": A→B→C");
            System.out.println("Plan inserted:    " + jp.getPlanID());
            Thread.sleep(1000);
        }

        /* ─── 9. Idle for inspection ───────────────────────────────────── */
        System.out.println("\nSimulation complete — check the web portal!");
        Thread.currentThread().join();
    }
}
