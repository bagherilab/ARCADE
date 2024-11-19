package arcade.core.sim;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import sim.display.GUIState;
import sim.engine.SimState;
import arcade.core.sim.output.*;
import arcade.core.util.Box;
import arcade.core.util.MiniBox;
import static arcade.core.util.MiniBox.TAG_SEPARATOR;

/**
 * Abstract simulation manager for {@link Simulation} objects, differing only in random seed.
 *
 * <p>The class is instantiated by parsing an XML document specifying model setup. Constructors for
 * the {@link Simulation} objects are built, but not called to instantiate the simulation until the
 * series is run. {@code Series} objects that are not valid are flagged with {@code isSkipped} and
 * are not run.
 *
 * <p>{@link Simulation} objects are passed their parent {@code Series} object.
 */
public abstract class Series {
    /** Logger for {@code Series}. */
    private static final Logger LOGGER = Logger.getLogger(Series.class.getName());

    /** Regular expression for numbers. */
    private static final String NUMBER_REGEX = "^(\\d+)|(\\d+E\\d+)$";

    /** Regular expression for fractions. */
    private static final String FRACTION_REGEX = "^(([0]*(\\.\\d*|))|(1[\\.0]*))$";

    /** Regular expression for distributions. */
    public static final String DISTRIBUTION_REGEX =
            "^([A-Z\\_]+)\\(([A-Z]+)=([\\d\\.]+)(?:,([A-Z]+)=([\\d\\.]+))*\\)$";

    /** Offset of random seed to avoid using seed of 0. */
    public static final int SEED_OFFSET = 1000;

    /** {@code true} if {@code Series} is not valid, {@code false} otherwise. */
    public boolean isSkipped;

    /** {@code true} if {@code Series} is visualized, {@code false} otherwise. */
    public boolean isVis;

    /** Output saver for the simulation. */
    public OutputSaver saver;

    /** Output loader for the simulation. */
    public OutputLoader loader;

    /** Name of the series. */
    private final String name;

    /** Path and prefix for the series. */
    private final String prefix;

    /** Spatial conversion factor (um/voxel). */
    public final double ds;

    /** Spatial conversion factor in z (um/voxel). */
    public final double dz;

    /** Temporal conversion factor (hrs/tick). */
    public final double dt;

    /** Constructor for the simulation. */
    protected Constructor<?> simCons;

    /** Constructor for the visualization. */
    protected Constructor<?> visCons;

    /** Random seed of the first simulation in the series. */
    private final int startSeed;

    /** Random seed of the last simulation in the series. */
    private final int endSeed;

    /** Simulation length in ticks. */
    private final int ticks;

    /** Snapshot interval in ticks. */
    private final int interval;

    /** Length of the simulation. */
    public final int length;

    /** Width of the simulation. */
    public final int width;

    /** Height of the simulation. */
    public final int height;

    /** Margin for the simulation. */
    public final int margin;

    /** Map of population settings. */
    public HashMap<String, MiniBox> populations;

    /** Map of layer settings. */
    public HashMap<String, MiniBox> layers;

    /** Map of action settings. */
    public HashMap<String, MiniBox> actions;

    /** Map of component settings. */
    public HashMap<String, MiniBox> components;

    /**
     * Creates a {@code Series} object given setup information parsed from XML.
     *
     * @param setupDicts the map of attribute to value for single instance tags
     * @param setupLists the map of attribute to value for multiple instance tags
     * @param path the path for simulation output
     * @param parameters the default parameter values
     * @param isVis {@code true} if visualized, {@code false} otherwise
     */
    public Series(
            HashMap<String, MiniBox> setupDicts,
            HashMap<String, ArrayList<Box>> setupLists,
            String path,
            Box parameters,
            boolean isVis) {
        MiniBox set = setupDicts.get("set");
        MiniBox series = setupDicts.get("series");
        MiniBox defaults = parameters.getIdValForTag("DEFAULT");

        this.isVis = isVis;

        // Set name and prefix.
        this.name = series.get("name");
        this.prefix = path + (set.contains("prefix") ? set.get("prefix") : "") + name;

        // Set random seeds.
        this.startSeed =
                (series.contains("start") ? series.getInt("start") : defaults.getInt("START_SEED"));
        this.endSeed =
                (series.contains("end") ? series.getInt("end") : defaults.getInt("END_SEED"));

        // Set number of ticks and interval
        this.ticks = (series.contains("ticks") ? series.getInt("ticks") : defaults.getInt("TICKS"));
        this.interval =
                (series.contains("interval")
                        ? series.getInt("interval")
                        : defaults.getInt("INTERVAL"));

        // Set sizing.
        this.length =
                (series.contains("length") ? series.getInt("length") : defaults.getInt("LENGTH"));
        this.width = (series.contains("width") ? series.getInt("width") : defaults.getInt("WIDTH"));
        this.height =
                (series.contains("height") ? series.getInt("height") : defaults.getInt("HEIGHT"));
        this.margin =
                (series.contains("margin") ? series.getInt("margin") : defaults.getInt("MARGIN"));

        // Set conversion factors.
        this.ds = (series.contains("ds") ? series.getDouble("ds") : defaults.getDouble("DS"));
        this.dz = (series.contains("dz") ? series.getDouble("dz") : defaults.getDouble("DZ"));
        this.dt = (series.contains("dt") ? series.getDouble("dt") : defaults.getDouble("DT"));

        // Initialize simulation series.
        initialize(setupLists, parameters);

        // Create constructors for simulation and visualization.
        makeConstructors();
    }

