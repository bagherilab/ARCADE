package arcade.potts.sim.hamiltonian;

import java.lang.reflect.Field;
import java.util.EnumMap;
import java.util.HashMap;
import org.junit.Test;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSeries;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.core.util.Enums.Region;
import static arcade.core.util.MiniBox.TAG_SEPARATOR;
import static arcade.potts.sim.PottsSeries.TARGET_SEPARATOR;

public class SurfaceHamiltonianTest {
    private static final double EPSILON = 1E-10;
    
    static class SurfaceHamiltonianMock extends SurfaceHamiltonian {
        SurfaceHamiltonianMock(PottsSeries series, Potts potts) { super(series, potts); }
        
        @Override
        int[] calculateChange(int sourceID, int targetID, int x, int y, int z) {
            return new int[] { (targetID > sourceID ? 1 : -1), (sourceID > targetID ? 1 : -1) };
        }
        
        @Override
        int[] calculateChange(int id, int sourceRegion, int targetRegion, int x, int y, int z) {
            if (sourceRegion == Region.DEFAULT.ordinal()) {
                return new int[] { 2, 2 };
            } else {
                return new int[] { -3, -3 };
            }
        }
    }
    
    @Test
    public void constructor_called_initializesMaps() {
        SurfaceHamiltonianMock shm = new SurfaceHamiltonianMock(mock(PottsSeries.class), mock(Potts.class));
        assertNotNull(shm.configs);
        assertNotNull(shm.popToLambda);
        assertNotNull(shm.popToLambdasRegion);
    }
    
    @Test
    public void constructor_called_setsArrays() {
        Potts potts = mock(Potts.class);
        int[][][] ids = new int[0][0][0];
        int[][][] regions = new int[0][0][0];
        potts.ids = ids;
        potts.regions = regions;
        
        SurfaceHamiltonianMock shm = new SurfaceHamiltonianMock(mock(PottsSeries.class), potts);
        
        assertSame(ids, shm.ids);
        assertSame(regions, shm.regions);
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
        
        series.potts.put("surface/LAMBDA" + TARGET_SEPARATOR + key1, lambda1);
        series.potts.put("surface/LAMBDA" + TARGET_SEPARATOR + key2, lambda2);
        
        double lambdaNucleus = randomDoubleBetween(1, 100);
        double lambdaDefault = randomDoubleBetween(1, 100);
        series.potts.put("surface/LAMBDA_" + Region.DEFAULT.name() + TARGET_SEPARATOR + key2, lambdaDefault);
        series.potts.put("surface/LAMBDA_" + Region.NUCLEUS.name() + TARGET_SEPARATOR + key2, lambdaNucleus);
        
        SurfaceHamiltonianMock shm = new SurfaceHamiltonianMock(series, mock(Potts.class));
        
        assertEquals(2, shm.popToLambda.size());
        assertTrue(shm.popToLambda.containsKey(code1));
        assertTrue(shm.popToLambda.containsKey(code2));
        assertEquals(lambda1, shm.popToLambda.get(code1), EPSILON);
        assertEquals(lambda2, shm.popToLambda.get(code2), EPSILON);
        assertNull(shm.popToLambdasRegion.get(code1));
        assertEquals(lambdaDefault, shm.popToLambdasRegion.get(code2).get(Region.DEFAULT), EPSILON);
        assertEquals(lambdaNucleus, shm.popToLambdasRegion.get(code2).get(Region.NUCLEUS), EPSILON);
    }
    
    @Test
    public void register_noRegions_addsConfig() {
        SurfaceHamiltonianMock shm = new SurfaceHamiltonianMock(mock(PottsSeries.class), mock(Potts.class));
        PottsCell cell = mock(PottsCell.class);
        
        int id = randomIntBetween(1, 10);
        int pop = randomIntBetween(1, 10);
        
        doReturn(id).when(cell).getID();
        doReturn(pop).when(cell).getPop();
        
        double lambda = randomDoubleBetween(1, 100);
        EnumMap<Region, Double> lambdasRegion = null;
        shm.popToLambda.put(pop, lambda);
        shm.popToLambdasRegion.put(pop, lambdasRegion);
        
        shm.register(cell);
        SurfaceHamiltonianConfig config = shm.configs.get(id);
        
        assertNotNull(config);
        assertEquals(cell, config.cell);
        assertEquals(lambda, config.getLambda(), EPSILON);
        assertEquals(Double.NaN, config.getLambda(Region.UNDEFINED), EPSILON);
    }
    
