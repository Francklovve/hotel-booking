# Hotel Booking System

Final project — Object-Oriented Programming in Java (JUNIA M1).

## 1. Purpose

A console application that manages the rooms of a small hotel: searching free rooms,
quoting a price, booking, cancelling, and producing management reports.
The business logic is the heart of the project: **conflict detection between reservations,
room-type-specific pricing, discount rules, capacity checks and occupancy statistics**.

## 2. How to run

Requirements: JDK 21 or newer (developed with JDK 26, compiled with `--release 21`).
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
| 4 | Cancel a reservation | cannot cancel twice, the room becomes free again |
| 5 / 6 | List reservations | per guest / all |
| 7 | Reports | revenue per room type, best customer, occupancy rate on a period |

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
│                            PricingService, PriceQuote
├── repository               Repository<T, ID> (generic interface), InMemoryRepository
├── service                  ReservationService, AvailabilityService, ReportService, DataImportService
├── io                       HotelDataSource (interface), CsvHotelDataSource, ReservationRecord
├── exception                custom exceptions
└── ui                       ConsoleMenu, ConsoleInput
```

A class diagram is available in [docs/architecture.md](docs/architecture.md).

## 5. Technical decisions

1. **Room is an abstract class, DiscountPolicy is an interface.** Every room *is a* room and shares
   state (number, capacity, base price) and behaviour (`priceForStay`, `canAccommodate`): that is a
   shared foundation, so an abstract class. A discount is a *capability* with no shared state and many
   unrelated implementations: an interface.
2. **Checked exceptions for business errors, unchecked for invalid arguments.** `BookingException`
   and its subclasses (room unavailable, capacity exceeded, not found…) are expected situations the
   caller must handle (the menu explains them to the user). `InvalidDateRangeException` extends
   `IllegalArgumentException`: an invalid period is an input mistake to prevent by validation.
3. **Half-open date intervals.** A stay is `[checkIn, checkOut)`: the check-out day is not a night,
   so a guest can arrive on the day another one leaves. This makes overlap detection a one-liner.
4. **`Clock` injected into `ReservationService`.** "A stay cannot start in the past" depends on today's
   date; injecting a `Clock` makes the rule testable with a fixed date.
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
- Services work with `Repository<T, ID>` and `HotelDataSource` references.

## 8. Abstract class and interfaces

| Abstraction | Kind | Why |
|---|---|---|
| `Room` | abstract class | shared state + shared behaviour, a generic room cannot exist |
| `DiscountPolicy` | interface | Strategy: interchangeable pricing rules |
| `Repository<T, ID>` | generic interface | storage abstraction used by the services |
| `HotelDataSource` | interface | the origin of the data (CSV today) can change |
| `BookingException` | abstract class | common type to catch every business error at once |

## 9. Custom exceptions

| Exception | Type | Thrown when |
|---|---|---|
| `BookingException` | checked, abstract | base of business errors |
| `RoomUnavailableException` | checked | the room is booked on overlapping dates |
| `CapacityExceededException` | checked | too many guests for the room |
| `RoomNotFoundException`, `GuestNotFoundException`, `ReservationNotFoundException` | checked | unknown id |
| `InvalidDateRangeException` | unchecked (`IllegalArgumentException`) | check-out not after check-in, check-in in the past |
| `DataLoadingException` | checked | missing or malformed data file (keeps the original cause) |

Standard exceptions are also used where they fit: `IllegalArgumentException` (invalid constructor
arguments), `IllegalStateException` (cancelling a cancelled reservation), `IOException`,
`NumberFormatException`, `DateTimeParseException` (wrapped with the file name and line number).

## 10. Data loading

`Application` → `DataImportService.importAll()` → `HotelDataSource` (implemented by `CsvHotelDataSource`).
Files are read with try-with-resources from the classpath (`src/main/resources/data`) or from a directory
given as first program argument. Header, blank lines and lines starting with `#` are ignored.
Rooms are created by `RoomFactory` according to the `type` column. A malformed line stops the startup
with a clear message (`rooms.csv line 4: …`); a reservation violating a business rule is only rejected
and reported.

## 11. SOLID

- **S** — each class has one reason to change: `CsvHotelDataSource` (file format), `PricingService`
  (pricing), `AvailabilityService` (conflicts), `ConsoleMenu` (user interaction), `Application` (wiring).
- **O** — a new discount is a new `DiscountPolicy` class, a new room type a new `Room` subclass:
  `PricingService` and `Room.priceForStay` do not change (test
  `newDiscountCanBeAddedWithoutChangingThePricingService`).
- **L** — every `Room` subclass honours the contract of `Room` (no subclass throws on a normal operation),
  so all services handle rooms without `instanceof`.
- **I** — interfaces are small and focused (`DiscountPolicy` has two methods, `HotelDataSource` only loads).
- **D** — services depend on `Repository` and `HotelDataSource` abstractions received through their
  constructor; `DataImportServiceTest` replaces the CSV source by a fake one.

## 12. Tests

```powershell
.\mvnw.cmd test
```

JUnit 5 tests cover normal cases, edge cases (adjacent stays, exactly 7 nights, full capacity,
check-in today) and error cases (overlap, capacity, unknown ids, invalid files) for every business rule.
