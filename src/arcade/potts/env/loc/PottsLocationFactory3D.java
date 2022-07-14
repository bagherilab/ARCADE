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
    ArrayList<Voxel> getPossible(Voxel focus, int s, int h) {
        ArrayList<Voxel> voxels = new ArrayList<>();
        
        for (int i = 0; i < s; i++) {
            for (int j = 0; j < s; j++) {
                for (int k = 0; k < h; k++) {
                    voxels.add(new Voxel(
                            focus.x + i - (s - 1) / 2,
                            focus.y + j - (s - 1) / 2,
                            focus.z + k - (h - 1) / 2));
                }
            }
        }
        
        return voxels;
    }
    
    @Override
    ArrayList<Voxel> getCenters(int length, int width, int height, int margin, int s, int h) {
        ArrayList<Voxel> centers = new ArrayList<>();
        
        for (int i = 0; i < (length - 2 - 2 * margin) / s; i++) {
            for (int j = 0; j < (width - 2 - 2 * margin) / s; j++) {
                for (int k = 0; k < (height - 2) / h; k++) {
                    int cx = i * s + (s + 1) / 2 + margin;
                    int cy = j * s + (s + 1) / 2 + margin;
                    int cz = k * h + (h + 1) / 2;
                    centers.add(new Voxel(cx, cy, cz));
                }
            }
        }
        
        return centers;
    }
}
