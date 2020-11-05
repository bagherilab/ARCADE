package arcade.sim;

import java.lang.reflect.Constructor;
import java.text.DecimalFormat;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import sim.display.GUIState;
import sim.engine.SimState;
import arcade.sim.output.*;
import arcade.util.*;
import static arcade.util.Box.KEY_SEPARATOR;
import static arcade.util.MiniBox.TAG_SEPARATOR;

public class Series {
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
	final double DS;
	
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
	
	/** Map of potts settings */
	public MiniBox _potts;
	
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
	final void initialize(HashMap<String, ArrayList<Box>> setupLists, Box parameters) {
		// Initialize potts.
		MiniBox pottsDefaults = parameters.getIdValForTag("POTTS");
		ArrayList<Box> potts = setupLists.get("potts");
		updatePotts(potts, pottsDefaults);
		
		// Initialize populations.
		MiniBox populationDefaults = parameters.getIdValForTag("POPULATION");
		ArrayList<Box> populations = setupLists.get("populations");
		updatePopulations(populations, populationDefaults);
		
		// Add helpers.
		MiniBox helperDefaults = parameters.getIdValForTag("HELPER");
		ArrayList<Box> helpers = setupLists.get("helpers");
		updateHelpers(helpers, helperDefaults);
		
		// Add components.
		MiniBox componentDefaults = parameters.getIdValForTag("COMPONENT");
		ArrayList<Box> components = setupLists.get("components");
		updateComponents(components, componentDefaults);
		
		// Add profilers.
		MiniBox profilerDefaults = parameters.getIdValForTag("PROFILER");
		ArrayList<Box> profilers = setupLists.get("profilers");
		updateProfilers(profilers, profilerDefaults);
		
		// Add checkpoints.
		MiniBox checkpointDefaults = parameters.getIdValForTag("CHECKPOINT");
		ArrayList<Box> checkpoints = setupLists.get("checkpoints");
		updateCheckpoints(checkpoints, checkpointDefaults);
		
		// Create constructors for simulation and visualization.
		makeConstructors();
	}
	
	/**
	 * Calculates model sizing parameters.
	 * 
	 * @param potts  the potts setup dictionary
	 * @param pottsDefaults  the dictionary of default potts parameters
	 */
	void updatePotts(ArrayList<Box> potts, MiniBox pottsDefaults) {
		_potts = new MiniBox();
		
		Box box = new Box();
		if (potts != null && potts.size() == 1 && potts.get(0) != null) { box = potts.get(0); }
		
		// Get default parameters and any parameter tags.
		Box parameters = box.filterBoxByTag("PARAMETER");
		MiniBox parameterValues = parameters.getIdValForTagAtt("PARAMETER", "value");
		MiniBox parameterScales = parameters.getIdValForTagAtt("PARAMETER", "scale");
		
		// Add in parameters. Start with value (if given) or default (if not
		// given). Then apply any scaling.
		for (String parameter : pottsDefaults.getKeys()) {
			updateParameter(_potts, parameter,
					pottsDefaults.get(parameter), parameterValues, parameterScales);
		}
	}
	
