package arcade.core.env.loc;

import org.junit.*;
import static org.junit.Assert.*;
import static arcade.core.TestUtilities.*;

public class LocationContainerTest {
	@Test
	public void constructor_setsFields() {
		int id = randomIntBetween(1, 10);
		
		LocationContainer locationContainer = new LocationContainer(id);
		assertEquals(id, locationContainer.id);
	}
}
