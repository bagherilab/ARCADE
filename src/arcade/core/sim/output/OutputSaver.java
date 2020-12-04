package arcade.core.sim.output;

import java.io.*;
import java.util.logging.Logger;
import com.google.gson.*;
import sim.engine.*;
import arcade.core.sim.Series;
import arcade.core.sim.Simulation;
import arcade.core.agent.cell.CellContainer;
import arcade.core.env.loc.LocationContainer;

public abstract class OutputSaver implements Steppable {
    /** Logger for class */
    private final static Logger LOGGER = Logger.getLogger(OutputSaver.class.getName());
    
    /** JSON representation */
    final Gson gson;
    
    /** {@link arcade.core.sim.Series} instance */
    final Series series;
    
    /** Prefix for saved files */
    public String prefix;
    
    /** {@link arcade.core.sim.Simulation} instance */
    Simulation sim;
    
    /**
     * Creates an {@code OutputSaver} for the series.
     * 
     * @param series  the simulation series
     */
    public OutputSaver(Series series) {
        this.series = series;
        this.gson = makeGSON();
    }
    
    /**
     * Creates a {@code Gson} instance for serializing objects.
     * 
     * @return  a {@code Gson} instance
     */
    protected abstract Gson makeGSON();
    
    /**
     * Equips the given {@link arcade.core.sim.Simulation} instance to the saver.
     * 
     * @param sim  the simulation instance
     */
    public void equip(Simulation sim) {
        this.prefix = String.format("%s_%04d", series.getPrefix(), sim.getSeed());
        this.sim = sim;
    }
    
    /**
     * Saves the {@link arcade.core.sim.Series} as a JSON.
     */
    public void saveSeries() {
        String path = series.getPrefix() + ".json";
        write(path, format(gson.toJson(series)));
    }
    
    /**
     * Save a list of {@link CellContainer} objects to a JSON.
     */
    public void saveCells(int tick) {
        String path = prefix + String.format("_%06d.CELLS.json", tick);
        write(path, format(gson.toJson(sim.getCells())));
    }
    
    /**
     * Save a list of {@link LocationContainer} objects to a JSON.
     */
    public void saveLocations(int tick) {
        String path = prefix + String.format("_%06d.LOCATIONS.json", tick);
        write(path, format(gson.toJson(sim.getLocations())));
    }
    
    /**
     * Steps through cell rules.
     * 
     * @param simstate  the MASON simulation state
     */
    public void step(SimState simstate) {
        int tick = (int)simstate.schedule.getTime();
        saveCells(tick);
        saveLocations(tick);
    }
    
    /**
     * Schedules the saver to take snapshots at the given interval.
     * 
     * @param schedule  the simulation schedule
     * @param interval  the interval (in ticks) between snapshots
     */
    public void schedule(Schedule schedule, double interval) {
        schedule.scheduleRepeating(Schedule.EPOCH, -1, this, interval);
    }
    
    /**
     * Writes the contents to the given file path.
     * 
     * @param filepath  the path for the file
     * @param contents  the contents of the file
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
     * <p>
     * Method modifies the output of GSON pretty printing by:
     * <ul>
     *     <li>Converting {@code [\n A,\n B\n ]} to {@code [ A, B ]}</li>
     *     <li>Converting {@code [\n A,\n B,\n C\n ]} to {@code [ A, B, C ]}</li>
     * </ul>
     * 
     * @param string  the string to format
     * @return  the formatted string
     */
    protected static String format(String string) {
        String r1 = "\\[\\n[\\s\\t]+([\\d\\.]+),\\n[\\s\\t]+([\\d\\.]+)\\n\\s+\\]";
        String r2 = "\\[\\n[\\s\\t]+([\\d\\.]+),\\n[\\s\\t]+([\\d\\.]+),"
                + "\\n[\\s\\t]+([\\d\\.]+)\\n\\s+\\]";
        String formatted = string;
        formatted = formatted.replaceAll(r1, "[$1, $2]");
        formatted = formatted.replaceAll(r2, "[$1, $2, $3]");
        return formatted;
    }
}
