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
 * <p>Loaded parameters include:
 *
 * <ul>
 *   <li>{@code DIFFUSIVITY} = diffusivity [um^2/s]
 *   <li>{@code MAX_SUBSTEP} = maximum substep [s]
 * </ul>
 */
public class PottsOperationDiffuser extends PottsOperation {
    /** Number of substeps per tick. */
    final int substeps;

    /** Substep length [s]. */
    final double substep;

    /** Dimensionless diffusion number D * substep / ds^2. */
    final double r;

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

        cpX = new double[latticeLength];
        invX = new double[latticeLength];
        cpY = new double[latticeWidth];
        invY = new double[latticeWidth];
        factor(r, cpX, invX);
        factor(r, cpY, invY);
        scratch = new double[Math.max(latticeLength, latticeWidth)];
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

    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {
        if (r == 0) {
            return;
        }
        double[][] f = lattice.getField()[0];
        for (int s = 0; s < substeps; s++) {
            diffuseSubstep(f);
        }
    }
}
