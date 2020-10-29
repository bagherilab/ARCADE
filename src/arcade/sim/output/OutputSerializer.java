package arcade.sim.output;

import java.lang.reflect.Type;
import java.util.*;
import com.google.gson.*;
import arcade.sim.Series;
import arcade.sim.Potts;
import arcade.agent.cell.Cell;
import arcade.env.grid.Grid;
import arcade.env.loc.*;
import arcade.util.MiniBox;
import static arcade.env.loc.Location.VOXEL_COMPARATOR;
import static arcade.env.loc.Location.Voxel;
import static arcade.agent.cell.Cell.*;

public final class OutputSerializer {
	/** Regular expression for fractions */
	public static final String DOUBLE_REGEX = "^(-?\\d*\\.\\d*)$|^(-?\\d*\\.\\d*E\\d+)$";
	
	/** Regular expression for integers */
	public static final String INTEGER_REGEX = "^(-?\\d+)$|^(-?\\d+E\\d+)$";
	
	static Gson makeGSON() {
		GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();
		gsonBuilder.registerTypeAdapter(Series.class, new SeriesSerializer());
		gsonBuilder.registerTypeAdapter(MiniBox.class, new MiniBoxSerializer());
		gsonBuilder.registerTypeHierarchyAdapter(Potts.class, new PottsSerializer());
		gsonBuilder.registerTypeHierarchyAdapter(Grid.class, new GridSerializer());
		gsonBuilder.registerTypeHierarchyAdapter(Cell.class, new CellSerializer());
		gsonBuilder.registerTypeHierarchyAdapter(Location.class, new LocationSerializer());
		gsonBuilder.registerTypeAdapter(Voxel.class, new VoxelSerializer());
		return gsonBuilder.create();
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
			
			json.addProperty("ticks", src.getTicks());
			
			JsonObject size = new JsonObject();
			size.addProperty("length", src._length);
			size.addProperty("width", src._width);
			size.addProperty("height", src._height);
			json.add("size", size);
			
			// Add potts parameters.
			JsonElement potts = context.serialize(src._potts);
			json.add("potts", potts);
			
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
	
	static class PottsSerializer implements JsonSerializer<Potts> {
		public JsonElement serialize(Potts src, Type typeOfSrc, JsonSerializationContext context) {
			JsonArray json = new JsonArray();
			
			for (Object obj : src.grid.getAllObjects()) {
				Cell cell = (Cell)obj;
				JsonElement voxels = context.serialize(cell.getLocation());
				JsonElement center = context.serialize(cell.getLocation().getCenter());
				JsonObject location = new JsonObject();
				location.addProperty("id", cell.getID());
				location.add("center", center);
				location.add("location", voxels);
				json.add(location);
			}
			
			return json;
		}
	}
	
	static class CellSerializer implements JsonSerializer<Cell> {
		public JsonElement serialize(Cell src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject json = new JsonObject();
			
			json.addProperty("id", src.getID());
			json.addProperty("pop", src.getPop());
			json.addProperty("age", src.getAge());
			json.addProperty("state", src.getState().name());
			json.addProperty("phase", src.getModule().getPhase().name());
			json.addProperty("voxels", src.getLocation().getVolume());
			
			JsonArray targets = new JsonArray();
			targets.add((int)(100*src.getTargetVolume())/100.0);
			targets.add((int)(100*src.getTargetSurface())/100.0);
			json.add("targets", targets);
			
			if (src.hasTags()) {
				JsonArray tags = new JsonArray();
				for (Tag tag : src.getLocation().getTags()) {
					JsonObject tagObject = new JsonObject();
					tagObject.addProperty("tag", tag.name());
					tagObject.addProperty("voxels", src.getLocation().getVolume(tag));
					
					JsonArray tagTargets = new JsonArray();
					tagTargets.add((int)(100*src.getTargetVolume(tag))/100.0);
					tagTargets.add((int)(100*src.getTargetSurface(tag))/100.0);
					tagObject.add("targets", tagTargets);
					
					tags.add(tagObject);
				}
				
				json.add("tags", tags);
			}
			
			return json;
		}
	}
	
	static class GridSerializer implements JsonSerializer<Grid> {
		public JsonElement serialize(Grid src, Type typeOfSrc, JsonSerializationContext context) {
			JsonArray json = new JsonArray();
			
			for (Object obj : src.getAllObjects()) {
				JsonElement cell = context.serialize(obj);
				json.add(cell);
			}
			
			return json;
		}
	}
	
	static class VoxelSerializer implements JsonSerializer<Voxel> {
		public JsonElement serialize(Voxel src, Type typeOfSrc, JsonSerializationContext context) {
			JsonArray json = new JsonArray();
			json.add(src.x);
			json.add(src.y);
			json.add(src.z);
			return json;
		}
	}
	
	static class LocationSerializer implements JsonSerializer<Location> {
		public JsonElement serialize(Location src, Type typeOfSrc, JsonSerializationContext context) {
			JsonArray json = new JsonArray();
			
			EnumMap<Tag, PottsLocation> locations;
			
			if (src instanceof PottsLocations) { locations = ((PottsLocations)src).locations; }
			else {
				locations = new EnumMap<>(Tag.class);
				locations.put(Tag.UNDEFINED, (PottsLocation)src);
			}
			
			for (Tag tag : Tag.values()) {
				JsonObject obj = new JsonObject();
				JsonArray array = new JsonArray();
				
				ArrayList<Voxel> voxels = locations.get(tag).getVoxels();
				voxels.sort(VOXEL_COMPARATOR);
				for (Voxel voxel : voxels) { array.add(context.serialize(voxel)); }
				
				obj.addProperty("tag", tag.name());
				obj.add("voxels", array);
				
				json.add(obj);
			}
			
			return json;
		}
	}
}
