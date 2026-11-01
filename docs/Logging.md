---
layout: page
title: Logging guide
---

* The project uses the `java.util.logging` package for logging.
* `LogsCenter` manages logging levels and destinations.
* A class obtains a `Logger` with `LogsCenter.getLogger(Class)`, which logs messages at the configured level.
* Log messages are written to the console and a `.log` file.
* The output logging level can be controlled by changing the `LOG_LEVEL` constant in the `LogsCenter` class.
* `DeleteCommand` emits `FINE` diagnostics for an out-of-range rejection and a completed in-memory removal.
  These messages contain no raw index, command text, or debtor details. A model removal does not imply that
  persistence succeeded: `LogicManager` saves the updated address book afterwards and reports any save error.
* **When choosing a level for a log message**, follow the conventions given in [_[se-edu/guides] Java: Logging conventions_](https://se-education.org/guides/conventions/java/logging.html).

## Privacy invariant

Names, phone numbers, email addresses, postal addresses, and interaction-note text must never appear in a log. This
applies at every logging level and on both successful and exceptional paths.

Do not pass any of the following to a logger:

* raw command text or command arguments
* a `Person`, an address book, or another object whose string form contains debtor data
* user-facing command feedback
* an exception message that can repeat command input or persisted debtor data

Use operational metadata instead. `CommandLogFormatter#getCommandWord` returns only an allowlisted command category;
it maps blank input to `<empty>` and unrecognised input to `<unknown>`. Aggregate counts, lifecycle events, and exception
classes are also acceptable when they do not expose record contents.

| Safe | Unsafe |
| --- | --- |
| `----------------[USER COMMAND][find]` | `----------------[USER COMMAND][find Alice 91234567]` |
| `Command completed successfully: add` | `Result: Added Alice at 1 Main Street` |
| `Command failed: note; error type: CommandException` | `Command failed: note --id 3 --text Discussed repayment` |
| `Initializing model with 42 address book entries` | `Initializing model with ` followed by the address book contents |

Before adding or changing a log statement, check both its normal and failure paths. Add a test with distinctive sentinel
values and capture the relevant logger output; the test should assert that the useful category or count is present and
that every sentinel value is absent. See `CommandLoggingTest` for the established pattern.
