package arcade.potts.util;

import java.util.EnumMap;
import java.util.Map;
import arcade.core.util.Plane;
import arcade.core.util.Point3D;
import arcade.core.util.Vector3D;
import arcade.core.util.PlaneContainer;
import static arcade.potts.util.PottsEnums.Direction;

/**
 * Factory class for creating Potts Planes based on a Direction.
 */
public class PottsPlaneFactory {

    // Mapping between Direction Enum and Plane parameters
    private static final Map<Direction, PlaneContainer> planeParametersMap = new EnumMap<>(Direction.class);

    static {
        planeParametersMap.put(Direction.YZ_PLANE, new PlaneContainer(new Point3D(0, 0, 0), new Vector3D(1, 0, 0)));
        planeParametersMap.put(Direction.ZX_PLANE, new PlaneContainer(new Point3D(0, 0, 0), new Vector3D(0, 1, 0)));
        planeParametersMap.put(Direction.XY_PLANE, new PlaneContainer(new Point3D(0, 0, 0), new Vector3D(0, 0, 1)));
        planeParametersMap.put(Direction.POSITIVE_XY, new PlaneContainer(new Point3D(0, 0, 0), new Vector3D(1, 1, 0)));
        planeParametersMap.put(Direction.NEGATIVE_XY, new PlaneContainer(new Point3D(0, 0, 0), new Vector3D(-1, 1, 0)));
        planeParametersMap.put(Direction.POSITIVE_YZ, new PlaneContainer(new Point3D(0, 0, 0), new Vector3D(0, 1, 1)));
        planeParametersMap.put(Direction.NEGATIVE_YZ, new PlaneContainer(new Point3D(0, 0, 0), new Vector3D(0, -1, 1)));
        planeParametersMap.put(Direction.POSITIVE_ZX, new PlaneContainer(new Point3D(0, 0, 0), new Vector3D(1, 0, 1)));
        planeParametersMap.put(Direction.NEGATIVE_ZX, new PlaneContainer(new Point3D(0, 0, 0), new Vector3D(-1, 0, 1)));
    }

    /**
     * Creates a Plane based on a Direction.
     *
     * @param direction  the Direction
     * @return  a Plane
     */
    public static Plane createPlane(Direction direction) {
        PlaneContainer params = planeParametersMap.get(direction);
        if (params == null) {
            throw new IllegalArgumentException("No plane associated with this direction");
        }
        return new Plane(params.getPoint(), params.getNormalVector());
    }
}
