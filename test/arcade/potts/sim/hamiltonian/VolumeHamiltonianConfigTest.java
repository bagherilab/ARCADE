package arcade.potts.sim.hamiltonian;

import java.util.EnumMap;
import org.junit.Test;
import arcade.potts.agent.cell.PottsCell;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.core.util.Enums.Region;

public class VolumeHamiltonianConfigTest {
    private static final double EPSILON = 1E-10;
    
    @Test
    public void constructor_called_setsFields() {
        PottsCell cell = mock(PottsCell.class);
        double lambda = randomDoubleBetween(1, 100);
        VolumeHamiltonianConfig vhc = new VolumeHamiltonianConfig(cell, lambda, null);
        assertEquals(cell, vhc.cell);
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
        assertEquals(Double.NaN, vhc.getLambda(null), EPSILON);
        assertEquals(Double.NaN, vhc.getLambda(Region.UNDEFINED), EPSILON);
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
