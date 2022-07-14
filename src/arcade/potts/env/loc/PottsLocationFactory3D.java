package arcade.potts.env.loc;

import java.util.ArrayList;

/**
 * Concrete implementation of {@link PottsLocationFactory} for 3D.
 */

public final class PottsLocationFactory3D extends PottsLocationFactory {
    public PottsLocationFactory3D() { super(); }
    
    @Override
    ArrayList<Voxel> getSelected(ArrayList<Voxel> voxels, Voxel focus, double n) {
        return Location3D.getSelected(voxels, focus, n);
    }
    
    @Override
    ArrayList<Voxel> getPossible(Voxel focus, int sideRange, int heightRange) {
        ArrayList<Voxel> voxels = new ArrayList<>();
        
        for (int i = 0; i < sideRange; i++) {
            for (int j = 0; j < sideRange; j++) {
                for (int k = 0; k < heightRange; k++) {
                    voxels.add(new Voxel(
                            focus.x + i - (sideRange - 1) / 2,
                            focus.y + j - (sideRange - 1) / 2,
                            focus.z + k - (heightRange - 1) / 2));
                }
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
                for (int k = 0; k < (height - 2) / heightRange; k++) {
                    int cx = i * sideRange + (sideRange + 1) / 2 + margin;
                    int cy = j * sideRange + (sideRange + 1) / 2 + margin;
                    int cz = k * heightRange + (heightRange + 1) / 2;
                    centers.add(new Voxel(cx, cy, cz));
                }
            }
        }
        
        return centers;
    }
}
