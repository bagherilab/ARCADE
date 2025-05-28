package arcade.potts.agent.cell;

import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellState;
import arcade.core.env.location.Location;
import arcade.core.util.GrabBag;
import arcade.core.util.Parameters;
import arcade.core.util.Vector;
import arcade.potts.agent.module.PottsModuleProliferationFlyStem;
import arcade.potts.util.PottsEnums.State;

/** Extension of {@link PottsCell} for fly stem cells. */
public class PottsCellFlyStem extends PottsCell {

    /** Enum outlining parameters for each cell type. */
    public enum StemType {
        /** Wild type stem cell. */
        WT(50, 80, 0, 0.2),

        /** mud Mutant stem cell. */
        MUDMUT(50, 50, -90, 0.5);

        /** Percentage x offset from cell edge where division will occur. */
        public final int splitOffsetPercentX;

        /** Percentage y offset from cell edge where division will occur. */
        public final int splitOffsetPercentY;

        /** Default direction of division is rotated this much off the apical vector. */
        public final double splitDirectionRotation;

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
         * @param splitDirectionRotation the plan of division's rotation off the apical vector
         * @param daughterCellCriticalVolumeProportion proportion of the stem cell's critical volume
         *     that will be the daughter cell's critical volume
         */
        StemType(
                int splitOffsetPercentX,
                int splitOffsetPercentY,
                double splitDirectionRotation,
                double daughterCellCriticalVolumeProportion) {
            this.splitOffsetPercentX = splitOffsetPercentX;
            this.splitOffsetPercentY = splitOffsetPercentY;
            this.splitDirectionRotation = splitDirectionRotation;
            this.daughterCellCriticalVolumeProportion = daughterCellCriticalVolumeProportion;
        }
    }

    /** The type of stem cell. */
    public final StemType stemType;

    private Vector apicalAxis;

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

    public void setApicalAxis(Vector apicalAxis) {
        // print apical axis to stdout
        System.out.println(
                "Cell "
                        + id
                        + " apical axis = "
                        + apicalAxis.getX()
                        + ","
                        + apicalAxis.getY()
                        + ","
                        + apicalAxis.getZ());
        this.apicalAxis = apicalAxis;
    }

    /**
     * Gets the apical axis of the cell. If no apical axis is set, it returns a vector along the y
     * axis as a default vector
     *
     * @return the apical axis of the cell
     */
    public Vector getApicalAxis() {
        if (apicalAxis != null) {
            return apicalAxis;
        } else {
            return new Vector(0, 1, 0);
        }
    }

    @Override
    public PottsCellContainer make(int newID, CellState newState, MersenneTwisterFast random) {
        throw new UnsupportedOperationException(
                "make(int, CellState, MersenneTwisterFast) not supported. Please use make(int, CellState, MersenneTwisterFast, int, double) instead.");
    }

    // TODO: Write a better test for this function
    public PottsCellContainer make(
            int newID,
            CellState newState,
            MersenneTwisterFast random,
            int newPop,
            double daughterCellCriticalVolume) {

        divisions++;

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

    /**
     * Gets the stem type of the cell.
     *
     * @return the stem type of the cell
     */
    public final StemType getStemType() {
        return stemType;
    }
}
