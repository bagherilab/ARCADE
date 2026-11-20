package arcade.potts.sim.hamiltonian;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import arcade.potts.util.PottsEnums.Term;
import static org.junit.jupiter.api.Assertions.*;

public class VesselHamiltonianTest {
    private static final double EPSILON = 1E-10;

    private static final double LAMBDA_A = 3;

    private static final double LAMBDA_G = 2;

    // Voxels (1,1) and (2,1) are in the anchor; (5,5) is outside.
    private static Set<Long> anchor() {
        Set<Long> keys = new HashSet<>();
        keys.add(VesselHamiltonian.key(1, 1));
        keys.add(VesselHamiltonian.key(2, 1));
        return keys;
    }

    private static VesselHamiltonian makeTerm() {
        VesselHamiltonian h = new VesselHamiltonian();
        h.setAnchor(1, anchor(), LAMBDA_A);
        return h;
    }

    @Test
    public void gainOutside_costs() {
        assertEquals(LAMBDA_A, makeTerm().getDelta(0, 1, 5, 5, 0), EPSILON);
    }

    @Test
    public void gainInside_earns() {
        assertEquals(-LAMBDA_A, makeTerm().getDelta(0, 1, 1, 1, 0), EPSILON);
    }

    @Test
    public void loseInside_costs() {
        assertEquals(LAMBDA_A, makeTerm().getDelta(1, 0, 1, 1, 0), EPSILON);
    }

    @Test
    public void loseOutside_earns() {
        assertEquals(-LAMBDA_A, makeTerm().getDelta(1, 0, 5, 5, 0), EPSILON);
    }

    @Test
    public void guide_gainInside_earns() {
        VesselHamiltonian h = makeTerm();
        Set<Long> guide = new HashSet<>();
        guide.add(VesselHamiltonian.key(5, 5));
        h.setGuide(1, guide, LAMBDA_G);
        // Gain outside anchor (+A), inside guide (-G).
        assertEquals(LAMBDA_A - LAMBDA_G, h.getDelta(0, 1, 5, 5, 0), EPSILON);
        // Lose a guide voxel: -A (outside anchor) + G.
        assertEquals(-LAMBDA_A + LAMBDA_G, h.getDelta(1, 0, 5, 5, 0), EPSILON);
    }

    @Test
    public void noAnchor_zero() {
        VesselHamiltonian h = new VesselHamiltonian();
        assertEquals(0, h.getDelta(0, 1, 1, 1, 0), EPSILON);
        assertEquals(0, h.getDelta(1, 0, 1, 1, 0), EPSILON);
        assertEquals(0, h.getDelta(-1, 0, 1, 1, 0), EPSILON);
    }

    @Test
    public void clear_removesCell() {
        VesselHamiltonian h = makeTerm();
        h.clear(1);
        assertEquals(0, h.getDelta(0, 1, 5, 5, 0), EPSILON);
    }

    @Test
    public void bothCells_sum() {
        VesselHamiltonian h = makeTerm();
        Set<Long> other = new HashSet<>();
        other.add(VesselHamiltonian.key(9, 9));
        h.setAnchor(2, other, LAMBDA_A);
        // Cell 1 loses an anchor voxel (+A), cell 2 gains it outside its anchor (+A).
        assertEquals(2 * LAMBDA_A, h.getDelta(1, 2, 1, 1, 0), EPSILON);
    }

    @Test
    public void tipGainsVesselVoxel_forbidden() {
        VesselHamiltonian h = makeTerm();
        h.setVessel(1);
        h.setTip(2);
        // Inside or outside the vessel cell's anchor, the flip is impossible.
        assertEquals(Double.POSITIVE_INFINITY, h.getDelta(1, 2, 1, 1, 0));
        assertEquals(Double.POSITIVE_INFINITY, h.getDelta(1, 2, 5, 5, 0));
    }

    @Test
    public void vesselGainsTipVoxel_normalRule() {
        VesselHamiltonian h = makeTerm();
        h.setVessel(1);
        h.setTip(2);
        // Cell 1 gains an anchor voxel (-A); the tip has no anchor.
        assertEquals(-LAMBDA_A, h.getDelta(2, 1, 1, 1, 0), EPSILON);
        assertEquals(LAMBDA_A, h.getDelta(2, 1, 5, 5, 0), EPSILON);
    }

    @Test
    public void otherPairs_unchanged() {
        VesselHamiltonian h = makeTerm();
        h.setVessel(1);
        h.setVessel(3);
        h.setTip(2);
        h.setTip(4);
        // Medium or an unmarked cell to a tip, tip to tip, vessel to vessel, tip to medium.
        assertEquals(0, h.getDelta(0, 2, 5, 5, 0), EPSILON);
        assertEquals(0, h.getDelta(7, 2, 5, 5, 0), EPSILON);
        assertEquals(0, h.getDelta(4, 2, 5, 5, 0), EPSILON);
        assertEquals(-LAMBDA_A, h.getDelta(1, 3, 5, 5, 0), EPSILON);
        assertEquals(0, h.getDelta(2, 0, 5, 5, 0), EPSILON);
        // A vessel cell gaining from medium follows the anchor.
        assertEquals(-LAMBDA_A, h.getDelta(0, 1, 1, 1, 0), EPSILON);
    }

    @Test
    public void marks_clearKeepsForgetRemoves() {
        VesselHamiltonian h = makeTerm();
        h.setVessel(1);
        h.setTip(2);
        h.clear(1);
        assertEquals(Double.POSITIVE_INFINITY, h.getDelta(1, 2, 5, 5, 0));
        h.forget(1);
        assertEquals(0, h.getDelta(1, 2, 5, 5, 0), EPSILON);
        // A tip marked as a vessel cell (it joined) is no longer a tip.
        h.setVessel(4);
        h.setVessel(2);
        assertEquals(0, h.getDelta(4, 2, 5, 5, 0), EPSILON);
    }

    @Test
    public void key_distinctVoxels_distinctKeys() {
        Set<Long> keys = new HashSet<>();
        for (int x = 0; x < 50; x++) {
            for (int y = 0; y < 50; y++) {
                assertTrue(keys.add(VesselHamiltonian.key(x, y)));
            }
        }
    }

    @Test
    public void regionDelta_zero() {
        assertEquals(0, makeTerm().getDelta(1, 1, 2, 1, 1, 0), EPSILON);
    }

    @Test
    public void termParses_vessel() {
        assertEquals(Term.VESSEL, Term.valueOf("VESSEL"));
        assertEquals(Term.VESSEL, Term.valueOf("vessel".toUpperCase()));
    }
}
