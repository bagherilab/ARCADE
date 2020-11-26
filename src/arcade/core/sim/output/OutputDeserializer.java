package arcade.core.sim.output;

import java.lang.reflect.Type;
import java.util.ArrayList;
import com.google.gson.*;
import arcade.core.agent.cell.CellContainer;
import arcade.core.env.loc.LocationContainer;
import static arcade.core.sim.Simulation.*;

public final class OutputDeserializer {
	public static GsonBuilder makeGSONBuilder() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(DEFAULT_CELL_TYPE, new CellListDeserializer());
		gsonBuilder.registerTypeAdapter(DEFAULT_LOCATION_TYPE, new LocationListDeserializer());
		return gsonBuilder;
	}
	
	public static class CellListDeserializer implements JsonDeserializer<ArrayList<CellContainer>> {
		public ArrayList<CellContainer> deserialize(JsonElement json, Type typeOfT,
												JsonDeserializationContext context) throws JsonParseException {
			ArrayList<CellContainer> cells = new ArrayList<>();
			JsonArray jsonArray = json.getAsJsonArray();
			
			for (JsonElement element : jsonArray) {
				JsonObject cell = element.getAsJsonObject();
				CellContainer cellContainer = context.deserialize(cell, CellContainer.class);
				cells.add(cellContainer);
			}
			
			return cells;
		}
	}
	
	public static class LocationListDeserializer implements JsonDeserializer<ArrayList<LocationContainer>> {
		public ArrayList<LocationContainer> deserialize(JsonElement json, Type typeOfT,
													JsonDeserializationContext context) throws JsonParseException {
			ArrayList<LocationContainer> locations = new ArrayList<>();
			JsonArray jsonArray = json.getAsJsonArray();
			
			for (JsonElement element : jsonArray) {
				JsonObject location = element.getAsJsonObject();
				LocationContainer locationContainer = context.deserialize(location, LocationContainer.class);
				locations.add(locationContainer);
			}
			
			return locations;
		}
	}
}
