package arcade.potts.env.operation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import arcade.core.util.MiniBox;
import arcade.potts.env.lattice.PottsLattice;
import arcade.potts.env.location.Voxel;
import static org.junit.jupiter.api.Assertions.*;

public class PottsOperationDiffuserTest {
    static MiniBox params(double d, double maxSubstep) {
        MiniBox p = new MiniBox();
        p.put("diffuser/DIFFUSIVITY", d);
        p.put("diffuser/MAX_SUBSTEP", maxSubstep);
        return p;
    }

    // Diffuser parameters plus a decayer rate [1/hour], converted to per tick.
    static MiniBox params(double d, double maxSubstep, double decayPerHour, double dtHours) {
        MiniBox p = params(d, maxSubstep);
        p.put("decayer/DECAY_RATE", decayPerHour * dtHours);
        return p;
    }

    static PottsLattice lattice(int length, int width, double ds, double dtHours, MiniBox p) {
        return new PottsLattice(length, width, 1, ds, dtHours, p);
    }

    static double total(double[][] f) {
        double sum = 0;
        for (double[] row : f) {
            for (double v : row) {
                sum += v;
            }
        }
        return sum;
    }

    @Test
    public void constructor_exactDivision_usesExactSubstepCount() {
        PottsLattice l = lattice(5, 5, 3, 1.0 / 60, params(10, 30));
        PottsOperationDiffuser d = new PottsOperationDiffuser(l);
        assertEquals(2, d.substeps);
        assertEquals(30.0, d.substep, 1E-12);
    }

