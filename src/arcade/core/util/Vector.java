package arcade.core.util;

import sim.util.Double3D;

/** A 3D vector. */
public class Vector {
    /** The (x,y,z) values of the vector. */
    public final Double3D vector;

    /**
     * Creates a vector from its components.
     *
     * @param x the x component
     * @param y the y component
     * @param z the z component
     */
    public Vector(double x, double y, double z) {
        this.vector = new Double3D(x, y, z);
    }

    /**
     * Creates a vector from a Double3D.
     *
     * @param vector the Double3D
     */
    public Vector(Double3D vector) {
        this.vector = vector;
    }

    /**
     * Gets the x component of the vector.
     *
     * @return the x component
     */
    public double getX() {
        return vector.x;
    }

    /**
     * Gets the y component of the vector.
     *
     * @return the y component
     */
    public double getY() {
        return vector.y;
    }

    /**
     * Gets the z component of the vector.
     *
     * @return the z component
     */
    public double getZ() {
        return vector.z;
    }

    /**
     * Determines the magnitude of the provided vector.
     *
     * @param vector the vector
     * @return the magnitude of the vector
     */
    public static double getVectorMagnitude(Vector vector) {
        double xsquared = vector.getX() * vector.getX();
        double ysquared = vector.getY() * vector.getY();
        double zsquared = vector.getZ() * vector.getZ();
        return Math.sqrt(xsquared + ysquared + zsquared);
    }

    /**
     * Scales the provided vector to a unit vector.
     *
     * @param vector the vector
     * @return the unit vector
     */
    public static Vector normalizeVector(Vector vector) {
        double magnitude = getVectorMagnitude(vector);
        if (magnitude == 0) {
            return new Vector(0, 0, 0);
        }
        double scaledX = vector.getX() / magnitude;
        double scaledY = vector.getY() / magnitude;
        double scaledZ = vector.getZ() / magnitude;
        return new Vector(scaledX, scaledY, scaledZ);
    }

    /**
     * Determines the dot product of two vectors.
     *
     * @param vector1 the first vector
     * @param vector2 the second vector
     * @return the dot product
     */
    public static double dotProduct(Vector vector1, Vector vector2) {
        return vector1.getX() * vector2.getX()
                + vector1.getY() * vector2.getY()
                + vector1.getZ() * vector2.getZ();
    }

    /**
     * Determines the cross product of two vectors.
     *
     * @param vector1 the first vector
     * @param vector2 the second vector
     * @return the cross product
     */
    public static Vector crossProduct(Vector vector1, Vector vector2) {
        double x = vector1.getY() * vector2.getZ() - vector1.getZ() * vector2.getY();
        double y = vector1.getZ() * vector2.getX() - vector1.getX() * vector2.getZ();
        double z = vector1.getX() * vector2.getY() - vector1.getY() * vector2.getX();
        return new Vector(x, y, z);
    }

    /**
     * Rotates a given vector around a given axis by a given angle.
     *
     * <p>Rotation is performed using Rodrigues' rotation formula.
     *
     * @param vector the vector
     * @param rotationAxis the axis of rotation
     * @param thetaDegrees the angle of rotation in degrees
     * @return the rotated vector scaled to unit length
     */
    public static Vector rotateVectorAroundAxis(
            Vector vector, Vector rotationAxis, double thetaDegrees) {
        double thetaRadians = Math.toRadians(thetaDegrees);
        // Normalize the axis vector
        Vector unitRotationAxis = normalizeVector(rotationAxis);

        double cosTheta = Math.cos(thetaRadians);
        double sinTheta = Math.sin(thetaRadians);

        // Compute the dot product (k • v)
        double dotProduct = dotProduct(unitRotationAxis, vector);
        // Compute the cross product (k × v)
        Vector crossProduct = crossProduct(unitRotationAxis, vector);

        // Compute the rotated Normal vector components
        double rotatedX =
                vector.getX() * cosTheta
                        + crossProduct.getX() * sinTheta
                        + unitRotationAxis.getX() * dotProduct * (1 - cosTheta);
        double rotatedY =
                vector.getY() * cosTheta
                        + crossProduct.getY() * sinTheta
                        + unitRotationAxis.getY() * dotProduct * (1 - cosTheta);
        double rotatedZ =
                vector.getZ() * cosTheta
                        + crossProduct.getZ() * sinTheta
                        + unitRotationAxis.getZ() * dotProduct * (1 - cosTheta);
        return new Vector(rotatedX, rotatedY, rotatedZ);
    }

    /**
     * Determines if two vectors are equal.
     *
     * @param obj the object to compare
     * @return true if the vectors are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Vector)) {
            return false;
        }
        Vector other = (Vector) obj;
        return vector.equals(other.vector);
    }

    /**
     * Returns a hash code for the vector.
     *
     * @return a hash code for the vector
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Double.hashCode(vector.x);
        hash = 31 * hash + Double.hashCode(vector.y);
        hash = 31 * hash + Double.hashCode(vector.z);
        return hash;
    }
}
