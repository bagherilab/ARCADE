package arcade.core;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Date;
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
import arcade.potts.PottsARCADE;

/**
 * Entry point class for ARCADE simulations.
 * <p>
 * The class loads two XML files {@code command.xml} and {@code parameter.xml}
 * that specify the command line parser options and the default parameter
 * values, respectively.
 * The setup XML file is then parsed to produce an array of {@link Series}
 * objects, each of which defines replicates (differing only in random seed) of
 * {@link arcade.core.sim.Simulation} instances to run.
 * <p>
 * If the visualization flag is used, only the first valid {@link Series} in
 * the array is run.
 * Otherwise, all valid {@code Series} are run.
 * <p>
 * An implementing package {@code <imp>} extends this class to define
 * implementation specific:
 * <ul>
 *     <li>{@code command.<imp>.xml} with custom command line parameters</li>
 *     <li>{@code parameter.<imp>.xml} with new default parameter values</li>
 *     <li>{@link InputBuilder}</li> for building implementation series from
 *     the setup XML</li>
 *     <li>{@link OutputLoader}</li> for loading classes</li>
 *     <li>{@link OutputSaver}</li> for saving classes</li>
 * </ul>
 */

public abstract class ARCADE {
    /** Logger for {@code ARCADE}. */
    protected static Logger logger;
    
    protected abstract String getResource(String s);
    
    protected abstract InputBuilder getBuilder();
    
    protected abstract OutputLoader getLoader(Series series);
    
    protected abstract OutputSaver getSaver(Series series);
    
    public static void main(String[] args) throws Exception {
        updateLogger();
        
        // Check that arguments includes at least one entry.
        if (args.length == 0) {
            logger.warning("ARCADE simulation type must be specified");
            throw new InvalidParameterException();
        }
        
        // Extract ARCADE type.
        ARCADE arcade;
        
        switch (args[0]) {
            case "potts":
                logger.info("running ARCADE [ potts ] simulations");
                arcade = new PottsARCADE();
                break;
            default:
                logger.warning("ARCADE [ " + args[0] + " ] does not exist");
                throw new InvalidParameterException();
        }
        
        // Load command and parameter XML files.
        Box commands = arcade.loadCommands();
        Box parameters = arcade.loadParameters();
        
        // Parse arguments from command line.
        MiniBox settings = arcade.parseArguments(args, commands);
        
        // Build series
        ArrayList<Series> series = arcade.buildSeries(parameters, settings);
        
        // Run series.
        arcade.runSeries(series, settings);
    }
    
    Box loadCommands() throws IOException, SAXException {
        InputLoader loader = new InputLoader();
    
        logger.info("loading framework command line parser from [ command.xml ]");
        Box commands = loader.load(ARCADE.class.getResource("command.xml").toString());
    
        logger.info("loading implementation [ " + this.getClass().getSimpleName()
                + " ] command line parser from [ command.xml ]");
        loader.load(this.getResource("command.xml"), commands);
        
        return commands;
    }
    
    Box loadParameters() throws IOException, SAXException {
        InputLoader loader = new InputLoader();
    
        logger.info("loading framework default parameters from [ parameter.xml ]");
        Box parameters = loader.load(ARCADE.class.getResource("parameter.xml").toString());
    
        logger.info("loading implementation [ " + this.getClass().getSimpleName()
                + " ] default parameters from [ parameter.xml ]");
        loader.load(this.getResource("parameter.xml"), parameters);
        
        return parameters;
    }
    
    MiniBox parseArguments(String[] args, Box commands) {
        // Parse command line arguments.
        logger.info("parsing command line arguments");
        InputParser parser = new InputParser(commands);
        return parser.parse(args);
    }
    
    ArrayList<Series> buildSeries(Box parameters, MiniBox settings)
            throws IOException, SAXException {
        String xml = settings.get("XML");
        boolean isVis = settings.contains("VIS");
        
        InputBuilder builder = this.getBuilder();
        builder.parameters = parameters;
        builder.isVis = isVis;
        return builder.build(xml);
    }
    
    void runSeries(ArrayList<Series> series, MiniBox settings) throws Exception {
        boolean isVis = settings.contains("VIS");
        String loadPath = settings.get("LOADPATH");
        boolean loadCells = settings.contains("LOADCELLS");
        boolean loadLocations = settings.contains("LOADLOCATIONS");
        
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
            if (s.isSkipped) { continue; }
            
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
    
    public static void updateLogger() {
        // Setup logger.
        logger = Logger.getLogger(ARCADE.class.getName());
        logger.setUseParentHandlers(false);
        
        // Change logger display format.
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter() {
            private static final String FORMAT = "%1$tF %1$tT %2$-7s %3$-35s : %4$s %n";
            public synchronized String format(LogRecord lr) {
                return String.format(FORMAT,
                        new Date(lr.getMillis()),
                        lr.getLevel().getLocalizedName(),
                        lr.getSourceClassName(),
                        lr.getMessage()
                );
            }
        });
        
        logger.addHandler(handler);
    }
}
