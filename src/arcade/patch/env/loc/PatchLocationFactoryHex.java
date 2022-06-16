package arcade.patch.env.loc;

import java.util.ArrayList;

/**
 * Concrete implementation of {@link PatchLocationFactory} for hexagonal geometry.
 * <p>
 * Cell agents exist on a hexagonal grid and molecules in the environment diffuse
 * on a triangular lattice, such that each hexagon corresponds to 6 triangles.
 * The hexagonal locations are defined in the (u, v, w, z) coordinate space such
 * that (0,0,0,0) is the hexagon in the center of the simulation.
 * The triangular locations are defined in the (x, y, z) coordinate space such that
 * (0,0,0) is the triangle in the top left of the center slice of the simulation.
 * Because environment radius is guaranteed to be even, the top left triangle of
 * the corresponding triangular lattices is always pointed down.
 */

public final class PatchLocationFactoryHex extends PatchLocationFactory {
    public PatchLocationFactoryHex() { super(); }
    
    @Override
    public ArrayList<int[]> getCoordinates(int radius, int depth) {
        ArrayList<int[]> locations = new ArrayList<>();
        
        for (int u = 1 - radius; u < radius; u++) {
            for (int v = 1 - radius; v < radius; v++) {
                for (int w = 1 - radius; w < radius; w++) {
                    if (u + v + w == 0) {
                        for (int z = 1 - depth; z < depth; z++) {
                            locations.add(new int[] { u, v, w, z });
                        }
                    }
                }
            }
        }
        
        return locations;
    }
}

