package arcade.sim.output;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import com.google.gson.*;
import com.google.gson.internal.bind.TreeTypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import sim.util.Bag;
import arcade.sim.Series;
import arcade.sim.Potts;
import arcade.agent.cell.Cell;
import arcade.agent.module.Module;
import arcade.env.grid.Grid;
import arcade.env.loc.*;
import arcade.util.MiniBox;
import static arcade.env.loc.Location.Voxel;
import static arcade.sim.output.OutputSerializer.*;
import static arcade.MainTest.*;

public class OutputSerializerTest {
	@Test
	public void makeGSON_registersAdaptors() {
		Gson gson = OutputSerializer.makeGSON();
		
		TypeToken<Series> series = new TypeToken<Series>() {};
		assertSame(gson.getAdapter(series).getClass(), TreeTypeAdapter.class);
		
		TypeToken<MiniBox> minibox = new TypeToken<MiniBox>() {};
		assertSame(gson.getAdapter(minibox).getClass(), TreeTypeAdapter.class);
		
		TypeToken<Potts> potts = new TypeToken<Potts>() {};
		assertSame(gson.getAdapter(potts).getClass(), TreeTypeAdapter.class);
		
		TypeToken<Grid> grid = new TypeToken<Grid>() {};
		assertSame(gson.getAdapter(grid).getClass(), TreeTypeAdapter.class);
		
		TypeToken<Cell> cell = new TypeToken<Cell>() {};
		assertSame(gson.getAdapter(cell).getClass(), TreeTypeAdapter.class);
		
		TypeToken<Module> module = new TypeToken<Module>() {};
		assertSame(gson.getAdapter(module).getClass(), TreeTypeAdapter.class);
		
		TypeToken<Location> location = new TypeToken<Location>() {};
		assertSame(gson.getAdapter(location).getClass(), TreeTypeAdapter.class);
		
		TypeToken<Voxel> voxel = new TypeToken<Voxel>() {};
		assertSame(gson.getAdapter(voxel).getClass(), TreeTypeAdapter.class);
	}
	
	@Test
	public void serialize_forMiniBox_createsJSON() {
		MiniBoxSerializer serializer = new MiniBoxSerializer();
		MiniBox box = new MiniBox();
		
		String key1 = randomString();
		String value1 = randomString();
		
		String key2 = randomString();
		double value2 = randomDouble();
		
		String key3 = randomString();
		int value3 = randomInt();
		
		box.put(key1, value1);
		box.put(key2, value2);
		box.put(key3, value3);
		
		String expected = "{"
			+ "\"" + key1 + "\":\"" + value1 + "\","
			+ "\"" + key2 + "\":" + value2 + ","
			+ "\"" + key3 + "\":" + value3 + ""
			+ "}";
		
		JsonElement json = serializer.serialize(box, null, null);
		assertEquals(expected, json.toString());
	}
	
	@Test
	public void serialize_forSeries_createsJSON() {
		SeriesSerializer serializer = new SeriesSerializer();
		Series series = mock(Series.class);
		
		int ticks = randomInt();
		doReturn(ticks).when(series).getTicks();
		
		int length = randomInt();
		int width = randomInt();
		int height = randomInt();
		
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
		
		series._potts = new MiniBox();
		
		String key1 = randomString();
		String value1 = randomString();
		series._potts.put(key1, value1);
		
		series._populations = new HashMap<>();
		String popA = "a" + randomString();
		String popB = "b" + randomString();
		
		MiniBox a = new MiniBox();
		String keyA = randomString();
		String valueA = randomString();
		a.put(keyA, valueA);
		
		MiniBox b = new MiniBox();
		String keyB = randomString();
		String valueB = randomString();
		b.put(keyB, valueB);
		
		series._populations.put(popA, a);
		series._populations.put(popB, b);
		
		JsonSerializationContext context = new JsonSerializationContext() {
			public JsonElement serialize(Object src) {
				if (src instanceof MiniBox) {
					MiniBox box = (MiniBox)src;
					JsonObject json = new JsonObject();
					json.addProperty(box.getKeys().get(0), box.get(box.getKeys().get(0)));
					return json;
				}
				return null;
			}
			
			public JsonElement serialize(Object src, Type typeOfSrc) { return null; }
		};
		
		String expected = "{"
				+ "\"ticks\":" + ticks + ","
				+ "\"size\":{"
				+ "\"length\":" + length + ","
				+ "\"width\":" + width + ","
				+ "\"height\":" + height
				+ "},"
				+ "\"potts\":{\"" + key1 + "\":\"" + value1 + "\"},"
				+ "\"populations\":{"
				+ "\"" + popA + "\":{\"" + keyA + "\":\"" + valueA + "\"},"
				+ "\"" + popB + "\":{\"" + keyB + "\":\"" + valueB + "\"}"
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
		
		Cell cell1 = mock(Cell.class);
		Cell cell2 = mock(Cell.class);
		bag.add(cell1);
		bag.add(cell2);
		
		int id1 = randomInt();
		int id2 = randomInt();
		doReturn(id1).when(cell1).getID();
		doReturn(id2).when(cell2).getID();
		
		Location location1 = mock(Location.class);
		Location location2 = mock(Location.class);
		doReturn(location1).when(cell1).getLocation();
		doReturn(location2).when(cell2).getLocation();
		
		int x1 = randomInt();
		int y1 = randomInt();
		int z1 = randomInt();
		doReturn(new Voxel(x1, y1, z1)).when(location1).getCenter();
		
		int x2 = randomInt();
		int y2 = randomInt();
		int z2 = randomInt();
		doReturn(new Voxel(x2, y2, z2)).when(location2).getCenter();
		
		JsonSerializationContext context = new JsonSerializationContext() {
			public JsonElement serialize(Object src) {
				Location location = (Location)src;
				JsonArray json = new JsonArray();
				json.add(location.getCenter().x);
				json.add(location.getCenter().y);
				json.add(location.getCenter().z);
				return json;
			}
			
			public JsonElement serialize(Object src, Type typeOfSrc) { return null; }
		};
		
		String expected = "["
				+ "{\"id\":" + id1 + ",\"voxels\":[" + x1 + "," + y1 + "," + z1 + "]},"
				+ "{\"id\":" + id2 + ",\"voxels\":[" + x2 + "," + y2 + "," + z2 + "]}"
				+ "]";
		
		JsonElement json = serializer.serialize(potts, null, context);
		assertEquals(expected, json.toString());
	}
	
