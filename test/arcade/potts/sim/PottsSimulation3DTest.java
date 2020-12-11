package arcade.potts.sim;

import java.util.HashMap;
import org.junit.Test;
import arcade.core.agent.cell.*;
import arcade.core.env.loc.*;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCellFactory;
import arcade.potts.env.loc.PottsLocationFactory3D;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.potts.sim.PottsSimulationTest.RANDOM_SEED;

public class PottsSimulation3DTest {
    @Test
    public void makePotts_mockSeries_initializesPotts() {
        PottsSeries series = mock(PottsSeries.class);
        series.potts = mock(MiniBox.class);
        series.populations = mock(HashMap.class);
        PottsSimulation3D sim = new PottsSimulation3D(RANDOM_SEED, series);
        Potts potts = sim.makePotts();
        assertTrue(potts instanceof Potts3D);
    }
    
    @Test
    public void makeLocationFactory_createsFactory() {
        PottsSeries series = mock(PottsSeries.class);
        series.populations = mock(HashMap.class);
        PottsSimulation3D sim = new PottsSimulation3D(RANDOM_SEED, series);
        LocationFactory factory = sim.makeLocationFactory();
        assertTrue(factory instanceof PottsLocationFactory3D);
    }
    
    @Test
    public void makeCellFactory_createsFactory() {
        PottsSeries series = mock(PottsSeries.class);
        series.populations = mock(HashMap.class);
        PottsSimulation3D sim = new PottsSimulation3D(RANDOM_SEED, series);
        CellFactory factory = sim.makeCellFactory();
        assertTrue(factory instanceof PottsCellFactory);
    }
}
