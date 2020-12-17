package arcade.potts.sim;

import arcade.core.sim.Series;
import arcade.potts.agent.cell.PottsCellFactory;
import arcade.potts.env.loc.PottsLocationFactory;
import arcade.potts.env.loc.PottsLocationFactory3D;

/**
 * Extension of {@link PottsSimulation} for 3D.
 */

public final class PottsSimulation3D extends PottsSimulation {
    public PottsSimulation3D(long seed, Series series) { super(seed, series); }
    
    @Override
    Potts makePotts() { return new Potts3D(series); }
    
    @Override
    PottsLocationFactory makeLocationFactory() {
        return new PottsLocationFactory3D();
    }
    
    @Override
    PottsCellFactory makeCellFactory() {
        return new PottsCellFactory();
    }
}
