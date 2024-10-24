package arcade.potts.agent.cell;

import java.util.EnumMap;
import arcade.core.agent.cell.Cell;
import arcade.core.agent.cell.CellContainer;
import arcade.core.agent.cell.CellFactory;
import arcade.core.agent.cell.CellState;
import arcade.core.env.location.Location;
import arcade.core.util.MiniBox;
import arcade.potts.agent.module.PottsModule;
import static arcade.potts.util.PottsEnums.Phase;
import static arcade.potts.util.PottsEnums.Region;

/**
 * Implementation of {@link CellContainer} for {@link PottsCell} agents.
 *
 * <p>The container can be instantiated for cells with or without regions. Cell parameters are drawn
 * from the associated {@link PottsCellFactory} instance for the given population.
 */
public final class PottsCellContainer implements CellContainer {
    /** Unique cell container ID. */
    public final int id;

    /** Cell parent ID. */
    public final int parent;

    /** Cell population index. */
    public final int pop;

    /** Cell age [ticks]. */
    public final int age;

    /** Number of divisions. */
    public final int divisions;

    /** Cell state. */
    public final CellState state;

    /** Cell phase. */
    public final Phase phase;

    /** Cell size [voxels]. */
    public final int voxels;

    /** Cell region sizes [voxels]. */
    public final EnumMap<Region, Integer> regionVoxels;

    /** Critical cell volume [voxels]. */
    public final double criticalVolume;

    /** Critical cell height [voxels]. */
    public final double criticalHeight;

    /** Critical region cell volumes [voxels]. */
    public final EnumMap<Region, Double> criticalRegionVolumes;

    /** Critical region cell heights [voxels]. */
    public final EnumMap<Region, Double> criticalRegionHeights;

    /**
     * Creates a {@code PottsCellContainer} instance.
     *
     * <p>The container does not have any regions.
     *
     * @param id the cell ID
     * @param parent the parent ID
     * @param pop the cell population index
     * @param age the cell age
     * @param divisions the number of cell divisions
     * @param state the cell state
     * @param phase the cell phase
     * @param voxels the cell size
     * @param criticalVolume the critical volume
     * @param criticalHeight the critical height
     */
    public PottsCellContainer(
            int id,
            int parent,
            int pop,
            int age,
            int divisions,
            CellState state,
            Phase phase,
            int voxels,
            double criticalVolume,
            double criticalHeight) {
        this(
                id,
                parent,
                pop,
                age,
                divisions,
                state,
                phase,
                voxels,
                null,
                criticalVolume,
                criticalHeight,
                null,
                null);
    }

    /**
     * Creates a {@code PottsCellContainer} instance.
     *
     * @param id the cell ID
     * @param parent the parent ID
     * @param pop the cell population index
     * @param age the cell age
     * @param divisions the number of cell divisions
     * @param state the cell state
     * @param phase the cell phase
     * @param voxels the cell size
     * @param regionVoxels the cell region sizes
     * @param criticalVolume the critical volume
     * @param criticalHeight the critical height
     * @param criticalRegionVolumes the critical region volumes
     * @param criticalRegionHeights the critical surface heights
     */
    public PottsCellContainer(
            int id,
            int parent,
            int pop,
            int age,
            int divisions,
            CellState state,
            Phase phase,
            int voxels,
            EnumMap<Region, Integer> regionVoxels,
            double criticalVolume,
            double criticalHeight,
            EnumMap<Region, Double> criticalRegionVolumes,
            EnumMap<Region, Double> criticalRegionHeights) {
        this.id = id;
        this.parent = parent;
        this.pop = pop;
        this.age = age;
        this.divisions = divisions;
        this.state = state;
        this.phase = phase;
        this.voxels = voxels;
        this.regionVoxels = regionVoxels;
        this.criticalVolume = criticalVolume;
        this.criticalHeight = criticalHeight;
        this.criticalRegionVolumes = criticalRegionVolumes;
        this.criticalRegionHeights = criticalRegionHeights;
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public Cell convert(CellFactory factory, Location location) {
        return convert((PottsCellFactory) factory, location);
    }

    /**
     * Converts the cell container into a {@link PottsCell}.
     *
     * @param factory the cell factory instance
     * @param location the cell location
     * @return a {@link PottsCell} instance
     */
    private Cell convert(PottsCellFactory factory, Location location) {
        // Get parameters for the cell population.
        MiniBox parameters = factory.popToParameters.get(pop);

        // Make cell.
        PottsCell cell;
        switch (parameters.get("CLASS")) {
            default:
            case "stem":
                if (factory.popToRegions.get(pop)) {
                    cell = new PottsCellStem(this, location, parameters, true);
                } else {
                    cell = new PottsCellStem(this, location, parameters, false);
                }
        }

        // Update cell module.
        if (phase != null) {
            PottsModule module = (PottsModule) cell.getModule();
            module.setPhase(phase);
        }

        return cell;
    }
}
