package arcade.agent.module;

import arcade.agent.cell.Cell;
import arcade.sim.Simulation;

public class SensingRandom extends Sensing {
    double rateRandom;

    public SensingRandom(Cell c, Simulation sim) {
        super(c, sim);
        rateRandom = c.getParams().get("VEGF_RAND").nextDouble();
        c.getParams().put("VEGF_RAND", c.getParams().get("VEGF_RAND").update(rateRandom));
    }
    

    @Override
    public void stepModule(Simulation sim) {
        Double VEGF = sim.getEnvironment("VEGF").getAverageVal(loc);
        sim.getEnvironment("VEGF").setVal(loc, VEGF + sim.getRandom() * rateRandom);
    }

    @Override
    public void updateModule(Module mod, double f)  { }
}

