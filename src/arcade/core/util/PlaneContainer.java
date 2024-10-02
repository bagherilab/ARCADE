package arcade.core.util;

/**
 * Container class for a plane.
 */
public class PlaneContainer {
    private final Point3D point;
    private final Vector3D normalVector;

    public PlaneContainer(Point3D point, Vector3D normalVector) {
        this.point = point;
        this.normalVector = normalVector;
    }

    public Point3D getPoint() {
        return point;
    }

    public Vector3D getNormalVector() {
        return normalVector;
    }

    public Plane toPlane() {
        return new Plane(point, normalVector);
    }
}
