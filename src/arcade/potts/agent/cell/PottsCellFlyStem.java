package arcade.potts.agent.cell;

import java.util.ArrayList;
import java.util.EnumMap;
import arcade.core.agent.cell.CellState;
import arcade.core.env.location.Location;
import arcade.core.util.MiniBox;
import arcade.potts.agent.module.*;
import arcade.potts.util.PottsEnums.Region;
import arcade.potts.util.PottsEnums.State;
import ec.util.MersenneTwisterFast;
import static arcade.potts.util.PottsEnums.Direction;

public abstract class PottsCellFlyStem extends PottsCell {
    /** Percentage offset from cell edge where division will occur */
    public final ArrayList<Integer> splitOffsetPercent;

    /** Direction of division */
    public final Direction splitDirection;

    /**
     * The probability that the first set of voxels is returned during the split operation.
     * <p>
     * This parameter allows for the specification of which group of voxels (geometrically)
     * remains as the stem cell, and which group differentiates into the daughter cell type.
    */
    public final double splitProbability;

    public PottsCellFlyStem(int id, int parent, int pop, CellState state, int age, int divisions,
                         Location location, boolean hasRegions, MiniBox parameters,
                         double criticalVolume, double criticalHeight,
                         EnumMap<Region, Double> criticalRegionVolumes,
                         EnumMap<Region, Double> criticalRegionHeights) {
        super(id, parent, pop, state, age, divisions, location, hasRegions, parameters,
                criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights);
        this.splitOffsetPercent = getSplitOffsetPercent();
        this.splitDirection = getSplitDirection();
        this.splitProbability = getSplitProbability();
    }

    abstract public ArrayList<Integer> getSplitOffsetPercent();

    abstract public Direction getSplitDirection();

    abstract public double getSplitProbability();

    public abstract PottsCell makeDaughterCell(int newID, CellState newState, Location newLocation,
                                               MersenneTwisterFast random);

    @Override
    public PottsCell make(int newID, CellState newState, Location newLocation,
                                   MersenneTwisterFast random){
            return makeDaughterCell(newID, newState, newLocation, random);
        }

    @Override
    void setStateModule(CellState newState) {
        switch ((State) newState) {
            case PROLIFERATIVE:
                module = new PottsModuleProliferationFlyStem(this);
                break;
            case QUIESCENT:
            case APOPTOTIC:
            case NECROTIC:
            case AUTOTIC:
            default:
                module = null;
                break;
        }
    }

    public static final class PottsCellFlyStemWT extends PottsCellFlyStem {
        public static final int POTTS_CELL_FLY_NEURON_WT_POP = 2;
        public static final int SPLIT_OFFSET_PERCENT_X = 50;
        public static final int SPLIT_OFFSET_PERCENT_Y = 66;
        public static final Direction SPLIT_DIRECTION = Direction.ZX_PLANE;
        // Split Probability 1 with ZX_PLANE divixion ensures apical cell remains stem
        public static final double SPLIT_PROBABILITY = 1;

        public PottsCellFlyStemWT(int id, int parent, int pop, CellState state, int age, int divisions,
                             Location location, boolean hasRegions, MiniBox parameters,
                             double criticalVolume, double criticalHeight,
                             EnumMap<Region, Double> criticalRegionVolumes,
                             EnumMap<Region, Double> criticalRegionHeights) {
            super(id, parent, pop, state, age, divisions, location, hasRegions, parameters,
                    criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights);
        }

        @Override
        public ArrayList<Integer> getSplitOffsetPercent() {
            ArrayList<Integer> splitOffsetPercent = new ArrayList<>();
            splitOffsetPercent.add(SPLIT_OFFSET_PERCENT_X);
            splitOffsetPercent.add(SPLIT_OFFSET_PERCENT_Y);
            return splitOffsetPercent;
        }

        @Override
        public Direction getSplitDirection() {
            return SPLIT_DIRECTION;
        }

        @Override
        public double getSplitProbability() {
            return SPLIT_PROBABILITY;
        }

