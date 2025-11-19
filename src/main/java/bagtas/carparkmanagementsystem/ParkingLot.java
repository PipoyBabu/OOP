/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bagtas.carparkmanagementsystem;

/**
 *
 * @author unit 1
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ParkingLot
 *
 * - Manages up to MAX_FLOORS floors, configurable via floorDistribution[][]
 * - Keeps quick lookup plate -> ParkingSlot for O(1) plate searches
 * - Exposes read-only snapshots for UI and storage
 */
public class ParkingLot {
    public static final int MAX_FLOORS = 3;
    public static final double DEFAULT_CLEARANCE_M = 2.1;

    // floorDistribution[floorIndex][slotTypeIndex]
    // slotTypeIndex mapping: 0 = Scooter, 1 = EV, 2 = Car, 3 = PWD
    // Row 0 corresponds to floor 1, row 1 -> floor 2, row 2 -> floor 3
    private static final int[][] floorDistribution = new int[][] {
        // floor 1: scooters, ev, cars, pwd (matches previous: 32,5,38,5)
        {32, 5, 38, 5},
        // floor 2: scooters, ev, cars, pwd (previously 0,5,70,5)
        {0, 5, 70, 5},
        // floor 3: same as floor 2
        {0, 5, 70, 5}
    };

    // mapping floorNumber -> list of slots on that floor
    private final Map<Integer, List<ParkingSlot>> floors;

    // quick lookup of which slot a plate is parked in
    private final Map<String, ParkingSlot> plateToSlot;

    public ParkingLot() {
        this.floors = new HashMap<>();
        this.plateToSlot = new HashMap<>();
        initializeSlots();
    }

    // Initialize slots according to the distribution table
    private void initializeSlots() {
        for (int floor = 1; floor <= MAX_FLOORS; floor++) {
            int[] dist;
            if (floor - 1 < floorDistribution.length) {
                dist = floorDistribution[floor - 1];
                // Ensure dist length of 4
                if (dist == null || dist.length < 4) {
                    dist = new int[] {0, 5, 70, 5}; // safe fallback
                }
            } else {
                // fallback: same as level 2/3
                dist = new int[] {0, 5, 70, 5};
            }

            List<ParkingSlot> slotList = new ArrayList<>();
            int slotId = 1;

            // Scooter slots
            for (int i = 0; i < Math.max(0, dist[0]); i++) {
                slotList.add(new ScooterSlot(slotId++, floor, DEFAULT_CLEARANCE_M));
            }
            // EV slots
            for (int i = 0; i < Math.max(0, dist[1]); i++) {
                slotList.add(new EVSlot(slotId++, floor, DEFAULT_CLEARANCE_M));
            }
            // Car slots
            for (int i = 0; i < Math.max(0, dist[2]); i++) {
                slotList.add(new CarSlot(slotId++, floor, DEFAULT_CLEARANCE_M));
            }
            // PWD slots
            for (int i = 0; i < Math.max(0, dist[3]); i++) {
                slotList.add(new PWDSlot(slotId++, floor, DEFAULT_CLEARANCE_M));
            }

            floors.put(floor, slotList);
        }
    }

    // Find the first available slot appropriate for the vehicle
    public ParkingSlot findAvailableSlot(Vehicle v) {
        if (v == null) return null;

        // Prefer PWD slots when driver is PWD
        if (v.isPwdDriver()) {
            for (int f = 1; f <= MAX_FLOORS; f++) {
                List<ParkingSlot> slots = floors.get(f);
                if (slots == null) continue;
                for (ParkingSlot s : slots) {
                    if (s instanceof PWDSlot && !s.isOccupied() && s.canFit(v)) {
                        return s;
                    }
                }
            }
        }

        // EVs go to EVSlot only
        if ("EV".equalsIgnoreCase(v.getType())) {
            for (int f = 1; f <= MAX_FLOORS; f++) {
                List<ParkingSlot> slots = floors.get(f);
                if (slots == null) continue;
                for (ParkingSlot s : slots) {
                    if (s instanceof EVSlot && !s.isOccupied() && s.canFit(v)) {
                        return s;
                    }
                }
            }
        }

        // Motorcycles: small prefers ScooterSlot; large uses CarSlot
        if (v instanceof Motorcycle m) {
            if (m.getEngineCC() < 400) {
                for (int f = 1; f <= MAX_FLOORS; f++) {
                    List<ParkingSlot> slots = floors.get(f);
                    if (slots == null) continue;
                    for (ParkingSlot s : slots) {
                        if (s instanceof ScooterSlot && !s.isOccupied() && s.canFit(v)) {
                            return s;
                        }
                    }
                }
            }
            // large motorcycles use CarSlot
            for (int f = 1; f <= MAX_FLOORS; f++) {
                List<ParkingSlot> slots = floors.get(f);
                if (slots == null) continue;
                for (ParkingSlot s : slots) {
                    if (s instanceof CarSlot && !s.isOccupied() && s.canFit(v)) {
                        return s;
                    }
                }
            }
        }

        // Cars and fallback search CarSlot
        for (int f = 1; f <= MAX_FLOORS; f++) {
            List<ParkingSlot> slots = floors.get(f);
            if (slots == null) continue;
            for (ParkingSlot s : slots) {
                if (s instanceof CarSlot && !s.isOccupied() && s.canFit(v)) {
                    return s;
                }
            }
        }

        return null;
    }

