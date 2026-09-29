package arcade.patch.sim.output;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import arcade.core.agent.cell.CellContainer;
import arcade.core.env.location.Location;
import arcade.core.env.location.LocationContainer;
import arcade.core.sim.Series;
import arcade.core.sim.output.OutputSerializer;
import arcade.patch.agent.cell.PatchCellContainer;
import arcade.patch.env.component.PatchComponentSitesGraph;
import arcade.patch.env.component.PatchComponentSitesGraph.SiteEdge;
import arcade.patch.env.component.PatchComponentSitesGraph.SiteNode;
import arcade.patch.env.location.Coordinate;
import arcade.patch.env.location.CoordinateUVWZ;
import arcade.patch.env.location.CoordinateXYZ;
import arcade.patch.env.location.PatchLocation;
import arcade.patch.env.location.PatchLocationContainer;
import arcade.patch.sim.PatchSeries;
import arcade.patch.util.PatchEnums.State;
import static arcade.core.sim.Simulation.DEFAULT_LOCATION_TYPE;
import static arcade.patch.sim.PatchSimulation.PATCH_LAYER_TYPE;

/**
 * Container class for patch-specific object serializers.
 *
 * <p>Generic serializers include:
 *
 * <ul>
 *   <li>{@link PatchSeriesSerializer} for serializing {@link PatchSeries}
 *   <li>{@link PatchCellSerializer} for serializing {@link PatchCellContainer}
 *   <li>{@link LocationListSerializer} for serializing {@link PatchLocationContainer} lists
 *   <li>{@link CoordinateXYZSerializer} for serializing (x, y, z) {@link Coordinate}
 *   <li>{@link CoordinateUVWZSerializer} for serializing (u, v, w, z) {@link Coordinate}
 * </ul>
 */
public final class PatchOutputSerializer {
    /** Hidden utility class constructor. */
    protected PatchOutputSerializer() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a {@code Gson} with generic and implementation-specific adaptors.
     *
     * @return a {@code Gson} instance
     */
    static Gson makeGSON() {
        GsonBuilder gsonBuilder = OutputSerializer.makeGSONBuilder();
        gsonBuilder.registerTypeAdapter(PatchSeries.class, new PatchSeriesSerializer());
        gsonBuilder.registerTypeAdapter(CellContainer.class, new CellSerializer());
        gsonBuilder.registerTypeAdapter(PatchCellContainer.class, new PatchCellSerializer());
        gsonBuilder.registerTypeAdapter(DEFAULT_LOCATION_TYPE, new LocationListSerializer());
        gsonBuilder.registerTypeAdapter(CoordinateXYZ.class, new CoordinateXYZSerializer());
        gsonBuilder.registerTypeAdapter(CoordinateUVWZ.class, new CoordinateUVWZSerializer());
        gsonBuilder.registerTypeAdapter(PatchComponentSitesGraph.class, new SitesGraphSerializer());
        gsonBuilder.registerTypeAdapter(SiteEdge.class, new SiteEdgeSerializer());
        gsonBuilder.registerTypeAdapter(SiteNode.class, new SiteNodeSerializer());
        gsonBuilder.registerTypeAdapter(PATCH_LAYER_TYPE, new LayersSerializer());
        return gsonBuilder.create();
    }

