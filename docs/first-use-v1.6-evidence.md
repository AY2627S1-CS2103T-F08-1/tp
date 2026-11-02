# First-use candidate check

Checked 2 November 2026 on Windows with Java 25.0.4, using the supplied `osps.jar` in a new writable folder.
JAR SHA-256: `a2af12e2798833ebb1ef796e63e441cacff93db875c8be7e0304abb10fbc6a95`.
The source commit corresponding to this JAR has not been independently verified.

The first launch created six sample debtors. Adding the fictional Maya Tan returned ID 7; a `reached` note,
`find Maya`, and `show --id 7` succeeded, and the profile contained the note and S$1,250.00 balance. `list`
subsequently showed seven active debtors. These commands were entered through the keyboard. `help` opened the
reference window; Page Down scrolled the reference and Escape closed it. `exit` closed the application successfully.

For the display check, Windows was set to 1280×720 at 150% scaling (an effective 853×480 desktop). OSPS opened with
a visible command box and a scrollable debtor list. Help opened as a 603×426 logical-pixel window; Page Down scrolled
it and Escape closed it. No reproducible usability problem was found in this condition. The laptop was restored to its
original 2560×1600 resolution at 150% scaling afterwards.

Clean native screenshots from the supplied v1.6 JAR now replace the homepage/User Guide main-window image and the
User Guide Help image. They use the fictional Maya Tan record from the walkthrough and contain no local file path or
pointer overlay. The candidate JAR's source commit has not been independently verified.
