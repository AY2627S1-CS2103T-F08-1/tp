package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.HOON;
import static seedu.address.testutil.TypicalPersons.IDA;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.ContactOutcome;
import seedu.address.model.person.InteractionNote;
import seedu.address.model.person.Person;
import seedu.address.model.person.RecoveryStage;
import seedu.address.testutil.PersonBuilder;

public class JsonAddressBookStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonAddressBookStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readAddressBook(null));
    }

    private java.util.Optional<ReadOnlyAddressBook> readAddressBook(String filePath) throws Exception {
        return new JsonAddressBookStorage(Paths.get(filePath)).readAddressBook(addToTestDataPathIfNotNull(filePath));
    }

    private Path addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(prefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readAddressBook("NonExistentFile.json").isPresent());
    }

    @Test
    public void read_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("notJsonFormatAddressBook.json"));
    }

    @Test
    public void readAddressBook_nullDocument_throwsDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("nullAddressBook.json"));
    }


    @Test
    public void readAddressBook_invalidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidPersonAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidAndValidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidAndValidPersonAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidInteractionNoteTimestamp_throwsDataLoadingException() {
        String filePath = "invalidInteractionNoteTimestampAddressBook.json";
        assertThrows(DataLoadingException.class, () -> readAddressBook(filePath));
    }

    @Test
    public void readAddressBook_invalidInteractionNoteOutcome_throwsDataLoadingException() {
        assertThrows(DataLoadingException.class, () ->
                readAddressBook("invalidInteractionNoteOutcomeAddressBook.json"));
    }

    @Test
    public void readAddressBook_negativeDebtorId_throwsDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("negativeDebtorIdAddressBook.json"));
    }

    @Test
    public void readAddressBook_duplicateDebtorIds_throwsDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("duplicateDebtorIdAddressBook.json"));
    }

    @Test
    public void readAddressBook_legacyAndExplicitDuplicateDebtorIds_throwsDataLoadingException() {
        assertThrows(DataLoadingException.class, () ->
                readAddressBook("legacyAndExplicitDuplicateDebtorIdAddressBook.json"));
    }

    @Test
    public void readAddressBook_blankInteractionNoteText_throwsDataLoadingException() {
        String filePath = "blankInteractionNoteTextAddressBook.json";
        assertThrows(DataLoadingException.class, () -> readAddressBook(filePath));
    }

    @Test
    public void readAddressBook_nullInteractionNote_throwsDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("nullInteractionNoteAddressBook.json"));
    }

    @Test
    public void readAddressBook_nullPerson_throwsDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("nullPersonAddressBook.json"));
    }

    @Test
    public void readAddressBook_maximumDebtorId_throwsDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("maximumDebtorIdAddressBook.json"));
    }

    @Test
    public void readAddressBook_maximumNextDebtorId_acceptedAsExhaustedCounter() throws Exception {
        ReadOnlyAddressBook addressBook = readAddressBook("maximumNextDebtorIdAddressBook.json").get();
        assertEquals(1, addressBook.getPersonList().size());
        assertEquals(Integer.MAX_VALUE, addressBook.getNextDebtorId());
    }

    @Test
    public void readAddressBook_reusedNextDebtorId_throwsDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("reusedNextDebtorIdAddressBook.json"));
    }

    @Test
    public void readAddressBook_nullTag_throwsDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("nullTagAddressBook.json"));
    }

    @Test
    public void readAddressBook_legacyRecoveryStage_defaultsToNew() throws Exception {
        ReadOnlyAddressBook restored = readAddressBook("legacyRecoveryStageAddressBook.json").orElseThrow();

        assertEquals(RecoveryStage.NEW, restored.getPersonList().get(0).getRecoveryStage());
        assertEquals(RecoveryStage.NEW, restored.getPersonList().get(1).getRecoveryStage());
    }

    @Test
    public void readAddressBook_invalidRecoveryStage_throwsWithoutOverwritingFile() throws Exception {
        Path filePath = TEST_DATA_FOLDER.resolve("invalidRecoveryStageAddressBook.json");
        String originalContents = Files.readString(filePath);

        assertThrows(DataLoadingException.class, () -> new JsonAddressBookStorage(filePath).readAddressBook());

        assertEquals(originalContents, Files.readString(filePath));
    }

    @Test
    public void readAndSaveAddressBook_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempAddressBook.json");
        AddressBook original = getTypicalAddressBook();
        JsonAddressBookStorage jsonAddressBookStorage = new JsonAddressBookStorage(filePath);

        // Save in new file and read back
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        ReadOnlyAddressBook readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Modify data, overwrite existing file, and read back
        original.addPerson(HOON);
        original.removePerson(ALICE);
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Save and read without specifying file path
        original.addPerson(IDA);
        jsonAddressBookStorage.saveAddressBook(original); // file path not specified
        readBack = jsonAddressBookStorage.readAddressBook().get(); // file path not specified
        assertEquals(original, new AddressBook(readBack));

    }

    @Test
    public void saveAddressBook_atomicMoveUnsupported_keepsPreviousBytesAndDoesNotCreateTarget() throws Exception {
        Path filePath = testFolder.resolve("atomicAddressBook.json");
        JsonAddressBookStorage regularStorage = new JsonAddressBookStorage(filePath);
        regularStorage.saveAddressBook(getTypicalAddressBook());
        byte[] originalBytes = Files.readAllBytes(filePath);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath) {
            @Override
            void moveAtomically(Path source, Path target) throws IOException {
                throw new AtomicMoveNotSupportedException(source.toString(), target.toString(), "test seam");
            }
        };

        AddressBook replacement = new AddressBook();
        assertThrows(AtomicMoveNotSupportedException.class, () -> storage.saveAddressBook(replacement));
        org.junit.jupiter.api.Assertions.assertArrayEquals(originalBytes, Files.readAllBytes(filePath));
        try (var files = Files.list(testFolder)) {
            assertEquals(List.of(filePath), files.toList());
        }

        Path absentPath = testFolder.resolve("absent.json");
        JsonAddressBookStorage absentStorage = new JsonAddressBookStorage(absentPath) {
            @Override
            void moveAtomically(Path source, Path target) throws IOException {
                throw new AtomicMoveNotSupportedException(source.toString(), target.toString(), "test seam");
            }
        };
        assertThrows(AtomicMoveNotSupportedException.class, () ->
                absentStorage.saveAddressBook(getTypicalAddressBook()));
        assertFalse(Files.exists(absentPath));
    }

    @Test
    public void saveAddressBook_temporaryFileCleanupFails_stillReplacesDataFile() throws Exception {
        Path filePath = testFolder.resolve("cleanupFailureAddressBook.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath) {
            @Override
            void deleteTemporaryFile(Path temporaryFile) throws IOException {
                throw new IOException("test seam");
            }
        };

        storage.saveAddressBook(getTypicalAddressBook());

        assertEquals(getTypicalAddressBook(), new JsonAddressBookStorage(filePath).readAddressBook().orElseThrow());
    }

    @Test
    public void readAndSaveAddressBook_nextDebtorIdIsNotReused_success() throws Exception {
        Path filePath = testFolder.resolve("DebtorIdsAddressBook.json");
        AddressBook original = new AddressBook();
        original.addPerson(new PersonBuilder().withName("Alice").build());
        Person secondPerson = original.addPerson(
                new PersonBuilder().withName("Bob").withPhone("91234567").withEmail("bob@example.com").build());
        original.removePerson(secondPerson);

        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        storage.saveAddressBook(original, filePath);
        AddressBook restored = new AddressBook(storage.readAddressBook(filePath).get());
        Person addedPerson = restored.addPerson(
                new PersonBuilder().withName("Carol").withPhone("92345678").withEmail("carol@example.com").build());

        assertEquals(3, addedPerson.getDebtorId());
    }

    @Test
    public void readAndSaveAddressBook_nonzeroOutstandingAmountAndDebtorId_roundTrips() throws Exception {
        Path filePath = testFolder.resolve("OutstandingAmountAddressBook.json");
        AddressBook original = new AddressBook();
        Person debtor = original.addPerson(new PersonBuilder().withName("Alice").withPhone("91234567")
                .withEmail("alice@example.com").withOutstandingAmount("1,250.50").build());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

        storage.saveAddressBook(original, filePath);
        Person restoredDebtor = storage.readAddressBook(filePath).get().getPersonList().getFirst();

        assertEquals(debtor.getDebtorId(), restoredDebtor.getDebtorId());
        assertEquals(debtor.getOutstandingAmount(), restoredDebtor.getOutstandingAmount());
    }

    @Test
    public void readAndSaveAddressBook_interactionNotes_roundTrips() throws Exception {
        Path filePath = testFolder.resolve("InteractionNotesAddressBook.json");
        InteractionNote duplicateNote = InteractionNote.fromStorage("2026-10-09T14:35:00",
                "Sent payment reminder.", ContactOutcome.PAYMENT_PROMISED.toString());
        List<InteractionNote> notes = List.of(
                InteractionNote.fromStorage("2026-10-09T14:30:00", "Called debtor, promised payment."),
                duplicateNote, duplicateNote);
        AddressBook original = new AddressBook();
        original.addPerson(new PersonBuilder().withName("Alice").withPhone("91234567")
                .withEmail("alice@example.com").withInteractionNotes(notes).build());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

        storage.saveAddressBook(original, filePath);
        Person restoredDebtor = storage.readAddressBook(filePath).get().getPersonList().getFirst();

        assertEquals(notes, restoredDebtor.getInteractionNotes());
    }

    @Test
    public void readAndSaveAddressBook_recoveryStage_roundTrips() throws Exception {
        Path filePath = testFolder.resolve("RecoveryStageAddressBook.json");
        AddressBook original = new AddressBook();
        Person debtor = new PersonBuilder().withName("Alice").withPhone("91234567")
                .withEmail("alice@example.com").build().withRecoveryStage(RecoveryStage.NEGOTIATING);
        original.addPerson(debtor);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

        storage.saveAddressBook(original, filePath);
        Person restoredDebtor = storage.readAddressBook(filePath).orElseThrow().getPersonList().getFirst();

        assertEquals(RecoveryStage.NEGOTIATING, restoredDebtor.getRecoveryStage());
    }

    @Test
    public void saveAddressBook_nullAddressBook_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(null, "SomeFile.json"));
    }

    /**
     * Saves {@code addressBook} at the specified {@code filePath}.
     */
    private void saveAddressBook(ReadOnlyAddressBook addressBook, String filePath) {
        try {
            new JsonAddressBookStorage(Paths.get(filePath))
                    .saveAddressBook(addressBook, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(new AddressBook(), null));
    }
}
