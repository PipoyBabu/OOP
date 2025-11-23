Polymorphism usage (where subtype objects are used via base types / overridden behavior)

- `Vehicle` polymorphism
  - `Vehicle` is abstract and code treats vehicles polymorphically via `Vehicle` references.
  - `Car` and `Motorcycle` implement `getType()` and pricing hooks (`getBasePrice()`, `getBaseHours()`, `getRatePerHour()`, `getOvernightFee()`), used by `BillingService.computeFee(...)`.
  - `ParkingLot.findAvailableSlot(...)` and other code use `Vehicle` references to call `getHeight()`, `isPwdDriver()`, and pricing behavior is resolved by the concrete vehicle type.

- `ParkingSlot` polymorphism
  - `ParkingSlot` is abstract and the system holds and iterates `ParkingSlot` references.
  - Concrete implementations (`CarSlot`, `EVSlot`, `ScooterSlot`, `PWDSlot`) override `canFit(Vehicle)` and `getSlotType()`.
  - `ParkingLot` and `CarParkManagementSystem` treat slots generically and rely on runtime dispatch of `canFit` and `getSlotType`.

- `Payment` polymorphism
  - `Payment` (abstract) is used as the parameter type for `BillingService.payAndCreateTransaction(...)`.
  - Concrete classes `CashPayment` and `CardPayment` implement `process()` differently (cash checks amount and returns change; card validates card and PIN).
  - `BillingService.payAndCreateTransaction(...)` reconstructs payments (e.g., building a `CashPayment` or `CardPayment`) and calls `process()` on the `Payment` polymorphic reference.

- General use-cases
  - Collections and method parameters frequently use base types such as `Vehicle` and `ParkingSlot`, enabling runtime substitution of concrete subtypes.
  - `instanceof` checks appear in some places (e.g., identifying `EV`, `Motorcycle`) when behavior must branch on concrete type.

Notes:
- Polymorphism is used for both behavioral specialization (pricing, slot-fit logic, payment processing) and for simplifying APIs (single `BillingService` that accepts `Payment`).