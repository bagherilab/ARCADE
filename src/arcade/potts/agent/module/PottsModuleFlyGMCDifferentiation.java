package arcade.potts.agent.module;

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
     * Adds a cell to the simulation.
     *
     * <p>The cell location is split. The new neuron cell is created, initialized, and added to the
     * schedule. This cell's location is also assigned to a new Neuron cell.
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
        CellContainer newContainer = cell.make(newID, State.QUIESCENT, random);
        PottsCell newCell =
                (PottsCell) newContainer.convert(sim.getCellFactory(), newLocation, random);
        sim.getGrid().addObject(newCell, null);
        potts.register(newCell);
        newCell.reset(potts.ids, potts.regions);
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
                        oldCell.getCriticalVolume(),
                        oldCell.getCriticalHeight(),
                        oldCell.getCriticalRegionVolumes(),
                        oldCell.getCriticalRegionHeights());
        PottsCellFlyNeuron differentiatedGMC =
                (PottsCellFlyNeuron)
                        differentiatedGMCContainer.convert(sim.getCellFactory(), location, random);

        sim.getGrid().addObject(differentiatedGMC, null);
        potts.register(differentiatedGMC);
        differentiatedGMC.reset(potts.ids, potts.regions);
        differentiatedGMC.schedule(sim.getSchedule());
    }

    public void updateGrowthRate(Simulation sim) {
        if (!dynamicGrowthRateVolume) {
            cellGrowthRate = cellGrowthRateBase;
        } else {
            if (!pdeLike) {
                updateCellVolumeBasedGrowthRate(
                        cell.getLocation().getVolume(), cell.getCriticalVolume());
            } else {
                // PDE-like: use population-wide averages for GMCs (same pop as this cell)
                sim.util.Bag objs = sim.getGrid().getAllObjects();

                double volSum = 0.0;
                double critSum = 0.0;
                int count = 0;

                for (int i = 0; i < objs.numObjs; i++) {
                    Object o = objs.objs[i];
                    if (!(o instanceof arcade.potts.agent.cell.PottsCell)) continue;

                    arcade.potts.agent.cell.PottsCell c = (arcade.potts.agent.cell.PottsCell) o;
                    if (c.getPop() != cell.getPop()) continue; // keep to same population

                    if (o instanceof arcade.potts.agent.cell.PottsCellFlyGMC) {
                        arcade.potts.agent.cell.PottsCellFlyGMC gmc =
                                (arcade.potts.agent.cell.PottsCellFlyGMC) o;
                        volSum += gmc.getLocation().getVolume();
                        critSum += gmc.getCriticalVolume();
                        count++;
                    }
                }
                double avgVolume = volSum / count;
                double avgCritVol = critSum / count;
                updateCellVolumeBasedGrowthRate(avgVolume, avgCritVol);
                System.out.println("GMC " + cell.getID() + "growth rate = " + cellGrowthRate);
            }
        }
    }
}
