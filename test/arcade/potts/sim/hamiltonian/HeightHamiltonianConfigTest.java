package arcade.potts.sim.hamiltonian;

import java.util.EnumMap;
import org.junit.Test;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.env.location.PottsLocation;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.core.util.Enums.Region;

public class HeightHamiltonianConfigTest {
    private static final double EPSILON = 1E-10;
    
    @Test
    public void constructor_noRegions_setsFields() {
        PottsCell cell = mock(PottsCell.class);
        PottsLocation location = mock(PottsLocation.class);
        doReturn(location).when(cell).getLocation();
        
        HeightHamiltonianConfig hhc = new HeightHamiltonianConfig(cell, 0, null);
        assertEquals(cell, hhc.cell);
        assertEquals(location, hhc.location);
        assertFalse(hhc.hasRegions);
    }
    
    @Test
    public void constructor_emptyRegions_setsFields() {
        PottsCell cell = mock(PottsCell.class);
        PottsLocation location = mock(PottsLocation.class);
        doReturn(location).when(cell).getLocation();
        EnumMap<Region, Double> lambdasRegion = new EnumMap<>(Region.class);
        
        HeightHamiltonianConfig hhc = new HeightHamiltonianConfig(cell, 0, lambdasRegion);
        assertEquals(cell, hhc.cell);
        assertEquals(location, hhc.location);
        assertFalse(hhc.hasRegions);
    }
    
    @Test
    public void constructor_hasRegions_setsFields() {
        PottsCell cell = mock(PottsCell.class);
        PottsLocation location = mock(PottsLocation.class);
        doReturn(location).when(cell).getLocation();
        EnumMap<Region, Double> lambdasRegion = new EnumMap<>(Region.class);
        lambdasRegion.put(Region.UNDEFINED, randomDoubleBetween(1, 100));
        
        HeightHamiltonianConfig hhc = new HeightHamiltonianConfig(cell, 0, lambdasRegion);
        assertEquals(cell, hhc.cell);
        assertEquals(location, hhc.location);
        assertTrue(hhc.hasRegions);
    }
    
    @Test
    public void getLambda_noRegion_returnsValue() {
        PottsCell cell = mock(PottsCell.class);
        double lambda = randomDoubleBetween(1, 100);
        HeightHamiltonianConfig hhc = new HeightHamiltonianConfig(cell, lambda, null);
        assertEquals(lambda, hhc.getLambda(), EPSILON);
    }
    
    @Test
    public void getLambda_validRegions_returnsValue() {
        PottsCell cell = mock(PottsCell.class);
        double lambda = randomDoubleBetween(1, 100);
        
        EnumMap<Region, Double> lambdasRegion = new EnumMap<>(Region.class);
        lambdasRegion.put(Region.DEFAULT, randomDoubleBetween(0, 100));
        lambdasRegion.put(Region.NUCLEUS, randomDoubleBetween(0, 100));
        
        HeightHamiltonianConfig hhc = new HeightHamiltonianConfig(cell, lambda, lambdasRegion);
        
        assertEquals(lambdasRegion.get(Region.DEFAULT), hhc.getLambda(Region.DEFAULT), EPSILON);
        assertEquals(lambdasRegion.get(Region.NUCLEUS), hhc.getLambda(Region.NUCLEUS), EPSILON);
    }
    
    @Test
    public void getLambda_invalidRegions_returnsNaN() {
        PottsCell cell = mock(PottsCell.class);
        double lambda = randomDoubleBetween(1, 100);
        
        EnumMap<Region, Double> lambdasRegion = new EnumMap<>(Region.class);
        lambdasRegion.put(Region.DEFAULT, randomDoubleBetween(0, 100));
        lambdasRegion.put(Region.NUCLEUS, randomDoubleBetween(0, 100));
        
        HeightHamiltonianConfig hhc = new HeightHamiltonianConfig(cell, lambda, lambdasRegion);
        
        assertEquals(Double.NaN, hhc.getLambda(null), EPSILON);
        assertEquals(Double.NaN, hhc.getLambda(Region.UNDEFINED), EPSILON);
    }
    
    @Test
    public void getLambda_nullRegion_returnsNaN() {
        PottsCell cell = mock(PottsCell.class);
        double lambda = randomDoubleBetween(1, 100);
        HeightHamiltonianConfig hhc = new HeightHamiltonianConfig(cell, lambda, null);
        assertEquals(Double.NaN, hhc.getLambda(Region.DEFAULT), EPSILON);
        assertEquals(Double.NaN, hhc.getLambda(Region.NUCLEUS), EPSILON);
    }
}
