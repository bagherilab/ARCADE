package arcade.potts.agent.cell;

import java.util.EnumMap;
import arcade.core.agent.cell.CellState;
import arcade.core.env.location.Location;
import arcade.core.util.MiniBox;
import arcade.potts.agent.module.*;
import arcade.potts.util.PottsEnums.Region;
import arcade.potts.util.PottsEnums.State;
import ec.util.MersenneTwisterFast;
import static arcade.potts.util.PottsEnums.Direction;
import static arcade.potts.util.PottsFlyEnums.StemDaughter;

public abstract class PottsCellFlyStem extends PottsCell {
    /** Percentage offset from cell edge where division will occur */
    public final int splitOffsetPercent;

    /** Direction of division */
    public final Direction splitDirection;

    public PottsCellFlyStem(int id, int parent, int pop, CellState state, int age, int divisions,
                         Location location, boolean hasRegions, MiniBox parameters,
                         double criticalVolume, double criticalHeight,
                         EnumMap<Region, Double> criticalRegionVolumes,
                         EnumMap<Region, Double> criticalRegionHeights,
                         int splitOffsetPercent, Direction splitDirection) {
        super(id, parent, pop, state, age, divisions, location, hasRegions, parameters,
                criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights);
        this.splitOffsetPercent = splitOffsetPercent;
        this.splitDirection = splitDirection;
    }

