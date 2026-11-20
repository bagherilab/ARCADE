package arcade.potts.env.operation;

import org.junit.jupiter.api.Test;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.potts.env.lattice.PottsLattice;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PottsOperationDegraderTest {
    static PottsLattice ecm(int height, String enzyme, double rate) {
        MiniBox p = new MiniBox();
        p.put("degrader/ENZYME", enzyme);
        p.put("degrader/RATE", rate);
        return new PottsLattice(4, 4, height, 3, 1, p);
    }

    static PottsLattice ecm(String enzyme, double rate) {
        return ecm(1, enzyme, rate);
    }

    static PottsLattice enzymeLattice(int height, double value) {
        PottsLattice mmp = new PottsLattice(4, 4, height, 3, 1, new MiniBox());
        mmp.setField(value);
        return mmp;
    }

    static Simulation simWith(PottsLattice mmp) {
        Simulation sim = mock(Simulation.class);
        doReturn(mmp).when(sim).getLattice("MMP");
        return sim;
    }

    @Test
    public void step_uniformEnzyme_decaysExponentially() {
        PottsLattice target = ecm("MMP", 0.3);
        target.setField(5.0);
        new PottsOperationDegrader(target).step(null, simWith(enzymeLattice(1, 2.0)));
        assertEquals(5.0 * Math.exp(-0.3 * 2.0), target.getField()[0][1][1], 1E-12);
    }

    @Test
    public void step_zeroEnzyme_unchanged() {
        PottsLattice target = ecm("MMP", 0.3);
        target.setField(5.0);
        new PottsOperationDegrader(target).step(null, simWith(enzymeLattice(1, 0.0)));
        assertEquals(5.0, target.getField()[0][2][3], 1E-12);
    }

    @Test
    public void step_spatialEnzyme_degradesPerVoxel() {
        PottsLattice target = ecm("MMP", 1.0);
        target.setField(1.0);
        PottsLattice mmp = enzymeLattice(1, 0.0);
        mmp.getField()[0][0][0] = 1.0;
        new PottsOperationDegrader(target).step(null, simWith(mmp));
        assertEquals(Math.exp(-1), target.getField()[0][0][0], 1E-12);
        assertEquals(1.0, target.getField()[0][3][3], 1E-12);
    }

    @Test
    public void step_largeRateTimesEnzyme_staysPositive() {
        PottsLattice target = ecm("MMP", 1.0);
        target.setField(1.0);
        new PottsOperationDegrader(target).step(null, simWith(enzymeLattice(1, 5.0)));
        assertTrue(target.getField()[0][1][2] > 0);
        assertEquals(Math.exp(-5), target.getField()[0][1][2], 1E-15);
    }

    @Test
    public void step_enzymeChangesBetweenSteps_usesCurrentEnzyme() {
        PottsLattice target = ecm("MMP", 1.0);
        target.setField(1.0);
        PottsLattice mmp = enzymeLattice(1, 1.0);
        Simulation sim = simWith(mmp);
        PottsOperationDegrader degrader = new PottsOperationDegrader(target);
        degrader.step(null, sim);
        mmp.setField(2.0);
        degrader.step(null, sim);
        assertEquals(Math.exp(-1) * Math.exp(-2), target.getField()[0][0][0], 1E-15);
    }

    @Test
    public void step_threeDimensional_degradesAllPlanes() {
        PottsLattice target = ecm(3, "MMP", 0.5);
        target.setField(1.0);
        new PottsOperationDegrader(target).step(null, simWith(enzymeLattice(3, 2.0)));
        for (int k = 0; k < 3; k++) {
            assertEquals(Math.exp(-1), target.getField()[k][2][1], 1E-15);
        }
    }

    @Test
    public void constructor_missingOrNoneEnzyme_throws() {
        assertThrows(
                IllegalArgumentException.class, () -> new PottsOperationDegrader(ecm("NONE", 1)));
        MiniBox missing = new MiniBox();
        missing.put("degrader/RATE", 1.0);
        PottsLattice target = new PottsLattice(4, 4, 1, 3, 1, missing);
        assertThrows(IllegalArgumentException.class, () -> new PottsOperationDegrader(target));
    }

    @Test
    public void constructor_negativeRate_throws() {
        assertThrows(
                IllegalArgumentException.class, () -> new PottsOperationDegrader(ecm("MMP", -1)));
    }

    @Test
    public void constructor_missingRate_throws() {
        MiniBox missing = new MiniBox();
        missing.put("degrader/ENZYME", "MMP");
        PottsLattice target = new PottsLattice(4, 4, 1, 3, 1, missing);
        assertThrows(IllegalArgumentException.class, () -> new PottsOperationDegrader(target));
    }

    // Protected fraction (2026-11-18): the part of a voxel covered by the PROTECTED layer (mature,
    // crosslinked dermal collagen) degrades at PROTECTED_RATE instead of RATE.

    static PottsLattice protectedEcm(double rate, double protectedRate) {
        MiniBox p = new MiniBox();
        p.put("degrader/ENZYME", "MMP");
        p.put("degrader/RATE", rate);
        p.put("degrader/PROTECTED", "MATURE_ECM");
        p.put("degrader/PROTECTED_RATE", protectedRate);
        return new PottsLattice(4, 4, 1, 3, 1, p);
    }

    static Simulation simWith(PottsLattice mmp, PottsLattice mature) {
        Simulation sim = simWith(mmp);
        doReturn(mature).when(sim).getLattice("MATURE_ECM");
        return sim;
    }

    @Test
    public void step_protectedPart_degradesAtProtectedRate() {
        PottsLattice target = protectedEcm(1.0, 0.1);
        target.setField(1.5);
        PottsLattice mature = enzymeLattice(1, 1.0);
        new PottsOperationDegrader(target).step(null, simWith(enzymeLattice(1, 2.0), mature));
        double expected = 0.5 * Math.exp(-1.0 * 2.0) + 1.0 * Math.exp(-0.1 * 2.0);
        assertEquals(expected, target.getField()[0][1][1], 1E-12);
    }

    @Test
    public void step_protectedLayerAboveField_protectsOnlyTheField() {
        PottsLattice target = protectedEcm(1.0, 0.1);
        target.setField(0.4);
        PottsLattice mature = enzymeLattice(1, 1.0);
        new PottsOperationDegrader(target).step(null, simWith(enzymeLattice(1, 2.0), mature));
        assertEquals(0.4 * Math.exp(-0.1 * 2.0), target.getField()[0][2][2], 1E-12);
    }

    @Test
    public void step_noProtectedLayer_matchesPlainDegrader() {
        PottsLattice a = protectedEcm(1.0, 0.1);
        a.setField(1.0);
        PottsLattice mature = enzymeLattice(1, 0.0);
        new PottsOperationDegrader(a).step(null, simWith(enzymeLattice(1, 2.0), mature));
        assertEquals(Math.exp(-2.0), a.getField()[0][0][0], 1E-12);
    }

    @Test
    public void constructor_protectedWithoutValidRate_throws() {
        MiniBox p = new MiniBox();
        p.put("degrader/ENZYME", "MMP");
        p.put("degrader/RATE", 1.0);
        p.put("degrader/PROTECTED", "MATURE_ECM");
        p.put("degrader/PROTECTED_RATE", -1.0);
        assertThrows(
                IllegalArgumentException.class,
                () -> new PottsOperationDegrader(new PottsLattice(4, 4, 1, 3, 1, p)));
    }
}
