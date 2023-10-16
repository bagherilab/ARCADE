package arcade.potts.sim;

import java.util.HashSet;
import org.junit.BeforeClass;
import org.junit.Test;
import arcade.potts.sim.hamiltonian.AdhesionHamiltonian3D;
import arcade.potts.sim.hamiltonian.Hamiltonian;
import arcade.potts.sim.hamiltonian.HeightHamiltonian;
import arcade.potts.sim.hamiltonian.JunctionHamiltonian;
import arcade.potts.sim.hamiltonian.PersistenceHamiltonian;
import arcade.potts.sim.hamiltonian.SubstrateHamiltonian;
import arcade.potts.sim.hamiltonian.SurfaceHamiltonian3D;
import arcade.potts.sim.hamiltonian.VolumeHamiltonian;
import static org.junit.Assert.*;
import static arcade.potts.sim.PottsTest.*;
import static arcade.potts.util.PottsEnums.Region;
import static arcade.potts.util.PottsEnums.Term;

public class Potts3DTest {
    static Potts3D potts;
    
    private static final double[][] VOLUME_MCS = new double[][] {
            { 19669, 13, 4.441502 }, {  1586,  2, 1.000000 }, { 12688, 16, 4.703909 },
            { 17430, 14, 4.553762 }, { 11055, 11, 3.770773 }, { 14224, 16, 4.771082 },
            { 20250, 18, 5.307441 }, {  3730, 10, 3.109355 }, { 33286, 22, 6.269841 },
            {  2185,  5, 1.926825 }, {  8364, 12, 3.817978 }, { 36638, 14, 4.993200 },
            { 34938, 18, 5.674056 }, { 73034, 26, 7.519614 }, {  9779, 11, 3.712390 },
            {  4539,  3, 1.555123 }, { 14694,  6, 2.784049 }, {     1,  1, 1.000000 },
            { 32997, 17, 5.466454 }, { 45700, 20, 6.197381 }, { 16872,  8, 3.336015 },
            {  3578,  2, 1.143097 }, {  6223,  7, 2.714233 }, {  3050, 10, 3.028743 },
            { 10468,  4, 2.095892 }, { 16643, 11, 3.971434 }, { 78652, 28, 7.883265 },
            {  1778,  2, 1.019109 }, { 15059, 11, 3.921524 }, { 81461, 29, 8.061169 },
            { 21437, 13, 4.489470 }, { 32980, 20, 5.956613 }, {  1369,  1, 1.000000 },
            { 46398, 22, 6.526960 }, {  3485,  5, 2.057350 }, { 48980, 20, 6.249687 },
            { 21904, 16, 5.032787 }, {  5481,  9, 3.080511 }, {   870,  6, 1.891372 },
            { 16428, 12, 4.157828 }, { 84270, 30, 8.236625 }, { 44082, 18, 5.837273 },
            { 68572, 28, 7.755204 }, { 39255, 15, 5.224730 }, { 18139, 11, 4.014838 },
            {  5476,  4, 1.913844 }, { 52340, 20, 6.300139 }, { 19788, 12, 4.256130 },
    };
    
    enum Axis { X_AXIS, Y_AXIS, Z_AXIS }
    
    @BeforeClass
    public static void setupGrid() {
        PottsSeries series = makeSeries();
        potts = new Potts3D(series);
        
        potts.ids = new int[][][] {
                {
                        { 0, 0, 0, 0, 0, 0 },
                        { 0, 0, 0, 0, 0, 0 },
                        { 0, 0, 0, 0, 0, 0 },
                        { 0, 0, 0, 0, 0, 0 },
                        { 0, 0, 0, 0, 0, 0 },
                },
                {
                        { 0, 0, 0, 0, 0, 0 },
                        { 0, 0, 1, 0, 3, 0 },
                        { 0, 0, 1, 3, 3, 0 },
                        { 0, 2, 0, 0, 0, 0 },
                        { 0, 0, 0, 0, 0, 0 },
                },
                {
                        { 0, 0, 0, 0, 0, 0 },
                        { 0, 1, 1, 3, 3, 0 },
                        { 0, 1, 1, 3, 3, 0 },
                        { 0, 2, 2, 0, 0, 0 },
                        { 0, 0, 0, 0, 0, 0 },
                },
                {
                        { 0, 0, 0, 0, 0, 0 },
                        { 0, 1, 1, 0, 0, 0 },
                        { 0, 1, 2, 0, 3, 0 },
                        { 0, 2, 2, 0, 0, 0 },
                        { 0, 0, 0, 0, 0, 0 },
                },
                {
                        { 0, 0, 0, 0, 0, 0 },
                        { 0, 0, 0, 0, 0, 0 },
                        { 0, 0, 0, 0, 0, 0 },
                        { 0, 0, 0, 0, 0, 0 },
                        { 0, 0, 0, 0, 0, 0 },
                },
        };
        
        int d = Region.DEFAULT.ordinal();
        int n = Region.NUCLEUS.ordinal();
        
        potts.regions = new int[][][] {
                {
                        { 0, 0, 0, 0, 0, 0 },
                        { 0, 0, 0, 0, 0, 0 },
                        { 0, 0, 0, 0, 0, 0 },
                        { 0, 0, 0, 0, 0, 0 },
                        { 0, 0, 0, 0, 0, 0 },
                },
                {
                        { 0, 0, 0, 0, 0, 0 },
                        { 0, 0, d, 0, d, 0 },
                        { 0, 0, d, d, d, 0 },
                        { 0, d, 0, 0, 0, 0 },
                        { 0, 0, 0, 0, 0, 0 },
                },
                {
                        { 0, 0, 0, 0, 0, 0 },
                        { 0, d, d, d, 0, 0 },
                        { 0, 0, n, n, 0, 0 },
                        { 0, d, d, 0, 0, 0 },
                        { 0, 0, 0, 0, 0, 0 },
                },
                {
                        { 0, 0, 0, 0, 0, 0 },
                        { 0, 0, d, 0, 0, 0 },
                        { 0, 0, n, 0, d, 0 },
                        { 0, d, d, 0, 0, 0 },
                        { 0, 0, 0, 0, 0, 0 },
                },
                {
                        { 0, 0, 0, 0, 0, 0 },
                        { 0, 0, 0, 0, 0, 0 },
                        { 0, 0, 0, 0, 0, 0 },
                        { 0, 0, 0, 0, 0, 0 },
                        { 0, 0, 0, 0, 0, 0 },
                },
        };
    }
    
    private static boolean[][][] duplicate(boolean[][][] array) {
        boolean[][][] duplicated = new boolean[3][3][3];
        for (int k = 0; k < 3; k++) {
            for (int i = 0; i < 3; i++) {
                System.arraycopy(array[k][i], 0, duplicated[k][i], 0, 3);
            }
        }
        return duplicated;
    }
    
    private static boolean[][][] combine(boolean[][][] base, int[] combo, int[][] links) {
        boolean[][][] array = duplicate(base);
        for (int i : combo) {
            array[links[0][i]][links[1][i]][links[2][i]] = true;
        }
        return array;
    }
    
    private static boolean[][][] rotate(boolean[][][] array, Axis axis, int rotations) {
        boolean[][][] rotated = duplicate(array);
        
        for (int rotation = 0; rotation < rotations; rotation++) {
            for (int k = 0; k < 3; k++) {
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        switch (axis) {
                            case X_AXIS:
                                rotated[k][i][j] = array[j][i][2 - k];
                                break;
                            case Y_AXIS:
                                rotated[k][i][j] = array[2 - i][k][j];
                                break;
                            case Z_AXIS:
                                rotated[k][i][j] = array[k][2 - j][i];
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
            
            array = duplicate(rotated);
        }
        
        return rotated;
    }
    
    private static boolean[][][] rotate(boolean[][][] array, int rotations) {
        boolean[][][] rotated = duplicate(array);
        
        for (int rotation = 0; rotation < rotations; rotation++) {
            for (int k = 0; k < 3; k++) {
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        rotated[k][i][j] = array[i][j][2 - k];
                    }
                }
            }
            
            array = duplicate(rotated);
        }
        
        return rotated;
    }
    
    private static void populate(int[][] array, int n, int k) {
        int index = 0;
        int[] s = new int[k];
        
        for (int i = 0; i < k; i++) {
            s[i] = i;
        }
        
        array[index++] = s.clone();
        
        for (;;) {
            int ii = k - 1;
            for (int i = k - 1; i >= 0 && s[i] == n - k + i; i--) {
                ii--;
            }
            
            if (ii < 0) {
                break;
            }
            
            s[ii]++;
            for (++ii; ii < k; ii++) {
                s[ii] = s[ii - 1] + 1;
            }
            
            array[index++] = s.clone();
        }
    }
    
    @Test
    public void getHamiltonian_validTerm_instantiatesObject() {
        PottsSeries series = makeSeries(1, 1, 1, 1, 1);
        Potts3D potts3D = new Potts3D(series);
        
        Hamiltonian h;
        
        h = potts3D.getHamiltonian(Term.ADHESION, series);
        assertTrue(h instanceof AdhesionHamiltonian3D);
        
        h = potts3D.getHamiltonian(Term.VOLUME, series);
        assertTrue(h instanceof VolumeHamiltonian);
        
        h = potts3D.getHamiltonian(Term.SURFACE, series);
        assertTrue(h instanceof SurfaceHamiltonian3D);
        
        h = potts3D.getHamiltonian(Term.HEIGHT, series);
        assertTrue(h instanceof HeightHamiltonian);
        
        h = potts3D.getHamiltonian(Term.JUNCTION, series);
        assertTrue(h instanceof JunctionHamiltonian);
        
        h = potts3D.getHamiltonian(Term.SUBSTRATE, series);
        assertTrue(h instanceof SubstrateHamiltonian);
        
        h = potts3D.getHamiltonian(Term.PERSISTENCE, series);
        assertTrue(h instanceof PersistenceHamiltonian);
    }
    
