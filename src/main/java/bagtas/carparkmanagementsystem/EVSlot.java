/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bagtas.carparkmanagementsystem;

/**
 *
 * @author unit 1
 */
public class EVSlot extends ParkingSlot {

    // All EV slots in this design include a charger
    public EVSlot(int slotNumber, int floorNumber, double heightClearance) {
        super(slotNumber, floorNumber, heightClearance);
    }

    @Override
    public String getSlotType() {
        return "EVSlot";
    }

    @Override
    public boolean canFit(Vehicle v) {
        if (v == null) return false;
        if (isOccupied()) return false;
        if (v.getHeight() > getHeightClearance()) return false;

        return "EV".equalsIgnoreCase(v.getType());
    }
}