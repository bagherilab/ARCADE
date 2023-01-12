package arcade.patch.sim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import sim.engine.Schedule;
import sim.engine.SimState;
import arcade.core.agent.cell.Cell;
import arcade.core.agent.cell.CellContainer;
import arcade.core.env.grid.Grid;
import arcade.core.env.lat.Lattice;
import arcade.core.env.loc.Location;
import arcade.core.env.loc.LocationContainer;
import arcade.core.sim.Series;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.patch.agent.cell.PatchCell;
import arcade.patch.agent.cell.PatchCellFactory;
import arcade.patch.env.grid.PatchGrid;
import arcade.patch.env.lat.PatchLattice;
import arcade.patch.env.lat.PatchLatticeFactory;
import arcade.patch.env.loc.PatchLocationFactory;

/**
 * Abstract implementation for patch {@link Simulation} instances.
 */

public abstract class PatchSimulation extends SimState implements Simulation {
    /** {@link arcade.core.sim.Series} object containing this simulation. */
    final PatchSeries series;
    
    /** Random number generator seed for this simulation. */
    final int seed;
    
    /** {@link Grid} containing agents in the simulation. */
    Grid grid;
    
    /** Map of {@link Lattice} objects in the simulation. */
    HashMap<String, Lattice> lattices;
    
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
    public final Lattice getLattice(String key) { return lattices.get(key); }
    
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
     * Creates a factory for locations.
     *
     * @return  a {@link arcade.core.env.loc.Location} factory
     */
    public abstract PatchLocationFactory makeLocationFactory();
    
    /**
     * Creates a factory for cells.
     *
     * @return  a {@link arcade.core.agent.cell.Cell} factory
     */
    public abstract PatchCellFactory makeCellFactory();
    
    /**
     * Creates a factory for lattices.
     *
     * @return  a {@link arcade.core.env.lat.Lattice} factory
     */
    public abstract PatchLatticeFactory makeLatticeFactory();
    
    @Override
    public final void setupAgents() {
        // Initialize grid for agents.
        grid = new PatchGrid();
        
        // Create factories.
        PatchLocationFactory locationFactory = makeLocationFactory();
        PatchCellFactory cellFactory = makeCellFactory();
        
        // Initialize factories.
        locationFactory.initialize(series, random);
        cellFactory.initialize(series, random);
        
        // Iterate through each population to create and schedule cells.
        for (MiniBox population : series.populations.values()) {
            int pop = population.getInt("CODE");
            HashSet<Integer> ids = cellFactory.popToIDs.get(pop);
            
            for (int i : ids) {
                // Get location and cell containers.
                LocationContainer locationContainer = locationFactory.locations.get(i);
                CellContainer cellContainer = cellFactory.cells.get(i);
                
                // Check that we have enough containers.
                if (locationContainer == null || cellContainer == null) { break; }
                
                // Make the location and cell.
                Location location = locationContainer.convert(locationFactory, cellContainer);
                PatchCell cell = (PatchCell) cellContainer.convert(cellFactory, location);
                
                // Add and schedule the cell.
                grid.addObject(cell, location);
                cell.schedule(schedule);
                
                // Update id tracking.
                id = Math.max(i, id);
            }
        }
    }
    
    @Override
    public final void setupEnvironment() {
        // Initialize lattice map for layers.
        lattices = new HashMap<>();
        
        // Create factory.
        PatchLatticeFactory latticeFactory = makeLatticeFactory();
        
        // Initialize factory.
        latticeFactory.initialize(series, random);
        
        // Iterate through each layer to create and schedule lattices.
        for (String key : series.layers.keySet()) {
            PatchLattice lattice = latticeFactory.lattices.get(key);
            
            // Add and schedule the lattice.
            lattices.put(key, lattice);
            lattice.schedule(schedule);
        }
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
