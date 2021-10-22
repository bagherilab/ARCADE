package arcade.potts.sim.hamiltonian;

import org.junit.Test;
import arcade.core.util.Enums;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.sim.Potts;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.util.Enums.Region;
import static arcade.potts.sim.hamiltonian.AdhesionHamiltonian2D.NEIGHBORHOOD_SIZE;

public class AdhesionHamiltonian2DTest {
    private static final double EPSILON = 1E-10;
    static final int REGION_DEFAULT = Region.DEFAULT.ordinal();
    static final int REGION_NUCLEUS = Region.NUCLEUS.ordinal();
    static final double[][] ADHESIONS = new double[][] {
            { Double.NaN, Double.NaN, Double.NaN },
            { 1, 2, 3 },
            { 4, 5, 6 }
    };
    static final double[][] SUBADHESIONS = new double[][] {
            { Double.NaN, 1 },
            { 2, Double.NaN, },
    };
    
    static Potts makePottsMock() {
        Potts potts = mock(Potts.class);
        
        int nCells = 3;
        int nSubcells = 2;
        int[] pops = new int[] { 1, 2, 1 };
        
        doReturn(null).when(potts).getCell(0);
        
        for (int i = 0; i < nCells; i++) {
            PottsCell c = mock(PottsCell.class);
            when(c.getPop()).thenReturn(pops[i]);
            
            // Assign adhesion values for cells.
            for (int j = 0; j < nCells; j++) {
                when(c.getAdhesion(j)).thenReturn(ADHESIONS[pops[i]][j]);
            }
            
            // Assign adhesion values for subcellular domain.
            for (int j = 0; j < nSubcells; j++) {
                for (int k = 0; k < nSubcells; k++) {
                    Enums.Region region1 = Enums.Region.values()[j + 1];
                    Enums.Region region2 = Enums.Region.values()[k + 1];
                    when(c.getAdhesion(region1, region2)).thenReturn(SUBADHESIONS[k][j]);
                }
            }
            
            doReturn(c).when(potts).getCell(i + 1);
        }
        
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
        AdhesionHamiltonian2D ah = new AdhesionHamiltonian2D(potts);
        double adhesion = (ADHESIONS[1][2] + ADHESIONS[2][1]) / 2;
        
        assertEquals(ADHESIONS[1][0] * 5 + ADHESIONS[2][0] * 2,
                ah.getAdhesion(0, 2, 2, 0) * NEIGHBORHOOD_SIZE, EPSILON);
        assertEquals(ADHESIONS[1][0] + adhesion * 2 + ADHESIONS[1][1] * 2,
                ah.getAdhesion(1, 2, 2, 0) * NEIGHBORHOOD_SIZE, EPSILON);
        assertEquals(ADHESIONS[2][0] * 1 + adhesion * 5,
                ah.getAdhesion(2, 2, 2, 0) * NEIGHBORHOOD_SIZE, EPSILON);
        assertEquals(ADHESIONS[1][0] + adhesion * 2 + ADHESIONS[1][1] * 3,
                ah.getAdhesion(3, 2, 2, 0) * NEIGHBORHOOD_SIZE, EPSILON);
    }
    
    @Test
    public void getAdhesion_validRegions_calculateValue() {
        Potts potts = makePottsMock();
        AdhesionHamiltonian2D ah = new AdhesionHamiltonian2D(potts);
        
        int aa = REGION_DEFAULT - 1;
        int bb = REGION_NUCLEUS - 1;
        double subadhesion = (SUBADHESIONS[aa][bb] + SUBADHESIONS[bb][aa]) / 2;
        
        assertEquals(subadhesion / NEIGHBORHOOD_SIZE, ah.getAdhesion(1, REGION_DEFAULT, 1, 2, 0), EPSILON);
        assertEquals(0, ah.getAdhesion(1, REGION_NUCLEUS, 2, 2, 0), EPSILON);
    }
}