        @Override
        public PottsCell makeDaughterCell(int newID, CellState newState, Location newLocation,
                                          MersenneTwisterFast random) {
            divisions++;
            MiniBox newParameters = new MiniBox();
            for (String key : this.getParameters().getKeys()) {
                newParameters.put(key, this.getParameters().get(key));
            }
            newParameters.put("proliferation/CELL_GROWTH_RATE", "0");
            return new PottsCellFlyNeuronWT(newID, id, POTTS_CELL_FLY_NEURON_WT_POP,
                                            newState, age, divisions, newLocation,
                                            hasRegions, newParameters, criticalVolume, criticalHeight,
                                            criticalRegionVolumes, criticalRegionHeights);
        }
    }

    public static final class PottsCellFlyStemMUDMut1StemRandom extends PottsCellFlyStem {
        public static final int POTTS_CELL_FLY_NEURON_WT_POP = 2;
        public static final int POTTS_STEM_1_POP = 1;
        public static final int SPLIT_OFFSET_PERCENT_X = 50;
        public static final int SPLIT_OFFSET_PERCENT_Y = 50;
        public static final Direction SPLIT_DIRECTION = Direction.YZ_PLANE;
        // Split Probability .5 with ZX_PLANE division makes stem daughter cell random
        public static final double SPLIT_PROBABILITY = 0.5;

        public PottsCellFlyStemMUDMut1StemRandom(int id, int parent, int pop, CellState state, int age, int divisions,
                             Location location, boolean hasRegions, MiniBox parameters,
                             double criticalVolume, double criticalHeight,
                             EnumMap<Region, Double> criticalRegionVolumes,
                             EnumMap<Region, Double> criticalRegionHeights) {
            super(id, parent, pop, state, age, divisions, location, hasRegions, parameters,
                    criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights);
        }

        @Override
        public ArrayList<Integer> getSplitOffsetPercent() {
            ArrayList<Integer> splitOffsetPercent = new ArrayList<>();
            splitOffsetPercent.add(SPLIT_OFFSET_PERCENT_X);
            splitOffsetPercent.add(SPLIT_OFFSET_PERCENT_Y);
            return splitOffsetPercent;
        }

        @Override
        public Direction getSplitDirection() {
            return SPLIT_DIRECTION;
        }

        @Override
        public double getSplitProbability() {
            return SPLIT_PROBABILITY;
        }

        @Override
        public PottsCell makeDaughterCell(int newID, CellState newState, Location newLocation,
                                          MersenneTwisterFast random) {
            divisions++;
            MiniBox newParameters = new MiniBox();
            for (String key : this.getParameters().getKeys()) {
                newParameters.put(key, this.getParameters().get(key));
            }
            newParameters.put("proliferation/CELL_GROWTH_RATE", "0");
            return new PottsCellFlyNeuronWT(newID, id, POTTS_CELL_FLY_NEURON_WT_POP, newState, age, divisions, newLocation,
                    hasRegions, newParameters, criticalVolume, criticalHeight,
                    criticalRegionVolumes, criticalRegionHeights);
        }
    }

    public static final class PottsCellFlyStemMUDMut1StemLeft extends PottsCellFlyStem {
        public static final int POTTS_CELL_FLY_NEURON_WT_POP = 2;
        public static final int SPLIT_OFFSET_PERCENT_X = 50;
        public static final int SPLIT_OFFSET_PERCENT_Y = 50;
        public static final Direction SPLIT_DIRECTION = Direction.YZ_PLANE;
        // Split Probability 1 with ZX_PLANE division ensures stem daughter cell is left daughter
        public static final double SPLIT_PROBABILITY = 1;


        public PottsCellFlyStemMUDMut1StemLeft(int id, int parent, int pop, CellState state, int age, int divisions,
                             Location location, boolean hasRegions, MiniBox parameters,
                             double criticalVolume, double criticalHeight,
                             EnumMap<Region, Double> criticalRegionVolumes,
                             EnumMap<Region, Double> criticalRegionHeights) {
            super(id, parent, pop, state, age, divisions, location, hasRegions, parameters,
                    criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights);
        }

