package arcade.patch.agent.cell;

import arcade.core.agent.cell.Cell;
import arcade.core.agent.cell.CellContainer;
import arcade.core.agent.cell.CellFactory;
import arcade.core.agent.cell.CellState;
import arcade.core.env.location.Location;
import arcade.core.util.MiniBox;

/**
 * Implementation of {@link CellContainer} for {@link PatchCell} agents.
 *
 * <p>Cell parameters are drawn from the associated {@link PatchCellFactory} instance for the given
 * population.
 */
public final class PatchCellContainer implements CellContainer {
    /** Unique cell container ID. */
    public final int id;

    /** Cell parent ID. */
    public final int parent;

    /** Cell population index. */
    public final int pop;

    /** Cell age [min]. */
    public final int age;

    /** Number of divisions. */
    public final int divisions;

    /** Cell state. */
    public final CellState state;

    /** Cell volume [um<sup>3</sup>]. */
    public final double volume;

    /** Cell height [um]. */
    public final double height;

    /** Critical cell volume [um<sup>3</sup>]. */
    public final double criticalVolume;

    /** Critical cell height [um]. */
    public final double criticalHeight;

    /**
     * Creates a {@code PatchCellContainer} instance.
     *
     * @param id the cell ID
     * @param parent the parent ID
     * @param pop the cell population index
     * @param age the cell age
     * @param divisions the number of cell divisions
     * @param state the cell state
     * @param volume the cell volume
     * @param height the cell height
     * @param criticalVolume the critical volume
     * @param criticalHeight the critical height
     */
    public PatchCellContainer(
            int id,
            int parent,
            int pop,
            int age,
            int divisions,
            CellState state,
            double volume,
            double height,
            double criticalVolume,
            double criticalHeight) {
        this.id = id;
        this.parent = parent;
        this.pop = pop;
        this.age = age;
        this.divisions = divisions;
        this.state = state;
        this.volume = volume;
        this.height = height;
        this.criticalVolume = criticalVolume;
        this.criticalHeight = criticalHeight;
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public Cell convert(CellFactory factory, Location location) {
        return convert((PatchCellFactory) factory, location);
    }

    /**
     * Converts the cell container into a {@link PatchCell}.
     *
     * @param factory the cell factory instance
     * @param location the cell location
     * @return a {@link PatchCell} instance
     */
    private Cell convert(PatchCellFactory factory, Location location) {
        // Get parameters for the cell population.
        MiniBox parameters = factory.popToParameters.get(pop);

        // Make cell.
        switch (parameters.get("CLASS")) {
            default:
            case "tissue":
                return new PatchCellTissue(
                        id,
                        parent,
                        pop,
                        state,
                        age,
                        divisions,
                        location,
                        parameters,
                        volume,
                        height,
                        criticalVolume,
                        criticalHeight);
            case "cancer":
                return new PatchCellCancer(
                        id,
                        parent,
                        pop,
                        state,
                        age,
                        divisions,
                        location,
                        parameters,
                        volume,
                        height,
                        criticalVolume,
                        criticalHeight);
            case "cancer_stem":
                return new PatchCellCancerStem(
                    id,
                    parent,
                    pop,
                    state,
                    age,
                    divisions,
                    location,
                    parameters,
                    volume,
                    height,
                    criticalVolume,
                    criticalHeight);
            case "cart_cd4":
                return new PatchCellCARTCD4(id, parent, pop, state, age, divisions, location,
                            parameters, volume, height, criticalVolume, criticalHeight);
            case "cart_cd8":
                return new PatchCellCARTCD8(id, parent, pop, state, age, divisions, location,
                                parameters, volume, height, criticalVolume, criticalHeight);
            case "random":
                return new PatchCellRandom(
                        id,
                        parent,
                        pop,
                        state,
                        age,
                        divisions,
                        location,
                        parameters,
                        volume,
                        height,
                        criticalVolume,
                        criticalHeight);
        }
    }
}
