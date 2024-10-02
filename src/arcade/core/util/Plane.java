package arcade.core.util;

/**
 * A plane in 3D space.
 */

public class Plane {
    /** A point on the plane */
    public final Point3D point;

    /** The normal to the plane */
    public final Vector3D normalVector;

    /**
     * Creates a plane from a point and a vector.
     *
     * @param point  a point on the plane
     * @param normalVector  the normal vector to the plane
     */
    public Plane(Point3D point, Vector3D normalVector) {
        this.point = point;
        this.normalVector = normalVector;
    }

    /**
     * Determines whether a point is on one side of the plane.
     * 
     * @param p  the point to test
     * @return  {@code true} if the point is on the side of the plane
     *         where the normal vector points
     *        {@code false} otherwise
     */
    public boolean isOnSide(Point3D p) {
        return (p.getX() - point.getX()) * normalVector.getA()
                + (p.getY() - point.getY()) * normalVector.getB()
                + (p.getZ() - point.getZ()) * normalVector.getC() > 0;
    }
}