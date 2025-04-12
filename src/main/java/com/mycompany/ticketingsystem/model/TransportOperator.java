package com.mycompany.ticketingsystem.model;

public class TransportOperator {
    private String operatorID;
    private String name;
    private String contactInfo;
    private String fleetDetails;
    private String operatingRoutes; // For simplicity, a comma-separated string

    public TransportOperator(String operatorID, String name, String contactInfo, String fleetDetails, String operatingRoutes) {
        this.operatorID = operatorID;
        this.name = name;
        this.contactInfo = contactInfo;
        this.fleetDetails = fleetDetails;
        this.operatingRoutes = operatingRoutes;
    }

    public void updateServiceStatus(String route, String status) {
        System.out.println("Route " + route + " updated with status: " + status);
    }

    public boolean validateTicket(Ticket ticket) {
        // Dummy validation: check if the ticket is still valid based on a sample date
        return ticket.isValid("2025-04-30");
    }

    public void recordValidation(TicketValidationRecord record) {
        System.out.println("Validation record " + record.getRecordID() + " recorded.");
    }

    // Getters and Setters
    public String getOperatorID() { return operatorID; }
    public void setOperatorID(String operatorID) { this.operatorID = operatorID; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    public String getFleetDetails() { return fleetDetails; }
    public void setFleetDetails(String fleetDetails) { this.fleetDetails = fleetDetails; }

    public String getOperatingRoutes() { return operatingRoutes; }
    public void setOperatingRoutes(String operatingRoutes) { this.operatingRoutes = operatingRoutes; }
}
