package arcade.env.loc;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.*;
import ec.util.MersenneTwisterFast;
import arcade.env.loc.Location.Voxel;
import static arcade.sim.Potts.*;

public class PottsLocations3DTest {
	MersenneTwisterFast randomDoubleZero, randomDoubleOne;
	private static final int TAG_ADDITIONAL = TAG_DEFAULT - 1;
	
	@Before
	public void setupMocks() {
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
	public void assignVoxels_randomFraction_updatesTags() {
		PottsLocations3D loc = new PottsLocations3D(new ArrayList<>());
		loc.locations.put(TAG_ADDITIONAL, new PottsLocation3D(new ArrayList<>()));
		
		int N = 10;
		int f = (int)(Math.random()*10*0.9) + 1; // between 1 and 9, inclusive
		
		for (int k = 0; k < N; k++) {
			for (int i = 0; i < N; i++) {
				for (int j = 0; j < N; j++) {
					loc.add(i, j, k);
				}
			}
		}
		
		double[] fractions = new double[] { f/10.0, 1 - f/10.0 };
		PottsLocations.assignVoxels(loc, fractions, randomDoubleZero);
		
		assertEquals(N*N*N, loc.voxels.size());
		assertEquals(N*N*N, loc.locations.get(TAG_DEFAULT).voxels.size() + loc.locations.get(TAG_ADDITIONAL).voxels.size());
		
		int sizeDefault = loc.locations.get(TAG_DEFAULT).voxels.size();
		int sizeAdditional = loc.locations.get(TAG_ADDITIONAL).voxels.size();
		
		assertTrue((sizeDefault < f*N*N + 10) && (sizeDefault >= f*N*N - 10));
		assertTrue((sizeAdditional <= (10 - f)*N*N + 10) && (sizeAdditional > (10 - f)*N*N - 10));
	}
	
	@Test
	public void assignVoxels_noCenterVoxel_updatesTags() {
		PottsLocations3D loc = new PottsLocations3D(new ArrayList<>());
		loc.locations.put(TAG_ADDITIONAL, new PottsLocation2D(new ArrayList<>()));
		
		loc.add(TAG_DEFAULT, 0, 0, 0);
		loc.add(TAG_DEFAULT, 1, 0, 0);
		loc.add(TAG_DEFAULT, 2, 0, 1);
		loc.add(TAG_DEFAULT, 0, 1, 2);
		loc.add(TAG_DEFAULT, 0, 2, 0);
		
		MersenneTwisterFast randomMock = mock(MersenneTwisterFast.class);
		when(randomMock.nextInt(5)).thenReturn(0);
		
		PottsLocations.assignVoxels(loc, new double[] { 0.8, 0.2 }, randomMock);
		
		assertEquals(4, loc.locations.get(TAG_DEFAULT).voxels.size());
		assertEquals(1, loc.locations.get(TAG_ADDITIONAL).voxels.size());
		assertEquals(new Voxel(0, 0, 0), loc.locations.get(TAG_ADDITIONAL).voxels.get(0));
	}
}
