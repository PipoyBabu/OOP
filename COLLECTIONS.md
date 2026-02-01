Collections used in the project (types + locations / notes)

- `Map` and implementations
  - `Map<String, VehicleRecord>` — `CarParkManagementSystem.registry` (uses `LinkedHashMap` to preserve insertion order).
  - `Map<Integer, List<ParkingSlot>> floors` — `ParkingLot` (stores slots per floor using `HashMap`).
  - `Map<String, Integer>` — `ParkingLot.getAvailableCountsForFloor(...)` uses a `HashMap` to return counts by slot type.
  - `Map<Integer, List<ParkingSlot>>` snapshots returned by `ParkingLot.getFloorsSnapshot()`.

- `List` and implementations
  - `List<ParkingSlot>` — used per floor inside `ParkingLot` (implementation: `ArrayList`).
  - `List<ParkingSlot>` snapshots and flattened lists created (`ArrayList`).

- `LinkedHashMap` — used for `registry` in `CarParkManagementSystem` to keep predictable iteration order.

- `Iterable<ParkingSlot>` — `ParkingLot.getAllSlots()` returns an unmodifiable `List` as an `Iterable`.

- Other collection utilities
  - `Collections.unmodifiableList(...)` usage (in `ParkingLot.getAllSlots()`), `Map.entrySet()` iteration used when building snapshots.

Notes:
- Most collections are from `java.util` and use `HashMap`/`ArrayList` for typical mutable storage. LinkedHashMap is used where iteration order matters.
- There are no usage of concurrent collections in the codebase; thread-safety is handled via `synchronized` at slot-level for park/remove operations.