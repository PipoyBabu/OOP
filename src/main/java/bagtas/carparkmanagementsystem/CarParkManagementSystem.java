/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package bagtas.carparkmanagementsystem;

/**
 *
 * @author unit 1
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Menu-first CLI (revised) with simple file handling (save/load/export).
 */
public class CarParkManagementSystem {
    private final Scanner scanner;
    private final Map<String, VehicleRecord> registry = new LinkedHashMap<>();
    private final ParkingLot parkingLot = new ParkingLot();
    private final BillingService billingService = new BillingService();

    // Simple text storage service (writes transaction lines)
    private final StorageService storageService = new StorageService("data/transactions.txt");

    // Human-readable timestamp formatter (system default zone)
    private static final DateTimeFormatter HUMAN_TS_FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    public CarParkManagementSystem() {
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        while (true) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    vehicleRecordsMenu();
                    break;
                case "2":
                    parkingOperationsMenu();
                    break;
                case "3":
                    occupancyTrackingMenu();
                    break;
                case "4":
                    billsPaymentsMenu();
                    break;
                case "5":
                    dataStorageMenu();
                    break;
                case "6":
                case "exit":
                    System.out.println("Exiting.");
                    return;
                default:
                    System.out.println("Invalid selection — press Enter to continue.");
                    scanner.nextLine();
            }
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("Multi-Level Parking Management System");
        System.out.println("\t\tMain Menu");
        System.out.println();
        System.out.println("OPENING HOURS : 10:00 a.m");
        System.out.println("CLOSING HOURS : 10:00 p.m");
        System.out.println("[1] - VEHICLE RECORDS");
        System.out.println("[2] - PARKING OPERATIONS");
        System.out.println("[3] - OCCUPANCY & TRACKING");
        System.out.println("[4] - BILLS & PAYMENTS");
        System.out.println("[5] - DATA STORAGE");
        System.out.println("[6] - EXIT");
        System.out.print("Choose an option: ");
    }

    private void vehicleRecordsMenu() {
        while (true) {
            System.out.println();
            System.out.println("--- VEHICLE RECORDS ---");
            System.out.println("[1] - REGISTER VEHICLE");
            System.out.println("[2] - DELETE VEHICLE");
            System.out.println("[3] - BACK TO MENU");
            System.out.print("Choose: ");
            String opt = scanner.nextLine().trim();
            switch (opt) {
                case "1":
                    registerVehicle();
                    break;
                case "2":
                    deleteVehicle();
                    break;
                case "3":
                    return;
                default:
                    System.out.println("Invalid selection — press Enter to continue.");
                    scanner.nextLine();
                    break;
            }
        }
    }


private void registerVehicle() {
    System.out.println();
    System.out.println("REGISTER VEHICLE");

    System.out.print("Plate number (required): ");
    String plate = scanner.nextLine().trim();
    if (plate.isEmpty()) {
        System.out.println("Plate number cannot be empty.");
        pause();
        return;
    }
    if (registry.containsKey(plate)) {
        System.out.println("A vehicle with that plate is already registered.");
        pause();
        return;
    }

    System.out.print("Vehicle type (car/motorcycle/scooter/ev) [default: car]: ");
    String type = scanner.nextLine().trim().toLowerCase();
    if (type.isEmpty()) {
        type = "car";
    }

    // Validate vehicle type input and handle domain error here
    try {
        validateVehicleType(type);
    } catch (InvalidVehicleTypeException ive) {
        System.err.println(ive.getMessage());
        pause();
        return;
    }

        // Height is now REQUIRED
        double height = 0.0;
        while (true) {
            System.out.print("Height in meters (required): ");
            String h = scanner.nextLine().trim();

            if (h.isEmpty()) {
                System.err.println("Height is required. Please enter a value in meters (e.g., 1.65). ");
                continue;
            }

            Double parsed = parseDoubleStrict(h);
            if (parsed == null) {
                System.err.println("Invalid height format. Please enter a numeric value (e.g., 1.65). ");
                continue;
            }

            if (parsed <= 0.0) {
                System.err.println("Height must be greater than 0.");
                continue;
            }

            if (parsed > ParkingLot.DEFAULT_CLEARANCE_M) {
                System.err.println("Height " + parsed + "m exceeds maximum allowed clearance of "
                        + ParkingLot.DEFAULT_CLEARANCE_M + "m");
                continue;
            }

            height = parsed;
            break;
        }

    int engineCc = 0;
    if ("motorcycle".equals(type) || "scooter".equals(type)) {
        System.out.print("Engine CC (press Enter for default 150): ");
        String cc = scanner.nextLine().trim();
        engineCc = parseIntOrDefault(cc, 150);
    }

    boolean isPwd = false;
    System.out.print("Is driver PWD? (y/n, press Enter for n): ");
    String pwd = scanner.nextLine().trim().toLowerCase();
    if ("y".equals(pwd) || "yes".equals(pwd)) {
        isPwd = true;
    }

    VehicleRecord record = new VehicleRecord(plate, type, height, engineCc, isPwd);
    registry.put(plate, record);

    System.out.println("Vehicle registered:");
    System.out.println(record);
    pause();
}

