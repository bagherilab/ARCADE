package arcade.patch.agent.action;

import java.util.ArrayList;
import sim.engine.Schedule;
import sim.engine.SimState;
import arcade.core.agent.action.Action;
import arcade.core.env.loc.Location;
import arcade.core.sim.Series;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.core.util.Utilities;
import arcade.patch.agent.cell.PatchCell;
import arcade.patch.agent.cell.PatchCellContainer;
import arcade.patch.env.grid.PatchGrid;
import arcade.patch.env.loc.Coordinate;
import arcade.patch.env.loc.PatchLocationContainer;
import arcade.patch.sim.PatchSeries;
import arcade.patch.sim.PatchSimulation;
import static arcade.patch.util.PatchEnums.Ordering;

/**
 * Implementation of {@link Action} for inserting cell agents.
 * <p>
 * The action is stepped once after {@code TIME_DELAY}. The action will insert a
 * mixture of {@code INSERT_NUMBER} cells from each of the registered
 * populations into locations within the specified radius {@code INSERT_RADIUS}
 * from the center of the simulation.
 */

public class PatchActionInsert implements Action {
    /** Time delay before calling the action [min]. */
    private final int timeDelay;
    
    /** Grid radius that cells are inserted into. */
    private final int insertRadius;
    
    /** Grid depth that cells are inserted into. */
    private final int insertDepth;
    
    /** Number of cells to insert from each population. */
    private final int insertNumber;
    
    /** List of populations. */
    private final ArrayList<MiniBox> populations;
    
    /**
     * Creates a {@link Action} for removing cell agents.
     * <p>
     * Loaded parameters include:
     * <ul>
     *     <li>{@code TIME_DELAY} = time delay before calling the action</li>
     *     <li>{@code INSERT_RADIUS} = grid radius that cells are inserted
     *         into</li>
     *     <li>{@code INSERT_NUMBER} = number of cells to insert from each
     *         population</li>
     * </ul>
     *
     * @param series  the simulation series
     * @param parameters  the component parameters dictionary
     */
    public PatchActionInsert(Series series, MiniBox parameters) {
        int maxRadius = ((PatchSeries) series).radius;
        
        // Set loaded parameters.
        timeDelay = parameters.getInt("TIME_DELAY");
        insertRadius = Math.min(maxRadius, parameters.getInt("INSERT_RADIUS"));
        insertDepth = ((PatchSeries) series).depth;
        insertNumber = parameters.getInt("INSERT_NUMBER");
        
        // Initialize population register.
        populations = new ArrayList<>();
    }
    
    @Override
    public void schedule(Schedule schedule) {
        schedule.scheduleOnce(timeDelay, Ordering.ACTIONS.ordinal(), this);
    }
    
    @Override
    public void register(Simulation sim, String population) {
        populations.add(sim.getSeries().populations.get(population));
    }
    
    @Override
    public void step(SimState simstate) {
        PatchSimulation sim = (PatchSimulation) simstate;
        PatchGrid grid = (PatchGrid) sim.getGrid();
        
        // Select valid coordinates to insert into and shuffle.
        ArrayList<Coordinate> coordinates =
                sim.locationFactory.getCoordinates(insertRadius, insertDepth);
        Utilities.shuffleList(coordinates, sim.random);
        
        // Add cells from each population into insertion area.
        for (MiniBox population : populations) {
            int pop = population.getInt("CODE");
            
            for (int i = 0; i < insertNumber; i++) {
                int id = sim.getID();
                
                if (coordinates.isEmpty()) {
                    break;
                }
                
                Coordinate coord = coordinates.remove(0);
                PatchLocationContainer locationContainer = new PatchLocationContainer(id, coord);
                PatchCellContainer cellContainer = sim.cellFactory.createCellForPopulation(id, pop);
                
                Location location = locationContainer.convert(sim.locationFactory, cellContainer);
                PatchCell cell = (PatchCell) cellContainer.convert(sim.cellFactory, location);
                
                grid.addObject(cell, location);
                cell.schedule(sim.getSchedule());
            }
        }
    }
}
