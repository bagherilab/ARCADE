package arcade.potts.env.loc;

import java.util.ArrayList;
import java.util.HashMap;
import static arcade.core.util.Enums.Region;
import static arcade.potts.util.PottsEnums.Direction;

/**
 * Concrete implementation of {@link PottsLocations} for 3D.
 */

public final class PottsLocations3D extends PottsLocations implements Location3D {
    /**
     * Creates a 3D {@link PottsLocation} for a list of voxels.
     *
     * @param voxels  the list of voxels
     */
    public PottsLocations3D(ArrayList<Voxel> voxels) { super(voxels); }
    
    @Override
    PottsLocation makeLocation(ArrayList<Voxel> voxels) {
        return new PottsLocation3D(voxels);
    }
    
    @Override
    PottsLocations makeLocations(ArrayList<Voxel> voxels) {
        return new PottsLocations3D(voxels);
    }
    
    @Override
    ArrayList<Voxel> getNeighbors(Voxel focus) {
        return Location3D.getNeighbors(focus);
    }
    
    @Override
    public double convertVolume(double volume) {
        return Location3D.convertVolume(volume);
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
        return Location3D.getSelected(locations.get(Region.DEFAULT).voxels, focus, n);
    }
}
