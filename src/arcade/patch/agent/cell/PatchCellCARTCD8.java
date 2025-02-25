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
import arcade.patch.util.PatchEnums.State;

/** Extension of {@link PatchCellCART} for CD8 CART-cells with selected module versions. */
public class PatchCellCARTCD8 extends PatchCellCART {
    /**
     * Creates a T cell {@code PatchCellCARTCD8} agent. *
     *
     * @param container the cell container
     * @param location the {@link Location} of the cell
     * @param parameters the dictionary of parameters
     */
    public PatchCellCARTCD8(
            PatchCellContainer container, Location location, Parameters parameters) {
        this(container, location, parameters, null);
    }

    /**
     * Creates a T cell {@code PatchCellCARTCD8} agent. *
     *
     * @param container the cell container
     * @param location the {@link Location} of the cell
     * @param parameters the dictionary of parameters
     * @param links the map of population links
     */
    public PatchCellCARTCD8(
            PatchCellContainer container, Location location, Parameters parameters, GrabBag links) {
        super(container, location, parameters, links);
    }

    @Override
    public PatchCellContainer make(int newID, CellState newState, MersenneTwisterFast random) {
        divisions--;
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

        // Increase age of cell.
        super.age++;

        if (state != State.APOPTOTIC && age > apoptosisAge) {
            setState(State.APOPTOTIC);
            super.unbind();
            this.activated = false;
        }

        // Increase time since last active ticker
        super.lastActiveTicker++;
        if (super.lastActiveTicker != 0 && super.lastActiveTicker % 1440 == 0) {
            if (super.boundCARAntigensCount != 0) {
                super.boundCARAntigensCount--;
            }
        }
        if (super.lastActiveTicker / 1440 > 7) {
            super.activated = false;
        }

        // Step metabolism process.
        super.processes.get(Domain.METABOLISM).step(simstate.random, sim);

        // Check energy status. If cell has less energy than threshold, it will
        // apoptose. If overall energy is negative, then cell enters quiescence.
        if (state != State.APOPTOTIC) {
            if (super.energy < super.energyThreshold) {

                super.setState(State.APOPTOTIC);
                super.unbind();
                this.activated = false;
            } else if (state != State.ANERGIC
                    && state != State.SENESCENT
                    && state != State.EXHAUSTED
                    && state != State.STARVED
                    && energy < 0) {

                super.setState(State.STARVED);
                super.unbind();
            } else if (state == State.STARVED && energy >= 0) {
                super.setState(State.UNDEFINED);
            }
        }

        // Step inflammation process.
        super.processes.get(Domain.INFLAMMATION).step(simstate.random, sim);

        // Change state from undefined.
        if (super.state == State.UNDEFINED || super.state == State.PAUSED) {
            if (divisions == 0) {
                if (simstate.random.nextDouble() > super.senescentFraction) {
                    super.setState(State.APOPTOTIC);
                } else {
                    super.setState(State.SENESCENT);
                }
                super.unbind();
                this.activated = false;
            } else {
                // Cell attempts to bind to a target
                PatchCellTissue target = super.bindTarget(sim, location, simstate.random);
                super.boundTarget = target;

                // If cell is bound to both antigen and self it will become anergic.
                if (super.getBindingFlag() == AntigenFlag.BOUND_ANTIGEN_CELL_RECEPTOR) {
                    if (simstate.random.nextDouble() > super.anergicFraction) {
                        super.setState(State.APOPTOTIC);
                    } else {
                        super.setState(State.ANERGIC);
                    }
                    super.unbind();
                    this.activated = false;
                } else if (super.getBindingFlag() == AntigenFlag.BOUND_ANTIGEN) {
                    // If cell is only bound to target antigen, the cell
                    // can potentially become properly activated.

                    // Check overstimulation. If cell has bound to
                    // target antigens too many times, becomes exhausted.
                    if (boundCARAntigensCount > maxAntigenBinding) {
                        if (simstate.random.nextDouble() > super.exhaustedFraction) {
                            super.setState(State.APOPTOTIC);
                        } else {
                            super.setState(State.EXHAUSTED);
                        }
                        super.unbind();
                        this.activated = false;
                    } else {
                        // if CD8 cell is properly activated, it can be cytotoxic
                        this.lastActiveTicker = 0;
                        this.activated = true;
                        super.setState(State.CYTOTOXIC);
                    }
                } else {
                    // If self binding, unbind
                    if (super.getBindingFlag() == AntigenFlag.BOUND_CELL_RECEPTOR) {
                        super.unbind();
                    }
                    // Check activation status. If cell has been activated before,
                    // it will proliferate. If not, it will migrate.
                    if (activated) {
                        super.setState(State.PROLIFERATIVE);
                    } else {
                        if (simstate.random.nextDouble() > super.proliferativeFraction) {
                            super.setState(State.MIGRATORY);
                        } else {
                            super.setState(State.PROLIFERATIVE);
                        }
                    }
                }
            }
        }

        // Step the module for the cell state.
        if (super.module != null) {
            super.module.step(simstate.random, sim);
        }
    }
}
