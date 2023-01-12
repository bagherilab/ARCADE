package arcade.patch.agent.action;

import java.util.ArrayList;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.util.distribution.Normal;
import sim.util.distribution.Uniform;
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
import static arcade.core.util.Enums.State;
import static arcade.patch.util.PatchEnums.Ordering;

/**
 * Implementation of {@link Action} for inserting cell agents.
 * <p>
 * {@code PatchActionInsert} is stepped once.
 * The {@code PatchActionInsert} will insert a mixture of cells from the
 * specified populations into locations within the specified radius from the
 * center of the simulation.
 */

public class PatchActionInsert implements Action {
    /** Time delay before calling the action (in minutes). */
    private final int timeDelay;
    
    /** Grid radius that cells are inserted into. */
    private final int insertRadius;
    
    /** Grid depth that cells are inserted into. */
    private final int insertDepth;
    
    /** Number of cells to insert from each population. */
    private final int insertNumber;
    
    /** List of populations. */
    ArrayList<MiniBox> populations;
    
    /**
     * Creates a {@link Action} for removing cell agents.
     * <p>
     * Loaded parameters include:
     * <ul>
     *     <li>{@code TIME_DELAY} = time delay before calling the action (in minutes)</li>
     *     <li>{@code INSERT_RADIUS} = grid radius that cells are inserted into</li>
     *     <li>{@code INSERT_NUMBER} = number of cells to insert from each population</li>
     * </ul>
     *
     * @param series  the simulation series
     * @param parameters  the component parameters dictionary
     */
    public PatchActionInsert(Series series, MiniBox parameters) {
        int maxRadius = ((PatchSeries) series).radius;
        
        timeDelay = parameters.getInt("insert/TIME_DELAY");
        insertRadius = Math.min(maxRadius, parameters.getInt("insert/INSERT_RADIUS"));
        insertDepth = ((PatchSeries) series).depth;
        insertNumber = parameters.getInt("insert/INSERT_NUMBER");
        
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
    public void step(SimState state) {
        PatchSimulation sim = (PatchSimulation) state;
        PatchGrid grid = (PatchGrid) sim.getGrid();
        
        // Select valid coordinates to insert into and shuffle.
        ArrayList<Coordinate> coordinates =
                sim.locationFactory.getCoordinates(insertRadius, insertDepth);
        Utilities.shuffleList(coordinates, sim.random);
        
        // Add cells from each population into insertion area.
        for (MiniBox population : populations) {
            int pop = population.getInt("CODE");
            
            Normal volumes = sim.cellFactory.popToCriticalVolumes.get(pop);
            Normal heights = sim.cellFactory.popToCriticalHeights.get(pop);
            Uniform ages = sim.cellFactory.popToAges.get(pop);
            
            int divisions = sim.cellFactory.popToDivisions.get(pop);
            double compression = sim.cellFactory.popToCompression.get(pop);
            
            for (int i = 0; i < insertNumber; i++) {
                int id = sim.getID();
                
                double volume = volumes.nextDouble();
                double height = heights.nextDouble();
                int age = ages.nextInt();
                
                Coordinate coordinate = coordinates.remove(i);
                
                if (coordinate == null) {
                    break;
                }
                
                PatchLocationContainer locationContainer =
                        new PatchLocationContainer(id, coordinate);
                PatchCellContainer cellContainer = new PatchCellContainer(id, 0, pop,
                        age, divisions, State.UNDEFINED, volume, height,
                        volume, height + compression);
                
                Location location = locationContainer.convert(sim.locationFactory, cellContainer);
                PatchCell cell = (PatchCell) cellContainer.convert(sim.cellFactory, location);
                
                grid.addObject(cell, location);
                cell.schedule(sim.getSchedule());
            }
        }
    }
}
