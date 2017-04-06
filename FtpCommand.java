import java.util.Arrays;
import java.util.List;

/**
 * Parsed command with list of arguments.
 * Created by Dalvir on 2017-01-24.
 */
public class FtpCommand {
    final String commandName;
    final List<String> commandValues;

    public FtpCommand(String input) {
        String[] arguments = input.split("\\s+");
        this.commandName = arguments[0].toLowerCase();
        this.commandValues = Arrays.asList(arguments).subList(1, arguments.length);
    }

    public String getCommandName() {
        return commandName;
    }

    public List<String> getCommandValues() {
        return commandValues;
    }
}
