package com.mycompany.ticketingsystem.mqtt;

import com.google.gson.Gson;
import com.mycompany.ticketingsystem.model.Ticket;
import com.mycompany.ticketingsystem.service.MessageService;
import org.eclipse.paho.client.mqttv3.*;

public class MqttSubscriber implements MqttCallback {

    private static final String BROKER_URL = "tcp://localhost:1883";
    private static final String CLIENT_ID = "TicketSystemSubscriber";
    private static final String TOPIC = "tickets/sold";

    private final MqttClient mqttClient;
    private final Gson gson = new Gson();

    public MqttSubscriber() throws MqttException {
        mqttClient = new MqttClient(BROKER_URL, CLIENT_ID);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(false);            // keep subscriptions across reconnects
        options.setAutomaticReconnect(true);       // auto-reconnect on connection loss
        mqttClient.setCallback(this);
        mqttClient.connect(options);
        mqttClient.subscribe(TOPIC);
        System.out.println("Subscribed to topic: " + TOPIC);
    }

    @Override
    public void connectionLost(Throwable cause) {
        System.err.println("MQTT connection lost: " + cause.getMessage());
        // automaticReconnect=true and cleanSession=false will handle reconnection
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        // Wrap entire logic to prevent exceptions from escaping and blocking the callback
        try {
            String payload = new String(message.getPayload(), "UTF-8");
            System.out.println("Received message on topic [" + topic + "]: " + payload);

            // Deserialize JSON into a Ticket object
            Ticket ticket = gson.fromJson(payload, Ticket.class);

            // Persist the ticket asynchronously via MessageService
            MessageService.getInstance().addTicket(ticket);
        } catch (Exception e) {
            // Log and swallow any error so the MQTT client thread remains healthy
            System.err.println("Error processing incoming MQTT message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // Not used by subscriber
    }
}
