package com.mycompany.ticketingsystem.model;

public class TicketValidationRecord {
    private String recordID;
    private String ticketOrCardID;
    private String validationDateTime;
    private String location;
    private String status; // e.g., "successful", "failed"

    public TicketValidationRecord(String recordID, String ticketOrCardID, String validationDateTime, String location, String status) {
        this.recordID = recordID;
        this.ticketOrCardID = ticketOrCardID;
        this.validationDateTime = validationDateTime;
        this.location = location;
        this.status = status;
    }

    // Getters and Setters
    public String getRecordID() { return recordID; }
    public void setRecordID(String recordID) { this.recordID = recordID; }

    public String getTicketOrCardID() { return ticketOrCardID; }
    public void setTicketOrCardID(String ticketOrCardID) { this.ticketOrCardID = ticketOrCardID; }

    public String getValidationDateTime() { return validationDateTime; }
    public void setValidationDateTime(String validationDateTime) { this.validationDateTime = validationDateTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