	@Test
	public void serialize_forCellNoTag_createsJSON() {
		CellSerializer serializer = new CellSerializer();
		Cell cell = mock(Cell.class);
		
		int id = randomInt();
		int pop = randomInt();
		int age = randomInt();
		doReturn(id).when(cell).getID();
		doReturn(pop).when(cell).getPop();
		doReturn(age).when(cell).getAge();
		
		Location location = mock(Location.class);
		doReturn(location).when(cell).getLocation();
		
		int volume = randomInt();
		int targetVolume = randomInt();
		doReturn(volume).when(location).getVolume();
		doReturn((double)targetVolume).when(cell).getTargetVolume();
		
		int surface = randomInt();
		int targetSurface = randomInt();
		doReturn(surface).when(location).getSurface();
		doReturn((double)targetSurface).when(cell).getTargetSurface();
		
		Module module = mock(Module.class);
		doReturn(module).when(cell).getModule();
		
		JsonSerializationContext context = new JsonSerializationContext() {
			public JsonElement serialize(Object src) {
				if (src instanceof Module) {
					JsonArray json = new JsonArray();
					json.add("MODULE");
					return json;
				}
				return null;
			}
			
			public JsonElement serialize(Object src, Type typeOfSrc) { return null; }
		};
		
		String expected = "{"
				+ "\"id\":" + id + ","
				+ "\"pop\":" + pop + ","
				+ "\"age\":" + age + ","
				+ "\"volumes\":[" + volume + "," + targetVolume + ".0],"
				+ "\"surfaces\":[" + surface + "," + targetSurface + ".0],"
				+ "\"module\":[\"MODULE\"]"
				+ "}";
		
		JsonElement json = serializer.serialize(cell, null, context);
		assertEquals(expected, json.toString());
	}
	
	@Test
	public void serialize_forCellWithTag_createsJSON() {
		CellSerializer serializer = new CellSerializer();
		Cell cell = mock(Cell.class);
		
		int id = randomInt();
		int pop = randomInt();
		int age = randomInt();
		doReturn(id).when(cell).getID();
		doReturn(pop).when(cell).getPop();
		doReturn(age).when(cell).getAge();
		
		Location location = mock(Location.class);
		doReturn(location).when(cell).getLocation();
		
		int volume = randomInt();
		int targetVolume = randomInt();
		doReturn(volume).when(location).getVolume();
		doReturn((double)targetVolume).when(cell).getTargetVolume();
		
		int surface = randomInt();
		int targetSurface = randomInt();
		doReturn(surface).when(location).getSurface();
		doReturn((double)targetSurface).when(cell).getTargetSurface();
		
		Module module = mock(Module.class);
		doReturn(module).when(cell).getModule();
		
		doReturn(2).when(cell).getTags();
		
		int volume1 = randomInt();
		int targetVolume1 = randomInt();
		doReturn(volume1).when(location).getVolume(-1);
		doReturn((double)targetVolume1).when(cell).getTargetVolume(-1);
		
		int surface1 = randomInt();
		int targetSurface1 = randomInt();
		doReturn(surface1).when(location).getSurface(-1);
		doReturn((double)targetSurface1).when(cell).getTargetSurface(-1);
		
		int volume2 = randomInt();
		int targetVolume2 = randomInt();
		doReturn(volume2).when(location).getVolume(-2);
		doReturn((double)targetVolume2).when(cell).getTargetVolume(-2);
		
		int surface2 = randomInt();
		int targetSurface2 = randomInt();
		doReturn(surface2).when(location).getSurface(-2);
		doReturn((double)targetSurface2).when(cell).getTargetSurface(-2);
		
		JsonSerializationContext context = new JsonSerializationContext() {
			public JsonElement serialize(Object src) {
				if (src instanceof Module) {
					JsonArray json = new JsonArray();
					json.add("MODULE");
					return json;
				}
				return null;
			}
			
			public JsonElement serialize(Object src, Type typeOfSrc) { return null; }
		};
		
		String expected = "{"
				+ "\"id\":" + id + ","
				+ "\"pop\":" + pop + ","
				+ "\"age\":" + age + ","
				+ "\"volumes\":[" + volume + "," + targetVolume + ".0],"
				+ "\"surfaces\":[" + surface + "," + targetSurface + ".0],"
				+ "\"module\":[\"MODULE\"],"
				+ "\"tags\":["
				+ "{\"id\":-1,"
				+ "\"volumes\":[" + volume1 + "," + targetVolume1 + ".0],"
				+ "\"surfaces\":[" + surface1 + "," + targetSurface1 + ".0]"
				+ "},"
				+ "{\"id\":-2,"
				+ "\"volumes\":[" + volume2 + "," + targetVolume2 + ".0],"
				+ "\"surfaces\":[" + surface2 + "," + targetSurface2 + ".0]"
				+ "}"
				+ "]"
				+ "}";
		
		JsonElement json = serializer.serialize(cell, null, context);
		assertEquals(expected, json.toString());
	}
	
