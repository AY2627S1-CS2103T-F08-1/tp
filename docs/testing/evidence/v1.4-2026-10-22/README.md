# Issue #69: integrated candidate smoke evidence, 22 October 2026

## Result and candidate identity

**Candidate smoke PASS: 203 assertions across eight GUI-driver runs. Release verification remains pending.**

The tested candidate was built from integrated master commit `0706f3eb7f155083012c12336252adc994496da7`
with `gradlew.bat clean check coverage shadowJar`, using Java 25.0.3 on Windows 11 (build 26100).
The build passed: 398 tests, zero failures/errors, one skipped Linux-only JavaFX test
(`ResultDisplayTest.setFeedbackToUser_updatesTextAndVisibleRows`). Current-commit Java CI passes on Windows,
macOS, and Ubuntu; Codecov reports success. See [manifest.json](manifest.json) for exact environment and links.

Tested `osps.jar` SHA-256:

```text
738f6896139a670395feb338529fbb3fd864a9e523cdfeb6470d7178fe5346d6
```

The exact JAR remains in the local verification workspace at
`qa-evidence/v1.4-issue69/candidate-osps.jar`; it is not committed to Git.

## Method and cases

`CandidateSmoke.java` is an external driver compiled against the packaged JAR. It initializes the shipped
`MainApp` and JavaFX UI, submits text through the real command field and its `ActionEvent`, checks model state,
and snapshots the rendered scene. It does not replace production command, model, or storage classes.
This is automated GUI smoke testing on Windows, not a claim of manual testing on three operating systems.

Separately, `java --enable-native-access=ALL-UNNAMED -jar osps.jar` was launched from an empty folder.
It remained running after five seconds, logged UI startup without a severe error, and was externally stopped
after startup. The driver exercises orderly application stop and a fresh-JVM restart separately.

The official v1.3 release JAR generated the compatibility fixture: six sample records, three synthetic debtors
with zero/nonzero balances, and two timestamped notes. Its JAR and resulting data hashes are in the manifest.

| Case | Outcome and evidence |
| --- | --- |
| Clean folder | PASS: six sample debtors, rendered GUI and working `list`. [Screenshot](clean/clean-folder-startup.png), [results](clean/result-clean.txt). |
| v1.3 workflow | PASS: add → list → find → show → note → edit → reapply find → show → delete. Persistent ID 10 was displayed as index 1; deletion named the edited target. ID, amount, and notes survived editing. [Profile](workflow-final/workflow-profile-before-delete.png), [deletion](workflow-final/workflow-filtered-delete.png), [results](workflow-final/result-workflow.txt). |
| Persistence/restart | PASS: new JVM reopened the same candidate and data; ID 10 remained absent, control note remained present, and every surviving persisted record exactly matched the v1.3 baseline. The next-ID counter advanced without reusing the deleted ID. [Screenshot](workflow-final/workflow-after-restart.png), [results](workflow-final/result-restart.txt). |
| First/middle/last/only | PASS: each started with an isolated fixture, showed the target profile, deleted the intended displayed index, confirmed the success message, checked every survivor's ID and fields/history, and rejected the deleted profile. `delete 0001` also passed. [First](first/result-first.txt), [middle](middle/result-middle.txt), [last](last/result-last.txt), [only](only/result-only.txt). |
| Invalid/oversized | PASS: missing, alphabetic, negative, zero/all-zero, fractional, count+1, 101-digit, and million-digit indexes; empty filtered results. Rejections preserved all model records and the exact persisted bytes. Million-digit commands took 32 ms with nine displayed debtors and 218 ms with an empty filter, including setting the command field and dispatching it. [Results](invalid/result-invalid.txt), [empty-list guidance](invalid/empty-filter-delete.png). |

The million-digit test supplies the full text using `TextField.setText`; it does not measure clipboard paste.
Screenshots are unedited JavaFX scene snapshots, excluding window decorations. Full feedback, including notes
and timestamps that require scrolling in the profile pane, is recorded in the text results.

The initial local driver forgot to reapply `find` after `edit` restores the full list. That driver error was
corrected to follow the existing checklist. Its failed attempt remains locally under `qa-evidence/v1.4-issue69/runs/`
and is excluded from these passing results. The committed driver includes the correction.

## Reproduction

Use an isolated directory, Java 25, and the exact candidate JAR whose hash matches the value above.
Copy the fixture's `addressbook.json` to `fixture-v13/data/addressbook.json` beneath this evidence directory;
place the candidate JAR at `candidate-osps.jar`. Do not use personal debtor data.

```powershell
New-Item -ItemType Directory -Force driver, fixture-v13/data | Out-Null
Copy-Item fixture-v13/addressbook.json fixture-v13/data/addressbook.json
javac -cp candidate-osps.jar -d driver CandidateSmoke.java
python run_smoke.py
```

The runner creates fresh folders under `runs-verified`; use a new copy of this directory for each run.
Compare JSON records and hashes, not only screenshots. `SHA256SUMS.json` covers the committed raw evidence;
the driver and runner are included for inspection and reproduction.

## Outstanding release work

- [#90](https://github.com/AY2627S1-CS2103T-F08-1/tp/issues/90): the candidate still declares and displays `OSPS v1.3`. Correct release branding before final packaging.
- No published v1.4 JAR was available during this run, so **uploaded-versus-tested SHA-256 comparison is PENDING**.
- If the final candidate differs from this JAR, rerun the verification against its exact commit and bytes. Do not transfer this PASS to a changed artifact.
- Keep #69 open until final candidate evidence and the published-asset hash comparison are complete.
