package arcade.potts.agent.module;

import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.potts.agent.cell.PottsCell;

public abstract class PottsModuleProliferation extends PottsModule {

    public PottsModuleProliferation(PottsCell cell) {
        super(cell);
    }

    /**
     * Adds a cell to the simulation.
     *
     * @param random the random number generator
     * @param sim the simulation instance
     */
    abstract void addCell(MersenneTwisterFast random, Simulation sim);
}
