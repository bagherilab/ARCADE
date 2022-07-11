package arcade.potts.sim.output;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.EnumMap;
import org.junit.Test;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.internal.bind.TreeTypeAdapter;
import com.google.gson.reflect.TypeToken;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellContainer;
import arcade.core.env.loc.LocationContainer;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCellContainer;
import arcade.potts.env.loc.PottsLocationContainer;
import arcade.potts.env.loc.Voxel;
import arcade.potts.sim.PottsSeries;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.core.util.Enums.Region;
import static arcade.core.util.Enums.State;
import static arcade.potts.sim.output.PottsOutputSerializer.*;
import static arcade.potts.util.PottsEnums.Phase;

public class PottsOutputSerializerTest {
    private static final MersenneTwisterFast RANDOM = new MersenneTwisterFast(randomIntBetween(1, 1000));
    
    static final JsonSerializationContext LOCATION_CONTEXT = new JsonSerializationContext() {
        @Override
        public JsonElement serialize(Object src) {
            Voxel voxel = (Voxel) src;
            JsonArray json = new JsonArray();
            json.add(voxel.x + "|" + voxel.y + "|" + voxel.z);
            return json;
        }
        
        @Override
        public JsonElement serialize(Object src, Type typeOfSrc) { return null; }
    };
    
