package arcade.potts.agent.cell;

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

    /** Enum outlining parameters for each cell type */
    public enum StemType {
        WT(50, 66, Direction.ZX_PLANE, 1.0),
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
         * <p>
         * This parameter allows for the specification of which group of voxels (geometrically)
         * remains as the stem cell, and which group differentiates into the daughter cell type.
         */
        public final double splitSelectionProbability;

        // Constructor
        StemType(int splitOffsetPercentX, int splitOffsetPercentY, Direction splitDirection,
                 double voxelGroupSelectionProbability) {
            this.splitOffsetPercentX = splitOffsetPercentX;
            this.splitOffsetPercentY = splitOffsetPercentY;
            this.splitDirection = splitDirection;
            this.splitSelectionProbability = voxelGroupSelectionProbability;
        }
    }

    public final StemType stemType;

    public PottsCellFlyStem(int id, int parent, int pop, CellState state, int age, int divisions,
                            Location location, boolean hasRegions, MiniBox parameters,
                            double criticalVolume, double criticalHeight,
                            EnumMap<Region, Double> criticalRegionVolumes,
                            EnumMap<Region, Double> criticalRegionHeights,
                            StemType stemType) {
        super(id, parent, pop, state, age, divisions, location, hasRegions, parameters,
              criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights);
        this.stemType = stemType;
    }

    @Override
    public PottsCell make(int newID, CellState newState, Location newLocation,
                                      MersenneTwisterFast random) {
        divisions++;
        switch (stemType) {
            case WT:
                return new PottsCellFlyNeuronWT(
                    newID, getID(), 2, newState, getAge(),
                    getDivisions(), newLocation, hasRegions(), getParameters(),
                    criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights, 1);
            case MUDMUT1_RANDOM:
                return new PottsCellFlyNeuronWT(
                    newID, getID(), 2, newState, getAge(),
                    getDivisions(), newLocation, hasRegions(), getParameters(),
                    criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights, 1);
            case MUDMUT1_LEFT:
                return new PottsCellFlyNeuronWT(
                    newID, getID(), 2, newState, getAge(),
                    getDivisions(), newLocation, hasRegions(), getParameters(),
                    criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights, 1);
            case MUDMUT2_RANDOM:
                double rand = random.nextDouble();
                if (rand < 0.25) {
                    return new PottsCellFlyStem(
                        newID, getID(), getPop(), newState, getAge(),
                        getDivisions(), newLocation, hasRegions(), getParameters(),
                        criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights,
                        StemType.MUDMUT2_RANDOM);
                } else if (rand < 0.5) {
                    return new PottsCellFlyStem(
                        newID, getID(), 1, newState, getAge(),
                        getDivisions(), newLocation, hasRegions(), getParameters(),
                        criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights,
                        StemType.MUDMUT1_RANDOM);
                } else {
                    return new PottsCellFlyNeuronWT(
                        newID, getID(), 2, newState, getAge(),
                        getDivisions(), newLocation, hasRegions(), getParameters(),
                        criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights, 1);
                }
            case INVERT1_BASAL:
                return new PottsCellFlyNeuronWT(
                    newID, getID(), 2, newState, getAge(),
                    getDivisions(), newLocation, hasRegions(), getParameters(),
                    criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights, 1);
            case INVERT2BASAL_OR_BOTH:
                double randInvert = random.nextDouble();
                if (randInvert < 0.25) {
                    return new PottsCellFlyStem(
                        newID, getID(), getPop(), newState, getAge(),
                        getDivisions(), newLocation, hasRegions(), getParameters(),
                        criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights,
                        StemType.WT);
                } else if (randInvert < 0.5) {
                    return new PottsCellFlyStem(
                        newID, getID(), 2, newState, getAge(),
                        getDivisions(), newLocation, hasRegions(), getParameters(),
                        criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights,
                        StemType.INVERT2BASAL_OR_BOTH);
                } else {
                    return new PottsCellFlyNeuronWT(
                        newID, getID(), 2, newState, getAge(),
                        getDivisions(), newLocation, hasRegions(), getParameters(),
                        criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights, 1);
                }
            case SYMMETRIC1_APICAL:
                return new PottsCellFlyNeuronWT(
                    newID, getID(), 2, newState, getAge(),
                    getDivisions(), newLocation, hasRegions(), getParameters(),
                    criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights, 1);
            case SYMMETRIC2APICAL_OR_BOTH:
                if (random.nextBoolean()) {
                    return new PottsCellFlyStem(
                        newID, getID(), getPop(), newState, getAge(),
                        getDivisions(), newLocation, hasRegions(), getParameters(),
                        criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights,
                        StemType.SYMMETRIC2APICAL_OR_BOTH);
                } else {
                    return new PottsCellFlyNeuronWT(
                        newID, getID(), 2, newState, getAge(),
                        getDivisions(), newLocation, hasRegions(), getParameters(),
                        criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights, 1);
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
}