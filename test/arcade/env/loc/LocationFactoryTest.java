package arcade.env.loc;

import org.junit.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import ec.util.MersenneTwisterFast;
import arcade.sim.Series;
import arcade.sim.output.OutputLoader;
import arcade.util.MiniBox;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.agent.cell.Cell.*;
import static arcade.env.loc.Location.Voxel;
import static arcade.MainTest.*;
import static arcade.agent.cell.CellFactory.CellContainer;
import static arcade.env.loc.LocationFactory.LocationContainer;
import static arcade.env.loc.LocationFactory.LocationFactoryContainer;
import static arcade.util.MiniBox.TAG_SEPARATOR;

public class LocationFactoryTest {
	static final Comparator<Voxel> COMPARATOR = (v1, v2) ->
			v1.z != v2.z ? Integer.compare(v1.z, v2.z) :
			v1.x != v2.x ? Integer.compare(v1.x, v2.x) :
					Integer.compare(v1.y, v2.y);
	
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
			doNothing().when(location).assign(anyInt(), any(Voxel.class));
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
	public void initialize_withLoading_callsMethod() {
		LocationFactory factory = spy(new LocationFactoryMock());
		Series series = mock(Series.class);
		series.loader = mock(OutputLoader.class);
		
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
	public void createLocations_onePopulationNoTags_createsList() {
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
	public void createLocations_onePopulationWithTags_createsList() {
		int length = randomInt();
		int width = randomInt();
		int height = randomInt();
		int volume = randomInt();
		Series series = createSeries(length, width, height, new double[] { volume });
		
		series._populations.get("pop1").put("TAG" + TAG_SEPARATOR + "A", 0.0);
		series._populations.get("pop1").put("TAG" + TAG_SEPARATOR + "B", 0.0);
		series._populations.get("pop1").put("TAG" + TAG_SEPARATOR + "C", 0.0);
		
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
			assertEquals(3, container.tags.size());
			
			ArrayList<Voxel> tagVoxels = new ArrayList<>(container.voxels);
			tagVoxels.remove(new Voxel(volume + 2, 0, 0));
			tagVoxels.remove(new Voxel(volume + 1, 0, 0));
			
			assertEquals(tagVoxels, container.tags.get("A"));
			assertEquals(tagVoxels, container.tags.get("B"));
			assertEquals(tagVoxels, container.tags.get("C"));
		}
	}
	
	@Test
	public void createLocations_multiplePopulationsNoTags_createsList() {
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
	public void createLocations_multiplePopulationsWithTags_createsList() {
		int length = randomInt();
		int width = randomInt();
		int height = randomInt();
		int volume1 = randomInt();
		int volume2 = volume1 + randomInt();
		int volume3 = volume1 - randomInt();
		Series series = createSeries(length, width, height, new double[] { volume1, volume2, volume3 });
		
		series._populations.get("pop1").put("TAG" + TAG_SEPARATOR + "A", 0.0);
		series._populations.get("pop2").put("TAG" + TAG_SEPARATOR + "B", 0.0);
		series._populations.get("pop3").put("TAG" + TAG_SEPARATOR + "C", 0.0);
		
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
			assertEquals(3, container.tags.size());
			
			ArrayList<Voxel> tagVoxels = new ArrayList<>(container.voxels);
			tagVoxels.remove(new Voxel(volume2 + 2, 0, 0));
			tagVoxels.remove(new Voxel(volume2 + 1, 0, 0));
			
			assertEquals(tagVoxels, container.tags.get("A"));
			assertEquals(tagVoxels, container.tags.get("B"));
			assertEquals(tagVoxels, container.tags.get("C"));
		}
	}
	
	@Test
	public void make_noTags_createsObject() {
		LocationFactory factory = new LocationFactoryMock();
		
		int N = 100;
		for (int i = 1; i < N; i++) {
			Voxel center = new Voxel(0, 0, 0);
			ArrayList<Voxel> voxels = factory.getPossible(center, N);
			CellContainer cellContainer = new CellContainer(0, 0, 0, i);
			LocationContainer locationContainer = new LocationContainer(0, center, voxels, null);
			
			Location location = factory.make(locationContainer, cellContainer, random);
			assertEquals(i, location.getVolume());
			assertTrue(location instanceof PottsLocation);
		}
	}
	
	@Test
	public void make_noTagsWithIncrease_createsObject() {
		LocationFactory factory = new LocationFactoryMock();
		
		int N = 100;
		for (int i = 2; i < N; i++) {
			Voxel center = new Voxel(-1, 0, 0);
			ArrayList<Voxel> voxels = factory.getPossible(center, N);
			CellContainer cellContainer = new CellContainer(0, 0, 0, i);
			LocationContainer locationContainer = new LocationContainer(0, center, voxels, null);
			
			Location location = factory.make(locationContainer, cellContainer, random);
			assertEquals(i, location.getVolume());
			assertTrue(location instanceof PottsLocation);
		}
	}
	
	@Test
	public void make_noTagsWithDecrease_createsObject() {
		LocationFactory factory = new LocationFactoryMock();
		
		int N = 100;
		for (int i = 2; i < N; i++) {
			Voxel center = new Voxel(1, 0, 0);
			ArrayList<Voxel> voxels = factory.getPossible(center, N);
			CellContainer cellContainer = new CellContainer(0, 0, 0, i);
			LocationContainer locationContainer = new LocationContainer(0, center, voxels, null);
			
			Location location = factory.make(locationContainer, cellContainer, random);
			assertEquals(i, location.getVolume());
			assertTrue(location instanceof PottsLocation);
		}
	}
	
	@Test
	public void make_withTags_createsObject() {
		LocationFactory factory = new LocationFactoryMock();
		
		int N = 100;
		for (int i = 0; i < N; i++) {
			Voxel center = new Voxel(0, 0, 0);
			ArrayList<Voxel> voxels = factory.getPossible(center, N);
			
			HashMap<String, ArrayList<Voxel>> tagVoxelMap = new HashMap<>();
			tagVoxelMap.put("CYTOPLASM", voxels);
			tagVoxelMap.put("NUCLEUS", voxels);
			
			HashMap<String, Integer> tagTargetMap = new HashMap<>();
			tagTargetMap.put("CYTOPLASM", i);
			tagTargetMap.put("NUCLEUS", N - i);
			
			CellContainer cellContainer = new CellContainer(0, 0, 0, N, tagTargetMap);
			LocationContainer locationContainer = new LocationContainer(0, center, voxels, tagVoxelMap);
			
			Location location = factory.make(locationContainer, cellContainer, random);
			assertEquals(N, location.getVolume());
			verify(location, times(N - i)).assign(eq(TAG_NUCLEUS), any(Voxel.class));
			assertTrue(location instanceof PottsLocations);
		}
	}
	
	@Test
	public void make_withTagsWithIncrease_createsObject() {
		LocationFactory factory = new LocationFactoryMock();
		
		int N = 100;
		for (int i = 0; i < N - 1; i++) {
			Voxel center = new Voxel(-1, 0, 0);
			ArrayList<Voxel> voxels = factory.getPossible(center, N);
			
			HashMap<String, ArrayList<Voxel>> tagVoxelMap = new HashMap<>();
			tagVoxelMap.put("CYTOPLASM", voxels);
			tagVoxelMap.put("NUCLEUS", voxels);
			
			HashMap<String, Integer> tagTargetMap = new HashMap<>();
			tagTargetMap.put("CYTOPLASM", i);
			tagTargetMap.put("NUCLEUS", N - i);
			
			CellContainer cellContainer = new CellContainer(0, 0, 0, N, tagTargetMap);
			LocationContainer locationContainer = new LocationContainer(0, center, voxels, tagVoxelMap);
			
			Location location = factory.make(locationContainer, cellContainer, random);
			assertEquals(N, location.getVolume());
			verify(location, times(N - i)).assign(eq(TAG_NUCLEUS), any(Voxel.class));
			assertTrue(location instanceof PottsLocations);
		}
	}
	
	@Test
	public void make_withTagsWithDecrease_createsObject() {
		LocationFactory factory = new LocationFactoryMock();
		
		int N = 100;
		for (int i = 0; i < N - 1; i++) {
			Voxel center = new Voxel(1, 0, 0);
			ArrayList<Voxel> voxels = factory.getPossible(center, N);
			
			HashMap<String, ArrayList<Voxel>> tagVoxelMap = new HashMap<>();
			tagVoxelMap.put("CYTOPLASM", voxels);
			tagVoxelMap.put("NUCLEUS", voxels);
			
			HashMap<String, Integer> tagTargetMap = new HashMap<>();
			tagTargetMap.put("CYTOPLASM", i);
			tagTargetMap.put("NUCLEUS", N - i);
			
			CellContainer cellContainer = new CellContainer(0, 0, 0, N, tagTargetMap);
			LocationContainer locationContainer = new LocationContainer(0, center, voxels, tagVoxelMap);
			
			Location location = factory.make(locationContainer, cellContainer, random);
			
			assertEquals(N, location.getVolume());
			verify(location, times(N - i)).assign(eq(TAG_NUCLEUS), any(Voxel.class));
			assertTrue(location instanceof PottsLocations);
		}
	}
}
