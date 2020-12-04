package arcade.potts.env.loc;

import java.util.ArrayList;

public class PottsLocationFactory2D extends PottsLocationFactory {
    public PottsLocationFactory2D() { super(); }
    
    int convert(double volume) {
        int sqrt = (int)Math.ceil(Math.sqrt(volume));
        return sqrt + (sqrt%2 == 0 ? 1 : 0);
    }
    
    ArrayList<Voxel> getNeighbors(Voxel voxel) {
        return Location2D.getNeighbors(voxel);
    }
    
    ArrayList<Voxel> getSelected(ArrayList<Voxel> voxels, Voxel focus, double n) {
        return Location2D.getSelected(voxels, focus, n);
    }
    
    ArrayList<Voxel> getPossible(Voxel focus, int m) {
        ArrayList<Voxel> voxels = new ArrayList<>();
        
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                voxels.add(new Voxel(
                        focus.x + i - (m - 1)/2,
                        focus.y + j - (m - 1)/2,
                        0));
            }
        }
        
        return voxels;
    }
    
    ArrayList<Voxel> getCenters(int length, int width, int height, int m) {
        ArrayList<Voxel> centers = new ArrayList<>();
        for (int i = 0; i < (length - 2)/m; i++) {
            for (int j = 0; j < (width - 2)/m; j++) {
                int cx = i*m + (m + 1)/2;
                int cy = j*m + (m + 1)/2;
                centers.add(new Voxel(cx, cy, 0));
            }
        }
        return centers;
    }
}
