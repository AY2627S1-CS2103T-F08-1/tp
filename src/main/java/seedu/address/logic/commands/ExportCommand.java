package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/** Exports a debtor profile to a new UTF-8 text file. */
public class ExportCommand extends Command {
    public static final String COMMAND_WORD = "export";
    public static final String MESSAGE_USAGE = "export --id DEBTOR_ID";
    private final int debtorId;
    private final Path directory;
    private final ProfileWriter writer;

    @FunctionalInterface
    interface ProfileWriter {
        void write(Path target, String content) throws IOException;
    }

    /** Creates an export command using the application working directory. */
    public ExportCommand(int debtorId) {
        this(debtorId, Path.of("exports"));
    }

    /** Creates an export command targeting the supplied directory. */
    public ExportCommand(int debtorId, Path directory) {
        this(debtorId, directory, (target, content) -> Files.writeString(target, content,
                StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE));
    }

    ExportCommand(int debtorId, Path directory, ProfileWriter writer) {
        this.debtorId = debtorId;
        this.directory = requireNonNull(directory);
        this.writer = requireNonNull(writer);
    }

    @Override
    public boolean shouldSaveAddressBook() {
        return false;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Person person = model.getAddressBook().getPersonList().stream()
                .filter(debtor -> debtor.getDebtorId() == debtorId).findFirst()
                .orElseThrow(() -> new CommandException(String.format(ShowCommand.MESSAGE_DEBTOR_NOT_FOUND, debtorId)));
        Path target = directory.resolve("debtor-" + debtorId + ".txt");
        try {
            Files.createDirectories(directory);
            if (Files.isSymbolicLink(directory)) {
                throw new IOException("Export directory must not be a symbolic link.");
            }
        } catch (IOException exception) {
            throw new CommandException("Could not create export directory: " + exception.getMessage(), exception);
        }
        try {
            writer.write(target, Messages.formatProfile(person) + System.lineSeparator());
        } catch (FileAlreadyExistsException exception) {
            throw new CommandException("Export already exists: " + target + ". Move or rename it first.", exception);
        } catch (IOException exception) {
            throw new CommandException("Could not export profile: " + exception.getMessage(), exception);
        }
        return new CommandResult("Exported debtor ID " + debtorId + " to " + target.toAbsolutePath().normalize());
    }
}
