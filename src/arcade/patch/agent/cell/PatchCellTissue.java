package arcade.patch.agent.cell;

import sim.engine.SimState;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellState;
import arcade.core.agent.process.Process;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.GrabBag;
import arcade.core.util.Parameters;
import arcade.patch.util.PatchEnums.Domain;
import arcade.patch.util.PatchEnums.Flag;
import arcade.patch.util.PatchEnums.State;

/**
 * Extension of {@link PatchCell} for healthy tissue cells. *
 *
 * <p>{@code PatchCellTissue} agents exist in one of seven states: undefined, apoptotic, quiescent,
 * migratory, proliferative, senescent, and necrotic. The undefined state is a transition state for
 * "undecided" cells, and does not have any biological analog.
 *
 * <p>{@code PatchCellTissue} agents have two required {@link Process} domains: metabolism and
 * signaling. Metabolism controls changes in cell energy and volume. Signaling controls the
 * proliferative vs. migratory decision.
 *
 * <p>General order of rules for the {@code PatchCellTissue} step:
 *
 * <ul>
 *   <li>update age
 *   <li>check lifespan (possible change to apoptotic)
 *   <li>step metabolism process
 *   <li>check energy status (possible change to quiescent or necrotic depending on {@code
 *       ENERGY_THRESHOLD})
 *   <li>step signaling process
 *   <li>check if neutral (change to proliferative, migratory, senescent)
 *   <li>step state-specific module
 * </ul>
 */
public class PatchCellTissue extends PatchCell {
    /** Cell surface antigen count. */
    private final int carAntigens;

    /** Cell surface PDL1 count. */
    private final int selfTargets;

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
        carAntigens = parameters.getInt("CAR_ANTIGENS");
        selfTargets = parameters.getInt("SELF_TARGETS");
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

    @Override
    public void step(SimState simstate) {
        Simulation sim = (Simulation) simstate;
        // Increase age of cell.
        age++;

        if (state != State.APOPTOTIC && age > apoptosisAge) {
            setState(State.APOPTOTIC);
        }

        // Step metabolism process.
        processes.get(Domain.METABOLISM).step(simstate.random, sim);

        // Check energy status. If cell has less energy than threshold, it will
        // necrose. If overall energy is negative, then cell enters quiescence.
        if (state != State.APOPTOTIC && energy < 0) {
            if (energy < energyThreshold) {
                if (simstate.random.nextDouble() > necroticFraction) {
                    setState(State.APOPTOTIC);
                } else {
                    setState(State.NECROTIC);
                }
            } else if (state != State.QUIESCENT && state != State.SENESCENT) {
                setState(State.QUIESCENT);
            }
        }

        // Step signaling network process.
        processes.get(Domain.SIGNALING).step(simstate.random, sim);

        // Change state from undefined.
        if (state == State.UNDEFINED) {
            if (flag == Flag.MIGRATORY) {
                setState(State.MIGRATORY);
            } else if (divisions == 0) {
                if (simstate.random.nextDouble() > senescentFraction) {
                    setState(State.APOPTOTIC);
                } else {
                    setState(State.SENESCENT);
                }
            } else {
                setState(State.PROLIFERATIVE);
            }
        }

        // Step the module for the cell state.
        if (module != null) {
            module.step(simstate.random, sim);
        }
    }

    /**
     * Returns the number of CAR antigens on this cell.
     *
     * @return the number of CAR antigens on this cell.
     */
    public int getCarAntigens() {
        return carAntigens;
    }

    /**
     * Returns the number of self receptor antigens on this cell.
     *
     * @return the number of self receptor antigens on this cell.
     */
    public int getSelfAntigens() {
        return selfTargets;
    }
}
