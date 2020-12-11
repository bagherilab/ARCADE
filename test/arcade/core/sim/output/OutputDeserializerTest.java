package arcade.core.sim.output;

import java.lang.reflect.Type;
import java.util.ArrayList;
import org.junit.Test;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.internal.bind.TreeTypeAdapter;
import com.google.gson.reflect.TypeToken;
import arcade.core.agent.cell.CellContainer;
import arcade.core.env.loc.LocationContainer;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.TestUtilities.*;
import static arcade.core.sim.output.OutputDeserializer.*;

public class OutputDeserializerTest {
    static final JsonDeserializationContext CELL_CONTEXT = new JsonDeserializationContext() {
        @Override
        public <T> T deserialize(JsonElement json, Type typeOfT)
                throws JsonParseException {
            JsonObject array = json.getAsJsonObject();
            int id = array.get("id").getAsInt();
            CellContainer container = mock(CellContainer.class);
            doReturn(id).when(container).getID();
            return (T) container;
        }
    };
    
    static final JsonDeserializationContext LOCATION_CONTEXT = new JsonDeserializationContext() {
        @Override
        public <T> T deserialize(JsonElement json, Type typeOfT)
                throws JsonParseException {
            JsonObject array = json.getAsJsonObject();
            int id = array.get("id").getAsInt();
            LocationContainer container = mock(LocationContainer.class);
            doReturn(id).when(container).getID();
            return (T) container;
        }
    };
    
    public static void checkAdaptors(Gson gson) {
        TypeToken<ArrayList<CellContainer>> cellContainerList =
                new TypeToken<ArrayList<CellContainer>>() { };
        assertSame(gson.getAdapter(cellContainerList).getClass(), TreeTypeAdapter.class);
        
        TypeToken<ArrayList<LocationContainer>> locationContainerList =
                new TypeToken<ArrayList<LocationContainer>>() { };
        assertSame(gson.getAdapter(locationContainerList).getClass(), TreeTypeAdapter.class);
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void constructor_called_throwsException() {
        OutputDeserializer deserializer = new OutputDeserializer();
    }
    
    @Test
    public void makeGSON_registersAdaptors() {
        GsonBuilder gsonBuilder = OutputDeserializer.makeGSONBuilder();
        Gson gson = gsonBuilder.create();
        checkAdaptors(gson);
    }
    
    @Test
    public void deserializer_forCellContainerList_createsObject() {
        CellListDeserializer deserializer = new CellListDeserializer();
        
        int n = randomIntBetween(1, 10);
        int id0 = randomIntBetween(1, 10);
        
        StringBuilder string = new StringBuilder("[");
        for (int i = 0; i < n; i++) {
            int id = id0 + i;
            string.append("{\"id\":").append(id).append("}");
            if (i < n - 1) { string.append(","); }
        }
        string.append("]");
        
        JsonArray json = JsonParser.parseString(string.toString()).getAsJsonArray();
        ArrayList<CellContainer> object = deserializer.deserialize(json, CellContainer.class, CELL_CONTEXT);
        
        assertEquals(n, object.size());
        for (int i = 0; i < n; i++) {
            int id = id0 + i;
            assertEquals(id, object.get(i).getID());
        }
    }
    
    @Test
    public void deserializer_forLocationContainerList_createsObject() {
        LocationListDeserializer deserializer = new LocationListDeserializer();
        
        int n = randomIntBetween(1, 10);
        int id0 = randomIntBetween(1, 10);
        
        StringBuilder string = new StringBuilder("[");
        for (int i = 0; i < n; i++) {
            int id = id0 + i;
            string.append("{\"id\":").append(id).append("}");
            if (i < n - 1) { string.append(","); }
        }
        string.append("]");
        
        JsonArray json = JsonParser.parseString(string.toString()).getAsJsonArray();
        ArrayList<LocationContainer> object = deserializer.deserialize(json, LocationContainer.class, LOCATION_CONTEXT);
        
        assertEquals(n, object.size());
        for (int i = 0; i < n; i++) {
            int id = id0 + i;
            assertEquals(id, object.get(i).getID());
        }
    }
}
