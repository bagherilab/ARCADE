package arcade.potts.env.operation;

import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.potts.env.lattice.PottsLattice;

/**
 * Extension of {@link PottsOperation} for diffusion.
 *
 * <p>Solves dc/dt = D (d2c/dx2 + d2c/dy2) with locally one-dimensional backward Euler splitting:
 * each substep solves an implicit tridiagonal system along every row, then every column (Thomas
 * algorithm). Boundaries are no-flux. The scheme is unconditionally stable, conserves mass exactly,
 * and preserves non-negativity. Decay is a separate operation ({@link PottsOperationDecayer}).
 *
 * <p>{@code PatchOperationDiffuser} uses explicit 1 s steps sized for the one-minute patch tick.
 * Potts tick lengths vary by setup, so this operation uses an implicit scheme that remains stable
 * for any tick length and diffusivity.
 *
 * <p>Secretion arrives as one pulse per tick, and the error in concentration near a source after
 * such a pulse grows with r = D * substep / ds^2, so large substeps overestimate values next to
 * secreting cells. This is why the default {@code MAX_SUBSTEP} is small (2 s).
 *
 * <p>Layers with a sink (maximum uptake rates V per voxel, from {@link PottsLattice#addSink}) or an
 * exchange (rates P toward targets T per voxel, from {@link PottsLattice#addExchange}) must set
 * {@code STEADY} to 1; otherwise the step throws an exception. Such a layer is solved each tick to
 * its steady state for the current sink and exchange, D (d2c/dx2 + d2c/dy2) + P (T - c) - V c / (K
 * + c) = 0, by Peaceman-Rachford alternating direction iterations started from the current field.
 * This suits layers that equilibrate much faster than a tick, such as oxygen. Time stepping with
 * uptake and exchange inside the row-then-column substeps was removed: it is badly wrong for fast
 * reactions in 2D, and no planned layer has slow ones. With no sink or exchange, a steady layer is
 * stepped in time as usual.
 *
 * <p>Loaded parameters include:
 *
 * <ul>
 *   <li>{@code DIFFUSIVITY} = diffusivity [um^2/s]
 *   <li>{@code MAX_SUBSTEP} = maximum substep [s]
 *   <li>{@code HALF_SATURATION} = half-saturation constant K for uptake [layer units], used only if
 *       the lattice has a sink
 *   <li>{@code STEADY} = 1 to solve the layer to steady state each tick, 0 to step in time
 *   <li>{@code STEADY_TOLERANCE} = the solve stops when the largest change over a cycle of
 *       iterations is below this [layer units]
 *   <li>{@code STEADY_MAX_ITERATIONS} = iterations allowed before throwing an exception
 * </ul>
 */
public class PottsOperationDiffuser extends PottsOperation {
    /** Number of substeps per tick. */
    final int substeps;

    /** Substep length [s]. */
    final double substep;

    /** Dimensionless diffusion number D * substep / ds^2. */
    final double r;

    /** Half-saturation constant for uptake [layer units]. */
    final double halfSaturation;

    /** Number of iteration parameters cycled in steady solves. */
    static final int STEADY_PARAMETERS = 8;

    /** {@code true} if the layer is solved to steady state each tick. */
    final boolean steady;

    /** Largest change per iteration at convergence [layer units]. */
    final double steadyTolerance;

    /** Iterations allowed before throwing an exception. */
    final int steadyMaxIterations;

    /** Diffusion rate D / ds^2 [1/s]. */
    final double diffusionRate;

    /** Iterations used by the last steady solve. */
    int lastIterations;

    /** Scratch field for steady solves. */
    private final double[][] scratchField;

    /** Scratch row for steady solves. */
    private final double[] scratchRow;

    /** Field at the start of the current cycle of steady iterations. */
    private final double[][] cycleStart;

    /** Per-voxel half reaction rates (k + P) / (2 rho) for the current steady iteration. */
    private final double[][] reaction;

    /** Second scratch array for variable-diagonal sweeps. */
    private final double[] scratchPivot;

