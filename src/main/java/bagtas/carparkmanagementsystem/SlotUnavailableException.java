/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bagtas.carparkmanagementsystem;

/**
 *
 * @author unit 1
 */

// Thrown when no appropriate parking slot can be allocated or parking failed. 
public class SlotUnavailableException extends RuntimeException {
    public SlotUnavailableException(String message) { super(message); }
    public SlotUnavailableException(String message, Throwable cause) { super(message, cause); }
}
