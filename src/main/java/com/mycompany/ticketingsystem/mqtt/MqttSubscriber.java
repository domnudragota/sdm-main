package com.mycompany.ticketingsystem.mqtt;

import com.google.gson.Gson;
import com.mycompany.ticketingsystem.model.JourneyPlan;
import com.mycompany.ticketingsystem.model.PaymentTransaction;
import com.mycompany.ticketingsystem.model.Ticket;
import com.mycompany.ticketingsystem.service.JourneyPlanner;
import com.mycompany.ticketingsystem.service.PaymentService;
import org.eclipse.paho.client.mqttv3.*;

public class MqttSubscriber implements MqttCallback {
    private static final String BROKER_URL = "tcp://localhost:1883";
    private static final String CLIENT_ID  = "TicketSystemSubscriber";

    private final MqttClient mqttClient;
    private final Gson gson = new Gson();
    private final MqttPublisher publisher;      // ← add publisher field
    private final PaymentService paymentSvc;    // ← add service fields
    private final JourneyPlanner journeyPlanner;

    public MqttSubscriber() throws MqttException {
        // initialize MQTT client
        this.mqttClient = new MqttClient(BROKER_URL, CLIENT_ID);
        MqttConnectOptions opts = new MqttConnectOptions();
        opts.setCleanSession(false);
        opts.setAutomaticReconnect(true);

        mqttClient.setCallback(this);
        mqttClient.connect(opts);

        // subscribe to topics
        mqttClient.subscribe(Topics.TICKET_ISSUED,    1);
        mqttClient.subscribe(Topics.PAYMENT_STATUS,   1);
        mqttClient.subscribe(Topics.JOURNEY_PLAN,     1);

        System.out.println("Subscribed to topics:");
        System.out.println("  " + Topics.TICKET_ISSUED);
        System.out.println("  " + Topics.PAYMENT_STATUS);
        System.out.println("  " + Topics.JOURNEY_PLAN);

        // initialize helper objects
        this.publisher       = new MqttPublisher();
        this.paymentSvc      = new PaymentService();
        this.journeyPlanner  = new JourneyPlanner();
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

            switch (topic) {
                case Topics.TICKET_ISSUED:
                    Ticket ticket = gson.fromJson(payload, Ticket.class);
                    // persist via your existing MessageService
                    com.mycompany.ticketingsystem.service.MessageService
                            .getInstance()
                            .addTicket(ticket);
                    break;

                case Topics.PAYMENT_STATUS:
                    PaymentTransaction txnUpdate = gson.fromJson(payload, PaymentTransaction.class);
                    // let PaymentService handle and persist
                    paymentSvc.handleIncomingStatus(txnUpdate);
                    break;

                case Topics.JOURNEY_PLAN:
                    JourneyPlan plan = gson.fromJson(payload, JourneyPlan.class);
                    // let JourneyPlanner handle and persist
                    journeyPlanner.handleIncomingPlan(plan);
                    break;

                default:
                    System.err.println("Unhandled topic: " + topic);
            }
        } catch (Exception e) {
            System.err.println("Error processing MQTT message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // not used in subscriber
    }
}
