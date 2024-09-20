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
    private final double splitProbability;

    /** Function to create daughter cells */
    private final DaughterCellMaker daughterCellMaker;

    public PottsCellFlyStem(int id, int parent, int pop, CellState state, int age, int divisions,
                            Location location, boolean hasRegions, MiniBox parameters,
                            double criticalVolume, double criticalHeight,
                            EnumMap<Region, Double> criticalRegionVolumes,
                            EnumMap<Region, Double> criticalRegionHeights,
                            int splitOffsetPercentX, int splitOffsetPercentY,
                            Direction splitDirection, double splitProbability,
                            DaughterCellMaker daughterCellMaker) {
        super(id, parent, pop, state, age, divisions, location, hasRegions, parameters,
                criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights);
        this.splitOffsetPercent = new ArrayList<>();
        this.splitOffsetPercent.add(splitOffsetPercentX);
        this.splitOffsetPercent.add(splitOffsetPercentY);
        this.splitDirection = splitDirection;
        this.splitProbability = splitProbability;
        this.daughterCellMaker = daughterCellMaker;
    }

    public ArrayList<Integer> getSplitOffsetPercent() {
        return splitOffsetPercent;
    }

    public Direction getSplitDirection() {
        return splitDirection;
    }

    public double getSplitProbability() {
        return splitProbability;
    }

    public PottsCell makeDaughterCell(int newID, CellState newState, Location newLocation,
                                      MersenneTwisterFast random) {
        return daughterCellMaker.makeDaughterCell(this, newID, newState, newLocation, random);
    }

    @Override
    public PottsCell make(int newID, CellState newState, Location newLocation, MersenneTwisterFast random) {
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

    @FunctionalInterface
    public interface DaughterCellMaker {
        PottsCell makeDaughterCell(PottsCellFlyStem parentCell, int newID, CellState newState,
                                   Location newLocation, MersenneTwisterFast random);
    }

    // Factory methods for each configuration

    // 1. PottsCellFlyStemWT
    public static PottsCellFlyStem createPottsCellFlyStemWT(int id, int parent, int pop, CellState state, int age,
                                                            int divisions, Location location, boolean hasRegions,
                                                            MiniBox parameters, double criticalVolume, double criticalHeight,
                                                            EnumMap<Region, Double> criticalRegionVolumes,
                                                            EnumMap<Region, Double> criticalRegionHeights) {
        int splitOffsetPercentX = 50;
        int splitOffsetPercentY = 66;
        Direction splitDirection = Direction.ZX_PLANE;
        // Apical daughter always stem
        double splitProbability = 1.0;
        int pottsCellFlyNeuronWTPop = 2;

        DaughterCellMaker daughterCellMaker = (parentCell, newID, newState, newLocation, random) -> {
            parentCell.divisions++;
            MiniBox newParameters = new MiniBox();
            for (String key : parentCell.getParameters().getKeys()) {
                newParameters.put(key, parentCell.getParameters().get(key));
            }
            newParameters.put("proliferation/CELL_GROWTH_RATE", "0");
            return new PottsCellFlyNeuronWT(newID, parentCell.getID(), pottsCellFlyNeuronWTPop,
                    newState, parentCell.age, parentCell.divisions, newLocation,
                    parentCell.hasRegions, newParameters, parentCell.criticalVolume, parentCell.criticalHeight,
                    parentCell.criticalRegionVolumes, parentCell.criticalRegionHeights);
        };

        return new PottsCellFlyStem(id, parent, pop, state, age, divisions, location, hasRegions, parameters,
                criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights,
                splitOffsetPercentX, splitOffsetPercentY, splitDirection, splitProbability, daughterCellMaker);
    }

    // 2. PottsCellFlyStemMUDMut1StemRandom
    public static PottsCellFlyStem createPottsCellFlyStemMUDMut1StemRandom(int id, int parent, int pop, CellState state, int age,
                                                                           int divisions, Location location, boolean hasRegions,
                                                                           MiniBox parameters, double criticalVolume, double criticalHeight,
                                                                           EnumMap<Region, Double> criticalRegionVolumes,
                                                                           EnumMap<Region, Double> criticalRegionHeights) {
        int splitOffsetPercentX = 50;
        int splitOffsetPercentY = 50;
        Direction splitDirection = Direction.YZ_PLANE;
        // Random daughter will remain stem
        double splitProbability = 0.5;
        int pottsCellFlyNeuronWTPop = 2;

        DaughterCellMaker daughterCellMaker = (parentCell, newID, newState, newLocation, random) -> {
            parentCell.divisions++;
            MiniBox newParameters = new MiniBox();
            for (String key : parentCell.getParameters().getKeys()) {
                newParameters.put(key, parentCell.getParameters().get(key));
            }
            newParameters.put("proliferation/CELL_GROWTH_RATE", "0");
            return new PottsCellFlyNeuronWT(newID, parentCell.getID(), pottsCellFlyNeuronWTPop,
                    newState, parentCell.age, parentCell.divisions, newLocation,
                    parentCell.hasRegions, newParameters, parentCell.criticalVolume, parentCell.criticalHeight,
                    parentCell.criticalRegionVolumes, parentCell.criticalRegionHeights);
        };

        return new PottsCellFlyStem(id, parent, pop, state, age, divisions, location, hasRegions, parameters,
                criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights,
                splitOffsetPercentX, splitOffsetPercentY, splitDirection, splitProbability, daughterCellMaker);
    }

    // 3. PottsCellFlyStemMUDMut1StemLeft
    public static PottsCellFlyStem createPottsCellFlyStemMUDMut1StemLeft(int id, int parent, int pop, CellState state, int age,
                                                                         int divisions, Location location, boolean hasRegions,
                                                                         MiniBox parameters, double criticalVolume, double criticalHeight,
                                                                         EnumMap<Region, Double> criticalRegionVolumes,
                                                                         EnumMap<Region, Double> criticalRegionHeights) {
        int splitOffsetPercentX = 50;
        int splitOffsetPercentY = 50;
        Direction splitDirection = Direction.YZ_PLANE;
        // Left daughter always stem
        double splitProbability = 1.0;
        int pottsCellFlyNeuronWTPop = 2;

        DaughterCellMaker daughterCellMaker = (parentCell, newID, newState, newLocation, random) -> {
            parentCell.divisions++;
            MiniBox newParameters = new MiniBox();
            for (String key : parentCell.getParameters().getKeys()) {
                newParameters.put(key, parentCell.getParameters().get(key));
            }
            newParameters.put("proliferation/CELL_GROWTH_RATE", "0");
            return new PottsCellFlyNeuronWT(newID, parentCell.getID(), pottsCellFlyNeuronWTPop,
                    newState, parentCell.age, parentCell.divisions, newLocation,
                    parentCell.hasRegions, newParameters, parentCell.criticalVolume, parentCell.criticalHeight,
                    parentCell.criticalRegionVolumes, parentCell.criticalRegionHeights);
        };

        return new PottsCellFlyStem(id, parent, pop, state, age, divisions, location, hasRegions, parameters,
                criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights,
                splitOffsetPercentX, splitOffsetPercentY, splitDirection, splitProbability, daughterCellMaker);
    }

    // 4. PottsCellFlyStemMUDMut2StemRandom
    public static PottsCellFlyStem createPottsCellFlyStemMUDMut2StemRandom(int id, int parent, int pop, CellState state, int age,
                                                                           int divisions, Location location, boolean hasRegions,
                                                                           MiniBox parameters, double criticalVolume, double criticalHeight,
                                                                           EnumMap<Region, Double> criticalRegionVolumes,
                                                                           EnumMap<Region, Double> criticalRegionHeights) {
        int splitOffsetPercentX = 50;
        int splitOffsetPercentY = 50;
        Direction splitDirection = Direction.YZ_PLANE;
        // Random daughter will remain stem
        double splitProbability = 0.5;
        int pottsCellFlyNeuronWTPop = 2;
        int pottsStem1Pop = 1;

        DaughterCellMaker daughterCellMaker = (parentCell, newID, newState, newLocation, random) -> {
            parentCell.divisions++;
            double rand = random.nextDouble();
            if (rand < 0.25) {
                System.out.println("Making new MUDMut2StemRandom");
                return createPottsCellFlyStemMUDMut2StemRandom(newID, parentCell.getID(), parentCell.pop, newState, parentCell.age,
                        parentCell.divisions, newLocation, parentCell.hasRegions, parentCell.getParameters(),
                        parentCell.criticalVolume, parentCell.criticalHeight,
                        parentCell.criticalRegionVolumes, parentCell.criticalRegionHeights);
            } else if (rand < 0.5) {
                System.out.println("Making new MUDMut1StemRandom");
                return createPottsCellFlyStemMUDMut1StemRandom(newID, parentCell.getID(), pottsStem1Pop, newState, parentCell.age,
                        parentCell.divisions, newLocation, parentCell.hasRegions, parentCell.getParameters(),
                        parentCell.criticalVolume, parentCell.criticalHeight,
                        parentCell.criticalRegionVolumes, parentCell.criticalRegionHeights);
            } else {
                System.out.println("Making new FlyNeuronWT");
                MiniBox newParameters = new MiniBox();
                for (String key : parentCell.getParameters().getKeys()) {
                    newParameters.put(key, parentCell.getParameters().get(key));
                }
                newParameters.put("proliferation/CELL_GROWTH_RATE", "0");
                return new PottsCellFlyNeuronWT(newID, parentCell.getID(), pottsCellFlyNeuronWTPop,
                        newState, parentCell.age, parentCell.divisions, newLocation,
                        parentCell.hasRegions, newParameters, parentCell.criticalVolume, parentCell.criticalHeight,
                        parentCell.criticalRegionVolumes, parentCell.criticalRegionHeights);
            }
        };

        return new PottsCellFlyStem(id, parent, pop, state, age, divisions, location, hasRegions, parameters,
                criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights,
                splitOffsetPercentX, splitOffsetPercentY, splitDirection, splitProbability, daughterCellMaker);
    }

    // 5. PottsCellFlyStemInvert1StemBasal
    public static PottsCellFlyStem createPottsCellFlyStemInvert1StemBasal(int id, int parent, int pop, CellState state, int age,
                                                                          int divisions, Location location, boolean hasRegions,
                                                                          MiniBox parameters, double criticalVolume, double criticalHeight,
                                                                          EnumMap<Region, Double> criticalRegionVolumes,
                                                                          EnumMap<Region, Double> criticalRegionHeights) {
        int splitOffsetPercentX = 50;
        int splitOffsetPercentY = 33;
        Direction splitDirection = Direction.ZX_PLANE;
        // Basal daughter always stem
        double splitProbability = 0.0;
        int pottsCellFlyNeuronWTPop = 2;

        DaughterCellMaker daughterCellMaker = (parentCell, newID, newState, newLocation, random) -> {
            parentCell.divisions++;
            MiniBox newParameters = new MiniBox();
            for (String key : parentCell.getParameters().getKeys()) {
                newParameters.put(key, parentCell.getParameters().get(key));
            }
            newParameters.put("proliferation/CELL_GROWTH_RATE", "0");
            return new PottsCellFlyNeuronWT(newID, parentCell.getID(), pottsCellFlyNeuronWTPop,
                    newState, parentCell.age, parentCell.divisions, newLocation,
                    parentCell.hasRegions, newParameters, parentCell.criticalVolume, parentCell.criticalHeight,
                    parentCell.criticalRegionVolumes, parentCell.criticalRegionHeights);
        };

        return new PottsCellFlyStem(id, parent, pop, state, age, divisions, location, hasRegions, parameters,
                criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights,
                splitOffsetPercentX, splitOffsetPercentY, splitDirection, splitProbability, daughterCellMaker);
    }

    // 6. PottsCellFlyStemInvert2StemBasalOrBoth
    public static PottsCellFlyStem createPottsCellFlyStemInvert2StemBasalOrBoth(int id, int parent, int pop, CellState state, int age,
                                                                                int divisions, Location location, boolean hasRegions,
                                                                                MiniBox parameters, double criticalVolume, double criticalHeight,
                                                                                EnumMap<Region, Double> criticalRegionVolumes,
                                                                                EnumMap<Region, Double> criticalRegionHeights) {
        int splitOffsetPercentX = 50;
        int splitOffsetPercentY = 33;
        Direction splitDirection = Direction.ZX_PLANE;
        // Basal daughter always stem
        double splitProbability = 0.0;
        int pottsCellFlyNeuronWTPop = 2;

        DaughterCellMaker daughterCellMaker = (parentCell, newID, newState, newLocation, random) -> {
            parentCell.divisions++;
            double rand = random.nextDouble();
            if (rand < 0.25) {
                System.out.println("Making new PottsCellFlyStemWT");
                return createPottsCellFlyStemWT(newID, parentCell.getID(), parentCell.pop, newState, parentCell.age,
                        parentCell.divisions, newLocation, parentCell.hasRegions, parentCell.getParameters(),
                        parentCell.criticalVolume, parentCell.criticalHeight,
                        parentCell.criticalRegionVolumes, parentCell.criticalRegionHeights);
            } else if (rand < 0.5) {
                System.out.println("Making new PottsCellFlyStemInvert2StemBasalOrBoth");
                return createPottsCellFlyStemInvert2StemBasalOrBoth(newID, parentCell.getID(), pottsCellFlyNeuronWTPop, newState, parentCell.age,
                        parentCell.divisions, newLocation, parentCell.hasRegions, parentCell.getParameters(),
                        parentCell.criticalVolume, parentCell.criticalHeight,
                        parentCell.criticalRegionVolumes, parentCell.criticalRegionHeights);
            } else {
                System.out.println("Making new FlyNeuronWT");
                MiniBox newParameters = new MiniBox();
                for (String key : parentCell.getParameters().getKeys()) {
                    newParameters.put(key, parentCell.getParameters().get(key));
                }
                newParameters.put("proliferation/CELL_GROWTH_RATE", "0");
                return new PottsCellFlyNeuronWT(newID, parentCell.getID(), pottsCellFlyNeuronWTPop,
                        newState, parentCell.age, parentCell.divisions, newLocation,
                        parentCell.hasRegions, newParameters, parentCell.criticalVolume, parentCell.criticalHeight,
                        parentCell.criticalRegionVolumes, parentCell.criticalRegionHeights);
            }
        };

        return new PottsCellFlyStem(id, parent, pop, state, age, divisions, location, hasRegions, parameters,
                criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights,
                splitOffsetPercentX, splitOffsetPercentY, splitDirection, splitProbability, daughterCellMaker);
    }

    // 7. PottsCellFlyStemSymmetric1StemApical
    public static PottsCellFlyStem createPottsCellFlyStemSymmetric1StemApical(int id, int parent, int pop, CellState state, int age,
                                                                              int divisions, Location location, boolean hasRegions,
                                                                              MiniBox parameters, double criticalVolume, double criticalHeight,
                                                                              EnumMap<Region, Double> criticalRegionVolumes,
                                                                              EnumMap<Region, Double> criticalRegionHeights) {
        int splitOffsetPercentX = 50;
        int splitOffsetPercentY = 50;
        Direction splitDirection = Direction.ZX_PLANE;
        // Apical daughter always stem
        double splitProbability = 1.0;
        int pottsCellFlyNeuronWTPop = 2;

        DaughterCellMaker daughterCellMaker = (parentCell, newID, newState, newLocation, random) -> {
            parentCell.divisions++;
            MiniBox newParameters = new MiniBox();
            for (String key : parentCell.getParameters().getKeys()) {
                newParameters.put(key, parentCell.getParameters().get(key));
            }
            newParameters.put("proliferation/CELL_GROWTH_RATE", "0");
            return new PottsCellFlyNeuronWT(newID, parentCell.getID(), pottsCellFlyNeuronWTPop,
                    newState, parentCell.age, parentCell.divisions, newLocation,
                    parentCell.hasRegions, newParameters, parentCell.criticalVolume, parentCell.criticalHeight,
                    parentCell.criticalRegionVolumes, parentCell.criticalRegionHeights);
        };

        return new PottsCellFlyStem(id, parent, pop, state, age, divisions, location, hasRegions, parameters,
                criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights,
                splitOffsetPercentX, splitOffsetPercentY, splitDirection, splitProbability, daughterCellMaker);
    }

    // 8. PottsCellFlyStemSymmetric2StemApicalOrBoth
    public static PottsCellFlyStem createPottsCellFlyStemSymmetric2StemApicalOrBoth(int id, int parent, int pop, CellState state, int age,
                                                                                    int divisions, Location location, boolean hasRegions,
                                                                                    MiniBox parameters, double criticalVolume, double criticalHeight,
                                                                                    EnumMap<Region, Double> criticalRegionVolumes,
                                                                                    EnumMap<Region, Double> criticalRegionHeights) {
        int splitOffsetPercentX = 50;
        int splitOffsetPercentY = 50;
        Direction splitDirection = Direction.ZX_PLANE;
        // Apical daughter always stem
        double splitProbability = 1.0;
        int pottsCellFlyNeuronWTPop = 2;

        DaughterCellMaker daughterCellMaker = (parentCell, newID, newState, newLocation, random) -> {
            parentCell.divisions++;
            if (random.nextBoolean()) {
                // 50% chance to create another stem cell
                return createPottsCellFlyStemSymmetric2StemApicalOrBoth(newID, parentCell.getID(), parentCell.pop,
                        newState, parentCell.age, parentCell.divisions, newLocation,
                        parentCell.hasRegions, parentCell.getParameters(), parentCell.criticalVolume,
                        parentCell.criticalHeight, parentCell.criticalRegionVolumes, parentCell.criticalRegionHeights);
            } else {
                // 50% chance to create neuron
                MiniBox newParameters = new MiniBox();
                for (String key : parentCell.getParameters().getKeys()) {
                    newParameters.put(key, parentCell.getParameters().get(key));
                }
                newParameters.put("proliferation/CELL_GROWTH_RATE", "0");
                return new PottsCellFlyNeuronWT(newID, parentCell.getID(), pottsCellFlyNeuronWTPop,
                        newState, parentCell.age, parentCell.divisions, newLocation,
                        parentCell.hasRegions, newParameters, parentCell.criticalVolume, parentCell.criticalHeight,
                        parentCell.criticalRegionVolumes, parentCell.criticalRegionHeights);
            }
        };

        return new PottsCellFlyStem(id, parent, pop, state, age, divisions, location, hasRegions, parameters,
                criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights,
                splitOffsetPercentX, splitOffsetPercentY, splitDirection, splitProbability, daughterCellMaker);
    }
}
