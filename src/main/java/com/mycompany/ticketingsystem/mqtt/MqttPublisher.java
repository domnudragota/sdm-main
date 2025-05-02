package com.mycompany.ticketingsystem.mqtt;

import com.google.gson.Gson;
import com.mycompany.ticketingsystem.model.Ticket;
import org.eclipse.paho.client.mqttv3.*;

import java.nio.charset.StandardCharsets;

public class MqttPublisher {

    private static final String BROKER_URL = "tcp://localhost:1883";
    private static final String CLIENT_ID  = "TicketSystemPublisher";
    private static final String TOPIC      = "tickets/sold";

    private final MqttClient mqttClient;
    private final Gson gson = new Gson();

    public MqttPublisher() throws MqttException {
        mqttClient = new MqttClient(BROKER_URL, CLIENT_ID);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        mqttClient.connect(options);
    }

    /**
     * Publishes the given Ticket as a JSON payload to the MQTT broker.
     */
    public void publishTicketInfo(Ticket ticket) throws MqttException {
        // Serialize the Ticket object to JSON
        String json = gson.toJson(ticket);

        MqttMessage message = new MqttMessage(json.getBytes(StandardCharsets.UTF_8));
        message.setQos(1); // Quality of Service level
        mqttClient.publish(TOPIC, message);

        System.out.println("Published ticket info (JSON): " + json);
    }

    public void disconnect() throws MqttException {
        mqttClient.disconnect();
    }
}
