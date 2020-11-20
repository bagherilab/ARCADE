package arcade.core.sim;

import java.lang.reflect.Constructor;
import java.text.DecimalFormat;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import sim.display.GUIState;
import sim.engine.SimState;
import arcade.core.sim.output.*;
import arcade.core.util.*;

public abstract class Series {
	/** Logger for {@code Series} */
	private final static Logger LOGGER = Logger.getLogger(Series.class.getName());
	
	/** Regular expression for numbers */
	private static final String NUMBER_REGEX = "^(\\d+)|(\\d+E\\d+)$";
	
	/** Regular expression for fractions */
	private static final String FRACTION_REGEX = "^(([0]*(\\.\\d*|))|(1[\\.0]*))$";
	
	/** Offset of random seed to avoid using seed of 0 */
	public static final int SEED_OFFSET = 1000;
	
	/** Separator character for targets */
	public static final String TARGET_SEPARATOR = ":";
	
	/** Format for console output of simulation time */
	private final static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.0000");
	
	/** {@code true} if the {@code Series} is not valid, {@code false} otherwise */
	public boolean isSkipped;
	
	/** {@code true} if {@code Series} is run with visualization, {@code false} otherwise */
	public boolean isVis;
	
	/** Output saver for the simulation */
	public OutputSaver saver;
	
	/** Output loader for the simulation */
	public OutputLoader loader;
	
	/** Name of the series */
	private final String name;
	
	/** Path and prefix for the series */ 
	private final String prefix;
	
	/** Spatial conversion factor (um/voxel) */
	public final double DS;
	
	/** Temporal conversion factor (hrs/tick) */
	public final double DT;
	
	/** Constructor for the simulation */
	Constructor<?> simCons;
	
	/** Constructor for the visualization */
	Constructor<?> visCons;
	
	/** Random seed of the first simulation in the series */
	private final int startSeed;
	
	/** Random seed of the last simulation in the series */
	private final int endSeed;
	
	/** Simulation length in ticks */
	private final int ticks;
	
	/** Snapshot interval in ticks */
	private final int interval;
	
	/** Length of the simulation */
	public final int _length;
	
	/** Width of the simulation */
	public final int _width;
	
	/** Height of the simulation */
	public final int _height;
	
	/** Map of population settings */
	public HashMap<String, MiniBox> _populations;
	
	/**
	 * Creates a {@code Series} object given setup information parsed from XML.
	 * 
	 * @param setupDicts  the map of attribute to value for single instance tags
	 * @param setupLists  the map of attribute to value for multiple instance tags
	 * @param parameters  the default parameter values loaded from {@code parameter.xml}
	 * @param isVis  {@code true} if run with visualization, {@code false} otherwise
	 */
	public Series(HashMap<String, MiniBox> setupDicts,
				  HashMap<String, ArrayList<Box>> setupLists,
				  Box parameters, boolean isVis) {
		MiniBox set = setupDicts.get("set");
		MiniBox series = setupDicts.get("series");
		MiniBox defaults = parameters.getIdValForTag("DEFAULT");
		
		this.isVis = isVis;
		
		// Set name and prefix.
		this.name = series.get("name");
		this.prefix = set.get("path") + (set.contains("prefix") ? set.get("prefix") : "") + name;
		
		// Set random seeds.
		this.startSeed = (series.contains("start") ? series.getInt("start") : defaults.getInt("START_SEED"));
		this.endSeed = (series.contains("end") ? series.getInt("end") : defaults.getInt("END_SEED"));
		
		// Set number of ticks and interval
		this.ticks = (series.contains("ticks") ? series.getInt("ticks") : defaults.getInt("TICKS"));
		this.interval = (series.contains("interval") ? series.getInt("interval") : defaults.getInt("INTERVAL"));
		
		// Set sizing.
		this._length = (series.contains("length") ? series.getInt("length") : defaults.getInt("LENGTH"));
		this._width = (series.contains("width") ? series.getInt("width") : defaults.getInt("WIDTH"));
		int height = (series.contains("height") ? series.getInt("height") : defaults.getInt("HEIGHT"));
		this._height = ((height & 1) == 1 ? height : height + 1); // enforce odd
		
		// Set conversion factors.
		this.DS = (series.contains("ds") ? series.getDouble("ds") : defaults.getDouble("DS"));
		this.DT = (series.contains("dt") ? series.getDouble("dt") : defaults.getDouble("DT"));
		
		// Initialize simulation series.
		initialize(setupLists, parameters);
	}
	
