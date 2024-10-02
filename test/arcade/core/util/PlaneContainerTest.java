package arcade.core.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class PlaneContainerTest {

    @Test
    public void constructor_givenPointAndNormalVector_returnsCorrectContainer() {
        Point3D point = new Point3D(1, 2, 3);
        Vector3D normalVector = new Vector3D(4, 5, 6);

        PlaneContainer container = new PlaneContainer(point, normalVector);

        assertEquals(point, container.getPoint());
        assertEquals(normalVector, container.getNormalVector());
    }

    @Test
    public void getPoint_called_returnsCorrectPoint() {
        Point3D point = new Point3D(1, 2, 3);
        Vector3D normalVector = new Vector3D(4, 5, 6);
        PlaneContainer container = new PlaneContainer(point, normalVector);

        assertEquals(point, container.getPoint());
    }

    @Test
    public void getNormalVector_called_returnsCorrectNormalVector() {
        Point3D point = new Point3D(1, 2, 3);
        Vector3D normalVector = new Vector3D(4, 5, 6);
        PlaneContainer container = new PlaneContainer(point, normalVector);

        assertEquals(normalVector, container.getNormalVector());
    }

    @Test
    public void toPlane_called_returnsCorrectPlane() {
        Point3D point = new Point3D(1, 2, 3);
        Vector3D normalVector = new Vector3D(4, 5, 6);
        PlaneContainer container = new PlaneContainer(point, normalVector);

        Plane plane = container.toPlane();

        assertEquals(point, plane.point);
        assertEquals(normalVector, plane.normalVector);
    }
}
