package arcade.patch.agent.module;

import sim.util.Bag;
import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.patch.agent.cell.PatchCell;
import arcade.patch.env.grid.PatchGrid;
import arcade.patch.env.loc.PatchLocation;
import static arcade.core.util.Enums.State;

/**
 * Extension of {@link PatchModule} for proliferation.
 * <p>
 * During proliferation, the module is repeatedly stepped from its creation
 * until either the cell is no longer able to proliferate or it has successfully
 * doubled in size and is able to create a new cell object.
 */

public class PatchModuleProliferation extends PatchModule {
    /** Tracker for duration of cell cycle. */
    private int ticker;
    
    /** Target volume for division. */
    private final double targetVolume;
    
    /** Maximum tolerable height for cell. */
    private final double maxHeight;
    
    /** Time required for DNA synthesis (in minutes). */
    private final double synthesisTime;
    
    /**
     * Creates a proliferation {@code Module} for the given {@link PatchCell}.
     *
     * @param cell  the {@link PatchCell} the module is associated with
     */
    public PatchModuleProliferation(PatchCell cell) {
        super(cell);
        
        targetVolume = 2 * cell.getCriticalVolume();
        maxHeight = cell.getCriticalHeight();
        
        MiniBox parameters = cell.getParameters();
        synthesisTime = parameters.getInt("proliferation/SYNTHESIS_TIME");
    }

    /**
     * Calls the step method for the module.
     *
     * @param random  the random number generator
     * @param sim  the simulation instance
     */
    public void step(MersenneTwisterFast random, Simulation sim) {
        Bag bag = ((PatchGrid) sim.getGrid()).getObjectsAtLocation(cell.getLocation());
        double totalVolume = PatchCell.calculateTotalVolume(bag);
        double currentHeight = totalVolume / location.getArea();
        
        // Check if cell is no longer able to proliferate due to (i) other
        // condition that has caused its type to no longer be proliferative,
        // (ii) cell no longer exists at a tolerable height, or (iii) no
        // space in neighborhood to divide into. Otherwise, check if double
        // volume has been reached, and if so, create a new cell.
        if (currentHeight > maxHeight) {
            cell.setState(State.QUIESCENT);
        } else {
            PatchLocation newLocation = PatchCell.selectBestLocation(sim, location,
                    cell.getVolume() * 0.5, maxHeight, random);
            
            if (newLocation == null) {
                cell.setState(State.QUIESCENT);
            } else if (cell.getVolume() > targetVolume) {
                if (ticker > synthesisTime) {
                    // TODO: ADD CYCLE TIME TO TRACKER.
                    
                    // Reset current cell.
                    cell.setState(State.UNDEFINED);
                    
                    // Create and schedule new cell.
                    int newID = sim.getID();
                    PatchCell newCell = cell.make(newID, State.UNDEFINED, newLocation, random);
                    sim.getGrid().addObject(newCell, newLocation);
                    newCell.schedule(sim.getSchedule());
                    
                    // TODO: Update daughter cell modules.
                    
                    // TODO: Update environment generator sites.
                } else {
                    ticker++;
                }
            }
        }
    }
}
