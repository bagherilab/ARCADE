package arcade.potts.agent.cell;

import java.util.ArrayList;
import java.util.EnumMap;
import arcade.core.agent.cell.CellState;
import arcade.core.env.location.Location;
import arcade.core.util.MiniBox;
import arcade.potts.agent.module.*;
import arcade.potts.util.PottsEnums.Direction;
import arcade.potts.util.PottsEnums.Region;
import arcade.potts.util.PottsEnums.State;
import ec.util.MersenneTwisterFast;

public class PottsCellFlyStem extends PottsCell {
    /** Percentage offset from cell edge where division will occur */
    private final ArrayList<Integer> splitOffsetPercent;

    /** Direction of division */
    private final Direction splitDirection;

    /**
     * The probability that the first set of voxels is returned during the split operation.
     * <p>
     * This parameter allows for the specification of which group of voxels (geometrically)
     * remains as the stem cell, and which group differentiates into the daughter cell type.
     */
    private final double voxelGroupSelectionProbability;

    /** Enum outlining parameters for each cell type */
    public enum StemType {
        WT(50, 66, Direction.ZX_PLANE, 1.0),
        MUDMut1Random(50, 50, Direction.YZ_PLANE, 0.5),
        MUDMut1Left(50, 50, Direction.YZ_PLANE, 1.0),
        MUDMut2Random(50, 50, Direction.YZ_PLANE, 0.5),
        Invert1Basal(50, 33, Direction.ZX_PLANE, 0.0),
        Invert2BasalOrBoth(50, 33, Direction.ZX_PLANE, 0.0),
        Symmetric1Apical(50, 50, Direction.ZX_PLANE, 1.0),
        Symmetric2ApicalOrBoth(50, 50, Direction.ZX_PLANE, 1.0);

        // Fields for configuration
        public final int splitOffsetPercentX;
        public final int splitOffsetPercentY;
        public final Direction splitDirection;
        public final double voxelGroupSelectionProbability;

        // Constructor
        StemType(int splitOffsetPercentX, int splitOffsetPercentY, Direction splitDirection,
                 double voxelGroupSelectionProbability) {
            this.splitOffsetPercentX = splitOffsetPercentX;
            this.splitOffsetPercentY = splitOffsetPercentY;
            this.splitDirection = splitDirection;
            this.voxelGroupSelectionProbability = voxelGroupSelectionProbability;
        }
    }

    private final StemType stemType;

    public PottsCellFlyStem(int id, int parent, int pop, CellState state, int age, int divisions,
                            Location location, boolean hasRegions, MiniBox parameters,
                            double criticalVolume, double criticalHeight,
                            EnumMap<Region, Double> criticalRegionVolumes,
                            EnumMap<Region, Double> criticalRegionHeights,
                            StemType stemType) {
        super(id, parent, pop, state, age, divisions, location, hasRegions, parameters,
              criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights);
        this.splitOffsetPercent = new ArrayList<>();
        this.splitOffsetPercent.add(stemType.splitOffsetPercentX);
        this.splitOffsetPercent.add(stemType.splitOffsetPercentY);
        this.splitDirection = stemType.splitDirection;
        this.voxelGroupSelectionProbability = stemType.voxelGroupSelectionProbability;
        this.stemType = stemType;
    }

    public ArrayList<Integer> getSplitOffsetPercent() {
        return splitOffsetPercent;
    }

    public Direction getSplitDirection() {
        return splitDirection;
    }

    public double getVoxelGroupSelectionProbability() {
        return voxelGroupSelectionProbability;
    }

