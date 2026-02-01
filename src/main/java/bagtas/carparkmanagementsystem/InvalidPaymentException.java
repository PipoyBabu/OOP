/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bagtas.carparkmanagementsystem;

/**
 *
 * @author unit 1
 */

// Thrown for invalid payment inputs (pre-checks). 
public class InvalidPaymentException extends RuntimeException {
    public InvalidPaymentException(String message) { 
        super(message); 
    }
    public InvalidPaymentException(String message, Throwable cause) { 
        super(message, cause); 
    }
}