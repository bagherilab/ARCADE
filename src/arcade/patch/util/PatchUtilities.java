package arcade.patch.util;

import arcade.patch.agent.cell.PatchCell;
import arcade.patch.agent.cell.PatchCellTissue;
import arcade.patch.env.grid.PatchGrid;
import arcade.patch.env.location.PatchLocation;
import sim.util.Bag;

public class PatchUtilities {
    public final class Utilities {
        /**
         * Hidden utility class constructor.
         */
        protected Utilities() {
            throw new UnsupportedOperationException();
        }

    }

    public static boolean checkLocation(
            PatchLocation loc,
            PatchGrid grid,
            double addedVolume,
            Double maxHeight,
            Integer population,
            Integer maxDensity,
            Integer maxOccupancy,
            boolean checkAllCriticalHeights) {

        double locationVolume = loc.getVolume();
        double locationArea = loc.getArea();

        Bag bag = new Bag(grid.getObjectsAtLocation(loc));
        int n = bag.numObjs; // number of agents in location

        if (n == 0) return true;

        if (maxOccupancy != null && n >= maxOccupancy) {
            return false;
        }

        double proposedVolume = PatchCell.calculateTotalVolume(bag) + addedVolume;
        double proposedHeight = proposedVolume / locationArea;

        if (proposedVolume > locationVolume) {
            return false;
        }

        if (maxHeight != null && proposedHeight > maxHeight) {
            return false;
        }

        int count = 0;
        for (Object obj : bag) {
            PatchCell cell = (PatchCell) obj;
            if (checkAllCriticalHeights || cell instanceof PatchCellTissue) {
                if (proposedHeight > cell.getCriticalHeight()) {
                    return false;
                }
            }
            if (population != null && maxDensity != null) {
                if (cell.getPop() == population) {
                    count++;
                    if (count >= maxDensity) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
