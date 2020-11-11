package arcade.sim.output;

import org.junit.*;
import static org.junit.Assert.*;
import com.google.gson.*;
import com.google.gson.internal.bind.TreeTypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;
import java.lang.reflect.Type;
import java.util.ArrayList;
import static arcade.agent.cell.Cell.State;
import static arcade.agent.module.Module.Phase;
import static arcade.agent.cell.CellFactory.CellContainer;
import static arcade.agent.cell.CellFactory.CellFactoryContainer;
import static arcade.env.loc.Location.Voxel;
import static arcade.env.loc.LocationFactory.LocationContainer;
import static arcade.env.loc.LocationFactory.LocationFactoryContainer;
import static arcade.env.loc.Location.VOXEL_COMPARATOR;
import static arcade.sim.output.OutputDeserializer.*;
import static arcade.MainTest.*;
import static arcade.agent.cell.CellFactoryTest.*;
import static arcade.agent.cell.Cell.Region;

public class OutputDeserializerTest {
	private static final double EPSILON = 1E-10;
	
	static final JsonDeserializationContext VOXEL_CONTEXT = new JsonDeserializationContext() {
		public <T> T deserialize(JsonElement json, Type typeOfT)
				throws JsonParseException {
			VoxelDeserializer deserializer = new VoxelDeserializer();
			return (T) deserializer.deserialize(json, Voxel.class, null);
		}
	};
	
	static final JsonDeserializationContext LOCATION_CONTEXT = new JsonDeserializationContext() {
		public <T> T deserialize(JsonElement json, Type typeOfT)
				throws JsonParseException {
			JsonObject array = json.getAsJsonObject();
			int id = array.get("id").getAsInt();
			LocationContainer container = new LocationContainer(id, null, null, null);
			return (T)container;
		}
	};
	
	static final JsonDeserializationContext CELL_CONTEXT = new JsonDeserializationContext() {
		public <T> T deserialize(JsonElement json, Type typeOfT)
				throws JsonParseException {
			JsonObject array = json.getAsJsonObject();
			int id = array.get("id").getAsInt();
			CellContainer container = new CellContainer(id, 0, 0);
			return (T)container;
		}
	};
	
	@Test
	public void makeGSON_registersAdaptors() {
		Gson gson = OutputDeserializer.makeGSON();
		
		TypeToken<CellContainer> cell = new TypeToken<CellContainer>() {};
		assertSame(gson.getAdapter(cell).getClass(), TreeTypeAdapter.class);
		
		TypeToken<CellFactoryContainer> cellFactory = new TypeToken<CellFactoryContainer>() {};
		assertSame(gson.getAdapter(cellFactory).getClass(), TreeTypeAdapter.class);
		
		TypeToken<Voxel> voxel = new TypeToken<Voxel>() {};
		assertSame(gson.getAdapter(voxel).getClass(), TreeTypeAdapter.class);
		
		TypeToken<LocationContainer> location = new TypeToken<LocationContainer>() {};
		assertSame(gson.getAdapter(location).getClass(), TreeTypeAdapter.class);
		
		TypeToken<LocationFactoryContainer> locationFactory = new TypeToken<LocationFactoryContainer>() {};
		assertSame(gson.getAdapter(locationFactory).getClass(), TreeTypeAdapter.class);
	}
	
	@Test
	public void deserializer_forCellNoRegion_createObject() {
		CellDeserializer deserializer = new CellDeserializer();
		
		int id = randomInt();
		int pop = randomInt();
		int age = randomInt();
		State state = randomState();
		Phase phase = randomPhase();
		int voxels = randomInt();
		int targetVolume = randomInt();
		int targetSurface = randomInt();
		
		String string = "{"
				+ "\"id\": " + id
				+ ",\"pop\": " + pop
				+ ",\"age\": " + age
				+ ",\"state\":\"" + state.name() + "\""
				+ ",\"phase\":\"" + phase.name() + "\""
				+ ",\"voxels\": " + voxels
				+ ",\"targets\":[" + targetVolume + "," + targetSurface + "]"
				+ "}";
		
		JsonObject json = JsonParser.parseString(string).getAsJsonObject();
		CellContainer object = deserializer.deserialize(json, CellContainer.class, null);
		
		assertEquals(id, object.id);
		assertEquals(pop, object.pop);
		assertEquals(age, object.age);
		assertEquals(state, object.state);
		assertEquals(phase, object.phase);
		assertEquals(voxels, object.voxels);
		assertEquals(targetVolume, object.targetVolume, EPSILON);
		assertEquals(targetSurface, object.targetSurface, EPSILON);
		assertNull(object.regionVoxels);
		assertNull(object.regionTargetVolume);
		assertNull(object.regionTargetSurface);
	}
	
