package arcade.env.loc;

import org.junit.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.EnumMap;
import ec.util.MersenneTwisterFast;
import arcade.sim.Series;
import arcade.sim.output.OutputLoader;
import arcade.util.MiniBox;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.agent.cell.Cell.Region;
import static arcade.env.loc.Location.Voxel;
import static arcade.MainTest.*;
import static arcade.agent.cell.CellFactory.CellContainer;
import static arcade.env.loc.LocationFactory.LocationContainer;
import static arcade.env.loc.LocationFactory.LocationFactoryContainer;
import static arcade.util.MiniBox.TAG_SEPARATOR;

public class LocationFactoryTest {
	final MersenneTwisterFast random = mock(MersenneTwisterFast.class);
	
	static Series createSeries(int length, int width, int height, double[] volumes) {
		Series series = mock(Series.class);
		series._populations = new HashMap<>();
		
		try {
			Field lengthField = Series.class.getDeclaredField("_length");
			lengthField.setAccessible(true);
			lengthField.setInt(series, length);
			
			Field widthField = Series.class.getDeclaredField("_width");
			widthField.setAccessible(true);
			widthField.setInt(series, width);
			
			Field heightField = Series.class.getDeclaredField("_height");
			heightField.setAccessible(true);
			heightField.setInt(series, height);
		} catch (Exception ignored) { }
		
		for (int i = 0; i < volumes.length; i++) {
			int pop = i + 1;
			MiniBox box = new MiniBox();
			box.put("CODE", pop);
			box.put("CRITICAL_VOLUME", volumes[i]);
			series._populations.put("pop" + pop, box);
		}
		
		return series;
	}
	
	static class LocationFactoryMock extends LocationFactory {
		public LocationFactoryMock() { super(); }
		
		int convert(double volume) { return (int)(volume + 1); }
		
		PottsLocation makeLocation(ArrayList<Voxel> voxels) {
			PottsLocation location = mock(PottsLocation.class);
			doReturn(voxels.size()).when(location).getVolume();
			return location;
		}
		
		PottsLocations makeLocations(ArrayList<Voxel> voxels) {
			PottsLocations location = spy(mock(PottsLocations.class));
			doReturn(voxels.size()).when(location).getVolume();
			doNothing().when(location).assign(any(Region.class), any(Voxel.class));
			return location;
		}
		
		ArrayList<Voxel> getNeighbors(Voxel voxel) {
			ArrayList<Voxel> neighbors = new ArrayList<>();
			neighbors.add(new Voxel(voxel.x - 1, 0, 0));
			neighbors.add(new Voxel(voxel.x + 1, 0, 0));
			return neighbors;
		}
		
		ArrayList<Voxel> getSelected(ArrayList<Voxel> voxels, Voxel focus, double n) {
			ArrayList<Voxel> selected = new ArrayList<>();
			for (int i = 0; i < n + focus.x; i++) { selected.add(new Voxel(i, 0, 0)); }
			return selected;
		}
		
		ArrayList<Voxel> getPossible(Voxel focus, int m) {
			ArrayList<Voxel> possible = new ArrayList<>();
			for (int i = 0; i < m; i++) { possible.add(new Voxel(i, 0, 0)); }
			return possible;
		}
		
		ArrayList<Voxel> getCenters(int length, int width, int height, int m) {
			ArrayList<Voxel> centers = new ArrayList<>();
			for (int i = 0; i < length; i++) {
				for (int j = 0; j < width; j++) {
					for (int k = 0; k < height; k++) {
						centers.add(new Voxel(i + m, j + m, k + m));
					}
				}
			}
			return centers;
		}
	}
	
	@Test
	public void initialize_noLoading_callsMethod() {
		LocationFactory factory = spy(new LocationFactoryMock());
		Series series = mock(Series.class);
		series.loader = null;
		
		doNothing().when(factory).loadLocations(series);
		doNothing().when(factory).createLocations(series, random);
		
		factory.initialize(series, random);
		
		verify(factory).createLocations(series, random);
		verify(factory, never()).loadLocations(series);
	}
	
