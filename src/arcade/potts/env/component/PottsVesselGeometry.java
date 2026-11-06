package arcade.potts.env.component;

import java.util.ArrayList;
import java.util.List;

/**
 * Geometry utilities for vessel segments in a slab that is periodic along z.
 *
 * <p>Points are {@code double[3]} arrays {x, y, z} in um. The slab has thickness T along z, and z
 * values are wrapped into [-T/2, T/2). A segment from a to b is the shortest periodic connection
 * between them, so its z extent must be less than T/2; methods throw an exception otherwise. The
 * tissue slice sits at z = 0.
 */
public final class PottsVesselGeometry {
    /** Hidden utility class constructor. */
    private PottsVesselGeometry() {}

    /**
     * Checks that the slab thickness is positive.
     *
     * @param thickness the slab thickness T [um]
     */
    private static void checkThickness(double thickness) {
        if (!(thickness > 0)) {
            throw new IllegalArgumentException("slab thickness must be > 0");
        }
    }

    /**
     * Wraps a z value into [-T/2, T/2).
     *
     * @param z the z value [um]
     * @param thickness the slab thickness T [um]
     * @return the wrapped z value [um]
     */
    public static double wrapZ(double z, double thickness) {
        checkThickness(thickness);
        double wrapped = z - thickness * Math.floor((z + thickness / 2) / thickness);
        return wrapped >= thickness / 2 ? wrapped - thickness : wrapped;
    }

    /**
     * Gets the shortest periodic separation along z.
     *
     * @param dz the separation [um]
     * @param thickness the slab thickness T [um]
     * @return the separation wrapped into [-T/2, T/2) [um]
     */
    public static double minimumImage(double dz, double thickness) {
        return wrapZ(dz, thickness);
    }

    /**
     * Gets the vector from a to b, using the shortest periodic separation along z.
     *
     * @param a the start point
     * @param b the end point
     * @param thickness the slab thickness T [um]
     * @return the vector {dx, dy, dz} [um]
     */
    static double[] vector(double[] a, double[] b, double thickness) {
        double dz = minimumImage(b[2] - a[2], thickness);
        if (!(Math.abs(dz) < thickness / 2)) {
            throw new IllegalArgumentException("segment z extent must be less than half the slab");
        }
        return new double[] {b[0] - a[0], b[1] - a[1], dz};
    }

    /**
     * Gets the length of a segment.
     *
     * @param a the start point
     * @param b the end point
     * @param thickness the slab thickness T [um]
     * @return the segment length [um]
     */
    public static double length(double[] a, double[] b, double thickness) {
        double[] v = vector(a, b, thickness);
        return Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
    }

