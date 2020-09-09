package arcade.sim;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.sim.PottsSimulationTest.*;

public class PottsSimulation3DTest {
	@Test
	public void setupPotts_anySeries_initializesPotts() {
		Series series = mock(Series.class);
		PottsSimulation3D sim = new PottsSimulation3D(RANDOM_SEED, series);
		sim.setupPotts();
		assertTrue(sim.potts instanceof Potts3D);
	}
}
