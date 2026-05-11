package arcade.patch.util;

import sim.util.Bag;
import arcade.patch.agent.cell.PatchCell;
import arcade.patch.agent.cell.PatchCellTissue;
import arcade.patch.env.location.PatchLocation;

/** Utility class providing static helper methods for patch simulations. */
public final class PatchUtilities {

    /** Hidden utility class constructor. */
    protected PatchUtilities() {
        throw new UnsupportedOperationException();
    }

    /**
     * Checks if the proposed height of a cell added to a location does not exceed height
     * constraints.
     *
     * @param loc the location
     * @param bag the cells currently in the location
     * @param addedVolume the volume of the cell being added
     * @param maxHeight the maximum height tolerance, or null to skip
     * @param checkAllCriticalHeights true to check all cells, false to check only tissue cells
     * @return true if the location height is within constraints, false otherwise
     */
    public static boolean checkLocationHeight(
            PatchLocation loc,
            Bag bag,
            double addedVolume,
            Double maxHeight,
            boolean checkAllCriticalHeights) {

        if (bag.numObjs == 0) {
            return true;
        }
        double proposedHeight = (PatchCell.calculateTotalVolume(bag) + addedVolume) / loc.getArea();
        if (maxHeight != null && proposedHeight > maxHeight) {
            return false;
        }

        for (Object obj : bag) {
            PatchCell cell = (PatchCell) obj;
            if (checkAllCriticalHeights || cell instanceof PatchCellTissue) {
                if (proposedHeight > cell.getCriticalHeight()) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Checks if the number of cells of a given population in a location does not exceed the maximum
     * density.
     *
     * @param bag the cells currently in the location
     * @param population the population index to check
     * @param maxDensity the maximum number of cells of the population allowed
     * @return true if the population density is within the maximum, false otherwise
     */
    public static boolean checkLocationDensity(Bag bag, int population, int maxDensity) {
        int count = 0;
        for (Object obj : bag) {
            PatchCell cell = (PatchCell) obj;
            if (cell.getPop() == population) {
                count++;
                if (count >= maxDensity) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Checks if the proposed volume added to a location does not exceed the location volume.
     *
     * @param bag the cells currently in the location
     * @param loc the location
     * @param maxOccupancy the maximum number of cells allowed in the location, or null to skip
     * @param addedVolume the volume of the cell being added
     * @return true if the location has sufficient volume, false otherwise
     */
    public static boolean checkLocationOccupancy(
            Bag bag, PatchLocation loc, Integer maxOccupancy, double addedVolume) {
        int n = bag.numObjs; // number of agents in location

        if (n == 0) {
            return true;
        }
        if (maxOccupancy != null && n >= maxOccupancy) {
            return false;
        }
        return !(PatchCell.calculateTotalVolume(bag) + addedVolume > loc.getVolume());
    }
}
