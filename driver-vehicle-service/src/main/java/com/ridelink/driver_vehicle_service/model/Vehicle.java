package com.ridelink.driver_vehicle_service.model;

public class Vehicle {

    private String vehicleNumber;
    private String type;
    private String model;
    private String color;
    private int capacity;

    public Vehicle() {}

    public Vehicle(String vehicleNumber, String type, String model, String color, int capacity) {
        this.vehicleNumber = vehicleNumber;
        this.type = type;
        this.model = model;
        this.color = color;
        this.capacity = capacity;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
}