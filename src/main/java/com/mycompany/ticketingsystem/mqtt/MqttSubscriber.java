package com.mycompany.ticketingsystem.mqtt;

import com.google.gson.Gson;
import com.mycompany.ticketingsystem.model.Ticket;
import com.mycompany.ticketingsystem.service.MessageService;
import org.eclipse.paho.client.mqttv3.*;

public class MqttSubscriber implements MqttCallback {
    private static final String BROKER_URL = "tcp://localhost:1883";
    private static final String CLIENT_ID  = "TicketSystemSubscriber";

    private final MqttClient mqttClient;
    private final Gson gson = new Gson();

    public MqttSubscriber() throws MqttException {
        this.mqttClient = new MqttClient(BROKER_URL, CLIENT_ID);
        MqttConnectOptions opts = new MqttConnectOptions();
        opts.setCleanSession(false);
        opts.setAutomaticReconnect(true);

        mqttClient.setCallback(this);
        mqttClient.connect(opts);

        // subscribe to all topics we care about
        mqttClient.subscribe(Topics.TICKET_ISSUE, 1);
        mqttClient.subscribe(Topics.TICKET_ISSUED, 1);
        mqttClient.subscribe(Topics.PAYMENT_STATUS, 1);
        mqttClient.subscribe(Topics.JOURNEY_PLAN, 1);

        System.out.println("Subscribed to topics:");
        System.out.println("  " + Topics.TICKET_ISSUE);
        System.out.println("  " + Topics.TICKET_ISSUED);
        System.out.println("  " + Topics.PAYMENT_STATUS);
        System.out.println("  " + Topics.JOURNEY_PLAN);
    }

    @Override
    public void connectionLost(Throwable cause) {
        System.err.println("MQTT connection lost: " + cause.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        try {
            String payload = new String(message.getPayload(), "UTF-8");
            System.out.println("Received on [" + topic + "]: " + payload);

            if (Topics.TICKET_ISSUED.equals(topic)) {
                Ticket ticket = gson.fromJson(payload, Ticket.class);
                MessageService.getInstance().addTicket(ticket);
            } else {
                System.out.println("Unhandled topic: " + topic);
            }
        } catch (Exception e) {
            System.err.println("Error processing MQTT message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // not used
    }
}
