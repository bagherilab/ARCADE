package arcade.core;

import java.util.logging.*;
import java.util.ArrayList;
import java.util.Date;
import arcade.core.sim.Series;
import arcade.core.sim.input.*;
import arcade.core.sim.output.OutputSaver;
import arcade.core.sim.output.OutputLoader;
import arcade.core.util.*;

/**
 * Main class for running simulations.
 * <p>
 * The class loads two XML files {@code command.xml} and {@code parameter.xml}
 * that specify the command line parser options and the default parameter
 * values, respectively.
 * The setup XML file is then parsed to produce an array of {@link arcade.core.sim.Series}
 * objects, each of which defines replicates (differing only in random seed) of
 * {@link arcade.core.sim.Simulation} instances to run.
 * <p>
 * If the VIS flag is used, only the first {@link arcade.core.sim.Series} in the array
 * is run.
 * Otherwise, all valid {@code Series} are run.
 */

public final class Main {
	/** Logger for {@code Main} */
	private static Logger LOGGER;
	
	public static void main(String[] args) throws Exception {
		updateLogger();
		
		// Load XML files specifying command line parser and default parameters.
		InputLoader loader = new InputLoader();
		LOGGER.info("loading command line parser from [ command.xml ]");
		Box commands = loader.load(Main.class.getResource("command.xml").toString());
		LOGGER.info("loading default parameters from [ parameter.xml ]");
		Box parameters = loader.load(Main.class.getResource("parameter.xml").toString());
		
		// Parse command line arguments.
		InputParser parser = new InputParser(commands);
		MiniBox settings = parser.parse(args);
	}
	
	static void updateLogger() {
		// Setup logger.
		Logger logger = Logger.getLogger("arcade");
		logger.setUseParentHandlers(false);
		
		// Change logger display format.
		ConsoleHandler handler = new ConsoleHandler();
		handler.setFormatter(new SimpleFormatter() {
			private static final String FORMAT = "%1$tF %1$tT %2$-7s %3$-30s : %4$s %n";
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
		LOGGER = Logger.getLogger(Main.class.getName());
	}
}