    // Attempt to park the vehicle. Returns true on success.
    public boolean parkVehicle(Vehicle v) {
        if (v == null) return false;
        // prevent duplicate plate parking
        if (plateToSlot.containsKey(v.getPlateNumber())) return false;

        ParkingSlot slot = findAvailableSlot(v);
        if (slot == null) return false;

        boolean parked = slot.parkVehicle(v);
        if (parked) {
            plateToSlot.put(v.getPlateNumber(), slot);
            return true;
        } else {
            return false;
        }
    }

    // Remove parked vehicle by plate number. Returns the removed vehicle or null.
    public Vehicle removeVehicleByPlate(String plateNumber) {
        if (plateNumber == null) return null;
        ParkingSlot slot = plateToSlot.get(plateNumber);
        if (slot == null) return null;
        Vehicle removed = slot.removeVehicle();
        // Always remove mapping to keep state consistent
        plateToSlot.remove(plateNumber);
        return removed;
    }

    // Lookup which slot (if any) a plate is currently occupying
    public ParkingSlot findSlotByPlate(String plateNumber) {
        if (plateNumber == null) return null;
        return plateToSlot.get(plateNumber);
    }

    // Get available slot counts per type on a given floor
    // Returns a map with keys: "CarSlot","ScooterSlot","EVSlot","PWDSlot"
    public Map<String, Integer> getAvailableCountsForFloor(int floorNumber) {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("CarSlot", 0);
        counts.put("ScooterSlot", 0);
        counts.put("EVSlot", 0);
        counts.put("PWDSlot", 0);

        List<ParkingSlot> slots = floors.get(floorNumber);
        if (slots == null) return counts;

        for (ParkingSlot s : slots) {
            if (!s.isOccupied()) {
                String key = s.getSlotType();
                counts.put(key, counts.getOrDefault(key, 0) + 1);
            }
        }
        return counts;
    }

    // Get total available slots across all floors
    public int getTotalAvailableSlots() {
        int total = 0;
        for (int f = 1; f <= MAX_FLOORS; f++) {
            List<ParkingSlot> slots = floors.get(f);
            if (slots == null) continue;
            for (ParkingSlot s : slots) {
                if (!s.isOccupied()) total++;
            }
        }
        return total;
    }

    // Expose internal floors for read-only iteration (copy of lists)
    public Map<Integer, List<ParkingSlot>> getFloorsSnapshot() {
        Map<Integer, List<ParkingSlot>> copy = new HashMap<>();
        for (Map.Entry<Integer, List<ParkingSlot>> e : floors.entrySet()) {
            copy.put(e.getKey(), new ArrayList<>(e.getValue()));
        }
        return copy;
    }

    // Flatten and return all slots across floors as an unmodifiable list
    public Iterable<ParkingSlot> getAllSlots() {
        List<ParkingSlot> flat = new ArrayList<>();
        for (Map.Entry<Integer, List<ParkingSlot>> e : floors.entrySet()) {
            flat.addAll(e.getValue());
        }
        return Collections.unmodifiableList(flat);
    }
    
    public void parkOrThrow(Vehicle v) {
    if (v == null) throw new IllegalArgumentException("vehicle null");
    // Global height guard: disallow vehicles taller than the lot's default clearance
    if (v.getHeight() > DEFAULT_CLEARANCE_M) {
        throw new InvalidVehicleHeightException("Vehicle height " + v.getHeight() + "m exceeds lot maximum clearance of " + DEFAULT_CLEARANCE_M + "m");
    }
    if (plateToSlot.containsKey(v.getPlateNumber())) {
        throw new SlotUnavailableException("Duplicate plate parked: " + v.getPlateNumber());
    }
    ParkingSlot slot = findAvailableSlot(v);
    if (slot == null) {
        throw new SlotUnavailableException("No suitable slot available for: " + v.getPlateNumber());
    }
    boolean parked = slot.parkVehicle(v);
    if (!parked) throw new SlotUnavailableException("Slot refused vehicle: " + v.getPlateNumber());
    plateToSlot.put(v.getPlateNumber(), slot);
}

    public Vehicle removeVehicleOrThrow(String plateNumber) throws VehicleNotFoundException {
    if (plateNumber == null) throw new IllegalArgumentException("plate null");
    ParkingSlot slot = plateToSlot.get(plateNumber);
    if (slot == null) throw new VehicleNotFoundException("No parked vehicle for plate: " + plateNumber);
    Vehicle removed = slot.removeVehicle();
    plateToSlot.remove(plateNumber);
    return removed;
}
}