	/**
	 * Creates agent populations.
	 * 
	 * @param populations  the list of population setup dictionaries
	 * @param populationDefaults  the dictionary of default population parameters
	 */
	void updatePopulations(ArrayList<Box> populations, MiniBox populationDefaults) {
		_populations = new HashMap<>();
		if (populations == null) { return; }
		
		// Get list of all populations (plus * indicating media).
		String[] pops = new String[populations.size() + 1];
		pops[0] = "*";
		for (int i = 0; i < populations.size(); i++) { pops[i + 1] = populations.get(i).getValue("id"); }
		
		int iPop = 1;
		
		// Iterate through each setup dictionary to build population settings.
		for (Box p : populations) {
			String id = p.getValue("id");
			
			// Create new population and update code.
			MiniBox population = new MiniBox();
			population.put("CODE", iPop++);
			_populations.put(id, population);
			
			// Add population init if given. If not given or invalid, set
			// to zero.
			int init = (isValidNumber(p, "init") ? (int)Double.parseDouble(p.getValue("init")) : 0);
			population.put("INIT", init);
			
			// Get default parameters and any parameter tags.
			Box parameters = p.filterBoxByTag("PARAMETER");
			MiniBox parameterValues = parameters.getIdValForTagAtt("PARAMETER", "value");
			MiniBox parameterScales = parameters.getIdValForTagAtt("PARAMETER", "scale");
			
			// Add in parameters. Start with value (if given) or default (if not
			// given). Then apply any scaling.
			for (String parameter : populationDefaults.getKeys()) {
				updateParameter(population, parameter,
						populationDefaults.get(parameter), parameterValues, parameterScales);
			}
			
			// Add adhesion values for each population and media (*). Values
			// are set as equal to the default (or adjusted) value, before
			// any specific values or scaling is applied.
			for (String target : pops) {
				updateParameter(population, "ADHESION" + TARGET_SEPARATOR + target,
						population.get("ADHESION"), parameterValues, parameterScales);
			}
			
			// Get tags.
			Box tags = p.filterBoxByTag("TAG");
			MiniBox tagFractions = tags.getIdValForTagAtt("TAG", "fraction");
			
			// Add tag fractions and parameters.
			for (String tag : tags.getKeys()) {
				double tagFraction = (isValidFraction(tags, tag + KEY_SEPARATOR + "fraction") ? tagFractions.getDouble(tag) : 0);
				population.put("TAG" + TAG_SEPARATOR + tag, tagFraction);
				
				// Add tag parameters.
				for (String parameter : populationDefaults.getKeys()) {
					String tagParameter = tag + TAG_SEPARATOR + parameter;
					updateParameter(population, tagParameter,
							population.get(parameter), parameterValues, parameterScales);
				}
				
				// Scale tag volume and surface parameter units.
				population.put(tag + TAG_SEPARATOR + "CRITICAL_VOLUME",
						Math.round(population.getDouble(tag + TAG_SEPARATOR + "CRITICAL_VOLUME")/(DS*DS*DS)));
				population.put(tag + TAG_SEPARATOR + "CRITICAL_SURFACE",
						Math.round(population.getDouble(tag + TAG_SEPARATOR + "CRITICAL_SURFACE")/(DS*DS)));
				
				// Add tag adhesion values.
				for (String target : tags.getKeys()) {
					updateParameter(population, tag + TAG_SEPARATOR + "ADHESION" + TARGET_SEPARATOR + target,
							population.get("ADHESION"), parameterValues, parameterScales);
				}
			}
			
			// Scale volume and surface parameter units.
			population.put("CRITICAL_VOLUME",
					Math.round(population.getDouble("CRITICAL_VOLUME")/(DS*DS*DS)));
			population.put("CRITICAL_SURFACE",
					Math.round(population.getDouble("CRITICAL_SURFACE")/(DS*DS)));
		}
	}
	
	/**
	 * Creates selected helpers.
	 * 
	 * @param helpers  the list of helper dictionaries
	 * @param helperDefaults  the dictionary of default helper parameters
	 */
	void updateHelpers(ArrayList<Box> helpers, MiniBox helperDefaults) {
		// TODO
	}
	
	/**
	 * Creates selected components.
	 *
	 * @param components  the list of component dictionaries
	 * @param componentDefaults  the dictionary of default component parameters
	 */
	void updateComponents(ArrayList<Box> components, MiniBox componentDefaults) {
		// TODO
	}
	
	/**
	 * Creates selected profilers.
	 *
	 * @param profilers  the list of profiler dictionaries
	 * @param profilerDefaults  the dictionary of default component parameters
	 */
	void updateProfilers(ArrayList<Box> profilers, MiniBox profilerDefaults) {
		// TODO
	}
	
	/**
	 * Creates selected checkpoints.
	 *
	 * @param checkpoints  the list of checkpoint dictionaries
	 * @param checkpointDefaults  the dictionary of default checkpoint parameters
	 */
	void updateCheckpoints(ArrayList<Box> checkpoints, MiniBox checkpointDefaults) {
		// TODO
	}
	
	/**
	 * Updates parameter values from default.
	 *
	 * @param box  the parameter map
	 * @param parameter  the parameter name
	 * @param defaultParameter  the default parameter value
	 * @param values  the map of parameter values
	 * @param scales  the map of parameter scaling
	 */
	static void updateParameter(MiniBox box, String parameter,
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
	 * Uses reflections to build constructors for simulation and visualization.
	 */
	void makeConstructors() {
		String simClass = "arcade.sim.PottsSimulation" + (_height > 1 ? "3D" : "2D");
		String visClass = "arcade.vis.PottsVisualization";
		
		// Create constructor for simulation class.
		try {
			Class<?> c = Class.forName(simClass);
			simCons = c.getConstructor(long.class, Series.class);
		} catch (Exception e) { e.printStackTrace(); }
		
		// Create constructor for visualization class.
		try {
			Class<?> c = Class.forName(visClass);
			visCons = c.getConstructor(Simulation.class);
		} catch (Exception e) { e.printStackTrace(); }
	}
	
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