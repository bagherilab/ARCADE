package arcade.core.agent.cell;

import org.junit.*;
import static org.junit.Assert.*;

public class CellFactoryContainerTest {
	@Test
	public void constructor_setsFields() {
		CellFactoryContainer cellFactoryContainer = new CellFactoryContainer();
		assertNotNull(cellFactoryContainer.cells);
	}
}
