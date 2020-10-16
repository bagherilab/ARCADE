package arcade.sim.output;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import com.google.gson.*;
import arcade.env.loc.*;
import static arcade.env.loc.Location.Voxel;

public final class OutputDeserializer {
	static Gson makeGSON() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Location.class, new LocationDeserializer());
		gsonBuilder.registerTypeAdapter(Voxel.class, new VoxelDeserializer());
		return gsonBuilder.create();
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
	
	static class LocationDeserializer implements JsonDeserializer<Location> {
		public Location deserialize(JsonElement json, Type typeOfT,
									   JsonDeserializationContext context) throws JsonParseException {
			JsonArray jsonArray = json.getAsJsonArray();
			HashMap<Integer, ArrayList<Voxel>> locations = new HashMap<>();
			boolean is3D = false;
			
			// Parse lists of voxels.
			for (JsonElement object : jsonArray) {
				JsonObject jsonObject = object.getAsJsonObject();
				int tag = jsonObject.get("tag").getAsInt();
				JsonArray voxelArray = jsonObject.get("voxels").getAsJsonArray();
				
				ArrayList<Voxel> voxels = new ArrayList<>();
				for (JsonElement element : voxelArray) {
					Voxel voxel = context.deserialize(element, Voxel.class);
					voxels.add(voxel);
					if (voxel.z != 0) { is3D = true; }
				}
				
				locations.put(tag, voxels);
			}
			
			// Create object based on tags and dimension.
			if (locations.size() == 1) {
				if (is3D) { return new PottsLocation3D(locations.get(0)); }
				else { return new PottsLocation2D(locations.get(0)); }
			} else {
				PottsLocations loc;
				
				if (is3D) { loc = new PottsLocations3D(new ArrayList<>()); }
				else { loc = new PottsLocations2D(new ArrayList<>()); }
				
				for (Integer tag : locations.keySet()) {
					for (Voxel voxel : locations.get(tag)) {
						loc.add(tag, voxel.x, voxel.y, voxel.z);
					}
				}
				
				return loc;
			}
		}
	}
}
