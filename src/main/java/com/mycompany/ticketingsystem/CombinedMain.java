package com.mycompany.ticketingsystem;

import com.mycompany.ticketingsystem.mqtt.MqttPublisher;
import com.mycompany.ticketingsystem.mqtt.MqttSubscriber;
import com.mycompany.ticketingsystem.web.WebServer;
import com.mycompany.ticketingsystem.model.Ticket;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.concurrent.TimeUnit;

public class CombinedMain {

    public static void main(String[] args) {
        // Start the web server in a separate thread
        Thread webServerThread = new Thread(() -> {
            // WebServer starts and listens on port 4567
            WebServer.main(new String[]{});
        });
        webServerThread.start();

        // Let the web server initialize (optional: wait a couple of seconds)
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Now start the MQTT publisher and subscriber in this same JVM.
        try {
            // Instantiate the MQTT subscriber (it will start listening on your shared MessageStore)
            MqttSubscriber subscriber = new MqttSubscriber();

            // Create the MQTT publisher
            MqttPublisher publisher = new MqttPublisher();

            // Publish a continuous stream of ticket events.
            for (int i = 1; i <= 10; i++) {
                // Create a new Ticket (each with a unique ID)
                Ticket ticket = new Ticket("TCKT" + i, "single-ride", 2.50, "2025-04-01", "2025-04-30");
                String ticketInfo = "Ticket ID: " + ticket.getTicketID() +
                        ", Type: " + ticket.getTicketType() +
                        ", Price: " + ticket.getPrice();

                // Publish the ticket information
                publisher.publishTicketInfo(ticketInfo);

                // Wait 5 seconds before sending the next ticket event.
                TimeUnit.SECONDS.sleep(5);
            }

            // Disconnect the publisher after publishing all messages.
            publisher.disconnect();

        } catch (MqttException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
