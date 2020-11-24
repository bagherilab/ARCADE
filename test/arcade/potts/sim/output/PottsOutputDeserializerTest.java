package arcade.potts.sim.output;

import org.junit.*;
import static org.junit.Assert.*;
import com.google.gson.*;
import com.google.gson.internal.bind.TreeTypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;
import java.lang.reflect.Type;
import java.util.ArrayList;
import arcade.potts.env.loc.Voxel;
import static arcade.core.agent.cell.Cell.Region;
import static arcade.core.agent.cell.Cell.State;
import static arcade.potts.agent.module.PottsModule.Phase;
import static arcade.potts.env.loc.Voxel.VOXEL_COMPARATOR;
import static arcade.potts.sim.output.PottsOutputDeserializer.*;
import static arcade.potts.agent.cell.PottsCellFactory.PottsCellContainer;
import static arcade.potts.env.loc.PottsLocationFactory.PottsLocationContainer;
import static arcade.core.sim.output.OutputDeserializer.*;
import static arcade.core.TestUtilities.*;
import static arcade.potts.PottsTestUtilities.*;

public class PottsOutputDeserializerTest {
	private static final double EPSILON = 1E-10;
	
	static final JsonDeserializationContext CELL_CONTEXT = new JsonDeserializationContext() {
		public <T> T deserialize(JsonElement json, Type typeOfT)
				throws JsonParseException {
			CellDeserializer deserializer = new CellDeserializer();
			return (T)deserializer.deserialize(json, typeOfT, null);
		}
	};
	
	static final JsonDeserializationContext LOCATION_CONTEXT = new JsonDeserializationContext() {
		public <T> T deserialize(JsonElement json, Type typeOfT)
				throws JsonParseException {
			if (typeOfT == Voxel.class) {
				VoxelDeserializer deserializer = new VoxelDeserializer();
				return (T) deserializer.deserialize(json, typeOfT, null);
			}
			else {
				LocationDeserializer deserializer = new LocationDeserializer();
				return (T)deserializer.deserialize(json, typeOfT, null);
			}
		}
	};
	
	public static void checkAdaptors(Gson gson) {
		TypeToken<PottsCellContainer> cell = new TypeToken<PottsCellContainer>() {};
		assertSame(gson.getAdapter(cell).getClass(), TreeTypeAdapter.class);
		
		TypeToken<Voxel> voxel = new TypeToken<Voxel>() {};
		assertSame(gson.getAdapter(voxel).getClass(), TreeTypeAdapter.class);
		
		TypeToken<PottsLocationContainer> location = new TypeToken<PottsLocationContainer>() {};
		assertSame(gson.getAdapter(location).getClass(), TreeTypeAdapter.class);
	}
	
	@Test
	public void makeGSON_registersAdaptors() {
		Gson gson = PottsOutputDeserializer.makeGSON();
		checkAdaptors(gson);
	}
	
	@Test
	public void deserializer_forCellNoRegion_createObject() {
		PottsCellDeserializer deserializer = new PottsCellDeserializer();
		
		int id = randomIntBetween(1,100);
		int pop = randomIntBetween(1,100);
		int age = randomIntBetween(1,100);
		State state = randomState();
		Phase phase = randomPhase();
		int voxels = randomIntBetween(1,100);
		int targetVolume = randomIntBetween(1,100);
		int targetSurface = randomIntBetween(1,100);
		
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
		PottsCellContainer object = deserializer.deserialize(json, PottsCellContainer.class, CELL_CONTEXT);
		
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
		PottsCellDeserializer deserializer = new PottsCellDeserializer();
		
		int id = randomIntBetween(1,100);
		int pop = randomIntBetween(1,100);
		int age = randomIntBetween(1,100);
		State state = randomState();
		Phase phase = randomPhase();
		int voxels = randomIntBetween(1,100);
		int targetVolume = randomIntBetween(1,100);
		int targetSurface = randomIntBetween(1,100);
		
		Region region1 = Region.DEFAULT;
		Region region2 = Region.NUCLEUS;
		int regionVoxels1 = randomIntBetween(1,100);
		int regionVoxels2 = randomIntBetween(1,100);
		int targetRegionVolume1 = randomIntBetween(1,100);
		int targetRegionSurface1 = randomIntBetween(1,100);
		int targetRegionVolume2 = randomIntBetween(1,100);
		int targetRegionSurface2 = randomIntBetween(1,100);
		
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
		PottsCellContainer object = deserializer.deserialize(json, PottsCellContainer.class, CELL_CONTEXT);
		
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
	public void deserializer_forVoxel_createObject() {
		VoxelDeserializer deserializer = new VoxelDeserializer();
		
		int x = randomIntBetween(1,100);
		int y = randomIntBetween(1,100);
		int z = randomIntBetween(1,100);
		String string = "[" + x + "," + y + "," + z + "]";
		
		Voxel expected = new Voxel(x, y, z);
		
		JsonArray json = JsonParser.parseString(string).getAsJsonArray();
		Voxel object = deserializer.deserialize(json, Voxel.class, null);
		assertEquals(expected, object);
	}
	
	@Test
	public void deserializer_forLocationNoRegion_createObject() {
		PottsLocationDeserializer deserializer = new PottsLocationDeserializer();
		
		Region region = Region.UNDEFINED;
		int id = randomIntBetween(1,100);
		Voxel center = new Voxel(randomIntBetween(1,100), randomIntBetween(1,100), randomIntBetween(1,100));
		
		int x1 = randomIntBetween(1,100);
		int y1 = randomIntBetween(1,100);
		int z1 = randomIntBetween(1,100);
		
		int x2 = x1 + randomIntBetween(1,100);
		int y2 = y1 + randomIntBetween(1,100);
		int z2 = z1 + randomIntBetween(1,100);
		
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
		PottsLocationContainer object = deserializer.deserialize(json, PottsLocationContainer.class, LOCATION_CONTEXT);
		
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
		PottsLocationDeserializer deserializer = new PottsLocationDeserializer();
		
		int id = randomIntBetween(1,100);
		Voxel center = new Voxel(randomIntBetween(1,100), randomIntBetween(1,100), randomIntBetween(1,100));
		
		int x1 = randomIntBetween(1,100);
		int y1 = randomIntBetween(1,100);
		int z1 = randomIntBetween(1,100);
		
		int x2 = x1 + randomIntBetween(1,100);
		int y2 = y1 + randomIntBetween(1,100);
		int z2 = z1 + randomIntBetween(1,100);
		
		int x3 = x2 + randomIntBetween(1,100);
		int y3 = y2 + randomIntBetween(1,100);
		int z3 = z2 + randomIntBetween(1,100);
		
		int x4 = x3 + randomIntBetween(1,100);
		int y4 = y3 + randomIntBetween(1,100);
		int z4 = z3 + randomIntBetween(1,100);
		
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
		PottsLocationContainer object = deserializer.deserialize(json, PottsLocationContainer.class, LOCATION_CONTEXT);
		
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
}
