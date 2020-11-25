package arcade.core.env.loc;

import java.util.ArrayList;

/**
 * Container class for loading into a {@link LocationFactory}.
 */
public class LocationFactoryContainer {
	/** List of loaded location containers */
	public final ArrayList<LocationContainer> locations;
	
	/** Creates an empty {@link LocationFactory} container. */
	public LocationFactoryContainer() { locations = new ArrayList<>(); }
}