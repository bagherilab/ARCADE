package arcade.potts.env.location;

import java.util.ArrayList;
import org.junit.BeforeClass;
import org.junit.Test;
import ec.util.MersenneTwisterFast;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.potts.util.PottsEnums.Region;

public class PottsLocations2DTest {
    static MersenneTwisterFast randomDoubleZero;
    
    static MersenneTwisterFast randomDoubleOne;
    
    @BeforeClass
    public static void setupMocks() {
        randomDoubleZero = mock(MersenneTwisterFast.class);
        when(randomDoubleZero.nextDouble()).thenReturn(0.0);
        
        randomDoubleOne = mock(MersenneTwisterFast.class);
        when(randomDoubleOne.nextDouble()).thenReturn(1.0);
    }
    
    @Test
    public void makeLocation_givenList_createsObject() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(0, 0, 0));
        PottsLocations2D oldLoc = new PottsLocations2D(new ArrayList<>());
        PottsLocation newLoc = oldLoc.makeLocation(voxels);
        
        assertTrue(newLoc instanceof PottsLocation2D);
        assertEquals(1, newLoc.voxels.size());
    }
    
    @Test
    public void makeLocations_givenList_createsObject() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(0, 0, 0));
        PottsLocations2D oldLoc = new PottsLocations2D(new ArrayList<>());
        PottsLocations newLoc = oldLoc.makeLocations(voxels);
        
        assertTrue(newLoc instanceof PottsLocations2D);
        assertEquals(1, newLoc.voxels.size());
    }
    
    @Test
    public void assignVoxels_defaultVoxelsOnly_updatesLists() {
        int[] targets = new int[] { 4, 5, 6 };
        
        for (int target : targets) {
            PottsLocations2D loc = new PottsLocations2D(new ArrayList<>());
            loc.locations.put(Region.NUCLEUS, new PottsLocation2D(new ArrayList<>()));
            
            int n = 3;
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    loc.add(i, j, 0);
                }
            }
            
            loc.distribute(Region.NUCLEUS, target, randomDoubleZero);
            
            assertEquals(n * n, loc.voxels.size());
            assertEquals(n * n, loc.locations.get(Region.DEFAULT).voxels.size()
                    + loc.locations.get(Region.NUCLEUS).voxels.size());
            
            int sizeDefault = loc.locations.get(Region.DEFAULT).voxels.size();
            int sizeRegion = loc.locations.get(Region.NUCLEUS).voxels.size();
            
            assertEquals(n * n - target, sizeDefault);
            assertEquals(target, sizeRegion);
        }
    }
    
    @Test
    public void assignVoxels_includesRegionVoxels_updatesLists() {
        int[] targets = new int[] { 4, 5, 6 };
        
        for (int target : targets) {
            PottsLocations2D loc = new PottsLocations2D(new ArrayList<>());
            loc.locations.put(Region.NUCLEUS, new PottsLocation2D(new ArrayList<>()));
            
            int n = 3;
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    loc.add(Region.NUCLEUS, i, j, 0);
                }
            }
            
            loc.distribute(Region.NUCLEUS, target, randomDoubleZero);
            
            assertEquals(n * n, loc.voxels.size());
            assertEquals(n * n, loc.locations.get(Region.DEFAULT).voxels.size()
                    + loc.locations.get(Region.NUCLEUS).voxels.size());
            
            int sizeDefault = loc.locations.get(Region.DEFAULT).voxels.size();
            int sizeRegion = loc.locations.get(Region.NUCLEUS).voxels.size();
            
            assertEquals(n * n - target, sizeDefault);
            assertEquals(target, sizeRegion);
        }
    }
}
