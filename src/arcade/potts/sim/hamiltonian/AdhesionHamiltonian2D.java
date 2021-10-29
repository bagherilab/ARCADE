package arcade.potts.sim.hamiltonian;

import arcade.potts.agent.cell.PottsCell;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSeries;
import static arcade.core.util.Enums.Region;

/**
 * Extension of {@link AdhesionHamiltonian} for 2D.
 */

public class AdhesionHamiltonian2D extends AdhesionHamiltonian {
    /** Neighborhood size. */
    public static final int NEIGHBORHOOD_SIZE = (3 * 3) - 1;
    
    /**
     * Creates the adhesion energy term for the {@code Potts} Hamiltonian in 2D.
     *
     * @param potts  the associated Potts instance
     * @param series  the associated Series instance
     */
    public AdhesionHamiltonian2D(Potts potts, PottsSeries series) { super(potts, series); }
    
    @Override
    double getAdhesion(int id, int x, int y, int z) {
        double h = 0;
        PottsCell a = potts.getCell(id);
        
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                if (!(i == x && j == y) && potts.ids[z][i][j] != id) {
                    PottsCell b = potts.getCell(potts.ids[z][i][j]);
                    if (a == null) {
                        h += 0; // TODO get adhesion of B:0 from config
                    } else if (b == null) {
                        h += 0; // TODO get adhesion of A:0 from config
                    } else {
                        h += 0; // TODO get adhesion of A:B from config
                    }
                }
            }
        }
        
        return h / NEIGHBORHOOD_SIZE;
    }
    
    @Override
    double getAdhesion(int id, int t, int x, int y, int z) {
        double h = 0;
        PottsCell c = potts.getCell(id);
        Region region = Region.values()[t];
        
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                Region xy = Region.values()[potts.regions[z][i][j]];
                if (!(i == x && j == y) && potts.ids[z][i][j] == id && xy != region
                        && xy != Region.UNDEFINED && xy != Region.DEFAULT) {
                    h += 0; // TODO get adhesion of regions from substrate
                }
            }
        }
        
        return h / NEIGHBORHOOD_SIZE;
    }
}
