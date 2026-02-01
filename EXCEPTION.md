Exception handling and custom exceptions

Custom exception classes in the project:
- `StorageException` (extends `Exception`)
  - Used by `StorageService` to wrap `IOException` and surface storage errors.

- `SlotUnavailableException` (extends `RuntimeException`)
  - Thrown by `ParkingLot.parkOrThrow(...)` when allocation fails (no suitable slot, duplicate plate, etc.).

- `VehicleNotFoundException` (extends `Exception`)
  - Thrown by `ParkingLot.removeVehicleOrThrow(...)` when attempting to remove a vehicle not present.

- `InvalidVehicleTypeException` (extends `RuntimeException`)
  - Thrown by `CarParkManagementSystem.validateVehicleType(...)` and used to signal unsupported registration types.

- `InvalidVehicleHeightException` (extends `RuntimeException`)
  - Thrown by `ParkingLot.parkOrThrow(...)` when vehicle height exceeds allowed clearance.

- `InvalidPaymentException` (extends `RuntimeException`)
  - Used by `CarParkManagementSystem.paymentFlow(...)` to represent invalid or unsupported payment method choices.

Where exceptions are used (examples):
- `StorageService.appendTransaction(...)` and `appendReceipt(...)` throw `StorageException` when file operations fail — callers handle it in `tryPersistTransaction(...)`.
- `ParkingLot.parkOrThrow(...)` throws `SlotUnavailableException` and `InvalidVehicleHeightException` for allocation errors — `parkVehicleFlow()` catches generic exceptions and reports to the user.
- `CarParkManagementSystem` commonly wraps I/O and parse code in try/catch and prints friendly error messages.

General exception patterns observed:
- The code uses checked exceptions for storage I/O (`StorageException`) and uses runtime exceptions for domain validation failures (invalid type/height/payment) and slot allocation failures.
- `try`/`catch` blocks are used in CLI flows to prevent the app from crashing and to show messages.
- `Transaction` creation and payment orchestration uses return objects (`PaymentResult`) to convey success/failure rather than throwing exceptions from the payment implementations.

Recommendations (optional):
- Consider making consistent choices between checked vs unchecked domain exceptions (either prefer checked for recoverable problems or runtime for programmer errors).
- Provide richer error messages and an operator-level log for persistence failures to help recover failed transactions.