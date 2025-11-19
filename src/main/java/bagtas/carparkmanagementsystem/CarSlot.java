/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bagtas.carparkmanagementsystem;

/**
 *
 * @author unit 1
 */
public class CarSlot extends ParkingSlot {

    public CarSlot(int slotNumber, int floorNumber, double heightClearance) {
        super(slotNumber, floorNumber, heightClearance);
    }

    @Override
    public String getSlotType() {
        return "CarSlot";
    }

    @Override
    public boolean canFit(Vehicle v) {
        if (v == null) {
            return false;
        }
        if (isOccupied()) {
            return false;
        }
        if (v.getHeight() > getHeightClearance()) {
            return false;
        }

        String type = v.getType();
        if ("Car".equalsIgnoreCase(type) || "EV".equalsIgnoreCase(type)) {
            return true;
        }

        if (v instanceof Motorcycle m) {
            return m.getEngineCC() >= 400;
        }

        return false;
    }
}