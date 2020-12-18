package arcade.potts.env.loc;

import java.util.ArrayList;
import java.util.HashMap;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static arcade.core.util.Enums.Region;
import static arcade.potts.env.loc.Location3D.SURFACE_VOLUME_MULTIPLIER;
import static arcade.potts.env.loc.Voxel.VOXEL_COMPARATOR;
import static arcade.potts.util.PottsEnums.Direction;

public class Location3DTest {
    private static final double EPSILON = 1E-10;
    static ArrayList<Voxel> voxelListForDiametersXY;
    static ArrayList<Voxel> voxelListForDiametersYZ;
    static ArrayList<Voxel> voxelListForDiametersZX;
    
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
    public void convertVolume_givenLocationValue_calculatesValue() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
        double volume = Math.random() * 100;
        assertEquals(SURFACE_VOLUME_MULTIPLIER * Math.pow(volume, 2. / 3), loc.convertVolume(volume), EPSILON);
    }
    
    @Test
    public void convertVolume_givenLocationsValue_calculatesValue() {
        PottsLocations3D loc = new PottsLocations3D(new ArrayList<>());
        double volume = Math.random() * 100;
        assertEquals(SURFACE_VOLUME_MULTIPLIER * Math.pow(volume, 2. / 3), loc.convertVolume(volume), EPSILON);
    }
    
    @Test
    public void calculateSurface_validID_calculatesValue() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        PottsLocation3D loc = new PottsLocation3D(voxels);
        
        // 1 voxel
        loc.add(1, 1, 0);
        assertEquals(6, loc.calculateSurface());
        
        // 2 voxels
        loc.add(1, 1, 1);
        assertEquals(10, loc.calculateSurface());
        
        // 3 voxels
        loc.add(1, 2, 0);
        assertEquals(14, loc.calculateSurface());
        
        // 4 voxels
        loc.add(2, 1, 1);
        assertEquals(18, loc.calculateSurface());
    }
    
    @Test
    public void updateSurface_validVoxels_calculatesValue() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        PottsLocation3D loc = new PottsLocation3D(voxels);
        Voxel voxel = new Voxel(1, 1, 0);
        
        // 0 voxels
        voxels.clear();
        loc.add(1, 1, 0);
        assertEquals(6, loc.updateSurface(voxel));
        
        // 1 voxel
        voxels.clear();
        voxels.add(new Voxel(1, 1, 1));
        loc = new PottsLocation3D(voxels);
        loc.add(1, 1, 0);
        assertEquals(4, loc.updateSurface(voxel));
        
        // 2 voxels
        voxels.clear();
        voxels.add(new Voxel(1, 1, 1));
        voxels.add(new Voxel(1, 0, 1));
        loc = new PottsLocation3D(voxels);
        loc.add(1, 1, 0);
        assertEquals(4, loc.updateSurface(voxel));
        
        // 3 voxels
        voxels.clear();
        voxels.add(new Voxel(1, 1, 1));
        voxels.add(new Voxel(1, 0, 1));
        voxels.add(new Voxel(1, 0, 0));
        loc = new PottsLocation3D(voxels);
        loc.add(1, 1, 0);
        assertEquals(2, loc.updateSurface(voxel));
        
        // 4 voxels
        voxels.clear();
        voxels.add(new Voxel(1, 1, 1));
        voxels.add(new Voxel(1, 0, 0));
        voxels.add(new Voxel(1, 0, 0));
        voxels.add(new Voxel(1, 2, 0));
        loc = new PottsLocation3D(voxels);
        loc.add(1, 1, 0);
        assertEquals(0, loc.updateSurface(voxel));
    }
    
    @Test
    public void getSlice_givenLocationYZPlane_returnsValue() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
        
        HashMap<Direction, Integer> diametersA = new HashMap<>();
        diametersA.put(Direction.ZX_PLANE, 1);
        diametersA.put(Direction.XY_PLANE, 2);
        
        HashMap<Direction, Integer> diametersB = new HashMap<>();
        diametersB.put(Direction.XY_PLANE, 1);
        diametersB.put(Direction.ZX_PLANE, 2);
        
        assertEquals(Direction.XY_PLANE, loc.getSlice(Direction.YZ_PLANE, diametersA));
        assertEquals(Direction.ZX_PLANE, loc.getSlice(Direction.YZ_PLANE, diametersB));
    }
    
    @Test
    public void getSlice_givenLocationZXPlane_returnsValue() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
        
        HashMap<Direction, Integer> diametersA = new HashMap<>();
        diametersA.put(Direction.XY_PLANE, 1);
        diametersA.put(Direction.YZ_PLANE, 2);
        
        HashMap<Direction, Integer> diametersB = new HashMap<>();
        diametersB.put(Direction.YZ_PLANE, 1);
        diametersB.put(Direction.XY_PLANE, 2);
        
        assertEquals(Direction.YZ_PLANE, loc.getSlice(Direction.ZX_PLANE, diametersA));
        assertEquals(Direction.XY_PLANE, loc.getSlice(Direction.ZX_PLANE, diametersB));
    }
    
    @Test
    public void getSlice_givenLocationXYPlane_returnsValue() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
        
        HashMap<Direction, Integer> diametersA = new HashMap<>();
        diametersA.put(Direction.YZ_PLANE, 1);
        diametersA.put(Direction.ZX_PLANE, 2);
        
        HashMap<Direction, Integer> diametersB = new HashMap<>();
        diametersB.put(Direction.ZX_PLANE, 1);
        diametersB.put(Direction.YZ_PLANE, 2);
        
        assertEquals(Direction.ZX_PLANE, loc.getSlice(Direction.XY_PLANE, diametersA));
        assertEquals(Direction.YZ_PLANE, loc.getSlice(Direction.XY_PLANE, diametersB));
    }
    
    @Test
    public void getSlice_givenLocationPositiveXY_returnsValue() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
        
        HashMap<Direction, Integer> diametersA = new HashMap<>();
        diametersA.put(Direction.NEGATIVE_XY, 1);
        diametersA.put(Direction.XY_PLANE, 2);
        
        HashMap<Direction, Integer> diametersB = new HashMap<>();
        diametersB.put(Direction.XY_PLANE, 1);
        diametersB.put(Direction.NEGATIVE_XY, 2);
        
        assertEquals(Direction.XY_PLANE, loc.getSlice(Direction.POSITIVE_XY, diametersA));
        assertEquals(Direction.NEGATIVE_XY, loc.getSlice(Direction.POSITIVE_XY, diametersB));
    }
    
    @Test
    public void getSlice_givenLocationNegativeXY_returnsValue() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
        
        HashMap<Direction, Integer> diametersA = new HashMap<>();
        diametersA.put(Direction.POSITIVE_XY, 1);
        diametersA.put(Direction.XY_PLANE, 2);
        
        HashMap<Direction, Integer> diametersB = new HashMap<>();
        diametersB.put(Direction.XY_PLANE, 1);
        diametersB.put(Direction.POSITIVE_XY, 2);
        
        assertEquals(Direction.XY_PLANE, loc.getSlice(Direction.NEGATIVE_XY, diametersA));
        assertEquals(Direction.POSITIVE_XY, loc.getSlice(Direction.NEGATIVE_XY, diametersB));
    }
    
    @Test
    public void getSlice_givenLocationPositiveYZ_returnsValue() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
        
        HashMap<Direction, Integer> diametersA = new HashMap<>();
        diametersA.put(Direction.NEGATIVE_YZ, 1);
        diametersA.put(Direction.YZ_PLANE, 2);
        
        HashMap<Direction, Integer> diametersB = new HashMap<>();
        diametersB.put(Direction.YZ_PLANE, 1);
        diametersB.put(Direction.NEGATIVE_YZ, 2);
        
        assertEquals(Direction.YZ_PLANE, loc.getSlice(Direction.POSITIVE_YZ, diametersA));
        assertEquals(Direction.NEGATIVE_YZ, loc.getSlice(Direction.POSITIVE_YZ, diametersB));
    }
    
    @Test
    public void getSlice_givenLocationNegativeYZ_returnsValue() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
        
        HashMap<Direction, Integer> diametersA = new HashMap<>();
        diametersA.put(Direction.POSITIVE_YZ, 1);
        diametersA.put(Direction.YZ_PLANE, 2);
        
        HashMap<Direction, Integer> diametersB = new HashMap<>();
        diametersB.put(Direction.YZ_PLANE, 1);
        diametersB.put(Direction.POSITIVE_YZ, 2);
        
        assertEquals(Direction.YZ_PLANE, loc.getSlice(Direction.NEGATIVE_YZ, diametersA));
        assertEquals(Direction.POSITIVE_YZ, loc.getSlice(Direction.NEGATIVE_YZ, diametersB));
    }
    
    @Test
    public void getSlice_givenLocationPositiveZX_returnsValue() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
        
        HashMap<Direction, Integer> diametersA = new HashMap<>();
        diametersA.put(Direction.NEGATIVE_ZX, 1);
        diametersA.put(Direction.ZX_PLANE, 2);
        
        HashMap<Direction, Integer> diametersB = new HashMap<>();
        diametersB.put(Direction.ZX_PLANE, 1);
        diametersB.put(Direction.NEGATIVE_ZX, 2);
        
        assertEquals(Direction.ZX_PLANE, loc.getSlice(Direction.POSITIVE_ZX, diametersA));
        assertEquals(Direction.NEGATIVE_ZX, loc.getSlice(Direction.POSITIVE_ZX, diametersB));
    }
    
    @Test
    public void getSlice_givenLocationNegativeZX_returnsValue() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
        
        HashMap<Direction, Integer> diametersA = new HashMap<>();
        diametersA.put(Direction.POSITIVE_ZX, 1);
        diametersA.put(Direction.ZX_PLANE, 2);
        
        HashMap<Direction, Integer> diametersB = new HashMap<>();
        diametersB.put(Direction.ZX_PLANE, 1);
        diametersB.put(Direction.POSITIVE_ZX, 2);
        
        assertEquals(Direction.ZX_PLANE, loc.getSlice(Direction.NEGATIVE_ZX, diametersA));
        assertEquals(Direction.POSITIVE_ZX, loc.getSlice(Direction.NEGATIVE_ZX, diametersB));
    }
    
    @Test
    public void getSlice_givenLocationsYZPlane_returnsValue() {
        PottsLocations3D loc = new PottsLocations3D(new ArrayList<>());
        
        HashMap<Direction, Integer> diametersA = new HashMap<>();
        diametersA.put(Direction.ZX_PLANE, 1);
        diametersA.put(Direction.XY_PLANE, 2);
        
        HashMap<Direction, Integer> diametersB = new HashMap<>();
        diametersB.put(Direction.XY_PLANE, 1);
        diametersB.put(Direction.ZX_PLANE, 2);
        
        assertEquals(Direction.XY_PLANE, loc.getSlice(Direction.YZ_PLANE, diametersA));
        assertEquals(Direction.ZX_PLANE, loc.getSlice(Direction.YZ_PLANE, diametersB));
    }
    
    @Test
    public void getSlice_givenLocationsZXPlane_returnsValue() {
        PottsLocations3D loc = new PottsLocations3D(new ArrayList<>());
        
        HashMap<Direction, Integer> diametersA = new HashMap<>();
        diametersA.put(Direction.XY_PLANE, 1);
        diametersA.put(Direction.YZ_PLANE, 2);
        
        HashMap<Direction, Integer> diametersB = new HashMap<>();
        diametersB.put(Direction.YZ_PLANE, 1);
        diametersB.put(Direction.XY_PLANE, 2);
        
        assertEquals(Direction.YZ_PLANE, loc.getSlice(Direction.ZX_PLANE, diametersA));
        assertEquals(Direction.XY_PLANE, loc.getSlice(Direction.ZX_PLANE, diametersB));
    }
    
    @Test
    public void getSlice_givenLocationsXYPlane_returnsValue() {
        PottsLocations3D loc = new PottsLocations3D(new ArrayList<>());
        
        HashMap<Direction, Integer> diametersA = new HashMap<>();
        diametersA.put(Direction.YZ_PLANE, 1);
        diametersA.put(Direction.ZX_PLANE, 2);
        
        HashMap<Direction, Integer> diametersB = new HashMap<>();
        diametersB.put(Direction.ZX_PLANE, 1);
        diametersB.put(Direction.YZ_PLANE, 2);
        
        assertEquals(Direction.ZX_PLANE, loc.getSlice(Direction.XY_PLANE, diametersA));
        assertEquals(Direction.YZ_PLANE, loc.getSlice(Direction.XY_PLANE, diametersB));
    }
    
    @Test
    public void getSlice_givenLocationsPositiveXY_returnsValue() {
        PottsLocations3D loc = new PottsLocations3D(new ArrayList<>());
        
        HashMap<Direction, Integer> diametersA = new HashMap<>();
        diametersA.put(Direction.NEGATIVE_XY, 1);
        diametersA.put(Direction.XY_PLANE, 2);
        
        HashMap<Direction, Integer> diametersB = new HashMap<>();
        diametersB.put(Direction.XY_PLANE, 1);
        diametersB.put(Direction.NEGATIVE_XY, 2);
        
        assertEquals(Direction.XY_PLANE, loc.getSlice(Direction.POSITIVE_XY, diametersA));
        assertEquals(Direction.NEGATIVE_XY, loc.getSlice(Direction.POSITIVE_XY, diametersB));
    }
    
    @Test
    public void getSlice_givenLocationsNegativeXY_returnsValue() {
        PottsLocations3D loc = new PottsLocations3D(new ArrayList<>());
        
        HashMap<Direction, Integer> diametersA = new HashMap<>();
        diametersA.put(Direction.POSITIVE_XY, 1);
        diametersA.put(Direction.XY_PLANE, 2);
        
        HashMap<Direction, Integer> diametersB = new HashMap<>();
        diametersB.put(Direction.XY_PLANE, 1);
        diametersB.put(Direction.POSITIVE_XY, 2);
        
        assertEquals(Direction.XY_PLANE, loc.getSlice(Direction.NEGATIVE_XY, diametersA));
        assertEquals(Direction.POSITIVE_XY, loc.getSlice(Direction.NEGATIVE_XY, diametersB));
    }
    
    @Test
    public void getSlice_givenLocationsPositiveYZ_returnsValue() {
        PottsLocations3D loc = new PottsLocations3D(new ArrayList<>());
        
        HashMap<Direction, Integer> diametersA = new HashMap<>();
        diametersA.put(Direction.NEGATIVE_YZ, 1);
        diametersA.put(Direction.YZ_PLANE, 2);
        
        HashMap<Direction, Integer> diametersB = new HashMap<>();
        diametersB.put(Direction.YZ_PLANE, 1);
        diametersB.put(Direction.NEGATIVE_YZ, 2);
        
        assertEquals(Direction.YZ_PLANE, loc.getSlice(Direction.POSITIVE_YZ, diametersA));
        assertEquals(Direction.NEGATIVE_YZ, loc.getSlice(Direction.POSITIVE_YZ, diametersB));
    }
    
    @Test
    public void getSlice_givenLocationsNegativeYZ_returnsValue() {
        PottsLocations3D loc = new PottsLocations3D(new ArrayList<>());
        
        HashMap<Direction, Integer> diametersA = new HashMap<>();
        diametersA.put(Direction.POSITIVE_YZ, 1);
        diametersA.put(Direction.YZ_PLANE, 2);
        
        HashMap<Direction, Integer> diametersB = new HashMap<>();
        diametersB.put(Direction.YZ_PLANE, 1);
        diametersB.put(Direction.POSITIVE_YZ, 2);
        
        assertEquals(Direction.YZ_PLANE, loc.getSlice(Direction.NEGATIVE_YZ, diametersA));
        assertEquals(Direction.POSITIVE_YZ, loc.getSlice(Direction.NEGATIVE_YZ, diametersB));
    }
    
    @Test
    public void getSlice_givenLocationsPositiveZX_returnsValue() {
        PottsLocations3D loc = new PottsLocations3D(new ArrayList<>());
        
        HashMap<Direction, Integer> diametersA = new HashMap<>();
        diametersA.put(Direction.NEGATIVE_ZX, 1);
        diametersA.put(Direction.ZX_PLANE, 2);
        
        HashMap<Direction, Integer> diametersB = new HashMap<>();
        diametersB.put(Direction.ZX_PLANE, 1);
        diametersB.put(Direction.NEGATIVE_ZX, 2);
        
        assertEquals(Direction.ZX_PLANE, loc.getSlice(Direction.POSITIVE_ZX, diametersA));
        assertEquals(Direction.NEGATIVE_ZX, loc.getSlice(Direction.POSITIVE_ZX, diametersB));
    }
    
    @Test
    public void getSlice_givenLocationsNegativeZX_returnsValue() {
        PottsLocations3D loc = new PottsLocations3D(new ArrayList<>());
        
        HashMap<Direction, Integer> diametersA = new HashMap<>();
        diametersA.put(Direction.POSITIVE_ZX, 1);
        diametersA.put(Direction.ZX_PLANE, 2);
        
        HashMap<Direction, Integer> diametersB = new HashMap<>();
        diametersB.put(Direction.ZX_PLANE, 1);
        diametersB.put(Direction.POSITIVE_ZX, 2);
        
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
        int n = 10;
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    voxels.add(new Voxel(i - n / 2, j - n / 2, k - n / 2));
                }
            }
        }
        
        PottsLocation3D loc = new PottsLocation3D(voxels);
        ArrayList<Voxel> selected = loc.getSelected(new Voxel(0, 0, 0), 4. / 3 * Math.PI * r * r * r);
        
        assertTrue(selected.size() < n * n * n);
        for (Voxel voxel : selected) {
            assertTrue(Math.sqrt(Math.pow(voxel.x, 2) + Math.pow(voxel.y, 2) + Math.pow(voxel.z, 2)) <= r);
        }
    }
    
    @Test
    public void getSelected_maxSizeLocation_returnsList() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        
        int n = 10;
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    voxels.add(new Voxel(i - n / 2, j - n / 2, k - n / 2));
                }
            }
        }
        
        PottsLocation3D loc = new PottsLocation3D(voxels);
        ArrayList<Voxel> selected = loc.getSelected(new Voxel(0, 0, 0), Integer.MAX_VALUE);
        
        assertEquals(selected.size(), n * n * n);
    }
    
    @Test
    public void getSelected_minSizeLocation_returnsList() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        
        int n = 10;
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    voxels.add(new Voxel(i - n / 2, j - n / 2, k - n / 2));
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
        int n = 10;
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (i == n / 2 && j == n / 2 && k == n / 2) {
                        voxelsB.add(new Voxel(i - n / 2, j - n / 2, k - n / 2));
                    } else if (Math.random() < 0.5) {
                        voxelsA.add(new Voxel(i - n / 2, j - n / 2, k - n / 2));
                    } else {
                        voxelsB.add(new Voxel(i - n / 2, j - n / 2, k - n / 2));
                    }
                }
            }
        }
        
        PottsLocations3D loc = new PottsLocations3D(voxelsA);
        voxelsB.forEach(voxel -> loc.add(Region.UNDEFINED, voxel.x, voxel.y, voxel.z));
        ArrayList<Voxel> selected = loc.getSelected(new Voxel(0, 0, 0), 4. / 3 * Math.PI * r * r * r);
        
        assertTrue(selected.size() < voxelsB.size());
        for (Voxel voxel : selected) {
            assertTrue(Math.sqrt(Math.pow(voxel.x, 2) + Math.pow(voxel.y, 2) + Math.pow(voxel.z, 2)) <= r);
            assertTrue(voxelsA.contains(voxel));
            assertFalse(voxelsB.contains(voxel));
        }
    }
    
    @Test
    public void getSelected_maxSizeLocations_returnsList() {
        ArrayList<Voxel> voxelsA = new ArrayList<>();
        ArrayList<Voxel> voxelsB = new ArrayList<>();
        
        int n = 10;
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (i == n / 2 && j == n / 2 && k == n / 2) {
                        voxelsB.add(new Voxel(i - n / 2, j - n / 2, k - n / 2));
                    } else if (Math.random() < 0.5) {
                        voxelsA.add(new Voxel(i - n / 2, j - n / 2, k - n / 2));
                    } else {
                        voxelsB.add(new Voxel(i - n / 2, j - n / 2, k - n / 2));
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
        
        int n = 10;
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (i == n / 2 && j == n / 2 && k == n / 2) {
                        voxelsB.add(new Voxel(i - n / 2, j - n / 2, k - n / 2));
                    } else if (Math.random() < 0.5) {
                        voxelsA.add(new Voxel(i - n / 2, j - n / 2, k - n / 2));
                    } else {
                        voxelsB.add(new Voxel(i - n / 2, j - n / 2, k - n / 2));
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
