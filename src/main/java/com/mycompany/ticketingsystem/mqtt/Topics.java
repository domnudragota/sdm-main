package com.mycompany.ticketingsystem.mqtt;

public final class Topics {
    public static final String TICKET_ISSUE    = "ticket/issue";
    public static final String TICKET_ISSUED   = "ticket/issued";
    public static final String PAYMENT_REQUEST = "payment/request";
    public static final String PAYMENT_STATUS  = "payment/status";
    public static final String SCHEDULE_UPDATE = "schedule/update";
    public static final String JOURNEY_PLAN    = "journey/plan";

    private Topics() {} // no instances
}
