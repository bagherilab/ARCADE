package arcade.sim.output;

import org.junit.*;
import static org.junit.Assert.*;
import com.google.gson.*;
import com.google.gson.internal.bind.TreeTypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;
import java.lang.reflect.Type;
import java.util.ArrayList;
import arcade.env.loc.*;
import static arcade.env.loc.Location.Voxel;
import static arcade.env.loc.LocationFactory.LocationFactoryContainer;
import static arcade.env.loc.LocationTest.COMPARATOR;
import static arcade.sim.output.OutputDeserializer.*;
import static arcade.MainTest.*;

public class OutputDeserializerTest {
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
			JsonArray array = json.getAsJsonArray();
			int i = array.get(0).getAsInt();
			ArrayList<Voxel> voxels = new ArrayList<>();
			voxels.add(new Voxel(i, i, i));
			PottsLocation2D location = new PottsLocation2D(voxels);
			return (T)location;
		}
	};
	
	@Test
	public void makeGSON_registersAdaptors() {
		Gson gson = OutputDeserializer.makeGSON();
		
		TypeToken<Voxel> voxel = new TypeToken<Voxel>() {};
		assertSame(gson.getAdapter(voxel).getClass(), TreeTypeAdapter.class);
		
		TypeToken<Location> location = new TypeToken<Location>() {};
		assertSame(gson.getAdapter(location).getClass(), TreeTypeAdapter.class);
		
		TypeToken<LocationFactoryContainer> locationFactory = new TypeToken<LocationFactoryContainer>() {};
		assertSame(gson.getAdapter(locationFactory).getClass(), TreeTypeAdapter.class);
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
	public void deserializer_forLocationNoTag2D_createObject() {
		LocationDeserializer deserializer = new LocationDeserializer();
		
		int x1 = randomInt();
		int y1 = randomInt();
		
		int x2 = x1 + randomInt();
		int y2 = y1 + randomInt();
		
		String string = "[{\"tag\":0,\"voxels\":["
				+ "[" + x1 + "," + y1 + ",0],"
				+ "[" + x2 + "," + y2 + ",0]"
				+ "]}]";
		
		ArrayList<Voxel> expected = new ArrayList<>();
		expected.add(new Voxel(x1, y1, 0));
		expected.add(new Voxel(x2, y2, 0));
		
		JsonArray json = JsonParser.parseString(string).getAsJsonArray();
		Location object = deserializer.deserialize(json, Location.class, VOXEL_CONTEXT);
		
		assertTrue(object instanceof PottsLocation2D);
		
		ArrayList<Voxel> voxels = object.getVoxels();
		voxels.sort(COMPARATOR);
		expected.sort(COMPARATOR);
		assertEquals(expected, voxels);
	}
	
	@Test
	public void deserializer_forLocationNoTag3D_createObject() {
		LocationDeserializer deserializer = new LocationDeserializer();
		
		int x1 = randomInt();
		int y1 = randomInt();
		int z1 = randomInt();
		
		int x2 = x1 + randomInt();
		int y2 = y1 + randomInt();
		int z2 = z1 + randomInt();
		
		String string = "[{\"tag\":0,\"voxels\":["
				+ "[" + x1 + "," + y1 + "," + z1 + "],"
				+ "[" + x2 + "," + y2 + "," + z2 + "]"
				+ "]}]";
		
		ArrayList<Voxel> expected = new ArrayList<>();
		expected.add(new Voxel(x1, y1, z1));
		expected.add(new Voxel(x2, y2, z2));
		
		JsonArray json = JsonParser.parseString(string).getAsJsonArray();
		Location object = deserializer.deserialize(json, Location.class, VOXEL_CONTEXT);
		
		assertTrue(object instanceof PottsLocation3D);
		
		ArrayList<Voxel> voxels = object.getVoxels();
		voxels.sort(COMPARATOR);
		expected.sort(COMPARATOR);
		assertEquals(expected, voxels);
	}
	
	@Test
	public void deserializer_forLocationWithTag2D_createObject() {
		LocationDeserializer deserializer = new LocationDeserializer();
		
		int x1 = randomInt();
		int y1 = randomInt();
		
		int x2 = x1 + randomInt();
		int y2 = y1 + randomInt();
		
		int x3 = x2 + randomInt();
		int y3 = y2 + randomInt();
		
		int x4 = x3 + randomInt();
		int y4 = y3 + randomInt();
		
		int tag1 = randomInt();
		int tag2 = tag1 + 1;
		
		String string = "["
				+ "{\"tag\":" + tag1 + ",\"voxels\":["
				+ "[" + x1 + "," + y1 + ",0],"
				+ "[" + x2 + "," + y2 + ",0]"
				+ "]},"
				+ "{\"tag\":" + tag2 + ",\"voxels\":["
				+ "[" + x3 + "," + y3 + ",0],"
				+ "[" + x4 + "," + y4 + ",0]"
				+ "]}"
				+ "]";
		
		ArrayList<Voxel> expected = new ArrayList<>();
		expected.add(new Voxel(x1, y1, 0));
		expected.add(new Voxel(x2, y2, 0));
		expected.add(new Voxel(x3, y3, 0));
		expected.add(new Voxel(x4, y4, 0));
		
		ArrayList<Voxel> expected1 = new ArrayList<>();
		expected1.add(new Voxel(x1, y1, 0));
		expected1.add(new Voxel(x2, y2, 0));
		
		ArrayList<Voxel> expected2 = new ArrayList<>();
		expected2.add(new Voxel(x3, y3, 0));
		expected2.add(new Voxel(x4, y4, 0));
		
		JsonArray json = JsonParser.parseString(string).getAsJsonArray();
		Location object = deserializer.deserialize(json, Location.class, VOXEL_CONTEXT);
		
		assertTrue(object instanceof PottsLocations2D);
		
		ArrayList<Voxel> voxels = object.getVoxels();
		voxels.sort(COMPARATOR);
		expected.sort(COMPARATOR);
		assertEquals(expected, voxels);
		
		ArrayList<Voxel> voxels1 = ((PottsLocations)object).locations.get(tag1).getVoxels();
		voxels1.sort(COMPARATOR);
		expected1.sort(COMPARATOR);
		assertEquals(expected1, voxels1);
		
		ArrayList<Voxel> voxels2 = ((PottsLocations)object).locations.get(tag2).getVoxels();
		voxels2.sort(COMPARATOR);
		expected2.sort(COMPARATOR);
		assertEquals(expected2, voxels2);
	}
	
	@Test
	public void deserializer_forLocationWithTag3D_createObject() {
		LocationDeserializer deserializer = new LocationDeserializer();
		
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
		
		int tag1 = randomInt();
		int tag2 = tag1 + 1;
		
		String string = "["
				+ "{\"tag\":" + tag1 + ",\"voxels\":["
				+ "[" + x1 + "," + y1 + "," + z1 + "],"
				+ "[" + x2 + "," + y2 + "," + z2 + "]"
				+ "]},"
				+ "{\"tag\":" + tag2 + ",\"voxels\":["
				+ "[" + x3 + "," + y3 + "," + z3 + "],"
				+ "[" + x4 + "," + y4 + "," + z4 + "]"
				+ "]}"
				+ "]";
		
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
		
		JsonArray json = JsonParser.parseString(string).getAsJsonArray();
		Location object = deserializer.deserialize(json, Location.class, VOXEL_CONTEXT);
		
		assertTrue(object instanceof PottsLocations3D);
		
		ArrayList<Voxel> voxels = object.getVoxels();
		voxels.sort(COMPARATOR);
		expected.sort(COMPARATOR);
		assertEquals(expected, voxels);
		
		ArrayList<Voxel> voxels1 = ((PottsLocations)object).locations.get(tag1).getVoxels();
		voxels1.sort(COMPARATOR);
		expected1.sort(COMPARATOR);
		assertEquals(expected1, voxels1);
		
		ArrayList<Voxel> voxels2 = ((PottsLocations)object).locations.get(tag2).getVoxels();
		voxels2.sort(COMPARATOR);
		expected2.sort(COMPARATOR);
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
		
		assertEquals(n, object.ids.size());
		
		for (int i = 0; i < n; i++) {
			int id = id0 + i;
			ArrayList<Voxel> voxels = object.idToLocation.get(id).getVoxels();
			assertEquals(1, voxels.size());
			assertEquals(new Voxel(id, id, id), voxels.get(0));
		}
	}
}
