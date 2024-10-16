package arcade.potts.env.location;

import java.util.ArrayList;
import java.util.HashMap;
import static arcade.potts.util.PottsEnums.Direction;

/** Concrete implementation of {@link PottsLocation} for 3D. */
public final class PottsLocation3D extends PottsLocation implements Location3D {
    /**
     * Creates a 3D {@link PottsLocation} for a list of voxels.
     *
     * @param voxels the list of voxels
     */
    public PottsLocation3D(ArrayList<Voxel> voxels) {
        super(voxels);
    }

    @Override
    PottsLocation makeLocation(ArrayList<Voxel> voxels) {
        return new PottsLocation3D(voxels);
    }

    @Override
    ArrayList<Voxel> getNeighbors(Voxel focus) {
        return Location3D.getNeighbors(focus);
    }

    @Override
    public double convertSurface(double volume, double height) {
        return Location3D.convertSurface(volume, height);
    }

    @Override
    int calculateSurface() {
        return Location3D.calculateSurface(voxels);
    }

    @Override
    int calculateHeight() {
        return Location3D.calculateHeight(voxels);
    }

    @Override
    int updateSurface(Voxel voxel) {
        return Location3D.updateSurface(voxels, voxel);
    }

    @Override
    int updateHeight(Voxel voxel) {
        return Location3D.updateHeight(voxels, voxel);
    }

    @Override
    HashMap<Direction, Integer> getDiameters() {
        return Location3D.getDiameters(voxels, getCenter());
    }

    @Override
    Direction getSlice(Direction direction, HashMap<Direction, Integer> diameters) {
        return Location3D.getSlice(direction, diameters);
    }

    @Override
    ArrayList<Voxel> getSelected(Voxel focus, double n) {
        return Location3D.getSelected(voxels, focus, n);
    }
}
