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
     * Determines whether a point is on the plane.
     *
     * @param p  the point to test
     * @return  {@code true} if the point is on the plane
     *        {@code false} otherwise
     */
    public boolean isOnPlane(Point3D p) {
        return (p.getX() - point.getX()) * normalVector.getA()
                + (p.getY() - point.getY()) * normalVector.getB()
                + (p.getZ() - point.getZ()) * normalVector.getC() == 0;
    }

    /**
     * Determines distance from a point to the plane.
     *
     * @param p  the point
     * @return  the distance from the point to the plane.
     *          The distance is positive if the point is on
     *          the same side of the plane as the normal vector
     *         and negative if it is on the opposite side.
     */
    public double distanceToPlane(Point3D p) {
        return (p.getX() - point.getX()) * normalVector.getA()
                + (p.getY() - point.getY()) * normalVector.getB()
                + (p.getZ() - point.getZ()) * normalVector.getC();
    }

    /**
     * Determines if two planes are equal.
     *
     * @param obj  the plane to compare
     * @return {@code true} if the planes are equal, {@code false} otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Plane other = (Plane) obj;
        return point.equals(other.point) && normalVector.equals(other.normalVector);
    }
}