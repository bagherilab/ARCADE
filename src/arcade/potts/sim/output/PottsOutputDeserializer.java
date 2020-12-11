package arcade.potts.sim.output;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.EnumMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import arcade.core.sim.output.OutputDeserializer;
import arcade.potts.agent.cell.PottsCellContainer;
import arcade.potts.env.loc.PottsLocationContainer;
import arcade.potts.env.loc.Voxel;
import static arcade.core.util.Enums.Region;
import static arcade.core.util.Enums.State;
import static arcade.potts.util.PottsEnums.Phase;

public final class PottsOutputDeserializer {
    protected PottsOutputDeserializer() {
        throw new UnsupportedOperationException();
    }
    
    static Gson makeGSON() {
        GsonBuilder gsonBuilder = OutputDeserializer.makeGSONBuilder();
        gsonBuilder.registerTypeAdapter(PottsCellContainer.class,
                new PottsCellDeserializer());
        gsonBuilder.registerTypeAdapter(PottsLocationContainer.class,
                new PottsLocationDeserializer());
        gsonBuilder.registerTypeAdapter(Voxel.class, new VoxelDeserializer());
        return gsonBuilder.create();
    }
    
    static class PottsCellDeserializer implements JsonDeserializer<PottsCellContainer> {
        @Override
        public PottsCellContainer deserialize(JsonElement json, Type typeOfT,
                                              JsonDeserializationContext context)
                throws JsonParseException {
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
            
            PottsCellContainer cell;
            if (regions.size() == 0) {
                cell = new PottsCellContainer(id, pop, age, state, phase,
                        voxels, targetVolume, targetSurface);
            } else {
                cell = new PottsCellContainer(id, pop, age, state, phase,
                        voxels, regions, targetVolume, targetSurface,
                        targetRegionVolumes, targetRegionSurfaces);
            }
            
            return cell;
        }
    }
    
    static class PottsLocationDeserializer implements JsonDeserializer<PottsLocationContainer> {
        @Override
        public PottsLocationContainer deserialize(JsonElement json, Type typeOfT,
                                                  JsonDeserializationContext context)
                throws JsonParseException {
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
            
            PottsLocationContainer location;
            if (jsonArray.size() == 1) {
                location = new PottsLocationContainer(id, center, allVoxels);
            } else {
                location = new PottsLocationContainer(id, center, allVoxels, regions);
            }
            
            return location;
        }
    }
    
    static class VoxelDeserializer implements JsonDeserializer<Voxel> {
        @Override
        public Voxel deserialize(JsonElement json, Type typeOfT,
                                 JsonDeserializationContext context) throws JsonParseException {
            JsonArray jsonArray = json.getAsJsonArray();
            int x = jsonArray.get(0).getAsInt();
            int y = jsonArray.get(1).getAsInt();
            int z = jsonArray.get(2).getAsInt();
            return new Voxel(x, y, z);
        }
    }
}
