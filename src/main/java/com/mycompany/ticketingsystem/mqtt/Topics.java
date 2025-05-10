package com.mycompany.ticketingsystem.mqtt;

public final class Topics {
    private Topics() {}

    public static final String TICKET_ISSUED         = "ticket/issued";
    public static final String TICKET_VALIDATION     = "ticket/validation";
    public static final String PAYMENT_REQUEST       = "payment/request";
    public static final String PAYMENT_STATUS        = "payment/status";
    public static final String PAYMENT_COMPLETE      = "payment/complete";
    public static final String PAYMENT_FAILED        = "payment/failed";
    public static final String JOURNEY_PLAN_REQUEST  = "journey/plan/request";
    public static final String JOURNEY_PLAN          = "journey/plan";
    public static final String SCHEDULE_UPDATE       = "schedule/update";
}
