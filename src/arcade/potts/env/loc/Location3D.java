package arcade.potts.env.loc;

import java.util.ArrayList;
import java.util.HashMap;
import static arcade.potts.sim.Potts3D.*;
import static arcade.potts.util.PottsEnums.Direction;

/**
 * Static location methods for 3D.
 * <p>
 * Interface defines generalized 3D voxel methods that can be applied for both
 * {@link PottsLocation} objects (without regions) and {@link PottsLocations}
 * objects (with regions).
 */

public interface Location3D {
    /** Multiplier for calculating surface area from volume. */
    double SURFACE_VOLUME_MULTIPLIER = Math.cbrt(36 * Math.PI) * 3;
    
    /** List of valid 3D directions. */
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
    
    /**
     * Gets list of neighbors of a given voxel.
     *
     * @param focus  the focus voxel
     * @return  the list of neighbor voxels
     */
    static ArrayList<Voxel> getNeighbors(Voxel focus) {
        ArrayList<Voxel> neighbors = new ArrayList<>();
        for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
            Voxel v = new Voxel(focus.x + MOVES_X[i], focus.y + MOVES_Y[i], focus.z + MOVES_Z[i]);
            neighbors.add(v);
        }
        return neighbors;
    }
    
    /**
     * Converts volume to surface area.
     *
     * @param volume  the volume (in voxels)
     * @return  the surface area (in voxels)
     */
    static double convertVolume(double volume) {
        return SURFACE_VOLUME_MULTIPLIER * Math.pow(volume, 2. / 3);
    }
    
    /**
     * Calculates surface of location.
     *
     * @param voxels  the list of voxels
     * @return  the surface
     */
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
    
    /**
     * Calculates height of location (z axis).
     *
     * @param voxels  the list of voxels
     * @return  the height
     */
    static int calculateHeight(ArrayList<Voxel> voxels) {
        // TODO
        return 0;
    }
    
    /**
     * Calculates the local change in surface of the location.
     *
     * @param voxels  the list of voxels
     * @param voxel  the voxel the update is centered in
     * @return  the change in surface
     */
    static int updateSurface(ArrayList<Voxel> voxels, Voxel voxel) {
        int change = 0;
        
        for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
            Voxel v = new Voxel(voxel.x + MOVES_X[i], voxel.y + MOVES_Y[i], voxel.z + MOVES_Z[i]);
            if (!voxels.contains(v)) {
                change++;
            } else {
                change--;
            }
        }
        
        return change;
    }
    
    /**
     * Calculates the local change in height of the location.
     *
     * @param voxels  the list of voxels
     * @param voxel  the voxel the update is centered in
     * @return  the change in height
     */
    static int updateHeight(ArrayList<Voxel> voxels, Voxel voxel) {
        // TODO
        return 0;
    }
    
    /**
     * Calculates diameters in each direction.
     *
     * @param voxels  the list of voxels
     * @param focus  the focus voxel
     * @return  the map of direction to diameter
     */
    static HashMap<Direction, Integer> getDiameters(ArrayList<Voxel> voxels, Voxel focus) {
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
            int i = voxel.x - focus.x;
            int j = voxel.y - focus.y;
            int k = voxel.z - focus.z;
            
            // Need to update all directions if at the center.
            if (i == 0 && j == 0 && k == 0) {
                v = 0;
                
                for (Direction direction : DIRECTIONS) {
                    existsMap.put(direction, true);
                    if (v > maxValueMap.get(direction)) { maxValueMap.put(direction, v); }
                    if (v < minValueMap.get(direction)) { minValueMap.put(direction, v); }
                }
                
                continue;
            } else if (j == 0 && k == 0) {
                dir = Direction.YZ_PLANE; v = i;
            } else if (k == 0 && i == 0) {
                dir = Direction.ZX_PLANE; v = j;
            } else if (i == 0 && j == 0) {
                dir = Direction.XY_PLANE; v = k;
            } else if (i == j && k == 0) {
                dir = Direction.POSITIVE_XY; v = i;
            } else if (i == -j && k == 0) {
                dir = Direction.NEGATIVE_XY; v = i;
            } else if (j == k && i == 0) {
                dir = Direction.POSITIVE_YZ; v = j;
            } else if (j == -k && i == 0) {
                dir = Direction.NEGATIVE_YZ; v = j;
            } else if (k == i && j == 0) {
                dir = Direction.POSITIVE_ZX; v = k;
            } else if (k == -i && j == 0) {
                dir = Direction.NEGATIVE_ZX; v = k;
            } else {
                continue;
            }
            
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
    
    /**
     * Selects the slice direction for a given minimum diameter direction.
     *
     * @param direction  the direction of the minimum diameter
     * @param diameters  the list of diameters
     * @return  the slice direction
     */
    static Direction getSlice(Direction direction, HashMap<Direction, Integer> diameters) {
        switch (direction) {
            case YZ_PLANE:
                return diameters.get(Direction.ZX_PLANE) > diameters.get(Direction.XY_PLANE)
                        ? Direction.ZX_PLANE : Direction.XY_PLANE;
            case ZX_PLANE:
                return diameters.get(Direction.XY_PLANE) > diameters.get(Direction.YZ_PLANE)
                        ? Direction.XY_PLANE : Direction.YZ_PLANE;
            case XY_PLANE:
                return diameters.get(Direction.YZ_PLANE) > diameters.get(Direction.ZX_PLANE)
                        ? Direction.YZ_PLANE : Direction.ZX_PLANE;
            case POSITIVE_XY:
                return diameters.get(Direction.NEGATIVE_XY) > diameters.get(Direction.XY_PLANE)
                        ? Direction.NEGATIVE_XY : Direction.XY_PLANE;
            case NEGATIVE_XY:
                return diameters.get(Direction.POSITIVE_XY) > diameters.get(Direction.XY_PLANE)
                        ? Direction.POSITIVE_XY : Direction.XY_PLANE;
            case POSITIVE_YZ:
                return diameters.get(Direction.NEGATIVE_YZ) > diameters.get(Direction.YZ_PLANE)
                        ? Direction.NEGATIVE_YZ : Direction.YZ_PLANE;
            case NEGATIVE_YZ:
                return diameters.get(Direction.POSITIVE_YZ) > diameters.get(Direction.YZ_PLANE)
                        ? Direction.POSITIVE_YZ : Direction.YZ_PLANE;
            case POSITIVE_ZX:
                return diameters.get(Direction.NEGATIVE_ZX) > diameters.get(Direction.ZX_PLANE)
                        ? Direction.NEGATIVE_ZX : Direction.ZX_PLANE;
            case NEGATIVE_ZX:
                return diameters.get(Direction.POSITIVE_ZX) > diameters.get(Direction.ZX_PLANE)
                        ? Direction.POSITIVE_ZX : Direction.ZX_PLANE;
            default:
                return null;
        }
    }
    
    /**
     * Selects specified number of voxels from a focus voxel.
     *
     * @param voxels  the list of voxels
     * @param focus  the focus voxel
     * @param n  the number of voxels to select
     * @return  the list of selected voxels
     */
    static ArrayList<Voxel> getSelected(ArrayList<Voxel> voxels, Voxel focus, double n) {
        ArrayList<Voxel> selected = new ArrayList<>();
        double r = Math.cbrt((3 * n) / (4 * Math.PI));
        
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
