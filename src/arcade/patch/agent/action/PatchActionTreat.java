package arcade.patch.agent.action;

import java.util.ArrayList;
import java.util.Objects;
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
import arcade.patch.util.PatchEnums.Ordering;

/**
 * Implementation of {@link Action} for removing cell agents.
 *
 * <p>The action is stepped once after {@code TIME_DELAY}. The {@code TreatAction} will add CAR
 * T-cell agents of specified dose and ratio next to source points or vasculature.
 */
public class PatchActionTreat implements Action {
    /** Serialization version identifier */
    private static final long serialVersionUID = 0;

    /** Delay before calling the helper (in minutes) */
    private final int delay;

    /** Total number of CAR T-cells to treat with */
    private final int dose;

    /** List of fraction of each population to treat with. CD4 to CD8 ratio */
    private final double treatFrac;

    /** Maximum damage value at which T-cells can spawn next to in source or pattern source */
    private double max_damage;

    /** Minimum radius value at which T- cells can spawn next to in graph source */
    private final double min_damage_radius;

    /** Number of agent positions per lattice site */
    private int latPositions;

    /** Coordinate system used for simulation */
    private final String coord;

    /** List of populations. */
    private final ArrayList<MiniBox> populations;

    /** parameters */
    MiniBox parameters;

    /** Maximum confluency of cells in any location */
    final int maxConfluency;

    /** location of available places to insert T cells. For testing purposes only */
    private ArrayList<Location> siteLocations;

