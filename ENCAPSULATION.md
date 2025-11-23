Encapsulation in the project

- Use of private fields with public getters (and setters where needed):
  - `Vehicle` fields (`plateNumber`, `height`, `pwdDriver`) are `private` with `getPlateNumber()`, `getHeight()`, `isPwdDriver()`, `setPwdDriver()`.
  - `ParkingSlot` fields (`slotNumber`, `floorNumber`, `heightClearance`, `occupied`, `entryTime`, `currentVehicle`) are `private` with public getters and controlled mutators (`parkVehicle`, `removeVehicle`). The mutators are `synchronized` to protect concurrent access.
  - `Transaction` is immutable: all fields are `private final` with only getters — this enforces read-only access after creation.
  - `Payment` exposes read-only accessors for amount, card number, card holder and card PIN; concrete `Payment` implementations call protected helpers from `BasePayment`.

- Controller encapsulation:
  - `CarParkManagementSystem` holds private maps/registries (`registry` LinkedHashMap) and exposes controlled flows via menu methods.
  - `VehicleRecord` is an inner private static-like record used only by the controller for registry storage.

- Storage/IO encapsulation:
  - `StorageService` encapsulates file operations; callers invoke `appendTransaction` / `appendReceipt` instead of handling files directly.

- Exception handling encapsulation:
  - Custom exceptions (`StorageException`, `InvalidPaymentException`, etc.) encapsulate error conditions and provide semantic meaning to callers.

Notes / observations:
- Most state is kept private and manipulated through methods, which follows encapsulation best practices.
- Some places still perform `instanceof` checks for type-based branching rather than using polymorphic dispatch; this is a design choice but does not break encapsulation per se.
- `CarParkManagementSystem` accesses some internals via public getters (e.g., `ParkingSlot.getCurrentVehicle()`), which is appropriate for a controller that needs runtime status.