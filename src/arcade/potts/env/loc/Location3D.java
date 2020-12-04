package arcade.potts.env.loc;

import java.util.ArrayList;
import java.util.HashMap;
import static arcade.potts.sim.Potts3D.*;
import static arcade.potts.util.PottsEnums.Direction;

public interface Location3D {
    /** Multiplier for calculating surface area from volume */
    double SURFACE_VOLUME_MULTIPLIER = Math.cbrt(36*Math.PI)*2;
    
    Direction[] DIRECTIONS = new Direction[] {
            Direction.YZ_PLANE,
            Direction.ZX_PLANE,
            Direction.XY_PLANE,
            Direction.POSITIVE_XY,
            Direction.NEGATIVE_XY,
            Direction.POSITIVE_YZ,
            Direction.NEGATIVE_YZ,
            Direction.POSITIVE_ZX,
            Direction.NEGATIVE_ZX
    };
    
    static ArrayList<Voxel> getNeighbors(Voxel voxel) {
        ArrayList<Voxel> neighbors = new ArrayList<>();
        for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
            Voxel v = new Voxel(voxel.x + MOVES_X[i], voxel.y + MOVES_Y[i], voxel.z + MOVES_Z[i]);
            neighbors.add(v);
        }
        return neighbors;
    }
    
    static double convertVolume(double volume) {
        return SURFACE_VOLUME_MULTIPLIER*Math.pow(volume, 2./3);
    }
    
    static int calculateSurface(ArrayList<Voxel> voxels) {
        int surface = 0;
        
        for (Voxel v : voxels) {
            for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
                Voxel voxel = new Voxel(v.x + MOVES_X[i], v.y + MOVES_Y[i], v.z + MOVES_Z[i]);
                if (!voxels.contains(voxel)) { surface++; }
            }
        }
        
        return surface;
    }
    
    static int updateSurface(ArrayList<Voxel> voxels, Voxel voxel) {
        int change = 0;
        
        for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
            Voxel v = new Voxel(voxel.x + MOVES_X[i], voxel.y + MOVES_Y[i], voxel.z + MOVES_Z[i]);
            if (!voxels.contains(v)) { change++; }
            else { change--; }
        }
        
        return change;
    }
    
    static HashMap<Direction, Integer> getDiameters(ArrayList<Voxel> voxels, Voxel center) {
        HashMap<Direction, Integer> minValueMap = new HashMap<>();
        HashMap<Direction, Integer> maxValueMap = new HashMap<>();
        HashMap<Direction, Boolean> existsMap = new HashMap<>();
        
        // Initialized entries into direction maps.
        for (Direction direction : DIRECTIONS) {
            minValueMap.put(direction, Integer.MAX_VALUE);
            maxValueMap.put(direction, Integer.MIN_VALUE);
            existsMap.put(direction, false);
        }
        
        Direction dir;
        int v;
        
        // Iterate through all the voxels for the location to update minimum and
        // maximum values in each direction.
        for (Voxel voxel : voxels) {
            int i = voxel.x - center.x;
            int j = voxel.y - center.y;
            int k = voxel.z - center.z;
            
            // Need to update all directions if at the center.
            if (i == 0 && j == 0 && k == 0) {
                v = 0;
                
                for (Direction direction : DIRECTIONS) {
                    existsMap.put(direction, true);
                    if (v > maxValueMap.get(direction)) { maxValueMap.put(direction, v); }
                    if (v < minValueMap.get(direction)) { minValueMap.put(direction, v); }
                }
                
                continue;
            }
            else if (j == 0 && k == 0) { dir = Direction.YZ_PLANE; v = i; }
            else if (k == 0 && i == 0) { dir = Direction.ZX_PLANE; v = j; }
            else if (i == 0 && j == 0) { dir = Direction.XY_PLANE; v = k; }
            else if (i == j && k == 0) { dir = Direction.POSITIVE_XY; v = i; }
            else if (i == -j && k == 0) { dir = Direction.NEGATIVE_XY; v = i; }
            else if (j == k && i == 0) { dir = Direction.POSITIVE_YZ; v = j; }
            else if (j == -k && i == 0) { dir = Direction.NEGATIVE_YZ; v = j; }
            else if (k == i && j == 0) { dir = Direction.POSITIVE_ZX; v = k; }
            else if (k == -i && j == 0) { dir = Direction.NEGATIVE_ZX; v = k; }
            else { continue; }
            
            existsMap.put(dir, true);
            if (v > maxValueMap.get(dir)) { maxValueMap.put(dir, v); }
            if (v < minValueMap.get(dir)) { minValueMap.put(dir, v); }
        }
        
        HashMap<Direction, Integer> diameterMap = new HashMap<>();
        
        // Calculate diameter in each direction.
        for (Direction direction : DIRECTIONS) {
            int diameter = maxValueMap.get(direction) - minValueMap.get(direction) + 1;
            diameterMap.put(direction, existsMap.get(direction) ? diameter : 0);
        }
        
        return diameterMap;
    }
    
    static Direction getSlice(Direction direction, HashMap<Direction, Integer> diameters) {
        switch (direction) {
            case YZ_PLANE:
                return diameters.get(Direction.ZX_PLANE) > diameters.get(Direction.XY_PLANE) ?
                    Direction.ZX_PLANE : Direction.XY_PLANE;
            case ZX_PLANE:
                return diameters.get(Direction.XY_PLANE) > diameters.get(Direction.YZ_PLANE) ?
                    Direction.XY_PLANE : Direction.YZ_PLANE;
            case XY_PLANE:
                return diameters.get(Direction.YZ_PLANE) > diameters.get(Direction.ZX_PLANE) ?
                    Direction.YZ_PLANE : Direction.ZX_PLANE;
            case POSITIVE_XY:
                return diameters.get(Direction.NEGATIVE_XY) > diameters.get(Direction.XY_PLANE) ?
                    Direction.NEGATIVE_XY : Direction.XY_PLANE;
            case NEGATIVE_XY:
                return diameters.get(Direction.POSITIVE_XY) > diameters.get(Direction.XY_PLANE) ?
                    Direction.POSITIVE_XY : Direction.XY_PLANE;
            case POSITIVE_YZ:
                return diameters.get(Direction.NEGATIVE_YZ) > diameters.get(Direction.YZ_PLANE) ?
                    Direction.NEGATIVE_YZ : Direction.YZ_PLANE;
            case NEGATIVE_YZ:
                return diameters.get(Direction.POSITIVE_YZ) > diameters.get(Direction.YZ_PLANE) ?
                    Direction.POSITIVE_YZ : Direction.YZ_PLANE;
            case POSITIVE_ZX:
                return diameters.get(Direction.NEGATIVE_ZX) > diameters.get(Direction.ZX_PLANE) ?
                    Direction.NEGATIVE_ZX : Direction.ZX_PLANE;
            case NEGATIVE_ZX:
                return diameters.get(Direction.POSITIVE_ZX) > diameters.get(Direction.ZX_PLANE) ?
                    Direction.POSITIVE_ZX : Direction.ZX_PLANE;
            default:
                return null;
        }
    }
    
    static ArrayList<Voxel> getSelected(ArrayList<Voxel> voxels, Voxel focus, double n) {
        ArrayList<Voxel> selected = new ArrayList<>();
        double r = Math.cbrt((3*n)/(4*Math.PI));
        
        // Select voxels within given radius.
        for (Voxel voxel : voxels) {
            double d = Math.sqrt(Math.pow(focus.x - voxel.x, 2)
                    + Math.pow(focus.y - voxel.y, 2)
                    + Math.pow(focus.z - voxel.z, 2));
            if (d < r) { selected.add(voxel); }
        }
        
        return selected;
    }
}
