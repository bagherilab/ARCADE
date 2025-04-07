package arcade.potts.agent.module;

import java.util.ArrayList;
import sim.util.Double3D;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellContainer;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.Parameters;
import arcade.core.util.Plane;
import arcade.core.util.Vector;
import arcade.core.util.distributions.NormalDistribution;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.agent.cell.PottsCellFlyStem;
import arcade.potts.env.location.PottsLocation;
import arcade.potts.env.location.PottsLocation2D;
import arcade.potts.env.location.Voxel;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import arcade.potts.util.PottsEnums.Direction;
import arcade.potts.util.PottsEnums.State;

/**
 * Extension of {@link PottsModuleProliferationSimple} with a custom addCell method for fly stem
 * cell behavior.
 */
public class PottsModuleProliferationFlyStem extends PottsModuleProliferationSimple {

    /** Distribution that determines rotational offset of cell's division plane. */
    final NormalDistribution splitDirectionDistribution;

    /** Ruleset for determining which daughter cell is the GMC. Can be `volume` or `location`. */
    final String differentiationRuleset;

    /**
     * Creates a simple proliferation {@code Module} for the given {@link PottsCellFlyStem}.
     *
     * @param cell the {@link PottsCellFlyStem} the module is associated with
     */
    public PottsModuleProliferationFlyStem(PottsCellFlyStem cell) {
        super(cell);
        Parameters parameters = cell.getParameters();

        splitDirectionDistribution =
                (NormalDistribution)
                        parameters.getDistribution("proliferation/DIV_ROTATION_DISTRIBUTION");
        differentiationRuleset = parameters.getString("proliferation/DIFFERENTIATION_RULESET");
    }

    /**
     * Gets the voxel location the cell's plane of division will pass through.
     *
     * @param cell the {@link PottsCellFlyStem} to get the division location for
     * @return the voxel location where the cell will split
     */
    public static Voxel getCellSplitLocation(PottsCellFlyStem cell) {
        ArrayList<Integer> splitOffsetPercent = new ArrayList<>();
        splitOffsetPercent.add(cell.getStemType().splitOffsetPercentX);
        splitOffsetPercent.add(cell.getStemType().splitOffsetPercentY);
        return ((PottsLocation) cell.getLocation()).getOffset(splitOffsetPercent);
    }

    /**
     * Gets the division plane for the cell after rotating the plane according to
     * splitDirectionDistribution. The plane is rotated around the XY plane.
     *
     * @param cell the {@link PottsCellFlyStem} to get the division plane for
     * @return the division plane for the cell
     */
    public Plane getDivisionPlaneWithRotationalVariance(PottsCellFlyStem cell) {
        Vector plainSplitNormal = cell.getStemType().splitDirection.vector;
        Vector rotatedNormalVector =
                Vector.rotateVectorAroundAxis(
                        plainSplitNormal,
                        Direction.XY_PLANE.vector,
                        getDivisionPlaneRotationOffset());
        return new Plane(
                new Double3D(
                        getCellSplitLocation(cell).x,
                        getCellSplitLocation(cell).y,
                        getCellSplitLocation(cell).z),
                rotatedNormalVector);
    }

    /**
     * Gets the rotation offset for the division plane according to splitDirectionDistribution.
     *
     * @return the rotation offset for the division plane
     */
    double getDivisionPlaneRotationOffset() {
        return splitDirectionDistribution.nextDouble();
    }

    /**
     * Gets the smaller location with fewer voxels and returns it.
     *
     * @param location1 the {@link PottsLocation} to compare to location2.
     * @param location2 {@link PottsLocation} to compare to location1.
     * @return the smaller location.
     */
    public static PottsLocation getSmallerLocation(
            PottsLocation location1, PottsLocation location2) {
        if (location1.getVolume() < location2.getVolume()) {
            return location1;
        } else {
            return location2;
        }
    }

    /**
     * Gets the location with the lower centroid and returns it.
     *
     * @param location1 {@link PottsLocation} to compare to location2.
     * @param location2 {@link PottsLocation} to compare to location1.
     * @return the basal location.
     */
    public static PottsLocation getBasalLocation(PottsLocation location1, PottsLocation location2) {
        double[] centroid1 = location1.getCentroid();
        double[] centroid2 = location2.getCentroid();
        if (centroid1[1] > centroid2[1]) {
            return location1;
        } else {
            return location2;
        }
    }

    @Override
    void addCell(MersenneTwisterFast random, Simulation sim) {
        Potts potts = ((PottsSimulation) sim).getPotts();

        PottsCellFlyStem flyStemCell = (PottsCellFlyStem) cell;
        Plane divisionPlane = getDivisionPlaneWithRotationalVariance(flyStemCell);

        // Split current location
        PottsLocation daughterLoc =
                (PottsLocation) ((PottsLocation2D) cell.getLocation()).split(random, divisionPlane);
        PottsLocation stemLoc = (PottsLocation) cell.getLocation();

        Location gmcLoc = null;

        // logic to determine which location should be with which cell());
        if (differentiationRuleset.equals("volume")) {
            gmcLoc = getSmallerLocation(daughterLoc, stemLoc);
        } else if (differentiationRuleset.equals("location")) {
            gmcLoc = getBasalLocation(daughterLoc, stemLoc);
        } else {
            throw new IllegalArgumentException(
                    "Invalid differentiation ruleset: " + differentiationRuleset);
        }

        // if the gmc location is currently assigned to parent cell, swap the voxels
        // with the daughter cell location
        if (stemLoc == gmcLoc) {
            PottsLocation.swapVoxels(
                    (PottsLocation) cell.getLocation(),
                    daughterLoc); // swaps the voxels of the two locations
        }

        // Reset current cell
        cell.reset(potts.ids, potts.regions);

        // Create and schedule new cell
        int newID = sim.getID();
        CellContainer newContainer = cell.make(newID, State.PROLIFERATIVE, random);
        PottsCell newCell =
                (PottsCell) newContainer.convert(sim.getCellFactory(), daughterLoc, random);
        sim.getGrid().addObject(newCell, null);
        potts.register(newCell);
        newCell.reset(potts.ids, potts.regions);
        newCell.schedule(sim.getSchedule());
    }
}
