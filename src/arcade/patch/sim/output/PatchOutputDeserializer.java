package arcade.patch.sim.output;

import java.lang.reflect.Type;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import arcade.core.agent.cell.CellContainer;
import arcade.core.sim.output.OutputDeserializer;
import arcade.patch.agent.cell.PatchCellContainer;
import static arcade.core.util.Enums.State;

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
        gsonBuilder.registerTypeAdapter(CellContainer.class,
                new PatchCellDeserializer());
        gsonBuilder.registerTypeAdapter(PatchCellContainer.class,
                new PatchCellDeserializer());
        return gsonBuilder.create();
    }
    
    /**
     * Deserializer for {@link PatchCellContainer} objects.
     */
    static class PatchCellDeserializer implements JsonDeserializer<PatchCellContainer> {
        @Override
        public PatchCellContainer deserialize(JsonElement json, Type typeOfT,
                                              JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            
            int id = jsonObject.get("id").getAsInt();
            int parent = jsonObject.get("parent").getAsInt();
            int pop = jsonObject.get("pop").getAsInt();
            int age = jsonObject.get("age").getAsInt();
            int divisions = jsonObject.get("divisions").getAsInt();
            double volume = jsonObject.get("volume").getAsDouble();
            double height = jsonObject.get("height").getAsDouble();
            
            State state = State.valueOf(jsonObject.get("state").getAsString());
            
            JsonArray criticals = jsonObject.get("criticals").getAsJsonArray();
            double criticalVolume = criticals.get(0).getAsDouble();
            double criticalHeight = criticals.get(1).getAsDouble();
            
            return new PatchCellContainer(id, parent, pop, age, divisions, state,
                        volume, height, criticalVolume, criticalHeight);
        }
    }
}
