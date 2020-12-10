package arcade.potts.sim;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.HashMap;
import arcade.core.agent.cell.*;
import arcade.core.env.loc.*;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCellFactory;
import arcade.potts.env.loc.PottsLocationFactory2D;
import static arcade.potts.sim.PottsSimulationTest.RANDOM_SEED;

public class PottsSimulation2DTest {
    @Test
    public void makePotts_mockSeries_initializesPotts() {
        PottsSeries series = mock(PottsSeries.class);
        series.potts = mock(MiniBox.class);
        series.populations = mock(HashMap.class);
        PottsSimulation2D sim = new PottsSimulation2D(RANDOM_SEED, series);
        Potts potts = sim.makePotts();
        assertTrue(potts instanceof Potts2D);
    }
    
    @Test
    public void makeLocationFactory_createsFactory() {
        PottsSeries series = mock(PottsSeries.class);
        series.populations = mock(HashMap.class);
        PottsSimulation2D sim = new PottsSimulation2D(RANDOM_SEED, series);
        LocationFactory factory = sim.makeLocationFactory();
        assertTrue(factory instanceof PottsLocationFactory2D);
    }
    
    @Test
    public void makeCellFactory_createsFactory() {
        PottsSeries series = mock(PottsSeries.class);
        series.populations = mock(HashMap.class);
        PottsSimulation2D sim = new PottsSimulation2D(RANDOM_SEED, series);
        CellFactory factory = sim.makeCellFactory();
        assertTrue(factory instanceof PottsCellFactory);
    }
}
