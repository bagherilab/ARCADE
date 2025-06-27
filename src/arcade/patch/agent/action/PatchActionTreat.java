package arcade.patch.agent.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.util.Bag;
import arcade.core.agent.action.Action;
import arcade.core.env.location.Location;
import arcade.core.env.location.LocationContainer;
import arcade.core.sim.Series;
import arcade.core.sim.Simulation;
import arcade.core.util.Graph;
import arcade.core.util.MiniBox;
import arcade.core.util.Utilities;
import arcade.patch.agent.cell.PatchCell;
import arcade.patch.agent.cell.PatchCellCART;
import arcade.patch.agent.cell.PatchCellContainer;
import arcade.patch.agent.cell.PatchCellTissue;
import arcade.patch.env.component.PatchComponentSites;
import arcade.patch.env.component.PatchComponentSitesGraph;
import arcade.patch.env.component.PatchComponentSitesGraph.SiteEdge;
import arcade.patch.env.component.PatchComponentSitesGraphRect;
import arcade.patch.env.component.PatchComponentSitesGraphTri;
import arcade.patch.env.component.PatchComponentSitesPattern;
import arcade.patch.env.component.PatchComponentSitesSource;
import arcade.patch.env.grid.PatchGrid;
import arcade.patch.env.location.Coordinate;
import arcade.patch.env.location.CoordinateXYZ;
import arcade.patch.env.location.PatchLocation;
import arcade.patch.env.location.PatchLocationContainer;
import arcade.patch.sim.PatchSeries;
import arcade.patch.sim.PatchSimulation;
import arcade.patch.util.PatchEnums.Immune;
import arcade.patch.util.PatchEnums.Ordering;

/**
 * Implementation of {@link Action} for inserting T cell agents.
 *
 * <p>The action is stepped once after {@code TIME_DELAY}. The {@code TreatAction} will add CAR
 * T-cell agents of specified dose next to source points or vasculature.
 */
public class PatchActionTreat implements Action {

    /** Delay before calling the helper (in minutes). */
    private final int delay;

    /** Total number of CAR T-cells to treat with. */
    private final int dose;

    /** Maximum damage value at which T-cells can spawn next to in source or pattern source. */
    private double maxDamage;

    /** Minimum radius value at which T- cells can spawn next to in graph source. */
    private final double minDamageRadius;

    /** Number of agent positions per lattice site. */
    private int latPositions;

    /** Coordinate system used for simulation. */
    private final String coord;

    /** List of populations. */
    private final ArrayList<MiniBox> populations;

    /** parameters. */
    MiniBox parameters;

    /** Maximum confluency of cells in any location. */
    final int maxConfluency;

    /**
     * Creates an {@code Action} to add agents after a delay.
     *
     * @param series the simulation series
     * @param parameters the component parameters dictionary
     */
    public PatchActionTreat(Series series, MiniBox parameters) {
        this.delay = parameters.getInt("TIME_DELAY");
        this.dose = parameters.getInt("DOSE");
        this.maxDamage = parameters.getDouble("MAX_DAMAGE_SEED");
        this.minDamageRadius = parameters.getDouble("MIN_RADIUS_SEED");
        this.maxConfluency = parameters.getInt("MAX_DENSITY");
        this.parameters = parameters;

        this.coord =
                ((PatchSeries) series).patch.get("GEOMETRY").equalsIgnoreCase("HEX")
                        ? "Hex"
                        : "Rect";
        if (coord.equals("Hex")) {
            latPositions = 9;
        }
        if (coord.equals("Rect")) {
            latPositions = 16;
        }

        populations = new ArrayList<>();
    }

    @Override
    public void schedule(Schedule schedule) {
        schedule.scheduleOnce(delay, Ordering.ACTIONS.ordinal(), this);
    }

    @Override
    public void register(Simulation sim, String population) {
        populations.add(sim.getSeries().populations.get(population));
    }

