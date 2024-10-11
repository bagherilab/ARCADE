package arcade.potts.env.location;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.junit.BeforeClass;
import org.junit.Test;
import sim.util.Int3D;
import ec.util.MersenneTwisterFast;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.potts.env.location.Voxel.VOXEL_COMPARATOR;
import static arcade.potts.util.PottsEnums.Direction;
import static arcade.potts.util.PottsEnums.Region;

public class PottsLocationTest {
    private static final double EPSILON = 1E-10;
    
    static MersenneTwisterFast randomDoubleZero;
    
    static MersenneTwisterFast randomDoubleOne;
    
    static final int LOCATION_SURFACE = randomIntBetween(0, 100);
    
    static final int LOCATION_HEIGHT = randomIntBetween(0, 100);
    
    static final int DELTA_SURFACE = randomIntBetween(1, 10);
    
    static final int DELTA_HEIGHT = randomIntBetween(1, 10);
    
    static ArrayList<Voxel> voxelListForAddRemove;
    
    static ArrayList<Voxel> voxelListA;
    
    static ArrayList<Voxel> voxelListB;
    
    static ArrayList<Voxel> voxelListAB;
    
    @BeforeClass
    public static void setupMocks() {
        randomDoubleZero = mock(MersenneTwisterFast.class);
        when(randomDoubleZero.nextDouble()).thenReturn(0.0);
        
        randomDoubleOne = mock(MersenneTwisterFast.class);
        when(randomDoubleOne.nextDouble()).thenReturn(1.0);
    }
    
    @BeforeClass
    public static void setupLists() {
        voxelListForAddRemove = new ArrayList<>();
        voxelListForAddRemove.add(new Voxel(0, 0, 0));
        voxelListForAddRemove.add(new Voxel(1, 0, 0));
        
        /*
         * Lattice site shape:
         *
         *     x x x x
         *     x     x
         *     x
         *
         * Each list is a subset of the shape:
         *
         *  (A)         (B)
         *  x x . .     . . x x
         *  x     .     .     x
         *  x           .
         */
        
        voxelListA = new ArrayList<>();
        voxelListA.add(new Voxel(0, 0, 1));
        voxelListA.add(new Voxel(0, 1, 1));
        voxelListA.add(new Voxel(1, 0, 1));
        voxelListA.add(new Voxel(0, 2, 1));
        
        voxelListB = new ArrayList<>();
        voxelListB.add(new Voxel(2, 0, 1));
        voxelListB.add(new Voxel(3, 0, 1));
        voxelListB.add(new Voxel(3, 1, 1));
        
        voxelListAB = new ArrayList<>(voxelListA);
        voxelListAB.addAll(voxelListB);
    }
    
    static class PottsLocationMock extends PottsLocation {
        HashMap<Direction, Integer> diameterMap;
        
        PottsLocationMock(ArrayList<Voxel> voxels) { super(voxels); }
        
        @Override
        PottsLocation makeLocation(ArrayList<Voxel> voxels) { return new PottsLocationMock(voxels); }
        
        @Override
        public double convertSurface(double volume, double height) { return 0; }
        
        @Override
        int calculateSurface() { return surface + LOCATION_SURFACE; }
        
        @Override
        int calculateHeight() { return height + LOCATION_HEIGHT; }
        
        @Override
        int updateSurface(Voxel voxel) { return DELTA_SURFACE; }
        
        @Override
        int updateHeight(Voxel voxel) { return DELTA_HEIGHT; }
        
        @Override
        ArrayList<Voxel> getNeighbors(Voxel voxel) {
            int num = 6;
            int[] x = { 0, 1, 0, -1, 0, 0 };
            int[] y = { -1, 0, 1, 0, 0, 0 };
            int[] z = { 0, 0, 0, 0, 1, -1 };
            
            ArrayList<Voxel> neighbors = new ArrayList<>();
            for (int i = 0; i < num; i++) {
                neighbors.add(new Voxel(voxel.x + x[i], voxel.y + y[i], voxel.z + z[i]));
            }
            return neighbors;
        }
        
        @Override
        HashMap<Direction, Integer> getDiameters() {
            if (diameterMap != null) {
                return diameterMap;
            } else {
                this.diameterMap = new HashMap<>();
                diameterMap.put(Direction.YZ_PLANE, 1);
            }
            
            return diameterMap;
        }
        
        @Override
        Direction getSlice(Direction direction, HashMap<Direction, Integer> diameters) {
            switch (direction) {
                case XY_PLANE:
                    return Direction.NEGATIVE_YZ;
                case POSITIVE_XY:
                    return Direction.YZ_PLANE;
                case NEGATIVE_ZX:
                    return Direction.POSITIVE_YZ;
                case YZ_PLANE:
                    return Direction.ZX_PLANE;
                default:
                    return null;
            }
        }
        
        @Override
        ArrayList<Voxel> getSelected(Voxel center, double n) { return new ArrayList<>(); }
    }
    
    @Test
    public void constructor_called_setsSizes() {
        PottsLocationMock loc = new PottsLocationMock(voxelListAB);
        assertEquals(voxelListAB.size(), loc.volume);
        assertEquals(LOCATION_SURFACE, loc.surface);
        assertEquals(LOCATION_HEIGHT, loc.height);
    }
    
    @Test
    public void constructor_called_setsCenter() {
        PottsLocationMock loc = new PottsLocationMock(voxelListAB);
        
        double cx = 0;
        double cy = 0;
        double cz = 0;
        
        for (Voxel v : voxelListAB) {
            cx += v.x;
            cy += v.y;
            cz += v.z;
        }
        
        int n = voxelListAB.size();
        
        assertEquals(cx / n, loc.cx, EPSILON);
        assertEquals(cy / n, loc.cy, EPSILON);
        assertEquals(cz / n, loc.cz, EPSILON);
    }
    
    @Test
    public void getVoxels_noVoxels_returnsEmpty() {
        PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
        assertEquals(0, loc.getVoxels().size());
    }
    
    @Test
    public void getVoxels_hasVoxels_returnsList() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        
        int n = randomIntBetween(1, 100);
        for (int i = 0; i < n; i++) {
            voxels.add(new Voxel(i, i, i));
        }
        
        PottsLocationMock loc = new PottsLocationMock(voxels);
        ArrayList<Voxel> voxelList = loc.getVoxels();
        
