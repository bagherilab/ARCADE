package arcade.potts.agent.module;

import arcade.core.util.Plane;
import arcade.potts.agent.cell.PottsCellFlyStem;
import arcade.potts.env.location.PottsLocation;

public class PottsModuleFlyStemProliferationWT extends PottsModuleFlyStemProliferation {

    public PottsModuleFlyStemProliferationWT(PottsCellFlyStem cell) {
        super(cell);
    }

    protected Plane chooseDivisionPlane(PottsCellFlyStem flyStemCell) {
        double rotationOffset = sampleDivisionPlaneOffset();
        return getWTLikeDivisionPlaneWithRotationalVariance(flyStemCell, rotationOffset);
    }

    @Override
    protected boolean daughterStemRuleBasedDifferentiation(
            PottsLocation loc1,
            PottsLocation loc2,
            double daughterProspero,
            double daughterDeadpan) {

        return false;
    }
}
