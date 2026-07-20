package arcade.potts.agent.module;

import sim.util.Bag;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellContainer;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.agent.cell.PottsCellContainer;
import arcade.potts.agent.cell.PottsCellFlyGMC;
import arcade.potts.agent.cell.PottsCellFlyNeuron;
import arcade.potts.env.location.PottsLocation2D;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import arcade.potts.util.PottsEnums.State;

/**
 * Implementation of {@link PottsModuleProliferationVolumeBasedDivision} for fly GMC agents. These
 * cells divide into two {@link PottsCellFlyNeuron} cells. The links must be set in the setup file
 * so that 100% of the daughter cells are Neurons.
 */
public class PottsModuleFlyGMCDifferentiation extends PottsModuleProliferationVolumeBasedDivision {

    /**
     * Indicates whether GMC growth rate is based on individual cell conditions or average cell
     * conditions.
     */
    Boolean pdeLike;

    /**
     * Creates a fly GMC proliferation module.
     *
     * @param cell the cell to which this module is attached
     */
    public PottsModuleFlyGMCDifferentiation(PottsCellFlyGMC cell) {
        super(cell);
        pdeLike = (cell.getParameters().getInt("proliferation/PDELIKE") != 0);
    }

    /**
     * Computes the expected equilibrium average GMC volume over one cell cycle.
     *
     * <p>In the Potts model, a cell's target volume is initialized to {@code criticalVolume} on
     * reset. The Potts energy immediately drives the cell's actual volume toward this target,
     * regardless of the current growth rate. As a result, the volume-regulated growth phase
     * effectively begins at {@code criticalVolume} (not the birth volume), even when {@code
     * VOLUME_BASED_CRITICAL_VOLUME} is off and birth volume is below {@code criticalVolume}.
     *
     * <p>The regulated growth phase therefore runs from {@code criticalVolume} to {@code sizeTarget
     * * criticalVolume}. Under constant-rate growth, the time-average volume over this phase is the
     * arithmetic mean of the two endpoints:
     *
     * <pre>
     *   V_ref = (criticalVolume + sizeTarget * criticalVolume) / 2
     *         = criticalVolume * (1 + sizeTarget) / 2
     * </pre>
     *
     * <p>This formula is consistent with the PDE-like branch, which uses {@code avgCritVol * (1 +
     * sizeTarget) / 2}, and holds whether or not {@code VOLUME_BASED_CRITICAL_VOLUME} is enabled.
     *
     * @return the expected equilibrium average GMC volume
     */
    double computeEquilibriumVolume() {
        return cell.getCriticalVolume() * (1.0 + sizeTarget) / 2.0;
    }

    /**
     * Adds a cell to the simulation.
     *
     * <p>The cell location is split. The new neuron cell is created, initialized, and added to the
     * schedule. This cell's location is also assigned to a new Neuron cell. The critical volume of
     * both neurons is set to the initial volume of each neuron's location.
     *
     * @param random the random number generator
     * @param sim the simulation instance
     */
    @Override
    void addCell(MersenneTwisterFast random, Simulation sim) {
        Potts potts = ((PottsSimulation) sim).getPotts();

        // Split current location
        Location newLocation = ((PottsLocation2D) cell.getLocation()).split(random);

        // Reset current cell
        cell.reset(potts.ids, potts.regions);

        // Create and schedule new neuron cell
        int newID = sim.getID();
        CellContainer newContainer =
                ((PottsCellFlyGMC) cell)
                        .make(newID, State.QUIESCENT, newLocation.getVolume(), random);
        PottsCell newCell =
                (PottsCell) newContainer.convert(sim.getCellFactory(), newLocation, random);
        sim.getGrid().addObject(newCell, null);
        potts.register(newCell);
        newCell.initialize(potts.ids, potts.regions);
        newCell.schedule(sim.getSchedule());

        // remove old GMC cell from simulation
        PottsCellFlyGMC oldCell = (PottsCellFlyGMC) cell;
        Location location = oldCell.getLocation();
        sim.getGrid().removeObject(oldCell, location);
        oldCell.stop();

        // create new neuron cell and add to simulation.
        int newPop = oldCell.getLinks().next(random);

        PottsCellContainer differentiatedGMCContainer =
                new PottsCellContainer(
                        oldCell.getID(),
                        oldCell.getParent(),
                        newPop,
                        oldCell.getAge(),
                        oldCell.getDivisions(),
                        State.QUIESCENT,
                        null,
                        0,
                        null,
                        location.getVolume(),
                        oldCell.getCriticalHeight(),
                        oldCell.getCriticalRegionVolumes(),
                        oldCell.getCriticalRegionHeights());
        PottsCellFlyNeuron differentiatedGMC =
                (PottsCellFlyNeuron)
                        differentiatedGMCContainer.convert(sim.getCellFactory(), location, random);

        sim.getGrid().addObject(differentiatedGMC, null);
        potts.register(differentiatedGMC);
        differentiatedGMC.initialize(potts.ids, potts.regions);
        differentiatedGMC.schedule(sim.getSchedule());
    }

