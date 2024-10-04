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
public final class PottsPlaneFactory {

    /** Mapping between Direction Enum and normal vectors */
    private static final Map<Direction, Vector3D> NORMAL_VECTOR_MAP = new EnumMap<>(Direction.class);

    static {
        NORMAL_VECTOR_MAP.put(Direction.YZ_PLANE, new Vector3D(1, 0, 0));
        NORMAL_VECTOR_MAP.put(Direction.ZX_PLANE, new Vector3D(0, 1, 0));
        NORMAL_VECTOR_MAP.put(Direction.XY_PLANE, new Vector3D(0, 0, 1));
        NORMAL_VECTOR_MAP.put(Direction.POSITIVE_XY, new Vector3D(-1, 1, 0));
        NORMAL_VECTOR_MAP.put(Direction.NEGATIVE_XY, new Vector3D(-1, -1, 0));
        NORMAL_VECTOR_MAP.put(Direction.POSITIVE_YZ, new Vector3D(0, -1, 1));
        NORMAL_VECTOR_MAP.put(Direction.NEGATIVE_YZ, new Vector3D(0, -1, -1));
        NORMAL_VECTOR_MAP.put(Direction.POSITIVE_ZX, new Vector3D(1, 0, -1));
        NORMAL_VECTOR_MAP.put(Direction.NEGATIVE_ZX, new Vector3D(-1, 0, -1));
    }

    /** Private constructor to prevent instantiation */
    private PottsPlaneFactory() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Creates a Plane based on a Direction and a specified point.
     *
     * @param direction  the Direction
     * @param point      the point on the plane
     * @return  a Plane
     */
    public static Plane createPlane(Point3D point, Direction direction) {
        Vector3D normalVector = NORMAL_VECTOR_MAP.get(direction);
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
        return NORMAL_VECTOR_MAP.get(direction);
    }
}
