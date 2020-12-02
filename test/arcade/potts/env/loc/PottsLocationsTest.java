package arcade.potts.env.loc;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.*;
import ec.util.MersenneTwisterFast;
import static arcade.core.util.Enums.Region;
import static arcade.potts.util.PottsEnums.Direction;
import static arcade.potts.env.loc.Voxel.VOXEL_COMPARATOR;
import static arcade.potts.env.loc.PottsLocationTest.*;
import static arcade.core.TestUtilities.*;

public class PottsLocationsTest {
    static MersenneTwisterFast randomDoubleZero, randomDoubleOne;
    static ArrayList<Voxel> voxelListForVolumeSurface;
    static ArrayList<Voxel> voxelListForMultipleRegionsA, voxelListForMultipleRegionsB, voxelListForMultipleRegions;
    static ArrayList<Voxel> voxelListForAddRemove;
    static ArrayList<Voxel> voxelListForRegionAddRemove;
    static ArrayList<Voxel> voxelListSingle;
    static ArrayList<Voxel> voxelListDouble;
    static ArrayList<Voxel> voxelListA, voxelListB, voxelListAB;
    final static int LOCATIONS_SURFACE = (int)(Math.random()*100);
    
    @BeforeClass
    public static void setupMocks() {
        randomDoubleZero = mock(MersenneTwisterFast.class);
        when(randomDoubleZero.nextDouble()).thenReturn(0.0);
        
        randomDoubleOne = mock(MersenneTwisterFast.class);
        when(randomDoubleOne.nextDouble()).thenReturn(1.0);
    }
    
    @BeforeClass
    public static void setupLists() {
        voxelListForVolumeSurface = new ArrayList<>();
        voxelListForVolumeSurface.add(new Voxel(0, 0, 0));
        
        voxelListForMultipleRegionsA = new ArrayList<>();
        voxelListForMultipleRegionsA.add(new Voxel(0, 0, 0));
        voxelListForMultipleRegionsA.add(new Voxel(0, 1, 0));
        voxelListForMultipleRegionsA.add(new Voxel(0, 2, 0));
        voxelListForMultipleRegionsA.add(new Voxel(1, 0, 0));
        
        voxelListForMultipleRegionsB = new ArrayList<>();
        voxelListForMultipleRegionsB.add(new Voxel(1, 2, 0));
        voxelListForMultipleRegionsB.add(new Voxel(2, 0, 0));
        voxelListForMultipleRegionsB.add(new Voxel(2, 1, 0));
        voxelListForMultipleRegionsB.add(new Voxel(2, 2, 0));
        
        voxelListForMultipleRegions = new ArrayList<>();
        voxelListForMultipleRegions.addAll(voxelListForMultipleRegionsA);
        voxelListForMultipleRegions.addAll(voxelListForMultipleRegionsB);
        
        voxelListForAddRemove = new ArrayList<>();
        voxelListForAddRemove.add(new Voxel(0, 0, 0));
        voxelListForAddRemove.add(new Voxel(1, 0, 0));
        
        voxelListForRegionAddRemove = new ArrayList<>();
        voxelListForRegionAddRemove.add(new Voxel(1, 1, 0));
        
        voxelListSingle = new ArrayList<>();
        voxelListSingle.add(new Voxel(0, 0, 0));
        
        voxelListDouble = new ArrayList<>();
        voxelListDouble.add(new Voxel(0, 0, 0));
        voxelListDouble.add(new Voxel(1, 1, 0));
        
        voxelListA = new ArrayList<>();
        voxelListA.add(new Voxel(0, 0, 0));
        voxelListA.add(new Voxel(0, 1, 0));
        voxelListA.add(new Voxel(1, 0, 0));
        voxelListA.add(new Voxel(0, 2, 0));
        
        voxelListB = new ArrayList<>();
        voxelListB.add(new Voxel(2, 0, 0));
        voxelListB.add(new Voxel(3, 0, 0));
        voxelListB.add(new Voxel(3, 1, 0));
        
        voxelListAB = new ArrayList<>(voxelListA);
        voxelListAB.addAll(voxelListB);
    }
    