/**
 * Strict double parser: returns null if parsing fails.
 */
private Double parseDoubleStrict(String s) {
    try {
        return Double.parseDouble(s);
    } catch (NumberFormatException e) {
        return null;
    }
}



    private void deleteVehicle() {
        System.out.print("Enter plate to delete: ");
        String plate = scanner.nextLine().trim();
        if (!registry.containsKey(plate)) {
            System.out.println("No such registered vehicle.");
            pause();
            return;
        }
        registry.remove(plate);
        System.out.println("Vehicle deleted.");
        pause();
    }

    private void parkingOperationsMenu() {
        while (true) {
            System.out.println();
            System.out.println("--- PARKING OPERATIONS ---");
            System.out.println("[1] - PARK VEHICLE");
            System.out.println("[2] - PULL-OUT VEHICLE");
            System.out.println("[3] - BACK TO MENU");
            System.out.print("Choose: ");
            String opt = scanner.nextLine().trim();
            switch (opt) {
                case "1":
                    parkVehicleFlow();
                    break;
                case "2":
                    pullOutVehicleFlow();
                    break;
                case "3":
                    return;
                default:
                    System.out.println("Invalid selection — press Enter to continue.");
                    scanner.nextLine();
                    break;
            }
        }
    }

    // now uses parkingLot.parkOrThrow to make allocation/duplicate errors explicit
    private void parkVehicleFlow() {
        System.out.println();
        System.out.println("PARK VEHICLE");
        System.out.print("Enter plate number (must be registered): ");
        String plate = scanner.nextLine().trim();
        if (!registry.containsKey(plate)) {
            System.out.println("Plate not found in registry. Register first.");
            pause();
            return;
        }
        VehicleRecord rec = registry.get(plate);

        Vehicle v;
        String type = rec.type.toLowerCase();
        double height = rec.height > 0.0 ? rec.height : 0.0;
        boolean pwd = rec.pwd;

        if ("car".equals(type) || "ev".equals(type)) {
            if ("ev".equals(type)) {
                v = new Car(rec.plate, height, pwd) {
                    @Override
                    public String getType() { return "EV"; }
                };
            } else {
                v = new Car(rec.plate, height, pwd);
            }
        } else {
            int cc = rec.engineCc > 0 ? rec.engineCc : 150;
            Motorcycle m = new Motorcycle(rec.plate, height > 0.0 ? height : 1.1, cc);
            m.setPwdDriver(rec.pwd);
            v = m;
        }

        // Consistent internal handling: check duplicate plate and height, then attempt non-throwing park
        if (parkingLot.findSlotByPlate(plate) != null) {
            System.err.println("A vehicle with that plate is already parked.");
            pause();
            return;
        }

        if (v.getHeight() > ParkingLot.DEFAULT_CLEARANCE_M) {
            System.err.println("Vehicle height " + v.getHeight() + "m exceeds lot maximum clearance of " + ParkingLot.DEFAULT_CLEARANCE_M + "m");
            pause();
            return;
        }

        boolean parked = false;
        try {
            parked = parkingLot.parkVehicle(v);
        } catch (IllegalArgumentException ex) {
            System.err.println("Invalid vehicle data: " + ex.getMessage());
            pause();
            return;
        } catch (Exception ex) {
            System.err.println("Unexpected error while parking: " + ex.getMessage());
            pause();
            return;
        }

        if (!parked) {
            System.err.println("Could not park vehicle: no suitable slot available or slot refused vehicle.");
            pause();
            return;
        }

        ParkingSlot slot = parkingLot.findSlotByPlate(plate);
        if (slot == null) {
            System.out.println("Parked, but could not find slot info (unexpected).");
        } else {
            System.out.println("Parked successfully in " + slot.getSlotType()
                + " on floor " + slot.getFloorNumber()
                + " slot #" + slot.getSlotNumber() + ".");
        }
        pause();
    }

    private void pullOutVehicleFlow() {
    System.out.println();
    System.out.println("PULL-OUT VEHICLE");
    System.out.print("Enter plate number to pull out: ");
    String plate = scanner.nextLine().trim();

    if (plate.isEmpty()) {
        System.out.println("Plate cannot be empty.");
        pause();
        return;
    }

    ParkingSlot slot = parkingLot.findSlotByPlate(plate);
    if (slot == null) {
        System.err.println("Vehicle not found in any slot.");
        pause();
        return;
    }

    Vehicle v = slot.getCurrentVehicle();
    long entry = slot.getEntryTime();
    long exit = System.currentTimeMillis();

    // Process payment first; only remove vehicle after successful payment so
    // we don't lose parked vehicles if payment fails.
    try {
        boolean ok = paymentFlow(v, plate, entry, exit);
        if (ok) {
            System.out.println("Vehicle successfully pulled out and billed.");
        } else {
            System.out.println("Payment not recorded. Vehicle remains parked.");
        }
    } catch (InvalidPaymentException ipe) {
        System.err.println("Payment error: " + ipe.getMessage());
    }
    pause();
}

    // Overloaded paymentFlow: process payment/persistence/receipt for an already-removed vehicle
    private boolean paymentFlow(Vehicle v, String plate, long entry, long exit) throws InvalidPaymentException {
        if (v == null) throw new IllegalArgumentException("vehicle is null");
        double fee = billingService.computeFee(v, entry, exit);
        System.out.println();
        System.out.println("Vehicle (plate " + plate + ") - processing payment");
        System.out.printf("Parking Fee: %.2f%n", fee);
        System.out.println("Entry: " + formatMillis(entry));
        System.out.println("Exit : " + formatMillis(exit));

        System.out.println();
        System.out.println("Select payment method:");
        System.out.println("[1] - CASH");
        System.out.println("[2] - CARD");
        System.out.print("Choose: ");
        String pm = scanner.nextLine().trim();

        Payment payment = null;
        if ("1".equals(pm)) {
            System.out.print("Enter cash amount: ");
            double cash = parseDoubleOrDefault(scanner.nextLine().trim(), 0.0);
            payment = new CashPayment(fee, cash);
        } else if ("2".equals(pm)) {
            System.out.print("Enter card number: ");
            String cn = scanner.nextLine().trim();
            System.out.print("Enter card holder: ");
            String ch = scanner.nextLine().trim();
            // Request 6-digit PIN (Philippine common practice)
            String pin = null;
            while (true) {
                System.out.print("Enter 6-digit PIN: ");
                pin = scanner.nextLine().trim();
                if (pin.matches("\\d{6}")) {
                    break;
                }
                System.out.println("PIN must be exactly 6 digits. Please try again.");
            }
            payment = new CardPayment(fee, cn, ch, pin);
        } else {
            throw new InvalidPaymentException("Unsupported payment method: " + pm);
        }

        Transaction[] out = new Transaction[1];
        PaymentResult pr = billingService.payAndCreateTransaction(v, entry, exit, payment, out);
        if (!pr.isSuccess()) {
            System.err.println("Payment failed: " + pr.getMessage());
            return false;
        }

        // Render receipt first (caller-provided rendering) then persist the
        // receipt file. This ensures the stored file matches the printed receipt.
        String receipt = ReceiptPrinter.renderReceipt(out[0], v, pr);
        if (!tryPersistTransaction(out[0], receipt)) {
            System.err.println("Warning: transaction could not be persisted.");
        }

        // Attempt to remove the vehicle now that payment succeeded. If the
        // caller already removed the vehicle, that's fine; otherwise remove it.
        ParkingSlot current = parkingLot.findSlotByPlate(plate);
        if (current != null) {
            Vehicle removed = parkingLot.removeVehicleByPlate(plate);
            if (removed == null) {
                System.err.println("Warning: could not remove vehicle after successful payment.");
            }
        }

        System.out.println("\n--- RECEIPT ---");
        System.out.println(receipt);
        System.out.println("Entry: " + formatMillis(entry));
        System.out.println("Exit : " + formatMillis(exit));
        System.out.println("----------------");
        return true;
    }

    private void occupancyTrackingMenu() {
        while (true) {
            System.out.println();
            System.out.println("--- OCCUPANCY & TRACKING ---");
            System.out.println("[1] - SHOW SLOTS WITH STATUS");
            System.out.println("[2] - COUNT VEHICLES BY TYPE");
            System.out.println("[3] - SEARCH VEHICLE BY PLATE NO.");
            System.out.println("[4] - BACK TO MENU");
            System.out.print("Choose: ");
            String opt = scanner.nextLine().trim();
            switch (opt) {
                case "1":
                    showSlotsWithStatus();
                    break;
                case "2":
                    countVehiclesByType();
                    break;
                case "3":
                    searchVehicleByPlate();
                    break;
                case "4":
                    return;
                default:
                    System.out.println("Invalid selection — press Enter to continue.");
                    scanner.nextLine();
                    break;
            }
        }
    }

    // Uses the ParkingLot.getFloorsSnapshot() that your ParkingLot currently provides.
    private void showSlotsWithStatus() {
        System.out.println();
        System.out.println("SHOW SLOTS WITH STATUS");

        Map<Integer, List<ParkingSlot>> snapshot = parkingLot.getFloorsSnapshot();
        if (snapshot == null || snapshot.isEmpty()) {
            System.out.println("No slot information available from ParkingLot.");
            pause();
            return;
        }

        System.out.printf("%-6s %-6s %-12s %-10s %-12s%n", "Floor", "Slot#", "SlotType", "Status", "Plate");
        for (int floor : snapshot.keySet()) {
            List<ParkingSlot> slots = snapshot.get(floor);
            if (slots == null) {
                continue;
            }
            for (ParkingSlot slot : slots) {
                String status = slot.getCurrentVehicle() == null ? "VACANT" : "OCCUPIED";
                String plate = slot.getCurrentVehicle() == null ? "-" : slot.getCurrentVehicle().getPlateNumber();
                System.out.printf("%-6d %-6d %-12s %-10s %-12s%n",
                    slot.getFloorNumber(),
                    slot.getSlotNumber(),
                    slot.getSlotType(),
                    status,
                    plate);
            }
        }
        pause();
    }

    // New: count vehicles by type across all floors
    private void countVehiclesByType() {
        System.out.println();
        System.out.println("COUNT VEHICLES BY TYPE");

        Map<Integer, List<ParkingSlot>> snapshot = parkingLot.getFloorsSnapshot();
        if (snapshot == null || snapshot.isEmpty()) {
            System.out.println("No slot information available from ParkingLot.");
            pause();
            return;
        }

        int cars = 0;
        int scooters = 0;
        int bigBikes = 0; // large motorcycles / big bikes
        int evs = 0;
        int pwd = 0; // count of PWD drivers among parked vehicles
        int unknown = 0;

        for (List<ParkingSlot> slots : snapshot.values()) {
            if (slots == null) {
                continue;
            }
            for (ParkingSlot slot : slots) {
                Vehicle v = slot.getCurrentVehicle();
                if (v == null) {
                    continue;
                }
                String vtype = safeLower(v.getType());
                if (vtype.equalsIgnoreCase("ev") || "ev".equalsIgnoreCase(v.getType())) {
                    evs++;
                } else if (v instanceof Motorcycle) {
                    int cc = ((Motorcycle) v).getEngineCC();
                    if (cc < 400) {
                        scooters++;
                    } else {
                        bigBikes++;
                    }
                } else if (vtype.equalsIgnoreCase("car") || "car".equalsIgnoreCase(v.getType())) {
                    cars++;
                } else {
                    // fallback: infer from slot type
                    String slotType = safeLower(slot.getSlotType());
                    switch (slotType) {
                        case "evslot": evs++; break;
                        case "scooterslot": scooters++; break;
                        case "carslot": cars++; break;
                        default: unknown++; break;
                    }
                }

                // Count PWD by the vehicle's registered attribute (avoid double-counting via slot types)
                try {
                    if (v.isPwdDriver()) pwd++;
                } catch (Exception ignored) {}
            }
        }

        System.out.println("Summary of parked vehicles:");
        System.out.println(" - Cars: " + cars);
        System.out.println(" - EVs: " + evs);
        System.out.println(" - Scooters (small motorcycles): " + scooters);
        System.out.println(" - Big bikes (large motorcycles): " + bigBikes);
        System.out.println(" - Parked in PWD slots: " + pwd);
        if (unknown > 0) {
            System.out.println(" - Unknown/other: " + unknown);
        }
        pause();
    }

    private void searchVehicleByPlate() {
        System.out.println();
        System.out.println("SEARCH VEHICLE BY PLATE");
        System.out.print("Enter plate number: ");
        String plate = scanner.nextLine().trim();
        if (plate.isEmpty()) {
            System.out.println("Plate cannot be empty.");
            pause();
            return;
        }

        VehicleRecord rec = registry.get(plate);
        if (rec == null) {
            System.out.println("No registry entry for plate: " + plate);
        } else {
            System.out.println("Registry: " + rec);
        }

        ParkingSlot slot = parkingLot.findSlotByPlate(plate);
        if (slot == null) {
            System.out.println("Vehicle is not currently parked.");
            pause();
            return;
        }

        Vehicle parked = slot.getCurrentVehicle();
        String parkedType = (parked != null) ? parked.getType() : slot.getSlotType();
        System.out.println("Currently parked:");
        System.out.println(" - Slot type: " + slot.getSlotType());
        System.out.println(" - Located on floor: " + slot.getFloorNumber());
        System.out.println(" - Slot number: " + slot.getSlotNumber());
        System.out.println(" - Vehicle type: " + parkedType);
        System.out.println(" - Entry time : " + formatMillis(slot.getEntryTime()));
        pause();
    }

    // --- Billing & Payments menu and flows (patched) ---

    private void billsPaymentsMenu() {
        while (true) {
            System.out.println();
            System.out.println("--- BILLS & PAYMENTS ---");
            System.out.println("[1] - SHOW FEE RULES");
            System.out.println("[2] - COMPUTE BILL");
            System.out.println("[3] - PAYMENT");
            System.out.println("[4] - BACK TO MENU");
            System.out.print("Choose: ");
            String opt = scanner.nextLine().trim();
            switch (opt) {
                case "1":
                    showFeeRules();
                    break;
                case "2":
                    computeBillFlow();
                    break;
                case "3":
                    paymentFlow();
                    break;
                case "4":
                    return;
                default:
                    System.out.println("Invalid selection - press Enter to continue.");
                    scanner.nextLine();
                    break;
            }
        }
    }

    // Displays current fee structure. Adjust text to match your actual rates and rules if needed.
    private void showFeeRules() {
        System.out.println();
        System.out.println("FEE RULES AND POLICIES");
        System.out.println("----------------------");
        System.out.println(" - Base price and rates are defined per vehicle type (see examples).");
        System.out.println(" - Base hours included; hourly rate applies after base hours.");
        System.out.println(" - Overnight fee per full day applies for multi-day stays.");
        System.out.println(" - PWD discount: 50% off (applies after computing fee).");
        System.out.println();

        // Print example values by constructing sample Vehicles when possible
        try {
            Vehicle sampleCar = new Car("SAMPLE", 1.6, false);
            System.out.printf(" Car  : base = %.2f, baseHours = %d, rate = %.2f, overnight = %.2f%n",
                sampleCar.getBasePrice(), sampleCar.getBaseHours(), sampleCar.getRatePerHour(), sampleCar.getOvernightFee());
        } catch (Exception ignored) {}
        try {
            Motorcycle sampleMoto = new Motorcycle("SAMPLE", 1.1, 150);
            System.out.printf(" Motorcycle/scooter : base = %.2f, baseHours = %d, rate = %.2f, overnight = %.2f%n",
                sampleMoto.getBasePrice(), sampleMoto.getBaseHours(), sampleMoto.getRatePerHour(), sampleMoto.getOvernightFee());
        } catch (Exception ignored) {}
        pause();
    }

    // Computes fee and displays it (no side-effects) with human-readable timestamps
    private void computeBillFlow() {
        System.out.print("Enter plate number to compute bill for: ");
        String plate = scanner.nextLine().trim();
        if (plate.isEmpty()) {
            System.out.println("Plate cannot be empty.");
            pause();
            return;
        }
        ParkingSlot slot = parkingLot.findSlotByPlate(plate);
        if (slot == null) {
            System.out.println("Vehicle not found in any slot.");
            pause();
            return;
        }
        Vehicle v = slot.getCurrentVehicle();
        if (v == null) {
            System.out.println("No vehicle present in slot for plate.");
            pause();
            return;
        }
        long entry = slot.getEntryTime();
        long now = System.currentTimeMillis();
        double fee = billingService.computeFee(v, entry, now);
        System.out.println();
        System.out.printf("Computed fee for plate %s: %.2f%n", plate, fee);
        System.out.printf("  Entry time: %s%n", formatMillis(entry));
        System.out.printf("  Now       : %s%n", formatMillis(now));
        System.out.println("Note: This only computes the fee. To pay and exit, use PAYMENT in the menu.");
        pause();
    }

    // Full payment flow: preview fee, validate payment, process, persist, remove vehicle, print receipt
    private void paymentFlow() {
        System.out.print("Enter plate number to pay & exit: ");
        String plate = scanner.nextLine().trim();
        if (plate.isEmpty()) {
            System.out.println("Plate cannot be empty.");
            pause();
            return;
        }

        ParkingSlot slot = parkingLot.findSlotByPlate(plate);
        if (slot == null) {
            System.out.println("Vehicle not found in any slot.");
            pause();
            return;
        }

        Vehicle v = slot.getCurrentVehicle();
        if (v == null) {
            System.out.println("No vehicle present in slot for plate.");
            pause();
            return;
        }

        // Allow operator to confirm pull-out then process payment from this menu
        System.out.print("Confirm pull-out and process payment now? (y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (!"y".equals(confirm) && !"yes".equals(confirm)) {
            System.out.println("Cancelled. Use PULL-OUT VEHICLE to perform exit later.");
            pause();
            return;
        }

        long entry = slot.getEntryTime();
        long exit = System.currentTimeMillis();

        try {
            boolean ok = paymentFlow(v, plate, entry, exit);
            if (ok) {
                System.out.println("Payment processed and vehicle pulled out.");
            } else {
                System.out.println("Payment not recorded. Vehicle remains parked.");
            }
        } catch (InvalidPaymentException ipe) {
            System.err.println("Payment error: " + ipe.getMessage());
        }
        pause();
    }

    // Attempt to persist a transaction and its rendered receipt via storageService; returns true on success
    private boolean tryPersistTransaction(Transaction tx, String renderedReceipt) {
        if (tx == null) {
            return false;
        }
        try {
            storageService.appendReceipt(tx, renderedReceipt);
            System.out.println("Receipt persisted to " + storageService.getPath());
            return true;
        } catch (StorageException se) {
            System.err.println("Failed to persist receipt: " + se.getMessage());
            return false;
        }
    }

    // --- Data storage menu: Save, Load, Export ---
    private void dataStorageMenu() {
        while (true) {
            System.out.println();
            System.out.println("--- DATA STORAGE ---");
            System.out.println("[1] - SAVE");
            System.out.println("[2] - LOAD");
            System.out.println("[3] - EXPORT FILE");
            System.out.println("[4] - BACK TO MENU");
            System.out.print("Choose: ");
            String opt = scanner.nextLine().trim();
            switch (opt) {
                case "1":
                    saveStateFlow();
                    break;
                case "2":
                    loadStateFlow();
                    break;
                case "3":
                    exportParkedFlow();
                    break;
                case "4":
                    return;
                default:
                    System.out.println("Invalid selection — press Enter to continue.");
                    scanner.nextLine();
                    break;
            }
        }
    }

    // Save registry (vehicles) to data/vehicles.txt and optionally persist transactions (no-op if none)
    private void saveStateFlow() {
        System.out.println();
        System.out.println("SAVE STATE");
        try {
            Path dataDir = Paths.get("data");
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
            }

            Path vehiclesPath = dataDir.resolve("vehicles.txt");
            try (BufferedWriter bw = Files.newBufferedWriter(vehiclesPath, StandardCharsets.UTF_8)) {
                // Write documented header (pipe-delimited) per user preference - aligned columns
                String h0 = padColumn("Plate", 8);
                String h1 = padColumn("Type", 8);
                String h2 = padColumn("Height", 8);
                String h3 = padColumn("EngineCc", 8);
                String h4 = padColumn("PWD", 8);
                bw.write(h0 + " | " + h1 + " | " + h2 + " | " + h3 + " | " + h4);
                bw.newLine();
                // Pipe-delimited line format with minimum column widths
                for (VehicleRecord r : registry.values()) {
                    String p0 = padColumn(safeForFile(r.plate), 8);
                    String p1 = padColumn(safeForFile(r.type), 8);
                    String p2 = padColumn(String.format("%.2f", r.height), 8);
                    String p3 = padColumn(String.valueOf(r.engineCc), 8);
                    String p4 = padColumn(r.pwd ? "1" : "0", 8);
                    String line = p0 + " | " + p1 + " | " + p2 + " | " + p3 + " | " + p4;
                    bw.write(line);
                    bw.newLine();
                }
                bw.flush();
            }

            System.out.println("Saved vehicles to " + vehiclesPath.toString());
        } catch (IOException ioe) {
            System.out.println("Failed to save state: " + ioe.getMessage());
        }
        pause();
    }

    // Load registry from data/vehicles.txt (tolerant)
    private void loadStateFlow() {
        System.out.println();
        System.out.println("LOAD STATE");
        Path vehiclesPath = Paths.get("data", "vehicles.txt");
        if (!Files.exists(vehiclesPath)) {
            System.out.println("No saved vehicles file found at " + vehiclesPath.toString());
            pause();
            return;
        }

        int loaded = 0;
        int skipped = 0;
        try (BufferedReader br = Files.newBufferedReader(vehiclesPath, StandardCharsets.UTF_8)) {
            String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) {
                        continue;
                    }
                    // Skip header/comment lines that start with '#' or a CSV header 'plate,'
                    if (line.startsWith("#")) {
                        continue;
                    }
                    if (line.toLowerCase().startsWith("plate,")) {
                        continue;
                    }

                    // Accept either pipe-delimited or comma-delimited lines for backward compatibility
                    String[] parts;
                    if (line.contains("|")) {
                        parts = line.split("\\|", -1);
                    } else if (line.contains(",")) {
                        parts = line.split(",", -1);
                    } else {
                        skipped++;
                        continue;
                    }
                    if (parts.length < 5) {
                        skipped++;
                        continue;
                    }
                    String plate = parts[0].trim();
                    String type = parts[1].trim();
                    double height = parseDoubleOrDefault(parts[2].trim(), 0.0);
                    int engineCc = parseIntOrDefault(parts[3].trim(), 0);
                    boolean pwd = "1".equals(parts[4].trim()) || "true".equalsIgnoreCase(parts[4].trim());
                VehicleRecord rec = new VehicleRecord(plate, type, height, engineCc, pwd);
                registry.put(plate, rec);
                loaded++;
            }
            System.out.println("Loaded " + loaded + " vehicle(s) from " + vehiclesPath.toString());
            if (skipped > 0) {
                System.out.println("Skipped " + skipped + " malformed line(s) while loading vehicles.");
            }
        } catch (IOException ioe) {
            System.out.println("Failed to load state: " + ioe.getMessage());
        }
        pause();
    }

    // Export currently parked vehicles to exports/parked.txt in simple text format (plate, type, entry human time)
    private void exportParkedFlow() {
        System.out.println();
        System.out.println("EXPORT FILE (currently parked vehicles)");
        Path exportsDir = Paths.get("exports");
        try {
            if (!Files.exists(exportsDir)) {
                Files.createDirectories(exportsDir);
            }
        } catch (IOException ignored) {}

        Path out = exportsDir.resolve("parked.txt");
        int written = 0;
        try (BufferedWriter bw = Files.newBufferedWriter(out, StandardCharsets.UTF_8)) {
            // Header with aligned columns (plate/type min 8, entry min 20)
            String hh0 = padColumn("Plate", 8);
            String hh1 = padColumn("Type", 8);
            String hh2 = padColumn("EntryTime", 20);
            bw.write(hh0 + " | " + hh1 + " | " + hh2);
            bw.newLine();
            Map<Integer, List<ParkingSlot>> snapshot = parkingLot.getFloorsSnapshot();
            if (snapshot != null) {
                for (List<ParkingSlot> slots : snapshot.values()) {
                    if (slots == null) {
                        continue;
                    }
                    for (ParkingSlot slot : slots) {
                        Vehicle v = slot.getCurrentVehicle();
                        if (v == null) {
                            continue;
                        }
                        String plate = padColumn(v.getPlateNumber(), 8);
                        String type = padColumn(v.getType(), 8);
                        String entry = padColumn(formatMillis(slot.getEntryTime()), 20);
                        String line = plate + " | " + type + " | " + entry;
                        bw.write(line);
                        bw.newLine();
                        written++;
                    }
                }
            }
            bw.flush();
            System.out.println("Exported " + written + " parked record(s) to " + out.toString());
        } catch (IOException ioe) {
            System.out.println("Failed to export parked file: " + ioe.getMessage());
        }
        pause();
    }

    private void pause() {
        System.out.println("Press Enter to continue.");
        scanner.nextLine();
    }

    private double parseDoubleOrDefault(String s, double d) {
        try {
            if (s == null || s.isEmpty()) {
                return d;
            }
            return Double.parseDouble(s);
        } catch (Exception ex) {
            return d;
        }
    }

    private int parseIntOrDefault(String s, int d) {
        try {
            if (s == null || s.isEmpty()) {
                return d;
            }
            return Integer.parseInt(s);
        } catch (Exception ex) {
            return d;
        }
    }

    // Validate vehicle type and throw a domain exception when unsupported
    private void validateVehicleType(String type) throws InvalidVehicleTypeException {
        if (type == null) {
            throw new InvalidVehicleTypeException("Vehicle type is required");
        }
        String t = type.trim().toLowerCase();
        if (!"car".equals(t) && !"motorcycle".equals(t) && !"scooter".equals(t) && !"ev".equals(t)) {
            throw new InvalidVehicleTypeException("Unsupported vehicle type: " + type);
        }
    }

    private String safeLower(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    // Human-readable formatter for epoch millis using system default zone
    private String formatMillis(long epochMillis) {
        if (epochMillis <= 0L) {
            return "N/A";
        }
        try {
            return HUMAN_TS_FMT.format(Instant.ofEpochMilli(epochMillis));
        } catch (Exception ex) {
            return String.valueOf(epochMillis);
        }
    }

    // Helper used for saving text fields
    private String safeForFile(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("|", " ").replace("\n", " ").trim();
    }

    // Pad a column to a minimum width (right-pad with spaces). Does not truncate.
    private String padColumn(String s, int minWidth) {
        if (s == null) {
            s = "";
        }
        if (s.length() >= minWidth) {
            return s;
        }
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < minWidth) sb.append(' ');
        return sb.toString();
    }

    // --- new helper flows exposed for programmatic use ---

    // Remove vehicle immediately without billing. Returns the removed Vehicle or null.
    public Vehicle pullOutOnly(String plate) {
        if (plate == null || plate.isEmpty()) {
            return null;
        }
        return parkingLot.removeVehicleByPlate(plate);
    }

    /**
     * Bill the vehicle then remove it on successful payment.
     * Returns a printable receipt on success, or an error message on failure.
     */
    public String billThenPullOut(String plate, String method, double cashGiven, String cardNumber, String cardHolder) {
        if (plate == null || plate.isEmpty()) {
            return "Invalid plate";
        }

        ParkingSlot slot = parkingLot.findSlotByPlate(plate);
        if (slot == null) {
            return "Plate not found in parking lot";
        }

        Vehicle v = slot.getCurrentVehicle();
        if (v == null) {
            return "No vehicle in slot for plate";
        }

        long entry = slot.getEntryTime();
        long exit = System.currentTimeMillis();

        // Compute fee first and construct payment with the correct amount
        double fee = billingService.computeFee(v, entry, exit);
        Payment payment;
        if ("CASH".equalsIgnoreCase(method)) {
            if (cashGiven < fee) {
                return "Insufficient cash provided (required: " + String.format("%.2f", fee) + ")";
            }
            payment = new CashPayment(fee, cashGiven);
        } else if ("CARD".equalsIgnoreCase(method)) {
            payment = new CardPayment(fee, cardNumber, cardHolder);
        } else {
            return "Unsupported payment method";
        }

        Transaction[] out = new Transaction[1];
        PaymentResult pr = billingService.payAndCreateTransaction(v, entry, exit, payment, out);

        if (!pr.isSuccess()) {
            return "Payment failed: " + pr.getMessage();
        }

        Transaction tx = out[0];
        parkingLot.removeVehicleByPlate(plate);

        String receipt = ReceiptPrinter.renderReceipt(tx, v, pr);
        receipt += System.lineSeparator() + "Entry: " + formatMillis(entry) + " | Exit: " + formatMillis(exit);
        return receipt;
    }

    private static class VehicleRecord {
        private final String plate;
        private final String type;
        private final double height;
        private final int engineCc;
        private final boolean pwd;

        VehicleRecord(String plate, String type, double height, int engineCc, boolean pwd) {
            this.plate = plate;
            this.type = type;
            this.height = height;
            this.engineCc = engineCc;
            this.pwd = pwd;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Plate: ").append(plate);
            sb.append(", Type: ").append(type);
            // owner removed from records
            if (height > 0.0) {
                sb.append(", Height: ").append(String.format("%.2fm", height));
            }
            if ("motorcycle".equalsIgnoreCase(type) || "scooter".equalsIgnoreCase(type)) {
                sb.append(", CC: ").append(engineCc > 0 ? engineCc : "-");
            }
            sb.append(", PWD: ").append(pwd ? "yes" : "no");
            return sb.toString();
        }
    }

    public static void main(String[] args) {
        CarParkManagementSystem app = new CarParkManagementSystem();
        app.start();
    }
}
