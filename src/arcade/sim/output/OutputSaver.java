package arcade.sim.output;

import java.io.*;
import java.util.logging.Logger;
import com.google.gson.*;
import arcade.sim.Potts;
import arcade.sim.Simulation;
import arcade.env.grid.Grid;

public class OutputSaver {
	private final static Logger LOGGER = Logger.getLogger(OutputSaver.class.getName());
	
	final Gson gson;
	
	final String prefix;
	
	final Grid agents;
	
	final Potts potts;
	
	public OutputSaver(Simulation sim) {
		prefix =  String.format("%s_%04d", sim.getSeries().getPrefix(), sim.getSeed());
		potts = sim.getPotts();
		agents = sim.getAgents();
		
		gson = OutputSerializer.makeGSON();
		String path = prefix + ".json";
		write(path, format(gson.toJson(sim.getSeries())));
	}
	
	public void save(double tick) {
		String agentsPath = prefix + String.format("_%06d.%s.%s",(int)tick, "AGENTS", "json");
		write(agentsPath, format(gson.toJson(agents)));
		
		String pottsPath = prefix + String.format("_%06d.%s.%s", (int)tick, "POTTS", "json");
		write(pottsPath, format(gson.toJson(potts)));
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
	
	static String format(String string){
		String formatted = string;
		formatted = formatted.replaceAll("\\[\\n[\\s\\t]+([\\d\\.]+),\\n[\\s\\t]+([\\d\\.]+)\\n\\s+\\]",
				"[$1, $2]");
		formatted = formatted.replaceAll("\\[\\n[\\s\\t]+([\\d\\.]+),\\n[\\s\\t]+([\\d\\.]+),\\n[\\s\\t]+([\\d\\.]+)\\n\\s+\\]",
				"[$1, $2, $3]");
		return formatted;
	}
}
