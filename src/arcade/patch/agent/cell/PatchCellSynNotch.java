package arcade.patch.agent.cell;

import sim.engine.SimState;
import sim.util.Bag;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.Cell;
import arcade.core.agent.cell.CellState;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.GrabBag;
import arcade.core.util.Parameters;
import arcade.patch.agent.action.PatchActionKill;
import arcade.patch.agent.action.PatchActionReset;
import arcade.patch.env.grid.PatchGrid;
import arcade.patch.env.location.PatchLocation;
import arcade.patch.sim.PatchSimulation;
import arcade.patch.util.PatchEnums.AntigenFlag;
import arcade.patch.util.PatchEnums.Domain;
import arcade.patch.util.PatchEnums.State;

public class PatchCellSynNotch extends PatchCellCART {
    protected final double synNotchReceptorAffinity;
    protected final double synNotchAlpha;
    protected final double synNotchBeta;
    public final int synnotchs;
    public int synNotchAntigensBound;

    /**
     * Creates a tissue {@code PatchCellSynNotch} agent. *
     *
     * @param location the {@link Location} of the cell
     * @param parameters the dictionary of parameters
     */
    public PatchCellSynNotch(
            PatchCellContainer container, Location location, Parameters parameters) {
        this(container, location, parameters, null);
    }

