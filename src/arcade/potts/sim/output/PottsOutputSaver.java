package arcade.potts.sim.output;

import com.google.gson.Gson;
import arcade.core.sim.Series;
import arcade.core.sim.output.OutputSaver;

/**
 * Custom saver for potts-specific serialization.
 */

public final class PottsOutputSaver extends OutputSaver {
    /**
     * Creates a {@code PottsOutputSaver} for the series.
     *
     * @param series  the simulation series
     */
    public PottsOutputSaver(Series series) { super(series); }
    
    @Override
    protected Gson makeGSON() { return PottsOutputSerializer.makeGSON(); }
}
