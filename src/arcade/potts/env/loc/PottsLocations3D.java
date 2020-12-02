package arcade.potts.env.loc;

import java.util.ArrayList;
import java.util.HashMap;
import static arcade.core.util.Enums.Region;
import static arcade.potts.util.PottsEnums.Direction;

public class PottsLocations3D extends PottsLocations implements Location3D {
    /**
     * Creates a 3D {@link PottsLocation} for a list of voxels.
     *
     * @param voxels  the list of voxels
     */
    public PottsLocations3D(ArrayList<Voxel> voxels) { super(voxels); }
    
    PottsLocation makeLocation(ArrayList<Voxel> voxels) { return new PottsLocation3D(voxels); }
    
    PottsLocations makeLocations(ArrayList<Voxel> voxels) { return new PottsLocations3D(voxels); }
    
    ArrayList<Voxel> getNeighbors(Voxel voxel) { return Location3D.getNeighbors(voxel); }
    
    public double convertVolume(double volume) { return Location3D.convertVolume(volume); }
    
    int calculateSurface() { return Location3D.calculateSurface(voxels); }
    
    int updateSurface(Voxel voxel) { return Location3D.updateSurface(voxels, voxel); }
    
    HashMap<Direction, Integer> getDiameters() { return Location3D.getDiameters(voxels, getCenter()); }
    
    Direction getSlice(Direction direction, HashMap<Direction, Integer> diameters) { return Location3D.getSlice(direction, diameters); }
    
    ArrayList<Voxel> getSelected(Voxel focus, double n) { return Location3D.getSelected(locations.get(Region.DEFAULT).voxels, focus, n); }
}