    static class PottsLocationsMock extends PottsLocations {
        public PottsLocationsMock(ArrayList<Voxel> voxels) { super(voxels); }
        
        PottsLocation makeLocation(ArrayList<Voxel> voxels) { return new PottsLocationMock(voxels); }
        
        PottsLocations makeLocations(ArrayList<Voxel> voxels) { return new PottsLocationsMock(voxels); }
        
        public double convertVolume(double volume) { return 0; }
        
        int calculateSurface() { return LOCATIONS_SURFACE; }
        
        int updateSurface(Voxel voxel) { return 1; }
        
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
        
        HashMap<Direction, Integer> getDiameters() {
            HashMap<Direction, Integer> diameters = new HashMap<>();
            
            if (voxels.size() == 0) {
                diameters.put(Direction.XY_PLANE, 1);
                diameters.put(Direction.POSITIVE_XY, 2);
                diameters.put(Direction.NEGATIVE_ZX, 3);
            }
            else if (voxels.size() == 7) {
                diameters.put(Direction.YZ_PLANE, 1);
            }
            else {
                diameters.put(Direction.XY_PLANE, 1);
                diameters.put(Direction.POSITIVE_XY, 1);
                diameters.put(Direction.NEGATIVE_ZX, 1);
            }
            
            return diameters;
        }
        
        Direction getSlice(Direction direction, HashMap<Direction, Integer> diameters) {
            switch (direction) {
                case XY_PLANE: return Direction.NEGATIVE_YZ;
                case POSITIVE_XY: return Direction.YZ_PLANE;
                case NEGATIVE_ZX: return Direction.POSITIVE_YZ;
                case YZ_PLANE: return Direction.ZX_PLANE;
            }
            return null;
        }
        
        ArrayList<Voxel> getSelected(Voxel center, double n) { return new ArrayList<>(); }
    }
    
    @Test
    public void getRegions_regionsNotAssigned_returnsOne() {
        PottsLocationsMock loc = new PottsLocationsMock(new ArrayList<>());
        assertEquals(1, loc.getRegions().size());
        assertTrue(loc.getRegions().contains(Region.DEFAULT));
    }
    
    @Test
    public void getRegions_regionsAssigned_returnsGiven() {
        PottsLocationsMock loc = new PottsLocationsMock(new ArrayList<>());
        loc.add(Region.UNDEFINED, 1, 0, 0);
        loc.add(Region.NUCLEUS, 2, 0, 0);
        assertEquals(3, loc.getRegions().size());
        assertTrue(loc.getRegions().contains(Region.DEFAULT));
        assertTrue(loc.getRegions().contains(Region.UNDEFINED));
        assertTrue(loc.getRegions().contains(Region.NUCLEUS));
    }
    
    @Test
    public void getVolume_validRegion_returnsValue() {
        PottsLocationsMock loc = new PottsLocationsMock(voxelListForVolumeSurface);
        assertEquals(1, loc.getVolume());
        assertEquals(1, loc.getVolume(Region.DEFAULT));
    }
    
    @Test
    public void getVolume_invalidRegion_returnsZero() {
        PottsLocationsMock loc = new PottsLocationsMock(voxelListForVolumeSurface);
        assertEquals(0, loc.getVolume(null));
    }
    
    @Test
    public void getVolume_multipleRegions_returnsValue() {
        PottsLocationsMock loc = new PottsLocationsMock(voxelListForMultipleRegions);
        loc.add(Region.UNDEFINED, 1, 1, 0);
        assertEquals(9, loc.getVolume());
        assertEquals(8, loc.getVolume(Region.DEFAULT));
        assertEquals(1, loc.getVolume(Region.UNDEFINED));
    }
    