    /** Thomas super-diagonal factors along x. */
    private final double[] cpX;

    /** Thomas inverse pivots along x. */
    private final double[] invX;

    /** Thomas super-diagonal factors along y. */
    private final double[] cpY;

    /** Thomas inverse pivots along y. */
    private final double[] invY;

    /** Scratch array for forward sweeps. */
    private final double[] scratch;

    /**
     * Creates a diffuser for the given lattice.
     *
     * @param lattice the {@link PottsLattice} the operation is associated with
     */
    public PottsOperationDiffuser(PottsLattice lattice) {
        super(lattice);
        if (latticeHeight != 1) {
            throw new UnsupportedOperationException("diffusion is implemented for 2D only");
        }

        MiniBox parameters = lattice.getParameters();
        double diffusivity = parameters.getDouble("diffuser/DIFFUSIVITY");
        double maxSubstep = parameters.getDouble("diffuser/MAX_SUBSTEP");

        if (!(diffusivity >= 0) || !(maxSubstep > 0)) {
            throw new IllegalArgumentException(
                    "diffuser requires DIFFUSIVITY >= 0 and MAX_SUBSTEP > 0");
        }

        double tick = lattice.getDt() * 3600.0;
        substeps = Math.max(1, (int) Math.ceil(tick / maxSubstep - 1E-9));
        substep = tick / substeps;

        double ds = lattice.getDs();
        r = diffusivity * substep / (ds * ds);
        halfSaturation = parameters.getDouble("diffuser/HALF_SATURATION");
        diffusionRate = diffusivity / (ds * ds);
        steady =
                parameters.contains("diffuser/STEADY") && parameters.getInt("diffuser/STEADY") == 1;
        steadyTolerance =
                parameters.contains("diffuser/STEADY_TOLERANCE")
                        ? parameters.getDouble("diffuser/STEADY_TOLERANCE")
                        : 1E-3;
        steadyMaxIterations =
                parameters.contains("diffuser/STEADY_MAX_ITERATIONS")
                        ? parameters.getInt("diffuser/STEADY_MAX_ITERATIONS")
                        : 500;
        if (steady && (!(steadyTolerance > 0) || steadyMaxIterations < 1)) {
            throw new IllegalArgumentException(
                    "steady diffuser requires STEADY_TOLERANCE > 0 and STEADY_MAX_ITERATIONS >= 1");
        }

        cpX = new double[latticeLength];
        invX = new double[latticeLength];
        cpY = new double[latticeWidth];
        invY = new double[latticeWidth];
        factor(r, cpX, invX);
        factor(r, cpY, invY);
        scratch = new double[Math.max(latticeLength, latticeWidth)];
        scratchPivot = new double[Math.max(latticeLength, latticeWidth)];
        reaction = new double[latticeLength][latticeWidth];
        scratchField = new double[latticeLength][latticeWidth];
        scratchRow = new double[latticeWidth];
        cycleStart = new double[latticeLength][latticeWidth];
    }

    /**
     * Precomputes Thomas factors for the no-flux implicit operator I - r * second difference.
     *
     * <p>Diagonal is 1 + r at the ends and 1 + 2r inside; off-diagonals are -r.
     *
     * @param r the dimensionless diffusion number
     * @param cp the super-diagonal factors (output)
     * @param inv the inverse pivots (output)
     */
    static void factor(double r, double[] cp, double[] inv) {
        int m = cp.length;
        if (m == 1) {
            cp[0] = 0;
            inv[0] = 1;
            return;
        }
        inv[0] = 1 / (1 + r);
        cp[0] = -r * inv[0];
        for (int i = 1; i < m; i++) {
            double b = (i == m - 1) ? 1 + r : 1 + 2 * r;
            inv[i] = 1 / (b + r * cp[i - 1]);
            cp[i] = -r * inv[i];
        }
    }

