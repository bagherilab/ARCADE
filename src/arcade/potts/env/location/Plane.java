package arcade.potts.env.location;

import sim.util.Double3D;
import sim.util.Int3D;
import arcade.potts.util.PottsEnums.Direction;

/**
 * A plane in 3D space.
 */

public final class Plane {
    /** A point on the plane. */
    public final Voxel referencePoint;
    
    /** The unit normal vector to the plane. */
    public final Double3D unitNormalVector;
    
    /**
     * Creates a plane from a point and a vector.
     *
     * @param voxel  a point on the plane
     * @param normalVector  the normal vector to the plane
     */
    public Plane(Voxel voxel, Int3D normalVector) {
        this.referencePoint = voxel;
        this.unitNormalVector = scaleNormalVector(normalVector);
    }
    
    /**
     * Creates a plane from a point and a direction.
     *
     * @param voxel a point on the plane
     * @param direction the direction of the plane
     */
    public Plane(Voxel voxel, Direction direction) {
        this(voxel, direction.vector);
    }
    
    /**
     * Determines the magnitude of the normal vector.
     *
     * @return  the magnitude of the normal vector
     */
    static public double getNormalVectorMagnitude(Int3D normalVector) {
        return Math.sqrt(normalVector.getX() * normalVector.getX()
                        + normalVector.getY() * normalVector.getY()
                        + normalVector.getZ() * normalVector.getZ());
    }
    
    /**
     * Scales the normal vector to a unit vector.
     *
     * @return  the unit normal vector
     */
    static public Double3D scaleNormalVector(Int3D normalVector) {
        double magnitude = getNormalVectorMagnitude(normalVector);
        double scaledX = normalVector.getX() / magnitude;
        double scaledY = normalVector.getY() / magnitude;
        double scaledZ = normalVector.getZ() / magnitude;
        Double3D unitNormalVector = new Double3D(scaledX, scaledY, scaledZ);
        return unitNormalVector;
    }
    
    /**
     * Determines distance from a point to the plane.
     *
     * The distance is positive if the point is on the same side of the plane
     * as the normal vector and negative if it is on the opposite side.
     *
     * @param p  the point
     * @return  the distance from the point to the plane.
     *
     */
    public double signedDistanceToPlane(Voxel point) {
        double dotProduct = (point.x - referencePoint.x) * unitNormalVector.getX()
                + (point.y - referencePoint.y) * unitNormalVector.getY()
                + (point.z - referencePoint.z) * unitNormalVector.getZ();
        return dotProduct;
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
        return referencePoint.equals(other.referencePoint)
                                     && unitNormalVector.equals(other.unitNormalVector);
    }
    
    /**
     * Returns a hash code for the plane.
     *
     * @return  a hash code for the plane
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
