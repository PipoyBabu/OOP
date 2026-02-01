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
import java.util.UUID;

public abstract class BasePayment {
    // produce a stable reference id
    protected static String makeRef() {
        return "PAY-" + Instant.now().toEpochMilli() + "-" + UUID.randomUUID().toString().substring(0,8);
    }

    protected static String maskLast4(String pan) {
        if (pan == null) return "****";
        String clean = pan.replaceAll("\\s",""); 
        if (clean.length() <= 4) return clean;
        return "****" + clean.substring(clean.length() - 4);
    }

    protected static double roundToCent(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}