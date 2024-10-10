package arcade.potts.util;

import sim.util.Int3D;
import arcade.potts.env.location.Voxel;

/**
 * A plane in 3D space.
 */

public final class Plane {
    /** A point on the plane. */
    public final Voxel referencePoint;

    /** The normal vector to the plane. */
    public final Int3D normalVector;

    /**
     * Creates a plane from a point and a vector.
     *
     * @param point  a point on the plane
     * @param normalVector  the normal vector to the plane
     */
    public Plane(Voxel voxel, Int3D normalVector) {
        this.referencePoint = voxel;
        this.normalVector = normalVector;
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
        
        dotProduct = (p.getX() - referencePoint.x) * normalVector.getX()
                + (p.getY() - referencePoint.y) * normalVector.getY()
                + (p.getZ() - referencePoint.z) * normalVector.getZ();
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
        return referencePoint.equals(other.referencePoint) && normalVector.equals(other.normalVector);
    }

    /**
     * Returns a hash code for the plane.
     *
     * @return  a hash code for the plane
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.referencePoint != null ? this.referencePoint.hashCode() : 0);
        hash = 97 * hash + (this.normalVector != null ? this.normalVector.hashCode() : 0);
        return hash;
    }
}
