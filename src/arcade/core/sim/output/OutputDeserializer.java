package arcade.core.sim.output;

import com.google.gson.*;

public final class OutputDeserializer {
	static Gson makeGSON() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		return gsonBuilder.create();
	}
}
