package arcade.env.loc;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.*;
import ec.util.MersenneTwisterFast;
import arcade.env.loc.Location.Voxel;
import static arcade.sim.Potts.*;

public class PottsLocations2DTest {
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
	public void assignVoxels_randomFraction_updatesTags() {
		PottsLocations2D loc = new PottsLocations2D(new ArrayList<>());
		loc.locations.put(TAG_ADDITIONAL, new PottsLocation2D(new ArrayList<>()));
		double f = Math.random();
		
		int N = 10;
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				loc.add(i, j, 0);
			}
		}
		
		double[] fractions = new double[] { f, 1 - f };
		PottsLocations.assignVoxels(loc, fractions, randomDoubleZero);
		
		assertEquals(N*N, loc.voxels.size());
		assertEquals(N*N, loc.locations.get(TAG_DEFAULT).voxels.size() + loc.locations.get(TAG_ADDITIONAL).voxels.size());
		assertTrue(loc.locations.get(TAG_DEFAULT).voxels.size() < N*N);
		assertTrue(loc.locations.get(TAG_ADDITIONAL).voxels.size() < N*N);
	}
	
	@Test
	public void assignVoxels_noCenterVoxel_updatesTags() {
		PottsLocations2D loc = new PottsLocations2D(new ArrayList<>());
		loc.locations.put(TAG_ADDITIONAL, new PottsLocation2D(new ArrayList<>()));
		
		loc.add(TAG_DEFAULT, 0, 0, 0);
		loc.add(TAG_DEFAULT, 1, 0, 0);
		loc.add(TAG_DEFAULT, 2, 0, 0);
		loc.add(TAG_DEFAULT, 0, 1, 0);
		loc.add(TAG_DEFAULT, 0, 2, 0);
		
		MersenneTwisterFast randomMock = mock(MersenneTwisterFast.class);
		when(randomMock.nextInt(5)).thenReturn(0);
		
		PottsLocations.assignVoxels(loc, new double[] { 0.8, 0.2 }, randomMock);
		
		assertEquals(4, loc.locations.get(TAG_DEFAULT).voxels.size());
		assertEquals(1, loc.locations.get(TAG_ADDITIONAL).voxels.size());
		assertEquals(new Voxel(0, 0, 0), loc.locations.get(TAG_ADDITIONAL).voxels.get(0));
	}

	@Test
	public void selectVoxels_maxNumber_updatesTags() {
		ArrayList<Voxel> voxels = new ArrayList<>();
		
		int N = 10;
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				voxels.add(new Voxel(i, j, 0));
			}
		}
		
		PottsLocations2D loc = new PottsLocations2D(voxels);
		loc.locations.put(TAG_ADDITIONAL, new PottsLocation2D(new ArrayList<>()));
		Voxel center = new Voxel(N/2, N/2, 0);
		
		int n = N*N;
		PottsLocations.selectVoxels(loc, center, TAG_ADDITIONAL, loc.locations.get(TAG_DEFAULT).voxels, n, randomDoubleZero);
		assertEquals(n, loc.locations.get(TAG_ADDITIONAL).voxels.size());
	}
	
	@Test
	public void selectVoxels_minNumber_updatesTags() {
		ArrayList<Voxel> voxels = new ArrayList<>();
		
		int N = 10;
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				voxels.add(new Voxel(i, j, 0));
			}
		}
		
		PottsLocations2D loc = new PottsLocations2D(voxels);
		loc.locations.put(TAG_ADDITIONAL, new PottsLocation2D(new ArrayList<>()));
		Voxel center = new Voxel(N/2, N/2, 0);
		
		int n = 1;
		PottsLocations.selectVoxels(loc, center, TAG_ADDITIONAL, loc.locations.get(TAG_DEFAULT).voxels, n, randomDoubleZero);
		assertEquals(n, loc.locations.get(TAG_ADDITIONAL).voxels.size());
	}
}
