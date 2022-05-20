package arcade.agent.helper;

import sim.engine.*;
import arcade.sim.Simulation;
import arcade.agent.cell.Cell;
import arcade.agent.cell.TissueCell;
import arcade.env.loc.Location;

/**
 * Extension of {@link arcade.agent.helper.TissueHelper} for cell movement.
 * <p>
 * {@code MoveTissueHelper} is stepped once after the number of ticks
 * corresponding to (distance to move)*(movement speed) has passed.
 * The {@code MoveTissueHelper} will move the cell from one location to another
 * based on best location as determined by the {@code getBestLocation} method in
 * {@link arcade.agent.cell.TissueCell}.
 */

public class MoveTissueHelper extends TissueHelper {
    /** Serialization version identifier */
    private static final long serialVersionUID = 0;
    
    /** Stopper for helper */
    private TentativeStep tent;
    
    /**
     * Creates a {@code MoveTissueHelper} for the given
     * {@link arcade.agent.cell.TissueCell}.
     * 
     * @param c  the {@link arcade.agent.cell.TissueCell} the helper is associated with
     */
    public MoveTissueHelper(Cell c) { super((TissueCell)c); }
    
    public void scheduleHelper(Simulation sim) { scheduleHelper(sim, sim.getTime()); }
    
    /**
     * {@inheritDoc}
     * <p>
     * {@code MoveTissueHelper} is scheduled once.
     */
    public void scheduleHelper(Simulation sim, double begin) {
        double distance = c.getLocation().getGridSize();
        double rate = sim.getSeries().getParam(c.getPop(), "MIGRA_RATE");
        this.begin = begin;
        this.end = begin + Math.round(distance/rate);
        tent = new TentativeStep(this);
        ((SimState)sim).schedule.scheduleOnce(end, Simulation.ORDERING_HELPER, tent);
    }
    
    public void stop() { tent.stop(); }
    
    /**
     * Steps the helper for moving a cell.
     *
     * @param state  the MASON simulation state
     */
    public void step(SimState state) {
        if (c.isStopped()) { return; }
        Simulation sim = (Simulation)state;
        
        if (c.getType() == Cell.TYPE_MIGRA) {
            // Turn off migration metabolism.
            c.setFlag(Cell.IS_MIGRATING, false);
            
            // Find best location to move to.
            Location newLoc = TissueCell.getBestLocation(sim, c);
            
            // Move cell if there is a location to move to and it is not the
            // same as the current location.
            if (newLoc == null) { c.quiesce(sim); }
            else {
                Location oldLoc = c.getLocation().getCopy();
                if (!newLoc.equals(oldLoc)) {
                    sim.getAgents().moveObject(c, newLoc);
                    
                    // Update environment generator sites.
                    sim.getEnvironment("sites").getComponent("sites").updateComponent(sim, oldLoc, newLoc);
                }
                c.setType(Cell.TYPE_NEUTRAL);
            }
            
            // Remove helper.
            if (c.helper == this) { c.helper = null; }
        }
    }
}