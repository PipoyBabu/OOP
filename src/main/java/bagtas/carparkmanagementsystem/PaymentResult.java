/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bagtas.carparkmanagementsystem;

/**
 *
 * @author unit 1
 */
// Immutable result for payment processing
public class PaymentResult {
    // whether payment succeeded
    private final boolean success;
    // payment reference id
    private final String referenceId;
    // change to return to customer (for cash payments)
    private final double change;
    // human-readable message or approval code
    private final String message;

    // constructor
    public PaymentResult(boolean success, String referenceId, double change, String message) {
        this.success = success;
        this.referenceId = referenceId;
        this.change = change;
        this.message = message;
    }

    // whether payment succeeded
    public boolean isSuccess() {
        return success;
    }

    // payment reference id
    public String getReferenceId() {
        return referenceId;
    }

    // change to return (cash)
    public double getChange() {
        return change;
    }

    // human-readable message
    public String getMessage() {
        return message;
    }

    // handy for logs and receipts
    @Override
    public String toString() {
        return "PaymentResult{success=" + success
            + ", referenceId='" + referenceId + '\''
            + ", change=" + change
            + ", message='" + message + '\''
            + '}';
    }
}
