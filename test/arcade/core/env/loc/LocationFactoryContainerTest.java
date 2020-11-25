package arcade.core.env.loc;

import org.junit.*;
import static org.junit.Assert.*;

public class LocationFactoryContainerTest {
	@Test
	public void constructor_setsFields() {
		LocationFactoryContainer locationFactoryContainer = new LocationFactoryContainer();
		assertNotNull(locationFactoryContainer.locations);
	}
}
