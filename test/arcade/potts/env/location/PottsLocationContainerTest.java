package arcade.potts.env.location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ec.util.MersenneTwisterFast;
import arcade.core.env.location.Location;
import arcade.potts.agent.cell.PottsCellContainer;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.potts.env.location.PottsLocationFactoryTest.PottsLocationFactoryMock;
import static arcade.potts.util.PottsEnums.Region;

public class PottsLocationContainerTest {
    static final PottsLocationFactory FACTORY = new PottsLocationFactoryMock();

    static final PottsLocationFactory2D FACTORY_2D =
            mock(PottsLocationFactory2D.class, CALLS_REAL_METHODS);

    static final PottsLocationFactory3D FACTORY_3D =
            mock(PottsLocationFactory3D.class, CALLS_REAL_METHODS);

    @BeforeAll
    public static void setupMocks() {
        FACTORY.random = mock(MersenneTwisterFast.class);
        FACTORY_2D.random = mock(MersenneTwisterFast.class);
        FACTORY_3D.random = mock(MersenneTwisterFast.class);

        doAnswer(invocation -> (int) invocation.getArgument(0) - 1)
                .when(FACTORY.random)
                .nextInt(anyInt());
    }

    @Test
    public void constructor_noRegions_setsFields() {
        int id = randomIntBetween(1, 10);
        ArrayList<Voxel> voxels = new ArrayList<>();
        Voxel center = new Voxel(0, 0, 0);

        PottsLocationContainer locationContainer = new PottsLocationContainer(id, center, voxels);

        assertEquals(id, locationContainer.id);
        assertSame(center, locationContainer.center);
        assertSame(voxels, locationContainer.allVoxels);
        assertNull(locationContainer.regions);
    }

    @Test
    public void constructor_withRegions_setsFields() {
        int id = randomIntBetween(1, 10);
        ArrayList<Voxel> voxels = new ArrayList<>();
        Voxel center = new Voxel(0, 0, 0);
        EnumMap<Region, ArrayList<Voxel>> regions = new EnumMap<>(Region.class);

        PottsLocationContainer locationContainer =
                new PottsLocationContainer(id, center, voxels, regions);

        assertEquals(id, locationContainer.id);
        assertSame(center, locationContainer.center);
        assertSame(voxels, locationContainer.allVoxels);
        assertSame(regions, locationContainer.regions);
    }

    @Test
    public void getID_called_returnsValue() {
        int id = randomIntBetween(1, 10);
        PottsLocationContainer locationContainer = new PottsLocationContainer(id, null, null, null);
        assertEquals(id, locationContainer.getID());
    }

    @Test
    public void convert_noRegions_createsObject() {
        int n = 100;
        for (int i = 1; i < n; i++) {
            Voxel center = new Voxel(0, 0, 0);
            ArrayList<Voxel> voxels = FACTORY.getPossible(center, n, 1);
            PottsCellContainer cellContainer =
                    new PottsCellContainer(0, 0, 0, 0, 0, null, null, i, 0, 0);
            PottsLocationContainer locationContainer =
                    new PottsLocationContainer(0, center, voxels, null);

            Location location = locationContainer.convert(FACTORY, cellContainer);
            assertEquals(i, (int) location.getVolume());
            assertTrue(location instanceof PottsLocation);
        }
    }

    @Test
    public void convert_noRegionsEqual_createsObject() {
        int n = 100;
        for (int i = 1; i < n; i++) {
            Voxel center = new Voxel(0, 0, 0);
            ArrayList<Voxel> voxels = new ArrayList<>(Collections.nCopies(i, new Voxel(0, 0, 0)));
            PottsCellContainer cellContainer =
                    new PottsCellContainer(0, 0, 0, 0, 0, null, null, i, 0, 0);
            PottsLocationContainer locationContainer =
                    new PottsLocationContainer(0, center, voxels, null);

            Location location = locationContainer.convert(FACTORY, cellContainer);
            assertEquals(i, (int) location.getVolume());
            assertTrue(location instanceof PottsLocation);
        }
    }

    @Test
    public void convert_noRegionsWithIncrease_createsObject() {
        int n = 20;
        for (int i = 10; i < n; i++) {
            Voxel center = new Voxel(-1, 0, 0);
            ArrayList<Voxel> voxels = FACTORY.getPossible(center, n, 1);
            PottsCellContainer cellContainer =
                    new PottsCellContainer(0, 0, 0, 0, 0, null, null, i, 0, 0);
            PottsLocationContainer locationContainer =
                    new PottsLocationContainer(0, center, voxels, null);

            Location location = locationContainer.convert(FACTORY, cellContainer);
            assertEquals(i, (int) location.getVolume());
            assertTrue(location instanceof PottsLocation);
        }
    }

