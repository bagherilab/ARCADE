package arcade.patch.sim;

import java.util.ArrayList;
import sim.engine.Schedule;
import sim.engine.SimState;
import arcade.core.agent.cell.Cell;
import arcade.core.agent.cell.CellContainer;
import arcade.core.env.grid.Grid;
import arcade.core.env.lat.Lattice;
import arcade.core.env.loc.LocationContainer;
import arcade.core.sim.Series;
import arcade.core.sim.Simulation;

/**
 * Abstract implementation for patch {@link Simulation} instances.
 */

public abstract class PatchSimulation extends SimState implements Simulation {
    /** {@link arcade.core.sim.Series} object containing this simulation. */
    final PatchSeries series;
    
    /** Random number generator seed for this simulation. */
    final int seed;
    
    /** {@link arcade.core.env.grid.Grid} containing agents in the simulation. */
    Grid grid;
    
    /** Cell ID tracker. */
    int id;
    
    /**
     * Simulation instance for a {@link arcade.core.sim.Series} for given random seed.
     *
     * @param seed  the random seed for random number generator
     * @param series  the simulation series
     */
    public PatchSimulation(long seed, Series series) {
        super(seed);
        this.series = (PatchSeries) series;
        this.seed = (int) seed - Series.SEED_OFFSET;
    }
    
    @Override
    public final Series getSeries() { return series; }
    
    @Override
    public final Schedule getSchedule() { return schedule; }
    
    @Override
    public final int getSeed() { return seed; }
    
    @Override
    public final int getID() { return ++id; }
    
    @Override
    public final ArrayList<CellContainer> getCells() {
        ArrayList<CellContainer> cellContainers = new ArrayList<>();
        
        for (Object obj : grid.getAllObjects()) {
            Cell cell = (Cell) obj;
            cellContainers.add(cell.convert());
        }
        
        return cellContainers;
    }
    
    @Override
    public final ArrayList<LocationContainer> getLocations() {
        ArrayList<LocationContainer> locationContainers = new ArrayList<>();
        
        for (Object obj : grid.getAllObjects()) {
            Cell cell = (Cell) obj;
            locationContainers.add(cell.getLocation().convert(cell.getID()));
        }
        
        return locationContainers;
    }
   
    @Override
    public final Grid getGrid() { return grid; }
    
    @Override
    public final Lattice getLattice(String key) { return null; }
    
    /**
     * Called at the start of the simulation to set up agents and environment
     * and schedule components and helpers as needed.
     */
    @Override
    public void start() {
        super.start();
    }
    
    /**
     * Called at the end of the simulation.
     */
    @Override
    public void finish() {
        super.finish();
    }
    
    @Override
    public final void setupAgents() {
        // TODO add agent setup
    }
    
    @Override
    public final void setupEnvironment() {
        // TODO add environment setup (currently not needed)
    }
    
    @Override
    public final void scheduleHelpers() {
        // TODO add helper scheduling
    }
    
    @Override
    public final void scheduleComponents() {
        // TODO add component scheduling
    }
    
    /**
     * Runs output methods.
     *
     * @param isScheduled  {@code true} if the output should be scheduled, {@code false} otherwise
     */
    public void doOutput(boolean isScheduled) {
        if (isScheduled) {
            series.saver.schedule(schedule, series.getInterval());
        } else {
            int tick = (int) schedule.getTime() + 1;
            series.saver.saveCells(tick);
            series.saver.saveLocations(tick);
        }
    }
}
