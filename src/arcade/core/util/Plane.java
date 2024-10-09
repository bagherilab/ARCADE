package arcade.core.util;

import sim.util.Int3D;

/**
 * A plane in 3D space.
 */

public final class Plane {
    /** A point on the plane. */
    public final Int3D point;
    
    /** The normal vector to the plane. */
    public final Int3D normalVector;
    
    /**
     * Creates a plane from a point and a vector.
     *
     * @param point  a point on the plane
     * @param normalVector  the normal vector to the plane
     */
    public Plane(Int3D point, Int3D normalVector) {
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
    public boolean isOnPlane(Int3D p) {
        return (p.getX() - point.getX()) * normalVector.getX()
                + (p.getY() - point.getY()) * normalVector.getY()
                + (p.getZ() - point.getZ()) * normalVector.getZ() == 0;
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
    public double signedDistanceToPlane(Int3D p) {
        double dotProduct;
        double normalVectorMagnitude;
        
        dotProduct = (p.getX() - point.getX()) * normalVector.getX()
                + (p.getY() - point.getY()) * normalVector.getY()
                + (p.getZ() - point.getZ()) * normalVector.getZ();
        normalVectorMagnitude = Math.sqrt(normalVector.getX() * normalVector.getX()
                                    + normalVector.getY() * normalVector.getY()
                                    + normalVector.getZ() * normalVector.getZ());
        return dotProduct / normalVectorMagnitude;
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
    
    /**
     * Returns a hash code for the plane.
     *
     * @return  a hash code for the plane
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.point != null ? this.point.hashCode() : 0);
        hash = 97 * hash + (this.normalVector != null ? this.normalVector.hashCode() : 0);
        return hash;
    }
}
