package com.greenloop.models;

public class Order {
    private int orderId;
    private int clientId;
    private String clientName;
    private String clientEmail;
    private String clientPhone;
    private double amount;
    private String status;
    private String deliveryAddress;

    public Order(int orderId, int clientId, String clientName, String clientEmail,
                 String clientPhone, double amount, String status, String deliveryAddress) {
        this.orderId = orderId;
        this.clientId = clientId;
        this.clientName = clientName;
        this.clientEmail = clientEmail;
        this.clientPhone = clientPhone;
        this.amount = amount;
        this.status = status;
        this.deliveryAddress = deliveryAddress;
    }

    public int getOrderId() { return orderId; }
    public String getDisplayOrderId() { return "ORD-" + String.format("%03d", orderId); }
    public int getClientId() { return clientId; }
    public String getClientName() { return clientName; }
    public String getClientEmail() { return clientEmail; }
    public String getClientPhone() { return clientPhone; }
    public double getAmount() { return amount; }
    public String getStatus() { return status; }
    public String getDeliveryAddress() { return deliveryAddress; }
}