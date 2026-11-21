# Laundry Service Order Tracker
A complete, robust console-based application written in **Java 21 (LTS)** for a laundry counter.

---

## 📌 Project Overview
The **Laundry Service Order Tracker** manages customer registration, laundry intake, lifecycle status updates, and sales reporting. It enforces strict business constraints, state transitions, and crash-resilient file persistence with zero third-party dependencies.

---

## 🏗️ Architecture & Class Structure

This project strictly implements **7 cohesive classes** following standard Object-Oriented Design:

| Class | Type / Role | Description |
| :--- | :--- | :--- |
| `User` | Abstract Base Class | Encapsulates `id` and `name`. Declares abstract `showProfile()`. |
| `Staff` | Child Class (`extends User`) | Built-in staff with ID 0. Implements custom `showProfile()`. |
| `Customer` | Child Class (`extends User`) | Encapsulates 10-digit `phone`. Implements custom `showProfile()`. |
| `LaundryOrder` | Entity Class | Manages `int[3]` service quantities, charge math, and strict status progression. |
| `FileManager` | Persistence Layer | Handles UTF-8 reading/writing, validation, and atomic file saves. |
| `LaundryService` | Service / Controller | Manages in-memory `ArrayList` records, `HashSet` duplicate checks, and business actions. |
| `Main` | Presentation Layer | Interactive console menus, input loops, and graceful error handling. |

### Auxiliary Classes Included:
- `ProjectTestSuite`: Automated 9-scenario verification suite.
- `PresentationLauncher`: Java tool to launch interactive presentation slides in your web browser.

---

## 💡 Core OOP Principles Demonstrated

1. **Encapsulation:**
   - Private fields across all classes.
   - Array encapsulation in `LaundryOrder` via `.clone()` in constructors and getters to prevent external modification.
2. **Inheritance:**
   - `Staff` and `Customer` extend common base class `User`.
3. **Abstraction:**
   - `User` is abstract, mandating that all user types define their own `showProfile()`.
4. **Polymorphism:**
   - Dynamic dispatch when invoking `user.showProfile()`.
5. **Composition:**
   - `LaundryOrder` contains `int[3]` quantities array.
   - `LaundryService` contains `ArrayList<Customer>`, `ArrayList<LaundryOrder>`, and `HashSet<String>`.

---

## 🗂️ Data Persistence & Files

All data is stored inside the `data/` directory:

- **`data/customers.txt`**: Format `id|name|phone`
- **`data/orders.txt`**: Format `id|customerId|qWash,qDry,qIron|status`
- **`data/report.txt`**: Formatted management summary.

### 🛡️ Crash-Resilient Atomic Writes
Every file update follows a two-phase commit:
1. Data is written to a temporary file (`.tmp`).
2. Stream is flushed and closed inside a try-with-resources block.
3. The temporary file replaces the target file atomically using `Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING)`.
4. If writing fails, in-memory updates are aborted, guaranteeing files are never left half-written or corrupted.

---

## 🚀 How to Build and Run

### Prerequisites
- **JDK 21** or later installed.
- Any Java IDE (IntelliJ IDEA, Eclipse, VS Code) or terminal.

### 1. Compile and Run via Batch Script (Windows)
Double-click `run.bat` or run:
```cmd
.\run.bat
```

### 2. Manual Terminal Commands
From the project root:
```cmd
# Compile all classes into bin/
javac -d bin -encoding UTF-8 src/com/laundry/*.java

# Run the application
java -cp bin com.laundry.Main
```

### 3. Run Automated Tests
```cmd
# Run via batch script
.\test.bat

# Or run manually
java -cp bin com.laundry.ProjectTestSuite
```

---

## 📊 Presentation Deck

An interactive slide presentation is included:
- **Web Presentation Deck:** Located at `presentation/slides.html`. Open it in any browser for a presentation experience with animations, keyboard controls (`←` / `→`), and fullscreen (`F`).
- **Launch via Java:**
  ```cmd
  java -cp bin com.laundry.PresentationLauncher
  ```
- **Markdown Slide Outline:** Located at `presentation/PRESENTATION_SLIDES.md`.

---

## 🧪 Verified Automated Test Matrix (9/9 Passed)

| Test # | Test Name | Expected Outcome |
| :---: | :--- | :--- |
| **1** | Charge Calculation | Quantities 2, 1, 3 produce ₹190 ($2\times50 + 1\times30 + 3\times20$). |
| **2** | Array Encapsulation | Modifying outside array does not affect stored order quantities. |
| **3** | State Machine Transitions | Enforces `PENDING` &rarr; `READY` &rarr; `COLLECTED`; blocks invalid skips. |
| **4** | Duplicate Phone Check | `HashSet` prevents registering a second customer with the same phone. |
| **5** | Order Input Validation | Quantities of 0,0,0 or negative values are refused. |
| **6** | Customer Isolation | Customers only see their own orders. |
| **7** | Persistence on Restart | Records and next generated IDs survive app restarts. |
| **8** | Sales Accounting | Only `COLLECTED` orders contribute to collected sales revenue. |
| **9** | Referential Integrity | Loading orders referencing unknown customer IDs halts with clear line error. |