        assertNotSame(loc.voxels, voxelList);
        voxelList.sort(VOXEL_COMPARATOR);
        voxels.sort(VOXEL_COMPARATOR);
        assertEquals(voxels, voxelList);
    }
    
    @Test
    public void getVoxels_noVoxelsRegion_returnsEmpty() {
        PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
        assertEquals(0, loc.getVoxels(Region.DEFAULT).size());
    }
    
    @Test
    public void getVoxels_hasVoxelsRegion_returnsEmpty() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        
        int n = randomIntBetween(1, 100);
        for (int i = 0; i < n; i++) {
            voxels.add(new Voxel(i, i, i));
        }
        
        PottsLocationMock loc = new PottsLocationMock(voxels);
        assertEquals(0, loc.getVoxels(Region.DEFAULT).size());
    }
    
    @Test
    public void getRegions_noRegions_returnsNull() {
        PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
        assertNull(loc.getRegions());
    }
    
    @Test
    public void getVolume_hasVoxels_returnsValue() {
        PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
        loc.add(0, 0, 0);
        assertEquals(1, (int) loc.getVolume());
    }
    
    @Test
    public void getVolume_noVoxels_returnsValue() {
        PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
        assertEquals(0, (int) loc.getVolume());
    }
    
    @Test
    public void getVolume_givenRegion_returnsValue() {
        PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
        loc.add(0, 0, 0);
        assertEquals(1, (int) loc.getVolume(Region.DEFAULT));
    }
    
    @Test
    public void getSurface_hasVoxels_returnsValue() {
        PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
        loc.add(0, 0, 0);
        assertEquals(LOCATION_SURFACE + DELTA_SURFACE, (int) loc.getSurface());
    }
    
    @Test
    public void getSurface_noVoxels_returnsValue() {
        PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
        assertEquals(LOCATION_SURFACE, (int) loc.getSurface());
    }
    
    @Test
    public void getSurface_givenRegion_returnsValue() {
        PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
        loc.add(0, 0, 0);
        assertEquals(LOCATION_SURFACE + DELTA_SURFACE, (int) loc.getSurface(Region.DEFAULT));
    }
    
    @Test
    public void getHeight_hasVoxels_returnsValue() {
        PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
        loc.add(0, 0, 0);
        assertEquals(LOCATION_HEIGHT + DELTA_HEIGHT, (int) loc.getHeight());
    }
    
    @Test
    public void getHeight_noVoxels_returnsValue() {
        PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
        assertEquals(LOCATION_HEIGHT, (int) loc.getHeight());
    }
    
    @Test
    public void getHeight_givenRegion_returnsValue() {
        PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
        loc.add(0, 0, 0);
        assertEquals(LOCATION_HEIGHT + DELTA_HEIGHT, (int) loc.getHeight(Region.DEFAULT));
    }
    
    @Test
    public void add_newVoxel_updatesList() {
        PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
        loc.add(0, 0, 0);
        loc.add(1, 0, 0);
        assertEquals(voxelListForAddRemove, loc.voxels);
    }
    
    @Test
    public void add_newVoxel_updatesVolume() {
        PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
        loc.add(0, 0, 0);
        assertEquals(1, loc.volume);
    }
    
    @Test
    public void add_newVoxel_updatesSurface() {
        PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
        loc.add(0, 0, 0);
        assertEquals(LOCATION_SURFACE + DELTA_SURFACE, loc.surface);
    }
    
    @Test
    public void add_newVoxel_updatesHeight() {
        PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
        loc.add(0, 0, 0);
        assertEquals(LOCATION_HEIGHT + DELTA_HEIGHT, loc.height);
    }
    
    @Test
    public void add_newVoxel_updatesCenter() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        
        voxels.add(new Voxel(1, 4, 1));
        voxels.add(new Voxel(2, 3, 0));
        voxels.add(new Voxel(2, 2, 2));
        voxels.add(new Voxel(3, 3, 1));
        
        PottsLocationMock loc = new PottsLocationMock(voxels);
        loc.add(1, 1, 1);
        
        assertEquals(9. / 5, loc.cx, EPSILON);
        assertEquals(13. / 5, loc.cy, EPSILON);
        assertEquals(5. / 5, loc.cz, EPSILON);
    }
    
    @Test
    public void add_firstVoxel_setsCenter() {
        PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
        int cx = randomIntBetween(1, 10);
        int cy = randomIntBetween(1, 10);
        int cz = randomIntBetween(1, 10);
        loc.add(cx, cy, cz);
        assertEquals(cx, loc.cx, EPSILON);
        assertEquals(cy, loc.cy, EPSILON);
        assertEquals(cz, loc.cz, EPSILON);
    }
    
    @Test
    public void add_existingVoxel_doesNothing() {
        PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
        loc.add(0, 0, 0);
        loc.add(1, 0, 0);
        loc.add(0, 0, 0);
        assertEquals(voxelListForAddRemove, loc.voxels);
    }
    
    @Test
    public void add_newVoxelWithRegion_updatesList() {
        PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
        loc.add(Region.DEFAULT, 0, 0, 0);
        loc.add(Region.UNDEFINED, 1, 0, 0);
        assertEquals(voxelListForAddRemove, loc.voxels);
    }
    
    @Test
    public void add_newVoxelWithRegion_updatesVolume() {
        PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
        loc.add(Region.DEFAULT, 0, 0, 0);
        assertEquals(1, loc.volume);
    }
    
    @Test
    public void add_newVoxelWithRegion_updatesSurface() {
        PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
        loc.add(Region.DEFAULT, 0, 0, 0);
        assertEquals(LOCATION_SURFACE + DELTA_SURFACE, loc.surface);
    }
    
    @Test
    public void add_newVoxelWithRegion_updatesHeight() {
        PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
        loc.add(Region.DEFAULT, 0, 0, 0);
        assertEquals(LOCATION_HEIGHT + DELTA_HEIGHT, loc.height);
    }
    
    @Test
    public void add_existingVoxelWithRegion_doesNothing() {
        PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
        loc.add(Region.DEFAULT, 0, 0, 0);
        loc.add(Region.UNDEFINED, 1, 0, 0);
        loc.add(Region.NUCLEUS, 0, 0, 0);
        assertEquals(voxelListForAddRemove, loc.voxels);
    }
    
    @Test
    public void remove_existingVoxel_updatesList() {
        PottsLocationMock loc = new PottsLocationMock(voxelListForAddRemove);
        ArrayList<Voxel> voxelsRemoved = new ArrayList<>();
        voxelsRemoved.add(new Voxel(1, 0, 0));
        loc.remove(0, 0, 0);
        assertEquals(voxelsRemoved, loc.voxels);
    }
    
    @Test
    public void remove_existingVoxel_updatesVolume() {
        PottsLocationMock loc = new PottsLocationMock(voxelListForAddRemove);
        loc.remove(0, 0, 0);
        assertEquals(1, loc.volume);
    }
    
    @Test
    public void remove_existingVoxel_updatesSurface() {
        PottsLocationMock loc = new PottsLocationMock(voxelListForAddRemove);
        loc.remove(0, 0, 0);
        assertEquals(LOCATION_SURFACE - DELTA_SURFACE, loc.surface);
    }
    
    @Test
    public void remove_existingVoxel_updatesHeight() {
        PottsLocationMock loc = new PottsLocationMock(voxelListForAddRemove);
        loc.remove(0, 0, 0);
        assertEquals(LOCATION_HEIGHT - DELTA_HEIGHT, loc.height);
    }
    
    @Test
    public void remove_existingVoxel_updatesCenter() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        
        voxels.add(new Voxel(1, 4, 1));
        voxels.add(new Voxel(2, 3, 0));
        voxels.add(new Voxel(2, 2, 2));
        voxels.add(new Voxel(3, 3, 1));
        
        PottsLocationMock loc = new PottsLocationMock(voxels);
        loc.remove(1, 4, 1);
        
        assertEquals(7. / 3, loc.cx, EPSILON);
        assertEquals(8. / 3, loc.cy, EPSILON);
        assertEquals(3. / 3, loc.cz, EPSILON);
    }
    
    @Test
    public void remove_allVoxels_returnsEmptyList() {
        PottsLocationMock loc = new PottsLocationMock(voxelListForAddRemove);
        loc.remove(0, 0, 0);
        loc.remove(1, 0, 0);
        assertEquals(new ArrayList<>(), loc.voxels);
    }
    
    @Test
    public void remove_allVoxels_updatesCenter() {
        PottsLocationMock loc = new PottsLocationMock(voxelListForAddRemove);
        loc.remove(0, 0, 0);
        loc.remove(1, 0, 0);
        assertEquals(0, loc.cx, EPSILON);
        assertEquals(0, loc.cy, EPSILON);
        assertEquals(0, loc.cz, EPSILON);
    }
    
    @Test
    public void remove_missingVoxel_doesNothing() {
        PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
        loc.remove(0, 0, 0);
        assertEquals(new ArrayList<>(), loc.voxels);
    }
    
    @Test
    public void remove_existingVoxelWithRegion_updatesList() {
        PottsLocationMock loc = new PottsLocationMock(voxelListForAddRemove);
        ArrayList<Voxel> voxelsRemoved = new ArrayList<>();
        voxelsRemoved.add(new Voxel(1, 0, 0));
        loc.remove(Region.DEFAULT, 0, 0, 0);
        assertEquals(voxelsRemoved, loc.voxels);
    }
    
    @Test
    public void remove_existingVoxelWithRegion_updatesVolume() {
        PottsLocationMock loc = new PottsLocationMock(voxelListForAddRemove);
        loc.remove(Region.DEFAULT, 0, 0, 0);
        assertEquals(1, loc.volume);
    }
    
    @Test
    public void remove_existingVoxelWithRegion_updatesSurface() {
        PottsLocationMock loc = new PottsLocationMock(voxelListForAddRemove);
        loc.remove(Region.DEFAULT, 0, 0, 0);
        assertEquals(LOCATION_SURFACE - DELTA_SURFACE, loc.surface);
    }
    
    @Test
    public void remove_existingVoxelWithRegion_updatesHeight() {
        PottsLocationMock loc = new PottsLocationMock(voxelListForAddRemove);
        loc.remove(Region.DEFAULT, 0, 0, 0);
        assertEquals(LOCATION_HEIGHT - DELTA_HEIGHT, loc.height);
    }
    
    @Test
    public void remove_allVoxelsWithRegion_returnsEmptyList() {
        PottsLocationMock loc = new PottsLocationMock(voxelListForAddRemove);
        loc.remove(Region.DEFAULT, 0, 0, 0);
        loc.remove(Region.UNDEFINED, 1, 0, 0);
        assertEquals(new ArrayList<>(), loc.voxels);
    }
    
    @Test
    public void remove_missingVoxelWithRegion_doesNothing() {
        PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
        loc.remove(Region.DEFAULT, 0, 0, 0);
        assertEquals(new ArrayList<>(), loc.voxels);
    }
    
    @Test
    public void assign_anyVoxel_doesNothing() {
        PottsLocationMock loc = new PottsLocationMock(voxelListForAddRemove);
        loc.assign(Region.DEFAULT, new Voxel(0, 0, 0));
        assertEquals(voxelListForAddRemove, loc.voxels);
    }
    
    @Test
    public void distribute_anyTarget_doesNothing() {
        PottsLocationMock loc = new PottsLocationMock(voxelListForAddRemove);
        loc.distribute(Region.DEFAULT, 1, randomDoubleOne);
        assertEquals(voxelListForAddRemove, loc.voxels);
    }
    
    @Test
    public void adjust_voxelExists_returnsVoxel() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(0, 0, 0));
        PottsLocationMock loc = new PottsLocationMock(voxels);
        
        Voxel voxel = new Voxel(0, 0, 0);
        Voxel adjusted = loc.adjust(voxel);
        
        assertEquals(voxel, adjusted);
    }
    
    @Test
    public void adjust_voxelDoesNotExist_returnsClosestVoxel() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(0, 1, 2));
        voxels.add(new Voxel(0, 2, 3));
        voxels.add(new Voxel(2, 1, 0));
        PottsLocationMock loc = new PottsLocationMock(voxels);
        
        Voxel voxel = new Voxel(1, 2, 3);
        Voxel adjusted = loc.adjust(voxel);
        Voxel expected = new Voxel(0, 2, 3);
        
        assertEquals(expected, adjusted);
    }
    
    @Test
    public void clear_hasVoxels_updatesArray() {
        PottsLocationMock loc = new PottsLocationMock(voxelListForAddRemove);
        int[][][] array = new int[][][] { { { 1, 0, 0 }, { 1, 0, 0 } } };
        loc.clear(array, null);
        
        assertArrayEquals(new int[] { 0, 0, 0 }, array[0][0]);
        assertArrayEquals(new int[] { 0, 0, 0 }, array[0][1]);
    }
    
    @Test
    public void clear_hasVoxels_updatesLists() {
        PottsLocationMock loc = new PottsLocationMock(voxelListForAddRemove);
        loc.clear(new int[1][3][3], new int[1][3][3]);
        assertEquals(0, loc.voxels.size());
    }
    
    @Test
    public void update_validID_updatesArray() {
        int[][][] array = new int[][][] { { { 0, 1, 2 } } };
        ArrayList<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(0, 1, 0));
        PottsLocationMock loc = new PottsLocationMock(voxels);
        
        loc.update(3, array, null);
        assertArrayEquals(new int[] { 0, 3, 2 }, array[0][0]);
    }
    
    @Test
    public void getCenter_hasVoxels_calculatesValue() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(0, 1, 1));
        voxels.add(new Voxel(1, 1, 2));
        voxels.add(new Voxel(2, 2, 2));
        voxels.add(new Voxel(2, 3, 3));
        PottsLocationMock loc = new PottsLocationMock(voxels);
        
        assertEquals(5 / 4., loc.cx, EPSILON);
        assertEquals(7 / 4., loc.cy, EPSILON);
        assertEquals(8 / 4., loc.cz, EPSILON);
        assertEquals(new Voxel(1, 2, 2), loc.getCenter());
    }
    
    @Test
    public void getCenter_noVoxels_returnsNull() {
        PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
        assertNull(loc.getCenter());
    }
    
    @Test
    public void getOffset_noVoxels_returnsNull() {
        PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
        assertNull(loc.getOffset(null));
    }
    
    @Test
    public void getOffset_oneOffset_returnsValue() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(0, 100, 200));
        voxels.add(new Voxel(100, 200, 300));
        PottsLocationMock loc = new PottsLocationMock(voxels);
        ArrayList<Integer> offsets = new ArrayList<>(Arrays.asList(25));
        assertEquals(new Voxel(25, 125, 225), loc.getOffset(offsets));
    }
    
    @Test
    public void getOffset_twoOffsets_returnsValue() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(0, 100, 200));
        voxels.add(new Voxel(100, 200, 300));
        PottsLocationMock loc = new PottsLocationMock(voxels);
        ArrayList<Integer> offsets = new ArrayList<>(Arrays.asList(33, 66));
        assertEquals(new Voxel(33, 166, 200), loc.getOffset(offsets));
    }
    
    @Test
    public void getOffset_threeOffsets_returnsValue() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(0, 100, 200));
        voxels.add(new Voxel(100, 200, 300));
        PottsLocationMock loc = new PottsLocationMock(voxels);
        ArrayList<Integer> offsets = new ArrayList<>(Arrays.asList(50, 50, 50));
        assertEquals(new Voxel(50, 150, 250), loc.getOffset(offsets));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void getOffset_noOffsets_raisesException() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(0, 100, 200));
        voxels.add(new Voxel(100, 200, 300));
        PottsLocationMock loc = new PottsLocationMock(voxels);
        ArrayList<Integer> offsets = new ArrayList<>();
        loc.getOffset(offsets);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void getOffset_invalidOffsets_raisesException() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(0, 100, 200));
        voxels.add(new Voxel(100, 200, 300));
        PottsLocationMock loc = new PottsLocationMock(voxels);
        ArrayList<Integer> offsets = new ArrayList<>(Arrays.asList(0, 0, 0, 0));
        loc.getOffset(offsets);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void getOffset_nullOffsets_raisesException() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(0, 100, 200));
        voxels.add(new Voxel(100, 200, 300));
        PottsLocationMock loc = new PottsLocationMock(voxels);
        loc.getOffset(null);
    }
    
    @Test
    public void getCentroid_hasVoxels_returnsArray() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(0, 1, 1));
        voxels.add(new Voxel(1, 1, 2));
        voxels.add(new Voxel(2, 2, 2));
        voxels.add(new Voxel(2, 3, 3));
        PottsLocationMock loc = new PottsLocationMock(voxels);
        
        double[] centroid = loc.getCentroid();
        
        assertEquals(5 / 4., centroid[0], EPSILON);
        assertEquals(7 / 4., centroid[1], EPSILON);
        assertEquals(8 / 4., centroid[2], EPSILON);
    }
    
    @Test
    public void getCentroid_noVoxels_returnsZeros() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        PottsLocationMock loc = new PottsLocationMock(voxels);
        
        double[] centroid = loc.getCentroid();
        
        assertEquals(0, centroid[0], EPSILON);
        assertEquals(0, centroid[1], EPSILON);
        assertEquals(0, centroid[2], EPSILON);
    }
    
    @Test
    public void getCentroid_hasVoxelsWithRegion_returnsArray() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(0, 1, 1));
        voxels.add(new Voxel(1, 1, 2));
        voxels.add(new Voxel(2, 2, 2));
        voxels.add(new Voxel(2, 3, 3));
        PottsLocationMock loc = new PottsLocationMock(voxels);
        
        double[] centroid = loc.getCentroid(Region.DEFAULT);
        
        assertEquals(5 / 4., centroid[0], EPSILON);
        assertEquals(7 / 4., centroid[1], EPSILON);
        assertEquals(8 / 4., centroid[2], EPSILON);
    }
    
    @Test
    public void getCentroid_noVoxelsWithRegion_returnsZeros() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        PottsLocationMock loc = new PottsLocationMock(voxels);
        
        double[] centroid = loc.getCentroid(Region.DEFAULT);
        
        assertEquals(0, centroid[0], EPSILON);
        assertEquals(0, centroid[1], EPSILON);
        assertEquals(0, centroid[2], EPSILON);
    }
    
    @Test
    public void getDirection_oneMaximumDiameter_returnsValue() {
        PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        when(random.nextInt(1)).thenReturn(0);
        
        HashMap<Direction, Integer> diameters = new HashMap<>();
        diameters.put(Direction.XY_PLANE, 50);
        diameters.put(Direction.POSITIVE_XY, 89);
        diameters.put(Direction.NEGATIVE_ZX, 100);
        loc.diameterMap = diameters;
        
        assertEquals(Direction.POSITIVE_YZ, loc.getDirection(random));
    }
    
    @Test
    public void getDirection_multipleMaximumDiameters_returnsValueBasedOnRandom() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(0, 0, 0));
        PottsLocationMock loc = new PottsLocationMock(voxels);
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        when(random.nextInt(3)).thenReturn(1).thenReturn(0).thenReturn(2);
        
        HashMap<Direction, Integer> diameters = new HashMap<>();
        diameters.put(Direction.YZ_PLANE, 50);
        diameters.put(Direction.XY_PLANE, 90);
        diameters.put(Direction.POSITIVE_XY, 95);
        diameters.put(Direction.NEGATIVE_ZX, 100);
        loc.diameterMap = diameters;
        
        assertEquals(Direction.YZ_PLANE, loc.getDirection(random));
        assertEquals(Direction.NEGATIVE_YZ, loc.getDirection(random));
        assertEquals(Direction.POSITIVE_YZ, loc.getDirection(random));
    }
    
    @Test
    public void getDirection_zeroDiameters_returnsRandomValue() {
        for (int i = 0; i < 100; i++) {
            PottsLocationMock loc = new PottsLocationMock(new ArrayList<>());
            MersenneTwisterFast random = mock(MersenneTwisterFast.class);
            when(random.nextInt(1)).thenReturn(0);
            
            HashMap<Direction, Integer> diameters = new HashMap<>();
            diameters.put(Direction.XY_PLANE, 0);
            diameters.put(Direction.POSITIVE_XY, 0);
            diameters.put(Direction.NEGATIVE_ZX, 0);
            loc.diameterMap = diameters;
            
            Direction direction = loc.getDirection(random);
            assertNotNull(direction);
            assertNotEquals(Direction.UNDEFINED, direction);
        }
    }
    
    @Test
    public void convert_createsContainer() {
        int locationID = randomIntBetween(1, 10);
        
        ArrayList<Voxel> voxels = new ArrayList<>();
        int n = randomIntBetween(1, 10);
        for (int i = 0; i < 2 * n; i++) {
            for (int j = 0; j < 2 * n; j++) {
                voxels.add(new Voxel(i, j, 0));
            }
        }
        
        PottsLocationMock location = new PottsLocationMock(voxels);
        
        PottsLocationContainer container = (PottsLocationContainer) location.convert(locationID);
        
        assertEquals(locationID, container.id);
        assertEquals(new Voxel(n, n, 0), container.center);
        assertEquals(voxels, container.allVoxels);
        assertNull(container.regions);
    }
    
    private ArrayList<Voxel> prepSplit() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(0, 0, 0));
        voxels.add(new Voxel(0, 1, 1));
        voxels.add(new Voxel(0, 2, 1));
        voxels.add(new Voxel(1, 0, 2));
        voxels.add(new Voxel(1, 1, 1));
        voxels.add(new Voxel(2, 2, 2));
        voxels.add(new Voxel(2, 2, 0));
        return voxels;
    }
    
    @Test
    public void splitVoxels_YZPlaneDirectionRandomZero_updatesLists() {
        ArrayList<Voxel> voxels = prepSplit();
        PottsLocationMock loc = new PottsLocationMock(voxels);
        
        ArrayList<Voxel> voxelsA = new ArrayList<>();
        ArrayList<Voxel> voxelsB = new ArrayList<>();
        ArrayList<Voxel> voxelsASplit = new ArrayList<>();
        ArrayList<Voxel> voxelsBSplit = new ArrayList<>();
        
        voxelsASplit.add(new Voxel(0, 0, 0));
        voxelsASplit.add(new Voxel(0, 1, 1));
        voxelsASplit.add(new Voxel(0, 2, 1));
        
        voxelsBSplit.add(new Voxel(1, 0, 2));
        voxelsBSplit.add(new Voxel(1, 1, 1));
        voxelsBSplit.add(new Voxel(2, 2, 2));
        voxelsBSplit.add(new Voxel(2, 2, 0));
        
        Voxel center = loc.getCenter();
        Plane yzPlane = new Plane(center, Direction.YZ_PLANE.vector);
        
        PottsLocation.splitVoxels(yzPlane, voxels, voxelsA, voxelsB, randomDoubleZero);
        assertEquals(voxelsASplit, voxelsA);
        assertEquals(voxelsBSplit, voxelsB);
        
        voxelsA.clear();
        voxelsB.clear();
        voxelsASplit.clear();
        voxelsBSplit.clear();
    }
    
    @Test
    public void splitVoxels_ZXPlaneDirectionRandomZero_updatesLists() {
        ArrayList<Voxel> voxels = prepSplit();
        PottsLocationMock loc = new PottsLocationMock(voxels);
        
        ArrayList<Voxel> voxelsA = new ArrayList<>();
        ArrayList<Voxel> voxelsB = new ArrayList<>();
        ArrayList<Voxel> voxelsASplit = new ArrayList<>();
        ArrayList<Voxel> voxelsBSplit = new ArrayList<>();
        
        voxelsASplit.add(new Voxel(0, 0, 0));
        voxelsBSplit.add(new Voxel(0, 1, 1));
        voxelsBSplit.add(new Voxel(0, 2, 1));
        voxelsASplit.add(new Voxel(1, 0, 2));
        voxelsBSplit.add(new Voxel(1, 1, 1));
        voxelsBSplit.add(new Voxel(2, 2, 2));
        voxelsBSplit.add(new Voxel(2, 2, 0));
        
        Voxel center = loc.getCenter();
        Plane zxPlane = new Plane(center, Direction.ZX_PLANE.vector);
        
        PottsLocation.splitVoxels(zxPlane, voxels, voxelsA, voxelsB, randomDoubleZero);
        assertEquals(voxelsASplit, voxelsA);
        assertEquals(voxelsBSplit, voxelsB);
    }
    
    @Test
    public void splitVoxels_XYPlaneDirectionRandomZero_updatesLists() {
        ArrayList<Voxel> voxels = prepSplit();
        PottsLocationMock loc = new PottsLocationMock(voxels);
        
        ArrayList<Voxel> voxelsA = new ArrayList<>();
        ArrayList<Voxel> voxelsB = new ArrayList<>();
        ArrayList<Voxel> voxelsASplit = new ArrayList<>();
        ArrayList<Voxel> voxelsBSplit = new ArrayList<>();
        
        voxelsASplit.add(new Voxel(0, 0, 0));
        voxelsBSplit.add(new Voxel(0, 1, 1));
        voxelsBSplit.add(new Voxel(0, 2, 1));
        voxelsBSplit.add(new Voxel(1, 0, 2));
        voxelsBSplit.add(new Voxel(1, 1, 1));
        voxelsBSplit.add(new Voxel(2, 2, 2));
        voxelsASplit.add(new Voxel(2, 2, 0));
        
        Voxel center = loc.getCenter();
        Plane xyPlane = new Plane(center, Direction.XY_PLANE.vector);
        
        PottsLocation.splitVoxels(xyPlane, voxels, voxelsA, voxelsB, randomDoubleZero);
        assertEquals(voxelsASplit, voxelsA);
        assertEquals(voxelsBSplit, voxelsB);
    }
    
    @Test
    public void splitVoxels_negativeXYDirectionRandomZero_updatesLists() {
        ArrayList<Voxel> voxels = prepSplit();
        PottsLocationMock loc = new PottsLocationMock(voxels);
        
        ArrayList<Voxel> voxelsA = new ArrayList<>();
        ArrayList<Voxel> voxelsB = new ArrayList<>();
        ArrayList<Voxel> voxelsASplit = new ArrayList<>();
        ArrayList<Voxel> voxelsBSplit = new ArrayList<>();
        
        voxelsBSplit.add(new Voxel(0, 0, 0));
        voxelsBSplit.add(new Voxel(0, 1, 1));
        voxelsBSplit.add(new Voxel(0, 2, 1));
        voxelsBSplit.add(new Voxel(1, 0, 2));
        voxelsBSplit.add(new Voxel(1, 1, 1));
        voxelsASplit.add(new Voxel(2, 2, 2));
        voxelsASplit.add(new Voxel(2, 2, 0));
        
        Voxel center = loc.getCenter();
        Plane negativeXYPlane = new Plane(center, Direction.NEGATIVE_XY.vector);
        
        PottsLocation.splitVoxels(negativeXYPlane, voxels, voxelsA, voxelsB, randomDoubleZero);
        assertEquals(voxelsASplit, voxelsA);
        assertEquals(voxelsBSplit, voxelsB);
    }
    
    @Test
    public void splitVoxels_positiveXYDirectionRandomZero_updatesLists() {
        ArrayList<Voxel> voxels = prepSplit();
        PottsLocationMock loc = new PottsLocationMock(voxels);
        
        ArrayList<Voxel> voxelsA = new ArrayList<>();
        ArrayList<Voxel> voxelsB = new ArrayList<>();
        ArrayList<Voxel> voxelsASplit = new ArrayList<>();
        ArrayList<Voxel> voxelsBSplit = new ArrayList<>();
        
        voxelsBSplit.add(new Voxel(0, 0, 0));
        voxelsBSplit.add(new Voxel(0, 1, 1));
        voxelsBSplit.add(new Voxel(0, 2, 1));
        voxelsASplit.add(new Voxel(1, 0, 2));
        voxelsBSplit.add(new Voxel(1, 1, 1));
        voxelsBSplit.add(new Voxel(2, 2, 2));
        voxelsBSplit.add(new Voxel(2, 2, 0));
        
        Voxel center = loc.getCenter();
        Plane positiveXYPlane = new Plane(center, Direction.POSITIVE_XY.vector);
        
        PottsLocation.splitVoxels(positiveXYPlane, voxels, voxelsA, voxelsB, randomDoubleZero);
        assertEquals(voxelsASplit, voxelsA);
        assertEquals(voxelsBSplit, voxelsB);
    }
    
    @Test
    public void splitVoxels_negativeYZDirectionRandomZero_updatesLists() {
        ArrayList<Voxel> voxels = prepSplit();
        PottsLocationMock loc = new PottsLocationMock(voxels);
        
        ArrayList<Voxel> voxelsA = new ArrayList<>();
        ArrayList<Voxel> voxelsB = new ArrayList<>();
        ArrayList<Voxel> voxelsASplit = new ArrayList<>();
        ArrayList<Voxel> voxelsBSplit = new ArrayList<>();
        
        voxelsBSplit.add(new Voxel(0, 0, 0));
        voxelsBSplit.add(new Voxel(0, 1, 1));
        voxelsASplit.add(new Voxel(0, 2, 1));
        voxelsBSplit.add(new Voxel(1, 0, 2));
        voxelsBSplit.add(new Voxel(1, 1, 1));
        voxelsASplit.add(new Voxel(2, 2, 2));
        voxelsBSplit.add(new Voxel(2, 2, 0));
        
        Voxel center = loc.getCenter();
        Plane negativeYZPlane = new Plane(center, Direction.NEGATIVE_YZ.vector);
        
        PottsLocation.splitVoxels(negativeYZPlane, voxels, voxelsA, voxelsB, randomDoubleZero);
        assertEquals(voxelsASplit, voxelsA);
        assertEquals(voxelsBSplit, voxelsB);
    }
    
    @Test
    public void splitVoxels_positiveYZDirectionRandomZero_updatesLists() {
        ArrayList<Voxel> voxels = prepSplit();
        PottsLocationMock loc = new PottsLocationMock(voxels);
        
        ArrayList<Voxel> voxelsA = new ArrayList<>();
        ArrayList<Voxel> voxelsB = new ArrayList<>();
        ArrayList<Voxel> voxelsASplit = new ArrayList<>();
        ArrayList<Voxel> voxelsBSplit = new ArrayList<>();
        
        voxelsBSplit.add(new Voxel(0, 0, 0));
        voxelsBSplit.add(new Voxel(0, 1, 1));
        voxelsASplit.add(new Voxel(0, 2, 1));
        voxelsBSplit.add(new Voxel(1, 0, 2));
        voxelsBSplit.add(new Voxel(1, 1, 1));
        voxelsBSplit.add(new Voxel(2, 2, 2));
        voxelsASplit.add(new Voxel(2, 2, 0));
        
        Voxel center = loc.getCenter();
        Plane positiveYZPlane = new Plane(center, Direction.POSITIVE_YZ.vector);
        
        PottsLocation.splitVoxels(positiveYZPlane, voxels, voxelsA, voxelsB, randomDoubleZero);
        assertEquals(voxelsASplit, voxelsA);
        assertEquals(voxelsBSplit, voxelsB);
    }
    
    @Test
    public void splitVoxels_negativeZXDirectionRandomZero_updatesLists() {
        ArrayList<Voxel> voxels = prepSplit();
        PottsLocationMock loc = new PottsLocationMock(voxels);
        
        ArrayList<Voxel> voxelsA = new ArrayList<>();
        ArrayList<Voxel> voxelsB = new ArrayList<>();
        ArrayList<Voxel> voxelsASplit = new ArrayList<>();
        ArrayList<Voxel> voxelsBSplit = new ArrayList<>();
        
        voxelsBSplit.add(new Voxel(0, 0, 0));
        voxelsBSplit.add(new Voxel(0, 1, 1));
        voxelsBSplit.add(new Voxel(0, 2, 1));
        voxelsASplit.add(new Voxel(1, 0, 2));
        voxelsBSplit.add(new Voxel(1, 1, 1));
        voxelsASplit.add(new Voxel(2, 2, 2));
        voxelsBSplit.add(new Voxel(2, 2, 0));
        
        Voxel center = loc.getCenter();
        Plane negativeZXPlane = new Plane(center, Direction.NEGATIVE_ZX.vector);
        
        PottsLocation.splitVoxels(negativeZXPlane, voxels, voxelsA, voxelsB, randomDoubleZero);
        assertEquals(voxelsASplit, voxelsA);
        assertEquals(voxelsBSplit, voxelsB);
    }
    
    @Test
    public void splitVoxels_positiveZXDirectionRandomZero_updatesLists() {
        ArrayList<Voxel> voxels = prepSplit();
        PottsLocationMock loc = new PottsLocationMock(voxels);
        
        ArrayList<Voxel> voxelsA = new ArrayList<>();
        ArrayList<Voxel> voxelsB = new ArrayList<>();
        ArrayList<Voxel> voxelsASplit = new ArrayList<>();
        ArrayList<Voxel> voxelsBSplit = new ArrayList<>();
        
        voxelsBSplit.add(new Voxel(0, 0, 0));
        voxelsASplit.add(new Voxel(0, 1, 1));
        voxelsASplit.add(new Voxel(0, 2, 1));
        voxelsASplit.add(new Voxel(1, 0, 2));
        voxelsBSplit.add(new Voxel(1, 1, 1));
        voxelsBSplit.add(new Voxel(2, 2, 2));
        voxelsBSplit.add(new Voxel(2, 2, 0));
        
        Voxel center = loc.getCenter();
        Plane positiveZXPlane = new Plane(center, Direction.POSITIVE_ZX.vector);
        
        PottsLocation.splitVoxels(positiveZXPlane, voxels, voxelsA, voxelsB, randomDoubleZero);
        assertEquals(voxelsASplit, voxelsA);
        assertEquals(voxelsBSplit, voxelsB);
    }
    
    @Test
    public void splitVoxels_YZPlaneDirectionRandomOne_updatesLists() {
        ArrayList<Voxel> voxels = prepSplit();
        PottsLocationMock loc = new PottsLocationMock(voxels);
        
        ArrayList<Voxel> voxelsA = new ArrayList<>();
        ArrayList<Voxel> voxelsB = new ArrayList<>();
        ArrayList<Voxel> voxelsASplit = new ArrayList<>();
        ArrayList<Voxel> voxelsBSplit = new ArrayList<>();
        
        voxelsASplit.add(new Voxel(0, 0, 0));
        voxelsASplit.add(new Voxel(0, 1, 1));
        voxelsASplit.add(new Voxel(0, 2, 1));
        voxelsASplit.add(new Voxel(1, 0, 2));
        voxelsASplit.add(new Voxel(1, 1, 1));
        voxelsBSplit.add(new Voxel(2, 2, 2));
        voxelsBSplit.add(new Voxel(2, 2, 0));
        
        Voxel center = loc.getCenter();
        Plane yzPlane = new Plane(center, Direction.YZ_PLANE.vector);
        
        PottsLocation.splitVoxels(yzPlane, voxels, voxelsA, voxelsB, randomDoubleOne);
        assertEquals(voxelsASplit, voxelsA);
        assertEquals(voxelsBSplit, voxelsB);
    }
    
    @Test
    public void splitVoxels_ZXPlaneDirectionRandomOne_updatesLists() {
        ArrayList<Voxel> voxels = prepSplit();
        PottsLocationMock loc = new PottsLocationMock(voxels);
        
        ArrayList<Voxel> voxelsA = new ArrayList<>();
        ArrayList<Voxel> voxelsB = new ArrayList<>();
        ArrayList<Voxel> voxelsASplit = new ArrayList<>();
        ArrayList<Voxel> voxelsBSplit = new ArrayList<>();
        
        voxelsASplit.add(new Voxel(0, 0, 0));
        voxelsASplit.add(new Voxel(0, 1, 1));
        voxelsBSplit.add(new Voxel(0, 2, 1));
        voxelsASplit.add(new Voxel(1, 0, 2));
        voxelsASplit.add(new Voxel(1, 1, 1));
        voxelsBSplit.add(new Voxel(2, 2, 2));
        voxelsBSplit.add(new Voxel(2, 2, 0));
        
        Voxel center = loc.getCenter();
        Plane zxPlane = new Plane(center, Direction.ZX_PLANE.vector);
        
        PottsLocation.splitVoxels(zxPlane, voxels, voxelsA, voxelsB, randomDoubleOne);
        assertEquals(voxelsASplit, voxelsA);
        assertEquals(voxelsBSplit, voxelsB);
    }
    
    @Test
    public void splitVoxels_XYPlaneDirectionRandomOne_updatesLists() {
        ArrayList<Voxel> voxels = prepSplit();
        PottsLocationMock loc = new PottsLocationMock(voxels);
        
        ArrayList<Voxel> voxelsA = new ArrayList<>();
        ArrayList<Voxel> voxelsB = new ArrayList<>();
        ArrayList<Voxel> voxelsASplit = new ArrayList<>();
        ArrayList<Voxel> voxelsBSplit = new ArrayList<>();
        
        voxelsASplit.add(new Voxel(0, 0, 0));
        voxelsASplit.add(new Voxel(0, 1, 1));
        voxelsASplit.add(new Voxel(0, 2, 1));
        voxelsBSplit.add(new Voxel(1, 0, 2));
        voxelsASplit.add(new Voxel(1, 1, 1));
        voxelsBSplit.add(new Voxel(2, 2, 2));
        voxelsASplit.add(new Voxel(2, 2, 0));
        
        Voxel center = loc.getCenter();
        Plane xyPlane = new Plane(center, Direction.XY_PLANE.vector);
        
        PottsLocation.splitVoxels(xyPlane, voxels, voxelsA, voxelsB, randomDoubleOne);
        assertEquals(voxelsASplit, voxelsA);
        assertEquals(voxelsBSplit, voxelsB);
    }
    
    @Test
    public void splitVoxels_negativeXYDirectionRandomOne_updatesLists() {
        ArrayList<Voxel> voxels = prepSplit();
        PottsLocationMock loc = new PottsLocationMock(voxels);
        
        ArrayList<Voxel> voxelsA = new ArrayList<>();
        ArrayList<Voxel> voxelsB = new ArrayList<>();
        ArrayList<Voxel> voxelsASplit = new ArrayList<>();
        ArrayList<Voxel> voxelsBSplit = new ArrayList<>();
        
        voxelsBSplit.add(new Voxel(0, 0, 0));
        voxelsBSplit.add(new Voxel(0, 1, 1));
        voxelsASplit.add(new Voxel(0, 2, 1));
        voxelsBSplit.add(new Voxel(1, 0, 2));
        voxelsASplit.add(new Voxel(1, 1, 1));
        voxelsASplit.add(new Voxel(2, 2, 2));
        voxelsASplit.add(new Voxel(2, 2, 0));
        
        Voxel center = loc.getCenter();
        Plane negativeXYPlane = new Plane(center, Direction.NEGATIVE_XY.vector);
        
        PottsLocation.splitVoxels(negativeXYPlane, voxels, voxelsA, voxelsB, randomDoubleOne);
        assertEquals(voxelsASplit, voxelsA);
        assertEquals(voxelsBSplit, voxelsB);
    }
    
    @Test
    public void splitVoxels_positiveXYDirectionRandomOne_updatesLists() {
        ArrayList<Voxel> voxels = prepSplit();
        PottsLocationMock loc = new PottsLocationMock(voxels);
        
        ArrayList<Voxel> voxelsA = new ArrayList<>();
        ArrayList<Voxel> voxelsB = new ArrayList<>();
        ArrayList<Voxel> voxelsASplit = new ArrayList<>();
        ArrayList<Voxel> voxelsBSplit = new ArrayList<>();
        
        voxelsASplit.add(new Voxel(0, 0, 0));
        voxelsBSplit.add(new Voxel(0, 1, 1));
        voxelsBSplit.add(new Voxel(0, 2, 1));
        voxelsASplit.add(new Voxel(1, 0, 2));
        voxelsASplit.add(new Voxel(1, 1, 1));
        voxelsASplit.add(new Voxel(2, 2, 2));
        voxelsASplit.add(new Voxel(2, 2, 0));
        
        Voxel center = loc.getCenter();
        Plane positiveXYPlane = new Plane(center, Direction.POSITIVE_XY.vector);
        
        PottsLocation.splitVoxels(positiveXYPlane, voxels, voxelsA, voxelsB, randomDoubleOne);
        assertEquals(voxelsASplit, voxelsA);
        assertEquals(voxelsBSplit, voxelsB);
    }
    
    @Test
    public void splitVoxels_negativeYZDirectionRandomOne_updatesLists() {
        ArrayList<Voxel> voxels = prepSplit();
        PottsLocationMock loc = new PottsLocationMock(voxels);
        
        ArrayList<Voxel> voxelsA = new ArrayList<>();
        ArrayList<Voxel> voxelsB = new ArrayList<>();
        ArrayList<Voxel> voxelsASplit = new ArrayList<>();
        ArrayList<Voxel> voxelsBSplit = new ArrayList<>();
        
        voxelsBSplit.add(new Voxel(0, 0, 0));
        voxelsASplit.add(new Voxel(0, 1, 1));
        voxelsASplit.add(new Voxel(0, 2, 1));
        voxelsASplit.add(new Voxel(1, 0, 2));
        voxelsASplit.add(new Voxel(1, 1, 1));
        voxelsASplit.add(new Voxel(2, 2, 2));
        voxelsASplit.add(new Voxel(2, 2, 0));
        
        Voxel center = loc.getCenter();
        Plane negativeYZPlane = new Plane(center, Direction.NEGATIVE_YZ.vector);
        
        PottsLocation.splitVoxels(negativeYZPlane, voxels, voxelsA, voxelsB, randomDoubleOne);
        assertEquals(voxelsASplit, voxelsA);
        assertEquals(voxelsBSplit, voxelsB);
    }
    
    @Test
    public void splitVoxels_positiveYZDirectionRandomOne_updatesLists() {
        ArrayList<Voxel> voxels = prepSplit();
        PottsLocationMock loc = new PottsLocationMock(voxels);
        
        ArrayList<Voxel> voxelsA = new ArrayList<>();
        ArrayList<Voxel> voxelsB = new ArrayList<>();
        ArrayList<Voxel> voxelsASplit = new ArrayList<>();
        ArrayList<Voxel> voxelsBSplit = new ArrayList<>();
        
        voxelsASplit.add(new Voxel(0, 0, 0));
        voxelsASplit.add(new Voxel(0, 1, 1));
        voxelsASplit.add(new Voxel(0, 2, 1));
        voxelsBSplit.add(new Voxel(1, 0, 2));
        voxelsASplit.add(new Voxel(1, 1, 1));
        voxelsASplit.add(new Voxel(2, 2, 2));
        voxelsASplit.add(new Voxel(2, 2, 0));
        
        Voxel center = loc.getCenter();
        Plane positiveYZPlane = new Plane(center, Direction.POSITIVE_YZ.vector);
        
        PottsLocation.splitVoxels(positiveYZPlane, voxels, voxelsA, voxelsB, randomDoubleOne);
        assertEquals(voxelsASplit, voxelsA);
        assertEquals(voxelsBSplit, voxelsB);
    }
    
    @Test
    public void splitVoxels_negativeZXDirectionRandomOne_updatesLists() {
        ArrayList<Voxel> voxels = prepSplit();
        PottsLocationMock loc = new PottsLocationMock(voxels);
        
        ArrayList<Voxel> voxelsA = new ArrayList<>();
        ArrayList<Voxel> voxelsB = new ArrayList<>();
        ArrayList<Voxel> voxelsASplit = new ArrayList<>();
        ArrayList<Voxel> voxelsBSplit = new ArrayList<>();
        
        voxelsBSplit.add(new Voxel(0, 0, 0));
        voxelsBSplit.add(new Voxel(0, 1, 1));
        voxelsBSplit.add(new Voxel(0, 2, 1));
        voxelsASplit.add(new Voxel(1, 0, 2));
        voxelsASplit.add(new Voxel(1, 1, 1));
        voxelsASplit.add(new Voxel(2, 2, 2));
        voxelsASplit.add(new Voxel(2, 2, 0));
        
        Voxel center = loc.getCenter();
        Plane negativeZXPlane = new Plane(center, Direction.NEGATIVE_ZX.vector);
        
        PottsLocation.splitVoxels(negativeZXPlane, voxels, voxelsA, voxelsB, randomDoubleOne);
        assertEquals(voxelsASplit, voxelsA);
        assertEquals(voxelsBSplit, voxelsB);
    }
    
    @Test
    public void splitVoxels_positiveZXDirectionRandomOne_updatesLists() {
        ArrayList<Voxel> voxels = prepSplit();
        PottsLocationMock loc = new PottsLocationMock(voxels);
        
        ArrayList<Voxel> voxelsA = new ArrayList<>();
        ArrayList<Voxel> voxelsB = new ArrayList<>();
        ArrayList<Voxel> voxelsASplit = new ArrayList<>();
        ArrayList<Voxel> voxelsBSplit = new ArrayList<>();
        
        voxelsASplit.add(new Voxel(0, 0, 0));
        voxelsASplit.add(new Voxel(0, 1, 1));
        voxelsASplit.add(new Voxel(0, 2, 1));
        voxelsASplit.add(new Voxel(1, 0, 2));
        voxelsASplit.add(new Voxel(1, 1, 1));
        voxelsASplit.add(new Voxel(2, 2, 2));
        voxelsBSplit.add(new Voxel(2, 2, 0));
        
        Voxel center = loc.getCenter();
        Plane positiveZXPlane = new Plane(center, Direction.POSITIVE_ZX.vector);
        
        PottsLocation.splitVoxels(positiveZXPlane, voxels, voxelsA, voxelsB, randomDoubleOne);
        assertEquals(voxelsASplit, voxelsA);
        assertEquals(voxelsBSplit, voxelsB);
    }
    
    @Test
    public void separateVoxels_validLists_updatesLists() {
        PottsLocationMock loc = new PottsLocationMock(voxelListAB);
        PottsLocation split = (PottsLocation) loc.separateVoxels(voxelListA, voxelListB, randomDoubleZero);
        
        ArrayList<Voxel> locVoxels = new ArrayList<>(voxelListA);
        ArrayList<Voxel> splitVoxels = new ArrayList<>(voxelListB);
        
        locVoxels.sort(VOXEL_COMPARATOR);
        loc.voxels.sort(VOXEL_COMPARATOR);
        splitVoxels.sort(VOXEL_COMPARATOR);
        split.voxels.sort(VOXEL_COMPARATOR);
        
        assertEquals(locVoxels, loc.voxels);
        assertEquals(splitVoxels, split.voxels);
    }
    
    @Test
    public void separateVoxels_validLists_updatesVolumes() {
        PottsLocationMock loc = new PottsLocationMock(voxelListAB);
        PottsLocation split = (PottsLocation) loc.separateVoxels(voxelListA, voxelListB, randomDoubleZero);
        assertEquals(voxelListA.size(), loc.volume);
        assertEquals(voxelListB.size(), split.volume);
    }
    
    @Test
    public void separateVoxels_validLists_updatesSurfaces() {
        PottsLocationMock loc = new PottsLocationMock(voxelListAB);
        PottsLocation split = (PottsLocation) loc.separateVoxels(voxelListA, voxelListB, randomDoubleZero);
        assertEquals(LOCATION_SURFACE + LOCATION_SURFACE, loc.surface);
        assertEquals(LOCATION_SURFACE, split.surface);
    }
    
    @Test
    public void separateVoxels_validLists_updatesHeights() {
        PottsLocationMock loc = new PottsLocationMock(voxelListAB);
        PottsLocation split = (PottsLocation) loc.separateVoxels(voxelListA, voxelListB, randomDoubleZero);
        assertEquals(LOCATION_HEIGHT + LOCATION_HEIGHT, loc.height);
        assertEquals(LOCATION_HEIGHT, split.height);
    }
    
    @Test
    public void separateVoxels_validLists_updatesCenter() {
        PottsLocationMock loc = new PottsLocationMock(voxelListAB);
        PottsLocation split = (PottsLocation) loc.separateVoxels(voxelListA, voxelListB, randomDoubleZero);
        assertEquals(1. / 4, loc.cx, EPSILON);
        assertEquals(3. / 4, loc.cy, EPSILON);
        assertEquals(4. / 4, loc.cz, EPSILON);
        assertEquals(8. / 3, split.cx, EPSILON);
        assertEquals(1. / 3, split.cy, EPSILON);
        assertEquals(3. / 3, split.cz, EPSILON);
    }
    
    @Test
    public void split_noOffsetsNoDirection_splitsVoxelsCorrectly() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        
        // Create a 2x2x2 cuboid of voxels
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                for (int z = 0; z < 2; z++) {
                    voxels.add(new Voxel(x, y, z));
                }
            }
        }
        
        ArrayList<Voxel> splitVoxels = new ArrayList<>();
        ArrayList<Voxel> locVoxels = new ArrayList<>();
        for (Voxel voxel : voxels) {
            if (voxel.y >= 1) {
                splitVoxels.add(voxel);
            } else {
                locVoxels.add(voxel);
            }
        }
        
        PottsLocation loc = new PottsLocationMock(voxels);
        PottsLocation split = (PottsLocation) loc.split(randomDoubleZero);
        
        locVoxels.sort(VOXEL_COMPARATOR);
        loc.voxels.sort(VOXEL_COMPARATOR);
        splitVoxels.sort(VOXEL_COMPARATOR);
        split.voxels.sort(VOXEL_COMPARATOR);
        
        assertEquals(locVoxels, loc.voxels);
        assertEquals(splitVoxels, split.voxels);
    }
    
    @Test
    public void split_withOffsetsNoDirection_splitsVoxelsCorrectly() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        ArrayList<Voxel> locVoxels = new ArrayList<>();
        ArrayList<Voxel> splitVoxels = new ArrayList<>();
        
        // Create a 5x5x5 cuboid of voxels
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                for (int z = 0; z < 5; z++) {
                    voxels.add(new Voxel(x, y, z));
                }
            }
        }
        
        ArrayList<Integer> offsets = new ArrayList<>();
        offsets.add(20); // split centered at x = 1
        offsets.add(20); // split centered at y = 1
        offsets.add(20); // split centered at x = 1
        
        for (Voxel voxel : voxels) {
            if (voxel.y >= 1) {
                splitVoxels.add(voxel);
            } else {
                locVoxels.add(voxel);
            }
        }
        
        PottsLocation location = new PottsLocationMock(voxels);
        PottsLocation splitLocation = (PottsLocation) location.split(randomDoubleZero, offsets);
        
        locVoxels.sort(VOXEL_COMPARATOR);
        location.voxels.sort(VOXEL_COMPARATOR);
        splitVoxels.sort(VOXEL_COMPARATOR);
        splitLocation.voxels.sort(VOXEL_COMPARATOR);
        
        assertEquals(locVoxels, location.voxels);
        assertEquals(splitVoxels, splitLocation.voxels);
    }
    
    @Test
    public void split_withOffsetsWithDirectionWithProbability_splitsVoxelsCorrectly() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        ArrayList<Voxel> locVoxels = new ArrayList<>();
        ArrayList<Voxel> splitVoxels = new ArrayList<>();
        
        // Create a 5x5x5 cuboid of voxels
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                for (int z = 0; z < 5; z++) {
                    voxels.add(new Voxel(x, y, z));
                }
            }
        }
        
        ArrayList<Integer> offsets = new ArrayList<>();
        offsets.add(50); // split centered at x = 2
        offsets.add(75); // split centered at y = 3
        offsets.add(100); // split centered at z = 1
        
        for (Voxel voxel : voxels) {
            if (voxel.x - voxel.y + 1 > 0) {
                locVoxels.add(voxel);
            } else if ((voxel.x - voxel.y + 1) <= 0) {
                splitVoxels.add(voxel);
            } else {
                // distance == 0
                // Since randomDoubleZero.nextDouble() == 0.0, and 0.0 > 0.5 is false
                splitVoxels.add(voxel);
            }
        }
        
        PottsLocation location = new PottsLocationMock(voxels);
        double probability = 0.5;
        PottsLocation splitLocation = (PottsLocation) location.split(randomDoubleZero, offsets,
                                                                     Direction.POSITIVE_XY, probability);
        
        locVoxels.sort(VOXEL_COMPARATOR);
        location.voxels.sort(VOXEL_COMPARATOR);
        splitVoxels.sort(VOXEL_COMPARATOR);
        splitLocation.voxels.sort(VOXEL_COMPARATOR);
        
        assertEquals(locVoxels, location.voxels);
        assertEquals(splitVoxels, splitLocation.voxels);
    }
    
    @Test
    public void split_withOffsetsWithXYDirectionWithProbability_splitsVoxelsCorrectly() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    voxels.add(new Voxel(i, j, k));
                }
            }
        }
        
        ArrayList<Voxel> locVoxels = new ArrayList<>();
        locVoxels.add(new Voxel(0, 2, 0));
        locVoxels.add(new Voxel(0, 2, 1));
        locVoxels.add(new Voxel(0, 2, 2));
        ArrayList<Voxel> splitVoxels = new ArrayList<>(voxels);
        splitVoxels.removeAll(locVoxels);
        
        PottsLocation loc = new PottsLocationMock(voxels);
        Direction direction = Direction.POSITIVE_XY;
        double probability = 0.0;
        
        ArrayList<Integer> offsets = new ArrayList<>(Arrays.asList(0, 100, 50));
        PottsLocation split = (PottsLocation) loc.split(randomDoubleZero, offsets, direction, probability);
        
        locVoxels.sort(VOXEL_COMPARATOR);
        loc.voxels.sort(VOXEL_COMPARATOR);
        splitVoxels.sort(VOXEL_COMPARATOR);
        split.voxels.sort(VOXEL_COMPARATOR);
        
        assertEquals(locVoxels, loc.voxels);
        assertEquals(splitVoxels, split.voxels);
    }
    
    @Test
    public void split_withOffsetsWithZXDirectionWithProbability_splitsVoxelsCorrectly() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    voxels.add(new Voxel(i, j, k));
                }
            }
        }
        
        ArrayList<Voxel> splitVoxels = new ArrayList<>();
        splitVoxels.add(new Voxel(0, 0, 2));
        splitVoxels.add(new Voxel(0, 1, 2));
        splitVoxels.add(new Voxel(0, 2, 2));
        ArrayList<Voxel> locVoxels = new ArrayList<>(voxels);
        locVoxels.removeAll(splitVoxels);
        
        PottsLocation loc = new PottsLocationMock(voxels);
        Direction direction = Direction.POSITIVE_ZX;
        double probability = 0.0;
        
        ArrayList<Integer> offsets = new ArrayList<>(Arrays.asList(0, 100, 50));
        PottsLocation split = (PottsLocation) loc.split(randomDoubleZero, offsets, direction, probability);
        
        locVoxels.sort(VOXEL_COMPARATOR);
        loc.voxels.sort(VOXEL_COMPARATOR);
        splitVoxels.sort(VOXEL_COMPARATOR);
        split.voxels.sort(VOXEL_COMPARATOR);
        
        assertEquals(locVoxels, loc.voxels);
        assertEquals(splitVoxels, split.voxels);
    }
    
    @Test
    public void split_withOddAnglePlaneThroughPoint_splitsVoxelsCorrectly() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        ArrayList<Voxel> locVoxels = new ArrayList<>();
        ArrayList<Voxel> splitVoxels = new ArrayList<>();
        
        // Create a 3x3x3 grid of voxels (total of 27 voxels)
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                for (int z = 0; z < 3; z++) {
                    voxels.add(new Voxel(x, y, z));
                }
            }
        }
        
        // Plane with normal vector (1, 2, 3) passing through point (1, 2, 1)
        // Plane equation: x + 2y + 3z = 8
        Voxel planePoint = new Voxel(1, 2, 1);
        Int3D normalVector = new Int3D(1, 2, 3);
        Plane plane = new Plane(planePoint, normalVector);
        
        for (Voxel voxel : voxels) {
            int value = voxel.x + 2 * voxel.y + 3 * voxel.z;
            if (value < 8) {
                locVoxels.add(voxel);
            } else {
                splitVoxels.add(voxel);
            }
        }
        
        PottsLocation loc = new PottsLocationMock(voxels);
        double probability = 1.0;
        
        PottsLocation split = (PottsLocation) loc.split(randomDoubleZero, plane, probability);
        
        locVoxels.sort(VOXEL_COMPARATOR);
        loc.voxels.sort(VOXEL_COMPARATOR);
        splitVoxels.sort(VOXEL_COMPARATOR);
        split.voxels.sort(VOXEL_COMPARATOR);
        
        assertEquals(locVoxels, loc.voxels);
        assertEquals(splitVoxels, split.voxels);
    }
}
