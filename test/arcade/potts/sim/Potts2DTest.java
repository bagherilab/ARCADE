package arcade.potts.sim;

import java.util.HashSet;
import org.junit.BeforeClass;
import org.junit.Test;
import arcade.potts.sim.hamiltonian.AdhesionHamiltonian2D;
import arcade.potts.sim.hamiltonian.Hamiltonian;
import arcade.potts.sim.hamiltonian.SurfaceHamiltonian2D;
import arcade.potts.sim.hamiltonian.VolumeHamiltonian;
import static org.junit.Assert.*;
import static arcade.core.util.Enums.Region;
import static arcade.potts.sim.PottsTest.*;
import static arcade.potts.util.PottsEnums.Term;

public class Potts2DTest {
    static Potts2D potts;
    private static final double[][] AREA_MCS = new double[][] {
            {    1, 0.6842640 }, {    9, 1.2501410 }, {   25, 1.8321450 },
            {   45, 2.3367460 }, {   69, 2.8135120 }, {  109, 3.4554160 },
            {  145, 3.9405750 }, {  193, 4.5044520 }, {  249, 5.0824390 },
            {  305, 5.6006040 }, {  373, 6.1715790 }, {  437, 6.6647040 },
            {  517, 7.2345420 }, {  609, 7.8396430 }, {  697, 8.3784780 },
            {  793, 8.9301870 }, {  889, 9.4506170 }, { 1005, 10.044628 },
            { 1125, 10.625359 }, { 1245, 11.176999 }, { 1369, 11.720781 },
            { 1513, 12.323346 }, { 1649, 12.867588 }, { 1789, 13.405737 },
            { 1941, 13.967562 }, { 2109, 14.564444 }, { 2285, 15.165712 },
            { 2449, 15.706243 }, { 2617, 16.242202 }, { 2809, 16.834856 },
    };
    
    @BeforeClass
    public static void setupGrid() {
        PottsSeries series = makeSeries();
        potts = new Potts2D(series);
        
        potts.ids = new int[][][] {
                {
                        { 0, 0, 0, 0, 0, 0 },
                        { 0, 1, 1, 3, 3, 0 },
                        { 0, 1, 1, 3, 3, 0 },
                        { 0, 2, 2, 0, 0, 0 },
                        { 0, 0, 0, 0, 0, 0 },
                }
        };
        
        int d = Region.DEFAULT.ordinal();
        int n = Region.NUCLEUS.ordinal();
        
        potts.regions = new int[][][] {
                {
                        { 0, 0, 0, 0, 0, 0 },
                        { 0, d, d, d, 0, 0 },
                        { 0, 0, n, n, 0, 0 },
                        { 0, d, d, 0, 0, 0 },
                        { 0, 0, 0, 0, 0, 0 },
                }
        };
    }
    
    private static boolean[][][] duplicate(boolean[][][] array) {
        boolean[][][] duplicated = new boolean[1][3][3];
        for (int i = 0; i < 3; i++) {
            System.arraycopy(array[0][i], 0, duplicated[0][i], 0, 3);
        }
        return duplicated;
    }
    
    private static boolean[][][] combine(boolean[][][] base, int[] combo, int[][] links) {
        boolean[][][] array = duplicate(base);
        for (int i : combo) {
            array[0][links[0][i]][links[1][i]] = true;
        }
        return array;
    }
    
