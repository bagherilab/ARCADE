package arcade.patch.sim;

import arcade.core.sim.Series;
import arcade.patch.agent.cell.PatchCellFactory;
import arcade.patch.env.loc.PatchLocationFactory;
import arcade.patch.env.loc.PatchLocationFactoryHex;
import arcade.patch.env.loc.PatchLocationHex;

/**
 * Extension of {@link PatchSimulation} for hexagonal geometry.
 */

public final class PatchSimulationHex extends PatchSimulation {
    public PatchSimulationHex(long seed, Series series) {
        super(seed, series);
        PatchLocationHex.updateConfigs((PatchSeries) series);
    }
    
    @Override
    PatchLocationFactory makeLocationFactory() {
        return new PatchLocationFactoryHex();
    }

    @Override
    PatchCellFactory makeCellFactory() {
        return new PatchCellFactory();
    }
}
