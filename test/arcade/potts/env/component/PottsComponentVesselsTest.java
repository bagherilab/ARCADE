package arcade.potts.env.component;

import org.junit.jupiter.api.Test;
import sim.engine.Schedule;
import arcade.core.util.MiniBox;
import arcade.potts.env.lattice.PottsLattice;
import arcade.potts.sim.PottsSimulation;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.core.util.MiniBox.TAG_SEPARATOR;
import static arcade.potts.env.component.PottsVesselNode.Root;
import static arcade.potts.util.PottsEnums.Ordering;

public class PottsComponentVesselsTest {
    private static final double EPSILON = 1E-9;

    // Slab thickness along z [um].
    private static final double T = 1000;

    // Voxel size [um].
    private static final double DS = 3;

    // Lattice size in x and y [voxels].
    private static final int SIZE = 12;

    static MiniBox parameters(double permeability) {
        MiniBox parameters = new MiniBox();
        parameters.put("P_O2", permeability);
        parameters.put("BLOOD_O2", 60);
        parameters.put("LAMBDA", 10);
        parameters.put("D_RECRUIT", 5);
        return parameters;
    }

    // Creates a lattice, with or without a diffuser.
    static PottsLattice makeLattice(boolean diffuser) {
        MiniBox parameters = new MiniBox();
        if (diffuser) {
            parameters.put("(OPERATION)" + TAG_SEPARATOR + "DIFFUSER", "");
            parameters.put("diffuser/DIFFUSIVITY", 10.0);
            parameters.put("diffuser/MAX_SUBSTEP", 2.0);
        }
        return new PottsLattice(SIZE, SIZE, 1, DS, 0.25, parameters);
    }

    static PottsSimulation makeSim(PottsLattice oxygen) {
        PottsSimulation sim = mock(PottsSimulation.class);
        doReturn(oxygen).when(sim).getLattice("O2");
        return sim;
    }

    // A perfused in-slice segment along x at y = 4.5 um (row 1), from x = 0 to 30 um, plus a
    // dead-end stub along y from (15, 4.5) to (15, 25.5) um.
    static PottsVesselNetwork network() {
        PottsVesselNetwork network = new PottsVesselNetwork(T);
        PottsVesselNode a = network.addNode(0, 4.5, 0, Root.ARTERIAL);
        PottsVesselNode b = network.addNode(15, 4.5, 0, Root.NONE);
        PottsVesselNode c = network.addNode(30, 4.5, 0, Root.VENOUS);
        PottsVesselNode tip = network.addNode(15, 25.5, 0, Root.NONE);
        network.addSegment(a, b, 1);
        network.addSegment(b, c, 0.5);
        network.addSegment(b, tip, 1);
        return network;
    }

    static PottsComponentVessels makeVessels(double permeability, PottsSimulation sim) {
        PottsComponentVessels vessels = new PottsComponentVessels(parameters(permeability));
        vessels.setNetwork(network());
        vessels.register(sim, "O2");
        return vessels;
    }

    @Test
    public void schedule_called_schedulesRepeatingAtComponents() {
        Schedule schedule = mock(Schedule.class);
        PottsComponentVessels vessels = new PottsComponentVessels(parameters(1));
        vessels.schedule(schedule);
        verify(schedule).scheduleRepeating(vessels, Ordering.COMPONENTS.ordinal(), 1);
    }

    @Test
    public void step_perfusedSegments_addExchangeScaledByWeight() {
        PottsLattice oxygen = makeLattice(true);
        PottsSimulation sim = makeSim(oxygen);
        makeVessels(0.4, sim).step(sim);

        // Integrity 1 for x in [0, 15) (voxels 0-4), 0.5 for x in [15, 30) (voxels 5-9).
        assertEquals(0.4, oxygen.getExchange()[0][0][1], EPSILON);
        assertEquals(0.4 * 60, oxygen.getExchangeTarget()[0][0][1], EPSILON);
        assertEquals(0.4, oxygen.getExchange()[0][4][1], EPSILON);
        assertEquals(0.4 * 0.5, oxygen.getExchange()[0][5][1], EPSILON);
        assertEquals(0.4 * 0.5, oxygen.getExchange()[0][8][1], EPSILON);
        assertEquals(0, oxygen.getExchange()[0][10][1], EPSILON);
        assertEquals(0, oxygen.getField()[0][0][1], EPSILON);
    }

