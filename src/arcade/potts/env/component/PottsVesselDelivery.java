package arcade.potts.env.component;

import java.util.List;

/**
 * Per-voxel O2 exchange weights and recruitment sites from perfused vessel segments.
 *
 * <p>Exchange: each perfused segment is sampled every ds / 2. A sample at height z (|z| &le; 3
 * lambda from the slice) adds I exp(-|z| / lambda) (sample length / ds) to the voxel under its (x,
 * y), where I is the segment integrity. A vessel lying in the slice gives a weight of I per voxel
 * along it. A vessel near but outside the slice gives the same, scaled by exp(-|z| / lambda), which
 * stands for O2 that diffuses to the slice along z. A vessel crossing the slice steeply puts all of
 * its weight in one voxel.
 *
 * <p>Recruitment sites: each perfused segment is clipped to |z| &le; {@code d_recruit} and sampled
 * every ds / 2. Each sample adds I (sample length) to the voxel under it.
 *
 * <p>Voxel i covers x in [i ds, (i + 1) ds) from the lattice origin, and likewise for y. Samples
 * outside the lattice are skipped.
 */
public final class PottsVesselDelivery {
    /** Kernel cutoff, in units of lambda. */
    public static final double CUTOFF = 3;

    /** Hidden utility class constructor. */
    private PottsVesselDelivery() {}

    /**
     * Adds a value to the voxel under a point, if the point is inside the lattice.
     *
     * @param weights the 2D weights, indexed [x][y]
     * @param x the point x [um]
     * @param y the point y [um]
     * @param ds the voxel size [um]
     * @param value the value to add
     */
    private static void deposit(double[][] weights, double x, double y, double ds, double value) {
        int i = (int) Math.floor(x / ds);
        int j = (int) Math.floor(y / ds);
        if (i < 0 || i >= weights.length || j < 0 || j >= weights[i].length) {
            return;
        }
        weights[i][j] += value;
    }

    /**
     * Gets the O2 exchange weight of each voxel.
     *
     * @param network the vessel network, with perfusion updated
     * @param length the lattice length (x direction) [voxels]
     * @param width the lattice width (y direction) [voxels]
     * @param ds the voxel size [um]
     * @param lambda the O2 diffusion length along z [um]
     * @return the 2D weights, indexed [x][y]
     */
    public static double[][] exchangeWeights(
            PottsVesselNetwork network, int length, int width, double ds, double lambda) {
        if (!(ds > 0) || !(lambda > 0)) {
            throw new IllegalArgumentException("delivery requires ds > 0 and lambda > 0");
        }
        double thickness = network.getThickness();
        double[][] weights = new double[length][width];
        for (PottsVesselEdge segment : network.getSegments()) {
            if (!segment.isPerfused() || segment.getIntegrity() == 0) {
                continue;
            }
            double integrity = segment.getIntegrity();
            List<double[]> samples =
                    PottsVesselGeometry.sample(
                            segment.getStart().getPosition(),
                            segment.getEnd().getPosition(),
                            ds / 2,
                            thickness);
            for (double[] sample : samples) {
                double z = Math.abs(sample[2]);
                if (z > CUTOFF * lambda) {
                    continue;
                }
                double value = integrity * Math.exp(-z / lambda) * sample[3] / ds;
                deposit(weights, sample[0], sample[1], ds, value);
            }
        }
        return weights;
    }

    /**
     * Gets the recruitment site weight of each voxel.
     *
     * @param network the vessel network, with perfusion updated
     * @param length the lattice length (x direction) [voxels]
     * @param width the lattice width (y direction) [voxels]
     * @param ds the voxel size [um]
     * @param distance the distance from the slice that sites come from [um]
     * @return the 2D weights (integrity times vessel length [um]), indexed [x][y]
     */
    public static double[][] siteWeights(
            PottsVesselNetwork network, int length, int width, double ds, double distance) {
        if (!(ds > 0)) {
            throw new IllegalArgumentException("delivery requires ds > 0");
        }
        double thickness = network.getThickness();
        double[][] weights = new double[length][width];
        for (PottsVesselEdge segment : network.getSegments()) {
            if (!segment.isPerfused() || segment.getIntegrity() == 0) {
                continue;
            }
            double[][] piece =
                    PottsVesselGeometry.clipToSlab(
                            segment.getStart().getPosition(),
                            segment.getEnd().getPosition(),
                            distance,
                            thickness);
            if (piece == null) {
                continue;
            }
            for (double[] sample :
                    PottsVesselGeometry.sample(piece[0], piece[1], ds / 2, thickness)) {
                deposit(weights, sample[0], sample[1], ds, segment.getIntegrity() * sample[3]);
            }
        }
        return weights;
    }
}
