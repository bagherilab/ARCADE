package arcade.potts.env.location;

import java.util.ArrayList;
import java.util.HashMap;
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
}
