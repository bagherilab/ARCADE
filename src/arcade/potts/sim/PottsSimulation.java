package arcade.potts.sim;

import java.util.ArrayList;
import java.util.HashSet;
import sim.engine.Schedule;
import sim.engine.SimState;
import arcade.core.agent.action.Action;
import arcade.core.agent.cell.Cell;
import arcade.core.agent.cell.CellContainer;
import arcade.core.env.component.Component;
import arcade.core.env.grid.Grid;
import arcade.core.env.lattice.Lattice;
import arcade.core.env.location.Location;
import arcade.core.env.location.LocationContainer;
import arcade.core.sim.Series;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.agent.cell.PottsCellFactory;
import arcade.potts.env.grid.PottsGrid;
import arcade.potts.env.location.PottsLocationFactory;
import static arcade.potts.util.PottsEnums.Ordering;

/**
 * Abstract implementation for potts {@link Simulation} instances.
 */

public abstract class PottsSimulation extends SimState implements Simulation {
    /** {@link arcade.core.sim.Series} object containing this simulation. */
    final PottsSeries series;
    
    /** Random number generator seed for this simulation. */
    final int seed;
    
    /** {@link arcade.potts.sim.Potts} object for the simulation. */
    Potts potts;
    
    /** {@link arcade.core.env.grid.Grid} containing agents in the simulation. */
    Grid grid;
    
    /** Cell ID tracker. */
    int id;
    
    /** Cell factory instance for the simulation. */
    public final PottsCellFactory cellFactory;
    
    /** Location factory instance for the simulation. */
    public final PottsLocationFactory locationFactory;
    
    /**
     * Simulation instance for a {@link Series} for given random seed.
     *
     * @param seed  the random seed for random number generator
     * @param series  the simulation series
     */
    public PottsSimulation(long seed, Series series) {
        super(seed);
        this.series = (PottsSeries) series;
        this.seed = (int) seed - Series.SEED_OFFSET;
        
        this.locationFactory = makeLocationFactory();
        this.cellFactory = makeCellFactory();
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
    
    @Override
    public final Action getAction(String key) { return null; }
    
    @Override
    public final Component getComponent(String key) { return null; }
    
    /**
     * Gets {@link Potts} instance for the simulation.
     *
     * @return  the potts instance
     */
    public final Potts getPotts() { return potts; }
    
    /**
     * Called at the start of the simulation to set up agents and environment
     * and schedule actions and components as needed.
     */
    @Override
    public void start() {
        super.start();
        
        // Reset id.
        id = 0;
        
        // Equip simulation to loader.
        if (series.loader != null) {
            series.loader.equip(this);
        }
        
        setupPotts();
        setupAgents();
        setupEnvironment();
        
        scheduleActions();
        scheduleComponents();
        
        // Equip simulation to saver and schedule.
        if (series.saver != null && !series.isVis) {
            series.saver.equip(this);
            doOutput(true);
        }
    }
    
    /**
     * Called at the end of the simulation.
     */
    @Override
    public void finish() {
        super.finish();
        
        // Finalize saver.
        if (!series.isVis) {
            doOutput(false);
        }
    }
    
    /**
     * Creates the {@link arcade.potts.sim.Potts} object for the simulation.
     *
     * @return  a {@link arcade.potts.sim.Potts} object
     */
    abstract Potts makePotts();
    
    /**
     * Creates a factory for locations.
     *
     * @return  a {@link arcade.core.env.location.Location} factory
     */
    public abstract PottsLocationFactory makeLocationFactory();
    
    /**
     * Creates a factory for cells.
     *
     * @return  a {@link arcade.core.agent.cell.Cell} factory
     */
    public abstract PottsCellFactory makeCellFactory();
    
    /**
     * Sets up the potts layer for the simulation.
     */
    public final void setupPotts() {
        potts = makePotts();
        schedule.scheduleRepeating(1, Ordering.POTTS.ordinal(), potts);
    }
    
    @Override
    public final void setupAgents() {
        // Initialize grid for agents.
        grid = new PottsGrid();
        potts.grid = grid;
        
        // Initialize factories.
        locationFactory.initialize(series, random);
        cellFactory.initialize(series, random);
        
        // Iterate through each population to create agents.
        for (MiniBox population : series.populations.values()) {
            int pop = population.getInt("CODE");
            HashSet<Integer> ids = cellFactory.popToIDs.get(pop);
            
            for (int i : ids) {
                // Get location and cell containers.
                LocationContainer locationContainer = locationFactory.locations.get(i);
                CellContainer cellContainer = cellFactory.cells.get(i);
                
                // Check that we have enough containers.
                if (locationContainer == null || cellContainer == null) {
                    break;
                }
                
                // Make the location and cell.
                Location location = locationContainer.convert(locationFactory, cellContainer);
                PottsCell cell = (PottsCell) cellContainer.convert(cellFactory, location);
                
                // Add, initialize, and schedule the cell.
                grid.addObject(cell, null);
                potts.register(cell);
                cell.initialize(potts.ids, potts.regions);
                cell.schedule(schedule);
                
                // Update id tracking.
                id = Math.max(i, id);
            }
        }
    }
    
    @Override
    public final void setupEnvironment() {
        // TODO add environment setup (currently not needed)
    }
    
    @Override
    public final void scheduleActions() {
        // TODO add action scheduling
    }
    
    @Override
    public final void scheduleComponents() {
        // TODO add component scheduling
    }
    
    /**
     * Runs output methods.
     *
     * @param isScheduled  {@code true} to schedule output, {@code false} otherwise
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
