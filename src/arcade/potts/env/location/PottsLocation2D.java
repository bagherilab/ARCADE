package arcade.potts.env.location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import arcade.core.util.Vector;
import static arcade.potts.util.PottsEnums.Direction;

/** Concrete implementation of {@link PottsLocation} for 2D. */
public final class PottsLocation2D extends PottsLocation implements Location2D {
    /**
     * Creates a 2D {@link PottsLocation} for a list of voxels.
     *
     * @param voxels the list of voxels
     */
    public PottsLocation2D(ArrayList<Voxel> voxels) {
        super(voxels);
    }

    @Override
    PottsLocation makeLocation(ArrayList<Voxel> voxels) {
        return new PottsLocation2D(voxels);
    }

    @Override
    ArrayList<Voxel> getNeighbors(Voxel focus) {
        return Location2D.getNeighbors(focus);
    }

    @Override
    public double convertSurface(double volume, double height) {
        return Location2D.convertSurface(volume, height);
    }

    @Override
    int calculateSurface() {
        return Location2D.calculateSurface(voxels);
    }

    @Override
    int calculateHeight() {
        return Location2D.calculateHeight(voxels);
    }

    @Override
    int updateSurface(Voxel voxel) {
        return Location2D.updateSurface(voxels, voxel);
    }

    @Override
    int updateHeight(Voxel voxel) {
        return Location2D.updateHeight(voxels, voxel);
    }

    @Override
    HashMap<Direction, Integer> getDiameters() {
        return Location2D.getDiameters(voxels, getCenter());
    }

    @Override
    Direction getSlice(Direction direction, HashMap<Direction, Integer> diameters) {
        return Location2D.getSlice(direction, diameters);
    }

    @Override
    ArrayList<Voxel> getSelected(Voxel focus, double n) {
        return Location2D.getSelected(voxels, focus, n);
    }

    /**
     * Gets the voxel at specified percentage offsets along the location's X and Y axes with the
     * provided apicalAxis considered to be pointing up the Y axis. Returns null if this
     * PottsLocation2D contains no voxels or if the offsets ArrayList provided is not 2 integers
     * long.
     *
     * @param offsets the percent offsets along the location's X and Y axes
     * @param apicalAxis the axis considered to be pointing up along the Y axis
     * @return the voxel through which the plane of division will pass
     */
    @Override
    public Voxel getOffsetInApicalFrame(ArrayList<Integer> offsets, Vector apicalAxis) {
        if (voxels.isEmpty()) {
            return null;
        }
        if (offsets == null || offsets.size() != 2) {
            throw new IllegalArgumentException("Offsets must be 2 integers.");
        }

        // Normalize axes
        Vector yAxis = Vector.normalizeVector(apicalAxis);
        Vector xAxis = Vector.normalizeVector(new Vector(apicalAxis.getY(), -apicalAxis.getX(), 0));

        // Project voxels onto apical axis and group by rounded projection
        HashMap<Integer, ArrayList<Voxel>> apicalBands = new HashMap<>();
        ArrayList<Integer> apicalKeys = new ArrayList<>();

        for (Voxel v : voxels) {
            Vector pos = new Vector(v.x, v.y, 0);
            double apicalProj = Vector.dotProduct(pos, yAxis);
            int roundedProj = (int) Math.round(apicalProj);
            apicalBands.computeIfAbsent(roundedProj, k -> new ArrayList<>()).add(v);
            apicalKeys.add(roundedProj);
        }

        // Sort apical keys and choose percentile
        Collections.sort(apicalKeys);
        int yIndex =
                Math.min(
                        apicalKeys.size() - 1,
                        (int) ((offsets.get(1) / 100.0) * apicalKeys.size()));
        int targetApicalKey = apicalKeys.get(yIndex);

        ArrayList<Voxel> band = apicalBands.get(targetApicalKey);
        if (band == null || band.isEmpty()) {
            return null;
        }
        // Project to orthogonal axis within the band and sort
        band.sort(
                Comparator.comparingDouble(v -> Vector.dotProduct(new Vector(v.x, v.y, 0), xAxis)));
        int xIndex = Math.min(band.size() - 1, (int) ((offsets.get(0) / 100.0) * band.size()));
        return band.get(xIndex);
    }
}
