package org.example.Models;

public class Agent {
    private int id;
    private String name;
    private String vehicleType;
    private String serviceArea;
    private String phone;
    private int currentOrders;

    public Agent(int id, String name, String vehicleType, String serviceArea, String phone, int currentOrders) {
        this.id = id;
        this.name = name;
        this.vehicleType = vehicleType;
        this.serviceArea = serviceArea;
        this.phone = phone;
        this.currentOrders = currentOrders;
    }

    // getters and setters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public String getServiceArea() {
        return serviceArea;
    }

    public String getPhone() {
        return phone;
    }

    public int getCurrentOrders() {
        return currentOrders;
    }

    @Override
    public String toString() {
        return name + " (" + vehicleType + ", " + serviceArea + ")";
    }
}