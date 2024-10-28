package arcade.potts.agent.module;

import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellContainer;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.agent.cell.PottsCellContainer;
import arcade.potts.agent.cell.PottsCellFlyGMC;
import arcade.potts.agent.cell.PottsCellFlyNeuronWT;
import arcade.potts.env.location.PottsLocation2D;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import arcade.potts.util.PottsEnums.State;

public class PottsModuleProliferationFlyGMC extends PottsModuleProliferationSimple {
    public PottsModuleProliferationFlyGMC(PottsCellFlyGMC cell) {
        super(cell);
    }

    @Override
    void addCell(MersenneTwisterFast random, Simulation sim) {
        Potts potts = ((PottsSimulation) sim).getPotts();

        // Split current location
        Location newLocation = ((PottsLocation2D) cell.getLocation()).split(random);

        // Reset current cell
        cell.reset(potts.ids, potts.regions);

        // Create and schedule new cell
        int newID = sim.getID();
        CellContainer newContainer = cell.make(newID, State.PROLIFERATIVE, random);
        PottsCell newCell = (PottsCell) newContainer.convert(sim.getCellFactory(), newLocation);
        sim.getGrid().addObject(newCell, null);
        potts.register(newCell);
        newCell.reset(potts.ids, potts.regions);
        newCell.schedule(sim.getSchedule());

        // change cell type to neuron
        // remove old cell from simulation
        System.out.println("PottsCellFlyGMC differentiating into neuron");
        PottsCellFlyGMC oldCell = (PottsCellFlyGMC) cell;
        Location location = oldCell.getLocation();
        sim.getGrid().removeObject(oldCell, location);
        oldCell.stop();

        // Create new cell and add to simulation.
        int newPop =
                oldCell.getLinks()
                        .next(random); // TODO: Sophia this is a bit of a clunky way of dealing
        // with this
        PottsCellContainer differentiatedGMCContainer =
                new PottsCellContainer(
                        oldCell.getID(),
                        oldCell.getParent(),
                        newPop,
                        oldCell.getAge(),
                        oldCell.getDivisions(),
                        State.PROLIFERATIVE,
                        null,
                        0,
                        null,
                        oldCell.getCriticalVolume(),
                        oldCell.getCriticalHeight(),
                        oldCell.getCriticalRegionVolumes(),
                        oldCell.getCriticalRegionHeights());
        PottsCellFlyNeuronWT differentiatedGMC =
                (PottsCellFlyNeuronWT)
                        differentiatedGMCContainer.convert(sim.getCellFactory(), location);
        sim.getGrid().addObject(differentiatedGMC, location);
        potts.register(differentiatedGMC);
        differentiatedGMC.reset(potts.ids, potts.regions);
        differentiatedGMC.schedule(sim.getSchedule());
    }
}
