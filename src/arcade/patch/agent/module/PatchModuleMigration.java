package arcade.patch.agent.module;

import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.patch.agent.cell.PatchCell;
import arcade.patch.env.loc.PatchLocation;
import static arcade.core.util.Enums.State;

/**
 * Extension of {@link PatchModule} for migration.
 * <p>
 * During migration, the module is stepped once after the number of ticks
 * corresponding to (distance to move) * (movement speed) has passed.
 * The module will move the cell from one location to the best valid location
 * in the neighborhood.
 */

public class PatchModuleMigration extends PatchModule {
    /** Tracker for duration of cell movement. */
    private int ticker;
    
    /** Cell migration rate. */
    private final double migrationRate;
    
    /** Time required for cell migration (in minutes). */
    private final double movementTime;
    
    /**
     * Creates a migration {@code Module} for the given {@link PatchCell}.
     *
     * @param cell  the {@link PatchCell} the module is associated with
     */
    public PatchModuleMigration(PatchCell cell) {
        super(cell);
        
        MiniBox parameters = cell.getParameters();
        migrationRate = parameters.getDouble("migration/MIGRATION_RATE");
        movementTime = Math.round(location.getCoordinateSize() / migrationRate);
    }
    
    /**
     * Calls the step method for the module.
     *
     * @param random  the random number generator
     * @param sim  the simulation instance
     */
    public void step(MersenneTwisterFast random, Simulation sim) {
        if (ticker > movementTime) {
            PatchLocation newLocation = PatchCell.selectBestLocation(sim, location,
                    cell.getVolume(), cell.getCriticalHeight(), random);
            
            if (newLocation == null) {
                cell.setState(State.QUIESCENT);
            } else {
                if (!location.equals(newLocation)) {
                    sim.getGrid().moveObject(cell, location, newLocation);
                    
                    // TODO: Update environment generator sites.
                }
               cell.setState(State.UNDEFINED);
            }
        } else {
            ticker++;
        }
    }
}
