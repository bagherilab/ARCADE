package arcade.agent.module;

import arcade.agent.cell.Cell;
import arcade.sim.Simulation;

public class SensingProportional extends Sensing {
    double proportionalFactor;

    public SensingProportional(Cell c, Simulation sim) {
        super(c, sim);
        proportionalFactor = c.getParams().get("VEGF_PROP").nextDouble();
        c.getParams().put("VEGF_PROP", c.getParams().get("VEGF_PROP").update(proportionalFactor));
    }
    
    @Override
    public void stepModule(Simulation sim) {
        Double O2 = sim.getEnvironment("oxygen").getAverageVal(loc);
        Double VEGF = sim.getEnvironment("vegf").getAverageVal(loc);
        Double newVEGF = VEGF + proportionalFactor * O2;
        sim.getEnvironment("vegf").setVal(loc, newVEGF);

    }

    @Override
    public void updateModule(Module mod, double f)  { }
}