        @Override
        public ArrayList<Integer> getSplitOffsetPercent() {
            ArrayList<Integer> splitOffsetPercent = new ArrayList<>();
            splitOffsetPercent.add(SPLIT_OFFSET_PERCENT_X);
            splitOffsetPercent.add(SPLIT_OFFSET_PERCENT_Y);
            return splitOffsetPercent;
        }

        @Override
        public Direction getSplitDirection() {
            return SPLIT_DIRECTION;
        }

        @Override
        public double getSplitProbability() {
            return SPLIT_PROBABILITY;
        }

        @Override
        public PottsCell makeDaughterCell(int newID, CellState newState, Location newLocation,
                                          MersenneTwisterFast random) {
            divisions++;
            MiniBox newParameters = new MiniBox();
            for (String key : this.getParameters().getKeys()) {
                newParameters.put(key, this.getParameters().get(key));
            }
            newParameters.put("proliferation/CELL_GROWTH_RATE", "0");
            return new PottsCellFlyNeuronWT(newID, id, POTTS_CELL_FLY_NEURON_WT_POP, newState, age, divisions, newLocation,
                    hasRegions, newParameters, criticalVolume, criticalHeight,
                    criticalRegionVolumes, criticalRegionHeights);
        }
    }

    public static final class PottsCellFlyStemMUDMut2StemRandom extends PottsCellFlyStem {
        public static final int POTTS_CELL_FLY_NEURON_WT_POP = 2;
        public static final int POTTS_STEM_1_POP = 1;
        public static final int SPLIT_OFFSET_PERCENT_X = 50;
        public static final int SPLIT_OFFSET_PERCENT_Y = 50;
        public static final Direction SPLIT_DIRECTION = Direction.YZ_PLANE;
        // Split Probability .5 with ZX_PLANE division makes stem daughter cell random
        public static final double SPLIT_PROBABILITY = 0.5;

        public PottsCellFlyStemMUDMut2StemRandom(int id, int parent, int pop, CellState state, int age, int divisions,
                             Location location, boolean hasRegions, MiniBox parameters,
                             double criticalVolume, double criticalHeight,
                             EnumMap<Region, Double> criticalRegionVolumes,
                             EnumMap<Region, Double> criticalRegionHeights) {
            super(id, parent, pop, state, age, divisions, location, hasRegions, parameters,
                    criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights);
        }

        @Override
        public ArrayList<Integer> getSplitOffsetPercent() {
            ArrayList<Integer> splitOffsetPercent = new ArrayList<>();
            splitOffsetPercent.add(SPLIT_OFFSET_PERCENT_X);
            splitOffsetPercent.add(SPLIT_OFFSET_PERCENT_Y);
            return splitOffsetPercent;
        }

        @Override
        public Direction getSplitDirection() {
            return SPLIT_DIRECTION;
        }

        @Override
        public double getSplitProbability() {
            return SPLIT_PROBABILITY;
        }

