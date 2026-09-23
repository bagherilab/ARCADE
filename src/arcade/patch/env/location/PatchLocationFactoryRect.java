package arcade.patch.env.location;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import arcade.patch.util.PatchEnums.Direction;

/**
 * Concrete implementation of {@link PatchLocationFactory} for rectangular geometry.
 *
 * <p>Cell agents exist on a rectangular grid and molecules in the environment diffuse on a smaller
 * rectangular lattice, such that each grid rectangle corresponds to 4 lattice rectangles. The
 * rectangular locations are defined in the (x, y, z) coordinate space such that (0,0,0) is the
 * rectangle in the center of the simulation (for the grid) and the left rectangle of the center
 * slice (for the lattices).
 */
public final class PatchLocationFactoryRect extends PatchLocationFactory {
    /**
     * Unit offsets in the (x, y) coordinate space for each valid rectangular direction, ordered
     * clockwise from north.
     *
     * <p>Offsets are given such that north is toward the top of the rendered simulation, which
     * corresponds to decreasing y.
     */
    private static final Map<Direction, int[]> DIRECTIONS = makeDirections();

    /** Creates a factory for rectangular {@link PatchLocation} instances. */
    public PatchLocationFactoryRect() {
        super();
    }

    /**
     * Creates the map of directions to unit offsets.
     *
     * @return a map of direction to (x, y) unit offset
     */
    private static Map<Direction, int[]> makeDirections() {
        EnumMap<Direction, int[]> directions = new EnumMap<>(Direction.class);
        directions.put(Direction.CENTER, new int[] {0, 0});
        directions.put(Direction.N, new int[] {0, -1});
        directions.put(Direction.E, new int[] {1, 0});
        directions.put(Direction.S, new int[] {0, 1});
        directions.put(Direction.W, new int[] {-1, 0});
        return directions;
    }

    @Override
    public ArrayList<Coordinate> getCoordinates(
            int radius, int depth, Direction direction, int offset) {
        ArrayList<Coordinate> coordinates = new ArrayList<>();
        CoordinateXYZ center = makeCenter(direction, offset, radius);

        for (int x = 1 - radius; x < radius; x++) {
            for (int y = 1 - radius; y < radius; y++) {
                for (int z = 1 - depth; z < depth; z++) {
                    coordinates.add(new CoordinateXYZ(x + center.x, y + center.y, z));
                }
            }
        }

        return coordinates;
    }

    /**
     * Creates the center coordinate of a region offset from the center of the simulation.
     *
     * @param direction the direction to offset along
     * @param offset the number of locations to offset by
     * @param radius the bound on the radius of the region
     * @return the center coordinate of the region
     */
    private CoordinateXYZ makeCenter(Direction direction, int offset, int radius) {
        int[] unit = DIRECTIONS.get(direction);

        if (unit == null) {
            throw new IllegalArgumentException(
                    "Direction [ "
                            + direction
                            + " ] is not valid for rectangular geometry, must be one of "
                            + DIRECTIONS.keySet());
        }

        int clamped = clampOffset(Math.abs(offset), radius);
        return new CoordinateXYZ(unit[0] * clamped, unit[1] * clamped, 0);
    }
}
