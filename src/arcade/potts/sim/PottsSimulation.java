package arcade.potts.sim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;
import sim.engine.Schedule;
import sim.engine.SimState;
import arcade.core.agent.action.Action;
import arcade.core.agent.cell.Cell;
import arcade.core.agent.cell.CellContainer;
import arcade.core.agent.cell.CellFactory;
import arcade.core.env.component.Component;
import arcade.core.env.grid.Grid;
import arcade.core.env.lattice.Lattice;
import arcade.core.env.location.Location;
import arcade.core.env.location.LocationContainer;
import arcade.core.env.location.LocationFactory;
import arcade.core.sim.Series;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.potts.agent.action.PottsActionInsert;
import arcade.potts.agent.action.PottsActionRecruit;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.agent.cell.PottsCellFactory;
import arcade.potts.env.component.PottsComponentOxygenSource;
import arcade.potts.env.component.PottsComponentVessels;
import arcade.potts.env.grid.PottsGrid;
import arcade.potts.env.lattice.PottsLattice;
import arcade.potts.env.lattice.PottsLatticeFactory;
import arcade.potts.env.location.PottsLocation;
import arcade.potts.env.location.PottsLocation2D;
import arcade.potts.env.location.PottsLocation3D;
import arcade.potts.env.location.PottsLocationFactory;
import arcade.potts.env.location.PottsLocationFactory3D;
import arcade.potts.env.location.Voxel;
import arcade.potts.sim.hamiltonian.ChemotaxisHamiltonian;
import arcade.potts.sim.hamiltonian.Hamiltonian;
import arcade.potts.util.PottsEnums.Ordering;
import arcade.potts.util.PottsEnums.Term;

/** Abstract implementation for potts {@link Simulation} instances. */
public abstract class PottsSimulation extends SimState implements Simulation {
    /** {@link arcade.core.sim.Series} object containing this simulation. */
    final PottsSeries series;

    /** Random number generator seed for this simulation. */
    final int seed;

    /** {@link arcade.potts.sim.Potts} object for the simulation. */
    Potts potts;

    /** {@link arcade.core.env.grid.Grid} containing agents in the simulation. */
    Grid grid;

    /** Map of layer id to lattice. */
    Map<String, PottsLattice> lattices;

    /** Map of action id to {@link Action} instance. */
    HashMap<String, Action> actions;

    /** Map of component id to {@link Component} instance. */
    HashMap<String, Component> components;

    /** Cell ID tracker. */
    int id;

    /** Cell factory instance for the simulation. */
    public final PottsCellFactory cellFactory;

    /** Location factory instance for the simulation. */
    public final PottsLocationFactory locationFactory;

    /** Logger for {@code PottsSeries}. */
    private static final Logger LOGGER = Logger.getLogger(PottsSimulation.class.getName());

    /**
     * Simulation instance for a {@link Series} for given random seed.
     *
     * @param seed the random seed for random number generator
     * @param series the simulation series
     */
    public PottsSimulation(long seed, Series series) {
        super(seed);
        this.series = (PottsSeries) series;
        this.seed = (int) seed - Series.SEED_OFFSET;

        this.locationFactory = makeLocationFactory();
        this.cellFactory = makeCellFactory();
    }

    @Override
    public final Series getSeries() {
        return series;
    }

    @Override
    public final Schedule getSchedule() {
        return schedule;
    }

    @Override
    public final int getSeed() {
        return seed;
    }

    @Override
    public final int getID() {
        return ++id;
    }

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
    public final CellFactory getCellFactory() {
        return cellFactory;
    }

    @Override
    public final LocationFactory getLocationFactory() {
        return locationFactory;
    }

    @Override
    public final Grid getGrid() {
        return grid;
    }

    @Override
    public final Lattice getLattice(String key) {
        return lattices == null ? null : lattices.get(key);
    }

    /**
     * Gets the set of lattice keys.
     *
     * @return the set of layer ids
     */
    public final Set<String> getLatticeKeys() {
        return lattices == null ? Collections.emptySet() : lattices.keySet();
    }

    /**
     * Gets all lattice fields, sorted by layer id.
     *
     * @return the map of layer id to field array
     */
    public final Map<String, double[][][]> getLayers() {
        TreeMap<String, double[][][]> layers = new TreeMap<>();
        if (lattices != null) {
            lattices.forEach((key, lattice) -> layers.put(key, lattice.getField()));
        }
        return layers;
    }

    @Override
    public final Action getAction(String key) {
        return actions == null ? null : actions.get(key);
    }

    @Override
    public final Component getComponent(String key) {
        return components == null ? null : components.get(key);
    }

    /**
     * Gets {@link Potts} instance for the simulation.
     *
     * @return the potts instance
     */
    public final Potts getPotts() {
        return potts;
    }

    /**
     * Called at the start of the simulation to set up agents and environment and schedule actions
     * and components as needed.
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

    /** Called at the end of the simulation. */
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
     * @return a {@link arcade.potts.sim.Potts} object
     */
    abstract Potts makePotts();

    /**
     * Creates a factory for locations.
     *
     * @return a {@link arcade.core.env.location.Location} factory
     */
    public abstract PottsLocationFactory makeLocationFactory();

    /**
     * Creates a factory for cells.
     *
     * @return a {@link arcade.core.agent.cell.Cell} factory
     */
    public abstract PottsCellFactory makeCellFactory();

    /**
     * Creates an instance of the given action.
     *
     * @param actionClass the name of the action class
     * @param parameters the dictionary of action parameters
     * @return an {@link Action} instance, or {@code null} if the class is not recognized
     */
    public Action makeAction(String actionClass, MiniBox parameters) {
        switch (actionClass) {
            case "insert":
                return new PottsActionInsert(parameters);
            case "recruit":
                return new PottsActionRecruit(parameters);
            default:
                return null;
        }
    }

