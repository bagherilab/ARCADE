package arcade.potts.sim.hamiltonian;

import java.lang.reflect.Field;
import java.util.Arrays;
import org.junit.Test;
import arcade.core.util.Enums;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.sim.Potts;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;

public class SubstrateHamiltonianTest {
    private static final double EPSILON = 1E-10;
    static final double[] SUBSTRATES = new double[] {
            randomDoubleBetween(10, 100),
            randomDoubleBetween(10, 100),
            randomDoubleBetween(10, 100),
    };
    
    static Potts makePottsMock(int length, int width) {
        Potts potts = mock(Potts.class);
        
        int nCells = 2;
        doReturn(null).when(potts).getCell(0);
        
        for (int i = 0; i < nCells; i++) {
            PottsCell c = mock(PottsCell.class);
            when(c.getSubstrate()).thenReturn(SUBSTRATES[i]);
            doReturn(c).when(potts).getCell(i + 1);
        }
        
        try {
            Field lengthField = Potts.class.getDeclaredField("length");
            lengthField.setAccessible(true);
            lengthField.setInt(potts, length);
            
            Field widthField = Potts.class.getDeclaredField("width");
            widthField.setAccessible(true);
            widthField.setInt(potts, width);
        } catch (Exception ignored) { }
        
        return potts;
    }
    
    @Test
    public void constructor_called_setsObject() {
        Potts potts = mock(Potts.class);
        SubstrateHamiltonian sh = new SubstrateHamiltonian(potts);
        assertEquals(potts, sh.potts);
    }
    
    @Test
    public void constructor_called_createsArray() {
        int length = randomIntBetween(10, 100);
        int width = randomIntBetween(10, 100);
        Potts potts = makePottsMock(length, width);
        
        SubstrateHamiltonian sh = new SubstrateHamiltonian(potts);
        
        assertEquals(length + 2, sh.substrate.length);
        assertEquals(width + 2, sh.substrate[0].length);
        
        for (int[] row : sh.substrate) {
            int[] unique = Arrays.stream(row).distinct().toArray();
            assertArrayEquals(new int[] { 1 }, unique);
        }
    }
    
    @Test
    public void getDelta_validIDs_calculatesValue() {
        Potts potts = mock(Potts.class);
        SubstrateHamiltonian sh = spy(new SubstrateHamiltonian(potts));
        
        double substrate1 = randomDoubleBetween(10, 100);
        doReturn(substrate1).when(sh).getSubstrate(1, 0, 0, 0);
        
        double substrate2 = randomDoubleBetween(10, 100);
        doReturn(substrate2).when(sh).getSubstrate(2, 0, 0, 0);
        
        assertEquals(substrate2 - substrate1, sh.getDelta(1, 2, 0, 0, 0), EPSILON);
        assertEquals(substrate1 - substrate2, sh.getDelta(2, 1, 0, 0, 0), EPSILON);
    }
    
    @Test
    public void getDelta_validRegions_returnsZero() {
        Potts potts = mock(Potts.class);
        SubstrateHamiltonian sh = new SubstrateHamiltonian(potts);
        
        int id = randomIntBetween(1, 100);
        
        double delta1 = sh.getDelta(id, Enums.Region.DEFAULT.ordinal(), Enums.Region.NUCLEUS.ordinal(), 0, 0, 0);
        assertEquals(0, delta1, EPSILON);
        
        double delta2 = sh.getDelta(id, Enums.Region.NUCLEUS.ordinal(), Enums.Region.DEFAULT.ordinal(), 0, 0, 0);
        assertEquals(0, delta2, EPSILON);
    }
    
    @Test
    public void getSubstrate_validID_returnsValue() {
        Potts potts = makePottsMock(3, 3);
        SubstrateHamiltonian sh = new SubstrateHamiltonian(potts);
        
        assertEquals(SUBSTRATES[0] * -9, sh.getSubstrate(1, 1, 1, 1), EPSILON);
        assertEquals(SUBSTRATES[1] * -9, sh.getSubstrate(2, 1, 1, 1), EPSILON);
    }
    
    @Test
    public void getSubstrate_invalidID_returnsZero() {
        Potts potts = makePottsMock(3, 3);
        SubstrateHamiltonian sh = new SubstrateHamiltonian(potts);
        
        assertEquals(0, sh.getSubstrate(0, 1, 1, 1), EPSILON);
        assertEquals(0, sh.getSubstrate(-1, 1, 1, 1), EPSILON);
    }
    
    @Test
    public void getSubstrate_invalidPosition_returnsZero() {
        Potts potts = makePottsMock(3, 3);
        SubstrateHamiltonian sh = new SubstrateHamiltonian(potts);
        
        assertEquals(0, sh.getSubstrate(1, 1, 1, 2), EPSILON);
        assertEquals(0, sh.getSubstrate(1, 1, 1, 0), EPSILON);
    }
}
