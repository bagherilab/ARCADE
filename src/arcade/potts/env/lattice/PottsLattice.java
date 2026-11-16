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
 * <p>Uptake capacities are added to a separate {@code sink} array with {@link #addSink(List,
 * double)}, and supply from vasculature is added as an exchange P (T - c) with {@link
 * #addExchange(List, double, double)}. The diffuser uses both inside its implicit solve, and they
 * are cleared at the end of the lattice step. Without a diffuser, they have no effect.
 *
 * <p>First-order clearance rates (per voxel, for example uptake of a growth factor by the cells
 * occupying the voxel) are added with {@link #addClearance(List, double)}. The time-stepping
 * diffuser removes c times the rate inside its substeps. They are cleared at the end of the lattice
 * step; without a diffuser, they have no effect.
 *
 * <p>Continuous sources (rates per voxel, for example release from another layer) are added with
 * {@link #addSource(List, double)}. The diffuser adds them in every substep, so they enter the
 * field spread over the tick instead of as one pulse at its start. They are cleared at the end of
 * the lattice step; without a diffuser, they have no effect.
 *
 * <p>Operations listed in the layer parameters are created with the lattice and stepped at {@code
 * LATTICES}: diffuser, then decayer. The degrader, which reads another lattice, is stepped at
 * {@code LAST_LATTICE}, after all lattices have stepped.
 *
 * <p>If the layer parameter {@code MAXIMUM} is above 0, every value above it is set to it at the
 * end of the lattice step, after the diffuser and decayer. This is a physical ceiling (for example,
 * packing of ECM), not a setpoint; 0 means no ceiling.
 */
public class PottsLattice implements Lattice {
    /** Array containing lattice values. */
    final double[][][] field;

    /** Array containing pending amounts, added to the field at the next lattice step. */
    final double[][][] delta;

    /**
     * Array of maximum uptake rates [layer units/s], used by the diffuser and cleared each step.
     */
    final double[][][] sink;

    /** Array of summed exchange rates P [1/s], used by the diffuser and cleared each step. */
    final double[][][] exchange;

    /**
     * Array of summed products P * T [layer units/s], used by the diffuser and cleared each step.
     */
    final double[][][] exchangeTarget;

    /**
     * Array of continuous source rates [layer units/s], used by the diffuser and cleared each step.
     */
    final double[][][] source;

    /** Array of first-order clearance rates [1/s], used by the diffuser and cleared each step. */
    final double[][][] clearance;

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

    /** Ceiling on lattice values, or 0 for no ceiling. */
    final double maximum;

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
        maximum = parameters.contains("MAXIMUM") ? parameters.getDouble("MAXIMUM") : 0;
        if (!(maximum >= 0)) {
            throw new IllegalArgumentException("layer MAXIMUM must be >= 0");
        }
        field = new double[height][length][width];
        delta = new double[height][length][width];
        sink = new double[height][length][width];
        exchange = new double[height][length][width];
        exchangeTarget = new double[height][length][width];
        source = new double[height][length][width];
        clearance = new double[height][length][width];

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

    /**
     * Adds a maximum uptake rate to each voxel for the current tick.
     *
     * <p>The rate is used by the diffuser and cleared at the end of the lattice step.
     *
     * @param voxels the voxels (repeated voxels receive the rate once per occurrence)
     * @param rate the maximum uptake rate per voxel [layer units/s]
     */
    public void addSink(List<Voxel> voxels, double rate) {
        for (Voxel v : voxels) {
            sink[v.z][v.x][v.y] += rate;
        }
    }

    /**
     * Gets the array of maximum uptake rates.
     *
     * @return the sink array [layer units/s]
     */
    public double[][][] getSink() {
        return sink;
    }

    /**
     * Adds an exchange P (T - c) to each voxel for the current tick.
     *
     * <p>Rates and rate-weighted targets are summed, so a voxel with several exchanges relaxes
     * toward their rate-weighted mean target. The exchange is used by the diffuser and cleared at
     * the end of the lattice step.
     *
     * @param voxels the voxels (repeated voxels receive the exchange once per occurrence)
     * @param rate the exchange rate P per voxel [1/s]
     * @param target the target value T [layer units]
     */
    public void addExchange(List<Voxel> voxels, double rate, double target) {
        for (Voxel v : voxels) {
            exchange[v.z][v.x][v.y] += rate;
            exchangeTarget[v.z][v.x][v.y] += rate * target;
        }
    }

    /**
     * Gets the array of summed exchange rates.
     *
     * @return the exchange array [1/s]
     */
    public double[][][] getExchange() {
        return exchange;
    }

    /**
     * Gets the array of summed products of exchange rate and target.
     *
     * @return the exchange target array [layer units/s]
     */
    public double[][][] getExchangeTarget() {
        return exchangeTarget;
    }

    /**
     * Adds a continuous source rate to each voxel for the current tick.
     *
     * <p>The diffuser adds the source in every substep and the rate is cleared at the end of the
     * lattice step.
     *
     * @param voxels the voxels (repeated voxels receive the rate once per occurrence)
     * @param rate the source rate per voxel [layer units/s]
     */
    public void addSource(List<Voxel> voxels, double rate) {
        for (Voxel v : voxels) {
            source[v.z][v.x][v.y] += rate;
        }
    }

    /**
     * Gets the array of continuous source rates.
     *
     * @return the source array [layer units/s]
     */
    public double[][][] getSource() {
        return source;
    }

    /**
     * Adds a first-order clearance rate to each voxel for the current tick.
     *
     * <p>The diffuser removes the field at this rate times the local value inside its time-stepping
     * substeps, and the rate is cleared at the end of the lattice step.
     *
     * @param voxels the voxels (repeated voxels receive the rate once per occurrence)
     * @param rate the clearance rate per voxel [1/s]
     */
    public void addClearance(List<Voxel> voxels, double rate) {
        for (Voxel v : voxels) {
            clearance[v.z][v.x][v.y] += rate;
        }
    }

    /**
     * Gets the array of first-order clearance rates.
     *
     * @return the clearance array [1/s]
     */
    public double[][][] getClearance() {
        return clearance;
    }

    /** Clears all uptake rates, exchanges, sources and clearance rates. */
    void clearReactions() {
        for (int k = 0; k < height; k++) {
            for (int i = 0; i < length; i++) {
                for (int j = 0; j < width; j++) {
                    sink[k][i][j] = 0;
                    exchange[k][i][j] = 0;
                    exchangeTarget[k][i][j] = 0;
                    source[k][i][j] = 0;
                    clearance[k][i][j] = 0;
                }
            }
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

        // Cap values at the ceiling, if there is one.
        if (maximum > 0) {
            applyMaximum();
        }

        // Clear uptake rates, exchanges, sources and clearance, which were used by the diffuser.
        clearReactions();
    }

    /** Sets every field value above {@code MAXIMUM} to {@code MAXIMUM}. */
    void applyMaximum() {
        for (double[][] plane : field) {
            for (double[] row : plane) {
                for (int j = 0; j < row.length; j++) {
                    if (row[j] > maximum) {
                        row[j] = maximum;
                    }
                }
            }
        }
    }
}