        @Override
        public PottsCell makeDaughterCell(int newID, CellState newState, Location newLocation,
                                          MersenneTwisterFast random) {
            divisions++;
            // 25% chance of making MUDMut2StemRandom, 25% chance of making MUDMut1StemRandom, 50% chance of making FlyNeuronWT
            if (random.nextDouble() < 0.25) {
                System.out.println("Making new MUDMut2StemRandom");
                System.out.println("Inside make method, growth rate is " + this.getParameters().get("proliferation/CELL_GROWTH_RATE"));
                return new PottsCellFlyStemMUDMut2StemRandom(newID, id, pop, newState, age, divisions, newLocation,
                        hasRegions, this.getParameters(), criticalVolume, criticalHeight,
                        criticalRegionVolumes, criticalRegionHeights);
            } else if (random.nextDouble() < 0.5) {
                System.out.println("Making new MUDMut1StemRandom");
                System.out.println("Inside make method, growth rate is " + this.getParameters().get("proliferation/CELL_GROWTH_RATE"));
                return new PottsCellFlyStemMUDMut1StemRandom(newID, id, POTTS_STEM_1_POP, newState, age, divisions, newLocation,
                        hasRegions, this.getParameters(), criticalVolume, criticalHeight,
                        criticalRegionVolumes, criticalRegionHeights);
            } else {
                System.out.println("Making new FlyNeuronWT");
                MiniBox newParameters = new MiniBox();
                for (String key : this.getParameters().getKeys()) {
                    newParameters.put(key, this.getParameters().get(key));
                }
                newParameters.put("proliferation/CELL_GROWTH_RATE", "0");
                return new PottsCellFlyNeuronWT(newID, id, POTTS_CELL_FLY_NEURON_WT_POP, newState, age, divisions, newLocation,
                        hasRegions, newParameters, criticalVolume, criticalHeight,
                        criticalRegionVolumes, criticalRegionHeights);
            }
        }
    }

    public static final class PottsCellFlyStemInvert1StemBasal extends PottsCellFlyStem {
        public static final int POTTS_CELL_FLY_NEURON_WT_POP = 2;
        public static final int SPLIT_OFFSET_PERCENT_X = 50;
        public static final int SPLIT_OFFSET_PERCENT_Y = 33;
        public static final Direction SPLIT_DIRECTION = Direction.ZX_PLANE;
        // Split Probability 0 with ZX_PLANE division ensures basal cell remains stem
        public static final double SPLIT_PROBABILITY = 0;

        public PottsCellFlyStemInvert1StemBasal(int id, int parent, int pop, CellState state, int age, int divisions,
                             Location location, boolean hasRegions, MiniBox parameters,
                             double criticalVolume, double criticalHeight,
                             EnumMap<Region, Double> criticalRegionVolumes,
                             EnumMap<Region, Double> criticalRegionHeights) {
            super(id, parent, pop, state, age, divisions, location, hasRegions, parameters,
                    criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights);
        }

        @Override
        public ArrayList<Integer> getSplitOffsetPercent() {
            ArrayList<Integer> splitOffsetPercent = new ArrayList<>();
            splitOffsetPercent.add(SPLIT_OFFSET_PERCENT_X);
            splitOffsetPercent.add(SPLIT_OFFSET_PERCENT_Y);
            return splitOffsetPercent;
        }

        @Override
        public Direction getSplitDirection() {
            return SPLIT_DIRECTION;
        }

        @Override
        public double getSplitProbability() {
            return SPLIT_PROBABILITY;
        }

        @Override
        public PottsCell makeDaughterCell(int newID, CellState newState, Location newLocation,
                                          MersenneTwisterFast random) {
            divisions++;
            MiniBox newParameters = new MiniBox();
            for (String key : this.getParameters().getKeys()) {
                newParameters.put(key, this.getParameters().get(key));
            }
            newParameters.put("proliferation/CELL_GROWTH_RATE", "0");
            return new PottsCellFlyNeuronWT(newID, id, POTTS_CELL_FLY_NEURON_WT_POP, newState, age, divisions, newLocation,
                    hasRegions, newParameters, criticalVolume, criticalHeight,
                    criticalRegionVolumes, criticalRegionHeights);
        }
    }

    public static final class PottsCellFlyStemInvert2StemBasalOrBoth extends PottsCellFlyStem {
        public static final int POTTS_CELL_FLY_NEURON_WT_POP = 2;
        public static final int SPLIT_OFFSET_PERCENT_X = 50;
        public static final int SPLIT_OFFSET_PERCENT_Y = 33;
        public static final Direction SPLIT_DIRECTION = Direction.ZX_PLANE;
        // Split Probability 0 with ZX_PLANE division ensures basal cell remains stem
        public static final double SPLIT_PROBABILITY = 0;

