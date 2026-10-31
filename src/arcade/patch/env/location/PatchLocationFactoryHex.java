package arcade.patch.env.location;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import arcade.patch.util.PatchEnums.Direction;

/**
 * Concrete implementation of {@link PatchLocationFactory} for hexagonal geometry.
 *
 * <p>Cell agents exist on a hexagonal grid and molecules in the environment diffuse on a triangular
 * lattice, such that each hexagon corresponds to 6 triangles. The hexagonal locations are defined
 * in the (u, v, w, z) coordinate space such that (0,0,0,0) is the hexagon in the center of the
 * simulation. The triangular locations are defined in the (x, y, z) coordinate space such that
 * (0,0,0) is the triangle in the top left of the center slice of the simulation. Because
 * environment radius is guaranteed to be even, the top left triangle of the corresponding
 * triangular lattices is always pointed down.
 */
public final class PatchLocationFactoryHex extends PatchLocationFactory {
    /**
     * Unit offsets in the (u, v, w) coordinate space for each valid hexagonal direction, ordered
     * clockwise from north.
     *
     * <p>Hexagons are flat-topped, such that a coordinate has neighbors directly above and below
     * it, but not directly to the left or right. There are therefore no east or west directions.
     *
     * <p>Offsets are given such that north is toward the top of the rendered simulation, which
     * corresponds to decreasing (w - v).
     */
    private static final Map<Direction, int[]> DIRECTIONS = makeDirections();

    /** Creates a factory for hexagonal {@link PatchLocation} instances. */
    public PatchLocationFactoryHex() {
        super();
    }

    /**
     * Creates the map of directions to unit offsets.
     *
     * @return a map of direction to (u, v, w) unit offset
     */
    private static Map<Direction, int[]> makeDirections() {
        EnumMap<Direction, int[]> directions = new EnumMap<>(Direction.class);
        directions.put(Direction.CENTER, new int[] {0, 0, 0});
        directions.put(Direction.N, new int[] {0, -1, 1});
        directions.put(Direction.NE, new int[] {1, -1, 0});
        directions.put(Direction.SE, new int[] {1, 0, -1});
        directions.put(Direction.S, new int[] {0, 1, -1});
        directions.put(Direction.SW, new int[] {-1, 1, 0});
        directions.put(Direction.NW, new int[] {-1, 0, 1});
        return directions;
    }

    @Override
    public ArrayList<Coordinate> getCoordinates(
            int radius, int depth, Direction direction, int offset) {
        ArrayList<Coordinate> coordinates = new ArrayList<>();
        CoordinateUVWZ center = makeCenter(direction, offset, radius);

        for (int u = 1 - radius; u < radius; u++) {
            for (int v = 1 - radius; v < radius; v++) {
                for (int w = 1 - radius; w < radius; w++) {
                    if (u + v + w == 0) {
                        for (int z = 1 - depth; z < depth; z++) {
                            coordinates.add(
                                    new CoordinateUVWZ(
                                            u + center.u, v + center.v, w + center.w, z));
                        }
                    }
                }
            }
        }

        return coordinates;
    }

    /**
     * Creates the center coordinate of a region offset from the center of the simulation.
     *
     * <p>Because each direction is a unit offset in the (u, v, w) coordinate space, scaling by the
     * offset preserves the constraint that the coordinate values sum to zero.
     *
     * @param direction the direction to offset along
     * @param offset the number of locations to offset by
     * @param radius the bound on the radius of the region
     * @return the center coordinate of the region
     */
    private CoordinateUVWZ makeCenter(Direction direction, int offset, int radius) {
        int[] unit = DIRECTIONS.get(direction);

        if (unit == null) {
            throw new IllegalArgumentException(
                    "Direction [ "
                            + direction
                            + " ] is not valid for hexagonal geometry, must be one of "
                            + DIRECTIONS.keySet());
        }

        int clamped = clampOffset(Math.abs(offset), radius);
        return new CoordinateUVWZ(unit[0] * clamped, unit[1] * clamped, unit[2] * clamped, 0);
    }
}
