package arcade.potts.sim;

import arcade.core.sim.Series;
import arcade.potts.agent.cell.PottsCellFactory;
import arcade.potts.env.loc.PottsLocationFactory;
import arcade.potts.env.loc.PottsLocationFactory3D;

public class PottsSimulation3D extends PottsSimulation {
    public PottsSimulation3D(long seed, Series series) { super(seed, series); }
    
    Potts makePotts() { return new Potts3D(series); }
    
    PottsLocationFactory makeLocationFactory() {
        return new PottsLocationFactory3D();
    }
    
    PottsCellFactory makeCellFactory() {
        return new PottsCellFactory();
    }
}
