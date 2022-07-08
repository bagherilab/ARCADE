package arcade.potts.sim.hamiltonian;

import org.junit.Test;
import static org.junit.Assert.*;
import static arcade.core.ARCADETestUtilities.*;

public class JunctionHamiltonianConfigTest {
    private static final double EPSILON = 1E-10;
    
    @Test
    public void getLambda_called_returnsValue() {
        double lambda = randomDoubleBetween(1, 100);
        JunctionHamiltonianConfig jhc = new JunctionHamiltonianConfig(lambda);
        assertEquals(lambda, jhc.getLambda(), EPSILON);
    }
}
