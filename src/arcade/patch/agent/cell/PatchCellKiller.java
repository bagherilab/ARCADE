package arcade.patch.agent.cell;

import sim.engine.SimState;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.GrabBag;
import arcade.core.util.Parameters;
import arcade.patch.util.PatchEnums;

public class PatchCellKiller extends PatchCellCARTCD8 {

    /** CAR Association rate constant for receptor/antigen binding [molecules per minute]. */
    protected double CARBindingRate;

    /** PLD1 association rate constant for receptor/antigen binding [molecules per minute]. */
    protected double selfBindingRate;

    public PatchCellKiller(PatchCellContainer container, Location location, Parameters parameters) {
        super(container, location, parameters);
    }

    public PatchCellKiller(
            PatchCellContainer container, Location location, Parameters parameters, GrabBag links) {
        super(container, location, parameters, links);
        this.CARBindingRate = parameters.getDouble("CAR_ANTIGEN_BINDING_RATE");
        this.selfBindingRate = parameters.getDouble("SELF_ANTIGEN_BINDING_RATE");
    }

    @Override
    public void step(SimState simstate) {
        Simulation sim = (Simulation) simstate;

        // Increase age of cell.
        super.age++;

        if (state != PatchEnums.State.APOPTOTIC && age > apoptosisAge) {
            setState(PatchEnums.State.APOPTOTIC);
            this.unbind();
        }

        // Increase time since last active ticker
        super.lastActiveTicker++;
        if (super.lastActiveTicker != 0 && super.lastActiveTicker % 1440 == 0) {
            if (super.boundCARAntigensCount != 0) super.boundCARAntigensCount--;
        }
        if (super.lastActiveTicker / 1440 > 7) super.activated = false;

        // Step metabolism process.
        super.processes.get(PatchEnums.Domain.METABOLISM).step(simstate.random, sim);

        // Check energy status. If cell has less energy than threshold, it will
        // apoptose. If overall energy is negative, then cell enters quiescence.
        if (state != PatchEnums.State.APOPTOTIC) {
            if (super.energy < super.energyThreshold) {

                super.setState(PatchEnums.State.APOPTOTIC);
                this.unbind();
                this.activated = false;
            } else if (state != PatchEnums.State.ANERGIC
                    && state != PatchEnums.State.SENESCENT
                    && state != PatchEnums.State.EXHAUSTED
                    && state != PatchEnums.State.STARVED
                    && energy < 0) {

                super.setState(PatchEnums.State.STARVED);
                this.unbind();
            } else if (state == PatchEnums.State.STARVED && energy >= 0) {
                super.setState(PatchEnums.State.UNDEFINED);
            }
        }

        // Step quorum sensing process.
        super.processes.get(PatchEnums.Domain.QUORUM).step(simstate.random, sim);

        // Step inflammation process.
        super.processes.get(PatchEnums.Domain.INFLAMMATION).step(simstate.random, sim);

        // Change state from undefined.
        if (super.state == PatchEnums.State.UNDEFINED || super.state == PatchEnums.State.PAUSED) {
            if (divisions == 0) {
                if (simstate.random.nextDouble() > super.senescentFraction) {
                    super.setState(PatchEnums.State.APOPTOTIC);
                } else {
                    super.setState(PatchEnums.State.SENESCENT);
                }
                this.unbind();
            } else {
                // Cell attempts to bind to a target
                this.boundTarget = super.bindTarget(sim, location, simstate.random);
                // If cell is bound to both antigen and self it will become anergic.
                if (super.getBindingFlag() == PatchEnums.AntigenFlag.BOUND_ANTIGEN_CELL_RECEPTOR) {
                    if (simstate.random.nextDouble() > super.anergicFraction) {
                        super.setState(PatchEnums.State.APOPTOTIC);
                    } else {
                        super.setState(PatchEnums.State.ANERGIC);
                    }
                    this.unbind();
                } else if (super.getBindingFlag() == PatchEnums.AntigenFlag.BOUND_ANTIGEN) {
                    // If cell is only bound to target antigen, the cell
                    // can potentially become properly activated.

                    // Check overstimulation. If cell has bound to
                    // target antigens too many times, becomes exhausted.
                    if (boundCARAntigensCount > maxAntigenBinding) {
                        if (simstate.random.nextDouble() > super.exhaustedFraction) {
                            super.setState(PatchEnums.State.APOPTOTIC);
                        } else {
                            super.setState(PatchEnums.State.EXHAUSTED);
                        }
                        this.unbind();
                    } else {
                        // if CD8 cell is properly activated, it can be cytotoxic
                        if (activated) {
                            super.setState(PatchEnums.State.CYTOTOXIC);
                        }
                    }
                } else {
                    // If self binding, unbind
                    if (super.getBindingFlag() == PatchEnums.AntigenFlag.BOUND_CELL_RECEPTOR) {
                        this.unbind();
                    }
                    // Check activation status. If cell has been activated before,
                    // it will proliferate. If not, it will migrate.
                    if (activated) {
                        super.setState(PatchEnums.State.PROLIFERATIVE);
                    } else {
                        if (simstate.random.nextDouble() > super.proliferativeFraction) {
                            super.setState(PatchEnums.State.MIGRATORY);
                        } else {
                            super.setState(PatchEnums.State.PROLIFERATIVE);
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

    /** Sets binding flag to unbound and binding target to null. */
    public void unbind() {
        if (this.bindingFlag == PatchEnums.AntigenFlag.BOUND_ANTIGEN) {
            super.setBindingFlag(PatchEnums.AntigenFlag.UNBOUND);
            this.boundTarget = null;
            this.boundCARAntigensCount--;
        }
    }
}
