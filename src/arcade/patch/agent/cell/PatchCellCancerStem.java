package arcade.patch.agent.cell;

import java.util.Map;
import arcade.core.sim.Simulation;
import arcade.core.agent.module.Module;
import arcade.core.env.loc.Location;
import arcade.core.util.Parameter;
import arcade.core.util.MiniBox;

/**
 * Extension of {@link arcade.agent.cell.PatchCellCancer} for cancerous stem cells.
 * <p>
 * {@code PatchCellCancerStem} agents are modified from their superclass:
 * <ul>
 *     <li>Code change to {@code CODE_S_CELL}</li>
 *     <li>Cells are immortal (death age set to maximum)</li>
 *     <li>Asymmetric division with probability of producing another stem cell
 *     ({@code PatchCellCancerStem}) or a cancerous cell ({@code PatchCellCancer})</li>
 *     <li>No division limit</li>
 * </ul>
 */

public class PatchCellCancerStem extends PatchCellCancer {
    /** Serialization version identifier */
    private static final long serialVersionUID = 0;
    
    /**
     * Creates a cancer stem cell {@link arcade.agent.cell.PatchCell} agent given
     * specific module versions.
     * <p>
     * Changes the cell agent code to cancer stem cell and life span to the 
     * maximum integer value (i.e. cell is immortal).
     *
     * @param sim  the simulation instance
     * @param pop  the population index
     * @param loc  the location of the cell 
     * @param vol  the initial (and critical) volume of the cell
     * @param age  the initial age of the cell in minutes
     * @param params  the map of parameter name to {@link arcade.core.util.Parameter} objects
     * @param box  the map of module name to version
     */
    public PatchCellCancerStem(Simulation sim, int pop, Location loc, double vol, 
                       int age, Map<String, Parameter> params, MiniBox box) {
        super(sim, pop, loc, vol, age, params, box);
        code = Cell.CODE_S_CELL;
        deathAge = Integer.MAX_VALUE;
    }
    
    /**
     * Creates a cancer stem cell {@link arcade.agent.cell.PatchCell} agent given
     * the modules of the parent cell.
     * <p>
     * Constructor uses reflection to create constructors based on the
     * existing {@link arcade.core.agent.module.Module} objects.
     * Changes the cell agent code to cancer stem cell and life span to the 
     * maximum integer value (i.e. cell is immortal).
     *
     * @param sim  the simulation instance
     * @param parent  the parent cell
     * @param f  the fractional reduction
     */
    public PatchCellCancerStem(Simulation sim, PatchCell parent, double f) {
        super(sim, parent, f);
        code = Cell.CODE_S_CELL;
        deathAge = Integer.MAX_VALUE;
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Cells have a certain probability of producing another cancer stem cell.
     */
    public Cell newCell(Simulation sim, Cell parent, double f) {
        return checkDivision(sim, this) ?
            new PatchCellCancerStem(sim, (PatchCell)parent, f) : new PatchCellCancer(sim, (PatchCell)parent, f);
    }
    
    /**
     * Checks if stem cell division is symmetric.
     * <p>
     * 
     * @param sim  the simulation instance
     * @param c  the parent cell
     * @return  {@code true} if daughter cell is a stem cell, {@code false} otherwise
     */
    private static boolean checkDivision(Simulation sim, PatchCell c) {
        if (c.divisions < 1) { c.divisions++; } // update division count
        return sim.getRandom() < sim.getSeries().getParam(c.getPop(), "DIVISION_PROB"); // random value
    }
}