    @Test
    public void getSurface_validRegion_returnsValue() {
        PottsLocationsMock loc = new PottsLocationsMock(voxelListForVolumeSurface);
        assertEquals(LOCATIONS_SURFACE, loc.getSurface());
        assertEquals(LOCATION_SURFACE, loc.getSurface(Region.DEFAULT));
    }
    
    @Test
    public void getSurface_invalidRegion_returnsZero() {
        PottsLocationsMock loc = new PottsLocationsMock(voxelListForVolumeSurface);
        assertEquals(0, loc.getSurface(null));
    }
    
    @Test
    public void getSurface_multipleRegions_returnsValue() {
        PottsLocationsMock loc = new PottsLocationsMock(voxelListForMultipleRegions);
        loc.add(Region.UNDEFINED, 1, 1, 0);
        assertEquals(LOCATIONS_SURFACE + 1, loc.getSurface());
        assertEquals(LOCATION_SURFACE, loc.getSurface(Region.DEFAULT));
        assertEquals(LOCATION_SURFACE + 1, loc.getSurface(Region.UNDEFINED));
    }
    
    @Test
    public void add_newVoxelNoRegion_updatesLists() {
        PottsLocationsMock loc = new PottsLocationsMock(new ArrayList<>());
        loc.add(0, 0, 0);
        loc.add(1, 0, 0);
        assertEquals(voxelListForAddRemove, loc.voxels);
        assertEquals(voxelListForAddRemove, loc.locations.get(Region.DEFAULT).voxels);
    }
    
    @Test
    public void add_newVoxelNoRegion_updatesVolumes() {
        PottsLocationsMock loc = new PottsLocationsMock(new ArrayList<>());
        loc.add(0, 0, 0);
        loc.add(1, 0, 0);
        assertEquals(2, loc.volume);
        assertEquals(2, loc.locations.get(Region.DEFAULT).volume);
    }
    
    @Test
    public void add_newVoxelNoRegion_updatesSurfaces() {
        PottsLocationsMock loc = new PottsLocationsMock(new ArrayList<>());
        loc.add(0, 0, 0);
        loc.add(1, 0, 0);
        assertEquals(LOCATIONS_SURFACE + 2, loc.surface);
        assertEquals(LOCATION_SURFACE + 2, loc.locations.get(Region.DEFAULT).surface);
    }
    
    @Test
    public void add_existingVoxelNoRegion_doesNothing() {
        PottsLocationsMock loc = new PottsLocationsMock(new ArrayList<>());
        loc.add(0, 0, 0);
        loc.add(1, 0, 0);
        loc.add(0, 0, 0);
        assertEquals(voxelListForAddRemove, loc.voxels);
        assertEquals(voxelListForAddRemove, loc.locations.get(Region.DEFAULT).voxels);
    }
    
    @Test
    public void add_newVoxelWithRegion_createsLists() {
        PottsLocationsMock loc = new PottsLocationsMock(voxelListSingle);
        loc.add(Region.UNDEFINED, 1, 1, 0);
        assertEquals(voxelListDouble, loc.voxels);
        assertEquals(voxelListSingle, loc.locations.get(Region.DEFAULT).voxels);
        assertEquals(voxelListForRegionAddRemove, loc.locations.get(Region.UNDEFINED).voxels);
    }
    
    @Test
    public void add_newVoxelWithRegion_updatesVolumes() {
        PottsLocationsMock loc = new PottsLocationsMock(voxelListSingle);
        loc.add(Region.UNDEFINED, 1, 1, 0);
        assertEquals(2, loc.volume);
        assertEquals(1, loc.locations.get(Region.DEFAULT).volume);
        assertEquals(1, loc.locations.get(Region.UNDEFINED).volume);
    }
    
    @Test
    public void add_newVoxelWithRegion_updatesSurfaces() {
        PottsLocationsMock loc = new PottsLocationsMock(voxelListSingle);
        loc.add(Region.UNDEFINED, 1, 1, 0);
        assertEquals(LOCATIONS_SURFACE + 1, loc.surface);
        assertEquals(LOCATION_SURFACE, loc.locations.get(Region.DEFAULT).surface);
        assertEquals(LOCATION_SURFACE + 1, loc.locations.get(Region.UNDEFINED).surface);
    }
    
