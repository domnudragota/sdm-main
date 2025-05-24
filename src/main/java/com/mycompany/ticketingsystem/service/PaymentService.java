package com.mycompany.ticketingsystem.service;

import com.google.cloud.firestore.Firestore;
import com.google.gson.Gson;

import com.mycompany.ticketingsystem.auth.AuthGuard;
import com.mycompany.ticketingsystem.auth.Role;
import com.mycompany.ticketingsystem.config.FirebaseConfig;
import com.mycompany.ticketingsystem.mqtt.MqttPublisher;
import com.mycompany.ticketingsystem.mqtt.Topics;
import com.mycompany.ticketingsystem.model.PaymentTransaction;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Handles fare collection.  All state-changing calls require an OPERATOR token.
 */
public class PaymentService {

    private final Firestore     db;
    private final MqttPublisher publisher;
    private final Gson          gson = new Gson();

    /* -------- constructors -------- */

    /** Production: caller supplies its Firebase ID-token. */
    public PaymentService(String idToken) {
        try {
            FirebaseConfig.init();
            this.db        = FirebaseConfig.getDb();
            this.publisher = new MqttPublisher(idToken);
        } catch (Exception e) {
            throw new RuntimeException("Unable to initialise PaymentService", e);
        }
    }

    /** Dev-only shortcut (broker auth disabled). */
    public PaymentService() {
        this("dev-token");
    }

    /* -------- public API -------- */

    /**
     * Start a new payment, persist it, and broadcast the request.
     *
     * @param idToken    Firebase ID-token of the caller.
     * @param amount     Fare amount.
     * @param passengerUid  UID of the passenger this fare belongs to.
     */
    public PaymentTransaction initiatePayment(String idToken,
                                              double amount,
                                              String passengerUid) {

        try {
            /* 1️⃣ Role check – must be OPERATOR */
            AuthGuard.require(idToken, Role.OPERATOR);

            /* 2️⃣ Build transaction object */
            String txnID  = UUID.randomUUID().toString();
            String date   = LocalDateTime.now().toString();
            String status = "pending";

            PaymentTransaction txn = new PaymentTransaction(
                    txnID, amount, date, status, passengerUid
            );

            /* 3️⃣ Persist initial transaction */
            db.collection("payments")
                    .document(txnID)
                    .set(txn)
                    .get();

            /* 4️⃣ Broadcast over MQTT (global topic) */
            String json = gson.toJson(txn);
            publisher.publish(Topics.PAYMENT_REQUEST, json);

            return txn;

        } catch (Exception ex) {
            throw new RuntimeException("Failed to initiate payment", ex);
        }
    }

    /**
     * Persist an incoming status update (already authorised by MQTT ACL).
     */
    public void handleIncomingStatus(PaymentTransaction update) {
        try {
            db.collection("payments")
                    .document(update.getTransactionID())
                    .set(update)
                    .get();
            System.out.println(
                    "Persisted payment update: " +
                            update.getTransactionID() + " → " + update.getStatus()
            );
        } catch (Exception e) {
            System.err.println("Error saving payment status: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
