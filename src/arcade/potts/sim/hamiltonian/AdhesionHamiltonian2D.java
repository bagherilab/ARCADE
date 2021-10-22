package arcade.potts.sim.hamiltonian;

import arcade.potts.agent.cell.PottsCell;
import arcade.potts.sim.Potts;
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
     */
    public AdhesionHamiltonian2D(Potts potts) { super(potts); }
    
    @Override
    double getAdhesion(int id, int x, int y, int z) {
        double h = 0;
        PottsCell a = potts.getCell(id);
        
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                if (!(i == x && j == y) && potts.ids[z][i][j] != id) {
                    PottsCell b = potts.getCell(potts.ids[z][i][j]);
                    if (a == null) {
                        h += b.getAdhesion(0);
                    } else if (b == null) {
                        h += a.getAdhesion(0);
                    } else {
                        h += (a.getAdhesion(b.getPop()) + b.getAdhesion(a.getPop())) / 2.0;
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
                    h += (c.getAdhesion(region, xy) + c.getAdhesion(xy, region)) / 2.0;
                }
            }
        }
        
        return h / NEIGHBORHOOD_SIZE;
    }
}
