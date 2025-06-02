package arcade.potts.env.location;

import java.util.ArrayList;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import arcade.core.util.Vector;
import static org.junit.jupiter.api.Assertions.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.potts.env.location.Voxel.VOXEL_COMPARATOR;
import static arcade.potts.util.PottsEnums.Direction;
import static arcade.potts.util.PottsEnums.Region;

public class Location2DTest {
    private static final double EPSILON = 1E-10;

    static ArrayList<Voxel> voxelListForDiameters;

    private static final int[][] VOLUME_SURFACE =
            new int[][] {
                {1, 4},
                {9, 12},
                {25, 21},
                {45, 29},
                {69, 36},
                {109, 45},
                {145, 52},
                {193, 61},
                {249, 69},
                {305, 77},
                {373, 85},
                {437, 92},
                {517, 100},
                {609, 109},
                {697, 117},
                {793, 124},
                {889, 132},
                {1005, 140},
                {1125, 149},
                {1245, 157},
                {1369, 164},
                {1513, 173},
                {1649, 181},
                {1789, 188},
                {1941, 196},
                {2109, 205},
                {2285, 213},
                {2449, 221},
                {2617, 228},
                {2809, 237},
            };

    @BeforeAll
    public static void setupLists() {
        voxelListForDiameters = new ArrayList<>();
        voxelListForDiameters.add(new Voxel(5, 3, 0));
        voxelListForDiameters.add(new Voxel(6, 3, 0));
        voxelListForDiameters.add(new Voxel(7, 3, 0));
        voxelListForDiameters.add(new Voxel(5, 4, 0));
        voxelListForDiameters.add(new Voxel(6, 4, 0));
        voxelListForDiameters.add(new Voxel(7, 4, 0));
        voxelListForDiameters.add(new Voxel(5, 5, 0));
        voxelListForDiameters.add(new Voxel(7, 5, 0));
        voxelListForDiameters.add(new Voxel(8, 5, 0));
        voxelListForDiameters.add(new Voxel(8, 6, 0));
    }

