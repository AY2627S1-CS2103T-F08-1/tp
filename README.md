# Orchestration and Scheduling of Payment Scheme (OSPS)

[![Java CI](https://github.com/AY2627S1-CS2103T-F08-1/tp/actions/workflows/gradle.yml/badge.svg)](https://github.com/AY2627S1-CS2103T-F08-1/tp/actions/workflows/gradle.yml)
[![codecov](https://codecov.io/gh/AY2627S1-CS2103T-F08-1/tp/branch/master/graph/badge.svg)](https://codecov.io/gh/AY2627S1-CS2103T-F08-1/tp)

![OSPS interface showing debtor profiles and outstanding amounts](docs/images/Ui.png)

OSPS v1.3 is a desktop application for debt recovery agents to manage debtor contact details, outstanding balances,
and interaction histories. It combines a graphical interface with keyboard-driven commands for fast case management.

The v1.3 MVP supports these core workflows:

* Add a debtor with an optional non-negative outstanding amount.
* List active debtors in ascending persistent debtor-ID order, including their outstanding amounts.
* View a debtor's complete profile and newest-first interaction history using the persistent debtor ID.
* Add a timestamped interaction note using the persistent debtor ID.
* Delete a debtor using the one-based index in the currently displayed list.

For setup, exact command syntax, and the distinction between debtor IDs and displayed indexes, see the
[User Guide](https://ay2627s1-cs2103t-f08-1.github.io/tp/UserGuide.html). Developers can refer to the
[Developer Guide](https://ay2627s1-cs2103t-f08-1.github.io/tp/DeveloperGuide.html).

OSPS is based on the AddressBook-Level3 project created by the
[SE-EDU initiative](https://se-education.org).
