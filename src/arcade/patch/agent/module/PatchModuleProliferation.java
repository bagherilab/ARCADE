package arcade.patch.agent.module;

import sim.util.Bag;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellContainer;
import arcade.core.sim.Simulation;
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

    /** Tick the {@code Module} was started. */
    int duration;

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
        duration = 0;
        // Load parameters.
        synthesisDuration = cell.getSynthesisDuration();
    }

    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {
        Bag bag = ((PatchGrid) sim.getGrid()).getObjectsAtLocation(location);
        double totalVolume = PatchCell.calculateTotalVolume(bag);
        double currentHeight = totalVolume / location.getArea();
        duration++;
        // Check if cell is no longer able to proliferate due to (i) other
        // condition that has caused its type to no longer be proliferative,
        // (ii) cell no longer exists at a tolerable height, or (iii) no
        // space in neighborhood to divide into. Otherwise, check if double
        // volume has been reached, and if so, create a new cell.
        if (currentHeight > maxHeight) {
            cell.setState(State.QUIESCENT);
        } else {
            PatchLocation newLocation = cell.selectBestLocation(sim, random);

            if (newLocation == null) {
                if (cell instanceof PatchCellCART) {
                    cell.setState(State.PAUSED);
                } else {
                    cell.setState(State.QUIESCENT);
                }
            } else if (cell.getVolume() >= targetVolume) {
                if (ticker > synthesisDuration) {

                    cell.addCycle(duration);
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

                    if (newCell instanceof PatchCellSynNotch) {
                        PatchCellSynNotch newSynNotchCell = (PatchCellSynNotch) newCell;
                        PatchCellSynNotch synNotchCell = (PatchCellSynNotch) this.cell;
                        newSynNotchCell.setCARs(synNotchCell.getCARs());
                    }
                    // Update processes.
                    Domain[] processes = Domain.values();
                    for (Domain processName : processes) {
                        PatchProcess process = (PatchProcess) newCell.getProcess(processName);
                        if (process != null) {
                            process.update(cell.getProcess(processName));
                        }
                    }
                    // TODO: Update environment generator sites.
                } else {
                    ticker++;
                }
            }
        }
    }
}