    @Test
    public void add_existingVoxelWithRegion_doesNothing() {
        PottsLocationsMock loc = new PottsLocationsMock(voxelListSingle);
        loc.add(Region.UNDEFINED,0, 0, 0);
        assertEquals(voxelListSingle, loc.voxels);
        assertEquals(voxelListSingle, loc.locations.get(Region.DEFAULT).voxels);
        assertNull(loc.locations.get(Region.UNDEFINED));
    }
    
    @Test
    public void remove_existingVoxelNoRegion_updatesList() {
        PottsLocationsMock loc = new PottsLocationsMock(voxelListForAddRemove);
        ArrayList<Voxel> voxelsRemoved = new ArrayList<>();
        voxelsRemoved.add(new Voxel(1, 0, 0));
        loc.remove(0, 0, 0);
        assertEquals(voxelsRemoved, loc.voxels);
        assertEquals(voxelsRemoved, loc.locations.get(Region.DEFAULT).voxels);
    }
    
    @Test
    public void remove_existingVoxelNoRegion_updatesVolume() {
        PottsLocationsMock loc = new PottsLocationsMock(voxelListForAddRemove);
        loc.remove(0, 0, 0);
        assertEquals(1, loc.volume);
        assertEquals(1, loc.locations.get(Region.DEFAULT).volume);
    }
    
    @Test
    public void remove_existingVoxelNoRegion_updatesSurface() {
        PottsLocationsMock loc = new PottsLocationsMock(voxelListForAddRemove);
        loc.remove(0, 0, 0);
        assertEquals(LOCATIONS_SURFACE - 1, loc.surface);
        assertEquals(LOCATION_SURFACE - 1, loc.locations.get(Region.DEFAULT).surface);
    }
    
    @Test
    public void remove_allVoxelsNoRegion_returnsEmptyList() {
        PottsLocationsMock loc = new PottsLocationsMock(voxelListForAddRemove);
        loc.remove(0, 0, 0);
        loc.remove(1, 0, 0);
        assertEquals(new ArrayList<>(), loc.voxels);
        assertEquals(new ArrayList<>(), loc.locations.get(Region.DEFAULT).voxels);
    }
    
    @Test
    public void remove_missingVoxelNoRegion_doesNothing() {
        PottsLocationsMock loc = new PottsLocationsMock(new ArrayList<>());
        loc.remove(0, 0, 0);
        assertEquals(new ArrayList<>(), loc.voxels);
        assertEquals(new ArrayList<>(), loc.locations.get(Region.DEFAULT).voxels);
    }
    
    @Test
    public void remove_existingVoxelWithRegion_updatesList() {
        PottsLocationsMock loc = new PottsLocationsMock(voxelListForRegionAddRemove);
        loc.add(Region.UNDEFINED,0, 0, 0);
        loc.add(Region.UNDEFINED, 1, 0, 0);
        
        ArrayList<Voxel> voxelsRemoved = new ArrayList<>();
        voxelsRemoved.add(new Voxel(1, 1, 0));
        voxelsRemoved.add(new Voxel(1, 0, 0));
        
        ArrayList<Voxel> voxelsRemoved1 = new ArrayList<>();
        voxelsRemoved1.add(new Voxel(1, 1, 0));
        
        ArrayList<Voxel> voxelsRemoved2 = new ArrayList<>();
        voxelsRemoved2.add(new Voxel(1, 0, 0));
        
        loc.remove(Region.UNDEFINED, 0, 0, 0);
        
        assertEquals(voxelsRemoved, loc.voxels);
        assertEquals(voxelsRemoved1, loc.locations.get(Region.DEFAULT).voxels);
        assertEquals(voxelsRemoved2, loc.locations.get(Region.UNDEFINED).voxels);
    }
    
