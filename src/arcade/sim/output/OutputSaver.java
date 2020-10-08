package arcade.sim.output;

import java.io.*;
import java.util.logging.Logger;
import arcade.sim.Potts;
import arcade.sim.Simulation;
import arcade.env.grid.Grid;

public class OutputSaver {
	private final static Logger LOGGER = Logger.getLogger(OutputSaver.class.getName());
	
	final String prefix;
	
	final Grid agents;
	
	final Potts potts;
	
	public OutputSaver(Simulation sim) {
		prefix =  String.format("%s_%04d", sim.getSeries().getPrefix(), sim.getSeed());
		potts = sim.getPotts();
		agents = sim.getAgents();
		
		String path = prefix + ".json";
		write(path, sim.getSeries().toJSON());
	}
	
	public void save(double tick) {
		String agentsPath = prefix + String.format("_%06d.%s.%s",(int)tick, "AGENTS", "json");
		write(agentsPath, agents.toJSON());
		
		String pottsPath = prefix + String.format("_%06d.%s.%s", (int)tick, "POTTS", "csv");
		write(pottsPath, potts.toCSV());
	}
	
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
		} catch(IOException ex) {
			LOGGER.severe("error writing [ " + filepath + " ] due to " + ex.getClass().getName());
			System.exit(-1);
		}
	}
}
