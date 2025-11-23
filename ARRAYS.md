Arrays used in the project

- `int[][] floorDistribution` — `ParkingLot` (static configuration for slot counts per floor). Example:
  - `private static final int[][] floorDistribution = new int[][] { {32,5,38,5}, {0,5,70,5}, {0,5,70,5} }`.

- `int[] dist` — `ParkingLot.initializeSlots()` uses temporary `int[]` to hold distribution for a floor.

- `new int[] { ... }` — fallback distributions in `ParkingLot`.

- `Transaction[] out = new Transaction[1];` — used as an output parameter pattern in `BillingService.payAndCreateTransaction(...)` and in controller payment flows.

- `String[] parts` — in `CarParkManagementSystem.loadStateFlow()` used to parse pipe- or comma-delimited lines when loading saved `vehicles.txt` (parsing file lines into `String[]`).

- `String[] args` — `public static void main(String[] args)` signature in `CarParkManagementSystem`.

Notes:
- Arrays are used sparingly and primarily for fixed small structures (floor distribution), simple parsing, and the `Transaction[]` out-parameter pattern. The code prefers `List`/`Map` for collection storage.