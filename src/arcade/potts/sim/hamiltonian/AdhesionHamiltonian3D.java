package arcade.potts.sim.hamiltonian;

import arcade.potts.agent.cell.PottsCell;
import arcade.potts.sim.Potts;
import static arcade.core.util.Enums.Region;

/**
 * Extension of {@link AdhesionHamiltonian} for 3D.
 */

public class AdhesionHamiltonian3D extends AdhesionHamiltonian {
    /** Neighborhood size. */
    public static final int NEIGHBORHOOD_SIZE = (3 * 3 * 3) - 1;
    
    /**
     * Creates the adhesion energy term for the {@code Potts} Hamiltonian in 3D.
     *
     * @param potts  the associated Potts instance
     */
    public AdhesionHamiltonian3D(Potts potts) { super(potts); }
    
    @Override
    double getAdhesion(int id, int x, int y, int z) {
        double h = 0;
        PottsCell a = potts.getCell(id);
        
        for (int k = z - 1; k <= z + 1; k++) {
            for (int i = x - 1; i <= x + 1; i++) {
                for (int j = y - 1; j <= y + 1; j++) {
                    if (!(k == z && i == x && j == y) && potts.ids[k][i][j] != id) {
                        PottsCell b = potts.getCell(potts.ids[k][i][j]);
                        if (a == null) {
                            h += b.getAdhesion(0);
                        }  else if (b == null) {
                            h += a.getAdhesion(0);
                        } else {
                            h += (a.getAdhesion(b.getPop()) + b.getAdhesion(a.getPop())) / 2.0;
                        }
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
        
        for (int k = z - 1; k <= z + 1; k++) {
            for (int i = x - 1; i <= x + 1; i++) {
                for (int j = y - 1; j <= y + 1; j++) {
                    Region xyz = Region.values()[potts.regions[k][i][j]];
                    if (!(k == z && i == x && j == y) && potts.ids[k][i][j] == id && region != xyz
                            && xyz != Region.UNDEFINED && xyz != Region.DEFAULT) {
                        h += (c.getAdhesion(region, xyz) + c.getAdhesion(xyz, region)) / 2.0;
                    }
                }
            }
        }
        
        return h / NEIGHBORHOOD_SIZE;
    }
}
