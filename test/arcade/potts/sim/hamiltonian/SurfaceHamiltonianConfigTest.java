package arcade.potts.sim.hamiltonian;

import java.util.EnumMap;
import org.junit.Test;
import arcade.potts.agent.cell.PottsCell;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.core.util.Enums.Region;

public class SurfaceHamiltonianConfigTest {
    private static final double EPSILON = 1E-10;
    
    @Test
    public void constructor_noRegions_setsFields() {
        PottsCell cell = mock(PottsCell.class);
        double lambda = randomDoubleBetween(1, 100);
        
        SurfaceHamiltonianConfig shc = new SurfaceHamiltonianConfig(cell, lambda, null);
        assertEquals(cell, shc.cell);
        assertFalse(shc.hasRegions);
    }
    
    @Test
    public void constructor_emptyRegions_setsFields() {
        PottsCell cell = mock(PottsCell.class);
        double lambda = randomDoubleBetween(1, 100);
        EnumMap<Region, Double> lambdasRegion = new EnumMap<>(Region.class);
        
        SurfaceHamiltonianConfig shc = new SurfaceHamiltonianConfig(cell, lambda, lambdasRegion);
        assertEquals(cell, shc.cell);
        assertFalse(shc.hasRegions);
    }
    
    @Test
    public void constructor_hasRegions_setsFields() {
        PottsCell cell = mock(PottsCell.class);
        double lambda = randomDoubleBetween(1, 100);
        EnumMap<Region, Double> lambdasRegion = new EnumMap<>(Region.class);
        lambdasRegion.put(Region.UNDEFINED, randomDoubleBetween(1, 100));
        
        SurfaceHamiltonianConfig shc = new SurfaceHamiltonianConfig(cell, lambda, lambdasRegion);
        assertEquals(cell, shc.cell);
        assertTrue(shc.hasRegions);
    }
    
    @Test
    public void getLambda_noRegion_returnsValue() {
        double lambda = randomDoubleBetween(1, 100);
        SurfaceHamiltonianConfig shc = new SurfaceHamiltonianConfig(null, lambda, null);
        assertEquals(lambda, shc.getLambda(), EPSILON);
    }
    
    @Test
    public void getLambda_validRegions_returnsValue() {
        double lambda = randomDoubleBetween(1, 100);
        
        EnumMap<Region, Double> lambdasRegion = new EnumMap<>(Region.class);
        lambdasRegion.put(Region.DEFAULT, randomDoubleBetween(0, 100));
        lambdasRegion.put(Region.NUCLEUS, randomDoubleBetween(0, 100));
        
        SurfaceHamiltonianConfig shc = new SurfaceHamiltonianConfig(null, lambda, lambdasRegion);
        
        assertEquals(lambdasRegion.get(Region.DEFAULT), shc.getLambda(Region.DEFAULT), EPSILON);
        assertEquals(lambdasRegion.get(Region.NUCLEUS), shc.getLambda(Region.NUCLEUS), EPSILON);
    }
    
    @Test
    public void getLambda_invalidRegions_returnsNaN() {
        double lambda = randomDoubleBetween(1, 100);
        
        EnumMap<Region, Double> lambdasRegion = new EnumMap<>(Region.class);
        lambdasRegion.put(Region.DEFAULT, randomDoubleBetween(0, 100));
        lambdasRegion.put(Region.NUCLEUS, randomDoubleBetween(0, 100));
        
        SurfaceHamiltonianConfig shc = new SurfaceHamiltonianConfig(null, lambda, lambdasRegion);
        
        assertEquals(Double.NaN, shc.getLambda(null), EPSILON);
        assertEquals(Double.NaN, shc.getLambda(null), EPSILON);
        assertEquals(Double.NaN, shc.getLambda(Region.UNDEFINED), EPSILON);
        assertEquals(Double.NaN, shc.getLambda(Region.UNDEFINED), EPSILON);
    }
    
    @Test
    public void getLambda_nullRegion_returnsNaN() {
        double lambda = randomDoubleBetween(1, 100);
        SurfaceHamiltonianConfig shc = new SurfaceHamiltonianConfig(null, lambda, null);
        assertEquals(Double.NaN, shc.getLambda(Region.DEFAULT), EPSILON);
        assertEquals(Double.NaN, shc.getLambda(Region.NUCLEUS), EPSILON);
    }
}
