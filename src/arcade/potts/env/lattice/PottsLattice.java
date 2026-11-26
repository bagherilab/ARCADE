package arcade.potts.env.lattice;

import java.util.ArrayList;
import sim.engine.Schedule;
import sim.engine.SimState;
import arcade.core.env.lattice.Lattice;
import arcade.core.env.location.Location;
import arcade.core.env.operation.Operation;
import arcade.core.env.operation.OperationCategory;
import arcade.core.util.MiniBox;
import arcade.potts.env.location.PottsLocation;
import arcade.potts.env.location.Voxel;

/**
 * Implementation of {@link Lattice} for potts models.
 *
 * <p>The field is a {@code [z][x][y]} array aligned 1:1 with the potts {@code ids} array. A lattice
 * without operations is static, so nothing is scheduled.
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
        return null;
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
    public void schedule(Schedule schedule) {}

    @Override
    public void step(SimState simstate) {}
}
