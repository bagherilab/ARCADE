package arcade.potts.sim.output;

import arcade.core.sim.Series;
import arcade.core.sim.output.OutputSaver;
import arcade.core.sim.Simulation;
import arcade.potts.sim.PottsSimulation;
import arcade.potts.sim.Potts;

public class PottsOutputSaver extends OutputSaver {
	/** {@link arcade.potts.sim.Potts} instance */
	Potts potts;
	
	/**
	 * Creates an {@code OutputSaver} for the series.
	 * 
	 * @param series  the simulation series
	 */
	public PottsOutputSaver(Series series) {
		super(series);
		gson = PottsOutputSerializer.makeGSON();
	}
	
	public void equip(Simulation sim) {
		super.equip(sim);
		this.potts = ((PottsSimulation)sim).getPotts();
	}
	
	/**
	 * Saves a snapshot of the simulation at the given tick.
	 * 
	 * @param tick  the tick
	 */
	public void save(double tick) {
		super.save(tick);
		
		String pottsPath = prefix + String.format("_%06d.%s.%s", (int)tick, "POTTS", "json");
		write(pottsPath, format(gson.toJson(potts)));
	}
}
