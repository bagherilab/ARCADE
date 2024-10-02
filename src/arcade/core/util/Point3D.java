package arcade.core.util;

import arcade.potts.env.location.Voxel;


/**
 * A point in 3D space.
 */
public class Point3D {

    private final int x, y, z;

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

    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
}
