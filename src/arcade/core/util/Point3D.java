package arcade.core.util;

import arcade.potts.env.location.Voxel;

/**
 * A point in 3D space.
 */
public class Point3D {
    /** The x coordinate of the point. */
    private final int x;

    /** The y coordinate of the point. */
    private final int y;
    
    /** The z coordinate of the point. */
    private final int z;

    /**
     * Creates a point in 3D space.
     *
     * @param x  the x coordinate
     * @param y  the y coordinate
     * @param z  the z coordinate
     */
    public Point3D(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Creates a point in the same location as a voxel.
     *
     * @param v  the voxel
     */
    public Point3D(Voxel v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
    }

    /**
     * Retrieves the x coordinate of the point.
     *
     * @return the x coordinate
     */
    public int getX() { return x; }

    /**
     * Retrieves the y coordinate of the point.
     *
     * @return the y coordinate
     */
    public int getY() { return y; }

    /**
     * Retrieves the z coordinate of the point.
     *
     * @return the z coordinate
     */
    public int getZ() { return z; }

    /**
     * Checks if two points have the same (x, y, z) coordinates.
     *
     * @param obj  the point to compare
     * @return {@code true} if coordinates are the same, {@code false} otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Point3D other = (Point3D) obj;
        return x == other.x && y == other.y && z == other.z;
    }

    /**
     * Retrieves the hash code of the point.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + x;
        hash = 31 * hash + y;
        hash = 31 * hash + z;
        return hash;
    }
}
