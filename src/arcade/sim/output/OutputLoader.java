package arcade.sim.output;

import java.io.*;
import java.util.logging.Logger;
import com.google.gson.*;
import arcade.sim.Series;
import arcade.sim.Simulation;
import arcade.agent.cell.CellFactory;
import arcade.env.loc.LocationFactory;
import static arcade.agent.cell.CellFactory.CellFactoryContainer;
import static arcade.env.loc.LocationFactory.LocationFactoryContainer;

public class OutputLoader {
	/** Logger for class */
	private final static Logger LOGGER = Logger.getLogger(OutputLoader.class.getName());
	
	/** JSON representation */
	final Gson gson;
	
	/** {@link arcade.sim.Series} instance */
	final Series series;
	
	/** Prefix for loaded files */
	final String prefix;
	
	/** JSON for cells */
	String cellJson;
	
	/** JSON for locations */
	String locationJson;
	
	/** {@code true} if cells are loaded, {@code false} otherwise */
	final public boolean loadCells;
	
	/** {@code true} if locations are loaded, {@code false} otherwise */
	final public boolean loadLocations;
	
	/**
	 * Creates an {@code OutputLoader} for the series.
	 *
	 * @param series  the simulation series
	 */
	public OutputLoader(Series series, String prefix, boolean loadCells, boolean loadLocations) {
		this.series = series;
		this.prefix = prefix;
		this.loadCells = loadCells;
		this.loadLocations = loadLocations;
		gson = OutputDeserializer.makeGSON();
	}
	
	/**
	 * Equips the given {@link arcade.sim.Simulation} instance to the loader.
	 *
	 * @param sim  the simulation instance
	 */
	public void equip(Simulation sim) {
		String seed = String.format("%04d", sim.getSeed());
		String path = prefix.replace("(#)", seed);
		if (loadCells) { this.cellJson = read(path + ".CELLS.json"); }
		if (loadLocations) {  this.locationJson = read(path + ".LOCATIONS.json"); }
	}
	
	/**
	 * Loads the JSON for a {@link arcade.env.loc.LocationFactory}
	 */
	public void load(LocationFactory factory) {
		factory.container = gson.fromJson(locationJson, LocationFactoryContainer.class);
	}
	
	/**
	 * Loads the JSON for a {@link arcade.agent.cell.CellFactory}
	 */
	public void load(CellFactory factory) {
		factory.container = gson.fromJson(cellJson, CellFactoryContainer.class);
	}
	
	/**
	 * Reads the contents of the given file path.
	 *
	 * @param filepath  the path for the file
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
			while ((line = br.readLine()) != null) { contents.append(line); }
			
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