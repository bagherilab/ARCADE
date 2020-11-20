package arcade.core.sim.output;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.EnumMap;
import com.google.gson.*;
import static arcade.core.agent.cell.Cell.State;
import static arcade.core.agent.cell.Cell.Region;
import static arcade.core.agent.module.Module.Phase;
import static arcade.core.agent.cell.CellFactory.CellContainer;
import static arcade.core.agent.cell.CellFactory.CellFactoryContainer;
import static arcade.core.env.loc.LocationFactory.LocationContainer;
import static arcade.core.env.loc.LocationFactory.LocationFactoryContainer;
import static arcade.core.env.loc.Location.Voxel;

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
			
			State state = State.valueOf(jsonObject.get("state").getAsString());
			Phase phase = Phase.valueOf(jsonObject.get("phase").getAsString());
			
			JsonArray targets = jsonObject.get("targets").getAsJsonArray();
			double targetVolume = targets.get(0).getAsDouble();
			double targetSurface = targets.get(1).getAsDouble();
			
			EnumMap<Region, Integer> regions = new EnumMap<>(Region.class);
			EnumMap<Region, Double> targetRegionVolumes = new EnumMap<>(Region.class);
			EnumMap<Region, Double> targetRegionSurfaces = new EnumMap<>(Region.class);
			
			if (jsonObject.has("regions")) {
				JsonArray jsonArray = jsonObject.getAsJsonArray("regions");
				for (JsonElement object : jsonArray) {
					JsonObject regionObject = object.getAsJsonObject();
					Region region = Region.valueOf(regionObject.get("region").getAsString());
					int regionVoxels = regionObject.get("voxels").getAsInt();
					
					JsonArray regionTargets = regionObject.get("targets").getAsJsonArray();
					targetRegionVolumes.put(region, regionTargets.get(0).getAsDouble());
					targetRegionSurfaces.put(region, regionTargets.get(1).getAsDouble());
					
					regions.put(region, regionVoxels);
				}
			}
			
			CellContainer cell;
			if (regions.size() == 0) { cell = new CellContainer(id, pop, age, state, phase,
					voxels, targetVolume, targetSurface); }
			else { cell = new CellContainer(id, pop, age, state, phase,
					voxels, regions, targetVolume, targetSurface, targetRegionVolumes, targetRegionSurfaces); }
			
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
			
			// Set up list of all voxels and map for region voxels.
			ArrayList<Voxel> allVoxels = new ArrayList<>();
			EnumMap<Region, ArrayList<Voxel>> regions = new EnumMap<>(Region.class);
			
			// Parse lists of voxels.
			JsonArray jsonArray = jsonObject.getAsJsonArray("location");
			for (JsonElement object : jsonArray) {
				JsonObject regionObject = object.getAsJsonObject();
				Region region = Region.valueOf(regionObject.get("region").getAsString());
				JsonArray voxelArray = regionObject.get("voxels").getAsJsonArray();
				
				ArrayList<Voxel> voxels = new ArrayList<>();
				for (JsonElement element : voxelArray) {
					Voxel voxel = context.deserialize(element, Voxel.class);
					voxels.add(voxel);
					allVoxels.add(voxel);
				}
				
				regions.put(region, voxels);
			}
			
			LocationContainer location;
			if (jsonArray.size() == 1) { location = new LocationContainer(id, center, allVoxels, null); }
			else { location = new LocationContainer(id, center, allVoxels, regions); }
			
			return location;
		}
	}
}
