package arcade.sim.output;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import arcade.env.grid.Grid;
import arcade.sim.Potts;
import arcade.sim.Simulation;
import static arcade.MainTest.*;

public class OutputSaverTest {
	@Test
	public void constructor_setsFields() {
		Potts potts = mock(Potts.class);
		Grid grid = mock(Grid.class);
		Simulation sim = mock(Simulation.class);
		
		doReturn(potts).when(sim).getPotts();
		doReturn(grid).when(sim).getAgents();
		
		OutputSaver saver = new OutputSaver("", sim);
		
		assertSame(sim, saver.sim);
		assertSame(potts, saver.potts);
		assertSame(grid, saver.agents);
	}
	
	@Test
	public void constructor_setsPrefix() {
		Simulation sim = mock(Simulation.class);
		
		String prefix = randomString();
		int seed = randomInt();
		doReturn(seed).when(sim).getSeed();
		
		OutputSaver saver = new OutputSaver(prefix, sim);
		assertEquals(prefix + "_" + String.format("%04d", seed), saver.prefix);
	}
	
	@Test
	public void constructor_initializesObjects() {
		Simulation sim = mock(Simulation.class);
		OutputSaver saver = new OutputSaver("", sim);
		assertNotNull(saver.gson);
	}
}