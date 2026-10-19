# Hotel Booking System

Final project — Object-Oriented Programming in Java (JUNIA M1).

## 1. Purpose

A console application that manages the rooms of a small hotel: searching free rooms,
quoting a price, booking, cancelling, and producing management reports.
The business logic is the heart of the project: **conflict detection between reservations,
room-type-specific pricing, discount rules, capacity checks, cancellation fees and occupancy statistics**.
Successful user operations also produce confirmation messages and a session history; reports can be
exported to CSV.

## 2. How to run

Requirements: JDK 17 or newer (the project is compiled with `--release 17`).
The Maven Wrapper downloads and uses Maven 3.9.11; no separate Maven installation is needed.
Set `JAVA_HOME` to your JDK directory. The first run needs an internet connection.

From IntelliJ: open the folder (it detects `pom.xml`), then run `com.junia.hotel.Application`.

From PowerShell on Windows:

```powershell
.\mvnw.cmd compile exec:java              # uses the data bundled in src/main/resources/data
.\mvnw.cmd package
java -jar target/hotel-booking-1.0.0.jar path/to/other/data/folder
```

On Linux or macOS, use `./mvnw` instead of `.\mvnw.cmd`.

The data folder must contain `rooms.csv`, `guests.csv` and `reservations.csv`.

## 3. Main features

| Menu | Feature | Business rules involved |
|---|---|---|
| 1 | Search available rooms | capacity, overlap with active reservations, sorted by price |
| 2 | Price quote | per-night pricing of each room type + best discount |
| 3 | Book a room | guest/room must exist, no past check-in, capacity, no overlap |
| 4 | Cancel a reservation | type-specific fee/refund, not after check-in day, cannot cancel twice, the room becomes free again |
| 5 / 6 | List reservations | per guest / all |
| 7 | Reports | active revenue per room type, best customer, occupancy rate on a period |
| 8 | Show history | successful user bookings/cancellations in chronological order |
| 9 | Export report to CSV | same indicators as option 7, UTF-8 file, parent directory created automatically |

Cancellation fees use the final price frozen at booking, including its discount, and are rounded to cents:
STANDARD and FAMILY are flexible (free at least 2 calendar days before check-in, 50% otherwise);
SUITE is non-refundable (100% whatever the date). A reservation can be cancelled until its check-in day
(included); once the stay has started it is refused with `CancellationNotAllowedException`.
The menu displays the fee and refund.

Booking and cancellation trigger a simulated confirmation email on the console and an entry in the
session history. Failed operations and startup imports do not trigger these events. History is kept
in memory and exposed as an unmodifiable snapshot.

Reports include revenue and the best customer across **all active reservations**; only occupancy is
limited to the selected half-open period. Cancelled reservations are excluded, including their fees:
this is a booking report, not a payment ledger. Option 9 writes `reports/report-<date>.csv` in the
current working directory. A second export on the same day replaces that file. The CSV has `metric,value`
columns, one row per room type (including zeros), total revenue, occupancy as a fraction from 0 to 1,
and the best customer name (`None` if empty). Decimal points and quoted names make the output
independent of the computer's language. Generated reports are ignored by Git.

At startup the existing reservations of `reservations.csv` go through the same rules as a new booking:
the demo file contains one conflicting line which is **rejected and reported** without stopping the application.

## 4. Main classes

```
com.junia.hotel
├── Application              composition root: creates and wires every object
├── model                    domain objects
│   ├── DateRange            immutable period, nights, overlap detection
│   ├── Guest, LoyaltyTier   customer and its loyalty level (enum with discount rate)
│   ├── Reservation          booking with its frozen price and status
│   └── room
│       ├── Room             abstract base of every room
│       ├── StandardRoom, Suite, FamilyRoom
│       ├── RoomType         enum
│       └── RoomFactory      creates the right subclass from a type
├── pricing                  DiscountPolicy (interface), LoyaltyDiscount, LongStayDiscount,
│                            PricingService, PriceQuote, CancellationPolicy (interface),
│                            FlexibleCancellationPolicy, NonRefundablePolicy, CancellationPolicyResolver
├── repository               Repository<T, ID> (generic interface), InMemoryRepository
├── service                  ReservationService, CancellationResult, AvailabilityService,
│                            ReservationListener (interface), ReservationHistory,
│                            ReportService, Report, DataImportService
├── io                       HotelDataSource (interface), CsvHotelDataSource, ReservationRecord,
│                            ReportExporter (interface), CsvReportExporter
├── exception                custom exceptions
└── ui                       ConsoleMenu, ConsoleInput, ConsoleNotifier
```

