package arcade.core.agent.cell;

import sim.engine.Schedule;
import sim.engine.Steppable;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.module.Module;
import arcade.core.agent.process.Process;
import arcade.core.agent.process.ProcessDomain;
import arcade.core.env.location.Location;
import arcade.core.util.Parameters;

/**
 * A {@code Cell} object represents a cell agent.
 *
 * <p>Each cell is associated with a {@link Location} object that defines their physical location.
 * Each cell is also associated with a {@link Module} object and/or {@link Process} objects that
 * characterizes cellular behaviors and states. The {@link Module} or {@link Process} object(s) are
 * stepped during the step method of the {@code Cell}.
 */
public interface Cell extends Steppable {
    /**
     * Converts the cell into a {@link CellContainer}.
     *
     * @return a {@link CellContainer} instance
     */
    CellContainer convert();

    /**
     * Gets the unique cell ID.
     *
     * @return the cell ID
     */
    int getID();

    /**
     * Gets the cell parent ID.
     *
     * @return the parent ID
     */
    int getParent();

    /**
     * Gets the cell population index.
     *
     * @return the cell population
     */
    int getPop();

    /**
     * Gets the cell state.
     *
     * @return the cell state
     */
    CellState getState();

    /**
     * Gets the cell age.
     *
     * @return the cell age
     */
    int getAge();

    /**
     * Gets the cell divisions.
     *
     * @return the number of divisions
     */
    int getDivisions();

    /**
     * Gets the cell location object.
     *
     * @return the cell location
     */
    Location getLocation();

    /**
     * Gets the cell module object.
     *
     * @return the cell module
     */
    Module getModule();

    /**
     * Gets the cell process object.
     *
     * @param domain the process domain
     * @return the cell process
     */
    Process getProcess(ProcessDomain domain);

    /**
     * Gets the cell parameters.
     *
     * @return the cell parameters
     */
    Parameters getParameters();

    /**
     * Gets the cell volume.
     *
     * @return the cell volume
     */
    double getVolume();

    /**
     * Gets the cell height.
     *
     * @return the cell height
     */
    double getHeight();

    /**
     * Gets the critical volume.
     *
     * @return the critical volume
     */
    double getCriticalVolume();

    /**
     * Gets the critical height.
     *
     * @return the critical height
     */
    double getCriticalHeight();

    /**
     * Sets the cell state.
     *
     * @param state the cell state
     */
    void setState(CellState state);

    /** Stop the cell from stepping. */
    void stop();

    /**
     * Creates a new cell container.
     *
     * @param id the new cell ID
     * @param state the new cell state
     * @param random the random number generator
     * @return the new {@code Cell} object
     */
    CellContainer make(int id, CellState state, MersenneTwisterFast random);

    /**
     * Schedules the cell in the simulation.
     *
     * @param schedule the simulation schedule
     */
    void schedule(Schedule schedule);
}
