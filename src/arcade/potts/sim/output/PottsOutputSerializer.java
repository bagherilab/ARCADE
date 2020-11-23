package arcade.potts.sim.output;

import java.lang.reflect.Type;
import java.util.*;
import com.google.gson.*;
import arcade.core.sim.Series;
import arcade.core.sim.output.OutputSerializer;
import arcade.core.agent.cell.Cell;
import arcade.core.env.grid.Grid;
import arcade.core.env.loc.*;
import arcade.potts.sim.*;
import arcade.potts.agent.module.PottsModule;
import arcade.potts.env.loc.*;
import static arcade.potts.env.loc.Voxel.VOXEL_COMPARATOR;
import static arcade.core.agent.cell.Cell.*;

public final class PottsOutputSerializer {
	static Gson makeGSON() {
		GsonBuilder gsonBuilder = OutputSerializer.makeGSONBuilder();
		gsonBuilder.registerTypeHierarchyAdapter(PottsSeries.class, new PottsSeriesSerializer());
		gsonBuilder.registerTypeHierarchyAdapter(Potts.class, new PottsSerializer());
		gsonBuilder.registerTypeHierarchyAdapter(Grid.class, new GridSerializer());
		gsonBuilder.registerTypeHierarchyAdapter(Cell.class, new CellSerializer());
		gsonBuilder.registerTypeHierarchyAdapter(Location.class, new LocationSerializer());
		gsonBuilder.registerTypeAdapter(Voxel.class, new VoxelSerializer());
		return gsonBuilder.create();
	}
	
	static class PottsSeriesSerializer implements JsonSerializer<PottsSeries> {
		public JsonElement serialize(PottsSeries src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject json = (JsonObject)context.serialize(src, Series.class);
			
			// Add potts parameters.
			JsonElement potts = context.serialize(src._potts);
			json.add("potts", potts);
			
			return json;
		}
	}
	
	static class PottsSerializer implements JsonSerializer<Potts> {
		public JsonElement serialize(Potts src, Type typeOfSrc, JsonSerializationContext context) {
			JsonArray json = new JsonArray();
			
			for (Object obj : src.grid.getAllObjects()) {
				Cell cell = (Cell)obj;
				JsonElement voxels = context.serialize(cell.getLocation());
				JsonElement center = context.serialize(((PottsLocation)cell.getLocation()).getCenter());
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
			json.addProperty("phase", ((PottsModule)src.getModule()).getPhase().name());
			json.addProperty("voxels", src.getLocation().getVolume());
			
			JsonArray targets = new JsonArray();
			targets.add((int)(100*src.getTargetVolume())/100.0);
			targets.add((int)(100*src.getTargetSurface())/100.0);
			json.add("targets", targets);
			
			if (src.hasRegions()) {
				JsonArray regions = new JsonArray();
				for (Region region : src.getLocation().getRegions()) {
					JsonObject regionObject = new JsonObject();
					regionObject.addProperty("region", region.name());
					regionObject.addProperty("voxels", src.getLocation().getVolume(region));
					
					JsonArray regionTargets = new JsonArray();
					regionTargets.add((int)(100*src.getTargetVolume(region))/100.0);
					regionTargets.add((int)(100*src.getTargetSurface(region))/100.0);
					regionObject.add("targets", regionTargets);
					
					regions.add(regionObject);
				}
				
				json.add("regions", regions);
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
			
			EnumMap<Region, PottsLocation> locations;
			
			if (src instanceof PottsLocations) { locations = ((PottsLocations)src).locations; }
			else {
				locations = new EnumMap<>(Region.class);
				locations.put(Region.UNDEFINED, (PottsLocation)src);
			}
			
			for (Region region : locations.keySet()) {
				JsonObject obj = new JsonObject();
				JsonArray array = new JsonArray();
				
				ArrayList<Voxel> voxels = locations.get(region).getVoxels();
				voxels.sort(VOXEL_COMPARATOR);
				for (Voxel voxel : voxels) { array.add(context.serialize(voxel)); }
				
				obj.addProperty("region", region.name());
				obj.add("voxels", array);
				
				json.add(obj);
			}
			
			return json;
		}
	}
}
