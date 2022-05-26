package arcade.patch.sim.output;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import arcade.core.sim.output.OutputDeserializer;

/**
 * Container class for patch-specific object deserializers.
 */

public final class PatchOutputDeserializer {
    /**
     * Hidden utility class constructor.
     */
    protected PatchOutputDeserializer() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Creates a {@code Gson} with generic and implementation-specific adaptors.
     *
     * @return  a {@code Gson} instance
     */
    static Gson makeGSON() {
        GsonBuilder gsonBuilder = OutputDeserializer.makeGSONBuilder();
        return gsonBuilder.create();
    }
}
