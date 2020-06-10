package arcade.sim.checkpoint;

import java.io.*;
import java.util.logging.Logger;
import sim.engine.SimState;
import arcade.sim.Series;

/**
 * Utility class for saving and loading checkpoint files.
 *
 * @author  Jessica S. Yu <jessicayu@u.northwestern.edu>
 * @version 2.3.1
 * @since   2.3
 */

public abstract class Checkpoint {
	private final static Logger LOGGER = Logger.getLogger(Checkpoint.class.getName());
	private final static String EXTENSION = ".checkpoint";
	
	// ABSTRACT METHODS.
	public abstract void scheduleCheckpoint(SimState state, Series series, String seed);
	
	// METHOD: save. Saves given object to given file checkpoint.
	static void save(Object object, String filename) {
		try {
			// File name with extension.
			filename = filename + EXTENSION;
			
			// Open streams.
			FileOutputStream file = new FileOutputStream(filename);
			ObjectOutputStream out = new ObjectOutputStream(file);
			
			// Serialize object
			out.writeObject(object);
			
			// Close streams.
			out.close();
			file.close();
			
			LOGGER.info("file [ " + filename + " ] successfully saved");
		} catch(IOException ex) {
			LOGGER.severe("error saving [ " + filename + " ] due to " + ex.getClass().getName());
			System.exit(-1);
		}
	}
	
	// METHOD: load. Loads object from given file checkpoint.
	static Object load(String filename) {
		try {
			// File name with extension.
			filename = filename + EXTENSION;
			
			// Open streams.
			FileInputStream file = new FileInputStream(filename);
			ObjectInputStream in = new ObjectInputStream(file);
			
			// Deserialize object
			Object object = in.readObject();
			
			// Close streams.
			in.close();
			file.close();
			
			LOGGER.info("file [ " + filename + " ] successfully loaded");
			return object;
		} catch (Exception ex) {
			LOGGER.severe("error loading [ " + filename + " ] due to " + ex.getClass().getName());
			System.exit(-1);
		}
		
		return null;
	}
}
