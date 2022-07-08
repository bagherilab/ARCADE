package arcade.potts.sim.hamiltonian;

import java.util.HashMap;
import org.junit.Test;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSeries;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.core.sim.Series.TARGET_SEPARATOR;
import static arcade.core.util.Enums.Region;

public class JunctionHamiltonianTest {
    private static final double EPSILON = 1E-5;
    
    @Test
    public void constructor_called_initializesMaps() {
        JunctionHamiltonian jh = new JunctionHamiltonian(mock(PottsSeries.class), mock(Potts.class));
        assertNotNull(jh.configs);
        assertNotNull(jh.popToLambda);
    }
    
    @Test
    public void constructor_called_setsArrays() {
        Potts potts = mock(Potts.class);
        int[][][] ids = new int[0][0][0];
        potts.ids = ids;
        
        JunctionHamiltonian jh = new JunctionHamiltonian(mock(PottsSeries.class), potts);
        
        assertSame(ids, jh.ids);
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
        series.populations.put(key2, population2);
        
        double lambda1 = randomDoubleBetween(1, 100);
        double lambda2 = randomDoubleBetween(1, 100);
        
        series.potts.put("junction/LAMBDA" + TARGET_SEPARATOR + key1, lambda1);
        series.potts.put("junction/LAMBDA" + TARGET_SEPARATOR + key2, lambda2);
        
        JunctionHamiltonian jh = new JunctionHamiltonian(series, mock(Potts.class));
        
        assertEquals(2, jh.popToLambda.size());
        assertTrue(jh.popToLambda.containsKey(code1));
        assertTrue(jh.popToLambda.containsKey(code2));
        assertEquals(lambda1, jh.popToLambda.get(code1), EPSILON);
        assertEquals(lambda2, jh.popToLambda.get(code2), EPSILON);
    }
    
    @Test
    public void register_givenCell_addsConfig() {
        JunctionHamiltonian jh = new JunctionHamiltonian(mock(PottsSeries.class), mock(Potts.class));
        PottsCell cell = mock(PottsCell.class);
        
        int id = randomIntBetween(1, 10);
        int pop = randomIntBetween(1, 10);
        
        doReturn(id).when(cell).getID();
        doReturn(pop).when(cell).getPop();
        
        double lambda = randomDoubleBetween(1, 100);
        jh.popToLambda.put(pop, lambda);
    
        jh.register(cell);
        JunctionHamiltonianConfig config = jh.configs.get(id);
        
        assertNotNull(config);
        assertEquals(lambda, config.getLambda(), EPSILON);
    }
    
    @Test
    public void deregister_exists_removesConfig() {
        JunctionHamiltonian jh = new JunctionHamiltonian(mock(PottsSeries.class), mock(Potts.class));
        PottsCell cell = mock(PottsCell.class);
        
        int id = randomIntBetween(1, 10);
        doReturn(id).when(cell).getID();
        
        JunctionHamiltonianConfig config = mock(JunctionHamiltonianConfig.class);
        jh.configs.put(id, config);
    
        jh.deregister(cell);
        
        assertFalse(jh.configs.containsKey(id));
    }
    
    @Test
    public void getDelta_validIDs_calculatesValue() {
        int id1 = randomIntBetween(1, 100);
        int id2 = id1 + randomIntBetween(1, 100);
        
        Potts potts = mock(Potts.class);
        potts.ids = new int[][][] {
                {
                        { id1, id1, id1, id1 },
                        { id1, id1, id1, id1 },
                        { id1, id1, id1, id1 },
                },
                {
                        { id2, id2, 0,   0 },
                        { id2,   0, 0, id1 },
                        { id2, id2, 0,   0 },
                },
        };
        
        JunctionHamiltonianConfig config = mock(JunctionHamiltonianConfig.class);
        double lambda = randomDoubleBetween(10, 100);
        doReturn(lambda).when(config).getLambda();
        
        JunctionHamiltonian jh = new JunctionHamiltonian(mock(PottsSeries.class), potts);
        
        jh.configs.put(id1, config);
        
        double delta = jh.getDelta(0, id1, 1, 2, 1);
        assertEquals(3 * lambda, delta, EPSILON);
    }
    
    @Test
    public void getDelta_invalidIDs_returnsZeros() {
        int id1 = randomIntBetween(1, 100);
        int id2 = id1 + randomIntBetween(1, 100);
        
        JunctionHamiltonian jh = new JunctionHamiltonian(mock(PottsSeries.class), mock(Potts.class));
        
        double delta1 = jh.getDelta(0, 0, 0, 0, 0);
        assertEquals(0, delta1, EPSILON);
        
        double delta2 = jh.getDelta(id1, 0, 0, 0, 0);
        assertEquals(0, delta2, EPSILON);
        
        double delta3 = jh.getDelta(id1, id2, 0, 0, 0);
        assertEquals(0, delta3, EPSILON);
    }
    
    @Test
    public void getDelta_validRegions_returnsZero() {
        JunctionHamiltonian jh = new JunctionHamiltonian(mock(PottsSeries.class), mock(Potts.class));
        int id = randomIntBetween(1, 100);
        
        double delta1 = jh.getDelta(id, Region.DEFAULT.ordinal(), Region.NUCLEUS.ordinal(), 0, 0, 0);
        assertEquals(0, delta1, EPSILON);
        
        double delta2 = jh.getDelta(id, Region.NUCLEUS.ordinal(), Region.DEFAULT.ordinal(), 0, 0, 0);
        assertEquals(0, delta2, EPSILON);
    }
}