    /**
     * Gets the distance from a point to a segment that starts at the origin.
     *
     * @param px the point x relative to the segment start
     * @param py the point y relative to the segment start
     * @param pz the point z relative to the segment start
     * @param v the segment vector
     * @return the distance [um]
     */
    private static double distanceToSegment(double px, double py, double pz, double[] v) {
        double vv = v[0] * v[0] + v[1] * v[1] + v[2] * v[2];
        double t = vv == 0 ? 0 : (px * v[0] + py * v[1] + pz * v[2]) / vv;
        t = Math.max(0, Math.min(1, t));
        double dx = px - t * v[0];
        double dy = py - t * v[1];
        double dz = pz - t * v[2];
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Gets the distance from a point to a segment, over the periodic images along z.
     *
     * @param p the point
     * @param a the segment start
     * @param b the segment end
     * @param thickness the slab thickness T [um]
     * @return the distance [um]
     */
    public static double pointSegmentDistance(
            double[] p, double[] a, double[] b, double thickness) {
        double[] v = vector(a, b, thickness);
        double pz = minimumImage(p[2] - a[2], thickness);
        double min = Double.POSITIVE_INFINITY;
        for (int k = -1; k <= 1; k++) {
            min = Math.min(min, distanceToSegment(p[0] - a[0], p[1] - a[1], pz + k * thickness, v));
        }
        return min;
    }

    /**
     * Gets the distance between two segments that start at given offsets.
     *
     * <p>Uses the standard clamped closest-points solution for two segments.
     *
     * @param u the first segment vector, starting at the origin
     * @param w0 the second segment start relative to the first segment start
     * @param v the second segment vector
     * @return the distance [um]
     */
    private static double distanceBetween(double[] u, double[] w0, double[] v) {
        double a = dot(u, u);
        double b = dot(u, v);
        double c = dot(v, v);
        double d = dot(u, w0);
        double e = dot(v, w0);
        double denom = a * c - b * b;

        double s;
        double t;
        if (a == 0 && c == 0) {
            return Math.sqrt(dot(w0, w0));
        } else if (a == 0) {
            s = 0;
            t = clamp(-e / c);
        } else if (c == 0) {
            t = 0;
            s = clamp(d / a);
        } else {
            s = denom > 1E-12 * a * c ? clamp((b * e - c * d) / -denom) : 0;
            t = (b * s - e) / c;
            if (t < 0) {
                t = 0;
                s = clamp(d / a);
            } else if (t > 1) {
                t = 1;
                s = clamp((b + d) / a);
            }
        }

        double dx = w0[0] + t * v[0] - s * u[0];
        double dy = w0[1] + t * v[1] - s * u[1];
        double dz = w0[2] + t * v[2] - s * u[2];
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Gets the distance between two segments, over the periodic images along z.
     *
     * @param a the first segment start
     * @param b the first segment end
     * @param c the second segment start
     * @param d the second segment end
     * @param thickness the slab thickness T [um]
     * @return the distance [um]
     */
    public static double segmentSegmentDistance(
            double[] a, double[] b, double[] c, double[] d, double thickness) {
        double[] u = vector(a, b, thickness);
        double[] v = vector(c, d, thickness);
        double cz = minimumImage(c[2] - a[2], thickness);
        double min = Double.POSITIVE_INFINITY;
        for (int k = -1; k <= 1; k++) {
            double[] w0 = {c[0] - a[0], c[1] - a[1], cz + k * thickness};
            min = Math.min(min, distanceBetween(u, w0, v));
        }
        return min;
    }

    /**
     * Samples points along a segment.
     *
     * <p>The segment is split into the fewest equal pieces no longer than {@code step}, and the
     * midpoint of each piece is returned with the piece length. The lengths sum to the segment
     * length.
     *
     * @param a the segment start
     * @param b the segment end
     * @param step the largest spacing between samples [um]
     * @param thickness the slab thickness T [um]
     * @return the samples as {x, y, z, length}, with z wrapped [um]
     */
    public static List<double[]> sample(double[] a, double[] b, double step, double thickness) {
        if (!(step > 0)) {
            throw new IllegalArgumentException("sample step must be > 0");
        }
        double[] v = vector(a, b, thickness);
        double length = Math.sqrt(dot(v, v));
        int n = Math.max(1, (int) Math.ceil(length / step - 1E-9));
        List<double[]> samples = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            double t = (i + 0.5) / n;
            samples.add(
                    new double[] {
                        a[0] + t * v[0],
                        a[1] + t * v[1],
                        wrapZ(a[2] + t * v[2], thickness),
                        length / n
                    });
        }
        return samples;
    }

    /**
     * Clips a segment to the slab |z| &le; h around the slice.
     *
     * <p>Because the z extent of a segment is less than T/2 and h is less than T/4, at most one
     * piece of the segment lies in the slab.
     *
     * @param a the segment start
     * @param b the segment end
     * @param halfWidth the slab half width h [um]
     * @param thickness the slab thickness T [um]
     * @return the clipped piece as {start, end}, with z wrapped, or {@code null} if none
     */
    public static double[][] clipToSlab(
            double[] a, double[] b, double halfWidth, double thickness) {
        if (!(halfWidth >= 0) || !(halfWidth < thickness / 4)) {
            throw new IllegalArgumentException("slab half width must be in [0, T/4)");
        }
        double[] v = vector(a, b, thickness);
        double az = wrapZ(a[2], thickness);

        for (int k = -1; k <= 1; k++) {
            double z0 = az - k * thickness;
            double low;
            double high;
            if (v[2] == 0) {
                if (Math.abs(z0) > halfWidth) {
                    continue;
                }
                low = 0;
                high = 1;
            } else {
                double t1 = (-halfWidth - z0) / v[2];
                double t2 = (halfWidth - z0) / v[2];
                low = Math.max(0, Math.min(t1, t2));
                high = Math.min(1, Math.max(t1, t2));
            }
            if (low <= high) {
                return new double[][] {point(a, v, low, thickness), point(a, v, high, thickness)};
            }
        }
        return null;
    }

    /**
     * Gets the point at a fraction along a segment, with z wrapped.
     *
     * @param a the segment start
     * @param v the segment vector
     * @param t the fraction along the segment
     * @param thickness the slab thickness T [um]
     * @return the point
     */
    private static double[] point(double[] a, double[] v, double t, double thickness) {
        return new double[] {a[0] + t * v[0], a[1] + t * v[1], wrapZ(a[2] + t * v[2], thickness)};
    }

    /**
     * Gets the dot product of two vectors.
     *
     * @param u the first vector
     * @param v the second vector
     * @return the dot product
     */
    private static double dot(double[] u, double[] v) {
        return u[0] * v[0] + u[1] * v[1] + u[2] * v[2];
    }

    /**
     * Clamps a value to [0, 1].
     *
     * @param x the value
     * @return the clamped value
     */
    private static double clamp(double x) {
        return Math.max(0, Math.min(1, x));
    }
}
