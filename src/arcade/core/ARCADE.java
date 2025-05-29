package arcade.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.Scanner;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.ConsoleHandler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import org.xml.sax.SAXException;
import arcade.core.sim.Series;
import arcade.core.sim.input.InputBuilder;
import arcade.core.sim.input.InputLoader;
import arcade.core.sim.input.InputParser;
import arcade.core.sim.output.OutputLoader;
import arcade.core.sim.output.OutputSaver;
import arcade.core.util.Box;
import arcade.core.util.MiniBox;
import arcade.patch.PatchARCADE;
import arcade.potts.PottsARCADE;

/**
 * Entry point class for ARCADE simulations.
 *
 * <p>
 * The class loads two XML files {@code command.xml} and {@code parameter.xml}
 * that specify the
 * command line parser options and the default parameter values, respectively.
 * The setup XML file is
 * then parsed to produce an array of {@link Series} objects, each of which
 * defines replicates
 * (differing only in random seed) of {@link arcade.core.sim.Simulation}
 * instances to run.
 *
 * <p>
 * If the visualization flag is used, only the first valid {@link Series} in the
 * array is run.
 * Otherwise, all valid {@code Series} are run.
 *
 * <p>
 * An implementing package {@code <implementation>} extends this class to define
 * implementation
 * specific:
 *
 * <ul>
 * <li>{@code command.<implementation>.xml} with custom command line parameters
 * <li>{@code parameter.<implementation>.xml} with new default parameter values
 * <li>{@link InputBuilder} for building implementation series from the setup
 * XML
 * <li>{@link OutputLoader} for loading classes
 * <li>{@link OutputSaver} for saving classes
 * </ul>
 */
public abstract class ARCADE {
    /** Logger for {@code ARCADE}. */
    protected static Logger logger;

    /** Version number. */
    public static final String VERSION = loadVersion();

    /** Command line settings for ARCADE simulation. */
    protected MiniBox settings = new MiniBox();

    /**
     * Gets the resource relative to the location of the class.
     *
     * @param s the resource name
     * @return the resource location
     */
    protected abstract String getResource(String s);

    /**
     * Gets an {@link InputBuilder} instance.
     *
     * @return an {@link InputBuilder} instance
     */
    protected abstract InputBuilder getBuilder();

    /**
     * Gets an {@link OutputLoader} instance for the series.
     *
     * @param series the {@link Series} instance
     * @return an {@link OutputLoader} instance
     */
    protected abstract OutputLoader getLoader(Series series);

    /**
     * Gets an {@link OutputSaver} instance for the series.
     *
     * @param series the {@link Series} instance
     * @return an {@link OutputSaver} instance
     */
    protected abstract OutputSaver getSaver(Series series);

    /**
     * Main function for running ARCADE simulations.
     *
     * @param args list of command line arguments
     */
    public static void main(String[] args) throws Exception {
        updateLogger();
        logger = Logger.getLogger(ARCADE.class.getName());

        // Check that arguments includes at least one entry.
        if (args.length == 0) {
            logger.warning("ARCADE simulation type must be specified");
            throw new InvalidParameterException();
        }

        // Display current model version and exit.
        if (args[0].equals("--version")) {
            System.out.println(loadVersion());
            System.exit(0);
        }

        // Display help message if any help flags exist and exit.
        if (Arrays.stream(args).anyMatch(s -> s.equals("-h") || s.equals("--help"))) {
            System.out.println(renderHelp());
            System.exit(0);
        }

        // Extract ARCADE type.
        ARCADE arcade;

        switch (args[0]) {
            case "patch":
                logger.info("running ARCADE [ patch | " + VERSION + " ] simulations");
                arcade = new PatchARCADE();
                break;
            case "potts":
                logger.info("running ARCADE [ potts | " + VERSION + " ] simulations");
                arcade = new PottsARCADE();
                break;
            default:
                logger.warning(
                        "ARCADE implementation [ "
                                + args[0]
                                + " ] does not exist. Valid implementations: patch | potts");
                throw new InvalidParameterException();
        }

        // Load command and parameter XML files.
        Box commands = arcade.loadCommands(args[0]);
        Box parameters = arcade.loadParameters(args[0]);

        // Parse arguments from command line.
        arcade.settings = arcade.parseArguments(args, commands);

        // Build series
        ArrayList<Series> series = arcade.buildSeries(parameters);

        // Run series.
        arcade.runSeries(series);
    }

