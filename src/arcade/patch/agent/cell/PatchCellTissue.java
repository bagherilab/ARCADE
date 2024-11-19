package arcade.patch.agent.cell;

import sim.engine.SimState;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellState;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.GrabBag;
import arcade.core.util.Parameters;
import arcade.patch.util.PatchEnums.AntigenFlag;
import arcade.patch.util.PatchEnums.Domain;
import arcade.patch.util.PatchEnums.Flag;
import arcade.patch.util.PatchEnums.State;

/** Extension of {@link PatchCell} for healthy tissue cells. */
public class PatchCellTissue extends PatchCell {
    // /** Fraction of necrotic cells that become apoptotic. */
    // private final double necroticFraction;

    // /** Fraction of senescent cells that become apoptotic. */
    // private final double senescentFraction;

    // these two variables are public bc I don't want to implement setter/getter methods for sims
    // that do not use CART cells.

    /** Cell surface antigen count */
    int carAntigens;

    /** Cell surface PDL1 count */
    int selfTargets;

    /** Cell binding flag */
    public AntigenFlag binding;

    /**
     * Creates a tissue {@code PatchCell} agent.
     *
     * @param container the cell container
     * @param location the {@link Location} of the cell
     * @param parameters the dictionary of parameters
     */
    public PatchCellTissue(PatchCellContainer container, Location location, Parameters parameters) {
        this(container, location, parameters, null);
    }

    /**
     * Creates a tissue {@code PatchCell} agent with population links.
     *
     * @param container the cell container
     * @param location the {@link Location} of the cell
     * @param parameters the dictionary of parameters
     * @param links the map of population links
     */
    public PatchCellTissue(
            PatchCellContainer container, Location location, Parameters parameters, GrabBag links) {
        super(container, location, parameters, links);
        carAntigens = parameters.getInt("CAR_ANTIGENS_HEALTHY");
        selfTargets = parameters.getInt("SELF_TARGETS");
        this.binding = AntigenFlag.UNDEFINED;
    }

    @Override
    public PatchCellContainer make(int newID, CellState newState, MersenneTwisterFast random) {
        divisions--;
        int newPop = links == null ? pop : links.next(random);
        return new PatchCellContainer(
                newID,
                id,
                newPop,
                age,
                divisions,
                newState,
                volume,
                height,
                criticalVolume,
                criticalHeight);
    }

    /* consider making PatchCell parameters protected instead of private */

    @Override
    public void step(SimState simstate) {
        Simulation sim = (Simulation) simstate;

        // Increase age of cell.
        super.age++;

        // TODO: check for death due to age

        // Step metabolism process.
        super.processes.get(Domain.METABOLISM).step(simstate.random, sim);

        // Check energy status. If cell has less energy than threshold, it will
        // necrose. If overall energy is negative, then cell enters quiescence.
        if (state != State.APOPTOTIC && energy < 0) {
            if (super.energy < super.energyThreshold) {
                if (simstate.random.nextDouble() > necroticFraction) {
                    super.setState(State.APOPTOTIC);
                } else {
                    super.setState(State.NECROTIC);
                }
            } else if (state != State.QUIESCENT && state != State.SENESCENT) {
                super.setState(State.QUIESCENT);
            }
        }

        // Step signaling network process.
        super.processes.get(Domain.SIGNALING).step(simstate.random, sim);

        // Change state from undefined.
        if (super.state == State.UNDEFINED) {
            if (super.flag == Flag.MIGRATORY) {
                super.setState(State.MIGRATORY);
            } else if (super.divisions == 0) {
                if (simstate.random.nextDouble() > senescentFraction) {
                    super.setState(State.APOPTOTIC);
                } else {
                    super.setState(State.SENESCENT);
                }
            } else {
                super.setState(State.PROLIFERATIVE);
            }
        }

        // Step the module for the cell state.
        if (super.module != null) {
            super.module.step(simstate.random, sim);
        }
    }
}