	@Test
	public void initialize_noLoadingWithLoader_callsMethod() {
		LocationFactory factory = spy(new LocationFactoryMock());
		Series series = mock(Series.class);
		series.loader = mock(OutputLoader.class);
		
		try {
			Field field = OutputLoader.class.getDeclaredField("loadLocations");
			field.setAccessible(true);
			field.set(series.loader, false);
		} catch (Exception ignored) { }
		
		doNothing().when(factory).loadLocations(series);
		doNothing().when(factory).createLocations(series, random);
		
		factory.initialize(series, random);
		
		verify(factory).createLocations(series, random);
		verify(factory, never()).loadLocations(series);
	}
	
	@Test
	public void initialize_withLoadingWithLoader_callsMethod() {
		LocationFactory factory = spy(new LocationFactoryMock());
		Series series = mock(Series.class);
		series.loader = mock(OutputLoader.class);
		
		try {
			Field field = OutputLoader.class.getDeclaredField("loadLocations");
			field.setAccessible(true);
			field.set(series.loader, true);
		} catch (Exception ignored) { }
		
		doNothing().when(factory).loadLocations(series);
		doNothing().when(factory).createLocations(series, random);
		
		factory.initialize(series, random);
		
		verify(factory, never()).createLocations(series, random);
		verify(factory).loadLocations(series);
	}
	
	@Test
	public void loadLocations_givenLoaded_updatesList() {
		int N = randomInt();
		ArrayList<LocationContainer> containers = new ArrayList<>();
		ArrayList<ArrayList<Voxel>> allVoxels = new ArrayList<>();
		for (int i = 0; i < N; i++) {
			ArrayList<Voxel> voxels = new ArrayList<>();
			voxels.add(new Voxel(i, i, i));
			voxels.add(new Voxel(i, 0, 0));
			voxels.add(new Voxel(0, i, 0));
			voxels.add(new Voxel(0, 0, i));
			LocationContainer container = new LocationContainer(i, new Voxel(i, i, i), voxels, null);
			containers.add(container);
			allVoxels.add(voxels);
		}
		
		LocationFactoryMock factory = new LocationFactoryMock();
		Series series = mock(Series.class);
		series.loader = mock(OutputLoader.class);
		
		doAnswer(invocation -> {
			factory.container = new LocationFactoryContainer();
			for (int i = 0; i < N; i++) { factory.container.locations.add(containers.get(i)); }
			return null;
		}).when(series.loader).load(factory);
		
		factory.loadLocations(series);
		assertEquals(N, factory.locations.size());
		for (int i = 0; i < N; i++) {
			assertEquals(new Voxel(i, i, i), factory.container.locations.get(i).center);
			assertEquals(allVoxels.get(i), factory.container.locations.get(i).voxels);
		}
	}
	
	@Test
	public void createLocations_noPopulation_createsEmpty() {
		Series series = createSeries(0, 0, 0, new double[] { });
		
		LocationFactoryMock factory = new LocationFactoryMock();
		factory.createLocations(series, random);
		
		assertEquals(0, factory.locations.size());
	}
	
	@Test
	public void createLocations_onePopulationNoRegions_createsList() {
		int length = randomInt();
		int width = randomInt();
		int height = randomInt();
		int volume = randomInt();
		Series series = createSeries(length, width, height, new double[] { volume });
		
		LocationFactoryMock factory = new LocationFactoryMock();
		factory.createLocations(series, random);
		
		assertEquals(length*width*height, factory.locations.values().size());
		for (LocationContainer container : factory.locations.values()) {
			assertTrue(container.center.x <= length + volume + 3);
			assertTrue(container.center.x >= volume + 3);
			assertTrue(container.center.y <= width + volume + 3);
			assertTrue(container.center.y >= volume + 3);
			assertTrue(container.center.z <= height + volume + 3);
			assertTrue(container.center.z >= volume + 3);
			assertEquals(volume + 3, container.voxels.size());
		}
	}
	
