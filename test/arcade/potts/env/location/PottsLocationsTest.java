package arcade.potts.env.location;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ec.util.MersenneTwisterFast;
import arcade.core.util.Vector;
import arcade.potts.env.location.PottsLocationTest.PottsLocationMock;
import arcade.potts.util.PottsEnums.Direction;
import arcade.potts.util.PottsEnums.Region;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.potts.env.location.PottsLocationTest.*;
import static arcade.potts.env.location.Voxel.VOXEL_COMPARATOR;
import static arcade.potts.util.PottsEnums.Direction;
import static arcade.potts.util.PottsEnums.Region;

public class PottsLocationsTest {
    private static final double EPSILON = 1E-10;

    static MersenneTwisterFast randomDoubleZero;

    static MersenneTwisterFast randomDoubleOne;

    static ArrayList<Voxel> voxelListForVolumeSurfaceHeight;

    static ArrayList<Voxel> voxelListForMultipleRegionsA;

    static ArrayList<Voxel> voxelListForMultipleRegionsB;

    static ArrayList<Voxel> voxelListForMultipleRegions;

    static ArrayList<Voxel> voxelListForAddRemove;

    static ArrayList<Voxel> voxelListForRegionAddRemove;

    static ArrayList<Voxel> voxelListSingle;

    static ArrayList<Voxel> voxelListDouble;

    static ArrayList<Voxel> voxelListA;

    static ArrayList<Voxel> voxelListB;

    static ArrayList<Voxel> voxelListAB;

    static final int LOCATIONS_SURFACE = randomIntBetween(0, 100);

    static final int LOCATIONS_HEIGHT = randomIntBetween(0, 100);

    @BeforeAll
    public static void setupMocks() {
        randomDoubleZero = mock(MersenneTwisterFast.class);
        when(randomDoubleZero.nextDouble()).thenReturn(0.0);

        randomDoubleOne = mock(MersenneTwisterFast.class);
        when(randomDoubleOne.nextDouble()).thenReturn(1.0);
    }

