package arcade.potts.agent.cell;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import arcade.potts.env.location.Voxel;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.potts.util.PottsEnums.State;

public class PottsCellTissueTest {
    private PottsCellWoundHealingFixture f;

    @BeforeEach
    public final void setUp() {
        f = new PottsCellWoundHealingFixture();
    }

    // Creates a tissue cell in the given state.
    private PottsCellTissue make(State state) {
        return new PottsCellTissue(f.container(state), f.location, f.parameters(), null);
    }

    @Test
    public void canProliferate_freeSpaceNoGate_returnsTrue() {
        assertTrue(make(State.QUIESCENT).canProliferate(f.sim));
    }

    @Test
    public void canProliferate_noFreeSpace_returnsFalse() {
        f.surround(8);
        assertFalse(make(State.QUIESCENT).canProliferate(f.sim));
    }

    @Test
    public void canProliferate_contactInhibited_returnsFalse() {
        f.box.put("CONTACT_FREE_FRACTION", 0.5);
        f.addNeighbor(8, 1, 2, mock(PottsCell.class));
        f.addNeighbor(8, 3, 2, mock(PottsCell.class));
        f.addNeighbor(8, 2, 1, mock(PottsCell.class));
        assertFalse(make(State.QUIESCENT).canProliferate(f.sim));
    }

    @Test
    public void canProliferate_ecmInFreeSpaceAboveGate_returnsTrue() {
        f.box.put("ECM_PROLIFERATION", 0.5);
        f.ecm[0][1][2] = 1;
        f.ecm[0][3][2] = 1;
        assertTrue(make(State.QUIESCENT).canProliferate(f.sim));
    }

    @Test
    public void canProliferate_ecmInFreeSpaceBelowGate_returnsFalse() {
        f.box.put("ECM_PROLIFERATION", 0.5);
        f.ecm[0][1][2] = 1;
        assertFalse(make(State.QUIESCENT).canProliferate(f.sim));
    }

    @Test
    public void canProliferate_ecmOnlyUnderCell_returnsFalse() {
        f.box.put("ECM_PROLIFERATION", 0.5);
        f.ecm[0][2][2] = 10;
        assertFalse(make(State.QUIESCENT).canProliferate(f.sim));
    }

    @Test
    public void canProliferate_atBorder_doesNotCountBorderAsFree() {
        f.box.put("CONTACT_FREE_FRACTION", 0.75);
        f.setVoxels(new Voxel(1, 1, 0));
        assertFalse(make(State.QUIESCENT).canProliferate(f.sim));
    }

    @Test
    public void stepRules_quiescentCanProliferate_becomesProliferative() {
        PottsCellTissue cell = make(State.QUIESCENT);
        assertTrue(cell.stepRules(f.random, f.sim));
        assertEquals(State.PROLIFERATIVE, cell.getState());
    }

    @Test
    public void stepRules_spontaneousSenescence_becomesSenescent() {
        f.box.put("SPONTANEOUS_SENESCENCE", 0.5);
        PottsCellTissue cell = make(State.QUIESCENT);
        assertTrue(cell.stepRules(f.random, f.sim));
        assertEquals(State.SENESCENT, cell.getState());
    }

    @Test
    public void stepRules_bystanderContact_becomesSenescent() {
        f.box.put("BYSTANDER_CONTACT", 0.5);
        PottsCell neighbor = mock(PottsCell.class);
        doReturn(State.SENESCENT).when(neighbor).getState();
        f.addNeighbor(8, 1, 2, neighbor);
        PottsCellTissue cell = make(State.QUIESCENT);
        assertTrue(cell.stepRules(f.random, f.sim));
        assertEquals(State.SENESCENT, cell.getState());
    }
}