    /**
     * Gets the name of the series.
     *
     * @return the name of the series
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the prefix for the series, including file path.
     *
     * @return the file path and prefix for the series
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Gets the start random seed.
     *
     * @return the random seed
     */
    public int getStartSeed() {
        return startSeed;
    }

    /**
     * Gets the end random seed.
     *
     * @return the random seed
     */
    public int getEndSeed() {
        return endSeed;
    }

    /**
     * Gets the number of ticks per simulation.
     *
     * @return the ticks
     */
    public int getTicks() {
        return ticks;
    }

    /**
     * Gets the number of ticks between snapshots.
     *
     * @return the interval
     */
    public int getInterval() {
        return interval;
    }

    /**
     * Checks if string contains valid number greater than 0.
     *
     * @param box the box containing the fraction
     * @param key the number key
     * @return {@code true if valid}, {@code false} otherwise
     */
    protected static boolean isValidNumber(Box box, String key) {
        if (box.getValue(key) == null) {
            return false;
        }
        return box.getValue(key).matches(NUMBER_REGEX);
    }

    /**
     * Checks if string contains valid fraction between 0 and 1, inclusive.
     *
     * @param box the box containing the fraction
     * @param key the fraction key
     * @return {@code true if valid}, {@code false} otherwise
     */
    protected static boolean isValidFraction(Box box, String key) {
        if (box.getValue(key) == null) {
            return false;
        }
        return box.getValue(key).matches(FRACTION_REGEX);
    }

    /**
     * Initializes series simulation, agents, and environment.
     *
     * @param setupLists the map of attribute to value for multiple instance tags
     * @param parameters the default parameter values loaded from {@code parameter.xml}
     */
    protected abstract void initialize(HashMap<String, ArrayList<Box>> setupLists, Box parameters);

    /**
     * Creates agent populations.
     *
     * @param populationsBox the list of population setup dictionaries
     * @param populationDefaults the dictionary of default population parameters
     * @param populationConversions the dictionary of population parameter conversions
     */
    protected abstract void updatePopulations(
            ArrayList<Box> populationsBox,
            MiniBox populationDefaults,
            MiniBox populationConversions);

    /**
     * Creates environment layers.
     *
     * @param layersBox the list of layer setup dictionaries
     * @param layerDefaults the dictionary of default layer parameters
     * @param layerConversions the dictionary of layer parameter conversions
     */
    protected abstract void updateLayers(
            ArrayList<Box> layersBox, MiniBox layerDefaults, MiniBox layerConversions);

    /**
     * Creates selected actions.
     *
     * @param actionsBox the list of action dictionaries
     * @param actionDefaults the dictionary of default action parameters
     */
    protected abstract void updateActions(ArrayList<Box> actionsBox, MiniBox actionDefaults);

    /**
     * Creates selected components.
     *
     * @param componentsBox the list of component dictionaries
     * @param componentDefaults the dictionary of default component parameters
     */
    protected abstract void updateComponents(
            ArrayList<Box> componentsBox, MiniBox componentDefaults);

    /**
     * Parses parameter values based on default value.
     *
     * @param box the parameter map
     * @param parameter the parameter name
     * @param defaultParameter the default parameter value
     * @param values the map of parameter values
     * @param scales the map of parameter scaling
     */
    protected static void parseParameter(
            MiniBox box,
            String parameter,
            String defaultParameter,
            MiniBox values,
            MiniBox scales) {
        String value = values.contains(parameter) ? values.get(parameter) : defaultParameter;
        Matcher match = Pattern.compile(DISTRIBUTION_REGEX).matcher(value);

        if (match.find()) {
            box.put("(DISTRIBUTION)" + TAG_SEPARATOR + parameter, match.group(1).toUpperCase());
            for (int i = 0; i < (match.groupCount() - 1) / 2; i++) {
                int index = 2 * (i + 1);
                box.put(parameter + "_" + match.group(index), match.group(index + 1));
            }
        } else {
            box.put(parameter, value);
            if (scales.contains(parameter)) {
                box.put(parameter, box.getDouble(parameter) * scales.getDouble(parameter));
            }
        }
    }

