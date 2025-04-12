package com.mycompany.ticketingsystem.service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MessageService {
    // Use a thread-safe list (CopyOnWriteArrayList) for concurrent access
    private List<String> messages = new CopyOnWriteArrayList<>();

    // Singleton instance
    private static MessageService instance;

    // Private constructor prevents instantiation from other classes
    private MessageService() {}

    // Double-checked locking for thread-safe singleton access
    public static MessageService getInstance() {
        if (instance == null) {
            synchronized (MessageService.class) {
                if (instance == null) {
                    instance = new MessageService();
                }
            }
        }
        return instance;
    }

    public void addMessage(String message) {
        messages.add(message);
    }

    public List<String> getMessages() {
        return messages;
    }
}
