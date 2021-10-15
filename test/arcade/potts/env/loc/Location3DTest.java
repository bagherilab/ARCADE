package arcade.potts.env.loc;

import java.util.ArrayList;
import java.util.HashMap;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.core.util.Enums.Region;
import static arcade.potts.env.loc.Voxel.VOXEL_COMPARATOR;
import static arcade.potts.util.PottsEnums.Direction;

public class Location3DTest {
    private static final double EPSILON = 1E-10;
    static ArrayList<Voxel> voxelListForDiametersXY;
    static ArrayList<Voxel> voxelListForDiametersYZ;
    static ArrayList<Voxel> voxelListForDiametersZX;
    private static final int[][] VOLUME_SURFACE = new int[][] {
            { 9, 1, 23 },
            { 119, 2, 174 },
            { 425, 3, 419 },
            { 1031, 4, 765 },
            { 2017, 5, 1201 },
            { 3551, 6, 1760 },
            { 5661, 7, 2407 },
            { 8471, 8, 3153 },
            { 12085, 9, 4001 },
            { 16615, 10, 4952 },
            { 22141, 11, 6001 },
            { 28819, 12, 7160 },
            { 36573, 13, 8393 },
            { 45891, 14, 9775 },
            { 56309, 15, 11203 }
    };
    
    @BeforeClass
    public static void setupLists() {
        int[][] diameter = new int[][] {
                { 5, 6, 7, 5, 6, 7, 5, 7, 8, 8, 5, 6, 7, 5, 6, 7 },
                { 3, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6, 6, 6, 2, 2, 2 },
                { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 2, 2, 2 }
        };
        
        voxelListForDiametersXY = new ArrayList<>();
        voxelListForDiametersYZ = new ArrayList<>();
        voxelListForDiametersZX = new ArrayList<>();
        
        for (int i = 0; i < diameter[0].length; i++) {
            voxelListForDiametersXY.add(new Voxel(diameter[0][i], diameter[1][i], diameter[2][i]));
            voxelListForDiametersYZ.add(new Voxel(diameter[2][i], diameter[0][i], diameter[1][i]));
            voxelListForDiametersZX.add(new Voxel(diameter[1][i], diameter[2][i], diameter[0][i]));
        }
    }
    
