package arcade.potts.sim.hamiltonian;

import java.lang.reflect.Field;
import java.util.EnumMap;
import java.util.HashMap;
import org.junit.Test;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.sim.PottsSeries;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.core.sim.Series.TARGET_SEPARATOR;
import static arcade.core.util.Enums.Region;
import static arcade.core.util.MiniBox.TAG_SEPARATOR;

public class VolumeHamiltonianTest {
    private static final double EPSILON = 1E-10;
    
    @Test
    public void constructor_called_initializesMaps() {
        VolumeHamiltonian vh = new VolumeHamiltonian(mock(PottsSeries.class));
        assertNotNull(vh.configs);
        assertNotNull(vh.popToLambda);
        assertNotNull(vh.popToLambdasRegion);
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
        
        series.potts.put("volume/LAMBDA" + TARGET_SEPARATOR + key1, lambda1);
        series.potts.put("volume/LAMBDA" + TARGET_SEPARATOR + key2, lambda2);
        
        double lambdaNucleus = randomDoubleBetween(1, 100);
        double lambdaDefault = randomDoubleBetween(1, 100);
        series.potts.put("volume/LAMBDA_" + Region.DEFAULT.name() + TARGET_SEPARATOR + key2, lambdaDefault);
        series.potts.put("volume/LAMBDA_" + Region.NUCLEUS.name() + TARGET_SEPARATOR + key2, lambdaNucleus);
        
        VolumeHamiltonian vh = new VolumeHamiltonian(series);
        
        assertEquals(2, vh.popToLambda.size());
        assertTrue(vh.popToLambda.containsKey(code1));
        assertTrue(vh.popToLambda.containsKey(code2));
        assertEquals(lambda1, vh.popToLambda.get(code1), EPSILON);
        assertEquals(lambda2, vh.popToLambda.get(code2), EPSILON);
        assertNull(vh.popToLambdasRegion.get(code1));
        assertEquals(lambdaDefault, vh.popToLambdasRegion.get(code2).get(Region.DEFAULT), EPSILON);
        assertEquals(lambdaNucleus, vh.popToLambdasRegion.get(code2).get(Region.NUCLEUS), EPSILON);
    }
    
    @Test
    public void register_noRegions_addsConfig() {
        VolumeHamiltonian vh = new VolumeHamiltonian(mock(PottsSeries.class));
        PottsCell cell = mock(PottsCell.class);
        
        int id = randomIntBetween(1, 10);
        int pop = randomIntBetween(1, 10);
        
        doReturn(id).when(cell).getID();
        doReturn(pop).when(cell).getPop();
        
        double lambda = randomDoubleBetween(1, 100);
        EnumMap<Region, Double> lambdasRegion = null;
        vh.popToLambda.put(pop, lambda);
        vh.popToLambdasRegion.put(pop, lambdasRegion);
        
        vh.register(cell);
        VolumeHamiltonianConfig config = vh.configs.get(id);
        
        assertNotNull(config);
        assertEquals(cell, config.cell);
        assertEquals(lambda, config.getLambda(), EPSILON);
        assertEquals(Double.NaN, config.getLambda(Region.UNDEFINED), EPSILON);
    }
    
    @Test
    public void register_withRegions_addsConfig() {
        VolumeHamiltonian vh = new VolumeHamiltonian(mock(PottsSeries.class));
        PottsCell cell = mock(PottsCell.class);
        
        int id = randomIntBetween(1, 10);
        int pop = randomIntBetween(1, 10);
        
        doReturn(id).when(cell).getID();
        doReturn(pop).when(cell).getPop();
        
        double lambda = randomDoubleBetween(1, 100);
        double lambdaNucleus = randomDoubleBetween(1, 100);
        EnumMap<Region, Double> lambdasRegion = new EnumMap<>(Region.class);
        lambdasRegion.put(Region.NUCLEUS, lambdaNucleus);
        
        vh.popToLambda.put(pop, lambda);
        vh.popToLambdasRegion.put(pop, lambdasRegion);
        
        vh.register(cell);
        VolumeHamiltonianConfig config = vh.configs.get(id);
        
        assertNotNull(config);
        assertEquals(cell, config.cell);
        assertEquals(lambda, config.getLambda(), EPSILON);
        assertEquals(lambdaNucleus, config.getLambda(Region.NUCLEUS), EPSILON);
    }
    
