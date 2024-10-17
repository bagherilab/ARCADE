package arcade.core.sim.output;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Logger;
import com.google.gson.Gson;
import arcade.core.agent.cell.CellContainer;
import arcade.core.env.location.LocationContainer;
import arcade.core.sim.Series;
import arcade.core.sim.Simulation;
import static arcade.core.sim.Simulation.DEFAULT_CELL_TYPE;
import static arcade.core.sim.Simulation.DEFAULT_LOCATION_TYPE;

/**
 * Custom loader for deserializing objects from JSON.
 *
 * <p>The loader is associated with an implementation-specific {@code Gson} instance that defines
 * deserialization of implementation-specific classes. The associated {@link arcade.core.sim.Series}
 * instance provides any static series-specific information needed for loading. The equipped {@link
 * arcade.core.sim.Simulation} instance is called to get the random seed (if needed) to select the
 * correct files to load. The tag {@code [#]} in the load path is replaced with the random seed of
 * the simulation instance.
 */
public abstract class OutputLoader {
    /** Logger for {@code OutputLoader}. */
    private static final Logger LOGGER = Logger.getLogger(OutputLoader.class.getName());

    /** JSON representation. */
    final Gson gson;

    /** {@link arcade.core.sim.Series} instance. */
    final Series series;

    /** Prefix for loaded files. */
    public String prefix;

    /** {@code true} if cells are loaded, {@code false} otherwise. */
    public boolean loadCells;

    /** {@code true} if locations are loaded, {@code false} otherwise. */
    public boolean loadLocations;

    /** JSON for cells. */
    String cellJson;

    /** JSON for locations. */
    String locationJson;

    /**
     * Creates an {@code OutputLoader} for the series.
     *
     * @param series the simulation series
     */
    public OutputLoader(Series series) {
        this.series = series;
        this.gson = makeGSON();
    }

    /**
     * Creates a {@code Gson} instance for deserializing objects.
     *
     * @return a {@code Gson} instance
     */
    protected abstract Gson makeGSON();

    /**
     * Equips a {@link arcade.core.sim.Simulation} instance to the loader.
     *
     * @param sim the simulation instance
     */
    public void equip(Simulation sim) {
        String seed = String.format("%04d", sim.getSeed());
        String path = prefix.replace("[#]", seed);
        if (loadCells) {
            this.cellJson = read(path + ".CELLS.json");
        }
        if (loadLocations) {
            this.locationJson = read(path + ".LOCATIONS.json");
        }
    }

    /**
     * Loads the JSON for a list of {@link CellContainer} objects.
     *
     * @return a list of {@link CellContainer} objects
     */
    public ArrayList<CellContainer> loadCells() {
        return gson.fromJson(cellJson, DEFAULT_CELL_TYPE);
    }

    /**
     * Loads the JSON for a list of {@link LocationContainer} objects.
     *
     * @return a list of {@link LocationContainer} objects
     */
    public ArrayList<LocationContainer> loadLocations() {
        return gson.fromJson(locationJson, DEFAULT_LOCATION_TYPE);
    }

    /**
     * Reads the contents of the given file path.
     *
     * @param filepath the path for the file
     * @return the contents of the path, {@code null} if unable to read
     */
    String read(String filepath) {
        try {
            // Get reader
            File infile = new File(filepath);
            FileInputStream fis = new FileInputStream(infile);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));

            // Write contents
            StringBuilder contents = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                contents.append(line);
            }

            // Close streams.
            br.close();
            fis.close();

            LOGGER.info("file [ " + filepath + " ] successfully read");
            return contents.toString();
        } catch (IOException ex) {
            LOGGER.severe("error reading [ " + filepath + " ] due to " + ex.getClass().getName());
            ex.printStackTrace();
            return null;
        }
    }
}
