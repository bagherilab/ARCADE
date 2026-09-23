package arcade.patch.env.location;

import arcade.core.agent.cell.CellContainer;
import arcade.core.env.location.Location;
import arcade.core.env.location.LocationContainer;
import arcade.core.env.location.LocationFactory;

/** Implementation of {@link LocationContainer} for {@link PatchLocation} objects. */
public final class PatchLocationContainer implements LocationContainer {
    /** Unique location container ID. */
    public final int id;

    /** Location coordinate. */
    public final Coordinate coordinate;

    /**
     * Creates a {@code PatchLocationContainer} instance.
     *
     * <p>The container does not have any regions.
     *
     * @param id the location ID
     * @param coordinate the location coordinate
     */
    public PatchLocationContainer(int id, Coordinate coordinate) {
        this.id = id;
        this.coordinate = coordinate;
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public Location convert(LocationFactory factory, CellContainer cell) {
        PatchLocation location;

        if (factory instanceof PatchLocationFactoryRect) {
            location = new PatchLocationRect((CoordinateXYZ) coordinate);
        } else {
            location = new PatchLocationHex((CoordinateUVWZ) coordinate);
        }
        return location;
    }
}
