package com.mycompany.ticketingsystem.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttPublisher {

    private static final String BROKER_URL = "tcp://localhost:1883";
    private static final String CLIENT_ID = "TicketSystemPublisher";
    private static final String TOPIC = "tickets/sold";
    private MqttClient mqttClient;

    public MqttPublisher() throws MqttException {
        mqttClient = new MqttClient(BROKER_URL, CLIENT_ID);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        mqttClient.connect(options);
    }

    public void publishTicketInfo(String ticketData) throws MqttException {
        MqttMessage message = new MqttMessage(ticketData.getBytes());
        message.setQos(1); // Quality of Service level
        mqttClient.publish(TOPIC, message);
        System.out.println("Published ticket info: " + ticketData);
    }

    public void disconnect() throws MqttException {
        mqttClient.disconnect();
    }
}
