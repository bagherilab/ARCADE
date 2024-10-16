package arcade.core.sim.output;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import org.junit.jupiter.api.Test;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.internal.bind.TreeTypeAdapter;
import com.google.gson.reflect.TypeToken;
import arcade.core.ARCADE;
import arcade.core.agent.cell.CellContainer;
import arcade.core.env.location.LocationContainer;
import arcade.core.sim.Series;
import arcade.core.util.MiniBox;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.core.sim.output.OutputSerializer.*;

public class OutputSerializerTest {
    static final JsonSerializationContext CELL_CONTEXT = new JsonSerializationContext() {
        @Override
        public JsonElement serialize(Object src) { return null; }
        
        @Override
        public JsonElement serialize(Object src, Type typeOfSrc) {
            JsonObject object = new JsonObject();
            object.addProperty("id", ((CellContainer) src).getID());
            return object;
        }
    };
    
    static final JsonSerializationContext LOCATION_CONTEXT = new JsonSerializationContext() {
        @Override
        public JsonElement serialize(Object src) { return null; }
        
        @Override
        public JsonElement serialize(Object src, Type typeOfSrc) {
            JsonObject object = new JsonObject();
            object.addProperty("id", ((LocationContainer) src).getID());
            return object;
        }
    };
    
    public static void checkAdaptors(Gson gson) {
        TypeToken<Series> series = new TypeToken<Series>() { };
        assertSame(gson.getAdapter(series).getClass(), TreeTypeAdapter.class);
        
        TypeToken<MiniBox> minibox = new TypeToken<MiniBox>() { };
        assertSame(gson.getAdapter(minibox).getClass(), TreeTypeAdapter.class);
        
        TypeToken<ArrayList<CellContainer>> cellContainerList =
                new TypeToken<ArrayList<CellContainer>>() { };
        assertSame(gson.getAdapter(cellContainerList).getClass(), TreeTypeAdapter.class);
        
        TypeToken<ArrayList<LocationContainer>> locationContainerList =
                new TypeToken<ArrayList<LocationContainer>>() { };
        assertSame(gson.getAdapter(locationContainerList).getClass(), TreeTypeAdapter.class);
    }
    
    @Test
    public void constructor_called_throwsException() {
        assertThrows(UnsupportedOperationException.class, OutputSerializer::new);
    }
    
    @Test
    public void makeGSON_registersAdaptors() {
        GsonBuilder gsonBuilder = OutputSerializer.makeGSONBuilder();
        Gson gson = gsonBuilder.create();
        checkAdaptors(gson);
    }
    
    @Test
    public void serialize_forMiniBox_createsJSON() {
        MiniBoxSerializer serializer = new MiniBoxSerializer();
        MiniBox box = new MiniBox();
        
        String key1 = randomString();
        String value1 = randomString();
        
        String key2 = randomString();
        double value2 = randomDoubleBetween(0, 100);
        
        String key3 = randomString();
        int value3 = randomIntBetween(0, 100);
        
        box.put(key1, value1);
        box.put(key2, value2);
        box.put(key3, value3);
        
        String expected = "{"
                + "\"" + key1 + "\":\"" + value1 + "\","
                + "\"" + key2 + "\":" + value2 + ","
                + "\"" + key3 + "\":" + value3 + ""
                + "}";
        
        JsonElement json = serializer.serialize(box, null, null);
        assertEquals(expected, json.toString());
    }
    
