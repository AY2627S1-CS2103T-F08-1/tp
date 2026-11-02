# Contact, preview and export verification

Recorded on 1 November 2026 (SGT). This is AI-assisted execution evidence, not a claim that Edison or another
team member personally completed beta testing or independent human review.

## Baseline and scope

The 30 October research was checked against upstream `master` at
`58361c4c2072c88be125c901436867db5291e1d8`. Existing contact/preview workflow tests and export tests were retained.
The guide already described persistent IDs, contact-window boundaries, preview isolation, UTF-8 reports and
non-overwriting exports. This change clarifies side effects, view replacement/order, and report recovery rather
than adding features. The Developer Guide also acknowledges Edison's reported AI use. No release, tag or milestone
was changed; acknowledgements for other members are outside this PR's scope.

## Reproduced defect and correction

The original export writer used `CREATE_NEW` directly on `debtor-ID.txt`. An injected writer that writes
`incomplete profile` and then throws `IOException("disk full after partial write")` left that incomplete report
at the final path. The new regression failed on the original implementation at the assertion that the final
path must not exist. The leftover file also blocked the normal retry.

Export now writes a temporary file in the export directory, moves it to the report name only after the write
completes, and removes the temporary file in a `finally` block. The move does not request replacement of an
existing report. The focused regression verifies the error, absent partial final file, temporary-file cleanup,
unchanged debtor, successful retry and preservation of a subsequent existing report. Existing tests also verify
successful UTF-8 export, collision handling, obstructed paths and symbolic-link-directory rejection.

Production diff: **9 added and 2 removed lines**, all in `ExportCommand.java`, comfortably below the requested
approximately 105-line ceiling. Tests and documentation account for the remaining changes.

## Environment and artifact

* Debian GNU/Linux 13, Linux amd64; Oracle JDK `25.0.5.0.3`; Gradle `9.6.1`; JavaFX `17.0.7`; Xvfb.
* Artifact: locally built `build/libs/osps.jar` from the upstream baseline plus this branch's production patch.
* SHA-256: `df5cce2425d123316a549360400ec824fc3f099950add1455df00fa13d67b913`.
* This is a tested branch candidate, not a downloaded published release or a team-approved final candidate.
  Rebuilding from changed source requires recording the new checksum and rerunning the relevant checks.

## Checks and results

### Follow-up on 2 November 2026

After merging team `master` `b75aeb30` into this branch, the combined source at merge commit
`e7453267ae69faf6a242956f73c5ecf7a05a96cf` was built and checked again on Debian 13 amd64 with Oracle JDK
25.0.5 and Xvfb. The locally built JAR SHA-256 was
`c2ab71ed3e13b9acdb122ac7b8b5931e278d90784fdbde415d3ce289da154d13`. Subsequent edits to this record and the
AI acknowledgement are documentation only. This remains a branch candidate, not the team's final release.

The same Gradle and repository checks below passed: **819 passed, 1 skipped, 0 failures/errors** out of 820
discovered tests. The skipped test was `PackagedWorklistRenderingTest`; all four packaged-JAR tests, six export
tests, four contact workflow tests and the preview workflow test ran and passed. The packaged driver used the
same command-field action mechanism described below, not physical keyboard input. Human beta testing, an
independent review, and checks of the final candidate on the supported platforms remain outstanding.

### Original 1 November run

Run from the repository root with JDK 25 activated:

```sh
xvfb-run -a bash gradlew --no-daemon --max-workers=2 check coverage
(cd .github && bash run-checks.sh)
```

Both completed successfully. The completed JUnit XML results contain **795 tests, 0 failures, 0 errors, 0 skips**.
This includes six export tests, four contact workflow tests, one preview workflow test, and four packaged-JAR
tests. Checkstyle and coverage generation passed. The initial failing regression run is distinct from this final
successful run.

The packaged tests launch the actual JAR in separate JVMs with disposable data and a test-only driver that is
not shipped in the JAR. Commands are submitted through the application's command field and its action handler.
This exercises the command UI but does not simulate physical keyboard focus/navigation.

| Starting condition and steps | Verified result |
| --- | --- |
| Disposable JAR home; clear, add synthetic `Packaged Smoke`; read its assigned persistent ID | No assumption that the new debtor has ID 1; exactly one debtor with balance 25.00 |
| `preview edit 1 o/50`, then `preview delete 1` | Original debtor and balance remain unchanged |
| `contact --id ID --method sms --hours 09:00-17:30`; undo, redo | Undo restores unset method; redo restores the preferences |
| `contactable --at 17:30 --method sms`, then `contactable --at 09:00 --method sms` | End excluded, start included |
| `export --id ID`, repeat the same command | One report containing the debtor and SMS preference; repeat rejected without replacement |
| Exit and restart the same copied JAR | Debtor, balance and contact preferences restored; JSON unchanged by the read-only restart |
| Launch copied JAR from a different folder, including a JAR path with spaces | Data, preferences, logs and export stay beside the JAR; tested JAR bytes unchanged |

Other existing tests cover contact save failure, no-op saves, contact preferences surviving edits/notes/reload,
and preview on reordered views without storage writes. They were rerun rather than duplicated.

## Remaining release checks and limitations

* A human teammate must independently review this fix and perform actual keyboard-only beta testing. This record
  does not satisfy those personal participation requirements.
* macOS, Windows, physical small-screen and scaling checks were not available here; no compatibility claim is made
  for those checks. The release coordinator must test the exact final distributed bytes on available supported platforms.
* Temporary staging is not a power-loss durability guarantee. A killed process or cleanup failure can leave a
  temporary file; the guide explains closing OSPS before removing it. Reported write failures leave debtor data intact.
* The research's release/source/guide mismatch remains a release-coordination item. This branch does not publish
  a replacement JAR or certify the currently downloadable release.

## AI usage for the consolidated acknowledgement

OpenAI Codex read the research and current source, identified and reproduced the partial-write failure, drafted
the fix, regression and packaged-test extensions, clarified the guide, and ran the reported automated checks.
This paragraph describes this task only; each member must provide their actual broader usage separately.
