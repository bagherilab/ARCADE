package arcade.patch.agent.cell;

import arcade.core.agent.cell.CellState;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.patch.agent.action.PatchActionKill;
import arcade.patch.agent.action.PatchActionReset;
import arcade.patch.sim.PatchSimulation;
import arcade.patch.util.PatchEnums.AntigenFlag;
import arcade.patch.util.PatchEnums.Domain;
import arcade.patch.util.PatchEnums.State;
import ec.util.MersenneTwisterFast;
import sim.engine.SimState;

public class PatchCellCARTCD8 extends PatchCellCART {
    /**
      * Creates a tissue {@code PatchCellCARTCD8} agent.
      * * <p>
      * Loaded parameters include:
      * <ul>
      *     <li>{@code CYTOTOXIC_FRACTION} = fraction of cytotoxic cells that
      *         become apoptotic</li>
      * </ul>
      *
      * @param id  the cell ID
      * @param parent  the parent ID
      * @param pop  the cell population index
      * @param state  the cell state
      * @param age  the cell age
      * @param divisions  the number of cell divisions
      * @param location  the {@link Location} of the cell
      * @param parameters  the dictionary of parameters
      * @param volume  the cell volume
      * @param height  the cell height
      * @param criticalVolume  the critical cell volume
      * @param criticalHeight  the critical cell height
      */

      public PatchCellCARTCD8(int id, int parent, int pop, CellState state, int age, int divisions,
                            Location location, MiniBox parameters, double volume, double height,
                            double criticalVolume, double criticalHeight) {
            
            super(id, parent, pop, state, age, divisions, location, parameters,
                                volume, height, criticalVolume, criticalHeight);
        }

        @Override
        public PatchCellContainer make(int newID, CellState newState, MersenneTwisterFast random) {
            
            divisions--;
            // return new PatchCellCARTCD8(newID, id, pop, newState, age, divisions, newLocation,
            //     parameters, volume, height, criticalVolume, criticalHeight);
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
            
            // TODO: check for death due to age


            // Increase time since last active ticker
            super.lastActiveTicker++;
            if (super.lastActiveTicker != 0 && super.lastActiveTicker % 1440 == 0) {
                if (super.boundAntigensCount != 0) super.boundAntigensCount--;
            }
            if (super.lastActiveTicker/1440 > 7) super.activated = false;

            // Step metabolism process.
            super.processes.get(Domain.METABOLISM).step(simstate.random, sim);
            
            // Check energy status. If cell has less energy than threshold, it will
            // apoptose. If overall energy is negative, then cell enters quiescence.
            if (state != State.APOPTOTIC && energy < 0) {
                if (super.energy < super.energyThreshold) {
                    super.setState(State.APOPTOTIC);
                    super.setAntigenFlag(AntigenFlag.UNBOUND);
                    this.activated = false;
                } else if (state != State.ANERGIC && state != State.SENESCENT && state != State.EXHAUSTED && state != State.STARVED ) {
                    super.setState(State.STARVED);
                    super.setAntigenFlag(AntigenFlag.UNBOUND);
                }
            } else if (state == State.STARVED && energy >= 0) {
                super.setState(State.UNDEFINED);
            }
            
            // Step inflammation process.
            super.processes.get(Domain.INFLAMMATION).step(simstate.random, sim);
            
            // Change state from undefined.
            if (super.state == State.UNDEFINED|| super.state == State.PAUSED || super.state == State.QUIESCENT) {
                if (divisions == 0) {
                    if (simstate.random.nextDouble() > super.senescentFraction) {
                        super.setState(State.APOPTOTIC);
                    } else {
                        super.setState(State.SENESCENT);
                    }
                    super.setAntigenFlag(AntigenFlag.UNBOUND);
                    this.activated = false;
                } else {
                    // Cell attempts to bind to a target
                    PatchCellTissue target = super.bindTarget(sim, location, simstate.random);

                    //If cell is bound to both antigen and self it will become anergic.
                    if (binding == AntigenFlag.BOUND_ANTIGEN_CELL_RECEPTOR) {
                        if (simstate.random.nextDouble() > super.anergicFraction) {
                            super.setState(State.APOPTOTIC);
                        } else {
                            super.setState(State.ANERGIC);
                        }
                        super.setAntigenFlag(AntigenFlag.UNBOUND);
                        this.activated = false;
                    } else if (binding == AntigenFlag.BOUND_ANTIGEN) {
                        // If cell is only bound to target antigen, the cell
                        // can potentially become properly activated.

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
                        } else {
                            //if CD4 cell is properly activated, it can stimulate
                            this.lastActiveTicker = 0;
                            this.activated = true;
                            super.setState(State.CYTOTOXIC);
                            //need to call kill
                            PatchActionKill kill = new PatchActionKill(this, target, simstate.random,((PatchSimulation) simstate).getSeries(), parameters);
                            kill.schedule(sim.getSchedule());
                            //need to reset
                            PatchActionReset reset = new PatchActionReset(this, simstate.random, ((PatchSimulation) simstate).getSeries(), parameters);
                            reset.schedule(sim.getSchedule());
                            
                        }
                    } else {
                        //If self binding, unbind
                        if (binding == AntigenFlag.BOUND_CELL_RECEPTOR) super.setAntigenFlag(AntigenFlag.UNBOUND);
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
