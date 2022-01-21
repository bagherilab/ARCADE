package arcade.potts.sim.hamiltonian;

import java.util.EnumMap;
import org.junit.Test;
import arcade.potts.env.loc.PottsLocation;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.core.util.Enums.Region;

public class PersistenceHamiltonianConfigTest {
    private static final double EPSILON = 1E-10;
    
    @Test
    public void constructor_noRegions_setsFields() {
        PottsLocation location = mock(PottsLocation.class);
        
        PersistenceHamiltonianConfig phc = new PersistenceHamiltonianConfig(location, 0, null, 0, 0);
        assertEquals(location, phc.location);
        assertNotNull(phc.vector);
        assertNotNull(phc.displacement);
        assertFalse(phc.hasRegions);
    }
    
    @Test
    public void constructor_emptyRegions_setsFields() {
        PottsLocation location = mock(PottsLocation.class);
        EnumMap<Region, Double> lambdasRegion = new EnumMap<>(Region.class);
        
        PersistenceHamiltonianConfig phc = new PersistenceHamiltonianConfig(location, 0, lambdasRegion, 0, 0);
        assertEquals(location, phc.location);
        assertNotNull(phc.vector);
        assertNotNull(phc.displacement);
        assertFalse(phc.hasRegions);
    }
    
    @Test
    public void constructor_hasRegions_setsFields() {
        PottsLocation location = mock(PottsLocation.class);
        EnumMap<Region, Double> lambdasRegion = new EnumMap<>(Region.class);
        lambdasRegion.put(Region.UNDEFINED, randomDoubleBetween(1, 100));
        
        PersistenceHamiltonianConfig phc = new PersistenceHamiltonianConfig(location, 0, lambdasRegion, 0, 0);
        assertEquals(location, phc.location);
        assertNotNull(phc.vector);
        assertNotNull(phc.displacement);
        assertTrue(phc.hasRegions);
    }
    
    @Test
    public void constructor_called_initializesVectors() {
        PottsLocation location = mock(PottsLocation.class);
        PersistenceHamiltonianConfig phc = new PersistenceHamiltonianConfig(location, 0, null, 0, 0);
        assertNotSame(PersistenceHamiltonianConfig.DEFAULT_UNIT_VECTOR, phc.vector);
        assertArrayEquals(PersistenceHamiltonianConfig.DEFAULT_UNIT_VECTOR, phc.vector, EPSILON);
        assertArrayEquals(new double[] { 0, 0, 0 }, phc.displacement, EPSILON);
    }
    
    @Test
    public void getLambda_noRegion_returnsValue() {
        PottsLocation location = mock(PottsLocation.class);
        double lambda = randomDoubleBetween(1, 100);
        PersistenceHamiltonianConfig phc = new PersistenceHamiltonianConfig(location, lambda, null, 0, 0);
        assertEquals(lambda, phc.getLambda(), EPSILON);
    }
    
    @Test
    public void getLambda_validRegions_returnsValue() {
        PottsLocation location = mock(PottsLocation.class);
        double lambda = randomDoubleBetween(1, 100);
        
        EnumMap<Region, Double> lambdasRegion = new EnumMap<>(Region.class);
        lambdasRegion.put(Region.DEFAULT, randomDoubleBetween(0, 100));
        lambdasRegion.put(Region.NUCLEUS, randomDoubleBetween(0, 100));
        
        PersistenceHamiltonianConfig phc = new PersistenceHamiltonianConfig(location, lambda, lambdasRegion, 0, 0);
        
        assertEquals(lambdasRegion.get(Region.DEFAULT), phc.getLambda(Region.DEFAULT), EPSILON);
        assertEquals(lambdasRegion.get(Region.NUCLEUS), phc.getLambda(Region.NUCLEUS), EPSILON);
    }
    
    @Test
    public void getLambda_invalidRegions_returnsNaN() {
        PottsLocation location = mock(PottsLocation.class);
        double lambda = randomDoubleBetween(1, 100);
        
        EnumMap<Region, Double> lambdasRegion = new EnumMap<>(Region.class);
        lambdasRegion.put(Region.DEFAULT, randomDoubleBetween(0, 100));
        lambdasRegion.put(Region.NUCLEUS, randomDoubleBetween(0, 100));
        
        PersistenceHamiltonianConfig phc = new PersistenceHamiltonianConfig(location, lambda, lambdasRegion, 0, 0);
        
        assertEquals(Double.NaN, phc.getLambda(null), EPSILON);
        assertEquals(Double.NaN, phc.getLambda(Region.UNDEFINED), EPSILON);
    }
    
