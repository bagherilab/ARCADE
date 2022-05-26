package arcade.patch.sim.output;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import arcade.core.sim.output.OutputSerializer;

/**
 * Container class for patch-specific object serializers.
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
        return gsonBuilder.create();
    }
}
