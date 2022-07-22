package arcade.patch.env.loc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import ec.util.MersenneTwisterFast;
import arcade.core.env.loc.*;
import arcade.core.sim.Series;
import arcade.core.util.Utilities;
import arcade.patch.sim.PatchSeries;

/**
 * Implementation of {@link LocationFactory} for {@link PatchLocation} objects.
 */

public abstract class PatchLocationFactory implements LocationFactory {
    /** Random number generator instance. */
    MersenneTwisterFast random;
    
    /** Map of id to location. */
    public final HashMap<Integer, PatchLocationContainer> locations;
    
    /**
     * Creates a factory for making {@link PatchLocation} instances.
     */
    public PatchLocationFactory() {
        locations = new HashMap<>();
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * For series with no loader, a list of available patches are created based
     * on population settings.
     * For series with a loader, the specified file is loaded into the factory.
     */
    @Override
    public void initialize(Series series, MersenneTwisterFast random) {
        this.random = random;
        if (series.loader != null && series.loader.loadLocations) {
            loadLocations(series);
        } else {
            createLocations(series);
        }
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Loaded locations are mapped by their id.
     */
    @Override
    public void loadLocations(Series series) {
        // Load locations.
        ArrayList<LocationContainer> containers = series.loader.loadLocations();

        // Map loaded container to factory.
        for (LocationContainer container : containers) {
            PatchLocationContainer locationContainer = (PatchLocationContainer) container;
            locations.put(locationContainer.id, locationContainer);
        }
    }
    
    @Override
    public void createLocations(Series series) {
        PatchSeries patchSeries = (PatchSeries) series;
        
        // Get all valid coordinates.
        ArrayList<Coordinate> coordinates = getCoordinates(patchSeries.radius, patchSeries.depth);
        Utilities.shuffleList(coordinates, random);
        coordinates.sort(Comparator.comparingDouble(Coordinate::calculateDistance));
        
        // Create containers for each coordinate.
        int id = 1;
        for (Coordinate coordinate : coordinates) {
            PatchLocationContainer container = new PatchLocationContainer(id, coordinate);
            locations.put(id, container);
            id++;
        }
    }
    
    /**
     * Gets all coordinates for the given range.
     *
     * @param radius  the bound on the radius
     * @param depth  the bound on the depth
     * @return  a list of location coordinates
     */
    public abstract ArrayList<Coordinate> getCoordinates(int radius, int depth);
}
