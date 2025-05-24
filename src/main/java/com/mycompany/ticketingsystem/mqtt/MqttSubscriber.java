package com.mycompany.ticketingsystem.mqtt;

import com.google.gson.Gson;
import com.mycompany.ticketingsystem.model.JourneyPlan;
import com.mycompany.ticketingsystem.model.PaymentTransaction;
import com.mycompany.ticketingsystem.model.Ticket;
import com.mycompany.ticketingsystem.service.JourneyPlanner;
import com.mycompany.ticketingsystem.service.MessageService;
import com.mycompany.ticketingsystem.service.PaymentService;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Listens to MQTT domain topics.
 * Sends the Firebase ID-token in the PASSWORD field so Mosquitto’s jwt-auth
 * plugin can authorise the connection.
 */
public class MqttSubscriber implements MqttCallback {

    private static final String BROKER_URL     = "tcp://localhost:1883";
    private static final String CLIENT_ID_BASE = "TicketSystemSubscriber";

    private final MqttClient      mqttClient;
    private final Gson            gson = new Gson();
    private final MqttPublisher   publisher;          // helper to echo / forward
    private final PaymentService  paymentSvc;
    private final JourneyPlanner  journeyPlanner;

    /* ───────── constructors ───────── */

    /** Production: give it the ID-token you got from /api/login. */
    public MqttSubscriber(String idToken) throws MqttException {
        this(idToken, BROKER_URL);
    }

    /** Overload for custom broker URL (tests). */
    public MqttSubscriber(String idToken, String brokerUrl) throws MqttException {

        String clientId = CLIENT_ID_BASE + "-" + UUID.randomUUID();
        mqttClient = new MqttClient(brokerUrl, clientId, new MemoryPersistence());

        MqttConnectOptions opts = new MqttConnectOptions();
        opts.setCleanSession(false);
        opts.setAutomaticReconnect(true);

        /* 🔑  JWT goes in PASSWORD; some plugins ignore USERNAME */
        opts.setPassword(idToken.toCharArray());
        opts.setUserName("jwt");

        mqttClient.setCallback(this);
        mqttClient.connect(opts);

        /* ---- subscriptions ---- */
        mqttClient.subscribe(Topics.TICKET_ISSUED,  1);
        mqttClient.subscribe(Topics.PAYMENT_STATUS, 1);
        mqttClient.subscribe(Topics.JOURNEY_PLAN,   1);

        System.out.println("Subscribed (with JWT) to:");
        System.out.println("  " + Topics.TICKET_ISSUED);
        System.out.println("  " + Topics.PAYMENT_STATUS);
        System.out.println("  " + Topics.JOURNEY_PLAN);

        /* ---- helper objects ---- */
        this.publisher      = new MqttPublisher(idToken);   // publishes with same JWT
        this.paymentSvc     = new PaymentService();         // updates don’t need token
        this.journeyPlanner = new JourneyPlanner();         // ditto
    }

    /** Dev-only shortcut when broker auth is disabled. */
    public MqttSubscriber() throws MqttException {
        this("dev-token");
    }

    /* ───────── MqttCallback impl ───────── */

    @Override
    public void connectionLost(Throwable cause) {
        System.err.println("MQTT connection lost: " + cause.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        try {
            String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
            System.out.println("Received [" + topic + "] " + payload);

            switch (topic) {
                case Topics.TICKET_ISSUED -> {
                    Ticket ticket = gson.fromJson(payload, Ticket.class);
                    MessageService.getInstance().addTicket(ticket);
                }
                case Topics.PAYMENT_STATUS -> {
                    PaymentTransaction txn = gson.fromJson(payload, PaymentTransaction.class);
                    paymentSvc.handleIncomingStatus(txn);
                }
                case Topics.JOURNEY_PLAN -> {
                    JourneyPlan plan = gson.fromJson(payload, JourneyPlan.class);
                    journeyPlanner.handleIncomingPlan(plan);
                }
                default -> System.err.println("Unhandled topic: " + topic);
            }
        } catch (Exception e) {
            System.err.println("Error processing MQTT msg: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        /* not used */
    }
}
