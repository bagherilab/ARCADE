package arcade.core.util;

/**
 * A 3D vector.
 */
public class Vector3D {
    /** The x component of the vector */
    private final int a;
    /** The y component of the vector */
    private final int b;
    /** The z component of the vector */
    private final int c;

    /**
     * Creates a 3D vector.
     *
     * @param a  the x component
     * @param b  the y component
     * @param c  the z component
     */
    public Vector3D(int a, int b, int c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    /**
     * Retrieves the x component of the vector.
     *
     * @return the x component
     */
    public int getA() { return a; }

    /**
     * Retrieves the y component of the vector.
     *
     * @return the y component
     */
    public int getB() { return b; }

    /**
     * Retrieves the z component of the vector.
     *
     * @return the z component
     */
    public int getC() { return c; }

    /**
     * Determines if two vectors have the same (a, b, c) components.
     *
     * @param obj  the vector to compare
     * @return {@code true} if the vectors are equal, {@code false} otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Vector3D other = (Vector3D) obj;
        return a == other.a && b == other.b && c == other.c;
    }

    /**
     * Generates a hash code for the vector.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + this.a;
        hash = 53 * hash + this.b;
        hash = 53 * hash + this.c;
        return hash;
    }
}
