# Issue #69: post-#92 master JAR verification

**PASS: 219 automated GUI-driver assertions.** This run supersedes the earlier candidate for release selection.

| Identity | Verified value |
| --- | --- |
| Tested master commit | `32732c620cf8776019671165b847c06e00e1d8d7` |
| Candidate JAR SHA-256 | `352d399f6e5c97aa4068fe7330d6063b61005c8d50825ecbd9cb5da524f0ce27` |
| OS / Java | Windows 11 build 26100; Temurin Java 25.0.3+9-LTS |
| Build | `gradlew.bat clean check coverage shadowJar`: PASS; 399 tests, zero failures/errors, one Linux-only UI test skipped on Windows |
| Branding | Loaded `Stage.getTitle()` is `OSPS v1.4`; packaged `MainApp.VERSION` is `v1.4`, checked in every driver process |

The exact tested JAR is preserved locally at `qa-evidence/v1.4-issue69-post92/candidate-osps.jar`.
It is not included in the Git commit. Upload these exact bytes, or verify a rebuilt artifact separately.

## Cases and evidence

The external [driver](CandidateSmoke.java) loads production classes from the unmodified JAR, starts the shipped
JavaFX UI, and submits commands through the real command field and its `ActionEvent`.
The fixture was generated earlier by the official v1.3 release JAR: six sample debtors, three synthetic debtors,
nonzero/zero balances, and two timestamped notes. Source fixture/JAR hashes are in [manifest.json](manifest.json).

| Cases | Result |
| --- | --- |
| Clean-folder GUI | PASS: first-run samples, working list, actual v1.4 title. [Results](clean/result-clean.txt), [screenshot](clean/clean-folder-startup.png). |
| Full v1.3-data workflow | PASS: add → list/find → show → note → edit → reapply find → show → delete. The filtered index selects persistent ID 10; success identifies the correct edited debtor. [Results](workflow/result-workflow.txt), [profile](workflow/workflow-profile-before-delete.png), [deletion](workflow/workflow-filtered-delete.png). |
| Restart and preservation | PASS: fresh JVM reloads the same JAR/data, deleted ID remains absent, control notes remain, and every surviving persisted record matches the baseline. Next ID advances without reuse. [Results](workflow/result-restart.txt), [screenshot](workflow/workflow-after-restart.png). |
| First/middle/last/only | PASS: intended debtor removed, correct success message, all surviving IDs/fields/amounts/notes unchanged, deleted profile unavailable. Leading-zero index also passes. [First](first/result-first.txt), [middle](middle/result-middle.txt), [last](last/result-last.txt), [only](only/result-only.txt). |
| Invalid/oversized | PASS: blank, alphabetic, negative, zero/all-zero, fractional, out-of-range, 101-digit and million-digit indexes; empty filtered results; exact stored bytes preserved. Million-digit UI commands returned in 33 ms and 211 ms on populated and empty displayed lists. [Results](invalid/result-invalid.txt). |

Separately, `java --enable-native-access=ALL-UNNAMED -jar osps.jar` started in an otherwise empty folder,
logged `Starting AddressBook v1.4` and UI startup without a severe error, and remained alive after five seconds.
It was externally terminated after startup. The driver separately exercised orderly stop and a fresh-JVM restart.

Screenshots are unedited JavaFX scene snapshots excluding window decorations. Full profile feedback is in the
text results. This is automated Windows GUI smoke testing, not manual three-platform testing. Million-digit
input is set on the command field; clipboard paste performance is not measured.

## Reproduction and checksums

Copy this directory to an isolated working folder, place the exact candidate at `candidate-osps.jar`, and use Java 25:

```powershell
New-Item -ItemType Directory -Force driver, fixture-v13/data | Out-Null
Copy-Item fixture-v13/addressbook.json fixture-v13/data/addressbook.json
javac -cp candidate-osps.jar -d driver CandidateSmoke.java
python run_smoke.py
```

The runner requires fresh `runs-verified` folders. Repository text copies normalize trailing spaces and final
newlines to satisfy CI; originals remain locally unchanged. The manifest records original byte hashes.
`SHA256SUMS.json` hashes canonical Git blob bytes, so Windows checkout line endings may differ.

## Release status

[#92](https://github.com/AY2627S1-CS2103T-F08-1/tp/pull/92) is merged and #90 is resolved; runtime branding is verified.
**The published-asset SHA-256 comparison is still pending**, because no v1.4 release asset was available during this run.
Keep #69 open until the uploaded JAR matches this tested hash. A changed final JAR requires new verification.
