package arcade.potts.env.component;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * Plane-section footprints of vessel segments.
 *
 * <p>The footprint of a segment is the set of lattice voxels (i, j) whose centre ((i + 0.5) ds, (j
 * + 0.5) ds, 0) lies within a radius of the segment. The slab is periodic along z, so a segment is
 * also tested at its images shifted by one slab thickness.
 */
public final class PottsVesselFootprint {
    /** Hidden utility class constructor. */
    private PottsVesselFootprint() {}

    /**
     * Gets the voxels of the plane section z = 0 that lie within a radius of a segment.
     *
     * @param a the segment start {x, y, z} [um]
     * @param b the segment end {x, y, z} [um]
     * @param r the radius [um]
     * @param ds the voxel size [um]
     * @param thickness the slab thickness T [um]
     * @param nx the lattice size along x [voxels]
     * @param ny the lattice size along y [voxels]
     * @return the voxels as {i, j}, ordered by i then j, clipped to the lattice
     */
    public static List<int[]> of(
            double[] a, double[] b, double r, double ds, double thickness, int nx, int ny) {
        double[] v = PottsVesselGeometry.vector(a, b, thickness);
        double vv = Math.max(v[0] * v[0] + v[1] * v[1] + v[2] * v[2], 1E-12);
        double[] end = {a[0] + v[0], a[1] + v[1], a[2] + v[2]};
        TreeSet<Long> keys = new TreeSet<>();

        for (double shift : new double[] {0, thickness, -thickness}) {
            double az = a[2] + shift;
            double bz = end[2] + shift;
            if (Math.min(az, bz) > r || Math.max(az, bz) < -r) {
                continue;
            }
            int iLow = Math.max((int) Math.floor((Math.min(a[0], end[0]) - r) / ds), 0);
            int iHigh = Math.min((int) Math.ceil((Math.max(a[0], end[0]) + r) / ds) + 1, nx);
            int jLow = Math.max((int) Math.floor((Math.min(a[1], end[1]) - r) / ds), 0);
            int jHigh = Math.min((int) Math.ceil((Math.max(a[1], end[1]) + r) / ds) + 1, ny);

            for (int i = iLow; i < iHigh; i++) {
                double qx = (i + 0.5) * ds - a[0];
                for (int j = jLow; j < jHigh; j++) {
                    double qy = (j + 0.5) * ds - a[1];
                    double qz = -az;
                    double t = Math.max(0, Math.min(1, (qx * v[0] + qy * v[1] + qz * v[2]) / vv));
                    double dx = qx - t * v[0];
                    double dy = qy - t * v[1];
                    double dz = qz - t * v[2];
                    if (Math.sqrt(dx * dx + dy * dy + dz * dz) <= r) {
                        keys.add((long) i * ny + j);
                    }
                }
            }
        }

        List<int[]> voxels = new ArrayList<>(keys.size());
        for (long key : keys) {
            voxels.add(new int[] {(int) (key / ny), (int) (key % ny)});
        }
        return voxels;
    }
}