    public static void checkAdaptors(Gson gson) {
        TypeToken<PottsSeries> series = new TypeToken<PottsSeries>() { };
        assertSame(gson.getAdapter(series).getClass(), TreeTypeAdapter.class);
        
        TypeToken<CellContainer> cellContainer = new TypeToken<CellContainer>() { };
        assertSame(gson.getAdapter(cellContainer).getClass(), TreeTypeAdapter.class);
        
        TypeToken<PottsCellContainer> cell = new TypeToken<PottsCellContainer>() { };
        assertSame(gson.getAdapter(cell).getClass(), TreeTypeAdapter.class);
        
        TypeToken<LocationContainer> locationContainer = new TypeToken<LocationContainer>() { };
        assertSame(gson.getAdapter(locationContainer).getClass(), TreeTypeAdapter.class);
        
        TypeToken<PottsLocationContainer> location = new TypeToken<PottsLocationContainer>() { };
        assertSame(gson.getAdapter(location).getClass(), TreeTypeAdapter.class);
        
        TypeToken<Voxel> voxel = new TypeToken<Voxel>() { };
        assertSame(gson.getAdapter(voxel).getClass(), TreeTypeAdapter.class);
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void constructor_called_throwsException() {
        PottsOutputSerializer serializer = new PottsOutputSerializer();
    }
    
    @Test
    public void makeGSON_registersAdaptors() {
        Gson gson = PottsOutputSerializer.makeGSON();
        checkAdaptors(gson);
    }
    
    @Test
    public void serialize_forSeries_createsJSON() {
        PottsSeriesSerializer serializer = new PottsSeriesSerializer();
        PottsSeries series = mock(PottsSeries.class);
        
        series.potts = new MiniBox();
        
        String key1 = randomString();
        String value1 = randomString();
        series.potts.put(key1, value1);
        
        String key2 = randomString();
        int value2 = randomIntBetween(0, 10);
        series.potts.put(key2, value2);
        
        JsonSerializationContext context = new JsonSerializationContext() {
            @Override
            public JsonElement serialize(Object src) {
                if (src instanceof MiniBox) {
                    MiniBox box = (MiniBox) src;
                    JsonObject json = new JsonObject();
                    json.addProperty(box.getKeys().get(0), box.get(box.getKeys().get(0)));
                    json.addProperty(box.getKeys().get(1), box.getInt(box.getKeys().get(1)));
                    return json;
                }
                return null;
            }
            
            @Override
            public JsonElement serialize(Object src, Type typeOfSrc) {
                JsonObject json = new JsonObject();
                json.addProperty("SERIES", "SERIES");
                return json;
            }
        };
        
        String expected = "{"
                + "\"SERIES\":\"SERIES\","
                + "\"potts\":{"
                + "\"" + key1 + "\":\"" + value1 + "\","
                + "\"" + key2 + "\":" + value2
                + "}"
                + "}";
        
        JsonElement json = serializer.serialize(series, null, context);
        assertEquals(expected, json.toString());
    }
    
    @Test
    public void serialize_forCellContainer_passesCall() {
        CellContainer cellContainer = mock(CellContainer.class);
        JsonSerializationContext context = mock(JsonSerializationContext.class);
        JsonElement expected = mock(JsonElement.class);
        doReturn(expected).when(context).serialize(cellContainer, PottsCellContainer.class);
        
        CellSerializer serializer = new CellSerializer();
        JsonElement jsonElement = serializer.serialize(cellContainer, null, context);
        
        assertSame(expected, jsonElement);
    }
    
    @Test
    public void serialize_forCellNoRegion_createsJSON() {
        PottsCellSerializer serializer = new PottsCellSerializer();
        
        int id = randomIntBetween(1, 100);
        int parent = randomIntBetween(1, 100);
        int pop = randomIntBetween(1, 100);
        int age = randomIntBetween(1, 100);
        int divisions = randomIntBetween(1, 100);
        State state = State.random(RANDOM);
        Phase phase = Phase.random(RANDOM);
        int voxels = randomIntBetween(1, 100);
        int criticalVolume = randomIntBetween(1, 100);
        int criticalHeight = randomIntBetween(1, 100);
        
        PottsCellContainer cellContainer = new PottsCellContainer(id, parent, pop, age, divisions,
                state, phase, voxels, null, criticalVolume, criticalHeight, null, null);
        
        String expected = "{"
                + "\"id\":" + id + ","
                + "\"parent\":" + parent + ","
                + "\"pop\":" + pop + ","
                + "\"age\":" + age + ","
                + "\"divisions\":" + divisions + ","
                + "\"state\":\"" + state.name() + "\","
                + "\"phase\":\"" + phase.name() + "\","
                + "\"voxels\":" + voxels + ","
                + "\"criticals\":[" + criticalVolume + ".0," + criticalHeight + ".0]"
                + "}";
        
        JsonElement json = serializer.serialize(cellContainer, null, null);
        assertEquals(expected, json.toString());
    }
    
    @Test
    public void serialize_forCellWithRegion_createsJSON() {
        PottsCellSerializer serializer = new PottsCellSerializer();
        
        int id = randomIntBetween(1, 100);
        int parent = randomIntBetween(1, 100);
        int pop = randomIntBetween(1, 100);
        int age = randomIntBetween(1, 100);
        int divisions = randomIntBetween(1, 100);
        State state = State.random(RANDOM);
        Phase phase = Phase.random(RANDOM);
        int voxels = randomIntBetween(1, 100);
        int criticalVolume = randomIntBetween(1, 100);
        int criticalHeight = randomIntBetween(1, 100);
        
        Region region1 = Region.DEFAULT;
        Region region2 = Region.NUCLEUS;
        int regionVoxels1 = randomIntBetween(1, 100);
        int regionVoxels2 = randomIntBetween(1, 100);
        int criticalRegionVolume1 = randomIntBetween(1, 100);
        int criticalRegionHeight1 = randomIntBetween(1, 100);
        int criticalRegionVolume2 = randomIntBetween(1, 100);
        int criticalRegionHeight2 = randomIntBetween(1, 100);
        
        EnumMap<Region, Integer> regionVoxels = new EnumMap<>(Region.class);
        regionVoxels.put(region1, regionVoxels1);
        regionVoxels.put(region2, regionVoxels2);
        
        EnumMap<Region, Double> criticalRegionVolumes = new EnumMap<>(Region.class);
        criticalRegionVolumes.put(region1, (double) criticalRegionVolume1);
        criticalRegionVolumes.put(region2, (double) criticalRegionVolume2);
        
        EnumMap<Region, Double> criticalRegionHeights = new EnumMap<>(Region.class);
        criticalRegionHeights.put(region1, (double) criticalRegionHeight1);
        criticalRegionHeights.put(region2, (double) criticalRegionHeight2);
        
        PottsCellContainer cellContainer = new PottsCellContainer(id, parent, pop, age, divisions,
                state, phase, voxels, regionVoxels, criticalVolume, criticalHeight,
                criticalRegionVolumes, criticalRegionHeights);
        
        String expected = "{"
                + "\"id\":" + id + ","
                + "\"parent\":" + parent + ","
                + "\"pop\":" + pop + ","
                + "\"age\":" + age + ","
                + "\"divisions\":" + divisions + ","
                + "\"state\":\"" + state.name() + "\","
                + "\"phase\":\"" + phase.name() + "\","
                + "\"voxels\":" + voxels + ","
                + "\"criticals\":[" + criticalVolume + ".0," + criticalHeight + ".0],"
                + "\"regions\":["
                + "{\"region\":\"DEFAULT\","
                + "\"voxels\":" + regionVoxels1 + ","
                + "\"criticals\":[" + criticalRegionVolume1 + ".0," + criticalRegionHeight1 + ".0]"
                + "},"
                + "{\"region\":\"NUCLEUS\","
                + "\"voxels\":" + regionVoxels2 + ","
                + "\"criticals\":[" + criticalRegionVolume2 + ".0," + criticalRegionHeight2 + ".0]"
                + "}]"
                + "}";
        
        JsonElement json = serializer.serialize(cellContainer, null, null);
        assertEquals(expected, json.toString());
    }
    
    @Test
    public void serialize_forLocationContainer_passesCall() {
        LocationContainer locationContainer = mock(LocationContainer.class);
        JsonSerializationContext context = mock(JsonSerializationContext.class);
        JsonElement expected = mock(JsonElement.class);
        doReturn(expected).when(context).serialize(locationContainer, PottsLocationContainer.class);
    
        LocationSerializer serializer = new LocationSerializer();
        JsonElement jsonElement = serializer.serialize(locationContainer, null, context);
        
        assertSame(expected, jsonElement);
    }
    
    @Test
    public void serialize_forLocationNoRegion_createJSON() {
        PottsLocationSerializer serializer = new PottsLocationSerializer();
        
        int id = randomIntBetween(1, 100);
        Voxel center = new Voxel(randomIntBetween(1, 100), randomIntBetween(1, 100), randomIntBetween(1, 100));
        
        int x1 = randomIntBetween(1, 100);
        int y1 = randomIntBetween(1, 100);
        int z1 = randomIntBetween(1, 100);
        
        int x2 = x1 + randomIntBetween(1, 100);
        int y2 = y1 + randomIntBetween(1, 100);
        int z2 = z1 + randomIntBetween(1, 100);
        
        ArrayList<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(x1, y1, z1));
        voxels.add(new Voxel(x2, y2, z2));
        
        PottsLocationContainer locationContainer = new PottsLocationContainer(id, center, voxels);
        
        String expected = "{"
                + "\"id\":" + id + ","
                + "\"center\":[\"" + center.x + "|" + center.y + "|" + center.z + "\"],"
                + "\"location\":["
                + "{\"region\":\"UNDEFINED\",\"voxels\":["
                + "[\"" + x1 + "|" + y1 + "|" + z1 + "\"],"
                + "[\"" + x2 + "|" + y2 + "|" + z2 + "\"]"
                + "]}"
                + "]}";
        
        JsonElement json = serializer.serialize(locationContainer, null, LOCATION_CONTEXT);
        assertEquals(expected, json.toString());
    }
    
