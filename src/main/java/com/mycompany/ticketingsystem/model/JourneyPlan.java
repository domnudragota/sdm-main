package com.mycompany.ticketingsystem.model;

public class JourneyPlan {
    private String planID;
    private String passengerID;
    private String routeDetails;
    private String estimatedTravelTime;
    private String departureTime;
    private String arrivalTime;

    /**
     * No-arg constructor required by Firestore for deserialization
     */
    public JourneyPlan() {
    }

    public JourneyPlan(String planID,
                       String passengerID,
                       String routeDetails,
                       String estimatedTravelTime,
                       String departureTime,
                       String arrivalTime) {
        this.planID             = planID;
        this.passengerID        = passengerID;
        this.routeDetails       = routeDetails;
        this.estimatedTravelTime = estimatedTravelTime;
        this.departureTime      = departureTime;
        this.arrivalTime        = arrivalTime;
    }

    public void updatePlan(String newRouteDetails,
                           String newDepartureTime,
                           String newArrivalTime) {
        this.routeDetails  = newRouteDetails;
        this.departureTime = newDepartureTime;
        this.arrivalTime   = newArrivalTime;
    }

    // Getters & Setters
    public String getPlanID() {
        return planID;
    }

    public void setPlanID(String planID) {
        this.planID = planID;
    }

    public String getPassengerID() {
        return passengerID;
    }

    public void setPassengerID(String passengerID) {
        this.passengerID = passengerID;
    }

    public String getRouteDetails() {
        return routeDetails;
    }

    public void setRouteDetails(String routeDetails) {
        this.routeDetails = routeDetails;
    }

    public String getEstimatedTravelTime() {
        return estimatedTravelTime;
    }

    public void setEstimatedTravelTime(String estimatedTravelTime) {
        this.estimatedTravelTime = estimatedTravelTime;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }
}
