package com.greenloop.models;

public class Client {
    private int clientId;
    private String clientName;
    private String email;
    private String phone;
    private String address;

    public Client(int clientId, String clientName, String email, String phone, String address) {
        this.clientId = clientId;
        this.clientName = clientName;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}