package arcade.patch.sim.output;

import com.google.gson.Gson;
import arcade.core.sim.Series;
import arcade.core.sim.output.OutputLoader;

/**
 * Custom loader for patch-specific deserialization.
 */

public final class PatchOutputLoader extends OutputLoader {
    /**
     * Creates an {@code PatchOutputLoader} for the series.
     *
     * @param series  the simulation series
     */
    public PatchOutputLoader(Series series) { super(series); }
    
    @Override
    protected Gson makeGSON() { return PatchOutputDeserializer.makeGSON(); }
}
