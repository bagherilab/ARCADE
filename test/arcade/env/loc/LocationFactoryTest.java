package arcade.env.loc;

import org.junit.*;
import java.util.ArrayList;
import java.util.Comparator;
import ec.util.MersenneTwisterFast;
import arcade.sim.Simulation;
import arcade.util.MiniBox;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.agent.cell.Cell.*;
import static arcade.env.loc.Location.Voxel;
import static arcade.MainTest.*;

public class LocationFactoryTest {
	static final Comparator<Voxel> COMPARATOR = (v1, v2) ->
			v1.z != v2.z ? Integer.compare(v1.z, v2.z) :
			v1.x != v2.x ? Integer.compare(v1.x, v2.x) :
					Integer.compare(v1.y, v2.y);
	
	final MersenneTwisterFast random = mock(MersenneTwisterFast.class);
	
	static ArrayList<MiniBox> createPopulations(double[] volumes) {
		ArrayList<MiniBox> populations = new ArrayList<>();
		
		for (double v : volumes) {
			MiniBox box = new MiniBox();
			box.put("CRITICAL_VOLUME", v);
			populations.add(box);
		}
		
		return populations;
	}
	
	static class LocationFactoryMock extends LocationFactory {
		public LocationFactoryMock() { super(0, 0, 0); }
		
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
		
		void getCenters(int m) { }
	}
	
	@Test
	public void getCount_noLocation_returnsZero() {
		LocationFactoryMock factory = new LocationFactoryMock();
		assertEquals(0, factory.getCount());
	}
	
	@Test
	public void getCount_hasLocations_returnsCount() {
		LocationFactoryMock factory = new LocationFactoryMock();
		
		int available = randomInt();
		int unavailable = randomInt();
		
		for (int i = 0; i < available; i++) { factory.availableLocations.add(new Voxel(i, 0, 0)); }
		for (int i = 0; i < unavailable; i++) { factory.availableLocations.add(new Voxel(0, 0, i)); }
				
		assertEquals(available + unavailable, factory.getCount());
	}
	
	@Test
	public void makeCenters_noPopulation_createsEmpty() {
		LocationFactoryMock factory = new LocationFactoryMock();
		factory.makeCenters(new ArrayList<>());
		assertEquals(0, factory.availableLocations.size());
	}
	
	@Test
	public void makeCenters_onePopulation_callsMethod() {
		int volume = randomInt();
		ArrayList<MiniBox> populations = createPopulations(new double[] { volume });
		LocationFactory factory = spy(new LocationFactoryMock());
		factory.makeCenters(populations);
		verify(factory).getCenters(volume + 3);
	}
	
	@Test
	public void makeCenters_multiplePopulation_callsMethod() {
		int volume1 = randomInt();
		int volume2 = volume1 + randomInt();
		int volume3 = volume1 - randomInt();
		ArrayList<MiniBox> populations = createPopulations(new double[] { volume1, volume2, volume3 });
		LocationFactory factory = spy(new LocationFactoryMock());
		factory.makeCenters(populations);
		verify(factory).getCenters(volume2 + 3);
	}
	
	@Test
	public void createLocation_noTags_createsObject() {
		LocationFactory factory = new LocationFactoryMock();
		MiniBox population = new MiniBox();
		population.put("CODE", 0);
		
		int N = 100;
		for (int i = 1; i < N; i++) {
			population.put("CRITICAL_VOLUME", i*Simulation.DS);
			Location location = factory.createLocation(population, new Voxel(0, 0, 0), random);
			
			assertEquals(i, location.getVolume());
			assertTrue(location instanceof PottsLocation);
		}
	}
	
	@Test
	public void createLocation_noTagsWithIncrease_createsObject() {
		LocationFactory factory = new LocationFactoryMock();
		MiniBox population = new MiniBox();
		population.put("CODE", 0);
		
		int N = 100;
		for (int i = 2; i < N; i++) {
			population.put("CRITICAL_VOLUME", i*Simulation.DS);
			Location location = factory.createLocation(population, new Voxel(-1, 0, 0), random);
			
			assertEquals(i, location.getVolume());
			assertTrue(location instanceof PottsLocation);
		}
	}
	
