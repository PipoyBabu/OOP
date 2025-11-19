/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bagtas.carparkmanagementsystem;

/**
 *
 * @author unit 1
 */

import java.time.Instant;

public class BillingService {
    private static final double PWD_DISCOUNT = 0.5;

    public double computeFee(Vehicle v, long entryTimeMillis, long exitTimeMillis) {
        if (v == null || entryTimeMillis <= 0L || exitTimeMillis <= entryTimeMillis) return 0.0;
        long durationMs = exitTimeMillis - entryTimeMillis;
        double durationHours = durationMs / 3_600_000.0;
        int fullDays = (int) (durationMs / 86_400_000L);
        int billedTotalHours = (int) Math.ceil(durationHours);

        double fee = v.getBasePrice();
        int baseHours = v.getBaseHours();
        if (billedTotalHours > baseHours) {
            int extra = billedTotalHours - baseHours;
            fee += extra * v.getRatePerHour();
        }
        if (fullDays > 0) fee += fullDays * v.getOvernightFee();
        if (v.isPwdDriver()) fee = fee * (1.0 - PWD_DISCOUNT);
        return Math.round(fee * 100.0) / 100.0;
    }

    /**
     * Orchestrate compute + payment + transaction creation.
     * Caller must persist the returned Transaction (if result.isSuccess()) via StorageService.appendTransaction(t).
     *
     * @param v vehicle being paid for
     * @param entryTimeMillis entry timestamp
     * @param exitTimeMillis exit timestamp (use System.currentTimeMillis() or provided)
     * @param payment concrete Payment instance (CashPayment or CardPayment)
     * @return PaymentResult; on success, Transaction is returned via outTx[0]
     */
    public PaymentResult payAndCreateTransaction(Vehicle v, long entryTimeMillis, long exitTimeMillis, Payment payment, Transaction[] outTx) {
        double fee = computeFee(v, entryTimeMillis, exitTimeMillis);
        // Replace payment amount with computed fee if necessary (safer)
        Payment effective;
        if (payment instanceof CashPayment) {
            effective = new CashPayment(fee, payment.getCashGiven());
        } else if (payment instanceof CardPayment) {
            effective = new CardPayment(fee, payment.getCardNumber(), payment.getCardHolder());
        } else {
            // if other payment subclasses exist, assume they can be reconstructed similarly
            effective = payment;
        }

        PaymentResult result = effective.process();
        if (result.isSuccess()) {
            String plate = v.getPlateNumber();
            String vtype = v.getType();
            long exitTime = (exitTimeMillis <= entryTimeMillis) ? Instant.now().toEpochMilli() : exitTimeMillis;
            Transaction tx = new Transaction(plate, vtype, entryTimeMillis, exitTime, fee, effective instanceof CashPayment ? "CASH" : "CARD", result.getReferenceId());
            
            if (outTx != null && outTx.length > 0) {
                outTx[0] = tx;
            }
        } 
        else {
            if (outTx != null && outTx.length > 0) outTx[0] = null;
        }
        return result;
    }
}