    @Test
    public void getLambda_nullRegion_returnsNaN() {
        PottsLocation location = mock(PottsLocation.class);
        double lambda = randomDoubleBetween(1, 100);
        PersistenceHamiltonianConfig phc = new PersistenceHamiltonianConfig(location, lambda, null, 0, 0);
        assertEquals(Double.NaN, phc.getLambda(Region.DEFAULT), EPSILON);
        assertEquals(Double.NaN, phc.getLambda(Region.NUCLEUS), EPSILON);
    }
    
    @Test
    public void getDecay_called_returnsValue() {
        PottsLocation location = mock(PottsLocation.class);
        double decay = randomDoubleBetween(1, 100);
        PersistenceHamiltonianConfig phc = new PersistenceHamiltonianConfig(location, 0, null, decay, 0);
        assertEquals(decay, phc.getDecay(), EPSILON);
    }
    
    @Test
    public void getVector_noChange_returnsVector() {
        PottsLocation location = mock(PottsLocation.class);
        int volume = randomIntBetween(10, 100);
        doReturn(volume).when(location).getVolume();
        
        double decay = randomDoubleBetween(0, 1);
        PersistenceHamiltonianConfig phc = new PersistenceHamiltonianConfig(location, 0, null, decay, 0);
        
        double dx = randomDoubleBetween(1, 10);
        double dy = randomDoubleBetween(1, 10);
        double dz = randomDoubleBetween(1, 10);
        
        phc.displacement[0] = dx;
        phc.displacement[1] = dy;
        phc.displacement[2] = dz;
        
        double[] vector = phc.getVector();
        assertArrayEquals(PersistenceHamiltonianConfig.DEFAULT_UNIT_VECTOR, vector, EPSILON);
    }
    
    @Test
    public void getVector_locationChanged_returnsVector() {
        PottsLocation location = mock(PottsLocation.class);
        int volume = randomIntBetween(10, 100);
        doReturn(volume).when(location).getVolume();
        
        double decay = randomDoubleBetween(0, 1);
        double threshold = randomDoubleBetween(1, 100);
        PersistenceHamiltonianConfig phc = new PersistenceHamiltonianConfig(location, 0, null, decay, threshold);
        
        int newVolume = volume + randomIntBetween(1, 10);
        doReturn(newVolume).when(location).getVolume();
        
        double dx = randomDoubleBetween(1, 10);
        double dy = randomDoubleBetween(1, 10);
        double dz = randomDoubleBetween(1, 10);
        
        phc.displacement[0] = dx;
        phc.displacement[1] = dy;
        phc.displacement[2] = dz;
        
        double[] defaultVector = PersistenceHamiltonianConfig.DEFAULT_UNIT_VECTOR;
        double[] vector = phc.getVector();
        
        double[] expected = new double[] {
                (1 - decay) * defaultVector[0] + decay * dx,
                (1 - decay) * defaultVector[1] + decay * dy,
                -newVolume / threshold,
        };
        
        double norm = Math.sqrt(expected[0] * expected[0] + expected[1] * expected[1] + expected[2] * expected[2]);
        
        expected[0] /= norm;
        expected[1] /= norm;
        expected[2] /= norm;
        
        assertArrayEquals(expected, vector, EPSILON);
    }
    
    @Test
    public void getDisplacement_voxelAdded_updatesVector() {
        PottsLocation location = mock(PottsLocation.class);
        int volume = randomIntBetween(10, 100);
        double[] centroid = new double[] {
                randomDoubleBetween(1, 100),
                randomDoubleBetween(1, 100),
                randomDoubleBetween(1, 100),
        };
        
        doReturn(volume).when(location).getVolume();
        doReturn(centroid).when(location).getCentroid();
        
        PersistenceHamiltonianConfig phc = new PersistenceHamiltonianConfig(location, 0, null, 0, 0);
        
        int x = randomIntBetween(1, 10);
        int y = randomIntBetween(1, 10);
        int z = randomIntBetween(1, 10);
        
        phc.getDisplacement(x, y, z, 1);
        
        double[] expected = new double[] {
                (x - centroid[0]) / (volume + 1),
                (y - centroid[1]) / (volume + 1),
                (z - centroid[2]) / (volume + 1),
        };
        
        double norm = Math.sqrt(expected[0] * expected[0] + expected[1] * expected[1] + expected[2] * expected[2]);
        
        expected[0] /= norm;
        expected[1] /= norm;
        expected[2] /= norm;
        
        assertArrayEquals(expected, phc.displacement, EPSILON);
    }
    
