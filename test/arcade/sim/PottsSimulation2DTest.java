package arcade.sim;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.sim.PottsSimulationTest.*;

public class PottsSimulation2DTest {
	@Test
	public void setupPotts_anySeries_initializesPotts() {
		Series series = mock(Series.class);
		PottsSimulation2D sim = new PottsSimulation2D(RANDOM_SEED, series);
		sim.setupPotts();
		assertTrue(sim.potts instanceof Potts2D);
	}
}
