package arcade.potts.env.location;

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
     * Calculates and returns the voxel at the specified percentage offset from
     * the boundaries of the location, in the X and Y dimensions.
     * <p>
     * The offset percentages determine the relative position of the returned voxel
     * within the location. For example, an offset of [50, 50] will return the voxel
     * closest to the center of the location in the X and Y directions.
     *
     * <p><b>Note:</b> The {@code offsetPercents} list must contain exactly 2 integers
     * representing the percentage offsets for the X and Y axes. A third offset of 0 is
     * automatically added for the Z axis.
     *
     * @param offsetPercents An {@code ArrayList<Integer>} containing exactly 2 integers,
     *                        which represent the percentage offsets in the X and Y
     *                        directions. Each percentage should be in the range [0, 100].
     * @return The voxel located at the calculated offset position.
     * @throws IllegalArgumentException If {@code offset_percents} is {@code null} or
     *                                  does not contain exactly 2 integers.
     */

     @Override
     public Voxel getSplitpoint(ArrayList<Integer> offsetPercents) {
         System.out.println("PottsLocation2D.getSplitpoint, offsetPercents: " + offsetPercents);
         if (offsetPercents == null || offsetPercents.size() != 2) {
             throw new IllegalArgumentException(
                 "PottsLocation2D offsets must be an ArrayList containing exactly 2 integers."
             );
         }
         ArrayList<Integer> fullOffsetPercents = new ArrayList<>(offsetPercents);
         fullOffsetPercents.add(0);
        return super.getSplitpoint(fullOffsetPercents);
    }
}
