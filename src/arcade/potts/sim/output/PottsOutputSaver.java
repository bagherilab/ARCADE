package arcade.potts.sim.output;

import com.google.gson.Gson;
import arcade.core.sim.Series;
import arcade.core.sim.output.OutputSaver;

public final class PottsOutputSaver extends OutputSaver {
    /**
     * Creates an {@code PottsOutputSaver} for the series.
     * 
     * @param series  the simulation series
     */
    public PottsOutputSaver(Series series) { super(series); }
    
    @Override
    protected Gson makeGSON() { return PottsOutputSerializer.makeGSON(); }
}