A class diagram is available in [docs/architecture.md](docs/architecture.md).

## 5. Technical decisions

1. **Room is an abstract class, policies are interfaces.** Every room *is a* room and shares
   state (number, capacity, base price) and behaviour (`priceForStay`, `canAccommodate`): that is a
   shared foundation, so an abstract class. A discount is a *capability* with no shared state and many
   unrelated implementations: an interface. `CancellationPolicy` follows the same Strategy pattern;
   its type mapping belongs to a separate resolver so `Room` keeps one responsibility.
2. **Checked exceptions for business errors, unchecked for invalid arguments.** `BookingException`
   and its subclasses (room unavailable, capacity exceeded, not found…) are expected situations the
   caller must handle (the menu explains them to the user). `InvalidDateRangeException` extends
   `IllegalArgumentException`: an invalid period is an input mistake to prevent by validation.
   Cancelling an already cancelled reservation is an invalid state transition (`IllegalStateException`).
   Technical loading/export errors use separate checked exceptions and keep their cause.
3. **Half-open date intervals.** A stay is `[checkIn, checkOut)`: the check-out day is not a night,
   so a guest can arrive on the day another one leaves. This makes overlap detection a one-liner.
4. **One `Clock` injected into the service and menu.** Past check-in validation, cancellation fees and
   the export filename depend on today's date; injecting a `Clock` makes them testable with a fixed date.
5. **Discounts do not stack.** Only the most advantageous one is applied (`max` on a stream), which is
   a common hotel rule and keeps prices explainable (`PriceQuote` records which discount was used).

## 6. Inheritance

`StandardRoom`, `Suite` and `FamilyRoom` extend the abstract class `Room`. Each subclass adds its own
attribute (`breakfastIncluded`, `extraGuestFee`) and its own pricing rule. Custom exceptions also
form a hierarchy: `RoomUnavailableException`, `CapacityExceededException`, … extend `BookingException`.

## 7. Polymorphism

- `Room.priceForStay()` (a *template method*) calls the abstract `priceForNight()`: the code that sums
  the nights is written once, the executed pricing depends on the actual object
  (standard: flat price; suite: +25 % on Friday/Saturday and optional breakfast; family room: fee per
  extra guest). See `RoomPricingTest.sameStayHasDifferentPricesDependingOnTheActualRoomType`.
- `PricingService` manipulates a `List<DiscountPolicy>` without knowing the concrete discounts.
- `ReservationService` calls `CancellationPolicy.feeRate()` on the strategy selected by an `EnumMap`
  in `CancellationPolicyResolver`; STANDARD/FAMILY share the flexible strategy, SUITE uses the other.
- **Observer:** `ReservationService` calls a `List<ReservationListener>` after successful changes.
  `ConsoleNotifier` and `ReservationHistory` implement the same interface. Observers can subscribe
  and unsubscribe; a listener is registered at most once.
- Services work with `Repository<T, ID>` and `HotelDataSource` references. The menu receives a
  `ReportExporter`; `ReportService` produces a `Report` snapshot without depending on CSV or files.

## 8. Abstract class and interfaces

| Abstraction | Kind | Why |
|---|---|---|
| `Room` | abstract class | shared state + shared behaviour, a generic room cannot exist |
| `DiscountPolicy` | interface | Strategy: interchangeable pricing rules |
| `CancellationPolicy` | interface | Strategy: interchangeable cancellation fee rules |
| `ReservationListener` | interface | Observer: booking/cancellation subscribers |
| `ReportExporter` | interface | export contract independent of the file format |
| `Repository<T, ID>` | generic interface | storage abstraction used by the services |
| `HotelDataSource` | interface | the origin of the data (CSV today) can change |
| `BookingException` | abstract class | common type to catch every business error at once |

