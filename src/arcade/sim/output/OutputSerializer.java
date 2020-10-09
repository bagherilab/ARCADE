package arcade.sim.output;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import arcade.sim.Series;
import arcade.agent.cell.Cell;
import arcade.agent.module.Module;
import arcade.env.grid.Grid;
import arcade.util.MiniBox;

public abstract class OutputSerializer {
	/** Regular expression for fractions */
	public static final String DOUBLE_REGEX = "^(-?\\d*\\.\\d*)$|^(-?\\d*\\.\\d*E\\d+)$";
	
	/** Regular expression for integers */
	public static final String INTEGER_REGEX = "^(-?\\d+)$|^(-?\\d+E\\d+)$";
	
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
			json.addProperty("ticks", src.getTicks());
			
			JsonObject size = new JsonObject();
			size.addProperty("length", src._length);
			size.addProperty("width", src._length);
			size.addProperty("height", src._width);
			json.add("size", size);
			
			TypeToken<MiniBox> miniBoxToken = new TypeToken<MiniBox>() { };
			
			// Add potts parameters.
			JsonElement potts = context.serialize(src._potts, miniBoxToken.getType());
			json.add("potts", potts);
			
			// Add population parameters.
			JsonObject populations = new JsonObject();
			for (String pop : src._populations.keySet()) {
				JsonElement population = context.serialize(src._populations.get(pop), miniBoxToken.getType());
				populations.add(pop, population);
			}
			json.add("populations", populations);
			
			return json;
		}
	}
	
	static class CellSerializer implements JsonSerializer<Cell> {
		public JsonElement serialize(Cell src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject json = new JsonObject();
			
			json.addProperty("id", src.getID());
			json.addProperty("pop", src.getPop());
			json.addProperty("age", src.getAge());
			
			JsonArray volumes = new JsonArray();
			volumes.add(src.getLocation().getVolume());
			volumes.add(Math.round(src.getTargetVolume() * 100) / 100.0);
			json.add("volumes", volumes);
			
			JsonArray surfaces = new JsonArray();
			surfaces.add(src.getLocation().getSurface());
			surfaces.add(Math.round(src.getTargetSurface() * 100) / 100.0);
			json.add("surfaces", surfaces);
			
			TypeToken<Module> moduleToken = new TypeToken<Module>() { };
			JsonElement module = context.serialize(src.getModule(), moduleToken.getType());
			json.add("module", module);
			
			if (src.getTags() > 0) {
				JsonArray tags = new JsonArray();
				
				for (int i = 0; i < src.getTags(); i++) {
					JsonObject tag = new JsonObject();
					tag.addProperty("id", -i - 1);
					
					JsonArray tagVolumes = new JsonArray();
					tagVolumes.add(src.getLocation().getVolume(-i - 1));
					tagVolumes.add(Math.round(src.getTargetVolume(-i - 1) * 100) / 100.0);
					tag.add("volumes", tagVolumes);
					
					JsonArray tagSurfaces = new JsonArray();
					tagSurfaces.add(src.getLocation().getSurface(-i - 1));
					tagSurfaces.add(Math.round(src.getTargetSurface(-i - 1) * 100) / 100.0);
					tag.add("surfaces", tagSurfaces);
					
					tags.add(tag);
				}
				
				json.add("tags", tags);
			}
			
			return json;
		}
	}
	
	static class ModuleSerializer implements JsonSerializer<Module> {
		public JsonElement serialize(Module src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject json = new JsonObject();
			json.addProperty("name", src.getName());
			json.addProperty("phase", src.getPhase());
			return json;
		}
	}
	
	static class GridSerializer implements JsonSerializer<Grid> {
		public JsonElement serialize(Grid src, Type typeOfSrc, JsonSerializationContext context) {
			JsonArray json = new JsonArray();
			TypeToken<Cell> cellToken = new TypeToken<Cell>() { };
			
			for (Object obj : src.getAllObjects()) {
				JsonElement cell = context.serialize(obj, cellToken.getType());
				json.add(cell);
			}
			
			return json;
		}
	}
}
