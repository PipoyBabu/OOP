/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package bagtas.carparkmanagementsystem;

/**
 * Thrown when a vehicle's height exceeds the allowed clearance.
 */
public class InvalidVehicleHeightException extends RuntimeException {
    public InvalidVehicleHeightException(String message) { super(message); }
    public InvalidVehicleHeightException(String message, Throwable cause) { super(message, cause); }
}
