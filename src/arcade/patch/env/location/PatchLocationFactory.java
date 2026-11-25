package arcade.patch.env.location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.logging.Logger;
import ec.util.MersenneTwisterFast;
import arcade.core.env.location.LocationContainer;
import arcade.core.env.location.LocationFactory;
import arcade.core.sim.Series;
import arcade.core.util.Utilities;
import arcade.patch.sim.PatchSeries;
import arcade.patch.util.PatchEnums.Direction;

/** Implementation of {@link LocationFactory} for {@link PatchLocation} objects. */
public abstract class PatchLocationFactory implements LocationFactory {
    /** Logger for {@code PatchLocationFactory}. */
    private static final Logger LOGGER = Logger.getLogger(PatchLocationFactory.class.getName());

    /** Random number generator instance. */
    MersenneTwisterFast random;

    /** Radius of the simulation. */
    int simulationRadius;

    /** Map of id to location. */
    public final HashMap<Integer, PatchLocationContainer> locations;

    /** Creates a factory for making {@link PatchLocation} instances. */
    public PatchLocationFactory() {
        locations = new HashMap<>();
    }

    /**
     * {@inheritDoc}
     *
     * <p>For series with no loader, a list of available patches are created based on population
     * settings. For series with a loader, the specified file is loaded into the factory.
     */
    @Override
    public void initialize(Series series, MersenneTwisterFast random) {
        this.random = random;
        this.simulationRadius = ((PatchSeries) series).radius;
        if (series.loader != null && series.loader.loadLocations) {
            loadLocations(series);
        } else {
            createLocations(series);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Loaded locations are mapped by their id.
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

        // Sort coordinates by distance if initialization scheme is not random.
        // For "outward" initialization, locations with distances closer to the
        // center are filled first. For "inward" initialization, locations with
        // distances further from the center are filled first.
        String initialization = patchSeries.patch.get("INITIALIZATION");
        if (!initialization.equalsIgnoreCase("random")) {
            coordinates.sort(Comparator.comparingDouble(Coordinate::calculateDistance));
            if (initialization.equalsIgnoreCase("inward")) {
                Collections.reverse(coordinates);
            }
        }

        // Create containers for each coordinate.
        int id = 1;
        for (Coordinate coordinate : coordinates) {
            PatchLocationContainer container = new PatchLocationContainer(id, coordinate);
            locations.put(id, container);
            id++;
        }
    }

    /**
     * Gets all coordinates for the given range, centered on the simulation center.
     *
     * @param radius the bound on the radius
     * @param depth the bound on the depth
     * @return a list of location coordinates
     */
    public ArrayList<Coordinate> getCoordinates(int radius, int depth) {
        return getCoordinates(radius, depth, Direction.CENTER, 0);
    }

    /**
     * Gets all coordinates for the given range, centered on an offset coordinate.
     *
     * <p>The center of the region is offset from the center of the simulation by {@code offset}
     * locations along the given direction. The offset is clamped such that the full region of the
     * given radius fits within the bounds of the simulation. Returned coordinates are always
     * centered on the middle slice, regardless of the offset.
     *
     * @param radius the bound on the radius
     * @param depth the bound on the depth
     * @param direction the direction to offset the center of the region along
     * @param offset the number of locations to offset the center of the region by
     * @return a list of location coordinates
     */
    public abstract ArrayList<Coordinate> getCoordinates(
            int radius, int depth, Direction direction, int offset);

    /**
     * Clamps the offset such that a region of the given radius fits in the simulation.
     *
     * <p>The furthest location of a region of the given radius centered at the returned offset is
     * guaranteed to lie within the bounds of the simulation. A warning is logged if the requested
     * offset is reduced.
     *
     * @param offset the requested offset
     * @param radius the bound on the radius of the region
     * @return the clamped offset
     */
    int clampOffset(int offset, int radius) {
        int maximum = Math.max(0, simulationRadius - radius);

        if (offset <= maximum) {
            return offset;
        }

        LOGGER.warning(
                String.format(
                        "offset [ %d ] with radius [ %d ] exceeds simulation radius [ %d ],"
                                + " clamped to [ %d ]",
                        offset, radius, simulationRadius, maximum));

        return maximum;
    }
}