    /**
     * Gets full version number.
     *
     * <p>
     * If running with a jar, the version is pulled from the jar manifest.
     * Otherwise, the version
     * is extracted using git.
     *
     * @return the version number
     */
    static String loadVersion() {
        String className = ARCADE.class.getSimpleName() + ".class";
        String classPath = Objects.requireNonNull(ARCADE.class.getResource(className)).toString();

        try {
            if (classPath.startsWith("jar")) {
                String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1)
                        + "/META-INF/MANIFEST.MF";
                Manifest manifest = new Manifest(new URL(manifestPath).openStream());
                Attributes attr = manifest.getMainAttributes();
                return attr.getValue("Implementation-Version");
            } else {
                Runtime runtime = Runtime.getRuntime();

                InputStream inputStream = runtime.exec("git describe --tags").getInputStream();
                Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
                String describe = scanner.hasNext() ? scanner.next() : "";

                inputStream = runtime.exec("git status --porcelain").getInputStream();
                scanner = new Scanner(inputStream).useDelimiter("\\A");
                String status = scanner.hasNext() ? scanner.next() : "";

                return describe.trim().substring(1) + (status.isEmpty() ? "" : ".dirty");
            }
        } catch (Exception e) {
            return "<undefined>";
        }
    }

    /**
     * Render help message.
     *
     * @return the help message
     */
    static String renderHelp() throws IOException, SAXException {
        InputLoader loader = new InputLoader();
        Box commands = loader.load(ARCADE.class.getResource("command.xml").toString());
        InputParser parser = new InputParser(commands);
        return parser.help("java -jar arcade-X.X.X.jar");
    }

    /**
     * Loads command line parser from {@code command.xml} files.
     *
     * @param implementation the implementation name
     * @return a container of command line settings
     */
    Box loadCommands(String implementation) throws IOException, SAXException {
        InputLoader loader = new InputLoader();

        logger.info("loading framework command line parser from [ command.xml ]");
        Box commands = loader.load(ARCADE.class.getResource("command.xml").toString());

        logger.info(
                "loading implementation [ "
                        + this.getClass().getSimpleName()
                        + " ]"
                        + " command line parser from [ command."
                        + implementation
                        + ".xml ]");
        loader.load(this.getResource("command." + implementation + ".xml"), commands);

        return commands;
    }

    /**
     * Loads default parameter from {@code parameter.xml} files.
     *
     * @param implementation the implementation name
     * @return a container of default parameter values
     */
    Box loadParameters(String implementation) throws IOException, SAXException {
        InputLoader loader = new InputLoader();

        logger.info("loading framework default parameters from [ parameter.xml ]");
        Box parameters = loader.load(ARCADE.class.getResource("parameter.xml").toString());

        logger.info(
                "loading implementation [ "
                        + this.getClass().getSimpleName()
                        + " ]"
                        + " default parameters from [ parameter."
                        + implementation
                        + ".xml ]");
        loader.load(this.getResource("parameter." + implementation + ".xml"), parameters);

        return parameters;
    }

    /**
     * Parses arguments using command line parser.
     *
     * @param args     the list of arguments
     * @param commands the command line parser settings
     * @return the container of parsed arguments
     */
    MiniBox parseArguments(String[] args, Box commands) {
        // Parse command line arguments.
        logger.info("parsing command line arguments");
        InputParser parser = new InputParser(commands);
        return parser.parse(args);
    }

    /**
     * Builds series based on setup file.
     *
     * @param parameters a container of default parameter values
     * @return a list of {@link Series} instances
     */
    ArrayList<Series> buildSeries(Box parameters) throws IOException, SAXException {
        String setupFile = settings.get("SETUP_FILE");
        String snapshotPath = settings.get("SNAPSHOT_PATH");
        boolean isVis = settings.contains("VIS");

        InputBuilder builder = this.getBuilder();
        builder.path = snapshotPath.endsWith("/") ? snapshotPath : (snapshotPath + "/");
        builder.parameters = parameters;
        builder.isVis = isVis;

        return builder.build(setupFile);
    }

    /**
     * Runs simulations for each {@link Series}.
     *
     * <p>
     * If the {@code --vis} flag is set, then only the first valid series is run.
     * Otherwise, all
     * valid series in the list are run.
     *
     * @param series the list of {@link Series} instances
     */
    void runSeries(ArrayList<Series> series) throws Exception {
        boolean isVis = settings.contains("VIS");
        String loadPath = settings.get("LOAD_PATH");
        boolean loadCells = settings.contains("LOAD_CELLS");
        boolean loadLocations = settings.contains("LOAD_LOCATIONS");

        // Iterate through each series and run.
        for (Series s : series) {
            // Create saver and save series JSON (for non-vis only)
            if (!isVis) {
                s.saver = this.getSaver(s);
                s.saver.saveSeries();
            }

            // Create loader if flagged.
            if (loadCells || loadLocations) {
                s.loader = this.getLoader(s);
                s.loader.prefix = loadPath;
                s.loader.loadCells = loadCells;
                s.loader.loadLocations = loadLocations;
            }

            // Skip simulations if there is an error in the series.
            if (s.isSkipped) {
                continue;
            }

            // Run with visualization if requested, otherwise run command line.
            if (isVis) {
                logger.info("running simulation with visualization");
                s.runVis();
                break;
            } else {
                logger.info("running simulation series [ " + s.getName() + " ]");
                s.runSims();
            }
        }
    }

    /** Updates the logger handler with custom formatting. */
    public static void updateLogger() {
        // Setup logger.
        Logger classLogger = Logger.getLogger("arcade");
        classLogger.setUseParentHandlers(false);

        // Change logger display format.
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(
                new SimpleFormatter() {
                    private static final String FORMAT = "%1$tF %1$tT %2$-7s %3$-35s : %4$s %n";

                    @Override
                    public synchronized String format(LogRecord lr) {
                        return String.format(
                                FORMAT,
                                new Date(lr.getMillis()),
                                lr.getLevel().getLocalizedName(),
                                lr.getSourceClassName(),
                                lr.getMessage());
                    }
                });

        classLogger.addHandler(handler);
    }
}