    @Test
    public void getHamiltonian_invalidTerm_returnsNull() {
        PottsSeries series = makeSeries(1, 1, 1, 1, 1);
        Potts3D potts3D = new Potts3D(series);
        
        Hamiltonian h;
        
        h = potts3D.getHamiltonian(Term.UNDEFINED, series);
        assertNull(h);
    }
    
    @Test
    public void getNeighborhood_givenID_createsArray() {
        boolean[][][] array1 = potts.getNeighborhood(1, 2, 2, 2);
        assertArrayEquals(new boolean[] { false,  true, false }, array1[0][0]);
        assertArrayEquals(new boolean[] { false,  true, false }, array1[0][1]);
        assertArrayEquals(new boolean[] { false, false, false }, array1[0][2]);
        assertArrayEquals(new boolean[] {  true,  true, false }, array1[1][0]);
        assertArrayEquals(new boolean[] {  true,  true, false }, array1[1][1]);
        assertArrayEquals(new boolean[] { false, false, false }, array1[1][2]);
        assertArrayEquals(new boolean[] {  true,  true, false }, array1[2][0]);
        assertArrayEquals(new boolean[] {  true, false, false }, array1[2][1]);
        assertArrayEquals(new boolean[] { false, false, false }, array1[2][2]);
        
        boolean[][][] array2 = potts.getNeighborhood(2, 2, 2, 2);
        assertArrayEquals(new boolean[] { false, false, false }, array2[0][0]);
        assertArrayEquals(new boolean[] { false, false, false }, array2[0][1]);
        assertArrayEquals(new boolean[] {  true, false, false }, array2[0][2]);
        assertArrayEquals(new boolean[] { false, false, false }, array2[1][0]);
        assertArrayEquals(new boolean[] { false, false, false }, array2[1][1]);
        assertArrayEquals(new boolean[] {  true,  true, false }, array2[1][2]);
        assertArrayEquals(new boolean[] { false, false, false }, array2[2][0]);
        assertArrayEquals(new boolean[] { false,  true, false }, array2[2][1]);
        assertArrayEquals(new boolean[] {  true,  true, false }, array2[2][2]);
        
        boolean[][][] array3 = potts.getNeighborhood(3, 2, 2, 2);
        assertArrayEquals(new boolean[] { false, false, false }, array3[0][0]);
        assertArrayEquals(new boolean[] { false, false,  true }, array3[0][1]);
        assertArrayEquals(new boolean[] { false, false, false }, array3[0][2]);
        assertArrayEquals(new boolean[] { false, false,  true }, array3[1][0]);
        assertArrayEquals(new boolean[] { false, false,  true }, array3[1][1]);
        assertArrayEquals(new boolean[] { false, false, false }, array3[1][2]);
        assertArrayEquals(new boolean[] { false, false, false }, array3[2][0]);
        assertArrayEquals(new boolean[] { false, false, false }, array3[2][1]);
        assertArrayEquals(new boolean[] { false, false, false }, array3[2][2]);
    }
    
    @Test
    public void getNeighborhood_givenRegion_createsArray() {
        boolean[][][] array1 = potts.getNeighborhood(1, Region.DEFAULT.ordinal(), 2, 2, 2);
        assertArrayEquals(new boolean[] { false,  true, false }, array1[0][0]);
        assertArrayEquals(new boolean[] { false,  true, false }, array1[0][1]);
        assertArrayEquals(new boolean[] { false, false, false }, array1[0][2]);
        assertArrayEquals(new boolean[] {  true,  true, false }, array1[1][0]);
        assertArrayEquals(new boolean[] { false, false, false }, array1[1][1]);
        assertArrayEquals(new boolean[] { false, false, false }, array1[1][2]);
        assertArrayEquals(new boolean[] { false,  true, false }, array1[2][0]);
        assertArrayEquals(new boolean[] { false, false, false }, array1[2][1]);
        assertArrayEquals(new boolean[] { false, false, false }, array1[2][2]);
        
        boolean[][][] array2 = potts.getNeighborhood(1, Region.NUCLEUS.ordinal(), 2, 2, 2);
        assertArrayEquals(new boolean[] { false, false, false }, array2[0][0]);
        assertArrayEquals(new boolean[] { false, false, false }, array2[0][1]);
        assertArrayEquals(new boolean[] { false, false, false }, array2[0][2]);
        assertArrayEquals(new boolean[] { false, false, false }, array2[1][0]);
        assertArrayEquals(new boolean[] { false,  true, false }, array2[1][1]);
        assertArrayEquals(new boolean[] { false, false, false }, array2[1][2]);
        assertArrayEquals(new boolean[] { false, false, false }, array2[2][0]);
        assertArrayEquals(new boolean[] { false, false, false }, array2[2][1]);
        assertArrayEquals(new boolean[] { false, false, false }, array2[2][2]);
    }
    
    private HashSet<Integer> checkUniqueID(Potts3D potts3D, int[][][] ids) {
        potts3D.ids = ids;
        return potts3D.getUniqueIDs(1, 1, 1);
    }
    
    @Test
    public void getUniqueIDs_validVoxel_returnsList() {
        PottsSeries series = makeSeries();
        Potts3D pottsMock = new Potts3D(series);
        HashSet<Integer> unique = new HashSet<>();
        
        unique.add(1);
        assertEquals(unique, checkUniqueID(pottsMock, new int[][][] {
                {
                        { 0, 0, 0 },
                        { 0, 1, 0 },
                        { 0, 0, 0 }
                },
                {
                        { 0, 0, 0 },
                        { 0, 0, 0 },
                        { 0, 0, 0 }
                },
                {
                        { 0, 0, 0 },
                        { 0, 0, 0 },
                        { 0, 0, 0 }
                }
        }));
        
        unique.clear();
        unique.add(0);
        assertEquals(unique, checkUniqueID(pottsMock, new int[][][] {
                {
                        { 0, 0, 0 },
                        { 0, 0, 0 },
                        { 0, 0, 0 }
                },
                {
                        { 0, 1, 0 },
                        { 0, 1, 0 },
                        { 0, 0, 0 }
                },
                {
                        { 0, 0, 0 },
                        { 0, 0, 0 },
                        { 0, 0, 0 }
                }
        }));
        
        unique.clear();
        assertEquals(unique, checkUniqueID(pottsMock, new int[][][] {
                {
                        { 1, 1, 1 },
                        { 1, 0, 1 },
                        { 1, 1, 1 }
                },
                {
                        { 1, 0, 1 },
                        { 0, 0, 0 },
                        { 1, 0, 1 }
                },
                {
                        { 1, 1, 1 },
                        { 1, 0, 1 },
                        { 1, 1, 1 }
                }
        }));
    }
    
    private HashSet<Integer> checkUniqueRegion(Potts3D potts3D, int[][][] ids, int[][][] regions) {
        potts3D.ids = ids;
        potts3D.regions = regions;
        return potts3D.getUniqueRegions(1, 1, 1);
    }
    
    @Test
    public void getUniqueRegions_validVoxel_returnsList() {
        PottsSeries series = makeSeries();
        Potts3D pottsMock = new Potts3D(series);
        HashSet<Integer> unique = new HashSet<>();
        
        assertEquals(unique, checkUniqueRegion(pottsMock, new int[][][] {
                {
                        { 0, 0, 0 },
                        { 0, 1, 0 },
                        { 0, 0, 0 }
                },
                {
                        { 0, 0, 0 },
                        { 0, 0, 0 },
                        { 0, 0, 0 }
                },
                {
                        { 0, 0, 0 },
                        { 0, 0, 0 },
                        { 0, 0, 0 }
                }
        }, new int[][][] {
                {
                        { 0, 0, 0 },
                        { 0, -1, 0 },
                        { 0, 0, 0 }
                },
                {
                        { 0, 0, 0 },
                        { 0, 0, 0 },
                        { 0, 0, 0 }
                },
                {
                        { 0, 0, 0 },
                        { 0, 0, 0 },
                        { 0, 0, 0 }
                }
        }));
        
        assertEquals(unique, checkUniqueRegion(pottsMock, new int[][][] {
                {
                        { 1, 1, 1 },
                        { 1, 1, 1 },
                        { 1, 1, 1 }
                },
                {
                        { 1, 1, 1 },
                        { 1, 1, 1 },
                        { 1, 1, 1 }
                },
                {
                        { 1, 1, 1 },
                        { 1, 1, 1 },
                        { 1, 1, 1 }
                }
        }, new int[][][] {
                {
                        { -2, -2, -2 },
                        { -2, -1, -2 },
                        { -2, -2, -2 }
                },
                {
                        { -2, -1, -2 },
                        { -1, -1, -1 },
                        { -2, -1, -2 }
                },
                {
                        { -2, -2, -2 },
                        { -2, -1, -2 },
                        { -2, -2, -2 }
                }
        }));
        
        unique.add(-2);
        unique.add(-5);
        assertEquals(unique, checkUniqueRegion(pottsMock, new int[][][] {
                {
                        { 0, 0, 0 },
                        { 0, 1, 0 },
                        { 0, 0, 0 }
                },
                {
                        { 0, 1, 0 },
                        { 1, 1, 2 },
                        { 0, 2, 0 }
                },
                {
                        { 0, 0, 0 },
                        { 0, 2, 0 },
                        { 0, 0, 0 }
                }
        }, new int[][][] {
                {
                        {  0,  0,  0 },
                        {  0, -5,  0 },
                        {  0,  0,  0 }
                },
                {
                        {  0, -1,  0 },
                        { -2, -1, -4 },
                        {  0, -3,  0 }
                },
                {
                        {  0,  0,  0 },
                        {  0, -6,  0 },
                        {  0,  0,  0 }
                }
        }));
    }
    
