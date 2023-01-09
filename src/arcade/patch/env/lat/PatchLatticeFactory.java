package arcade.patch.env.lat;

import java.util.ArrayList;
import java.util.HashMap;
import ec.util.MersenneTwisterFast;
import arcade.core.env.operation.Operation;
import arcade.core.sim.Series;
import arcade.core.util.MiniBox;
import arcade.patch.sim.PatchSeries;
import static arcade.core.util.Enums.Category;

public abstract class PatchLatticeFactory {
    /** Random number generator instance. */
    MersenneTwisterFast random;
    
    /** Map of id to lattice. */
    public final HashMap<String, PatchLattice> lattices;
    
    /**
     * Creates a factory for making {@link PatchLattice} instances.
     */
    public PatchLatticeFactory() {
        lattices = new HashMap<>();
    }
    
    public void initialize(Series series, MersenneTwisterFast random) {
        this.random = random;
        createLattices(series);
    }
    
    public void createLattices(Series series) {
        int length = series.length;
        int width = series.width;
        int depth = ((PatchSeries) series).depthBounds;
        
        double dxy = ((PatchSeries) series).dxy;
        double dz = ((PatchSeries) series).dz;
        
        for (String key : series.layers.keySet()) {
            double initialValue = 0;
            
            MiniBox parameters = series.layers.get(key);
            PatchLattice lattice = getLattice(length, width, depth, parameters);
            lattice.setField(initialValue);
            
            MiniBox layer = series.layers.get(key);
            
            // Get operations (if they exist).
            MiniBox operationBox = layer.filter("(OPERATION)");
            ArrayList<String> operationKeys = operationBox.getKeys();
            
            // Create operation instances.
            if (operationKeys.size() > 0) {
                for (String operationKey : operationKeys) {
                    Category category = Category.valueOf(operationKey);
                    Operation operation = getOperation(category, lattice, dxy, dz);
                    lattice.setOperation(category, operation);
                }
            }
            
            lattices.put(key, lattice);
        }
    }
    
    public abstract PatchLattice getLattice(int length, int width, int depth, MiniBox parameters);
    
    public abstract Operation getOperation(Category category, PatchLattice lattice,
                                           double dxy, double dz);
}