    /**
     * Updates conversion string into a value.
     *
     * <p>Conversion string is in the form of {@code D^N} where {@code D} is either {@code DS},
     * {@code DZ}, or {@code DT} and {@code N} is an integer exponent. Conversions with {@code DZ}
     * are replaced with {@code DS}. Multiple terms can be chained in the form {@code D^N1.D^N2}.
     * The {@code ^N} is not required if N = 1.
     *
     * @param convert the conversion string
     * @param ds the spatial conversion factor
     * @param dt the temporal conversion factor
     * @return the updated conversion factor
     */
    protected static double parseConversion(String convert, double ds, double dt) {
        return parseConversion(convert, ds, ds, dt);
    }

    /**
     * Updates conversion string into a value.
     *
     * <p>Conversion string is in the form of {@code D^N} where {@code D} is either {@code DS},
     * {@code DZ}, or {@code DT} and {@code N} is an integer exponent. Multiple terms can be chained
     * in the form {@code D^N1.D^N2}. The {@code ^N} is not required if N = 1.
     *
     * @param convert the conversion string
     * @param ds the spatial conversion factor in xy
     * @param dz the spatial conversion factor in z
     * @param dt the temporal conversion factor
     * @return the updated conversion factor
     */
    protected static double parseConversion(String convert, double ds, double dz, double dt) {
        double value = 1;
        String[] split = convert.split("\\.");
        for (String s : split) {
            String[] subsplit = s.replace(" ", "").split("\\^");
            double v =
                    (subsplit[0].equals("DS")
                            ? ds
                            : (subsplit[0].equals("DZ")
                                    ? dz
                                    : (subsplit[0].equals("DT") ? dt : 1)));
            int n = (subsplit.length == 2 ? Integer.parseInt(subsplit[1]) : 1);
            value *= Math.pow(v, n);
        }
        return value;
    }

    /** Uses reflections to build constructors for simulation and visualization. */
    protected void makeConstructors() {
        // Create constructor for simulation class.
        try {
            Class<?> c = Class.forName(getSimClass());
            simCons = c.getConstructor(long.class, Series.class);
        } catch (Exception e) {
            LOGGER.severe("simulation class [ " + getSimClass() + " ] not found");
            e.printStackTrace();
            isSkipped = true;
        }

        // Create constructor for visualization class.
        try {
            Class<?> c = Class.forName(getVisClass());
            visCons = c.getConstructor(Simulation.class);
        } catch (Exception e) {
            LOGGER.severe("visualization class [ " + getSimClass() + " ] not found");
            e.printStackTrace();
            isSkipped = true;
        }
    }

    /**
     * Gets the class name for the simulation.
     *
     * @return the simulation class
     */
    protected abstract String getSimClass();

    /**
     * Gets the class name for the visualization.
     *
     * @return the visualization class
     */
    protected abstract String getVisClass();

    /**
     * Calls {@code runSim} for each random seed.
     *
     * @throws Exception if simulation constructor cannot be instantiated
     */
    public void runSims() throws Exception {
        long simStart;
        long simEnd;

        // Iterate through each seed.
        for (int seed = startSeed; seed <= endSeed; seed++) {
            // Pre-simulation output.
            LOGGER.info(String.format("simulation [ %s | %04d ] started", name, seed));
            simStart = System.currentTimeMillis();

            // Run simulation.
            SimState simstate = (SimState) (simCons.newInstance(seed + SEED_OFFSET, this));
            runSim(simstate, seed);

            // Post-simulation output.
            simEnd = System.currentTimeMillis();
            LOGGER.info(
                    String.format(
                            "simulation [ %s | %04d ] finished in %.4f minutes",
                            name, seed, (double) (simEnd - simStart) / 1000 / 60));
        }
    }

    /**
     * Iterates through each tick of the simulation.
     *
     * @param simstate the simulation state instance
     * @param seed the random seed
     */
    void runSim(SimState simstate, int seed) {
        // Start simulation.
        simstate.start();

        // Set up logger checkpoints.
        double delta = ticks / 10.0;
        double checkpoint = delta;

        // Run simulation loop.
        double tick;
        do {
            if (!simstate.schedule.step(simstate)) {
                break;
            }
            tick = simstate.schedule.getTime();

            if (tick >= checkpoint) {
                LOGGER.info(
                        String.format(
                                "simulation [ %s | %04d ] tick %6d ( %4.2f %% )",
                                name, seed, (int) tick, (100 * tick / ticks)));
                checkpoint += delta;
            }
        } while (tick < ticks - 1);

        // Finish simulation.
        simstate.finish();
    }

    /**
     * Creates controller for visualization.
     *
     * @throws Exception if visualization constructor cannot be instantiated
     */
    public void runVis() throws Exception {
        if (System.getProperty("java.awt.headless") != null
                && System.getProperty("java.awt.headless").equals("true")) {
            return;
        }
        Simulation sim = (Simulation) (simCons.newInstance(startSeed + SEED_OFFSET, this));
        ((GUIState) visCons.newInstance(sim)).createController();
    }
}
