package arcade.core.sim.input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import arcade.core.util.Box;
import arcade.core.util.MiniBox;

/**
 * Command line parser built from XML defining the possible commands.
 *
 * <p>{@code InputParser} is built using the {@link arcade.core.util.Box} created by {@link
 * arcade.core.sim.input.InputLoader} when parsing {@code command.xml}. There are three command
 * argument types:
 *
 * <ul>
 *   <li><em>position</em> arguments are ordered by their location in the command and all are
 *       required
 *   <li><em>option</em> arguments are indicated by a short ({@code -}) and/ or long ({@code --})
 *       flag followed by the contents
 *   <li><em>switch</em> arguments are booleans for selecting an option
 * </ul>
 *
 * <p>General structure of {@code command.xml}:
 *
 * <pre>
 *     &#60;commands&#62;
 *         &#60;position id="(unique id)" help="(help text)" /&#62;
 *         ...
 *         &#60;option id="(unique id)" long="(long flag)" short="(short flag)"
 *                     default="(default value)" help="(help text)" /&#62;
 *         ...
 *         &#60;switch id="(unique id)" long="(long flag)" short="(short flag)"
 *                     help="(help text)" /&#62;
 *         ...
 *     &#60;/commands&#62;
 * </pre>
 */
public class InputParser {
    /** Logger for {@code InputParser}. */
    private static final Logger LOGGER = Logger.getLogger(InputParser.class.getName());

    /** ID for position commands. */
    static final int POSITION = 0;

    /** ID for option commands. */
    static final int OPTION = 1;

    /** ID for switch commands. */
    static final int SWITCH = 2;

    /** Dictionary of parsed commands. */
    MiniBox parsed;

    /** List of all commands. */
    final ArrayList<Command> allCommands;

    /** Map of short flags to command. */
    final HashMap<String, Command> shortToCommand;

    /** Map of long flags to command. */
    final HashMap<String, Command> longToCommand;

    /** List of position commands. */
    final ArrayList<Command> positionCommands;

    /** Current index for position arguments. */
    int positionIndex;

    /**
     * Creates a command line {@code InputParser} object.
     *
     * @param options the map of command line options
     */
    public InputParser(Box options) {
        // Initialize maps to commands.
        allCommands = new ArrayList<>();
        shortToCommand = new HashMap<>();
        longToCommand = new HashMap<>();
        positionCommands = new ArrayList<>();

        LOGGER.config("configuring parser");

        // Create a Command object for each entry.
        for (String id : options.getKeys()) {
            // Create appropriate command type object.
            Command cmd;
            switch (options.getTag(id)) {
                case "POSITION":
                    cmd = new Command(id, POSITION);
                    break;
                case "OPTION":
                    cmd = new Command(id, OPTION);
                    break;
                case "SWITCH":
                    cmd = new Command(id, SWITCH);
                    break;
                default:
                    continue;
            }

            // Modify selected attributes.
            MiniBox atts = options.getAttValForId(id);
            for (String att : atts.getKeys()) {
                String val = atts.get(att);

                switch (att) {
                    case "help":
                        cmd.help = val;
                        break;
                    case "default":
                        if (cmd.type != POSITION && cmd.type != SWITCH) {
                            cmd.defaults = val;
                        }
                        break;
                    case "short":
                        if (cmd.type != POSITION) {
                            cmd.shortFlag = val;
                        }
                        break;
                    case "long":
                        if (cmd.type != POSITION) {
                            cmd.longFlag = val;
                        }
                        break;
                    default:
                        break;
                }
            }

            allCommands.add(cmd);
            if (cmd.type != POSITION) {
                if (cmd.shortFlag != null) {
                    shortToCommand.put(cmd.shortFlag, cmd);
                }
                if (cmd.longFlag != null) {
                    longToCommand.put(cmd.longFlag, cmd);
                }
            } else {
                positionCommands.add(cmd);
            }
        }

        LOGGER.config("successfully configured command line parser\n\n" + this.toString());
    }

    /**
     * Container class for arguments of a command.
     *
     * <p>A {@code Command} object is created for each command defined from parsing {@code
     * command.xml}.
     */
    static class Command {
        /** Format string. */
        String format = "\t%20s %s\n";

        /** Command type. */
        int type;

        /** Command id. */
        String id;

        /** Command default value. */
        String defaults;

        /** Command help text. */
        String help;

        /** Command short flag. */
        String shortFlag;

        /** Command long flag. */
        String longFlag;

        /**
         * Creates a {@code Command} object of a given type.
         *
         * @param id the unique id of the command
         * @param type the command type
         */
        Command(String id, int type) {
            this.id = id;
            this.type = type;
        }

