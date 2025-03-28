package arcade.core.env.lattice;

import sim.engine.Schedule;
import sim.engine.Steppable;
import arcade.core.env.location.Location;
import arcade.core.env.operation.Operation;
import arcade.core.env.operation.OperationCategory;
import arcade.core.util.MiniBox;

/**
 * A {@code Lattice} represents an environment layer.
 *
 * <p>Each lattice is a 3D array of doubles, where the values can represent molecular concentrations
 * or other continuous quantities. Each lattice is associated with {@link Operation} objects that
 * characterize environmental behaviors. The {@link Operation} object(s) are stepped during the step
 * method of the {@code Lattice}.
 */
public interface Lattice extends Steppable {
    /**
     * Gets the underlying lattice array.
     *
     * @return the array
     */
    double[][][] getField();

    /**
     * Gets the length of the lattice (x direction).
     *
     * @return the length of the lattice
     */
    int getLength();

    /**
     * Gets the width of the lattice (y direction).
     *
     * @return the width of the lattice
     */
    int getWidth();

    /**
     * Gets the height of the lattice (z direction).
     *
     * @return the height of the lattice
     */
    int getHeight();

    /**
     * Gets the lattice operation object.
     *
     * @param category the operation category
     * @return the lattice operation
     */
    Operation getOperation(OperationCategory category);

    /**
     * Gets the lattice layer parameters.
     *
     * @return a dictionary of parameters
     */
    MiniBox getParameters();

    /**
     * Sets the underlying array at the height index to the given array.
     *
     * @param values the array of values
     * @param index the height index
     */
    void setField(double[][] values, int index);

    /**
     * Sets the underlying array to the given array of values.
     *
     * @param values the array of values
     */
    void setField(double[][][] values);

    /**
     * Sets the underlying array to the given value.
     *
     * @param value the value to set
     */
    void setField(double value);

    /**
     * Gets the sum of values across lattice coordinates for the location.
     *
     * @param location the location
     * @return the sum value
     */
    double getTotalValue(Location location);

    /**
     * Gets the average value across lattice coordinates for the location.
     *
     * @param location the location
     * @return the average values
     */
    double getAverageValue(Location location);

    /**
     * Updates the value at the lattice coordinates for the location.
     *
     * @param location the location
     * @param fraction the fraction change in value
     */
    void updateValue(Location location, double fraction);

    /**
     * Increments the value at the lattice coordinates for the location.
     *
     * @param location the location
     * @param increment the change in value
     */
    void incrementValue(Location location, double increment);

    /**
     * Sets the value at the lattice coordinates corresponding to the location.
     *
     * @param location the location
     * @param value the new value
     */
    void setValue(Location location, double value);

    /**
     * Schedules the lattice in the simulation.
     *
     * @param schedule the simulation schedule
     */
    void schedule(Schedule schedule);
}
