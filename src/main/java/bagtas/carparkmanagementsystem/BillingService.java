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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.LocalDate;
import java.time.LocalTime;

public class BillingService {
    private static final double PWD_DISCOUNT = 0.5;

    public double computeFee(Vehicle v, long entryTimeMillis, long exitTimeMillis) {
        if (v == null || entryTimeMillis <= 0L) return 0.0;
        long now = Instant.now().toEpochMilli();
        if (exitTimeMillis <= entryTimeMillis) exitTimeMillis = now;
        long durationMs = Math.max(0L, exitTimeMillis - entryTimeMillis);
        double durationHours = durationMs / 3_600_000.0;
        int fullDays = (int) (durationMs / 86_400_000L);
        int billedTotalHours = (int) Math.ceil(durationHours);

        double fee = v.getBasePrice();
        int baseHours = v.getBaseHours();
        if (billedTotalHours > baseHours) {
            int extra = billedTotalHours - baseHours;
            fee += extra * v.getRatePerHour();
        }
        // Count overnight windows (22:00 -> 10:00 next day) intersecting the stay
        int overnightCount = countOvernightWindows(entryTimeMillis, exitTimeMillis, ZoneId.systemDefault());
        if (overnightCount > 0) fee += overnightCount * v.getOvernightFee();
        if (v.isPwdDriver()) fee = fee * (1.0 - PWD_DISCOUNT);
        return Math.round(fee * 100.0) / 100.0;
    }

    // Count how many overnight windows (22:00 on day D -> 10:00 on day D+1) the time range intersects.
    private int countOvernightWindows(long entryMs, long exitMs, ZoneId zone) {
        if (exitMs <= entryMs) return 0;
        ZonedDateTime start = Instant.ofEpochMilli(entryMs).atZone(zone);
        ZonedDateTime end = Instant.ofEpochMilli(exitMs).atZone(zone);

        // iterate from the day before start to the day of end (covers windows starting before entry)
        LocalDate day = start.toLocalDate().minusDays(1);
        LocalDate last = end.toLocalDate();
        int nights = 0;
        while (!day.isAfter(last)) {
            ZonedDateTime windowStart = day.atTime(LocalTime.of(22, 0)).atZone(zone);
            ZonedDateTime windowEnd = day.plusDays(1).atTime(LocalTime.of(10, 0)).atZone(zone);
            boolean intersects = start.isBefore(windowEnd) && end.isAfter(windowStart);
            if (intersects) nights++;
            day = day.plusDays(1);
        }
        return nights;
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
