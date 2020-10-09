package arcade.sim.output;

import java.lang.reflect.Type;
import java.util.HashMap;
import com.google.gson.*;
import arcade.sim.Series;
import arcade.sim.Potts;
import arcade.agent.cell.Cell;
import arcade.agent.module.Module;
import arcade.env.grid.Grid;
import arcade.env.loc.*;
import arcade.util.MiniBox;
import static arcade.env.loc.Location.Voxel;

public abstract class OutputSerializer {
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
		gsonBuilder.registerTypeHierarchyAdapter(Module.class, new ModuleSerializer());
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
			json.addProperty("ticks", src.getTicks());
			
			JsonObject size = new JsonObject();
			size.addProperty("length", src._length);
			size.addProperty("width", src._length);
			size.addProperty("height", src._width);
			json.add("size", size);
			
			// Add potts parameters.
			JsonElement potts = context.serialize(src._potts);
			json.add("potts", potts);
			
			// Add population parameters.
			JsonObject populations = new JsonObject();
			for (String pop : src._populations.keySet()) {
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
				JsonObject location = new JsonObject();
				location.addProperty("id", cell.getID());
				location.add("voxels", voxels);
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
			
			JsonArray volumes = new JsonArray();
			volumes.add(src.getLocation().getVolume());
			volumes.add(Math.round(src.getTargetVolume() * 100) / 100.0);
			json.add("volumes", volumes);
			
			JsonArray surfaces = new JsonArray();
			surfaces.add(src.getLocation().getSurface());
			surfaces.add(Math.round(src.getTargetSurface() * 100) / 100.0);
			json.add("surfaces", surfaces);
			
			JsonElement module = context.serialize(src.getModule());
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
			
			HashMap<Integer, PottsLocation> locations;
			
			if (src instanceof PottsLocations) { locations = ((PottsLocations) src).locations; }
			else {
				locations = new HashMap<>();
				locations.put(0, (PottsLocation)src);
			}
			
			for (int tag : locations.keySet()) {
				JsonObject obj = new JsonObject();
				
				JsonArray voxels = new JsonArray();
				for (Voxel voxel : src.getVoxels()) { voxels.add(context.serialize(voxel)); }
				
				obj.addProperty("tag", tag);
				obj.add("voxels", voxels);
				
				json.add(obj);
			}
			
			return json;
		}
	}
}
