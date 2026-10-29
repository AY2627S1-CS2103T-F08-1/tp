# Issue #97: manual GUI checks

Alfred reported all checks below passed on 29 October 2026 after following this checklist with the released v1.4 JAR.
He supplied profile output showing Alpha's ID, contact details, S$123.45 balance, both repeated interactions and the
`--identifier` note, and confirmed correct rejection of invalid commands. He then confirmed completion of the full
checklist, including scrolling, filtered deletion and GUI exit/relaunch. These are user-reported manual results;
the agent did not operate the JavaFX window, and no GUI screenshots were supplied with the confirmation.

Automated command/storage checks are recorded separately and do not establish visual results.
Use the released v1.4 JAR in a new writable folder with synthetic data and Java 25. Record the JAR SHA-256, OS,
Java version and actual results. Do not use the working folder that contains personal records.

## Start with two identifiable records

Run `clear` only in this disposable dataset, then:

```text
add n/Alpha Tester p/98880001 e/alpha@example.com a/Alpha Street o/123.45
add n/Beta Tester p/98880002 e/beta@example.com a/Beta Street o/50
list
```

Record Alpha's persistent ID. Replace `ALPHA_ID` in subsequent commands with that value; do not assume it is the
displayed position. The released v1.4 `clear` behaviour differs from the integrated ID-allocation fix.

## Note display and rejected input

```text
note --id ALPHA_ID --text Called debtor; callback at 5pm?
note --text Called debtor; callback at 5pm? --id ALPHA_ID
note --id ALPHA_ID --text Use the --identifier portal.  Follow-up?
show --id ALPHA_ID
```

Confirm that all three notes are readable and the repeated interaction appears twice. Add a long synthetic note
and check that the complete history can be scrolled and read at normal and reduced window sizes. Capture the result.

Run each rejected command separately, then show Alpha and confirm that history did not grow:

```text
note --id ALPHA_ID --text
note --id 0 --text Rejected
note --id ALPHA_ID --text Reserved --text token
```

## Edit, filtered deletion and actual GUI restart

```text
list
edit 1 n/Alpha Updated o/0
find Beta
delete 1
list
show --id ALPHA_ID
exit
```

Before editing, verify that Alpha is displayed at position 1; adjust the index if necessary. Confirm only Beta was
deleted, Alpha's profile/list display agree, and all Alpha notes remain. Relaunch with `java -jar "osps.jar"` from the
same working folder, then run `list` and `show --id ALPHA_ID`. Check the updated name, S$0.00 balance, unchanged ID,
full history and absence of Beta. Capture the post-restart result.

| Check | Actual result / evidence |
| --- | --- |
| Repeated notes and punctuation readable | Passed; profile text supplied by Alfred |
| Long-history scrolling and reduced-width readability | Passed; Alfred's full-checklist confirmation |
| Rejected input leaves history unchanged | Passed; Alfred confirmed rejection and full-checklist completion |
| Edit updates visible details and preserves history | Passed; Alfred's full-checklist confirmation |
| Filtered deletion removes only Beta | Passed; Alfred's full-checklist confirmation |
| GUI exit/relaunch preserves ID, balance and full history | Passed; Alfred's full-checklist confirmation |

Do not test exact equal timestamps by racing manual input. The deterministic fixed-clock automated test covers
that condition; displayed timestamps have minute precision and may conceal different stored timestamps.
