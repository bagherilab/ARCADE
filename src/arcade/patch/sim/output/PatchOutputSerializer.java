package arcade.patch.sim.output;

import java.lang.reflect.Type;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import arcade.core.sim.Series;
import arcade.core.sim.output.OutputSerializer;
import arcade.patch.sim.PatchSeries;

/**
 * Container class for patch-specific object serializers.
 * <p>
 * Generic serializers include:
 * <ul>
 *     <li>{@link PatchSeriesSerializer} for serializing {@link PatchSeries} settings</li>
 * </ul>
 */

public final class PatchOutputSerializer {
    /**
     * Hidden utility class constructor.
     */
    protected PatchOutputSerializer() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Creates a {@code Gson} with generic and implementation-specific adaptors.
     *
     * @return  a {@code Gson} instance
     */
    static Gson makeGSON() {
        GsonBuilder gsonBuilder = OutputSerializer.makeGSONBuilder();
        gsonBuilder.registerTypeAdapter(PatchSeries.class,
                new PatchSeriesSerializer());
        return gsonBuilder.create();
    }
    
    /**
     * Serializer for {@link PatchSeries} objects.
     * <p>
     * The object is first serialized using the generic {@link Series} and
     * potts-specific contents are then appended:
     * <pre>
     *     ...
     *     "patch": {
     *         "(key)" : (value),
     *         "(key)" : (value),
     *         ...
     *     }
     *     ...
     * </pre>
     */
    static class PatchSeriesSerializer implements JsonSerializer<PatchSeries> {
        @Override
        public JsonElement serialize(PatchSeries src, Type typeOfSrc,
                                     JsonSerializationContext context) {
            JsonObject json = (JsonObject) context.serialize(src, Series.class);
            
            // Add potts parameters.
            JsonElement patch = context.serialize(src.patch);
            json.add("patch", patch);
            
            return json;
        }
    }
}
