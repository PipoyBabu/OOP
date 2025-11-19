/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bagtas.carparkmanagementsystem;

/**
 *
 * @author unit 1
 */

import java.util.UUID;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Immutable transaction record used for logging and export.
 * CSV-friendly; toCsvLine does minimal escaping (commas removed from notes).
 */
public final class Transaction {
    private final String id;
    private final String plate;
    private final String vehicleType;
    private final long entryTime;
    private final long exitTime;
    private final long durationMs;
    private final double fee;
    private final String modeOfPayment;
    private final String notes;

    public Transaction(String plate, String vehicleType, long entryTime, long exitTime, double fee, String modeOfPayment, String notes) {
        this.id = UUID.randomUUID().toString();
        this.plate = plate == null ? "" : plate;
        this.vehicleType = vehicleType == null ? "" : vehicleType;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.durationMs = Math.max(0L, exitTime - entryTime);
        this.fee = fee;
        this.modeOfPayment = modeOfPayment == null ? "" : modeOfPayment.toUpperCase();
        this.notes = notes == null ? "" : notes;
    }

    public String getId() { return id; }
    public String getPlate() { return plate; }
    public String getVehicleType() { return vehicleType; }
    public long getEntryTime() { return entryTime; }
    public long getExitTime() { return exitTime; }
    public long getDurationMs() { return durationMs; }
    public double getFee() { return fee; }
    public String getModeOfPayment() { return modeOfPayment; }
    public String getNotes() { return notes; }

    public static String csvHeader() {
        return "transactionId,plate,vehicleType,entryTime,entryHuman,exitTime,exitHuman,durationMs,fee,modeOfPayment,notes";
    }

    public String toCsvLine() {
        String safeNotes = notes.replace("\n", " ").replace("\r", " ").replace(",", " ");
        // human-friendly timestamps (system default zone)
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
        String entryHuman = entryTime <= 0 ? "N/A" : fmt.format(Instant.ofEpochMilli(entryTime));
        String exitHuman = exitTime <= 0 ? "N/A" : fmt.format(Instant.ofEpochMilli(exitTime));
        return String.join(",",
            escape(id),
            escape(plate),
            escape(vehicleType),
            String.valueOf(entryTime),
            escape(entryHuman),
            String.valueOf(exitTime),
            escape(exitHuman),
            String.valueOf(durationMs),
            String.format("%.2f", fee),
            escape(modeOfPayment),
            escape(safeNotes)
        );
    }

    private String escape(String s) {
        if (s == null) return "";
        // strip newlines, trim
        return s.replace("\n", " ").replace("\r", " ").trim();
    }

    @Override
    public String toString() {
        return toCsvLine();
    }
}
