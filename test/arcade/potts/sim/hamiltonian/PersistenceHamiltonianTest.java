package arcade.potts.sim.hamiltonian;

import java.lang.reflect.Field;
import java.util.HashMap;
import org.junit.Test;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.env.loc.PottsLocation;
import arcade.potts.sim.PottsSeries;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.core.sim.Series.TARGET_SEPARATOR;
import static arcade.core.util.Enums.Region;

public class PersistenceHamiltonianTest {
    private static final double EPSILON = 1E-10;
    
    @Test
    public void constructor_called_initializesMaps() {
        PersistenceHamiltonian ph = new PersistenceHamiltonian(mock(PottsSeries.class));
        assertNotNull(ph.configs);
        assertNotNull(ph.popToLambda);
        assertNotNull(ph.popToDecay);
    }
    
    @Test
    public void constructor_called_initializesParameters() {
        PottsSeries series = mock(PottsSeries.class);
        
        series.potts = new MiniBox();
        series.populations = new HashMap<>();
        
        String key1 = randomString();
        int code1 = randomIntBetween(1, 10);
        MiniBox population1 = new MiniBox();
        population1.put("CODE", code1);
        series.populations.put(key1, population1);
        
        String key2 = randomString();
        int code2 = code1 + randomIntBetween(1, 10);
        MiniBox population2 = new MiniBox();
        population2.put("CODE", code2);
        series.populations.put(key2, population2);
        
        double lambda1 = randomDoubleBetween(1, 100);
        double lambda2 = randomDoubleBetween(1, 100);
        
        series.potts.put("persistence/LAMBDA" + TARGET_SEPARATOR + key1, lambda1);
        series.potts.put("persistence/LAMBDA" + TARGET_SEPARATOR + key2, lambda2);
        
        double decay1 = randomDoubleBetween(1, 100);
        double decay2 = randomDoubleBetween(1, 100);
        
        series.potts.put("persistence/DECAY" + TARGET_SEPARATOR + key1, decay1);
        series.potts.put("persistence/DECAY" + TARGET_SEPARATOR + key2, decay2);
        
        double volumeThreshold = randomDoubleBetween(1, 10);
        
        series.potts.put("persistence/VOLUME_THRESHOLD", volumeThreshold);
        
        PersistenceHamiltonian ph = new PersistenceHamiltonian(series);
        
        assertEquals(2, ph.popToLambda.size());
        assertTrue(ph.popToLambda.containsKey(code1));
        assertTrue(ph.popToLambda.containsKey(code2));
        assertEquals(lambda1, ph.popToLambda.get(code1), EPSILON);
        assertEquals(lambda2, ph.popToLambda.get(code2), EPSILON);
        assertTrue(ph.popToDecay.containsKey(code1));
        assertTrue(ph.popToDecay.containsKey(code2));
        assertEquals(decay1, ph.popToDecay.get(code1), EPSILON);
        assertEquals(decay2, ph.popToDecay.get(code2), EPSILON);
        assertEquals(volumeThreshold, ph.threshold, EPSILON);
    }
    
    @Test
    public void register_noRegions_addsConfig() {
        PersistenceHamiltonian ph = new PersistenceHamiltonian(mock(PottsSeries.class));
        PottsCell cell = mock(PottsCell.class);
        PottsLocation location = mock(PottsLocation.class);
        
        int id = randomIntBetween(1, 10);
        int pop = randomIntBetween(1, 10);
        
        doReturn(id).when(cell).getID();
        doReturn(pop).when(cell).getPop();
        doReturn(location).when(cell).getLocation();
        
        double lambda = randomDoubleBetween(1, 100);
        ph.popToLambda.put(pop, lambda);
        
        double decay = randomDoubleBetween(1, 100);
        ph.popToDecay.put(pop, decay);
        
        double threshold = randomDoubleBetween(1, 100);
        ph.threshold = threshold;
        
        ph.register(cell);
        PersistenceHamiltonianConfig config = ph.configs.get(id);
        
        assertNotNull(config);
        assertEquals(location, config.location);
        assertEquals(lambda, config.getLambda(), EPSILON);
        assertEquals(decay, config.getDecay(), EPSILON);
        assertEquals(threshold, config.threshold, EPSILON);
    }
    
    @Test
    public void deregister_exists_removesConfig() {
        PersistenceHamiltonian ph = new PersistenceHamiltonian(mock(PottsSeries.class));
        PottsCell cell = mock(PottsCell.class);
        
        int id = randomIntBetween(1, 10);
        doReturn(id).when(cell).getID();
        
        PersistenceHamiltonianConfig config = mock(PersistenceHamiltonianConfig.class);
        ph.configs.put(id, config);
        
        ph.deregister(cell);
        
        assertFalse(ph.configs.containsKey(id));
    }
    