	@Test
	public void createLocation_noTagsWithDecrease_createsObject() {
		LocationFactory factory = new LocationFactoryMock();
		MiniBox population = new MiniBox();
		population.put("CODE", 0);
		
		int N = 100;
		for (int i = 2; i < N; i++) {
			population.put("CRITICAL_VOLUME", i*Simulation.DS);
			Location location = factory.createLocation(population, new Voxel(1, 0, 0), random);
			
			assertEquals(i, location.getVolume());
			assertTrue(location instanceof PottsLocation);
		}
	}
	
	@Test
	public void createLocation_withTags_createsObject() {
		LocationFactory factory = new LocationFactoryMock();
		MiniBox population = new MiniBox();
		population.put("CODE", 0);
		
		int N = 100;
		for (int i = 0; i < N; i++) {
			population.put("TAG/CYTOPLASM", i/(double)N);
			population.put("TAG/NUCLEUS", (N - i)/(double)N);
			population.put("TAG/OTHER", 0);
			population.put("CRITICAL_VOLUME", N*Simulation.DS);
			Location location = factory.createLocation(population, new Voxel(0, 0, 0), random);
			
			assertEquals(N, location.getVolume());
			verify(location, times(N - i)).assign(eq(TAG_NUCLEUS), any(Voxel.class));
			assertTrue(location instanceof PottsLocations);
		}
	}
	
	@Test
	public void createLocation_withTagsWithIncrease_createsObject() {
		LocationFactory factory = new LocationFactoryMock();
		MiniBox population = new MiniBox();
		population.put("CODE", 0);
		
		int N = 100;
		for (int i = 0; i < N - 1; i++) {
			population.put("TAG/CYTOPLASM", i/(double)N);
			population.put("TAG/NUCLEUS", (N - i)/(double)N);
			population.put("TAG/OTHER", 0);
			population.put("CRITICAL_VOLUME", N*Simulation.DS);
			Location location = factory.createLocation(population, new Voxel(-1, 0, 0), random);
			
			assertEquals(N, location.getVolume());
			verify(location, times(N - i)).assign(eq(TAG_NUCLEUS), any(Voxel.class));
			assertTrue(location instanceof PottsLocations);
		}
	}
	
	@Test
	public void createLocation_withTagsWithDecrease_createsObject() {
		LocationFactory factory = new LocationFactoryMock();
		MiniBox population = new MiniBox();
		population.put("CODE", 0);
		
		int N = 100;
		for (int i = 0; i < N - 1; i++) {
			population.put("TAG/CYTOPLASM", i/(double)N);
			population.put("TAG/NUCLEUS", (N - i)/(double)N);
			population.put("TAG/OTHER", 0);
			population.put("CRITICAL_VOLUME", N*Simulation.DS);
			Location location = factory.createLocation(population, new Voxel(1, 0, 0), random);
			
			assertEquals(N, location.getVolume());
			verify(location, times(N - i)).assign(eq(TAG_NUCLEUS), any(Voxel.class));
			assertTrue(location instanceof PottsLocations);
		}
	}
	
	@Test
	public void make_hasLocations_returnsLocation() {
		MiniBox population = new MiniBox();
		LocationFactory factory = spy(new LocationFactoryMock());
		
		int i = randomInt();
		Location expected = mock(Location.class);
		Voxel center = new Voxel(i, i, i);
		factory.availableLocations.add(center);
		doReturn(expected).when(factory).createLocation(population, center, random);
		
		assertEquals(1, factory.availableLocations.size());
		assertEquals(0, factory.unavailableLocations.size());
		assertEquals(center, factory.availableLocations.get(0));
		
		Location location = factory.make(0, population, random);
		assertEquals(expected, location);
		assertEquals(0, factory.availableLocations.size());
		assertEquals(1, factory.unavailableLocations.size());
		assertEquals(center, factory.unavailableLocations.get(0));
		
	}
}
