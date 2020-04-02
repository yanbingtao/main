package csdev.couponstash.logic.commands;

import static java.util.Objects.requireNonNull;

import csdev.couponstash.logic.parser.CliSyntax;
import csdev.couponstash.model.Model;


/**
 * Terminates the program.
 */
public class GoToCommand extends Command {

    public static final String COMMAND_WORD = "goto";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Shows the specified Month Year on the Calendar. \n"
            + "Parameters: "
            + CliSyntax.PREFIX_MONTH_YEAR + "Month Year\n"
            + "Example: " + COMMAND_WORD + CliSyntax.PREFIX_MONTH_YEAR + " 12-2020";

    public static final String MESSAGE_SUCCESS = "Showing Calendar on %s";

    private String value;

    public GoToCommand(String yearMonth) {
        this.value = yearMonth;
    }

    @Override
    public CommandResult execute(Model model, String commandText) {
        requireNonNull(model);
        model.updateMonthView(value);
        return new CommandResult(String.format(MESSAGE_SUCCESS, value));
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof GoToCommand // instanceof handles nulls
                && value.equals(((GoToCommand) other).value)); // state check
    }
}
