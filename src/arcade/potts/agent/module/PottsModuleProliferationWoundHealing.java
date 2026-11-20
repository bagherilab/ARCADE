package arcade.potts.agent.module;

import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.potts.agent.cell.PottsCellWoundHealing;
import static arcade.potts.util.PottsEnums.Phase;
import static arcade.potts.util.PottsEnums.State;

/**
 * Extension of {@link PottsModuleProliferationWithCellCycleCheckSimple} for wound healing cells.
 *
 * <p>During G1, the cell returns to the quiescent state if it can no longer proliferate. Once the
 * cell passes G1 (the restriction point), it is committed to dividing.
 */
public class PottsModuleProliferationWoundHealing
        extends PottsModuleProliferationWithCellCycleCheckSimple {
    /**
     * Creates a wound healing proliferation {@code Module} for the given {@link
     * PottsCellWoundHealing}.
     *
     * @param cell the {@link PottsCellWoundHealing} the module is associated with
     */
    public PottsModuleProliferationWoundHealing(PottsCellWoundHealing cell) {
        super(cell);
    }

    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {
        if (phase == Phase.PROLIFERATIVE_G1
                && !((PottsCellWoundHealing) cell).canProliferate(sim)) {
            cell.setState(State.QUIESCENT);
            return;
        }

        super.step(random, sim);
    }
}
