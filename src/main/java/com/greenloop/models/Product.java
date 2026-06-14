package com.greenloop.models;

public class Product {
    private int id;
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getEcoRating() {
        return ecoRating;
    }

    public void setEcoRating(double ecoRating) {
        this.ecoRating = ecoRating;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private String type;
    private double price;
    private double ecoRating;
    private String description;

    public Product(int id, String name, String type, double price, String description, double ecoRating) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.price = price;
        this.description = description;
        this.ecoRating = ecoRating;
    }
}
