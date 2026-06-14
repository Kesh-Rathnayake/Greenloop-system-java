package com.greenloop.models;

public class EmailNotification {
    private int orderId;
    private int clientId;
    private String recipientEmail;
    private String subject;
    private String message;
    private String sentStatus;

    public EmailNotification(int orderId, int clientId, String recipientEmail,
                             String subject, String message, String sentStatus) {
        this.orderId = orderId;
        this.clientId = clientId;
        this.recipientEmail = recipientEmail;
        this.subject = subject;
        this.message = message;
        this.sentStatus = sentStatus;
    }

    public int getOrderId() {
        return orderId;
    }

    public int getClientId() {
        return clientId;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public String getSubject() {
        return subject;
    }

    public String getMessage() {
        return message;
    }

    public String getSentStatus() {
        return sentStatus;
    }
}