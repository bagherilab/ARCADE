package arcade.potts.agent.module;

import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellContainer;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.Parameters;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.agent.cell.PottsCellContainer;
import arcade.potts.agent.cell.PottsCellFlyGMC;
import arcade.potts.agent.cell.PottsCellFlyNeuron;
import arcade.potts.env.location.PottsLocation2D;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import arcade.potts.util.PottsEnums.Phase;
import arcade.potts.util.PottsEnums.State;

/**
 * Implementation of {@link PottsModule} for fly GMC agents. These cells divide into two {@link
 * PottsCellFlyNeuron} cells. The links must be set in the setup file so that 100% of the daughter
 * cells are Neurons.
 */
public class PottsModuleFlyGMCDifferentiation extends PottsModule {

    /** Overall growth rate for cell (voxels/tick). */
    final double cellGrowthRate;

    /**
     * Target ratio of critical volume for division size checkpoint (cell must reach CRITICAL_VOLUME
     * * SIZE_TARGET * SIZE_CHECKPOINT to divide).
     */
    final double sizeTarget;

    /**
     * Creates a fly GMC proliferation module.
     *
     * @param cell the cell to which this module is attached
     */
    public PottsModuleFlyGMCDifferentiation(PottsCellFlyGMC cell) {
        super(cell);
        Parameters parameters = cell.getParameters();
        sizeTarget = parameters.getDouble("proliferation/SIZE_TARGET");
        cellGrowthRate = parameters.getDouble("proliferation/CELL_GROWTH_RATE");
        setPhase(Phase.UNDEFINED);
    }

    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {
        // Increase size of cell.
        System.out.println("Pre updateTarget volume: " + cell.getVolume());
        cell.updateTarget(cellGrowthRate, sizeTarget);
        System.out.println("Post updateTarget volume: " + cell.getVolume());
        boolean sizeCheck = cell.getVolume() >= sizeTarget * cell.getCriticalVolume();
        if (sizeCheck) {
            addCell(random, sim);
        }
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
}
