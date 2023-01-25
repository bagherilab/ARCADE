package arcade.patch.env.lat;

import arcade.core.env.loc.Location;
import arcade.core.env.operation.Operation;
import arcade.core.util.MiniBox;
import arcade.patch.env.loc.CoordinateTri;
import arcade.patch.env.loc.PatchLocation;
import arcade.patch.env.operation.PatchOperationDiffuserTri;
import arcade.patch.env.operation.PatchOperationGenerator;
import static arcade.core.util.Enums.Category;

/**
 * Concrete implementation of {@link PatchLattice} for triangular coordinates.
 */

public class PatchLatticeTri extends PatchLattice {
    /**
     * Creates a triangular {@code PatchLattice} environment.
     *
     * @param length  the length of array (x direction)
     * @param width  the width of array (y direction)
     * @param height  the height of array (z direction)
     * @param ds  the spatial scaling (x and y directions)
     * @param dz  the spatial scaling (z direction)
     * @param parameters  the dictionary of parameters
     */
    public PatchLatticeTri(int length, int width, int height,
                           double ds, double dz, MiniBox parameters) {
        super(length, width, height, ds, dz, parameters);
    }
    
    @Override
    public Operation makeOperation(Category category, String version) {
        switch (category) {
            case DIFFUSER:
                return new PatchOperationDiffuserTri(this, ds, dz);
            case GENERATOR:
                return new PatchOperationGenerator(this);
            default:
                return null;
        }
    }
    
    @Override
    public double getTotalValue(Location location) {
        PatchLocation patchLocation = (PatchLocation) location;
        return patchLocation.getSubcoordinates().stream()
                .map(e -> (CoordinateTri) e)
                .mapToDouble(c -> field[c.z][c.x][c.y])
                .sum();
    }
    
    @Override
    public double getAverageValue(Location location) {
        PatchLocation patchLocation = (PatchLocation) location;
        return patchLocation.getSubcoordinates().stream()
                .map(e -> (CoordinateTri) e)
                .mapToDouble(c -> field[c.z][c.x][c.y])
                .sum() / patchLocation.getMaximum();
    }
    
    @Override
    public void updateValue(Location location, double fraction) {
        if (!Double.isNaN(fraction)) {
            PatchLocation patchLocation = (PatchLocation) location;
            patchLocation.getSubcoordinates().stream()
                    .map(e -> (CoordinateTri) e)
                    .forEach(c -> field[c.z][c.x][c.y] *= fraction);
        }
    }
    
    @Override
    public void incrementValue(Location location, double increment) {
        PatchLocation patchLocation = (PatchLocation) location;
        patchLocation.getSubcoordinates().stream()
                .map(e -> (CoordinateTri) e)
                .forEach(c -> field[c.z][c.x][c.y] += increment);
    }
    
    @Override
    public void setValue(Location location, double value) {
        PatchLocation patchLocation = (PatchLocation) location;
        patchLocation.getSubcoordinates().stream()
                .map(e -> (CoordinateTri) e)
                .forEach(c -> field[c.z][c.x][c.y] = value);
    }
}
