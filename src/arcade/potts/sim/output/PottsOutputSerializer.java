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
import arcade.core.agent.cell.CellContainer;
import arcade.core.env.location.LocationContainer;
import arcade.core.sim.Series;
import arcade.core.sim.output.OutputSerializer;
import arcade.potts.agent.cell.PottsCellContainer;
import arcade.potts.env.location.PottsLocationContainer;
import arcade.potts.env.location.Voxel;
import arcade.potts.sim.PottsSeries;
import static arcade.core.util.Enums.Region;
import static arcade.potts.env.location.Voxel.VOXEL_COMPARATOR;

/**
 * Container class for potts-specific object serializers.
 * <p>
 * Generic serializers include:
 * <ul>
 *     <li>{@link PottsSeriesSerializer} for serializing {@link PottsSeries}</li>
 *     <li>{@link PottsCellSerializer} for serializing {@link PottsCellContainer}</li>
 *     <li>{@link PottsLocationSerializer} for serializing {@link PottsLocationContainer}</li>
 *     <li>{@link VoxelSerializer} for serializing {@link Voxel}</li>
 * </ul>
 */

public final class PottsOutputSerializer {
    /**
     * Hidden utility class constructor.
     */
    protected PottsOutputSerializer() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Creates a {@code Gson} with generic and implementation-specific adaptors.
     *
     * @return  a {@code Gson} instance
     */
    static Gson makeGSON() {
        GsonBuilder gsonBuilder = OutputSerializer.makeGSONBuilder();
        gsonBuilder.registerTypeAdapter(PottsSeries.class,
                new PottsSeriesSerializer());
        gsonBuilder.registerTypeAdapter(CellContainer.class,
                new CellSerializer());
        gsonBuilder.registerTypeAdapter(PottsCellContainer.class,
                new PottsCellSerializer());
        gsonBuilder.registerTypeAdapter(LocationContainer.class,
                new LocationSerializer());
        gsonBuilder.registerTypeAdapter(PottsLocationContainer.class,
                new PottsLocationSerializer());
        gsonBuilder.registerTypeAdapter(Voxel.class,
                new VoxelSerializer());
        return gsonBuilder.create();
    }
    
    /**
     * Serializer for {@link PottsSeries} objects.
     * <p>
     * The object is first serialized using the generic {@link Series} and
     * potts-specific contents are then appended:
     * <pre>
     *     ...
     *     "potts": {
     *         "(key)" : (value),
     *         "(key)" : (value),
     *         ...
     *     }
     *     ...
     * </pre>
     */
    static class PottsSeriesSerializer implements JsonSerializer<PottsSeries> {
        @Override
        public JsonElement serialize(PottsSeries src, Type typeOfSrc,
                                     JsonSerializationContext context) {
            JsonObject json = (JsonObject) context.serialize(src, Series.class);
            
            // Add potts parameters.
            JsonElement potts = context.serialize(src.potts);
            json.add("potts", potts);
            
            return json;
        }
    }
    
    /**
     * Serializer for {@link CellContainer} objects.
     * <p>
     * Uses serialization for {@link PottsCellContainer}.
     * </p>
     */
    static class CellSerializer implements JsonSerializer<CellContainer> {
        @Override
        public JsonElement serialize(CellContainer src, Type typeOfSrc,
                                     JsonSerializationContext context) {
            return context.serialize(src, PottsCellContainer.class);
        }
    }
    
    /**
     * Serializer for {@link PottsCellContainer} objects.
     * <p>
     * The container object is formatted as:
     * <pre>
     *     {
     *         "id": (id),
     *         "parent": (parent),
     *         "pop": (pop),
     *         "age": (age),
     *         "divisions": (divisions),
     *         "state": (state),
     *         "phase": (phase),
     *         "voxels": (voxels),
     *         "criticals": [(critical volume), (critical height)],
     *         "regions": [
     *             {
     *                 "region": (region name),
     *                 "voxels": (region voxels),
     *                 "criticals": [(critical region volume), (critical region height)]
     *             },
     *             ...
     *         ]
     *     }
     * </pre>
     * <p>
     * If there are no regions, the regions array is not included.
     */
    static class PottsCellSerializer implements JsonSerializer<PottsCellContainer> {
        @Override
        public JsonElement serialize(PottsCellContainer src, Type typeOfSrc,
                                     JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            
            json.addProperty("id", src.id);
            json.addProperty("parent", src.parent);
            json.addProperty("pop", src.pop);
            json.addProperty("age", src.age);
            json.addProperty("divisions", src.divisions);
            json.addProperty("state", src.state.name());
            json.addProperty("phase", src.phase.name());
            json.addProperty("voxels", src.voxels);
            
            JsonArray criticals = new JsonArray();
            criticals.add((int) (100 * src.criticalVolume) / 100.0);
            criticals.add((int) (100 * src.criticalHeight) / 100.0);
            json.add("criticals", criticals);
            
            if (src.regionVoxels != null) {
                JsonArray regions = new JsonArray();
                for (Region region : src.regionVoxels.keySet()) {
                    JsonObject regionObject = new JsonObject();
                    regionObject.addProperty("region", region.name());
                    regionObject.addProperty("voxels", src.regionVoxels.get(region));
                    
                    JsonArray regionCriticals = new JsonArray();
                    double volume = (int) (100 * src.criticalRegionVolumes.get(region)) / 100.0;
                    double height = (int) (100 * src.criticalRegionHeights.get(region)) / 100.0;
                    regionCriticals.add(volume);
                    regionCriticals.add(height);
                    regionObject.add("criticals", regionCriticals);
                    
                    regions.add(regionObject);
                }
                
                json.add("regions", regions);
            }
            
            return json;
        }
    }
    
    /**
     * Serializer for {@link LocationContainer} objects.
     * <p>
     * Uses serialization for {@link PottsLocationContainer}.
     * </p>
     */
    static class LocationSerializer implements JsonSerializer<LocationContainer> {
        @Override
        public JsonElement serialize(LocationContainer src, Type typeOfSrc,
                                     JsonSerializationContext context) {
            return context.serialize(src, PottsLocationContainer.class);
        }
    }
    
    /**
     * Serializer for {@link PottsLocationContainer} objects.
     * <p>
     * The container object is formatted as:
     * <pre>
     *     {
     *         "id": (id),
     *         "center": (center voxel),
     *         "location": [
     *             {
     *                 "region": (region name),
     *                 "voxels": [
     *                     (region voxel),
     *                     (region voxel),
     *                     ...
     *                 ]
     *             },
     *             ...
     *         ]
     *     }
     * </pre>
     * <p>
     * If there are no regions, all voxels are listed as one region with the
     * "UNDEFINED" region name.
     */
    static class PottsLocationSerializer implements JsonSerializer<PottsLocationContainer> {
        @Override
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
    
    /**
     * Serializer for {@link Voxel} objects.
     * <p>
     * The voxel object is formatted as:
     * <pre>
     *     [(x), (y), (z)]
     * </pre>
     */
    static class VoxelSerializer implements JsonSerializer<Voxel> {
        @Override
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
