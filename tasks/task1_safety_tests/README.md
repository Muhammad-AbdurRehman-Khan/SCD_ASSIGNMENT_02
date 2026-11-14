# Task 1 - JUnit 5 Safety Net

The tests were written **before** touching the legacy code and run against the untouched
`OrderBillingEngine`. They only use its public API (`processOrder`, `setLogFilePath`,
`getLogFilePath`) and check the three observable results: the returned `boolean`, the console
text and the audit-log file. That is why they keep passing after the internal redesign.

Location of the code: `src/test/java/com/uet/billing/`

| Test class | Commit step | What it protects |
|------------|-------------|------------------|
| `BillingTestBase` | 2 | Helpers: temp log file, console capture, order builders |
| `DiscountCharacterizationTest` | 2 | REGULAR / VIP / STUDENT / CORPORATE discount rules, all boundaries (5000, 10000, 9 vs 10 items), unknown types, subtotal maths |
| `PaymentMethodTest` | 3 | Credit card, EasyPaisa, Cash on Delivery (incl. limit after tax), unsupported / missing method |
| `InputValidationTest` | 3 | Null/empty order, bad e-mail, empty cart, missing/zero/negative price & quantity, order of the checks |
| `TaxAndSideEffectsTest` | 3 | Tax per country, audit-log writing, log failure, e-mail / SMS / unknown notification |

## How to run

* **NetBeans:** right-click the project -> *Test* (or `Alt+F6`).
* **Command line:** `mvn test`

## Rule for the refactoring phase

> The assertions in these files must **never be edited** while refactoring.
> If a test goes red, the *production code* is wrong, not the test.
