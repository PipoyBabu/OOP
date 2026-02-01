/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bagtas.carparkmanagementsystem;

/**
 *
 * @author unit 1
 */
public abstract class ParkingSlot {
    // --- Private attributes ---
    private final int slotNumber;
    private final int floorNumber;
    private final double heightClearance;
    private boolean occupied;
    private long entryTime;
    private Vehicle currentVehicle;

    // --- Constructor ---
    public ParkingSlot(int slotNumber, int floorNumber, double heightClearance) {
        this.slotNumber = slotNumber;
        this.floorNumber = floorNumber;
        this.heightClearance = heightClearance;
        this.entryTime = 0L;
        this.occupied = false;
        this.currentVehicle = null;
    }

    // --- Getters ---
    public int getSlotNumber() { return slotNumber; }
    public int getFloorNumber() { return floorNumber; }
    public double getHeightClearance() { return heightClearance; }
    public boolean isOccupied() { return occupied; }
    public Vehicle getCurrentVehicle() { return currentVehicle; }
    public long getEntryTime() { return entryTime; }

    // --- Mutators used by ParkingLot ---
    // Attempt to park the vehicle in this slot. Return true on success.
    public synchronized boolean parkVehicle(Vehicle v) {
        if (v == null) return false;
        if (this.occupied) return false;
        if (!canFit(v)) return false;
        this.currentVehicle = v;
        this.occupied = true;
        this.entryTime = System.currentTimeMillis();
        return true;
    }

    // Remove and return the parked vehicle, or null if none
    public synchronized Vehicle removeVehicle() {
        if (!this.occupied || this.currentVehicle == null) {
            this.currentVehicle = null;
            this.occupied = false;
            this.entryTime = 0L;
            return null;
        }
        Vehicle removed = this.currentVehicle;
        this.currentVehicle = null;
        this.occupied = false;
        this.entryTime = 0L;
        return removed;
    }

    // Restore a parked vehicle into this slot with a provided entry time.
    // Intended for system restore/load operations. Returns true on success.
    public synchronized boolean restoreVehicle(Vehicle v, long entryTimeMillis) {
        if (v == null) return false;
        if (this.occupied) return false;
        if (!canFit(v)) return false;
        this.currentVehicle = v;
        this.occupied = true;
        this.entryTime = entryTimeMillis <= 0L ? System.currentTimeMillis() : entryTimeMillis;
        return true;
    }

    // --- Abstract methods ---
    public abstract String getSlotType();
    public abstract boolean canFit(Vehicle v);
}