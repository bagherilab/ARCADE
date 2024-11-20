package arcade.potts.agent.module;

import java.util.ArrayList;
import sim.util.Double3D;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellContainer;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.Parameters;
import arcade.core.util.distributions.NormalDistribution;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.agent.cell.PottsCellFlyStem;
import arcade.potts.agent.cell.PottsCellFlyStem.StemType;
import arcade.potts.env.location.Plane;
import arcade.potts.env.location.PottsLocation;
import arcade.potts.env.location.PottsLocation2D;
import arcade.potts.env.location.Voxel;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import arcade.potts.util.PottsEnums.State;
import static arcade.potts.util.PottsEnums.State;

/**
 * Extension of {@link PottsModuleProliferationSimple} with a custom addCell method for fly stem
 * cell behavior.
 */
public class PottsModuleProliferationFlyStem extends PottsModuleProliferationSimple {

    final NormalDistribution splitDirectionDistribution;

    /**
     * Creates a simple proliferation {@code Module} for the given {@link PottsCellFlyStem}.
     *
     * @param cell the {@link PottsCellFlyStem} the module is associated with
     */
    public PottsModuleProliferationFlyStem(PottsCellFlyStem cell) {
        super(cell); // Reuse the logic from PottsModuleProliferationSimple
        Parameters parameters = cell.getParameters();
        splitDirectionDistribution = (NormalDistribution) parameters.getDistribution("proliferation/DIV_ROTATION_DISTRIBUTION");
    }

    public static Voxel getCellSplitLocation(PottsCellFlyStem cell) {
        // Prepare info to get location of split
        ArrayList<Integer> splitOffsetPercent = new ArrayList<>();
        splitOffsetPercent.add(cell.stemType.splitOffsetPercentX);
        splitOffsetPercent.add(cell.stemType.splitOffsetPercentY);
        PottsLocation location = (PottsLocation) cell.getLocation();
        return location.getOffset(splitOffsetPercent);
    }

    public Plane getDivisionPlaneWithRotationalVariance(
            PottsCellFlyStem cell, double stdevDegrees, MersenneTwisterFast random) {

        // Get original split direction
        Double3D plainSplitNormal = cell.stemType.splitDirection.vector;

        // Create and return the new Plane
        return new Plane(
                getCellSplitLocation(cell),
                Plane.probablisticallyRotateNormalVector(plainSplitNormal, stdevDegrees, random));
    }

    Plane getDivisionPlaneDeterministic(PottsCellFlyStem cell, MersenneTwisterFast random) {
        return new Plane(getCellSplitLocation(cell), cell.stemType.splitDirection);
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
        Plane divisionPlane = getDivisionPlaneWithRotationalVariance(flyStemCell, 30.0, random);
        double splitProbability = flyStemCell.stemType.splitSelectionProbability;

        // Split current location
        Location newLocation =
                ((PottsLocation2D) cell.getLocation())
                        .split(random, divisionPlane, splitProbability);

        // Reset current cell
        cell.reset(potts.ids, potts.regions);

        // Create and schedule new cell
        int newID = sim.getID();
        CellContainer newContainer = cell.make(newID, State.PROLIFERATIVE, random);
        PottsCell newCell = (PottsCell) newContainer.convert(sim.getCellFactory(), newLocation, random);
        sim.getGrid().addObject(newCell, null);
        potts.register(newCell);
        newCell.reset(potts.ids, potts.regions);
        newCell.schedule(sim.getSchedule());
    }
}