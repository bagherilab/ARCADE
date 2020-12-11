package arcade.potts.sim.output;

import com.google.gson.Gson;
import arcade.core.sim.Series;
import arcade.core.sim.output.OutputSaver;

public class PottsOutputSaver extends OutputSaver {
    /**
     * Creates an {@code PottsOutputSaver} for the series.
     * 
     * @param series  the simulation series
     */
    public PottsOutputSaver(Series series) { super(series); }
    
    protected Gson makeGSON() { return PottsOutputSerializer.makeGSON(); }
}
