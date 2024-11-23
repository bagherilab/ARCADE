package arcade.potts.env.location;

import sim.util.Double3D;
import arcade.potts.util.PottsEnums.Direction;

/** A plane in 3D space. */
public final class Plane {
    /** A point on the plane. */
    public final Voxel referencePoint;

    /** The unit normal vector to the plane. */
    public final Double3D unitNormalVector;

    /**
     * Creates a plane from a point and a vector.
     *
     * @param voxel a point on the plane
     * @param normalVector the normal vector to the plane
     */
    public Plane(Voxel voxel, Double3D normalVector) {
        this.referencePoint = voxel;
        this.unitNormalVector = scaleVector(normalVector);
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
     * Determines the magnitude of the provided vector.
     *
     * @param vector the vector
     * @return the magnitude of the normal vector
     */
    static double getVectorMagnitude(Double3D vector) {
        return Math.sqrt(
                vector.getX() * vector.getX()
                        + vector.getY() * vector.getY()
                        + vector.getZ() * vector.getZ());
    }

    /**
     * Scales the provided vector to a unit vector.
     *
     * @param vector the normal vector
     * @return the unit normal vector
     */
    static Double3D scaleVector(Double3D vector) {
        double magnitude = getVectorMagnitude(vector);
        double scaledX = vector.getX() / magnitude;
        double scaledY = vector.getY() / magnitude;
        double scaledZ = vector.getZ() / magnitude;
        return new Double3D(scaledX, scaledY, scaledZ);
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
    public double signedDistanceToPlane(Voxel point) {
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

    /**
     * Rotates a unit normal vector around an axis by a given angle.
     *
     * <p>Rotation is performed using Rodrigues' rotation formula. Any non-unit normal vector
     * provided is scaled to a unit vector before rotation.
     *
     * @param normalVector the normal vector
     * @param axis the axis of rotation
     * @param thetaDegrees the angle of rotation in degrees
     * @return the rotated normal vector
     */
    public static Double3D rotateUnitVectorAroundAxis(
            Double3D vector, Direction axis, double thetaDegrees) {
        Double3D unitVector = scaleVector(vector);
        double thetaRadians = Math.toRadians(thetaDegrees);

        // Normalize the axis vector
        Double3D unitAxisVector = scaleVector(axis.vector);
        double uX = unitAxisVector.getX();
        double uY = unitAxisVector.getY();
        double uZ = unitAxisVector.getZ();

        double cosTheta = Math.cos(thetaRadians);
        double sinTheta = Math.sin(thetaRadians);

        // Compute the dot product (k • v)
        double dotProduct = uX * unitVector.x + uY * unitVector.y + uZ * unitVector.z;

        // Compute the cross product (k × v)
        double crossX = uY * unitVector.z - uZ * unitVector.y;
        double crossY = uZ * unitVector.x - uX * unitVector.z;
        double crossZ = uX * unitVector.y - uY * unitVector.x;

        // Compute the rotated Normal vector components
        double rotatedX =
                unitVector.x * cosTheta + crossX * sinTheta + uX * dotProduct * (1 - cosTheta);
        double rotatedY =
                unitVector.y * cosTheta + crossY * sinTheta + uY * dotProduct * (1 - cosTheta);
        double rotatedZ =
                unitVector.z * cosTheta + crossZ * sinTheta + uZ * dotProduct * (1 - cosTheta);

        return new Double3D(rotatedX, rotatedY, rotatedZ);
    }
}
