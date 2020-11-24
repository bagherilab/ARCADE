package arcade.core.sim.output;

import java.lang.reflect.Type;
import java.util.*;
import com.google.gson.*;
import arcade.core.sim.Series;
import arcade.core.util.MiniBox;
import arcade.core.agent.cell.CellFactory.*;
import arcade.core.env.loc.LocationFactory.*;

public final class OutputSerializer {
	/** Regular expression for fractions */
	public static final String DOUBLE_REGEX = "^(-?\\d*\\.\\d*)$|^(-?\\d*\\.\\d*E-?\\d+)$";
	
	/** Regular expression for integers */
	public static final String INTEGER_REGEX = "^(-?\\d+)$|^(-?\\d+E-?\\d+)$";
	
	public static GsonBuilder makeGSONBuilder() {
		GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();
		gsonBuilder.registerTypeAdapter(Series.class, new SeriesSerializer());
		gsonBuilder.registerTypeAdapter(MiniBox.class, new MiniBoxSerializer());
		gsonBuilder.registerTypeAdapter(CellFactoryContainer.class, new CellFactorySerializer());
		gsonBuilder.registerTypeAdapter(CellContainer.class, new CellSerializer());
		gsonBuilder.registerTypeAdapter(LocationFactoryContainer.class, new LocationFactorySerializer());
		gsonBuilder.registerTypeAdapter(LocationContainer.class, new LocationSerializer());
		return gsonBuilder;
	}
	
	static class MiniBoxSerializer implements JsonSerializer<MiniBox> {
		public JsonElement serialize(MiniBox src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject json = new JsonObject();
			
			for (String key : src.getKeys()) {
				String value = src.get(key);
				
				if (value.matches(INTEGER_REGEX)) { json.addProperty(key, src.getInt(key)); }
				else if (value.matches(DOUBLE_REGEX)) { json.addProperty(key, src.getDouble(key)); }
				else { json.addProperty(key, value); }
			}
			
			return json;
		}
	}
	
	static class SeriesSerializer implements JsonSerializer<Series> {
		public JsonElement serialize(Series src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject json = new JsonObject();
			
			JsonObject seeds = new JsonObject();
			seeds.addProperty("start", src.getStartSeed());
			seeds.addProperty("end", src.getEndSeed());
			json.add("seeds", seeds);
			
			JsonObject conversions = new JsonObject();
			conversions.addProperty("DS", src.DS);
			conversions.addProperty("DT", src.DT);
			json.add("conversions", conversions);
			
			json.addProperty("ticks", src.getTicks());
			
			JsonObject size = new JsonObject();
			size.addProperty("length", src._length);
			size.addProperty("width", src._width);
			size.addProperty("height", src._height);
			json.add("size", size);
			
			// Add population parameters.
			JsonObject populations = new JsonObject();
			List<String> keys = new ArrayList<>(src._populations.keySet());
			Collections.sort(keys);
			for (String pop : keys) {
				JsonElement population = context.serialize(src._populations.get(pop));
				populations.add(pop, population);
			}
			json.add("populations", populations);
			
			return json;
		}
	}
	
	static class CellFactorySerializer implements JsonSerializer<CellFactoryContainer> {
		public JsonElement serialize(CellFactoryContainer src, Type typeOfSrc, JsonSerializationContext context) {
			JsonArray json = new JsonArray();
			
			for (CellContainer cellContainer : src.cells) {
				JsonElement cell = context.serialize(cellContainer, CellContainer.class);
				json.add(cell);
			}
			
			return json;
		}
	}
	
	static class CellSerializer implements JsonSerializer<CellContainer> {
		public JsonElement serialize(CellContainer src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject json = new JsonObject();
			json.addProperty("id", src.id);
			json.addProperty("pop", src.pop);
			json.addProperty("age", src.age);
			return json;
		}
	}
	
	static class LocationFactorySerializer implements JsonSerializer<LocationFactoryContainer> {
		public JsonElement serialize(LocationFactoryContainer src, Type typeOfSrc, JsonSerializationContext context) {
			JsonArray json = new JsonArray();
			
			for (LocationContainer locationContainer : src.locations) {
				JsonElement location = context.serialize(locationContainer, LocationContainer.class);
				json.add(location);
			}
			
			return json;
		}
	}
	
	static class LocationSerializer implements JsonSerializer<LocationContainer> {
		public JsonElement serialize(LocationContainer src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject json = new JsonObject();
			json.addProperty("id", src.id);
			return json;
		}
	}
}
