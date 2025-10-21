package arcade.potts.agent.module;

import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.potts.agent.cell.PottsCell;

/** Abstract extention of {@link PottsModule} for proliferation modules. */
public abstract class PottsModuleProliferation extends PottsModule {

    /**
     * Creates a proliferation module.
     *
     * @param cell the cell to which this module is attached
     */
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
