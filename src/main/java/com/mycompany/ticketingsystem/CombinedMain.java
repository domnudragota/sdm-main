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
            try {
                WebServer.main(new String[]{});
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        webServerThread.start();

        // Let the web server initialize
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            // Start the MQTT subscriber
            MqttSubscriber subscriber = new MqttSubscriber();

            // Create the MQTT publisher
            MqttPublisher publisher = new MqttPublisher();

            // Publish a continuous stream of ticket events.
            for (int i = 1; i <= 10; i++) {
                Ticket ticket = new Ticket(
                        "TCKT" + i,
                        "single-ride",
                        2.50,
                        "2025-04-01",
                        "2025-04-30"
                );

                // Now passing the Ticket object instead of a String
                publisher.publishTicketInfo(ticket);

                TimeUnit.SECONDS.sleep(5);
            }

            publisher.disconnect();

        } catch (MqttException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