    /**
     * Steps the action to insert cells of the treatment population(s).
     *
     * @param simstate the MASON simulation state
     */
    public void step(SimState simstate) {
        PatchSimulation sim = (PatchSimulation) simstate;
        String type = "null";
        PatchGrid grid = (PatchGrid) sim.getGrid();
        PatchComponentSites comp = (PatchComponentSites) sim.getComponent("SITES");

        // Determine type of sites component implemented.
        if (comp instanceof PatchComponentSitesSource) {
            type = "source";
        } else if (comp instanceof PatchComponentSitesPattern) {
            type = "pattern";
        } else if (comp instanceof PatchComponentSitesGraph) {
            type = "graph";
        }

        Set<String> immuneCells =
                Arrays.stream(Immune.values()).map(Enum::name).collect(Collectors.toSet());

        for (MiniBox population : populations) {
            String className = population.get("CLASS").toUpperCase();

            if (!immuneCells.contains(className)) {
                throw new IllegalArgumentException(
                        "Population "
                                + population.get("CLASS")
                                + " is not an immune cell and cannot be treated.");
            }

            int pop = population.getInt("CODE");

            ArrayList<LocationContainer> locs = sim.getLocations();
            ArrayList<Location> siteLocs = new ArrayList<Location>();

            // Find sites without specified level of damage based on component type.
            findLocations(comp, type, locs, siteLocs, sim);
            Utilities.shuffleList(siteLocs, sim.random);
            // sort locations in descending order from highest to lowest density
            siteLocs.sort(Comparator.comparingInt(l -> -computeDensity(grid, l)));
            insert(siteLocs, simstate, pop);
        }
    }

    /**
     * Helper method to find possible locations to insert t cells.
     *
     * @param comp the component
     * @param type the type of component (source, pattern, or graph)
     * @param locs the locations to check
     * @param siteLocs the locations that meet the criteria
     * @param sim the simulation instance
     * @throws IllegalArgumentException if the component type is invalid
     */
    private void findLocations(
            PatchComponentSites comp,
            String type,
            ArrayList<LocationContainer> locs,
            ArrayList<Location> siteLocs,
            PatchSimulation sim) {
        if (type.equals("graph")) {
            findGraphSites(comp, locs, siteLocs);
        } else if (type.equals("source") || type.equals("pattern")) {
            double[][][] damage;
            boolean[][][] sitesLat;

            if (type.equals("source")) {
                damage = ((PatchComponentSitesSource) comp).getDamage();
                sitesLat = ((PatchComponentSitesSource) comp).getSources();
            } else {
                damage = ((PatchComponentSitesPattern) comp).getDamage();
                sitesLat = ((PatchComponentSitesPattern) comp).getPatterns();
            }
            pruneSite(locs, sim, damage, sitesLat, siteLocs);
        } else {
            throw new IllegalArgumentException(
                    "Invalid component type: "
                            + type
                            + ". Must be of type source, pattern, or graph.");
        }
    }

    /**
     * Helper method to check if radius is wide enough for T-cells to pass through.
     *
     * @param comp the component
     * @param locs the locations to check
     * @param siteLocs the locations that meet the criteria
     */
    private void findGraphSites(
            PatchComponentSites comp,
            ArrayList<LocationContainer> locs,
            ArrayList<Location> siteLocs) {
        Graph graph = ((PatchComponentSitesGraph) comp).getGraph();
        Bag allEdges = new Bag(graph.getAllEdges());
        PatchComponentSitesGraph graphSites = (PatchComponentSitesGraph) comp;

        for (Object edgeObj : allEdges) {
            SiteEdge edge = (SiteEdge) edgeObj;
            Bag allEdgeLocs = new Bag();
            if (Objects.equals(coord, "Hex")) {
                allEdgeLocs.add(
                        ((PatchComponentSitesGraphTri) graphSites)
                                .getSpan(edge.getFrom(), edge.getTo()));
            } else {
                allEdgeLocs.add(
                        ((PatchComponentSitesGraphRect) graphSites)
                                .getSpan(edge.getFrom(), edge.getTo()));
            }

            for (Object locObj : allEdgeLocs) {
                Location loc = (Location) locObj;
                if (locs.contains(loc)) {
                    if (edge.getRadius() >= minDamageRadius) {
                        for (int p = 0; p < latPositions; p++) {
                            siteLocs.add(loc);
                        }
                    }
                }
            }
        }
    }

