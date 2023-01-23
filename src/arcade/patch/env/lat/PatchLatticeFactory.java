package arcade.patch.env.lat;

import java.util.ArrayList;
import java.util.HashMap;
import ec.util.MersenneTwisterFast;
import arcade.core.env.lat.LatticeFactory;
import arcade.core.sim.Series;
import arcade.core.util.MiniBox;
import arcade.patch.env.operation.PatchOperation;
import static arcade.core.util.Enums.Category;

/**
 * Implementation of{@link LatticeFactory} for {@link PatchLattice} objects.
 */

public abstract class PatchLatticeFactory implements LatticeFactory {
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
    
    @Override
    public void initialize(Series series, MersenneTwisterFast random) {
        this.random = random;
        createLattices(series);
    }
    
    @Override
    public void createLattices(Series series) {
        int length = series.length;
        int width = series.width;
        int height = series.height;
        
        for (String key : series.layers.keySet()) {
            MiniBox layer = series.layers.get(key);
            PatchLattice lattice = getLattice(length, width, height, layer);
            
            // Set initial lattice value.
            double initialValue = layer.getDouble("INITIAL_CONCENTRATION");
            lattice.setField(initialValue);
            
            // Get operations (if they exist).
            MiniBox operationBox = layer.filter("(OPERATION)");
            ArrayList<String> operationKeys = operationBox.getKeys();
            
            // Create operation instances.
            if (operationKeys.size() > 0) {
                for (String operationKey : operationKeys) {
                    Category category = Category.valueOf(operationKey);
                    PatchOperation operation = getOperation(category, lattice);
                    lattice.setOperation(category, operation);
                }
            }
            
            lattices.put(key, lattice);
        }
    }
    
    /**
     * Creates a new {@link PatchLattice} instance.
     *
     * @param length  the length of array (x direction)
     * @param width  the width of array (y direction)
     * @param height  the height of array (z direction)
     * @param parameters  the dictionary of parameters
     * @return  the lattice instance
     */
    public abstract PatchLattice getLattice(int length, int width, int height, MiniBox parameters);
    
    /**
     * Creates a new {@link PatchOperation} instance for the given category.
     *
     * @param category  the operation category
     * @param lattice  the associated lattice instance
     * @return  the operation instance
     */
    public abstract PatchOperation getOperation(Category category, PatchLattice lattice);
}
