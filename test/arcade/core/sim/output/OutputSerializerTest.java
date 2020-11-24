package arcade.core.sim.output;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import com.google.gson.*;
import com.google.gson.internal.bind.TreeTypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;
import arcade.core.sim.Series;
import arcade.core.util.MiniBox;
import static arcade.core.agent.cell.CellFactory.CellContainer;
import static arcade.core.agent.cell.CellFactory.CellFactoryContainer;
import static arcade.core.env.loc.LocationFactory.LocationContainer;
import static arcade.core.env.loc.LocationFactory.LocationFactoryContainer;
import static arcade.core.sim.output.OutputSerializer.*;
import static arcade.core.TestUtilities.*;

public class OutputSerializerTest {
	static final JsonSerializationContext LOCATION_CONTEXT = new JsonSerializationContext() {
		public JsonElement serialize(Object src) { return null; }
		public JsonElement serialize(Object src, Type typeOfSrc) {
			JsonObject object = new JsonObject();
			object.addProperty("id", ((LocationContainer)src).id);
			JsonArray array = new JsonArray();
			array.add(((LocationContainer)src).id);
			object.add("location", array);
			return object;
		}
	};
	
	static final JsonSerializationContext CELL_CONTEXT = new JsonSerializationContext() {
		public JsonElement serialize(Object src) { return null; }
		public JsonElement serialize(Object src, Type typeOfSrc) {
			JsonObject object = new JsonObject();
			object.addProperty("id", ((CellContainer)src).id);
			return object;
		}
	};
	
	@Test
	public void makeGSON_registersAdaptors() {
		GsonBuilder gsonBuilder = OutputSerializer.makeGSONBuilder();
		Gson gson = gsonBuilder.create();
		
		TypeToken<Series> series = new TypeToken<Series>() {};
		assertSame(gson.getAdapter(series).getClass(), TreeTypeAdapter.class);
		
		TypeToken<MiniBox> minibox = new TypeToken<MiniBox>() {};
		assertSame(gson.getAdapter(minibox).getClass(), TreeTypeAdapter.class);
		
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
	public void serialize_forMiniBox_createsJSON() {
		MiniBoxSerializer serializer = new MiniBoxSerializer();
		MiniBox box = new MiniBox();
		
		String key1 = randomString();
		String value1 = randomString();
		
		String key2 = randomString();
		double value2 = randomDoubleBetween(0, 100);
		
		String key3 = randomString();
		int value3 = randomIntBetween(0, 100);
		
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
		
		int start = randomIntBetween(0, 100);
		int end = randomIntBetween(0, 100);
		doReturn(start).when(series).getStartSeed();
		doReturn(end).when(series).getEndSeed();
		
		double ds = randomDoubleBetween(0, 100);
		double dt = randomDoubleBetween(0, 100);
		
		int ticks = randomIntBetween(0, 100);
		doReturn(ticks).when(series).getTicks();
		
		int length = randomIntBetween(0, 100);
		int width = randomIntBetween(0, 100);
		int height = randomIntBetween(0, 100);
		
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
			
			Field dsField = Series.class.getDeclaredField("DS");
			dsField.setAccessible(true);
			dsField.setDouble(series, ds);
			
			Field dtField = Series.class.getDeclaredField("DT");
			dtField.setAccessible(true);
			dtField.setDouble(series, dt);
		} catch (Exception ignored) { }
		
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
				+ "\"seeds\":{"
				+ "\"start\":" + start + ","
				+ "\"end\":" + end
				+ "},"
				+ "\"conversions\":{"
				+ "\"DS\":" + ds + ","
				+ "\"DT\":" + dt
				+ "},"
				+ "\"ticks\":" + ticks + ","
				+ "\"size\":{"
				+ "\"length\":" + length + ","
				+ "\"width\":" + width + ","
				+ "\"height\":" + height
				+ "},"
				+ "\"populations\":{"
				+ "\"" + popA + "\":{\"" + keyA + "\":\"" + valueA + "\"},"
				+ "\"" + popB + "\":{\"" + keyB + "\":\"" + valueB + "\"}"
				+ "}"
				+ "}";
		
		JsonElement json = serializer.serialize(series, null, context);
		assertEquals(expected, json.toString());
	}
	
	@Test
	public void serialize_forCellFactory_createsJSON() {
		CellFactorySerializer serializer = new CellFactorySerializer();
		CellFactoryContainer cellFactory = new CellFactoryContainer();
		
		int n = randomIntBetween(1, 10);
		int id0 = randomIntBetween(1, 10);
		
		for (int i = 0; i < n; i++) {
			int id = id0 + i;
			CellContainer cell = new CellContainer(id, 0, 0);
			cellFactory.cells.add(cell);
		}
		
		StringBuilder expected = new StringBuilder("[");
		for (int i = 0; i < n; i++) {
			int id = id0 + i;
			expected.append("{\"id\":").append(id).append("}");
			if (i < n - 1) { expected.append(","); }
		}
		expected.append("]");
		
		JsonElement json = serializer.serialize(cellFactory, CellContainer.class, CELL_CONTEXT);
		assertEquals(expected.toString(), json.toString());
	}
	
	@Test
	public void serialize_forCell_createsJSON() {
		int id = randomIntBetween(1,100);
		int pop = randomIntBetween(1,100);
		int age = randomIntBetween(1,100);
		
		CellSerializer serializer = new CellSerializer();
		CellContainer cell = new CellContainer(id, pop, age);
		
		String expected = "{"
				+ "\"id\":" + id + ","
				+ "\"pop\":" + pop + ","
				+ "\"age\":" + age + ""
				+ "}";
		
		JsonElement json = serializer.serialize(cell, null, null);
		assertEquals(expected, json.toString());
	}
	
	@Test
	public void serialize_forLocationFactory_createsJSON() {
		LocationFactorySerializer serializer = new LocationFactorySerializer();
		LocationFactoryContainer locationFactory = new LocationFactoryContainer();
		
		int n = randomIntBetween(1, 10);
		int id0 = randomIntBetween(1, 10);
		
		for (int i = 0; i < n; i++) {
			int id = id0 + i;
			LocationContainer location = new LocationContainer(id);
			locationFactory.locations.add(location);
		}
		
		StringBuilder expected = new StringBuilder("[");
		for (int i = 0; i < n; i++) {
			int id = id0 + i;
			expected.append("{\"id\":").append(id).append(",\"location\":[").append(id).append("]}");
			if (i < n - 1) { expected.append(","); }
		}
		expected.append("]");
		
		JsonElement json = serializer.serialize(locationFactory, LocationContainer.class, LOCATION_CONTEXT);
		assertEquals(expected.toString(), json.toString());
	}
	
	@Test
	public void serialize_forLocation_createsJSON() {
		int id = randomIntBetween(1,100);
		
		LocationSerializer serializer = new LocationSerializer();
		LocationContainer location = new LocationContainer(id);
		
		String expected = "{"
				+ "\"id\":" + id + ""
				+ "}";
		
		JsonElement json = serializer.serialize(location, null, null);
		assertEquals(expected, json.toString());
	}
}
