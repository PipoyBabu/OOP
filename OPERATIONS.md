OPERATIONS - Parking Management System

Overview

This document explains, in simple words, what each menu and submenu in the CLI does and how the main processes work. It is written for a non-technical operator or a new developer who wants to understand how the program behaves.

Main Menu (what you see after starting the program)

- VEHICLE RECORDS: Manage the list of vehicles allowed to use the parking lot.
- PARKING OPERATIONS: Park vehicles and pull them out (the actual parking actions).
- OCCUPANCY & TRACKING: See who is parked and simple counts and searches.
- BILLS & PAYMENTS: Check price rules, calculate fees, and process payments.
- DATA STORAGE: Save or load application data and export parked lists.
- EXIT: Close the program.

1) VEHICLE RECORDS

- REGISTER VEHICLE
  - Purpose: Add a vehicle to the program's registry so it can be parked later.
  - How it works: Operator types the plate number, vehicle type (car, motorcycle, ev, scooter), height (meters), engine CC (for motorcycles) and whether the driver is PWD.
  - Notes: The system validates the type and height. "scooter" is treated as a small motorcycle internally.

- DELETE VEHICLE
  - Purpose: Remove a vehicle from the registry.
  - How it works: Operator enters the plate number to remove.

2) PARKING OPERATIONS

- PARK VEHICLE
  - Purpose: Put a registered vehicle into a free slot.
  - How it works:
    - Operator supplies the plate number (vehicle must be registered first).
    - The program creates a Vehicle object from the registry data.
    - The program checks the vehicle height and whether it is already parked.
    - The ParkingLot finds a suitable slot (car, EV slot, motorcycle/scooter or PWD slot) and marks the vehicle as parked with an entry time.
  - Important checks: Prevent duplicate plates in the lot; reject vehicles taller than the lot clearance.

- PULL-OUT VEHICLE
  - Purpose: Prepare a vehicle to leave the lot and run the billing flow.
  - How it works:
    - Operator enters the plate number.
    - The system locates the parked vehicle and calculates the fee.
    - Payment is processed first; if payment is successful the vehicle is removed from its slot.
    - A receipt is printed and persisted when possible.
  - Why payment first: This avoids losing a parked vehicle record if the payment fails.

3) OCCUPANCY & TRACKING

- SHOW SLOTS WITH STATUS
  - Purpose: Display every slot on every floor and whether it is VACANT or OCCUPIED.
  - Shown info: Floor number, slot number, slot type, status, and plate (if occupied).

- COUNT VEHICLES BY TYPE
  - Purpose: Give a simple summary of how many cars, EVs, scooters, and big motorcycles are parked.
  - How it works: Inspects each parked vehicle and counts by type or infers type from slot.

- SEARCH VEHICLE BY PLATE NO.
  - Purpose: Find registry details and current parking location for a plate.
  - How it works: Shows registry entry (if any) and slot details (floor, slot, entry time) if parked.

4) BILLS & PAYMENTS

- SHOW FEE RULES
  - Purpose: Display the pricing rules and sample values for each vehicle type.
  - How it works: Prints base price, included hours, rate per hour, overnight fee and the PWD discount rule.

- COMPUTE BILL
  - Purpose: Show how much a currently parked vehicle would pay now (without actually charging).
  - How it works: Operator enters plate; the program uses the vehicle's entry time and current time to compute the fee.

- PAYMENT
  - Purpose: Process payment for a parked vehicle and remove it from the lot.
  - How it works (interactive):
    - Operator provides the plate and confirms they want to proceed.
    - The system calculates the fee and asks for a payment method.
    - CASH: Operator enters cash amount. The system constructs a `CashPayment` and processes it.
    - CARD: Operator enters card number, card holder and a 6-digit PIN. The system constructs a `CardPayment` and processes it.
    - The `BillingService` runs the payment and generates a `Transaction` record.
    - If payment succeeds, the system renders a receipt, attempts to persist it, and then removes the vehicle from the slot.
  - Notes: The program validates PIN format (6 digits) and reports failures. The receipt is printed to screen and persisted if possible.

5) DATA STORAGE

- SAVE
  - Purpose: Save current registry and parked vehicles to disk so they can be restored later.
  - Files written:
    - `data/vehicles.txt` — the registered vehicle list (human-friendly pipe-delimited columns). Header lines start with `#` so the loader can skip them.
    - `data/parked.txt` — the currently parked vehicles (plate, type, height, engineCc, pwd flag, floor, slot number, entry time). Header lines start with `#`.
  - Notes: Values are padded for readability. Entry times in `parked.txt` are written as human timestamps where possible.

- LOAD
  - Purpose: Read `data/vehicles.txt` and `data/parked.txt` to restore registry and parked vehicles.
  - How it works:
    - `vehicles.txt` lines are parsed (pipe or comma delimited). Malformed lines are skipped.
    - `parked.txt` lines are parsed; each line results in creating a Vehicle object and calling `ParkingLot.restoreParkedVehicle(floor, slot, vehicle, entryMillis)` to put it back in the correct slot with original entry time.

- EXPORT FILE
  - Purpose: Create a simple text list of currently parked vehicles in `exports/parked.txt` (plate, type, entry time).
  - How it works: A readable report is written under `exports/parked.txt`.

6) EXIT

- Purpose: End the program.
- How it works: Choose Exit from the main menu or type `exit` from the main prompt.

Other helpful operations and helpers

- Internal payment helper (`paymentFlow`): Shared code used both by the interactive PULL-OUT flow and the PAYMENT menu. It computes the fee, asks for payment method, processes payment through `BillingService`, renders the receipt and persists it with the `StorageService`.

- Persistence and receipts
  - `StorageService` stores transaction entries and can write a human-readable receipt file. The system attempts to persist each successful transaction; failures are reported but do not crash the program.
  - Receipt messages are printed to the console for the operator.

- Vehicle types and mapping
  - `car`: Uses car slots.
  - `ev`: Treated as a `Car` subclass with `getType()` returning `EV`. Uses EV slots when available.
  - `motorcycle`: Uses motorcycle slots. Engine CC determines small vs big motorcycle for counting.
  - `scooter`: Normalized to `motorcycle` at registration; treated as a small motorcycle internally.

- Important business rules and safety checks (short)
  - Vehicles must be registered before parking.
  - Vehicle height is required and checked against the lot clearance (`ParkingLot.DEFAULT_CLEARANCE_M`).
  - Duplicate plates cannot be parked twice.
  - Payment is processed before physically removing a vehicle from its slot.
  - Load/save files are tolerant: headers are ignored and both pipe `|` and comma `,` formats are supported.

Programmer notes (for maintainers)

- Programmatic helper flows exist for automation or tests:
  - `pullOutOnly(String plate)` — removes a parked vehicle without billing.
  - `billThenPullOut(String plate, String method, double cashGiven, String cardNumber, String cardHolder)` — computes fee, performs payment (cash or card), removes the vehicle, and returns a receipt string or an error message.

Where to look in code

- `CarParkManagementSystem.java` — menu definitions and all user flows.
- `ParkingLot.java` / `ParkingSlot` and concrete slot types — allocation and slot rules.
- `Vehicle`, `Car`, `Motorcycle` — vehicle data and pricing rules.
- `BillingService.java` — calculates fees and coordinates payment.
- `Payment`, `CashPayment`, `CardPayment` — payment objects and processing.
- `StorageService.java` — transaction persistence and receipt writing.

If you want, I can also:

- Convert `EXPORT` into a timestamped CSV file under `exports/`.
- Add a short quick-start checklist for operators (what to press in which order).

---
