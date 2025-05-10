package com.mycompany.ticketingsystem.service;

import com.google.cloud.firestore.Firestore;
import com.google.gson.Gson;
import com.mycompany.ticketingsystem.config.FirebaseConfig;
import com.mycompany.ticketingsystem.mqtt.MqttPublisher;
import com.mycompany.ticketingsystem.mqtt.Topics;
import com.mycompany.ticketingsystem.model.JourneyPlan;

import java.time.LocalDateTime;
import java.util.UUID;

public class JourneyPlanner {
    private final Firestore db;
    private final MqttPublisher publisher;
    private final Gson gson = new Gson();

    public JourneyPlanner() {
        try {
            FirebaseConfig.init();
            this.db        = FirebaseConfig.getDb();
            this.publisher = new MqttPublisher();
        } catch (Exception e) {
            throw new RuntimeException("Unable to initialize JourneyPlanner", e);
        }
    }

    /**
     * Generate a plan, persist it, and broadcast on MQTT.
     */
    public JourneyPlan generatePlan(String passengerID, String routeDetails) {
        String planID = UUID.randomUUID().toString();
        String now    = LocalDateTime.now().toString();

        JourneyPlan plan = new JourneyPlan(
                planID,
                passengerID,
                routeDetails,
                "30m",                       // dummy ETA
                now,
                LocalDateTime.now()
                        .plusMinutes(30)
                        .toString()
        );

        try {
            // 1. Persist to Firestore
            db.collection("plans")
                    .document(planID)
                    .set(plan)
                    .get();

            // 2. Publish over MQTT
            String json = gson.toJson(plan);
            publisher.publish(Topics.JOURNEY_PLAN, json);

            return plan;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate journey plan", e);
        }
    }

    /**
     * Handle incoming plan requests from MQTT—persist to Firestore as well.
     */
    public void handleIncomingPlan(JourneyPlan plan) {
        try {
            db.collection("plans")
                    .document(plan.getPlanID())
                    .set(plan)
                    .get();
            System.out.println("Persisted incoming plan: " + plan.getPlanID());
        } catch (Exception e) {
            System.err.println("Error saving journey plan: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
