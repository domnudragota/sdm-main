package com.mycompany.ticketingsystem.mqtt;

import com.google.gson.Gson;
import com.mycompany.ticketingsystem.model.Ticket;
import org.eclipse.paho.client.mqttv3.*;

import java.nio.charset.StandardCharsets;

public class MqttPublisher {
    private static final String BROKER_URL = "tcp://localhost:1883";
    private static final String CLIENT_ID  = "TicketSystemPublisher";

    private final MqttClient mqttClient;
    private final Gson gson = new Gson();

    public MqttPublisher() throws MqttException {
        this.mqttClient = new MqttClient(BROKER_URL, CLIENT_ID);
        MqttConnectOptions opts = new MqttConnectOptions();
        opts.setCleanSession(true);
        this.mqttClient.connect(opts);
    }

    /**
     * Serialize the Ticket to JSON and publish it on the "ticket/issued" topic.
     */
    public void publishTicketInfo(Ticket ticket) throws MqttException {
        String json = gson.toJson(ticket);
        MqttMessage msg = new MqttMessage(json.getBytes(StandardCharsets.UTF_8));
        msg.setQos(1);
        // retain the last issued-ticket so late subscribers still receive it
        msg.setRetained(true);
        mqttClient.publish(Topics.TICKET_ISSUED, msg);
        System.out.println("Published ticket info: " + json);
    }

    public void disconnect() throws MqttException {
        mqttClient.disconnect();
    }
}
