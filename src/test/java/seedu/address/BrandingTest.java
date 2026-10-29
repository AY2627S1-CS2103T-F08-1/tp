package seedu.address;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

import seedu.address.logic.commands.ExitCommand;
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
            Map.entry("edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [o/AMOUNT] [t/TAG]...",
                    List.of("edit 1 n/Alice p/92345678 e/new@example.com a/New Street o/20 t/priority",
                            "edit 2 o/S$500.00")),
            Map.entry("delete INDEX", List.of("delete 1", "delete 2")),
            Map.entry("clear", List.of("clear")),
            Map.entry("undo", List.of("undo")),
            Map.entry("redo", List.of("redo")),
            Map.entry("help", List.of("help")),
            Map.entry("exit", List.of("exit")));

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
                .filter(line -> parserCommandWords.contains(line.split(" ", 2)[0]))
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
                assertEquals(syntax.split(" ", 2)[0], validExample.split(" ", 2)[0]);
                assertDoesNotThrow(() -> parser.parseCommand(validExample));
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
            assertDoesNotThrow(() -> parser.parseCommand(example));
        });
    }
}