	/**
	 * Gets the name of the series.
	 * 
	 * @return  the name of the series
	 */
	public String getName() { return name; }
	
	/**
	 * Gets the prefix for the series, including file path.
	 * 
	 * @return  the file path and prefix for the series
	 */
	public String getPrefix() { return prefix; }
	
	/**
	 * Gets the start random seed.
	 * 
	 * @return  the random seed
	 */
	public int getStartSeed() { return startSeed; }
	
	/**
	 * Gets the end random seed.
	 * 
	 * @return  the random seed
	 */
	public int getEndSeed() { return endSeed; }
	
	/**
	 * Gets the number of ticks per simulation
	 *
	 * @return  the ticks
	 */
	public int getTicks() { return ticks; }
	
	/**
	 * Gets the number of ticks between snapshots
	 *
	 * @return  the interval
	 */
	public int getInterval() { return interval; }
	
	/**
	 * Checks if string contains valid number greater than 0.
	 * 
	 * @param box  the box containing the fraction
	 * @param key  the number key
	 * @return  {@code true if valid}, {@code false} otherwise
	 */
	static boolean isValidNumber(Box box, String key) {
		if (box.getValue(key) == null) { return false; }
		return box.getValue(key).matches(NUMBER_REGEX);
	}
	
	/**
	 * Checks if string contains valid fraction between 0 and 1, inclusive.
	 *
	 * @param box  the box containing the fraction
	 * @param key  the fraction key
	 * @return  {@code true if valid}, {@code false} otherwise
	 */
	static boolean isValidFraction(Box box, String key) {
		if (box.getValue(key) == null) { return false; }
		return box.getValue(key).matches(FRACTION_REGEX);
	}
	
	/**
	 * Initializes series simulation, agents, and environment.
	 * 
	 * @param setupLists  the map of attribute to value for multiple instance tags
	 * @param parameters  the default parameter values loaded from {@code parameter.xml}
	 */
	abstract void initialize(HashMap<String, ArrayList<Box>> setupLists, Box parameters);
	
	/**
	 * Creates agent populations.
	 * 
	 * @param populations  the list of population setup dictionaries
	 * @param populationDefaults  the dictionary of default population parameters
	 * @param populationConversions  the dictionary of population parameter conversions
	 */
	abstract void updatePopulations(ArrayList<Box> populations, MiniBox populationDefaults, MiniBox populationConversions);
	
	/**
	 * Creates environment molecules.
	 *
	 * @param molecules  the list of molecule setup dictionaries
	 * @param moleculeDefaults  the dictionary of default molecule parameters
	 */
	abstract void updateMolecules(ArrayList<Box> molecules, MiniBox moleculeDefaults);
	
	/**
	 * Creates selected helpers.
	 * 
	 * @param helpers  the list of helper dictionaries
	 * @param helperDefaults  the dictionary of default helper parameters
	 */
	abstract void updateHelpers(ArrayList<Box> helpers, MiniBox helperDefaults);
	
	/**
	 * Creates selected components.
	 *
	 * @param components  the list of component dictionaries
	 * @param componentDefaults  the dictionary of default component parameters
	 */
	abstract void updateComponents(ArrayList<Box> components, MiniBox componentDefaults);
	
	/**
	 * Parses parameter values based on default value.
	 *
	 * @param box  the parameter map
	 * @param parameter  the parameter name
	 * @param defaultParameter  the default parameter value
	 * @param values  the map of parameter values
	 * @param scales  the map of parameter scaling
	 */
	static void parseParameter(MiniBox box, String parameter,
								String defaultParameter, MiniBox values, MiniBox scales) {
		box.put(parameter, defaultParameter);
		
		if (values.get(parameter) != null) {
			box.put(parameter, values.get(parameter));
		}
		
		if (scales.get(parameter) != null) {
			box.put(parameter, box.getDouble(parameter)*scales.getDouble(parameter));
		}
	}
	
