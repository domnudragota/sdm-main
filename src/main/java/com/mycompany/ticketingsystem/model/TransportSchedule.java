package com.mycompany.ticketingsystem.model;

import java.util.List;

public class TransportSchedule {
    private String scheduleID;
    private String transportMode;
    private String route;
    private List<String> departureTimes;
    private List<String> arrivalTimes;

    public TransportSchedule(String scheduleID, String transportMode, String route, List<String> departureTimes, List<String> arrivalTimes) {
        this.scheduleID = scheduleID;
        this.transportMode = transportMode;
        this.route = route;
        this.departureTimes = departureTimes;
        this.arrivalTimes = arrivalTimes;
    }

    public void updateSchedule(List<String> newDepartureTimes, List<String> newArrivalTimes) {
        this.departureTimes = newDepartureTimes;
        this.arrivalTimes = newArrivalTimes;
    }

    public List<String> getTimetable() {
        // Simplified: return departure times as an example
        return departureTimes;
    }

    // Getters and Setters
    public String getScheduleID() { return scheduleID; }
    public void setScheduleID(String scheduleID) { this.scheduleID = scheduleID; }

    public String getTransportMode() { return transportMode; }
    public void setTransportMode(String transportMode) { this.transportMode = transportMode; }

    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }

    public List<String> getDepartureTimes() { return departureTimes; }
    public void setDepartureTimes(List<String> departureTimes) { this.departureTimes = departureTimes; }

    public List<String> getArrivalTimes() { return arrivalTimes; }
    public void setArrivalTimes(List<String> arrivalTimes) { this.arrivalTimes = arrivalTimes; }
}
