package arcade.potts.agent.cell;

import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellState;
import arcade.core.env.location.Location;
import arcade.core.util.GrabBag;
import arcade.core.util.MiniBox;
import arcade.core.util.Parameters;
import arcade.potts.agent.module.*;
import arcade.potts.util.PottsEnums.Direction;
import arcade.potts.util.PottsEnums.State;

public class PottsCellFlyStem extends PottsCell {

    /** Enum outlining parameters for each cell type */
    public enum StemType {
        WT(50, 75, Direction.ZX_PLANE, 1.0),
        MUDMUT1_RANDOM(50, 50, Direction.YZ_PLANE, 0.5),
        MUDMUT1_LEFT(50, 50, Direction.YZ_PLANE, 1.0),
        MUDMUT2_RANDOM(50, 50, Direction.YZ_PLANE, 0.5),
        INVERT1_BASAL(50, 33, Direction.ZX_PLANE, 0.0),
        INVERT2BASAL_OR_BOTH(50, 33, Direction.ZX_PLANE, 0.0),
        SYMMETRIC1_APICAL(50, 50, Direction.ZX_PLANE, 1.0),
        SYMMETRIC2APICAL_OR_BOTH(50, 50, Direction.ZX_PLANE, 1.0);

        /** Percentage x offset from cell edge where division will occur */
        public final int splitOffsetPercentX;

        /** Percentage y offset from cell edge where division will occur */
        public final int splitOffsetPercentY;

        /** Direction of division */
        public final Direction splitDirection;

        /**
         * The probability that the first set of voxels is returned during the split operation.
         *
         * <p>This parameter allows for the specification of which group of voxels (geometrically)
         * remains as the stem cell, and which group differentiates into the daughter cell type.
         */
        public final double splitSelectionProbability;

        // Constructor
        StemType(
                int splitOffsetPercentX,
                int splitOffsetPercentY,
                Direction splitDirection,
                double voxelGroupSelectionProbability) {
            this.splitOffsetPercentX = splitOffsetPercentX;
            this.splitOffsetPercentY = splitOffsetPercentY;
            this.splitDirection = splitDirection;
            this.splitSelectionProbability = voxelGroupSelectionProbability;
        }
    }

    public final StemType stemType;

    public PottsCellFlyStem(
            PottsCellContainer container, Location location, Parameters parameters, GrabBag links) {
        super(container, location, parameters, links);
        MiniBox stemTypeMiniBox = parameters.filter("CLASS");
        String stemTypeString = stemTypeMiniBox.get("");
        switch (stemTypeString) {
            case "flystem-wt":
                stemType = StemType.WT;
                break;
            case "flystem-mudmut-onestemdaughter-stemdaughterrandom":
                stemType = StemType.MUDMUT1_RANDOM;
                break;
            case "flystem-mudmut-onestemdaughter-stemdaughterleft":
                stemType = StemType.MUDMUT1_LEFT;
                break;
            case "flystem-mudmut-twostemdaughters-stemdaughterrandom":
                stemType = StemType.MUDMUT2_RANDOM;
                break;
            case "flystem-invert-onestemdaughter-stemdaughterbasalL":
                stemType = StemType.INVERT1_BASAL;
                break;
            case "flystem-invert-twostemdaughters-stemdaughterbasalorboth":
                stemType = StemType.INVERT2BASAL_OR_BOTH;
                break;
            case "flystem-symmetric-onestemdaughter-stemdaughterapical":
                stemType = StemType.SYMMETRIC1_APICAL;
                break;
            case "flystem-symmetric-twostemdaughters-stemdaughterapicalorboth":
                stemType = StemType.SYMMETRIC2APICAL_OR_BOTH;
                break;
            default:
                throw new IllegalArgumentException("Unknown StemType: " + stemTypeString);
        }
    }

    @Override
    public PottsCellContainer make(int newID, CellState newState, MersenneTwisterFast random) {
        divisions++;

        int newPop = links == null ? pop : links.next(random);

        return new PottsCellContainer(
                newID,
                id,
                newPop,
                age,
                divisions,
                newState,
                null,
                0,
                null,
                criticalVolume,
                criticalHeight,
                criticalRegionVolumes,
                criticalRegionHeights);
    }

    @Override
    void setStateModule(CellState newState) {
        switch ((State) newState) {
            case PROLIFERATIVE:
                module = new PottsModuleProliferationFlyStem(this);
                break;
            default:
                module = null;
                break;
        }
    }
}
