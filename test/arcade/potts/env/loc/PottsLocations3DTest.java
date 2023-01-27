package arcade.potts.env.loc;

import java.util.ArrayList;
import org.junit.BeforeClass;
import org.junit.Test;
import ec.util.MersenneTwisterFast;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.util.Enums.Region;

public class PottsLocations3DTest {
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
        PottsLocations3D oldLoc = new PottsLocations3D(new ArrayList<>());
        PottsLocation newLoc = oldLoc.makeLocation(voxels);
        
        assertTrue(newLoc instanceof PottsLocation3D);
        assertEquals(1, newLoc.voxels.size());
    }
    
    @Test
    public void makeLocations_givenList_createsObject() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(0, 0, 0));
        PottsLocations3D oldLoc = new PottsLocations3D(new ArrayList<>());
        PottsLocations newLoc = oldLoc.makeLocations(voxels);
        
        assertTrue(newLoc instanceof PottsLocations3D);
        assertEquals(1, newLoc.voxels.size());
    }
    
    @Test
    public void assignVoxels_defaultVoxelsOnly_updatesLists() {
        int[] targets = new int[] { 14, 15, 16 };
        
        for (int target : targets) {
            PottsLocations3D loc = new PottsLocations3D(new ArrayList<>());
            loc.locations.put(Region.NUCLEUS, new PottsLocation3D(new ArrayList<>()));
            
            int n = 3;
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    for (int k = 0; k < n; k++) {
                        loc.add(i, j, k);
                    }
                }
            }
            
            loc.distribute(Region.NUCLEUS, target, randomDoubleZero);
            
            assertEquals(n * n * n, loc.voxels.size());
            assertEquals(n * n * n, loc.locations.get(Region.DEFAULT).voxels.size()
                    + loc.locations.get(Region.NUCLEUS).voxels.size());
            
            int sizeDefault = loc.locations.get(Region.DEFAULT).voxels.size();
            int sizeRegion = loc.locations.get(Region.NUCLEUS).voxels.size();
            
            assertEquals(n * n * n - target, sizeDefault);
            assertEquals(target, sizeRegion);
        }
    }
    
    @Test
    public void assignVoxels_includesRegionVoxels_updatesLists() {
        int[] targets = new int[] { 14, 15, 16 };
        
        for (int target : targets) {
            PottsLocations3D loc = new PottsLocations3D(new ArrayList<>());
            loc.locations.put(Region.NUCLEUS, new PottsLocation3D(new ArrayList<>()));
            
            int n = 3;
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    for (int k = 0; k < n; k++) {
                        loc.add(Region.NUCLEUS, i, j, k);
                    }
                }
            }
            
            loc.distribute(Region.NUCLEUS, target, randomDoubleZero);
            
            assertEquals(n * n * n, loc.voxels.size());
            assertEquals(n * n * n, loc.locations.get(Region.DEFAULT).voxels.size()
                    + loc.locations.get(Region.NUCLEUS).voxels.size());
            
            int sizeDefault = loc.locations.get(Region.DEFAULT).voxels.size();
            int sizeRegion = loc.locations.get(Region.NUCLEUS).voxels.size();
            
            assertEquals(n * n * n - target, sizeDefault);
            assertEquals(target, sizeRegion);
        }
    }
}