    @Test
    public void constructor_invalidParameters_throws() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PottsOperationDiffuser(lattice(3, 3, 3, 1, params(-1, 30))));
        assertThrows(
                IllegalArgumentException.class,
                () -> new PottsOperationDiffuser(lattice(3, 3, 3, 1, params(1, 0))));
        MiniBox missing = new MiniBox();
        missing.put("diffuser/DIFFUSIVITY", 1.0);
        assertThrows(
                IllegalArgumentException.class,
                () -> new PottsOperationDiffuser(lattice(3, 3, 3, 1, missing)));
    }

    @Test
    public void constructor_threeDimensional_throws() {
        PottsLattice l = new PottsLattice(3, 3, 3, 3, 1, params(1, 30));
        assertThrows(UnsupportedOperationException.class, () -> new PottsOperationDiffuser(l));
    }

    @Test
    public void step_conservesMass() {
        PottsLattice l = lattice(40, 30, 3, 1.0 / 60, params(50, 30));
        Random rng = new Random(1);
        for (int i = 0; i < 40; i++) {
            for (int j = 0; j < 30; j++) {
                l.getField()[0][i][j] = rng.nextDouble();
            }
        }
        double before = total(l.getField()[0]);
        PottsOperationDiffuser d = new PottsOperationDiffuser(l);
        for (int t = 0; t < 100; t++) {
            d.step(null, null);
        }
        assertEquals(before, total(l.getField()[0]), 1E-10 * before);
    }

    @Test
    public void step_spikeWithLargeRate_staysNonNegative() {
        PottsLattice l = lattice(31, 31, 1, 1.0, params(1000, 60));
        l.getField()[0][15][15] = 1;
        PottsOperationDiffuser d = new PottsOperationDiffuser(l);
        for (int t = 0; t < 5; t++) {
            d.step(null, null);
            for (double[] row : l.getField()[0]) {
                for (double v : row) {
                    assertTrue(v >= 0, "negative concentration " + v);
                }
            }
        }
    }

    @Test
    public void step_tickLength_doesNotChangeResult() {
        PottsLattice minute = lattice(30, 20, 3, 1.0 / 60, params(20, 30));
        PottsLattice hour = lattice(30, 20, 3, 1.0, params(20, 30));
        Random rng = new Random(2);
        for (int i = 0; i < 30; i++) {
            for (int j = 0; j < 20; j++) {
                double v = rng.nextDouble();
                minute.getField()[0][i][j] = v;
                hour.getField()[0][i][j] = v;
            }
        }
        PottsOperationDiffuser dm = new PottsOperationDiffuser(minute);
        PottsOperationDiffuser dh = new PottsOperationDiffuser(hour);
        for (int t = 0; t < 60; t++) {
            dm.step(null, null);
        }
        dh.step(null, null);
        for (int i = 0; i < 30; i++) {
            for (int j = 0; j < 20; j++) {
                assertEquals(hour.getField()[0][i][j], minute.getField()[0][i][j], 1E-12);
            }
        }
    }

    @Test
    public void step_withDecayer_matchesDecayWithinSubsteps() {
        double dt = 0.25;
        double decayPerHour = 6;
        PottsLattice split = lattice(20, 15, 3, dt, params(0.6, 2, decayPerHour, dt));
        PottsLattice manual = lattice(20, 15, 3, dt, params(0.6, 2, decayPerHour, dt));
        Random rng = new Random(3);
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 15; j++) {
                double v = rng.nextDouble();
                split.getField()[0][i][j] = v;
                manual.getField()[0][i][j] = v;
            }
        }

        new PottsOperationDiffuser(split).step(null, null);
        new PottsOperationDecayer(split).step(null, null);

        PottsOperationDiffuser d = new PottsOperationDiffuser(manual);
        double perSubstep = Math.exp(-decayPerHour / 3600 * d.substep);
        double[][] f = manual.getField()[0];
        for (int s = 0; s < d.substeps; s++) {
            d.diffuseSubstep(f);
            for (double[] row : f) {
                for (int j = 0; j < row.length; j++) {
                    row[j] *= perSubstep;
                }
            }
        }

        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 15; j++) {
                double expected = f[i][j];
                assertEquals(expected, split.getField()[0][i][j], 1E-12 * expected);
            }
        }
    }

    // Runs a point source for one hour and returns the center and 28-voxel values.
    static double[] pointSource(double maxSubstep) {
        int n = 161;
        PottsLattice l = lattice(n, n, 3, 1.0, params(1, maxSubstep));
        l.getField()[0][80][80] = 1;
        new PottsOperationDiffuser(l).step(null, null);
        return new double[] {l.getField()[0][80][80], l.getField()[0][108][80]};
    }

    @Test
    public void step_pointSource_matchesGaussianAndConvergesInTime() {
        double ds = 3;
        double d = 1;
        double t = 3600;
        double r = 28 * ds;
        double amp = ds * ds / (4 * Math.PI * d * t);
        double centerExact = amp;
        double offsetExact = amp * Math.exp(-r * r / (4 * d * t));

        double[] fine = pointSource(6);
        double[] coarse = pointSource(60);
        double[] reference = pointSource(0.5);

        assertEquals(centerExact, fine[0], 0.02 * centerExact);
        assertEquals(offsetExact, fine[1], 0.02 * offsetExact);

        double fineTimeError = Math.abs(fine[0] - reference[0]);
        double coarseTimeError = Math.abs(coarse[0] - reference[0]);
        assertTrue(fineTimeError < coarseTimeError);
    }

    // Holds x = 0 at 1, runs to steady state, returns measured decay length [um].
    static double measureLambda(double lambda, double decayPerHour, double dtHours, int ticks) {
        double ds = 3;
        double kPerSecond = decayPerHour / 3600;
        double d = lambda * lambda * kPerSecond;
        int n = (int) Math.ceil(8 * lambda / ds) + 1;
        PottsLattice l = lattice(n, 3, ds, dtHours, params(d, 30, decayPerHour, dtHours));
        PottsOperationDiffuser diffuser = new PottsOperationDiffuser(l);
        PottsOperationDecayer decayer = new PottsOperationDecayer(l);
        double[][] f = l.getField()[0];
        for (int t = 0; t < ticks; t++) {
            for (int j = 0; j < 3; j++) {
                f[0][j] = 1;
            }
            diffuser.step(null, null);
            decayer.step(null, null);
        }
        int i1 = (int) Math.round(lambda / ds);
        int i2 = (int) Math.round(2 * lambda / ds);
        return (i2 - i1) * ds / Math.log(f[i1][1] / f[i2][1]);
    }

    @Test
    public void step_lineSourceWithDecay_reachesExpectedLengthScale() {
        // k = 1/hour; 12 hours of 1-minute ticks reaches steady state (e^-12).
        for (double lambda : new double[] {10, 30, 100, 300}) {
            double measured = measureLambda(lambda, 1.0, 1.0 / 60, 720);
            assertEquals(lambda, measured, 0.02 * lambda, "lambda = " + lambda);
        }
    }

    // Mean over the central 2x2 block after 60 ticks of a pulsed ring source around it.
    static double pulsedRingBlockMean(double maxSubstep) {
        double d = 10;
        double lambda = 100;
        double dt = 1.0 / 60;
        PottsLattice l =
                lattice(101, 101, 3, dt, params(d, maxSubstep, 3600 * d / (lambda * lambda), dt));
        PottsOperationDiffuser diffuser = new PottsOperationDiffuser(l);
        PottsOperationDecayer decayer = new PottsOperationDecayer(l);
        List<Voxel> ring = new ArrayList<>();
        for (int x = 49; x <= 52; x++) {
            for (int y = 49; y <= 52; y++) {
                boolean inside = x >= 50 && x <= 51 && y >= 50 && y <= 51;
                if (!inside) {
                    ring.add(new Voxel(x, y, 0));
                }
            }
        }
        // 4x4 minus 2x2 leaves 12 voxels; keep the 8 edge-adjacent ones.
        ring.removeIf(v -> (v.x == 49 || v.x == 52) && (v.y == 49 || v.y == 52));
        assertEquals(8, ring.size());
        double[][] f = l.getField()[0];
        for (int t = 0; t < 60; t++) {
            for (Voxel v : ring) {
                f[v.x][v.y] += 1.0;
            }
            diffuser.step(null, null);
            decayer.step(null, null);
        }
        return (f[50][50] + f[50][51] + f[51][50] + f[51][51]) / 4.0;
    }

    @Test
    public void step_pulsedSourceNearCell_defaultSubstepIsAccurate() {
        double reference = pulsedRingBlockMean(0.1);
        double fine = Math.abs(pulsedRingBlockMean(2) - reference) / reference;
        double coarse = Math.abs(pulsedRingBlockMean(30) - reference) / reference;
        assertTrue(fine < 0.03, "2 s error " + fine);
        assertTrue(coarse > fine, "30 s error " + coarse + " should exceed 2 s error " + fine);
    }
}
