---
layout: page
title: User Guide
---

Orchestration and Scheduling of Payment Scheme (OSPS) v1.3 is a **desktop application for debt recovery agents**.
It manages debtor contact details, outstanding balances, and interaction histories through keyboard-driven commands
while retaining the benefits of a graphical user interface (GUI).

* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download `osps.jar` from the latest
   [OSPS release](https://github.com/AY2627S1-CS2103T-F08-1/tp/releases).

1. Copy the file to the folder you want to use as the _home folder_ for OSPS.

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar osps.jar`.<br>
   The OSPS window should appear in a few seconds with sample debtor data on a first run.

   <div markdown="span" class="alert alert-warning">
   The image below is an interface concept from an earlier iteration, not a screenshot of the v1.3 executable. It must
   be replaced with a current application screenshot before the v1.3 release is published.
   </div>

   ![OSPS interface concept](images/Ui.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Some example commands you can try:

   * `list` : Lists all active debtors.

   * `add n/Olaf Go p/91234567 e/olaf.letnonego@example.com a/H4M8+3J3, Kunlun Station, Antarctica o/S$1,250.00` : Adds a debtor named `Olaf Go` to OSPS.

   * `note --id 1 --text Called debtor, promised payment on Friday.` : Adds an interaction note to debtor ID 1.

   * `show --id 1` : Shows the complete profile and interaction history of debtor ID 1.

   * `delete 3` : Deletes the 3rd debtor shown in the current list.

   * `clear` : Deletes all debtor records.

   * `exit` : Exits the app.

1. Refer to the [Features](#features) section below for details of each command.

--------------------------------------------------------------------------------------------------------------------

## Features

<div markdown="block" class="alert alert-info">

**:information_source: Notes about the command format:**<br>

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `add n/NAME`, replace `NAME` with a value such as `John Doe`.

* Items in square brackets are optional.<br>
  For example, `n/NAME [t/TAG]` can be used as `n/John Doe t/friend` or as `n/John Doe`.

* Items followed by `…`​ can appear zero or more times.<br>
  For example, `[t/TAG]…​` may be omitted, or written as `t/friend` or `t/friend t/family`.

* Parameters can be in any order.<br>
  For example, if the command specifies `n/NAME p/PHONE_NUMBER`, `p/PHONE_NUMBER n/NAME` is also acceptable.

* Extraneous parameters for commands that take no parameters, such as `help`, `exit`, and `clear`, are ignored.<br>
  For example, `help 123` is interpreted as `help`.

* The `list` command accepts no parameters. Unexpected parameters are rejected with an error message so that typing
  mistakes are not silently ignored.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</div>

### Choosing the correct debtor number

Each debtor card shows both a displayed index and a persistent debtor ID. In a heading such as `2. [ID 7] Alex Tan`,
`2` is the debtor's current displayed index while `7` is the debtor's persistent ID.

* Use the persistent ID with `show --id DEBTOR_ID` and `note --id DEBTOR_ID --text NOTE_TEXT`.
* Use the displayed index with `delete INDEX` and `edit INDEX ...`.
* A persistent ID stays with the same debtor until that debtor is deleted. A displayed index can change after a `find`,
  `list`, `add`, or `delete` command.

### Viewing help: `help`

Shows a message explaining how to access the help page.

![help message](images/helpMessage.png)

Format: `help`


### Adding a debtor: `add`

Adds a debtor to OSPS. The outstanding amount is optional, may be zero, and defaults to `S$0.00`.
Each debtor is assigned a unique ID, which is shown in the debtor list. IDs are assigned from 1 again after
using `clear`.

Format: `add n/NAME p/PHONE_NUMBER e/EMAIL a/ADDRESS [o/OUTSTANDING_AMOUNT] [t/TAG]…​`

<div markdown="span" class="alert alert-primary">:bulb: **Tip:**
A debtor can have any number of tags, including zero.
</div>

* `OUTSTANDING_AMOUNT` must be a non-negative number with at most two decimal places. An optional `S$` or `$` prefix
  and correctly grouped comma separators are accepted; for example, `0`, `S$0.00`, `$1250.5`, and `S$1,250.00`.
* Negative values, malformed currency, and values with more than two decimal places are rejected.
* Phone numbers and email addresses must be unique across active debtors. Debtors may share the same name.

Examples:
* `add n/Olaf Go p/91234567 e/olaf.letnonego@example.com a/H4M8+3J3, Kunlun Station, Antarctica o/S$1,250.00`
* `add n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01`
* `add n/Betsy Crowe t/friend e/betsycrowe@example.com a/Newgate Prison p/1234567 t/criminal`

### Listing all debtors: `list`

Shows every active debtor in ascending debtor ID order. Each debtor card shows the ID, name, phone number,
outstanding amount, email address, address, and tags. The result reports the active-debtor count and displays
a helpful message when there are no active debtors.

Format: `list`

The command does not accept any parameters.

Examples of result messages:
* `No active debtors found (0 active). Add a debtor with the add command.`
* `1 active debtor listed.`
* `5 active debtors listed.`

### Adding an interaction note: `note`

Adds a timestamped interaction note to the debtor identified by debtor ID.

Format: `note --id DEBTOR_ID --text NOTE_TEXT`

* `DEBTOR_ID` must be a positive integer shown as the debtor's ID in the debtor list.
* `NOTE_TEXT` must not be blank. Ordinary punctuation is allowed.
* The `--id` and `--text` parameters can be provided in either order.
* The note is saved with the debtor and is preserved after restarting the application.

Example:
* `note --id 1 --text Called debtor, promised payment on Friday.`

### Viewing a debtor profile: `show`

Shows the complete profile of the active debtor identified by persistent debtor ID. The profile includes the
debtor's name, phone number, email address, address, outstanding amount, tags, and interaction history. Interaction
notes are shown newest first. If the debtor has no notes, the profile states that no interaction notes are recorded.

Format: `show --id DEBTOR_ID`

* `DEBTOR_ID` must be a positive integer shown as the debtor's ID in the debtor list.
* Debtor IDs remain associated with the same debtor when the displayed list is filtered or reordered.
* A deleted debtor cannot be viewed. The command reports that no active debtor with that ID exists.
* The command accepts exactly one `--id` parameter. For example, `show 3` is not supported.

Example:
* `show --id 3` shows the complete profile of the active debtor with ID 3.

### Editing a debtor: `edit`

Edits an existing debtor in OSPS.

Format: `edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [t/TAG]…​`

* Edits the debtor at the specified `INDEX`. The index refers to the index number shown in the displayed debtor list.
  The index **must be a positive integer** 1, 2, 3, …​
* At least one of the optional fields must be provided.
* Existing values will be updated to the input values.
* When editing tags, all of the debtor's existing tags are removed; adding tags is not cumulative.
* To remove all of a debtor's tags, enter `t/` without a tag after it.

Examples:
* `edit 1 p/91234567 e/johndoe@example.com` edits the phone number and email address of the 1st displayed debtor.
* `edit 2 n/Betsy Crower t/` edits the name of the 2nd displayed debtor and clears all existing tags.

### Locating debtors by name: `find`

Finds debtors whose names contain any of the given keywords.

Format: `find KEYWORD [MORE_KEYWORDS]`

* The search is case-insensitive; for example, `hans` matches `Hans`.
* Keyword order does not matter; for example, `Hans Bo` matches `Bo Hans`.
* The search considers only names.
* Only full words match; for example, `Han` does not match `Hans`.
* Persons matching at least one keyword are returned (an `OR` search); for example, `Hans Bo` returns `Hans Gruber` and `Bo Yang`.

Examples:
* `find John` returns `john` and `John Doe`
* `find alex david` returns `Alex Yeoh`, `David Li`<br>
  ![result for 'find alex david'](images/findAlexDavidResult.png)

### Deleting a debtor: `delete`

Deletes the specified debtor from OSPS.

Format: `delete INDEX`

* Deletes the debtor at the specified `INDEX`.
* The index refers to the one-based position in the currently displayed debtor list, including `find` results.
* After deletion, the list refreshes and the current filter remains active.
* The index **must be a positive integer** 1, 2, 3, …​
* For an index outside the displayed list, the error gives the displayed count and valid range. If no debtors are shown,
  run `list` or adjust the `find` query.

Examples:
* `list` followed by `delete 2` deletes the 2nd displayed debtor.
* `find Betsy` followed by `delete 1` deletes the 1st displayed debtor in the `find` results.

### Clearing all entries: `clear`

Clears all debtor records from OSPS.

Format: `clear`

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Saving the data

OSPS automatically saves data after every successful data-changing command. You do not need to save manually.

### Editing the data file

OSPS data is saved as `[JAR file location]/data/addressbook.json`. The path is unchanged from the earlier application,
so an existing data file continues to be discovered after upgrading to `osps.jar`. Advanced users may update data
directly by editing that JSON file.

<div markdown="span" class="alert alert-warning">:exclamation: **Caution:**
If your changes make the data file invalid, OSPS starts with no debtor records at the next run. The invalid file remains
on disk until you run a data-changing command. Back up the file before editing it.<br>
Furthermore, certain edits can cause OSPS to behave unexpectedly (for example, if a value is outside the accepted
range). Edit the data file only if you are confident that you can update it correctly.
</div>

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install OSPS on the other computer and overwrite the data file it creates with the `data/addressbook.json` file
from your previous OSPS home folder.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.
2. **If you minimize the Help Window** and then run the `help` command (or use the `Help` menu, or the keyboard shortcut `F1`) again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action | Format, Examples
--------|------------------
**Add** | `add n/NAME p/PHONE_NUMBER e/EMAIL a/ADDRESS [o/OUTSTANDING_AMOUNT] [t/TAG]…​` <br> e.g., `add n/Olaf Go p/91234567 e/olaf.letnonego@example.com a/H4M8+3J3, Kunlun Station, Antarctica o/S$1,250.00`
**Clear** | `clear`
**Delete** | `delete INDEX`<br> e.g., `delete 3`
**Edit** | `edit INDEX [n/NAME] [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]…​`<br> e.g., `edit 2 n/James Lee e/jameslee@example.com`
**Find** | `find KEYWORD [MORE_KEYWORDS]`<br> e.g., `find James Jake`
**List** | `list`
**Note** | `note --id DEBTOR_ID --text NOTE_TEXT`<br> e.g., `note --id 1 --text Called debtor, promised payment on Friday.`
**Show** | `show --id DEBTOR_ID`<br> e.g., `show --id 3`
**Help** | `help`
**Exit** | `exit`
