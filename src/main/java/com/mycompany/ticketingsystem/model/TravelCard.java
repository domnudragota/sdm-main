package com.mycompany.ticketingsystem.model;

public class TravelCard {
    private String cardID;
    private String passengerID;
    private double balance;
    private String expirationDate;

    public TravelCard(String cardID, String passengerID, double balance, String expirationDate) {
        this.cardID = cardID;
        this.passengerID = passengerID;
        this.balance = balance;
        this.expirationDate = expirationDate;
    }

    public void reload(double amount) {
        balance += amount;
    }

    public boolean deductFare(double amount) {
        if (balance >= amount) {
            balance -= amount;
            return true;
        }
        return false;
    }

    // Getters and Setters
    public String getCardID() { return cardID; }
    public void setCardID(String cardID) { this.cardID = cardID; }

    public String getPassengerID() { return passengerID; }
    public void setPassengerID(String passengerID) { this.passengerID = passengerID; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public String getExpirationDate() { return expirationDate; }
    public void setExpirationDate(String expirationDate) { this.expirationDate = expirationDate; }
}
