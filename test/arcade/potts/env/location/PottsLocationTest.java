package arcade.potts.env.location;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import arcade.potts.util.PottsEnums.Direction;
import arcade.potts.util.PottsEnums.Region;
import ec.util.MersenneTwisterFast;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
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
    public void getSplitpoint_noOffsets_returnsCenter() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(0, 1, 1));
        voxels.add(new Voxel(1, 1, 2));
        voxels.add(new Voxel(2, 2, 2));
        voxels.add(new Voxel(2, 3, 3));
        PottsLocationMock loc = new PottsLocationMock(voxels);
        
        assertEquals(5 / 4., loc.cx, EPSILON);
        assertEquals(7 / 4., loc.cy, EPSILON);
        assertEquals(8 / 4., loc.cz, EPSILON);
        assertEquals(new Voxel(1, 2, 2), loc.getSplitpoint());
    }
    
    @Test
    public void getSplitpoint_50percentOffsets_returnsValue() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(0, 1, 1));
        voxels.add(new Voxel(1, 1, 2));
        voxels.add(new Voxel(2, 2, 2));
        voxels.add(new Voxel(2, 3, 3));
        PottsLocationMock loc = new PottsLocationMock(voxels);
        
        assertEquals(5 / 4., loc.cx, EPSILON);
        assertEquals(7 / 4., loc.cy, EPSILON);
        assertEquals(8 / 4., loc.cz, EPSILON);
        ArrayList<Integer> offsets = new ArrayList<>();
        offsets.add(50);
        offsets.add(50);
        offsets.add(50);
        assertEquals(new Voxel(1, 2, 2), loc.getSplitpoint(offsets));
    }
    
    @Test
    public void getSplitpoint_33percentOffsets_returnsValue() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(0, 1, 1));
        voxels.add(new Voxel(1, 1, 2));
        voxels.add(new Voxel(2, 2, 2));
        voxels.add(new Voxel(2, 3, 3));
        PottsLocationMock loc = new PottsLocationMock(voxels);

        assertEquals(5 / 4., loc.cx, EPSILON);
        assertEquals(7 / 4., loc.cy, EPSILON);
        assertEquals(8 / 4., loc.cz, EPSILON);

        ArrayList<Integer> offsets = new ArrayList<>();
        offsets.add(33);
        offsets.add(33);
        offsets.add(33);

        int expectedX = (int) Math.round(0 + (2 - 0) * (33 / 100.0)); // 33% of the range [0, 2]
        int expectedY = (int) Math.round(1 + (3 - 1) * (33 / 100.0)); // 33% of the range [1, 3]
        int expectedZ = (int) Math.round(1 + (3 - 1) * (33 / 100.0)); // 33% of the range [1, 3]

        assertEquals(new Voxel(expectedX, expectedY, expectedZ), loc.getSplitpoint(offsets));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void getSplitpoint_invalidOffsetPercentLength_raisesException() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(0, 1, 1));
        voxels.add(new Voxel(1, 1, 2));
        voxels.add(new Voxel(2, 2, 2));
        voxels.add(new Voxel(2, 3, 3));
        PottsLocationMock loc = new PottsLocationMock(voxels);
    
        ArrayList<Integer> wrongOffsets = new ArrayList<>();
        wrongOffsets.add(50); // Only one element
    
        loc.getSplitpoint(wrongOffsets);
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
    public void splitVoxels_invalidDirection_doesNothing() {
        ArrayList<Voxel> voxels = prepSplit();
        PottsLocationMock loc = new PottsLocationMock(voxels);
        ArrayList<Voxel> voxelsA = new ArrayList<>();
        ArrayList<Voxel> voxelsB = new ArrayList<>();
        
        PottsLocation.splitVoxels(Direction.UNDEFINED, voxels, voxelsA, voxelsB, loc.getCenter(), randomDoubleZero);
        assertEquals(0, voxelsA.size());
        assertEquals(0, voxelsB.size());
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
        
        PottsLocation.splitVoxels(Direction.YZ_PLANE, voxels, voxelsA, voxelsB, loc.getCenter(), randomDoubleZero);
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
        
        PottsLocation.splitVoxels(Direction.ZX_PLANE, voxels, voxelsA, voxelsB, loc.getCenter(), randomDoubleZero);
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
        
        PottsLocation.splitVoxels(Direction.XY_PLANE, voxels, voxelsA, voxelsB, loc.getCenter(), randomDoubleZero);
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
        
        PottsLocation.splitVoxels(Direction.NEGATIVE_XY, voxels, voxelsA, voxelsB, loc.getCenter(), randomDoubleZero);
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
        
        PottsLocation.splitVoxels(Direction.POSITIVE_XY, voxels, voxelsA, voxelsB, loc.getCenter(), randomDoubleZero);
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
        
        PottsLocation.splitVoxels(Direction.NEGATIVE_YZ, voxels, voxelsA, voxelsB, loc.getCenter(), randomDoubleZero);
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
        
        PottsLocation.splitVoxels(Direction.POSITIVE_YZ, voxels, voxelsA, voxelsB, loc.getCenter(), randomDoubleZero);
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
        
        PottsLocation.splitVoxels(Direction.NEGATIVE_ZX, voxels, voxelsA, voxelsB, loc.getCenter(), randomDoubleZero);
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
        
        PottsLocation.splitVoxels(Direction.POSITIVE_ZX, voxels, voxelsA, voxelsB, loc.getCenter(), randomDoubleZero);
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
        
        PottsLocation.splitVoxels(Direction.YZ_PLANE, voxels, voxelsA, voxelsB, loc.getCenter(), randomDoubleOne);
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
        
        PottsLocation.splitVoxels(Direction.ZX_PLANE, voxels, voxelsA, voxelsB, loc.getCenter(), randomDoubleOne);
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
        
        PottsLocation.splitVoxels(Direction.XY_PLANE, voxels, voxelsA, voxelsB, loc.getCenter(), randomDoubleOne);
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
        
        PottsLocation.splitVoxels(Direction.NEGATIVE_XY, voxels, voxelsA, voxelsB, loc.getCenter(), randomDoubleOne);
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
        
        PottsLocation.splitVoxels(Direction.POSITIVE_XY, voxels, voxelsA, voxelsB, loc.getCenter(), randomDoubleOne);
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
        
        PottsLocation.splitVoxels(Direction.NEGATIVE_YZ, voxels, voxelsA, voxelsB, loc.getCenter(), randomDoubleOne);
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
        
        PottsLocation.splitVoxels(Direction.POSITIVE_YZ, voxels, voxelsA, voxelsB, loc.getCenter(), randomDoubleOne);
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
        
        PottsLocation.splitVoxels(Direction.NEGATIVE_ZX, voxels, voxelsA, voxelsB, loc.getCenter(), randomDoubleOne);
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
        
        PottsLocation.splitVoxels(Direction.POSITIVE_ZX, voxels, voxelsA, voxelsB, loc.getCenter(), randomDoubleOne);
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
    public void split_noOffsets_callsPerformSplit() {
        PottsLocation spy = spy(new PottsLocationMock(voxelListAB));
        doCallRealMethod().when(spy).performSplit(any(MersenneTwisterFast.class), any(Voxel.class), any(Direction.class), anyBoolean());
        spy.split(randomDoubleZero);
        verify(spy).getSplitpoint();
        verify(spy).performSplit(eq(randomDoubleZero), Mockito.any(Voxel.class), eq(null), eq(true));
    }
    
    @Test
    public void split_withOffsets_callsPerformSplit() {
        ArrayList<Integer> offsets = new ArrayList<>(Arrays.asList(50, 50, 50));
        PottsLocation spy = spy(new PottsLocationMock(voxelListAB));
        doCallRealMethod().when(spy).performSplit(any(MersenneTwisterFast.class), any(Voxel.class), any(Direction.class), anyBoolean());
        spy.split(randomDoubleZero, offsets);
        verify(spy).getSplitpoint(offsets);
        verify(spy).performSplit(eq(randomDoubleZero), Mockito.any(Voxel.class), eq(null), eq(false));    }
    
    @Test
    public void performSplit_noOffsets_splitsVoxelsCorrectly() {
        PottsLocation location = new PottsLocationMock(voxelListAB);
        MersenneTwisterFast random = new MersenneTwisterFast(12345);
        Voxel splitpoint = location.getSplitpoint();
        PottsLocation splitLocation = (PottsLocation) location.performSplit(random, splitpoint, null, true);
        assertNotNull(splitLocation);
        assertTrue(location.voxels.size() > 0);
        assertTrue(splitLocation.voxels.size() > 0);
        // Ensure location.voxels.size() is roughly equal to splitLocation.voxels.size()
        assertTrue(Math.abs(location.voxels.size() - splitLocation.voxels.size()) <= 1);
        assertEquals(voxelListAB.size(), location.voxels.size() + splitLocation.voxels.size());
    }
    
    @Test
    public void performSplit_withOffsets_splitsVoxelsCorrectly() {
        PottsLocation location = new PottsLocationMock(voxelListAB);
        MersenneTwisterFast random = new MersenneTwisterFast(12345);
        // Add two voxels to the location bringing total to 9
        location.add(3, 3, 3);
        location.add(4, 4, 4);
        ArrayList<Integer> offsets = new ArrayList<>(Arrays.asList(33, 33, 33)); // 33% offsets
        Voxel splitpoint = location.getSplitpoint(offsets);
        PottsLocation splitLocation = (PottsLocation) location.performSplit(random, splitpoint, null, false);
        assertNotNull(splitLocation);
        assertTrue(location.voxels.size() > 0);
        assertTrue(splitLocation.voxels.size() > 0);
        assertEquals(voxelListAB.size() + 2, location.voxels.size() + splitLocation.voxels.size());
        // Check that one location is approximately 1/3 the size of the other
        int locationSize = location.voxels.size();
        int splitLocationSize = splitLocation.voxels.size();
        int totalSize = locationSize + splitLocationSize;
        double expectedOneThird = totalSize / 3.0;
        double expectedTwoThirds = 2 * totalSize / 3.0;
        boolean locationIsOneThird = Math.abs(locationSize - expectedOneThird) <= 1 && Math.abs(splitLocationSize - expectedTwoThirds) <= 1;
        boolean locationIsTwoThirds = Math.abs(locationSize - expectedTwoThirds) <= 1 && Math.abs(splitLocationSize - expectedOneThird) <= 1;
        assertTrue(locationIsOneThird || locationIsTwoThirds);
    }
}
