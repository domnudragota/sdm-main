package com.mycompany.ticketingsystem;

import com.mycompany.ticketingsystem.model.Ticket;
import com.mycompany.ticketingsystem.mqtt.MqttPublisher;
import com.mycompany.ticketingsystem.mqtt.MqttSubscriber;
import org.eclipse.paho.client.mqttv3.MqttException;

public class Main {
    public static void main(String[] args) {
        try {
            // Start the subscriber to listen on the ticket topic (optional)
            MqttSubscriber subscriber = new MqttSubscriber();

            // Create the MQTT publisher
            MqttPublisher publisher = new MqttPublisher();

            // Create an example Ticket
            Ticket ticket = new Ticket("TCKT123", "single-ride", 2.50, "2025-04-01", "2025-04-30");
            String ticketInfo = "Ticket ID: " + ticket.getTicketID() +
                    ", Type: " + ticket.getTicketType() +
                    ", Price: " + ticket.getPrice();

            // Publish the ticket info
            publisher.publishTicketInfo(ticketInfo);

            // Disconnect the publisher
            publisher.disconnect();

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