`CancellationResult` and `Report` are records that carry operation results. `Report` makes a defensive
copy of its revenue map; `ReservationHistory` similarly returns an unmodifiable list snapshot.

## 9. Custom exceptions

| Exception | Type | Thrown when |
|---|---|---|
| `BookingException` | checked, abstract | base of business errors |
| `RoomUnavailableException` | checked | the room is booked on overlapping dates |
| `CapacityExceededException` | checked | too many guests for the room |
| `CancellationNotAllowedException` | checked | the stay has already started or is over |
| `RoomNotFoundException`, `GuestNotFoundException`, `ReservationNotFoundException` | checked | unknown id |
| `InvalidDateRangeException` | unchecked (`IllegalArgumentException`) | check-out not after check-in, check-in in the past |
| `DataLoadingException` | checked | missing or malformed data file (keeps the original cause) |
| `DataExportException` | checked | report cannot be written or its parent directory created (keeps the `IOException`) |

Standard exceptions are also used where they fit: `IllegalArgumentException` (invalid constructor
arguments), `IllegalStateException` (cancelling a cancelled reservation), `IOException`,
`NumberFormatException`, `DateTimeParseException` (wrapped with the file name and line number).

## 10. Data loading

`Application` → `DataImportService.importAll()` → `HotelDataSource` (implemented by `CsvHotelDataSource`).
Files are read with try-with-resources from the classpath (`src/main/resources/data`) or from a directory
given as first program argument. Header, blank lines and lines starting with `#` are ignored.
Rooms are created by `RoomFactory` according to the `type` column. A malformed line stops the startup
with a clear message (`rooms.csv line 4: …`); a reservation violating a business rule is only rejected
and reported. Imports do not notify reservation listeners or add session-history entries.

## 11. SOLID

- **S** — each class has one reason to change: `CsvHotelDataSource` (file format), `PricingService`
  (pricing), `AvailabilityService` (conflicts), `CancellationPolicyResolver` (type/policy mapping),
  `ReportService` (indicators), `CsvReportExporter` (CSV/files), `ConsoleMenu` (interaction), `Application` (wiring).
- **O** — a new discount is a new `DiscountPolicy` class, a new room type a new `Room` subclass:
  `PricingService` and `Room.priceForStay` do not change (test
  `newDiscountCanBeAddedWithoutChangingThePricingService`). New observers implement
  `ReservationListener` and subscribe without changing booking logic; a new export format implements
  `ReportExporter` without changing report calculations.
- **L** — every `Room` subclass honours the contract of `Room` (no subclass throws on a normal operation),
  so all services handle rooms without `instanceof`.
- **I** — interfaces are small and focused (policies and listeners have two methods, `ReportExporter`
  has one, `HotelDataSource` only loads).
- **D** — services depend on `Repository` and `HotelDataSource` abstractions received through their
  constructor; `DataImportServiceTest` replaces the CSV source by a fake one. The cancellation resolver,
  shared `Clock`, and menu's `ReportExporter` are also injected. Observer callbacks use only their interface.

## 12. Tests

```powershell
.\mvnw.cmd test
```

The suite has **116 JUnit 5 tests**. They cover normal cases, boundaries (adjacent stays, exactly
7 nights, full capacity, check-in today, cancellation exactly 2 days before arrival or on the check-in day)
and failures (overlap, capacity, unknown ids, invalid files, repeated cancellation, stay already started).
Fake listeners verify events and silent imports; `@TempDir` tests verify CSV contents, directory creation
and invalid paths with the original cause. Console menu tests use a fake exporter and a fixed `Clock`.

To rebuild the executable JAR and run the bundled demo data:

```powershell
.\mvnw.cmd clean package
java -jar target/hotel-booking-1.0.0.jar
```

Use option 0 to quit. The application stores bookings and history in memory for the current session.
