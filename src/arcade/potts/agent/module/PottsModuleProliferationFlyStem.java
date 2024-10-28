package arcade.potts.agent.module;

import java.util.ArrayList;
import sim.util.Int3D;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellContainer;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.agent.cell.PottsCellFlyStem;
import arcade.potts.agent.cell.PottsCellFlyStem.StemType;
import arcade.potts.env.location.Plane;
import arcade.potts.env.location.PottsLocation;
import arcade.potts.env.location.PottsLocation2D;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import arcade.potts.util.PottsEnums.Direction;
import arcade.potts.util.PottsEnums.State;
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

    Plane getDivisionPlaneDeterministic(PottsCellFlyStem cell) {
        ArrayList<Integer> splitOffsetPercent = new ArrayList<>();
        splitOffsetPercent.add(cell.stemType.splitOffsetPercentX);
        splitOffsetPercent.add(cell.stemType.splitOffsetPercentY);
        Direction splitDirection = cell.stemType.splitDirection;
        PottsLocation location = (PottsLocation) cell.getLocation();
        return new Plane(location.getOffset(splitOffsetPercent), splitDirection);
    }

    /**
     * Generates a division plane for a PottsCellFlyStem with rotational variance.
     *
     * @param cell The cell for which to generate the division plane.
     * @param meanDegrees The mean rotation angle in degrees.
     * @param varianceDegrees The variance of the rotation angle in degrees squared.
     * @return A Plane object representing the division plane.
     */
    public Plane getDivisionPlaneWithRotationalVariance(
            PottsCellFlyStem cell, double meanDegrees, double varianceDegrees) {
        ArrayList<Integer> splitOffsetPercent = new ArrayList<>();
        splitOffsetPercent.add(cell.stemType.splitOffsetPercentX);
        splitOffsetPercent.add(cell.stemType.splitOffsetPercentY);

        // Original split direction (assuming [0,1,0])
        Direction splitDirection = cell.stemType.splitDirection;
        PottsLocation location = (PottsLocation) cell.getLocation();

        // Initialize Random instance
        MersenneTwisterFast random = new MersenneTwisterFast();

        // Sample theta from normal distribution: N(mean, variance)
        double thetaDegrees = meanDegrees + Math.sqrt(varianceDegrees) * random.nextGaussian();
        double thetaRadians = Math.toRadians(thetaDegrees); // Convert to radians

        // Compute rotated normal vector [-sin(theta), cos(theta), 0]
        int rotatedX = (int) Math.round(-Math.sin(thetaRadians));
        int rotatedY = (int) Math.round(Math.cos(thetaRadians));
        int rotatedZ = 0;

        // Create new Direction object with rotated normal vector
        Int3D rotatedDirection = new Int3D(rotatedX, rotatedY, rotatedZ);

        // Create and return the new Plane
        return new Plane(location.getOffset(splitOffsetPercent), rotatedDirection);
    }

    public Plane getDivisionPlaneWithRandomX(PottsCellFlyStem cell) {
        ArrayList<Integer> splitOffsetPercent = new ArrayList<>();
        splitOffsetPercent.add(cell.stemType.splitOffsetPercentX);
        splitOffsetPercent.add(cell.stemType.splitOffsetPercentY);

        // Original split direction (assuming [0,1,0])
        Direction splitDirection = cell.stemType.splitDirection;
        Int3D plainSplitNormal = splitDirection.vector;
        PottsLocation location = (PottsLocation) cell.getLocation();

        // set x to [-1, 0, 1] with equal probability
        MersenneTwisterFast random = new MersenneTwisterFast();
        int x = random.nextInt(3) - 1;
        Int3D splitNormal = new Int3D(x, plainSplitNormal.y, plainSplitNormal.z);
        // Create and return the new Plane
        return new Plane(location.getOffset(splitOffsetPercent), splitNormal);
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
        Plane divisionPlane = getDivisionPlaneDeterministic(flyStemCell);
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
        PottsCell newCell = (PottsCell) newContainer.convert(sim.getCellFactory(), newLocation);
        sim.getGrid().addObject(newCell, null);
        potts.register(newCell);
        newCell.reset(potts.ids, potts.regions);
        newCell.schedule(sim.getSchedule());
    }
}
