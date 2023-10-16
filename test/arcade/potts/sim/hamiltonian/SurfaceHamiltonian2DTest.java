package arcade.potts.sim.hamiltonian;

import org.junit.Test;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSeries;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.potts.util.PottsEnums.Region;

public class SurfaceHamiltonian2DTest {
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
    public void calculateChange_validIDs_calculatesValue() {
        Potts potts = makePottsMock();
        SurfaceHamiltonian2D sh = new SurfaceHamiltonian2D(mock(PottsSeries.class), potts);
        
        assertArrayEquals(new int[] { 0, 2 }, sh.calculateChange(1, 2, 2, 2, 0));
        assertArrayEquals(new int[] { 0, 2 }, sh.calculateChange(1, 3, 2, 2, 0));
    }
    
    @Test
    public void calculateChange_validRegions_calculatesValue() {
        Potts potts = makePottsMock();
        SurfaceHamiltonian2D sh = new SurfaceHamiltonian2D(mock(PottsSeries.class), potts);
        
        assertArrayEquals(new int[] { -4, 2 }, sh.calculateChange(1, REGION_NUCLEUS, REGION_DEFAULT, 2, 2, 0));
        assertArrayEquals(new int[] { -2, 2 }, sh.calculateChange(1, REGION_DEFAULT, REGION_NUCLEUS, 2, 1, 0));
    }
}
