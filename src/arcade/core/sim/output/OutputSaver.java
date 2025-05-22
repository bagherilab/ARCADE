package arcade.core.sim.output;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import com.google.gson.Gson;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;
import arcade.core.sim.Series;
import arcade.core.sim.Simulation;
import static arcade.core.sim.Simulation.DEFAULT_CELL_TYPE;
import static arcade.core.sim.Simulation.DEFAULT_LOCATION_TYPE;

/**
 * Custom saver for serializing objects to JSON.
 *
 * <p>The saver is associated with an implementation-specific {@code Gson} instance that defines
 * serialization of implementation-specific classes. The associated {@link arcade.core.sim.Series}
 * instance is used to save static information specific to the series. The equipped {@link
 * arcade.core.sim.Simulation} instance is called to get cells / locations that are saved at a given
 * tick.
 */
public abstract class OutputSaver implements Steppable {
    /** Logger for {@code OutputSaver}. */
    protected static final Logger LOGGER = Logger.getLogger(OutputSaver.class.getName());

    /** Number of elements to format in output string. */
    private static final int FORMAT_ELEMENTS = 6;

    /** JSON representation. */
    final Gson gson;

    /** {@link arcade.core.sim.Series} instance. */
    final Series series;

    /** Prefix for saved files. */
    public String prefix;

    /** {@link arcade.core.sim.Simulation} instance. */
    Simulation sim;

    /**
     * Creates an {@code OutputSaver} for the series.
     *
     * @param series the simulation series
     */
    public OutputSaver(Series series) {
        this.series = series;
        this.gson = makeGSON();
    }

    /**
     * Creates a {@code Gson} instance for serializing objects.
     *
     * @return a {@code Gson} instance
     */
    protected abstract Gson makeGSON();

    /**
     * Equips a {@link arcade.core.sim.Simulation} instance to the saver.
     *
     * @param sim the simulation instance
     */
    public void equip(Simulation sim) {
        this.prefix = String.format("%s_%04d", series.getPrefix(), sim.getSeed());
        this.sim = sim;
    }

    /** Saves the {@link arcade.core.sim.Series} as a JSON. */
    public void saveSeries() {
        String path = series.getPrefix() + ".json";
        String json = gson.toJson(series);
        write(path, format(json, FORMAT_ELEMENTS));
    }

    /**
     * Save a list of {@link arcade.core.agent.cell.CellContainer} to a JSON.
     *
     * @param tick the simulation tick
     */
    public void saveCells(int tick) {
        String path = prefix + String.format("_%06d.CELLS.json", tick);
        String json = gson.toJson(sim.getCells(), DEFAULT_CELL_TYPE);
        write(path, format(json, FORMAT_ELEMENTS));
    }

    /**
     * Save a list of {@link arcade.core.env.location.LocationContainer} to a JSON.
     *
     * @param tick the simulation tick
     */
    public void saveLocations(int tick) {
        String path = prefix + String.format("_%06d.LOCATIONS.json", tick);
        String json = gson.toJson(sim.getLocations(), DEFAULT_LOCATION_TYPE);
        write(path, format(json, FORMAT_ELEMENTS));
    }

    /**
     * Steps through cell rules.
     *
     * @param simstate the MASON simulation state
     */
    @Override
    public void step(SimState simstate) {
        int tick = (int) simstate.schedule.getTime();
        saveCells(tick);
        saveLocations(tick);
    }

    /**
     * Schedules the saver to take snapshots at the given interval.
     *
     * @param schedule the simulation schedule
     * @param interval the interval (in ticks) between snapshots
     */
    public void schedule(Schedule schedule, double interval) {
        schedule.scheduleRepeating(Schedule.EPOCH, -1, this, interval);
    }

    /**
     * Writes the contents to the given file path.
     *
     * @param filepath the path for the file
     * @param contents the contents of the file
     */
    public void write(String filepath, String contents) {
        try {
            // Get writer
            File outfile = new File(filepath);
            FileOutputStream fos = new FileOutputStream(outfile, false);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

            // Write contents
            bw.write(contents);

            // Close streams.
            bw.close();
            fos.close();

            LOGGER.info("file [ " + filepath + " ] successfully written");
        } catch (IOException ex) {
            LOGGER.severe("error writing [ " + filepath + " ] due to " + ex.getClass().getName());
            ex.printStackTrace();
            series.isSkipped = true;
        }
    }

    /**
     * Formats the arrays in the given string.
     *
     * <p>Method reformats the output of GSON pretty printing by converting {@code [\n A,\n B,\n ...
     * N\n ]} to {@code [ A, B, ..., N ]} for lists of up to N elements.
     *
     * @param string the string to format
     * @param maxElements the maximum number of elements to format.
     * @return the formatted string
     */
    protected static String format(String string, int maxElements) {
        String formatted = string;

        for (int elements = 1; elements < maxElements + 1; elements++) {
            String inputPattern =
                    IntStream.rangeClosed(1, elements)
                            .mapToObj(Integer::toString)
                            .map(s -> "([\\-\\d\\.]+)")
                            .collect(Collectors.joining(",\\n[\\s\\t]+"));
            inputPattern = "\\[\\n[\\s\\t]+" + inputPattern + "\\n[\\s\\t]+\\]";

            String outputPattern =
                    IntStream.rangeClosed(1, elements)
                            .mapToObj(Integer::toString)
                            .map(s -> "$" + s)
                            .collect(Collectors.joining(", "));
            outputPattern = "[" + outputPattern + "]";

            formatted = formatted.replaceAll(inputPattern, outputPattern);
        }

        return formatted;
    }
}