    /**
     * Solves the implicit system along x for column {@code j}, in place.
     *
     * @param f the 2D field
     * @param j the y index
     */
    private void solveX(double[][] f, int j) {
        int m = latticeLength;
        scratch[0] = f[0][j] * invX[0];
        for (int i = 1; i < m; i++) {
            scratch[i] = (f[i][j] + r * scratch[i - 1]) * invX[i];
        }
        f[m - 1][j] = scratch[m - 1];
        for (int i = m - 2; i >= 0; i--) {
            f[i][j] = scratch[i] - cpX[i] * f[i + 1][j];
        }
    }

    /**
     * Solves the implicit system along y for one row, in place.
     *
     * @param row the row {@code f[i]}
     */
    private void solveY(double[] row) {
        int m = latticeWidth;
        scratch[0] = row[0] * invY[0];
        for (int j = 1; j < m; j++) {
            scratch[j] = (row[j] + r * scratch[j - 1]) * invY[j];
        }
        row[m - 1] = scratch[m - 1];
        for (int j = m - 2; j >= 0; j--) {
            row[j] = scratch[j] - cpY[j] * row[j + 1];
        }
    }

    /**
     * Advances the field by one substep.
     *
     * @param f the 2D field
     */
    void diffuseSubstep(double[][] f) {
        for (int j = 0; j < latticeWidth; j++) {
            solveX(f, j);
        }
        for (int i = 0; i < latticeLength; i++) {
            solveY(f[i]);
        }
    }

    /**
     * Solves (1 + 2 rr + reaction) u - rr (neighbors) = f along x for column {@code j}, in place,
     * with no-flux ends.
     *
     * @param f the 2D field, holding the right-hand side on entry
     * @param j the y index
     * @param rr the off-diagonal weight
     */
    private void solveLineX(double[][] f, int j, double rr) {
        int m = latticeLength;
        if (m == 1) {
            f[0][j] /= 1 + reaction[0][j];
            return;
        }
        double b = 1 + rr + reaction[0][j];
        scratchPivot[0] = -rr / b;
        scratch[0] = f[0][j] / b;
        for (int i = 1; i < m; i++) {
            b = (i == m - 1 ? 1 + rr : 1 + 2 * rr) + reaction[i][j] + rr * scratchPivot[i - 1];
            scratchPivot[i] = -rr / b;
            scratch[i] = (f[i][j] + rr * scratch[i - 1]) / b;
        }
        f[m - 1][j] = scratch[m - 1];
        for (int i = m - 2; i >= 0; i--) {
            f[i][j] = scratch[i] - scratchPivot[i] * f[i + 1][j];
        }
    }

    /**
     * Solves (1 + 2 rr + reaction) u - rr (neighbors) = f along y for row {@code i}, in place, with
     * no-flux ends.
     *
     * @param row the row, holding the right-hand side on entry
     * @param i the x index
     * @param rr the off-diagonal weight
     */
    private void solveLineY(double[] row, int i, double rr) {
        int m = latticeWidth;
        double[] d = reaction[i];
        if (m == 1) {
            row[0] /= 1 + d[0];
            return;
        }
        double b = 1 + rr + d[0];
        scratchPivot[0] = -rr / b;
        scratch[0] = row[0] / b;
        for (int j = 1; j < m; j++) {
            b = (j == m - 1 ? 1 + rr : 1 + 2 * rr) + d[j] + rr * scratchPivot[j - 1];
            scratchPivot[j] = -rr / b;
            scratch[j] = (row[j] + rr * scratch[j - 1]) / b;
        }
        row[m - 1] = scratch[m - 1];
        for (int j = m - 2; j >= 0; j--) {
            row[j] = scratch[j] - scratchPivot[j] * row[j + 1];
        }
    }

    /**
     * Gets the no-flux second difference of a field at a voxel along x.
     *
     * @param f the 2D field
     * @param i the x index
     * @param j the y index
     * @return the second difference
     */
    private double secondDifferenceX(double[][] f, int i, int j) {
        double center = f[i][j];
        double left = i > 0 ? f[i - 1][j] : center;
        double right = i < latticeLength - 1 ? f[i + 1][j] : center;
        return left - 2 * center + right;
    }

