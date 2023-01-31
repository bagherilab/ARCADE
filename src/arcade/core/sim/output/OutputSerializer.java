package arcade.core.sim.output;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import arcade.core.agent.cell.CellContainer;
import arcade.core.env.location.LocationContainer;
import arcade.core.sim.Series;
import arcade.core.util.MiniBox;
import static arcade.core.sim.Simulation.DEFAULT_CELL_TYPE;
import static arcade.core.sim.Simulation.DEFAULT_LOCATION_TYPE;

/**
 * Container class for object serializers.
 * <p>
 * Generic serializers include:
 * <ul>
 *     <li>{@code SeriesSerializer} for serializing {@link Series}</li>
 *     <li>{@code MiniBoxSerializer} for serializing {@link MiniBox}</li>
 *     <li>{@code CellListSerializer} for serializing a list of
 *     {@link CellContainer}</li>
 *     <li>{@code LocationListSerializer} for serializing a list of
 *     {@link LocationContainer}</li>
 * </ul>
 */

public final class OutputSerializer {
    /** Regular expression for fractions. */
    public static final String DOUBLE_REGEX = "^(-?\\d*\\.\\d*)$|^(-?\\d*\\.\\d*E-?\\d+)$";
    
    /** Regular expression for integers. */
    public static final String INTEGER_REGEX = "^(-?\\d+)$|^(-?\\d+E-?\\d+)$";
    
    /**
     * Hidden utility class constructor.
     */
    protected OutputSerializer() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Creates a {@code GsonBuilder} with generic adaptors.
     *
     * @return  a {@code GsonBuilder} instance
     */
    public static GsonBuilder makeGSONBuilder() {
        GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();
        gsonBuilder.registerTypeAdapter(Series.class, new SeriesSerializer());
        gsonBuilder.registerTypeAdapter(MiniBox.class, new MiniBoxSerializer());
        gsonBuilder.registerTypeAdapter(DEFAULT_CELL_TYPE, new CellListSerializer());
        gsonBuilder.registerTypeAdapter(DEFAULT_LOCATION_TYPE, new LocationListSerializer());
        return gsonBuilder;
    }
    
    /**
     * Serializer for {@link MiniBox} objects.
     * <p>
     * The contents of the box are formatted as:
     * <pre>
     *     {
     *         "(key)" : (value),
     *         "(key)" : (value),
     *         ...
     *     }
     * </pre>
     */
    public static final class MiniBoxSerializer implements JsonSerializer<MiniBox> {
        @Override
        public JsonElement serialize(MiniBox src, Type typeOfSrc,
                                     JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            
            for (String key : src.getKeys()) {
                String value = src.get(key);
                
                if (value.matches(INTEGER_REGEX)) {
                    json.addProperty(key, src.getInt(key));
                } else if (value.matches(DOUBLE_REGEX)) {
                    json.addProperty(key, src.getDouble(key));
                } else {
                    json.addProperty(key, value);
                }
            }
            
            return json;
        }
    }
    
    /**
     * Serializer for {@link Series} objects.
     * <p>
     * The series object is formatted as:
     * <pre>
     *     {
     *         "conversions": {
     *             "DS": (ds),
     *             "DT": (dt)
     *         },
     *         "ticks": (ticks),
     *         "size": {
     *             "length": (length),
     *             "width": (width),
     *             "height": (height),
     *             "margin": (margin)
     *         },
     *         "populations": {
     *             "(population_code)": {
     *                 "(key)" : (value),
     *                 "(key)" : (value),
     *                 ...
     *             },
     *             ...
     *         }
     *     }
     * </pre>
     */
    public static final class SeriesSerializer implements JsonSerializer<Series> {
        @Override
        public JsonElement serialize(Series src, Type typeOfSrc,
                                     JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            
            JsonObject conversions = new JsonObject();
            conversions.addProperty("DS", src.ds);
            conversions.addProperty("DT", src.dt);
            json.add("conversions", conversions);
            
            json.addProperty("ticks", src.getTicks());
            
            JsonObject size = new JsonObject();
            size.addProperty("length", src.length);
            size.addProperty("width", src.width);
            size.addProperty("height", src.height);
            size.addProperty("margin", src.margin);
            json.add("size", size);
            
            // Add population parameters.
            JsonObject populations = new JsonObject();
            List<String> keys = new ArrayList<>(src.populations.keySet());
            Collections.sort(keys);
            for (String pop : keys) {
                JsonElement population = context.serialize(src.populations.get(pop));
                populations.add(pop, population);
            }
            json.add("populations", populations);
            
            return json;
        }
    }
    
    /**
     * Serializer for a list of {@link CellContainer} objects.
     * <p>
     * The series object is formatted as:
     * <pre>
     *     [
     *         (serialized cell container),
     *         (serialized cell container),
     *         ...
     *     ]
     * </pre>
     */
    public static final class CellListSerializer
            implements JsonSerializer<ArrayList<CellContainer>> {
        @Override
        public JsonElement serialize(ArrayList<CellContainer> src, Type typeOfSrc,
                                     JsonSerializationContext context) {
            JsonArray json = new JsonArray();
            
            for (CellContainer cellContainer : src) {
                JsonElement cell = context.serialize(cellContainer, CellContainer.class);
                json.add(cell);
            }
            
            return json;
        }
    }
    
    /**
     * Serializer for a list of {@link LocationContainer} objects.
     * <p>
     * The series object is formatted as:
     * <pre>
     *     [
     *         (serialized location container),
     *         (serialized location container),
     *         ...
     *     ]
     * </pre>
     */
    public static final class LocationListSerializer
            implements JsonSerializer<ArrayList<LocationContainer>> {
        @Override
        public JsonElement serialize(ArrayList<LocationContainer> src, Type typeOfSrc,
                                     JsonSerializationContext context) {
            JsonArray json = new JsonArray();
            
            for (LocationContainer locationContainer : src) {
                JsonElement location = context.serialize(locationContainer,
                        LocationContainer.class);
                json.add(location);
            }
            
            return json;
        }
    }
}