        public PottsCellFlyStemInvert2StemBasalOrBoth(int id, int parent, int pop, CellState state, int age, int divisions,
                             Location location, boolean hasRegions, MiniBox parameters,
                             double criticalVolume, double criticalHeight,
                             EnumMap<Region, Double> criticalRegionVolumes,
                             EnumMap<Region, Double> criticalRegionHeights) {
            super(id, parent, pop, state, age, divisions, location, hasRegions, parameters,
                    criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights);
        }

        @Override
        public ArrayList<Integer> getSplitOffsetPercent() {
            ArrayList<Integer> splitOffsetPercent = new ArrayList<>();
            splitOffsetPercent.add(SPLIT_OFFSET_PERCENT_X);
            splitOffsetPercent.add(SPLIT_OFFSET_PERCENT_Y);
            return splitOffsetPercent;
        }

        @Override
        public Direction getSplitDirection() {
            return SPLIT_DIRECTION;
        }

        @Override
        public double getSplitProbability() {
            return SPLIT_PROBABILITY;
        }

        @Override
        public PottsCell makeDaughterCell(int newID, CellState newState, Location newLocation,
                                          MersenneTwisterFast random) {
            divisions++;
            // 25% chance of making PottsCellFlyStemWT, 25% chance of making PottsCellFlyStemInvert2StemRandom 50% chance of making FlyNeuronWT
            if (random.nextDouble() < 0.25) {
                System.out.println("Making new PottsCellFlyStemWT");
                System.out.println("Inside make method, growth rate is " + this.getParameters().get("proliferation/CELL_GROWTH_RATE"));
                return new PottsCellFlyStemWT(newID, id, pop, newState, age, divisions, newLocation,
                        hasRegions, this.getParameters(), criticalVolume, criticalHeight,
                        criticalRegionVolumes, criticalRegionHeights);
            } else if (random.nextDouble() < 0.5) {
                System.out.println("Making new PottsCellFlyStemInvert2StemRandom");
                System.out.println("Inside make method, growth rate is " + this.getParameters().get("proliferation/CELL_GROWTH_RATE"));
                return new PottsCellFlyStemInvert2StemBasalOrBoth(newID, id, POTTS_CELL_FLY_NEURON_WT_POP, newState, age, divisions, newLocation,
                        hasRegions, this.getParameters(), criticalVolume, criticalHeight,
                        criticalRegionVolumes, criticalRegionHeights);
            } else {
                System.out.println("Making new FlyNeuronWT");
                MiniBox newParameters = new MiniBox();
                for (String key : this.getParameters().getKeys()) {
                    newParameters.put(key, this.getParameters().get(key));
                }
                newParameters.put("proliferation/CELL_GROWTH_RATE", "0");
                return new PottsCellFlyNeuronWT(newID, id, POTTS_CELL_FLY_NEURON_WT_POP, newState, age, divisions, newLocation,
                        hasRegions, newParameters, criticalVolume, criticalHeight,
                        criticalRegionVolumes, criticalRegionHeights);
            }
        }
    }

    public static final class PottsCellFlyStemSymmetric1StemApical extends PottsCellFlyStem {
        public static final int POTTS_CELL_FLY_NEURON_WT_POP = 2;
        public static final int SPLIT_OFFSET_PERCENT_X = 50;
        public static final int SPLIT_OFFSET_PERCENT_Y = 50;
        public static final Direction SPLIT_DIRECTION = Direction.ZX_PLANE;
        // Split Probability 1 with ZX_PLANE division ensures apical cell remains stem
        public static final double SPLIT_PROBABILITY = 1;

        public PottsCellFlyStemSymmetric1StemApical(int id, int parent, int pop, CellState state, int age, int divisions,
                             Location location, boolean hasRegions, MiniBox parameters,
                             double criticalVolume, double criticalHeight,
                             EnumMap<Region, Double> criticalRegionVolumes,
                             EnumMap<Region, Double> criticalRegionHeights) {
            super(id, parent, pop, state, age, divisions, location, hasRegions, parameters,
                    criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights);
        }

