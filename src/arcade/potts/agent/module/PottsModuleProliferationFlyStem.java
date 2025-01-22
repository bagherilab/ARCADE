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
import arcade.potts.util.PottsEnums.Direction;
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
        splitDirectionDistribution =
                (NormalDistribution)
                        parameters.getDistribution("proliferation/DIV_ROTATION_DISTRIBUTION");
    }

    public static Voxel getCellSplitLocation(PottsCellFlyStem cell) {
        // Prepare info to get location of split
        ArrayList<Integer> splitOffsetPercent = new ArrayList<>();
        splitOffsetPercent.add(cell.stemType.splitOffsetPercentX);
        splitOffsetPercent.add(cell.stemType.splitOffsetPercentY);
        PottsLocation location = (PottsLocation) cell.getLocation();
        return location.getOffset(splitOffsetPercent);
    }

    public Plane getDivisionPlaneWithRotationalVariance(PottsCellFlyStem cell) {
        Double3D plainSplitNormal = cell.stemType.splitDirection.vector;
        Double3D rotatedNormalVector =
                Plane.rotateVectorAroundAxis(
                        plainSplitNormal, Direction.XY_PLANE, getDivisionPlaneRotationOffset());
        return new Plane(getCellSplitLocation(cell), rotatedNormalVector);
    }

    Plane getDivisionPlaneDeterministic(PottsCellFlyStem cell) {
        return new Plane(getCellSplitLocation(cell), cell.stemType.splitDirection);
    }

    double getDivisionPlaneRotationOffset() {
        return splitDirectionDistribution.nextDouble();
    }

    public static Location getSmallerLocation(Location location1, Location location2) {
        if (location1.getVolume() < location2.getVolume()) {
            return location1;
        } else {
            return location2;
        }
    }

    private PottsLocation getApicalLocation(PottsLocation location1, PottsLocation location2) {
        double[] centroid1 = location1.getCentroid();
        double[] centroid2 = location2.getCentroid();
        if (centroid1[1] < centroid2[1]) {
            return location2;
        } else {
            return location1;
        }
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
        Plane divisionPlane = getDivisionPlaneWithRotationalVariance(flyStemCell);
        double splitProbability = flyStemCell.stemType.splitSelectionProbability;

        // Split current location
        Location newLocation =
                ((PottsLocation2D) cell.getLocation())
                        .split(random, divisionPlane, splitProbability);
        Location originalLocation = cell.getLocation();
        Location smallerLocation = getSmallerLocation(newLocation, originalLocation);

        // Reset current cell
        cell.reset(potts.ids, potts.regions);

        // Create and schedule new cell
        int newID = sim.getID();
        CellContainer newContainer = cell.make(newID, State.PROLIFERATIVE, random);
        PottsCell newCell =
                (PottsCell) newContainer.convert(sim.getCellFactory(), newLocation, random);
        sim.getGrid().addObject(newCell, null);
        potts.register(newCell);
        newCell.reset(potts.ids, potts.regions);
        newCell.schedule(sim.getSchedule());
    }
}
