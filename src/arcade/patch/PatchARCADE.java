package arcade.patch;

import arcade.core.ARCADE;
import arcade.core.sim.Series;
import arcade.core.sim.input.InputBuilder;
import arcade.core.sim.output.OutputLoader;
import arcade.core.sim.output.OutputSaver;
import arcade.patch.sim.input.PatchInputBuilder;
import arcade.patch.sim.output.PatchOutputLoader;
import arcade.patch.sim.output.PatchOutputSaver;

/**
 * Implementation of ARCADE for patch models.
 */

public final class PatchARCADE extends ARCADE {
    /**
     * ARCADE model with patches.
     */
    public PatchARCADE() { }
    
    @Override
    public String getResource(String s) { return PatchARCADE.class.getResource(s).toString(); }
    
    @Override
    public InputBuilder getBuilder() { return new PatchInputBuilder(); }
    
    @Override
    public OutputLoader getLoader(Series series) { return new PatchOutputLoader(series); }
    
    @Override
    public OutputSaver getSaver(Series series) { return new PatchOutputSaver(series); }
}
