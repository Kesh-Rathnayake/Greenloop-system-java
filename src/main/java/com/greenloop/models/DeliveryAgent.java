package com.greenloop.models;

public class DeliveryAgent {
    private int agentId;
    private String name;
    private String vehicleType;
    private String serviceArea;
    private String phone;
    private String email;

    public DeliveryAgent(int agentId, String name, String vehicleType, String phone, String serviceArea, String email) {
        this.agentId = agentId;
        this.name = name;
        this.vehicleType = vehicleType;
        this.phone = phone;
        this.serviceArea = serviceArea;
        this.email = email;
    }

    public int getAgentId() {
        return agentId;
    }

    public void setAgentId(int agentId) {
        this.agentId = agentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getServiceArea() {
        return serviceArea;
    }

    public void setServiceArea(String serviceArea) {
        this.serviceArea = serviceArea;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
