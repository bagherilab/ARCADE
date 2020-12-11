package arcade.potts.sim;

import java.util.HashSet;
import arcade.potts.agent.cell.PottsCell;
import static arcade.core.util.Enums.Region;

public final class Potts2D extends Potts {
    /** Number of neighbors. */
    public static final int NUMBER_NEIGHBORS = 4;
    
    /** List of x direction movements (N, E, S, W). */
    public static final int[] MOVES_X = { 0, 1, 0, -1 };
    
    /** List of y direction movements (N, E, S, W). */
    public static final int[] MOVES_Y = { -1, 0, 1, 0 };
    
    /** List of x direction corner movements (NE, SE, SW, NW). */
    private static final int[] CORNER_X = { 1, 1, -1, -1 };
    
    /** List of y direction corner movements (NE, SE, SW, NW). */
    private static final int[] CORNER_Y = { -1, 1, 1, -1 };
    
    /**
     * Creates a cellular {@code Potts} model in 2D.
     * 
     * @param series  the simulation series
     */
    public Potts2D(PottsSeries series) { super(series); }
    
    @Override
    double getAdhesion(int id, int x, int y, int z) {
        double h = 0;
        PottsCell a = getCell(id);
        
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                if (!(i == x && j == y) && ids[z][i][j] != id) {
                    PottsCell b = getCell(ids[z][i][j]);
                    if (a == null) {
                        h += b.getAdhesion(0);
                    } else if (b == null) {
                        h += a.getAdhesion(0);
                    } else {
                        h += (a.getAdhesion(b.getPop()) + b.getAdhesion(a.getPop())) / 2.0;
                    }
                }
            }
        }
        
        return h;
    }
    
    @Override
    double getAdhesion(int id, int t, int x, int y, int z) {
        double h = 0;
        PottsCell c = getCell(id);
        Region region = Region.values()[t];
        
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                Region xy = Region.values()[regions[z][i][j]];
                if (!(i == x && j == y) && ids[z][i][j] == id && xy != region
                        && xy != Region.UNDEFINED && xy != Region.DEFAULT) {
                    h += (c.getAdhesion(region, xy) + c.getAdhesion(xy, region)) / 2.0;
                }
            }
        }
        
        return h;
    }
    
    @Override
    int[] calculateChange(int sourceID, int targetID, int x, int y, int z) {
        int beforeSource = 0;
        int afterSource = 0;
        int beforeTarget = 0;
        int afterTarget = 0;
        
        // Iterate through each neighbor.
        for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
            int neighbor = ids[z][x + MOVES_X[i]][y + MOVES_Y[i]];
            
            if (neighbor != sourceID) {
                beforeSource++;
                if (neighbor == targetID) { beforeTarget++; }
            }
            
            if (neighbor != targetID) {
                afterTarget++;
                if (neighbor == sourceID) { afterSource++; }
            }
        }
        
        // Save changes to surface.
        int sourceSurfaceChange = afterSource - beforeSource;
        int targetSurfaceChange = afterTarget - beforeTarget;
        
        return new int[] { sourceSurfaceChange, targetSurfaceChange };
    }
    
    @Override
    int[] calculateChange(int id, int sourceRegion, int targetRegion, int x, int y, int z) {
        int beforeSource = 0;
        int afterSource = 0;
        int beforeTarget = 0;
        int afterTarget = 0;
        
        // Iterate through each neighbor.
        for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
            int neighborID = ids[z][x + MOVES_X[i]][y + MOVES_Y[i]];
            int neighborRegion = regions[z][x + MOVES_X[i]][y + MOVES_Y[i]];
            
            if (neighborRegion != sourceRegion || neighborID != id) {
                beforeSource++;
                if (neighborRegion == targetRegion && neighborID == id) { beforeTarget++; }
            }
            
            if (neighborRegion != targetRegion || neighborID != id) {
                afterTarget++;
                if (neighborRegion == sourceRegion && neighborID == id) { afterSource++; }
            }
        }
        
        // Save changes to surface.
        int sourceSurfaceChange = afterSource - beforeSource;
        int targetSurfaceChange = afterTarget - beforeTarget;
        
        return new int[] { sourceSurfaceChange, targetSurfaceChange };
    }
    
    @Override
    boolean[][][] getNeighborhood(int id, int x, int y, int z) {
        boolean[][] array = new boolean[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                array[i][j] = ids[0][i + x - 1][j + y - 1] == id;
            }
        }
        return new boolean[][][] { array };
    }
    
    @Override
    boolean[][][] getNeighborhood(int id, int region, int x, int y, int z) {
        boolean[][] array = new boolean[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                array[i][j] = ids[0][i + x - 1][j + y - 1] == id
                        && regions[0][i + x - 1][j + y - 1] == region;
            }
        }
        return new boolean[][][] { array };
    }
    
    @Override
    boolean getConnectivity(boolean[][][] array, boolean zero) {
        boolean[][] subarray = array[0];
        int links = 0;
        for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
            if (subarray[1 + MOVES_X[i]][1 + MOVES_Y[i]]) { links++; }
        }
        
        switch (links) {
            case 1: return true;
            case 2: return getConnectivityTwoNeighbors(subarray);
            case 3: return getConnectivityThreeNeighbors(subarray);
            case 4: return zero;
            default: return false;
        }
    }
    
    private boolean getConnectivityTwoNeighbors(boolean[][] subarray) {
        if (subarray[1][2] && subarray[1][0]) {
            // Check for opposites N / S
            return false;
        } else if (subarray[2][1] && subarray[0][1]) {
            // Check for opposites E / W
            return false;
        } else {
            // Check for corners
            for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
                boolean check1 = subarray[1 + MOVES_X[i]][1 + MOVES_Y[i]];
                boolean check2 = subarray[1 + MOVES_X[(i + 1) % NUMBER_NEIGHBORS]]
                        [1 + MOVES_Y[(i + 1) % NUMBER_NEIGHBORS]];
                boolean check3 = subarray[1 + CORNER_X[i]][1 + CORNER_Y[i]];
                if (check1 && check2 && check3) { return true; }
            }
            return false;
        }
    }
    
    private boolean getConnectivityThreeNeighbors(boolean[][] subarray) {
        for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
            if (!subarray[1 + MOVES_X[i]][1 + MOVES_Y[i]]) {
                boolean check1 = subarray[1 + CORNER_X[(i + 1) % NUMBER_NEIGHBORS]]
                        [1 + CORNER_Y[(i + 1) % NUMBER_NEIGHBORS]];
                boolean check2 = subarray[1 + CORNER_X[(i + 2) % NUMBER_NEIGHBORS]]
                        [1 + CORNER_Y[(i + 2) % NUMBER_NEIGHBORS]];
                if (check1 && check2) { return true; }
            }
        }
        return false;
    }
    
    @Override
    HashSet<Integer> getUniqueIDs(int x, int y, int z) {
        int id = ids[z][x][y];
        HashSet<Integer> unique = new HashSet<>();
        
        for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
            int neighbor = ids[z][x + MOVES_X[i]][y + MOVES_Y[i]];
            if (id != neighbor) { unique.add(neighbor); }
        }
        return unique;
    }
    
    @Override
    HashSet<Integer> getUniqueRegions(int x, int y, int z) {
        int id = ids[z][x][y];
        int region = regions[z][x][y];
        HashSet<Integer> unique = new HashSet<>();
        
        for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
            int neighborID = ids[z][x + MOVES_X[i]][y + MOVES_Y[i]];
            int neighborRegion = regions[z][x + MOVES_X[i]][y + MOVES_Y[i]];
            
            if (neighborID != id) { continue; }
            if (region != neighborRegion) { unique.add(neighborRegion); }
        }
        
        return unique;
    }
}
