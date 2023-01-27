package arcade.potts.env.loc;

import java.util.ArrayList;

/**
 * Concrete implementation of {@link PottsLocationFactory} for 2D.
 */

public final class PottsLocationFactory2D extends PottsLocationFactory {
    /**
     * Creates a factory for making 2D {@link PottsLocation} instances.
     */
    public PottsLocationFactory2D() { super(); }
    
    @Override
    ArrayList<Voxel> getSelected(ArrayList<Voxel> voxels, Voxel focus, double n) {
        return Location2D.getSelected(voxels, focus, n);
    }
    
    @Override
    ArrayList<Voxel> getPossible(Voxel focus, int sideRange, int heightRange) {
        ArrayList<Voxel> voxels = new ArrayList<>();
        
        for (int i = 0; i < sideRange; i++) {
            for (int j = 0; j < sideRange; j++) {
                voxels.add(new Voxel(
                        focus.x + i - (sideRange - 1) / 2,
                        focus.y + j - (sideRange - 1) / 2,
                        0));
            }
        }
        
        return voxels;
    }
    
    @Override
    ArrayList<Voxel> getCenters(int length, int width, int height, int margin,
                                int sideRange, int heightRange) {
        ArrayList<Voxel> centers = new ArrayList<>();
        
        for (int i = 0; i < (length - 2 - 2 * margin) / sideRange; i++) {
            for (int j = 0; j < (width - 2 - 2 * margin) / sideRange; j++) {
                int cx = i * sideRange + (sideRange + 1) / 2 + margin;
                int cy = j * sideRange + (sideRange + 1) / 2 + margin;
                centers.add(new Voxel(cx, cy, 0));
            }
        }
        
        return centers;
    }
}
