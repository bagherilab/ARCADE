package arcade.potts.sim.hamiltonian;

import arcade.potts.sim.Potts;
import static arcade.potts.sim.Potts2D.MOVES_X;
import static arcade.potts.sim.Potts2D.MOVES_Y;
import static arcade.potts.sim.Potts2D.NUMBER_NEIGHBORS;

/**
 * Extension of {@link SurfaceHamiltonian} for 2D.
 */

public class SurfaceHamiltonian2D extends SurfaceHamiltonian {
    /**
     * Creates the surface energy term for the {@code Potts} Hamiltonian in 2D.
     *
     * @param potts  the associated Potts instance
     */
    public SurfaceHamiltonian2D(Potts potts) { super(potts); }
    
    @Override
    int[] calculateChange(int sourceID, int targetID, int x, int y, int z) {
        int beforeSource = 0;
        int afterSource = 0;
        int beforeTarget = 0;
        int afterTarget = 0;
        
        // Iterate through each neighbor.
        for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
            int neighbor = potts.ids[z][x + MOVES_X[i]][y + MOVES_Y[i]];
            
            if (neighbor != sourceID) {
                beforeSource++;
                if (neighbor == targetID) { beforeTarget++; }
            }
            
            if (neighbor != targetID) {
                afterTarget++;
                if (neighbor == sourceID) { afterSource++; }
            }
        }
        
        // Save changes to surface.
        int sourceSurfaceChange = afterSource - beforeSource;
        int targetSurfaceChange = afterTarget - beforeTarget;
        
        return new int[] { sourceSurfaceChange, targetSurfaceChange };
    }
    
    @Override
    int[] calculateChange(int id, int sourceRegion, int targetRegion, int x, int y, int z) {
        int beforeSource = 0;
        int afterSource = 0;
        int beforeTarget = 0;
        int afterTarget = 0;
        
        // Iterate through each neighbor.
        for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
            int neighborID = potts.ids[z][x + MOVES_X[i]][y + MOVES_Y[i]];
            int neighborRegion = potts.regions[z][x + MOVES_X[i]][y + MOVES_Y[i]];
            
            if (neighborRegion != sourceRegion || neighborID != id) {
                beforeSource++;
                if (neighborRegion == targetRegion && neighborID == id) { beforeTarget++; }
            }
            
            if (neighborRegion != targetRegion || neighborID != id) {
                afterTarget++;
                if (neighborRegion == sourceRegion && neighborID == id) { afterSource++; }
            }
        }
        
        // Save changes to surface.
        int sourceSurfaceChange = afterSource - beforeSource;
        int targetSurfaceChange = afterTarget - beforeTarget;
        
        return new int[] { sourceSurfaceChange, targetSurfaceChange };
    }
}
