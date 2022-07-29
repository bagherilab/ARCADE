package arcade.patch.sim.output;

import java.lang.reflect.Type;
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import arcade.core.agent.cell.CellContainer;
import arcade.core.env.loc.LocationContainer;
import arcade.core.sim.output.OutputDeserializer;
import arcade.patch.agent.cell.PatchCellContainer;
import arcade.patch.env.loc.Coordinate;
import arcade.patch.env.loc.CoordinateHex;
import arcade.patch.env.loc.CoordinateRect;
import arcade.patch.env.loc.PatchLocationContainer;
import static arcade.core.sim.Simulation.DEFAULT_LOCATION_TYPE;
import static arcade.core.util.Enums.State;

/**
 * Container class for patch-specific object deserializers.
 * <p>
 * Deserializers include:
 * <ul>
 *     <li>{@link PatchCellDeserializer} for deserializing {@link PatchCellContainer}</li>
 *     <li>{@link LocationListDeserializer} for deserializing {@link PatchLocationContainer}
 *     lists</li>
 *     <li>{@link CoordinateDeserializer} for deserializing {@link Coordinate}</li>
 * </ul>
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
        gsonBuilder.registerTypeAdapter(DEFAULT_LOCATION_TYPE,
                new LocationListDeserializer());
        gsonBuilder.registerTypeAdapter(Coordinate.class,
                new CoordinateDeserializer());
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
            
            return new PatchCellContainer(id, parent, pop, age, divisions,
                    state, volume, height, criticalVolume, criticalHeight);
        }
    }
    
    /**
     * Deserializer for list of {@link PatchLocationContainer} objects.
     */
    public static final class LocationListDeserializer
            implements JsonDeserializer<ArrayList<LocationContainer>> {
        @Override
        public ArrayList<LocationContainer> deserialize(JsonElement json, Type typeOfT,
                                                        JsonDeserializationContext context)
                throws JsonParseException {
            ArrayList<LocationContainer> locations = new ArrayList<>();
            JsonArray jsonArray = json.getAsJsonArray();
            
            for (JsonElement element : jsonArray) {
                JsonObject location = element.getAsJsonObject();
                
                JsonElement coordinateElement = location.get("coordinate");
                Coordinate coordinate = context.deserialize(coordinateElement, Coordinate.class);
                
                for (JsonElement idElement : location.getAsJsonArray("ids")) {
                    int id = idElement.getAsInt();
                    PatchLocationContainer container = new PatchLocationContainer(id, coordinate);
                    locations.add(container);
                }
            }
            
            return locations;
        }
    }
    
    /**
     * Deserializer for {@link Coordinate} objects.
     */
    static class CoordinateDeserializer implements JsonDeserializer<Coordinate> {
        @Override
        public Coordinate deserialize(JsonElement json, Type typeOfT,
                                 JsonDeserializationContext context) throws JsonParseException {
            JsonArray jsonArray = json.getAsJsonArray();
            
            if (jsonArray.size() == 3) {
                int x = jsonArray.get(0).getAsInt();
                int y = jsonArray.get(1).getAsInt();
                int z = jsonArray.get(2).getAsInt();
                return new CoordinateRect(x, y, z);
            } else if (jsonArray.size() == 4) {
                int u = jsonArray.get(0).getAsInt();
                int v = jsonArray.get(1).getAsInt();
                int w = jsonArray.get(2).getAsInt();
                int z = jsonArray.get(3).getAsInt();
                return new CoordinateHex(u, v, w, z);
            }
            
            return null;
        }
    }
}