    @Test
    public void remove_existingVoxelWithRegion_updatesVolume() {
        PottsLocationsMock loc = new PottsLocationsMock(voxelListForRegionAddRemove);
        loc.add(Region.UNDEFINED,0, 0, 0);
        loc.add(Region.UNDEFINED, 1, 0, 0);
        loc.remove(Region.UNDEFINED, 0, 0, 0);
        assertEquals(2, loc.volume);
        assertEquals(1, loc.locations.get(Region.DEFAULT).volume);
        assertEquals(1, loc.locations.get(Region.UNDEFINED).volume);
    }
    
    @Test
    public void remove_existingVoxelWithRegion_updatesSurface() {
        PottsLocationsMock loc = new PottsLocationsMock(voxelListForRegionAddRemove);
        loc.add(Region.UNDEFINED,0, 0, 0);
        loc.add(Region.UNDEFINED, 1, 0, 0);
        loc.remove(Region.UNDEFINED, 0, 0, 0);
        assertEquals(LOCATIONS_SURFACE + 2 - 1, loc.surface);
        assertEquals(LOCATION_SURFACE, loc.locations.get(Region.DEFAULT).surface);
        assertEquals(LOCATION_SURFACE + 2 - 1, loc.locations.get(Region.UNDEFINED).surface);
    }
    
    @Test
    public void remove_alternateVoxelWithRegion_doesNothing() {
        PottsLocationsMock loc = new PottsLocationsMock(voxelListForRegionAddRemove);
        loc.add(Region.UNDEFINED,0, 0, 0);
        loc.add(Region.UNDEFINED, 1, 0, 0);
        
        ArrayList<Voxel> voxelsRemoved = new ArrayList<>();
        voxelsRemoved.add(new Voxel(1, 1, 0));
        voxelsRemoved.add(new Voxel(0, 0, 0));
        voxelsRemoved.add(new Voxel(1, 0, 0));
        
        ArrayList<Voxel> voxelsRemoved1 = new ArrayList<>();
        voxelsRemoved1.add(new Voxel(1, 1, 0));
        
        ArrayList<Voxel> voxelsRemoved2 = new ArrayList<>();
        voxelsRemoved2.add(new Voxel(0, 0, 0));
        voxelsRemoved2.add(new Voxel(1, 0, 0));
        
        loc.remove(Region.UNDEFINED, 1, 1, 0);
        
        assertEquals(voxelsRemoved, loc.voxels);
        assertEquals(voxelsRemoved1, loc.locations.get(Region.DEFAULT).voxels);
        assertEquals(voxelsRemoved2, loc.locations.get(Region.UNDEFINED).voxels);
    }
    
    @Test
    public void remove_allVoxelsWithRegion_returnsEmptyList() {
        PottsLocationsMock loc = new PottsLocationsMock(voxelListForRegionAddRemove);
        loc.add(Region.UNDEFINED,0, 0, 0);
        loc.add(Region.UNDEFINED, 1, 0, 0);
        loc.remove(0, 0, 0);
        loc.remove(1, 0, 0);
        assertEquals(voxelListForRegionAddRemove, loc.voxels);
        assertEquals(voxelListForRegionAddRemove, loc.locations.get(Region.DEFAULT).voxels);
        assertEquals(new ArrayList<>(), loc.locations.get(Region.UNDEFINED).voxels);
    }
    
    @Test
    public void remove_missingVoxelWithRegion_doesNothing() {
        PottsLocationsMock loc = new PottsLocationsMock(voxelListForRegionAddRemove);
        loc.remove(Region.UNDEFINED, 0, 0, 0);
        assertEquals(voxelListForRegionAddRemove, loc.voxels);
        assertEquals(voxelListForRegionAddRemove, loc.locations.get(Region.DEFAULT).voxels);
        assertNull(loc.locations.get(Region.UNDEFINED));
    }
    