    @Test
    public void step_deadEndStub_addsNothing() {
        PottsLattice oxygen = makeLattice(true);
        PottsSimulation sim = makeSim(oxygen);
        makeVessels(0.4, sim).step(sim);
        for (int j = 2; j < SIZE; j++) {
            assertEquals(0, oxygen.getExchange()[0][5][j], EPSILON);
        }
    }

    @Test
    public void step_repeatedTicks_reusesCache() {
        PottsLattice oxygen = makeLattice(true);
        PottsSimulation sim = makeSim(oxygen);
        PottsComponentVessels vessels = makeVessels(0.4, sim);
        vessels.step(sim);
        vessels.step(sim);
        assertEquals(0.8, oxygen.getExchange()[0][0][1], EPSILON);
    }

    @Test
    public void refresh_afterLoopCloses_suppliesStub() {
        PottsLattice oxygen = makeLattice(true);
        PottsSimulation sim = makeSim(oxygen);
        PottsComponentVessels vessels = makeVessels(0.4, sim);
        vessels.step(sim);
        assertEquals(0, oxygen.getExchange()[0][5][5], EPSILON);

        // Join the stub tip to the venous root, closing a loop through the stub.
        PottsVesselNetwork network = vessels.getNetwork();
        PottsVesselNode tip = network.addNode(15, 25.5, 0, Root.NONE);
        PottsVesselNode venous = network.addNode(30, 4.5, 0, Root.VENOUS);
        PottsVesselNode middle = network.addNode(30, 25.5, 0, Root.NONE);
        network.addSegment(tip, middle, 1);
        network.addSegment(middle, venous, 1);

        vessels.step(sim);
        assertEquals(0, oxygen.getExchange()[0][5][5], EPSILON);
        vessels.refresh();
        vessels.step(sim);
        assertEquals(0.4, oxygen.getExchange()[0][5][5], EPSILON);
    }

    @Test
    public void getSiteWeights_givenNetwork_matchesDeliverySites() {
        PottsSimulation sim = makeSim(makeLattice(true));
        PottsComponentVessels vessels = makeVessels(0.4, sim);
        PottsVesselNetwork reference = network();
        PottsVesselPerfusion.update(reference);
        double[][] expected = PottsVesselDelivery.siteWeights(reference, SIZE, SIZE, DS, 5);

        double[][] sites = vessels.getSiteWeights(sim);
        for (int i = 0; i < SIZE; i++) {
            assertArrayEquals(expected[i], sites[i], EPSILON);
        }
        assertTrue(sites[0][1] > 0);
        assertEquals(0, sites[5][5], EPSILON);
    }

    @Test
    public void step_zeroPermeability_doesNothingAndNeedsNoLayers() {
        PottsSimulation sim = mock(PottsSimulation.class);
        PottsComponentVessels vessels = makeVessels(0, sim);
        assertDoesNotThrow(() -> vessels.step(sim));
    }

    @Test
    public void step_layerWithoutDiffuser_throws() {
        PottsSimulation sim = makeSim(makeLattice(false));
        PottsComponentVessels vessels = makeVessels(0.4, sim);
        Exception e = assertThrows(IllegalStateException.class, () -> vessels.step(sim));
        assertTrue(e.getMessage().contains("O2"));
    }

    @Test
    public void step_noNetwork_throws() {
        PottsSimulation sim = makeSim(makeLattice(true));
        PottsComponentVessels vessels = new PottsComponentVessels(parameters(0.4));
        vessels.register(sim, "O2");
        Exception e = assertThrows(IllegalStateException.class, () -> vessels.step(sim));
        assertTrue(e.getMessage().contains("network"));
    }

    @Test
    public void constructor_invalidParameters_throws() {
        MiniBox negative = parameters(-1);
        assertThrows(IllegalArgumentException.class, () -> new PottsComponentVessels(negative));
        MiniBox noLambda = parameters(1);
        noLambda.put("LAMBDA", 0);
        assertThrows(IllegalArgumentException.class, () -> new PottsComponentVessels(noLambda));
    }
}
