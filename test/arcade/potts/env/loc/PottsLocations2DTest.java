package arcade.potts.env.loc;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.*;
import ec.util.MersenneTwisterFast;
import static arcade.core.util.Enums.Region;

public class PottsLocations2DTest {
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
	public void assignVoxels_randomFraction_updatesRegions() {
		PottsLocations2D loc = new PottsLocations2D(new ArrayList<>());
		loc.locations.put(Region.UNDEFINED, new PottsLocation2D(new ArrayList<>()));
		
		int N = 10;
		int f = (int)(Math.random()*10*0.9) + 1; // between 1 and 9, inclusive
		
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				loc.add(i, j, 0);
			}
		}
		
		EnumMap<Region, Double> fractions = new EnumMap<>(Region.class);
		fractions.put(Region.DEFAULT, f/10.0);
		fractions.put(Region.UNDEFINED, 1 - f/10.0);
		PottsLocations.assignVoxels(loc, fractions, randomDoubleZero);
		
		assertEquals(N*N, loc.voxels.size());
		assertEquals(N*N, loc.locations.get(Region.DEFAULT).voxels.size() + loc.locations.get(Region.UNDEFINED).voxels.size());
		
		int sizeDefault = loc.locations.get(Region.DEFAULT).voxels.size();
		int sizeAdditional = loc.locations.get(Region.UNDEFINED).voxels.size();
		assertTrue((sizeDefault < f*N + 1) && (sizeDefault >= f*N - 1));
		assertTrue((sizeAdditional <= (10 - f)*N + 1) && (sizeAdditional > (10 - f)*N - 1));
	}
	
	@Test
	public void assignVoxels_noCenterVoxel_updatesRegions() {
		PottsLocations2D loc = new PottsLocations2D(new ArrayList<>());
		loc.locations.put(Region.UNDEFINED, new PottsLocation2D(new ArrayList<>()));
		
		loc.add(Region.DEFAULT, 0, 0, 0);
		loc.add(Region.DEFAULT, 1, 0, 0);
		loc.add(Region.DEFAULT, 2, 0, 0);
		loc.add(Region.DEFAULT, 0, 1, 0);
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