    @Test
    public void convert_noRegionsWithDecrease_createsObject() {
        int n = 20;
        for (int i = 10; i < n; i++) {
            Voxel center = new Voxel(1, 0, 0);
            ArrayList<Voxel> voxels = FACTORY.getPossible(center, n, 1);
            PottsCellContainer cellContainer =
                    new PottsCellContainer(0, 0, 0, 0, 0, null, null, i, 0, 0);
            PottsLocationContainer locationContainer =
                    new PottsLocationContainer(0, center, voxels, null);

            Location location = locationContainer.convert(FACTORY, cellContainer);
            assertEquals(i, (int) location.getVolume());
            assertTrue(location instanceof PottsLocation);
        }
    }

    @Test
    public void convert_withRegions_createsObject() {
        int n = 20;
        for (int i = 10; i < n; i++) {
            Voxel center = new Voxel(0, 0, 0);
            ArrayList<Voxel> voxels = FACTORY.getPossible(center, n, 1);

            EnumMap<Region, ArrayList<Voxel>> regionVoxelMap = new EnumMap<>(Region.class);
            regionVoxelMap.put(Region.DEFAULT, voxels);
            regionVoxelMap.put(Region.NUCLEUS, voxels);

            EnumMap<Region, Integer> regionTargetMap = new EnumMap<>(Region.class);
            regionTargetMap.put(Region.DEFAULT, i);
            regionTargetMap.put(Region.NUCLEUS, n - i);
            PottsCellContainer cellContainer =
                    new PottsCellContainer(
                            0, 0, 0, 0, 0, null, null, n, regionTargetMap, 0, 0, null, null);
            PottsLocationContainer locationContainer =
                    new PottsLocationContainer(0, center, voxels, regionVoxelMap);

            Location location = locationContainer.convert(FACTORY, cellContainer);
            assertEquals(n, (int) location.getVolume());
            assertTrue(location instanceof PottsLocations);
            assertEquals(
                    n - i,
                    (int) ((PottsLocations) location).locations.get(Region.NUCLEUS).getVolume());
        }
    }

    @Test
    public void convert_withRegionsEqual_createsObject() {
        int n = 20;
        for (int i = 10; i < n; i++) {
            Voxel center = new Voxel(0, 0, 0);
            ArrayList<Voxel> voxels = new ArrayList<>(Collections.nCopies(n, new Voxel(0, 0, 0)));

            EnumMap<Region, ArrayList<Voxel>> regionVoxelMap = new EnumMap<>(Region.class);
            regionVoxelMap.put(
                    Region.DEFAULT, new ArrayList<>(Collections.nCopies(i, new Voxel(0, 0, 0))));
            regionVoxelMap.put(
                    Region.NUCLEUS,
                    new ArrayList<>(Collections.nCopies(n - i, new Voxel(0, 0, 0))));

            EnumMap<Region, Integer> regionTargetMap = new EnumMap<>(Region.class);
            regionTargetMap.put(Region.DEFAULT, i);
            regionTargetMap.put(Region.NUCLEUS, n - i);
            PottsCellContainer cellContainer =
                    new PottsCellContainer(
                            0, 0, 0, 0, 0, null, null, n, regionTargetMap, 0, 0, null, null);
            PottsLocationContainer locationContainer =
                    new PottsLocationContainer(0, center, voxels, regionVoxelMap);

            Location location = locationContainer.convert(FACTORY, cellContainer);
            assertEquals(n, (int) location.getVolume());
            assertTrue(location instanceof PottsLocations);
            assertEquals(
                    n - i,
                    (int) ((PottsLocations) location).locations.get(Region.NUCLEUS).getVolume());
        }
    }

    @Test
    public void convert_withRegionsWithIncrease_createsObject() {
        int n = 20;
        for (int i = 10; i < n - 1; i++) {
            Voxel center = new Voxel(-1, 0, 0);
            ArrayList<Voxel> voxels = FACTORY.getPossible(center, n, 1);

            EnumMap<Region, ArrayList<Voxel>> regionVoxelMap = new EnumMap<>(Region.class);
            regionVoxelMap.put(Region.DEFAULT, voxels);
            regionVoxelMap.put(Region.NUCLEUS, voxels);

            EnumMap<Region, Integer> regionTargetMap = new EnumMap<>(Region.class);
            regionTargetMap.put(Region.DEFAULT, i);
            regionTargetMap.put(Region.NUCLEUS, n - i);

            PottsCellContainer cellContainer =
                    new PottsCellContainer(
                            0, 0, 0, 0, 0, null, null, n, regionTargetMap, 0, 0, null, null);
            PottsLocationContainer locationContainer =
                    new PottsLocationContainer(0, center, voxels, regionVoxelMap);

            Location location = locationContainer.convert(FACTORY, cellContainer);
            assertEquals(n, (int) location.getVolume());
            assertTrue(location instanceof PottsLocations);
            assertEquals(
                    n - i,
                    (int) ((PottsLocations) location).locations.get(Region.NUCLEUS).getVolume());
        }
    }

