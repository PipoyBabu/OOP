Inheritance relationships (classes that extend other classes)

- `Vehicle` (abstract)
  - `Car` extends `Vehicle`
  - `Motorcycle` extends `Vehicle`

- `ParkingSlot` (abstract)
  - `CarSlot` extends `ParkingSlot`
  - `EVSlot` extends `ParkingSlot`
  - `ScooterSlot` extends `ParkingSlot`
  - `PWDSlot` extends `ParkingSlot`

- `Payment` extends `BasePayment` (abstract `Payment`)
  - `CashPayment` extends `Payment`
  - `CardPayment` extends `Payment`

- Exceptions
  - `StorageException` extends `Exception`
  - `SlotUnavailableException` extends `RuntimeException`
  - `VehicleNotFoundException` extends `Exception`
  - `InvalidVehicleTypeException` extends `RuntimeException`
  - `InvalidVehicleHeightException` extends `RuntimeException`
  - `InvalidPaymentException` extends `RuntimeException`

Notes:
- Several classes are composed rather than inherited (e.g., `CarParkManagementSystem` holds `ParkingLot`, `StorageService`, `BillingService`).
- Abstract base classes (`Vehicle`, `ParkingSlot`, `Payment`) define common APIs implemented by concrete subclasses.