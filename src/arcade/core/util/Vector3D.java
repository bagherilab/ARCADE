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
}