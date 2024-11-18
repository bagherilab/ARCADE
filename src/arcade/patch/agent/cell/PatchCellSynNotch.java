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
    protected final int synnotchs;
    public int synNotchAntigensBound;
        /**
         * Creates a tissue {@code PatchCellCARTCD8} agent. *
         *
         * <p>Loaded parameters include:
         *
         * <ul>
         *   <li>{@code CYTOTOXIC_FRACTION} = fraction of cytotoxic cells that become apoptotic
         * </ul>
         *
         * @param id the cell ID
         * @param parent the parent ID
         * @param pop the cell population index
         * @param state the cell state
         * @param age the cell age
         * @param divisions the number of cell divisions
         * @param location the {@link Location} of the cell
         * @param parameters the dictionary of parameters
         * @param volume the cell volume
         * @param height the cell height
         * @param criticalVolume the critical cell volume
         * @param criticalHeight the critical cell height
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
         *
         * @param state the MASON simulation state
         * @param loc the location of the SynNotch T-cell
         * @param random random seed
         */
        @Override
        public PatchCellTissue bindTarget(
                Simulation sim, PatchLocation loc, MersenneTwisterFast random) {
            double KDCAR = carAffinity * (loc.getVolume() * 1e-15 * 6.022E23);
            double KDSelf = selfReceptorAffinity * (loc.getVolume() * 1e-15 * 6.022E23);
            double KDSynNotch = synNotchReceptorAffinity * (loc.getVolume() * 1e-15 * 6.022E23);
    
            PatchGrid grid = (PatchGrid) sim.getGrid();
    
            // get all agents from this location
            Bag allAgents = new Bag(grid.getObjectsAtLocation(loc));
    
            // get all agents from neighboring locations
            for (Location neighborLocation : loc.getNeighbors()) {
                Bag bag = new Bag(grid.getObjectsAtLocation(neighborLocation));
                for (Object b : bag) {
                    // add all agents from neighboring locations
                    if (!allAgents.contains(b)) allAgents.add(b);
                }
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
                binding = AntigenFlag.UNBOUND;
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
                    if (!(cell instanceof PatchCellCART)
                            && cell.getState() != State.APOPTOTIC
                            && cell.getState() != State.NECROTIC) {
                        PatchCellTissue tissueCell = (PatchCellTissue) cell;
                        double CARAntigens = tissueCell.carAntigens;
                        double selfTargets = tissueCell.selfTargets;
                        double synNotchAntigens = tissueCell.synNotchAntigens;
    
                        double hillCAR =
                                (CARAntigens
                                                * contactFraction
                                                / (KDCAR * carBeta + CARAntigens * contactFraction))
                                        * (cars / 50000)
                                        * carAlpha;
                        double hillSelf =
                                (selfTargets
                                                * contactFraction
                                                / (KDSelf * selfBeta + selfTargets * contactFraction))
                                        * (selfReceptors / selfReceptorsStart)
                                        * selfAlpha;
                        double hillSynNotch =
                                (synNotchAntigens
                                                * contactFraction
                                                / (KDSynNotch * synNotchBeta + synNotchAntigens * contactFraction))
                                        //TODO: find literature value for avg synnotch receptors and replace 50000
                                        * (synnotchs / 50000)
                                        * synNotchAlpha;
    
                        double logCAR = 2 * (1 / (1 + Math.exp(-1 * hillCAR))) - 1;
                        double logSelf = 2 * (1 / (1 + Math.exp(-1 * hillSelf))) - 1;
                        double logSynNotch = 2 * (1 / (1 + Math.exp(-1 * hillSynNotch))) - 1;
    
                        double randomAntigen = random.nextDouble();
                        double randomSelf = random.nextDouble();
                        double randomSynNotch = random.nextDouble();
    
                        if (logCAR >= randomAntigen && logSelf < randomSelf && logSynNotch < randomSynNotch) {
                            // cell binds to antigen receptor
                            binding = AntigenFlag.BOUND_ANTIGEN;
                            boundAntigensCount++;
                            selfReceptors +=
                                    (int)
                                            ((double) selfReceptorsStart
                                                    * (0.95 + random.nextDouble() / 10));
                            return tissueCell;
                        } else if (logCAR >= randomAntigen && logSelf >= randomSelf && logSynNotch < randomSynNotch) {
                            // cell binds to antigen receptor and self
                            binding = AntigenFlag.BOUND_ANTIGEN_CELL_RECEPTOR;
                            boundAntigensCount++;
                            boundSelfCount++;
                            selfReceptors +=
                                    (int)
                                            ((double) selfReceptorsStart
                                                    * (0.95 + random.nextDouble() / 10));
                            return tissueCell;
                        } else if (logCAR < randomAntigen && logSelf >= randomSelf && logSynNotch < randomSynNotch) {
                            // cell binds to self
                            binding = AntigenFlag.BOUND_CELL_RECEPTOR;
                            boundSelfCount++;
                            return tissueCell;
                        } else if (logSynNotch >= randomSynNotch && logSelf < randomSelf && logCAR < randomAntigen) {
                            binding = AntigenFlag.BOUND_SYNNOTCH;
                            synNotchAntigensBound++;
                        selfReceptors +=
                                (int)
                                        ((double) selfReceptorsStart
                                                * (0.95 + random.nextDouble() / 10));
                        return tissueCell;
                    } else if (logSynNotch >= randomSynNotch && logSelf >= randomSelf && logCAR < randomAntigen){
                        binding = AntigenFlag.BOUND_CELL_SYNNOTCH_RECEPTOR;
                        synNotchAntigensBound++;
                        boundSelfCount++;
                        selfReceptors +=
                                (int)
                                        ((double) selfReceptorsStart
                                                * (0.95 + random.nextDouble() / 10));
                        return tissueCell;
                    } else if (logSynNotch >= randomSynNotch && logSelf >= randomSelf && logCAR >= randomAntigen) {
                        binding = AntigenFlag.BOUND_ANTIGEN_CELL_SYNNOTCH_RECEPTOR;
                        synNotchAntigensBound++;
                        boundAntigensCount++;
                        boundSelfCount++;
                        selfReceptors +=
                                (int)
                                        ((double) selfReceptorsStart
                                                * (0.95 + random.nextDouble() / 10));
                        return tissueCell;
                    }
                    else {
                        // cell doesn't bind to anything
                        binding = AntigenFlag.UNBOUND;
                    }
                }
            }
            binding = AntigenFlag.UNBOUND;
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
                this.activated = false;
            } else if (state != State.EXHAUSTED
                    && state != State.STARVED) {
                super.setState(State.STARVED);
                super.setAntigenFlag(AntigenFlag.UNBOUND);
            }
        } else if (state == State.STARVED && energy >= 0) {
            super.setState(State.UNDEFINED);
        }

        // Step inflammation process.
        super.processes.get(Domain.INFLAMMATION).step(simstate.random, sim);

        // Change state from undefined.
        if (super.state == State.UNDEFINED) {
            // Cell attempts to bind to a target
            PatchCellTissue target = super.bindTarget(sim, location, simstate.random);

            // Step inflammation process.
            super.processes.get(Domain.QUORUM).step(simstate.random, sim);

            // If cell is bound to target antigen and/or SynNotch, the cell
            // can potentially become properly activated.
            if (binding == AntigenFlag.BOUND_ANTIGEN || binding == AntigenFlag.BOUND_ANTIGEN_CELL_SYNNOTCH_RECEPTOR) {
                // Check overstimulation. If cell has bound to
                // target antigens too many times, becomes exhausted.
                if (boundAntigensCount > maxAntigenBinding) {
                    if (simstate.random.nextDouble() > super.exhaustedFraction) {
                        super.setState(State.APOPTOTIC);
                    } else {
                        super.setState(State.EXHAUSTED);
                    }
                    super.setAntigenFlag(AntigenFlag.UNBOUND);
                    this.activated = false;
                } else if (binding == AntigenFlag.BOUND_ANTIGEN_SYNNOTCH_RECEPTOR){
                    // if CD4 cell is properly activated, it can be cytotoxic
                    this.lastActiveTicker = 0;
                    this.activated = true;
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
}
