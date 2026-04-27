package arcade.patch.util;

import sim.util.Bag;
import arcade.patch.agent.cell.PatchCell;
import arcade.patch.agent.cell.PatchCellTissue;
import arcade.patch.env.location.PatchLocation;

public class PatchUtilities {
    public final class Utilities {
        /** Hidden utility class constructor. */
        protected Utilities() {
            throw new UnsupportedOperationException();
        }
    }

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

    public static boolean checkLocationDensity(
            PatchLocation loc, Bag bag, Integer population, Integer maxDensity) {
        if (population == null || maxDensity == null) {
            return true;
        }
        int count = 0;
        for (Object obj : bag) {
            PatchCell cell = (PatchCell) obj;
            if (cell.getPop() == population) {
                count++;
                if (count >= maxDensity) return false;
            }
        }

        return true;
    }

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