    @Test
    public void getDisplacement_voxelRemoved_updatesVector() {
        PottsLocation location = mock(PottsLocation.class);
        int volume = randomIntBetween(10, 100);
        double[] centroid = new double[] {
                randomDoubleBetween(1, 100),
                randomDoubleBetween(1, 100),
                randomDoubleBetween(1, 100),
        };
        
        doReturn(volume).when(location).getVolume();
        doReturn(centroid).when(location).getCentroid();
        
        PersistenceHamiltonianConfig phc = new PersistenceHamiltonianConfig(location, 0, null, 0, 0);
        
        int x = randomIntBetween(1, 10);
        int y = randomIntBetween(1, 10);
        int z = randomIntBetween(1, 10);
        
        phc.getDisplacement(x, y, z, -1);
        
        double[] expected = new double[] {
                (centroid[0] - x) / (volume - 1),
                (centroid[1] - y) / (volume - 1),
                (centroid[2] - z) / (volume - 1),
        };
        
        double norm = Math.sqrt(expected[0] * expected[0] + expected[1] * expected[1] + expected[2] * expected[2]);
        
        expected[0] /= norm;
        expected[1] /= norm;
        expected[2] /= norm;
        
        assertArrayEquals(expected, phc.displacement, EPSILON);
    }
    
    @Test
    public void getDisplacement_voxelAddedRegion_returnsVector() {
        PottsLocation location = mock(PottsLocation.class);
        int volume = randomIntBetween(10, 100);
        double[] centroid = new double[] {
                randomDoubleBetween(1, 100),
                randomDoubleBetween(1, 100),
                randomDoubleBetween(1, 100),
        };
        
        Region region = Region.NUCLEUS;
        doReturn(volume).when(location).getVolume(region);
        doReturn(centroid).when(location).getCentroid(region);
        
        PersistenceHamiltonianConfig phc = new PersistenceHamiltonianConfig(location, 0, null, 0, 0);
        
        double[] displacement = new double[] {
            randomDoubleBetween(1, 10),
            randomDoubleBetween(1, 10),
            randomDoubleBetween(1, 10),
        };
        
        phc.displacement[0] = displacement[0];
        phc.displacement[1] = displacement[1];
        phc.displacement[2] = displacement[2];
        
        int x = randomIntBetween(1, 10);
        int y = randomIntBetween(1, 10);
        int z = randomIntBetween(1, 10);
        
        double[] regionDisplacement = phc.getDisplacement(x, y, z, 1, region);
        
        double[] expected = new double[] {
                (x - centroid[0]) / (volume + 1),
                (y - centroid[1]) / (volume + 1),
                (z - centroid[2]) / (volume + 1),
        };
        
        double norm = Math.sqrt(expected[0] * expected[0] + expected[1] * expected[1] + expected[2] * expected[2]);
        
        expected[0] /= norm;
        expected[1] /= norm;
        expected[2] /= norm;
        
        assertArrayEquals(expected, regionDisplacement, EPSILON);
        assertArrayEquals(displacement, phc.displacement, EPSILON);
    }
    
    @Test
    public void getDisplacement_voxelRemovedRegion_returnsVector() {
        PottsLocation location = mock(PottsLocation.class);
        int volume = randomIntBetween(10, 100);
        double[] centroid = new double[] {
                randomDoubleBetween(1, 100),
                randomDoubleBetween(1, 100),
                randomDoubleBetween(1, 100),
        };
        
        Region region = Region.NUCLEUS;
        doReturn(volume).when(location).getVolume(region);
        doReturn(centroid).when(location).getCentroid(region);
        
        PersistenceHamiltonianConfig phc = new PersistenceHamiltonianConfig(location, 0, null, 0, 0);
        
        double[] displacement = new double[] {
                randomDoubleBetween(1, 10),
                randomDoubleBetween(1, 10),
                randomDoubleBetween(1, 10),
        };
        
        phc.displacement[0] = displacement[0];
        phc.displacement[1] = displacement[1];
        phc.displacement[2] = displacement[2];
        
        int x = randomIntBetween(1, 10);
        int y = randomIntBetween(1, 10);
        int z = randomIntBetween(1, 10);
        
        double[] regionDisplacement = phc.getDisplacement(x, y, z, -1, region);
        
        double[] expected = new double[] {
                (centroid[0] - x) / (volume - 1),
                (centroid[1] - y) / (volume - 1),
                (centroid[2] - z) / (volume - 1),
        };
        
        double norm = Math.sqrt(expected[0] * expected[0] + expected[1] * expected[1] + expected[2] * expected[2]);
        
        expected[0] /= norm;
        expected[1] /= norm;
        expected[2] /= norm;
        
        assertArrayEquals(expected, regionDisplacement, EPSILON);
        assertArrayEquals(displacement, phc.displacement, EPSILON);
    }
}
