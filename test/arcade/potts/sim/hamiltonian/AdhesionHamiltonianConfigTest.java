package arcade.potts.sim.hamiltonian;

import java.util.EnumMap;
import java.util.EnumSet;
import org.junit.jupiter.api.Test;
import arcade.potts.agent.cell.PottsCell;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.potts.util.PottsEnums.Region;

public class AdhesionHamiltonianConfigTest {
    private static final double EPSILON = 1E-10;
    
    static EnumMap<Region, Double> makeRegionMap(EnumSet<Region> regions) {
        EnumMap<Region, Double> map = new EnumMap<>(Region.class);
        for (Region region : regions) {
            map.put(region, randomDoubleBetween(1, 100));
        }
        return map;
    }
    
    @Test
    public void constructor_called_setsFields() {
        PottsCell cell = mock(PottsCell.class);
        double[] adhesion = new double[] { randomDoubleBetween(1, 100) };
        AdhesionHamiltonianConfig ahc = new AdhesionHamiltonianConfig(cell, adhesion, null);
        assertEquals(cell, ahc.cell);
        assertFalse(ahc.hasRegions);
    }
    
    @Test
    public void constructor_emptyRegions_setsFields() {
        PottsCell cell = mock(PottsCell.class);
        double[] adhesion = new double[] { randomDoubleBetween(1, 100) };
        EnumMap<Region, EnumMap<Region, Double>> adhesionRegion = new EnumMap<>(Region.class);
        
        AdhesionHamiltonianConfig ahc = new AdhesionHamiltonianConfig(cell, adhesion, adhesionRegion);
        assertEquals(cell, ahc.cell);
        assertFalse(ahc.hasRegions);
    }
    
    @Test
    public void constructor_hasRegions_setsFields() {
        PottsCell cell = mock(PottsCell.class);
        double[] adhesion = new double[] { randomDoubleBetween(1, 100) };
        EnumMap<Region, EnumMap<Region, Double>> adhesionRegion = new EnumMap<>(Region.class);
        adhesionRegion.put(Region.UNDEFINED, new EnumMap<>(Region.class));
        
        AdhesionHamiltonianConfig ahc = new AdhesionHamiltonianConfig(cell, adhesion, adhesionRegion);
        assertEquals(cell, ahc.cell);
        assertTrue(ahc.hasRegions);
    }
    
    @Test
    public void getAdhesion_givenPop_returnsValue() {
        double[] adhesion = new double[] {
                randomDoubleBetween(1, 100),
                randomDoubleBetween(1, 100),
                randomDoubleBetween(1, 100)
        };
        AdhesionHamiltonianConfig ahc = new AdhesionHamiltonianConfig(null, adhesion, null);
        
        assertEquals(adhesion[0], ahc.getAdhesion(0), EPSILON);
        assertEquals(adhesion[1], ahc.getAdhesion(1), EPSILON);
        assertEquals(adhesion[2], ahc.getAdhesion(2), EPSILON);
    }
    
    @Test
    public void getAdhesion_validRegions_returnsValue() {
        EnumSet<Region> regions = EnumSet.of(Region.DEFAULT, Region.NUCLEUS);
        
        EnumMap<Region, Double> defaultAdhesion = makeRegionMap(regions);
        EnumMap<Region, Double> nucleusAdhesion = makeRegionMap(regions);
        
        EnumMap<Region, EnumMap<Region, Double>> adhesionRegion = new EnumMap<>(Region.class);
        adhesionRegion.put(Region.DEFAULT, defaultAdhesion);
        adhesionRegion.put(Region.NUCLEUS, nucleusAdhesion);
        
        AdhesionHamiltonianConfig ahc = new AdhesionHamiltonianConfig(null, new double[] { }, adhesionRegion);
        
        for (Region target : regions) {
            assertEquals(defaultAdhesion.get(target), ahc.getAdhesion(Region.DEFAULT, target), EPSILON);
            assertEquals(nucleusAdhesion.get(target), ahc.getAdhesion(Region.NUCLEUS, target), EPSILON);
        }
    }
    
    @Test
    public void getAdhesion_nullRegions_returnsNaN() {
        EnumSet<Region> regions = EnumSet.of(Region.DEFAULT, Region.NUCLEUS);
        
        EnumMap<Region, Double> defaultAdhesion = makeRegionMap(regions);
        EnumMap<Region, Double> nucleusAdhesion = makeRegionMap(regions);
        
        EnumMap<Region, EnumMap<Region, Double>> adhesionRegion = new EnumMap<>(Region.class);
        adhesionRegion.put(Region.DEFAULT, defaultAdhesion);
        adhesionRegion.put(Region.NUCLEUS, nucleusAdhesion);
        
        AdhesionHamiltonianConfig ahc = new AdhesionHamiltonianConfig(null, new double[] { }, adhesionRegion);
        
        assertEquals(Double.NaN, ahc.getAdhesion(null, null), EPSILON);
        for (Region region : regions) {
            assertEquals(Double.NaN, ahc.getAdhesion(region, null), EPSILON);
            assertEquals(Double.NaN, ahc.getAdhesion(null, region), EPSILON);
        }
    }
    
    @Test
    public void getAdhesion_invalidRegions_returnsNaN() {
        EnumSet<Region> regions = EnumSet.of(Region.DEFAULT, Region.NUCLEUS);
        
        EnumMap<Region, Double> defaultAdhesion = makeRegionMap(regions);
        EnumMap<Region, Double> nucleusAdhesion = makeRegionMap(regions);
        
        EnumMap<Region, EnumMap<Region, Double>> adhesionRegion = new EnumMap<>(Region.class);
        adhesionRegion.put(Region.DEFAULT, defaultAdhesion);
        adhesionRegion.put(Region.NUCLEUS, nucleusAdhesion);
        
        AdhesionHamiltonianConfig ahc = new AdhesionHamiltonianConfig(null, new double[] { }, adhesionRegion);
        
        assertEquals(Double.NaN, ahc.getAdhesion(Region.UNDEFINED, Region.UNDEFINED), EPSILON);
        for (Region region : regions) {
            assertEquals(Double.NaN, ahc.getAdhesion(region, Region.UNDEFINED), EPSILON);
            assertEquals(Double.NaN, ahc.getAdhesion(Region.UNDEFINED, region), EPSILON);
        }
    }
    
    @Test
    public void getAdhesion_noRegions_returnsNaN() {
        EnumSet<Region> regions = EnumSet.of(Region.DEFAULT, Region.NUCLEUS);
        
        AdhesionHamiltonianConfig ahc = new AdhesionHamiltonianConfig(null, new double[] { }, null);
        
        for (Region region : regions) {
            for (Region target : regions) {
                assertEquals(Double.NaN, ahc.getAdhesion(region, target), EPSILON);
            }
        }
    }
}
