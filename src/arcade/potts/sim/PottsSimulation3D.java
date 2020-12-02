package arcade.potts.sim;

import arcade.core.sim.Series;
import arcade.potts.agent.cell.*;
import arcade.potts.env.loc.*;

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