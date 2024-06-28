package arcade.agent.module;

import arcade.agent.cell.Cell;
import arcade.sim.Simulation;

public class SensingSimple extends Sensing {
    private static final double SECRETION_RATE = 100;

    public SensingSimple(Cell c, Simulation sim) {
        super(c, sim);
    }
    
    @Override
    public void stepModule(Simulation sim) {
        if (c.getEnergy() < 0) {
            Double VEGF = sim.getEnvironment("vegf").getAverageVal(loc);
            Double newVEGF = VEGF + SECRETION_RATE;
            sim.getEnvironment("vegf").setVal(loc, newVEGF);
        }
    }

    @Override
    public void updateModule(Module mod, double f)  { }
}
