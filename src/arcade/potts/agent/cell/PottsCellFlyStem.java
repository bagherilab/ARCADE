package arcade.potts.agent.cell;

import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellState;
import arcade.core.env.location.Location;
import arcade.core.util.GrabBag;
import arcade.core.util.Parameters;
import arcade.potts.agent.module.PottsModuleProliferationFlyStem;
import arcade.potts.util.PottsEnums.Direction;
import arcade.potts.util.PottsEnums.State;

/** Extension of {@link PottsCell} for fly stem cells. */
public class PottsCellFlyStem extends PottsCell {

    /** Enum outlining parameters for each cell type. */
    public enum StemType {
        WT(50, 80, Direction.ZX_PLANE, 0.2),
        MUDMUT(50, 50, Direction.YZ_PLANE, 0.5);

        /** Percentage x offset from cell edge where division will occur. */
        public final int splitOffsetPercentX;

        /** Percentage y offset from cell edge where division will occur. */
        public final int splitOffsetPercentY;

        /** Direction of division */
        public final Direction splitDirection;

        /**
         * The proportion of the stem cell's critical volume that will be the daughter cell's
         * critical volume.
         */
        public final double daughterCellCriticalVolumeProportion;

        /**
         * Constructor for StemType.
         *
         * @param splitOffsetPercentX percentage x offset from cell edge where division will occur
         * @param splitOffsetPercentY percentage y offset from cell edge where division will occur
         * @param splitDirection direction of division
         * @param daughterCellCriticalVolumeProportion proportion of the stem cell's critical volume
         *     that will be the daughter cell's critical volume
         */
        StemType(
                int splitOffsetPercentX,
                int splitOffsetPercentY,
                Direction splitDirection,
                double daughterCellCriticalVolumeProportion) {
            this.splitOffsetPercentX = splitOffsetPercentX;
            this.splitOffsetPercentY = splitOffsetPercentY;
            this.splitDirection = splitDirection;
            this.daughterCellCriticalVolumeProportion = daughterCellCriticalVolumeProportion;
        }
    }

    /** The type of stem cell. */
    public final StemType stemType;

    /**
     * Constructor for PottsCellFlyStem.
     *
     * @param container the container for the cell
     * @param location the location of the cell
     * @param parameters the parameters for the cell
     * @param links the links for the cell
     * @throws IllegalArgumentException if the stem type is not recognized
     */
    public PottsCellFlyStem(
            PottsCellContainer container, Location location, Parameters parameters, GrabBag links) {
        super(container, location, parameters, links);
        String stemTypeString = parameters.getString("CLASS");
        switch (stemTypeString) {
            case "fly-stem-wt":
                stemType = StemType.WT;
                break;
            case "fly-stem-mudmut":
                stemType = StemType.MUDMUT;
                break;
            default:
                throw new IllegalArgumentException("Unknown StemType: " + stemTypeString);
        }
    }

    @Override
    public PottsCellContainer make(int newID, CellState newState, MersenneTwisterFast random) {
        divisions++;

        int newPop = links == null ? pop : links.next(random);

        double daughterCellCriticalVolume =
                criticalVolume * stemType.daughterCellCriticalVolumeProportion;

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
                daughterCellCriticalVolume,
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

    /*
     * Gets the stem type of the cell.
     *
     * @return the stem type of the cell
     */
    public StemType getStemType() {
        return stemType;
    }
}
