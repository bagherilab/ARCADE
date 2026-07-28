package arcade.potts.agent.module;

import arcade.core.util.Plane;
import arcade.potts.agent.cell.PottsCellFlyStem;

public class PottsModuleFlyStemProliferationWT extends PottsModuleFlyStemProliferation {

    public PottsModuleFlyStemProliferationWT(PottsCellFlyStem cell) {
        super(cell);
    }

    protected Plane chooseDivisionPlane(PottsCellFlyStem flyStemCell) {
        double rotationOffset = sampleDivisionPlaneOffset();
        return getWTLikeDivisionPlaneWithRotationalVariance(flyStemCell, rotationOffset);
    }
}