	@Test
	public void deserializer_forCellWithRegions_createObject() {
		CellDeserializer deserializer = new CellDeserializer();
		
		int id = randomInt();
		int pop = randomInt();
		int age = randomInt();
		State state = randomState();
		Phase phase = randomPhase();
		int voxels = randomInt();
		int targetVolume = randomInt();
		int targetSurface = randomInt();
		
		Region region1 = Region.DEFAULT;
		Region region2 = Region.NUCLEUS;
		int regionVoxels1 = randomInt();
		int regionVoxels2 = randomInt();
		int targetRegionVolume1 = randomInt();
		int targetRegionSurface1 = randomInt();
		int targetRegionVolume2 = randomInt();
		int targetRegionSurface2 = randomInt();
		
		String string = "{"
				+ "\"id\": " + id
				+ ",\"pop\": " + pop
				+ ",\"age\": " + age
				+ ",\"state\":\"" + state.name() + "\""
				+ ",\"phase\": \"" + phase.name() + "\""
				+ ",\"voxels\": " + voxels
				+ ",\"targets\":[" + targetVolume + "," + targetSurface + "]"
				+ ",\"regions\":["
				+ "{\"region\":" + region1.name()
				+ ",\"voxels\":" + regionVoxels1 
				+ ",\"targets\":[" + targetRegionVolume1 + "," + targetRegionSurface1 + "]"
				+ "},"
				+ "{\"region\":" + region2.name()
				+ ",\"voxels\":" + regionVoxels2
				+ ",\"targets\":[" + targetRegionVolume2 + "," + targetRegionSurface2 + "]"
				+ "}"
				+ "]"
				+ "}";
		
		JsonObject json = JsonParser.parseString(string).getAsJsonObject();
		CellContainer object = deserializer.deserialize(json, CellContainer.class, null);
		
		assertEquals(id, object.id);
		assertEquals(pop, object.pop);
		assertEquals(age, object.age);
		assertEquals(state, object.state);
		assertEquals(phase, object.phase);
		assertEquals(voxels, object.voxels);
		assertEquals(targetVolume, object.targetVolume, EPSILON);
		assertEquals(targetSurface, object.targetSurface, EPSILON);
		assertEquals(regionVoxels1, (int)object.regionVoxels.get(region1));
		assertEquals(regionVoxels2, (int)object.regionVoxels.get(region2));
		assertEquals(targetRegionVolume1, object.regionTargetVolume.get(region1), EPSILON);
		assertEquals(targetRegionSurface1, object.regionTargetSurface.get(region1), EPSILON);
		assertEquals(targetRegionVolume2, object.regionTargetVolume.get(region2), EPSILON);
		assertEquals(targetRegionSurface2, object.regionTargetSurface.get(region2), EPSILON);
	}
	
	@Test
	public void deserializer_forCellFactory_createsObject() {
		CellFactoryDeserializer deserializer = new CellFactoryDeserializer();
		
		int n = randomInt();
		int id0 = randomInt();
		
		StringBuilder string = new StringBuilder("[");
		for (int i = 0; i < n; i++) {
			int id = id0 + i;
			string.append("{\"id\":").append(id).append("}");
			if (i < n - 1) { string.append(","); }
		}
		string.append("]");
		
		JsonArray json = JsonParser.parseString(string.toString()).getAsJsonArray();
		CellFactoryContainer object = deserializer.deserialize(json, CellFactoryContainer.class, CELL_CONTEXT);
		
		assertEquals(n, object.cells.size());
		for (int i = 0; i < n; i++) {
			int id = id0 + i;
			assertEquals(id, object.cells.get(i).id);
		}
	}
	
	@Test
	public void deserializer_forVoxel_createObject() {
		VoxelDeserializer deserializer = new VoxelDeserializer();
		
		int x = randomInt();
		int y = randomInt();
		int z = randomInt();
		String string = "[" + x + "," + y + "," + z + "]";
		
		Voxel expected = new Voxel(x, y, z);
		
		JsonArray json = JsonParser.parseString(string).getAsJsonArray();
		Voxel object = deserializer.deserialize(json, Voxel.class, null);
		assertEquals(expected, object);
	}
	
	@Test
	public void deserializer_forLocationNoRegion_createObject() {
		LocationDeserializer deserializer = new LocationDeserializer();
		
		Region region = Region.UNDEFINED;
		int id = randomInt();
		Voxel center = new Voxel(randomInt(), randomInt(), randomInt());
		
		int x1 = randomInt();
		int y1 = randomInt();
		int z1 = randomInt();
		
		int x2 = x1 + randomInt();
		int y2 = y1 + randomInt();
		int z2 = z1 + randomInt();
		
		String string = "{\"id\": " + id
				+ ",\"center\":[" + center.x + "," + center.y + "," + center.z + "]"
				+ ",\"location\":["
				+ "{\"region\":" + region.name() + ",\"voxels\":["
				+ "[" + x1 + "," + y1 + "," + z1 + "],"
				+ "[" + x2 + "," + y2 + "," + z2 + "]"
				+ "]}]}";
		
		ArrayList<Voxel> expected = new ArrayList<>();
		expected.add(new Voxel(x1, y1, z1));
		expected.add(new Voxel(x2, y2, z2));
		
		JsonObject json = JsonParser.parseString(string).getAsJsonObject();
		LocationContainer object = deserializer.deserialize(json, LocationContainer.class, VOXEL_CONTEXT);
		
		assertEquals(id, object.id);
		assertEquals(center, object.center);
		assertNull(object.regions);
		
		ArrayList<Voxel> voxels = object.voxels;
		voxels.sort(VOXEL_COMPARATOR);
		expected.sort(VOXEL_COMPARATOR);
		assertEquals(expected, voxels);
	}
	
