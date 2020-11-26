package arcade.potts.env.loc;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.EnumMap;
import java.util.ArrayList;
import ec.util.MersenneTwisterFast;
import arcade.core.env.loc.Location;
import arcade.potts.agent.cell.PottsCellContainer;
import static arcade.potts.env.loc.PottsLocationFactoryTest.PottsLocationFactoryMock;
import static arcade.core.util.Enums.Region;
import static arcade.core.TestUtilities.*;

public class PottsLocationContainerTest {
	final static PottsLocationFactory factory = new PottsLocationFactoryMock();
	final static PottsLocationFactory2D factory2D = mock(PottsLocationFactory2D.class, CALLS_REAL_METHODS);
	final static PottsLocationFactory3D factory3D = mock(PottsLocationFactory3D.class, CALLS_REAL_METHODS);
	
	@BeforeClass
	public static void setupMocks() {
		factory.random = mock(MersenneTwisterFast.class);
		factory2D.random = mock(MersenneTwisterFast.class);
		factory3D.random = mock(MersenneTwisterFast.class);
	}
	
	@Test
	public void constructor_setsFields() {
		int id = randomIntBetween(1, 10);
		ArrayList<Voxel> voxels = new ArrayList<>();
		Voxel center = new Voxel(0, 0, 0);
		EnumMap<Region, ArrayList<Voxel>> regions = new EnumMap<>(Region.class);
		
		PottsLocationContainer locationContainer = new PottsLocationContainer(id, center, voxels, regions);
		
		assertEquals(id, locationContainer.id);
		assertSame(center, locationContainer.center);
		assertSame(voxels, locationContainer.allVoxels);
		assertSame(regions, locationContainer.regions);
	}
	
	@Test
	public void getID_called_returnsValue() {
		int id = randomIntBetween(1, 10);
		PottsLocationContainer locationContainer = new PottsLocationContainer(id, null, null, null);
		assertEquals(id, locationContainer.getID());
	}
	
	@Test
	public void convert_noRegions_createsObject() {
		int N = 100;
		for (int i = 1; i < N; i++) {
			Voxel center = new Voxel(0, 0, 0);
			ArrayList<Voxel> voxels = factory.getPossible(center, N);
			PottsCellContainer cellContainer = new PottsCellContainer(0, 0, i);
			PottsLocationContainer locationContainer = new PottsLocationContainer(0, center, voxels, null);
			
			Location location = locationContainer.convert(factory, cellContainer);
			assertEquals(i, location.getVolume());
			assertTrue(location instanceof PottsLocation);
		}
	}
	
	@Test
	public void convert_noRegionsWithIncrease_createsObject() {
		int N = 100;
		for (int i = 2; i < N; i++) {
			Voxel center = new Voxel(-1, 0, 0);
			ArrayList<Voxel> voxels = factory.getPossible(center, N);
			PottsCellContainer cellContainer = new PottsCellContainer(0, 0, i);
			PottsLocationContainer locationContainer = new PottsLocationContainer(0, center, voxels, null);
			
			Location location = locationContainer.convert(factory, cellContainer);
			assertEquals(i, location.getVolume());
			assertTrue(location instanceof PottsLocation);
		}
	}
	
	@Test
	public void convert_noRegionsWithDecrease_createsObject() {
		int N = 100;
		for (int i = 2; i < N; i++) {
			Voxel center = new Voxel(1, 0, 0);
			ArrayList<Voxel> voxels = factory.getPossible(center, N);
			PottsCellContainer cellContainer = new PottsCellContainer(0, 0, i);
			PottsLocationContainer locationContainer = new PottsLocationContainer(0, center, voxels, null);
			
			Location location = locationContainer.convert(factory, cellContainer);
			assertEquals(i, location.getVolume());
			assertTrue(location instanceof PottsLocation);
		}
	}
	
	@Test
	public void convert_withRegions_createsObject() {
		int N = 100;
		for (int i = 0; i < N; i++) {
			Voxel center = new Voxel(0, 0, 0);
			ArrayList<Voxel> voxels = factory.getPossible(center, N);
			
			EnumMap<Region, ArrayList<Voxel>> regionVoxelMap = new EnumMap<>(Region.class);
			regionVoxelMap.put(Region.DEFAULT, voxels);
			regionVoxelMap.put(Region.NUCLEUS, voxels);
			
			EnumMap<Region, Integer> regionTargetMap = new EnumMap<>(Region.class);
			regionTargetMap.put(Region.DEFAULT, i);
			regionTargetMap.put(Region.NUCLEUS, N - i);
			
			PottsCellContainer cellContainer = new PottsCellContainer(0, 0, N, regionTargetMap);
			PottsLocationContainer locationContainer = new PottsLocationContainer(0, center, voxels, regionVoxelMap);
			
			Location location = locationContainer.convert(factory, cellContainer);
			assertEquals(N, location.getVolume());
			assertTrue(location instanceof PottsLocations);
			assertEquals(N - i, ((PottsLocations)location).locations.get(Region.NUCLEUS).getVolume());
		}
	}
	
