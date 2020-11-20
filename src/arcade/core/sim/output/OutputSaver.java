package arcade.core.sim.output;

import java.io.*;
import java.util.logging.Logger;
import com.google.gson.*;
import sim.engine.*;
import arcade.core.sim.Series;
import arcade.core.sim.Potts;
import arcade.core.sim.Simulation;
import arcade.core.env.grid.Grid;

public class OutputSaver implements Steppable {
	/** Logger for class */
	private final static Logger LOGGER = Logger.getLogger(OutputSaver.class.getName());
	
	/** JSON representation */
	final Gson gson;
	
	/** {@link arcade.core.sim.Series} instance */
	final Series series;
	
	/** Prefix for saved files */
	String prefix;
	
	/** {@link arcade.core.sim.Simulation} instance */
	Simulation sim;
	
	/** {@link arcade.core.env.grid.Grid} instance containing agents */
	Grid agents;
	
	/** {@link arcade.sim.Potts} instance */
	Potts potts;
	
	/**
	 * Creates an {@code OutputSaver} for the series.
	 * 
	 * @param series  the simulation series
	 */
	public OutputSaver(Series series) {
		this.series = series;
		gson = OutputSerializer.makeGSON();
	}
	
	/**
	 * Equips the given {@link arcade.core.sim.Simulation} instance to the saver.
	 * 
	 * @param sim  the simulation instance
	 */
	public void equip(Simulation sim) {
		this.prefix = String.format("%s_%04d", series.getPrefix(), sim.getSeed());
		this.sim = sim;
		this.potts = sim.getPotts();
		this.agents = sim.getAgents();
	}
	
	/**
	 * Saves the {@link arcade.core.sim.Series} as a JSON.
	 */
	public void save() {
		String path = series.getPrefix() + ".json";
		write(path, format(gson.toJson(series)));
	}
	
	/**
	 * Saves a snapshot of the simulation at the given tick.
	 * 
	 * @param tick  the tick
	 */
	public void save(double tick) {
		String agentsPath = prefix + String.format("_%06d.%s.%s",(int)tick, "CELLS", "json");
		write(agentsPath, format(gson.toJson(agents)));
		
		String pottsPath = prefix + String.format("_%06d.%s.%s", (int)tick, "LOCATIONS", "json");
		write(pottsPath, format(gson.toJson(potts)));
	}
	
	/**
	 * Steps through cell rules.
	 *
	 * @param simstate  the MASON simulation state
	 */
	public void step(SimState simstate) {
		save(simstate.schedule.getTime());
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
	void write(String filepath, String contents) {
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
	static String format(String string) {
		String formatted = string;
		formatted = formatted.replaceAll("\\[\\n[\\s\\t]+([\\d\\.]+),\\n[\\s\\t]+([\\d\\.]+)\\n\\s+\\]",
				"[$1, $2]");
		formatted = formatted.replaceAll("\\[\\n[\\s\\t]+([\\d\\.]+),\\n[\\s\\t]+([\\d\\.]+),\\n[\\s\\t]+([\\d\\.]+)\\n\\s+\\]",
				"[$1, $2, $3]");
		return formatted;
	}
}
