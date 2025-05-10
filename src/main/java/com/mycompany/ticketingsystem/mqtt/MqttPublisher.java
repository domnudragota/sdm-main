package com.mycompany.ticketingsystem.mqtt;

import com.google.gson.Gson;
import com.mycompany.ticketingsystem.model.Ticket;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Publishes domain events to the MQTT broker.
 */
public class MqttPublisher {
    private static final String BROKER_URL      = "tcp://localhost:1883";
    private static final String CLIENT_ID_BASE  = "TicketSystemPublisher";

    private final MqttClient mqttClient;
    private final Gson       gson = new Gson();

    public MqttPublisher() throws MqttException {
        // each publisher gets a unique client ID and in-memory persistence
        String clientId = CLIENT_ID_BASE + "-" + UUID.randomUUID();
        mqttClient = new MqttClient(BROKER_URL, clientId, new MemoryPersistence());

        MqttConnectOptions opts = new MqttConnectOptions();
        opts.setCleanSession(true);
        mqttClient.connect(opts);
    }

    /**
     * Issue a new ticket: serialize it to JSON, then publish on the standard topic.
     */
    public void publishTicketInfo(Ticket ticket) throws MqttException {
        String json = gson.toJson(ticket);
        publish(Topics.TICKET_ISSUED, json);
        System.out.println("Published ticket info: " + json);
    }

    /**
     * Generic publish routine: QoS=1, and retain on TICKET_ISSUED & SCHEDULE_UPDATE.
     */
    public void publish(String topic, String payload) throws MqttException {
        MqttMessage msg = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
        msg.setQos(1);

        // Retain last issued-ticket & schedule-update for late subscribers
        if (Topics.TICKET_ISSUED.equals(topic) || Topics.SCHEDULE_UPDATE.equals(topic)) {
            msg.setRetained(true);
        }

        mqttClient.publish(topic, msg);
    }

    /** Cleanly disconnect from the broker. */
    public void disconnect() throws MqttException {
        mqttClient.disconnect();
    }
}
