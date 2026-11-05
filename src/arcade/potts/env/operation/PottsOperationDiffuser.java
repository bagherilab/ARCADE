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
 * <p>If the lattice has a sink (maximum uptake rates V per voxel, from {@link
 * PottsLattice#addSink}) or an exchange (rates P toward targets T per voxel, from {@link
 * PottsLattice#addExchange}), the reaction P (T - c) - V c / (K + c) is solved inside each substep.
 * The uptake rate k = V / (K + c) is taken from the field at the start of the substep, and k + P is
 * added to the diagonal and P T to the right-hand side, half in each sweep, so the system stays
 * linear. For oxygen, uptake and exchange are much faster than a substep, and applying them as a
 * separate step after diffusion is badly wrong, so they must be part of the implicit solve.
 *
 * <p>Loaded parameters include:
 *
 * <ul>
 *   <li>{@code DIFFUSIVITY} = diffusivity [um^2/s]
 *   <li>{@code MAX_SUBSTEP} = maximum substep [s]
 *   <li>{@code HALF_SATURATION} = half-saturation constant K for uptake [layer units], used only if
 *       the lattice has a sink
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

    /** Per-voxel diagonal terms (k + P) * substep / 2 for the current substep. */
    private final double[][] reaction;

    /** Per-voxel right-hand side terms P * T * substep / 2 for the current substep. */
    private final double[][] supply;

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

        cpX = new double[latticeLength];
        invX = new double[latticeLength];
        cpY = new double[latticeWidth];
        invY = new double[latticeWidth];
        factor(r, cpX, invX);
        factor(r, cpY, invY);
        scratch = new double[Math.max(latticeLength, latticeWidth)];
        scratchPivot = new double[Math.max(latticeLength, latticeWidth)];
        reaction = new double[latticeLength][latticeWidth];
        supply = new double[latticeLength][latticeWidth];
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
     * Solves the implicit system along x for column {@code j}, in place, with extra diagonal and
     * right-hand side terms from reactions.
     *
     * @param f the 2D field
     * @param j the y index
     */
    private void solveReactionX(double[][] f, int j) {
        int m = latticeLength;
        if (m == 1) {
            f[0][j] = (f[0][j] + supply[0][j]) / (1 + reaction[0][j]);
            return;
        }
        double b = 1 + r + reaction[0][j];
        scratchPivot[0] = -r / b;
        scratch[0] = (f[0][j] + supply[0][j]) / b;
        for (int i = 1; i < m; i++) {
            b = (i == m - 1 ? 1 + r : 1 + 2 * r) + reaction[i][j] + r * scratchPivot[i - 1];
            scratchPivot[i] = -r / b;
            scratch[i] = (f[i][j] + supply[i][j] + r * scratch[i - 1]) / b;
        }
        f[m - 1][j] = scratch[m - 1];
        for (int i = m - 2; i >= 0; i--) {
            f[i][j] = scratch[i] - scratchPivot[i] * f[i + 1][j];
        }
    }

    /**
     * Solves the implicit system along y for row {@code i}, in place, with extra diagonal and
     * right-hand side terms from reactions.
     *
     * @param row the row {@code f[i]}
     * @param i the x index
     */
    private void solveReactionY(double[] row, int i) {
        int m = latticeWidth;
        double[] d = reaction[i];
        double[] q = supply[i];
        if (m == 1) {
            row[0] = (row[0] + q[0]) / (1 + d[0]);
            return;
        }
        double b = 1 + r + d[0];
        scratchPivot[0] = -r / b;
        scratch[0] = (row[0] + q[0]) / b;
        for (int j = 1; j < m; j++) {
            b = (j == m - 1 ? 1 + r : 1 + 2 * r) + d[j] + r * scratchPivot[j - 1];
            scratchPivot[j] = -r / b;
            scratch[j] = (row[j] + q[j] + r * scratch[j - 1]) / b;
        }
        row[m - 1] = scratch[m - 1];
        for (int j = m - 2; j >= 0; j--) {
            row[j] = scratch[j] - scratchPivot[j] * row[j + 1];
        }
    }

    /**
     * Advances the field by one substep with uptake and exchange.
     *
     * <p>The uptake rate k = V / (K + c) is computed from the field at the start of the substep.
     * Half of (k + P) * substep is added to the diagonal and half of P * T * substep to the
     * right-hand side in each sweep.
     *
     * @param f the 2D field
     * @param sink the 2D maximum uptake rates V [layer units/s]
     * @param exchange the 2D exchange rates P [1/s]
     * @param target the 2D products P * T [layer units/s]
     */
    void reactionSubstep(double[][] f, double[][] sink, double[][] exchange, double[][] target) {
        double half = substep / 2;
        for (int i = 0; i < latticeLength; i++) {
            for (int j = 0; j < latticeWidth; j++) {
                double k = sink[i][j] > 0 ? sink[i][j] / (halfSaturation + f[i][j]) : 0;
                reaction[i][j] = (k + exchange[i][j]) * half;
                supply[i][j] = target[i][j] * half;
            }
        }
        for (int j = 0; j < latticeWidth; j++) {
            solveReactionX(f, j);
        }
        for (int i = 0; i < latticeLength; i++) {
            solveReactionY(f[i], i);
        }
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

        if (!hasReaction && r == 0) {
            return;
        }

        for (int s = 0; s < substeps; s++) {
            if (hasReaction) {
                reactionSubstep(f, sink, exchange, target);
            } else {
                diffuseSubstep(f);
            }
        }
    }
}