    /* -------------------------------------------------------------------------
     * CONNECTIVITY FOR ZERO (0) NEIGHBORS
     *
     * If there are zero neighbors, then the voxel is never connected.
    ------------------------------------------------------------------------- */
    
    @Test
    public void getConnectivity_zeroNeighbors_returnsFalse() {
        assertFalse(potts.getConnectivity(new boolean[][][] {
            {
                    { false, false, false },
                    { false, false, false },
                    { false, false, false }
            },
            {
                    { false, false, false },
                    { false,  true, false },
                    { false, false, false }
            },
            {
                    { false, false, false },
                    { false, false, false },
                    { false, false, false }
            }
        }, false));
    }
    
    /* -------------------------------------------------------------------------
     * CONNECTIVITY FOR ONE (1) NEIGHBOR
     *
     * The neighbor can be located on each face of the cube (6 options).
     *
     * If there is only one neighbor, the voxel is always connected.
    ------------------------------------------------------------------------- */
    
    private static final boolean[][][] BASE_ONE_NEIGHBOR = new boolean[][][] {
            {
                    { false, false, false },
                    { false, false, false },
                    { false, false, false }
            },
            {
                    { false,  true, false },
                    { false,  true, false },
                    { false, false, false }
            },
            {
                    { false, false, false },
                    { false, false, false },
                    { false, false, false }
            }
    };
    
    @Test
    public void getConnectivity_oneNeighbor_returnsTrue() {
        for (int rotation = 0; rotation < 6; rotation++) {
            boolean[][][] array = rotate(BASE_ONE_NEIGHBOR, rotation);
            assertTrue(potts.getConnectivity(array, false));
        }
    }
    
    /* -------------------------------------------------------------------------
     * CONNECTIVITY FOR TWO (2) NEIGHBORS
     *
     * The two neighbors can be either adjacent in the same plane (3 planes x 4
     * rotations = 12 options) or opposite in the same plane (3 options).
     *
     * If there are two opposite neighbors, the voxel is never connected.
     *
     * If there are two adjacent neighbors, the voxel is connected if there is
     * a link in the shared corner.
    ------------------------------------------------------------------------- */
    
    private static final boolean[][][] BASE_TWO_NEIGHBORS_OPPOSITE = new boolean[][][] {
            {
                    { false, false, false },
                    { false, false, false },
                    { false, false, false }
            },
            {
                    { false,  true, false },
                    { false,  true, false },
                    { false,  true, false }
            },
            {
                    { false, false, false },
                    { false, false, false },
                    { false, false, false }
            }
    };
    
    private static final boolean[][][] BASE_TWO_NEIGHBORS_ADJACENT_XY = new boolean[][][] {
            {
                    { false, false, false },
                    { false, false, false },
                    { false, false, false }
            },
            {
                    { false,  true, false },
                    {  true,  true, false },
                    { false, false, false }
            },
            {
                    { false, false, false },
                    { false, false, false },
                    { false, false, false }
            }
    };
    
    private static final boolean[][][] BASE_TWO_NEIGHBORS_ADJACENT_YZ = new boolean[][][] {
            {
                    { false, false, false },
                    { false,  true, false },
                    { false, false, false }
            },
            {
                    { false, false, false },
                    {  true,  true, false },
                    { false, false, false }
            },
            {
                    { false, false, false },
                    { false, false, false },
                    { false, false, false }
            }
    };
    
    private static final boolean[][][] BASE_TWO_NEIGHBORS_ADJACENT_ZX = new boolean[][][] {
            {
                    { false, false, false },
                    { false,  true, false },
                    { false, false, false }
            },
            {
                    { false,  true, false },
                    { false,  true, false },
                    { false, false, false }
            },
            {
                    { false, false, false },
                    { false, false, false },
                    { false, false, false }
            }
    };
    
    private static final int[][] LINKS_TWO_NEIGHBORS_ADJACENT_XY = new int[][] {
            { 1 }, // Z
            { 0 }, // X
            { 0 }, // Y
    };
    
    private static final int[][] LINKS_TWO_NEIGHBORS_ADJACENT_YZ = new int[][] {
            { 0 }, // Z
            { 1 }, // X
            { 0 }, // Y
    };
    
    private static final int[][] LINKS_TWO_NEIGHBORS_ADJACENT_ZX = new int[][] {
            { 0 }, // Z
            { 0 }, // X
            { 1 }, // Y
    };
    
    private static final int[][] COMBOS_TWO_NEIGHBORS_ADJACENT_ZERO_LINKS = new int[][] { { } };
    
    private static final int[][] COMBOS_TWO_NEIGHBORS_ADJACENT_ONE_LINK = new int[][] { { 0 } };
    
    @Test
    public void getConnectivity_twoNeighborsOpposite_returnsFalse() {
        for (int rotation = 0; rotation < 3; rotation++) {
            boolean[][][] array = rotate(BASE_TWO_NEIGHBORS_OPPOSITE, rotation);
            assertFalse(potts.getConnectivity(array, false));
        }
    }
    
