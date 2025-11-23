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
package bagtas.carparkmanagementsystem;

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

public class CarParkManagementSystem {
    private final Scanner scanner;
    private final Map<String, VehicleRecord> registry = new LinkedHashMap<>();
    private final ParkingLot parkingLot = new ParkingLot();
    private final BillingService billingService = new BillingService();
    private final StorageService storageService = new StorageService("data/transactions.txt");
    private static final DateTimeFormatter HUMAN_TS_FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    public CarParkManagementSystem() {
        this.scanner = new Scanner(System.in);
    }

    // Main loop: show menu and dispatch choices.
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

    // Print the main menu.
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

    // Vehicle records menu.
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

    // Register a vehicle in the registry.
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

        System.out.print("Vehicle type (car/motorcycle/ev) [default: car]: ");
        String type = scanner.nextLine().trim().toLowerCase();
        if (type.isEmpty()) {
            type = "car";
        }

        try {
            validateVehicleType(type);
        } catch (InvalidVehicleTypeException ive) {
            System.err.println(ive.getMessage());
            pause();
            return;
        }
        boolean normalizedFromScooter = false;
        if ("scooter".equals(type)) {
            type = "motorcycle";
            normalizedFromScooter = true;
            System.out.println("Note: 'scooter' normalized to 'motorcycle' — engine CC will determine slot.");
        }

        double height = 0.0;
        System.out.print("Height in meters (required): ");
        String h = scanner.nextLine().trim();
        Double parsed = parseDoubleStrict(h);
        try {
            if (h.isEmpty() || parsed == null) {
                throw new InvalidVehicleHeightException("Invalid or missing height: please provide numeric height in meters");
            }
            if (parsed <= 0.0) {
                throw new InvalidVehicleHeightException("Height must be greater than 0");
            }
            if (parsed > ParkingLot.DEFAULT_CLEARANCE_M) {
                throw new InvalidVehicleHeightException("Height " + parsed + "m exceeds maximum allowed clearance of " + ParkingLot.DEFAULT_CLEARANCE_M + "m");
            }
            height = parsed;
        } catch (InvalidVehicleHeightException ivhe) {
            System.err.println(ivhe.getMessage());
            pause();
            return;
        }

