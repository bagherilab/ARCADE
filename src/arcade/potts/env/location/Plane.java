package arcade.potts.env.location;

import sim.util.Double3D;
import arcade.potts.util.PottsEnums.Direction;

/** A plane in 3D space. */
public final class Plane {
    /** A point on the plane. */
    public final Voxel referencePoint;

    /** The unit normal vector to the plane. */
    public Double3D unitNormalVector;

    /**
     * Creates a plane from a point and a vector.
     *
     * @param voxel a point on the plane
     * @param normalVector the normal vector to the plane
     */
    public Plane(Voxel voxel, Double3D normalVector) {
        this.referencePoint = voxel;
        this.unitNormalVector = scaleVectorToUnitVector(normalVector);
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
     * @param normalVector the normal vector
     * @return the magnitude of the normal vector
     */
    static double getVectorMagnitude(Double3D normalVector) {
        return Math.sqrt(
                normalVector.getX() * normalVector.getX()
                        + normalVector.getY() * normalVector.getY()
                        + normalVector.getZ() * normalVector.getZ());
    }

    /**
     * Scales the normal vector to a unit vector.
     *
     * @param normalVector the normal vector
     * @return the unit normal vector
     */
    static Double3D scaleVectorToUnitVector(Double3D normalVector) {
        double magnitude = getVectorMagnitude(normalVector);
        double scaledX = normalVector.getX() / magnitude;
        double scaledY = normalVector.getY() / magnitude;
        double scaledZ = normalVector.getZ() / magnitude;
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

    public static Double3D rotateUnitNormalAroundAxis(
            Double3D normalVector, Direction axis, double thetaDegrees) {

        Double3D unitNormalVector = scaleVectorToUnitVector(normalVector);

        double thetaRadians = Math.toRadians(thetaDegrees);

        // Normalize the axis vector
        double axisLength =
                Math.sqrt(
                        axis.vector.getX() * axis.vector.getX()
                                + axis.vector.getY() * axis.vector.getY()
                                + axis.vector.getZ() * axis.vector.getZ());
        double uX = axis.vector.getX() / axisLength;
        double uY = axis.vector.getY() / axisLength;
        double uZ = axis.vector.getZ() / axisLength;

        double cosTheta = Math.cos(thetaRadians);
        double sinTheta = Math.sin(thetaRadians);

        // Compute the dot product (k • v)
        double dotProduct =
                uX * unitNormalVector.x + uY * unitNormalVector.y + uZ * unitNormalVector.z;

        // Compute the cross product (k × v)
        double crossX = uY * unitNormalVector.z - uZ * unitNormalVector.y;
        double crossY = uZ * unitNormalVector.x - uX * unitNormalVector.z;
        double crossZ = uX * unitNormalVector.y - uY * unitNormalVector.x;

        // Compute the rotated Normal vector components
        double rotatedX =
                unitNormalVector.x * cosTheta
                        + crossX * sinTheta
                        + uX * dotProduct * (1 - cosTheta);
        double rotatedY =
                unitNormalVector.y * cosTheta
                        + crossY * sinTheta
                        + uY * dotProduct * (1 - cosTheta);
        double rotatedZ =
                unitNormalVector.z * cosTheta
                        + crossZ * sinTheta
                        + uZ * dotProduct * (1 - cosTheta);

        return new Double3D(rotatedX, rotatedY, rotatedZ);
    }
}
