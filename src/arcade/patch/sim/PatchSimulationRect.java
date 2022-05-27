package arcade.patch.sim;

import arcade.core.sim.Series;
import arcade.patch.agent.cell.PatchCellFactory;
import arcade.patch.env.loc.PatchLocationFactory;
import arcade.patch.env.loc.PatchLocationFactoryRect;
import arcade.patch.env.loc.PatchLocationRect;

/**
 * Extension of {@link PatchSimulation} for rectangular geometry.
 */

public final class PatchSimulationRect extends PatchSimulation {
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
}
