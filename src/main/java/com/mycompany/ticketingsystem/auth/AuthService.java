package com.mycompany.ticketingsystem.auth;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.google.gson.JsonParser;
import com.mycompany.ticketingsystem.config.FirebaseConfig;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Wraps Firebase Auth and returns a real **ID-token** so callers can pass it
 * to HTTP (Bearer) and MQTT (password) while Mosquitto & the server verify it.
 */
public class AuthService {

    /* ── Your Web API key; keep it outside source control ─────────────── */
    // AuthService.java  – top of class
    private static final String API_KEY;

    static {
        String env = System.getenv("FIREBASE_API_KEY");      // real env var
        String prop = System.getProperty("FIREBASE_API_KEY"); // set by Dotenv
        API_KEY = (env != null && !env.isBlank()) ? env : prop;
    }


    private final FirebaseAuth auth = FirebaseConfig.getAuth();
    private final Firestore    db   = FirebaseConfig.getDb();

    /* ── Public API ───────────────────────────────────────────────────── */

    public String registerPassenger(String email, String pwd) throws Exception {
        return createUserAndIdToken(email, pwd, Role.PASSENGER);
    }

    public String registerOperator(String email, String pwd) throws Exception {
        return createUserAndIdToken(email, pwd, Role.OPERATOR);
    }

    /** Sign-in an existing account and return a fresh ID-token. */
    public String login(String email, String pwd) throws Exception {
        return exchangePasswordForIdToken(email, pwd);
    }

    /* ── Internals ────────────────────────────────────────────────────── */

    private String createUserAndIdToken(String email,
                                        String pwd,
                                        Role   role) throws Exception {

        UserRecord rec = auth.createUser(
                new UserRecord.CreateRequest().setEmail(email).setPassword(pwd));

        db.collection("users")
                .document(rec.getUid())
                .set(Map.of("uid", rec.getUid(), "role", role.name()), SetOptions.merge())
                .get();

        // Immediately sign-in to get an ID-token
        return exchangePasswordForIdToken(email, pwd);
    }

    /** Call Firebase’s REST API to swap email+password for an ID-token. */
    private String exchangePasswordForIdToken(String email,
                                              String pwd) throws Exception {

        if (API_KEY == null || API_KEY.isBlank()) {
            throw new IllegalStateException(
                    "FIREBASE_API_KEY env var missing.  Set it to your Web API key.");
        }

        URL url = new URL(
                "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + API_KEY);

        String body = """
            {
              "email":"%s",
              "password":"%s",
              "returnSecureToken":true
            }
            """.formatted(email, pwd);

        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setRequestMethod("POST");
        c.setRequestProperty("Content-Type", "application/json");
        c.setDoOutput(true);
        c.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));

        String json = new String(c.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return JsonParser.parseString(json)
                .getAsJsonObject()
                .get("idToken")          // ← real JWT ready for verifyIdToken
                .getAsString();
    }
}
