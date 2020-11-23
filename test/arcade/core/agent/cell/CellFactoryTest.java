package arcade.core.agent.cell;

import org.junit.*;
import static org.junit.Assert.*;
import static arcade.core.agent.cell.Cell.State;
import static arcade.core.agent.cell.CellFactory.*;
import static arcade.core.TestUtilities.*;

public class CellFactoryTest {
	@Test
	public void CellContainer_constructor_setsFields() {
		int id = randomIntBetween(1, 10);
		int pop = randomIntBetween(1, 10);
		int age = randomIntBetween(1, 100);
		State state = randomState();
		
		CellContainer cellContainer = new CellContainer(id, pop, age, state);
		assertEquals(id, cellContainer.id);
		assertEquals(pop, cellContainer.pop);
		assertEquals(age, cellContainer.age);
		assertEquals(state, cellContainer.state);
	}
	
	@Test
	public void CellFactoryContainer_constructor_setsFields() {
		CellFactoryContainer cellFactoryContainer = new CellFactoryContainer();
		assertNotNull(cellFactoryContainer.cells);
	}
}
