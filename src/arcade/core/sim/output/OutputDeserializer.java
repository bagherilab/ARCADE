package arcade.core.sim.output;

import com.google.gson.*;
import java.lang.reflect.Type;
import arcade.core.agent.cell.CellContainer;
import arcade.core.agent.cell.CellFactoryContainer;
import arcade.core.env.loc.LocationContainer;
import arcade.core.env.loc.LocationFactoryContainer;

public final class OutputDeserializer {
	public static GsonBuilder makeGSONBuilder() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(CellFactoryContainer.class, new CellFactoryDeserializer());
		gsonBuilder.registerTypeAdapter(CellContainer.class, new CellDeserializer());
		gsonBuilder.registerTypeAdapter(LocationFactoryContainer.class, new LocationFactoryDeserializer());
		gsonBuilder.registerTypeAdapter(LocationContainer.class, new LocationDeserializer());
		return gsonBuilder;
	}
	
	public static class CellFactoryDeserializer implements JsonDeserializer<CellFactoryContainer> {
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
	
	public static class CellDeserializer implements JsonDeserializer<CellContainer> {
		public CellContainer deserialize(JsonElement json, Type typeOfT,
												JsonDeserializationContext context) throws JsonParseException {
			JsonObject array = json.getAsJsonObject();
			int id = array.get("id").getAsInt();
			int pop = array.get("pop").getAsInt();
			int age = array.get("age").getAsInt();
			CellContainer container = new CellContainer(id, pop, age);
			return container;
		}
	}
	
	public static class LocationFactoryDeserializer implements JsonDeserializer<LocationFactoryContainer> {
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
	
	public static class LocationDeserializer implements JsonDeserializer<LocationContainer> {
		public LocationContainer deserialize(JsonElement json, Type typeOfT,
										 JsonDeserializationContext context) throws JsonParseException {
			JsonObject array = json.getAsJsonObject();
			int id = array.get("id").getAsInt();
			LocationContainer container = new LocationContainer(id);
			return container;
		}
	}
}
