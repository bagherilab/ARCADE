package arcade.patch.agent.module;

import arcade.core.agent.process.ProcessDomain;
import arcade.core.sim.Simulation;
import arcade.patch.sim.PatchSimulation;
import arcade.core.util.MiniBox;
import arcade.patch.agent.cell.PatchCell;
import arcade.patch.agent.cell.PatchCellCART;
import arcade.patch.agent.cell.PatchCellTissue;
import arcade.patch.agent.process.PatchProcess;
import arcade.patch.agent.process.PatchProcessInflammation;
import arcade.patch.util.PatchEnums.AntigenFlag;
import arcade.patch.util.PatchEnums.Domain;
import arcade.patch.util.PatchEnums.State;
import ec.util.MersenneTwisterFast;
import sim.engine.SimState;

// collaprse reset with kill

public class PatchModuleKill extends PatchModule  {
    /** The {@link PatchCell} the module is associated with. */

    //immune cell itself
    PatchCellCART cellImmune;
	
	/** Target cell cytotoxic CAR T-cell is bound to */
	PatchCell target;
	
	/** CAR T-cell inflammation module */
	PatchProcessInflammation inflammation;
	
	/** Amount of granzyme inside CAR T-cell */
	double granzyme;

    /**
     * Creates a proliferation {@link PatchModule} for the given cell.
     * <p>
     * Loaded parameters include:
     * <ul>
     *     <li>{@code SYNTHESIS_DURATION} = time required for DNA synthesis</li>
     * </ul>
     *
     * @param cell  the {@link PatchCell} the module is associated with
     */
    public PatchModuleKill(PatchCell inputCell, PatchCell target) {
        super(inputCell);
        //I have to override this.cell bc patchmodule can only take patch cells
        this.cellImmune = (PatchCellCART) inputCell;
        this.target = target;
        this.inflammation = (PatchProcessInflammation) cell.getProcess(Domain.INFLAMMATION);
        this.granzyme = inflammation.getInternal("granzyme");
    }

    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {
        PatchSimulation growthSim = (PatchSimulation) sim;
            
        // If current CAR T-cell is stopped, stop helper.
        if (cellImmune.isStopped()) { return; }
            
        // If bound target cell is stopped, stop helper.

        //All cells need isStopped method
        if (target.isStopped()) { stop(); return; }
            
        if (granzyme >= 1) {
            // Kill bound target cell.
            PatchCellTissue tissueCell = (PatchCellTissue) target;

            //setState?
            tissueCell.setState(State.APOPTOTIC);

            //what is this
            growthSim.lysedCells.add(recordLysis(sim, target));
                
            // Use up some granzyme in the process.
            granzyme--;
            inflammation.setInternal("granzyme", granzyme);
            reset(cellImmune);
		} 
    }

     /**
	 * Stops the helper from if can't kill target.
	 * 
	 * @param sim  the simulation instance
	 */
	private void stop() {
		// Unbind from target.
        if (cellImmune.binding == AntigenFlag.BOUND_ANTIGEN_CELL_RECEPTOR) {
            cellImmune.setAntigenFlag(AntigenFlag.BOUND_CELL_RECEPTOR);
        } else {
            cellImmune.setAntigenFlag(AntigenFlag.UNBOUND);
        }
		// Stop helper and set cell type to neutral.
		cell.setState(State.QUIESCENT);
	}

    /**
	 * Steps the helper for killing target cell.
	 * <p>
	 * The String is formatted as:
	 * <pre>
	 *     [time of death, [location], [ code, pop, type, position, volume, age, [ list, of, cycle, lengths, ... ] ] ]
	 * </pre>
	 */
	private String recordLysis(PatchSimulation sim, PatchCell cell) {
        //what is the toJSON equivalent in 3.0?
        
        //TODO: get toJSON version of the stuff
		return "[" + sim.getSchedule().getTime() + "," + cell.getLocation() + "," + cell + "]";
	}

    //for now this is unscheduled...
    private void reset(PatchCellCART cell) {
        if (cell.getState() == State.CYTOTOXIC || cell.getState() == State.STIMULATORY) {
			// Return to neutral state  and reset flags to make a new state decision.
            cell.setAntigenFlag(AntigenFlag.UNDEFINED);
            cell.setState(State.QUIESCENT);
		}
    }
}