	@Test
	public void createLocations_onePopulationWithRegions_createsList() {
		int length = randomInt();
		int width = randomInt();
		int height = randomInt();
		int volume = randomInt();
		Series series = createSeries(length, width, height, new double[] { volume });
		
		series._populations.get("pop1").put("REGION" + TAG_SEPARATOR + "DEFAULT", 0.0);
		series._populations.get("pop1").put("REGION" + TAG_SEPARATOR + "NUCLEUS", 0.0);
		
		LocationFactoryMock factory = new LocationFactoryMock();
		factory.createLocations(series, random);
		
		assertEquals(length*width*height, factory.locations.values().size());
		for (LocationContainer container : factory.locations.values()) {
			assertTrue(container.center.x <= length + volume + 3);
			assertTrue(container.center.x >= volume + 3);
			assertTrue(container.center.y <= width + volume + 3);
			assertTrue(container.center.y >= volume + 3);
			assertTrue(container.center.z <= height + volume + 3);
			assertTrue(container.center.z >= volume + 3);
			assertEquals(volume + 3, container.voxels.size());
			assertEquals(2, container.regions.size());
			
			ArrayList<Voxel> regionVoxels = new ArrayList<>(container.voxels);
			regionVoxels.remove(new Voxel(volume + 2, 0, 0));
			regionVoxels.remove(new Voxel(volume + 1, 0, 0));
			
			assertEquals(regionVoxels, container.regions.get(Region.DEFAULT));
			assertEquals(regionVoxels, container.regions.get(Region.NUCLEUS));
		}
	}
	
	@Test
	public void createLocations_multiplePopulationsNoRegions_createsList() {
		int length = randomInt();
		int width = randomInt();
		int height = randomInt();
		int volume1 = randomInt();
		int volume2 = volume1 + randomInt();
		int volume3 = volume1 - randomInt();
		Series series = createSeries(length, width, height, new double[] { volume1, volume2, volume3 });
		
		LocationFactoryMock factory = new LocationFactoryMock();
		factory.createLocations(series, random);
		
		assertEquals(length*width*height, factory.locations.values().size());
		for (LocationContainer container : factory.locations.values()) {
			assertTrue(container.center.x <= length + volume2 + 3);
			assertTrue(container.center.x >= volume2 + 3);
			assertTrue(container.center.y <= width + volume2 + 3);
			assertTrue(container.center.y >= volume2 + 3);
			assertTrue(container.center.z <= height + volume2 + 3);
			assertTrue(container.center.z >= volume2 + 3);
			assertEquals(volume2 + 3, container.voxels.size());
		}
	}
	
	@Test
	public void createLocations_multiplePopulationsWithRegions_createsList() {
		int length = randomInt();
		int width = randomInt();
		int height = randomInt();
		int volume1 = randomInt();
		int volume2 = volume1 + randomInt();
		int volume3 = volume1 - randomInt();
		Series series = createSeries(length, width, height, new double[] { volume1, volume2, volume3 });
		
		series._populations.get("pop1").put("REGION" + TAG_SEPARATOR + "DEFAULT", 0.0);
		series._populations.get("pop1").put("REGION" + TAG_SEPARATOR + "NUCLEUS", 0.0);
		
		LocationFactoryMock factory = new LocationFactoryMock();
		factory.createLocations(series, random);
		
		assertEquals(length*width*height, factory.locations.values().size());
		for (LocationContainer container : factory.locations.values()) {
			assertTrue(container.center.x <= length + volume2 + 3);
			assertTrue(container.center.x >= volume2 + 3);
			assertTrue(container.center.y <= width + volume2 + 3);
			assertTrue(container.center.y >= volume2 + 3);
			assertTrue(container.center.z <= height + volume2 + 3);
			assertTrue(container.center.z >= volume2 + 3);
			assertEquals(volume2 + 3, container.voxels.size());
			assertEquals(2, container.regions.size());
			
			ArrayList<Voxel> regionVoxels = new ArrayList<>(container.voxels);
			regionVoxels.remove(new Voxel(volume2 + 2, 0, 0));
			regionVoxels.remove(new Voxel(volume2 + 1, 0, 0));
			
			assertEquals(regionVoxels, container.regions.get(Region.DEFAULT));
			assertEquals(regionVoxels, container.regions.get(Region.NUCLEUS));
		}
	}
	
