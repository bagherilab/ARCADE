package arcade.core.agent.cell;

import org.junit.*;
import static org.junit.Assert.*;
import static arcade.core.TestUtilities.*;

public class CellContainerTest {
	@Test
	public void constructor_setsFields() {
		int id = randomIntBetween(1, 10);
		int pop = randomIntBetween(1, 10);
		int age = randomIntBetween(1, 100);
		
		CellContainer cellContainer = new CellContainer(id, pop, age);
		assertEquals(id, cellContainer.id);
		assertEquals(pop, cellContainer.pop);
		assertEquals(age, cellContainer.age);
	}
}
