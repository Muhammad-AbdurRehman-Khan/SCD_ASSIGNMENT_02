# Task 4 - Git Commit Trail

The refactoring was done in 10 small commits, one per step of the assignment workflow.

| # | Commit message | Result of the step |
|---|----------------|--------------------|
| 1 | `chore: add baseline legacy OrderBillingEngine file` | Maven project + untouched legacy class |
| 2 | `test: add characterization tests for discount calculations` | 17 tests green on legacy code |
| 3 | `test: add tests for payment options and invalid error paths` | 71 tests green on legacy code |
| 4 | `docs: document baseline code smells and SOLID violations` | Audit table (Task 2) |
| 5 | `refactor: extract tax and discount calculator classes` | 71 green |
| 6 | `refactor: implement strategy pattern for payments` | 71 green |
| 7 | `refactor: decouple email and sms notification dispatchers` | 71 green |
| 8 | `refactor: move file persistence behind repository interface` | 71 green |
| 9 | `refactor: clean up main service to follow single responsibility` | 71 green, engine is an 18-line facade |
| 10 | `test: verify all unit tests pass with clean architecture` | 74 green, metrics + diagram |

## For the PDF report

Paste the output of this command below (replace this placeholder with your own real output):

```
git log --oneline
```