        int engineCc = 0;
        if ("motorcycle".equals(type)) {
            System.out.print("Engine CC (press Enter for default 150): ");
            String cc = scanner.nextLine().trim();
            engineCc = parseIntOrDefault(cc, 150);
            if (normalizedFromScooter && engineCc <= 0) engineCc = 150;
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

    // Parse double strictly or return null.
    private Double parseDoubleStrict(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Delete a registered vehicle.
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

    // Parking operations menu.
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

    // Park a vehicle using the registry data.
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

    // Pull-out flow: process payment then remove vehicle.
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

    // Process payment for a vehicle and persist receipt.
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

        String receipt = ReceiptPrinter.renderReceipt(out[0], v, pr);
        if (!tryPersistTransaction(out[0], receipt)) {
            System.err.println("Warning: transaction could not be persisted.");
        }

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

    // Occupancy tracking menu.
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

    // Show all slots and status.
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

    // Count parked vehicles by category.
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
        int bigBikes = 0;
        int evs = 0;
        int pwd = 0;
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
                    String slotType = safeLower(slot.getSlotType());
                    switch (slotType) {
                        case "evslot": evs++; break;
                        case "scooterslot": scooters++; break;
                        case "carslot": cars++; break;
                        default: unknown++; break;
                    }
                }

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

    // Search registry and parked info by plate.
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

    // Bills & payments menu.
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

    // Show fee rules and example values.
    private void showFeeRules() {
        System.out.println();
        System.out.println("FEE RULES AND POLICIES");
        System.out.println("----------------------");
        System.out.println(" - Base price and rates are defined per vehicle type (see examples).");
        System.out.println(" - Base hours included; hourly rate applies after base hours.");
        System.out.println(" - Overnight fee per full day applies for multi-day stays.");
        System.out.println(" - PWD discount: 50% off (applies after computing fee).");
        System.out.println();

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

    // Compute bill without side-effects.
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

    // Interactive payment via menu.
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

    // Persist transaction receipt via storage service.
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

    // Data storage menu (save/load/export).
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

    // Save registry and parked vehicles to data files.
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
                String h0 = padColumn("Plate", 25);
                String h1 = padColumn("Type", 25);
                String h2 = padColumn("Height", 25);
                String h3 = padColumn("EngineCc", 25);
                String h4 = padColumn("PWD", 25);
                bw.write("# " + h0 + " | " + h1 + " | " + h2 + " | " + h3 + " | " + h4);
                bw.newLine();
                for (VehicleRecord r : registry.values()) {
                    String p0 = padColumn(safeForFile(r.plate), 27);
                    String p1 = padColumn(safeForFile(r.type), 25);
                    String p2 = padColumn(String.format("%.2f", r.height), 25);
                    String p3 = padColumn(String.valueOf(r.engineCc), 25);
                    String p4 = padColumn(r.pwd ? "1" : "0", 25);
                    String line = p0 + " | " + p1 + " | " + p2 + " | " + p3 + " | " + p4;
                    bw.write(line);
                    bw.newLine();
                }
                bw.flush();
            }

            Path parkedPath = dataDir.resolve("parked.txt");
            try (BufferedWriter pbw = Files.newBufferedWriter(parkedPath, StandardCharsets.UTF_8)) {
                String hh0 = padColumn("Plate", 25);
                String hh1 = padColumn("Type", 27);
                String hh2 = padColumn("Height", 27);
                String hh3 = padColumn("EngineCc", 27);
                String hh4 = padColumn("PWD", 27);
                String hh5 = padColumn("Floor", 27);
                String hh6 = padColumn("Slot#", 27);
                String hh7 = padColumn("EntryTime", 27);
                pbw.write("# " + hh0 + " | " + hh1 + " | " + hh2 + " | " + hh3 + " | " + hh4 + " | " + hh5 + " | " + hh6 + " | " + hh7);
                pbw.newLine();
                Map<Integer, List<ParkingSlot>> snapshot = parkingLot.getFloorsSnapshot();
                if (snapshot != null) {
                    for (List<ParkingSlot> slots : snapshot.values()) {
                        if (slots == null) continue;
                        for (ParkingSlot slot : slots) {
                            Vehicle v = slot.getCurrentVehicle();
                            if (v == null) continue;
                            String plate = padColumn(safeForFile(v.getPlateNumber()), 25);
                            String type = padColumn(safeForFile(v.getType()), 27);
                            String height = padColumn(String.format("%.2f", v.getHeight()), 27);
                            String engine = "0";
                            if (v instanceof Motorcycle) engine = String.valueOf(((Motorcycle) v).getEngineCC());
                            engine = padColumn(engine, 27);
                            String pwd = padColumn(v.isPwdDriver() ? "1" : "0", 27);
                            String floor = padColumn(String.valueOf(slot.getFloorNumber()), 27);
                            String slotno = padColumn(String.valueOf(slot.getSlotNumber()), 27);
                            String entry = padColumn(formatMillis(slot.getEntryTime()), 27);
                            String line = plate + " | " + type + " | " + height + " | " + engine + " | " + pwd + " | " + floor + " | " + slotno + " | " + entry;
                            pbw.write(line);
                            pbw.newLine();
                        }
                    }
                }
                pbw.flush();
            }

            System.out.println("Saved vehicles to " + vehiclesPath.toString());
        } catch (IOException ioe) {
            System.out.println("Failed to save state: " + ioe.getMessage());
        }
        pause();
    }

    // Load saved registry and parked state.
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
                if (line.startsWith("#")) {
                    continue;
                }
                if (line.toLowerCase().startsWith("plate,")) {
                    continue;
                }

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

        Path parkedPath = Paths.get("data", "parked.txt");
        if (Files.exists(parkedPath)) {
            int restored = 0;
            int skip = 0;
            try (BufferedReader pbr = Files.newBufferedReader(parkedPath, StandardCharsets.UTF_8)) {
                String pline;
                while ((pline = pbr.readLine()) != null) {
                    pline = pline.trim();
                    if (pline.isEmpty()) continue;
                    if (pline.startsWith("#")) continue;
                    if (pline.toLowerCase().startsWith("plate")) continue;
                    String[] parts;
                    if (pline.contains("|")) parts = pline.split("\\|", -1);
                    else if (pline.contains(",")) parts = pline.split(",", -1);
                    else { skip++; continue; }
                    if (parts.length < 8) { skip++; continue; }
                    String plate = parts[0].trim();
                    String type = parts[1].trim().toLowerCase();
                    double height = parseDoubleOrDefault(parts[2].trim(), 0.0);
                    int engineCc = parseIntOrDefault(parts[3].trim(), 0);
                    boolean pwd = "1".equals(parts[4].trim()) || "true".equalsIgnoreCase(parts[4].trim());
                    int floor = parseIntOrDefault(parts[5].trim(), 1);
                    int slotno = parseIntOrDefault(parts[6].trim(), 0);
                    long entry = 0L;
                    try { entry = Long.parseLong(parts[7].trim()); } catch (Exception ignored) {}

                    Vehicle v;
                    if ("ev".equalsIgnoreCase(type)) {
                        v = new Car(plate, height, pwd) {
                            @Override public String getType() { return "EV"; }
                        };
                    } else if ("car".equalsIgnoreCase(type)) {
                        v = new Car(plate, height, pwd);
                    } else {
                        Motorcycle m = new Motorcycle(plate, height > 0.0 ? height : 1.1, engineCc > 0 ? engineCc : 150);
                        m.setPwdDriver(pwd);
                        v = m;
                    }

                    boolean ok = parkingLot.restoreParkedVehicle(floor, slotno, v, entry);
                    if (ok) restored++; else skip++;
                }
                System.out.println("Restored " + restored + " parked vehicle(s) from " + parkedPath.toString());
                if (skip > 0) System.out.println("Skipped " + skip + " parked lines due to errors.");
            } catch (IOException ioe) {
                System.out.println("Failed to load parked state: " + ioe.getMessage());
            }
        } else {
            System.out.println("No parked state file found at data/parked.txt (skipping)");
        }
        pause();
    }

    // Export parked vehicles to an exports file.
    private void exportParkedFlow() {
        System.out.println();
        System.out.println("EXPORT FILE (currently parked vehicles)");
        Path exportsDir = Paths.get("exports");
        try {
            if (!Files.exists(exportsDir)) {
                Files.createDirectories(exportsDir);
            }
        } catch (IOException ignored) {}

        Path out = exportsDir.resolve("parked_export.txt");
        int written = 0;
        try (BufferedWriter bw = Files.newBufferedWriter(out, StandardCharsets.UTF_8)) {
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

    // Wait for Enter.
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

    // Validate vehicle type.
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

    // Format epoch millis to human timestamp.
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

    private String safeForFile(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("|", " ").replace("\n", " ").trim();
    }

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

    // Remove vehicle immediately without billing.
    public Vehicle pullOutOnly(String plate) {
        if (plate == null || plate.isEmpty()) {
            return null;
        }
        return parkingLot.removeVehicleByPlate(plate);
    }

    // Bill the vehicle then remove it on successful payment.
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
