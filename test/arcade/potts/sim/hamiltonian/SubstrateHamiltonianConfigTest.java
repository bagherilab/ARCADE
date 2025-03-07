package arcade.potts.sim.hamiltonian;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static arcade.core.ARCADETestUtilities.*;

public class SubstrateHamiltonianConfigTest {
    private static final double EPSILON = 1E-10;

    @Test
    public void getSubstrate_called_returnsValue() {
        double substrate = randomDoubleBetween(1, 100);
        SubstrateHamiltonianConfig shc = new SubstrateHamiltonianConfig(substrate);
        assertEquals(substrate, shc.getSubstrate(), EPSILON);
    }
}
