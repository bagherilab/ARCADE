package arcade.potts.sim.output;

import com.google.gson.Gson;
import arcade.core.sim.Series;
import arcade.core.sim.output.OutputLoader;

public class PottsOutputLoader extends OutputLoader {
    /**
     * Creates an {@code PottsOutputLoader} for the series.
     * 
     * @param series  the simulation series
     */
    public PottsOutputLoader(Series series) { super(series); }
    
    protected Gson makeGSON() { return PottsOutputDeserializer.makeGSON(); }
}
