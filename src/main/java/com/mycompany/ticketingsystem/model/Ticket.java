package com.mycompany.ticketingsystem.model;

public class Ticket {
    private String ticketID;
    private String ticketType;
    private double price;
    private String issueDate;
    private String expirationDate;

    public Ticket(String ticketID, String ticketType, double price, String issueDate, String expirationDate) {
        this.ticketID = ticketID;
        this.ticketType = ticketType;
        this.price = price;
        this.issueDate = issueDate;
        this.expirationDate = expirationDate;
    }

    // A simple validity check (for illustration purposes)
    public boolean isValid(String currentDate) {
        // In a real implementation, parse dates properly.
        return currentDate.compareTo(expirationDate) < 0;
    }

    // Getters and Setters
    public String getTicketID() { return ticketID; }
    public void setTicketID(String ticketID) { this.ticketID = ticketID; }

    public String getTicketType() { return ticketType; }
    public void setTicketType(String ticketType) { this.ticketType = ticketType; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getIssueDate() { return issueDate; }
    public void setIssueDate(String issueDate) { this.issueDate = issueDate; }

    public String getExpirationDate() { return expirationDate; }
    public void setExpirationDate(String expirationDate) { this.expirationDate = expirationDate; }
}