    @Test
    public void getNeighbors_givenLocation_returnsList() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(-1, 0, 0));
        voxels.add(new Voxel(1, 0, 0));
        voxels.add(new Voxel(0, -1, 0));
        voxels.add(new Voxel(0, 1, 0));

        PottsLocation2D loc = new PottsLocation2D(new ArrayList<>());
        ArrayList<Voxel> neighbors = loc.getNeighbors(new Voxel(0, 0, 0));

        voxels.sort(VOXEL_COMPARATOR);
        neighbors.sort(VOXEL_COMPARATOR);

        assertEquals(voxels, neighbors);
    }

    @Test
    public void getNeighbors_givenLocations_returnsList() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(-1, 0, 0));
        voxels.add(new Voxel(1, 0, 0));
        voxels.add(new Voxel(0, -1, 0));
        voxels.add(new Voxel(0, 1, 0));

        PottsLocations2D loc = new PottsLocations2D(new ArrayList<>());
        ArrayList<Voxel> neighbors = loc.getNeighbors(new Voxel(0, 0, 0));

        voxels.sort(VOXEL_COMPARATOR);
        neighbors.sort(VOXEL_COMPARATOR);

        assertEquals(voxels, neighbors);
    }

    @Test
    public void getDiameters_validLocation_calculatesValues() {
        PottsLocation2D loc = new PottsLocation2D(voxelListForDiameters);
        HashMap<Direction, Integer> diameters = loc.getDiameters();
        assertEquals(3, (int) diameters.get(Direction.YZ_PLANE));
        assertEquals(2, (int) diameters.get(Direction.ZX_PLANE));
        assertEquals(4, (int) diameters.get(Direction.POSITIVE_XY));
        assertEquals(3, (int) diameters.get(Direction.NEGATIVE_XY));
    }

    @Test
    public void getDiameters_invalidLocation_returnsZero() {
        PottsLocation2D loc = new PottsLocation2D(new ArrayList<>());
        HashMap<Direction, Integer> diameters = loc.getDiameters();
        assertEquals(0, (int) diameters.get(Direction.YZ_PLANE));
        assertEquals(0, (int) diameters.get(Direction.ZX_PLANE));
        assertEquals(0, (int) diameters.get(Direction.POSITIVE_XY));
        assertEquals(0, (int) diameters.get(Direction.NEGATIVE_XY));
    }

    @Test
    public void getDiameters_validLocations_calculatesValues() {
        PottsLocations2D loc = new PottsLocations2D(voxelListForDiameters);
        HashMap<Direction, Integer> diameters = loc.getDiameters();
        assertEquals(3, (int) diameters.get(Direction.YZ_PLANE));
        assertEquals(2, (int) diameters.get(Direction.ZX_PLANE));
        assertEquals(4, (int) diameters.get(Direction.POSITIVE_XY));
        assertEquals(3, (int) diameters.get(Direction.NEGATIVE_XY));
    }

    @Test
    public void getDiameters_invalidLocations_returnsZero() {
        PottsLocations2D loc = new PottsLocations2D(new ArrayList<>());
        HashMap<Direction, Integer> diameters = loc.getDiameters();
        assertEquals(0, (int) diameters.get(Direction.YZ_PLANE));
        assertEquals(0, (int) diameters.get(Direction.ZX_PLANE));
        assertEquals(0, (int) diameters.get(Direction.POSITIVE_XY));
        assertEquals(0, (int) diameters.get(Direction.NEGATIVE_XY));
    }

    @Test
    public void convertSurface_givenLocationValue_calculatesValue() {
        PottsLocation2D loc = new PottsLocation2D(new ArrayList<>());
        for (int[] sv : VOLUME_SURFACE) {
            assertEquals(sv[1], loc.convertSurface(sv[0], 1), EPSILON);
        }
    }

    @Test
    public void convertSurface_givenLocationsValue_calculatesValue() {
        PottsLocations2D loc = new PottsLocations2D(new ArrayList<>());
        for (int[] sv : VOLUME_SURFACE) {
            assertEquals(sv[1], loc.convertSurface(sv[0], 1), EPSILON);
        }
    }

    @Test
    public void calculateSurface_emptyList_returnsZero() {
        PottsLocation2D loc = new PottsLocation2D(new ArrayList<>());
        assertEquals(0, loc.calculateSurface());
    }

    @Test
    public void calculateSurface_validVoxels_returnsValue() {
        int[] surfaces = new int[] {4, 6, 8, 8};
        int[][][] voxelLists =
                new int[][][] {
                    {{1, 1, 0}},
                    {{1, 1, 0}, {2, 1, 0}},
                    {{1, 1, 0}, {2, 1, 0}, {1, 2, 0}},
                    {{1, 1, 0}, {2, 1, 0}, {1, 2, 0}, {2, 2, 0}},
                };

        for (int i = 0; i < surfaces.length; i++) {
            ArrayList<Voxel> voxels = new ArrayList<>();
            for (int[] v : voxelLists[i]) {
                voxels.add(new Voxel(v[0], v[1], v[2]));
            }
            PottsLocation2D loc = new PottsLocation2D(voxels);
            assertEquals(surfaces[i], loc.calculateSurface());
        }
    }

    @Test
    public void calculateHeight_emptyList_returnsZero() {
        PottsLocation2D loc = new PottsLocation2D(new ArrayList<>());
        assertEquals(0, loc.calculateHeight());
    }

    @Test
    public void calculateHeight_validVoxels_returnsValue() {
        int[] heights = new int[] {1, 1, 1, 1};
        int[][][] voxelLists =
                new int[][][] {
                    {{1, 1, 0}},
                    {{1, 1, 0}, {2, 1, 0}},
                    {{1, 1, 0}, {2, 1, 0}, {1, 2, 0}},
                    {{1, 1, 0}, {2, 1, 0}, {1, 2, 0}, {2, 2, 0}},
                };

        for (int i = 0; i < heights.length; i++) {
            ArrayList<Voxel> voxels = new ArrayList<>();
            for (int[] v : voxelLists[i]) {
                voxels.add(new Voxel(v[0], v[1], v[2]));
            }
            PottsLocation2D loc = new PottsLocation2D(voxels);
            assertEquals(heights[i], loc.calculateHeight());
        }
    }

    @Test
    public void updateSurface_voxelAdded_returnsValue() {
        int[] surfaces = new int[] {4, 2, 0, -2, -4};
        int[][][] voxelLists =
                new int[][][] {
                    {},
                    {{0, 1, 0}},
                    {{0, 1, 0}, {1, 0, 0}},
                    {{0, 1, 0}, {1, 0, 0}, {2, 1, 0}},
                    {{0, 1, 0}, {1, 0, 0}, {2, 1, 0}, {1, 2, 0}},
                };

        for (int i = 0; i < surfaces.length; i++) {
            ArrayList<Voxel> voxels = new ArrayList<>();
            for (int[] v : voxelLists[i]) {
                voxels.add(new Voxel(v[0], v[1], v[2]));
            }
            PottsLocation2D loc = new PottsLocation2D(voxels);
            assertEquals(surfaces[i], loc.updateSurface(new Voxel(1, 1, 0)));
        }
    }

    @Test
    public void updateSurface_voxelRemoved_returnsValue() {
        int[] surfaces = new int[] {4, 2, 0, -2, -4};
        int[][][] voxelLists =
                new int[][][] {
                    {{1, 1, 0}},
                    {{1, 1, 0}, {0, 1, 0}},
                    {{1, 1, 0}, {0, 1, 0}, {1, 0, 0}},
                    {{1, 1, 0}, {0, 1, 0}, {1, 0, 0}, {2, 1, 0}},
                    {{1, 1, 0}, {0, 1, 0}, {1, 0, 0}, {2, 1, 0}, {1, 2, 0}},
                };

        for (int i = 0; i < surfaces.length; i++) {
            ArrayList<Voxel> voxels = new ArrayList<>();
            for (int[] v : voxelLists[i]) {
                voxels.add(new Voxel(v[0], v[1], v[2]));
            }
            PottsLocation2D loc = new PottsLocation2D(voxels);
            assertEquals(surfaces[i], loc.updateSurface(new Voxel(1, 1, 0)));
        }
    }

    @Test
    public void updateHeight_voxelAdded_returnsValue() {
        int[] heights = new int[] {1, 0, 0, 0, 0};
        int[][][] voxelLists =
                new int[][][] {
                    {},
                    {{0, 1, 0}},
                    {{0, 1, 0}, {1, 0, 0}},
                    {{0, 1, 0}, {1, 0, 0}, {2, 1, 0}},
                    {{0, 1, 0}, {1, 0, 0}, {2, 1, 0}, {1, 2, 0}},
                };

        for (int i = 0; i < heights.length; i++) {
            ArrayList<Voxel> voxels = new ArrayList<>();
            for (int[] v : voxelLists[i]) {
                voxels.add(new Voxel(v[0], v[1], v[2]));
            }
            PottsLocation2D loc = new PottsLocation2D(voxels);
            assertEquals(heights[i], loc.updateHeight(new Voxel(1, 1, 0)));
        }
    }

    @Test
    public void updateHeight_voxelRemoved_returnsValue() {
        int[] heights = new int[] {1, 0, 0, 0, 0};
        int[][][] voxelLists =
                new int[][][] {
                    {{1, 1, 0}},
                    {{1, 1, 0}, {0, 1, 0}},
                    {{1, 1, 0}, {0, 1, 0}, {1, 0, 0}},
                    {{1, 1, 0}, {0, 1, 0}, {1, 0, 0}, {2, 1, 0}},
                    {{1, 1, 0}, {0, 1, 0}, {1, 0, 0}, {2, 1, 0}, {1, 2, 0}},
                };

        for (int i = 0; i < heights.length; i++) {
            ArrayList<Voxel> voxels = new ArrayList<>();
            for (int[] v : voxelLists[i]) {
                voxels.add(new Voxel(v[0], v[1], v[2]));
            }
            PottsLocation2D loc = new PottsLocation2D(voxels);
            assertEquals(heights[i], loc.updateHeight(new Voxel(1, 1, 0)));
        }
    }

    @Test
    public void getSlice_givenLocation_returnsValue() {
        PottsLocation2D loc = new PottsLocation2D(new ArrayList<>());
        assertEquals(Direction.YZ_PLANE, loc.getSlice(Direction.YZ_PLANE, null));
        assertEquals(Direction.ZX_PLANE, loc.getSlice(Direction.ZX_PLANE, null));
        assertEquals(Direction.POSITIVE_XY, loc.getSlice(Direction.NEGATIVE_XY, null));
        assertEquals(Direction.NEGATIVE_XY, loc.getSlice(Direction.POSITIVE_XY, null));
    }

    @Test
    public void getSlice_givenLocations_returnsValue() {
        PottsLocations2D loc = new PottsLocations2D(new ArrayList<>());
        assertEquals(Direction.YZ_PLANE, loc.getSlice(Direction.YZ_PLANE, null));
        assertEquals(Direction.ZX_PLANE, loc.getSlice(Direction.ZX_PLANE, null));
        assertEquals(Direction.POSITIVE_XY, loc.getSlice(Direction.NEGATIVE_XY, null));
        assertEquals(Direction.NEGATIVE_XY, loc.getSlice(Direction.POSITIVE_XY, null));
    }

    @Test
    public void getSlice_invalidDirection_returnsNull() {
        PottsLocation2D loc = new PottsLocation2D(new ArrayList<>());
        assertNull(loc.getSlice(Direction.UNDEFINED, null));

        PottsLocations2D locs = new PottsLocations2D(new ArrayList<>());
        assertNull(locs.getSlice(Direction.UNDEFINED, null));
    }

    @Test
    public void getSelected_midSizeLocation_returnsList() {
        ArrayList<Voxel> voxels = new ArrayList<>();

        int r = 4;
        int n = 10;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                voxels.add(new Voxel(i - n / 2, j - n / 2, 0));
            }
        }

        PottsLocation2D loc = new PottsLocation2D(voxels);
        ArrayList<Voxel> selected = loc.getSelected(new Voxel(0, 0, 0), Math.PI * r * r);

        assertTrue(selected.size() < n * n);
        for (Voxel voxel : selected) {
            assertTrue(Math.sqrt(Math.pow(voxel.x, 2) + Math.pow(voxel.y, 2)) <= r);
        }
    }

    @Test
    public void getSelected_maxSizeLocation_returnsList() {
        ArrayList<Voxel> voxels = new ArrayList<>();

        int n = 10;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                voxels.add(new Voxel(i - n / 2, j - n / 2, 0));
            }
        }

        PottsLocation2D loc = new PottsLocation2D(voxels);
        ArrayList<Voxel> selected = loc.getSelected(new Voxel(0, 0, 0), Integer.MAX_VALUE);

        assertEquals(selected.size(), n * n);
    }

    @Test
    public void getSelected_minSizeLocation_returnsList() {
        ArrayList<Voxel> voxels = new ArrayList<>();

        int n = 10;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                voxels.add(new Voxel(i - n / 2, j - n / 2, 0));
            }
        }

        PottsLocation2D loc = new PottsLocation2D(voxels);
        ArrayList<Voxel> selected = loc.getSelected(new Voxel(0, 0, 0), 0);

        assertEquals(selected.size(), 0);
    }

    @Test
    public void getSelected_midSizeLocations_returnsList() {
        ArrayList<Voxel> voxelsA = new ArrayList<>();
        ArrayList<Voxel> voxelsB = new ArrayList<>();

        int r = 4;
        int n = 10;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                Voxel voxel = new Voxel(i - n / 2, j - n / 2, 0);
                if (i == n / 2 && j == n / 2) {
                    voxelsB.add(voxel);
                } else if (randomDoubleBetween(0, 1) < 0.5) {
                    voxelsA.add(voxel);
                } else {
                    voxelsB.add(voxel);
                }
            }
        }

        PottsLocations2D loc = new PottsLocations2D(voxelsA);
        voxelsB.forEach(voxel -> loc.add(Region.UNDEFINED, voxel.x, voxel.y, voxel.z));
        ArrayList<Voxel> selected = loc.getSelected(new Voxel(0, 0, 0), Math.PI * r * r);

        assertTrue(selected.size() < voxelsB.size());
        for (Voxel voxel : selected) {
            assertTrue(Math.sqrt(Math.pow(voxel.x, 2) + Math.pow(voxel.y, 2)) <= r);
            assertTrue(voxelsA.contains(voxel));
            assertFalse(voxelsB.contains(voxel));
        }
    }

    @Test
    public void getSelected_maxSizeLocations_returnsList() {
        ArrayList<Voxel> voxelsA = new ArrayList<>();
        ArrayList<Voxel> voxelsB = new ArrayList<>();

        int n = 10;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                Voxel voxel = new Voxel(i - n / 2, j - n / 2, 0);
                if (i == n / 2 && j == n / 2) {
                    voxelsB.add(voxel);
                } else if (randomDoubleBetween(0, 1) < 0.5) {
                    voxelsA.add(voxel);
                } else {
                    voxelsB.add(voxel);
                }
            }
        }

        PottsLocations2D loc = new PottsLocations2D(voxelsA);
        voxelsB.forEach(voxel -> loc.add(Region.UNDEFINED, voxel.x, voxel.y, voxel.z));
        ArrayList<Voxel> selected = loc.getSelected(new Voxel(0, 0, 0), Integer.MAX_VALUE);

        assertEquals(selected.size(), voxelsA.size());
    }

    @Test
    public void getSelected_minSizeLocations_returnsList() {
        ArrayList<Voxel> voxelsA = new ArrayList<>();
        ArrayList<Voxel> voxelsB = new ArrayList<>();

        int n = 10;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                Voxel voxel = new Voxel(i - n / 2, j - n / 2, 0);
                if (i == n / 2 && j == n / 2) {
                    voxelsB.add(voxel);
                } else if (randomDoubleBetween(0, 1) < 0.5) {
                    voxelsA.add(voxel);
                } else {
                    voxelsB.add(voxel);
                }
            }
        }

        PottsLocations2D loc = new PottsLocations2D(voxelsA);
        voxelsB.forEach(voxel -> loc.add(Region.UNDEFINED, voxel.x, voxel.y, voxel.z));
        ArrayList<Voxel> selected = loc.getSelected(new Voxel(0, 0, 0), 0);

        assertEquals(selected.size(), 0);
    }

    @Test
    public void getOffsetInApicalFrame2D_returnsExpectedVoxel_atCenter() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        // 3x3 grid centered at (0,0)
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                voxels.add(new Voxel(x, y, 0));
            }
        }
        PottsLocation2D loc = new PottsLocation2D(voxels);

        Vector apicalAxis = new Vector(0, 1, 0); // Y-axis
        ArrayList<Integer> offsets = new ArrayList<>();
        offsets.add(50); // middle of X axis
        offsets.add(50); // middle of Y axis

        Voxel result = loc.getOffsetInApicalFrame2D(offsets, apicalAxis);
        assertEquals(new Voxel(0, 0, 0), result);
    }

    @Test
    public void getOffsetInApicalFrame2D_returnsExpectedVoxel_upperRight() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        for (int x = 0; x <= 4; x++) {
            for (int y = 0; y <= 4; y++) {
                voxels.add(new Voxel(x, y, 0));
            }
        }
        PottsLocation2D loc = new PottsLocation2D(voxels);

        Vector apicalAxis = new Vector(0, 1, 0); // Y-axis
        ArrayList<Integer> offsets = new ArrayList<>();
        offsets.add(100); // far right of X axis
        offsets.add(100); // top of Y axis

        Voxel result = loc.getOffsetInApicalFrame2D(offsets, apicalAxis);
        assertEquals(new Voxel(4, 4, 0), result);
    }

    @Test
    public void getOffsetInApicalFrame2D_emptyVoxels_returnsNull() {
        PottsLocation2D loc = new PottsLocation2D(new ArrayList<>());

        Vector apicalAxis = new Vector(1, 0, 0);
        ArrayList<Integer> offsets = new ArrayList<>();
        offsets.add(50);
        offsets.add(50);

        Voxel result = loc.getOffsetInApicalFrame2D(offsets, apicalAxis);
        assertNull(result);
    }

    @Test
    public void getOffsetInApicalFrame2D_invalidOffset_throwsException() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(0, 0, 0));
        PottsLocation2D loc = new PottsLocation2D(voxels);

        Vector apicalAxis = new Vector(1, 0, 0);

        ArrayList<Integer> badOffset = new ArrayList<>();
        badOffset.add(50); // only one element

        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    loc.getOffsetInApicalFrame2D(badOffset, apicalAxis);
                });
    }

    @Test
    public void getOffsetInApicalFrame2D_nonOrthogonalAxis_returnsExpected() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(0, 0, 0));
        voxels.add(new Voxel(1, 1, 0));
        voxels.add(new Voxel(2, 2, 0));
        voxels.add(new Voxel(3, 3, 0));
        PottsLocation2D loc = new PottsLocation2D(voxels);

        Vector apicalAxis = new Vector(1, 1, 0); // diagonal
        ArrayList<Integer> offsets = new ArrayList<>();
        offsets.add(0); // lowest orthogonal axis
        offsets.add(100); // farthest along apical

        Voxel result = loc.getOffsetInApicalFrame2D(offsets, apicalAxis);
        assertEquals(new Voxel(3, 3, 0), result);
    }
}
