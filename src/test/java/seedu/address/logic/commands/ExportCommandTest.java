package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.ExportCommandParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class ExportCommandTest {
    @TempDir
    public Path directory;

    @Test
    public void execute_exportsFullProfileAndNeverOverwrites() throws Exception {
        Model model = new ModelManager();
        Person debtor = model.addPerson(new PersonBuilder().withName("Alice").build());
        ExportCommand command = new ExportCommand(debtor.getDebtorId(), directory.resolve("exports"));
        assertFalse(command.shouldSaveAddressBook());
        String message = command.execute(model).getFeedbackToUser();
        Path target = directory.resolve("exports/debtor-" + debtor.getDebtorId() + ".txt");
        String text = Files.readString(target, StandardCharsets.UTF_8);
        assertTrue(message.contains(target.toString()));
        assertTrue(text.contains("Alice"));
        assertTrue(text.contains("Interaction history"));
        assertThrows(CommandException.class, () -> command.execute(model));
        assertEquals(text, Files.readString(target, StandardCharsets.UTF_8));
        assertEquals(debtor, model.getAddressBook().getPersonList().getFirst());
    }

    @Test
    public void execute_missingDebtorAndInvalidDirectory_reportFailure() throws Exception {
        Model model = new ModelManager();
        Person debtor = model.addPerson(new PersonBuilder().build());
        Path target = directory.resolve("not-a-directory");
        Files.writeString(target, "unchanged");
        assertThrows(CommandException.class, () -> new ExportCommand(999, directory).execute(model));
        assertThrows(CommandException.class, () -> new ExportCommand(debtor.getDebtorId(), target).execute(model));
        assertEquals("unchanged", Files.readString(target));
    }

    @Test
    public void execute_writeFailure_isReported() {
        Model model = new ModelManager();
        Person debtor = model.addPerson(new PersonBuilder().build());
        ExportCommand command = new ExportCommand(debtor.getDebtorId(), directory, (path, content) -> {
            throw new IOException("disk full");
        });
        CommandException error = assertThrows(CommandException.class, () -> command.execute(model));
        assertTrue(error.getMessage().contains("disk full"));
        assertTrue(Files.notExists(directory.resolve("debtor-" + debtor.getDebtorId() + ".txt")));
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    public void execute_symbolicLinkDirectory_rejected() throws Exception {
        Model model = new ModelManager();
        Person debtor = model.addPerson(new PersonBuilder().build());
        Path real = Files.createDirectory(directory.resolve("real"));
        Path link = Files.createSymbolicLink(directory.resolve("link"), real);
        CommandException error = assertThrows(CommandException.class, () ->
                new ExportCommand(debtor.getDebtorId(), link).execute(model));
        assertTrue(error.getMessage().contains("symbolic link"));
        assertTrue(Files.notExists(real.resolve("debtor-" + debtor.getDebtorId() + ".txt")));
    }

    @Test
    public void parser_rejectsInvalidOrExtraOptions() throws Exception {
        ExportCommandParser parser = new ExportCommandParser();
        parser.parse("--id 1");
        for (String input : new String[] {"", "--id", "--id 0", "--id -1", "--id 99999999999999",
            "--id 1 --id 2", "--other 1", "--id 1 extra"}) {
            assertThrows(ParseException.class, () -> parser.parse(input), input);
        }
    }
}
