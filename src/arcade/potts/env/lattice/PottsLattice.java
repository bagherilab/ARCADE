package arcade.potts.env.lattice;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import sim.engine.Schedule;
import sim.engine.SimState;
import arcade.core.env.lattice.Lattice;
import arcade.core.env.location.Location;
import arcade.core.env.operation.Operation;
import arcade.core.env.operation.OperationCategory;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.potts.env.location.PottsLocation;
import arcade.potts.env.location.Voxel;
import arcade.potts.env.operation.PottsOperationDecayer;
import arcade.potts.env.operation.PottsOperationDiffuser;
import static arcade.potts.util.PottsEnums.Category;
import static arcade.potts.util.PottsEnums.Ordering;

/**
 * Implementation of {@link Lattice} for potts models.
 *
 * <p>The field is a {@code [z][x][y]} array aligned 1:1 with the potts {@code ids} array.
 * Operations listed in the layer parameters are created with the lattice and stepped at {@code
 * LATTICES}: diffuser, then decayer.
 */
public class PottsLattice implements Lattice {
    /** Array containing lattice values. */
    final double[][][] field;

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

    @Override
    public void schedule(Schedule schedule) {
        schedule.scheduleRepeating(this, Ordering.LATTICES.ordinal(), 1);
    }

    @Override
    public void step(SimState simstate) {
        Simulation sim = (Simulation) simstate;

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
