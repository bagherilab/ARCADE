package arcade.potts.agent.module;

import sim.util.Double3D;
import arcade.core.util.Plane;
import arcade.core.util.Vector;
import arcade.potts.agent.cell.PottsCellFlyStem;
import arcade.potts.env.location.Voxel;
import arcade.potts.util.PottsEnums;

public class PottsModuleFlyStemProliferationMM extends PottsModuleFlyStemProliferation {

    public PottsModuleFlyStemProliferationMM(PottsCellFlyStem cell) {
        super(cell);
    }

    @Override
    protected Plane chooseDivisionPlane(PottsCellFlyStem flyStemCell) {
        double offset = sampleDivisionPlaneOffset();
        if (Math.abs(offset) < 45) {
            return getWTDivisionPlaneWithRotationalVariance(flyStemCell, offset);
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
                        PottsCellFlyStem.StemType.MUDMUT, cell, defaultNormal, likeX, likeY);
        return new Plane(new Double3D(splitVoxel.x, splitVoxel.y, splitVoxel.z), defaultNormal);
    }
}
