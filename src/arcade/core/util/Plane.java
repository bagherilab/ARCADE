package arcade.core.util;

import sim.util.Double3D;

/** A plane in 3D space. */
public final class Plane {
    /** A point on the plane. */
    public final Double3D referencePoint;

    /** The unit normal vector to the plane. */
    public final Vector unitNormalVector;

    /**
     * Creates a plane from a Double3D point on plane and a vector.
     *
     * @param point a point on the plane
     * @param normalVector the normal vector to the plane
     */
    public Plane(Double3D point, Vector normalVector) {
        this.referencePoint = point;
        this.unitNormalVector = Vector.normalizeVector(normalVector);
    }

    /**
     * Returns the reference point on the plane.
     *
     * @return the reference point on the plane
     */
    public Double3D getReferencePoint() {
        return referencePoint;
    }

    /**
     * Returns the unit normal vector to the plane.
     *
     * @return the unit normal vector to the plane
     */
    public Vector getUnitNormalVector() {
        return unitNormalVector;
    }

    /**
     * Determines distance from a point to the plane.
     *
     * <p>The distance is positive if the point is on the same side of the plane as the normal
     * vector and negative if it is on the opposite side.
     *
     * @param point the point
     * @return the distance from the point to the plane.
     */
    public double signedDistanceToPlane(Double3D point) {
        return (point.x - referencePoint.x) * unitNormalVector.getX()
                + (point.y - referencePoint.y) * unitNormalVector.getY()
                + (point.z - referencePoint.z) * unitNormalVector.getZ();
    }

    /**
     * Determines if two planes are equal.
     *
     * @param obj the plane to compare
     * @return {@code true} if the planes are equal, {@code false} otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Plane other = (Plane) obj;
        return referencePoint.equals(other.referencePoint)
                && unitNormalVector.equals(other.unitNormalVector);
    }

    /**
     * Returns a hash code for the plane.
     *
     * @return a hash code for the plane
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + referencePoint.hashCode();
        hash = 31 * hash + Double.hashCode(unitNormalVector.getX());
        hash = 31 * hash + Double.hashCode(unitNormalVector.getY());
        hash = 31 * hash + Double.hashCode(unitNormalVector.getZ());
        return hash;
    }
}
