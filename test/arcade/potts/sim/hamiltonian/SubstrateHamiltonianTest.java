package arcade.potts.sim.hamiltonian;

import java.lang.reflect.Field;
import java.util.Arrays;
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

public class SubstrateHamiltonianTest {
    private static final double EPSILON = 1E-10;
    
    @Test
    public void constructor_called_initializesMaps() {
        SubstrateHamiltonian sh = new SubstrateHamiltonian(mock(PottsSeries.class), mock(Potts.class));
        assertNotNull(sh.configs);
        assertNotNull(sh.popToSubstrate);
    }
    
    @Test
    public void constructor_called_createsArray() {
        int length = randomIntBetween(10, 100);
        int width = randomIntBetween(10, 100);
        Potts potts = mock(Potts.class);
        
        try {
            Field lengthField = Potts.class.getDeclaredField("length");
            lengthField.setAccessible(true);
            lengthField.setInt(potts, length);
            
            Field widthField = Potts.class.getDeclaredField("width");
            widthField.setAccessible(true);
            widthField.setInt(potts, width);
        } catch (Exception ignored) { }
        
        SubstrateHamiltonian sh = new SubstrateHamiltonian(mock(PottsSeries.class), potts);
        
        assertEquals(length + 2, sh.substrates.length);
        assertEquals(width + 2, sh.substrates[0].length);
        
        for (int[] row : sh.substrates) {
            int[] unique = Arrays.stream(row).distinct().toArray();
            assertArrayEquals(new int[] { 1 }, unique);
        }
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
        
        double substrate1 = randomDoubleBetween(1, 100);
        double substrate2 = randomDoubleBetween(1, 100);
        
        series.potts.put("substrate/ADHESION" + TARGET_SEPARATOR + key1, substrate1);
        series.potts.put("substrate/ADHESION" + TARGET_SEPARATOR + key2, substrate2);
        
        double power = randomDoubleBetween(1, 10);
        double heightThreshold = Math.exp(Math.log(SubstrateHamiltonian.THRESHOLD_FRACTION) / power);
        
        series.potts.put("substrate/HEIGHT_THRESHOLD", heightThreshold);
        
        SubstrateHamiltonian sh = new SubstrateHamiltonian(series, mock(Potts.class));
        
        assertEquals(2, sh.popToSubstrate.size());
        assertTrue(sh.popToSubstrate.containsKey(code1));
        assertTrue(sh.popToSubstrate.containsKey(code2));
        assertEquals(substrate1, sh.popToSubstrate.get(code1), EPSILON);
        assertEquals(substrate2, sh.popToSubstrate.get(code2), EPSILON);
        assertEquals(power, sh.power, EPSILON);
    }
    
    @Test
    public void register_givenCell_addsConfig() {
        SubstrateHamiltonian sh = new SubstrateHamiltonian(mock(PottsSeries.class), mock(Potts.class));
        PottsCell cell = mock(PottsCell.class);
        
        int id = randomIntBetween(1, 10);
        int pop = randomIntBetween(1, 10);
        
        doReturn(id).when(cell).getID();
        doReturn(pop).when(cell).getPop();
        
        double substrate = randomDoubleBetween(1, 100);
        sh.popToSubstrate.put(pop, substrate);
        
        sh.register(cell);
        SubstrateHamiltonianConfig config = sh.configs.get(id);
        
        assertNotNull(config);
        assertEquals(substrate, config.getSubstrate(), EPSILON);
    }
    
    @Test
    public void deregister_exists_removesConfig() {
        SubstrateHamiltonian sh = new SubstrateHamiltonian(mock(PottsSeries.class), mock(Potts.class));
        PottsCell cell = mock(PottsCell.class);
        
        int id = randomIntBetween(1, 10);
        doReturn(id).when(cell).getID();
        
        SubstrateHamiltonianConfig config = mock(SubstrateHamiltonianConfig.class);
        sh.configs.put(id, config);
        
        sh.deregister(cell);
        
        assertFalse(sh.configs.containsKey(id));
    }
    
    @Test
    public void getDelta_validIDs_calculatesValue() {
        SubstrateHamiltonian sh = spy(new SubstrateHamiltonian(mock(PottsSeries.class), mock(Potts.class)));
        
        double substrate1 = randomDoubleBetween(10, 100);
        doReturn(substrate1).when(sh).getSubstrate(1, 0, 0, 0);
        
        double substrate2 = randomDoubleBetween(10, 100);
        doReturn(substrate2).when(sh).getSubstrate(2, 0, 0, 0);
        
        assertEquals(substrate2 - substrate1, sh.getDelta(1, 2, 0, 0, 0), EPSILON);
        assertEquals(substrate1 - substrate2, sh.getDelta(2, 1, 0, 0, 0), EPSILON);
    }
    
    @Test
    public void getDelta_validRegions_returnsZero() {
        SubstrateHamiltonian sh = spy(new SubstrateHamiltonian(mock(PottsSeries.class), mock(Potts.class)));
        
        int id = randomIntBetween(1, 100);
        
        double delta1 = sh.getDelta(id, Region.DEFAULT.ordinal(), Region.NUCLEUS.ordinal(), 0, 0, 0);
        assertEquals(0, delta1, EPSILON);
        
        double delta2 = sh.getDelta(id, Region.NUCLEUS.ordinal(), Region.DEFAULT.ordinal(), 0, 0, 0);
        assertEquals(0, delta2, EPSILON);
    }
    
    @Test
    public void getSubstrate_validID_returnsValue() {
        Potts potts = mock(Potts.class);
        
        try {
            Field lengthField = Potts.class.getDeclaredField("length");
            lengthField.setAccessible(true);
            lengthField.setInt(potts, 3);
            
            Field widthField = Potts.class.getDeclaredField("width");
            widthField.setAccessible(true);
            widthField.setInt(potts, 3);
        } catch (Exception ignored) { }
        
        double power = randomDoubleBetween(1, 5);
        
        SubstrateHamiltonian sh = new SubstrateHamiltonian(mock(PottsSeries.class), potts);
        sh.power = power;
        
        SubstrateHamiltonianConfig config = mock(SubstrateHamiltonianConfig.class);
        double substrate = randomDoubleBetween(10, 20);
        doReturn(substrate).when(config).getSubstrate();
        
        sh.configs.put(1, config);
        
        double scale = -9. / SubstrateHamiltonian.NUMBER_NEIGHBORS;
        assertEquals(scale * substrate, sh.getSubstrate(1, 1, 1, 1), EPSILON);
        assertEquals(scale * substrate * Math.pow(2, power), sh.getSubstrate(1, 1, 1, 2), EPSILON);
        assertEquals(scale * substrate * Math.pow(10, power), sh.getSubstrate(1, 1, 1, 10), EPSILON);
    }
    
    @Test
    public void getSubstrate_invalidID_returnsZero() {
        SubstrateHamiltonian sh = new SubstrateHamiltonian(mock(PottsSeries.class), mock(Potts.class));
        assertEquals(0, sh.getSubstrate(0, 1, 1, 1), EPSILON);
        assertEquals(0, sh.getSubstrate(-1, 1, 1, 1), EPSILON);
    }
}
