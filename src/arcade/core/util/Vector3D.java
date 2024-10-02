package arcade.core.util;

/**
 * A 3D vector.
 */
public class Vector3D {

    private final int a,b,c;

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

    public int getA() { return a; }
    public int getB() { return b; }
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
}