    /**
     * Helper method to remove locations that are not next to a site or have too much damage for
     * T-cells to pass through.
     *
     * @param locs the locations to check
     * @param sim the simuation instance
     * @param damage the damage array for sites
     * @param sitesLat the lattice array for sites
     * @param siteLocs the locations that meet the criteria
     */
    public void pruneSite(
            ArrayList<LocationContainer> locs,
            PatchSimulation sim,
            double[][][] damage,
            boolean[][][] sitesLat,
            ArrayList<Location> siteLocs) {
        for (LocationContainer l : locs) {
            PatchLocationContainer contain = (PatchLocationContainer) l;
            PatchLocation loc =
                    (PatchLocation)
                            contain.convert(
                                    sim.locationFactory,
                                    sim.cellFactory.createCellForPopulation(
                                            0, populations.get(0).getInt("CODE")));
            CoordinateXYZ coordinate = (CoordinateXYZ) loc.getSubcoordinate();
            int z = coordinate.z;
            if (sitesLat[z][coordinate.x][coordinate.y]
                    && damage[z][coordinate.x][coordinate.y] <= this.maxDamage) {
                for (int p = 0; p < latPositions; p++) {
                    siteLocs.add(loc);
                }
            }
        }
    }

    /**
     * Helper method to sort locations.
     *
     * @param grid the simulation grid
     * @param loc the current location being looked
     * @return the density of agents at the location
     */
    private int computeDensity(PatchGrid grid, Location loc) {
        Bag bag = new Bag(grid.getObjectsAtLocation(loc));
        int numAgents = bag.numObjs;
        return numAgents;
    }

    /**
     * Helper method to add cells into the grid.
     *
     * @param coordinates the locations to insert the cells
     * @param simstate the simulation state
     */
    private void insert(ArrayList<Location> coordinates, SimState simstate, int pop) {
        PatchSimulation sim = (PatchSimulation) simstate;
        PatchGrid grid = (PatchGrid) sim.getGrid();
        Utilities.shuffleList(coordinates, sim.random);

        for (int i = 0; i < dose; i++) {
            int id = sim.getID();

            PatchLocation loc = ((PatchLocation) coordinates.remove(0));

            while (!coordinates.isEmpty() && !checkLocationSpace(loc, grid)) {
                loc = ((PatchLocation) coordinates.remove(0));
            }

            if (coordinates.isEmpty()) {
                break;
            }

            Coordinate coordinate = loc.getCoordinate();
            PatchLocationContainer locationContainer = new PatchLocationContainer(id, coordinate);
            PatchCellContainer cellContainer = sim.cellFactory.createCellForPopulation(id, pop);

            Location location = locationContainer.convert(sim.locationFactory, cellContainer);
            PatchCell cell =
                    (PatchCell) cellContainer.convert(sim.cellFactory, location, sim.random);

            grid.addObject(cell, location);
            cell.schedule(sim.getSchedule());
        }
    }

    /**
     * Helper method to check if location is available.
     *
     * @param grid the simulation grid
     * @param loc the current location being looked at
     * @return boolean indicating if location is free
     */
    protected boolean checkLocationSpace(Location loc, PatchGrid grid) {
        boolean available;
        int locMax = this.maxConfluency;
        double locVolume = ((PatchLocation) loc).getVolume();
        double locArea = ((PatchLocation) loc).getArea();

        Bag bag = new Bag(grid.getObjectsAtLocation(loc));
        int n = bag.numObjs; // number of agents in location

        if (n == 0) {
            // no cells in location
            available = true;
        } else if (n >= locMax) {
            // location already full
            available = false;
        } else {
            available = true;
            double totalVol = PatchCell.calculateTotalVolume(bag);
            double currentHeight = totalVol / locArea;

            if (totalVol > locVolume) {
                available = false;
            }

            for (Object cellObj : bag) {
                PatchCell cell = (PatchCell) cellObj;
                if (cell instanceof PatchCellCART) {
                    totalVol =
                            PatchCell.calculateTotalVolume(bag)
                                    + parameters.getDouble("T_CELL_VOL_AVG");
                    currentHeight = totalVol / locArea;
                }
                if (cell instanceof PatchCellTissue) {
                    if (currentHeight > cell.getCriticalHeight()) {
                        available = false;
                    }
                }
            }
        }

        return available;
    }
}
