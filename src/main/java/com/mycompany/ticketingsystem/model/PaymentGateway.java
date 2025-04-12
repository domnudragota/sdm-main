package com.mycompany.ticketingsystem.model;

public class PaymentGateway {
    private String gatewayID;
    private String name;

    public PaymentGateway(String gatewayID, String name) {
        this.gatewayID = gatewayID;
        this.name = name;
    }

    // Process a payment transaction
    public boolean processPaymentTransaction(PaymentTransaction transaction) {
        transaction.processPayment();
        return transaction.getStatus().equalsIgnoreCase("approved");
    }

    // Getters and Setters
    public String getGatewayID() { return gatewayID; }
    public void setGatewayID(String gatewayID) { this.gatewayID = gatewayID; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
