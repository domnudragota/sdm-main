package com.mycompany.ticketingsystem.service;

import com.google.cloud.firestore.Firestore;
import com.google.gson.Gson;
import com.mycompany.ticketingsystem.auth.AuthGuard;
import com.mycompany.ticketingsystem.auth.Role;
import com.mycompany.ticketingsystem.config.FirebaseConfig;
import com.mycompany.ticketingsystem.mqtt.MqttPublisher;
import com.mycompany.ticketingsystem.mqtt.Topics;
import com.mycompany.ticketingsystem.model.JourneyPlan;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Generates and persists journey plans, then broadcasts them on MQTT.
 * All state-changing calls require a PASSENGER role token.
 */
public class JourneyPlanner {

    private final Firestore     db;
    private final MqttPublisher publisher;
    private final Gson          gson = new Gson();

    /* ---------- constructors ---------- */

    /** Production constructor: caller supplies its Firebase ID-token. */
    public JourneyPlanner(String idToken) {
        try {
            FirebaseConfig.init();
            this.db        = FirebaseConfig.getDb();
            this.publisher = new MqttPublisher(idToken);
        } catch (Exception e) {
            throw new RuntimeException("Unable to initialise JourneyPlanner", e);
        }
    }

    /** Dev-only shortcut (broker auth disabled). */
    public JourneyPlanner() {
        this("dev-token");
    }

    /* ---------- public API ---------- */

    /**
     * Create a journey plan for the logged-in passenger.
     *
     * @param idToken       Firebase ID-token of the caller.
     * @param passengerUid  UID of the passenger (normally the same as the token’s UID).
     * @param routeDetails  Free-text route description.
     */
    public JourneyPlan generatePlan(String idToken,
                                    String passengerUid,
                                    String routeDetails) {

        try {
            /* 1️⃣  Enforce role - throws if not a PASSENGER */
            AuthGuard.require(idToken, Role.PASSENGER);

            /* 2️⃣  Build plan object */
            String planID = UUID.randomUUID().toString();
            String now    = LocalDateTime.now().toString();

            JourneyPlan plan = new JourneyPlan(
                    planID,
                    passengerUid,
                    routeDetails,
                    "30m",                       // dummy ETA
                    now,
                    LocalDateTime.now()
                            .plusMinutes(30)
                            .toString()
            );

            /* 3️⃣  Persist to Firestore */
            db.collection("plans")
                    .document(planID)
                    .set(plan)
                    .get();

            /* 4️⃣  Publish to the passenger-scoped topic */
            String json = gson.toJson(plan);
            publisher.publish(Topics.journeyPlan(passengerUid), json);

            return plan;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate journey plan", ex);
        }
    }

    /**
     * Persist a plan that arrived via MQTT (already authorised by broker ACLs).
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
