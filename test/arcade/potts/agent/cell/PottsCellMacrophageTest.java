package arcade.potts.agent.cell;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static arcade.potts.util.PottsEnums.State;

public class PottsCellMacrophageTest {
    private PottsCellWoundHealingFixture f;

    @BeforeEach
    public final void setUp() {
        f = new PottsCellWoundHealingFixture();
    }

    @Test
    public void canProliferate_highPdgf_returnsFalse() {
        f.means.put("PDGF", 10.0);
        PottsCellMacrophage cell =
                new PottsCellMacrophage(
                        f.container(State.QUIESCENT), f.location, f.parameters(), null);
        assertFalse(cell.canProliferate(f.sim));
    }

    @Test
    public void stepRules_quiescent_staysQuiescent() {
        PottsCellMacrophage cell =
                new PottsCellMacrophage(
                        f.container(State.QUIESCENT), f.location, f.parameters(), null);
        assertTrue(cell.stepRules(f.random, f.sim));
        assertEquals(State.QUIESCENT, cell.getState());
    }
}
