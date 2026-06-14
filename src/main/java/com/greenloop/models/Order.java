package com.greenloop.models;

import java.sql.Date;
import java.sql.Timestamp;

public class Order {
    private int orderId;
    private int clientId;
    private double amount;
    private String status;
    private String deliveryAddress;
    private int deliveryAgentId;
    private Timestamp scheduledDatetime;
    private Date actualDeliveryDate;
    private String delayReason;
    private Timestamp createdAt;

    public Order(int orderId, int clientId, double amount, String status, String deliveryAddress,
                 int deliveryAgentId, Timestamp scheduledDatetime, Date actualDeliveryDate,
                 String delayReason, Timestamp createdAt) {
        this.orderId = orderId;
        this.clientId = clientId;
        this.amount = amount;
        this.status = status;
        this.deliveryAddress = deliveryAddress;
        this.deliveryAgentId = deliveryAgentId;
        this.scheduledDatetime = scheduledDatetime;
        this.actualDeliveryDate = actualDeliveryDate;
        this.delayReason = delayReason;
        this.createdAt = createdAt;
    }

    // Getters
    public int getOrderId() { return orderId; }
    public int getClientId() { return clientId; }
    public double getAmount() { return amount; }
    public String getStatus() { return status; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public int getDeliveryAgentId() { return deliveryAgentId; }
    public Timestamp getScheduledDatetime() { return scheduledDatetime; }
    public Date getActualDeliveryDate() { return actualDeliveryDate; }
    public String getDelayReason() { return delayReason; }
    public Timestamp getCreatedAt() { return createdAt; }
}