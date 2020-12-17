package arcade.potts.sim;

import arcade.core.sim.Series;
import arcade.potts.agent.cell.PottsCellFactory;
import arcade.potts.env.loc.PottsLocationFactory;
import arcade.potts.env.loc.PottsLocationFactory2D;

/**
 * Extension of {@link PottsSimulation} for 2D.
 */

public final class PottsSimulation2D extends PottsSimulation {
    public PottsSimulation2D(long seed, Series series) { super(seed, series); }
    
    @Override
    Potts makePotts() { return new Potts2D(series); }
    
    @Override
    PottsLocationFactory makeLocationFactory() {
        return new PottsLocationFactory2D();
    }
    
    @Override
    PottsCellFactory makeCellFactory() {
        return new PottsCellFactory();
    }
}
