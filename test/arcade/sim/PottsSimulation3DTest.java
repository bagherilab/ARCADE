package arcade.sim;

import org.junit.*;
import java.util.HashMap;
import arcade.agent.cell.*;
import arcade.env.loc.*;
import arcade.util.MiniBox;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.sim.PottsSimulationTest.*;

public class PottsSimulation3DTest {
	@Test
	public void makePotts_mockSeries_initializesPotts() {
		Series series = mock(Series.class);
		series._potts = mock(MiniBox.class);
		series._populations = mock(HashMap.class);
		PottsSimulation3D sim = new PottsSimulation3D(RANDOM_SEED, series);
		Potts potts = sim.makePotts();
		assertTrue(potts instanceof Potts3D);
	}
	
	@Test
	public void makeLocationFactory_createsFactory() {
		Series series = mock(Series.class);
		series._populations = mock(HashMap.class);
		PottsSimulation3D sim = new PottsSimulation3D(RANDOM_SEED, series);
		LocationFactory factory = sim.makeLocationFactory();
		assertTrue(factory instanceof LocationFactory3D);
	}
	
	@Test
	public void makeCellFactory_createsFactory() {
		Series series = mock(Series.class);
		series._populations = mock(HashMap.class);
		PottsSimulation3D sim = new PottsSimulation3D(RANDOM_SEED, series);
		CellFactory factory = sim.makeCellFactory();
		assertTrue(factory instanceof CellFactory3D);
	}
}