    @Test
    public void getConnectivity_twoNeighborsAdjacentXYZeroLinks_returnsFalse() {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int[] combo : COMBOS_TWO_NEIGHBORS_ADJACENT_ZERO_LINKS) {
                boolean[][][] array = rotate(combine(BASE_TWO_NEIGHBORS_ADJACENT_XY,
                        combo, LINKS_TWO_NEIGHBORS_ADJACENT_XY), Axis.Z_AXIS, rotation);
                assertFalse(potts.getConnectivity(array, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_twoNeighborsAdjacentYZZeroLinks_returnsFalse() {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int[] combo : COMBOS_TWO_NEIGHBORS_ADJACENT_ZERO_LINKS) {
                boolean[][][] array = rotate(combine(BASE_TWO_NEIGHBORS_ADJACENT_YZ,
                        combo, LINKS_TWO_NEIGHBORS_ADJACENT_YZ), Axis.X_AXIS, rotation);
                assertFalse(potts.getConnectivity(array, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_twoNeighborsAdjacentZXZeroLinks_returnsFalse() {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int[] combo : COMBOS_TWO_NEIGHBORS_ADJACENT_ZERO_LINKS) {
                boolean[][][] array = rotate(combine(BASE_TWO_NEIGHBORS_ADJACENT_ZX,
                        combo, LINKS_TWO_NEIGHBORS_ADJACENT_ZX), Axis.Y_AXIS, rotation);
                assertFalse(potts.getConnectivity(array, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_twoNeighborsAdjacentXYOneLink_returnsTrue() {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int[] combo : COMBOS_TWO_NEIGHBORS_ADJACENT_ONE_LINK) {
                boolean[][][] array = rotate(combine(BASE_TWO_NEIGHBORS_ADJACENT_XY,
                        combo, LINKS_TWO_NEIGHBORS_ADJACENT_XY), Axis.Z_AXIS, rotation);
                assertTrue(potts.getConnectivity(array, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_twoNeighborsAdjacentYZOneLink_returnsTrue() {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int[] combo : COMBOS_TWO_NEIGHBORS_ADJACENT_ONE_LINK) {
                boolean[][][] array = rotate(combine(BASE_TWO_NEIGHBORS_ADJACENT_YZ,
                        combo, LINKS_TWO_NEIGHBORS_ADJACENT_YZ), Axis.X_AXIS, rotation);
                assertTrue(potts.getConnectivity(array, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_twoNeighborsAdjacentZXOneLink_returnsTrue() {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int[] combo : COMBOS_TWO_NEIGHBORS_ADJACENT_ONE_LINK) {
                boolean[][][] array = rotate(combine(BASE_TWO_NEIGHBORS_ADJACENT_ZX,
                        combo, LINKS_TWO_NEIGHBORS_ADJACENT_ZX), Axis.Y_AXIS, rotation);
                assertTrue(potts.getConnectivity(array, false));
            }
        }
    }
    
    /* -------------------------------------------------------------------------
     * CONNECTIVITY FOR THREE (3) NEIGHBORS
     *
     * The three neighbors can be either in the same plane (3 planes x 4
     * rotations = 12 options) or positioned along corners (8 options).
     *
     * For the plane neighbors, there can be up to 2 links:
     *      0 links | 1 combo   | unconnected
     *      1 link  | 2 combos  | unconnected
     *      2 links | 1 combo   | connected
     *
     * For the corner neighbors, there can be up to 3 links:
     *      0 links | 1 combo   | unconnected
     *      1 link  | 3 combos  | unconnected
     *      2 links | 3 combos  | connected
     *      3 links | 1 combo   | connected
    ------------------------------------------------------------------------- */
    
    private static final boolean[][][] BASE_THREE_NEIGHBORS_PLANE_XY = new boolean[][][] {
            {
                    { false,  true, false },
                    {  true, false, false },
                    { false,  true, false }
            },
            {
                    { false,  true, false },
                    {  true,  true, false },
                    { false,  true, false }
            },
            {
                    { false,  true, false },
                    {  true, false, false },
                    { false,  true, false }
            }
    };
    
    private static final boolean[][][] BASE_THREE_NEIGHBORS_PLANE_YZ = new boolean[][][] {
            {
                    { false,  true, false },
                    { false,  true, false },
                    { false,  true, false }
            },
            {
                    {  true, false, false },
                    {  true,  true, false },
                    {  true, false, false }
            },
            {
                    { false,  true, false },
                    { false,  true, false },
                    { false,  true, false }
            }
    };
    
    private static final boolean[][][] BASE_THREE_NEIGHBORS_PLANE_ZX = new boolean[][][] {
            {
                    { false, false, false },
                    {  true,  true,  true },
                    { false, false, false }
            },
            {
                    {  true,  true,  true },
                    { false,  true, false },
                    { false, false, false }
            },
            {
                    { false, false, false },
                    {  true,  true,  true },
                    { false, false, false }
            }
    };
    
    private static final boolean[][][] BASE_THREE_NEIGHBORS_CORNER_A = new boolean[][][] {
            {
                    { false, false, false },
                    { false,  true, false },
                    { false, false, false }
            },
            {
                    { false,  true, false },
                    {  true,  true, false },
                    { false, false, false }
            },
            {
                    { false, false, false },
                    { false, false, false },
                    { false, false, false }
            }
    };
    
    private static final boolean[][][] BASE_THREE_NEIGHBORS_CORNER_B = new boolean[][][] {
            {
                    { false, false, false },
                    { false, false, false },
                    { false, false, false }
            },
            {
                    { false,  true, false },
                    {  true,  true, false },
                    { false, false, false }
            },
            {
                    { false, false, false },
                    { false,  true, false },
                    { false, false, false }
            }
    };
    
    private static final int[][] LINKS_THREE_NEIGHBORS_PLANE_XY = new int[][] {
            { 1, 1 }, // Z
            { 0, 2 }, // X
            { 0, 0 }, // Y
    };
    
    private static final int[][] LINKS_THREE_NEIGHBORS_PLANE_YZ = new int[][] {
            { 0, 2 }, // Z
            { 1, 1 }, // X
            { 0, 0 }, // Y
    };
    
    private static final int[][] LINKS_THREE_NEIGHBORS_PLANE_ZX = new int[][] {
            { 0, 2 }, // Z
            { 0, 0 }, // X
            { 1, 1 }, // Y
    };
    
    private static final int[][] LINKS_THREE_NEIGHBORS_CORNER_A = new int[][] {
            { 0, 0, 1 }, // Z
            { 0, 1, 0 }, // X
            { 1, 0, 0 }, // Y
    };
    
    private static final int[][] LINKS_THREE_NEIGHBORS_CORNER_B = new int[][] {
            { 2, 2, 1 }, // Z
            { 0, 1, 0 }, // X
            { 1, 0, 0 }, // Y
    };
    
    private static final int[][] COMBOS_THREE_NEIGHBORS_PLANE_ZERO_LINKS = new int[][] { { } };
    
    private static final int[][] COMBOS_THREE_NEIGHBORS_PLANE_ONE_LINK = new int[][] {
            { 0 },
            { 1 },
    };
    
    private static final int[][] COMBOS_THREE_NEIGHBORS_PLANE_TWO_LINKS = new int[][] {
            { 0, 1 },
    };
    
    private static final int[][] COMBOS_THREE_NEIGHBORS_CORNER_ZERO_LINKS = new int[][] { { } };
    
    private static final int[][] COMBOS_THREE_NEIGHBORS_CORNER_ONE_LINK = new int[][] {
            { 0 },
            { 1 },
            { 2 },
    };
    
    private static final int[][] COMBOS_THREE_NEIGHBORS_CORNER_TWO_LINKS = new int[][] {
            { 0, 1 },
            { 0, 2 },
            { 1, 2 },
    };
    
    private static final int[][] COMBOS_THREE_NEIGHBORS_CORNER_THREE_LINKS = new int[][] {
            { 0, 1, 2 },
    };
    
    @Test
    public void getConnectivity_threeNeighborsPlaneXYZeroLinks_returnsFalse() {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int[] combo : COMBOS_THREE_NEIGHBORS_PLANE_ZERO_LINKS) {
                boolean[][][] array = rotate(combine(BASE_THREE_NEIGHBORS_PLANE_XY,
                        combo, LINKS_THREE_NEIGHBORS_PLANE_XY), Axis.Z_AXIS, rotation);
                assertFalse(potts.getConnectivity(array, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_threeNeighborsPlaneYZZeroLinks_returnsFalse() {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int[] combo : COMBOS_THREE_NEIGHBORS_PLANE_ZERO_LINKS) {
                boolean[][][] array = rotate(combine(BASE_THREE_NEIGHBORS_PLANE_YZ,
                        combo, LINKS_THREE_NEIGHBORS_PLANE_YZ), Axis.X_AXIS, rotation);
                assertFalse(potts.getConnectivity(array, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_threeNeighborsPlaneZXZeroLinks_returnsFalse() {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int[] combo : COMBOS_THREE_NEIGHBORS_PLANE_ZERO_LINKS) {
                boolean[][][] array = rotate(combine(BASE_THREE_NEIGHBORS_PLANE_ZX,
                        combo, LINKS_THREE_NEIGHBORS_PLANE_ZX), Axis.Y_AXIS, rotation);
                assertFalse(potts.getConnectivity(array, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_threeNeighborsPlaneXYOneLink_returnsFalse() {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int[] combo : COMBOS_THREE_NEIGHBORS_PLANE_ONE_LINK) {
                boolean[][][] array = rotate(combine(BASE_THREE_NEIGHBORS_PLANE_XY,
                        combo, LINKS_THREE_NEIGHBORS_PLANE_XY), Axis.Z_AXIS, rotation);
                assertFalse(potts.getConnectivity(array, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_threeNeighborsPlaneYZOneLink_returnsFalse() {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int[] combo : COMBOS_THREE_NEIGHBORS_PLANE_ONE_LINK) {
                boolean[][][] array = rotate(combine(BASE_THREE_NEIGHBORS_PLANE_YZ,
                        combo, LINKS_THREE_NEIGHBORS_PLANE_YZ), Axis.X_AXIS, rotation);
                assertFalse(potts.getConnectivity(array, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_threeNeighborsPlaneZXOneLink_returnsFalse() {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int[] combo : COMBOS_THREE_NEIGHBORS_PLANE_ONE_LINK) {
                boolean[][][] array = rotate(combine(BASE_THREE_NEIGHBORS_PLANE_ZX,
                        combo, LINKS_THREE_NEIGHBORS_PLANE_ZX), Axis.Y_AXIS, rotation);
                assertFalse(potts.getConnectivity(array, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_threeNeighborsPlaneXYTwoLinks_returnsTrue() {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int[] combo : COMBOS_THREE_NEIGHBORS_PLANE_TWO_LINKS) {
                boolean[][][] array = rotate(combine(BASE_THREE_NEIGHBORS_PLANE_XY,
                        combo, LINKS_THREE_NEIGHBORS_PLANE_XY), Axis.Z_AXIS, rotation);
                assertTrue(potts.getConnectivity(array, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_threeNeighborsPlaneYZTwoLinks_returnsTrue() {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int[] combo : COMBOS_THREE_NEIGHBORS_PLANE_TWO_LINKS) {
                boolean[][][] array = rotate(combine(BASE_THREE_NEIGHBORS_PLANE_YZ,
                        combo, LINKS_THREE_NEIGHBORS_PLANE_YZ), Axis.X_AXIS, rotation);
                assertTrue(potts.getConnectivity(array, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_threeNeighborsPlaneZXTwoLinks_returnsTrue() {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int[] combo : COMBOS_THREE_NEIGHBORS_PLANE_TWO_LINKS) {
                boolean[][][] array = rotate(combine(BASE_THREE_NEIGHBORS_PLANE_ZX,
                        combo, LINKS_THREE_NEIGHBORS_PLANE_ZX), Axis.Y_AXIS, rotation);
                assertTrue(potts.getConnectivity(array, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_threeNeighborsCornerZeroLinks_returnsFalse() {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int[] combo : COMBOS_THREE_NEIGHBORS_CORNER_ZERO_LINKS) {
                boolean[][][] arrayA = rotate(combine(BASE_THREE_NEIGHBORS_CORNER_A,
                        combo, LINKS_THREE_NEIGHBORS_CORNER_A), Axis.Z_AXIS, rotation);
                assertFalse(potts.getConnectivity(arrayA, false));
                
                boolean[][][] arrayB = rotate(combine(BASE_THREE_NEIGHBORS_CORNER_B,
                        combo, LINKS_THREE_NEIGHBORS_CORNER_B), Axis.Z_AXIS, rotation);
                assertFalse(potts.getConnectivity(arrayB, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_threeNeighborsCornerOneLink_returnsFalse() {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int[] combo : COMBOS_THREE_NEIGHBORS_CORNER_ONE_LINK) {
                boolean[][][] arrayA = rotate(combine(BASE_THREE_NEIGHBORS_CORNER_A,
                        combo, LINKS_THREE_NEIGHBORS_CORNER_A), Axis.Z_AXIS, rotation);
                assertFalse(potts.getConnectivity(arrayA, false));
                
                boolean[][][] arrayB = rotate(combine(BASE_THREE_NEIGHBORS_CORNER_B,
                        combo, LINKS_THREE_NEIGHBORS_CORNER_B), Axis.Z_AXIS, rotation);
                assertFalse(potts.getConnectivity(arrayB, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_threeNeighborsCornerTwoLinks_returnsTrue() {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int[] combo : COMBOS_THREE_NEIGHBORS_CORNER_TWO_LINKS) {
                boolean[][][] arrayA = rotate(combine(BASE_THREE_NEIGHBORS_CORNER_A,
                        combo, LINKS_THREE_NEIGHBORS_CORNER_A), Axis.Z_AXIS, rotation);
                assertTrue(potts.getConnectivity(arrayA, false));
                
                boolean[][][] arrayB = rotate(combine(BASE_THREE_NEIGHBORS_CORNER_B,
                        combo, LINKS_THREE_NEIGHBORS_CORNER_B), Axis.Z_AXIS, rotation);
                assertTrue(potts.getConnectivity(arrayB, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_threeNeighborsCornerThreeLinks_returnsTrue() {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int[] combo : COMBOS_THREE_NEIGHBORS_CORNER_THREE_LINKS) {
                boolean[][][] arrayA = rotate(combine(BASE_THREE_NEIGHBORS_CORNER_A,
                        combo, LINKS_THREE_NEIGHBORS_CORNER_A), Axis.Z_AXIS, rotation);
                assertTrue(potts.getConnectivity(arrayA, false));
                
                boolean[][][] arrayB = rotate(combine(BASE_THREE_NEIGHBORS_CORNER_B,
                        combo, LINKS_THREE_NEIGHBORS_CORNER_B), Axis.Z_AXIS, rotation);
                assertTrue(potts.getConnectivity(arrayB, false));
            }
        }
    }
    
    /* -------------------------------------------------------------------------
     * CONNECTIVITY FOR FOUR (4) NEIGHBORS
     *
     * The four neighbors can be either in the same plane (3 options) or
     * positioned with two along an axis and two in the plane normal (3 axis
     * x 4 rotations = 12 options).
     *
     * For the plane neighbors, there can be up to 4 links:
     *      0 links | 1 combo   | unconnected
     *      1 link  | 4 combos  | unconnected
     *      2 links | 6 combos  | unconnected
     *      3 links | 4 combos  | connected
     *      4 links | 1 combo   | connected
     *
     * For the axis neighbors, there can be up to 5 links:
     *      0 links | 1 combo   | unconnected
     *      1 link  | 5 combos  | unconnected
     *      2 links | 10 combos | unconnected
     *      3 links | 10 combos | unconnected / connected
     *      4 links | 5 combos  | connected
     *      5 links | 1 combo   | connected
    ------------------------------------------------------------------------- */
    
    private static final boolean[][][] BASE_FOUR_NEIGHBORS_PLANE_XY = new boolean[][][] {
            {
                    { false, false, false },
                    { false, false, false },
                    { false, false, false }
            },
            {
                    { false,  true, false },
                    {  true,  true,  true },
                    { false,  true, false }
            },
            {
                    { false, false, false },
                    { false, false, false },
                    { false, false, false }
            }
    };
    
    private static final boolean[][][] BASE_FOUR_NEIGHBORS_PLANE_YZ = new boolean[][][] {
            {
                    { false, false, false },
                    { false,  true, false },
                    { false, false, false }
            },
            {
                    { false, false, false },
                    {  true,  true,  true },
                    { false, false, false }
            },
            {
                    { false, false, false },
                    { false,  true, false },
                    { false, false, false }
            }
    };
    
    private static final boolean[][][] BASE_FOUR_NEIGHBORS_PLANE_ZX = new boolean[][][] {
            {
                    { false, false, false },
                    { false,  true, false },
                    { false, false, false }
            },
            {
                    { false,  true, false },
                    { false,  true, false },
                    { false,  true, false }
            },
            {
                    { false, false, false },
                    { false,  true, false },
                    { false, false, false }
            }
    };
    
    private static final boolean[][][] BASE_FOUR_NEIGHBORS_AXIS_X = new boolean[][][] {
            {
                    { false, false, false },
                    { false,  true, false },
                    { false, false, false }
            },
            {
                    { false,  true, false },
                    {  true,  true, false },
                    { false,  true, false }
            },
            {
                    { false, false, false },
                    { false, false, false },
                    { false, false, false }
            }
    };
    
    private static final boolean[][][] BASE_FOUR_NEIGHBORS_AXIS_Y = new boolean[][][] {
            {
                    { false, false, false },
                    { false,  true, false },
                    { false, false, false }
            },
            {
                    { false,  true, false },
                    {  true,  true,  true },
                    { false, false, false }
            },
            {
                    { false, false, false },
                    { false, false, false },
                    { false, false, false }
            }
    };
    
    private static final boolean[][][] BASE_FOUR_NEIGHBORS_AXIS_Z = new boolean[][][] {
            {
                    { false, false, false },
                    { false,  true, false },
                    { false, false, false }
            },
            {
                    { false,  true, false },
                    {  true,  true, false },
                    { false, false, false }
            },
            {
                    { false, false, false },
                    { false,  true, false },
                    { false, false, false }
            }
    };
    
    private static final int[][] LINKS_FOUR_NEIGHBORS_PLANE_XY = new int[][] {
            { 1, 1, 1, 1 }, // Z
            { 0, 0, 2, 2 }, // X
            { 0, 2, 0, 2 }, // Y
    };
    
    private static final int[][] LINKS_FOUR_NEIGHBORS_PLANE_YZ = new int[][] {
            { 0, 0, 2, 2 }, // Z
            { 1, 1, 1, 1 }, // X
            { 0, 2, 0, 2 }, // Y
    };
    
    private static final int[][] LINKS_FOUR_NEIGHBORS_PLANE_ZX = new int[][] {
            { 0, 0, 2, 2 }, // Z
            { 0, 2, 0, 2 }, // Y
            { 1, 1, 1, 1 }, // X
    };
    
    private static final int[][] LINKS_FOUR_NEIGHBORS_AXIS_X = new int[][] {
            { 0, 0, 1, 1, 0 }, // Z
            { 0, 2, 0, 2, 1 }, // X
            { 1, 1, 0, 0, 0 }, // Y
    };
    
    private static final int[][] LINKS_FOUR_NEIGHBORS_AXIS_Y = new int[][] {
            { 0, 0, 1, 1, 0 }, // Z
            { 1, 1, 0, 0, 0 }, // X
            { 0, 2, 0, 2, 1 }, // Y
    };
    
    private static final int[][] LINKS_FOUR_NEIGHBORS_AXIS_Z = new int[][] {
            { 0, 2, 0, 2, 1 }, // Z
            { 0, 0, 1, 1, 0 }, // X
            { 1, 1, 0, 0, 0 }, // Y
    };
    
    private static final int[][] COMBOS_FOUR_NEIGHBORS_PLANE_ZERO_LINKS = new int[][] { { } };
    
    private static final int[][] COMBOS_FOUR_NEIGHBORS_PLANE_ONE_LINK = new int[][] {
            { 0 },
            { 1 },
            { 2 },
            { 3 },
    };
    
    private static final int[][] COMBOS_FOUR_NEIGHBORS_PLANE_TWO_LINKS = new int[][] {
            { 0, 1 },
            { 0, 2 },
            { 0, 3 },
            { 1, 2 },
            { 1, 3 },
            { 2, 3 },
    };
    
    private static final int[][] COMBOS_FOUR_NEIGHBORS_PLANE_THREE_LINKS = new int[][] {
            { 0, 1, 2 },
            { 0, 1, 3 },
            { 0, 2, 3 },
            { 1, 2, 3 },
    };
    
    private static final int[][] COMBOS_FOUR_NEIGHBORS_PLANE_FOUR_LINKS = new int[][] {
            { 0, 1, 2, 3 },
    };
    
    private static final int[][] COMBOS_FOUR_NEIGHBORS_AXIS_ZERO_LINKS = new int[][] { { } };
    
    private static final int[][] COMBOS_FOUR_NEIGHBORS_AXIS_ONE_LINK = new int[][] {
            { 0 },
            { 1 },
            { 2 },
            { 3 },
            { 4 },
    };
    
    private static final int[][] COMBOS_FOUR_NEIGHBORS_AXIS_TWO_LINKS = new int[][] {
            { 0, 1 },
            { 0, 2 },
            { 0, 3 },
            { 0, 4 },
            { 1, 2 },
            { 1, 3 },
            { 1, 4 },
            { 2, 3 },
            { 2, 4 },
            { 3, 4 },
    };
    
    private static final int[][] COMBOS_FOUR_NEIGHBORS_AXIS_THREE_LINKS_VALID = new int[][] {
            { 0, 1, 2 },
            { 0, 1, 3 },
            { 2, 3, 0 },
            { 2, 3, 1 },
            { 4, 0, 1 },
            { 4, 2, 3 },
            { 4, 0, 3 },
            { 4, 1, 2 },
    };
    
    private static final int[][] COMBOS_FOUR_NEIGHBORS_AXIS_THREE_LINKS_INVALID = new int[][] {
            { 4, 0, 2 },
            { 4, 1, 3 },
    };
    
    private static final int[][] COMBOS_FOUR_NEIGHBORS_AXIS_FOUR_LINKS = new int[][] {
            { 0, 1, 2, 3 },
            { 0, 1, 2, 4 },
            { 1, 2, 3, 4 },
            { 2, 3, 0, 4 },
            { 3, 0, 1, 4 },
    };
    
    private static final int[][] COMBOS_FOUR_NEIGHBORS_AXIS_FIVE_LINKS = new int[][] {
            { 0, 1, 2, 3, 4 },
    };
    
    @Test
    public void getConnectivity_fourNeighborsPlaneZeroLinks_returnsFalse() {
        for (int[] combo : COMBOS_FOUR_NEIGHBORS_PLANE_ZERO_LINKS) {
            boolean[][][] arrayXY = combine(BASE_FOUR_NEIGHBORS_PLANE_XY,
                    combo, LINKS_FOUR_NEIGHBORS_PLANE_XY);
            assertFalse(potts.getConnectivity(arrayXY, false));
            
            boolean[][][] arrayYZ = combine(BASE_FOUR_NEIGHBORS_PLANE_YZ,
                    combo, LINKS_FOUR_NEIGHBORS_PLANE_YZ);
            assertFalse(potts.getConnectivity(arrayYZ, false));
            
            boolean[][][] arrayZX = combine(BASE_FOUR_NEIGHBORS_PLANE_ZX,
                    combo, LINKS_FOUR_NEIGHBORS_PLANE_ZX);
            assertFalse(potts.getConnectivity(arrayZX, false));
        }
    }
    
    @Test
    public void getConnectivity_fourNeighborsPlaneOneLink_returnsFalse() {
        for (int[] combo : COMBOS_FOUR_NEIGHBORS_PLANE_ONE_LINK) {
            boolean[][][] arrayXY = combine(BASE_FOUR_NEIGHBORS_PLANE_XY,
                    combo, LINKS_FOUR_NEIGHBORS_PLANE_XY);
            assertFalse(potts.getConnectivity(arrayXY, false));
            
            boolean[][][] arrayYZ = combine(BASE_FOUR_NEIGHBORS_PLANE_YZ,
                    combo, LINKS_FOUR_NEIGHBORS_PLANE_YZ);
            assertFalse(potts.getConnectivity(arrayYZ, false));
            
            boolean[][][] arrayZX = combine(BASE_FOUR_NEIGHBORS_PLANE_ZX,
                    combo, LINKS_FOUR_NEIGHBORS_PLANE_ZX);
            assertFalse(potts.getConnectivity(arrayZX, false));
        }
    }
    
    @Test
    public void getConnectivity_fourNeighborsPlaneTwoLinks_returnsFalse() {
        for (int[] combo : COMBOS_FOUR_NEIGHBORS_PLANE_TWO_LINKS) {
            boolean[][][] arrayXY = combine(BASE_FOUR_NEIGHBORS_PLANE_XY,
                    combo, LINKS_FOUR_NEIGHBORS_PLANE_XY);
            assertFalse(potts.getConnectivity(arrayXY, false));
            
            boolean[][][] arrayYZ = combine(BASE_FOUR_NEIGHBORS_PLANE_YZ,
                    combo, LINKS_FOUR_NEIGHBORS_PLANE_YZ);
            assertFalse(potts.getConnectivity(arrayYZ, false));
            
            boolean[][][] arrayZX = combine(BASE_FOUR_NEIGHBORS_PLANE_ZX,
                    combo, LINKS_FOUR_NEIGHBORS_PLANE_ZX);
            assertFalse(potts.getConnectivity(arrayZX, false));
        }
    }
    
    @Test
    public void getConnectivity_fourNeighborsPlaneThreeLinks_returnsTrue() {
        for (int[] combo : COMBOS_FOUR_NEIGHBORS_PLANE_THREE_LINKS) {
            boolean[][][] arrayXY = combine(BASE_FOUR_NEIGHBORS_PLANE_XY,
                    combo, LINKS_FOUR_NEIGHBORS_PLANE_XY);
            assertTrue(potts.getConnectivity(arrayXY, false));
            
            boolean[][][] arrayYZ = combine(BASE_FOUR_NEIGHBORS_PLANE_YZ,
                    combo, LINKS_FOUR_NEIGHBORS_PLANE_YZ);
            assertTrue(potts.getConnectivity(arrayYZ, false));
            
            boolean[][][] arrayZX = combine(BASE_FOUR_NEIGHBORS_PLANE_ZX,
                    combo, LINKS_FOUR_NEIGHBORS_PLANE_ZX);
            assertTrue(potts.getConnectivity(arrayZX, false));
        }
    }
    
    @Test
    public void getConnectivity_fourNeighborsPlaneFourLinks_returnsTrue() {
        for (int[] combo : COMBOS_FOUR_NEIGHBORS_PLANE_FOUR_LINKS) {
            boolean[][][] arrayXY = combine(BASE_FOUR_NEIGHBORS_PLANE_XY,
                    combo, LINKS_FOUR_NEIGHBORS_PLANE_XY);
            assertTrue(potts.getConnectivity(arrayXY, false));
            
            boolean[][][] arrayYZ = combine(BASE_FOUR_NEIGHBORS_PLANE_YZ,
                    combo, LINKS_FOUR_NEIGHBORS_PLANE_YZ);
            assertTrue(potts.getConnectivity(arrayYZ, false));
            
            boolean[][][] arrayZX = combine(BASE_FOUR_NEIGHBORS_PLANE_ZX,
                    combo, LINKS_FOUR_NEIGHBORS_PLANE_ZX);
            assertTrue(potts.getConnectivity(arrayZX, false));
        }
    }
    
    @Test
    public void getConnectivity_fourNeighborsAxisXZeroLinks_returnsFalse() {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int[] combo : COMBOS_FOUR_NEIGHBORS_AXIS_ZERO_LINKS) {
                boolean[][][] array = rotate(combine(BASE_FOUR_NEIGHBORS_AXIS_X,
                        combo, LINKS_FOUR_NEIGHBORS_AXIS_X), Axis.X_AXIS, rotation);
                assertFalse(potts.getConnectivity(array, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_fourNeighborsAxisYZeroLinks_returnsFalse() {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int[] combo : COMBOS_FOUR_NEIGHBORS_AXIS_ZERO_LINKS) {
                boolean[][][] array = rotate(combine(BASE_FOUR_NEIGHBORS_AXIS_Y,
                        combo, LINKS_FOUR_NEIGHBORS_AXIS_Y), Axis.Y_AXIS, rotation);
                assertFalse(potts.getConnectivity(array, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_fourNeighborsAxisZZeroLinks_returnsFalse() {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int[] combo : COMBOS_FOUR_NEIGHBORS_AXIS_ZERO_LINKS) {
                boolean[][][] array = rotate(combine(BASE_FOUR_NEIGHBORS_AXIS_Z,
                        combo, LINKS_FOUR_NEIGHBORS_AXIS_Z), Axis.Z_AXIS, rotation);
                assertFalse(potts.getConnectivity(array, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_fourNeighborsAxisXOneLink_returnsFalse() {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int[] combo : COMBOS_FOUR_NEIGHBORS_AXIS_ONE_LINK) {
                boolean[][][] array = rotate(combine(BASE_FOUR_NEIGHBORS_AXIS_X,
                        combo, LINKS_FOUR_NEIGHBORS_AXIS_X), Axis.X_AXIS, rotation);
                assertFalse(potts.getConnectivity(array, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_fourNeighborsAxisYOneLink_returnsFalse() {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int[] combo : COMBOS_FOUR_NEIGHBORS_AXIS_ONE_LINK) {
                boolean[][][] array = rotate(combine(BASE_FOUR_NEIGHBORS_AXIS_Y,
                        combo, LINKS_FOUR_NEIGHBORS_AXIS_Y), Axis.Y_AXIS, rotation);
                assertFalse(potts.getConnectivity(array, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_fourNeighborsAxisZOneLink_returnsFalse() {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int[] combo : COMBOS_FOUR_NEIGHBORS_AXIS_ONE_LINK) {
                boolean[][][] array = rotate(combine(BASE_FOUR_NEIGHBORS_AXIS_Z,
                        combo, LINKS_FOUR_NEIGHBORS_AXIS_Z), Axis.Z_AXIS, rotation);
                assertFalse(potts.getConnectivity(array, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_fourNeighborsAxisXTwoLinks_returnsFalse() {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int[] combo : COMBOS_FOUR_NEIGHBORS_AXIS_TWO_LINKS) {
                boolean[][][] array = rotate(combine(BASE_FOUR_NEIGHBORS_AXIS_X,
                        combo, LINKS_FOUR_NEIGHBORS_AXIS_X), Axis.X_AXIS, rotation);
                assertFalse(potts.getConnectivity(array, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_fourNeighborsAxisYTwoLinks_returnsFalse() {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int[] combo : COMBOS_FOUR_NEIGHBORS_AXIS_TWO_LINKS) {
                boolean[][][] array = rotate(combine(BASE_FOUR_NEIGHBORS_AXIS_Y,
                        combo, LINKS_FOUR_NEIGHBORS_AXIS_Y), Axis.Y_AXIS, rotation);
                assertFalse(potts.getConnectivity(array, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_fourNeighborsAxisZTwoLinks_returnsFalse() {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int[] combo : COMBOS_FOUR_NEIGHBORS_AXIS_TWO_LINKS) {
                boolean[][][] array = rotate(combine(BASE_FOUR_NEIGHBORS_AXIS_Z,
                        combo, LINKS_FOUR_NEIGHBORS_AXIS_Z), Axis.Z_AXIS, rotation);
                assertFalse(potts.getConnectivity(array, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_fourNeighborsAxisXThreeLinksValid_returnsTrue() {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int[] combo : COMBOS_FOUR_NEIGHBORS_AXIS_THREE_LINKS_VALID) {
                boolean[][][] array = rotate(combine(BASE_FOUR_NEIGHBORS_AXIS_X,
                        combo, LINKS_FOUR_NEIGHBORS_AXIS_X), Axis.X_AXIS, rotation);
                assertTrue(potts.getConnectivity(array, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_fourNeighborsAxisYThreeLinksValid_returnsTrue() {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int[] combo : COMBOS_FOUR_NEIGHBORS_AXIS_THREE_LINKS_VALID) {
                boolean[][][] array = rotate(combine(BASE_FOUR_NEIGHBORS_AXIS_Y,
                        combo, LINKS_FOUR_NEIGHBORS_AXIS_Y), Axis.Y_AXIS, rotation);
                assertTrue(potts.getConnectivity(array, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_fourNeighborsAxisZThreeLinksValid_returnsTrue() {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int[] combo : COMBOS_FOUR_NEIGHBORS_AXIS_THREE_LINKS_VALID) {
                boolean[][][] array = rotate(combine(BASE_FOUR_NEIGHBORS_AXIS_Z,
                        combo, LINKS_FOUR_NEIGHBORS_AXIS_Z), Axis.Z_AXIS, rotation);
                assertTrue(potts.getConnectivity(array, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_fourNeighborsAxisXThreeLinksInvalid_returnsFalse() {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int[] combo : COMBOS_FOUR_NEIGHBORS_AXIS_THREE_LINKS_INVALID) {
                boolean[][][] array = rotate(combine(BASE_FOUR_NEIGHBORS_AXIS_X,
                        combo, LINKS_FOUR_NEIGHBORS_AXIS_X), Axis.X_AXIS, rotation);
                assertFalse(potts.getConnectivity(array, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_fourNeighborsAxisYThreeLinksInvalid_returnsFalse() {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int[] combo : COMBOS_FOUR_NEIGHBORS_AXIS_THREE_LINKS_INVALID) {
                boolean[][][] array = rotate(combine(BASE_FOUR_NEIGHBORS_AXIS_Y,
                        combo, LINKS_FOUR_NEIGHBORS_AXIS_Y), Axis.Y_AXIS, rotation);
                assertFalse(potts.getConnectivity(array, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_fourNeighborsAxisZThreeLinksInvalid_returnsFalse() {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int[] combo : COMBOS_FOUR_NEIGHBORS_AXIS_THREE_LINKS_INVALID) {
                boolean[][][] array = rotate(combine(BASE_FOUR_NEIGHBORS_AXIS_Z,
                        combo, LINKS_FOUR_NEIGHBORS_AXIS_Z), Axis.Z_AXIS, rotation);
                assertFalse(potts.getConnectivity(array, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_fourNeighborsAxisXFourLinks_returnsTrue() {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int[] combo : COMBOS_FOUR_NEIGHBORS_AXIS_FOUR_LINKS) {
                boolean[][][] array = rotate(combine(BASE_FOUR_NEIGHBORS_AXIS_X,
                        combo, LINKS_FOUR_NEIGHBORS_AXIS_X), Axis.X_AXIS, rotation);
                assertTrue(potts.getConnectivity(array, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_fourNeighborsAxisYFourLinks_returnsTrue() {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int[] combo : COMBOS_FOUR_NEIGHBORS_AXIS_FOUR_LINKS) {
                boolean[][][] array = rotate(combine(BASE_FOUR_NEIGHBORS_AXIS_Y,
                        combo, LINKS_FOUR_NEIGHBORS_AXIS_Y), Axis.Y_AXIS, rotation);
                assertTrue(potts.getConnectivity(array, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_fourNeighborsAxisZFourLinks_returnsTrue() {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int[] combo : COMBOS_FOUR_NEIGHBORS_AXIS_FOUR_LINKS) {
                boolean[][][] array = rotate(combine(BASE_FOUR_NEIGHBORS_AXIS_Z,
                        combo, LINKS_FOUR_NEIGHBORS_AXIS_Z), Axis.Z_AXIS, rotation);
                assertTrue(potts.getConnectivity(array, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_fourNeighborsAxisXFiveLinks_returnsTrue() {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int[] combo : COMBOS_FOUR_NEIGHBORS_AXIS_FIVE_LINKS) {
                boolean[][][] array = rotate(combine(BASE_FOUR_NEIGHBORS_AXIS_X,
                        combo, LINKS_FOUR_NEIGHBORS_AXIS_X), Axis.X_AXIS, rotation);
                assertTrue(potts.getConnectivity(array, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_fourNeighborsAxisYFiveLinks_returnsTrue() {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int[] combo : COMBOS_FOUR_NEIGHBORS_AXIS_FIVE_LINKS) {
                boolean[][][] array = rotate(combine(BASE_FOUR_NEIGHBORS_AXIS_Y,
                        combo, LINKS_FOUR_NEIGHBORS_AXIS_Y), Axis.Y_AXIS, rotation);
                assertTrue(potts.getConnectivity(array, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_fourNeighborsAxisZFiveLinks_returnsTrue() {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int[] combo : COMBOS_FOUR_NEIGHBORS_AXIS_FIVE_LINKS) {
                boolean[][][] array = rotate(combine(BASE_FOUR_NEIGHBORS_AXIS_Z,
                        combo, LINKS_FOUR_NEIGHBORS_AXIS_Z), Axis.Z_AXIS, rotation);
                assertTrue(potts.getConnectivity(array, false));
            }
        }
    }
    
    /* -------------------------------------------------------------------------
     * CONNECTIVITY FOR FIVE (5) NEIGHBORS
     *
     * The five neighbors are positioned such that only one face is missing a
     * neighbor (6 options).
     *
     * There can be up to 8 links:
     *       0 links | 1 combo   | unconnected
     *       1 link  | 8 combos  | unconnected
     *       2 links | 28 combos | unconnected
     *       3 links | 56 combos | unconnected
     *       4 links | 70 combos | unconnected / connected
     *       5 links | 56 combos | unconnected / connected
     *       6 links | 28 combos | connected
     *       7 links | 8 combos  | connected
     *       8 links | 1 combo   | connected
    ------------------------------------------------------------------------- */
    
    private static final boolean[][][] BASE_FIVE_NEIGHBORS = new boolean[][][] {
            {
                    { false, false, false },
                    { false,  true, false },
                    { false, false, false }
            },
            {
                    { false,  true, false },
                    {  true,  true,  true },
                    { false,  true, false }
            },
            {
                    { false, false, false },
                    { false, false, false },
                    { false, false, false }
            }
    };
    
    private static final int[][] LINKS_FIVE_NEIGHBORS = new int[][] {
            { 0, 0, 0, 0, 1, 1, 1, 1 }, // Z
            { 0, 1, 1, 2, 0, 0, 2, 2 }, // X
            { 1, 0, 2, 1, 0, 2, 0, 2 }, // Y
    };
    
    private static final int[][] COMBOS_FIVE_NEIGHBORS_ZERO_LINKS = new int[][] { { } };
    
    private static final int[][] COMBOS_FIVE_NEIGHBORS_ONE_LINK = new int[8][1];
    
    private static final int[][] COMBOS_FIVE_NEIGHBORS_TWO_LINKS = new int[28][2];
    
    private static final int[][] COMBOS_FIVE_NEIGHBORS_THREE_LINKS = new int[56][3];
    
    private static final int[][] COMBOS_FIVE_NEIGHBORS_FOUR_LINKS_VALID = new int[][] {
            // 4 plane
            { 0, 1, 2, 3 },
            
            // 2 plane, 2 corner
            { 0, 3, 4, 7 },
            { 0, 3, 5, 6 },
            { 0, 3, 4, 5 },
            { 0, 3, 6, 7 },
            { 1, 2, 4, 7 },
            { 1, 2, 5, 6 },
            { 1, 2, 4, 6 },
            { 1, 2, 5, 7 },
    };
    
    private static final int[][] COMBOS_FIVE_NEIGHBORS_FOUR_LINKS_INVALID = new int[][] {
            // 4 corner
            { 4, 5, 6, 7 },
            
            // 2 plane, 2 corner
            { 0, 3, 4, 6 },
            { 0, 3, 5, 7 },
            { 1, 2, 4, 5 },
            { 1, 2, 6, 7 },
    };
    
    private static final int[][] COMBOS_FIVE_NEIGHBORS_FOUR_LINKS_VALID_SYMMETRY = new int[][] {
            // 3 corners, 1 plane
            { 4, 5, 6, 0 },
            { 4, 5, 6, 1 },
            { 4, 5, 6, 2 },
            { 4, 5, 6, 3 },
            
            // 3 plane, 1 corner
            { 0, 1, 2, 6 },
            { 0, 1, 2, 7 },
            
            // 2 plane, 2 corner
            { 0, 1, 5, 7 },
            { 0, 1, 5, 6 },
            { 0, 1, 6, 7 },
    };
    
    private static final int[][] COMBOS_FIVE_NEIGHBORS_FOUR_LINKS_INVALID_SYMMETRY = new int[][] {
            // 3 plane, 1 corner
            { 0, 1, 2, 4 },
            { 0, 1, 2, 5 },
            
            // 2 plane, 2 corner
            { 0, 1, 4, 5 },
            { 0, 1, 4, 6 },
            { 0, 1, 4, 7 },
    };
    
    private static final int[][] COMBOS_FIVE_NEIGHBORS_FIVE_LINKS_VALID_SYMMETRY = new int[][] {
            // 4 plane, 1 corner
            { 0, 1, 2, 3, 4 },
            
            // 3 plane, 2 corner
            { 0, 1, 2, 4, 6 },
            { 0, 1, 2, 4, 7 },
            { 0, 1, 2, 5, 6 },
            { 0, 1, 2, 5, 7 },
            { 0, 1, 2, 6, 7 },
            
            // 2 plane, 3 corner
            { 4, 5, 6, 0, 1 },
            { 4, 5, 6, 0, 2 },
            { 4, 5, 6, 0, 3 },
            { 4, 5, 6, 1, 2 },
            { 4, 5, 6, 1, 3 },
            { 4, 5, 6, 2, 3 },
            
            // 1 plane, 4 corner
            { 0, 4, 5, 6, 7 },
    };
    
    private static final int[][] COMBOS_FIVE_NEIGHBORS_FIVE_LINKS_INVALID_SYMMETRY = new int[][] {
            // 3 plane, 2 corner
            { 0, 1, 2, 4, 5 },
    };
    
    private static final int[][] COMBOS_FIVE_NEIGHBORS_SIX_LINKS = new int[28][6];
    
    private static final int[][] COMBOS_FIVE_NEIGHBORS_SEVEN_LINKS = new int[8][6];
    
    private static final int[][] COMBOS_FIVE_NEIGHBORS_EIGHT_LINKS = new int[1][6];
    
    @BeforeClass
    public static void createFiveNeighborCombos() {
        populate(COMBOS_FIVE_NEIGHBORS_ONE_LINK, 8, 1);
        populate(COMBOS_FIVE_NEIGHBORS_TWO_LINKS, 8, 2);
        populate(COMBOS_FIVE_NEIGHBORS_THREE_LINKS, 8, 3);
        populate(COMBOS_FIVE_NEIGHBORS_SIX_LINKS, 8, 6);
        populate(COMBOS_FIVE_NEIGHBORS_SEVEN_LINKS, 8, 7);
        populate(COMBOS_FIVE_NEIGHBORS_EIGHT_LINKS, 8, 8);
    }
    
    @Test
    public void getConnectivity_fiveNeighborsZeroLinks_returnsFalse() {
        for (int rotation = 0; rotation < 6; rotation++) {
            for (int[] combo : COMBOS_FIVE_NEIGHBORS_ZERO_LINKS) {
                boolean[][][] array = rotate(combine(BASE_FIVE_NEIGHBORS,
                        combo, LINKS_FIVE_NEIGHBORS), rotation);
                assertFalse(potts.getConnectivity(array, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_fiveNeighborsOneLink_returnsFalse() {
        for (int rotation = 0; rotation < 6; rotation++) {
            for (int[] combo : COMBOS_FIVE_NEIGHBORS_ONE_LINK) {
                boolean[][][] array = rotate(combine(BASE_FIVE_NEIGHBORS,
                        combo, LINKS_FIVE_NEIGHBORS), rotation);
                assertFalse(potts.getConnectivity(array, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_fiveNeighborsTwoLinks_returnsFalse() {
        for (int rotation = 0; rotation < 6; rotation++) {
            for (int[] combo : COMBOS_FIVE_NEIGHBORS_TWO_LINKS) {
                boolean[][][] array = rotate(combine(BASE_FIVE_NEIGHBORS,
                        combo, LINKS_FIVE_NEIGHBORS), rotation);
                assertFalse(potts.getConnectivity(array, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_fiveNeighborsThreeLinks_returnsFalse() {
        for (int rotation = 0; rotation < 6; rotation++) {
            for (int[] combo : COMBOS_FIVE_NEIGHBORS_THREE_LINKS) {
                boolean[][][] array = rotate(combine(BASE_FIVE_NEIGHBORS,
                        combo, LINKS_FIVE_NEIGHBORS), rotation);
                assertFalse(potts.getConnectivity(array, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_fiveNeighborsFourLinksValid_returnsTrue() {
        for (int rotation = 0; rotation < 6; rotation++) {
            for (int[] combo : COMBOS_FIVE_NEIGHBORS_FOUR_LINKS_VALID) {
                boolean[][][] array = rotate(combine(BASE_FIVE_NEIGHBORS,
                        combo, LINKS_FIVE_NEIGHBORS), rotation);
                assertTrue(potts.getConnectivity(array, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_fiveNeighborsFourLinksInvalid_returnsFalse() {
        for (int rotation = 0; rotation < 6; rotation++) {
            for (int[] combo : COMBOS_FIVE_NEIGHBORS_FOUR_LINKS_INVALID) {
                boolean[][][] array = rotate(combine(BASE_FIVE_NEIGHBORS,
                        combo, LINKS_FIVE_NEIGHBORS), rotation);
                assertFalse(potts.getConnectivity(array, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_fiveNeighborsFourLinksValidSymmetry_returnsTrue() {
        for (int rotation = 0; rotation < 6; rotation++) {
            for (int symmetry = 0; symmetry < 4; symmetry++) {
                for (int[] combo : COMBOS_FIVE_NEIGHBORS_FOUR_LINKS_VALID_SYMMETRY) {
                    boolean[][][] array = rotate(rotate(combine(BASE_FIVE_NEIGHBORS,
                            combo, LINKS_FIVE_NEIGHBORS), Axis.Z_AXIS, symmetry), rotation);
                    assertTrue(potts.getConnectivity(array, false));
                }
            }
        }
    }
    
    @Test
    public void getConnectivity_fiveNeighborsFourLinksInvalidSymmetry_returnsFalse() {
        for (int rotation = 0; rotation < 6; rotation++) {
            for (int symmetry = 0; symmetry < 4; symmetry++) {
                for (int[] combo : COMBOS_FIVE_NEIGHBORS_FOUR_LINKS_INVALID_SYMMETRY) {
                    boolean[][][] array = rotate(rotate(combine(BASE_FIVE_NEIGHBORS,
                            combo, LINKS_FIVE_NEIGHBORS), Axis.Z_AXIS, symmetry), rotation);
                    assertFalse(potts.getConnectivity(array, false));
                }
            }
        }
    }
    
    @Test
    public void getConnectivity_fiveNeighborsFiveLinksValidSymmetry_returnsTrue() {
        for (int rotation = 0; rotation < 6; rotation++) {
            for (int symmetry = 0; symmetry < 4; symmetry++) {
                for (int[] combo : COMBOS_FIVE_NEIGHBORS_FIVE_LINKS_VALID_SYMMETRY) {
                    boolean[][][] array = rotate(rotate(combine(BASE_FIVE_NEIGHBORS,
                            combo, LINKS_FIVE_NEIGHBORS), Axis.Z_AXIS, symmetry), rotation);
                    assertTrue(potts.getConnectivity(array, false));
                }
            }
        }
    }
    
    @Test
    public void getConnectivity_fiveNeighborsFiveLinksInvalidSymmetry_returnsFalse() {
        for (int rotation = 0; rotation < 6; rotation++) {
            for (int symmetry = 0; symmetry < 4; symmetry++) {
                for (int[] combo : COMBOS_FIVE_NEIGHBORS_FIVE_LINKS_INVALID_SYMMETRY) {
                    boolean[][][] array = rotate(rotate(combine(BASE_FIVE_NEIGHBORS,
                            combo, LINKS_FIVE_NEIGHBORS), Axis.Z_AXIS, symmetry), rotation);
                    assertFalse(potts.getConnectivity(array, false));
                }
            }
        }
    }
    
    @Test
    public void getConnectivity_fiveNeighborsSixLinks_returnsTrue() {
        for (int rotation = 0; rotation < 6; rotation++) {
            for (int[] combo : COMBOS_FIVE_NEIGHBORS_SIX_LINKS) {
                boolean[][][] array = rotate(combine(BASE_FIVE_NEIGHBORS,
                        combo, LINKS_FIVE_NEIGHBORS), rotation);
                assertTrue(potts.getConnectivity(array, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_fiveNeighborsSevenLinks_returnsTrue() {
        for (int rotation = 0; rotation < 6; rotation++) {
            for (int[] combo : COMBOS_FIVE_NEIGHBORS_SEVEN_LINKS) {
                boolean[][][] array = rotate(combine(BASE_FIVE_NEIGHBORS,
                        combo, LINKS_FIVE_NEIGHBORS), rotation);
                assertTrue(potts.getConnectivity(array, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_fiveNeighborsEightLinks_returnsTrue() {
        for (int rotation = 0; rotation < 6; rotation++) {
            for (int[] combo : COMBOS_FIVE_NEIGHBORS_EIGHT_LINKS) {
                boolean[][][] array = rotate(combine(BASE_FIVE_NEIGHBORS,
                        combo, LINKS_FIVE_NEIGHBORS), rotation);
                assertTrue(potts.getConnectivity(array, false));
            }
        }
    }
    
    /* -------------------------------------------------------------------------
     * CONNECTIVITY FOR SIX (6) NEIGHBORS
     *
     * All six possible neighbor positions are occupied. The connectivity
     * depends on the ID of the voxel. Only ID = 0 is considered connected;
     * all other ID values are unconnected.
    ------------------------------------------------------------------------- */
    
    @Test
    public void getConnectivity_sixNeighborsNonZeroID_returnsFalse() {
        assertFalse(potts.getConnectivity(new boolean[][][] {
                {
                        { false, false, false },
                        { false,  true, false },
                        { false, false, false }
                },
                {
                        { false,  true, false },
                        {  true,  true,  true },
                        { false,  true, false }
                },
                {
                        { false, false, false },
                        { false,  true, false },
                        { false, false, false }
                }
        }, false));
    }
    
    @Test
    public void getConnectivity_sixNeighborsZeroID_returnsTrue() {
        assertTrue(potts.getConnectivity(new boolean[][][] {
                {
                        { false, false, false },
                        { false,  true, false },
                        { false, false, false }
                },
                {
                        { false,  true, false },
                        {  true,  true,  true },
                        { false,  true, false }
                },
                {
                        { false, false, false },
                        { false,  true, false },
                        { false, false, false }
                }
        }, true));
    }
}
