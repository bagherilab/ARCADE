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

    // Max |A x - b| / max |b| for A = tridiag(-r, 1 + 2r, -r) with 1 + r at both ends.
    static double tridiagonalResidual(double[] x, double[] b, double r) {
        int n = x.length;
        double worst = 0;
        double scale = 0;
        for (int i = 0; i < n; i++) {
            double diag = (n == 1) ? 1 : (i == 0 || i == n - 1) ? 1 + r : 1 + 2 * r;
            double ax = diag * x[i];
            if (i > 0) {
                ax -= r * x[i - 1];
            }
            if (i < n - 1) {
                ax -= r * x[i + 1];
            }
            worst = Math.max(worst, Math.abs(ax - b[i]));
            scale = Math.max(scale, Math.abs(b[i]));
        }
        return worst / scale;
    }

    @Test
    public void diffuseSubstep_singleDirection_solvesTridiagonalSystem() {
        // A field that varies along one axis only is unchanged by the other axis solve, so one
        // substep must solve exactly the 1D system along the varying axis.
        Random rng = new Random(7);
        for (int n : new int[] {1, 2, 3, 10, 61}) {
            for (double d : new double[] {0.01, 1, 100, 1000}) {
                double[] b = new double[n];
                for (int i = 0; i < n; i++) {
                    b[i] = rng.nextDouble();
                }

                PottsLattice alongX = lattice(n, 3, 3, 2.0 / 3600, params(d, 2));
                PottsOperationDiffuser opX = new PottsOperationDiffuser(alongX);
                double[][] fx = alongX.getField()[0];
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < 3; j++) {
                        fx[i][j] = b[i];
                    }
                }
                opX.diffuseSubstep(fx);
                double[] x = new double[n];
                for (int i = 0; i < n; i++) {
                    x[i] = fx[i][1];
                }
                assertTrue(tridiagonalResidual(x, b, opX.r) < 1E-12, "x, n=" + n + ", D=" + d);

                PottsLattice alongY = lattice(3, n, 3, 2.0 / 3600, params(d, 2));
                PottsOperationDiffuser opY = new PottsOperationDiffuser(alongY);
                double[][] fy = alongY.getField()[0];
                for (int i = 0; i < 3; i++) {
                    fy[i] = b.clone();
                }
                opY.diffuseSubstep(fy);
                assertTrue(tridiagonalResidual(fy[1], b, opY.r) < 1E-12, "y, n=" + n + ", D=" + d);
            }
        }
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

    // Diffuser parameters plus a half-saturation constant for uptake.
    static MiniBox uptakeParams(double d, double maxSubstep, double halfSaturation) {
        MiniBox p = params(d, maxSubstep);
        p.put("diffuser/HALF_SATURATION", halfSaturation);
        return p;
    }

    static PottsLattice randomLattice(int length, int width, MiniBox p, long seed) {
        PottsLattice l = lattice(length, width, 3, 1.0 / 60, p);
        Random rng = new Random(seed);
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < width; j++) {
                l.getField()[0][i][j] = rng.nextDouble();
            }
        }
        return l;
    }

    @Test
    public void step_noSink_matchesPrecomputedPath() {
        PottsLattice withK = randomLattice(7, 5, uptakeParams(10, 2, 0.5), 3);
        PottsLattice withoutK = randomLattice(7, 5, params(10, 2), 3);
        PottsOperationDiffuser a = new PottsOperationDiffuser(withK);
        PottsOperationDiffuser b = new PottsOperationDiffuser(withoutK);

        a.step(null, null);
        for (int s = 0; s < b.substeps; s++) {
            b.diffuseSubstep(withoutK.getField()[0]);
        }

        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 5; j++) {
                assertEquals(withoutK.getField()[0][i][j], withK.getField()[0][i][j], 0);
            }
        }
    }

    @Test
    public void step_sinkWithNonPositiveHalfSaturation_throws() {
        PottsLattice zero = lattice(3, 3, 3, 1.0 / 60, uptakeParams(10, 2, 0));
        zero.addSink(List.of(new Voxel(1, 1, 0)), 1.0);
        PottsLattice missing = lattice(3, 3, 3, 1.0 / 60, params(10, 2));
        missing.addSink(List.of(new Voxel(1, 1, 0)), 1.0);

        assertThrows(
                IllegalArgumentException.class,
                () -> new PottsOperationDiffuser(zero).step(null, null));
        assertThrows(
                IllegalArgumentException.class,
                () -> new PottsOperationDiffuser(missing).step(null, null));
    }

    static List<Voxel> allVoxels(int length, int width) {
        List<Voxel> all = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < width; j++) {
                all.add(new Voxel(i, j, 0));
            }
        }
        return all;
    }

    // Steady diffuser parameters at the O2 settings.
    static MiniBox steadyParams(double maxSubstep, double tolerance, int maxIterations) {
        MiniBox p = uptakeParams(2000, maxSubstep, 0.76);
        p.put("diffuser/STEADY", 1);
        p.put("diffuser/STEADY_TOLERANCE", tolerance);
        p.put("diffuser/STEADY_MAX_ITERATIONS", maxIterations);
        return p;
    }

    // A 2D test case: supply on the left half, uptake in about half of all voxels.
    static PottsLattice steadyCase(MiniBox p) {
        int length = 60;
        int width = 16;
        PottsLattice l = lattice(length, width, 3, 0.25, p);
        Random rng = new Random(2);
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < width; j++) {
                if (i < length / 2) {
                    l.addExchange(List.of(new Voxel(i, j, 0)), 0.5, 60);
                }
                if (rng.nextDouble() < 0.45) {
                    l.addSink(List.of(new Voxel(i, j, 0)), 37.7);
                }
            }
        }
        return l;
    }

    // Largest residual of D lap(c) + P (T - c) - V c / (K + c) = 0 [layer units/s].
    static double steadyResidual(PottsLattice l, double d, double k) {
        double[][] f = l.getField()[0];
        double[][] v = l.getSink()[0];
        double[][] p = l.getExchange()[0];
        double[][] pt = l.getExchangeTarget()[0];
        int length = f.length;
        int width = f[0].length;
        double max = 0;
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < width; j++) {
                double c = f[i][j];
                double lap =
                        (i > 0 ? f[i - 1][j] : c)
                                + (i < length - 1 ? f[i + 1][j] : c)
                                + (j > 0 ? f[i][j - 1] : c)
                                + (j < width - 1 ? f[i][j + 1] : c)
                                - 4 * c;
                double residual = d * lap + pt[i][j] - p[i][j] * c - v[i][j] * c / (k + c);
                max = Math.max(max, Math.abs(residual));
            }
        }
        return max;
    }

    @Test
    public void step_steady_satisfiesSteadyEquation() {
        PottsLattice l = steadyCase(steadyParams(30, 1E-6, 2000));
        new PottsOperationDiffuser(l).step(null, null);

        // Terms are up to ~30 mmHg/s; the residual is a tiny fraction of that.
        assertTrue(steadyResidual(l, 2000 / 9.0, 0.76) < 1E-3);
        double[][] f = l.getField()[0];
        assertTrue(f[0][0] > f[59][0]);
    }

    @Test
    public void step_steady_doesNotDependOnSubstep() {
        PottsLattice coarse = steadyCase(steadyParams(30, 1E-6, 2000));
        PottsLattice fine = steadyCase(steadyParams(0.5, 1E-6, 2000));
        new PottsOperationDiffuser(coarse).step(null, null);
        new PottsOperationDiffuser(fine).step(null, null);
        for (int i = 0; i < 60; i++) {
            for (int j = 0; j < 16; j++) {
                assertEquals(fine.getField()[0][i][j], coarse.getField()[0][i][j], 1E-4);
            }
        }
    }

    @Test
    public void step_steadyFromPreviousSolution_needsFewerIterations() {
        PottsLattice l = steadyCase(steadyParams(30, 1E-3, 2000));
        PottsOperationDiffuser d = new PottsOperationDiffuser(l);
        d.step(null, null);
        int cold = d.lastIterations;
        d.step(null, null);
        int warm = d.lastIterations;
        assertTrue(warm < cold, "warm " + warm + " cold " + cold);
    }

    @Test
    public void step_steadyNotConverged_throws() {
        PottsLattice l = steadyCase(steadyParams(30, 1E-9, 2));
        PottsOperationDiffuser d = new PottsOperationDiffuser(l);
        assertThrows(IllegalStateException.class, () -> d.step(null, null));
    }

    @Test
    public void step_steadyWithoutReactions_stepsInTime() {
        PottsLattice steady = randomLattice(7, 5, steadyParams(2, 1E-3, 100), 3);
        PottsLattice timed = randomLattice(7, 5, uptakeParams(2000, 2, 0.76), 3);
        new PottsOperationDiffuser(steady).step(null, null);
        new PottsOperationDiffuser(timed).step(null, null);
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 5; j++) {
                assertEquals(timed.getField()[0][i][j], steady.getField()[0][i][j], 0);
            }
        }
    }

    @Test
    public void constructor_steadyInvalidTolerance_throws() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PottsOperationDiffuser(lattice(3, 3, 3, 1, steadyParams(30, 0, 100))));
    }

    // Steady parameters with the given diffusivity.
    static MiniBox steadyParams(double d) {
        MiniBox p = uptakeParams(d, 30, 0.76);
        p.put("diffuser/STEADY", 1);
        p.put("diffuser/STEADY_TOLERANCE", 1E-8);
        p.put("diffuser/STEADY_MAX_ITERATIONS", 2000);
        return p;
    }

    // Steady state of P (T - c) = V c / (K + c), solved as a quadratic.
    static double localSteady(double rate, double target, double vmax, double k) {
        double b = rate * target - rate * k - vmax;
        return (b + Math.sqrt(b * b + 4 * rate * rate * target * k)) / (2 * rate);
    }

    @Test
    public void step_reactionWithoutSteady_throws() {
        PottsLattice sink = lattice(3, 3, 3, 1.0 / 60, uptakeParams(10, 2, 1));
        sink.addSink(List.of(new Voxel(1, 1, 0)), 1.0);
        PottsLattice exchange = lattice(3, 3, 3, 1.0 / 60, params(10, 2));
        exchange.addExchange(List.of(new Voxel(1, 1, 0)), 0.5, 60);

        Exception e =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new PottsOperationDiffuser(sink).step(null, null));
        assertTrue(e.getMessage().contains("STEADY"));
        assertThrows(
                IllegalArgumentException.class,
                () -> new PottsOperationDiffuser(exchange).step(null, null));
    }

    @Test
    public void step_steadyUniformExchangeAndUptake_matchesLocalBalance() {
        double rate = 0.5;
        double target = 60;
        double vmax = 12.7;
        double steady = localSteady(rate, target, vmax, 0.76);
        PottsLattice l = lattice(6, 4, 3, 0.25, steadyParams(2000));
        l.setField(35.0);
        l.addExchange(allVoxels(6, 4), rate, target);
        l.addSink(allVoxels(6, 4), vmax);

        new PottsOperationDiffuser(l).step(null, null);

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 4; j++) {
                assertEquals(steady, l.getField()[0][i][j], 1E-6);
            }
        }
    }

    @Test
    public void step_steadyZeroDiffusivity_solvesEachVoxelAlone() {
        PottsLattice l = lattice(3, 3, 3, 0.25, steadyParams(0));
        l.addExchange(List.of(new Voxel(0, 0, 0), new Voxel(1, 1, 0)), 0.5, 60);
        l.addSink(List.of(new Voxel(1, 1, 0)), 12.7);

        new PottsOperationDiffuser(l).step(null, null);

        assertEquals(60, l.getField()[0][0][0], 1E-6);
        assertEquals(localSteady(0.5, 60, 12.7, 0.76), l.getField()[0][1][1], 1E-6);
        assertEquals(0, l.getField()[0][2][2], 1E-9);
    }

    @Test
    public void step_steadySupplyAndUptake_balancesAndStaysNonNegative() {
        PottsLattice l = lattice(9, 7, 3, 0.25, steadyParams(2000));
        l.addExchange(List.of(new Voxel(0, 0, 0), new Voxel(0, 1, 0)), 0.5, 60);
        l.addSink(List.of(new Voxel(4, 3, 0), new Voxel(4, 4, 0), new Voxel(8, 6, 0)), 12.7);

        new PottsOperationDiffuser(l).step(null, null);

        // With no-flux edges, total supply equals total uptake at steady state.
        double[][] f = l.getField()[0];
        double supply = 0;
        double uptake = 0;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 7; j++) {
                assertTrue(f[i][j] >= 0);
                supply += l.getExchangeTarget()[0][i][j] - l.getExchange()[0][i][j] * f[i][j];
                uptake += l.getSink()[0][i][j] * f[i][j] / (0.76 + f[i][j]);
            }
        }
        assertTrue(uptake > 0);
        assertEquals(supply, uptake, 1E-6 * supply);
    }

    @Test
    public void step_steadyExchangeOnly_doesNotNeedHalfSaturation() {
        MiniBox p = params(2000, 30);
        p.put("diffuser/STEADY", 1);
        PottsLattice l = lattice(3, 3, 3, 0.25, p);
        l.addExchange(List.of(new Voxel(1, 1, 0)), 0.5, 60);

        new PottsOperationDiffuser(l).step(null, null);

        for (double[] row : l.getField()[0]) {
            for (double v : row) {
                assertEquals(60, v, 0.1);
            }
        }
    }
}
