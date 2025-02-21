package arcade.potts.agent.module;

import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.potts.agent.cell.PottsCellFlyNeuron;
import arcade.potts.util.PottsEnums.Phase;

/** Quiescence module for fly neuron cells. */
public final class PottsModuleFlyNeuronQuiescence extends PottsModuleQuiescence {

    /**
     * Creates a quiescence {@code Module} for the given {@link PottsCellFlyNeuron}.
     *
     * @param cell the {@link PottsCellFlyNeuron} the module is associated with
     */
    public PottsModuleFlyNeuronQuiescence(PottsCellFlyNeuron cell) {
        super(cell);
        setPhase(Phase.UNDEFINED);
    }

    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {
        // Do nothing
    }
}
