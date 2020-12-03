package arcade.potts.env.loc;

import java.util.ArrayList;

public class PottsLocationFactory3D extends PottsLocationFactory {
    public PottsLocationFactory3D() { super(); }
    
    int convert(double volume) {
        int cbrt = (int)Math.ceil(Math.cbrt(volume));
        return cbrt + (cbrt%2 == 0 ? 1 : 0);
    }
    
    ArrayList<Voxel> getNeighbors(Voxel voxel) { return Location3D.getNeighbors(voxel); }
    
    ArrayList<Voxel> getSelected(ArrayList<Voxel> voxels, Voxel focus, double n) { return Location3D.getSelected(voxels, focus, n); }
    
    ArrayList<Voxel> getPossible(Voxel focus, int m) {
        ArrayList<Voxel> voxels = new ArrayList<>();
        
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                for (int k = 0; k < m; k++) {
                    voxels.add(new Voxel(
                            focus.x + i - (m - 1)/2,
                            focus.y + j - (m - 1)/2,
                            focus.z + k - (m - 1)/2));
                }
            }
        }
        
        return voxels;
    }
    
    ArrayList<Voxel> getCenters(int length, int width, int height, int m) {
        ArrayList<Voxel> centers = new ArrayList<>();
        for (int i = 0; i < (length - 2)/m; i++) {
            for (int j = 0; j < (width - 2)/m; j++) {
                for (int k = 0; k < (height - 2)/m; k++) {
                    int cx = i*m + (m + 1)/2;
                    int cy = j*m + (m + 1)/2;
                    int cz = k*m + (m + 1)/2;
                    centers.add(new Voxel(cx, cy, cz));
                }
            }
        }
        return centers;
    }
}