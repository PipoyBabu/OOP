/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bagtas.carparkmanagementsystem;

/**
 *
 * @author unit 1
 */
public class Car extends Vehicle {

    // Constructor for non-PWD car
    public Car(String plateNumber, double height) {
        super(plateNumber, height);
    }

    // Constructor allowing PWD flag
    public Car(String plateNumber, double height, boolean pwdDriver) {
        super(plateNumber, height, pwdDriver);
    }

    @Override
    public String getType() {
        return "Car";
    }

    @Override
    public double getBasePrice() {
        return 50.0;
    }

    @Override
    public int getBaseHours() {
        return 3;
    }

    @Override
    public double getRatePerHour() {
        return 20.0;
    }

    @Override
    public double getOvernightFee() {
        return 300.0;
    }
}