File-handling imports and locations

The code uses `java.io` and `java.nio` packages for file I/O. Below are the imports related to file operations found in the project and where they appear.

Common java.io imports
- `java.io.BufferedReader` — used in `CarParkManagementSystem.loadStateFlow()` to read `data/vehicles.txt`.
- `java.io.BufferedWriter` — used in `CarParkManagementSystem.saveStateFlow()` and `StorageService.appendTransaction/appendReceipt(...)` to write files.
- `java.io.IOException` — checked exception handled around file operations in `CarParkManagementSystem` and `StorageService`.

Common java.nio imports
- `java.nio.charset.StandardCharsets` — used when creating readers/writers to specify UTF-8 encoding (`CarParkManagementSystem`, `StorageService`).
- `java.nio.file.Files` — used for existence checks, `newBufferedReader`/`newBufferedWriter`, and directory creation (`CarParkManagementSystem`, `StorageService`).
- `java.nio.file.Path` — used for representing file paths (`CarParkManagementSystem`, `StorageService`).
- `java.nio.file.Paths` — used to build `Path` instances (`CarParkManagementSystem`, `StorageService`).
- `java.nio.file.StandardOpenOption` — used by `StorageService.appendTransaction` and `appendReceipt` for `CREATE`, `APPEND` and `CREATE_NEW` options.

Where I/O occurs (files & locations)
- `data/vehicles.txt` — written/read by `CarParkManagementSystem.saveStateFlow()` and `loadStateFlow()` (registry persistence). Uses `BufferedWriter`/`BufferedReader` + `Files.newBufferedWriter`/`newBufferedReader`.
- `data/transactions.txt` — original append-index file created by `StorageService.appendTransaction(...)` (pipe-delimited lines).
- `data/transactions/` — per-transaction receipt files are now stored under this directory by `StorageService.appendReceipt(...)` (individual `.txt` files named `<timestamp>_<txId>.txt`).
- `exports/parked.txt` — exported parked report created by `CarParkManagementSystem.exportParkedFlow()`.

Other notable I/O
- Directory creation uses `Files.createDirectories(...)` and checks `Files.exists(...)` before writing.
- File writing is performed with explicit `flush()` and `try-with-resources` (`try (BufferedWriter bw = ...) { ... }`).

Recommendation / notes:
- The code is already using `java.nio` APIs (recommended). Consider adding atomic write patterns (write to temp file then move/replace) or file locking if concurrent writers are expected.
- All I/O currently uses UTF-8 (`StandardCharsets.UTF_8`) which is good for portability.