package arcade.patch.sim;

import arcade.core.sim.Series;
import arcade.patch.agent.cell.PatchCellFactory;
import arcade.patch.env.lat.PatchLatticeFactory;
import arcade.patch.env.lat.PatchLatticeFactoryRect;
import arcade.patch.env.loc.PatchLocationFactory;
import arcade.patch.env.loc.PatchLocationFactoryRect;
import arcade.patch.env.loc.PatchLocationRect;

/**
 * Extension of {@link PatchSimulation} for rectangular geometry.
 */

public final class PatchSimulationRect extends PatchSimulation {
    /**
     * Rectangular simulation instance for a {@link Series} for given random seed.
     *
     * @param seed  the random seed for random number generator
     * @param series  the simulation series
     */
    public PatchSimulationRect(long seed, Series series) {
        super(seed, series);
        PatchLocationRect.updateConfigs((PatchSeries) series);
    }
    
    @Override
    PatchLocationFactory makeLocationFactory() {
        return new PatchLocationFactoryRect();
    }
    
    @Override
    PatchCellFactory makeCellFactory() {
        return new PatchCellFactory();
    }
    
    @Override
    PatchLatticeFactory makeLatticeFactory() {
        return new PatchLatticeFactoryRect();
    }
}
