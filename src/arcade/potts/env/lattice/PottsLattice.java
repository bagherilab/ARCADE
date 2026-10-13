package arcade.potts.env.lattice;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;
import arcade.core.env.lattice.Lattice;
import arcade.core.env.location.Location;
import arcade.core.env.operation.Operation;
import arcade.core.env.operation.OperationCategory;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.potts.env.location.PottsLocation;
import arcade.potts.env.location.Voxel;
import arcade.potts.env.operation.PottsOperationDecayer;
import arcade.potts.env.operation.PottsOperationDegrader;
import arcade.potts.env.operation.PottsOperationDiffuser;
import static arcade.potts.util.PottsEnums.Category;
import static arcade.potts.util.PottsEnums.Ordering;

/**
 * Implementation of {@link Lattice} for potts models.
 *
 * <p>The field is a {@code [z][x][y]} array aligned 1:1 with the potts {@code ids} array. Secreted
 * amounts are added to a separate {@code delta} array with {@link #addDelta(List, double)} and
 * added to the field at the start of the lattice step, so all cells sense the same field regardless
 * of the order in which they step.
 *
 * <p>Operations listed in the layer parameters are created with the lattice and stepped at {@code
 * LATTICES}: diffuser, then decayer. The degrader, which reads another lattice, is stepped at
 * {@code LAST_LATTICE}, after all lattices have stepped.
 */
public class PottsLattice implements Lattice {
    /** Array containing lattice values. */
    final double[][][] field;

    /** Array containing pending amounts, added to the field at the next lattice step. */
    final double[][][] delta;

    /** Length of the array (x direction). */
    private final int length;

    /** Width of the array (y direction). */
    private final int width;

    /** Height of the array (z direction). */
    private final int height;

    /** Spatial conversion factor [um/voxel]. */
    private final double ds;

    /** Temporal conversion factor [hours/tick]. */
    private final double dt;

    /** Lattice parameters. */
    final MiniBox parameters;

    /** Map of operation categories and {@link Operation} instances. */
    final Map<Category, Operation> operations;

    /**
     * Creates a {@code PottsLattice}.
     *
     * @param length the length of array (x direction)
     * @param width the width of array (y direction)
     * @param height the height of array (z direction)
     * @param ds the spatial conversion factor [um/voxel]
     * @param dt the temporal conversion factor [hours/tick]
     * @param parameters the dictionary of layer parameters
     */
    public PottsLattice(
            int length, int width, int height, double ds, double dt, MiniBox parameters) {
        this.length = length;
        this.width = width;
        this.height = height;
        this.ds = ds;
        this.dt = dt;
        this.parameters = parameters;
        field = new double[height][length][width];
        delta = new double[height][length][width];

        // Add lattice operations.
        operations = new EnumMap<>(Category.class);
        MiniBox operationBox = parameters.filter("(OPERATION)");
        for (String operationKey : operationBox.getKeys()) {
            Category category = Category.valueOf(operationKey);
            String version = operationBox.get(operationKey);
            operations.put(category, makeOperation(category, version));
        }
    }

    /**
     * Makes the specified {@link Operation} object.
     *
     * @param category the operation category
     * @param version the operation version
     * @return the operation instance
     */
    Operation makeOperation(Category category, String version) {
        switch (category) {
            case DIFFUSER:
                return new PottsOperationDiffuser(this);
            case DECAYER:
                return new PottsOperationDecayer(this);
            case DEGRADER:
                return new PottsOperationDegrader(this);
            default:
                throw new IllegalArgumentException("unsupported operation [ " + category + " ]");
        }
    }

    /**
     * Gets the spatial conversion factor.
     *
     * @return the spatial conversion factor [um/voxel]
     */
    public double getDs() {
        return ds;
    }

    /**
     * Gets the temporal conversion factor.
     *
     * @return the temporal conversion factor [hours/tick]
     */
    public double getDt() {
        return dt;
    }

    @Override
    public double[][][] getField() {
        return field;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public Operation getOperation(OperationCategory category) {
        return operations.get(category);
    }

    @Override
    public MiniBox getParameters() {
        return parameters;
    }

    @Override
    public void setField(double[][] values, int index) {
        for (int i = 0; i < values.length; i++) {
            field[index][i] = values[i].clone();
        }
    }

    @Override
    public void setField(double[][][] values) {
        for (int k = 0; k < values.length; k++) {
            setField(values[k], k);
        }
    }

    @Override
    public void setField(double value) {
        for (int k = 0; k < height; k++) {
            for (int i = 0; i < length; i++) {
                for (int j = 0; j < width; j++) {
                    field[k][i][j] = value;
                }
            }
        }
    }

    /**
     * Gets the voxels of a location.
     *
     * @param location the location
     * @return the list of voxels
     */
    private static ArrayList<Voxel> voxels(Location location) {
        return ((PottsLocation) location).getVoxels();
    }

    @Override
    public double getTotalValue(Location location) {
        double total = 0;
        for (Voxel v : voxels(location)) {
            total += field[v.z][v.x][v.y];
        }
        return total;
    }

    @Override
    public double getAverageValue(Location location) {
        ArrayList<Voxel> voxels = voxels(location);
        if (voxels.isEmpty()) {
            return 0;
        }
        return getTotalValue(location) / voxels.size();
    }

    @Override
    public void updateValue(Location location, double fraction) {
        if (Double.isNaN(fraction)) {
            return;
        }
        for (Voxel v : voxels(location)) {
            field[v.z][v.x][v.y] *= fraction;
        }
    }

    @Override
    public void incrementValue(Location location, double increment) {
        for (Voxel v : voxels(location)) {
            field[v.z][v.x][v.y] += increment;
        }
    }

    @Override
    public void setValue(Location location, double value) {
        for (Voxel v : voxels(location)) {
            field[v.z][v.x][v.y] = value;
        }
    }

    /**
     * Adds a pending amount to each voxel, added to the field at the next lattice step.
     *
     * @param voxels the voxels (repeated voxels receive the amount once per occurrence)
     * @param increment the concentration increment per voxel
     */
    public void addDelta(List<Voxel> voxels, double increment) {
        for (Voxel v : voxels) {
            delta[v.z][v.x][v.y] += increment;
        }
    }

    /** Adds pending amounts to the field and clears them. */
    void applyDelta() {
        for (int k = 0; k < height; k++) {
            for (int i = 0; i < length; i++) {
                for (int j = 0; j < width; j++) {
                    field[k][i][j] += delta[k][i][j];
                    delta[k][i][j] = 0;
                }
            }
        }
    }

    @Override
    public void schedule(Schedule schedule) {
        schedule.scheduleRepeating(this, Ordering.LATTICES.ordinal(), 1);

        // Schedule degrader operation after all lattices have stepped, if it exists.
        if (operations.containsKey(Category.DEGRADER)) {
            Operation degrader = operations.get(Category.DEGRADER);
            Steppable step = state -> degrader.step(state.random, (Simulation) state);
            schedule.scheduleRepeating(step, Ordering.LAST_LATTICE.ordinal(), 1);
        }
    }

    @Override
    public void step(SimState simstate) {
        Simulation sim = (Simulation) simstate;

        // Add pending amounts from cells.
        applyDelta();

        // Step diffuser operation, if it exists.
        if (operations.containsKey(Category.DIFFUSER)) {
            operations.get(Category.DIFFUSER).step(simstate.random, sim);
        }

        // Step decayer operation, if it exists.
        if (operations.containsKey(Category.DECAYER)) {
            operations.get(Category.DECAYER).step(simstate.random, sim);
        }
    }
}