    public abstract StemDaughter getStemDaughter();

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
            case QUIESCENT:
                module = new PottsModuleQuiescence(this);
                break;
            case PROLIFERATIVE:
                module = new PottsModuleProliferationFlyStem(this);
                break;
            case APOPTOTIC:
                module = new PottsModuleApoptosisSimple(this);
                break;
            case NECROTIC:
                module = new PottsModuleNecrosis(this);
                break;
            case AUTOTIC:
                module = new PottsModuleAutosis(this);
                break;
            default:
                module = null;
                break;
        }
    }

    public static final class PottsCellFlyStemWT extends PottsCellFlyStem {
        public static final int POTTS_CELL_FLY_NEURON_WT_POP = 2;

        public PottsCellFlyStemWT(int id, int parent, int pop, CellState state, int age, int divisions,
                             Location location, boolean hasRegions, MiniBox parameters,
                             double criticalVolume, double criticalHeight,
                             EnumMap<Region, Double> criticalRegionVolumes,
                             EnumMap<Region, Double> criticalRegionHeights,
                             int splitOffsetPercent, Direction splitDirection) {
            super(id, parent, pop, state, age, divisions, location, hasRegions, parameters,
                    criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights,
                    splitOffsetPercent, splitDirection);
        }

        @Override
        public StemDaughter getStemDaughter() {
            return StemDaughter.APICAL;
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

    public static final class PottsCellFlyStemMUDMut1StemRandom extends PottsCellFlyStem {
        public static final int POTTS_CELL_FLY_NEURON_WT_POP = 2;

        public PottsCellFlyStemMUDMut1StemRandom(int id, int parent, int pop, CellState state, int age, int divisions,
                             Location location, boolean hasRegions, MiniBox parameters,
                             double criticalVolume, double criticalHeight,
                             EnumMap<Region, Double> criticalRegionVolumes,
                             EnumMap<Region, Double> criticalRegionHeights,
                             int splitOffsetPercent, Direction splitDirection) {
            super(id, parent, pop, state, age, divisions, location, hasRegions, parameters,
                    criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights,
                    splitOffsetPercent, splitDirection);
        }

        @Override
        public StemDaughter getStemDaughter() {
            return StemDaughter.RANDOM;
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

        public PottsCellFlyStemMUDMut1StemLeft(int id, int parent, int pop, CellState state, int age, int divisions,
                             Location location, boolean hasRegions, MiniBox parameters,
                             double criticalVolume, double criticalHeight,
                             EnumMap<Region, Double> criticalRegionVolumes,
                             EnumMap<Region, Double> criticalRegionHeights,
                             int splitOffsetPercent, Direction splitDirection) {
            super(id, parent, pop, state, age, divisions, location, hasRegions, parameters,
                    criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights,
                    splitOffsetPercent, splitDirection);
        }

        @Override
        public StemDaughter getStemDaughter() {
            return StemDaughter.LEFT;
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

        public PottsCellFlyStemMUDMut2StemRandom(int id, int parent, int pop, CellState state, int age, int divisions,
                             Location location, boolean hasRegions, MiniBox parameters,
                             double criticalVolume, double criticalHeight,
                             EnumMap<Region, Double> criticalRegionVolumes,
                             EnumMap<Region, Double> criticalRegionHeights,
                             int splitOffsetPercent, Direction splitDirection) {
            super(id, parent, pop, state, age, divisions, location, hasRegions, parameters,
                    criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights,
                    splitOffsetPercent, splitDirection);
        }

        @Override
        public StemDaughter getStemDaughter() {
            return StemDaughter.RANDOM;
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
                        criticalRegionVolumes, criticalRegionHeights, splitOffsetPercent, splitDirection);
            } else if (random.nextDouble() < 0.5) {
                System.out.println("Making new MUDMut1StemRandom");
                System.out.println("Inside make method, growth rate is " + this.getParameters().get("proliferation/CELL_GROWTH_RATE"));
                return new PottsCellFlyStemMUDMut1StemRandom(newID, id, POTTS_STEM_1_POP, newState, age, divisions, newLocation,
                        hasRegions, this.getParameters(), criticalVolume, criticalHeight,
                        criticalRegionVolumes, criticalRegionHeights, splitOffsetPercent, splitDirection);
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

        public PottsCellFlyStemInvert1StemBasal(int id, int parent, int pop, CellState state, int age, int divisions,
                             Location location, boolean hasRegions, MiniBox parameters,
                             double criticalVolume, double criticalHeight,
                             EnumMap<Region, Double> criticalRegionVolumes,
                             EnumMap<Region, Double> criticalRegionHeights,
                             int splitOffsetPercent, Direction splitDirection) {
            super(id, parent, pop, state, age, divisions, location, hasRegions, parameters,
                    criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights,
                    splitOffsetPercent, splitDirection);
        }

        @Override
        public StemDaughter getStemDaughter() {
            return StemDaughter.BASAL;
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
        public static final int POTTS_STEM_1_POP = 1;

        public PottsCellFlyStemInvert2StemBasalOrBoth(int id, int parent, int pop, CellState state, int age, int divisions,
                             Location location, boolean hasRegions, MiniBox parameters,
                             double criticalVolume, double criticalHeight,
                             EnumMap<Region, Double> criticalRegionVolumes,
                             EnumMap<Region, Double> criticalRegionHeights,
                             int splitOffsetPercent, Direction splitDirection) {
            super(id, parent, pop, state, age, divisions, location, hasRegions, parameters,
                    criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights,
                    splitOffsetPercent, splitDirection);
        }

        @Override
        public StemDaughter getStemDaughter() {
            return StemDaughter.BASAL;
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
                        criticalRegionVolumes, criticalRegionHeights, splitOffsetPercent, splitDirection);
            } else if (random.nextDouble() < 0.5) {
                System.out.println("Making new PottsCellFlyStemInvert2StemRandom");
                System.out.println("Inside make method, growth rate is " + this.getParameters().get("proliferation/CELL_GROWTH_RATE"));
                return new PottsCellFlyStemInvert2StemBasalOrBoth(newID, id, POTTS_STEM_1_POP, newState, age, divisions, newLocation,
                        hasRegions, this.getParameters(), criticalVolume, criticalHeight,
                        criticalRegionVolumes, criticalRegionHeights, splitOffsetPercent, splitDirection);
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

        public PottsCellFlyStemSymmetric1StemApical(int id, int parent, int pop, CellState state, int age, int divisions,
                             Location location, boolean hasRegions, MiniBox parameters,
                             double criticalVolume, double criticalHeight,
                             EnumMap<Region, Double> criticalRegionVolumes,
                             EnumMap<Region, Double> criticalRegionHeights,
                             int splitOffsetPercent, Direction splitDirection) {
            super(id, parent, pop, state, age, divisions, location, hasRegions, parameters,
                    criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights,
                    splitOffsetPercent, splitDirection);
        }

        @Override
        public StemDaughter getStemDaughter() {
            return StemDaughter.APICAL;
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

        public PottsCellFlyStemSymmetric2StemApicalOrBoth(int id, int parent, int pop, CellState state, int age, int divisions,
                             Location location, boolean hasRegions, MiniBox parameters,
                             double criticalVolume, double criticalHeight,
                             EnumMap<Region, Double> criticalRegionVolumes,
                             EnumMap<Region, Double> criticalRegionHeights,
                             int splitOffsetPercent, Direction splitDirection) {
            super(id, parent, pop, state, age, divisions, location, hasRegions, parameters,
                    criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights,
                    splitOffsetPercent, splitDirection);
        }

        @Override
        public StemDaughter getStemDaughter() {
            return StemDaughter.APICAL;
        }

        @Override
        public PottsCell makeDaughterCell(int newID, CellState newState, Location newLocation,
                                          MersenneTwisterFast random) {
            divisions++;
            // 50% chance daughter is PottsCellFlyStemSymmetric2StemApical, 50% chance it is neuron
            if (random.nextBoolean()) {
                return new PottsCellFlyStemSymmetric2StemApicalOrBoth(newID, id, pop, newState, age, divisions, newLocation,
                        hasRegions, this.getParameters(), criticalVolume, criticalHeight,
                        criticalRegionVolumes, criticalRegionHeights, splitOffsetPercent, splitDirection);
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