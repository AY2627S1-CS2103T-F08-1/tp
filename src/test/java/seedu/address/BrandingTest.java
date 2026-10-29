package seedu.address;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.ContactCommand;
import seedu.address.logic.commands.ContactableCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.ExportCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.FollowUpCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListAmountCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.commands.PreviewCommand;
import seedu.address.logic.commands.RedoCommand;
import seedu.address.logic.commands.ShowCommand;
import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.ui.HelpWindow;
import seedu.address.ui.MainWindow;

public class BrandingTest {

    private static final Pattern LONG_OPTION = Pattern.compile("--[a-z-]+");
    private static final Pattern USER_GUIDE_SUMMARY_COMMAND =
            Pattern.compile("(?m)^\\*\\*[^*]+\\*\\* \\| `(?<command>[a-z-]+)(?:\\s|`)");
    private static final Map<String, List<String>> RELEASED_COMMANDS = Map.ofEntries(
            Map.entry("add n/NAME p/PHONE e/EMAIL a/ADDRESS [o/AMOUNT] [t/TAG]...",
                    List.of("add n/Alice p/91234567 e/alice@example.com a/Example Street o/10 t/client",
                            "add n/Olaf Go p/91234567 e/olaf@example.com a/Antarctica o/S$1250.00")),
            Map.entry("list", List.of("list")),
            Map.entry("find KEYWORD [MORE_KEYWORDS]...", List.of("find Alice Bob", "find Alice 91234567")),
            Map.entry("sort --by id|name|amount [--order asc|desc]",
                    List.of("sort --by id", "sort --by name --order asc", "sort --by amount --order desc")),
            Map.entry("show --id DEBTOR_ID", List.of("show --id 1", "show --id 3")),
            Map.entry("note --id DEBTOR_ID [--outcome OUTCOME] --text NOTE_TEXT",
                    List.of("note --id 1 --text Called debtor", "note --id 1 --outcome reached --text Paid",
                            "note --id 3 --outcome reached --text Called debtor.")),
            Map.entry("contact --id DEBTOR_ID [--method METHOD | --clear-method] "
                    + "[--hours HH:mm-HH:mm | --clear-hours]",
                    List.of("contact --id 1 --method sms --hours 09:00-17:30",
                            "contact --id 1 --clear-method --clear-hours",
                            "contact --id 3 --method sms --hours 09:00-17:30")),
            Map.entry("list-amount [min/AMOUNT] [max/AMOUNT]",
                    List.of("list-amount min/1000", "list-amount max/S$500.00",
                            "list-amount min/500 max/2000")),
            Map.entry("contactable [--at HH:mm] [--method call|sms|email|letter]",
                    List.of("contactable", "contactable --at 09:30 --method sms")),
            Map.entry("preview edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [o/AMOUNT] [t/TAG]... "
                    + "| delete INDEX",
                    List.of("preview edit 1 n/Alice", "preview delete 2")),
            Map.entry("export --id DEBTOR_ID", List.of("export --id 1", "export --id 3")),
            Map.entry("followup --id DEBTOR_ID --date YYYY-MM-DD | --clear | --done",
                    List.of("followup --id 1 --date 2026-11-03", "followup --id 1 --clear",
                            "followup --id 1 --done")),
            Map.entry("edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [o/AMOUNT] [t/TAG]...",
                    List.of("edit 1 n/Alice p/92345678 e/new@example.com a/New Street o/20 t/priority",
                            "edit 2 o/S$500.00")),
            Map.entry("delete INDEX", List.of("delete 1", "delete 2")),
            Map.entry("clear", List.of("clear")),
            Map.entry("undo", List.of("undo")),
            Map.entry("redo", List.of("redo")),
            Map.entry("help", List.of("help")),
            Map.entry("exit", List.of("exit")));
    private static final Map<String, Class<? extends Command>> COMMAND_TYPES = Map.ofEntries(
            Map.entry("add", AddCommand.class),
            Map.entry("clear", ClearCommand.class),
            Map.entry("contact", ContactCommand.class),
            Map.entry("contactable", ContactableCommand.class),
            Map.entry("export", ExportCommand.class),
            Map.entry("followup", FollowUpCommand.class),
            Map.entry("list-amount", ListAmountCommand.class),
            Map.entry("preview", PreviewCommand.class),
            Map.entry("delete", DeleteCommand.class),
            Map.entry("edit", EditCommand.class),
            Map.entry("exit", ExitCommand.class),
            Map.entry("find", FindCommand.class),
            Map.entry("help", HelpCommand.class),
            Map.entry("list", ListCommand.class),
            Map.entry("note", NoteCommand.class),
            Map.entry("redo", RedoCommand.class),
            Map.entry("show", ShowCommand.class),
            Map.entry("sort", SortCommand.class),
            Map.entry("undo", UndoCommand.class));

