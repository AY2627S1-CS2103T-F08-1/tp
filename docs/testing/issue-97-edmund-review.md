# Issue #97: deletion review preparation

Technical review prepared on 29 October 2026 for Alfred's review responsibility under the Week 11 rotation.
This document is AI-assisted preparation. Alfred should inspect the reasoning and evidence before submitting his
own substantive review. No GitHub review or approval has been submitted on his behalf.

## Target and scope

* PR: [#84, Prevent oversized delete indexes from blocking the UI](https://github.com/AY2627S1-CS2103T-F08-1/tp/pull/84).
* Author: Edmund (`ewonglh`).
* Reviewed PR head: `02d1a46d8b99e20958cbf687e8c06c0db3cfcd06`.
* Merge commit: `967c671a9903a7abdd4bbe6d90e88837c733d0f8` (already merged on 21 October).
* Integrated implementation checked at `ab749e07`.
* No open PR by Edmund was available at the time of this check. This retrospective check must not be presented as
  pre-merge approval or substituted for reviewing a later fix at its actual head.

## Findings and reasoning

No blocking defect was identified in the reviewed deletion path.

1. `DeleteCommandParser` scans decimal characters once and removes leading zeroes before constructing the command.
   It rejects blank, zero-only and malformed targets without changing the model. User input does not enter the
   expensive `BigInteger` conversion previously used for oversized indexes.
2. `DeleteCommand` compares normalized decimal length and then lexical value against the displayed count. It calls
   `Integer.parseInt` only after the requested index is known to fit inside that list, avoiding numeric overflow.
3. The target is obtained from `getFilteredPersonList()` and removed by its record object. It therefore uses the
   displayed position, including filtered results, while retaining unrelated IDs and data.
4. Inputs above 100 significant digits receive a bounded preview plus digit count. This limits feedback size rather
   than rejecting long input or imposing an undocumented input-length limit.
5. Added diagnostics contain fixed text and do not expose raw input or debtor details. The success-path diagnostic
   accurately describes in-memory deletion; it does not claim that the subsequent persistence step succeeded.

## Verification evidence

The full Java 25 `clean check coverage` run in the issue-97 worktree passed. Relevant existing regressions include:

* `DeleteCommandParserTest`: million-digit populated/empty-list targets; leading zeroes; malformed targets; int
  boundaries; 100/101 significant-digit feedback boundaries; no model mutation on rejection.
* `DeleteCommandTest`: first/middle/last/only record; similar names; filtered targets; remaining filter matches;
  empty and out-of-range targets.
* `DeleteCommandLoggingTest`: safe fixed-text messages for success, oversized rejection and model failure.
* `LogicManagerTest`: successful deletion reload, rejected-command model/disk immutability, and save failure without
  rollback claims.
* Released v1.4 checks in the [verification record](v1.5-note-history-verification.md): filtered deletion preserved
  the surviving debtor, zero balance and full repeated-note history across separate JVM processes.

The JavaFX selection/scrolling behaviour remains a manual check. The released-JAR checks do not validate an
unmerged future PR or the final v1.5 artifact.

## Suggested review text for Alfred to assess

I checked the normalized-string index path, the range-before-int-conversion guard, filtered-list target selection,
bounded oversized-index feedback, and the distinction between model mutation and persistence success. I found no
blocking issue in those paths. The million-digit, boundary, filtering and rejected-mutation regressions passed in
the integrated Java 25 test run. This is a retrospective check of the merged implementation; I still need to inspect
any subsequent fix at its current PR head. GUI selection behaviour remains separately tracked.
