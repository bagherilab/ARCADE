package arcade.patch.env.lat;

import java.util.HashMap;
import java.util.Map;
import arcade.core.env.lat.Lattice;
import arcade.core.env.operation.Operation;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import sim.engine.Schedule;
import sim.engine.SimState;
import static arcade.core.util.Enums.Category;
import static arcade.patch.util.PatchEnums.Ordering;

/** 
 * Implementation of {@link Lattice} for patch models.
 * <p>
 * {@code PatchLattice} agents can call two {@link Operation} categories:
 * diffusers and generators.
 * Diffusers diffuse values on the underlying array.
 * Generators add values to the underlying array.
 * <p>
 * General order of rules for the {@code PatchLattice} step:
 * <ul>
 *     <li>step generator operation</li>
 *     <li>step diffuser operation</li>
 * </ul>
 */

public abstract class PatchLattice implements Lattice {
    /** Array containing lattice values */
    protected final double[][][] field;
    
    /** Length of the array (x direction) */
    private final int length;
    
    /** Width of the array (y direction) */
    private final int width;
    
    /** Depth of the array (z direction) */
    private final int depth;
    
    /** Map of operation categories and {@link Operation} instance. */
    protected final Map<Category, Operation> operations;
    
    /** Lattice parameters. */
    final MiniBox parameters;
    
    /**
     * Creates a {@code PatchLattice} environment.
     *
     * @param length  the length of array (x direction)
     * @param width  the width of array (y direction)
     * @param depth  the depth of array (z direction)
     * @param parameters  the dictionary of parameters
     */
    public PatchLattice(int length, int width, int depth, MiniBox parameters) {
        this.length = length;
        this.width = width;
        this.depth = depth;
        this.operations = new HashMap<>();
        this.parameters = parameters;
        this.field = new double[depth][length][width];
    }
    
    @Override
    public double[][][] getField() { return field; }
    
    @Override
    public double[][][] getCopy() {
        double[][][] copy = new double[depth][length][width];
        for (int k = 0; k < depth; k++) {
            for (int i = 0; i < length; i++) {
                System.arraycopy(field[k][i], 0, copy[k][i], 0, width);
            }
        }
        return copy;
    }
    
    @Override
    public int getLength() { return length; }
    
    @Override
    public int getWidth() { return width; }
    
    @Override
    public int getDepth() { return depth; }
    
    @Override
    public Operation getOperation(String key) { return operations.get(Category.valueOf(key)); }
    
    @Override
    public MiniBox getParameters() { return parameters; }
    
    @Override
    public void setField(double[][] values, int index) {
        for (int i = 0; i < values.length; i++) {
            field[index][i] = values[i].clone();
        }
    }
    
    @Override
    public void setField(double[][][] values) {
        for (int k = 0; k < values.length; k++) {
            setField(values[k], k);
        }
    }
    
    @Override
    public void setField(double value) {
        for (int k = 0; k < depth; k++) {
            for (int i = 0; i < length; i++) {
                for (int j = 0; j < width; j++) { field[k][i][j] = value; }
            }
        }
    }
    
    @Override
    public void setOperation(Category category, Operation operation) {
        operations.put(category, operation);
    }
    
    @Override
    public void schedule(Schedule schedule) {
        schedule.scheduleRepeating(this, Ordering.LATTICES.ordinal(), 1);
    }
    
    /**
     * Steps through cell rules.
     *
     * @param simstate  the MASON simulation state
     */
    @Override
    public void step(SimState simstate) {
        Simulation sim = (Simulation) simstate;
        
        // Step generator operation, if it exists.
        if (operations.containsKey(Category.GENERATOR)) {
            operations.get(Category.GENERATOR).step(simstate.random, sim);
        }
        
        // Step diffuser operation, if it exists.
        if (operations.containsKey(Category.DIFFUSER)) {
            operations.get(Category.DIFFUSER).step(simstate.random, sim);
        }
    }
}