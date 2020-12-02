package arcade.potts.env.loc;

import org.junit.*;
import static org.junit.Assert.*;
import java.util.*;
import static arcade.core.util.Enums.Region;
import static arcade.potts.util.PottsEnums.Direction;
import static arcade.potts.env.loc.Voxel.VOXEL_COMPARATOR;
import static arcade.potts.env.loc.Location2D.SURFACE_VOLUME_MULTIPLIER;
import static arcade.core.TestUtilities.EPSILON;

public class Location2DTest {
    static ArrayList<Voxel> voxelListForDiameters;
    
    @BeforeClass
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
        assertEquals(3, (int)diameters.get(Direction.YZ_PLANE));
        assertEquals(2, (int)diameters.get(Direction.ZX_PLANE));
        assertEquals(4, (int)diameters.get(Direction.POSITIVE_XY));
        assertEquals(3, (int)diameters.get(Direction.NEGATIVE_XY));
    }
    
    @Test
    public void getDiameters_invalidLocation_returnsZero() {
        PottsLocation2D loc = new PottsLocation2D(new ArrayList<>());
        HashMap<Direction, Integer> diameters = loc.getDiameters();
        assertEquals(0, (int)diameters.get(Direction.YZ_PLANE));
        assertEquals(0, (int)diameters.get(Direction.ZX_PLANE));
        assertEquals(0, (int)diameters.get(Direction.POSITIVE_XY));
        assertEquals(0, (int)diameters.get(Direction.NEGATIVE_XY));
    }
    
    @Test
    public void getDiameters_validLocations_calculatesValues() {
        PottsLocations2D loc = new PottsLocations2D(voxelListForDiameters);
        HashMap<Direction, Integer> diameters = loc.getDiameters();
        assertEquals(3, (int)diameters.get(Direction.YZ_PLANE));
        assertEquals(2, (int)diameters.get(Direction.ZX_PLANE));
        assertEquals(4, (int)diameters.get(Direction.POSITIVE_XY));
        assertEquals(3, (int)diameters.get(Direction.NEGATIVE_XY));
    }
    
    @Test
    public void getDiameters_invalidLocations_returnsZero() {
        PottsLocations2D loc = new PottsLocations2D(new ArrayList<>());
        HashMap<Direction, Integer> diameters = loc.getDiameters();
        assertEquals(0, (int)diameters.get(Direction.YZ_PLANE));
        assertEquals(0, (int)diameters.get(Direction.ZX_PLANE));
        assertEquals(0, (int)diameters.get(Direction.POSITIVE_XY));
        assertEquals(0, (int)diameters.get(Direction.NEGATIVE_XY));
    }
    
    @Test
    public void convertVolume_givenLocationValue_calculatesValue() {
        PottsLocation2D loc = new PottsLocation2D(new ArrayList<>());
        double volume = Math.random()*100;
        assertEquals(SURFACE_VOLUME_MULTIPLIER*Math.sqrt(volume), loc.convertVolume(volume), EPSILON);
    }
    
    @Test
    public void convertVolume_givenLocationsValue_calculatesValue() {
        PottsLocations2D loc = new PottsLocations2D(new ArrayList<>());
        double volume = Math.random()*100;
        assertEquals(SURFACE_VOLUME_MULTIPLIER*Math.sqrt(volume), loc.convertVolume(volume), EPSILON);
    }
    
    @Test
    public void calculateSurface_validID_calculatesValue() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        PottsLocation2D loc = new PottsLocation2D(voxels);
        
        // 1 voxel
        loc.add(1, 1, 0);
        assertEquals(4, loc.calculateSurface());
        
        // 2 voxels
        loc.add(2, 1, 0);
        assertEquals(6, loc.calculateSurface());
        
        // 3 voxels
        loc.add(1, 2, 0);
        assertEquals(8, loc.calculateSurface());
        
        // 4 voxels
        loc.add(2, 2, 0);
        assertEquals(8, loc.calculateSurface());
    }
    
    @Test
    public void updateSurface_validVoxels_calculatesValue() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        PottsLocation2D loc = new PottsLocation2D(voxels);
        Voxel voxel = new Voxel(1, 1, 0);
        
        // 0 voxels
        voxels.clear();
        loc.add(1, 1, 0);
        assertEquals(4, loc.updateSurface(voxel));
        
        // 1 voxel
        voxels.clear();
        voxels.add(new Voxel(0, 1, 0));
        loc = new PottsLocation2D(voxels);
        loc.add(1, 1, 0);
        assertEquals(2, loc.updateSurface(voxel));
        
        // 2 voxels
        voxels.clear();
        voxels.add(new Voxel(0, 1, 0));
        voxels.add(new Voxel(1, 0, 0));
        loc = new PottsLocation2D(voxels);
        loc.add(1, 1, 0);
        assertEquals(0, loc.updateSurface(voxel));
        
        // 3 voxels
        voxels.clear();
        voxels.add(new Voxel(0, 1, 0));
        voxels.add(new Voxel(1, 0, 0));
        voxels.add(new Voxel(2, 1, 0));
        loc = new PottsLocation2D(voxels);
        loc.add(1, 1, 0);
        assertEquals(-2, loc.updateSurface(voxel));
        
        // 4 voxels
        voxels.clear();
        voxels.add(new Voxel(0, 1, 0));
        voxels.add(new Voxel(1, 0, 0));
        voxels.add(new Voxel(2, 1, 0));
        voxels.add(new Voxel(1, 2, 0));
        loc = new PottsLocation2D(voxels);
        loc.add(1, 1, 0);
        assertEquals(-4, loc.updateSurface(voxel));
    }
    
    @Test
    public void getSlice_givenLocation_returnsValue() {
        PottsLocation2D loc = new PottsLocation2D(new ArrayList<>());
        assertEquals(Direction.YZ_PLANE, loc.getSlice(Direction.ZX_PLANE, null));
        assertEquals(Direction.ZX_PLANE, loc.getSlice(Direction.YZ_PLANE, null));
        assertEquals(Direction.POSITIVE_XY, loc.getSlice(Direction.NEGATIVE_XY, null));
        assertEquals(Direction.NEGATIVE_XY, loc.getSlice(Direction.POSITIVE_XY, null));
    }
    
    @Test
    public void getSlice_givenLocations_returnsValue() {
        PottsLocations2D loc = new PottsLocations2D(new ArrayList<>());
        assertEquals(Direction.YZ_PLANE, loc.getSlice(Direction.ZX_PLANE, null));
        assertEquals(Direction.ZX_PLANE, loc.getSlice(Direction.YZ_PLANE, null));
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
        int N = 10;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                voxels.add(new Voxel(i - N/2, j - N/2, 0));
            }
        }
        
        PottsLocation2D loc = new PottsLocation2D(voxels);
        ArrayList<Voxel> selected = loc.getSelected(new Voxel(0, 0, 0), Math.PI*r*r);
        
        assertTrue(selected.size() < N*N);
        for (Voxel voxel : selected) {
            assertTrue(Math.sqrt(Math.pow(voxel.x, 2) + Math.pow(voxel.y, 2)) <= r);
        }
    }
    
    @Test
    public void getSelected_maxSizeLocation_returnsList() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        
        int N = 10;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                voxels.add(new Voxel(i - N/2, j - N/2, 0));
            }
        }
        
        PottsLocation2D loc = new PottsLocation2D(voxels);
        ArrayList<Voxel> selected = loc.getSelected(new Voxel(0, 0, 0), Integer.MAX_VALUE);
        
        assertEquals(selected.size(), N*N);
    }
    
    @Test
    public void getSelected_minSizeLocation_returnsList() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        
        int N = 10;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                voxels.add(new Voxel(i - N/2, j - N/2, 0));
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
        int N = 10;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (i == N/2 && j == N/2) { voxelsB.add(new Voxel(i - N/2, j - N/2, 0)); }
                else if (Math.random() < 0.5) { voxelsA.add(new Voxel(i - N/2, j - N/2, 0)); }
                else { voxelsB.add(new Voxel(i - N/2, j - N/2, 0)); }
            }
        }
        
        PottsLocations2D loc = new PottsLocations2D(voxelsA);
        for (Voxel voxel : voxelsB) { loc.add(Region.UNDEFINED, voxel.x, voxel.y, voxel.z); }
        ArrayList<Voxel> selected = loc.getSelected(new Voxel(0, 0, 0), Math.PI*r*r);
        
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
        
        int N = 10;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (i == N/2 && j == N/2) { voxelsB.add(new Voxel(i - N/2, j - N/2, 0)); }
                else if (Math.random() < 0.5) { voxelsA.add(new Voxel(i - N/2, j - N/2, 0)); }
                else { voxelsB.add(new Voxel(i - N/2, j - N/2, 0)); }
            }
        }
        
        PottsLocations2D loc = new PottsLocations2D(voxelsA);
        for (Voxel voxel : voxelsB) { loc.add(Region.UNDEFINED, voxel.x, voxel.y, voxel.z); }
        ArrayList<Voxel> selected = loc.getSelected(new Voxel(0, 0, 0), Integer.MAX_VALUE);
        
        assertEquals(selected.size(), voxelsA.size());
    }
    
    @Test
    public void getSelected_minSizeLocations_returnsList() {
        ArrayList<Voxel> voxelsA = new ArrayList<>();
        ArrayList<Voxel> voxelsB = new ArrayList<>();
        
        int N = 10;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (i == N/2 && j == N/2) { voxelsB.add(new Voxel(i - N/2, j - N/2, 0)); }
                else if (Math.random() < 0.5) { voxelsA.add(new Voxel(i - N/2, j - N/2, 0)); }
                else { voxelsB.add(new Voxel(i - N/2, j - N/2, 0)); }
            }
        }
        
        PottsLocations2D loc = new PottsLocations2D(voxelsA);
        for (Voxel voxel : voxelsB) { loc.add(Region.UNDEFINED, voxel.x, voxel.y, voxel.z); }
        ArrayList<Voxel> selected = loc.getSelected(new Voxel(0, 0, 0), 0);
        
        assertEquals(selected.size(), 0);
    }
}
