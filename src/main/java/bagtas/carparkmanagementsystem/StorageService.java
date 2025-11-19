/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package bagtas.carparkmanagementsystem;

/**
 *
 * @author unit 1
 */
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Simple append-only text storage for transactions.
 * Writes one plain line per Transaction to a configured file.
 *
 * Line format:
 *  timestampMillis | plate | type | entryHuman | exitHuman | amount | method | reference
 *
 * Uses java.io only and wraps IO problems into StorageException.
 */
public class StorageService {
    private final File outFile;

    // Human-friendly timestamp formatter (system default zone)
    private static final DateTimeFormatter HUMAN_TS_FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    public StorageService(String path) {
        if (path == null || path.isEmpty()) throw new IllegalArgumentException("path required");
        this.outFile = new File(path);
        File parent = outFile.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();
    }

    /**
     * Append a transaction line. Throws StorageException on I/O failure.
     */
    public void appendTransaction(Transaction tx) throws StorageException {
        if (tx == null) throw new IllegalArgumentException("tx required");

        String line = buildLine(tx);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outFile, true))) {
            bw.write(line);
            bw.newLine();
            bw.flush();
        } catch (IOException ioe) {
            throw new StorageException("Failed to append transaction to " + outFile.getPath(), ioe);
        }
    }

    /**
     * Return configured path (useful for user messages).
     */
    public String getPath() {
        return outFile.getPath();
    }

    // Build a tolerant, human-friendly line using reflection fallbacks for common getters.
    private String buildLine(Transaction tx) {
        long now = System.currentTimeMillis();
        String plate = safe(getByReflection(tx, "getPlateNumber", "getPlate", "getPlateNo"));
        String type  = safe(getByReflection(tx, "getVehicleType", "getType"));
        long entryMs = safeLong(tx, "getEntryTime", "getEntryMillis", "getEntry");
        long exitMs  = safeLong(tx, "getExitTime",  "getExitMillis",  "getExit");
        double amt   = safeDouble(tx, "getAmount", "getFee", "getTotal");
        String method= safe(getByReflection(tx, "getMethod", "getPaymentMethod", "getMOP"));
        String ref   = safe(getByReflection(tx, "getReferenceId", "getReference", "getRef"));

        // convert epoch millis to human readable strings
        String entryHuman = epochToHuman(entryMs);
        String exitHuman  = epochToHuman(exitMs);

        // Minimal sanitization: replace pipe/newline to keep columns safe
        plate = sanitizeField(plate);
        type  = sanitizeField(type);
        method= sanitizeField(method);
        ref   = sanitizeField(ref);

        return String.format("%d | %s | %s | %s | %s | %.2f | %s | %s",
            now, plate, type, entryHuman, exitHuman, amt, method, ref);
    }

    // Convert epoch millis to human timestamp; return "N/A" for 0
    private String epochToHuman(long epochMillis) {
        if (epochMillis <= 0L) return "N/A";
        try {
            return HUMAN_TS_FMT.format(Instant.ofEpochMilli(epochMillis));
        } catch (Exception ex) {
            return String.valueOf(epochMillis);
        }
    }

    // Reflection helpers (non-fatal)
    private Object getByReflection(Transaction tx, String... names) {
        if (tx == null) return "";
        Class<?> cls = tx.getClass();
        for (String n : names) {
            try {
                java.lang.reflect.Method m = cls.getMethod(n);
                Object val = m.invoke(tx);
                if (val != null) return val;
            } catch (NoSuchMethodException nsme) {
                // try next
            } catch (Exception ex) {
                // ignore and try next
            }
        }
        return "";
    }

    private long safeLong(Transaction tx, String... names) {
        Object o = getByReflection(tx, names);
        if (o instanceof Number) return ((Number) o).longValue();
        if (o instanceof String) {
            try { return Long.parseLong(((String)o).trim()); } catch (Exception ignored) {}
        }
        return 0L;
    }

    private double safeDouble(Transaction tx, String... names) {
        Object o = getByReflection(tx, names);
        if (o instanceof Number) return ((Number) o).doubleValue();
        if (o instanceof String) {
            try { return Double.parseDouble(((String)o).trim()); } catch (Exception ignored) {}
        }
        return 0.0;
    }

    private String safe(Object o) {
        return o == null ? "" : o.toString();
    }

    private String sanitizeField(String s) {
        if (s == null) return "";
        return s.replace("|", " ").replace("\n", " ").replace("\r", " ").trim();
    }
}
