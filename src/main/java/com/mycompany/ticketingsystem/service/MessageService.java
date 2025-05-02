package com.mycompany.ticketingsystem.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.mycompany.ticketingsystem.config.FirebaseConfig;
import com.mycompany.ticketingsystem.model.Ticket;

public class MessageService {
    private final Firestore db;
    private static volatile MessageService instance;

    private MessageService() {
        try {
            FirebaseConfig.init();
            this.db = FirebaseConfig.getDb();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Firebase", e);
        }
    }

    public static MessageService getInstance() {
        if (instance == null) {
            synchronized (MessageService.class) {
                if (instance == null) {
                    instance = new MessageService();
                }
            }
        }
        return instance;
    }

    /**
     * Persists a Ticket to Firestore under the "tickets" collection asynchronously.
     */
    public void addTicket(Ticket ticket) {
        ApiFuture<WriteResult> future = db.collection("tickets")
                .document(ticket.getTicketID())
                .set(ticket);

        // Add a listener to handle success or failure without blocking the MQTT thread
        future.addListener(() -> {
            try {
                WriteResult result = future.get();
                System.out.println("Ticket saved at: " + result.getUpdateTime());
            } catch (Exception e) {
                System.err.println("Error saving ticket: " + e.getMessage());
                e.printStackTrace();
            }
        }, Runnable::run);
    }
}
