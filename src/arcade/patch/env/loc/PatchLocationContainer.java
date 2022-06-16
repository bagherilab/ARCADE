package arcade.patch.env.loc;

import arcade.core.agent.cell.CellContainer;
import arcade.core.env.loc.Location;
import arcade.core.env.loc.LocationContainer;
import arcade.core.env.loc.LocationFactory;

/**
 * Implementation of {@link LocationContainer} for {@link PatchLocation} objects.
 */

public final class PatchLocationContainer implements LocationContainer {
    /** Unique location container ID. */
    public final int id;
    
    /** Location coordinates. */
    public final int[] coordinates;
    
    /**
     * Creates a {@code PatchLocationContainer} instance.
     * <p>
     * The container does not have any regions.
     *
     * @param id  the location ID
     */
    public PatchLocationContainer(int id, int[] coordinates) {
        this.id = id;
        this.coordinates = coordinates;
    }
    
    @Override
    public int getID() { return id; }
    
    @Override
    public Location convert(LocationFactory factory, CellContainer cell) {
        PatchLocation location;
        
        if (factory instanceof PatchLocationFactoryRect) {
            location = new PatchLocationRect(coordinates[0], coordinates[1], coordinates[2]);
        } else {
            location = new PatchLocationHex(coordinates[0], coordinates[1], coordinates[2], coordinates[3]);
        }
        
        return location;
    }
}
