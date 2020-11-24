package arcade.potts.sim.output;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import com.google.gson.*;
import com.google.gson.internal.bind.TreeTypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;
import java.lang.reflect.Type;
import java.util.*;
import sim.util.Bag;
import arcade.core.env.grid.Grid;
import arcade.core.env.loc.*;
import arcade.core.util.MiniBox;
import arcade.potts.sim.*;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.agent.module.PottsModule;
import arcade.potts.env.grid.PottsGrid;
import arcade.potts.env.loc.*;
import static arcade.core.agent.cell.Cell.State;
import static arcade.core.agent.cell.Cell.Region;
import static arcade.potts.agent.module.PottsModule.Phase;
import static arcade.potts.sim.output.PottsOutputSerializer.*;
import static arcade.core.TestUtilities.*;

public class PottsOutputSerializerTest {
	@Test
	public void makeGSON_registersAdaptors() {
		Gson gson = PottsOutputSerializer.makeGSON();
		
		TypeToken<Potts> potts = new TypeToken<Potts>() {};
		assertSame(gson.getAdapter(potts).getClass(), TreeTypeAdapter.class);
		
		TypeToken<PottsGrid> grid = new TypeToken<PottsGrid>() {};
		assertSame(gson.getAdapter(grid).getClass(), TreeTypeAdapter.class);
		
		TypeToken<PottsCell> cell = new TypeToken<PottsCell>() {};
		assertSame(gson.getAdapter(cell).getClass(), TreeTypeAdapter.class);
		
		TypeToken<PottsLocation> location = new TypeToken<PottsLocation>() {};
		assertSame(gson.getAdapter(location).getClass(), TreeTypeAdapter.class);
		
		TypeToken<Voxel> voxel = new TypeToken<Voxel>() {};
		assertSame(gson.getAdapter(voxel).getClass(), TreeTypeAdapter.class);
	}
	
	@Test
	public void serialize_forSeries_createsJSON() {
		PottsSeriesSerializer serializer = new PottsSeriesSerializer();
		PottsSeries series = mock(PottsSeries.class);
		
		series._potts = new MiniBox();
		
		String key1 = randomString();
		String value1 = randomString();
		series._potts.put(key1, value1);
		
		String key2 = randomString();
		int value2 = randomIntBetween(0, 10);
		series._potts.put(key2, value2);
		
		JsonSerializationContext context = new JsonSerializationContext() {
			public JsonElement serialize(Object src) {
				if (src instanceof MiniBox) {
					MiniBox box = (MiniBox)src;
					JsonObject json = new JsonObject();
					json.addProperty(box.getKeys().get(0), box.get(box.getKeys().get(0)));
					json.addProperty(box.getKeys().get(1), box.getInt(box.getKeys().get(1)));
					return json;
				}
				return null;
			}
			
			public JsonElement serialize(Object src, Type typeOfSrc) {
				JsonObject json = new JsonObject();
				json.addProperty("SERIES", "SERIES");
				return json;
			}
		};
		
		String expected = "{"
				+ "\"SERIES\":\"SERIES\","
				+ "\"potts\":{" 
				+ "\"" + key1 + "\":\"" + value1 + "\","
				+ "\"" + key2 + "\":" + value2
				+ "}"
				+ "}";
		
		JsonElement json = serializer.serialize(series, null, context);
		assertEquals(expected, json.toString());
	}
	
