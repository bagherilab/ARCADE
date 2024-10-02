package arcade.potts.util;

import java.util.EnumMap;
import java.util.Map;
import arcade.core.util.Plane;
import arcade.core.util.Point3D;
import arcade.core.util.Vector3D;
import static arcade.potts.util.PottsEnums.Direction;

/**
 * Factory class for creating Potts Planes based on a Direction and a given point.
 */
public class PottsPlaneFactory {

    // Mapping between Direction Enum and normal vectors
    private static final Map<Direction, Vector3D> normalVectorMap = new EnumMap<>(Direction.class);

    static {
        normalVectorMap.put(Direction.YZ_PLANE, new Vector3D(1, 0, 0));
        normalVectorMap.put(Direction.ZX_PLANE, new Vector3D(0, 1, 0));
        normalVectorMap.put(Direction.XY_PLANE, new Vector3D(0, 0, 1));
        normalVectorMap.put(Direction.POSITIVE_XY, new Vector3D(1, 1, 0));
        normalVectorMap.put(Direction.NEGATIVE_XY, new Vector3D(-1, 1, 0));
        normalVectorMap.put(Direction.POSITIVE_YZ, new Vector3D(0, 1, 1));
        normalVectorMap.put(Direction.NEGATIVE_YZ, new Vector3D(0, -1, 1));
        normalVectorMap.put(Direction.POSITIVE_ZX, new Vector3D(1, 0, 1));
        normalVectorMap.put(Direction.NEGATIVE_ZX, new Vector3D(-1, 0, 1));
    }

    /**
     * Creates a Plane based on a Direction and a specified point.
     *
     * @param direction  the Direction
     * @param point      the point on the plane
     * @return  a Plane
     */
    public static Plane createPlane(Direction direction, Point3D point) {
        Vector3D normalVector = normalVectorMap.get(direction);
        if (normalVector == null) {
            throw new IllegalArgumentException("No normal vector associated with this direction");
        }
        return new Plane(point, normalVector);
    }

    /**
     * Retrieves the normal vector associated with a Direction.
     *
     * @param direction  the Direction
     * @return  the normal vector
     */
    public static Vector3D getNormalVector(Direction direction) {
        return normalVectorMap.get(direction);
    }
}