package arcade.potts.env.loc;

import java.util.ArrayList;

/**
 * Concrete implementation of {@link PottsLocationFactory} for 3D.
 */

public final class PottsLocationFactory3D extends PottsLocationFactory {
    public PottsLocationFactory3D() { super(); }
    
    @Override
    int convert(double volume) {
        int cbrt = (int) Math.ceil(Math.cbrt(volume));
        return cbrt + (cbrt % 2 == 0 ? 1 : 0);
    }
    
    @Override
    ArrayList<Voxel> getNeighbors(Voxel focus) {
        return Location3D.getNeighbors(focus);
    }
    
    @Override
    ArrayList<Voxel> getSelected(ArrayList<Voxel> voxels, Voxel focus, double n) {
        return Location3D.getSelected(voxels, focus, n);
    }
    
    @Override
    ArrayList<Voxel> getPossible(Voxel focus, int height, int m) {
        ArrayList<Voxel> voxels = new ArrayList<>();
        
        if (height - 2 < m) {
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < m; j++) {
                    for (int k = 1; k < height - 1; k++) {
                        voxels.add(new Voxel(
                                focus.x + i - (m - 1) / 2,
                                focus.y + j - (m - 1) / 2,
                                k));
                    }
                }
            }
            
            return voxels;
        }
        
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                for (int k = 0; k < m; k++) {
                    voxels.add(new Voxel(
                            focus.x + i - (m - 1) / 2,
                            focus.y + j - (m - 1) / 2,
                            focus.z + k - (m - 1) / 2));
                }
            }
        }
        
        return voxels;
    }
    
    @Override
    ArrayList<Voxel> getCenters(int length, int width, int height, int m) {
        ArrayList<Voxel> centers = new ArrayList<>();
    
        // Special case "quasi-3D" where length, width >> height. Uses
        // the middle layer in the z direction as the center z coordinate.
        if (height - 2 < m) {
            for (int i = 0; i < (length - 2) / m; i++) {
                for (int j = 0; j < (width - 2) / m; j++) {
                    int cx = i * m + (m + 1) / 2;
                    int cy = j * m + (m + 1) / 2;
                    int cz = (height - 1) / 2;
                    centers.add(new Voxel(cx, cy, cz));
                }
            }
            
            return centers;
        }
        
        for (int i = 0; i < (length - 2) / m; i++) {
            for (int j = 0; j < (width - 2) / m; j++) {
                for (int k = 0; k < (height - 2) / m; k++) {
                    int cx = i * m + (m + 1) / 2;
                    int cy = j * m + (m + 1) / 2;
                    int cz = k * m + (m + 1) / 2;
                    centers.add(new Voxel(cx, cy, cz));
                }
            }
        }
        
        return centers;
    }
}
