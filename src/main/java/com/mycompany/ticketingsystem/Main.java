package com.mycompany.ticketingsystem;

import com.mycompany.ticketingsystem.model.Ticket;
import com.mycompany.ticketingsystem.mqtt.MqttPublisher;
import com.mycompany.ticketingsystem.mqtt.MqttSubscriber;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        try {
            // Start the subscriber so it continuously listens to the topic.
            MqttSubscriber subscriber = new MqttSubscriber();

            // Create the MQTT publisher.
            MqttPublisher publisher = new MqttPublisher();

            // Simulate a continuous stream of ticket purchase events.
            // For this example, we publish 10 messages with a 5-second interval.
            for (int i = 1; i <= 10; i++) {
                // Create a new Ticket with a unique ID for each iteration.
                Ticket ticket = new Ticket("TCKT" + i, "single-ride", 2.50, "2025-04-01", "2025-04-30");
                String ticketInfo = "Ticket ID: " + ticket.getTicketID() +
                        ", Type: " + ticket.getTicketType() +
                        ", Price: " + ticket.getPrice();

                // Publish the ticket info to the MQTT topic.
                publisher.publishTicketInfo(ticketInfo);

                // Wait for 5 seconds before publishing the next ticket.
                TimeUnit.SECONDS.sleep(5);
            }

            // After publishing all messages, disconnect the publisher.
            publisher.disconnect();

        } catch (MqttException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
