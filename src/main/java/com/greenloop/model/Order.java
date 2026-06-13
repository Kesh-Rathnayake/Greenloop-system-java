package com.greenloop.model;

public class Order {
    private int orderId;
    private String orderCode;
    private Client client;
    private double amount;
    private String status;
    private String address;

    public Order(int orderId, String orderCode, Client client, double amount, String status, String address) {
        this.orderId = orderId;
        this.orderCode = orderCode;
        this.client = client;
        this.amount = amount;
        this.status = status;
        this.address = address;
    }

    public int getOrderId() {
        return orderId;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public Client getClient() {
        return client;
    }

    public double getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public String getAddress() {
        return address;
    }
}
