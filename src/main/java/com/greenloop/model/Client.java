package com.greenloop.model;

public class Client {
    private int clientId;
    private String clientName;
    private String email;
    private String phone;

    public Client(int clientId, String clientName, String email, String phone) {
        this.clientId = clientId;
        this.clientName = clientName;
        this.email = email;
        this.phone = phone;
    }

    public int getClientId() {
        return clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }
}