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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
    private final Path outPath;
    private final Path receiptsDir;

    // Human-friendly timestamp formatter (system default zone)
    private static final DateTimeFormatter HUMAN_TS_FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    public StorageService(String path) {
        if (path == null || path.isEmpty()) throw new IllegalArgumentException("path required");
        this.outPath = Paths.get(path);
        Path parent = outPath.getParent();
        // Determine receipts directory. If the configured path looks like a file (ends with .txt),
        // create a sibling directory with the same base name (e.g., data/transactions.txt -> data/transactions/)
        if (outPath.getFileName().toString().toLowerCase().endsWith(".txt")) {
            String base = outPath.getFileName().toString();
            base = base.replaceFirst("\\.txt$", "");
            if (parent != null) {
                receiptsDir = parent.resolve(base);
            } else {
                receiptsDir = Paths.get(base);
            }
        } else {
            receiptsDir = outPath;
        }
        if (parent != null) {
            try {
                if (!Files.exists(parent)) Files.createDirectories(parent);
            } catch (IOException ioe) {
                // best-effort: continue and let appendTransaction fail later if needed
            }
        }
        // Ensure receipts directory exists (best-effort)
        try {
            if (!Files.exists(receiptsDir)) Files.createDirectories(receiptsDir);
        } catch (IOException ignored) {}
    }

    /**
     * Append a transaction line. Throws StorageException on I/O failure.
     */
    public void appendTransaction(Transaction tx) throws StorageException {
        if (tx == null) throw new IllegalArgumentException("tx required");

        String line = buildLine(tx);

        boolean existed = Files.exists(outPath);
        try (BufferedWriter bw = Files.newBufferedWriter(outPath, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            if (!existed) {
                // Header with pipe-separated columns (exact format requested)
                bw.write("timestampMillis | plate | type | entryHuman | exitHuman | amount | method | reference");
                bw.newLine();
            }
            bw.write(line);
            bw.newLine();
            bw.flush();
        } catch (IOException ioe) {
            throw new StorageException("Failed to append transaction to " + outPath.toString(), ioe);
        }
    }

    /**
     * Append a human-readable receipt file for the given transaction. The receipt
     * content should be provided by the caller (rendered via ReceiptPrinter).
     */
    public void appendReceipt(Transaction tx, String receipt) throws StorageException {
        if (tx == null) throw new IllegalArgumentException("tx required");
        if (receipt == null) receipt = "";

        // Ensure receiptsDir exists
        try {
            if (!Files.exists(receiptsDir)) Files.createDirectories(receiptsDir);
        } catch (IOException ioe) {
            throw new StorageException("Failed to create receipts directory " + receiptsDir.toString(), ioe);
        }

        String fileName = String.format("%d_%s.txt", System.currentTimeMillis(), tx.getId());
        Path out = receiptsDir.resolve(fileName);
        try (BufferedWriter bw = Files.newBufferedWriter(out, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW)) {
            bw.write("Transaction ID: " + tx.getId()); bw.newLine();
            bw.write("Plate         : " + tx.getPlate()); bw.newLine();
            bw.write("Vehicle Type  : " + tx.getVehicleType()); bw.newLine();
            bw.write("Entry         : " + epochToHuman(tx.getEntryTime())); bw.newLine();
            bw.write("Exit          : " + epochToHuman(tx.getExitTime())); bw.newLine();
            bw.write("Fee           : " + String.format("%.2f", tx.getFee())); bw.newLine();
            bw.write("Payment Method: " + tx.getModeOfPayment()); bw.newLine();
            bw.write("Payment Ref   : " + tx.getNotes()); bw.newLine();
            bw.newLine();
            bw.write("--- RECEIPT ---"); bw.newLine();
            bw.write(receipt); bw.newLine();
            bw.flush();
        } catch (IOException ioe) {
            throw new StorageException("Failed to write receipt to " + out.toString(), ioe);
        }
    }

    /**
     * Return configured path (useful for user messages).
     */
    public String getPath() {
        // Return configured receipts directory for user messages (where receipts are stored)
        return receiptsDir.toString();
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

        // Pad columns to minimum widths so pipe-delimited columns align for humans
        plate = padField(plate, 8);
        type = padField(type, 8);
        entryHuman = padField(entryHuman, 20);
        exitHuman = padField(exitHuman, 20);
        method = padField(method, 8);
        ref = padField(ref, 8);

        return String.format("%d | %s | %s | %s | %s | %.2f | %s | %s",
            now, plate, type, entryHuman, exitHuman, amt, method, ref);
    }

    // Pad a field to a minimum width (right-pad with spaces); does not truncate
    private String padField(String s, int minWidth) {
        if (s == null) s = "";
        if (s.length() >= minWidth) return s;
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < minWidth) sb.append(' ');
        return sb.toString();
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
