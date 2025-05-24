package com.mycompany.ticketingsystem.auth;

public class User {
    private String uid;
    private Role   role;

    // Firestore needs a no-arg ctor
    public User() {}

    public User(String uid, Role role) {
        this.uid  = uid;
        this.role = role;
    }

    public String getUid()  { return uid;  }
    public Role   getRole() { return role; }
}
