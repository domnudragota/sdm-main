package com.mycompany.ticketingsystem.model;

import java.util.ArrayList;
import java.util.List;

public class PassengerProfile {
    private String profileID;
    private String passengerID;
    private List<String> travelHistory; // In a real app, this might be a list of JourneyPlan objects.

    public PassengerProfile(String profileID, String passengerID) {
        this.profileID = profileID;
        this.passengerID = passengerID;
        this.travelHistory = new ArrayList<>();
    }

    public void addTravelRecord(String record) {
        travelHistory.add(record);
    }

    // Getters and Setters
    public String getProfileID() { return profileID; }
    public void setProfileID(String profileID) { this.profileID = profileID; }

    public String getPassengerID() { return passengerID; }
    public void setPassengerID(String passengerID) { this.passengerID = passengerID; }

    public List<String> getTravelHistory() { return travelHistory; }
}
