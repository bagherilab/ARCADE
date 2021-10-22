package arcade.potts.sim.hamiltonian;

import org.junit.Test;
import arcade.potts.sim.Potts;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;

public class AdhesionHamiltonianTest {
    private static final double EPSILON = 1E-10;
    private static final double[] ADHESION_ID = {
            0,
            randomDoubleBetween(0, 10),
            randomDoubleBetween(0, 10)
    };
    private static final double[] ADHESION_REGION = {
            0,
            randomDoubleBetween(0, 10),
            randomDoubleBetween(0, 10)
    };
    
    static class AdhesionHamiltonianMock extends AdhesionHamiltonian {
        AdhesionHamiltonianMock(Potts potts) { super(potts); }
        
        @Override
        double getAdhesion(int id, int x, int y, int z) { return ADHESION_ID[id]; }
        
        @Override
        double getAdhesion(int id, int region, int x, int y, int z) { return ADHESION_REGION[region]; }
    }
    
    @Test
    public void constructor_called_setsObject() {
        Potts potts = mock(Potts.class);
        AdhesionHamiltonianMock ahm = new AdhesionHamiltonianMock(potts);
        assertEquals(potts, ahm.potts);
    }
    
    @Test
    public void getDelta_validIDs_calculatesValue() {
        AdhesionHamiltonianMock ahm = new AdhesionHamiltonianMock(null);
        assertEquals(ADHESION_ID[1] - ADHESION_ID[2], ahm.getDelta(2, 1, 0, 0, 0), EPSILON);
        assertEquals(ADHESION_ID[2] - ADHESION_ID[1], ahm.getDelta(1, 2, 0, 0, 0), EPSILON);
    }
    
    @Test
    public void getDelta_validRegions_calculatesValue() {
        AdhesionHamiltonianMock ahm = new AdhesionHamiltonianMock(null);
        assertEquals(ADHESION_REGION[1] - ADHESION_REGION[2], ahm.getDelta(1, 2, 1, 0, 0, 0), EPSILON);
        assertEquals(ADHESION_REGION[2] - ADHESION_REGION[1], ahm.getDelta(1, 1, 2, 0, 0, 0), EPSILON);
    }
}