    /**
     * Creates an instance of the given component.
     *
     * @param componentClass the name of the component class
     * @param parameters the dictionary of component parameters
     * @return a {@link Component} instance, or {@code null} if the class is not recognized
     */
    public Component makeComponent(String componentClass, MiniBox parameters) {
        switch (componentClass) {
            case "oxygen_source":
                return new PottsComponentOxygenSource(parameters);
            case "vessels":
                return new PottsComponentVessels(parameters);
            default:
                return null;
        }
    }

    /** Sets up the potts layer for the simulation. */
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
                PottsCell cell = (PottsCell) cellContainer.convert(cellFactory, location, random);

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
        PottsLatticeFactory factory = new PottsLatticeFactory();
        Map<String, double[][][]> loaded = null;
        if (series.loader != null && series.loader.loadLayers) {
            loaded = series.loader.loadLayers();
            if (loaded == null) {
                throw new IllegalStateException(
                        "unable to load layers file [ "
                                + series.loader.prefix.replace(
                                        "[#]", String.format("%04d", getSeed()))
                                + ".LAYERS.json ]");
            }
        }
        factory.createLattices(series, loaded);
        lattices = factory.lattices;
        for (PottsLattice lattice : lattices.values()) {
            lattice.schedule(schedule);
        }

        // Pass lattices to the chemotaxis term, if it is used.
        Hamiltonian chemotaxis = potts.getHamiltonian(Term.CHEMOTAXIS);
        if (chemotaxis != null) {
            ((ChemotaxisHamiltonian) chemotaxis).setLattices(lattices);
        }
    }

    @Override
    public final void scheduleActions() {
        // TODO: same as PatchSimulation.scheduleActions; consider moving to the Simulation level
        actions = new HashMap<>();

        // Create all action instances.
        for (String actionKey : series.actions.keySet()) {
            MiniBox actionParameters = series.actions.get(actionKey);
            String actionClass = actionParameters.get("CLASS");
            Action action = makeAction(actionClass, actionParameters);

            if (action == null) {
                continue;
            }

            actions.put(actionKey, action);
        }

        // Register and schedule actions.
        for (String actionKey : actions.keySet()) {
            MiniBox actionParameters = series.actions.get(actionKey);
            Action action = actions.get(actionKey);

            MiniBox registerBox = actionParameters.filter("(REGISTER)");
            for (String registerKey : registerBox.getKeys()) {
                action.register(this, registerKey);
            }

            action.schedule(schedule);
        }
    }

    @Override
    public final void scheduleComponents() {
        // TODO: Consider whether to move scheduleComponents to the Simulation level as
        // I think this
        // is the same as PatchSimulation's scheduleComponents
        components = new HashMap<>();

        // Create all component instances.
        for (String componentKey : series.components.keySet()) {
            MiniBox componentParameters = series.components.get(componentKey);
            String componentClass = componentParameters.get("CLASS");
            Component component = makeComponent(componentClass, componentParameters);

            if (component == null) {
                continue;
            }

            components.put(componentKey, component);
        }

        // Register and schedule components.
        for (String componentKey : components.keySet()) {
            MiniBox componentParameters = series.components.get(componentKey);
            Component component = components.get(componentKey);

            MiniBox registerBox = componentParameters.filter("(REGISTER)");
            for (String registerKey : registerBox.getKeys()) {
                component.register(this, registerKey);
            }

            component.schedule(schedule);
        }
    }

    /**
     * Inserts a new cell of the given population on the given voxels.
     *
     * @param pop the population code
     * @param voxels the voxels for the new cell, which must be empty and connected
     * @return the new cell, or {@code null} if the cell could not be inserted
     */
    public PottsCell insertCell(int pop, ArrayList<Voxel> voxels) {
        // 0. There must be at least one voxel.
        if (voxels.isEmpty()) {
            LOGGER.info("unable to insert cell, no voxels given");
            return null;
        }

        // 0. The population must not use regions.
        if (cellFactory.hasRegions(pop)) {
            LOGGER.info("unable to insert cell, population [ " + pop + " ] has regions");
            return null;
        }

        // 1. All voxels must be empty.
        for (Voxel v : voxels) {
            if (potts.ids[v.z][v.x][v.y] != 0) {
                LOGGER.info("unable to insert cell, voxel [ " + v + " ] is not empty");
                return null;
            }
        }

        // 2. Build the location.
        boolean is3D = locationFactory instanceof PottsLocationFactory3D;
        PottsLocation location = is3D ? new PottsLocation3D(voxels) : new PottsLocation2D(voxels);

        // 3. The voxels must be connected.
        if (!location.isConnected(random)) {
            LOGGER.info("unable to insert cell, voxels are not connected");
            return null;
        }

        // 4. Take an id. 5. Make the container. 6. Convert to a cell.
        int newID = getID();
        CellContainer container = cellFactory.createCellForPopulation(newID, pop);
        PottsCell cell = (PottsCell) container.convert(cellFactory, location, random);

        // 7. Wire the cell in.
        grid.addObject(cell, null);
        potts.register(cell);
        cell.reset(potts.ids, potts.regions);
        cell.schedule(schedule);

        return cell;
    }

    /**
     * Runs output methods.
     *
     * @param isScheduled {@code true} to schedule output, {@code false} otherwise
     */
    public void doOutput(boolean isScheduled) {
        if (isScheduled) {
            series.saver.schedule(schedule);
        } else {
            int tick = (int) schedule.getTime() + 1;
            series.saver.save(tick);
        }
    }
}
