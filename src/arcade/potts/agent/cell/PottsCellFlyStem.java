package arcade.potts.agent.cell;

import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellState;
import arcade.core.env.location.Location;
import arcade.core.util.GrabBag;
import arcade.core.util.Parameters;
import arcade.core.util.Vector;
import arcade.potts.agent.module.PottsModule;
import arcade.potts.agent.module.PottsModuleFlyStemProliferation;
import arcade.potts.util.PottsEnums.Phase;
import static arcade.potts.util.PottsEnums.State;

/**
 * Implementation of {@link PottsCell} for fly stem agents. Genotype is specified by the StemType
 * enum.
 */
public class PottsCellFlyStem extends PottsCell {
    /** Enum outlining parameters for each cell type. */
    public enum StemType {
        /** Wild type stem cell. */
        WT(50, 86, 0),

        /** mud Mutant stem cell. */
        MUDMUT(50, 50, -90);

        /** Percentage x offset from cell edge where division will occur. */
        public final int splitOffsetPercentX;

        /** Percentage y offset from cell edge where division will occur. */
        public final int splitOffsetPercentY;

        /** Default direction of division is rotated this much off the apical vector. */
        public final double splitDirectionRotation;

        /**
         * The proportion of the NB division volume allocated to the GMC daughter cell. Derived from
         * {@code splitOffsetPercentY} as {@code 1 - splitOffsetPercentY / 100}
         */
        public final double daughterCellCriticalVolumeProportion;

        /**
         * Constructor for StemType.
         *
         * @param splitOffsetPercentX percentage x offset from cell edge where division will occur
         * @param splitOffsetPercentY percentage y offset from cell edge where division will occur
         * @param splitDirectionRotation the plane of division's rotation off the apical vector
         */
        StemType(int splitOffsetPercentX, int splitOffsetPercentY, double splitDirectionRotation) {
            this.splitOffsetPercentX = splitOffsetPercentX;
            this.splitOffsetPercentY = splitOffsetPercentY;
            this.splitDirectionRotation = splitDirectionRotation;
            this.daughterCellCriticalVolumeProportion = 1.0 - splitOffsetPercentY / 100.0;
        }
    }

    /** The type of stem cell. */
    public final StemType stemType;

    /** The cell's apical axis. The vector points towards the apical membrane. */
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

        if (module != null) {
            ((PottsModule) module).setPhase(Phase.UNDEFINED);
        }

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

    /**
     * Sets the apical axis.
     *
     * @param apicalAxis the new apical axis
     */
    public void setApicalAxis(Vector apicalAxis) {
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
                "make(int, CellState, MersenneTwisterFast) not supported."
                        + "Please use make(int, CellState, MersenneTwisterFast, int, double) instead.");
    }

    /**
     * Makes a potts cell container with information about the daughter cell's population and
     * critical volume calculated by the proliferation module.
     *
     * @param newID the new cell ID
     * @param newState the new cell state
     * @param random the random number generator
     * @param newPop the new cell population
     * @param daughterCellCriticalVolume the new cell's critical volume
     * @return a {@link PottsCellContainer} with the information needed to make the daughter cell
     */
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
                Phase.UNDEFINED,
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
                module = new PottsModuleFlyStemProliferation(this);
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