    /**
     * Gets the no-flux second difference of a field at a voxel along y.
     *
     * @param f the 2D field
     * @param i the x index
     * @param j the y index
     * @return the second difference
     */
    private double secondDifferenceY(double[][] f, int i, int j) {
        double center = f[i][j];
        double down = j > 0 ? f[i][j - 1] : center;
        double up = j < latticeWidth - 1 ? f[i][j + 1] : center;
        return down - 2 * center + up;
    }

    /**
     * Gets the iteration parameters for steady solves.
     *
     * <p>Parameters are spaced geometrically between an estimate of the smallest and largest
     * eigenvalues of the steady operator. The smallest is taken as the smaller of a lower bound on
     * the mean reaction rate (the smoothest mode) and the smallest diffusion eigenvalue of the grid
     * (modes in regions with no reaction). The largest is the largest diffusion eigenvalue plus the
     * largest reaction rate.
     *
     * @param meanReaction a lower bound on the mean reaction rate k + P over the grid [1/s]
     * @param minReaction the smallest positive lower bound on a voxel reaction rate [1/s]
     * @param maxReaction the largest reaction rate k + P [1/s]
     * @return the iteration parameters [1/s]
     */
    double[] steadyParameters(double meanReaction, double minReaction, double maxReaction) {
        int n = Math.max(latticeLength, latticeWidth);
        double high = 8 * diffusionRate + maxReaction;
        double low = Math.min(meanReaction, diffusionRate * Math.pow(Math.PI / n, 2));
        if (!(low > 0)) {
            low = minReaction;
        }
        low = Math.max(low, high * 1E-12);
        double[] rhos = new double[STEADY_PARAMETERS];
        for (int p = 0; p < STEADY_PARAMETERS; p++) {
            rhos[p] = low * Math.pow(high / low, p / (double) (STEADY_PARAMETERS - 1));
        }
        return rhos;
    }