    public PatchCellSynNotch(
            PatchCellContainer container, Location location, Parameters parameters, GrabBag links) {
        super(container, location, parameters, links);

        // Set loaded parameters.
        synNotchReceptorAffinity = parameters.getDouble("SYNNOTCH_RECEPTOR_AFFINITY");
        synNotchAlpha = parameters.getDouble("SYNNOTCH_ALPHA");
        synNotchBeta = parameters.getDouble("SYNNOTCH_BETA");
        synnotchs = parameters.getInt("SYNNOTCHS");
        synNotchAntigensBound = 0;
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

    /**
     * Determines if SynNotch cell agent is bound to neighbor through receptor-target binding.
     *
     * <p>Searches the number of allowed neighbors in series, calculates bound probability to
     * antigen and self receptors, compares values to random variable. Sets flags accordingly and
     * returns a target cell if one was bound by antigen or self receptor.
     */
    @Override
    public PatchCellTissue bindTarget(
            Simulation sim, PatchLocation loc, MersenneTwisterFast random) {
        double kDCAR = carAffinity * (loc.getVolume() * 1e-15 * 6.022E23);
        double kDSelf = selfReceptorAffinity * (loc.getVolume() * 1e-15 * 6.022E23);
        double kDSynNotch = synNotchReceptorAffinity * (loc.getVolume() * 1e-15 * 6.022E23);

        PatchGrid grid = (PatchGrid) sim.getGrid();

        // get all tissue agents from this location
        Bag allAgents = new Bag();
        getTissueAgents(allAgents, grid.getObjectsAtLocation(loc));

        // get all agents from neighboring locations
        for (Location neighborLocation : loc.getNeighbors()) {
            Bag bag = new Bag(grid.getObjectsAtLocation(neighborLocation));
            getTissueAgents(allAgents, bag);
        }

        // remove self
        allAgents.remove(this);

        // shuffle bag
        allAgents.shuffle(random);

        // get number of neighbors
        int neighbors = allAgents.size();

        // Bind target with some probability if a nearby cell has targets to bind.
        int maxSearch = 0;
        if (neighbors == 0) {
            this.setAntigenFlag(AntigenFlag.UNBOUND);
            return null;
        } else {
            if (neighbors < searchAbility) {
                maxSearch = neighbors;
            } else {
                maxSearch = (int) searchAbility;
            }

            // Within maximum search vicinity, search for neighboring cells to bind to
            for (int i = 0; i < maxSearch; i++) {
                Cell cell = (Cell) allAgents.get(i);
                if (cell.getState() != State.APOPTOTIC && cell.getState() != State.NECROTIC) {
                    PatchCellTissue tissueCell = (PatchCellTissue) cell;
                    double cARAntigens = tissueCell.getCarAntigens();
                    double selfTargets = tissueCell.getSelfAntigens();
                    double synNotchAntigens = tissueCell.getSynNotchAntigens();

                    double hillCAR =
                            getHillCoefficient(cARAntigens, kDCAR, cars, 5000, carAlpha, carBeta);

                    double hillSelf =
                            getHillCoefficient(
                                    selfTargets,
                                    kDSelf,
                                    selfReceptors,
                                    selfReceptorsStart,
                                    selfAlpha,
                                    selfBeta);

                    double hillSynNotch =
                            getHillCoefficient(
                                    synNotchAntigens,
                                    kDSynNotch,
                                    synnotchs,
                                    5000,
                                    synNotchAlpha,
                                    synNotchBeta);

                    double logCAR = getLog(hillCAR);
                    double logSelf = getLog(hillSelf);
                    double logSynNotch = getLog(hillSynNotch);

                    double randomAntigen = random.nextDouble();
                    double randomSelf = random.nextDouble();
                    double randomSynNotch = random.nextDouble();

                    if (logCAR >= randomAntigen
                            && logSelf < randomSelf
                            && logSynNotch < randomSynNotch) {
                        // cell binds to antigen receptor
                        super.setAntigenFlag(AntigenFlag.BOUND_ANTIGEN);
                        boundAntigensCount++;
                        selfReceptors +=
                                (int)
                                        ((double) selfReceptorsStart
                                                * (0.95 + random.nextDouble() / 10));
                        return tissueCell;
                    } else if (logCAR >= randomAntigen
                            && logSelf >= randomSelf
                            && logSynNotch < randomSynNotch) {
                        // cell binds to antigen receptor and self
                        super.setAntigenFlag(AntigenFlag.BOUND_ANTIGEN_CELL_RECEPTOR);
                        boundAntigensCount++;
                        boundSelfCount++;
                        selfReceptors +=
                                (int)
                                        ((double) selfReceptorsStart
                                                * (0.95 + random.nextDouble() / 10));
                        return tissueCell;
                    } else if (logCAR < randomAntigen
                            && logSelf >= randomSelf
                            && logSynNotch < randomSynNotch) {
                        // cell binds to self
                        super.setAntigenFlag(AntigenFlag.BOUND_CELL_RECEPTOR);
                        boundSelfCount++;
                        return tissueCell;
                    } else if (logSynNotch >= randomSynNotch
                            && logSelf < randomSelf
                            && logCAR < randomAntigen) {
                        super.setAntigenFlag(AntigenFlag.BOUND_SYNNOTCH);
                        synNotchAntigensBound++;
                        selfReceptors +=
                                (int)
                                        ((double) selfReceptorsStart
                                                * (0.95 + random.nextDouble() / 10));
                        return tissueCell;
                    } else if (logSynNotch >= randomSynNotch
                            && logSelf >= randomSelf
                            && logCAR < randomAntigen) {
                        super.setAntigenFlag(AntigenFlag.BOUND_CELL_SYNNOTCH_RECEPTOR);
                        synNotchAntigensBound++;
                        boundSelfCount++;
                        selfReceptors +=
                                (int)
                                        ((double) selfReceptorsStart
                                                * (0.95 + random.nextDouble() / 10));
                        return tissueCell;
                    } else if (logSynNotch >= randomSynNotch
                            && logCAR >= randomAntigen
                            && logSelf < randomSelf) {
                        super.setAntigenFlag(AntigenFlag.BOUND_ANTIGEN_SYNNOTCH_RECEPTOR);
                        synNotchAntigensBound++;
                        boundAntigensCount++;
                        return tissueCell;
                    } else if (logSynNotch >= randomSynNotch
                            && logSelf >= randomSelf
                            && logCAR >= randomAntigen) {
                        super.setAntigenFlag(AntigenFlag.BOUND_ANTIGEN_CELL_SYNNOTCH_RECEPTOR);
                        synNotchAntigensBound++;
                        boundAntigensCount++;
                        boundSelfCount++;
                        selfReceptors +=
                                (int)
                                        ((double) selfReceptorsStart
                                                * (0.95 + random.nextDouble() / 10));
                        return tissueCell;
                    } else {
                        // cell doesn't bind to anything
                        super.setAntigenFlag(AntigenFlag.UNBOUND);
                    }
                }
            }
            super.setAntigenFlag(AntigenFlag.UNBOUND);
        }
        return null;
    }

    @Override
    public void step(SimState simstate) {
        Simulation sim = (Simulation) simstate;

        // Increase age of cell.
        super.age++;

        // TODO: check for death due to age

        // Increase time since last active ticker
        super.lastActiveTicker++;
        if (super.lastActiveTicker != 0 && super.lastActiveTicker % 1440 == 0) {
            if (super.boundAntigensCount != 0) super.boundAntigensCount--;
            if (synNotchAntigensBound != 0) synNotchAntigensBound--;
        }
        if (super.lastActiveTicker / 1440 > 7) super.activated = false;

        // Step metabolism process.
        super.processes.get(Domain.METABOLISM).step(simstate.random, sim);

        // Check energy status. If cell has less energy than threshold, it will
        // apoptose. If overall energy is negative, then cell enters quiescence.
        if (state != State.APOPTOTIC && energy < 0) {
            if (super.energy < super.energyThreshold) {
                super.setState(State.APOPTOTIC);
                super.setAntigenFlag(AntigenFlag.UNBOUND);
            } else if (state != State.EXHAUSTED && state != State.STARVED) {
                super.setState(State.STARVED);
                super.setAntigenFlag(AntigenFlag.UNBOUND);
            }
        } else if (state == State.STARVED && energy >= 0) {
            super.setState(State.UNDEFINED);
        }

        // Step recruiting process.
        super.processes.get(Domain.QUORUM).step(simstate.random, sim);

        // Change state from undefined.
        if (super.state == State.UNDEFINED || super.state == State.QUIESCENT) {
            // Cell attempts to bind to a target
            PatchCellTissue target = this.bindTarget(sim, location, simstate.random);

            // If cell is bound to target antigen and/or SynNotch, the cell
            // can potentially become properly activated.
            if (this.getAntigenFlag() == AntigenFlag.BOUND_ANTIGEN
                    || this.getAntigenFlag() == AntigenFlag.BOUND_ANTIGEN_CELL_SYNNOTCH_RECEPTOR) {
                // Check overstimulation. If cell has bound to
                // target antigens too many times, becomes exhausted.
                if (boundAntigensCount > maxAntigenBinding) {
                    if (simstate.random.nextDouble() > super.exhaustedFraction) {
                        super.setState(State.APOPTOTIC);
                    } else {
                        super.setState(State.EXHAUSTED);
                    }
                    super.setAntigenFlag(AntigenFlag.UNBOUND);
                } else if (this.activated) {
                    // if CD4 cell is properly activated, it can be cytotoxic
                    super.setState(State.CYTOTOXIC);
                    // need to call kill
                    PatchActionKill kill =
                            new PatchActionKill(
                                    this,
                                    target,
                                    simstate.random,
                                    ((PatchSimulation) simstate).getSeries(),
                                    parameters);
                    kill.schedule(sim.getSchedule());
                    // need to reset
                    PatchActionReset reset =
                            new PatchActionReset(
                                    this,
                                    simstate.random,
                                    ((PatchSimulation) simstate).getSeries(),
                                    parameters);
                    reset.schedule(sim.getSchedule());
                }
            } else {
                super.setAntigenFlag(AntigenFlag.UNBOUND);
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

        // Step the module for the cell state.
        if (super.module != null) {
            super.module.step(simstate.random, sim);
        }
    }

    public void setActivationStatus(boolean status) {
        this.activated = status;
    }

    public void resetLastActiveTicker() {
        this.lastActiveTicker = 0;
    }

    public int returnBoundCars() {
        return super.boundAntigensCount;
    }
}
