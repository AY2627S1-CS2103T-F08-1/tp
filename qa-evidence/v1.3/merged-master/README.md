# OSPS v1.3 merged-master release evidence

Tested release commit: 6a2b9f4df56bb652749040b53ce371984fe42744
Environment: Temurin Java 25.0.3 on Windows
JAR SHA-256: 2128B6CE4B3A4571B598E8C3E592C41C361ACC31D48D79610C3E15CAA97A2746
Automated verification: 339 tests passed; 0 failures, errors, or skipped. Packaged-JAR command smoke: 37 assertions passed.
CI: post-merge Java CI passed on Ubuntu, macOS, and Windows. Pages deployment succeeded.

Interactive checks recorded:
- list: six seeded debtors shown.
- add: QA Smoke created as ID 7 with S$1,250.00 and the qa tag.
- show: all profile fields and interaction history inspected by scrolling.
- note: timestamped note created and confirmed in newest-first history.
- show --id 0: validation error shown; no record changed.
- find QA Smoke: only ID 7 matched.
- delete 1 while filtered: ID 7 deleted.
- list after deletion: original IDs 1-6 remained, with ID 7 absent.
- Persistence: user confirmed after restarting the app that the six-record state persisted. The same list screenshot is reused for this state as requested.

Known UI defect: the profile result viewport is hard-capped at 100 px. Tracked separately in issue #63.