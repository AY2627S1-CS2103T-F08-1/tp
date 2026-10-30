# Orchestration and Scheduling of Payment Scheme (OSPS)

[![Java CI](https://github.com/AY2627S1-CS2103T-F08-1/tp/actions/workflows/gradle.yml/badge.svg)](https://github.com/AY2627S1-CS2103T-F08-1/tp/actions/workflows/gradle.yml)
[![codecov](https://codecov.io/gh/AY2627S1-CS2103T-F08-1/tp/branch/master/graph/badge.svg)](https://codecov.io/gh/AY2627S1-CS2103T-F08-1/tp)

![OSPS interface showing debtor profiles and outstanding amounts](docs/images/Ui.png)

OSPS is a desktop application for debt recovery agents to manage debtor contact details, outstanding balances,
interaction histories, follow-ups, and recovery stages. It combines a graphical interface with keyboard-driven
commands for fast case management.

**Build identity:** these guides describe the refreshed v1.5 build. Download [osps.jar from the v1.5 release](https://github.com/AY2627S1-CS2103T-F08-1/tp/releases/tag/v1.5)
and check the source commit and SHA-256 recorded in its release notes. The refresh replaces the JAR asset; the
original v1.5 source tag remains unchanged. An older saved v1.5 JAR supports fewer commands; use its
[original User Guide](https://github.com/AY2627S1-CS2103T-F08-1/tp/blob/v1.5/docs/UserGuide.md)
or download the refreshed artifact once its release notes identify the updated build.

The refreshed build supports these connected workflows:

* Capture and maintain debtor profiles, contact preferences, and non-negative outstanding amounts.
* Find, filter, and sort the current worklist by name, phone, balance, recovery stage, follow-up date, or contact window.
* Record interaction outcomes, promised-payment dates, follow-ups, and recovery-stage changes with persistence.
* Review complete profiles, workload summaries, and due follow-ups, or export one debtor's profile for authorised use.
* Preview risky edits or deletions and recover saved changes with session undo and redo.

OSPS deliberately distinguishes a debtor's stable persistent ID from the one-based index of the currently displayed
worklist. Commands that contain `--id` use the persistent ID; `edit`, `delete`, and their previews use the visible
displayed index. The User Guide explains this distinction with examples and a visual reference.

For setup, exact command syntax, and the distinction between debtor IDs and displayed indexes, see the
[User Guide](https://ay2627s1-cs2103t-f08-1.github.io/tp/UserGuide.html). Developers can refer to the
[Developer Guide](https://ay2627s1-cs2103t-f08-1.github.io/tp/DeveloperGuide.html).

OSPS is based on the AddressBook-Level3 project created by the
[SE-EDU initiative](https://se-education.org).