	@Test
	public void convert_withRegionsWithIncrease_createsObject() {
		int N = 100;
		for (int i = 0; i < N - 1; i++) {
			Voxel center = new Voxel(-1, 0, 0);
			ArrayList<Voxel> voxels = factory.getPossible(center, N);
			
			EnumMap<Region, ArrayList<Voxel>> regionVoxelMap = new EnumMap<>(Region.class);
			regionVoxelMap.put(Region.DEFAULT, voxels);
			regionVoxelMap.put(Region.NUCLEUS, voxels);
			
			EnumMap<Region, Integer> regionTargetMap = new EnumMap<>(Region.class);
			regionTargetMap.put(Region.DEFAULT, i);
			regionTargetMap.put(Region.NUCLEUS, N - i);
			
			PottsCellContainer cellContainer = new PottsCellContainer(0, 0, N, regionTargetMap);
			PottsLocationContainer locationContainer = new PottsLocationContainer(0, center, voxels, regionVoxelMap);
			
			Location location = locationContainer.convert(factory, cellContainer);
			assertEquals(N, location.getVolume());
			assertTrue(location instanceof PottsLocations);
			assertEquals(N - i, ((PottsLocations)location).locations.get(Region.NUCLEUS).getVolume());
		}
	}
	
	@Test
	public void convert_withRegionsWithDecrease_createsObject() {
		int N = 100;
		for (int i = 0; i < N - 1; i++) {
			Voxel center = new Voxel(1, 0, 0);
			ArrayList<Voxel> voxels = factory.getPossible(center, N);
			
			EnumMap<Region, ArrayList<Voxel>> regionVoxelMap = new EnumMap<>(Region.class);
			regionVoxelMap.put(Region.DEFAULT, voxels);
			regionVoxelMap.put(Region.NUCLEUS, voxels);
			
			EnumMap<Region, Integer> regionTargetMap = new EnumMap<>(Region.class);
			regionTargetMap.put(Region.DEFAULT, i);
			regionTargetMap.put(Region.NUCLEUS, N - i);
			
			PottsCellContainer cellContainer = new PottsCellContainer(0, 0, N, regionTargetMap);
			PottsLocationContainer locationContainer = new PottsLocationContainer(0, center, voxels, regionVoxelMap);
			
			Location location = locationContainer.convert(factory, cellContainer);
			assertEquals(N, location.getVolume());
			assertTrue(location instanceof PottsLocations);
			assertEquals(N - i, ((PottsLocations)location).locations.get(Region.NUCLEUS).getVolume());
		}
	}
	
	@Test
	public void convert_noRegions2D_createsObject() {
		Voxel center = new Voxel(0, 0, 0);
		ArrayList<Voxel> voxels = new ArrayList<>();
		PottsCellContainer cellContainer = new PottsCellContainer(0, 0, 0);
		PottsLocationContainer locationContainer = new PottsLocationContainer(0, center, voxels, null);
		
		Location location = locationContainer.convert(factory2D, cellContainer);
		assertTrue(location instanceof PottsLocation2D);
	}
	
	@Test
	public void convert_withRegions2D_createsObject() {
		Voxel center = new Voxel(0, 0, 0);
		ArrayList<Voxel> voxels = new ArrayList<>();
		
		EnumMap<Region, ArrayList<Voxel>> regionVoxelMap = new EnumMap<>(Region.class);
		regionVoxelMap.put(Region.DEFAULT, voxels);
		regionVoxelMap.put(Region.NUCLEUS, voxels);
		
		EnumMap<Region, Integer> regionTargetMap = new EnumMap<>(Region.class);
		regionTargetMap.put(Region.DEFAULT, 0);
		regionTargetMap.put(Region.NUCLEUS, 0);
		
		PottsCellContainer cellContainer = new PottsCellContainer(0, 0, 0, regionTargetMap);
		PottsLocationContainer locationContainer = new PottsLocationContainer(0, center, voxels, regionVoxelMap);
		
		Location location = locationContainer.convert(factory2D, cellContainer);
		assertTrue(location instanceof PottsLocations2D);
	}
	
	@Test
	public void convert_noRegions3D_createsObject() {
		Voxel center = new Voxel(0, 0, 0);
		ArrayList<Voxel> voxels = new ArrayList<>();
		PottsCellContainer cellContainer = new PottsCellContainer(0, 0, 0);
		PottsLocationContainer locationContainer = new PottsLocationContainer(0, center, voxels, null);
		
		Location location = locationContainer.convert(factory3D, cellContainer);
		assertTrue(location instanceof PottsLocation3D);
	}
	
	@Test
	public void convert_withRegions3D_createsObject() {
		Voxel center = new Voxel(0, 0, 0);
		ArrayList<Voxel> voxels = new ArrayList<>();
		
		EnumMap<Region, ArrayList<Voxel>> regionVoxelMap = new EnumMap<>(Region.class);
		regionVoxelMap.put(Region.DEFAULT, voxels);
		regionVoxelMap.put(Region.NUCLEUS, voxels);
		
		EnumMap<Region, Integer> regionTargetMap = new EnumMap<>(Region.class);
		regionTargetMap.put(Region.DEFAULT, 0);
		regionTargetMap.put(Region.NUCLEUS, 0);
		
		PottsCellContainer cellContainer = new PottsCellContainer(0, 0, 0, regionTargetMap);
		PottsLocationContainer locationContainer = new PottsLocationContainer(0, center, voxels, regionVoxelMap);
		
		Location location = locationContainer.convert(factory3D, cellContainer);
		assertTrue(location instanceof PottsLocations3D);
	}
}
