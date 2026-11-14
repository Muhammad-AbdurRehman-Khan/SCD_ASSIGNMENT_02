# Task 2 - Code Smells & SOLID Violations Audit

**File audited:** `src/main/java/com/uet/billing/OrderBillingEngine.java` (original legacy version, 160 lines)
**All line numbers refer to that original file** (see the first commit, `chore: add baseline legacy OrderBillingEngine file`).

---

## Part A - Code Smells (8 found, 5 required)

| # | Code smell | Line(s) | Why it is bad | How it is fixed in the refactoring |
|---|-----------|---------|---------------|------------------------------------|
| 1 | **God Class / Long Method** - one `processOrder()` method does validation, pricing, discounts, tax, payment, file saving and notification | 16-160 (class), 21-150 (method, 130 lines) | Hard to read, impossible to test one rule in isolation, any change risks breaking an unrelated part. A bug in notification code can block order processing. | Split into small single-purpose classes: `OrderRequestParser`, `DiscountCalculator`, `TaxCalculator`, `PaymentStrategy` implementations, `NotificationService` implementations, `FileOrderRepository`. `OrderService.processOrder()` only coordinates them. |
| 2 | **Magic Numbers / Magic Strings** - raw literals with no name | 62-77 (`5000`, `0.15`, `0.10`, `0.08`, `10`, `0.20`, `0.05`, `10000`, `500.0`), 85-91 (`0.15`, `0.17`, `0.08`, `0.20`), 103 (`16`), 112 (`11`, `"03"`), 120 (`25000`), 58/84/98/139 (`"VIP"`, `"PK"`, `"CASH"`, `"EMAIL"` ...) | Nobody knows what a number means without reading a comment; the same rule can't be changed in one place; typos in strings are not caught by the compiler. | Every number becomes a named `private static final` constant inside the class that owns the rule (e.g. `VipDiscountPolicy.HIGH_RATE`, `CashOnDeliveryPayment.MAX_AMOUNT`); customer types become the `CustomerType` enum. |
| 3 | **Primitive Obsession** - everything is a `Map<String,Object>`, `String` or `double` | 21 (`Map<String, Object> orderData`), 35 (`List<Map<String,Object>> items`), 42/57/81/95 (`double` for money), 58/84/98/139 (`String` used as an enum) | No type safety, no place for behaviour, needs casts everywhere, a wrong value type only fails at run time. | Introduce `OrderRequest`, `OrderItem`, `CustomerType`, `PaymentStatus`, `PaymentResult`, `OrderRecord` value objects. (Money stays `double` on purpose so that behaviour is *identical*; switching to `BigDecimal` is the recommended next step.) |
| 4 | **Direct File I/O / hard-coded path inside business logic** | 19 (`"order_audit.log"`), 131-136 (`FileWriter`) | Business logic cannot run without touching the disk; tests have to redirect the path; swapping to a database means editing the pricing code. | `OrderRepository` interface + `FileOrderRepository` implementation; the service only sees `repository.save(record)`. |
| 5 | **Switch-statement smell (long if / else-if chains)** | 61-79 (discount), 86-92 (tax), 101-128 (payment), 140-147 (notification) | Each new customer type, country, payment method or channel means editing the same method; the chains are nested up to 3 levels deep. | Lookup tables + Strategy pattern: `Map<CustomerType, DiscountPolicy>`, `Map<String, Double>` for tax, `PaymentStrategy` and `NotificationService` interfaces. No if-else ladders remain. |
| 6 | **Error handling by `System.out.println` + boolean flags** | 24, 30, 37, 45, 51, 107, 116, 121, 126, 135 | The caller only learns `false` and cannot tell *why*; messages are mixed into the algorithm; the console cannot be replaced by a logger or UI. | Validation throws `InvalidOrderException` carrying the message, payment returns a `PaymentResult` (status + message). Only the coordinating service prints, in one place, so it can be swapped for a logger easily. |
| 7 | **Unsafe casts / `@SuppressWarnings("unchecked")`** | 28, 34-35, 48-49, 58, 84, 98, 102, 111, 139, 144 | `ClassCastException` waits at every cast; the warning is silenced instead of fixed. | Casting happens once, inside `OrderRequestParser`; the rest of the code works with typed objects. |
| 8 | **Test-induced design / hidden dependency** (`LocalDateTime.now()` and file path setter only exist to make testing possible) | 132, 152-159 | The class has to expose a setter just so tests can redirect I/O - a sign the dependency is hidden inside. | Dependencies are injected through `OrderService`'s constructor; `OrderBillingEngine` is kept only as a thin backward-compatible facade so the safety tests still run unchanged. |

---

## Part B - SOLID Violations (3 required, 3 found + 2 notes)

| Principle | Line(s) | What is broken | How it is fixed |
|-----------|---------|----------------|-----------------|
| **S - Single Responsibility** | 16-160 (whole class), esp. 21-150 | The class has at least **six** reasons to change: input validation, discount rules, tax rules, payment rules, persistence format, notification wording. | One class per responsibility (see smell #1). `OrderService` has a single job: coordinate the steps in order. |
| **O - Open/Closed** | 61-79, 86-92, 101-128, 140-147 | Adding JazzCash, a WhatsApp notification, a new customer type or a new country means **editing the existing, working method** and re-testing everything. | New behaviour = new class implementing `PaymentStrategy` / `NotificationService` / `DiscountPolicy`; existing classes stay untouched (this is what the 3-minute live challenge checks). |
| **D - Dependency Inversion** | 131 (`new FileWriter`), 132 (`LocalDateTime.now()`), 141 & 145 (`System.out`) | The high-level business rule depends directly on low-level details (disk, console, system clock). It can't be reused or tested without them. | High-level `OrderService` depends on abstractions (`OrderRepository`, `NotificationService`, `PaymentStrategy`); concrete classes are wired in `BillingFactory`. |
| *L - Liskov Substitution* | - | Not violated: the legacy class uses no inheritance. After refactoring, every `PaymentStrategy` / `NotificationService` implementation can be substituted for another without the service noticing. | - |
| *I - Interface Segregation* | 21 | Not a strict violation, but callers are forced to pass one giant untyped map that mixes payment, shipping and notification data. | Small focused interfaces: `PaymentStrategy` receives only a `PaymentRequest`; `NotificationService` receives only a `NotificationRequest`. |

---

## Part C - Behaviour that must be preserved (found while writing the safety tests)

These quirks were discovered by the characterization tests. They were **kept** during refactoring because the assignment requires all original assertions to keep passing:

* Customer types and country codes are case-sensitive (`"vip"` is *not* VIP).
* Corporate bulk discount counts **line items**, not total quantity.
* Cash-on-delivery limit is checked against the amount **after tax**.
* Validation order is: order empty -> e-mail -> cart empty -> each item.
* A failing log write does **not** fail the order.
* An unknown notification channel silently sends nothing but the order still succeeds.