	@Test
	public void serialize_forPotts_createsJSON() {
		PottsSerializer serializer = new PottsSerializer();
		Potts potts = mock(Potts.class);
		
		Grid grid = mock(Grid.class);
		potts.grid = grid;
		
		Bag bag = new Bag();
		doReturn(bag).when(grid).getAllObjects();
		
		PottsCell cell1 = mock(PottsCell.class);
		PottsCell cell2 = mock(PottsCell.class);
		bag.add(cell1);
		bag.add(cell2);
		
		int id1 = randomIntBetween(1,100);
		int id2 = randomIntBetween(1,100);
		doReturn(id1).when(cell1).getID();
		doReturn(id2).when(cell2).getID();
		
		PottsLocation location1 = mock(PottsLocation.class);
		PottsLocation location2 = mock(PottsLocation.class);
		doReturn(location1).when(cell1).getLocation();
		doReturn(location2).when(cell2).getLocation();
		
		int x1 = randomIntBetween(1,100);
		int y1 = randomIntBetween(1,100);
		int z1 = randomIntBetween(1,100);
		doReturn(new Voxel(x1, y1, z1)).when(location1).getCenter();
		
		int x2 = randomIntBetween(1,100);
		int y2 = randomIntBetween(1,100);
		int z2 = randomIntBetween(1,100);
		doReturn(new Voxel(x2, y2, z2)).when(location2).getCenter();
		
		JsonSerializationContext context = new JsonSerializationContext() {
			public JsonElement serialize(Object src) {
				if (src instanceof Voxel) {
					Voxel voxel = (Voxel)src;
					JsonArray json = new JsonArray();
					json.add(voxel.x);
					json.add(voxel.y);
					json.add(voxel.z);
					return json;
				} else {
					PottsLocation location = (PottsLocation)src;
					JsonArray json = new JsonArray();
					json.add(location.getCenter().x);
					json.add(location.getCenter().y);
					json.add(location.getCenter().z);
					return json;
				}
			}
			
			public JsonElement serialize(Object src, Type typeOfSrc) { return null; }
		};
		
		String expected = "["
				+ "{\"id\":" + id1
				+ ",\"center\":[" + x1 + "," + y1 + "," + z1 + "]" 
				+ ",\"location\":[" + x1 + "," + y1 + "," + z1 + "]},"
				+ "{\"id\":" + id2
				+ ",\"center\":[" + x2 + "," + y2 + "," + z2 + "]"
				+ ",\"location\":[" + x2 + "," + y2 + "," + z2 + "]}"
				+ "]";
		
		JsonElement json = serializer.serialize(potts, null, context);
		assertEquals(expected, json.toString());
	}
	
	@Test
	public void serialize_forCellNoRegion_createsJSON() {
		PottsCellSerializer serializer = new PottsCellSerializer();
		PottsCell cell = mock(PottsCell.class);
		
		int id = randomIntBetween(1,100);
		int pop = randomIntBetween(1,100);
		int age = randomIntBetween(1,100);
		doReturn(id).when(cell).getID();
		doReturn(pop).when(cell).getPop();
		doReturn(age).when(cell).getAge();
		
		Location location = mock(Location.class);
		doReturn(location).when(cell).getLocation();
		
		int voxels = randomIntBetween(1,100);
		int targetVolume = randomIntBetween(1,100);
		int targetSurface = randomIntBetween(1,100);
		doReturn(voxels).when(location).getVolume();
		doReturn((double)targetVolume).when(cell).getTargetVolume();
		doReturn((double)targetSurface).when(cell).getTargetSurface();
		
		PottsModule module = mock(PottsModule.class);
		doReturn(module).when(cell).getModule();
		
		State state = State.values()[(int)(Math.random()*State.values().length)];
		Phase phase = Phase.values()[(int)(Math.random()*Phase.values().length)];
		doReturn(state).when(cell).getState();
		doReturn(phase).when((PottsModule)module).getPhase();
		
		doReturn(false).when(cell).hasRegions();
		doReturn(null).when(location).getRegions();
		
		String expected = "{"
				+ "\"id\":" + id + ","
				+ "\"pop\":" + pop + ","
				+ "\"age\":" + age + ","
				+ "\"state\":\"" + state.name() + "\","
				+ "\"phase\":\"" + phase.name() + "\","
				+ "\"voxels\":" + voxels + ","
				+ "\"targets\":[" + targetVolume + ".0," + targetSurface + ".0]"
				+ "}";
		
		JsonElement json = serializer.serialize(cell, null, null);
		assertEquals(expected, json.toString());
	}
	
