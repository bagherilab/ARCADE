package arcade.potts.env.component;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static arcade.potts.env.component.PottsVesselNode.Root;

public class PottsVesselDeliveryTest {
    private static final double EPSILON = 1E-9;

    // Slab thickness along z [um].
    private static final double T = 1000;

    // Voxel size [um].
    private static final double DS = 3;

    // O2 diffusion length along z [um].
    private static final double LAMBDA = 10;

    // Lattice size in x and y [voxels].
    private static final int SIZE = 12;

    // Creates a network with one perfused segment between two roots.
    static PottsVesselNetwork perfused(double[] a, double[] b, double integrity) {
        PottsVesselNetwork network = new PottsVesselNetwork(T);
        PottsVesselNode start = network.addNode(a[0], a[1], a[2], Root.ARTERIAL);
        PottsVesselNode end = network.addNode(b[0], b[1], b[2], Root.VENOUS);
        network.addSegment(start, end, integrity);
        PottsVesselPerfusion.update(network);
        return network;
    }

    static double total(double[][] weights) {
        double sum = 0;
        for (double[] row : weights) {
            for (double v : row) {
                sum += v;
            }
        }
        return sum;
    }

    @Test
    public void exchangeWeights_vesselInSlice_givesIntegrityPerVoxel() {
        PottsVesselNetwork network =
                perfused(new double[] {0, 4.5, 0}, new double[] {30, 4.5, 0}, 0.8);
        double[][] w = PottsVesselDelivery.exchangeWeights(network, SIZE, SIZE, DS, LAMBDA);

        for (int i = 0; i < 10; i++) {
            assertEquals(0.8, w[i][1], EPSILON);
        }
        assertEquals(0, w[10][1], EPSILON);
        assertEquals(0, w[3][0], EPSILON);
        assertEquals(0.8 * 30 / DS, total(w), EPSILON);
    }

    @Test
    public void exchangeWeights_vesselNearSlice_isScaledByKernel() {
        PottsVesselNetwork near =
                perfused(new double[] {0, 4.5, 10}, new double[] {30, 4.5, 10}, 1);
        double[][] w = PottsVesselDelivery.exchangeWeights(near, SIZE, SIZE, DS, LAMBDA);
        assertEquals(Math.exp(-1), w[4][1], EPSILON);
        assertEquals(10 * Math.exp(-1), total(w), EPSILON);
    }

    @Test
    public void exchangeWeights_vesselBeyondCutoff_givesNothing() {
        PottsVesselNetwork far = perfused(new double[] {0, 4.5, 31}, new double[] {30, 4.5, 31}, 1);
        double[][] w = PottsVesselDelivery.exchangeWeights(far, SIZE, SIZE, DS, LAMBDA);
        assertEquals(0, total(w), EPSILON);
    }

    @Test
    public void exchangeWeights_vesselCrossingSlice_putsKernelIntegralInOneVoxel() {
        PottsVesselNetwork network =
                perfused(new double[] {4.5, 4.5, -40}, new double[] {4.5, 4.5, 40}, 1);
        double[][] w = PottsVesselDelivery.exchangeWeights(network, SIZE, SIZE, DS, LAMBDA);

        // Integral of exp(-|z| / lambda) over |z| <= 3 lambda, divided by ds.
        double expected = 2 * LAMBDA * (1 - Math.exp(-3)) / DS;
        assertEquals(expected, w[1][1], 0.01 * expected);
        assertEquals(w[1][1], total(w), EPSILON);
    }

    @Test
    public void exchangeWeights_unperfusedSegment_givesNothing() {
        PottsVesselNetwork network = new PottsVesselNetwork(T);
        PottsVesselNode root = network.addNode(0, 4.5, 0, Root.ARTERIAL);
        PottsVesselNode tip = network.addNode(30, 4.5, 0, Root.NONE);
        network.addSegment(root, tip, 1);
        PottsVesselPerfusion.update(network);

        assertEquals(
                0, total(PottsVesselDelivery.exchangeWeights(network, SIZE, SIZE, DS, LAMBDA)));
        assertEquals(0, total(PottsVesselDelivery.siteWeights(network, SIZE, SIZE, DS, 5)));
    }

    @Test
    public void exchangeWeights_vesselLeavingLattice_skipsOutsideSamples() {
        PottsVesselNetwork network =
                perfused(new double[] {24, 4.5, 0}, new double[] {54, 4.5, 0}, 1);
        double[][] w = PottsVesselDelivery.exchangeWeights(network, SIZE, SIZE, DS, LAMBDA);
        // Only x in [24, 36) is inside a 12-voxel lattice.
        assertEquals(12 / DS, total(w), EPSILON);
    }

    @Test
    public void siteWeights_vesselCrossingSlice_usesLengthWithinDistance() {
        PottsVesselNetwork network =
                perfused(new double[] {4.5, 4.5, -40}, new double[] {4.5, 4.5, 40}, 0.5);
        double[][] w = PottsVesselDelivery.siteWeights(network, SIZE, SIZE, DS, 5);
        assertEquals(0.5 * 10, w[1][1], EPSILON);
        assertEquals(w[1][1], total(w), EPSILON);
    }

    @Test
    public void siteWeights_vesselOutsideDistance_givesNothing() {
        PottsVesselNetwork network =
                perfused(new double[] {0, 4.5, 20}, new double[] {30, 4.5, 20}, 1);
        assertEquals(0, total(PottsVesselDelivery.siteWeights(network, SIZE, SIZE, DS, 5)));
    }

    @Test
    public void exchangeWeights_invalidParameters_throws() {
        PottsVesselNetwork network = perfused(new double[] {0, 0, 0}, new double[] {3, 0, 0}, 1);
        assertThrows(
                IllegalArgumentException.class,
                () -> PottsVesselDelivery.exchangeWeights(network, SIZE, SIZE, DS, 0));
        assertThrows(
                IllegalArgumentException.class,
                () -> PottsVesselDelivery.exchangeWeights(network, SIZE, SIZE, 0, LAMBDA));
        assertThrows(
                IllegalArgumentException.class,
                () -> PottsVesselDelivery.siteWeights(network, SIZE, SIZE, DS, T));
    }
}