    @Test
    public void releaseIdentity_matchesV14Branding() {
        assertEquals("OSPS", MainApp.APP_NAME);
        assertEquals("v1.5", MainApp.VERSION);
        assertEquals("OSPS v1.5", MainWindow.APP_TITLE);
        assertEquals("Exiting OSPS as requested ...", ExitCommand.MESSAGE_EXIT_ACKNOWLEDGEMENT);
    }

    @Test
    public void helpUrl_pointsToTeamUserGuide() {
        assertEquals("https://ay2627s1-cs2103t-f08-1.github.io/tp/UserGuide.html",
                HelpWindow.USERGUIDE_URL);
    }

    @Test
    public void commandReferenceAndUserGuide_matchParserDispatchTable() throws IOException {
        Set<String> parserCommandWords = AddressBookParser.getSupportedCommandWords();
        Set<String> referenceSyntaxLines = HelpWindow.COMMAND_REFERENCE.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .filter(line -> Character.isLowerCase(line.charAt(0)))
                .collect(Collectors.toSet());
        Set<String> referenceCommandWords = referenceSyntaxLines.stream()
                .map(syntax -> syntax.split(" ", 2)[0])
                .collect(Collectors.toSet());
        String userGuide = Files.readString(Path.of("docs", "UserGuide.md"));
        Set<String> userGuideCommandWords = USER_GUIDE_SUMMARY_COMMAND.matcher(userGuide).results()
                .map(match -> match.group("command"))
                .collect(Collectors.toSet());

        assertEquals(parserCommandWords, referenceCommandWords);
        assertEquals(parserCommandWords, userGuideCommandWords);
        assertEquals(RELEASED_COMMANDS.keySet(), referenceSyntaxLines);
    }

    @Test
    public void commandReference_everyListedCommandIsAcceptedByParser() {
        AddressBookParser parser = new AddressBookParser();
        RELEASED_COMMANDS.forEach((syntax, validExamples) -> {
            assertFalse(validExamples.isEmpty());
            Set<String> documentedOptions = LONG_OPTION.matcher(syntax).results()
                    .map(match -> match.group())
                    .collect(Collectors.toSet());
            Set<String> exercisedOptions = validExamples.stream()
                    .flatMap(example -> LONG_OPTION.matcher(example).results())
                    .map(match -> match.group())
                    .collect(Collectors.toSet());
            assertTrue(exercisedOptions.containsAll(documentedOptions));
            validExamples.forEach(validExample -> {
                String commandWord = syntax.split(" ", 2)[0];
                assertEquals(commandWord, validExample.split(" ", 2)[0]);
                Command parsed = assertDoesNotThrow(() -> parser.parseCommand(validExample));
                assertInstanceOf(COMMAND_TYPES.get(commandWord), parsed);
            });
        });
        Set<String> displayedExamples = HelpWindow.COMMAND_REFERENCE.lines()
                .map(String::trim)
                .filter(line -> line.startsWith("Example: "))
                .map(line -> line.substring("Example: ".length()))
                .collect(Collectors.toSet());

        displayedExamples.forEach(example -> {
            String commandWord = example.split(" ", 2)[0];
            List<String> examplesForSyntax = RELEASED_COMMANDS.entrySet().stream()
                    .filter(entry -> entry.getKey().split(" ", 2)[0].equals(commandWord))
                    .findFirst()
                    .orElseThrow()
                    .getValue();
            assertTrue(examplesForSyntax.contains(example));
            Command parsed = assertDoesNotThrow(() -> parser.parseCommand(example));
            assertInstanceOf(COMMAND_TYPES.get(commandWord), parsed);
        });
    }
}