	@Test
	public void deserializer_forLocationWithRegion_createObject() {
		LocationDeserializer deserializer = new LocationDeserializer();
		
		int id = randomInt();
		Voxel center = new Voxel(randomInt(), randomInt(), randomInt());
		
		int x1 = randomInt();
		int y1 = randomInt();
		int z1 = randomInt();
		
		int x2 = x1 + randomInt();
		int y2 = y1 + randomInt();
		int z2 = z1 + randomInt();
		
		int x3 = x2 + randomInt();
		int y3 = y2 + randomInt();
		int z3 = z2 + randomInt();
		
		int x4 = x3 + randomInt();
		int y4 = y3 + randomInt();
		int z4 = z3 + randomInt();
		
		Region region1 = Region.DEFAULT;
		Region region2 = Region.NUCLEUS;
		
		String string = "{\"id\": " + id
				+ ",\"center\":[" + center.x + "," + center.y + "," + center.z + "]"
				+ ",\"location\":["
				+ "{\"region\":" + region1.name() + ",\"voxels\":["
				+ "[" + x1 + "," + y1 + "," + z1 + "],"
				+ "[" + x2 + "," + y2 + "," + z2 + "]"
				+ "]},"
				+ "{\"region\":" + region2.name() + ",\"voxels\":["
				+ "[" + x3 + "," + y3 + "," + z3 + "],"
				+ "[" + x4 + "," + y4 + "," + z4 + "]"
				+ "]}]}";
		
		ArrayList<Voxel> expected = new ArrayList<>();
		expected.add(new Voxel(x1, y1, z1));
		expected.add(new Voxel(x2, y2, z2));
		expected.add(new Voxel(x3, y3, z3));
		expected.add(new Voxel(x4, y4, z4));
		
		ArrayList<Voxel> expected1 = new ArrayList<>();
		expected1.add(new Voxel(x1, y1, z1));
		expected1.add(new Voxel(x2, y2, z2));
		
		ArrayList<Voxel> expected2 = new ArrayList<>();
		expected2.add(new Voxel(x3, y3, z3));
		expected2.add(new Voxel(x4, y4, z4));
		
		JsonObject json = JsonParser.parseString(string).getAsJsonObject();
		LocationContainer object = deserializer.deserialize(json, LocationContainer.class, VOXEL_CONTEXT);
		
		assertEquals(id, object.id);
		assertEquals(center, object.center);
		
		assertTrue(object.regions.containsKey(region1));
		assertTrue(object.regions.containsKey(region2));
		
		ArrayList<Voxel> voxels = object.voxels;
		voxels.sort(VOXEL_COMPARATOR);
		expected.sort(VOXEL_COMPARATOR);
		assertEquals(expected, voxels);
		
		ArrayList<Voxel> voxels1 = object.regions.get(region1);
		voxels1.sort(VOXEL_COMPARATOR);
		expected1.sort(VOXEL_COMPARATOR);
		assertEquals(expected1, voxels1);
		
		ArrayList<Voxel> voxels2 = object.regions.get(region2);
		voxels2.sort(VOXEL_COMPARATOR);
		expected2.sort(VOXEL_COMPARATOR);
		assertEquals(expected2, voxels2);
	}
	
	@Test
	public void deserializer_forLocationFactory_createsObject() {
		LocationFactoryDeserializer deserializer = new LocationFactoryDeserializer();
		
		int n = randomInt();
		int id0 = randomInt();
		
		StringBuilder string = new StringBuilder("[");
		for (int i = 0; i < n; i++) {
			int id = id0 + i;
			string.append("{\"id\":").append(id).append(",\"location\":[").append(id).append("]}");
			if (i < n - 1) { string.append(","); }
		}
		string.append("]");
		
		JsonArray json = JsonParser.parseString(string.toString()).getAsJsonArray();
		LocationFactoryContainer object = deserializer.deserialize(json, LocationFactoryContainer.class, LOCATION_CONTEXT);
		
		assertEquals(n, object.locations.size());
		for (int i = 0; i < n; i++) {
			int id = id0 + i;
			assertEquals(id, object.locations.get(i).id);
		}
	}
}
