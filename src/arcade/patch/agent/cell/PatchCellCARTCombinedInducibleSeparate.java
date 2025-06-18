package arcade.patch.agent.cell;

import java.util.logging.Logger;
import sim.engine.SimState;
import ec.util.MersenneTwisterFast;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.GrabBag;
import arcade.core.util.Parameters;
import arcade.patch.util.PatchEnums;

/** Extension of {@link PatchCellCARTCombinedCombinatorial} for induced combinatorial circuit. */
public class PatchCellCARTCombinedInducibleSeparate extends PatchCellCARTCombinedCombinatorial {

    /** Logger for this class. */
    private static final Logger LOGGER =
            Logger.getLogger(PatchCellCARTCombinedInducibleSeparate.class.getName());

    /**
     * Creates a tissue {@code PatchCellCombinedInducibleSeparate} agent. *
     *
     * @param location the {@link Location} of the cell
     * @param container the cell container
     * @param parameters the dictionary of parameters
     */
    public PatchCellCARTCombinedInducibleSeparate(
            PatchCellContainer container, Location location, Parameters parameters) {
        this(container, location, parameters, null);
    }

    /**
     * Creates a T cell {@code PatchCellCombinedInducibleSeparate} agent. *
     *
     * @param container the cell container
     * @param location the {@link Location} of the cell
     * @param parameters the dictionary of parameters
     * @param links the map of population links
     */
    public PatchCellCARTCombinedInducibleSeparate(
            PatchCellContainer container, Location location, Parameters parameters, GrabBag links) {
        super(container, location, parameters, links);
        cars = 0;
    }

    @Override
    public void step(SimState simstate) {
        Simulation sim = (Simulation) simstate;
        if (super.boundCell == null) {
            super.checkForBinding(simstate);
        } else {
            calculateActivation(simstate.random, sim);
        }

        super.age++;

        if (state != PatchEnums.State.APOPTOTIC && age > apoptosisAge) {
            setState(PatchEnums.State.APOPTOTIC);
            super.unbind();
            this.activated = false;
        }

        super.lastActiveTicker++;

        if (super.lastActiveTicker != 0 && super.lastActiveTicker % MINUTES_IN_DAY == 0) {
            if (super.boundCARAntigensCount != 0) {
                super.boundCARAntigensCount--;
            }
        }
        if (super.lastActiveTicker / MINUTES_IN_DAY >= 7) {
            super.activated = false;
        }

        super.processes.get(PatchEnums.Domain.METABOLISM).step(simstate.random, sim);

        // Check energy status. If cell has less energy than threshold, it will
        // apoptose. If overall energy is negative, then cell enters quiescence.
        if (state != PatchEnums.State.APOPTOTIC) {
            if (super.energy < super.energyThreshold) {

                super.setState(PatchEnums.State.APOPTOTIC);
                super.unbind();
                this.activated = false;
            } else if (state != PatchEnums.State.ANERGIC
                    && state != PatchEnums.State.SENESCENT
                    && state != PatchEnums.State.EXHAUSTED
                    && state != PatchEnums.State.STARVED
                    && energy < 0) {

                super.setState(PatchEnums.State.STARVED);
                super.unbind();
            } else if (state == PatchEnums.State.STARVED && energy >= 0) {
                super.setState(PatchEnums.State.UNDEFINED);
            }
        }

        super.processes.get(PatchEnums.Domain.INFLAMMATION).step(simstate.random, sim);

        if (super.state == PatchEnums.State.UNDEFINED || super.state == PatchEnums.State.PAUSED) {
            if (divisions == divisionPotential) {
                if (simstate.random.nextDouble() > super.senescentFraction) {
                    super.setState(PatchEnums.State.APOPTOTIC);
                } else {
                    super.setState(PatchEnums.State.SENESCENT);
                }
                super.unbind();
                this.activated = false;
            } else {
                PatchCellTissue target = super.bindTarget(sim, location, simstate.random);
                super.boundTarget = target;

                // If cell is bound to both antigen and self it will become anergic.
                if (super.getBindingFlag() == PatchEnums.AntigenFlag.BOUND_ANTIGEN_CELL_RECEPTOR) {
                    if (simstate.random.nextDouble() > super.anergicFraction) {
                        super.setState(PatchEnums.State.APOPTOTIC);
                    } else {
                        super.setState(PatchEnums.State.ANERGIC);
                    }
                    super.unbind();
                    this.activated = false;
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
                        super.unbind();
                        this.activated = false;
                    } else {
                        // if CD8 cell is properly activated, it can be cytotoxic
                        super.setState(PatchEnums.State.CYTOTOXIC);
                    }
                } else {
                    // If self binding, unbind
                    if (super.getBindingFlag() == PatchEnums.AntigenFlag.BOUND_CELL_RECEPTOR) {
                        super.unbind();
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
    }

    /**
     * Calculates T cell activation given bound synnotchs. *
     *
     * @param random the random object
     * @param sim the simulation instance
     */
    protected void calculateActivation(MersenneTwisterFast random, Simulation sim) {
        int TAU = 60;
        super.calculateCARS(random, sim);
        cars =
                Math.max(
                        (int)
                                (cars
                                        + (basalCARGenerationRate * TAU)
                                        - (carDegradationConstant * cars * TAU)),
                        0);
        if (boundSynNotch >= synNotchThreshold) {
            this.lastActiveTicker = 0;
            this.activated = true;
        }
    }
}
