package arcade.potts.env.loc;

import java.util.ArrayList;
import java.util.HashMap;
import static arcade.potts.util.PottsEnums.Direction;

/**
 * Concrete implementation of {@link PottsLocation} for 2D.
 */

public final class PottsLocation2D extends PottsLocation implements Location2D {
    /**
     * Creates a 2D {@link PottsLocation} for a list of voxels.
     *
     * @param voxels  the list of voxels
     */
    public PottsLocation2D(ArrayList<Voxel> voxels) { super(voxels); }
    
    @Override
    PottsLocation makeLocation(ArrayList<Voxel> voxels) {
        return new PottsLocation2D(voxels);
    }
    
    @Override
    ArrayList<Voxel> getNeighbors(Voxel focus) {
        return Location2D.getNeighbors(focus);
    }
    
    @Override
    public double convertVolume(double volume) {
        return Location2D.convertVolume(volume);
    }
    
    @Override
    int calculateSurface() {
        return Location2D.calculateSurface(voxels);
    }
    
    @Override
    int updateSurface(Voxel voxel) {
        return Location2D.updateSurface(voxels, voxel);
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
