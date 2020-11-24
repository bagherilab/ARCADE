package arcade.core.sim.output;

import com.google.gson.*;
import java.lang.reflect.Type;
import static arcade.core.agent.cell.CellFactory.*;
import static arcade.core.env.loc.LocationFactory.*;

public final class OutputDeserializer {
	public static GsonBuilder makeGSONBuilder() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(CellFactoryContainer.class, new CellFactoryDeserializer());
		gsonBuilder.registerTypeAdapter(LocationFactoryContainer.class, new LocationFactoryDeserializer());
		return gsonBuilder;
	}
	
	static class CellFactoryDeserializer implements JsonDeserializer<CellFactoryContainer> {
		public CellFactoryContainer deserialize(JsonElement json, Type typeOfT,
												JsonDeserializationContext context) throws JsonParseException {
			JsonArray jsonArray = json.getAsJsonArray();
			CellFactoryContainer container = new CellFactoryContainer();
			
			for (JsonElement element : jsonArray) {
				JsonObject cell = element.getAsJsonObject();
				CellContainer cellContainer = context.deserialize(cell, CellContainer.class);
				container.cells.add(cellContainer);
			}
			
			return container;
		}
	}
	
	static class LocationFactoryDeserializer implements JsonDeserializer<LocationFactoryContainer> {
		public LocationFactoryContainer deserialize(JsonElement json, Type typeOfT,
													JsonDeserializationContext context) throws JsonParseException {
			JsonArray jsonArray = json.getAsJsonArray();
			LocationFactoryContainer container = new LocationFactoryContainer();
			
			for (JsonElement element : jsonArray) {
				JsonObject locationObject = element.getAsJsonObject();
				LocationContainer location = context.deserialize(locationObject, LocationContainer.class);
				container.locations.add(location);
			}
			
			return container;
		}
	}
}