    @Test
    public void assign_existingVoxelSameRegion_doesNothing() {
        PottsLocationsMock location = new PottsLocationsMock(new ArrayList<>());
        ArrayList<Voxel> voxelDefault = new ArrayList<>();
        ArrayList<Voxel> voxelAdditional = new ArrayList<>();
        
        voxelDefault.add(new Voxel(0, 0, 0));
        voxelAdditional.add(new Voxel(1, 0, 0));
        
        location.add(Region.DEFAULT, 0, 0, 0);
        location.add(Region.UNDEFINED, 1, 0, 0);
        
        location.assign(Region.DEFAULT, new Voxel(0, 0, 0));
        assertEquals(voxelDefault, location.locations.get(Region.DEFAULT).voxels);
        assertEquals(voxelAdditional, location.locations.get(Region.UNDEFINED).voxels);
    }
    
    @Test
    public void assign_existingVoxelDifferentRegion_updatesRegions() {
        PottsLocationsMock location = new PottsLocationsMock(new ArrayList<>());
        ArrayList<Voxel> voxelDefault = new ArrayList<>();
        ArrayList<Voxel> voxelAdditional = new ArrayList<>();
        ArrayList<Voxel> voxelUpdated = new ArrayList<>();
        
        voxelDefault.add(new Voxel(0, 0, 0));
        voxelAdditional.add(new Voxel(1, 0, 0));
        
        voxelUpdated.addAll(voxelAdditional);
        voxelUpdated.addAll(voxelDefault);
        
        location.add(Region.DEFAULT, 0, 0, 0);
        location.add(Region.UNDEFINED, 1, 0, 0);
        location.assign(Region.UNDEFINED, new Voxel(0, 0, 0));
        
        assertEquals(new ArrayList<>(), location.locations.get(Region.DEFAULT).voxels);
        assertEquals(voxelUpdated, location.locations.get(Region.UNDEFINED).voxels);
    }
    
    @Test
    public void assign_existingVoxelDifferentRegion_updatesVolumes() {
        PottsLocationsMock location = new PottsLocationsMock(new ArrayList<>());
        location.add(Region.DEFAULT, 0, 0, 0);
        location.add(Region.UNDEFINED, 1, 0, 0);
        location.assign(Region.UNDEFINED, new Voxel(0, 0, 0));
        
        assertEquals(0, location.locations.get(Region.DEFAULT).volume);
        assertEquals(2, location.locations.get(Region.UNDEFINED).volume);
    }
    
    @Test
    public void assign_existingVoxelDifferentRegion_updatesSurfaces() {
        PottsLocationsMock location = new PottsLocationsMock(new ArrayList<>());
        location.add(Region.DEFAULT, 0, 0, 0);
        location.add(Region.UNDEFINED, 1, 0, 0);
        location.assign(Region.UNDEFINED, new Voxel(0, 0, 0));
        
        assertEquals(LOCATION_SURFACE, location.locations.get(Region.DEFAULT).surface);
        assertEquals(LOCATION_SURFACE + 2, location.locations.get(Region.UNDEFINED).surface);
    }
    
    @Test
    public void assign_existingVoxelNewRegion_updatesRegions() {
        PottsLocationsMock location = new PottsLocationsMock(new ArrayList<>());
        ArrayList<Voxel> voxelDefault = new ArrayList<>();
        
        voxelDefault.add(new Voxel(0, 0, 0));
        
        location.add(Region.DEFAULT, 0, 0, 0);
        location.assign(Region.UNDEFINED, new Voxel(0, 0, 0));
        
        assertEquals(new ArrayList<>(), location.locations.get(Region.DEFAULT).voxels);
        assertEquals(voxelDefault, location.locations.get(Region.UNDEFINED).voxels);
    }
    
    @Test
    public void assign_existingVoxelNewRegion_updatesVolumes() {
        PottsLocationsMock location = new PottsLocationsMock(new ArrayList<>());
        location.add(Region.DEFAULT, 0, 0, 0);
        location.assign(Region.UNDEFINED, new Voxel(0, 0, 0));
        
        assertEquals(0, location.locations.get(Region.DEFAULT).volume);
        assertEquals(1, location.locations.get(Region.UNDEFINED).volume);
    }
    
