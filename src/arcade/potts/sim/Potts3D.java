package arcade.potts.sim;

import java.util.HashSet;
import arcade.potts.sim.hamiltonian.AdhesionHamiltonian3D;
import arcade.potts.sim.hamiltonian.Hamiltonian;
import arcade.potts.sim.hamiltonian.HeightHamiltonian;
import arcade.potts.sim.hamiltonian.JunctionHamiltonian;
import arcade.potts.sim.hamiltonian.PersistenceHamiltonian;
import arcade.potts.sim.hamiltonian.SubstrateHamiltonian;
import arcade.potts.sim.hamiltonian.SurfaceHamiltonian3D;
import arcade.potts.sim.hamiltonian.VolumeHamiltonian;
import static arcade.potts.util.PottsEnums.Term;

/**
 * Extension of {@link Potts} for 3D.
 */

public final class Potts3D extends Potts {
    /** Number of neighbors. */
    public static final int NUMBER_NEIGHBORS = 6;
    
    /** List of x direction movements (N, E, S, W, U, D). */
    public static final int[] MOVES_X = { 0, 1, 0, -1, 0, 0 };
    
    /** List of y direction movements (N, E, S, W, U, D). */
    public static final int[] MOVES_Y = { -1, 0, 1, 0, 0, 0 };
    
    /** List of z direction movements (N, E, S, W, U, D). */
    public static final int[] MOVES_Z = { 0, 0, 0, 0, 1, -1 };
    
    /** Number of neighbors in plane. */
    private static final int NUMBER_PLANE = 4;
    
    /** List of plane movements for first coordinate. */
    private static final int[] PLANE_A = { 0, 1, 0, -1 };
    
    /** List of plane movements for second coordinate. */
    private static final int[] PLANE_B = { -1, 0, 1, 0 };
    
    /** List of a direction corner movements. */
    private static final int[] CORNER_A = { 1, 1, -1, -1 };
    
    /** List of b direction corner movements. */
    private static final int[] CORNER_B = { -1, 1, 1, -1 };
    
    /**
     * Creates a cellular {@code Potts} model in 3D.
     *
     * @param series  the simulation series
     */
    public Potts3D(PottsSeries series) { super(series); }
    
    @Override
    Hamiltonian getHamiltonian(Term term, PottsSeries series) {
        switch (term) {
            case ADHESION:
                return new AdhesionHamiltonian3D(series, this);
            case VOLUME:
                return new VolumeHamiltonian(series);
            case SURFACE:
                return new SurfaceHamiltonian3D(series, this);
            case HEIGHT:
                return new HeightHamiltonian(series);
            case JUNCTION:
                return new JunctionHamiltonian(series, this);
            case SUBSTRATE:
                return new SubstrateHamiltonian(series, this);
            case PERSISTENCE:
                return new PersistenceHamiltonian(series);
            default:
                return null;
        }
    }
    
