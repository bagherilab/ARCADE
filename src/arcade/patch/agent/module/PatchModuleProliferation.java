package arcade.patch.agent.module;

import sim.util.Bag;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellContainer;
import arcade.core.sim.Simulation;
import arcade.core.util.Parameters;
import arcade.patch.agent.cell.PatchCell;
import arcade.patch.agent.cell.PatchCellCART;
import arcade.patch.agent.cell.PatchCellSynNotch;
import arcade.patch.agent.process.PatchProcess;
import arcade.patch.env.grid.PatchGrid;
import arcade.patch.env.location.PatchLocation;
import static arcade.patch.util.PatchEnums.Domain;
import static arcade.patch.util.PatchEnums.State;

/**
 * Extension of {@link PatchModule} for proliferation.
 *
 * <p>During proliferation, the module is repeatedly stepped from its creation until either the cell
 * is no longer able to proliferate or it has successfully doubled in size and is able to create a
 * new cell object. The module will wait for at least {@code SYNTHESIS_DURATION} ticks to pass
 * before creating a new cell.
 */
public class PatchModuleProliferation extends PatchModule {
    /** Tracker for duration of cell cycle. */
    private int ticker;

    /** Target volume for division. */
    private final double targetVolume;

    /** Maximum tolerable height for cell. */
    private final double maxHeight;

    /** Time required for DNA synthesis [min]. */
    private final double synthesisDuration;

    /**
     * Creates a proliferation {@link PatchModule} for the given cell.
     *
     * <p>Loaded parameters include:
     *
     * <ul>
     *   <li>{@code SYNTHESIS_DURATION} = time required for DNA synthesis
     * </ul>
     *
     * @param cell the {@link PatchCell} the module is associated with
     */
    public PatchModuleProliferation(PatchCell cell) {
        super(cell);

        // Calculate thresholds.
        targetVolume = 2 * cell.getCriticalVolume();
        maxHeight = cell.getCriticalHeight();

        // Set loaded parameters.
        Parameters parameters = cell.getParameters();
        if (cell instanceof PatchCellCART) {
            synthesisDuration = parameters.getInt("proliferation/T_CELL_SYNTHESIS_DURATION");
        } else {
            synthesisDuration = parameters.getInt("proliferation/SYNTHESIS_DURATION");
        }
    }

    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {
        Bag bag = ((PatchGrid) sim.getGrid()).getObjectsAtLocation(location);
        double totalVolume = PatchCell.calculateTotalVolume(bag);
        double currentHeight = totalVolume / location.getArea();

        // Check if cell is no longer able to proliferate due to (i) other
        // condition that has caused its type to no longer be proliferative,
        // (ii) cell no longer exists at a tolerable height, or (iii) no
        // space in neighborhood to divide into. Otherwise, check if double
        // volume has been reached, and if so, create a new cell.
        if (currentHeight > maxHeight) {
            cell.setState(State.QUIESCENT);
        } else {
            PatchLocation newLocation =
                    PatchCell.selectBestLocation(
                            sim, location, cell.getVolume() * 0.5, maxHeight, random);

            if (newLocation == null) {
                cell.setState(State.QUIESCENT);
            } else if (cell.getVolume() >= targetVolume) {
                if (ticker > synthesisDuration) {
                    // TODO: ADD CYCLE TIME TO TRACKER.

                    // Reset current cell.
                    cell.setState(State.UNDEFINED);

                    // Create and schedule new cell.
                    int newID = sim.getID();
                    CellContainer newContainer = cell.make(newID, State.UNDEFINED, random);
                    PatchCell newCell =
                            (PatchCell)
                                    newContainer.convert(
                                            sim.getCellFactory(),
                                            newLocation,
                                            random,
                                            cell.getParameters());
                    sim.getGrid().addObject(newCell, newLocation);
                    newCell.schedule(sim.getSchedule());

                    // Update cell volume and energy based on split.
                    double split = (random.nextDouble() / 10 + 0.45);
                    double volume = cell.getVolume();
                    double energy = cell.getEnergy();
                    cell.setVolume(volume * split);
                    cell.setEnergy(energy * split);
                    newCell.setVolume(volume * (1 - split));
                    newCell.setEnergy(energy * (1 - split));

                    // Update processes.
                    PatchProcess metabolism = (PatchProcess) newCell.getProcess(Domain.METABOLISM);
                    metabolism.update(cell.getProcess(Domain.METABOLISM));
                    if (cell instanceof PatchCellSynNotch) {
                        PatchProcess quorum = (PatchProcess) newCell.getProcess(Domain.QUORUM);
                        quorum.update(cell.getProcess(Domain.QUORUM));
                    } else if (cell instanceof PatchCellCART) {
                        PatchProcess inflammation =
                                (PatchProcess) newCell.getProcess(Domain.INFLAMMATION);
                        inflammation.update(cell.getProcess(Domain.INFLAMMATION));
                    } else {
                        PatchProcess signaling =
                                (PatchProcess) newCell.getProcess(Domain.SIGNALING);
                        signaling.update(cell.getProcess(Domain.SIGNALING));
                    }
                    // TODO: Update environment generator sites.
                } else {
                    ticker++;
                }
            }
        }
    }
}