    @BeforeAll
    public static void setupLists() {
        voxelListForVolumeSurfaceHeight = new ArrayList<>();
        voxelListForVolumeSurfaceHeight.add(new Voxel(0, 0, 0));

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
        PottsLocationsMock(ArrayList<Voxel> voxels) {
            super(voxels);
        }

        @Override
        PottsLocation makeLocation(ArrayList<Voxel> voxels) {
            return new PottsLocationMock(voxels);
        }

        @Override
        PottsLocations makeLocations(ArrayList<Voxel> voxels) {
            return new PottsLocationsMock(voxels);
        }

        @Override
        public double convertSurface(double volume, double height) {
            return 0;
        }

        @Override
        int calculateSurface() {
            return LOCATIONS_SURFACE;
        }

        @Override
        int calculateHeight() {
            return LOCATIONS_HEIGHT;
        }

        @Override
        int updateSurface(Voxel voxel) {
            return DELTA_SURFACE;
        }

        @Override
        int updateHeight(Voxel voxel) {
            return DELTA_HEIGHT;
        }

        @Override
        ArrayList<Voxel> getNeighbors(Voxel voxel) {
            int num = 6;
            int[] x = {0, 1, 0, -1, 0, 0};
            int[] y = {-1, 0, 1, 0, 0, 0};
            int[] z = {0, 0, 0, 0, 1, -1};

            ArrayList<Voxel> neighbors = new ArrayList<>();
            for (int i = 0; i < num; i++) {
                neighbors.add(new Voxel(voxel.x + x[i], voxel.y + y[i], voxel.z + z[i]));
            }
            return neighbors;
        }

        @Override
        HashMap<Direction, Integer> getDiameters() {
            HashMap<Direction, Integer> diameters = new HashMap<>();

            if (voxels.size() == 0) {
                diameters.put(Direction.XY_PLANE, 1);
                diameters.put(Direction.POSITIVE_XY, 2);
                diameters.put(Direction.NEGATIVE_ZX, 3);
            } else if (voxels.size() == 7) {
                diameters.put(Direction.YZ_PLANE, 1);
            } else {
                diameters.put(Direction.XY_PLANE, 1);
                diameters.put(Direction.POSITIVE_XY, 1);
                diameters.put(Direction.NEGATIVE_ZX, 1);
            }

            return diameters;
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
        ArrayList<Voxel> getSelected(Voxel center, double n) {
            ArrayList<Voxel> selected = new ArrayList<>();
            selected.add(center);
            return selected;
        }
    }

    @Test
    public void getVoxels_invalidRegion_returnsEmpty() {
        ArrayList<Voxel> voxels = new ArrayList<>();

        int n = randomIntBetween(1, 100);
        for (int i = 0; i < n; i++) {
            voxels.add(new Voxel(i, i, i));
        }

        PottsLocationsMock loc = new PottsLocationsMock(voxels);
        assertEquals(0, loc.getVoxels(Region.NUCLEUS).size());
    }

    @Test
    public void getVoxels_validRegion_returnsEmpty() {
        ArrayList<Voxel> voxels = new ArrayList<>();

        int n = randomIntBetween(1, 100);
        for (int i = 0; i < n; i++) {
            voxels.add(new Voxel(i, i, i));
        }

        PottsLocationsMock loc = new PottsLocationsMock(voxels);
        ArrayList<Voxel> voxelList = loc.getVoxels(Region.DEFAULT);

        assertNotSame(loc.voxels, voxelList);
        voxelList.sort(VOXEL_COMPARATOR);
        voxels.sort(VOXEL_COMPARATOR);
        assertEquals(voxels, voxelList);
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
        PottsLocationsMock loc = new PottsLocationsMock(voxelListForVolumeSurfaceHeight);
        assertEquals(1, (int) loc.getVolume());
        assertEquals(1, (int) loc.getVolume(Region.DEFAULT));
    }

    @Test
    public void getVolume_invalidRegion_returnsZero() {
        PottsLocationsMock loc = new PottsLocationsMock(voxelListForVolumeSurfaceHeight);
        assertEquals(0, (int) loc.getVolume(null));
    }

    @Test
    public void getVolume_multipleRegions_returnsValue() {
        PottsLocationsMock loc = new PottsLocationsMock(voxelListForMultipleRegions);
        loc.add(Region.UNDEFINED, 1, 1, 0);
        assertEquals(9, (int) loc.getVolume());
        assertEquals(8, (int) loc.getVolume(Region.DEFAULT));
        assertEquals(1, (int) loc.getVolume(Region.UNDEFINED));
    }

    @Test
    public void getSurface_validRegion_returnsValue() {
        PottsLocationsMock loc = new PottsLocationsMock(voxelListForVolumeSurfaceHeight);
        assertEquals(LOCATIONS_SURFACE, (int) loc.getSurface());
        assertEquals(LOCATION_SURFACE, (int) loc.getSurface(Region.DEFAULT));
    }

    @Test
    public void getSurface_invalidRegion_returnsZero() {
        PottsLocationsMock loc = new PottsLocationsMock(voxelListForVolumeSurfaceHeight);
        assertEquals(0, (int) loc.getSurface(null));
    }

    @Test
    public void getSurface_multipleRegions_returnsValue() {
        PottsLocationsMock loc = new PottsLocationsMock(voxelListForMultipleRegions);
        loc.add(Region.UNDEFINED, 1, 1, 0);
        assertEquals(LOCATIONS_SURFACE + DELTA_SURFACE, (int) loc.getSurface());
        assertEquals(LOCATION_SURFACE, (int) loc.getSurface(Region.DEFAULT));
        assertEquals(LOCATION_SURFACE + DELTA_SURFACE, (int) loc.getSurface(Region.UNDEFINED));
    }

    @Test
    public void getHeight_validRegion_returnsValue() {
        PottsLocationsMock loc = new PottsLocationsMock(voxelListForVolumeSurfaceHeight);
        assertEquals(LOCATIONS_HEIGHT, (int) loc.getHeight());
        assertEquals(LOCATION_HEIGHT, (int) loc.getHeight(Region.DEFAULT));
    }

    @Test
    public void getHeight_invalidRegion_returnsZero() {
        PottsLocationsMock loc = new PottsLocationsMock(voxelListForVolumeSurfaceHeight);
        assertEquals(0, (int) loc.getHeight(null));
    }

    @Test
    public void getHeight_multipleRegions_returnsValue() {
        PottsLocationsMock loc = new PottsLocationsMock(voxelListForMultipleRegions);
        loc.add(Region.UNDEFINED, 1, 1, 0);
        assertEquals(LOCATIONS_HEIGHT + DELTA_HEIGHT, (int) loc.getHeight());
        assertEquals(LOCATION_HEIGHT, (int) loc.getHeight(Region.DEFAULT));
        assertEquals(LOCATION_HEIGHT + DELTA_HEIGHT, (int) loc.getHeight(Region.UNDEFINED));
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
        assertEquals(LOCATIONS_SURFACE + 2 * DELTA_SURFACE, loc.surface);
        assertEquals(
                LOCATION_SURFACE + 2 * DELTA_SURFACE, loc.locations.get(Region.DEFAULT).surface);
    }

    @Test
    public void add_newVoxelNoRegion_updatesHeights() {
        PottsLocationsMock loc = new PottsLocationsMock(new ArrayList<>());
        loc.add(0, 0, 0);
        loc.add(1, 0, 0);
        assertEquals(LOCATIONS_HEIGHT + 2 * DELTA_HEIGHT, loc.height);
        assertEquals(LOCATION_HEIGHT + 2 * DELTA_HEIGHT, loc.locations.get(Region.DEFAULT).height);
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
        assertEquals(LOCATIONS_SURFACE + DELTA_SURFACE, loc.surface);
        assertEquals(LOCATION_SURFACE, loc.locations.get(Region.DEFAULT).surface);
        assertEquals(LOCATION_SURFACE + DELTA_SURFACE, loc.locations.get(Region.UNDEFINED).surface);
    }

    @Test
    public void add_newVoxelWithRegion_updatesHeights() {
        PottsLocationsMock loc = new PottsLocationsMock(voxelListSingle);
        loc.add(Region.UNDEFINED, 1, 1, 0);
        assertEquals(LOCATIONS_HEIGHT + DELTA_HEIGHT, loc.height);
        assertEquals(LOCATION_HEIGHT, loc.locations.get(Region.DEFAULT).height);
        assertEquals(LOCATION_HEIGHT + DELTA_HEIGHT, loc.locations.get(Region.UNDEFINED).height);
    }

    @Test
    public void add_existingVoxelWithRegion_doesNothing() {
        PottsLocationsMock loc = new PottsLocationsMock(voxelListSingle);
        loc.add(Region.UNDEFINED, 0, 0, 0);
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
    public void remove_existingVoxelNoRegion_updatesVolumes() {
        PottsLocationsMock loc = new PottsLocationsMock(voxelListForAddRemove);
        loc.remove(0, 0, 0);
        assertEquals(1, loc.volume);
        assertEquals(1, loc.locations.get(Region.DEFAULT).volume);
    }

    @Test
    public void remove_existingVoxelNoRegion_updatesSurfaces() {
        PottsLocationsMock loc = new PottsLocationsMock(voxelListForAddRemove);
        loc.remove(0, 0, 0);
        assertEquals(LOCATIONS_SURFACE - DELTA_SURFACE, loc.surface);
        assertEquals(LOCATION_SURFACE - DELTA_SURFACE, loc.locations.get(Region.DEFAULT).surface);
    }

    @Test
    public void remove_existingVoxelNoRegion_updatesHeights() {
        PottsLocationsMock loc = new PottsLocationsMock(voxelListForAddRemove);
        loc.remove(0, 0, 0);
        assertEquals(LOCATIONS_HEIGHT - DELTA_HEIGHT, loc.height);
        assertEquals(LOCATION_HEIGHT - DELTA_HEIGHT, loc.locations.get(Region.DEFAULT).height);
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
        loc.add(Region.UNDEFINED, 0, 0, 0);
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
    public void remove_existingVoxelWithRegion_updatesVolumes() {
        PottsLocationsMock loc = new PottsLocationsMock(voxelListForRegionAddRemove);
        loc.add(Region.UNDEFINED, 0, 0, 0);
        loc.add(Region.UNDEFINED, 1, 0, 0);
        loc.remove(Region.UNDEFINED, 0, 0, 0);
        assertEquals(2, loc.volume);
        assertEquals(1, loc.locations.get(Region.DEFAULT).volume);
        assertEquals(1, loc.locations.get(Region.UNDEFINED).volume);
    }

    @Test
    public void remove_existingVoxelWithRegion_updatesSurfaces() {
        PottsLocationsMock loc = new PottsLocationsMock(voxelListForRegionAddRemove);
        loc.add(Region.UNDEFINED, 0, 0, 0);
        loc.add(Region.UNDEFINED, 1, 0, 0);
        loc.remove(Region.UNDEFINED, 0, 0, 0);
        assertEquals(LOCATIONS_SURFACE + 2 * DELTA_SURFACE - DELTA_SURFACE, loc.surface);
        assertEquals(LOCATION_SURFACE, loc.locations.get(Region.DEFAULT).surface);
        assertEquals(
                LOCATION_SURFACE + 2 * DELTA_SURFACE - DELTA_SURFACE,
                loc.locations.get(Region.UNDEFINED).surface);
    }

    @Test
    public void remove_existingVoxelWithRegion_updatesHeights() {
        PottsLocationsMock loc = new PottsLocationsMock(voxelListForRegionAddRemove);
        loc.add(Region.UNDEFINED, 0, 0, 0);
        loc.add(Region.UNDEFINED, 1, 0, 0);
        loc.remove(Region.UNDEFINED, 0, 0, 0);
        assertEquals(LOCATIONS_HEIGHT + 2 * DELTA_HEIGHT - DELTA_HEIGHT, loc.height);
        assertEquals(LOCATION_HEIGHT, loc.locations.get(Region.DEFAULT).height);
        assertEquals(
                LOCATION_HEIGHT + 2 * DELTA_HEIGHT - DELTA_HEIGHT,
                loc.locations.get(Region.UNDEFINED).height);
    }

    @Test
    public void remove_alternateVoxelWithRegion_doesNothing() {
        PottsLocationsMock loc = new PottsLocationsMock(voxelListForRegionAddRemove);
        loc.add(Region.UNDEFINED, 0, 0, 0);
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
        loc.add(Region.UNDEFINED, 0, 0, 0);
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
        assertEquals(
                LOCATION_SURFACE + 2 * DELTA_SURFACE,
                location.locations.get(Region.UNDEFINED).surface);
    }

    @Test
    public void assign_existingVoxelDifferentRegion_updatesHeights() {
        PottsLocationsMock location = new PottsLocationsMock(new ArrayList<>());
        location.add(Region.DEFAULT, 0, 0, 0);
        location.add(Region.UNDEFINED, 1, 0, 0);
        location.assign(Region.UNDEFINED, new Voxel(0, 0, 0));

        assertEquals(LOCATION_HEIGHT, location.locations.get(Region.DEFAULT).height);
        assertEquals(
                LOCATION_HEIGHT + 2 * DELTA_HEIGHT,
                location.locations.get(Region.UNDEFINED).height);
    }

    @Test
    public void assign_existingVoxelDifferentRegion_updatesCenters() {
        PottsLocationsMock location = new PottsLocationsMock(new ArrayList<>());
        location.add(Region.DEFAULT, 1, 2, 3);
        location.add(Region.DEFAULT, 4, 5, 6);
        location.add(Region.UNDEFINED, 7, 8, 9);
        location.assign(Region.UNDEFINED, new Voxel(4, 5, 6));

        assertEquals(12 / 3., location.cx, EPSILON);
        assertEquals(15 / 3., location.cy, EPSILON);
        assertEquals(18 / 3., location.cz, EPSILON);

        assertEquals(1, location.locations.get(Region.DEFAULT).cx, EPSILON);
        assertEquals(2, location.locations.get(Region.DEFAULT).cy, EPSILON);
        assertEquals(3, location.locations.get(Region.DEFAULT).cz, EPSILON);

        assertEquals(11 / 2., location.locations.get(Region.UNDEFINED).cx, EPSILON);
        assertEquals(13 / 2., location.locations.get(Region.UNDEFINED).cy, EPSILON);
        assertEquals(15 / 2., location.locations.get(Region.UNDEFINED).cz, EPSILON);
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
        assertEquals(
                LOCATION_SURFACE + DELTA_SURFACE, location.locations.get(Region.UNDEFINED).surface);
    }

    @Test
    public void assign_existingVoxelNewRegion_updatesHeights() {
        PottsLocationsMock location = new PottsLocationsMock(new ArrayList<>());
        location.add(Region.DEFAULT, 0, 0, 0);
        location.assign(Region.UNDEFINED, new Voxel(0, 0, 0));

        assertEquals(LOCATION_HEIGHT, location.locations.get(Region.DEFAULT).height);
        assertEquals(
                LOCATION_HEIGHT + DELTA_HEIGHT, location.locations.get(Region.UNDEFINED).height);
    }

    @Test
    public void assign_existingVoxelNewRegion_updatesCenters() {
        PottsLocationsMock location = new PottsLocationsMock(new ArrayList<>());
        location.add(Region.DEFAULT, 1, 2, 3);
        location.add(Region.DEFAULT, 4, 5, 6);
        location.assign(Region.UNDEFINED, new Voxel(1, 2, 3));

        assertEquals(5 / 2., location.cx, EPSILON);
        assertEquals(7 / 2., location.cy, EPSILON);
        assertEquals(9 / 2., location.cz, EPSILON);

        assertEquals(4, location.locations.get(Region.DEFAULT).cx, EPSILON);
        assertEquals(5, location.locations.get(Region.DEFAULT).cy, EPSILON);
        assertEquals(6, location.locations.get(Region.DEFAULT).cz, EPSILON);

        assertEquals(1, location.locations.get(Region.UNDEFINED).cx, EPSILON);
        assertEquals(2, location.locations.get(Region.UNDEFINED).cy, EPSILON);
        assertEquals(3, location.locations.get(Region.UNDEFINED).cz, EPSILON);
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
    public void distribute_defaultRegion_doesNothing() {
        PottsLocationsMock loc = new PottsLocationsMock(new ArrayList<>());

        for (Voxel v : voxelListForMultipleRegions) {
            loc.add(Region.UNDEFINED, v.x, v.y, v.z);
        }

        int target = randomIntBetween(1, voxelListForMultipleRegions.size());
        loc.distribute(Region.DEFAULT, target, randomDoubleZero);

        ArrayList<Voxel> locVoxels = new ArrayList<>(voxelListForMultipleRegions);
        ArrayList<Voxel> defaultVoxels = new ArrayList<>(loc.locations.get(Region.DEFAULT).voxels);
        ArrayList<Voxel> regionVoxels = new ArrayList<>(loc.locations.get(Region.UNDEFINED).voxels);

        locVoxels.sort(VOXEL_COMPARATOR);
        regionVoxels.sort(VOXEL_COMPARATOR);

        assertEquals(locVoxels, regionVoxels);
        assertEquals(0, defaultVoxels.size());
    }

    @Test
    public void clear_hasVoxels_updatesArray() {
        PottsLocationsMock location = new PottsLocationsMock(voxelListForAddRemove);
        int[][][] ids = new int[][][] {{{1, 0, 0}, {1, 0, 0}}};
        int[][][] regions = new int[][][] {{{-1, 0, 0}, {-2, 0, 0}}};
        location.clear(ids, regions);

        assertArrayEquals(new int[] {0, 0, 0}, ids[0][0]);
        assertArrayEquals(new int[] {0, 0, 0}, ids[0][1]);
        assertArrayEquals(new int[] {0, 0, 0}, regions[0][0]);
        assertArrayEquals(new int[] {0, 0, 0}, regions[0][1]);
    }

    @Test
    public void clear_hasVoxels_updatesLists() {
        PottsLocationsMock location = new PottsLocationsMock(voxelListForAddRemove);
        location.clear(new int[1][3][3], new int[1][3][3]);
        assertEquals(0, location.locations.size());
    }

    @Test
    public void update_validRegion_updatesArrays() {
        int[][][] ids = new int[][][] {{{0, 1, 2}}};
        int[][][] regions = new int[][][] {{{0, 0, 0}}};

        ArrayList<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(0, 1, 0));
        PottsLocationsMock loc = new PottsLocationsMock(voxels);
        loc.add(Region.UNDEFINED, 0, 0, 0);

        loc.update(3, ids, regions);
        assertArrayEquals(new int[] {3, 3, 2}, ids[0][0]);
        assertArrayEquals(
                new int[] {Region.UNDEFINED.ordinal(), Region.DEFAULT.ordinal(), 0}, regions[0][0]);
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

        PottsLocationsMock location = new PottsLocationsMock(voxels);

        EnumMap<Region, ArrayList<Voxel>> regions = new EnumMap<>(Region.class);
        regions.put(Region.DEFAULT, new ArrayList<>());
        regions.put(Region.NUCLEUS, new ArrayList<>());
        for (int i = 0; i < 2 * n; i++) {
            for (int j = 0; j < 2 * n; j++) {
                if ((i + j) % 2 == 0) {
                    location.assign(Region.NUCLEUS, new Voxel(i, j, 0));
                    regions.get(Region.NUCLEUS).add(new Voxel(i, j, 0));
                } else {
                    regions.get(Region.DEFAULT).add(new Voxel(i, j, 0));
                }
            }
        }

        PottsLocationContainer container = (PottsLocationContainer) location.convert(locationID);

        assertEquals(locationID, container.id);
        assertEquals(new Voxel(n, n, 0), container.center);
        assertEquals(voxels, container.allVoxels);
        assertEquals(regions, container.regions);
    }

    @Test
    public void getCentroid_validRegion_returnsArray() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(0, 1, 1));
        voxels.add(new Voxel(1, 1, 2));
        voxels.add(new Voxel(2, 2, 2));
        voxels.add(new Voxel(2, 3, 3));
        PottsLocationsMock loc = new PottsLocationsMock(voxels);

        double[] centroid = loc.getCentroid(Region.DEFAULT);

        assertEquals(5 / 4., centroid[0], EPSILON);
        assertEquals(7 / 4., centroid[1], EPSILON);
        assertEquals(8 / 4., centroid[2], EPSILON);
    }

    @Test
    public void getCentroid_invalidRegion_returnsNull() {
        PottsLocationsMock loc = new PottsLocationsMock(new ArrayList<>());
        assertNull(loc.getCentroid(null));
    }

    @Test
    public void getCentroid_multipleRegions_returnsValue() {
        PottsLocationsMock loc = new PottsLocationsMock(new ArrayList<>());
        loc.add(Region.DEFAULT, 0, 1, 1);
        loc.add(Region.UNDEFINED, 1, 1, 2);
        loc.add(Region.UNDEFINED, 2, 2, 2);
        loc.add(Region.UNDEFINED, 2, 3, 3);

        double[] centroidDefault = loc.getCentroid(Region.DEFAULT);

        assertEquals(0, centroidDefault[0], EPSILON);
        assertEquals(1, centroidDefault[1], EPSILON);
        assertEquals(1, centroidDefault[2], EPSILON);

        double[] centroidUndefined = loc.getCentroid(Region.UNDEFINED);

        assertEquals(5 / 3., centroidUndefined[0], EPSILON);
        assertEquals(6 / 3., centroidUndefined[1], EPSILON);
        assertEquals(7 / 3., centroidUndefined[2], EPSILON);
    }

    @Test
    public void separateVoxels_validListsNoRegions_updatesLists() {
        PottsLocationsMock loc = new PottsLocationsMock(voxelListAB);
        PottsLocations split =
                (PottsLocations) loc.separateVoxels(voxelListA, voxelListB, randomDoubleZero);

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
        PottsLocations split =
                (PottsLocations) loc.separateVoxels(voxelListA, voxelListB, randomDoubleZero);
        assertEquals(4, loc.volume);
        assertEquals(3, split.volume);
    }

    @Test
    public void separateVoxels_validListsNoRegions_updatesSurfaces() {
        PottsLocationsMock loc = new PottsLocationsMock(voxelListAB);
        PottsLocations split =
                (PottsLocations) loc.separateVoxels(voxelListA, voxelListB, randomDoubleZero);
        assertEquals(LOCATIONS_SURFACE - 3 * DELTA_SURFACE, loc.surface);
        assertEquals(LOCATIONS_SURFACE, split.surface);
    }

    @Test
    public void separateVoxels_validListsWithRegions_updatesLists() {
        PottsLocationsMock loc = new PottsLocationsMock(voxelListForMultipleRegionsA);

        loc.add(Region.UNDEFINED, 1, 2, 0);
        loc.add(Region.UNDEFINED, 2, 0, 0);
        loc.add(Region.UNDEFINED, 2, 1, 0);
        loc.add(Region.UNDEFINED, 2, 2, 0);

        PottsLocations split =
                (PottsLocations)
                        loc.separateVoxels(
                                voxelListForMultipleRegionsA,
                                voxelListForMultipleRegionsB,
                                randomDoubleZero);

        ArrayList<Voxel> locVoxels = new ArrayList<>(voxelListForMultipleRegionsA);
        ArrayList<Voxel> splitVoxels = new ArrayList<>(voxelListForMultipleRegionsB);

        ArrayList<Voxel> locRegionVoxels =
                new ArrayList<>(loc.locations.get(Region.DEFAULT).voxels);
        locRegionVoxels.addAll(loc.locations.get(Region.UNDEFINED).voxels);

        ArrayList<Voxel> splitRegionVoxels =
                new ArrayList<>(split.locations.get(Region.DEFAULT).voxels);
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

        assertEquals(2, loc.locations.get(Region.DEFAULT).voxels.size());
        assertEquals(2, loc.locations.get(Region.UNDEFINED).voxels.size());
        assertEquals(2, split.locations.get(Region.DEFAULT).voxels.size());
        assertEquals(2, split.locations.get(Region.DEFAULT).voxels.size());
    }

    @Test
    public void getOffsetInApicalFrame_called_raisesUnsupportedOperationException() {
        PottsLocationsMock loc = new PottsLocationsMock(voxelListForMultipleRegionsA);
        Vector apicalAxis = new Vector(0, 1, 0);
        ArrayList<Integer> offsets = new ArrayList<>();
        assertThrows(
                UnsupportedOperationException.class,
                () -> loc.getOffsetInApicalFrame(offsets, apicalAxis));
    }
}