    @Test
    public void deregister_exists_removesConfig() {
        VolumeHamiltonian vh = new VolumeHamiltonian(mock(PottsSeries.class));
        PottsCell cell = mock(PottsCell.class);
        
        int id = randomIntBetween(1, 10);
        doReturn(id).when(cell).getID();
        
        VolumeHamiltonianConfig config = mock(VolumeHamiltonianConfig.class);
        vh.configs.put(id, config);
        
        vh.deregister(cell);
        
        assertFalse(vh.configs.containsKey(id));
    }
    
    @Test
    public void getDelta_validIDs_calculatesValue() {
        VolumeHamiltonian vh = spy(new VolumeHamiltonian(mock(PottsSeries.class)));
        int id1 = randomIntBetween(1, 100);
        int id2 = id1 + randomIntBetween(1, 10);
        
        double cell1 = randomDoubleBetween(1, 100);
        doReturn(cell1).when(vh).getVolume(id1, 0);
        
        double cell1plus1 = randomDoubleBetween(1, 100);
        doReturn(cell1plus1).when(vh).getVolume(id1, 1);
        
        double cell1minus1 = randomDoubleBetween(1, 100);
        doReturn(cell1minus1).when(vh).getVolume(id1, -1);
        
        double cell2 = randomDoubleBetween(1, 100);
        doReturn(cell2).when(vh).getVolume(id2, 0);
        
        double cell2plus1 = randomDoubleBetween(1, 100);
        doReturn(cell2plus1).when(vh).getVolume(id2, 1);
        
        double cell2minus1 = randomDoubleBetween(1, 100);
        doReturn(cell2minus1).when(vh).getVolume(id2, -1);
        
        assertEquals((cell1minus1 - cell1 + cell2plus1 - cell2), vh.getDelta(id1, id2, 0, 0, 0), EPSILON);
        assertEquals((cell2minus1 - cell2 + cell1plus1 - cell1), vh.getDelta(id2, id1, 0, 0, 0), EPSILON);
    }
    
    @Test
    public void getDelta_validRegions_calculatesValue() {
        VolumeHamiltonian vh = spy(new VolumeHamiltonian(mock(PottsSeries.class)));
        int id = randomIntBetween(1, 100);
        
        double region = randomDoubleBetween(1, 100);
        doReturn(0.0).when(vh).getVolume(id, Region.DEFAULT.ordinal(), 0);
        doReturn(region).when(vh).getVolume(id, Region.NUCLEUS.ordinal(), 0);
        
        double regionplus1 = randomDoubleBetween(1, 100);
        doReturn(0.0).when(vh).getVolume(id, Region.DEFAULT.ordinal(), 1);
        doReturn(regionplus1).when(vh).getVolume(id, Region.NUCLEUS.ordinal(), 1);
        
        double regionminus1 = randomDoubleBetween(1, 100);
        doReturn(0.0).when(vh).getVolume(id, Region.DEFAULT.ordinal(), -1);
        doReturn(regionminus1).when(vh).getVolume(id, Region.NUCLEUS.ordinal(), -1);
        
        assertEquals((regionplus1 - region),
                vh.getDelta(id, Region.DEFAULT.ordinal(), Region.NUCLEUS.ordinal(), 0, 0, 0), EPSILON);
        assertEquals((regionminus1 - region),
                vh.getDelta(id, Region.NUCLEUS.ordinal(), Region.DEFAULT.ordinal(), 0, 0, 0), EPSILON);
    }
    
