package arcade.patch.env.loc;

import java.util.ArrayList;

/**
 * Concrete implementation of {@link PatchLocationFactory} for rectangular
 * geometry.
 * <p>
 * Cell agents exist on a rectangular grid and molecules in the environment
 * diffuse on a smaller rectangular lattice, such that each grid rectangle
 * corresponds to 4 lattice rectangles. The rectangular locations are defined in
 * the (x, y, z) coordinate space such that (0,0,0) is the rectangle in the
 * center of the simulation (for the grid) and the left rectangle of the center
 * slice (for the lattices).
 */

public final class PatchLocationFactoryRect extends PatchLocationFactory {
    /**
     * Creates a factory for rectangular {@link PatchLocation} instances.
     */
    public PatchLocationFactoryRect() { super(); }
    
    @Override
    public ArrayList<Coordinate> getCoordinates(int radius, int depth) {
        ArrayList<Coordinate> coordinates = new ArrayList<>();
        
        for (int x = 1 - radius; x < radius; x++) {
            for (int y = 1 - radius; y < radius; y++) {
                for (int z = 1 - depth; z < depth; z++) {
                    coordinates.add(new CoordinateRect(x, y, z));
                }
            }
        }
        
        return coordinates;
    }
}
