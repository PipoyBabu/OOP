/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bagtas.carparkmanagementsystem;

/**
 *
 * @author unit 1
 */
public abstract class Vehicle {
    private final String plateNumber;
    private final double height;
    private boolean pwdDriver;

    public Vehicle(String plateNumber, double height) {
        this.plateNumber = plateNumber;
        this.height = height;
        this.pwdDriver = false;
    }

    // optional full constructor
    public Vehicle(String plateNumber, double height, boolean pwdDriver) {
        this.plateNumber = plateNumber;
        this.height = height;
        this.pwdDriver = pwdDriver;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public double getHeight() {
        return height;
    }

    public boolean isPwdDriver() {
        return pwdDriver;
    }

    public void setPwdDriver(boolean pwdDriver) {
        this.pwdDriver = pwdDriver;
    }

    // Display token (human-friendly). Do not rely on exact string for control flow.
    public abstract String getType();

    // Pricing hooks
    public abstract double getBasePrice();
    public abstract int getBaseHours();
    public abstract double getRatePerHour();
    public abstract double getOvernightFee();
}
