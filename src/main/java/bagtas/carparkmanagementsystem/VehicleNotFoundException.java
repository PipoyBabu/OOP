package bagtas.carparkmanagementsystem;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author unit 1
 */

// Thrown when an operation expects a parked vehicle for the given plate but none exists. 
public class VehicleNotFoundException extends Exception {
    public VehicleNotFoundException(String message) { super(message); }
    public VehicleNotFoundException(String message, Throwable cause) { super(message, cause); }
}