    /**
     * Creates an {@code Action} to add agents after a delay.
     *
     * @param series the simulation series
     * @param parameters the component parameters dictionary
     */
    public PatchActionTreat(Series series, MiniBox parameters) {
        // Set loaded parameters.
        this.delay = parameters.getInt("TIME_DELAY");
        this.dose = parameters.getInt("DOSE");
        this.treatFrac = parameters.getDouble("RATIO");
        this.max_damage = parameters.getDouble("MAX_DAMAGE_SEED");
        this.min_damage_radius = parameters.getDouble("MIN_RADIUS_SEED");
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

        // Initialize population register.
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
     * Steps the helper to insert cells of the treatment population(s).
     *
     * @param simstate the MASON simulation state
     */
    public void step(SimState simstate) {

        PatchSimulation sim = (PatchSimulation) simstate;
        String type = "null";
        PatchGrid grid = (PatchGrid) sim.getGrid();
        PatchComponentSites comp = (PatchComponentSites) sim.getComponent("SITES");

        ArrayList<LocationContainer> locs = sim.getLocations();

        ArrayList<Location> siteLocs0 = new ArrayList<Location>();
        ArrayList<Location> siteLocs1 = new ArrayList<Location>();
        ArrayList<Location> siteLocs2 = new ArrayList<Location>();
        ArrayList<Location> siteLocs3 = new ArrayList<Location>();
        ArrayList<Location> siteLocs = new ArrayList<Location>();

        // Determine type of sites component implemented.
        if (comp instanceof PatchComponentSitesSource) {
            type = "source";
        } else if (comp instanceof PatchComponentSitesPattern) {
            type = "pattern";
        } else if (comp instanceof PatchComponentSitesGraph) {
            type = "graph";
        }

        // Find sites without specified level of damage based on component type.
        switch (type) {
            case "source":
            case "pattern":
                double[][][] damage;
                boolean[][][] sitesLat;

                if (type.equals("source")) {
                    damage = ((PatchComponentSitesSource) comp).getDamage();
                    sitesLat = ((PatchComponentSitesSource) comp).getSources();
                } else {
                    damage = ((PatchComponentSitesPattern) comp).getDamage();
                    sitesLat = ((PatchComponentSitesPattern) comp).getPatterns();
                }

                // Iterate through list of locations and remove locations not next to a site.
                for (LocationContainer l : locs) {
                    PatchLocationContainer contain = (PatchLocationContainer) l;
                    PatchLocation loc =
                            (PatchLocation)
                                    contain.convert(
                                            sim.locationFactory,
                                            sim.cellFactory.createCellForPopulation(
                                                    0, populations.get(0).getInt("CODE")));
                    CoordinateXYZ coord = (CoordinateXYZ) loc.getSubcoordinate();
                    int z = coord.z;
                    if (sitesLat[z][coord.x][coord.y]
                            && damage[z][coord.x][coord.y] <= this.max_damage) {
                        addCellsIntoList(grid, loc, siteLocs0, siteLocs1, siteLocs2, siteLocs3);
                    }
                }
                break;

            case "graph":
                Graph G = ((PatchComponentSitesGraph) comp).getGraph();
                Bag allEdges = new Bag(G.getAllEdges());
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
                            if (edge.getRadius() >= min_damage_radius) {
                                addCellsIntoList(
                                        grid, loc, siteLocs0, siteLocs1, siteLocs2, siteLocs3);
                            }
                        }
                    }
                }
                break;
        }

        Utilities.shuffleList(siteLocs3, sim.random);
        Utilities.shuffleList(siteLocs2, sim.random);
        Utilities.shuffleList(siteLocs1, sim.random);
        Utilities.shuffleList(siteLocs0, sim.random);
        siteLocs.addAll(siteLocs3);
        siteLocs.addAll(siteLocs2);
        siteLocs.addAll(siteLocs1);
        siteLocs.addAll(siteLocs0);
        siteLocations = (ArrayList<Location>) siteLocs.clone();
        insert(siteLocs, simstate);
    }

    private void addCellsIntoList(
            PatchGrid grid,
            Location loc,
            ArrayList<Location> siteLocs0,
            ArrayList<Location> siteLocs1,
            ArrayList<Location> siteLocs2,
            ArrayList<Location> siteLocs3) {
        Bag bag = new Bag(grid.getObjectsAtLocation(loc));
        int numAgents = bag.numObjs;

        if (numAgents == 0) {
            for (int p = 0; p < latPositions; p++) {
                siteLocs0.add(loc);
            }
        } else if (numAgents == 1) {
            for (int p = 0; p < latPositions; p++) {
                siteLocs1.add(loc);
            }
        } else if (numAgents == 2) {
            for (int p = 0; p < latPositions; p++) {
                siteLocs2.add(loc);
            }
        } else {
            for (int p = 0; p < latPositions; p++) {
                siteLocs3.add(loc);
            }
        }
        // Remove break statement if more than one per hex can appear
        // with break statement, each location can only be added to list once
        // without it, places with more vasc sites get added more times to list
        // break;
    }

    private void insert(ArrayList<Location> coordinates, SimState simstate) {
        PatchSimulation sim = (PatchSimulation) simstate;
        PatchGrid grid = (PatchGrid) sim.getGrid();
        Utilities.shuffleList(coordinates, sim.random);

        int cd4Code = 0;
        int cd8Code = 0;

        for (MiniBox population : populations) {
            String className = population.get("CLASS");
            if (className.equals("cart_cd4")) {
                cd4Code = population.getInt("CODE");
            }
            if (className.equals("cart_cd8")) {
                cd8Code = population.getInt("CODE");
            }
        }

        for (int i = 0; i < dose; i++) {

            int id = sim.getID();

            int pop = cd4Code;

            if (sim.random.nextDouble() > treatFrac) {
                pop = cd8Code;
            }

            PatchLocation loc = ((PatchLocation) coordinates.remove(0));
            Coordinate coord = loc.getCoordinate();

            // find available location space
            while (!coordinates.isEmpty() && !checkLocationSpace(loc, grid)) {
                loc = (PatchLocation) coordinates.remove(0);
            }

            if (coordinates.isEmpty()) {
                break;
            }

            PatchLocationContainer locationContainer = new PatchLocationContainer(id, coord);
            PatchCellContainer cellContainer = sim.cellFactory.createCellForPopulation(id, pop);
            Location location = locationContainer.convert(sim.locationFactory, cellContainer);
            PatchCell cell =
                    (PatchCell) cellContainer.convert(sim.cellFactory, location, sim.random);
            grid.addObject(cell, location);
            cell.schedule(sim.getSchedule());
        }
    }

    protected boolean checkLocationSpace(Location loc, PatchGrid grid) {
        boolean available;
        int locMax = this.maxConfluency;
        double locVolume = ((PatchLocation) loc).getVolume();
        double locArea = ((PatchLocation) loc).getArea();

        Bag bag = new Bag(grid.getObjectsAtLocation(loc));
        int n = bag.numObjs; // number of agents in location

        if (n == 0) {
            available = true;
        } // no cells in location
        else if (n >= locMax) {
            available = false;
        } // location already full
        else {
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

    /**
     * Returns registered populations for the action. Exists for testing purposes only
     *
     * @return registered populations for the action
     */
    public ArrayList<MiniBox> getPopulations() {
        return populations;
    }

    /**
     * Returns locations of sites available for insertion. Exists for testing purposes only
     *
     * @return Returns locations of sites available for insertion
     */
    public ArrayList<Location> getSiteLocs() {
        return siteLocations;
    }
}