	@Test
	public void serialize_forCellWithRegion_createsJSON() {
		PottsCellSerializer serializer = new PottsCellSerializer();
		PottsCell cell = mock(PottsCell.class);
		
		int id = randomIntBetween(1,100);
		int pop = randomIntBetween(1,100);
		int age = randomIntBetween(1,100);
		doReturn(id).when(cell).getID();
		doReturn(pop).when(cell).getPop();
		doReturn(age).when(cell).getAge();
		
		Location location = mock(Location.class);
		doReturn(location).when(cell).getLocation();
		
		int voxels = randomIntBetween(1,100);
		int targetVolume = randomIntBetween(1,100);
		int targetSurface = randomIntBetween(1,100);
		doReturn(voxels).when(location).getVolume();
		doReturn((double)targetVolume).when(cell).getTargetVolume();
		doReturn((double)targetSurface).when(cell).getTargetSurface();
		
		PottsModule module = mock(PottsModule.class);
		doReturn(module).when(cell).getModule();
		
		State state = State.values()[(int)(Math.random()*State.values().length)];
		Phase phase = Phase.values()[(int)(Math.random()*Phase.values().length)];
		doReturn(state).when(cell).getState();
		doReturn(phase).when((PottsModule)module).getPhase();
		
		doReturn(true).when(cell).hasRegions();
		
		EnumSet<Region> regions = EnumSet.of(Region.NUCLEUS, Region.DEFAULT);
		doReturn(regions).when(location).getRegions();
		
		int volume1 = randomIntBetween(1,100);
		int targetVolume1 = randomIntBetween(1,100);
		doReturn(volume1).when(location).getVolume(Region.DEFAULT);
		doReturn((double)targetVolume1).when(cell).getTargetVolume(Region.DEFAULT);
		
		int surface1 = randomIntBetween(1,100);
		int targetSurface1 = randomIntBetween(1,100);
		doReturn(surface1).when(location).getSurface(Region.DEFAULT);
		doReturn((double)targetSurface1).when(cell).getTargetSurface(Region.DEFAULT);
		
		int volume2 = randomIntBetween(1,100);
		int targetVolume2 = randomIntBetween(1,100);
		doReturn(volume2).when(location).getVolume(Region.NUCLEUS);
		doReturn((double)targetVolume2).when(cell).getTargetVolume(Region.NUCLEUS);
		
		int surface2 = randomIntBetween(1,100);
		int targetSurface2 = randomIntBetween(1,100);
		doReturn(surface2).when(location).getSurface(Region.NUCLEUS);
		doReturn((double)targetSurface2).when(cell).getTargetSurface(Region.NUCLEUS);
		
		String expected = "{"
				+ "\"id\":" + id + ","
				+ "\"pop\":" + pop + ","
				+ "\"age\":" + age + ","
				+ "\"state\":\"" + state.name() + "\","
				+ "\"phase\":\"" + phase.name() + "\","
				+ "\"voxels\":" + voxels + ","
				+ "\"targets\":[" + targetVolume + ".0," + targetSurface + ".0],"
				+ "\"regions\":["
				+ "{\"region\":\"DEFAULT\","
				+ "\"voxels\":" + volume1 + ","
				+ "\"targets\":[" + targetVolume1 + ".0," + targetSurface1 + ".0]"
				+ "},"
				+ "{\"region\":\"NUCLEUS\","
				+ "\"voxels\":" + volume2 + ","
				+ "\"targets\":[" + targetVolume2 + ".0," + targetSurface2 + ".0]"
				+ "}]"
				+ "}";
		
		JsonElement json = serializer.serialize(cell, null, null);
		assertEquals(expected, json.toString());
	}
	
	@Test
	public void serialize_forGrid_createsJSON() {
		PottsGridSerializer serializer = new PottsGridSerializer();
		PottsGrid grid = mock(PottsGrid.class);
		
		Bag bag = new Bag();
		doReturn(bag).when(grid).getAllObjects();
		
		int a1 = randomIntBetween(1,100);
		int b1 = randomIntBetween(1,100);
		int c1 = randomIntBetween(1,100);
		bag.add(new int[] { a1, b1, c1 });
		
		int a2 = randomIntBetween(1,100);
		int b2 = randomIntBetween(1,100);
		int c2 = randomIntBetween(1,100);
		bag.add(new int[] { a2, b2, c2 });
		
		JsonSerializationContext context = new JsonSerializationContext() {
			public JsonElement serialize(Object src) {
				JsonArray json = new JsonArray();
				int[] arr = (int[])src;
				for (int value : arr) { json.add(value); }
				return json;
			}
			
			public JsonElement serialize(Object src, Type typeOfSrc) { return null; }
		};
		
		String expected = "["
				+ "[" + a1 + "," + b1 + "," + c1 + "],"
				+ "[" + a2 + "," + b2 + "," + c2 + "]"
				+ "]";
		
		JsonElement json = serializer.serialize(grid, null, context);
		assertEquals(expected, json.toString());
	}
	
