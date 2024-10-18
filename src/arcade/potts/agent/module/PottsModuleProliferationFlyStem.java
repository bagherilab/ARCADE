package arcade.potts.agent.module;

import java.util.ArrayList;
import ec.util.MersenneTwisterFast;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.agent.cell.PottsCellFlyStem;
import arcade.potts.env.location.PottsLocation2D;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import static arcade.potts.agent.cell.PottsCellFlyStem.StemType;
import static arcade.potts.util.PottsEnums.Direction;
import static arcade.potts.util.PottsEnums.State;

/**
 * Extension of {@link PottsModuleProliferationSimple} with a custom addCell method for fly stem
 * cell behavior.
 */
public class PottsModuleProliferationFlyStem extends PottsModuleProliferationSimple {
    /**
     * Creates a simple proliferation {@code Module} for the given {@link PottsCellFlyStem}.
     *
     * @param cell the {@link PottsCellFlyStem} the module is associated with
     */
    public PottsModuleProliferationFlyStem(PottsCellFlyStem cell) {
        super(cell); // Reuse the logic from PottsModuleProliferationSimple
    }

    /**
     * {@inheritDoc}
     *
     * <p>The addCell method is overridden to implement specific behavior for fly stem cells in the
     * proliferation process. Parameters are passed to the {@link PottsLocation2D#split} method
     * according to the {@link PottsCellFlyStem} {@link StemType}
     */
    @Override
    void addCell(MersenneTwisterFast random, Simulation sim) {
        Potts potts = ((PottsSimulation) sim).getPotts();

        PottsCellFlyStem flyStemCell = (PottsCellFlyStem) cell;
        StemType stemtype = flyStemCell.stemType;
        ArrayList<Integer> splitOffsetPercent = new ArrayList<>();
        splitOffsetPercent.add(stemtype.splitOffsetPercentX);
        splitOffsetPercent.add(stemtype.splitOffsetPercentY);
        Direction splitDirection = stemtype.splitDirection;
        double splitProbability = stemtype.splitSelectionProbability;

        // Split current location
        Location newLocation =
                ((PottsLocation2D) cell.getLocation())
                        .split(random, splitOffsetPercent, splitDirection, splitProbability);

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