    private static boolean[][][] rotate(boolean[][][] array, int rotations) {
        boolean[][][] rotated = duplicate(array);
        
        for (int rotation = 0; rotation < rotations; rotation++) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    rotated[0][i][j] = array[0][j][2 - i];
                }
            }
            
            array = duplicate(rotated);
        }
        
        return rotated;
    }
    
    @Test
    public void getHamiltonian_validTerm_instantiatesObject() {
        PottsSeries series = makeSeries(1, 1, 1, 1, 1);
        Potts2D potts2D = new Potts2D(series);
        
        Hamiltonian h;
        
        h = potts2D.getHamiltonian(Term.ADHESION, series);
        assertTrue(h instanceof AdhesionHamiltonian2D);
        
        h = potts2D.getHamiltonian(Term.VOLUME, series);
        assertTrue(h instanceof VolumeHamiltonian);
        
        h = potts2D.getHamiltonian(Term.SURFACE, series);
        assertTrue(h instanceof SurfaceHamiltonian2D);
    
        h = potts2D.getHamiltonian(Term.SUBSTRATE, series);
        assertNull(h);
    }
    
    @Test
    public void getHamiltonian_invalidTerm_returnsNull() {
        PottsSeries series = makeSeries(1, 1, 1, 1, 1);
        Potts2D potts2D = new Potts2D(series);
        
        Hamiltonian h;
        
        h = potts2D.getHamiltonian(Term.SUBSTRATE, series);
        assertNull(h);
    
        h = potts2D.getHamiltonian(Term.UNDEFINED, series);
        assertNull(h);
    }
    
    @Test
    public void getRatio_givenArea_calculatesValue() {
        PottsSeries series = makeSeries(1, 1, 1, 1, 1);
        Potts2D potts2D = new Potts2D(series);
        double epsilon = 1E-5;
        for (double[] mcs : AREA_MCS) {
            double dsdt = potts2D.getRatio(mcs[0], 0);
            assertEquals(mcs[1], dsdt, epsilon);
        }
    }
    
    @Test
    public void getNeighborhood_givenID_createsArray() {
        boolean[][][] array1 = potts.getNeighborhood(1, 2, 2, 0);
        assertArrayEquals(new boolean[] {  true,  true, false }, array1[0][0]);
        assertArrayEquals(new boolean[] {  true,  true, false }, array1[0][1]);
        assertArrayEquals(new boolean[] { false, false, false }, array1[0][2]);
        
        boolean[][][] array2 = potts.getNeighborhood(2, 2, 2, 0);
        assertArrayEquals(new boolean[] { false, false, false }, array2[0][0]);
        assertArrayEquals(new boolean[] { false, false, false }, array2[0][1]);
        assertArrayEquals(new boolean[] {  true,  true, false }, array2[0][2]);
        
        boolean[][][] array3 = potts.getNeighborhood(3, 2, 2, 0);
        assertArrayEquals(new boolean[] { false, false,  true }, array3[0][0]);
        assertArrayEquals(new boolean[] { false, false,  true }, array3[0][1]);
        assertArrayEquals(new boolean[] { false, false, false }, array3[0][2]);
    }
    
    @Test
    public void getNeighborhood_givenRegion_createsArray() {
        boolean[][][] array1 = potts.getNeighborhood(1, Region.DEFAULT.ordinal(), 2, 2, 0);
        assertArrayEquals(new boolean[] {  true,  true, false }, array1[0][0]);
        assertArrayEquals(new boolean[] { false, false, false }, array1[0][1]);
        assertArrayEquals(new boolean[] { false, false, false }, array1[0][2]);
        
        boolean[][][] array2 = potts.getNeighborhood(1, Region.NUCLEUS.ordinal(), 2, 2, 0);
        assertArrayEquals(new boolean[] { false, false, false }, array2[0][0]);
        assertArrayEquals(new boolean[] { false,  true, false }, array2[0][1]);
        assertArrayEquals(new boolean[] { false, false, false }, array2[0][2]);
    }
    
    private HashSet<Integer> checkUniqueID(Potts2D potts2D, int[][] ids) {
        potts2D.ids = new int[][][] { ids };
        return potts2D.getUniqueIDs(1, 1, 0);
    }
    
    @Test
    public void getUniqueIDs_validVoxel_returnsList() {
        PottsSeries series = makeSeries();
        Potts2D pottsMock = new Potts2D(series);
        HashSet<Integer> unique = new HashSet<>();
        
        unique.add(1);
        assertEquals(unique, checkUniqueID(pottsMock, new int[][] {
                { 0, 1, 0 },
                { 0, 0, 0 },
                { 0, 0, 0 } }));
        
        unique.clear();
        unique.add(0);
        assertEquals(unique, checkUniqueID(pottsMock, new int[][] {
                { 0, 1, 0 },
                { 0, 1, 0 },
                { 0, 0, 0 } }));
        
        unique.clear();
        assertEquals(unique, checkUniqueID(pottsMock, new int[][] {
                { 1, 0, 1 },
                { 0, 0, 0 },
                { 1, 0, 1 } }));
    }
    
    private HashSet<Integer> checkUniqueRegion(Potts2D potts2D, int[][] ids, int[][] regions) {
        potts2D.ids = new int[][][] { ids };
        potts2D.regions = new int[][][] { regions };
        return potts2D.getUniqueRegions(1, 1, 0);
    }
    
    @Test
    public void getUniqueRegions_validVoxel_returnsList() {
        PottsSeries series = makeSeries();
        Potts2D pottsMock = new Potts2D(series);
        HashSet<Integer> unique = new HashSet<>();
        
        assertEquals(unique, checkUniqueRegion(pottsMock,
                new int[][] {
                        { 0, 0, 0 },
                        { 0, 1, 0 },
                        { 0, 0, 0 } },
                new int[][] {
                        { 0,  0, 0 },
                        { 0, -1, 0 },
                        { 0,  0, 0 } }));
        
        assertEquals(unique, checkUniqueRegion(pottsMock,
                new int[][] {
                        { 1, 1, 1 },
                        { 1, 1, 1 },
                        { 1, 1, 1 } },
                new int[][] {
                        { -2, -1, -2 },
                        { -1, -1, -1 },
                        { -2, -1, -2 } }));
        
        unique.add(-2);
        assertEquals(unique, checkUniqueRegion(pottsMock,
                new int[][] {
                        { 0, 1, 0 },
                        { 1, 1, 2 },
                        { 0, 2, 0 } },
                new int[][] {
                        {  0, -1,  0 },
                        { -2, -1, -4 },
                        {  0, -3,  0 } }));
    }
    
    /* -------------------------------------------------------------------------
     * CONNECTIVITY FOR ZERO (0) NEIGHBORS
     *
     * If there are zero neighbors, then the voxel is never connected.
    ------------------------------------------------------------------------- */
    
    @Test
    public void getConnectivity_zeroNeighbors_returnsFalse() {
        assertFalse(potts.getConnectivity(new boolean[][][] {{
                { false, false, false },
                { false,  true, false },
                { false, false, false } }}, false));
    }
    
    /* -------------------------------------------------------------------------
     * CONNECTIVITY FOR ONE (1) NEIGHBOR
     *
     * The neighbor can be located on each sides of the square (4 options).
     *
     * If there is only one neighbor, the voxel is always connected.
    ------------------------------------------------------------------------- */
    
    private static final boolean[][][] BASE_ONE_NEIGHBOR = new boolean[][][] {{
            { false,  true, false },
            { false,  true, false },
            { false, false, false }
    }};
    
    @Test
    public void getConnectivity_oneNeighbor_returnsTrue() {
        for (int rotation = 0; rotation < 4; rotation++) {
            boolean[][][] array = rotate(BASE_ONE_NEIGHBOR, rotation);
            assertTrue(potts.getConnectivity(array, false));
        }
    }
    
    /* -------------------------------------------------------------------------
     * CONNECTIVITY FOR TWO (2) NEIGHBORS
     *
     * The two neighbors can be either adjacent (4 options) or opposite (2 options).
     *
     * If there are two opposite neighbors, the voxel is never connected.
     *
     * If there are two adjacent neighbors, the voxel is connected if there is
     * a link in the shared corner.
    ------------------------------------------------------------------------- */
    
    private static final boolean[][][] BASE_TWO_NEIGHBORS_OPPOSITE = new boolean[][][] {{
            { false,  true, false },
            { false,  true, false },
            { false,  true, false }
    }};
    
    private static final boolean[][][] BASE_TWO_NEIGHBORS_ADJACENT = new boolean[][][] {{
            { false,  true, false },
            {  true,  true, false },
            { false, false, false }
    }};
    
    private static final int[][] LINKS_TWO_NEIGHBORS_ADJACENT = new int[][] {
            { 0 }, // X
            { 0 }, // Y
    };
    
    private static final int[][] COMBOS_TWO_NEIGHBORS_ADJACENT_ZERO_LINKS = new int[][] { {} };
    
    private static final int[][] COMBOS_TWO_NEIGHBORS_ADJACENT_ONE_LINK = new int[][] { { 0 } };
    
    @Test
    public void getConnectivity_twoNeighborsOpposite_returnsFalse() {
        for (int rotation = 0; rotation < 2; rotation++) {
            boolean[][][] array = rotate(BASE_TWO_NEIGHBORS_OPPOSITE, rotation);
            assertFalse(potts.getConnectivity(array, false));
        }
    }
    
    @Test
    public void getConnectivity_twoNeighborsAdjacentZeroLink_returnsFalse() {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int[] combo : COMBOS_TWO_NEIGHBORS_ADJACENT_ZERO_LINKS) {
                boolean[][][] array = rotate(combine(BASE_TWO_NEIGHBORS_ADJACENT,
                        combo, LINKS_TWO_NEIGHBORS_ADJACENT), rotation);
                assertFalse(potts.getConnectivity(array, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_twoNeighborsAdjacentOneLink_returnsTrue() {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int[] combo : COMBOS_TWO_NEIGHBORS_ADJACENT_ONE_LINK) {
                boolean[][][] array = rotate(combine(BASE_TWO_NEIGHBORS_ADJACENT,
                        combo, LINKS_TWO_NEIGHBORS_ADJACENT), rotation);
                assertTrue(potts.getConnectivity(array, false));
            }
        }
    }
    
    /* -------------------------------------------------------------------------
     * CONNECTIVITY FOR THREE (3) NEIGHBORS
     *
     * The three neighbors are positioned such that only one side is missing a
     * neighbor (4 options).
     *
     * There can be up to 2 links:
     *       0 links | 1 combo   | unconnected
     *       1 link  | 2 combos  | unconnected
     *       2 links | 1 combo   | connected
    ------------------------------------------------------------------------- */
    
    private static final boolean[][][] BASE_THREE_NEIGHBORS = new boolean[][][] {{
            { false,  true, false },
            {  true,  true, false },
            { false,  true, false }
    }};
    
    private static final int[][] LINKS_THREE_NEIGHBORS = new int[][] {
            { 0, 2 }, // X
            { 0, 0 }, // Y
    };
    
    private static final int[][] COMBOS_THREE_NEIGHBORS_ZERO_LINKS = new int[][] { {} };
    
    private static final int[][] COMBOS_THREE_NEIGHBORS_ONE_LINK = new int[][] {
            { 0 },
            { 1 },
    };
    
    private static final int[][] COMBOS_THREE_NEIGHBORS_TWO_LINKS = new int[][] {
            { 0, 1 },
    };
    
    @Test
    public void getConnectivity_threeNeighborsZeroLinks_returnsFalse() {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int[] combo : COMBOS_THREE_NEIGHBORS_ZERO_LINKS) {
                boolean[][][] array = rotate(combine(BASE_THREE_NEIGHBORS,
                        combo, LINKS_THREE_NEIGHBORS), rotation);
                assertFalse(potts.getConnectivity(array, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_threeNeighborsOneLink_returnsFalse() {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int[] combo : COMBOS_THREE_NEIGHBORS_ONE_LINK) {
                boolean[][][] array = rotate(combine(BASE_THREE_NEIGHBORS,
                        combo, LINKS_THREE_NEIGHBORS), rotation);
                assertFalse(potts.getConnectivity(array, false));
            }
        }
    }
    
    @Test
    public void getConnectivity_threeNeighborsPlaneXYTwoLinks_returnsTrue() {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int[] combo : COMBOS_THREE_NEIGHBORS_TWO_LINKS) {
                boolean[][][] array = rotate(combine(BASE_THREE_NEIGHBORS,
                        combo, LINKS_THREE_NEIGHBORS), rotation);
                assertTrue(potts.getConnectivity(array, false));
            }
        }
    }
    
    /* -------------------------------------------------------------------------
     * CONNECTIVITY FOR FOUR (4) NEIGHBORS
     *
     * All four possible neighbor positions are occupied. The connectivity
     * depends on the ID of the voxel. Only ID = 0 is considered connected;
     * all other ID values are unconnected.
    ------------------------------------------------------------------------- */
    
    @Test
    public void getConnectivity_fourNeighborsNonZeroID_returnsFalse() {
        assertFalse(potts.getConnectivity(new boolean[][][] {{
                { false,  true, false },
                {  true,  true,  true },
                { false,  true, false } }}, false));
    }
    
    @Test
    public void getConnectivity_fourNeighborsZeroID_returnsTrue() {
        assertTrue(potts.getConnectivity(new boolean[][][] {{
                { false,  true, false },
                {  true,  true,  true },
                { false,  true, false } }}, true));
    }
}
