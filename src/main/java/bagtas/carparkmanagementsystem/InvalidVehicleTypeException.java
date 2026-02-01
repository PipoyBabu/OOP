/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bagtas.carparkmanagementsystem;

/**
 *
 * @author unit 1
 */

// Thrown when an unsupported vehicle type is encountered. 
public class InvalidVehicleTypeException extends RuntimeException {
    public InvalidVehicleTypeException(String message) { super(message); }
    public InvalidVehicleTypeException(String message, Throwable cause) { super(message, cause); }
}