	@Test
	public void make_noRegions_createsObject() {
		LocationFactory factory = new LocationFactoryMock();
		
		int N = 100;
		for (int i = 1; i < N; i++) {
			Voxel center = new Voxel(0, 0, 0);
			ArrayList<Voxel> voxels = factory.getPossible(center, N);
			CellContainer cellContainer = new CellContainer(0, 0, i);
			LocationContainer locationContainer = new LocationContainer(0, center, voxels, null);
			
			Location location = factory.make(locationContainer, cellContainer, random);
			assertEquals(i, location.getVolume());
			assertTrue(location instanceof PottsLocation);
		}
	}
	
	@Test
	public void make_noRegionsWithIncrease_createsObject() {
		LocationFactory factory = new LocationFactoryMock();
		
		int N = 100;
		for (int i = 2; i < N; i++) {
			Voxel center = new Voxel(-1, 0, 0);
			ArrayList<Voxel> voxels = factory.getPossible(center, N);
			CellContainer cellContainer = new CellContainer(0, 0, i);
			LocationContainer locationContainer = new LocationContainer(0, center, voxels, null);
			
			Location location = factory.make(locationContainer, cellContainer, random);
			assertEquals(i, location.getVolume());
			assertTrue(location instanceof PottsLocation);
		}
	}
	
	@Test
	public void make_noRegionsWithDecrease_createsObject() {
		LocationFactory factory = new LocationFactoryMock();
		
		int N = 100;
		for (int i = 2; i < N; i++) {
			Voxel center = new Voxel(1, 0, 0);
			ArrayList<Voxel> voxels = factory.getPossible(center, N);
			CellContainer cellContainer = new CellContainer(0, 0, i);
			LocationContainer locationContainer = new LocationContainer(0, center, voxels, null);
			
			Location location = factory.make(locationContainer, cellContainer, random);
			assertEquals(i, location.getVolume());
			assertTrue(location instanceof PottsLocation);
		}
	}
	
	@Test
	public void make_withRegions_createsObject() {
		LocationFactory factory = new LocationFactoryMock();
		
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
			
			CellContainer cellContainer = new CellContainer(0, 0, N, regionTargetMap);
			LocationContainer locationContainer = new LocationContainer(0, center, voxels, regionVoxelMap);
			
			Location location = factory.make(locationContainer, cellContainer, random);
			assertEquals(N, location.getVolume());
			verify(location, times(N - i)).assign(eq(Region.NUCLEUS), any(Voxel.class));
			assertTrue(location instanceof PottsLocations);
		}
	}
	
	@Test
	public void make_withRegionsWithIncrease_createsObject() {
		LocationFactory factory = new LocationFactoryMock();
		
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
			
			CellContainer cellContainer = new CellContainer(0, 0, N, regionTargetMap);
			LocationContainer locationContainer = new LocationContainer(0, center, voxels, regionVoxelMap);
			
			Location location = factory.make(locationContainer, cellContainer, random);
			assertEquals(N, location.getVolume());
			verify(location, times(N - i)).assign(eq(Region.NUCLEUS), any(Voxel.class));
			assertTrue(location instanceof PottsLocations);
		}
	}
	
	@Test
	public void make_withRegionsWithDecrease_createsObject() {
		LocationFactory factory = new LocationFactoryMock();
		
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
			
			CellContainer cellContainer = new CellContainer(0, 0, N, regionTargetMap);
			LocationContainer locationContainer = new LocationContainer(0, center, voxels, regionVoxelMap);
			
			Location location = factory.make(locationContainer, cellContainer, random);
			
			assertEquals(N, location.getVolume());
			verify(location, times(N - i)).assign(eq(Region.NUCLEUS), any(Voxel.class));
			assertTrue(location instanceof PottsLocations);
		}
	}
}
