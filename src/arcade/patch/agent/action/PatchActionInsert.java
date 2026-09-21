package arcade.patch.agent.action;

import java.util.ArrayList;
import sim.engine.Schedule;
import sim.engine.SimState;
import arcade.core.agent.action.Action;
import arcade.core.env.location.Location;
import arcade.core.sim.Series;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.core.util.Utilities;
import arcade.patch.agent.cell.PatchCell;
import arcade.patch.agent.cell.PatchCellContainer;
import arcade.patch.env.grid.PatchGrid;
import arcade.patch.env.location.Coordinate;
import arcade.patch.env.location.PatchLocation;
import arcade.patch.env.location.PatchLocationContainer;
import arcade.patch.sim.PatchSeries;
import arcade.patch.sim.PatchSimulation;
import arcade.patch.util.PatchEnums.Ordering;
import static arcade.patch.util.PatchEnums.Ordering;

/**
 * Implementation of {@link Action} for inserting cell agents.
 *
 * <p>The action is stepped once after {@code TIME_DELAY}. The action will insert a mixture of
 * {@code INSERT_NUMBER} cells from each of the registered populations into locations within the
 * specified radius {@code INSERT_RADIUS} from the center of the simulation.
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

    /** Whether the insertion is to confluence. */
    private final boolean confluence;

    /** Number of cells placed. */
    private int cellsPlaced;

    /**
     * Creates a {@link Action} for removing cell agents.
     *
     * <p>Loaded parameters include:
     *
     * <ul>
     *   <li>{@code TIME_DELAY} = time delay before calling the action
     *   <li>{@code INSERT_RADIUS} = grid radius that cells are inserted into
     *   <li>{@code INSERT_NUMBER} = number of cells to insert from each population
     * </ul>
     *
     * @param series the simulation series
     * @param parameters the component parameters dictionary
     */
    public PatchActionInsert(Series series, MiniBox parameters) {
        int maxRadius = ((PatchSeries) series).radius;

        // Set loaded parameters.
        timeDelay = parameters.getInt("TIME_DELAY");
        insertRadius = Math.min(maxRadius, parameters.getInt("INSERT_RADIUS"));
        insertDepth = ((PatchSeries) series).depth;
        insertNumber = parameters.getInt("INSERT_NUMBER");
        confluence = parameters.getInt("CONFLUENCE") > 0;

        // Initialize population register.
        populations = new ArrayList<>();

        // Initialize number of cells placed.
        cellsPlaced = 0;
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

            while (cellsPlaced < insertNumber) {

                if (coordinates.isEmpty()) {
                    break;
                }

                Coordinate coord = coordinates.remove(0);

                // Create a new cell and location.
                // place multiple cells in same location if confluence is true, otherwise place one
                // cell per location
                if (confluence) {
                    boolean free = true;
                    while (free && cellsPlaced < insertNumber) {
                        // Create a new location and cell.
                        PatchCell cell = generateCell(sim, coord, pop);
                        Location location = cell.getLocation();
                        free =
                                PatchCell.checkLocation(
                                        sim,
                                        (PatchLocation) location,
                                        cell.getVolume(),
                                        cell.getCriticalHeight(),
                                        pop,
                                        cell.getMaxDensity());
                        if (free) {
                            addCellToLocation(sim, grid, cell, location);
                        }
                    }
                } else {
                    PatchCell cell = generateCell(sim, coord, pop);
                    Location location = cell.getLocation();
                    addCellToLocation(sim, grid, cell, location);
                }
            }
        }
    }

    /**
     * Adds a cell to a location in the grid.
     *
     * @param sim the simulation series
     * @param grid the patch grid
     * @param cell the cell to add
     * @param location the location to add the cell to
     */
    private void addCellToLocation(
            PatchSimulation sim, PatchGrid grid, PatchCell cell, Location location) {
        grid.addObject(cell, location);
        cell.schedule(sim.getSchedule());
        cellsPlaced++;
    }

    /**
     * Generates a new cell for the simulation.
     *
     * @param sim the simulation series
     * @param coord the coordinate to generate the cell at
     * @param pop the population to generate the cell for
     * @return the generated cell
     */
    private PatchCell generateCell(PatchSimulation sim, Coordinate coord, int pop) {
        int id = sim.getID();
        PatchLocationContainer locationContainer = new PatchLocationContainer(id, coord);
        PatchCellContainer cellContainer = sim.cellFactory.createCellForPopulation(id, pop);
        Location location = locationContainer.convert(sim.locationFactory, cellContainer);
        PatchCell cell = (PatchCell) cellContainer.convert(sim.cellFactory, location, sim.random);
        return cell;
    }
}
