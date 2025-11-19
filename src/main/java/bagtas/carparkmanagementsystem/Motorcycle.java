package bagtas.carparkmanagementsystem;

/**
 *
 * @author unit 1
 */
public class Motorcycle extends Vehicle {
    // unique attribute
    private final int engineCC;

    // primary constructor (non-PWD)
    public Motorcycle(String plateNumber, double height, int engineCC) {
        super(plateNumber, height);
        this.engineCC = engineCC;
    }

    // optional constructor with PWD flag
    public Motorcycle(String plateNumber, double height, int engineCC, boolean pwdDriver) {
        super(plateNumber, height, pwdDriver);
        this.engineCC = engineCC;
    }

    // getter
    public int getEngineCC() {
        return engineCC;
    }

    // size helper used by allocation/billing logic
    public boolean isLargeMotorcycle() {
        return this.engineCC >= 400;
    }

    // human-friendly display
    @Override
    public String getType() {
        if (engineCC < 400) {
            return "Small Motorcycle/Scooter";
        } else {
            return "Large Motorcycle/Big Bike";
        }
    }

    @Override
    public double getBasePrice() {
        return (engineCC < 400) ? 25.0 : 50.0;
    }

    @Override
    public int getBaseHours() {
        return (engineCC < 400) ? 8 : 3;
    }

    @Override
    public double getRatePerHour() {
        return (engineCC < 400) ? 10.0 : 20.0;
    }

    @Override
    public double getOvernightFee() {
        return (engineCC < 400) ? 200.0 : 300.0;
    }
}