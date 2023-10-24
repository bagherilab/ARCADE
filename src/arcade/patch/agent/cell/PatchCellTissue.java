package arcade.patch.agent.cell;

import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellState;
import arcade.core.env.location.Location;
import arcade.core.util.MiniBox;
import arcade.patch.util.PatchEnums.Domain;
import arcade.patch.util.PatchEnums.Flag;
import arcade.patch.util.PatchEnums.State;
import sim.engine.SimState;
import arcade.core.sim.Simulation;
import static arcade.patch.util.PatchEnums.Domain;
import static arcade.patch.util.PatchEnums.Flag;
import static arcade.patch.util.PatchEnums.State;
/**
 * Extension of {@link PatchCell} for healthy tissue cells.
 */



public class PatchCellTissue extends PatchCell {
    /** Fraction of necrotic cells that become apoptotic. */
    private final double necroticFraction;
    
    /** Fraction of senescent cells that become apoptotic. */
    private final double senescentFraction;

    /** Cell surface antigen count */
	protected int carAntigens;
	
	/** Cell surface PDL1 count */
	protected final int selfTargets;


    /**
     * Creates a tissue {@code PatchCell} agent.
     * * <p>
     * Loaded parameters include:
     * <ul>
     *     <li>{@code NECROTIC_FRACTION} = fraction of necrotic cells that
     *         become apoptotic</li>
     *     <li>{@code SENESCENT_FRACTION} = fraction of senescent cells that
     *         become apoptotic</li>
     *     <li>{@code CAR_ANTIGENS} = Cell surface antigen count </li>
     *     <li>{@code SELF_TARGETS} = Cell surface PDL1 count </li>
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
    public PatchCellTissue(int id, int parent, int pop, CellState state, int age, int divisions,
                           Location location, MiniBox parameters, double volume, double height,
                           double criticalVolume, double criticalHeight) {
        super(id, parent, pop, state, age, divisions, location, parameters,
                volume, height, criticalVolume, criticalHeight);
        
        // Set loaded parameters.
        necroticFraction = parameters.getDouble("NECROTIC_FRACTION");
        senescentFraction = parameters.getDouble("SENESCENT_FRACTION");
        carAntigens = parameters.getInt("CAR_ANTIGENS");
        selfTargets = parameters.getInt("SELF_TARGETS");
    }
    
    @Override
    public PatchCell make(int newID, CellState newState, Location newLocation,
                          MersenneTwisterFast random) {
        divisions--;
        return new PatchCellTissue(newID, id, pop, newState, age, divisions, newLocation,
                parameters, volume, height, criticalVolume, criticalHeight);
    }

    /* consider making PatchCell parameters protected instead of private */
    /* make step()  method that overrides main that is moved over from PatchCell */

    @Override
    public void step(SimState simstate) {
        Simulation sim = (Simulation) simstate;
        
        // Increase age of cell.
        super.age++;
        
        // TODO: check for death due to age
        
        // Step metabolism process.
        super.processes.get(Domain.METABOLISM).step(simstate.random, sim);
        
        // Check energy status. If cell has less energy than threshold, it will
        // necrose. If overall energy is negative, then cell enters quiescence.
        if (state != State.APOPTOTIC && energy < 0) {
            if (super.energy < super.energyThreshold) {
                if (simstate.random.nextDouble() > necroticFraction) {
                    super.setState(State.APOPTOTIC);
                } else {
                    super.setState(State.NECROTIC);
                }
            } else if (state != State.QUIESCENT && state != State.SENESCENT) {
                super.setState(State.QUIESCENT);
            }
        }
        
        // Step signaling network process.
        super.processes.get(Domain.SIGNALING).step(simstate.random, sim);
        
        // Change state from undefined.
        if (super.state == State.UNDEFINED) {
            if (super.flag == Flag.MIGRATORY) {
                super.setState(State.MIGRATORY);
            } else if (super.divisions == 0) {
                if (simstate.random.nextDouble() > senescentFraction) {
                    super.setState(State.APOPTOTIC);
                } else {
                    super.setState(State.SENESCENT);
                }
            } else {
                super.setState(State.PROLIFERATIVE);
            }
        }
        
        // Step the module for the cell state.
        if (super.module != null) {
            super.module.step(simstate.random, sim);
        }
    }
}