	/**
	 * Updates conversion string into a value.
	 * <p>
	 * Conversion string is in the form of {@code D^N} where {@code D} is either
	 * {@code DS} or {@code DT} and {@code N} is an integer exponent.
	 * The {@code ^N} is not required if N = 1.
	 * 
	 * @param convert  the conversion string
	 * @param DS  the spatial conversion factor
	 * @param DT  the temporal conversion factor
	 * @return  the updated conversion factor
	 */
	static double parseConversion(String convert, double DS, double DT) {
		String[] split = convert.replace(" ","").split("\\^");
		double v = (split[0].equals("DS") ? DS : (split[0].equals("DT") ? DT : 1));
		int n = (split.length == 2 ? Integer.parseInt(split[1]) : 1);
		return Math.pow(v, n);
	}
	
	/**
	 * Uses reflections to build constructors for simulation and visualization.
	 */
	abstract void makeConstructors();
	
	/** Calls {@code runSim} for each random seed.
	 * 
	 * @throws Exception  if the simulation constructor cannot be instantiated
	 */
	public void runSims() throws Exception {
		long simStart, simEnd;
		
		// Iterate through each seed.
		for (int iSeed = startSeed; iSeed <= endSeed; iSeed++) {
			// Pre-simulation output.
			String seed = (iSeed < 10 ? "0" : "") + iSeed;
			LOGGER.info("simulation [ " + name + " | " + seed + " ] started");
			simStart = System.currentTimeMillis();
			
			// Run simulation.
			SimState state = (SimState)(simCons.newInstance(iSeed + SEED_OFFSET, this));
			runSim(state, seed);
			
			// Post-simulation output.
			simEnd = System.currentTimeMillis();
			LOGGER.info("simulation [ " + name + " | " + seed + " ] finished in " 
					+ DECIMAL_FORMAT.format((double)(simEnd - simStart)/1000/60) + " minutes\n\n");
		}
	}
	
	/**
	 * Iterates through each tick of the simulation.
	 * 
	 * @param state  the simulation state instance
	 * @param seed  the random seed
	 */
	void runSim(SimState state, String seed) {
		// Start simulation.
		state.start();
		
		// Run simulation loop.
		double tick;
		int percent = 0;
		do {
			if (!state.schedule.step(state)) { break; }
			tick = state.schedule.getTime();
			
			if (tick%(ticks/10.0) == 0) {
				if (percent > 0 && percent < 100) { LOGGER.info("simulation [ " + name + " | " + seed + " ] " + percent + " % complete"); }
				percent += 10;
			}
		} while (tick < ticks - 1);
		
		// Finish simulation.
		state.finish();
	}
	
	/**
	 * Creates controller for visualization.
	 * 
	 * @throws Exception  if the visualization constructor cannot be instantiated
	 */
	public void runVis() throws Exception {
		if (System.getProperty("java.awt.headless") != null && System.getProperty("java.awt.headless").equals("true")) { return; }
		Simulation sim = (Simulation)(simCons.newInstance(startSeed + SEED_OFFSET, this));
		((GUIState)visCons.newInstance(sim)).createController();
	}
	
	public String toString() {
		// Convert populations to string.
		StringBuilder pop = new StringBuilder();
		for (String p : _populations.keySet()) {
			pop.append(String.format("\t\tPOPULATION [ %s ]\n", p));
		}
		
		String format = "\t%10s : %s\n";
		return "\t======================================================================\n"
			+ (isVis ? "" : String.format(format, "output", prefix))
			+ String.format(format, "class", (isVis ? visCons.getName() : simCons.getName()))
			+ (isVis ? "" : String.format(format, "seeds", startSeed + " - " + endSeed))
			+ (isVis ? "" : String.format(format, "ticks", ticks))
			+ String.format(format, "size", _length + " x " + _width + " x " + _height)
			+ "\t----------------------------------------------------------------------\n"
			+ pop
			+ "\t======================================================================\n";
	}
}