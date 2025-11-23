CLASSES in this project

- `CarParkManagementSystem` — Main CLI controller and orchestrator for menus, flows, and user I/O.
- `ParkingLot` — Manages floors and slots, plate↔slot lookup and allocation.
- `ParkingSlot` (abstract) — Base slot behavior (park/remove, entry time, occupancy).
- `CarSlot` — Concrete `ParkingSlot` for cars.
- `EVSlot` — Concrete `ParkingSlot` for EVs.
- `ScooterSlot` — Concrete `ParkingSlot` for scooters/small bikes.
- `PWDSlot` — Concrete `ParkingSlot` for PWD slots.
- `Vehicle` (abstract) — Base vehicle (plate, height, PWD flag, pricing hooks).
- `Car` — Concrete `Vehicle` for cars.
- `Motorcycle` — Concrete `Vehicle` for motorcycles/scooters.
- `Payment` (abstract) — Base payment abstraction (amount, cash/card details).
- `BasePayment` — Helper base that provides utility methods used by payments.
- `CashPayment` — Concrete `Payment` for cash.
- `CardPayment` — Concrete `Payment` for card payments.
- `PaymentResult` — Immutable result returned by payment processing.
- `BillingService` — Computes fees and orchestrates payment → Transaction creation.
- `Transaction` — Immutable record representing a completed billing transaction.
- `ReceiptPrinter` — Renders/returns human-readable receipt strings.
- `StorageService` — Simple file-based persistence for transactions/receipts.
- `VehicleRecord` (inner class in `CarParkManagementSystem`) — simple DTO for registry entries.

Exceptions & errors (custom classes):
- `StorageException` — I/O wrapper for storage operations.
- `SlotUnavailableException` — Runtime exception when slot allocation fails.
- `VehicleNotFoundException` — Thrown when trying to remove/search non-existent parked vehicle.
- `InvalidVehicleTypeException` — Validation error for unsupported type during registration.
- `InvalidVehicleHeightException` — Validation error when height exceeds lot clearance.
- `InvalidPaymentException` — Payment method/processing error.

Utility / support classes:
- (none beyond listed)