    @Test
    public void register_withRegions_addsConfig() {
        SurfaceHamiltonianMock shm = new SurfaceHamiltonianMock(mock(PottsSeries.class), mock(Potts.class));
        PottsCell cell = mock(PottsCell.class);
        
        int id = randomIntBetween(1, 10);
        int pop = randomIntBetween(1, 10);
        
        doReturn(id).when(cell).getID();
        doReturn(pop).when(cell).getPop();
        
        double lambda = randomDoubleBetween(1, 100);
        double lambdaNucleus = randomDoubleBetween(1, 100);
        EnumMap<Region, Double> lambdasRegion = new EnumMap<>(Region.class);
        lambdasRegion.put(Region.NUCLEUS, lambdaNucleus);
        
        shm.popToLambda.put(pop, lambda);
        shm.popToLambdasRegion.put(pop, lambdasRegion);
        
        shm.register(cell);
        SurfaceHamiltonianConfig config = shm.configs.get(id);
        
        assertNotNull(config);
        assertEquals(cell, config.cell);
        assertEquals(lambda, config.getLambda(), EPSILON);
        assertEquals(lambdaNucleus, config.getLambda(Region.NUCLEUS), EPSILON);
    }
    
    @Test
    public void deregister_exists_removesConfig() {
        SurfaceHamiltonianMock shm = new SurfaceHamiltonianMock(mock(PottsSeries.class), mock(Potts.class));
        PottsCell cell = mock(PottsCell.class);
        
        int id = randomIntBetween(1, 10);
        doReturn(id).when(cell).getID();
        
        SurfaceHamiltonianConfig config = mock(SurfaceHamiltonianConfig.class);
        shm.configs.put(id, config);
    
        shm.deregister(cell);
        
        assertFalse(shm.configs.containsKey(id));
    }
    
    @Test
    public void getDelta_validIDs_calculatesValue() {
        SurfaceHamiltonianMock shm = spy(new SurfaceHamiltonianMock(mock(PottsSeries.class), mock(Potts.class)));
        int id1 = randomIntBetween(1, 100);
        int id2 = id1 + randomIntBetween(1, 10);
        
        double cell1 = randomDoubleBetween(1, 100);
        doReturn(cell1).when(shm).getSurface(id1, 0);
        
        double cell1plus1 = randomDoubleBetween(1, 100);
        doReturn(cell1plus1).when(shm).getSurface(id1, 1);
        
        double cell2 = randomDoubleBetween(1, 100);
        doReturn(cell2).when(shm).getSurface(id2, 0);
        
        double cell2minus1 = randomDoubleBetween(1, 100);
        doReturn(cell2minus1).when(shm).getSurface(id2, -1);
        
        assertEquals((cell1plus1 - cell1 + cell2minus1 - cell2), shm.getDelta(id1, id2, 0, 0, 0), EPSILON);
        assertEquals((cell2minus1 - cell2 + cell1plus1 - cell1), shm.getDelta(id2, id1, 0, 0, 0), EPSILON);
    }
    
    @Test
    public void getDelta_validRegions_calculatesValue() {
        SurfaceHamiltonianMock shm = spy(new SurfaceHamiltonianMock(mock(PottsSeries.class), mock(Potts.class)));
        int id = randomIntBetween(1, 100);
        
        double region = randomDoubleBetween(1, 100);
        doReturn(0.0).when(shm).getSurface(id, Region.DEFAULT.ordinal(), 0);
        doReturn(region).when(shm).getSurface(id, Region.NUCLEUS.ordinal(), 0);
        
        double regionminus3 = randomDoubleBetween(1, 100);
        doReturn(0.0).when(shm).getSurface(id, Region.DEFAULT.ordinal(), -3);
        doReturn(regionminus3).when(shm).getSurface(id, Region.NUCLEUS.ordinal(), -3);
        
        double regionplus2 = randomDoubleBetween(1, 100);
        doReturn(0.0).when(shm).getSurface(id, Region.DEFAULT.ordinal(), 2);
        doReturn(regionplus2).when(shm).getSurface(id, Region.NUCLEUS.ordinal(), 2);
        
        assertEquals((regionminus3 - region),
                shm.getDelta(id, Region.NUCLEUS.ordinal(), Region.DEFAULT.ordinal(), 0, 0, 0), EPSILON);
        assertEquals((regionplus2 - region),
                shm.getDelta(id, Region.DEFAULT.ordinal(), Region.NUCLEUS.ordinal(), 0, 0, 0), EPSILON);
    }
    
