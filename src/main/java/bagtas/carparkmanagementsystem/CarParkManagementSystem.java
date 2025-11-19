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
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
                    System.out.println("Invalid choice. Press Enter to continue.");
                    scanner.nextLine();
            }
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("Multi-Level Parking Management System");
        System.out.println("\t\tMain Menu");
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
                    try {
                        registerVehicle();
                    } catch (InvalidVehicleTypeException ivt) {
                        System.err.println("Invalid vehicle type: " + ivt.getMessage());
                        pause();
                    }
                    break;
                case "2":
                    deleteVehicle();
                    break;
                default:
                    return;
            }
        }
    }

    // owner removed from the interactive flow; still stored as empty string
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

    // Validate vehicle type input and signal domain error via custom exception
    if (!"car".equals(type) && !"motorcycle".equals(type) && !"scooter".equals(type) && !"ev".equals(type)) {
        throw new InvalidVehicleTypeException("Unsupported vehicle type: " + type);
    }

    double height = 0.0;
    System.out.print("Height in meters (optional - press Enter to skip): ");
    String h = scanner.nextLine().trim();
    if (!h.isEmpty()) {
        height = parseDoubleOrDefault(h, 0.0);
    }

    int engineCc = 0;
    if ("motorcycle".equals(type) || "scooter".equals(type)) {
        System.out.print("Engine CC (press Enter for default 150): ");
        String cc = scanner.nextLine().trim();
        engineCc = parseIntOrDefault(cc, 150);
    }

    // --- OWNER REMOVED ---
    String owner = "";  // Always blank

    boolean isPwd = false;
    System.out.print("Is driver PWD? (y/n, press Enter for n): ");
    String pwd = scanner.nextLine().trim().toLowerCase();
    if ("y".equals(pwd) || "yes".equals(pwd)) {
        isPwd = true;
    }

    VehicleRecord record = new VehicleRecord(plate, type, owner, height, engineCc, isPwd);
    registry.put(plate, record);

    System.out.println("Vehicle registered:");
    System.out.println(record);
    pause();
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
                default:
                    return;
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

        try {
            parkingLot.parkOrThrow(v);
        } catch (SlotUnavailableException ex) {
            System.err.println("Could not park vehicle: " + ex.getMessage());
            pause();
            return;
        } catch (IllegalArgumentException ex) {
            System.err.println("Invalid vehicle data: " + ex.getMessage());
            pause();
            return;
        } catch (Exception ex) {
            System.err.println("Unexpected error while parking: " + ex.getMessage());
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
        System.out.println("PULL-OUT VEHICLE (Exit from slot without billing)");
        System.out.print("Enter plate number to pull out: ");
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

        System.out.println("Vehicle located in " + slot.getSlotType()
            + " on floor " + slot.getFloorNumber()
            + " slot #" + slot.getSlotNumber() + ".");
        System.out.print("Confirm pull-out (y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (!"y".equals(confirm) && !"yes".equals(confirm)) {
            System.out.println("Pull-out cancelled.");
            pause();
            return;
        }

        try {
            Vehicle removed = parkingLot.removeVehicleOrThrow(plate);
            System.out.println("Vehicle successfully pulled out and slot freed.");
        } catch (VehicleNotFoundException ex) {
            System.err.println("Pull-out failed: " + ex.getMessage());
        } catch (Exception ex) {
            System.err.println("Unexpected error while removing vehicle: " + ex.getMessage());
        }
        pause();
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
                default:
                    return;
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
            if (slots == null) continue;
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
        int pwd = 0; // count of PWD-parked vehicles (regardless of type)
        int unknown = 0;

        for (List<ParkingSlot> slots : snapshot.values()) {
            if (slots == null) continue;
            for (ParkingSlot slot : slots) {
                Vehicle v = slot.getCurrentVehicle();
                if (v == null) continue;
                String vtype = safeLower(v.getType());
                if (vtype.equals("ev")) {
                    evs++;
                } else if (v instanceof Motorcycle) {
                    int cc = ((Motorcycle) v).getEngineCC();
                    if (cc < 400) {
                        scooters++;
                    } else {
                        bigBikes++;
                    }
                } else if (vtype.equals("car")) {
                    cars++;
                } else {
                    // fallback: infer from slot type
                    String slotType = safeLower(slot.getSlotType());
                    switch (slotType) {
                        case "evslot": evs++; break;
                        case "scooterslot": scooters++; break;
                        case "carslot": cars++; break;
                        case "pwdslot": pwd++; break;
                        default: unknown++; break;
                    }
                }
                if (slot instanceof PWDSlot) {
                    pwd++;
                }
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
                default:
                    return;
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
            System.out.printf(" Car  : base=%.2f, baseHours=%d, rate=%.2f, overnight=%.2f%n",
                sampleCar.getBasePrice(), sampleCar.getBaseHours(), sampleCar.getRatePerHour(), sampleCar.getOvernightFee());
        } catch (Exception ignored) {}
        try {
            Motorcycle sampleMoto = new Motorcycle("SAMPLE", 1.1, 150);
            System.out.printf(" Motorcycle/scooter : base=%.2f, baseHours=%d, rate=%.2f, overnight=%.2f%n",
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

        long entry = slot.getEntryTime();
        long exit = System.currentTimeMillis();
        double fee = billingService.computeFee(v, entry, exit);

        System.out.printf("Amount due for plate %s: %.2f (entry: %s)%n", plate, fee, formatMillis(entry));
        System.out.print("Proceed to payment? (y/n): ");
        String ok = scanner.nextLine().trim().toLowerCase();
        if (!"y".equals(ok) && !"yes".equals(ok)) {
            System.out.println("Payment cancelled.");
            pause();
            return;
        }

        System.out.print("Payment method (CASH/CARD): ");
        String method = scanner.nextLine().trim().toUpperCase();
        Payment payment = null;

        try {
            if ("CASH".equals(method)) {
                System.out.print("Cash given: ");
                String cashS = scanner.nextLine().trim();
                double cashGiven = parseDoubleOrDefault(cashS, -1.0);
                if (cashGiven < fee) {
                    throw new InvalidPaymentException("Cash given is less than amount due");
                }
                payment = new CashPayment(fee, cashGiven);
            } else if ("CARD".equals(method)) {
                System.out.print("Card number: ");
                String card = scanner.nextLine().trim();
                if (card == null || card.replaceAll("\\s", "").length() < 12) {
                    throw new InvalidPaymentException("Card number appears too short");
                }
                System.out.print("Cardholder name (optional): ");
                String holder = scanner.nextLine().trim();
                payment = new CardPayment(fee, card, holder);
            } else {
                throw new InvalidPaymentException("Unsupported payment method: " + method);
            }
        } catch (InvalidPaymentException ex) {
            System.err.println("Invalid payment input: " + ex.getMessage());
            pause();
            return;
        }

        Transaction[] out = new Transaction[1];
        PaymentResult pr = billingService.payAndCreateTransaction(v, entry, exit, payment, out);
        if (!pr.isSuccess()) {
            System.out.println("Payment failed: " + pr.getMessage());
            pause();
            return;
        }

        Transaction tx = out[0];
        if (tx == null) {
            tx = new Transaction(v.getPlateNumber(), v.getType(), entry, exit, fee, ("CASH".equalsIgnoreCase(method) ? "CASH" : "CARD"), pr.getReferenceId());
        }

        boolean persisted = tryPersistTransaction(tx);

        try {
            parkingLot.removeVehicleByPlate(plate);
        } catch (Exception ex) {
            System.err.println("Warning: payment succeeded but failed to free the slot: " + ex.getMessage());
        }

        String receipt = ReceiptPrinter.renderReceipt(tx, v, pr);
        System.out.println();
        System.out.println(receipt);
        System.out.printf("Entry: %s | Exit: %s%n", formatMillis(entry), formatMillis(exit));

        if (!persisted) {
            System.out.println("Warning: transaction not persisted. Use DATA STORAGE -> Save state to retry.");
        }
        pause();
    }

    // Attempt to persist a transaction via storageService; returns true on success
    private boolean tryPersistTransaction(Transaction tx) {
        if (tx == null) return false;
        try {
            storageService.appendTransaction(tx);
            System.out.println("Transaction persisted to " + storageService.getPath());
            return true;
        } catch (StorageException se) {
            System.err.println("Failed to persist transaction: " + se.getMessage());
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
                default:
                    return;
            }
        }
    }

    // Save registry (vehicles) to data/vehicles.txt and optionally persist transactions (no-op if none)
    private void saveStateFlow() {
        System.out.println();
        System.out.println("SAVE STATE");
        try {
            File dataDir = new File("data");
            if (!dataDir.exists()) dataDir.mkdirs();

            File vehiclesFile = new File(dataDir, "vehicles.txt");
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(vehiclesFile))) {
                // Simple line format: plate|type|owner|height|engineCc|pwd
                for (VehicleRecord r : registry.values()) {
                    String line = String.format("%s|%s|%s|%.2f|%d|%s",
                        r.plate,
                        safeForFile(r.type),
                        safeForFile(r.owner),
                        r.height,
                        r.engineCc,
                        r.pwd ? "1" : "0");
                    bw.write(line);
                    bw.newLine();
                }
                bw.flush();
            }

            System.out.println("Saved vehicles to " + vehiclesFile.getPath());
        } catch (IOException ioe) {
            System.out.println("Failed to save state: " + ioe.getMessage());
        }
        pause();
    }

    // Load registry from data/vehicles.txt (tolerant)
    private void loadStateFlow() {
        System.out.println();
        System.out.println("LOAD STATE");
        File vehiclesFile = new File("data/vehicles.txt");
        if (!vehiclesFile.exists()) {
            System.out.println("No saved vehicles file found at " + vehiclesFile.getPath());
            pause();
            return;
        }

        int loaded = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(vehiclesFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                // expected: plate|type|owner|height|engineCc|pwd
                String[] parts = line.split("\\|", -1);
                if (parts.length < 6) continue;
                String plate = parts[0].trim();
                String type = parts[1].trim();
                String owner = parts[2].trim();
                double height = parseDoubleOrDefault(parts[3].trim(), 0.0);
                int engineCc = parseIntOrDefault(parts[4].trim(), 0);
                boolean pwd = "1".equals(parts[5].trim()) || "true".equalsIgnoreCase(parts[5].trim());
                VehicleRecord rec = new VehicleRecord(plate, type, owner, height, engineCc, pwd);
                registry.put(plate, rec);
                loaded++;
            }
            System.out.println("Loaded " + loaded + " vehicle(s) from " + vehiclesFile.getPath());
        } catch (IOException ioe) {
            System.out.println("Failed to load state: " + ioe.getMessage());
        }
        pause();
    }

    // Export currently parked vehicles to exports/parked.txt in simple text format (plate, type, entry human time)
    private void exportParkedFlow() {
        System.out.println();
        System.out.println("EXPORT FILE (currently parked vehicles)");
        File exportsDir = new File("exports");
        if (!exportsDir.exists()) exportsDir.mkdirs();

        File out = new File(exportsDir, "parked.txt");
        int written = 0;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(out))) {
            bw.write("Plate | Type | EntryTime");
            bw.newLine();
            Map<Integer, List<ParkingSlot>> snapshot = parkingLot.getFloorsSnapshot();
            if (snapshot != null) {
                for (List<ParkingSlot> slots : snapshot.values()) {
                    if (slots == null) continue;
                    for (ParkingSlot slot : slots) {
                        Vehicle v = slot.getCurrentVehicle();
                        if (v == null) continue;
                        String plate = v.getPlateNumber();
                        String type = v.getType();
                        String entry = formatMillis(slot.getEntryTime());
                        String line = plate + " | " + type + " | " + entry;
                        bw.write(line);
                        bw.newLine();
                        written++;
                    }
                }
            }
            bw.flush();
            System.out.println("Exported " + written + " parked record(s) to " + out.getPath());
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
            if (s == null || s.isEmpty()) return d;
            return Double.parseDouble(s);
        } catch (Exception ex) {
            return d;
        }
    }

    private int parseIntOrDefault(String s, int d) {
        try {
            if (s == null || s.isEmpty()) return d;
            return Integer.parseInt(s);
        } catch (Exception ex) {
            return d;
        }
    }

    private String safeLower(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    // Human-readable formatter for epoch millis using system default zone
    private String formatMillis(long epochMillis) {
        if (epochMillis <= 0L) return "N/A";
        try {
            return HUMAN_TS_FMT.format(Instant.ofEpochMilli(epochMillis));
        } catch (Exception ex) {
            return String.valueOf(epochMillis);
        }
    }

    // Helper used for saving text fields
    private String safeForFile(String s) {
        if (s == null) return "";
        return s.replace("|", " ").replace("\n", " ").trim();
    }

    // --- new helper flows exposed for programmatic use ---

    // Remove vehicle immediately without billing. Returns the removed Vehicle or null.
    public Vehicle pullOutOnly(String plate) {
        if (plate == null || plate.isEmpty()) return null;
        return parkingLot.removeVehicleByPlate(plate);
    }

    /**
     * Bill the vehicle then remove it on successful payment.
     * Returns a printable receipt on success, or an error message on failure.
     */
    public String billThenPullOut(String plate, String method, double cashGiven, String cardNumber, String cardHolder) {
        if (plate == null || plate.isEmpty()) return "Invalid plate";

        ParkingSlot slot = parkingLot.findSlotByPlate(plate);
        if (slot == null) return "Plate not found in parking lot";

        Vehicle v = slot.getCurrentVehicle();
        if (v == null) return "No vehicle in slot for plate";

        long entry = slot.getEntryTime();
        long exit = System.currentTimeMillis();

        Payment payment;
        if ("CASH".equalsIgnoreCase(method)) {
            payment = new CashPayment(0.0, cashGiven);
        } else if ("CARD".equalsIgnoreCase(method)) {
            payment = new CardPayment(0.0, cardNumber, cardHolder);
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
        private final String owner;
        private final double height;
        private final int engineCc;
        private final boolean pwd;

        VehicleRecord(String plate, String type, String owner, double height, int engineCc, boolean pwd) {
            this.plate = plate;
            this.type = type;
            this.owner = owner;
            this.height = height;
            this.engineCc = engineCc;
            this.pwd = pwd;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Plate: ").append(plate);
            sb.append(", Type: ").append(type);
            if (owner != null && !owner.isEmpty()) {
                sb.append(", Owner: ").append(owner);
            }
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