    /**
     * Serializer for {@link PatchSeries} objects.
     *
     * <p>The object is first serialized using the generic {@link Series} and patch-specific
     * contents are then appended:
     *
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
        public JsonElement serialize(
                PatchSeries src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = (JsonObject) context.serialize(src, Series.class);

            // Add additional sizing parameters.
            JsonObject sizes = json.get("size").getAsJsonObject();
            sizes.addProperty("radius", src.radius);
            sizes.addProperty("depth", src.depth);

            // Add patch parameters.
            JsonElement patch = context.serialize(src.patch);
            json.add("patch", patch);

            return json;
        }
    }

    /**
     * Serializer for {@link CellContainer} objects.
     *
     * <p>Uses serialization for {@link PatchCellContainer}.
     */
    static class CellSerializer implements JsonSerializer<CellContainer> {
        @Override
        public JsonElement serialize(
                CellContainer src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src, PatchCellContainer.class);
        }
    }

    /**
     * Serializer for {@link PatchCellContainer} objects.
     *
     * <p>The container object is formatted as:
     *
     * <pre>
     *     {
     *         "id": (id),
     *         "parent": (parent),
     *         "pop": (pop),
     *         "age": (age),
     *         "divisions": (divisions),
     *         "state": (state),
     *         "volume": (volume),
     *         "height": (height),
     *         "criticals": [(critical volume), (critical height)],
     *     }
     * </pre>
     */
    static class PatchCellSerializer implements JsonSerializer<PatchCellContainer> {
        @Override
        public JsonElement serialize(
                PatchCellContainer src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();

            json.addProperty("id", src.id);
            json.addProperty("parent", src.parent);
            json.addProperty("pop", src.pop);
            json.addProperty("age", src.age);
            json.addProperty("divisions", src.divisions);
            json.addProperty("state", ((State) src.state).name());
            json.addProperty("volume", src.volume);
            json.addProperty("height", src.height);

            JsonArray criticals = new JsonArray();
            criticals.add((int) (100 * src.criticalVolume) / 100.0);
            criticals.add((int) (100 * src.criticalHeight) / 100.0);
            json.add("criticals", criticals);

            // TODO: add cycles

            return json;
        }
    }

    /**
     * Serializer for {@link PatchComponentSitesGraph} objects.
     *
     * <p>The object is formatted as:
     *
     * <pre>
     *     [
     *         {
     *             "from": (from),
     *             "to": (to),
     *             "type": (type),
     *             "radius": (radius),
     *             "length": (length),
     *             "wall": (wall),
     *             "shear": (shear),
     *             "stress": (stress),
     *             "flow": (flow),
     *         },
     *         {
     *             "from": (from),
     *             "to": (to),
     *             "type": (type),
     *             "radius": (radius),
     *             "length": (length),
     *             "wall": (wall),
     *             "shear": (shear),
     *             "stress": (stress),
     *             "flow": (flow),
     *         },
     *         ...
     *     ]
     * </pre>
     */
    static class SitesGraphSerializer implements JsonSerializer<PatchComponentSitesGraph> {
        @Override
        public JsonElement serialize(
                PatchComponentSitesGraph src, Type typeOfSrc, JsonSerializationContext context) {
            JsonArray json = new JsonArray();

            for (Object obj : src.getGraph().getAllEdges()) {
                SiteEdge e = (SiteEdge) obj;
                JsonElement edge = context.serialize(e, SiteEdge.class);
                json.add(edge);
            }

            return json;
        }
    }

    /**
     * Serializer for {@link SiteEdge} objects.
     *
     * <p>The object is formatted as:
     *
     * <pre>
     *     {
     *         "from": (from),
     *         "to": (to),
     *         "type": (type),
     *         "radius": (radius),
     *         "length": (length),
     *         "wall": (wall),
     *         "shear": (shear),
     *         "stress": (stress),
     *         "flow": (flow),
     *     }
     * </pre>
     */
    static class SiteEdgeSerializer implements JsonSerializer<SiteEdge> {
        @Override
        public JsonElement serialize(
                SiteEdge src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();

            json.add("from", context.serialize(src.getFrom()));
            json.add("to", context.serialize(src.getTo()));
            json.addProperty("type", src.getType());
            json.addProperty("radius", src.getRadius());
            json.addProperty("length", src.getLength());
            json.addProperty("wall", src.getWall());
            json.addProperty("shear", src.getShear());
            json.addProperty("stress", src.getCircum());
            json.addProperty("flow", src.getFlow());
            json.addProperty("fraction", src.getFraction());
            json.addProperty("transport", src.getTransport());

            return json;
        }
    }

    /**
     * Serializer for {@link SiteNode} objects.
     *
     * <p>The object is formatted as:
     *
     * <pre>
     *     {
     *         "x": (x),
     *         "y": (y),
     *         "z": (z),
     *         "pressure": (pressure),
     *         "oxygen": (oxygen),
     *     }
     * </pre>
     */
    static class SiteNodeSerializer implements JsonSerializer<SiteNode> {
        @Override
        public JsonElement serialize(
                SiteNode src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();

            json.addProperty("x", src.getX());
            json.addProperty("y", src.getY());
            json.addProperty("z", src.getZ());
            json.addProperty("pressure", src.getPressure());
            json.addProperty("oxygen", src.getOxygen());

            return json;
        }
    }

    /**
     * Serializer for list of {@link PatchLocationContainer} objects.
     *
     * <p>This serializer overrides the {@code LocationListSerializer} defined in {@link
     * OutputSerializer}. The container object is formatted as:
     *
     * <pre>
     *     [
     *         {
     *             "coordinate": (coordinate of location),
     *             "ids": (list of ids of cells in the location),
     *         },
     *         {
     *             "coordinate": (coordinate of location),
     *             "ids": (list of ids of cells in the location),
     *         },
     *         ...
     *     ]
     * </pre>
     */
    public static final class LocationListSerializer
            implements JsonSerializer<ArrayList<LocationContainer>> {
        @Override
        public JsonElement serialize(
                ArrayList<LocationContainer> src,
                Type typeOfSrc,
                JsonSerializationContext context) {
            JsonArray json = new JsonArray();
            HashMap<Coordinate, ArrayList<Integer>> containerMap = new HashMap<>();

            for (LocationContainer locationContainer : src) {
                PatchLocationContainer container = (PatchLocationContainer) locationContainer;
                ArrayList<Integer> ids =
                        containerMap.computeIfAbsent(container.coordinate, k -> new ArrayList<>());
                ids.add(container.id);
            }

            for (Coordinate coordinate : containerMap.keySet()) {
                JsonObject location = new JsonObject();
                location.add("coordinate", context.serialize(coordinate));
                location.add("ids", context.serialize(containerMap.get(coordinate)));
                json.add(location);
            }

            return json;
        }
    }

    /**
     * Serializer for collection of molecule layer values.
     *
     * <p>The object is formatted as:
     *
     * <pre>
     *     [
     *         {
     *             "location": (coordinate of location),
     *             "layers": {
     *                 "(LATTICE NAME)" : (average value at location),
     *                 "(LATTICE NAME)" : (average value at location),
     *                 ...
     *             }
     *         },
     *         ...
     *     ]
     * </pre>
     */
    static class LayersSerializer
            implements JsonSerializer<HashMap<Location, HashMap<String, Double>>> {
        @Override
        public JsonElement serialize(
                HashMap<Location, HashMap<String, Double>> src,
                Type typeOfSrc,
                JsonSerializationContext context) {
            JsonArray json = new JsonArray();
            for (Location loc : src.keySet()) {
                JsonObject location = new JsonObject();
                JsonObject layers = new JsonObject();
                for (String key : src.get(loc).keySet()) {
                    layers.addProperty(key, src.get(loc).get(key));
                }
                location.add("location", context.serialize(((PatchLocation) loc).getCoordinate()));
                location.add("layers", layers);
                json.add(location);
            }
            return json;
        }
    }

    /**
     * Serializer for {@link CoordinateXYZ} objects.
     *
     * <p>The coordinate object is formatted as:
     *
     * <pre>
     *     [(x), (y), (z)]
     * </pre>
     */
    static class CoordinateXYZSerializer implements JsonSerializer<CoordinateXYZ> {
        @Override
        public JsonElement serialize(
                CoordinateXYZ src, Type typeOfSrc, JsonSerializationContext context) {
            JsonArray json = new JsonArray();
            json.add(src.x);
            json.add(src.y);
            json.add(src.z);
            return json;
        }
    }

    /**
     * Serializer for {@link CoordinateUVWZ} objects.
     *
     * <p>The coordinate object is formatted as:
     *
     * <pre>
     *     [(u), (v), (w), (z)]
     * </pre>
     */
    static class CoordinateUVWZSerializer implements JsonSerializer<CoordinateUVWZ> {
        @Override
        public JsonElement serialize(
                CoordinateUVWZ src, Type typeOfSrc, JsonSerializationContext context) {
            JsonArray json = new JsonArray();
            json.add(src.u);
            json.add(src.v);
            json.add(src.w);
            json.add(src.z);
            return json;
        }
    }
}
