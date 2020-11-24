package arcade.core.sim.output;

import org.junit.*;
import static org.junit.Assert.*;
import com.google.gson.*;
import com.google.gson.internal.bind.TreeTypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;
import java.lang.reflect.Type;
import static arcade.core.agent.cell.CellFactory.CellContainer;
import static arcade.core.agent.cell.CellFactory.CellFactoryContainer;
import static arcade.core.env.loc.LocationFactory.LocationContainer;
import static arcade.core.env.loc.LocationFactory.LocationFactoryContainer;
import static arcade.core.sim.output.OutputDeserializer.*;
import static arcade.core.agent.cell.Cell.State;
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
			CellContainer container = new CellContainer(id, 0, 0, State.UNDEFINED);
			return (T)container;
		}
	};
	
	@Test
	public void makeGSON_registersAdaptors() {
		GsonBuilder gsonBuilder = OutputDeserializer.makeGSONBuilder();
		Gson gson = gsonBuilder.create();
		
		TypeToken<CellFactoryContainer> cellFactory = new TypeToken<CellFactoryContainer>() {};
		assertSame(gson.getAdapter(cellFactory).getClass(), TreeTypeAdapter.class);
		
		TypeToken<LocationFactoryContainer> locationFactory = new TypeToken<LocationFactoryContainer>() {};
		assertSame(gson.getAdapter(locationFactory).getClass(), TreeTypeAdapter.class);
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
}
