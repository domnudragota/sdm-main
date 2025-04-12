package com.mycompany.ticketingsystem.mqtt;

import org.eclipse.paho.client.mqttv3.*;

public class MqttSubscriber implements MqttCallback {

    private static final String BROKER_URL = "tcp://localhost:1883";
    private static final String CLIENT_ID = "TicketSystemSubscriber";
    private static final String TOPIC = "tickets/sold";
    private MqttClient mqttClient;

    public MqttSubscriber() throws MqttException {
        mqttClient = new MqttClient(BROKER_URL, CLIENT_ID);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        mqttClient.setCallback(this);
        mqttClient.connect(options);
        mqttClient.subscribe(TOPIC);
        System.out.println("Subscribed to topic: " + TOPIC);
    }

    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("Connection lost: " + cause.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        System.out.println("Received message on topic " + topic + ": " + new String(message.getPayload()));
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // Not used in Subscriber
    }
}
