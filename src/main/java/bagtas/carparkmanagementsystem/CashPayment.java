/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bagtas.carparkmanagementsystem;

/**
 *
 * @author unit 1
 */

// Cash payment implementation
public class CashPayment extends Payment {
    public CashPayment(double amount, double cashGiven) {
        super(amount, cashGiven);
    }

    @Override
    public PaymentResult process() {
        String ref = makeRef();
        double amount = getAmount();
        double cash = getCashGiven();
        if (cash < amount) {
            return new PaymentResult(false, ref, 0.0, "Insufficient cash");
        }
        double change = roundToCent(cash - amount);
        return new PaymentResult(true, ref, change, "Cash accepted");
    }
}
