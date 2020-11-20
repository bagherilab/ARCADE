package arcade.core.sim.output;

import java.io.*;
import java.util.logging.Logger;
import com.google.gson.*;
import arcade.core.sim.Series;
import arcade.core.sim.Simulation;

public class OutputLoader {
	/** Logger for class */
	private final static Logger LOGGER = Logger.getLogger(OutputLoader.class.getName());
	
	/** JSON representation */
	final Gson gson;
	
	/** {@link arcade.core.sim.Series} instance */
	final Series series;
	
	/** Prefix for loaded files */
	final String prefix;
	
	/**
	 * Creates an {@code OutputLoader} for the series.
	 *
	 * @param series  the simulation series
	 */
	public OutputLoader(Series series, String prefix) {
		this.series = series;
		this.prefix = prefix;
		gson = OutputDeserializer.makeGSON();
	}
	
	/**
	 * Equips the given {@link arcade.core.sim.Simulation} instance to the loader.
	 *
	 * @param sim  the simulation instance
	 */
	public void equip(Simulation sim) {
		String seed = String.format("%04d", sim.getSeed());
		String path = prefix.replace("(#)", seed);
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
