package arcade.core.sim.output;

import org.junit.*;
import static org.junit.Assert.*;
import com.google.gson.*;
import com.google.gson.internal.bind.TreeTypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;
import java.lang.reflect.Type;
import arcade.core.agent.cell.CellContainer;
import arcade.core.agent.cell.CellFactoryContainer;
import arcade.core.env.loc.LocationContainer;
import arcade.core.env.loc.LocationFactoryContainer;
import static arcade.core.sim.output.OutputDeserializer.*;
import static arcade.core.TestUtilities.*;

public class OutputDeserializerTest {
	static final JsonDeserializationContext LOCATION_CONTEXT = new JsonDeserializationContext() {
		public <T> T deserialize(JsonElement json, Type typeOfT)
				throws JsonParseException {
			JsonObject array = json.getAsJsonObject();
			int id = array.get("id").getAsInt();
			LocationContainer container = new LocationContainer(id);
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
	
	public static void checkAdaptors(Gson gson) {
		TypeToken<CellFactoryContainer> cellFactory = new TypeToken<CellFactoryContainer>() {};
		assertSame(gson.getAdapter(cellFactory).getClass(), TreeTypeAdapter.class);
		
		TypeToken<CellContainer> cell = new TypeToken<CellContainer>() {};
		assertSame(gson.getAdapter(cell).getClass(), TreeTypeAdapter.class);
		
		TypeToken<LocationFactoryContainer> locationFactory = new TypeToken<LocationFactoryContainer>() {};
		assertSame(gson.getAdapter(locationFactory).getClass(), TreeTypeAdapter.class);
		
		TypeToken<LocationContainer> location = new TypeToken<LocationContainer>() {};
		assertSame(gson.getAdapter(location).getClass(), TreeTypeAdapter.class);
	}
	
	@Test
	public void makeGSON_registersAdaptors() {
		GsonBuilder gsonBuilder = OutputDeserializer.makeGSONBuilder();
		Gson gson = gsonBuilder.create();
		checkAdaptors(gson);
	}
	
	@Test
	public void deserializer_forCellFactory_createsObject() {
		CellFactoryDeserializer deserializer = new CellFactoryDeserializer();
		
		int n = randomIntBetween(1, 10);
		int id0 = randomIntBetween(1, 10);
		
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
	public void deserializer_forCell_createObject() {
		CellDeserializer deserializer = new CellDeserializer();
		
		int id = randomIntBetween(1,100);
		int pop = randomIntBetween(1,100);
		int age = randomIntBetween(1,100);
		
		String string = "{"
				+ "\"id\": " + id
				+ ",\"pop\": " + pop
				+ ",\"age\": " + age
				+ "}";
		
		JsonObject json = JsonParser.parseString(string).getAsJsonObject();
		CellContainer object = deserializer.deserialize(json, CellContainer.class, null);
		
		assertEquals(id, object.id);
		assertEquals(pop, object.pop);
		assertEquals(age, object.age);
	}
	
	@Test
	public void deserializer_forLocationFactory_createsObject() {
		LocationFactoryDeserializer deserializer = new LocationFactoryDeserializer();
		
		int n = randomIntBetween(1, 10);
		int id0 = randomIntBetween(1, 10);
		
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
	
	@Test
	public void deserializer_forLocation_createObject() {
		LocationDeserializer deserializer = new LocationDeserializer();
		
		int id = randomIntBetween(1,100);
		
		String string = "{"
				+ "\"id\": " + id
				+ "}";
		
		JsonObject json = JsonParser.parseString(string).getAsJsonObject();
		LocationContainer object = deserializer.deserialize(json, LocationContainer.class, null);
		
		assertEquals(id, object.id);
	}
}
