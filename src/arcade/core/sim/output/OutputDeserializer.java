package arcade.core.sim.output;

import java.lang.reflect.Type;
import java.util.ArrayList;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import arcade.core.agent.cell.CellContainer;
import arcade.core.env.location.LocationContainer;
import static arcade.core.sim.Simulation.DEFAULT_CELL_TYPE;
import static arcade.core.sim.Simulation.DEFAULT_LOCATION_TYPE;

/**
 * Container class for object deserializers.
 *
 * <p>Generic deserializers include:
 *
 * <ul>
 *   <li>{@link CellListDeserializer} for deserializing a list of {@link CellContainer} instances
 *   <li>{@link LocationListDeserializer} for deserializing a list of {@link LocationContainer}
 *       instances
 * </ul>
 */
public final class OutputDeserializer {
    /** Hidden utility class constructor. */
    protected OutputDeserializer() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a {@code GsonBuilder} with generic adaptors.
     *
     * @return a {@code GsonBuilder} instance
     */
    public static GsonBuilder makeGSONBuilder() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(DEFAULT_CELL_TYPE, new CellListDeserializer());
        gsonBuilder.registerTypeAdapter(DEFAULT_LOCATION_TYPE, new LocationListDeserializer());
        return gsonBuilder;
    }

    /** Deserializer for a list of {@link CellContainer} objects. */
    public static final class CellListDeserializer
            implements JsonDeserializer<ArrayList<CellContainer>> {
        @Override
        public ArrayList<CellContainer> deserialize(
                JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            ArrayList<CellContainer> cells = new ArrayList<>();
            JsonArray jsonArray = json.getAsJsonArray();

            for (JsonElement element : jsonArray) {
                JsonObject cell = element.getAsJsonObject();
                CellContainer cont = context.deserialize(cell, CellContainer.class);
                cells.add(cont);
            }

            return cells;
        }
    }

    /** Deserializer for a list of {@link LocationContainer} objects. */
    public static final class LocationListDeserializer
            implements JsonDeserializer<ArrayList<LocationContainer>> {
        @Override
        public ArrayList<LocationContainer> deserialize(
                JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            ArrayList<LocationContainer> locations = new ArrayList<>();
            JsonArray jsonArray = json.getAsJsonArray();

            for (JsonElement element : jsonArray) {
                JsonObject location = element.getAsJsonObject();
                LocationContainer cont = context.deserialize(location, LocationContainer.class);
                locations.add(cont);
            }

            return locations;
        }
    }
}