    @Test
    public void convert_withRegionsWithDecrease_createsObject() {
        int n = 20;
        for (int i = 10; i < n - 1; i++) {
            Voxel center = new Voxel(1, 0, 0);
            ArrayList<Voxel> voxels = FACTORY.getPossible(center, n, 1);

            EnumMap<Region, ArrayList<Voxel>> regionVoxelMap = new EnumMap<>(Region.class);
            regionVoxelMap.put(Region.DEFAULT, voxels);
            regionVoxelMap.put(Region.NUCLEUS, voxels);

            EnumMap<Region, Integer> regionTargetMap = new EnumMap<>(Region.class);
            regionTargetMap.put(Region.DEFAULT, i);
            regionTargetMap.put(Region.NUCLEUS, n - i);

            PottsCellContainer cellContainer =
                    new PottsCellContainer(
                            0, 0, 0, 0, 0, null, null, n, regionTargetMap, 0, 0, null, null);
            PottsLocationContainer locationContainer =
                    new PottsLocationContainer(0, center, voxels, regionVoxelMap);

            Location location = locationContainer.convert(FACTORY, cellContainer);
            assertEquals(n, (int) location.getVolume());
            assertTrue(location instanceof PottsLocations);
            assertEquals(
                    n - i,
                    (int) ((PottsLocations) location).locations.get(Region.NUCLEUS).getVolume());
        }
    }

    @Test
    public void convert_noRegions2D_createsObject() {
        Voxel center = new Voxel(0, 0, 0);
        ArrayList<Voxel> voxels = new ArrayList<>();
        PottsCellContainer cellContainer =
                new PottsCellContainer(0, 0, 0, 0, 0, null, null, 1, 0, 0);
        PottsLocationContainer locationContainer =
                new PottsLocationContainer(0, center, voxels, null);

        Location location = locationContainer.convert(FACTORY_2D, cellContainer);
        assertTrue(location instanceof PottsLocation2D);
    }

    @Test
    public void convert_withRegions2D_createsObject() {
        Voxel center = new Voxel(0, 0, 0);
        ArrayList<Voxel> voxels = new ArrayList<>();

        EnumMap<Region, ArrayList<Voxel>> regionVoxelMap = new EnumMap<>(Region.class);
        regionVoxelMap.put(Region.DEFAULT, voxels);
        regionVoxelMap.put(Region.NUCLEUS, voxels);

        EnumMap<Region, Integer> regionTargetMap = new EnumMap<>(Region.class);
        regionTargetMap.put(Region.DEFAULT, 0);
        regionTargetMap.put(Region.NUCLEUS, 0);

        PottsCellContainer cellContainer =
                new PottsCellContainer(
                        0, 0, 0, 0, 0, null, null, 1, regionTargetMap, 0, 0, null, null);
        PottsLocationContainer locationContainer =
                new PottsLocationContainer(0, center, voxels, regionVoxelMap);

        Location location = locationContainer.convert(FACTORY_2D, cellContainer);
        assertTrue(location instanceof PottsLocations2D);
    }

    @Test
    public void convert_noRegions3D_createsObject() {
        Voxel center = new Voxel(0, 0, 0);
        ArrayList<Voxel> voxels = new ArrayList<>();
        PottsCellContainer cellContainer =
                new PottsCellContainer(0, 0, 0, 0, 0, null, null, 1, 0, 0);
        PottsLocationContainer locationContainer =
                new PottsLocationContainer(0, center, voxels, null);

        Location location = locationContainer.convert(FACTORY_3D, cellContainer);
        assertTrue(location instanceof PottsLocation3D);
    }

    @Test
    public void convert_withRegions3D_createsObject() {
        Voxel center = new Voxel(0, 0, 0);
        ArrayList<Voxel> voxels = new ArrayList<>();

        EnumMap<Region, ArrayList<Voxel>> regionVoxelMap = new EnumMap<>(Region.class);
        regionVoxelMap.put(Region.DEFAULT, voxels);
        regionVoxelMap.put(Region.NUCLEUS, voxels);

        EnumMap<Region, Integer> regionTargetMap = new EnumMap<>(Region.class);
        regionTargetMap.put(Region.DEFAULT, 0);
        regionTargetMap.put(Region.NUCLEUS, 0);

        PottsCellContainer cellContainer =
                new PottsCellContainer(
                        0, 0, 0, 0, 0, null, null, 1, regionTargetMap, 0, 0, null, null);
        PottsLocationContainer locationContainer =
                new PottsLocationContainer(0, center, voxels, regionVoxelMap);

        Location location = locationContainer.convert(FACTORY_3D, cellContainer);
        assertTrue(location instanceof PottsLocations3D);
    }
}
