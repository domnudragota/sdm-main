package com.mycompany.ticketingsystem.service;

import com.google.cloud.firestore.*;
import com.google.gson.Gson;
import com.mycompany.ticketingsystem.config.FirebaseConfig;
import com.mycompany.ticketingsystem.mqtt.MqttPublisher;
import com.mycompany.ticketingsystem.mqtt.Topics;
import com.mycompany.ticketingsystem.model.Ticket;
import com.mycompany.ticketingsystem.model.TicketValidationRecord;

import java.time.LocalDateTime;
import java.util.UUID;

public class TicketService {
    private final Firestore db;
    private final MqttPublisher publisher;
    private final Gson gson = new Gson();

    public TicketService() throws Exception {
        FirebaseConfig.init();
        this.db = FirebaseConfig.getDb();
        this.publisher = new MqttPublisher();
    }

    /** Issue a new ticket and broadcast it. */
    public Ticket createTicket(String ticketType, double price) throws Exception {
        String ticketID = UUID.randomUUID().toString();
        String issueDate = LocalDateTime.now().toString();
        String expirationDate = LocalDateTime.now().plusDays(30).toString();
        Ticket ticket = new Ticket(ticketID, ticketType, price, issueDate, expirationDate);

        // persist to Firestore
        db.collection("tickets").document(ticketID).set(ticket).get();

        // publish over MQTT
        publisher.publish(Topics.TICKET_ISSUED, gson.toJson(ticket));
        return ticket;
    }

    /** Validate an existing ticket, record the validation, and return the result. */
    public boolean validateTicket(String ticketID, String location) throws Exception {
        DocumentSnapshot snap = db.collection("tickets").document(ticketID).get().get();
        Ticket ticket = snap.toObject(Ticket.class);

        boolean valid = ticket != null && ticket.isValid(LocalDateTime.now().toString());
        String recordID = UUID.randomUUID().toString();
        String timestamp = LocalDateTime.now().toString();
        String status    = valid ? "successful" : "failed";

        TicketValidationRecord record =
                new TicketValidationRecord(recordID, ticketID, timestamp, location, status);
        db.collection("validations").document(recordID).set(record).get();

        // optionally broadcast validation (e.g. on schedule/update or add a new topic)
        publisher.publish(Topics.SCHEDULE_UPDATE, gson.toJson(record));
        return valid;
    }
}
