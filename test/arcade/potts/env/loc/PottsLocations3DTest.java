package arcade.potts.env.loc;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.*;
import ec.util.MersenneTwisterFast;
import static arcade.core.util.Enums.Region;

public class PottsLocations3DTest {
    static MersenneTwisterFast randomDoubleZero, randomDoubleOne;
    
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
    public void assignVoxels_randomFraction_updatesRegions() {
        PottsLocations3D loc = new PottsLocations3D(new ArrayList<>());
        loc.locations.put(Region.UNDEFINED, new PottsLocation3D(new ArrayList<>()));
        
        int n = 10;
        int f = (int) (Math.random() * 10 * 0.9) + 1; // between 1 and 9, inclusive
        
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    loc.add(i, j, k);
                }
            }
        }
        
        EnumMap<Region, Double> fractions = new EnumMap<>(Region.class);
        fractions.put(Region.DEFAULT, f / 10.0);
        fractions.put(Region.UNDEFINED, 1 - f / 10.0);
        PottsLocations.assignVoxels(loc, fractions, randomDoubleZero);
        
        assertEquals(n * n * n, loc.voxels.size());
        assertEquals(n * n * n, loc.locations.get(Region.DEFAULT).voxels.size()
                + loc.locations.get(Region.UNDEFINED).voxels.size());
        
        int sizeDefault = loc.locations.get(Region.DEFAULT).voxels.size();
        int sizeAdditional = loc.locations.get(Region.UNDEFINED).voxels.size();
        
        assertTrue((sizeDefault < f * n * n + 10) && (sizeDefault >= f * n * n - 10));
        assertTrue((sizeAdditional <= (10 - f) * n * n + 10) && (sizeAdditional > (10 - f) * n * n - 10));
    }
    
    @Test
    public void assignVoxels_noCenterVoxel_updatesRegions() {
        PottsLocations3D loc = new PottsLocations3D(new ArrayList<>());
        loc.locations.put(Region.UNDEFINED, new PottsLocation2D(new ArrayList<>()));
        
        loc.add(Region.DEFAULT, 0, 0, 0);
        loc.add(Region.DEFAULT, 1, 0, 0);
        loc.add(Region.DEFAULT, 2, 0, 1);
        loc.add(Region.DEFAULT, 0, 1, 2);
        loc.add(Region.DEFAULT, 0, 2, 0);
        
        MersenneTwisterFast randomMock = mock(MersenneTwisterFast.class);
        when(randomMock.nextInt(5)).thenReturn(0);
        
        EnumMap<Region, Double> fractions = new EnumMap<>(Region.class);
        fractions.put(Region.DEFAULT, 0.8);
        fractions.put(Region.UNDEFINED, 0.2);
        PottsLocations.assignVoxels(loc, fractions, randomMock);
        
        assertEquals(4, loc.locations.get(Region.DEFAULT).voxels.size());
        assertEquals(1, loc.locations.get(Region.UNDEFINED).voxels.size());
        assertEquals(new Voxel(0, 0, 0), loc.locations.get(Region.UNDEFINED).voxels.get(0));
    }
}
