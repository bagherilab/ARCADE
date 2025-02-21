package arcade.potts.agent.module;

import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.util.PottsEnums.Phase;

public class PottsModuleFlyNeuronQuiescence extends PottsModuleQuiescence {

    /**
     * Creates a quiescence {@code Module} for the given {@link PottsCell}.
     *
     * @param cell the {@link PottsCell} the module is associated with
     */
    public PottsModuleFlyNeuronQuiescence(PottsCell cell) {
        super(cell);
        setPhase(Phase.UNDEFINED);
    }

    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {
        // Do nothing
    }
}
