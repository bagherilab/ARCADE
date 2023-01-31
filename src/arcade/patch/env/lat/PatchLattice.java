package arcade.patch.env.lat;

import java.util.HashMap;
import java.util.Map;
import sim.engine.Schedule;
import sim.engine.SimState;
import arcade.core.env.lat.Lattice;
import arcade.core.env.loc.Location;
import arcade.core.env.operation.Operation;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.patch.env.loc.CoordinateXYZ;
import arcade.patch.env.loc.PatchLocation;
import static arcade.core.util.Enums.Category;
import static arcade.patch.util.PatchEnums.Ordering;

/**
 * Abstract implementation of {@link Lattice} for patch models.
 * <p>
 * {@code PatchLattice} agents can call two {@link Operation} categories:
 * diffusers and generators. Diffusers diffuse values on the underlying array.
 * Generators add values to the underlying array.
 * <p>
 * General order of rules for the {@code PatchLattice} step:
 * <ul>
 *     <li>step generator operation</li>
 *     <li>step diffuser operation</li>
 * </ul>
 */

public abstract class PatchLattice implements Lattice {
    /** Array containing lattice values. */
    protected final double[][][] field;
    
    /** Length of the array (x direction). */
    private final int length;
    
    /** Width of the array (y direction). */
    private final int width;
    
    /** Height of the array (z direction). */
    private final int height;
    
    /** Spatial conversion factor (um/voxel). */
    protected final double ds;
    
    /** Spatial conversion factor in z (um/voxel). */
    protected final double dz;
    
    /** Map of operation categories and {@link Operation} instance. */
    protected final Map<Category, Operation> operations;
    
    /** Lattice parameters. */
    final MiniBox parameters;
    
    /**
     * Creates a {@code PatchLattice} environment.
     *
     * @param length  the length of array (x direction)
     * @param width  the width of array (y direction)
     * @param height  the height of array (z direction)
     * @param ds  the spatial scaling (x and y directions)
     * @param dz  the spatial scaling (z direction)
     * @param parameters  the dictionary of parameters
     */
    public PatchLattice(int length, int width, int height,
                        double ds, double dz, MiniBox parameters) {
        this.length = length;
        this.width = width;
        this.height = height;
        this.ds = ds;
        this.dz = dz;
        this.parameters = parameters;
        
        field = new double[height][length][width];
        
        // Add lattice operations.
        operations = new HashMap<>();
        MiniBox operationBox = parameters.filter("(OPERATION)");
        for (String operationKey : operationBox.getKeys()) {
            Category category = Category.valueOf(operationKey);
            String version = operationBox.get(operationKey);
            Operation operation = makeOperation(category, version);
            operations.put(category, operation);
        }
    }
    
    /**
     * Makes the specified {@link Operation} object.
     *
     * @param category  the operation category
     * @param version  the operation version
     * @return  the operation instance
     */
    public abstract Operation makeOperation(Category category, String version);
    
    @Override
    public double[][][] getField() { return field; }
    
    @Override
    public int getLength() { return length; }
    
    @Override
    public int getWidth() { return width; }
    
    @Override
    public int getHeight() { return height; }
    
    @Override
    public Operation getOperation(Category category) { return operations.get(category); }
    
    @Override
    public MiniBox getParameters() { return parameters; }
    
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
    
    @Override
    public double getTotalValue(Location location) {
        PatchLocation patchLocation = (PatchLocation) location;
        return patchLocation.getSubcoordinates().stream()
                .map(e -> (CoordinateXYZ) e)
                .mapToDouble(c -> field[c.z][c.x][c.y])
                .sum();
    }
    
    @Override
    public double getAverageValue(Location location) {
        PatchLocation patchLocation = (PatchLocation) location;
        return patchLocation.getSubcoordinates().stream()
                .map(e -> (CoordinateXYZ) e)
                .mapToDouble(c -> field[c.z][c.x][c.y])
                .sum() / patchLocation.getMaximum();
    }
    
    @Override
    public void updateValue(Location location, double fraction) {
        if (!Double.isNaN(fraction)) {
            PatchLocation patchLocation = (PatchLocation) location;
            patchLocation.getSubcoordinates().stream()
                    .map(e -> (CoordinateXYZ) e)
                    .forEach(c -> field[c.z][c.x][c.y] *= fraction);
        }
    }
    
    @Override
    public void incrementValue(Location location, double increment) {
        PatchLocation patchLocation = (PatchLocation) location;
        patchLocation.getSubcoordinates().stream()
                .map(e -> (CoordinateXYZ) e)
                .forEach(c -> field[c.z][c.x][c.y] += increment);
    }
    
    @Override
    public void setValue(Location location, double value) {
        PatchLocation patchLocation = (PatchLocation) location;
        patchLocation.getSubcoordinates().stream()
                .map(e -> (CoordinateXYZ) e)
                .forEach(c -> field[c.z][c.x][c.y] = value);
    }
    
    @Override
    public void schedule(Schedule schedule) {
        schedule.scheduleOnce(this, Ordering.FIRST.ordinal());
        schedule.scheduleRepeating(this, Ordering.LATTICES.ordinal(), 1);
    }
    
    @Override
    public void step(SimState simstate) {
        Simulation sim = (Simulation) simstate;
        
        // Step generator operation, if it exists.
        if (operations.containsKey(Category.GENERATOR)) {
            operations.get(Category.GENERATOR).step(simstate.random, sim);
        }
        
        // Step diffuser operation, if it exists.
        if (operations.containsKey(Category.DIFFUSER)) {
            operations.get(Category.DIFFUSER).step(simstate.random, sim);
        }
    }
}
