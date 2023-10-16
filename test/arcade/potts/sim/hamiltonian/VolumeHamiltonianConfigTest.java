package arcade.potts.sim.hamiltonian;

import java.util.EnumMap;
import org.junit.Test;
import arcade.potts.agent.cell.PottsCell;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.potts.util.PottsEnums.Region;

public class VolumeHamiltonianConfigTest {
    private static final double EPSILON = 1E-10;
    
    @Test
    public void constructor_noRegions_setsFields() {
        PottsCell cell = mock(PottsCell.class);
        
        VolumeHamiltonianConfig vhc = new VolumeHamiltonianConfig(cell, 0, null);
        assertEquals(cell, vhc.cell);
        assertFalse(vhc.hasRegions);
    }
    
    @Test
    public void constructor_emptyRegions_setsFields() {
        PottsCell cell = mock(PottsCell.class);
        EnumMap<Region, Double> lambdasRegion = new EnumMap<>(Region.class);
        
        VolumeHamiltonianConfig vhc = new VolumeHamiltonianConfig(cell, 0, lambdasRegion);
        assertEquals(cell, vhc.cell);
        assertFalse(vhc.hasRegions);
    }
    
    @Test
    public void constructor_hasRegions_setsFields() {
        PottsCell cell = mock(PottsCell.class);
        EnumMap<Region, Double> lambdasRegion = new EnumMap<>(Region.class);
        lambdasRegion.put(Region.UNDEFINED, randomDoubleBetween(1, 100));
        
        VolumeHamiltonianConfig vhc = new VolumeHamiltonianConfig(cell, 0, lambdasRegion);
        assertEquals(cell, vhc.cell);
        assertTrue(vhc.hasRegions);
    }
    
    @Test
    public void getLambda_noRegion_returnsValue() {
        double lambda = randomDoubleBetween(1, 100);
        VolumeHamiltonianConfig vhc = new VolumeHamiltonianConfig(null, lambda, null);
        assertEquals(lambda, vhc.getLambda(), EPSILON);
    }
    
    @Test
    public void getLambda_validRegions_returnsValue() {
        double lambda = randomDoubleBetween(1, 100);
        
        EnumMap<Region, Double> lambdasRegion = new EnumMap<>(Region.class);
        lambdasRegion.put(Region.DEFAULT, randomDoubleBetween(0, 100));
        lambdasRegion.put(Region.NUCLEUS, randomDoubleBetween(0, 100));
        
        VolumeHamiltonianConfig vhc = new VolumeHamiltonianConfig(null, lambda, lambdasRegion);
        
        assertEquals(lambdasRegion.get(Region.DEFAULT), vhc.getLambda(Region.DEFAULT), EPSILON);
        assertEquals(lambdasRegion.get(Region.NUCLEUS), vhc.getLambda(Region.NUCLEUS), EPSILON);
    }
    
    @Test
    public void getLambda_invalidRegions_returnsNaN() {
        double lambda = randomDoubleBetween(1, 100);
        
        EnumMap<Region, Double> lambdasRegion = new EnumMap<>(Region.class);
        lambdasRegion.put(Region.DEFAULT, randomDoubleBetween(0, 100));
        lambdasRegion.put(Region.NUCLEUS, randomDoubleBetween(0, 100));
        
        VolumeHamiltonianConfig vhc = new VolumeHamiltonianConfig(null, lambda, lambdasRegion);
        
        assertEquals(Double.NaN, vhc.getLambda(null), EPSILON);
        assertEquals(Double.NaN, vhc.getLambda(Region.UNDEFINED), EPSILON);
    }
    
    @Test
    public void getLambda_nullRegion_returnsNaN() {
        double lambda = randomDoubleBetween(1, 100);
        VolumeHamiltonianConfig vhc = new VolumeHamiltonianConfig(null, lambda, null);
        assertEquals(Double.NaN, vhc.getLambda(Region.DEFAULT), EPSILON);
        assertEquals(Double.NaN, vhc.getLambda(Region.NUCLEUS), EPSILON);
    }
}