    @Test
    public void getDelta_validIDs_calculatesValue() {
        PersistenceHamiltonian ph = spy(new PersistenceHamiltonian(mock(PottsSeries.class)));
        
        double persistence1minus = randomDoubleBetween(10, 100);
        doReturn(persistence1minus).when(ph).getPersistence(1, 0, 0, 0, -1);
        
        double persistence1plus = randomDoubleBetween(10, 100);
        doReturn(persistence1plus).when(ph).getPersistence(1, 0, 0, 0, 1);
        
        double persistence2minus = randomDoubleBetween(10, 100);
        doReturn(persistence2minus).when(ph).getPersistence(2, 0, 0, 0, -1);
        
        double persistence2plus = randomDoubleBetween(10, 100);
        doReturn(persistence2plus).when(ph).getPersistence(2, 0, 0, 0, 1);
        
        assertEquals(persistence1minus + persistence2plus, ph.getDelta(1, 2, 0, 0, 0), EPSILON);
        assertEquals(persistence2minus + persistence1plus, ph.getDelta(2, 1, 0, 0, 0), EPSILON);
    }
    
    @Test
    public void getDelta_validRegions_returnsZero() {
        PersistenceHamiltonian ph = spy(new PersistenceHamiltonian(mock(PottsSeries.class)));
        
        int id = randomIntBetween(1, 100);
        
        double delta1 = ph.getDelta(id, Region.DEFAULT.ordinal(), Region.NUCLEUS.ordinal(), 0, 0, 0);
        assertEquals(0, delta1, EPSILON);
        
        double delta2 = ph.getDelta(id, Region.NUCLEUS.ordinal(), Region.DEFAULT.ordinal(), 0, 0, 0);
        assertEquals(0, delta2, EPSILON);
    }

    @Test
    public void getPersistence_validID_returnsValue() {
        PersistenceHamiltonian ph = spy(new PersistenceHamiltonian(mock(PottsSeries.class)));
        
        PottsLocation location = mock(PottsLocation.class);
        int surface = randomIntBetween(1, 100);
        doReturn(surface).when(location).getSurface();
        
        PersistenceHamiltonianConfig config = mock(PersistenceHamiltonianConfig.class);
        
        try {
            Field locationField = PersistenceHamiltonianConfig.class.getDeclaredField("location");
            locationField.setAccessible(true);
            locationField.set(config, location);
        } catch (Exception ignored) { }
        
        double[] vector1 = new double[] {
                randomDoubleBetween(1, 10),
                randomDoubleBetween(1, 10),
                randomDoubleBetween(1, 10),
        };
        
        double[] vector2 = new double[] {
                randomDoubleBetween(1, 10),
                randomDoubleBetween(1, 10),
                randomDoubleBetween(1, 10),
        };
        
        double[] vector3 = new double[] {
                randomDoubleBetween(1, 10),
                randomDoubleBetween(1, 10),
                randomDoubleBetween(1, 10),
        };
        
        doReturn(vector1).when(config).getVector();
        doReturn(vector2).when(config).getDisplacement();
        
        double lambda = randomDoubleBetween(1, 100);
        doReturn(lambda).when(config).getLambda();
        
        int x = randomIntBetween(1, 10);
        int y = randomIntBetween(1, 10);
        int z = randomIntBetween(1, 10);
        int change = randomIntBetween(1, 10);
        
        doAnswer(invocation -> {
            doReturn(vector3).when(config).getDisplacement();
            return null;
        }).when(config).updateDisplacement(x, y, z, change);
        
        ph.configs.put(1, config);
        
        double dot = vector1[0] * vector3[0] + vector1[1] * vector3[1] + vector1[2] * vector3[2];
        double expected = -lambda * dot * surface;
        
        assertEquals(expected, ph.getPersistence(1, x, y, z, change), EPSILON);
    }
    
    @Test
    public void getPersistence_invalidID_returnsZero() {
        PersistenceHamiltonian ph = spy(new PersistenceHamiltonian(mock(PottsSeries.class)));
        assertEquals(0, ph.getPersistence(0, 1, 1, 1, 1), EPSILON);
        assertEquals(0, ph.getPersistence(-1, 1, 1, 1, 1), EPSILON);
        assertEquals(0, ph.getPersistence(0, 1, 1, 1, -1), EPSILON);
        assertEquals(0, ph.getPersistence(-1, 1, 1, 1, -1), EPSILON);
    }
}
