package arcade.potts.env.operation;

import org.junit.jupiter.api.Test;
import arcade.core.util.MiniBox;
import arcade.potts.env.lattice.PottsLattice;
import static org.junit.jupiter.api.Assertions.*;

public class PottsOperationDecayerTest {
    static PottsLattice lattice(int height, double rate) {
        MiniBox p = new MiniBox();
        p.put("decayer/DECAY_RATE", rate);
        return new PottsLattice(4, 4, height, 3, 1.0, p);
    }

    @Test
    public void step_manyTicks_decaysExactly() {
        PottsLattice l = lattice(1, 0.5);
        l.setField(2.0);
        PottsOperationDecayer d = new PottsOperationDecayer(l);
        for (int t = 0; t < 10; t++) {
            d.step(null, null);
        }
        double expected = 2.0 * Math.exp(-0.5 * 10);
        assertEquals(expected, l.getField()[0][1][2], 1E-12 * expected);
    }

    @Test
    public void step_rateAboveOne_staysPositive() {
        PottsLattice l = lattice(1, 1.5);
        l.setField(1.0);
        PottsOperationDecayer d = new PottsOperationDecayer(l);
        for (int t = 0; t < 5; t++) {
            d.step(null, null);
        }
        double expected = Math.exp(-7.5);
        assertTrue(l.getField()[0][0][0] > 0);
        assertEquals(expected, l.getField()[0][0][0], 1E-12 * expected);
    }

    @Test
    public void step_threeDimensional_decaysAllPlanes() {
        PottsLattice l = lattice(3, 1.0);
        l.setField(1.0);
        new PottsOperationDecayer(l).step(null, null);
        for (int k = 0; k < 3; k++) {
            assertEquals(Math.exp(-1), l.getField()[k][2][3], 1E-15);
        }
    }

    @Test
    public void constructor_invalidRate_throws() {
        assertThrows(
                IllegalArgumentException.class, () -> new PottsOperationDecayer(lattice(1, -1)));
        PottsLattice missing = new PottsLattice(3, 3, 1, 3, 1.0, new MiniBox());
        assertThrows(IllegalArgumentException.class, () -> new PottsOperationDecayer(missing));
    }
}
