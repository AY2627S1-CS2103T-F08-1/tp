package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;

import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.RecoveryStage;

/** Shows counts and outstanding balances for the displayed or complete case workload. */
public class SummaryCommand extends Command {
    public static final String COMMAND_WORD = "summary";
    public static final String MESSAGE_USAGE = "summary: Summarises the displayed debtors. "
            + "Use --all to summarise every active debtor.\nExample: summary --all";

    private final boolean includeAll;

    public SummaryCommand(boolean includeAll) {
        this.includeAll = includeAll;
    }

    @Override
    public boolean shouldSaveAddressBook() {
        return false;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        List<Person> people = includeAll ? model.getAddressBook().getPersonList() : model.getFilteredPersonList();
        BigDecimal total = BigDecimal.ZERO;
        int zeroBalances = 0;
        EnumMap<RecoveryStage, Integer> stageCounts = new EnumMap<>(RecoveryStage.class);
        for (RecoveryStage stage : RecoveryStage.values()) {
            stageCounts.put(stage, 0);
        }
        for (Person person : people) {
            BigDecimal amount = person.getOutstandingAmount().getValue();
            total = total.add(amount);
            if (amount.signum() == 0) {
                zeroBalances++;
            }
            stageCounts.compute(person.getRecoveryStage(), (stage, count) -> count + 1);
        }
        String scope = includeAll ? "all active debtors" : "displayed debtors";
        StringBuilder message = new StringBuilder("Case workload summary (" + scope + ")\n")
                .append("Debtors: ").append(people.size()).append('\n')
                .append("Outstanding total: S$").append(formatAmount(total)).append('\n')
                .append("Zero-balance debtors: ").append(zeroBalances).append('\n')
                .append("Stages:");
        for (RecoveryStage stage : RecoveryStage.values()) {
            message.append("\n- ").append(stage).append(": ").append(stageCounts.get(stage));
        }
        return new CommandResult(message.toString());
    }

    private static String formatAmount(BigDecimal amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        return formatter.format(amount);
    }
}