    /**
     * Solves the field to steady state with uptake and exchange.
     *
     * <p>Each Peaceman-Rachford iteration with parameter rho solves (rho + A_x) u = (rho - A_y) c +
     * b along x, then (rho + A_y) c' = (rho - A_x) u + b along y. A_x and A_y each hold diffusion
     * along one axis and half of the reaction rate R = k + P, and b = P T. The uptake rate k = V /
     * (K + c) is updated at the start of each iteration, and values are kept non-negative. The
     * fixed point of the iteration is the steady state, so the result does not depend on the
     * substep. Convergence is checked once per cycle of parameters, as the largest change over the
     * cycle, because single iterations with some parameters barely change the field.
     *
     * @param f the 2D field, holding the starting guess on entry
     * @param sink the 2D maximum uptake rates V [layer units/s]
     * @param exchange the 2D exchange rates P [1/s]
     * @param target the 2D products P * T [layer units/s]
     */
    void steadySolve(double[][] f, double[][] sink, double[][] exchange, double[][] target) {
        // The field never exceeds the larger of its current maximum and the largest exchange
        // target,
        // so the uptake rate k = V / (K + c) is at least V / (K + that ceiling).
        double ceiling = 0;
        for (int i = 0; i < latticeLength; i++) {
            for (int j = 0; j < latticeWidth; j++) {
                ceiling = Math.max(ceiling, f[i][j]);
                if (exchange[i][j] > 0) {
                    ceiling = Math.max(ceiling, target[i][j] / exchange[i][j]);
                }
            }
        }
        double maxReaction = 0;
        double minReaction = Double.POSITIVE_INFINITY;
        double sumReaction = 0;
        for (int i = 0; i < latticeLength; i++) {
            for (int j = 0; j < latticeWidth; j++) {
                double kLow = sink[i][j] > 0 ? sink[i][j] / (halfSaturation + ceiling) : 0;
                double kMax = sink[i][j] > 0 ? sink[i][j] / halfSaturation : 0;
                double low = kLow + exchange[i][j];
                maxReaction = Math.max(maxReaction, kMax + exchange[i][j]);
                sumReaction += low;
                if (low > 0) {
                    minReaction = Math.min(minReaction, low);
                }
            }
        }
        double meanReaction = sumReaction / (latticeLength * latticeWidth);
        double[] rhos = steadyParameters(meanReaction, minReaction, maxReaction);
        double[][] u = scratchField;

        for (int it = 1; it <= steadyMaxIterations; it++) {
            int p = (it - 1) % rhos.length;
            double rho = rhos[p];
            if (p == 0) {
                for (int i = 0; i < latticeLength; i++) {
                    System.arraycopy(f[i], 0, cycleStart[i], 0, latticeWidth);
                }
            }
            double rr = diffusionRate / rho;

            // Half reaction rates for this iteration, divided by rho.
            for (int i = 0; i < latticeLength; i++) {
                for (int j = 0; j < latticeWidth; j++) {
                    double k = sink[i][j] > 0 ? sink[i][j] / (halfSaturation + f[i][j]) : 0;
                    reaction[i][j] = (k + exchange[i][j]) / (2 * rho);
                }
            }

            // Sweep along x: (rho + A_x) u = (rho - A_y) c + b.
            for (int i = 0; i < latticeLength; i++) {
                for (int j = 0; j < latticeWidth; j++) {
                    u[i][j] =
                            (1 - reaction[i][j]) * f[i][j]
                                    + rr * secondDifferenceY(f, i, j)
                                    + target[i][j] / rho;
                }
            }
            for (int j = 0; j < latticeWidth; j++) {
                solveLineX(u, j, rr);
            }

            // Sweep along y: (rho + A_y) c' = (rho - A_x) u + b.
            for (int i = 0; i < latticeLength; i++) {
                for (int j = 0; j < latticeWidth; j++) {
                    scratchRow[j] =
                            (1 - reaction[i][j]) * u[i][j]
                                    + rr * secondDifferenceX(u, i, j)
                                    + target[i][j] / rho;
                }
                solveLineY(scratchRow, i, rr);
                for (int j = 0; j < latticeWidth; j++) {
                    f[i][j] = Math.max(scratchRow[j], 0);
                }
            }

            // Check convergence at the end of each cycle of parameters.
            if (p == rhos.length - 1) {
                double change = 0;
                for (int i = 0; i < latticeLength; i++) {
                    for (int j = 0; j < latticeWidth; j++) {
                        change = Math.max(change, Math.abs(f[i][j] - cycleStart[i][j]));
                    }
                }
                if (change < steadyTolerance) {
                    lastIterations = it;
                    return;
                }
            }
        }
        throw new IllegalStateException(
                "steady diffuser did not converge in " + steadyMaxIterations + " iterations");
    }

    /**
     * Checks if any voxel has a positive rate.
     *
     * @param rates the 2D rates
     * @return {@code true} if any rate is positive, {@code false} otherwise
     */
    static boolean anyPositive(double[][] rates) {
        for (double[] row : rates) {
            for (double v : row) {
                if (v > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {
        double[][] f = lattice.getField()[0];
        double[][] sink = lattice.getSink()[0];
        double[][] exchange = lattice.getExchange()[0];
        double[][] target = lattice.getExchangeTarget()[0];
        boolean hasSink = anyPositive(sink);
        boolean hasReaction = hasSink || anyPositive(exchange);

        if (hasSink && !(halfSaturation > 0)) {
            throw new IllegalArgumentException("uptake requires diffuser HALF_SATURATION > 0");
        }

        if (hasReaction && !steady) {
            throw new IllegalArgumentException(
                    "uptake or exchange requires diffuser STEADY = 1 (steady solve)");
        }

        if (hasReaction) {
            steadySolve(f, sink, exchange, target);
            return;
        }

        if (r == 0) {
            return;
        }

        for (int s = 0; s < substeps; s++) {
            diffuseSubstep(f);
        }
    }
}