        @Override
        public ArrayList<Integer> getSplitOffsetPercent() {
            ArrayList<Integer> splitOffsetPercent = new ArrayList<>();
            splitOffsetPercent.add(SPLIT_OFFSET_PERCENT_X);
            splitOffsetPercent.add(SPLIT_OFFSET_PERCENT_Y);
            return splitOffsetPercent;
        }

        @Override
        public Direction getSplitDirection() {
            return SPLIT_DIRECTION;
        }

        @Override
        public double getSplitProbability() {
            return SPLIT_PROBABILITY;
        }

        @Override
        public PottsCell makeDaughterCell(int newID, CellState newState, Location newLocation,
                                          MersenneTwisterFast random) {
            divisions++;
            MiniBox newParameters = new MiniBox();
            for (String key : this.getParameters().getKeys()) {
                newParameters.put(key, this.getParameters().get(key));
            }
            newParameters.put("proliferation/CELL_GROWTH_RATE", "0");
            return new PottsCellFlyNeuronWT(newID, id, POTTS_CELL_FLY_NEURON_WT_POP, newState, age, divisions, newLocation,
                    hasRegions, newParameters, criticalVolume, criticalHeight,
                    criticalRegionVolumes, criticalRegionHeights);
        }
    }

    public static final class PottsCellFlyStemSymmetric2StemApicalOrBoth extends PottsCellFlyStem {
        public static final int POTTS_CELL_FLY_NEURON_WT_POP = 2;
        public static final int SPLIT_OFFSET_PERCENT_X = 50;
        public static final int SPLIT_OFFSET_PERCENT_Y = 50;
        public static final Direction SPLIT_DIRECTION = Direction.ZX_PLANE;
        // Split Probability 1 with ZX_PLANE division ensures apical cell remains stem
        public static final double SPLIT_PROBABILITY = 1;


        public PottsCellFlyStemSymmetric2StemApicalOrBoth(int id, int parent, int pop, CellState state, int age, int divisions,
                             Location location, boolean hasRegions, MiniBox parameters,
                             double criticalVolume, double criticalHeight,
                             EnumMap<Region, Double> criticalRegionVolumes,
                             EnumMap<Region, Double> criticalRegionHeights) {
            super(id, parent, pop, state, age, divisions, location, hasRegions, parameters,
                    criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights);
        }

        @Override
        public ArrayList<Integer> getSplitOffsetPercent() {
            ArrayList<Integer> splitOffsetPercent = new ArrayList<>();
            splitOffsetPercent.add(SPLIT_OFFSET_PERCENT_X);
            splitOffsetPercent.add(SPLIT_OFFSET_PERCENT_Y);
            return splitOffsetPercent;
        }

        @Override
        public Direction getSplitDirection() {
            return SPLIT_DIRECTION;
        }

        @Override
        public double getSplitProbability() {
            return SPLIT_PROBABILITY;
        }

        @Override
        public PottsCell makeDaughterCell(int newID, CellState newState, Location newLocation,
                                          MersenneTwisterFast random) {
            divisions++;
            // 50% chance daughter is PottsCellFlyStemSymmetric2StemApical, 50% chance it is neuron
            if (random.nextBoolean()) {
                return new PottsCellFlyStemSymmetric2StemApicalOrBoth(newID, id, pop, newState, age,
                                                                      divisions, newLocation, hasRegions,
                                                                      this.getParameters(), criticalVolume,
                                                                      criticalHeight, criticalRegionVolumes,
                                                                      criticalRegionHeights);
            } else {
                MiniBox newParameters = new MiniBox();
                for (String key : this.getParameters().getKeys()) {
                    newParameters.put(key, this.getParameters().get(key));
                }
                newParameters.put("proliferation/CELL_GROWTH_RATE", "0");
                return new PottsCellFlyNeuronWT(newID, id, POTTS_CELL_FLY_NEURON_WT_POP, newState, age, divisions, newLocation,
                        hasRegions, newParameters, criticalVolume, criticalHeight,
                        criticalRegionVolumes, criticalRegionHeights);
            }
        }
    }
}