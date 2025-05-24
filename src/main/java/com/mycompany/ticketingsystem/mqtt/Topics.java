package com.mycompany.ticketingsystem.mqtt;

/** Central list of MQTT topics + helpers for per-user channels. */
public final class Topics {
    private Topics() {}

    /* -------- global (operator-wide) topics -------- */
    public static final String TICKET_ISSUED        = "ticket/issued";
    public static final String TICKET_VALIDATION    = "ticket/validation";
    public static final String PAYMENT_REQUEST      = "payment/request";
    public static final String PAYMENT_STATUS       = "payment/status";
    public static final String PAYMENT_COMPLETE     = "payment/complete";
    public static final String PAYMENT_FAILED       = "payment/failed";
    public static final String JOURNEY_PLAN_REQUEST = "journey/plan/request";
    public static final String JOURNEY_PLAN         = "journey/plan";
    public static final String SCHEDULE_UPDATE      = "schedule/update";

    /* -------- per-passenger helpers -------- */

    /** A passenger’s personal ticket feed: ticket/issued/{uid} */
    public static String ticketIssued(String uid) {
        return TICKET_ISSUED + "/" + uid;
    }

    /** Journey plan for one passenger: journey/plan/{uid} */
    public static String journeyPlan(String uid) {
        return JOURNEY_PLAN + "/" + uid;
    }
}