    @Test
    public void serialize_forSeries_createsJSON() {
        SeriesSerializer serializer = new SeriesSerializer();
        Series series = mock(Series.class);
        
        double ds = randomDoubleBetween(0, 100);
        double dt = randomDoubleBetween(0, 100);
        
        int ticks = randomIntBetween(0, 100);
        doReturn(ticks).when(series).getTicks();
        
        int length = randomIntBetween(0, 100);
        int width = randomIntBetween(0, 100);
        int height = randomIntBetween(0, 100);
        int margin = randomIntBetween(0, 100);
        
        try {
            Field lengthField = Series.class.getDeclaredField("length");
            lengthField.setAccessible(true);
            lengthField.setInt(series, length);
            
            Field widthField = Series.class.getDeclaredField("width");
            widthField.setAccessible(true);
            widthField.setInt(series, width);
            
            Field heightField = Series.class.getDeclaredField("height");
            heightField.setAccessible(true);
            heightField.setInt(series, height);
            
            Field marginField = Series.class.getDeclaredField("margin");
            marginField.setAccessible(true);
            marginField.setInt(series, margin);
            
            Field dsField = Series.class.getDeclaredField("ds");
            dsField.setAccessible(true);
            dsField.setDouble(series, ds);
            
            Field dtField = Series.class.getDeclaredField("dt");
            dtField.setAccessible(true);
            dtField.setDouble(series, dt);
        } catch (Exception ignored) { }
        
        series.populations = new HashMap<>();
        String popA = "a" + randomString();
        String popB = "b" + randomString();
        
        MiniBox a = new MiniBox();
        String keyA = randomString();
        String valueA = randomString();
        a.put(keyA, valueA);
        
        MiniBox b = new MiniBox();
        String keyB = randomString();
        String valueB = randomString();
        b.put(keyB, valueB);
        
        series.populations.put(popA, a);
        series.populations.put(popB, b);
        
        JsonSerializationContext context = new JsonSerializationContext() {
            @Override
            public JsonElement serialize(Object src) {
                if (src instanceof MiniBox) {
                    MiniBox box = (MiniBox) src;
                    JsonObject json = new JsonObject();
                    json.addProperty(box.getKeys().get(0), box.get(box.getKeys().get(0)));
                    return json;
                }
                return null;
            }
            
            @Override
            public JsonElement serialize(Object src, Type typeOfSrc) { return null; }
        };
        
        String expected = "{"
                + "\"version\":\"" + ARCADE.VERSION + "\","
                + "\"conversions\":{"
                + "\"DS\":" + ds + ","
                + "\"DT\":" + dt
                + "},"
                + "\"ticks\":" + ticks + ","
                + "\"size\":{"
                + "\"length\":" + length + ","
                + "\"width\":" + width + ","
                + "\"height\":" + height + ","
                + "\"margin\":" + margin
                + "},"
                + "\"populations\":{"
                + "\"" + popA + "\":{\"" + keyA + "\":\"" + valueA + "\"},"
                + "\"" + popB + "\":{\"" + keyB + "\":\"" + valueB + "\"}"
                + "}"
                + "}";
        
        JsonElement json = serializer.serialize(series, null, context);
        assertEquals(expected, json.toString());
    }
    
    @Test
    public void serialize_forCellContainerList_createsJSON() {
        CellListSerializer serializer = new CellListSerializer();
        ArrayList<CellContainer> cells = new ArrayList<>();
        
        int n = randomIntBetween(1, 10);
        int id0 = randomIntBetween(1, 10);
        
        for (int i = 0; i < n; i++) {
            int id = id0 + i;
            CellContainer cell = mock(CellContainer.class);
            doReturn(id).when(cell).getID();
            cells.add(cell);
        }
        
        StringBuilder expected = new StringBuilder("[");
        for (int i = 0; i < n; i++) {
            int id = id0 + i;
            expected.append("{\"id\":").append(id).append("}");
            if (i < n - 1) {
                expected.append(",");
            }
        }
        expected.append("]");
        
        JsonElement json = serializer.serialize(cells, CellContainer.class, CELL_CONTEXT);
        assertEquals(expected.toString(), json.toString());
    }
    
    @Test
    public void serialize_forLocationContainerList_createsJSON() {
        LocationListSerializer serializer = new LocationListSerializer();
        ArrayList<LocationContainer> locations = new ArrayList<>();
        
        int n = randomIntBetween(1, 10);
        int id0 = randomIntBetween(1, 10);
        
        for (int i = 0; i < n; i++) {
            int id = id0 + i;
            LocationContainer location = mock(LocationContainer.class);
            doReturn(id).when(location).getID();
            locations.add(location);
        }
        
        StringBuilder expected = new StringBuilder("[");
        for (int i = 0; i < n; i++) {
            int id = id0 + i;
            expected.append("{\"id\":").append(id).append("}");
            if (i < n - 1) {
                expected.append(",");
            }
        }
        expected.append("]");
        
        JsonElement json = serializer.serialize(locations, LocationContainer.class, LOCATION_CONTEXT);
        assertEquals(expected.toString(), json.toString());
    }
}
