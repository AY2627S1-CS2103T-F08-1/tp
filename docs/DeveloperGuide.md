---
layout: page
title: Developer Guide
---
* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* _{List the sources of reused or adapted ideas, code, documentation, and third-party libraries here, with links to the originals.}_

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

<div markdown="span" class="alert alert-primary">

:bulb: **Tip:** The `.puml` files used to create diagrams are in `docs/diagrams`. Refer to the [_PlantUML Tutorial_ at se-edu/guides](https://se-education.org/guides/tutorials/plantUml.html) to learn how to create and edit diagrams.
</div>

### Architecture

<img src="images/ArchitectureDiagram.png" width="280" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

The following provides a quick overview of the main components and their interactions.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delete 1`.

<img src="images/ArchitectureSequenceDiagram.png" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<img src="images/ComponentManagers.png" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/Ui.java)

![Structure of the UI Component](images/UiClassDiagram.png)

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<img src="images/LogicClassDiagram.png" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete 1")` API call as an example.

![Interactions Inside the Logic Component for the `delete 1` Command](images/DeleteSequenceDiagram.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</div>

How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a person).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<img src="images/ParserClasses.png" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/model/Model.java)

<img src="images/ModelClassDiagram.png" width="450" />


The `Model` component,

* stores the address book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `AddressBook`, and each `Person` references tags from that list. This lets `AddressBook` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<img src="images/BetterModelClassDiagram.png" width="450" />

</div>


### Storage component

