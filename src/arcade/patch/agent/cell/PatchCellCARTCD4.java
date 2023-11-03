package arcade.patch.agent.cell;

import arcade.core.agent.cell.CellState;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.patch.util.PatchEnums.AntigenFlag;
import arcade.patch.util.PatchEnums.Domain;
import arcade.patch.util.PatchEnums.State;
import ec.util.MersenneTwisterFast;
import sim.engine.SimState;

public class PatchCellCARTCD4 extends PatchCellCART{

    /** Fraction of stimulatory cells that become apoptotic. */
    private final double stimulatoryFraction;

    /**
      * Creates a tissue {@code PatchCellCARTCD4} agent.
      * * <p>
      * Loaded parameters include:
      * <ul>
      *     <li>{@code STIMULATORY_FRACTION} = fraction of stimulatory cells that
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

       public PatchCellCARTCD4(int id, int parent, int pop, CellState state, int age, int divisions,
                            Location location, MiniBox parameters, double volume, double height,
                            double criticalVolume, double criticalHeight) {
            
            super(id, parent, pop, state, age, divisions, location, parameters,
                                volume, height, criticalVolume, criticalHeight);
         
            // Set loaded parameters.
            stimulatoryFraction = parameters.getDouble(  "STIMULATORY_FRACTION");
        }

        
        @Override
        public PatchCellCARTCD4 make(int newID, CellState newState, Location newLocation,
                          MersenneTwisterFast random) {
            
            divisions--;
            return new PatchCellCARTCD4(newID, id, pop, newState, age, divisions, newLocation,
                parameters, volume, height, criticalVolume, criticalHeight);
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
            if (super.lastActiveTicker/1440 >- 7) super.activated = false;

            // Step metabolism process.
            super.processes.get(Domain.METABOLISM).step(simstate.random, sim);
            
            // Check energy status. If cell has less energy than threshold, it will
            // apoptose. If overall energy is negative, then cell enters quiescence.
            if (state != State.APOPTOTIC && energy < 0) {
                if (super.energy < super.energyThreshold) {
                    super.setState(State.APOPTOTIC);
                } else if (state != State.ANERGIC && state != State.SENESCENT && state != State.EXHAUSTED && state != State.STARVED && state != State.PROLIFERATIVE) {
                    super.setState(State.STARVED);
                }
            } else {
                super.setState(State.UNDEFINED);
            }
            
            // Step inflammation process.
            //super.processes.get(Domain.INFLAMMATION).step(simstate.random, sim);
            
            // Change state from undefined.
            if (super.state == State.UNDEFINED || super.state == State.PAUSED) {
                if (divisions == 0) {
                    if (simstate.random.nextDouble() > super.senescentFraction) {
                        super.setState(State.APOPTOTIC);
                    } else {
                        super.setState(State.SENESCENT);
                    }
                }
            } else {
                // Cell attempts to bind to a target
                super.bindTarget(sim, location, new MersenneTwisterFast(simstate.seed()));

                //If cell is bound to both antigen and self it will become anergic.
                if (binding == AntigenFlag.BOUND_ANTIGEN_CELL_RECEPTOR) {
                    if (simstate.random.nextDouble() > super.anergicFraction) {
                        super.setState(State.APOPTOTIC);
                    } else {
                        super.setState(State.ANERGIC);
                    }
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
                    } else {
                        //if CD4 cell is properly activated, it can stimulate
                        if (simstate.random.nextDouble() > stimulatoryFraction) {
                            super.setState(State.APOPTOTIC);
                        } else {
                            super.setState(State.STIMULATORY);
                        }
                    }
                } else if (binding == AntigenFlag.BOUND_CELL_RECEPTOR) {
                    //If self binding, unbind
                    super.setAntigenFlag(AntigenFlag.UNBOUND);
                } else {
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