        /**
         * Convert command to usage.
         *
         * <p>Usage depends on the command type.
         *
         * @return the command usage
         */
        public String toUsage() {
            if (type == POSITION) {
                return String.format(" %s", id);
            } else if (type == OPTION) {
                return String.format(" [--%s <%s>]", longFlag, id.toLowerCase());
            } else if (type == SWITCH) {
                return String.format(" [(-%s|--%s)]", shortFlag, longFlag);
            } else {
                return "";
            }
        }

        /**
         * Convert command to description.
         *
         * <p>Description depends on the command type.
         *
         * @return the command description.
         */
        public String toDescription() {
            String description = "";

            if (type == POSITION) {
                description = id;
            } else if (type == OPTION) {
                description = String.format("--%s <%s>", longFlag, id.toLowerCase());
            } else if (type == SWITCH) {
                description = String.format("-%s --%s", shortFlag, longFlag);
            }

            return String.format("  %-23s  %s.\n", description, help);
        }

        /**
         * Formats command as a string.
         *
         * @return a string representation of the command
         */
        public String toString() {
            return "\t"
                    + id.toUpperCase()
                    + "\n"
                    + (help == null ? "" : String.format(format, "[help]", help))
                    + (defaults == null ? "" : String.format(format, "[default]", defaults))
                    + (shortFlag == null ? "" : String.format(format, "[short]", shortFlag))
                    + (longFlag == null ? "" : String.format(format, "[long]", longFlag));
        }
    }

    /**
     * Render help message.
     *
     * @param program the command for running the program
     * @return the help message
     */
    public String help(String program) {
        String usage =
                allCommands.stream()
                        .filter(command -> command.type == POSITION)
                        .map(Command::toUsage)
                        .collect(Collectors.joining(""));

        String arguments =
                allCommands.stream()
                        .filter(command -> command.type == POSITION)
                        .map(Command::toDescription)
                        .collect(Collectors.joining(""));

        String options =
                allCommands.stream()
                        .filter(command -> command.type != POSITION)
                        .map(Command::toDescription)
                        .collect(Collectors.joining(""));

        return String.format("\nUsage:\n  %s%s [options]", program, usage)
                + String.format("\n  %s (-h | --help)", program)
                + String.format("\n  %s --version\n", program)
                + String.format("\nArguments:\n%s", arguments)
                + String.format("\nOptions:\n%s", options);
    }

    /**
     * Parse the given arguments into a dictionary.
     *
     * @param args the list of arguments from command line
     * @return a dictionary of parsed commands
     */
    public MiniBox parse(String[] args) {
        LOGGER.config("parsing command line arguments");
        parsed = new MiniBox();

        addDefaults();
        parseArguments(args);

        if (positionIndex != positionCommands.size()) {
            LOGGER.severe("missing position arguments");
            throw new IllegalArgumentException();
        } else {
            LOGGER.config("successfully parsed commands\n\n" + parsed.toString());
        }

        return parsed;
    }

    /**
     * Adds default values for all commands.
     *
     * <p>Switches are assumed to be {@code false} if not present.
     */
    void addDefaults() {
        for (Command cmd : allCommands) {
            if (cmd.defaults != null) {
                parsed.put(cmd.id, cmd.defaults);
            }
        }
    }

    /**
     * Parses a list of arguments.
     *
     * @param args the list of arguments
     */
    void parseArguments(String[] args) {
        positionIndex = 0;
        int currentIndex = 0;
        int argCount = args.length;

        while (currentIndex < argCount) {
            String arg = args[currentIndex];

            if (arg.startsWith("--")) {
                Command cmd = longToCommand.get(args[currentIndex].substring(2));
                currentIndex = parseFlaggedArgument(args, currentIndex, cmd);
            } else if (arg.startsWith("-")) {
                Command cmd = shortToCommand.get(args[currentIndex].substring(1));
                currentIndex = parseFlaggedArgument(args, currentIndex, cmd);
            } else {
                currentIndex = parsePositionArgument(args, currentIndex);
            }
        }
    }

    /**
     * Parses a flagged (either option or switch) argument.
     *
     * @param args the argument contents
     * @param index the argument index
     * @param cmd the command object
     * @return the next argument index
     */
    int parseFlaggedArgument(String[] args, int index, Command cmd) {
        if (cmd.type == SWITCH) {
            parsed.put(cmd.id, "");
        } else {
            parsed.put(cmd.id, args[++index]);
        }
        return ++index;
    }

    /**
     * Parses a position argument.
     *
     * @param args the argument contents
     * @param index the argument index
     * @return the next argument index
     */
    int parsePositionArgument(String[] args, int index) {
        parsed.put(positionCommands.get(positionIndex).id, args[index]);
        positionIndex++;
        return ++index;
    }

    /**
     * Formats parser as a string.
     *
     * @return a string representation of the parser
     */
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (Command cmd : allCommands) {
            s.append(cmd.toString());
        }
        return s.toString();
    }
}