    @Test
    public void assign_existingVoxelNewRegion_updatesSurfaces() {
        PottsLocationsMock location = new PottsLocationsMock(new ArrayList<>());
        location.add(Region.DEFAULT, 0, 0, 0);
        location.assign(Region.UNDEFINED, new Voxel(0, 0, 0));
        
        assertEquals(LOCATION_SURFACE, location.locations.get(Region.DEFAULT).surface);
        assertEquals(LOCATION_SURFACE + 1, location.locations.get(Region.UNDEFINED).surface);
    }
    
    @Test
    public void assign_missingVoxel_doesNothing() {
        PottsLocationsMock location = new PottsLocationsMock(new ArrayList<>());
        ArrayList<Voxel> voxelDefault = new ArrayList<>();
        ArrayList<Voxel> voxelAdditional = new ArrayList<>();
        
        voxelDefault.add(new Voxel(0, 0, 0));
        voxelAdditional.add(new Voxel(1, 0, 0));
        
        location.add(Region.DEFAULT, 0, 0, 0);
        location.add(Region.UNDEFINED, 1, 0, 0);
        
        location.assign(Region.DEFAULT, new Voxel(2, 0, 0));
        assertEquals(voxelDefault, location.locations.get(Region.DEFAULT).voxels);
        assertEquals(voxelAdditional, location.locations.get(Region.UNDEFINED).voxels);
    }
    
    @Test
    public void clear_hasVoxels_updatesArray() {
        PottsLocationsMock location = new PottsLocationsMock(voxelListForAddRemove);
        int[][][] ids = new int[][][] { { { 1, 0, 0 }, { 1, 0, 0 } } };
        int[][][] regions = new int[][][] { { { -1, 0, 0 }, { -2, 0, 0 } } };
        location.clear(ids, regions);
        
        assertArrayEquals(new int[] { 0, 0, 0 }, ids[0][0]);
        assertArrayEquals(new int[] { 0, 0, 0 }, ids[0][1]);
        assertArrayEquals(new int[] { 0, 0, 0 }, regions[0][0]);
        assertArrayEquals(new int[] { 0, 0, 0 }, regions[0][1]);
    }
    
    @Test
    public void clear_hasVoxels_updatesLists() {
        PottsLocationsMock location = new PottsLocationsMock(voxelListForAddRemove);
        location.clear(new int[1][3][3], new int[1][3][3]);
        assertEquals(0, location.locations.size());
    }
    
    @Test
    public void update_validRegion_updatesArrays() {
        int[][][] ids = new int[][][] { { { 0, 1, 2 } } };
        int[][][] regions = new int[][][] { { { 0, 0, 0 } } };
        
        ArrayList<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(0, 1, 0));
        PottsLocationsMock loc = new PottsLocationsMock(voxels);
        loc.add(Region.UNDEFINED, 0, 0, 0);
        
