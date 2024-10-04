package arcade.core.util;

/**
 * Container class for a plane.
 */
public class PlaneContainer {
    /** A point on the plane. */
    private final Point3D point;

    /** The normal vector to the plane. */
    private final Vector3D normalVector;
    
    /**
     * Creates a PlaneContainer from a point and a vector.
     *
     * @param point  a point on the plane
     * @param normalVector  the normal vector to the plane
     */
    public PlaneContainer(Point3D point, Vector3D normalVector) {
        this.point = point;
        this.normalVector = normalVector;
    }
    
    /**
     * Retrieves the point on the plane.
     *
     * @return the point
     */
    public Point3D getPoint() {
        return point;
    }
    
    /**
     * Retrieves the normal vector to the plane.
     *
     * @return the normal vector
     */
    public Vector3D getNormalVector() {
        return normalVector;
    }
    
    /**
     * Converts the PlaneContainer to a Plane.
     *
     * @return the Plane
     */
    public Plane toPlane() {
        return new Plane(point, normalVector);
    }
}