    @Override
    public PottsCell make(int newID, CellState newState, Location newLocation,
                                      MersenneTwisterFast random) {
        divisions++;
        switch (stemType) {
            case WT:
                return PottsCellFlyNeuronWT.createPottsCellFlyNeuronWT(
                    newID, getID(), 2, newState, getAge(),
                    getDivisions(), newLocation, hasRegions(), getParameters(),
                    criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights);
            case MUDMut1Random:
                return PottsCellFlyNeuronWT.createPottsCellFlyNeuronWT(
                    newID, getID(), 2, newState, getAge(),
                    getDivisions(), newLocation, hasRegions(), getParameters(),
                    criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights);
            case MUDMut1Left:
                return PottsCellFlyNeuronWT.createPottsCellFlyNeuronWT(
                    newID, getID(), 2, newState, getAge(),
                    getDivisions(), newLocation, hasRegions(), getParameters(),
                    criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights);
            case MUDMut2Random:
                double rand = random.nextDouble();
                if (rand < 0.25) {
                    return PottsCellFlyStem.createPottsCellFlyStem(
                        newID, getID(), getPop(), newState, getAge(),
                        getDivisions(), newLocation, hasRegions(), getParameters(),
                        criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights,
                        StemType.MUDMut2Random);
                } else if (rand < 0.5) {
                    return PottsCellFlyStem.createPottsCellFlyStem(
                        newID, getID(), 1, newState, getAge(),
                        getDivisions(), newLocation, hasRegions(), getParameters(),
                        criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights,
                        StemType.MUDMut1Random);
                } else {
                    return PottsCellFlyNeuronWT.createPottsCellFlyNeuronWT(
                        newID, getID(), 2, newState, getAge(),
                        getDivisions(), newLocation, hasRegions(), getParameters(),
                        criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights);
                }
            case Invert1Basal:
                return PottsCellFlyNeuronWT.createPottsCellFlyNeuronWT(
                    newID, getID(), 2, newState, getAge(),
                    getDivisions(), newLocation, hasRegions(), getParameters(),
                    criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights);
            case Invert2BasalOrBoth:
                double randInvert = random.nextDouble();
                if (randInvert < 0.25) {
                    return PottsCellFlyStem.createPottsCellFlyStem(
                        newID, getID(), getPop(), newState, getAge(),
                        getDivisions(), newLocation, hasRegions(), getParameters(),
                        criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights,
                        StemType.WT);
                } else if (randInvert < 0.5) {
                    return PottsCellFlyStem.createPottsCellFlyStem(
                        newID, getID(), 2, newState, getAge(),
                        getDivisions(), newLocation, hasRegions(), getParameters(),
                        criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights,
                        StemType.Invert2BasalOrBoth);
                } else {
                    return PottsCellFlyNeuronWT.createPottsCellFlyNeuronWT(
                        newID, getID(), 2, newState, getAge(),
                        getDivisions(), newLocation, hasRegions(), getParameters(),
                        criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights);
                }
            case Symmetric1Apical:
                return PottsCellFlyNeuronWT.createPottsCellFlyNeuronWT(
                    newID, getID(), 2, newState, getAge(),
                    getDivisions(), newLocation, hasRegions(), getParameters(),
                    criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights);
            case Symmetric2ApicalOrBoth:
                if (random.nextBoolean()) {
                    return PottsCellFlyStem.createPottsCellFlyStem(
                        newID, getID(), getPop(), newState, getAge(),
                        getDivisions(), newLocation, hasRegions(), getParameters(),
                        criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights,
                        StemType.Symmetric2ApicalOrBoth);
                } else {
                    return PottsCellFlyNeuronWT.createPottsCellFlyNeuronWT(
                        newID, getID(), 2, newState, getAge(),
                        getDivisions(), newLocation, hasRegions(), getParameters(),
                        criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights);
                }
            default:
                throw new IllegalArgumentException("Unknown StemType: " + stemType);
        }
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

    public static PottsCellFlyStem createPottsCellFlyStem(int id, int parent, int pop, CellState state, int age,
                                                          int divisions, Location location, boolean hasRegions,
                                                          MiniBox parameters, double criticalVolume, double criticalHeight,
                                                          EnumMap<Region, Double> criticalRegionVolumes,
                                                          EnumMap<Region, Double> criticalRegionHeights,
                                                          StemType stemType) {
        return new PottsCellFlyStem(id, parent, pop, state, age, divisions, location, hasRegions, parameters,
                                    criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights,
                                    stemType);
    }
}