        loc.update(3, ids, regions);
        assertArrayEquals(new int[] { 3, 3, 2 }, ids[0][0]);
        assertArrayEquals(new int[] { Region.UNDEFINED.ordinal(), Region.DEFAULT.ordinal(), 0 }, regions[0][0]);
    }
    
    @Test
    public void convert_createsContainer() {
        int locationID = randomIntBetween(1, 10);
        
        ArrayList<Voxel> voxels = new ArrayList<>();
        int N = randomIntBetween(1, 10);
        for (int i = 0; i < 2*N; i++) {
            for (int j = 0; j < 2*N; j++) {
                voxels.add(new Voxel(i, j, 0));
            }
        }
        
        PottsLocationsMock location = new PottsLocationsMock(voxels);
        
        EnumMap<Region, ArrayList<Voxel>> regions = new EnumMap<>(Region.class);
        regions.put(Region.DEFAULT, new ArrayList<>());
        regions.put(Region.NUCLEUS, new ArrayList<>());
        for (int i = 0; i < 2*N; i++) {
            for (int j = 0; j < 2*N; j++) {
                if ((i + j)%2 == 0) {
                    location.assign(Region.NUCLEUS, new Voxel(i, j, 0));
                    regions.get(Region.NUCLEUS).add(new Voxel(i, j, 0));
                } else {
                    regions.get(Region.DEFAULT).add(new Voxel(i, j, 0));
                }
            }
        }
        
        PottsLocationContainer container = (PottsLocationContainer)location.convert(locationID);
        
        assertEquals(locationID, container.id);
        assertEquals(new Voxel(N, N, 0), container.center);
        assertEquals(voxels, container.allVoxels);
        assertEquals(regions, container.regions);
    }
    
    @Test
    public void separateVoxels_validListsNoRegions_updatesLists() {
        PottsLocationsMock loc = new PottsLocationsMock(voxelListAB);
        PottsLocations split = (PottsLocations)loc.separateVoxels(voxelListA, voxelListB, randomDoubleZero);
        
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
    public void separateVoxels_validListsNoRegions_updatesVolumes() {
        PottsLocationsMock loc = new PottsLocationsMock(voxelListAB);
        PottsLocations split = (PottsLocations)loc.separateVoxels(voxelListA, voxelListB, randomDoubleZero);
        assertEquals(4, loc.volume);
        assertEquals(3, split.volume);
        
    }
    
    @Test
    public void separateVoxels_validListsNoRegions_updatesSurfaces() {
        PottsLocationsMock loc = new PottsLocationsMock(voxelListAB);
        PottsLocations split = (PottsLocations)loc.separateVoxels(voxelListA, voxelListB, randomDoubleZero);
        assertEquals(LOCATIONS_SURFACE - 3, loc.surface);
        assertEquals(LOCATIONS_SURFACE, split.surface);
    }
    
    @Test
    public void separateVoxels_validListsWithRegions_updatesLists() {
        PottsLocationsMock loc = new PottsLocationsMock(voxelListForMultipleRegionsA);
        
        loc.add(Region.UNDEFINED, 1, 2, 0);
        loc.add(Region.UNDEFINED, 2, 0, 0);
        loc.add(Region.UNDEFINED, 2, 1, 0);
        loc.add(Region.UNDEFINED, 2, 2, 0);
        
        PottsLocations split = (PottsLocations)loc.separateVoxels(voxelListForMultipleRegionsA, voxelListForMultipleRegionsB, randomDoubleZero);
        
        ArrayList<Voxel> locVoxels = new ArrayList<>(voxelListForMultipleRegionsA);
        ArrayList<Voxel> splitVoxels = new ArrayList<>(voxelListForMultipleRegionsB);
        
        ArrayList<Voxel> locRegionVoxels = new ArrayList<>(loc.locations.get(Region.DEFAULT).voxels);
        locRegionVoxels.addAll(loc.locations.get(Region.UNDEFINED).voxels);
        
        ArrayList<Voxel> splitRegionVoxels = new ArrayList<>(split.locations.get(Region.DEFAULT).voxels);
        splitRegionVoxels.addAll(split.locations.get(Region.UNDEFINED).voxels);
        
        locVoxels.sort(VOXEL_COMPARATOR);
        loc.voxels.sort(VOXEL_COMPARATOR);
        locRegionVoxels.sort(VOXEL_COMPARATOR);
        splitVoxels.sort(VOXEL_COMPARATOR);
        split.voxels.sort(VOXEL_COMPARATOR);
        splitRegionVoxels.sort(VOXEL_COMPARATOR);
        
        assertEquals(locVoxels, loc.voxels);
        assertEquals(splitVoxels, split.voxels);
        assertEquals(locVoxels, locRegionVoxels);
        assertEquals(splitVoxels, splitRegionVoxels);
    }
}
