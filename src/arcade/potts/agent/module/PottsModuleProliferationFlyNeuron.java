package arcade.potts.agent.module;

import ec.util.MersenneTwisterFast;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.agent.cell.PottsCellFlyNeuronWT;
import arcade.potts.agent.cell.PottsCellFlyStem.StemType;
import arcade.potts.env.location.PottsLocation;
import arcade.potts.env.location.PottsLocation2D;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import arcade.potts.util.PottsEnums.State;

/**
 * Extension of {@link PottsModuleProliferationSimple} with custom addCell method
 * for fly neuron proliferation behavior.
 */
public class PottsModuleProliferationFlyNeuron extends PottsModuleProliferationSimple {
    /**
     * Creates a simple proliferation {@code Module} for the given
     * {@link PottsCellFlyNeuronWT}.
     * ]
     * @param cell  the {@link PottsCellFlyNeuronWT} the module is associated with
     */
    public PottsModuleProliferationFlyNeuron(PottsCellFlyNeuronWT cell) {
        super(cell);  // Reuse the logic from PottsModuleProliferationSimple
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * The addCell method is overridden to implement specific behavior for
     * fly neuron cells in the proliferation process. Parameters are passed
     * to the {@link PottsLocation2D#split} method according to the
     * {@link PottsCellFlyNeuronWT} {@link StemType}
     */
    @Override
    void addCell(MersenneTwisterFast random, Simulation sim){
        Potts potts = ((PottsSimulation) sim).getPotts();
        
        PottsCellFlyNeuronWT flyNeuronCell = (PottsCellFlyNeuronWT) cell;
        if (flyNeuronCell.neuronGeneration == 1){
            // Split current location.
            Location newLocation = ((PottsLocation) cell.getLocation()).split(random);
            
            // Reset current cell.
            cell.reset(potts.ids, potts.regions);
            
            // Create and schedule new cell.
            int newID = sim.getID();
            PottsCell newCell = (PottsCell) cell.make(newID, State.PROLIFERATIVE, newLocation, random);
            sim.getGrid().addObject(newCell, null);
            potts.register(newCell);
            newCell.reset(potts.ids, potts.regions);
            newCell.schedule(sim.getSchedule());
        }
    }
}
