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
import java.time.format.DateTimeFormatter;

public final class ReceiptPrinter {
    private static final DateTimeFormatter TS_FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private static String fmt(long epochMs) {
        return epochMs <= 0 ? "-" : TS_FMT.format(Instant.ofEpochMilli(epochMs));
    }

    private static String fmtDuration(long ms) {
        long secs = ms / 1000;
        long hours = secs / 3600;
        long mins = (secs % 3600) / 60;
        long s = secs % 60;
        return String.format("%02dh %02dm %02ds", hours, mins, s);
    }

    // Count overnight windows intersecting the stay (22:00 -> 10:00 next day)
    private static int countOvernightWindows(long entryMs, long exitMs) {
        if (exitMs <= entryMs) return 0;
        java.time.ZoneId zone = java.time.ZoneId.systemDefault();
        java.time.ZonedDateTime start = java.time.Instant.ofEpochMilli(entryMs).atZone(zone);
        java.time.ZonedDateTime end = java.time.Instant.ofEpochMilli(exitMs).atZone(zone);
        java.time.LocalDate day = start.toLocalDate().minusDays(1);
        java.time.LocalDate last = end.toLocalDate();
        int nights = 0;
        while (!day.isAfter(last)) {
            java.time.ZonedDateTime windowStart = day.atTime(java.time.LocalTime.of(22, 0)).atZone(zone);
            java.time.ZonedDateTime windowEnd = day.plusDays(1).atTime(java.time.LocalTime.of(10, 0)).atZone(zone);
            boolean intersects = start.isBefore(windowEnd) && end.isAfter(windowStart);
            if (intersects) nights++;
            day = day.plusDays(1);
        }
        return nights;
    }

    public static String renderReceipt(Transaction tx, Vehicle v, PaymentResult pr) {
        StringBuilder sb = new StringBuilder();
        sb.append("MULTI-LEVEL PARKING MANAGEMENT\n");
        sb.append("Receipt: ").append(tx.getId()).append("\n");
        sb.append("Printed: ").append(fmt(tx.getExitTime())).append("\n");
        sb.append("----------------------------------------\n");
        sb.append("Plate: ").append(tx.getPlate()).append("\n");
        sb.append("Vehicle: ").append(tx.getVehicleType()).append(v.isPwdDriver() ? " (PWD)" : "").append("\n");
        sb.append("Entry: ").append(fmt(tx.getEntryTime())).append("\n");
        sb.append("Exit : ").append(fmt(tx.getExitTime())).append("\n");
        sb.append("Duration: ").append(fmtDuration(tx.getDurationMs())).append("\n");
        sb.append("----------------------------------------\n");

        double base = v.getBasePrice();
        int baseH = v.getBaseHours();
        double rate = v.getRatePerHour();
        long durationMs = tx.getDurationMs();
        int billedHours = (int) Math.ceil(durationMs / 3_600_000.0);
        int extraHours = Math.max(0, billedHours - baseH);
        double extraCharge = extraHours * rate;
        int overnightCount = countOvernightWindows(tx.getEntryTime(), tx.getExitTime());
        double overnight = overnightCount * v.getOvernightFee();

        sb.append(String.format("Base (%dh): %,.2f\n", baseH, base));
        if (extraHours > 0) sb.append(String.format("Extra hours (%d @ %,.2f): %,.2f\n", extraHours, rate, extraCharge));
        if (overnight > 0) sb.append(String.format("Overnight (%d nights): %,.2f\n", overnightCount, overnight));

        double subtotal = base + extraCharge + overnight;
        sb.append(String.format("Subtotal: %,.2f\n", subtotal));

        if (v.isPwdDriver()) {
            double discounted = subtotal - tx.getFee();
            sb.append(String.format("PWD discount: -%,.2f\n", discounted));
        }

        sb.append(String.format("TOTAL: %,.2f\n", tx.getFee()));
        sb.append("----------------------------------------\n");

        String mop = tx.getModeOfPayment();
        sb.append("Payment method: ").append(mop).append("\n");
        sb.append("Payment ref: ").append(pr.getReferenceId()).append("\n");
        if ("CASH".equalsIgnoreCase(mop)) {
            sb.append(String.format("Cash given: %,.2f\n", pr.getChange() + tx.getFee()));
            sb.append(String.format("Change: %,.2f\n", pr.getChange()));
        }
        sb.append("Status: ").append(pr.isSuccess() ? "PAID" : "FAILED").append("\n");
        sb.append("----------------------------------------\n");
        sb.append("Thank you. Please keep this receipt for inquiries.\n");
        return sb.toString();
    }
}