	@Test
	public void serialize_forModule_createsJSON() {
		ModuleSerializer serializer = new ModuleSerializer();
		Module module = mock(Module.class);
		
		String name = randomString();
		int phase = randomInt();
		
		doReturn(name).when(module).getName();
		doReturn(phase).when(module).getPhase();
		
		String expected = "{"
				+ "\"name\":\"" + name + "\","
				+ "\"phase\":" + phase
				+ "}";
		
		JsonElement json = serializer.serialize(module, null, null);
		assertEquals(expected, json.toString());
	}
	
	@Test
	public void serialize_forGrid_createsJSON() {
		GridSerializer serializer = new GridSerializer();
		Grid grid = mock(Grid.class);
		
		Bag bag = new Bag();
		doReturn(bag).when(grid).getAllObjects();
		
		int a1 = randomInt();
		int b1 = randomInt();
		int c1 = randomInt();
		bag.add(new int[] { a1, b1, c1 });
		
		int a2 = randomInt();
		int b2 = randomInt();
		int c2 = randomInt();
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
		
		int x = randomInt();
		int y = randomInt();
		int z = randomInt();
		Voxel voxel = new Voxel(x, y, z);
		
		String expected = "[" + x + "," + y + "," + z + "]";
		
		JsonElement json = serializer.serialize(voxel, null, null);
		assertEquals(expected, json.toString());
	}
	
	@Test
	public void serialize_forLocationNoTag_createJSON() {
		LocationSerializer serializer = new LocationSerializer();
		PottsLocation location = mock(PottsLocation.class);
		
		ArrayList<Voxel> voxels = new ArrayList<>();
		doReturn(voxels).when(location).getVoxels();
		
		int x1 = randomInt();
		int y1 = randomInt();
		int z1 = randomInt();
		voxels.add(new Voxel(x1, y1, z1));
		
		int x2 = randomInt();
		int y2 = randomInt();
		int z2 = randomInt();
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
				+ "{\"tag\":0,\"voxels\":["
				+ "[\"" + x1 + "|" + y1 + "|" + z1 + "\"],"
				+ "[\"" + x2 + "|" + y2 + "|" + z2 + "\"]"
				+ "]}"
				+ "]";
		
		JsonElement json = serializer.serialize(location, null, context);
		assertEquals(expected, json.toString());
	}
	
	@Test
	public void serialize_forLocationWithTag_createJSON() {
		LocationSerializer serializer = new LocationSerializer();
		PottsLocations location = mock(PottsLocations.class);
		
		PottsLocation location1 = mock(PottsLocation.class);
		PottsLocation location2 = mock(PottsLocation.class);
		
		ArrayList<Voxel> voxels1 = new ArrayList<>();
		doReturn(voxels1).when(location1).getVoxels();
		
		ArrayList<Voxel> voxels2 = new ArrayList<>();
		doReturn(voxels2).when(location2).getVoxels();
		
		int x1 = randomInt();
		int y1 = randomInt();
		int z1 = randomInt();
		voxels1.add(new Voxel(x1, y1, z1));
		
		int x2 = randomInt();
		int y2 = randomInt();
		int z2 = randomInt();
		voxels2.add(new Voxel(x2, y2, z2));
		
		int tag1 = randomInt();
		int tag2 = randomInt() + 1;
		
		location.locations = new HashMap<>();
		location.locations.put(tag1, location1);
		location.locations.put(tag2, location2);
		
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
				+ "{\"tag\":" + tag1 
				+ ",\"voxels\":[[\"" + x1 + "|" + y1 + "|" + z1 + "\"]]},"
				+ "{\"tag\":" + tag2
				+ ",\"voxels\":[[\"" + x2 + "|" + y2 + "|" + z2 + "\"]]}"
				+ "]";
		
		JsonElement json = serializer.serialize(location, null, context);
		assertEquals(expected, json.toString());
	}
}
