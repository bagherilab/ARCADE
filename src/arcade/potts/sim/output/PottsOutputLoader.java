package arcade.potts.sim.output;

import com.google.gson.Gson;
import arcade.core.sim.Series;
import arcade.core.sim.output.OutputLoader;

/**
 * Custom loader for potts-specific deserialization.
 */

public final class PottsOutputLoader extends OutputLoader {
    /**
     * Creates a {@code PottsOutputLoader} for the series.
     *
     * @param series  the simulation series
     */
    public PottsOutputLoader(Series series) { super(series); }
    
    @Override
    protected Gson makeGSON() { return PottsOutputDeserializer.makeGSON(); }
}