**API** : [`Storage.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/storage/Storage.java)

<img src="images/StorageClassDiagram.png" width="550" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### \[Proposed\] Undo/redo feature

#### Proposed Implementation

The proposed undo/redo mechanism is facilitated by `VersionedAddressBook`. It extends `AddressBook` with an undo/redo history, stored internally as an `addressBookStateList` and `currentStatePointer`. Additionally, it implements the following operations:

* `VersionedAddressBook#commit()` — Saves the current address book state in its history.
* `VersionedAddressBook#undo()` — Restores the previous address book state from its history.
* `VersionedAddressBook#redo()` — Restores a previously undone address book state from its history.

These operations are exposed in the `Model` interface as `Model#commitAddressBook()`, `Model#undoAddressBook()` and `Model#redoAddressBook()` respectively.

Given below is an example usage scenario and how the undo/redo mechanism behaves at each step.

Step 1. The user launches the application for the first time. The `VersionedAddressBook` will be initialized with the initial address book state, and the `currentStatePointer` pointing to that single address book state.

![UndoRedoState0](images/UndoRedoState0.png)

Step 2. The user executes `delete 5` command to delete the 5th person in the address book. The `delete` command calls `Model#commitAddressBook()`, causing the modified state of the address book after the `delete 5` command executes to be saved in the `addressBookStateList`, and the `currentStatePointer` is shifted to the newly inserted address book state.

![UndoRedoState1](images/UndoRedoState1.png)

Step 3. The user executes `add n/David …​` to add a new person. The `add` command also calls `Model#commitAddressBook()`, causing another modified address book state to be saved into the `addressBookStateList`.

![UndoRedoState2](images/UndoRedoState2.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** If a command fails its execution, it will not call `Model#commitAddressBook()`, so the address book state will not be saved into the `addressBookStateList`.

</div>

Step 4. The user now decides that adding the person was a mistake, and decides to undo that action by executing the `undo` command. The `undo` command will call `Model#undoAddressBook()`, which will shift the `currentStatePointer` once to the left, pointing it to the previous address book state, and restores the address book to that state.

![UndoRedoState3](images/UndoRedoState3.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** If the `currentStatePointer` is at index 0, pointing to the initial AddressBook state, then there are no previous AddressBook states to restore. The `undo` command uses `Model#canUndoAddressBook()` to check if this is the case. If so, it will return an error to the user rather
than attempting to perform the undo.

</div>

The following sequence diagram shows how an undo operation goes through the `Logic` component:

![UndoSequenceDiagram](images/UndoSequenceDiagram-Logic.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The lifeline for `UndoCommand` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.

</div>

Similarly, how an undo operation goes through the `Model` component is shown below:

![UndoSequenceDiagram](images/UndoSequenceDiagram-Model.png)

The `redo` command does the opposite — it calls `Model#redoAddressBook()`, which shifts the `currentStatePointer` once to the right, pointing to the previously undone state, and restores the address book to that state.

<div markdown="span" class="alert alert-info">:information_source: **Note:** If the `currentStatePointer` is at index `addressBookStateList.size() - 1`, pointing to the latest address book state, then there are no undone AddressBook states to restore. The `redo` command uses `Model#canRedoAddressBook()` to check if this is the case. If so, it will return an error to the user rather than attempting to perform the redo.

</div>

Step 5. The user then decides to execute the command `list`. Commands that do not modify the address book, such as `list`, will usually not call `Model#commitAddressBook()`, `Model#undoAddressBook()` or `Model#redoAddressBook()`. Thus, the `addressBookStateList` remains unchanged.

![UndoRedoState4](images/UndoRedoState4.png)

Step 6. The user executes `clear`, which calls `Model#commitAddressBook()`. Since the `currentStatePointer` is not pointing at the end of the `addressBookStateList`, all address book states after the `currentStatePointer` will be purged. Reason: It no longer makes sense to redo the `add n/David …​` command. This is the behavior that most modern desktop applications follow.

![UndoRedoState5](images/UndoRedoState5.png)

The following activity diagram summarizes what happens when a user executes a new command:

<img src="images/CommitActivityDiagram.png" width="250" />

#### Design considerations:

**Aspect: How undo & redo execute:**

* **Alternative 1 (current choice):** Saves the entire address book.
  * Pros: Easy to implement.
  * Cons: May have performance issues in terms of memory usage.

* **Alternative 2:** Individual command knows how to undo/redo by
  itself.
  * Pros: Will use less memory (e.g. for `delete`, just save the person being deleted).
  * Cons: We must ensure that the implementation of each individual command is correct.

_{more aspects and alternatives to be added}_

### \[Proposed\] Data archiving

_{Explain here how the data archiving feature will be implemented}_


--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

**Target user profile**:

* works in debt collection or recovery
* manages numerous debtor contacts, repayment commitments, outstanding amounts, and follow-ups
* is comfortable with desktop applications
* can type quickly and prefers keyboard commands to mouse-heavy workflows
* needs efficient access to accurate debtor contact and case information

**Value proposition**: OSPS aims to help fast-typing debt recovery employees organize debtor details, repayment status, payment dates, and interaction notes in one place through keyboard-driven search and update workflows, reducing administrative work, lowering the risk of missed follow-ups, and helping users prioritize cases while keeping contact management as the application's core focus.


### User stories

Priorities: High (must have) - `* * *`, Medium (should have) - `* *`, Low (could have) - `*`

| Priority | As a …​ | I want to …​ | So that I can …​ |
| -------- | ------ | ------------ | ---------------- |
| `* * *` | debt recovery agent | add a new debtor with their contact details and outstanding amount | officially register their case and begin tracking it |
| `* * *` | debt recovery agent | append interaction notes to a debtor's profile | keep an accurate, chronological history of call discussions |
| `* * *` | debt recovery agent | view all active cases sorted by the closest follow-up date | identify and prioritise people to call today |
| `* * *` | debt recovery agent | mark a debtor's case as settled or delete it | keep the active dashboard uncluttered after recovery |
| `* * *` | debt recovery agent | view a debtor's complete profile | see their contact details, debt, deadlines, status, and notes together |
| `* *` | fast-typing employee | use GNU-style keyboard commands to execute all actions | maintain my productivity without needing to reach for the mouse |
| `* *` | busy debt collector | quickly search for a debtor by name or phone number | instantly pull up their case details when they unexpectedly call |
| `* *` | debt recovery agent | set a follow-up deadline and promised payment date for a debtor | avoid missing critical contact windows |
| `* *` | debt recovery agent | update a debtor's outstanding amount | ensure the system reflects partial repayments accurately |
| `* *` | new team member | view a help menu summarising all available commands | learn the application's syntax without external documentation |
| `* *` | debt recovery agent | edit a debtor's contact details | keep their phone number, email address, and postal address current |
| `* *` | considerate debt collector | record a debtor's preferred contact method and contactable hours | contact them through an appropriate channel at a suitable time |
| `* *` | debt recovery agent | record the outcome of each contact attempt | track whether the debtor answered, requested a callback, or was unreachable |
| `* *` | debt recovery agent | assign a recovery stage to a debtor | distinguish new, contacted, negotiating, disputed, and broken-promise cases |
| `* *` | busy debt collector | list debtors whose follow-ups are due or overdue | immediately identify cases requiring attention |
| `* *` | debt recovery agent | filter debtor contacts by recovery stage | focus on a category of cases without scanning the entire list |
| `*` | careful employee | undo the most recent command | recover quickly after an accidental deletion or incorrect amount |
| `*` | fast-typing employee | find debtors using partial or slightly misspelled names | retrieve the right contact without remembering the exact spelling |
| `*` | careful employee | preview an update or deletion command | verify the affected debtor and changes before committing a risky action |
| `*` | compliance-conscious debt recovery agent | export a debtor's profile and interaction history | provide a case record for auditing or authorised handover |

### Use cases

(For all use cases below, the **System** is `OSPS` and the **Actor** is the `debt recovery agent`, unless
specified otherwise)

**Use case: Add a debtor**

**MSS**

1.  Debt recovery agent requests to add a new debtor with the required debtor details.
2.  OSPS validates the provided debtor details.
3.  OSPS adds the debtor record.
4.  OSPS confirms that the debtor record has been added.

    Use case ends.

**Extensions**

* 1a. Debt recovery agent omits a required debtor detail.

    * 1a1. OSPS shows an error message explaining which required detail is missing.

      Use case resumes at step 1.

* 2a. The provided debtor details are invalid.

    * 2a1. OSPS shows an error message explaining the invalid detail.

      Use case resumes at step 1.

**Use case: List debtors**

**MSS**

1.  Debt recovery agent requests to view all debtor records.
2.  OSPS shows a list of debtor records with summary information.

    Use case ends.

**Extensions**

* 2a. There are no debtor records.

    * 2a1. OSPS informs the debt recovery agent that no debtor records are available.

      Use case ends.

**Use case: View debtor profile**

**MSS**

1.  Debt recovery agent requests to view the list of debtor records.
2.  OSPS shows a list of debtor records with summary information.
3.  Debt recovery agent selects a debtor record to view in detail.
4.  OSPS shows the selected debtor's profile, including debtor details, outstanding balance, repayment status,
    payment dates, and interaction notes where available.

    Use case ends.

**Extensions**

* 2a. There are no debtor records.

    * 2a1. OSPS informs the debt recovery agent that no debtor records are available.

      Use case ends.

* 3a. The selected debtor record does not exist.

    * 3a1. OSPS shows an error message.

      Use case resumes at step 2.

**Use case: Add an interaction note**

**MSS**

1.  Debt recovery agent requests to view a debtor profile.
2.  OSPS shows the selected debtor's profile.
3.  Debt recovery agent requests to add an interaction note to the debtor profile.
4.  OSPS validates the interaction note.
5.  OSPS adds the interaction note to the debtor profile.
6.  OSPS confirms that the interaction note has been added.

    Use case ends.

**Extensions**

* 1a. The selected debtor record does not exist.

    * 1a1. OSPS shows an error message.

      Use case ends.

* 4a. The interaction note is empty or invalid.

    * 4a1. OSPS shows an error message explaining why the note cannot be added.

      Use case resumes at step 3.

**Use case: Delete a debtor**

**MSS**

1.  Debt recovery agent requests to view all debtor records.
2.  OSPS shows a list of debtor records with summary information.
3.  Debt recovery agent selects a debtor record to delete.
4.  OSPS deletes the selected debtor record.
5.  OSPS confirms that the debtor record has been deleted.

    Use case ends.

**Extensions**

* 2a. There are no debtor records.

    * 2a1. OSPS informs the debt recovery agent that no debtor records are available.

      Use case ends.

* 3a. The selected debtor record does not exist.

    * 3a1. OSPS shows an error message.

      Use case resumes at step 2.

### Non-Functional Requirements

The following requirements describe qualities of OSPS rather than individual commands.
Unless otherwise stated, response-time measurements use a supported desktop machine
with a representative dataset of 1,000 debtor records.

1. **NFR-Portability**: OSPS must run on Windows, Linux, and macOS systems with
   Java `25` installed. The documented setup procedure must be sufficient to launch
   OSPS and complete a basic debtor-listing workflow on each operating system.
2. **NFR-Capacity and responsiveness**: OSPS must retain at least 1,000 debtor
   records, including balances, repayment statuses, dates, and interaction notes.
   With that dataset, listing debtors and opening one debtor's profile must each
   complete within 2 seconds.
3. **NFR-Persistence**: After a successful change, restarting OSPS must reproduce
   every changed debtor field and interaction note exactly as before shutdown. This
   is verified with a save--restart--compare test.
4. **NFR-Input integrity**: Invalid commands or field values must not change existing
   debtor records. OSPS must return a message identifying the invalid input so the
   agent can correct it; this is verified with invalid-input tests before and after
   persistence.
5. **NFR-Keyboard workflow**: The documented primary workflows---adding a debtor,
   listing debtors, viewing a debtor profile, and recording an interaction note---must
   be completable through the CLI without mouse input after launch.

### Glossary

* **Debt recovery agent**: An OSPS user who manages debtor records and follows up on outstanding balances.
* **Debtor**: A person or organisation with an outstanding payment obligation recorded in OSPS.
* **Follow-up date**: The date on which an agent next plans to contact or review a debtor's case.
* **Interaction history**: The collection of interaction notes associated with a debtor.
* **Interaction note**: A dated record of a contact or other relevant interaction with a debtor.
* **Outstanding balance**: The amount that remains to be paid under a debtor's payment scheme.
* **Payment scheme**: The agreed plan describing how and when an outstanding balance is repaid.
* **Repayment status**: An indicator of the current progress of a debtor's payment scheme.

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<div markdown="span" class="alert alert-info">:information_source: **Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.

</div>

### Launch and shutdown

1. Initial launch

   1. Download the JAR file and copy it into an empty folder.

   1. Double-click the JAR file.<br>
      Expected: The GUI opens with a set of sample contacts. The window size may not be optimal.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app by double-clicking the JAR file.<br>
       Expected: The most recent window size and location are retained.

1. _{ more test cases …​ }_

### Deleting a person

1. Deleting a person while all persons are being shown

   1. Prerequisites: List all persons using the `list` command, with multiple persons in the list.

   1. Test case: `delete 1`<br>
      Expected: The first contact is deleted from the list. The status message shows the deleted contact's details.

   1. Test case: `delete 0`<br>
      Expected: No person is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

1. _{ more test cases …​ }_

### Saving data

1. Dealing with missing/corrupted data files

   1. _{Explain how to simulate missing or corrupted data files and state the expected behavior.}_

1. _{ more test cases …​ }_
