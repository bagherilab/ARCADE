package arcade.sim.output;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import com.google.gson.*;
import arcade.env.loc.*;
import static arcade.agent.cell.CellFactory.CellContainer;
import static arcade.agent.cell.CellFactory.CellFactoryContainer;
import static arcade.env.loc.LocationFactory.LocationContainer;
import static arcade.env.loc.LocationFactory.LocationFactoryContainer;
import static arcade.env.loc.Location.Voxel;

public final class OutputDeserializer {
	static Gson makeGSON() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(CellFactoryContainer.class, new CellFactoryDeserializer());
		gsonBuilder.registerTypeAdapter(CellContainer.class, new CellDeserializer());
		gsonBuilder.registerTypeAdapter(LocationFactoryContainer.class, new LocationFactoryDeserializer());
		gsonBuilder.registerTypeAdapter(LocationContainer.class, new LocationDeserializer());
		gsonBuilder.registerTypeAdapter(Voxel.class, new VoxelDeserializer());
		return gsonBuilder.create();
	}
	
	static class CellFactoryDeserializer implements JsonDeserializer<CellFactoryContainer> {
		public CellFactoryContainer deserialize(JsonElement json, Type typeOfT,
													JsonDeserializationContext context) throws JsonParseException {
			JsonArray jsonArray = json.getAsJsonArray();
			CellFactoryContainer container = new CellFactoryContainer();
			
			for (JsonElement element : jsonArray) {
				JsonObject cell = element.getAsJsonObject();
				CellContainer cellContainer = context.deserialize(cell, CellContainer.class);
				container.cells.add(cellContainer);
			}
			
			return container;
		}
	}
	
	static class CellDeserializer implements JsonDeserializer<CellContainer> {
		public CellContainer deserialize(JsonElement json, Type typeOfT,
									JsonDeserializationContext context) throws JsonParseException {
			JsonObject jsonObject = json.getAsJsonObject();
			
			int id = jsonObject.get("id").getAsInt();
			int pop = jsonObject.get("pop").getAsInt();
			int age = jsonObject.get("age").getAsInt();
			int voxels = jsonObject.get("voxels").getAsInt();
			
			JsonArray targets = jsonObject.get("targets").getAsJsonArray();
			double targetVolume = targets.get(0).getAsDouble();
			double targetSurface = targets.get(1).getAsDouble();
			
			HashMap<String, Integer> tags = new HashMap<>();
			HashMap<String, Double> targetTagVolumes = new HashMap<>();
			HashMap<String, Double> targetTagSurfaces = new HashMap<>();
			
			if (jsonObject.has("tags")) {
				JsonArray jsonArray = jsonObject.getAsJsonArray("tags");
				for (JsonElement object : jsonArray) {
					JsonObject tagObject = object.getAsJsonObject();
					String tag = tagObject.get("tag").getAsString();
					int tagVoxels = tagObject.get("voxels").getAsInt();
					
					JsonArray tagTargets = tagObject.get("targets").getAsJsonArray();
					targetTagVolumes.put(tag, tagTargets.get(0).getAsDouble());
					targetTagSurfaces.put(tag, tagTargets.get(1).getAsDouble());
					
					tags.put(tag, tagVoxels);
				}
			}
		
			CellContainer cell;
			if (tags.size() == 0) { cell = new CellContainer(id, pop, age,
					voxels, targetVolume, targetSurface); }
			else { cell = new CellContainer(id, pop, age,
					voxels, tags, targetVolume, targetSurface, targetTagVolumes, targetTagSurfaces); }
			
			return cell;
		}
	}
	
	static class LocationFactoryDeserializer implements JsonDeserializer<LocationFactoryContainer> {
		public LocationFactoryContainer deserialize(JsonElement json, Type typeOfT,
										   JsonDeserializationContext context) throws JsonParseException {
			JsonArray jsonArray = json.getAsJsonArray();
			LocationFactoryContainer container = new LocationFactoryContainer();
			
			for (JsonElement element : jsonArray) {
				JsonObject locationObject = element.getAsJsonObject();
				LocationContainer location = context.deserialize(locationObject, LocationContainer.class);
				container.locations.add(location);
			}
			
			return container;
		}
	}
	
	static class VoxelDeserializer implements JsonDeserializer<Voxel> {
		public Voxel deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonArray jsonArray = json.getAsJsonArray();
			int x = jsonArray.get(0).getAsInt();
			int y = jsonArray.get(1).getAsInt();
			int z = jsonArray.get(2).getAsInt();
			return new Voxel(x, y, z);
		}
	}
	
	static class LocationDeserializer implements JsonDeserializer<LocationContainer> {
		public LocationContainer deserialize(JsonElement json, Type typeOfT,
									   JsonDeserializationContext context) throws JsonParseException {
			JsonObject jsonObject = json.getAsJsonObject();
			
			// Parse out id and center voxel.
			int id = jsonObject.get("id").getAsInt();
			Voxel center = context.deserialize(jsonObject.get("center"), Voxel.class);
			
			// Set up list of all voxels and map for tag voxels.
			ArrayList<Voxel> allVoxels = new ArrayList<>();
			HashMap<String, ArrayList<Voxel>> tags = new HashMap<>();
			
			// Parse lists of voxels.
			JsonArray jsonArray = jsonObject.getAsJsonArray("location");
			for (JsonElement object : jsonArray) {
				JsonObject tagObject = object.getAsJsonObject();
				String tag = tagObject.get("tag").getAsString();
				JsonArray voxelArray = tagObject.get("voxels").getAsJsonArray();
				
				ArrayList<Voxel> voxels = new ArrayList<>();
				for (JsonElement element : voxelArray) {
					Voxel voxel = context.deserialize(element, Voxel.class);
					voxels.add(voxel);
					allVoxels.add(voxel);
				}
				
				tags.put(tag, voxels);
			}
			
			LocationContainer location;
			if (jsonArray.size() == 1) { location = new LocationContainer(id, center, allVoxels, null); }
			else { location = new LocationContainer(id, center, allVoxels, tags); }
			
			return location;
		}
	}
}