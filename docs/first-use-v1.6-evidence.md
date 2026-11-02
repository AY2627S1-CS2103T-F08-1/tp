# First-use candidate check

Checked 2 November 2026 on Windows with Java 25.0.4, using the supplied `osps.jar` in a new writable folder.
JAR SHA-256: `a2af12e2798833ebb1ef796e63e441cacff93db875c8be7e0304abb10fbc6a95`.
The source commit corresponding to this JAR has not been independently verified.

The first launch created six sample debtors. Adding the fictional Maya Tan returned ID 7; a `reached` note,
`find Maya`, and `show --id 7` succeeded, and the profile contained the note and S$1,250.00 balance. `list`
subsequently showed seven active debtors. These commands were entered through the keyboard. `help` opened the
reference window; Page Down scrolled the reference and Escape closed it. `exit` closed the application successfully.

For the compact-window check, OSPS was started with saved dimensions of 1280×720. The capture reports a 1268×714
client area after the window frame is excluded. The main window retained a visible command box and scrollable debtor
list. Help opened as a 603×571 window; Page Down scrolled it and Escape closed it. The Windows display was configured
at 100% scaling, so 150% display scaling remains untested.

The capture interface adds a cursor highlight and renders text poorly, so its images are not suitable for publication.
They were not substituted into the guide. A clean v1.6 main-window, Help, and homepage capture is still required.
The candidate JAR's source commit has also not been independently verified.
