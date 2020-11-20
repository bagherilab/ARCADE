package arcade.potts.sim.output;

import arcade.core.sim.Series;
import arcade.core.sim.output.OutputLoader;

public class PottsOutputLoader extends OutputLoader {
	/**
	 * Creates an {@code PottsOutputLoader} for the series.
	 *
	 * @param series  the simulation series
	 */
	public PottsOutputLoader(Series series, String prefix, boolean loadCells, boolean loadLocations) {
		super(series, prefix, loadCells, loadLocations);
		gson = PottsOutputDeserializer.makeGSON();
	}
}
