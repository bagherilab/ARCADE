package arcade.potts.sim.hamiltonian;

import org.junit.Test;
import arcade.potts.env.loc.PottsLocation;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;

public class PersistenceHamiltonianConfigTest {
    private static final double EPSILON = 1E-10;
    
    @Test
    public void constructor_called_setsFields() {
        PottsLocation location = mock(PottsLocation.class);
        PersistenceHamiltonianConfig phc = new PersistenceHamiltonianConfig(location, 0, 0);
        assertEquals(location, phc.location);
        assertNotNull(phc.vector);
        assertNotNull(phc.displacement);
    }
    
    @Test
    public void constructor_called_initializesVectors() {
        PottsLocation location = mock(PottsLocation.class);
        PersistenceHamiltonianConfig phc = new PersistenceHamiltonianConfig(location, 0, 0);
        assertNotSame(PersistenceHamiltonianConfig.DEFAULT_UNIT_VECTOR, phc.vector);
        assertArrayEquals(PersistenceHamiltonianConfig.DEFAULT_UNIT_VECTOR, phc.vector, EPSILON);
        assertArrayEquals(new double[] { 0, 0, 0 }, phc.displacement, EPSILON);
    }
    
    @Test
    public void getLambda_called_returnsValue() {
        PottsLocation location = mock(PottsLocation.class);
        double lambda = randomDoubleBetween(1, 100);
        PersistenceHamiltonianConfig phc = new PersistenceHamiltonianConfig(location, lambda, 0);
        assertEquals(lambda, phc.getLambda(), EPSILON);
    }
    
    @Test
    public void getDecay_called_returnsValue() {
        PottsLocation location = mock(PottsLocation.class);
        double decay = randomDoubleBetween(1, 100);
        PersistenceHamiltonianConfig phc = new PersistenceHamiltonianConfig(location, 0, decay);
        assertEquals(decay, phc.getDecay(), EPSILON);
    }
    
    @Test
    public void getVector_noChange_returnsVector() {
        PottsLocation location = mock(PottsLocation.class);
        int volume = randomIntBetween(10, 100);
        doReturn(volume).when(location).getVolume();
    
        double decay = randomDoubleBetween(0, 1);
        PersistenceHamiltonianConfig phc = new PersistenceHamiltonianConfig(location, 0, decay);
        
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
        PersistenceHamiltonianConfig phc = new PersistenceHamiltonianConfig(location, 0, decay);
        
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
                (1 - decay) * defaultVector[2] + decay * dz,
        };
        
        double norm = Math.sqrt(expected[0] * expected[0] + expected[1] * expected[1] + expected[2] * expected[2]);
        
        expected[0] /= norm;
        expected[1] /= norm;
        expected[2] /= norm;
        
        assertArrayEquals(expected, vector, EPSILON);
    }
    
    @Test
    public void getDisplacement_called_returnsVector() {
        PersistenceHamiltonianConfig phc = new PersistenceHamiltonianConfig(mock(PottsLocation.class), 0, 0);
        double[] expected = new double[] {
                randomDoubleBetween(1, 100),
                randomDoubleBetween(1, 100),
                randomDoubleBetween(1, 100),
        };
        phc.displacement[0] = expected[0];
        phc.displacement[1] = expected[1];
        phc.displacement[2] = expected[2];
        
        assertArrayEquals(expected, phc.getDisplacement(), EPSILON);
    }
    
    @Test
    public void updateDisplacement_voxelAdded_updatesVector() {
        PottsLocation location = mock(PottsLocation.class);
        int volume = randomIntBetween(10, 100);
        double[] centroid = new double[] {
                randomDoubleBetween(1, 100),
                randomDoubleBetween(1, 100),
                randomDoubleBetween(1, 100),
        };
        
        doReturn(volume).when(location).getVolume();
        doReturn(centroid).when(location).getCentroid();
        
        PersistenceHamiltonianConfig phc = new PersistenceHamiltonianConfig(location, 0, 0);
        
        int x = randomIntBetween(1, 10);
        int y = randomIntBetween(1, 10);
        int z = randomIntBetween(1, 10);
        
        phc.updateDisplacement(x, y, z, 1);
        
        double[] expected = new double[] {
                (x - centroid[0]) / (volume + 1),
                (y - centroid[1]) / (volume + 1),
                (z - centroid[2]) / (volume + 1),
        };
        
        double norm = Math.sqrt(expected[0] * expected[0] + expected[1] * expected[1] + expected[2] * expected[2]);
        
        expected[0] /= norm;
        expected[1] /= norm;
        expected[2] /= norm;
        
        assertEquals(expected[0], phc.displacement[0], EPSILON);
        assertEquals(expected[1], phc.displacement[1], EPSILON);
        assertEquals(expected[2], phc.displacement[2], EPSILON);
    }
    
    @Test
    public void updateDisplacement_voxelRemoved_updatesVector() {
        PottsLocation location = mock(PottsLocation.class);
        int volume = randomIntBetween(10, 100);
        double[] centroid = new double[] {
                randomDoubleBetween(1, 100),
                randomDoubleBetween(1, 100),
                randomDoubleBetween(1, 100),
        };
        
        doReturn(volume).when(location).getVolume();
        doReturn(centroid).when(location).getCentroid();
        
        PersistenceHamiltonianConfig phc = new PersistenceHamiltonianConfig(location, 0, 0);
        
        int x = randomIntBetween(1, 10);
        int y = randomIntBetween(1, 10);
        int z = randomIntBetween(1, 10);
        
        phc.updateDisplacement(x, y, z, -1);
        
        double[] expected = new double[] {
                (centroid[0] - x) / (volume - 1),
                (centroid[1] - y) / (volume - 1),
                (centroid[2] - z) / (volume - 1),
        };
        
        double norm = Math.sqrt(expected[0] * expected[0] + expected[1] * expected[1] + expected[2] * expected[2]);
        
        expected[0] /= norm;
        expected[1] /= norm;
        expected[2] /= norm;
        
        assertEquals(expected[0], phc.displacement[0], EPSILON);
        assertEquals(expected[1], phc.displacement[1], EPSILON);
        assertEquals(expected[2], phc.displacement[2], EPSILON);
    }
}
