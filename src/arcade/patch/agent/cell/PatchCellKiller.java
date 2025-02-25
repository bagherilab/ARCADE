package arcade.patch.agent.cell;

import sim.engine.SimState;
import sim.util.Bag;
import sim.util.distribution.Poisson;
import ec.util.MersenneTwisterFast;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.GrabBag;
import arcade.core.util.Parameters;
import arcade.patch.env.grid.PatchGrid;
import arcade.patch.env.location.PatchLocation;
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
                this.checkForBinding(simstate);

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

    /**
     * Determines if a binding event occurs at the current time step.
     *
     * @param simstate the current state of the simulation
     */
    protected void checkForBinding(SimState simstate) {
        Simulation sim = (Simulation) simstate;
        PatchGrid grid = (PatchGrid) sim.getGrid();

        Bag allAgents = grabAllTissueNeighbors(grid, location);

        if (!allAgents.isEmpty()) {
            PatchCellTissue target =
                    (PatchCellTissue) allAgents.get(simstate.random.nextInt(allAgents.size()));
            double carProbability =
                    calculateProbability(
                            ((PatchCellTissue) target).getCarAntigens(),
                            CARBindingRate,
                            simstate.random);
            double selfProbability =
                    calculateProbability(
                            ((PatchCellTissue) target).getSelfAntigens(),
                            selfBindingRate,
                            simstate.random);
            double randomCar = simstate.random.nextDouble();
            double randomSelf = simstate.random.nextDouble();
            if (carProbability > randomCar && selfProbability < randomSelf) {
                this.boundTarget = target;
                this.bindingFlag = PatchEnums.AntigenFlag.BOUND_ANTIGEN;
                this.boundCARAntigensCount++;
                updateSelfReceptors();
            } else if (carProbability > randomCar && selfProbability > randomSelf) {
                this.boundTarget = target;
                this.bindingFlag = PatchEnums.AntigenFlag.BOUND_ANTIGEN_CELL_RECEPTOR;
                this.boundCARAntigensCount++;
                boundSelfAntigensCount++;
                updateSelfReceptors();
            } else if (carProbability < randomCar && selfProbability > randomSelf) {
                this.boundTarget = target;
                super.setBindingFlag(PatchEnums.AntigenFlag.BOUND_CELL_RECEPTOR);
                boundSelfAntigensCount++;
            } else {
                this.boundTarget = null;
                super.setBindingFlag(PatchEnums.AntigenFlag.UNBOUND);
            }
        }
    }

    /**
     * Calculates probability of binding using a poisson distribution.
     *
     * @param antigens the number of antigens on target cell surface
     * @param bindingRate the affinity of the antigen to the receptor
     * @param random the random number generator
     * @return probability that at least one binding event occurs
     */
    private double calculateProbability(
            double antigens, double bindingRate, MersenneTwisterFast random) {
        double bindingEvent = antigens * bindingRate;
        double timeInterval = computeTimeInterval(bindingEvent, random);
        Poisson distribution = new Poisson(timeInterval, random);
        // calculate probability of 1 or more events occurring in this time step
        return 1 - distribution.pdf(0);
    }

    /**
     * Calculates time interval for an event to occur according to kinetic monte carlo algorithm.
     *
     * @param rate the rate of the event
     * @param random the random number generator
     * @return the time interval for the event and given rate
     */
    private double computeTimeInterval(double rate, MersenneTwisterFast random) {
        double randomSample = 1 / random.nextDouble();
        return Math.log(randomSample) / rate;
    }

    /**
     * Adds all tissue agents at a given location in the input bag.
     *
     * @param tissueAgents the bag to add the tissue agents to
     * @param potentialTargets all agents at the location
     */
    private void getTissueAgents(Bag tissueAgents, Bag potentialTargets) {
        for (Object cell : potentialTargets) {
            if (cell instanceof PatchCellTissue) {
                tissueAgents.add((PatchCellTissue) cell);
            }
        }
    }

    /**
     * Adds all tissue agents at a given location in the input bag.
     *
     * @param grid the simulation grid
     * @param loc the location
     * @return bag of all tissue neighbor agents
     */
    private Bag grabAllTissueNeighbors(PatchGrid grid, PatchLocation loc) {
        Bag neighbors = new Bag();
        getTissueAgents(neighbors, grid.getObjectsAtLocation(loc));
        for (Location neighborLocation : loc.getNeighbors()) {
            Bag bag = new Bag(grid.getObjectsAtLocation(neighborLocation));
            getTissueAgents(neighbors, bag);
        }

        return neighbors;
    }

    /** Sets binding flag to unbound and binding target to null. */
    public void unbind() {
        if (this.bindingFlag == PatchEnums.AntigenFlag.BOUND_ANTIGEN) {
            super.setBindingFlag(PatchEnums.AntigenFlag.UNBOUND);
            this.boundTarget = null;
            this.boundCARAntigensCount--;
        }
    }

    /** Randomly increases number of self receptors after CAR binding. */
    private void updateSelfReceptors() {
        selfReceptors += (int) ((double) selfReceptorsStart * (0.95 + Math.random() / 10));
    }
}
