package arcade.potts.util;

import arcade.potts.env.location.PottsLocation;
import arcade.potts.env.location.Voxel;
import sim.util.Bag;
import ec.util.MersenneTwisterFast;

import java.util.ArrayList;

/** Utility class providing static helper methods for Potts simulations. */
public final class PottsUtilities {

    /** Hidden utility class constructor. */
    protected PottsUtilities() {
        throw new UnsupportedOperationException();
    }

    public static void splitBagDupesRandomly(
            Bag firstBag, Bag secondBag, MersenneTwisterFast random) {
        for (int i = firstBag.numObjs - 1; i >= 0; i--) {
            Object obj = firstBag.objs[i];
            if (secondBag.contains(obj)) {
                if (random.nextBoolean()) {
                    secondBag.remove(obj);
                } else {
                    firstBag.remove(i);
                }
            }
        }
    }


    /**
     * Calculates the fraction of voxels in a daughter cell to distribute transcription factors.
     * Returns 0 if the voxels Bag is empty.
     *
     * @param voxels voxels in the region of interest
     * @param daughterLoc the daughter cell's location
     * @return fraction of voxels in the daughter cell
     */
    public static double voxelFraction(Bag voxels, PottsLocation daughterLoc) {
        if (voxels.numObjs == 0) {
            return 0;
        }

        ArrayList<Voxel> daughterVoxels = daughterLoc.getVoxels();
        double daughterCount = 0;

        for (int i = 0; i < voxels.numObjs; i++) {
            Voxel v = (Voxel) voxels.objs[i];
            for (Voxel d : daughterVoxels) {
                if (v.x == d.x && v.y == d.y && v.z == d.z) {
                    daughterCount++;
                    break;
                }
            }
        }

        return daughterCount / voxels.numObjs;
    }
}
