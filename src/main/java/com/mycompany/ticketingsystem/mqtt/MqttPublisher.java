package com.mycompany.ticketingsystem.mqtt;

import com.google.gson.Gson;
import com.mycompany.ticketingsystem.model.Ticket;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Publishes domain events to the MQTT broker.
 * The caller MUST supply the Firebase ID-token so the broker’s jwt-auth plugin
 * can authenticate / authorise the connection.
 */
public class MqttPublisher {

    private static final String BROKER_URL     = "tcp://localhost:1883";
    private static final String CLIENT_ID_BASE = "TicketSystemPublisher";

    private final MqttClient mqttClient;
    private final Gson       gson = new Gson();

    /* ───────── constructors ───────── */

    /** Production: pass the ID-token obtained from /api/login or /api/register. */
    public MqttPublisher(String idToken) throws MqttException {
        this(idToken, BROKER_URL);
    }

    /** Overload for tests / alternate broker URL. */
    public MqttPublisher(String idToken, String brokerUrl) throws MqttException {
        String clientId = CLIENT_ID_BASE + "-" + UUID.randomUUID();
        mqttClient = new MqttClient(brokerUrl, clientId, new MemoryPersistence());

        MqttConnectOptions opts = new MqttConnectOptions();
        opts.setCleanSession(true);

        /*  🔑  Send the JWT in the PASSWORD field (common for Mosquitto JWT plugins) */
        opts.setPassword(idToken.toCharArray());
        // Optional: setUserName to a hint (not used by jwt plugin)
        opts.setUserName("jwt");

        mqttClient.connect(opts);
    }

    /** Dev-only shortcut when broker auth is disabled. */
    public MqttPublisher() throws MqttException {
        this("dev-token");
    }

    /* ───────── publishing helpers ───────── */

    /** Serialise and publish a new ticket on the standard topic. */
    public void publishTicketInfo(Ticket ticket) throws MqttException {
        String json = gson.toJson(ticket);
        publish(Topics.TICKET_ISSUED, json);
        System.out.println("Published ticket info: " + json);
    }

    /** Generic publish routine: QoS 1, retain on key topics. */
    public void publish(String topic, String payload) throws MqttException {
        MqttMessage msg = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
        msg.setQos(1);

        // Retain ticket-issued & schedule-update for late subscribers
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
