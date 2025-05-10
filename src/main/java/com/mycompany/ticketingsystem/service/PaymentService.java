package com.mycompany.ticketingsystem.service;

import com.google.cloud.firestore.Firestore;
import com.google.gson.Gson;
import com.mycompany.ticketingsystem.config.FirebaseConfig;
import com.mycompany.ticketingsystem.mqtt.MqttPublisher;
import com.mycompany.ticketingsystem.mqtt.Topics;
import com.mycompany.ticketingsystem.model.PaymentTransaction;

import java.time.LocalDateTime;
import java.util.UUID;

public class PaymentService {
    private final Firestore db;
    private final MqttPublisher publisher;
    private final Gson gson = new Gson();

    public PaymentService() {
        try {
            FirebaseConfig.init();
            this.db        = FirebaseConfig.getDb();
            this.publisher = new MqttPublisher();
        } catch (Exception e) {
            throw new RuntimeException("Unable to initialize PaymentService", e);
        }
    }

    /**
     * Start a new payment, persist it, and broadcast the request.
     */
    public PaymentTransaction initiatePayment(double amount, String relatedID) {
        String txnID  = UUID.randomUUID().toString();
        String date   = LocalDateTime.now().toString();
        String status = "pending";

        PaymentTransaction txn = new PaymentTransaction(
                txnID, amount, date, status, relatedID
        );

        try {
            // 1. Persist initial transaction
            db.collection("payments")
                    .document(txnID)
                    .set(txn)
                    .get();

            // 2. Publish over MQTT
            String json = gson.toJson(txn);
            publisher.publish(Topics.PAYMENT_REQUEST, json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initiate payment", e);
        }

        return txn;
    }

    /**
     * Handle an incoming status update: persist to Firestore.
     */
    public void handleIncomingStatus(PaymentTransaction update) {
        try {
            db.collection("payments")
                    .document(update.getTransactionID())
                    .set(update)
                    .get();
            System.out.println(
                    "Persisted payment update: " +
                            update.getTransactionID() + " → " +
                            update.getStatus()
            );
        } catch (Exception e) {
            System.err.println("Error saving payment status: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
