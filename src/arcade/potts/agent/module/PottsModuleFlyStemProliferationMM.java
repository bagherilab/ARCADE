package arcade.potts.agent.module;

import sim.util.Double3D;
import arcade.core.util.Parameters;
import arcade.core.util.Plane;
import arcade.core.util.Vector;
import arcade.potts.agent.cell.PottsCellFlyStem;
import arcade.potts.env.location.PottsLocation;
import arcade.potts.env.location.Voxel;
import arcade.potts.util.PottsEnums;

public class PottsModuleFlyStemProliferationMM extends PottsModuleFlyStemProliferation {

    final int mmLikeX;

    final int mmLikeY;

    public PottsModuleFlyStemProliferationMM(PottsCellFlyStem cell) {
        super(cell);
        Parameters parameters = cell.getParameters();
        mmLikeX = parameters.getInt("proliferation/MM_LIKE_X");
        mmLikeY = parameters.getInt("proliferation/MM_LIKE_Y");
    }

    @Override
    protected Plane chooseDivisionPlane(PottsCellFlyStem flyStemCell) {
        double offset = sampleDivisionPlaneOffset();
        if (Math.abs(offset) < 45) {
            return getWTLikeDivisionPlaneWithRotationalVariance(flyStemCell, offset);
        }
        return getMUDDivisionPlane(flyStemCell);
    }

    /**
     * Gets the division plane for the cell. This follows MUDMUT division rules. The division plane
     * is not rotated.
     *
     * @param cell the {@link PottsCellFlyStem} to get the division plane for
     * @return the division plane for the cell
     */
    public Plane getMUDDivisionPlane(PottsCellFlyStem cell) {
        Vector defaultNormal =
                Vector.rotateVectorAroundAxis(
                        cell.getApicalAxis(),
                        PottsEnums.Direction.XY_PLANE.vector,
                        PottsCellFlyStem.StemType.MUDMUT.splitDirectionRotation);
        Voxel splitVoxel =
                getCellSplitVoxel(
                        PottsCellFlyStem.StemType.MUDMUT, cell, defaultNormal, mmLikeX, mmLikeY);
        return new Plane(new Double3D(splitVoxel.x, splitVoxel.y, splitVoxel.z), defaultNormal);
    }

    protected boolean daughterStemRuleBasedDifferentiation(
            PottsLocation loc1,
            PottsLocation loc2,
            double daughterProspero,
            double daughterDeadpan) {

        if (differentiationRuleset.equals("volume")) {
            double vol1 = loc1.getVolume();
            double vol2 = loc2.getVolume();
            if (Math.abs(vol1 - vol2) < range) {
                return true;
            } else {
                return false;
            }
        } else if (differentiationRuleset.equals("location")) {
            double[] centroid1 = loc1.getCentroid();
            double[] centroid2 = loc2.getCentroid();
            return (centroidsWithinRangeAlongApicalAxis(
                    centroid1, centroid2, ((PottsCellFlyStem) cell).getApicalAxis(), range));
        } else if (differentiationRuleset.equals("tfRatio")) {
            if (daughterDeadpan <= 0) {
                return daughterProspero <= 0;
            }
            return (daughterProspero / daughterDeadpan) <= tfRatio;
        }

        throw new IllegalArgumentException(
                "Invalid differentiation ruleset: " + differentiationRuleset);
    }

    @Override
    protected boolean daughterStemDeterministic(Plane divisionPlane) {
        Vector normalVector = divisionPlane.getUnitNormalVector();

        Vector apicalAxis = ((PottsCellFlyStem) cell).getApicalAxis();
        Vector expectedMUDNormalVector =
                Vector.rotateVectorAroundAxis(
                        apicalAxis,
                        PottsEnums.Direction.XY_PLANE.vector,
                        PottsCellFlyStem.StemType.MUDMUT.splitDirectionRotation);
        // If TRUE, the daughter should be stem. Otherwise, should be GMC
        return Math.abs(normalVector.getX() - expectedMUDNormalVector.getX()) <= EPSILON
                && Math.abs(normalVector.getY() - expectedMUDNormalVector.getY()) <= EPSILON
                && Math.abs(normalVector.getZ() - expectedMUDNormalVector.getZ()) <= EPSILON;
    }
}
