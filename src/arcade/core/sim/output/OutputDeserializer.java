package arcade.core.sim.output;

import com.google.gson.*;
import java.lang.reflect.Type;
import arcade.core.agent.cell.CellFactory;
import arcade.core.env.loc.LocationFactory;

public final class OutputDeserializer {
	public static GsonBuilder makeGSONBuilder() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(CellFactory.CellFactoryContainer.class, new CellFactoryDeserializer());
		gsonBuilder.registerTypeAdapter(LocationFactory.LocationFactoryContainer.class, new LocationFactoryDeserializer());
		return gsonBuilder;
	}
	
	static class CellFactoryDeserializer implements JsonDeserializer<CellFactory.CellFactoryContainer> {
		public CellFactory.CellFactoryContainer deserialize(JsonElement json, Type typeOfT,
															JsonDeserializationContext context) throws JsonParseException {
			JsonArray jsonArray = json.getAsJsonArray();
			CellFactory.CellFactoryContainer container = new CellFactory.CellFactoryContainer();
			
			for (JsonElement element : jsonArray) {
				JsonObject cell = element.getAsJsonObject();
				CellFactory.CellContainer cellContainer = context.deserialize(cell, CellFactory.CellContainer.class);
				container.cells.add(cellContainer);
			}
			
			return container;
		}
	}
	
	static class LocationFactoryDeserializer implements JsonDeserializer<LocationFactory.LocationFactoryContainer> {
		public LocationFactory.LocationFactoryContainer deserialize(JsonElement json, Type typeOfT,
																	JsonDeserializationContext context) throws JsonParseException {
			JsonArray jsonArray = json.getAsJsonArray();
			LocationFactory.LocationFactoryContainer container = new LocationFactory.LocationFactoryContainer();
			
			for (JsonElement element : jsonArray) {
				JsonObject locationObject = element.getAsJsonObject();
				LocationFactory.LocationContainer location = context.deserialize(locationObject, LocationFactory.LocationContainer.class);
				container.locations.add(location);
			}
			
			return container;
		}
	}
}
