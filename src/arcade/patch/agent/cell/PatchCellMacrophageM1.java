package arcade.patch.agent.cell;

import sim.engine.SimState;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellContainer;
import arcade.core.agent.cell.CellState;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.GrabBag;
import arcade.core.util.Parameters;
import arcade.patch.util.PatchEnums;

public class PatchCellMacrophageM1 extends PatchCellMacrophage {

    /** Average time that macrophage is bound to target [min]. */
    private double boundTime;

    /**
     * Creates a tissue {@code PatchCell} agent.
     *
     * @param container the cell container
     * @param location the {@link Location} of the cell
     * @param parameters the dictionary of parameters
     */
    public PatchCellMacrophageM1(
            PatchCellContainer container, Location location, Parameters parameters) {
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
    public PatchCellMacrophageM1(
            PatchCellContainer container, Location location, Parameters parameters, GrabBag links) {
        super(container, location, parameters, links);
        this.boundTime = 360;
    }

    @Override
    public CellContainer make(int newID, CellState newState, MersenneTwisterFast random) {
        divisions++;
        return new PatchCellContainer(
                newID,
                id,
                pop,
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

        age++;

        // unbind from target if delay is reached
        // otherwise increment if still bound
        if (ticker > boundTime) {
            this.unbind();
        } else if (this.bindingFlag == PatchEnums.AntigenFlag.BOUND_ANTIGEN) {
            ticker++;
        }

        if (state != PatchEnums.State.APOPTOTIC && age > apoptosisAge) {
            setState(PatchEnums.State.APOPTOTIC);
            this.unbind();
        }

        processes.get(PatchEnums.Domain.METABOLISM).step(simstate.random, sim);

        // Check energy status. If cell has less energy than threshold, it will
        // necrose. If overall energy is negative, then cell enters quiescence.
        if (state != PatchEnums.State.APOPTOTIC && energy < 0) {
            if (energy < energyThreshold) {
                if (simstate.random.nextDouble() > necroticFraction) {
                    setState(PatchEnums.State.APOPTOTIC);
                    this.unbind();
                } else {
                    setState(PatchEnums.State.NECROTIC);
                    this.unbind();
                }
            } else if (state != PatchEnums.State.QUIESCENT && state != PatchEnums.State.SENESCENT) {
                setState(PatchEnums.State.QUIESCENT);
            }
        }

        processes.get(PatchEnums.Domain.QUORUM).step(simstate.random, sim);

        if (boundCell == null) {
            checkForBinding(simstate);
        } else {
            calculateBindingProb(simstate.random);
        }

        if (state == PatchEnums.State.QUIESCENT || state == PatchEnums.State.UNDEFINED) {
            if (simstate.random.nextDouble() > 0.5) {
                setState(PatchEnums.State.MIGRATORY);
            } else if (divisions == divisionPotential) {
                if (simstate.random.nextDouble() > senescentFraction) {
                    setState(PatchEnums.State.APOPTOTIC);
                } else {
                    setState(PatchEnums.State.SENESCENT);
                }
            }
        }

        if (module != null) {
            module.step(simstate.random, sim);
        }
    }
}