    @Test
    public void getSurface_validIDs_calculatesValue() {
        SurfaceHamiltonianMock shm = new SurfaceHamiltonianMock(mock(PottsSeries.class), mock(Potts.class));
        int id = randomIntBetween(1, 100);
        
        int surface = randomIntBetween(10, 20);
        double targetSurface = randomDoubleBetween(10, 20);
        
        PottsCell cell = mock(PottsCell.class);
        doReturn((double) surface).when(cell).getSurface();
        doReturn(targetSurface).when(cell).getTargetSurface();
        
        SurfaceHamiltonianConfig config = mock(SurfaceHamiltonianConfig.class);
        
        try {
            Field cellField = SurfaceHamiltonianConfig.class.getDeclaredField("cell");
            cellField.setAccessible(true);
            cellField.set(config, cell);
        } catch (Exception ignored) { }
        
        double lambda = randomDoubleBetween(10, 100);
        doReturn(lambda).when(config).getLambda();
        
        shm.configs.put(id, config);
        
        assertEquals(lambda * Math.pow(surface - targetSurface, 2), shm.getSurface(id, 0), EPSILON);
        assertEquals(lambda * Math.pow(surface - targetSurface + 1, 2), shm.getSurface(id, 1), EPSILON);
        assertEquals(lambda * Math.pow(surface - targetSurface - 1, 2), shm.getSurface(id, -1), EPSILON);
    }
    
    @Test
    public void getSurface_validRegions_calculatesValue() {
        SurfaceHamiltonianMock shm = new SurfaceHamiltonianMock(mock(PottsSeries.class), mock(Potts.class));
        int id = randomIntBetween(1, 100);
        Region region = Region.NUCLEUS;
        
        int surface = randomIntBetween(10, 20);
        double targetSurface = randomDoubleBetween(10, 20);
        
        PottsCell cell = mock(PottsCell.class);
        doReturn((double) surface).when(cell).getSurface(region);
        doReturn(targetSurface).when(cell).getTargetSurface(region);
        
        SurfaceHamiltonianConfig config = mock(SurfaceHamiltonianConfig.class);
        
        try {
            Field cellField = SurfaceHamiltonianConfig.class.getDeclaredField("cell");
            cellField.setAccessible(true);
            cellField.set(config, cell);
        } catch (Exception ignored) { }
        
        double lambda = randomDoubleBetween(10, 100);
        doReturn(lambda).when(config).getLambda(region);
        
        shm.configs.put(id, config);
        
        assertEquals(lambda * Math.pow(surface - targetSurface, 2),
                shm.getSurface(id, region.ordinal(), 0), EPSILON);
        assertEquals(lambda * Math.pow(surface - targetSurface + 1, 2),
                shm.getSurface(id, region.ordinal(), 1), EPSILON);
        assertEquals(lambda * Math.pow(surface - targetSurface - 1, 2),
                shm.getSurface(id, region.ordinal(), -1), EPSILON);
    }
    
    @Test
    public void getSurface_zeroID_returnsZero() {
        SurfaceHamiltonianMock shm = new SurfaceHamiltonianMock(mock(PottsSeries.class), mock(Potts.class));
        assertEquals(0, shm.getSurface(0, 1), EPSILON);
        assertEquals(0, shm.getSurface(0, 0), EPSILON);
        assertEquals(0, shm.getSurface(0, -1), EPSILON);
    }
    
    @Test
    public void getSurface_defaultRegion_returnsZero() {
        SurfaceHamiltonianMock shm = new SurfaceHamiltonianMock(mock(PottsSeries.class), mock(Potts.class));
        int id = randomIntBetween(1, 100);
        assertEquals(0, shm.getSurface(0, Region.DEFAULT.ordinal(), 1), EPSILON);
        assertEquals(0, shm.getSurface(0, Region.DEFAULT.ordinal(), 0), EPSILON);
        assertEquals(0, shm.getSurface(0, Region.DEFAULT.ordinal(), -1), EPSILON);
        assertEquals(0, shm.getSurface(id, Region.DEFAULT.ordinal(), 1), EPSILON);
        assertEquals(0, shm.getSurface(id, Region.DEFAULT.ordinal(), 0), EPSILON);
        assertEquals(0, shm.getSurface(id, Region.DEFAULT.ordinal(), -1), EPSILON);
    }
}
