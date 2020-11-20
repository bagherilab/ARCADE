package arcade.core.sim.output;

import java.lang.reflect.Type;
import java.util.*;
import com.google.gson.*;
import arcade.core.sim.Series;
import arcade.core.util.MiniBox;

public final class OutputSerializer {
	/** Regular expression for fractions */
	public static final String DOUBLE_REGEX = "^(-?\\d*\\.\\d*)$|^(-?\\d*\\.\\d*E-?\\d+)$";
	
	/** Regular expression for integers */
	public static final String INTEGER_REGEX = "^(-?\\d+)$|^(-?\\d+E-?\\d+)$";
	
	public static GsonBuilder makeGSONBuilder() {
		GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();
		gsonBuilder.registerTypeAdapter(Series.class, new SeriesSerializer());
		gsonBuilder.registerTypeAdapter(MiniBox.class, new MiniBoxSerializer());
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
}
