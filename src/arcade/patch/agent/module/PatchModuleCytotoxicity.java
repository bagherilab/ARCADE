package arcade.patch.agent.module;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.core.util.Parameters;
import arcade.patch.agent.cell.PatchCell;
import arcade.patch.agent.cell.PatchCellCART;
import arcade.patch.agent.cell.PatchCellTissue;
import arcade.patch.agent.process.PatchProcessInflammation;
import static arcade.patch.util.PatchEnums.Domain;
import static arcade.patch.util.PatchEnums.State;

/**
 * Implementation of {@link Module} for killing tissue agents.
 *
 * <p>{@code PatchModuleCytotoxicity} is stepped once after a CD8 CAR T-cell binds to a target
 * tissue cell. The {@code PatchModuleCytotoxicity} determines if cell has enough granzyme to kill.
 * If so, it kills cell and calls the reset to neutral helper to return to neutral state. If not, it
 * waits until it has enough granzyme to kill cell.
 */
public class PatchModuleCytotoxicity extends PatchModule {
    private static final Logger logger = Logger.getLogger(PatchModuleCytotoxicity.class.getName());

    /** Target cell cytotoxic CAR T-cell is bound to. */
    PatchCellTissue target;

    /** CAR T-cell inflammation module. */
    PatchProcessInflammation inflammation;

    /** Amount of granzyme inside CAR T-cell. */
    double granzyme;

    /** Average time that T cell is bound to target [min]. */
    private final int timeDelay;

    /** Ticker to keep track of the time delay [min]. */
    private int ticker;

    /**
     * Creates a {@code PatchActionKill} for the given {@link PatchCellCART}.
     *
     * @param cell the {@link PatchCell} the helper is associated with
     */
    public PatchModuleCytotoxicity(PatchCell cell) {
        super(cell);
        this.target = (PatchCellTissue) ((PatchCellCART) cell).getBoundTarget();
        this.inflammation = (PatchProcessInflammation) cell.getProcess(Domain.INFLAMMATION);
        this.granzyme = inflammation.getInternal("granzyme");

        Parameters parameters = cell.getParameters();
        this.timeDelay = parameters.getInt("BOUND_TIME");
        this.ticker = 0;

        try {
            if (logger.getHandlers().length == 0) {
                FileHandler fileHandler = new FileHandler("apoptosis_log.txt", true);
                fileHandler.setFormatter(new SimpleFormatter());
                ConsoleHandler consoleHandler = new ConsoleHandler();
                consoleHandler.setFormatter(new SimpleFormatter());
                logger.addHandler(fileHandler);
                logger.addHandler(consoleHandler);
                logger.setLevel(Level.INFO);
                fileHandler.setLevel(Level.INFO);
                consoleHandler.setLevel(Level.INFO);
                logger.setUseParentHandlers(false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {
        if (cell.isStopped()) {
            return;
        }

        if (target.isStopped()) {
            ((PatchCellCART) cell).unbind();
            cell.setState(State.UNDEFINED);
            return;
        }

        if (ticker == 0) {
            if (granzyme >= 1) {
                PatchCellTissue tissueCell = (PatchCellTissue) target;
                tissueCell.setState(State.APOPTOTIC);
                logger.info(
                        "T cell "
                                + cell.getID()
                                + " lysis of tissueCell "
                                + tissueCell.getID()
                                + " at time "
                                + sim.getSchedule().getTime());
                granzyme--;
                inflammation.setInternal("granzyme", granzyme);
            }
        }

        if (ticker >= timeDelay) {
            ((PatchCellCART) cell).unbind();
            cell.setState(State.UNDEFINED);
        }

        ticker++;
    }
}
