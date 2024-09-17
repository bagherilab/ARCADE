package arcade.potts.agent.module;

import ec.util.MersenneTwisterFast;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.agent.cell.PottsCellFlyStem;
import arcade.potts.env.location.PottsLocation2D;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import static arcade.potts.util.PottsEnums.State;
import static arcade.potts.util.PottsEnums.Direction;
import static arcade.potts.util.PottsFlyEnums.StemDaughter;

/**
 * Extension of {@link PottsModuleProliferationSimple} with a custom addCell
 * method for fly stem cell behavior.
 */

public class PottsModuleProliferationFlyStem extends PottsModuleProliferationSimple {
    /**
     * Creates a simple proliferation {@code Module} for the given
     * {@link PottsCellFlyStem}.
     *
     * @param cell  the {@link PottsCellFlyStem} the module is associated with
     */
    public PottsModuleProliferationFlyStem(PottsCellFlyStem cell) {
        super(cell);  // Reuse the logic from PottsModuleProliferationSimple
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * The addCell method is overridden to implement specific behavior for
     * fly stem cells in the proliferation process.
     */
    @Override
    void addCell(MersenneTwisterFast random, Simulation sim) {
        Potts potts = ((PottsSimulation) sim).getPotts();
        
        PottsCellFlyStem flyStemCell = (PottsCellFlyStem) cell;
        int splitOffset = flyStemCell.splitOffsetPercent;
        Direction splitDirection = flyStemCell.splitDirection;
        StemDaughter stemDaughter = flyStemCell.stemDaughter;

        // Split current location using splitFly method
        Location newLocation = ((PottsLocation2D) cell.getLocation())
                .splitFly(random, splitOffset, splitDirection, stemDaughter);
        
        // Reset current cell
        cell.reset(potts.ids, potts.regions);
        
        // Create and schedule new cell
        int newID = sim.getID();
        PottsCell newCell = (PottsCell) cell.make(newID, State.PROLIFERATIVE, newLocation, random);
        sim.getGrid().addObject(newCell, null);
        potts.register(newCell);
        newCell.reset(potts.ids, potts.regions);
        newCell.schedule(sim.getSchedule());
    }
}