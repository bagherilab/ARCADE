package arcade.env.loc;

import org.junit.*;
import java.util.ArrayList;
import java.util.Comparator;
import ec.util.MersenneTwisterFast;
import arcade.util.MiniBox;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.env.loc.Location.Voxel;

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
		
		public void makeCenters(ArrayList<MiniBox> populations) { }
		
		Location makeLocation(MiniBox population, Voxel center, MersenneTwisterFast random) { return null; }
	}
	
	@Test
	public void getLocations_exceedsLocations_setsPotts() {
		MiniBox population = new MiniBox();
		population.put("FRACTION", 2);
		LocationFactoryMock factory = new LocationFactoryMock();
		
		int n = (int)(Math.random()*100) + 1;
		for (int i = 0; i < n; i++) {
			factory.availableLocations.add(new Voxel(i, i, i));
			factory.unavailableLocations.add(new Voxel(i, i, i));
		}
		
		ArrayList<Location> locations = factory.getLocations(population, random);
		assertEquals(n, locations.size());
	}
}