    @Test
    public void serialize_forLocationWithRegion_createJSON() {
        PottsLocationSerializer serializer = new PottsLocationSerializer();
        
        int id = randomIntBetween(1, 100);
        Voxel center = new Voxel(randomIntBetween(1, 100), randomIntBetween(1, 100), randomIntBetween(1, 100));
        
        int x1 = randomIntBetween(1, 100);
        int y1 = randomIntBetween(1, 100);
        int z1 = randomIntBetween(1, 100);
        
        int x2 = x1 + randomIntBetween(1, 100);
        int y2 = y1 + randomIntBetween(1, 100);
        int z2 = z1 + randomIntBetween(1, 100);
        
        int x3 = x2 + randomIntBetween(1, 100);
        int y3 = y2 + randomIntBetween(1, 100);
        int z3 = z2 + randomIntBetween(1, 100);
        
        int x4 = x3 + randomIntBetween(1, 100);
        int y4 = y3 + randomIntBetween(1, 100);
        int z4 = z3 + randomIntBetween(1, 100);
        
        ArrayList<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(x1, y1, z1));
        voxels.add(new Voxel(x2, y2, z2));
        voxels.add(new Voxel(x3, y3, z3));
        voxels.add(new Voxel(x4, y4, z4));
        
        ArrayList<Voxel> region1 = new ArrayList<>();
        region1.add(new Voxel(x1, y1, z1));
        region1.add(new Voxel(x2, y2, z2));
        
        ArrayList<Voxel> region2 = new ArrayList<>();
        region2.add(new Voxel(x3, y3, z3));
        region2.add(new Voxel(x4, y4, z4));
        
        EnumMap<Region, ArrayList<Voxel>> regions = new EnumMap<>(Region.class);
        regions.put(Region.DEFAULT, region1);
        regions.put(Region.NUCLEUS, region2);
        
        PottsLocationContainer locationContainer = new PottsLocationContainer(id, center, voxels, regions);
        
        String expected = "{"
                + "\"id\":" + id + ","
                + "\"center\":[\"" + center.x + "|" + center.y + "|" + center.z + "\"],"
                + "\"location\":["
                + "{\"region\":\"DEFAULT\",\"voxels\":["
                + "[\"" + x1 + "|" + y1 + "|" + z1 + "\"],"
                + "[\"" + x2 + "|" + y2 + "|" + z2 + "\"]"
                + "]},"
                + "{\"region\":\"NUCLEUS\",\"voxels\":["
                + "[\"" + x3 + "|" + y3 + "|" + z3 + "\"],"
                + "[\"" + x4 + "|" + y4 + "|" + z4 + "\"]"
                + "]}"
                + "]}";
        
        JsonElement json = serializer.serialize(locationContainer, null, LOCATION_CONTEXT);
        assertEquals(expected, json.toString());
    }
    
    @Test
    public void serialize_forVoxel_createsJSON() {
        VoxelSerializer serializer = new VoxelSerializer();
        
        int x = randomIntBetween(1, 100);
        int y = randomIntBetween(1, 100);
        int z = randomIntBetween(1, 100);
        Voxel voxel = new Voxel(x, y, z);
        
        String expected = "[" + x + "," + y + "," + z + "]";
        
        JsonElement json = serializer.serialize(voxel, null, null);
        assertEquals(expected, json.toString());
    }
}
