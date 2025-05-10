package com.mycompany.ticketingsystem.model;

public class PaymentTransaction {
    private String transactionID;
    private double amount;
    private String date;
    private String status;    // e.g., "approved", "declined", "pending"
    private String relatedID; // e.g., a ticketID or cardID

    /**
     * No-arg constructor required by Firestore for deserialization
     */
    public PaymentTransaction() {
    }

    public PaymentTransaction(String transactionID,
                              double amount,
                              String date,
                              String status,
                              String relatedID) {
        this.transactionID = transactionID;
        this.amount        = amount;
        this.date          = date;
        this.status        = status;
        this.relatedID     = relatedID;
    }

    // Dummy method to process payment
    public void processPayment() {
        // In a real system, this would involve external payment checks.
        this.status = "approved";
    }

    // Getters & Setters
    public String getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRelatedID() {
        return relatedID;
    }

    public void setRelatedID(String relatedID) {
        this.relatedID = relatedID;
    }
}
