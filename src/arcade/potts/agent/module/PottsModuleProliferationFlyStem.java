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
import static arcade.potts.agent.cell.PottsCellFlyStem.StemType;

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
     * Range of values considered equal when determining daughter cell identity. ex. if ruleset is
     * location, range determines the distance between centroid y values that is considered equal.
     */
    final double range;

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
        range = parameters.getDouble("proliferation/DIFFERENTIATION_RULESET_EQUALITY_RANGE");
    }

    /**
     * Gets the voxel location the cell's plane of division will pass through.
     *
     * @param cell the {@link PottsCellFlyStem} to get the division location for
     * @return the voxel location where the cell will split
     */
    public static Voxel getCellSplitLocation(StemType stemType, PottsCell cell) {
        ArrayList<Integer> splitOffsetPercent = new ArrayList<>();
        splitOffsetPercent.add(stemType.splitOffsetPercentX);
        splitOffsetPercent.add(stemType.splitOffsetPercentY);
        return ((PottsLocation) cell.getLocation()).getOffset(splitOffsetPercent);
    }

    /**
     * Gets the division plane for the cell after rotating the plane according to
     * splitDirectionDistribution. This follows WT division rules. The plane is rotated around the
     * XY plane.
     *
     * @param cell the {@link PottsCellFlyStem} to get the division plane for
     * @param rotationOffset the angle to rotate the plane
     * @return the division plane for the cell
     */
    public Plane getWTDivisionPlaneWithRotationalVariance(
            PottsCellFlyStem cell, double rotationOffset) {
        Vector plainSplitNormal = StemType.WT.splitDirection.vector;
        Vector rotatedNormalVector =
                Vector.rotateVectorAroundAxis(
                        plainSplitNormal, Direction.XY_PLANE.vector, rotationOffset);
        return new Plane(
                new Double3D(
                        getCellSplitLocation(StemType.WT, cell).x,
                        getCellSplitLocation(StemType.WT, cell).y,
                        getCellSplitLocation(StemType.WT, cell).z),
                rotatedNormalVector);
    }

    /**
     * Gets the division plane for the cell. This follows MUDMUT division rules. The division plane
     * is not rotated.
     *
     * @param cell the {@link PottsCellFlyStem} to get the division plane for
     * @return the division plane for the cell
     */
    public Plane getMUDDivisionPlane(PottsCellFlyStem cell) {
        Vector plainSplitNormal = StemType.MUDMUT.splitDirection.vector;
        return new Plane(
                new Double3D(
                        getCellSplitLocation(StemType.MUDMUT, cell).x,
                        getCellSplitLocation(StemType.MUDMUT, cell).y,
                        getCellSplitLocation(StemType.MUDMUT, cell).z),
                plainSplitNormal);
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

        Plane divisionPlane = null;
        double offset = getDivisionPlaneRotationOffset();

        if (flyStemCell.getStemType() == StemType.WT
                || (flyStemCell.getStemType() == StemType.MUDMUT && Math.abs(offset) < 45)) {
            divisionPlane = getWTDivisionPlaneWithRotationalVariance(flyStemCell, offset);
        } else if (flyStemCell.getStemType() == StemType.MUDMUT) {
            divisionPlane = getMUDDivisionPlane(flyStemCell);
        }

        // Split current location
        PottsLocation daughterLoc =
                (PottsLocation) ((PottsLocation2D) cell.getLocation()).split(random, divisionPlane);
        PottsLocation parentLoc = (PottsLocation) cell.getLocation();

        // Determine if daughter should be stem
        boolean isDaughterStem = daughterStem(parentLoc, daughterLoc);

        // If daughter is not stem, determine which location is GMC and make cells
        if (isDaughterStem == false) {

            Location gmcLoc = null;

            // logic to determine which location should daughter cell
            if (differentiationRuleset.equals("volume")) {
                gmcLoc = getSmallerLocation(daughterLoc, parentLoc);
            } else if (differentiationRuleset.equals("location")) {
                gmcLoc = getBasalLocation(daughterLoc, parentLoc);
            } else {
                throw new IllegalArgumentException(
                        "Invalid differentiation ruleset: " + differentiationRuleset);
            }

            // if the gmc location is currently assigned to parent cell, swap the voxels
            // with the daughter cell location
            if (parentLoc == gmcLoc) {
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
        } else if (isDaughterStem) {
            // If daughter is stem, call make with parent pop as newPop
            // Reset current cell
            cell.reset(potts.ids, potts.regions);
            // Create and schedule new cell
            int newID = sim.getID();
            CellContainer newContainer =
                    ((PottsCellFlyStem) cell)
                            .make(newID, State.PROLIFERATIVE, random, cell.getPop());
            PottsCell newCell =
                    (PottsCell) newContainer.convert(sim.getCellFactory(), daughterLoc, random);
            sim.getGrid().addObject(newCell, null);
            potts.register(newCell);
            newCell.reset(potts.ids, potts.regions);
            newCell.schedule(sim.getSchedule());
        }
    }

    public boolean daughterStem(PottsLocation Location1, PottsLocation Location2) {
        if (((PottsCellFlyStem) cell).getStemType() == StemType.WT) {
            return false;
        } else if (((PottsCellFlyStem) cell).getStemType() == StemType.MUDMUT) {
            if (differentiationRuleset.equals("volume")) {
                double vol1 = Location1.getVolume();
                double vol2 = Location2.getVolume();
                if (Math.abs(vol1 - vol2) < range) {
                    return true;
                } else {
                    return false;
                }
            } else if (differentiationRuleset.equals("location")) {
                double[] centroid1 = Location1.getCentroid();
                double[] centroid2 = Location2.getCentroid();
                if (Math.abs(centroid1[1] - centroid2[1]) < range) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        throw new IllegalArgumentException(
                "Invalid differentiation ruleset: " + differentiationRuleset);
    }
}