	@Test
	public void serialize_forVoxel_createsJSON() {
		VoxelSerializer serializer = new VoxelSerializer();
		
		int x = randomIntBetween(1,100);
		int y = randomIntBetween(1,100);
		int z = randomIntBetween(1,100);
		Voxel voxel = new Voxel(x, y, z);
		
		String expected = "[" + x + "," + y + "," + z + "]";
		
		JsonElement json = serializer.serialize(voxel, null, null);
		assertEquals(expected, json.toString());
	}
	
	@Test
	public void serialize_forLocationNoRegion_createJSON() {
		PottsLocationSerializer serializer = new PottsLocationSerializer();
		PottsLocation location = mock(PottsLocation.class);
		
		ArrayList<Voxel> voxels = new ArrayList<>();
		doReturn(voxels).when(location).getVoxels();
		
		int x1 = randomIntBetween(1,100);
		int y1 = randomIntBetween(1,100);
		int z1 = randomIntBetween(1,100);
		voxels.add(new Voxel(x1, y1, z1));
		
		int x2 = x1 + randomIntBetween(1,100);
		int y2 = y1 + randomIntBetween(1,100);
		int z2 = z1 + randomIntBetween(1,100);
		voxels.add(new Voxel(x2, y2, z2));
		
		JsonSerializationContext context = new JsonSerializationContext() {
			public JsonElement serialize(Object src) {
				Voxel voxel = (Voxel)src;
				JsonArray json = new JsonArray();
				json.add(voxel.x + "|" + voxel.y + "|" + voxel.z);
				return json;
			}
			
			public JsonElement serialize(Object src, Type typeOfSrc) { return null; }
		};
		
		String expected = "["
				+ "{\"region\":\"UNDEFINED\",\"voxels\":["
				+ "[\"" + x1 + "|" + y1 + "|" + z1 + "\"],"
				+ "[\"" + x2 + "|" + y2 + "|" + z2 + "\"]"
				+ "]}"
				+ "]";
		
		JsonElement json = serializer.serialize(location, null, context);
		assertEquals(expected, json.toString());
	}
	
	@Test
	public void serialize_forLocationWithRegion_createJSON() {
		PottsLocationSerializer serializer = new PottsLocationSerializer();
		PottsLocations location = mock(PottsLocations.class);
		
		PottsLocation location1 = mock(PottsLocation.class);
		PottsLocation location2 = mock(PottsLocation.class);
		
		ArrayList<Voxel> voxels1 = new ArrayList<>();
		doReturn(voxels1).when(location1).getVoxels();
		
		ArrayList<Voxel> voxels2 = new ArrayList<>();
		doReturn(voxels2).when(location2).getVoxels();
		
		int x1 = randomIntBetween(1,100);
		int y1 = randomIntBetween(1,100);
		int z1 = randomIntBetween(1,100);
		voxels1.add(new Voxel(x1, y1, z1));
		
		int x2 = x1 + randomIntBetween(1,100);
		int y2 = randomIntBetween(1,100);
		int z2 = randomIntBetween(1,100);
		voxels2.add(new Voxel(x2, y2, z2));
		
		location.locations = new EnumMap<>(Region.class);
		location.locations.put(Region.DEFAULT, location1);
		location.locations.put(Region.NUCLEUS, location2);
		
		JsonSerializationContext context = new JsonSerializationContext() {
			public JsonElement serialize(Object src) {
				Voxel voxel = (Voxel)src;
				JsonArray json = new JsonArray();
				json.add(voxel.x + "|" + voxel.y + "|" + voxel.z);
				return json;
			}
			
			public JsonElement serialize(Object src, Type typeOfSrc) { return null; }
		};
		
		String expected = "["
				+ "{\"region\":\"DEFAULT\""
				+ ",\"voxels\":[[\"" + x1 + "|" + y1 + "|" + z1 + "\"]]},"
				+ "{\"region\":\"NUCLEUS\""
				+ ",\"voxels\":[[\"" + x2 + "|" + y2 + "|" + z2 + "\"]]}"
				+ "]";
		
		JsonElement json = serializer.serialize(location, null, context);
		assertEquals(expected, json.toString());
	}
}
