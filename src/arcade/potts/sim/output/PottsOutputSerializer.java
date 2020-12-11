package arcade.potts.sim.output;

import java.lang.reflect.Type;
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import arcade.core.sim.Series;
import arcade.core.sim.output.OutputSerializer;
import arcade.potts.agent.cell.PottsCellContainer;
import arcade.potts.env.loc.PottsLocationContainer;
import arcade.potts.env.loc.Voxel;
import arcade.potts.sim.PottsSeries;
import static arcade.core.util.Enums.Region;
import static arcade.potts.env.loc.Voxel.VOXEL_COMPARATOR;

public final class PottsOutputSerializer {
    protected PottsOutputSerializer() {
        throw new UnsupportedOperationException();
    }
    
    static Gson makeGSON() {
        GsonBuilder gsonBuilder = OutputSerializer.makeGSONBuilder();
        gsonBuilder.registerTypeAdapter(PottsSeries.class,
                new PottsSeriesSerializer());
        gsonBuilder.registerTypeAdapter(PottsCellContainer.class,
                new PottsCellSerializer());
        gsonBuilder.registerTypeAdapter(PottsLocationContainer.class,
                new PottsLocationSerializer());
        gsonBuilder.registerTypeAdapter(Voxel.class,
                new VoxelSerializer());
        return gsonBuilder.create();
    }
    
    static class PottsSeriesSerializer implements JsonSerializer<PottsSeries> {
        public JsonElement serialize(PottsSeries src, Type typeOfSrc,
                                     JsonSerializationContext context) {
            JsonObject json = (JsonObject) context.serialize(src, Series.class);
            
            // Add potts parameters.
            JsonElement potts = context.serialize(src.potts);
            json.add("potts", potts);
            
            return json;
        }
    }
    
    static class PottsCellSerializer implements JsonSerializer<PottsCellContainer> {
        public JsonElement serialize(PottsCellContainer src, Type typeOfSrc,
                                     JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            
            json.addProperty("id", src.id);
            json.addProperty("pop", src.pop);
            json.addProperty("age", src.age);
            json.addProperty("state", src.state.name());
            json.addProperty("phase", src.phase.name());
            json.addProperty("voxels", src.voxels);
            
            JsonArray targets = new JsonArray();
            targets.add((int) (100 * src.targetVolume) / 100.0);
            targets.add((int) (100 * src.targetSurface) / 100.0);
            json.add("targets", targets);
            
            if (src.regionVoxels != null) {
                JsonArray regions = new JsonArray();
                for (Region region : src.regionVoxels.keySet()) {
                    JsonObject regionObject = new JsonObject();
                    regionObject.addProperty("region", region.name());
                    regionObject.addProperty("voxels", src.regionVoxels.get(region));
                    
                    JsonArray regionTargets = new JsonArray();
                    regionTargets.add((int) (100 * src.regionTargetVolume.get(region)) / 100.0);
                    regionTargets.add((int) (100 * src.regionTargetSurface.get(region)) / 100.0);
                    regionObject.add("targets", regionTargets);
                    
                    regions.add(regionObject);
                }
                
                json.add("regions", regions);
            }
            
            return json;
        }
    }
    
    static class PottsLocationSerializer implements JsonSerializer<PottsLocationContainer> {
        public JsonElement serialize(PottsLocationContainer src, Type typeOfSrc,
                                     JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            
            json.addProperty("id", src.id);
            json.add("center", context.serialize(src.center));
            
            JsonArray location = new JsonArray();
            
            if (src.regions == null) {
                JsonObject obj = new JsonObject();
                JsonArray array = new JsonArray();
                
                ArrayList<Voxel> voxels = src.allVoxels;
                voxels.sort(VOXEL_COMPARATOR);
                voxels.forEach(voxel -> array.add(context.serialize(voxel)));
                
                obj.addProperty("region", Region.UNDEFINED.name());
                obj.add("voxels", array);
                
                location.add(obj);
            } else {
                for (Region region : src.regions.keySet()) {
                    JsonObject obj = new JsonObject();
                    JsonArray array = new JsonArray();
                    
                    ArrayList<Voxel> voxels = src.regions.get(region);
                    voxels.sort(VOXEL_COMPARATOR);
                    voxels.forEach(voxel -> array.add(context.serialize(voxel)));
                    
                    obj.addProperty("region", region.name());
                    obj.add("voxels", array);
                    
                    location.add(obj);
                }
            }
            
            json.add("location", location);
            
            return json;
        }
    }
    
    static class VoxelSerializer implements JsonSerializer<Voxel> {
        public JsonElement serialize(Voxel src, Type typeOfSrc,
                                     JsonSerializationContext context) {
            JsonArray json = new JsonArray();
            json.add(src.x);
            json.add(src.y);
            json.add(src.z);
            return json;
        }
    }
}