    @Test
    public void getNeighbors_givenLocation_returnsList() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(-1, 0, 0));
        voxels.add(new Voxel(1, 0, 0));
        voxels.add(new Voxel(0, -1, 0));
        voxels.add(new Voxel(0, 1, 0));
        voxels.add(new Voxel(0, 0, -1));
        voxels.add(new Voxel(0, 0, 1));
        
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
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
        voxels.add(new Voxel(0, 0, -1));
        voxels.add(new Voxel(0, 0, 1));
        
        PottsLocations3D loc = new PottsLocations3D(new ArrayList<>());
        ArrayList<Voxel> neighbors = loc.getNeighbors(new Voxel(0, 0, 0));
        
        voxels.sort(VOXEL_COMPARATOR);
        neighbors.sort(VOXEL_COMPARATOR);
        
        assertEquals(voxels, neighbors);
    }
    
    @Test
    public void getDiameters_validLocationXY_calculatesValues() {
        PottsLocation3D loc = new PottsLocation3D(voxelListForDiametersXY);
        HashMap<Direction, Integer> diameters = loc.getDiameters();
        assertEquals(3, (int) diameters.get(Direction.YZ_PLANE));
        assertEquals(2, (int) diameters.get(Direction.ZX_PLANE));
        assertEquals(4, (int) diameters.get(Direction.POSITIVE_XY));
        assertEquals(3, (int) diameters.get(Direction.NEGATIVE_XY));
    }
    
    @Test
    public void getDiameters_invalidLocationXY_returnsZero() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
        HashMap<Direction, Integer> diameters = loc.getDiameters();
        assertEquals(0, (int) diameters.get(Direction.YZ_PLANE));
        assertEquals(0, (int) diameters.get(Direction.ZX_PLANE));
        assertEquals(0, (int) diameters.get(Direction.POSITIVE_XY));
        assertEquals(0, (int) diameters.get(Direction.NEGATIVE_XY));
    }
    
    @Test
    public void getDiameters_validLocationYZ_calculatesValues() {
        PottsLocation3D loc = new PottsLocation3D(voxelListForDiametersYZ);
        HashMap<Direction, Integer> diameters = loc.getDiameters();
        assertEquals(3, (int) diameters.get(Direction.ZX_PLANE));
        assertEquals(2, (int) diameters.get(Direction.XY_PLANE));
        assertEquals(4, (int) diameters.get(Direction.POSITIVE_YZ));
        assertEquals(3, (int) diameters.get(Direction.NEGATIVE_YZ));
    }
    
    @Test
    public void getDiameters_invalidLocationYZ_returnsZero() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
        HashMap<Direction, Integer> diameters = loc.getDiameters();
        assertEquals(0, (int) diameters.get(Direction.ZX_PLANE));
        assertEquals(0, (int) diameters.get(Direction.XY_PLANE));
        assertEquals(0, (int) diameters.get(Direction.POSITIVE_YZ));
        assertEquals(0, (int) diameters.get(Direction.NEGATIVE_YZ));
    }
    
    @Test
    public void getDiameters_validLocationZX_calculatesValues() {
        PottsLocation3D loc = new PottsLocation3D(voxelListForDiametersZX);
        HashMap<Direction, Integer> diameters = loc.getDiameters();
        assertEquals(3, (int) diameters.get(Direction.XY_PLANE));
        assertEquals(2, (int) diameters.get(Direction.YZ_PLANE));
        assertEquals(4, (int) diameters.get(Direction.POSITIVE_ZX));
        assertEquals(3, (int) diameters.get(Direction.NEGATIVE_ZX));
    }
    
    @Test
    public void getDiameters_invalidLocationZX_returnsZero() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
        HashMap<Direction, Integer> diameters = loc.getDiameters();
        assertEquals(0, (int) diameters.get(Direction.XY_PLANE));
        assertEquals(0, (int) diameters.get(Direction.YZ_PLANE));
        assertEquals(0, (int) diameters.get(Direction.POSITIVE_ZX));
        assertEquals(0, (int) diameters.get(Direction.NEGATIVE_ZX));
    }
    
    @Test
    public void getDiameters_validLocationsXY_calculatesValues() {
        PottsLocations3D loc = new PottsLocations3D(voxelListForDiametersXY);
        HashMap<Direction, Integer> diameters = loc.getDiameters();
        assertEquals(3, (int) diameters.get(Direction.YZ_PLANE));
        assertEquals(2, (int) diameters.get(Direction.ZX_PLANE));
        assertEquals(4, (int) diameters.get(Direction.POSITIVE_XY));
        assertEquals(3, (int) diameters.get(Direction.NEGATIVE_XY));
    }
    
    @Test
    public void getDiameters_invalidLocationsXY_returnsZero() {
        PottsLocations3D loc = new PottsLocations3D(new ArrayList<>());
        HashMap<Direction, Integer> diameters = loc.getDiameters();
        assertEquals(0, (int) diameters.get(Direction.YZ_PLANE));
        assertEquals(0, (int) diameters.get(Direction.ZX_PLANE));
        assertEquals(0, (int) diameters.get(Direction.POSITIVE_XY));
        assertEquals(0, (int) diameters.get(Direction.NEGATIVE_XY));
    }
    
    @Test
    public void getDiameters_validLocationsYZ_calculatesValues() {
        PottsLocations3D loc = new PottsLocations3D(voxelListForDiametersYZ);
        HashMap<Direction, Integer> diameters = loc.getDiameters();
        assertEquals(3, (int) diameters.get(Direction.ZX_PLANE));
        assertEquals(2, (int) diameters.get(Direction.XY_PLANE));
        assertEquals(4, (int) diameters.get(Direction.POSITIVE_YZ));
        assertEquals(3, (int) diameters.get(Direction.NEGATIVE_YZ));
    }
    
    @Test
    public void getDiameters_invalidLocationsYZ_returnsZero() {
        PottsLocations3D loc = new PottsLocations3D(new ArrayList<>());
        HashMap<Direction, Integer> diameters = loc.getDiameters();
        assertEquals(0, (int) diameters.get(Direction.ZX_PLANE));
        assertEquals(0, (int) diameters.get(Direction.XY_PLANE));
        assertEquals(0, (int) diameters.get(Direction.POSITIVE_YZ));
        assertEquals(0, (int) diameters.get(Direction.NEGATIVE_YZ));
    }
    
    @Test
    public void getDiameters_validLocationsZX_calculatesValues() {
        PottsLocations3D loc = new PottsLocations3D(voxelListForDiametersZX);
        HashMap<Direction, Integer> diameters = loc.getDiameters();
        assertEquals(3, (int) diameters.get(Direction.XY_PLANE));
        assertEquals(2, (int) diameters.get(Direction.YZ_PLANE));
        assertEquals(4, (int) diameters.get(Direction.POSITIVE_ZX));
        assertEquals(3, (int) diameters.get(Direction.NEGATIVE_ZX));
    }
    
    @Test
    public void getDiameters_invalidLocationsZX_returnsZero() {
        PottsLocations3D loc = new PottsLocations3D(new ArrayList<>());
        HashMap<Direction, Integer> diameters = loc.getDiameters();
        assertEquals(0, (int) diameters.get(Direction.XY_PLANE));
        assertEquals(0, (int) diameters.get(Direction.YZ_PLANE));
        assertEquals(0, (int) diameters.get(Direction.POSITIVE_ZX));
        assertEquals(0, (int) diameters.get(Direction.NEGATIVE_ZX));
    }
    
    @Test
    public void convertSurface_givenLocationValue_calculatesValue() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
        for (int[] sv : VOLUME_SURFACE) {
            assertEquals(sv[2], loc.convertSurface(sv[0], sv[1]), EPSILON);
        }
    }
    
    @Test
    public void convertSurface_givenLocationsValue_calculatesValue() {
        PottsLocations3D loc = new PottsLocations3D(new ArrayList<>());
        for (int[] sv : VOLUME_SURFACE) {
            assertEquals(sv[2], loc.convertSurface(sv[0], sv[1]), EPSILON);
        }
    }
    
    @Test
    public void calculateSurface_emptyList_returnsZero() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
        assertEquals(0, loc.calculateSurface());
    }
    
    @Test
    public void calculateSurface_validVoxels_returnsValue() {
        int[] surfaces = new int[] { 6, 10, 14, 18 };
        int[][][] voxelLists = new int[][][] {
                { { 1, 1, 0 } },
                { { 1, 1, 0 }, { 1, 1, 1 } },
                { { 1, 1, 0 }, { 1, 1, 1 }, { 1, 2, 0 } },
                { { 1, 1, 0 }, { 1, 1, 1 }, { 1, 2, 0 }, { 2, 1, 1 } },
        };
        
        for (int i = 0; i < surfaces.length; i++) {
            ArrayList<Voxel> voxels = new ArrayList<>();
            for (int[] v : voxelLists[i]) {
                voxels.add(new Voxel(v[0], v[1], v[2]));
            }
            PottsLocation3D loc = new PottsLocation3D(voxels);
            assertEquals(surfaces[i], loc.calculateSurface());
        }
    }
    
    @Test
    public void calculateHeight_emptyList_returnsZero() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
        assertEquals(0, loc.calculateHeight());
    }
    
    @Test
    public void calculateHeight_validVoxels_returnsValue() {
        int[] heights = new int[] { 1, 2, 3, 5 };
        int[][][] voxelLists = new int[][][] {
                { { 1, 1, 0 } },
                { { 1, 1, 0 }, { 1, 1, 1 } },
                { { 1, 1, 0 }, { 1, 1, 1 }, { 1, 1, 2 } },
                { { 1, 1, 0 }, { 1, 1, 1 }, { 1, 1, 2 }, { 1, 1, 4 } },
        };
        
        for (int i = 0; i < heights.length; i++) {
            ArrayList<Voxel> voxels = new ArrayList<>();
            for (int[] v : voxelLists[i]) {
                voxels.add(new Voxel(v[0], v[1], v[2]));
            }
            PottsLocation3D loc = new PottsLocation3D(voxels);
            assertEquals(heights[i], loc.calculateHeight());
        }
    }
    
    @Test
    public void updateSurface_voxelAdded_returnsValue() {
        int[] surfaces = new int[] { 6, 4, 4, 2, 0 };
        int[][][] voxelLists = new int[][][] {
                { },
                { { 1, 1, 1 } },
                { { 1, 1, 1 }, { 1, 0, 1 } },
                { { 1, 1, 1 }, { 1, 0, 1 }, { 1, 0, 0 } },
                { { 1, 1, 1 }, { 1, 0, 1 }, { 1, 0, 0 }, { 1, 2, 0 } },
        };
    
        for (int i = 0; i < surfaces.length; i++) {
            ArrayList<Voxel> voxels = new ArrayList<>();
            for (int[] v : voxelLists[i]) {
                voxels.add(new Voxel(v[0], v[1], v[2]));
            }
            PottsLocation3D loc = new PottsLocation3D(voxels);
            assertEquals(surfaces[i], loc.updateSurface(new Voxel(1, 1, 0)));
        }
    }
    
    @Test
    public void updateSurface_voxelRemoved_returnsValue() {
        int[] surfaces = new int[] { 6, 4, 4, 2, 0 };
        int[][][] voxelLists = new int[][][] {
                { { 1, 1, 0 }, },
                { { 1, 1, 0 }, { 1, 1, 1 } },
                { { 1, 1, 0 }, { 1, 1, 1 }, { 1, 0, 1 } },
                { { 1, 1, 0 }, { 1, 1, 1 }, { 1, 0, 1 }, { 1, 0, 0 } },
                { { 1, 1, 0 }, { 1, 1, 1 }, { 1, 0, 1 }, { 1, 0, 0 }, { 1, 2, 0 } },
        };
        
        for (int i = 0; i < surfaces.length; i++) {
            ArrayList<Voxel> voxels = new ArrayList<>();
            for (int[] v : voxelLists[i]) {
                voxels.add(new Voxel(v[0], v[1], v[2]));
            }
            PottsLocation3D loc = new PottsLocation3D(voxels);
            assertEquals(surfaces[i], loc.updateSurface(new Voxel(1, 1, 0)));
        }
    }
    
    @Test
    public void updateHeight_voxelAdded_returnsValue() {
        int[] heights = new int[] { 1, 0, 1, 1, 2, 2, 0, 0, 0 };
        int[][][] voxelLists = new int[][][] {
                { },
                { { 0, 0, 2 } },
                { { 0, 0, 1 } },
                { { 0, 0, 3 } },
                { { 0, 0, 0 } },
                { { 0, 0, 4 } },
                { { 0, 0, 2 }, { 0, 0, 3 } },
                { { 0, 0, 2 }, { 0, 0, 1 } },
                { { 0, 0, 1 }, { 0, 0, 2 }, { 0, 0, 3 } },
        };
        
        for (int i = 0; i < heights.length; i++) {
            ArrayList<Voxel> voxels = new ArrayList<>();
            for (int[] v : voxelLists[i]) {
                voxels.add(new Voxel(v[0], v[1], v[2]));
            }
            PottsLocation3D loc = new PottsLocation3D(voxels);
            assertEquals(heights[i], loc.updateHeight(new Voxel(1, 1, 2)));
        }
    }
    
    @Test
    public void updateHeight_voxelRemoved_returnsValue() {
        int[] heights = new int[] { 1, 0, 1, 1, 2, 2, 0, 0, 0 };
        int[][][] voxelLists = new int[][][] {
                { { 1, 1, 2 } },
                { { 1, 1, 2 }, { 0, 0, 2 } },
                { { 1, 1, 2 }, { 0, 0, 1 } },
                { { 1, 1, 2 }, { 0, 0, 3 } },
                { { 1, 1, 2 }, { 0, 0, 0 } },
                { { 1, 1, 2 }, { 0, 0, 4 } },
                { { 1, 1, 2 }, { 0, 0, 2 }, { 0, 0, 3 } },
                { { 1, 1, 2 }, { 0, 0, 2 }, { 0, 0, 1 } },
                { { 1, 1, 2 }, { 0, 0, 1 }, { 0, 0, 2 }, { 0, 0, 3 } },
        };
        
        for (int i = 0; i < heights.length; i++) {
            ArrayList<Voxel> voxels = new ArrayList<>();
            for (int[] v : voxelLists[i]) {
                voxels.add(new Voxel(v[0], v[1], v[2]));
            }
            PottsLocation3D loc = new PottsLocation3D(voxels);
            assertEquals(heights[i], loc.updateHeight(new Voxel(1, 1, 2)));
        }
    }
    
    @Test
    public void getSlice_givenLocationPlane_returnsValue() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
        assertEquals(Direction.XY_PLANE, loc.getSlice(Direction.XY_PLANE, null));
        assertEquals(Direction.YZ_PLANE, loc.getSlice(Direction.YZ_PLANE, null));
        assertEquals(Direction.ZX_PLANE, loc.getSlice(Direction.ZX_PLANE, null));
    }
    
    @Test
    public void getSlice_givenLocationPositiveXY_returnsValue() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
        
        HashMap<Direction, Integer> diametersA = new HashMap<>();
        diametersA.put(Direction.POSITIVE_XY, 1);
        diametersA.put(Direction.XY_PLANE, 2);
        
        HashMap<Direction, Integer> diametersB = new HashMap<>();
        diametersB.put(Direction.XY_PLANE, 1);
        diametersB.put(Direction.POSITIVE_XY, 2);
        
        assertEquals(Direction.XY_PLANE, loc.getSlice(Direction.POSITIVE_XY, diametersA));
        assertEquals(Direction.NEGATIVE_XY, loc.getSlice(Direction.POSITIVE_XY, diametersB));
    }
    
    @Test
    public void getSlice_givenLocationNegativeXY_returnsValue() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
        
        HashMap<Direction, Integer> diametersA = new HashMap<>();
        diametersA.put(Direction.NEGATIVE_XY, 1);
        diametersA.put(Direction.XY_PLANE, 2);
        
        HashMap<Direction, Integer> diametersB = new HashMap<>();
        diametersB.put(Direction.XY_PLANE, 1);
        diametersB.put(Direction.NEGATIVE_XY, 2);
        
        assertEquals(Direction.XY_PLANE, loc.getSlice(Direction.NEGATIVE_XY, diametersA));
        assertEquals(Direction.POSITIVE_XY, loc.getSlice(Direction.NEGATIVE_XY, diametersB));
    }
    
    @Test
    public void getSlice_givenLocationPositiveYZ_returnsValue() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
        
        HashMap<Direction, Integer> diametersA = new HashMap<>();
        diametersA.put(Direction.POSITIVE_YZ, 1);
        diametersA.put(Direction.YZ_PLANE, 2);
        
        HashMap<Direction, Integer> diametersB = new HashMap<>();
        diametersB.put(Direction.YZ_PLANE, 1);
        diametersB.put(Direction.POSITIVE_YZ, 2);
        
        assertEquals(Direction.YZ_PLANE, loc.getSlice(Direction.POSITIVE_YZ, diametersA));
        assertEquals(Direction.NEGATIVE_YZ, loc.getSlice(Direction.POSITIVE_YZ, diametersB));
    }
    
    @Test
    public void getSlice_givenLocationNegativeYZ_returnsValue() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
        
        HashMap<Direction, Integer> diametersA = new HashMap<>();
        diametersA.put(Direction.NEGATIVE_YZ, 1);
        diametersA.put(Direction.YZ_PLANE, 2);
        
        HashMap<Direction, Integer> diametersB = new HashMap<>();
        diametersB.put(Direction.YZ_PLANE, 1);
        diametersB.put(Direction.NEGATIVE_YZ, 2);
        
        assertEquals(Direction.YZ_PLANE, loc.getSlice(Direction.NEGATIVE_YZ, diametersA));
        assertEquals(Direction.POSITIVE_YZ, loc.getSlice(Direction.NEGATIVE_YZ, diametersB));
    }
    
    @Test
    public void getSlice_givenLocationPositiveZX_returnsValue() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
        
        HashMap<Direction, Integer> diametersA = new HashMap<>();
        diametersA.put(Direction.POSITIVE_ZX, 1);
        diametersA.put(Direction.ZX_PLANE, 2);
        
        HashMap<Direction, Integer> diametersB = new HashMap<>();
        diametersB.put(Direction.ZX_PLANE, 1);
        diametersB.put(Direction.POSITIVE_ZX, 2);
        
        assertEquals(Direction.ZX_PLANE, loc.getSlice(Direction.POSITIVE_ZX, diametersA));
        assertEquals(Direction.NEGATIVE_ZX, loc.getSlice(Direction.POSITIVE_ZX, diametersB));
    }
    
    @Test
    public void getSlice_givenLocationNegativeZX_returnsValue() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
        
        HashMap<Direction, Integer> diametersA = new HashMap<>();
        diametersA.put(Direction.NEGATIVE_ZX, 1);
        diametersA.put(Direction.ZX_PLANE, 2);
        
        HashMap<Direction, Integer> diametersB = new HashMap<>();
        diametersB.put(Direction.ZX_PLANE, 1);
        diametersB.put(Direction.NEGATIVE_ZX, 2);
        
        assertEquals(Direction.ZX_PLANE, loc.getSlice(Direction.NEGATIVE_ZX, diametersA));
        assertEquals(Direction.POSITIVE_ZX, loc.getSlice(Direction.NEGATIVE_ZX, diametersB));
    }
    
    @Test
    public void getSlice_givenLocationsPlane_returnsValue() {
        PottsLocations3D loc = new PottsLocations3D(new ArrayList<>());
        assertEquals(Direction.XY_PLANE, loc.getSlice(Direction.XY_PLANE, null));
        assertEquals(Direction.YZ_PLANE, loc.getSlice(Direction.YZ_PLANE, null));
        assertEquals(Direction.ZX_PLANE, loc.getSlice(Direction.ZX_PLANE, null));
    }
    
    @Test
    public void getSlice_givenLocationsPositiveXY_returnsValue() {
        PottsLocations3D loc = new PottsLocations3D(new ArrayList<>());
        
        HashMap<Direction, Integer> diametersA = new HashMap<>();
        diametersA.put(Direction.POSITIVE_XY, 1);
        diametersA.put(Direction.XY_PLANE, 2);
        
        HashMap<Direction, Integer> diametersB = new HashMap<>();
        diametersB.put(Direction.XY_PLANE, 1);
        diametersB.put(Direction.POSITIVE_XY, 2);
        
        assertEquals(Direction.XY_PLANE, loc.getSlice(Direction.POSITIVE_XY, diametersA));
        assertEquals(Direction.NEGATIVE_XY, loc.getSlice(Direction.POSITIVE_XY, diametersB));
    }
    
    @Test
    public void getSlice_givenLocationsNegativeXY_returnsValue() {
        PottsLocations3D loc = new PottsLocations3D(new ArrayList<>());
        
        HashMap<Direction, Integer> diametersA = new HashMap<>();
        diametersA.put(Direction.NEGATIVE_XY, 1);
        diametersA.put(Direction.XY_PLANE, 2);
        
        HashMap<Direction, Integer> diametersB = new HashMap<>();
        diametersB.put(Direction.XY_PLANE, 1);
        diametersB.put(Direction.NEGATIVE_XY, 2);
        
        assertEquals(Direction.XY_PLANE, loc.getSlice(Direction.NEGATIVE_XY, diametersA));
        assertEquals(Direction.POSITIVE_XY, loc.getSlice(Direction.NEGATIVE_XY, diametersB));
    }
    
    @Test
    public void getSlice_givenLocationsPositiveYZ_returnsValue() {
        PottsLocations3D loc = new PottsLocations3D(new ArrayList<>());
        
        HashMap<Direction, Integer> diametersA = new HashMap<>();
        diametersA.put(Direction.POSITIVE_YZ, 1);
        diametersA.put(Direction.YZ_PLANE, 2);
        
        HashMap<Direction, Integer> diametersB = new HashMap<>();
        diametersB.put(Direction.YZ_PLANE, 1);
        diametersB.put(Direction.POSITIVE_YZ, 2);
        
        assertEquals(Direction.YZ_PLANE, loc.getSlice(Direction.POSITIVE_YZ, diametersA));
        assertEquals(Direction.NEGATIVE_YZ, loc.getSlice(Direction.POSITIVE_YZ, diametersB));
    }
    
    @Test
    public void getSlice_givenLocationsNegativeYZ_returnsValue() {
        PottsLocations3D loc = new PottsLocations3D(new ArrayList<>());
        
        HashMap<Direction, Integer> diametersA = new HashMap<>();
        diametersA.put(Direction.NEGATIVE_YZ, 1);
        diametersA.put(Direction.YZ_PLANE, 2);
        
        HashMap<Direction, Integer> diametersB = new HashMap<>();
        diametersB.put(Direction.YZ_PLANE, 1);
        diametersB.put(Direction.NEGATIVE_YZ, 2);
        
        assertEquals(Direction.YZ_PLANE, loc.getSlice(Direction.NEGATIVE_YZ, diametersA));
        assertEquals(Direction.POSITIVE_YZ, loc.getSlice(Direction.NEGATIVE_YZ, diametersB));
    }
    
    @Test
    public void getSlice_givenLocationsPositiveZX_returnsValue() {
        PottsLocations3D loc = new PottsLocations3D(new ArrayList<>());
        
        HashMap<Direction, Integer> diametersA = new HashMap<>();
        diametersA.put(Direction.POSITIVE_ZX, 1);
        diametersA.put(Direction.ZX_PLANE, 2);
        
        HashMap<Direction, Integer> diametersB = new HashMap<>();
        diametersB.put(Direction.ZX_PLANE, 1);
        diametersB.put(Direction.POSITIVE_ZX, 2);
        
        assertEquals(Direction.ZX_PLANE, loc.getSlice(Direction.POSITIVE_ZX, diametersA));
        assertEquals(Direction.NEGATIVE_ZX, loc.getSlice(Direction.POSITIVE_ZX, diametersB));
    }
    
    @Test
    public void getSlice_givenLocationsNegativeZX_returnsValue() {
        PottsLocations3D loc = new PottsLocations3D(new ArrayList<>());
        
        HashMap<Direction, Integer> diametersA = new HashMap<>();
        diametersA.put(Direction.NEGATIVE_ZX, 1);
        diametersA.put(Direction.ZX_PLANE, 2);
        
        HashMap<Direction, Integer> diametersB = new HashMap<>();
        diametersB.put(Direction.ZX_PLANE, 1);
        diametersB.put(Direction.NEGATIVE_ZX, 2);
        
        assertEquals(Direction.ZX_PLANE, loc.getSlice(Direction.NEGATIVE_ZX, diametersA));
        assertEquals(Direction.POSITIVE_ZX, loc.getSlice(Direction.NEGATIVE_ZX, diametersB));
    }
    
    @Test
    public void getSlice_invalidDirection_returnsNull() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
        assertNull(loc.getSlice(Direction.UNDEFINED, null));
        
        PottsLocations3D locs = new PottsLocations3D(new ArrayList<>());
        assertNull(locs.getSlice(Direction.UNDEFINED, null));
    }
    
    @Test
    public void getSelected_midSizeLocation_returnsList() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        
        int r = 4;
        int h = 6;
        int n = 10;
        for (int k = 0; k < h; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    voxels.add(new Voxel(i - n / 2, j - n / 2, k - h / 2));
                }
            }
        }
        
        PottsLocation3D loc = new PottsLocation3D(voxels);
        ArrayList<Voxel> selected = loc.getSelected(new Voxel(0, 0, 0), Math.PI * r * r * h);
        
        assertTrue(selected.size() < n * n * h);
        for (Voxel voxel : selected) {
            assertTrue(Math.sqrt(Math.pow(voxel.x, 2) + Math.pow(voxel.y, 2)) <= r);
        }
    }
    
    @Test
    public void getSelected_maxSizeLocation_returnsList() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        
        int h = 6;
        int n = 10;
        for (int k = 0; k < h; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    voxels.add(new Voxel(i - n / 2, j - n / 2, k - h / 2));
                }
            }
        }
        
        PottsLocation3D loc = new PottsLocation3D(voxels);
        ArrayList<Voxel> selected = loc.getSelected(new Voxel(0, 0, 0), Integer.MAX_VALUE);
        
        assertEquals(selected.size(), n * n * h);
    }
    
    @Test
    public void getSelected_minSizeLocation_returnsList() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        
        int h = 6;
        int n = 10;
        for (int k = 0; k < h; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    voxels.add(new Voxel(i - n / 2, j - n / 2, k - h / 2));
                }
            }
        }
        
        PottsLocation3D loc = new PottsLocation3D(voxels);
        ArrayList<Voxel> selected = loc.getSelected(new Voxel(0, 0, 0), 0);
        
        assertEquals(selected.size(), 0);
    }
    
    @Test
    public void getSelected_midSizeLocations_returnsList() {
        ArrayList<Voxel> voxelsA = new ArrayList<>();
        ArrayList<Voxel> voxelsB = new ArrayList<>();
        
        int r = 4;
        int h = 6;
        int n = 10;
        for (int k = 0; k < h; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (i == n / 2 && j == n / 2) {
                        voxelsB.add(new Voxel(i - n / 2, j - n / 2, k - h / 2));
                    } else if (randomDoubleBetween(0, 1) < 0.5) {
                        voxelsA.add(new Voxel(i - n / 2, j - n / 2, k - h / 2));
                    } else {
                        voxelsB.add(new Voxel(i - n / 2, j - n / 2, k - h / 2));
                    }
                }
            }
        }
        
        PottsLocations3D loc = new PottsLocations3D(voxelsA);
        voxelsB.forEach(voxel -> loc.add(Region.UNDEFINED, voxel.x, voxel.y, voxel.z));
        ArrayList<Voxel> selected = loc.getSelected(new Voxel(0, 0, 0), Math.PI * r * r * h);
        
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
        
        int h = 6;
        int n = 10;
        for (int k = 0; k < h; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (i == n / 2 && j == n / 2) {
                        voxelsB.add(new Voxel(i - n / 2, j - n / 2, k - h / 2));
                    } else if (randomDoubleBetween(0, 1) < 0.5) {
                        voxelsA.add(new Voxel(i - n / 2, j - n / 2, k - h / 2));
                    } else {
                        voxelsB.add(new Voxel(i - n / 2, j - n / 2, k - h / 2));
                    }
                }
            }
        }
        
        PottsLocations3D loc = new PottsLocations3D(voxelsA);
        voxelsB.forEach(voxel -> loc.add(Region.UNDEFINED, voxel.x, voxel.y, voxel.z));
        ArrayList<Voxel> selected = loc.getSelected(new Voxel(0, 0, 0), Integer.MAX_VALUE);
        
        assertEquals(selected.size(), voxelsA.size());
    }
    
    @Test
    public void getSelected_minSizeLocations_returnsList() {
        ArrayList<Voxel> voxelsA = new ArrayList<>();
        ArrayList<Voxel> voxelsB = new ArrayList<>();
        
        int h = 6;
        int n = 10;
        for (int k = 0; k < h; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (i == n / 2 && j == n / 2) {
                        voxelsB.add(new Voxel(i - n / 2, j - n / 2, k - h / 2));
                    } else if (randomDoubleBetween(0, 1) < 0.5) {
                        voxelsA.add(new Voxel(i - n / 2, j - n / 2, k - h / 2));
                    } else {
                        voxelsB.add(new Voxel(i - n / 2, j - n / 2, k - h / 2));
                    }
                }
            }
        }
        
        PottsLocations3D loc = new PottsLocations3D(voxelsA);
        voxelsB.forEach(voxel -> loc.add(Region.UNDEFINED, voxel.x, voxel.y, voxel.z));
        ArrayList<Voxel> selected = loc.getSelected(new Voxel(0, 0, 0), 0);
        
        assertEquals(selected.size(), 0);
    }
}
