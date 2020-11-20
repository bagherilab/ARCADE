package arcade.potts;

import java.util.logging.*;
import java.util.ArrayList;
import arcade.core.sim.Series;
import arcade.core.sim.input.*;
import arcade.core.util.Box;
import arcade.core.util.MiniBox;
import arcade.core.Main;
import arcade.potts.sim.input.PottsInputBuilder;
import arcade.potts.sim.output.PottsOutputLoader;
import arcade.potts.sim.output.PottsOutputSaver;

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

public final class PottsMain {
	/** Logger for {@code Main} */
	private static Logger LOGGER;
	
	public static void main(String[] args) throws Exception {
		LOGGER = Main.updateLogger();
		
		// Load XML files specifying command line parser and default parameters.
		InputLoader loader = new InputLoader();
		LOGGER.info("loading command line parser from [ command.xml ]");
		Box commands = loader.load(PottsMain.class.getResource("command.xml").toString());
		LOGGER.info("loading default parameters from [ parameter.xml ]");
		Box parameters = loader.load(PottsMain.class.getResource("parameter.xml").toString());
		
		// Parse command line arguments.
		InputParser parser = new InputParser(commands);
		MiniBox settings = parser.parse(args);
		
		// Extract command line arguments.
		boolean isVis = settings.contains("VIS");
		String xml = settings.get("XML");
		String loadPath = settings.get("LOADPATH");
		boolean loadCells = settings.contains("LOADCELLS");
		boolean loadLocations = settings.contains("LOADLOCATIONS");
		
		// Build series.
		InputBuilder builder = new PottsInputBuilder();
		ArrayList<Series> series = builder.build(xml, parameters, isVis);
		
		// Run series.
		for (Series s : series) {
			// Create saver and save series JSON (for non-vis only)
			if (!isVis) {
				s.saver = new PottsOutputSaver(s);
				s.saver.save();
			}
			
			// Create loader if flagged.
			if (loadCells || loadLocations) {
				s.loader = new PottsOutputLoader(s, loadPath, loadCells, loadLocations);
			}
			
			// Skip simulations if there is an error in the series.
			if (s.isSkipped) { continue; }
			
			// Run with visualization if requested, otherwise run command line.
			if (isVis) {
				LOGGER.info("running simulation with visualization\n\n" + s.toString());
				s.runVis();
				break;
			} else {
				LOGGER.info("running simulation series [ " + s.getName() + " ]\n\n" + s.toString());
				s.runSims();
			}
		}
	}
}