package arcade.potts.sim.hamiltonian;

import java.lang.reflect.Field;
import java.util.EnumMap;
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
import static arcade.core.util.MiniBox.TAG_SEPARATOR;

public class PersistenceHamiltonianTest {
    private static final double EPSILON = 1E-10;
    
    @Test
    public void constructor_called_initializesMaps() {
        PersistenceHamiltonian ph = new PersistenceHamiltonian(mock(PottsSeries.class));
        assertNotNull(ph.configs);
        assertNotNull(ph.popToLambda);
        assertNotNull(ph.popToDecay);
        assertNotNull(ph.popToLambdasRegion);
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
        population2.put("(REGION)" + TAG_SEPARATOR + Region.DEFAULT.name(), 0);
        population2.put("(REGION)" + TAG_SEPARATOR + Region.NUCLEUS.name(), 0);
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
        
        double lambdaNucleus = randomDoubleBetween(1, 100);
        double lambdaDefault = randomDoubleBetween(1, 100);
        series.potts.put("persistence/LAMBDA_" + Region.DEFAULT.name() + TARGET_SEPARATOR + key2, lambdaDefault);
        series.potts.put("persistence/LAMBDA_" + Region.NUCLEUS.name() + TARGET_SEPARATOR + key2, lambdaNucleus);
        
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
        assertEquals(lambdaDefault, ph.popToLambdasRegion.get(code2).get(Region.DEFAULT), EPSILON);
        assertEquals(lambdaNucleus, ph.popToLambdasRegion.get(code2).get(Region.NUCLEUS), EPSILON);
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
    public void register_withRegions_addsConfig() {
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
        
        double lambdaNucleus = randomDoubleBetween(1, 100);
        EnumMap<Region, Double> lambdasRegion = new EnumMap<>(Region.class);
        lambdasRegion.put(Region.NUCLEUS, lambdaNucleus);
        ph.popToLambdasRegion.put(pop, lambdasRegion);
        
        double decay = randomDoubleBetween(1, 100);
        ph.popToDecay.put(pop, decay);
        
        double threshold = randomDoubleBetween(1, 100);
        ph.threshold = threshold;
        
        ph.register(cell);
        PersistenceHamiltonianConfig config = ph.configs.get(id);
        
        assertNotNull(config);
        assertEquals(location, config.location);
        assertEquals(lambda, config.getLambda(), EPSILON);
        assertEquals(lambdaNucleus, config.getLambda(Region.NUCLEUS), EPSILON);
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
        int id1 = randomIntBetween(1, 100);
        int id2 = id1 + randomIntBetween(1, 10);
        
        double persistence1minus = randomDoubleBetween(10, 100);
        doReturn(persistence1minus).when(ph).getPersistence(id1, 0, 0, 0, -1);
        
        double persistence1plus = randomDoubleBetween(10, 100);
        doReturn(persistence1plus).when(ph).getPersistence(id1, 0, 0, 0, 1);
        
        double persistence2minus = randomDoubleBetween(10, 100);
        doReturn(persistence2minus).when(ph).getPersistence(id2, 0, 0, 0, -1);
        
        double persistence2plus = randomDoubleBetween(10, 100);
        doReturn(persistence2plus).when(ph).getPersistence(id2, 0, 0, 0, 1);
        
        assertEquals(persistence1minus + persistence2plus, ph.getDelta(id1, id2, 0, 0, 0), EPSILON);
        assertEquals(persistence2minus + persistence1plus, ph.getDelta(id2, id1, 0, 0, 0), EPSILON);
    }
    
    @Test
    public void getDelta_validRegions_returnsZero() {
        PersistenceHamiltonian ph = spy(new PersistenceHamiltonian(mock(PottsSeries.class)));
        int id = randomIntBetween(1, 100);
        int region1 = Region.DEFAULT.ordinal();
        int region2 = Region.NUCLEUS.ordinal();
        
        double persistence1minus = randomDoubleBetween(10, 100);
        doReturn(persistence1minus).when(ph).getPersistence(id, region1, 0, 0, 0, -1);
        
        double persistence1plus = randomDoubleBetween(10, 100);
        doReturn(persistence1plus).when(ph).getPersistence(id, region1, 0, 0, 0, 1);
        
        double persistence2minus = randomDoubleBetween(10, 100);
        doReturn(persistence2minus).when(ph).getPersistence(id, region2, 0, 0, 0, -1);
        
        double persistence2plus = randomDoubleBetween(10, 100);
        doReturn(persistence2plus).when(ph).getPersistence(id, region2, 0, 0, 0, 1);
        
        assertEquals(persistence1minus + persistence2plus, ph.getDelta(id, region1, region2, 0, 0, 0), EPSILON);
        assertEquals(persistence2minus + persistence1plus, ph.getDelta(id, region2, region1, 0, 0, 0), EPSILON);
    }

    @Test
    public void getPersistence_validID_returnsValue() {
        PersistenceHamiltonian ph = spy(new PersistenceHamiltonian(mock(PottsSeries.class)));
        int id = randomIntBetween(1, 100);
        
        PottsLocation location = mock(PottsLocation.class);
        int surface = randomIntBetween(1, 100);
        doReturn((double) surface).when(location).getSurface();
        
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
        
        int x = randomIntBetween(1, 10);
        int y = randomIntBetween(1, 10);
        int z = randomIntBetween(1, 10);
        int change = randomIntBetween(1, 10);
        
        doReturn(vector1).when(config).getVector();
        doReturn(vector2).when(config).getDisplacement(x, y, z, change);
        
        double lambda = randomDoubleBetween(1, 100);
        doReturn(lambda).when(config).getLambda();
        
        ph.configs.put(id, config);
        
        double dot = vector1[0] * vector2[0] + vector1[1] * vector2[1] + vector1[2] * vector2[2];
        double expected = -lambda * dot * surface;
        
        assertEquals(expected, ph.getPersistence(id, x, y, z, change), EPSILON);
    }
    
    @Test
    public void getPersistence_validRegions_returnsValue() {
        PersistenceHamiltonian ph = spy(new PersistenceHamiltonian(mock(PottsSeries.class)));
        int id = randomIntBetween(1, 100);
        
        PottsLocation location = mock(PottsLocation.class);
        Region region = Region.NUCLEUS;
        int surface = randomIntBetween(1, 100);
        doReturn((double) surface).when(location).getSurface(region);
        
        PersistenceHamiltonianConfig config = mock(PersistenceHamiltonianConfig.class);
    
        double[] vector = new double[] {
                randomDoubleBetween(1, 10),
                randomDoubleBetween(1, 10),
                randomDoubleBetween(1, 10),
        };
    
        double[] displacement = new double[] {
                randomDoubleBetween(1, 10),
                randomDoubleBetween(1, 10),
                randomDoubleBetween(1, 10),
        };
        
        try {
            Field locationField = PersistenceHamiltonianConfig.class.getDeclaredField("location");
            locationField.setAccessible(true);
            locationField.set(config, location);
    
            Field vectorField = PersistenceHamiltonianConfig.class.getDeclaredField("vector");
            vectorField.setAccessible(true);
            vectorField.set(config, vector);
    
            Field displacementField = PersistenceHamiltonianConfig.class.getDeclaredField("displacement");
            displacementField.setAccessible(true);
            displacementField.set(config, displacement);
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
        
        int x = randomIntBetween(1, 10);
        int y = randomIntBetween(1, 10);
        int z = randomIntBetween(1, 10);
        int change = randomIntBetween(1, 10);
        
        doReturn(vector1).when(config).getVector();
        doReturn(vector2).when(config).getDisplacement(x, y, z, change);
        doReturn(vector3).when(config).getDisplacement(x, y, z, change, region);
        
        double lambda = randomDoubleBetween(1, 100);
        doReturn(lambda).when(config).getLambda(region);
        
        ph.configs.put(id, config);
        
        double dot = vector3[0] * vector[0] + vector3[1] * vector[1] + vector3[2] * vector[2];
        double expected = -lambda * dot * surface;
        
        assertEquals(expected, ph.getPersistence(id, region.ordinal(), x, y, z, change), EPSILON);
        assertArrayEquals(vector, config.vector, EPSILON);
        assertArrayEquals(displacement, config.displacement, EPSILON);
    }
    
    @Test
    public void getPersistence_defaultRegion_returnsZero() {
        PersistenceHamiltonian ph = spy(new PersistenceHamiltonian(mock(PottsSeries.class)));
        int id = randomIntBetween(1, 100);
        assertEquals(0, ph.getPersistence(0, Region.DEFAULT.ordinal(), 1, 1, 1, 1), EPSILON);
        assertEquals(0, ph.getPersistence(0, Region.DEFAULT.ordinal(), 1, 1, 1, -1), EPSILON);
        assertEquals(0, ph.getPersistence(id, Region.DEFAULT.ordinal(), 1, 1, 1, 1), EPSILON);
        assertEquals(0, ph.getPersistence(id, Region.DEFAULT.ordinal(), 1, 1, 1, -1), EPSILON);
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
