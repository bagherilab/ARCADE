package arcade.potts.sim;

import arcade.core.sim.Series;
import arcade.potts.agent.cell.PottsCellFactory;
import arcade.potts.env.loc.PottsLocationFactory;
import arcade.potts.env.loc.PottsLocationFactory2D;

public class PottsSimulation2D extends PottsSimulation {
    public PottsSimulation2D(long seed, Series series) { super(seed, series); }
    
    Potts makePotts() { return new Potts2D(series); }
    
    PottsLocationFactory makeLocationFactory() {
        return new PottsLocationFactory2D();
    }
    
    PottsCellFactory makeCellFactory() {
        return new PottsCellFactory();
    }
}