    @Test
    public void getVolume_validID_calculatesValue() {
        VolumeHamiltonian vh = new VolumeHamiltonian(mock(PottsSeries.class));
        int id = randomIntBetween(1, 100);
        
        int volume = randomIntBetween(10, 20);
        double targetVolume = randomDoubleBetween(10, 20);
        
        PottsCell cell = mock(PottsCell.class);
        doReturn((double) volume).when(cell).getVolume();
        doReturn(targetVolume).when(cell).getTargetVolume();
        
        VolumeHamiltonianConfig config = mock(VolumeHamiltonianConfig.class);
        
        try {
            Field cellField = VolumeHamiltonianConfig.class.getDeclaredField("cell");
            cellField.setAccessible(true);
            cellField.set(config, cell);
        } catch (Exception ignored) { }
        
        double lambda = randomDoubleBetween(10, 100);
        doReturn(lambda).when(config).getLambda();
        
        vh.configs.put(id, config);
        
        assertEquals(lambda * Math.pow(volume - targetVolume, 2), vh.getVolume(id, 0), EPSILON);
        assertEquals(lambda * Math.pow(volume - targetVolume + 1, 2), vh.getVolume(id, 1), EPSILON);
        assertEquals(lambda * Math.pow(volume - targetVolume - 1, 2), vh.getVolume(id, -1), EPSILON);
    }
    
    @Test
    public void getVolume_validRegions_calculatesValue() {
        VolumeHamiltonian vh = new VolumeHamiltonian(mock(PottsSeries.class));
        int id = randomIntBetween(1, 100);
        Region region = Region.NUCLEUS;
        
        int volume = randomIntBetween(10, 20);
        double targetVolume = randomDoubleBetween(10, 20);
        
        PottsCell cell = mock(PottsCell.class);
        doReturn((double) volume).when(cell).getVolume(region);
        doReturn(targetVolume).when(cell).getTargetVolume(region);
        
        VolumeHamiltonianConfig config = mock(VolumeHamiltonianConfig.class);
        
        try {
            Field cellField = VolumeHamiltonianConfig.class.getDeclaredField("cell");
            cellField.setAccessible(true);
            cellField.set(config, cell);
        } catch (Exception ignored) { }
        
        double lambda = randomDoubleBetween(10, 100);
        doReturn(lambda).when(config).getLambda(region);
        
        vh.configs.put(id, config);
        
        assertEquals(lambda * Math.pow(volume - targetVolume, 2),
                vh.getVolume(id, region.ordinal(), 0), EPSILON);
        assertEquals(lambda * Math.pow(volume - targetVolume + 1, 2),
                vh.getVolume(id, region.ordinal(), 1), EPSILON);
        assertEquals(lambda * Math.pow(volume - targetVolume - 1, 2),
                vh.getVolume(id, region.ordinal(), -1), EPSILON);
    }
    
    @Test
    public void getVolume_zeroID_returnsZero() {
        VolumeHamiltonian vh = new VolumeHamiltonian(mock(PottsSeries.class));
        assertEquals(0, vh.getVolume(0, 1), EPSILON);
        assertEquals(0, vh.getVolume(0, 0), EPSILON);
        assertEquals(0, vh.getVolume(0, -1), EPSILON);
    }
    
    @Test
    public void getVolume_defaultRegion_returnsZero() {
        VolumeHamiltonian vh = new VolumeHamiltonian(mock(PottsSeries.class));
        int id = randomIntBetween(1, 100);
        assertEquals(0, vh.getVolume(0, Region.DEFAULT.ordinal(), 1), EPSILON);
        assertEquals(0, vh.getVolume(0, Region.DEFAULT.ordinal(), 0), EPSILON);
        assertEquals(0, vh.getVolume(0, Region.DEFAULT.ordinal(), -1), EPSILON);
        assertEquals(0, vh.getVolume(id, Region.DEFAULT.ordinal(), 1), EPSILON);
        assertEquals(0, vh.getVolume(id, Region.DEFAULT.ordinal(), 0), EPSILON);
        assertEquals(0, vh.getVolume(id, Region.DEFAULT.ordinal(), -1), EPSILON);
    }
}