    @Override
    boolean[][][] getNeighborhood(int id, int x, int y, int z) {
        boolean[][][] array = new boolean[3][3][3];
        for (int k = 0; k < 3; k++) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    array[k][i][j] = ids[k + z - 1][i + x - 1][j + y - 1] == id;
                }
            }
        }
        return array;
    }
    
    @Override
    boolean[][][] getNeighborhood(int id, int region, int x, int y, int z) {
        boolean[][][] array = new boolean[3][3][3];
        for (int k = 0; k < 3; k++) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    array[k][i][j] = ids[k + z - 1][i + x - 1][j + y - 1] == id
                            && regions[k + z - 1][i + x - 1][j + y - 1] == region;
                }
            }
        }
        return array;
    }
    
    @Override
    boolean getConnectivity(boolean[][][] array, boolean zero) {
        int links = 0;
        for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
            if (array[1 + MOVES_Z[i]][1 + MOVES_X[i]][1 + MOVES_Y[i]]) { links++; }
        }
        
        switch (links) {
            case 1: return true;
            case 2: return getConnectivityTwoNeighbors(array);
            case 3: return getConnectivityThreeNeighbors(array);
            case 4: return getConnectivityFourNeighbors(array);
            case 5: return getConnectivityFiveNeighbors(array);
            case 6: return zero;
            default: return false;
        }
    }
    
    /**
     * Determines simple connectivity for a position with two neighbors.
     *
     * @param array  the local neighborhood array
     * @return  {@code true} if simply connected, {@code false} otherwise
     */
    private boolean getConnectivityTwoNeighbors(boolean[][][] array) {
        if (array[1][1][0] && array[1][1][2]) {
            // Check for opposites N/S
            return false;
        } else if (array[1][0][1] && array[1][2][1]) {
            // Check for opposites E/W
            return false;
        } else if (array[0][1][1] && array[2][1][1]) {
            // Check for opposites U/D
            return false;
        } else {
            // Check for corners
            for (int i = 0; i < NUMBER_PLANE; i++) {
                // XY plane
                boolean xy1 = array[1][1 + PLANE_A[i]][1 + PLANE_B[i]];
                boolean xy2 = array[1][1 + PLANE_A[(i + 1) % NUMBER_PLANE]]
                        [1 + PLANE_B[(i + 1) % NUMBER_PLANE]];
                boolean xy3 = array[1][1 + CORNER_A[i]][1 + CORNER_B[i]];
                if (xy1 && xy2 && xy3) { return true; }
                
                // YZ plane
                boolean yz1 = array[1 + PLANE_B[i]][1][1 + PLANE_A[i]];
                boolean yz2 = array[1 + PLANE_B[(i + 1) % NUMBER_PLANE]]
                        [1][1 + PLANE_A[(i + 1) % NUMBER_PLANE]];
                boolean yz3 = array[1 + CORNER_B[i]][1][1 + CORNER_A[i]];
                if (yz1 && yz2 && yz3) { return true; }
                
                // ZX plane
                boolean zx1 = array[1 + PLANE_A[i]][1 + PLANE_B[i]][1];
                boolean zx2 = array[1 + PLANE_A[(i + 1) % NUMBER_PLANE]]
                        [1 + PLANE_B[(i + 1) % NUMBER_PLANE]][1];
                boolean zx3 = array[1 + CORNER_A[i]][1 + CORNER_B[i]][1];
                if (zx1 && zx2 && zx3) { return true; }
            }
            return false;
        }
    }
    
    /**
     * Determines simple connectivity for a position with three neighbors.
     *
     * @param array  the local neighborhood array
     * @return  {@code true} if simply connected, {@code false} otherwise
     */
    private boolean getConnectivityThreeNeighbors(boolean[][][] array) {
        for (int i = 0; i < NUMBER_PLANE; i++) {
            // XY plane
            boolean xy1 = array[1][1 + PLANE_A[i]][1 + PLANE_B[i]];
            if (!xy1 && !array[0][1][1] && !array[2][1][1]) {
                boolean xy2 = array[1][1 + CORNER_A[(i + 1) % NUMBER_PLANE]]
                        [1 + CORNER_B[(i + 1) % NUMBER_PLANE]];
                boolean xy3 = array[1][1 + CORNER_A[(i + 2) % NUMBER_PLANE]]
                        [1 + CORNER_B[(i + 2) % NUMBER_PLANE]];
                if (xy2 && xy3) { return true; }
            }
        
            // YZ plane
            boolean yz1 = array[1 + PLANE_B[i]][1][1 + PLANE_A[i]];
            if (!yz1 && !array[1][0][1] && !array[1][2][1]) {
                boolean yz2 = array[1 + CORNER_B[(i + 1) % NUMBER_PLANE]][1]
                        [1 + CORNER_A[(i + 1) % NUMBER_PLANE]];
                boolean yz3 = array[1 + CORNER_B[(i + 2) % NUMBER_PLANE]][1]
                        [1 + CORNER_A[(i + 2) % NUMBER_PLANE]];
                if (yz2 && yz3) { return true; }
            }
        
            // ZX plane
            boolean zx1 = array[1 + PLANE_A[i]][1 + PLANE_B[i]][1];
            if (!zx1 && !array[1][1][0] && !array[1][1][2]) {
                boolean zx2 = array[1 + CORNER_A[(i + 1) % NUMBER_PLANE]]
                        [1 + CORNER_B[(i + 1) % NUMBER_PLANE]][1];
                boolean zx3 = array[1 + CORNER_A[(i + 2) % NUMBER_PLANE]]
                        [1 + CORNER_B[(i + 2) % NUMBER_PLANE]][1];
                if (zx2 && zx3) { return true; }
            }
        
            // XYZ corners
            boolean xyz1 = array[1][1 + PLANE_A[i]][1 + PLANE_B[i]];
            boolean xyz2 = array[1][1 + PLANE_A[(i + 1) % NUMBER_PLANE]]
                    [1 + PLANE_B[(i + 1) % NUMBER_PLANE]];
            if (xyz1 && xyz2) {
                boolean xyz3 = array[1][1 + CORNER_A[i]][1 + CORNER_B[i]];
            
                boolean xyz4a1 = array[0][1 + PLANE_A[i]][1 + PLANE_B[i]];
                boolean xyz4a2 = array[0][1 + PLANE_A[(i + 1) % NUMBER_PLANE]]
                        [1 + PLANE_B[(i + 1) % NUMBER_PLANE]];
                if (array[0][1][1] && (xyz3 ? (xyz4a1 || xyz4a2) : (xyz4a1 && xyz4a2))) {
                    return true;
                }
            
                boolean xyz4b1 = array[2][1 + PLANE_A[i]][1 + PLANE_B[i]];
                boolean xyz4b2 = array[2][1 + PLANE_A[(i + 1) % NUMBER_PLANE]]
                        [1 + PLANE_B[(i + 1) % NUMBER_PLANE]];
                if (array[2][1][1] && (xyz3 ? (xyz4b1 || xyz4b2) : (xyz4b1 && xyz4b2))) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Determines simple connectivity for a position with four neighbors.
     *
     * @param array  the local neighborhood array
     * @return  {@code true} if simply connected, {@code false} otherwise
     */
    private boolean getConnectivityFourNeighbors(boolean[][][] array) {
        if (!array[0][1][1] && !array[2][1][1]) {
            // Check for XY plane
            int n = 0;
            for (int i = 0; i < NUMBER_PLANE; i++) {
                n += (array[1][1 + CORNER_A[i]][1 + CORNER_B[i]] ? 1 : 0);
            }
            return n > 2;
        } else if (!array[1][0][1] && !array[1][2][1]) {
            // Check for YZ plane
            int n = 0;
            for (int i = 0; i < NUMBER_PLANE; i++) {
                n += (array[1 + CORNER_B[i]][1][1 + CORNER_A[i]] ? 1 : 0);
            }
            return n > 2;
        } else if (!array[1][1][0] && !array[1][1][2]) {
            // Check for ZX plane
            int n = 0;
            for (int i = 0; i < NUMBER_PLANE; i++) {
                n += (array[1 + CORNER_A[i]][1 + CORNER_B[i]][1] ? 1 : 0);
            }
            return n > 2;
        } else {
            boolean[] planeA = new boolean[2];
            boolean[] planeB = new boolean[2];
            boolean corner = false;
            
            for (int i = 0; i < NUMBER_PLANE; i++) {
                // Check for X
                boolean x1 = array[1 + PLANE_B[i]][1][1 + PLANE_A[i]];
                boolean x2 = array[1 + PLANE_B[(i + 1) % NUMBER_PLANE]]
                        [1][1 + PLANE_A[(i + 1) % NUMBER_PLANE]];
                if (array[1][0][1] && array[1][2][1] && x1 && x2) {
                    planeA = new boolean[] {
                            array[1 + PLANE_B[i]][0][1 + PLANE_A[i]],
                            array[1 + PLANE_B[i]][2][1 + PLANE_A[i]]
                    };
                    planeB = new boolean[] {
                            array[1 + PLANE_B[(i + 1) % NUMBER_PLANE]][0]
                                    [1 + PLANE_A[(i + 1) % NUMBER_PLANE]],
                            array[1 + PLANE_B[(i + 1) % NUMBER_PLANE]][2]
                                    [1 + PLANE_A[(i + 1) % NUMBER_PLANE]]
                    };
                    corner = array[1 + CORNER_B[i]][1][1 + CORNER_A[i]];
                    break;
                }
                
                // Check for Y
                boolean y1 = array[1 + PLANE_A[i]][1 + PLANE_B[i]][1];
                boolean y2 = array[1 + PLANE_A[(i + 1) % NUMBER_PLANE]]
                        [1 + PLANE_B[(i + 1) % NUMBER_PLANE]][1];
                if (array[1][1][0] && array[1][1][2] && y1 && y2) {
                    planeA = new boolean[] {
                            array[1 + PLANE_A[i]][1 + PLANE_B[i]][0],
                            array[1 + PLANE_A[i]][1 + PLANE_B[i]][2]
                    };
                    planeB = new boolean[] {
                            array[1 + PLANE_A[(i + 1) % NUMBER_PLANE]]
                                    [1 + PLANE_B[(i + 1) % NUMBER_PLANE]][0],
                            array[1 + PLANE_A[(i + 1) % NUMBER_PLANE]]
                                    [1 + PLANE_B[(i + 1) % NUMBER_PLANE]][2]
                    };
                    corner = array[1 + CORNER_A[i]][1 + CORNER_B[i]][1];
                    break;
                }
            
                // Check for Z
                boolean z1 = array[1][1 + PLANE_A[i]][1 + PLANE_B[i]];
                boolean z2 = array[1][1 + PLANE_A[(i + 1) % NUMBER_PLANE]]
                        [1 + PLANE_B[(i + 1) % NUMBER_PLANE]];
                if (array[0][1][1] && array[2][1][1] && z1 && z2) {
                    planeA = new boolean[] {
                            array[0][1 + PLANE_A[i]][1 + PLANE_B[i]],
                            array[2][1 + PLANE_A[i]][1 + PLANE_B[i]]
                    };
                    planeB = new boolean[] {
                            array[0][1 + PLANE_A[(i + 1) % NUMBER_PLANE]]
                                    [1 + PLANE_B[(i + 1) % NUMBER_PLANE]],
                            array[2][1 + PLANE_A[(i + 1) % NUMBER_PLANE]]
                                    [1 + PLANE_B[(i + 1) % NUMBER_PLANE]]
                    };
                    corner = array[1][1 + CORNER_A[i]][1 + CORNER_B[i]];
                }
            }
            
            if (planeA[0] && planeA[1] && (planeB[0] || planeB[1] || corner)) {
                return true;
            } else if (planeB[0] && planeB[1] && (planeA[0] || planeA[1] || corner)) {
                return true;
            } else {
                return corner && ((planeA[0] && planeB[1]) || (planeA[1] && planeB[0]));
            }
        }
    }
    
    /**
     * Determines simple connectivity for a position with five neighbors.
     *
     * @param array  the local neighborhood array
     * @return  {@code true} if simply connected, {@code false} otherwise
     */
    private boolean getConnectivityFiveNeighbors(boolean[][][] array) {
        boolean[] plane = new boolean[NUMBER_PLANE];
        boolean[] corner = new boolean[NUMBER_PLANE];
        int nPlane = 0;
        int nCorner = 0;
        
        if (!array[0][1][1] || !array[2][1][1]) {
            // Check XY
            int z = (array[0][1][1] ? 0 : 2);
            for (int i = 0; i < NUMBER_PLANE; i++) {
                corner[i] = array[1][1 + CORNER_A[i]][1 + CORNER_B[i]];
                plane[i] = array[z][1 + PLANE_A[i]][1 + PLANE_B[i]];
                if (corner[i]) { nCorner++; }
                if (plane[i]) { nPlane++; }
            }
        } else if (!array[1][0][1] || !array[1][2][1]) {
            // Check YZ
            int x = (array[1][0][1] ? 0 : 2);
            for (int i = 0; i < NUMBER_PLANE; i++) {
                corner[i] = array[1 + CORNER_A[i]][1][1 + CORNER_B[i]];
                plane[i] = array[1 + PLANE_A[i]][x][1 + PLANE_B[i]];
                if (corner[i]) { nCorner++; }
                if (plane[i]) { nPlane++; }
            }
        } else { // !array[1][1][0] || !array[1][1][2]
            // Check ZX
            int y = (array[1][1][0] ? 0 : 2);
            for (int i = 0; i < NUMBER_PLANE; i++) {
                corner[i] = array[1 + CORNER_A[i]][1 + CORNER_B[i]][1];
                plane[i] = array[1 + PLANE_A[i]][1 + PLANE_B[i]][y];
                if (corner[i]) { nCorner++; }
                if (plane[i]) { nPlane++; }
            }
        }
    
        if (nCorner + nPlane < 4) {
            return false;
        } else if (nPlane == 4 || nCorner + nPlane > 5) {
            return true;
        } else if (nCorner > 2) {
            return nPlane > 0;
        } else if (nCorner == 1) { // nPlane == 3
            for (int i = 0; i < NUMBER_PLANE; i++) {
                if (!plane[i] && (corner[i] || corner[(i + 3) % NUMBER_PLANE])) {
                    return true;
                }
            }
            return false;
        } else if (nPlane == 2) { // nCorner == 2
            boolean isAdjacent = false;
            int index = NUMBER_PLANE;
            for (int i = 0; i < NUMBER_PLANE; i++) {
                if (plane[i] && plane[(i + 1) % NUMBER_PLANE]) {
                    isAdjacent = true;
                    index = i;
                } else if (plane[i] && plane[(i + 2) % NUMBER_PLANE]) {
                    index = i;
                }
            }
            
            if (isAdjacent) {
                return !corner[index];
            } else {
                boolean corner1 = corner[(index + 1) % NUMBER_PLANE];
                boolean corner2 = corner[(index + 2) % NUMBER_PLANE];
                boolean corner3 = corner[(index + 3) % NUMBER_PLANE];
                return (!corner[index] || !corner1) && (!corner2 || !corner3);
            }
        } else { // nCorner == 2 && nPlane == 3
            int index = NUMBER_PLANE;
            for (int i = 0; i < NUMBER_PLANE; i++) {
                if (!plane[i]) { index = i; }
            }
            return corner[index] || corner[(index + 3) % NUMBER_PLANE];
        }
    }
    
    @Override
    HashSet<Integer> getUniqueIDs(int x, int y, int z) {
        int id = ids[z][x][y];
        HashSet<Integer> unique = new HashSet<>();
        
        for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
            int neighbor = ids[z + MOVES_Z[i]][x + MOVES_X[i]][y + MOVES_Y[i]];
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
            int neighborID = ids[z + MOVES_Z[i]][x + MOVES_X[i]][y + MOVES_Y[i]];
            int neighborRegion = regions[z + MOVES_Z[i]][x + MOVES_X[i]][y + MOVES_Y[i]];
            
            if (neighborID != id) { continue; }
            if (region != neighborRegion) { unique.add(neighborRegion); }
        }
        
        return unique;
    }
}