    /**
     * Updates the effective growth rate according to boolean flags specified in parameters.
     *
     * <p>The rule is selected as follows. When {@code DYNAMIC_GROWTH_RATE_VOLUME} is off the growth
     * rate is simply the basal rate. When it is on, {@code PDELIKE} chooses between a per-cell rule
     * (this cell's own volume against its equilibrium volume) and a PDE-like rule (population
     * averages across GMCs of the same population). {@code PDELIKE} therefore only has an effect
     * while {@code DYNAMIC_GROWTH_RATE_VOLUME} is on.
     *
     * @param sim the simulation
     */
    public void updateGrowthRate(Simulation sim) {
        if (!dynamicGrowthRateVolume) {
            cellGrowthRate = cellGrowthRateBase;
        } else if (!pdeLike) {
            updateCellVolumeBasedGrowthRate(
                    cell.getLocation().getVolume(), computeEquilibriumVolume());
        } else {
            // PDE-like: use population-wide averages for GMCs (same pop as this cell).
            // The reference volume is the population-average equilibrium volume:
            //   avgVRef = avgCritVol * (1 + sizeTarget) / 2
            VolumeAverages averages = getPopulationVolumeAverages(sim);

            if (averages.count == 0) {
                cellGrowthRate = cellGrowthRateBase;
            } else {
                double avgVRef = averages.averageCriticalVolume * (1.0 + sizeTarget) / 2.0;
                updateCellVolumeBasedGrowthRate(averages.averageVolume, avgVRef);
            }
        }
    }

    /**
     * Computes population-average volume statistics across GMCs sharing this cell's population.
     *
     * <p>The object grid is traversed once, accumulating both the current volume and the critical
     * volume of every {@link PottsCellFlyGMC} whose population matches this cell's.
     *
     * @param sim the simulation
     * @return the population volume averages, with a {@code count} of zero if no GMCs were found
     */
    VolumeAverages getPopulationVolumeAverages(Simulation sim) {
        Bag objs = sim.getGrid().getAllObjects();

        double volSum = 0.0;
        double critSum = 0.0;
        int count = 0;

        for (int i = 0; i < objs.numObjs; i++) {
            if (!(objs.objs[i] instanceof PottsCellFlyGMC)) {
                continue;
            }

            PottsCellFlyGMC gmc = (PottsCellFlyGMC) objs.objs[i];
            if (gmc.getPop() != cell.getPop()) {
                continue; // keep to same population
            }

            volSum += gmc.getLocation().getVolume();
            critSum += gmc.getCriticalVolume();
            count++;
        }

        if (count == 0) {
            return new VolumeAverages(0, 0.0, 0.0);
        }

        return new VolumeAverages(count, volSum / count, critSum / count);
    }

    /** Population-average volume statistics for the GMCs of a single population. */
    static final class VolumeAverages {

        /** Number of GMCs included in the averages. */
        final int count;

        /** Average current volume across the included GMCs. */
        final double averageVolume;

        /** Average critical volume across the included GMCs. */
        final double averageCriticalVolume;

        /**
         * Creates a set of population volume averages.
         *
         * @param count the number of GMCs included in the averages
         * @param averageVolume the average current volume
         * @param averageCriticalVolume the average critical volume
         */
        VolumeAverages(int count, double averageVolume, double averageCriticalVolume) {
            this.count = count;
            this.averageVolume = averageVolume;
            this.averageCriticalVolume = averageCriticalVolume;
        }
    }
}
