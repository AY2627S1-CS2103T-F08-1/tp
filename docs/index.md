---
layout: page
title: Orchestration and Scheduling of Payment Scheme
---

[![Java CI](https://github.com/AY2627S1-CS2103T-F08-1/tp/actions/workflows/gradle.yml/badge.svg)](https://github.com/AY2627S1-CS2103T-F08-1/tp/actions/workflows/gradle.yml)
[![codecov](https://codecov.io/gh/AY2627S1-CS2103T-F08-1/tp/branch/master/graph/badge.svg)](https://codecov.io/gh/AY2627S1-CS2103T-F08-1/tp)

![OSPS interface concept](images/Ui.png)

<div markdown="span" class="alert alert-warning">
The image above is an interface concept from an earlier iteration. It must be replaced with a screenshot of the tested
v1.3 application before the release is published.
</div>

**Orchestration and Scheduling of Payment Scheme (OSPS) v1.3 is a desktop application for debt recovery agents to
manage debtor details, outstanding balances, and interaction histories.** Its graphical interface and keyboard-driven
commands support fast case-management workflows.

The v1.3 MVP can add and list debtors, show a complete debtor profile, append timestamped interaction notes, and delete
debtors. Persistent debtor IDs are used by `show` and `note`; the displayed one-based list index is used by `delete`.

* To install and use OSPS, see the [_Quick start_ section of the **User Guide**](UserGuide.html#quick-start).
* For exact command syntax, see the [**Command summary**](UserGuide.html#command-summary).
* To develop OSPS, see the [**Developer Guide**](DeveloperGuide.html).

**Acknowledgements**

* OSPS is based on the AddressBook-Level3 project created by the [SE-EDU initiative](https://se-education.org).
* Libraries used: [JavaFX](https://openjfx.io/), [Jackson](https://github.com/FasterXML/jackson),
  [JUnit 5](https://github.com/junit-team/junit5)
