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
