package arcade.potts.sim.hamiltonian;

import java.lang.reflect.Field;
import org.junit.Test;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSeries;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.util.Enums.Region;
import static arcade.potts.sim.hamiltonian.AdhesionHamiltonian2D.NEIGHBORHOOD_SIZE;

public class AdhesionHamiltonian2DTest {
    private static final double EPSILON = 1E-10;
    
    static final int REGION_DEFAULT = Region.DEFAULT.ordinal();
    
    static final int REGION_NUCLEUS = Region.NUCLEUS.ordinal();
    
    static Potts makePottsMock() {
        Potts potts = mock(Potts.class);
        
        potts.ids = new int[][][] {
                {
                        { 0, 0, 0, 0, 0, 0 },
                        { 0, 1, 1, 3, 3, 0 },
                        { 0, 1, 1, 3, 3, 0 },
                        { 0, 2, 2, 0, 0, 0 },
                        { 0, 0, 0, 0, 0, 0 },
                }
        };
        
        int d = REGION_DEFAULT;
        int n = REGION_NUCLEUS;
        
        potts.regions = new int[][][] {
                {
                        { 0, 0, 0, 0, 0, 0 },
                        { 0, d, d, d, 0, 0 },
                        { 0, 0, n, n, 0, 0 },
                        { 0, d, d, 0, 0, 0 },
                        { 0, 0, 0, 0, 0, 0 },
                }
        };
        
        return potts;
    }
    
    @Test
    public void getAdhesion_validIDs_calculatesValue() {
        Potts potts = makePottsMock();
        AdhesionHamiltonian2D ah = new AdhesionHamiltonian2D(mock(PottsSeries.class), potts);
        
        double[][] adhesions = new double[][] {
                { Double.NaN, Double.NaN, Double.NaN },
                { 1, 2, 3 },
                { 4, 5, 6 }
        };
        double adhesion = (adhesions[1][2] + adhesions[2][1]) / 2;
        
        PottsCell cell1 = mock(PottsCell.class);
        doReturn(1).when(cell1).getPop();
    
        PottsCell cell2 = mock(PottsCell.class);
        doReturn(2).when(cell2).getPop();
    
        PottsCell cell3 = mock(PottsCell.class);
        doReturn(1).when(cell3).getPop();
        
        AdhesionHamiltonianConfig config1 = mock(AdhesionHamiltonianConfig.class);
        AdhesionHamiltonianConfig config2 = mock(AdhesionHamiltonianConfig.class);
        AdhesionHamiltonianConfig config3 = mock(AdhesionHamiltonianConfig.class);
        
        try {
            Field cellField = AdhesionHamiltonianConfig.class.getDeclaredField("cell");
            cellField.setAccessible(true);
            cellField.set(config1, cell1);
            cellField.set(config2, cell2);
            cellField.set(config3, cell3);
        } catch (Exception ignored) { }
        
        doReturn(adhesions[1][0]).when(config1).getAdhesion(0);
        doReturn(adhesions[1][1]).when(config1).getAdhesion(1);
        doReturn(adhesions[1][2]).when(config1).getAdhesion(2);
        
        doReturn(adhesions[2][0]).when(config2).getAdhesion(0);
        doReturn(adhesions[2][1]).when(config2).getAdhesion(1);
        doReturn(adhesions[2][2]).when(config2).getAdhesion(2);
        
        doReturn(adhesions[1][0]).when(config3).getAdhesion(0);
        doReturn(adhesions[1][1]).when(config3).getAdhesion(1);
        doReturn(adhesions[1][2]).when(config3).getAdhesion(2);
        
        ah.configs.put(1, config1);
        ah.configs.put(2, config2);
        ah.configs.put(3, config3);
        
        assertEquals(adhesions[1][0] * 5 + adhesions[2][0] * 2,
                ah.getAdhesion(0, 2, 2, 0) * NEIGHBORHOOD_SIZE, EPSILON);
        assertEquals(adhesions[1][0] + adhesion * 2 + adhesions[1][1] * 2,
                ah.getAdhesion(1, 2, 2, 0) * NEIGHBORHOOD_SIZE, EPSILON);
        assertEquals(adhesions[2][0] * 1 + adhesion * 5,
                ah.getAdhesion(2, 2, 2, 0) * NEIGHBORHOOD_SIZE, EPSILON);
        assertEquals(adhesions[1][0] + adhesion * 2 + adhesions[1][1] * 3,
                ah.getAdhesion(3, 2, 2, 0) * NEIGHBORHOOD_SIZE, EPSILON);
    }
    
    @Test
    public void getAdhesion_validRegions_calculateValue() {
        Potts potts = makePottsMock();
        AdhesionHamiltonian2D ah = new AdhesionHamiltonian2D(mock(PottsSeries.class), potts);
        
        double[][] subadhesions = new double[][] {
                { Double.NaN, 1 },
                { 2, Double.NaN, },
        };
        
        int aa = REGION_DEFAULT - 1;
        int bb = REGION_NUCLEUS - 1;
        double subadhesion = (subadhesions[aa][bb] + subadhesions[bb][aa]) / 2;
        
        PottsCell cell = mock(PottsCell.class);
        AdhesionHamiltonianConfig config = mock(AdhesionHamiltonianConfig.class);
        try {
            Field cellField = AdhesionHamiltonianConfig.class.getDeclaredField("cell");
            cellField.setAccessible(true);
            cellField.set(config, cell);
        } catch (Exception ignored) { }
        
        doReturn(subadhesions[0][1]).when(config).getAdhesion(Region.DEFAULT, Region.NUCLEUS);
        doReturn(subadhesions[1][0]).when(config).getAdhesion(Region.NUCLEUS, Region.DEFAULT);
        
        ah.configs.put(1, config);
        
        assertEquals(subadhesion / NEIGHBORHOOD_SIZE, ah.getAdhesion(1, REGION_DEFAULT, 1, 2, 0), EPSILON);
        assertEquals(0, ah.getAdhesion(1, REGION_NUCLEUS, 2, 2, 0), EPSILON);
    }
}
