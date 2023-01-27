package arcade.potts.agent.module;

import ec.util.MersenneTwisterFast;
import arcade.core.env.loc.Location;
import arcade.core.sim.Simulation;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.env.loc.PottsLocation;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import static arcade.core.util.Enums.State;
import static arcade.potts.util.PottsEnums.Phase;

/**
 * Extension of {@link PottsModule} for proliferation.
 * <p>
 * During proliferation, cells cycle through G1, S, G2, and M phases. Once the
 * cell complete M phase, it divides to create a new daughter cell.
 */

public abstract class PottsModuleProliferation extends PottsModule {
    /**
     * Creates a proliferation {@code Module} for the given {@link PottsCell}.
     *
     * @param cell  the {@link PottsCell} the module is associated with
     */
    public PottsModuleProliferation(PottsCell cell) {
        super(cell);
        setPhase(Phase.PROLIFERATIVE_G1);
    }
    
    /**
     * Calls the step method for the current simple phase.
     *
     * @param random  the random number generator
     * @param sim  the simulation instance
     */
    public void step(MersenneTwisterFast random, Simulation sim) {
        switch (phase) {
            case PROLIFERATIVE_G1:
                stepG1(random);
                break;
            case PROLIFERATIVE_S:
                stepS(random);
                break;
            case PROLIFERATIVE_G2:
                stepG2(random);
                break;
            case PROLIFERATIVE_M:
                stepM(random, sim);
                break;
            default:
                break;
        }
    }
    
    /**
     * Performs actions for G1 phase.
     *
     * @param random  the random number generator
     */
    abstract void stepG1(MersenneTwisterFast random);
    
    /**
     * Performs actions for S phase.
     *
     * @param random  the random number generator
     */
    abstract void stepS(MersenneTwisterFast random);
    
    /**
     * Performs actions for G2 phase.
     *
     * @param random  the random number generator
     */
    abstract void stepG2(MersenneTwisterFast random);
    
    /**
     * Performs actions for M phase.
     *
     * @param random  the random number generator
     * @param sim  the simulation instance
     */
    abstract void stepM(MersenneTwisterFast random, Simulation sim);
    
    /**
     * Adds a cell to the simulation.
     * <p>
     * The cell location is split, along with any regions. The new cell is
     * created, initialized, and added to the schedule. Both cells are reset and
     * remain in the proliferative state.
     *
     * @param random  the random number generator
     * @param sim  the simulation instance
     */
    void addCell(MersenneTwisterFast random, Simulation sim) {
        Potts potts = ((PottsSimulation) sim).getPotts();
        
        // Split current location.
        Location newLocation = ((PottsLocation) cell.getLocation()).split(random);
        
        // Reset current cell.
        cell.reset(potts.ids, potts.regions);
        
        // Create and schedule new cell.
        int newID = sim.getID();
        PottsCell newCell = cell.make(newID, State.PROLIFERATIVE, newLocation, random);
        sim.getGrid().addObject(newCell, null);
        potts.register(newCell);
        newCell.reset(potts.ids, potts.regions);
        newCell.schedule(sim.getSchedule());
    }
}
