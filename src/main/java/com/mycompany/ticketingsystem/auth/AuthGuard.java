package com.mycompany.ticketingsystem.auth;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.cloud.firestore.Firestore;
import com.mycompany.ticketingsystem.config.FirebaseConfig;

/**
 * Central guard: verify the caller’s Firebase ID-token and ensure they have
 * the expected role.  Returns the UID so business logic can tag data rows.
 */
public final class AuthGuard {

    private static final FirebaseAuth fbAuth = FirebaseConfig.getAuth();
    private static final Firestore    db     = FirebaseConfig.getDb();

    private AuthGuard() {}

    /** Verify token + role; return UID for downstream business logic. */
    public static String require(String idToken, Role expected) throws Exception {

        /* 1️⃣  Verify signature & expiry */
        FirebaseToken decoded = fbAuth.verifyIdToken(idToken);
        String uid = decoded.getUid();

        /* 2️⃣  Get role – prefer custom claim, else Firestore */
        String roleStr = (String) decoded.getClaims().get("role");
        if (roleStr == null) {
            roleStr = db.collection("users")
                    .document(uid)
                    .get().get()
                    .getString("role");
        }

        if (roleStr == null) {
            throw new SecurityException("No role set for user " + uid);
        }

        Role actual = Role.valueOf(roleStr);
        if (actual != expected) {
            throw new SecurityException(
                    "Role " + actual + " cannot perform this action; expected " + expected);
        }
        return uid;          // handy for downstream logic
    }
}
