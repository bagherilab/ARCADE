package arcade.potts;

import arcade.core.ARCADE;
import arcade.core.sim.Series;
import arcade.core.sim.input.InputBuilder;
import arcade.core.sim.output.OutputLoader;
import arcade.core.sim.output.OutputSaver;
import arcade.potts.sim.input.PottsInputBuilder;
import arcade.potts.sim.output.PottsOutputLoader;
import arcade.potts.sim.output.PottsOutputSaver;

public final class PottsARCADE extends ARCADE {
    public PottsARCADE() { }
    
    @Override
    public String getResource(String s) { return PottsARCADE.class.getResource(s).toString(); }
    
    @Override
    public InputBuilder getBuilder() { return new PottsInputBuilder(); }
    
    @Override
    public OutputLoader getLoader(Series series) { return new PottsOutputLoader(series); }
    
    @Override
    public OutputSaver getSaver(Series series) { return new PottsOutputSaver(series); }
}
