package arcade.potts.env.component;

import java.util.List;
import org.junit.jupiter.api.Test;
import sim.engine.Schedule;
import ec.util.MersenneTwisterFast;
import arcade.core.util.MiniBox;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.potts.env.component.PottsComponentVesselGrowth.Sprout;
import static arcade.potts.env.component.PottsVesselNode.Root;
import static arcade.potts.util.PottsEnums.Ordering;

public class PottsComponentVesselGrowthTest {
    private static final double EPSILON = 1E-9;

    // Slab thickness along z [um].
    private static final double T = 1000;

    // Voxel size [um].
    private static final double DS = 1;

    // Slice size in x and y [voxels].
    private static final int SIZE = 100;

    static MiniBox parameters() {
        MiniBox parameters = new MiniBox();
        parameters.put("INTERVAL", 4);
        parameters.put("P_MAX", 0);
        parameters.put("K_V", 1);
        parameters.put("D_TIP", 40);
        parameters.put("T_REFRACT", 24);
        parameters.put("BETA", 0);
        parameters.put("KAPPA", 0);
        parameters.put("V_TIP", 10);
        parameters.put("D_SUPPORT", 10);
        parameters.put("D_ANAST", 10);
        parameters.put("L_MAX", 200);
        parameters.put("T_REGRESS", 24);
        parameters.put("I_SPROUT", 0.2);
        parameters.put("K_MAT", 0);
        parameters.put("I_STABLE", 0.8);
        parameters.put("S_SURVIVE", 0.2);
        parameters.put("K_PRUNE", 0);
        parameters.put("VESSELS", "VESSELS");
        return parameters;
    }

    static PottsComponentVesselGrowth growth(MiniBox parameters) {
        return new PottsComponentVesselGrowth(parameters);
    }

    static double[][] field(double value) {
        double[][] field = new double[SIZE][SIZE];
        for (double[] row : field) {
            java.util.Arrays.fill(row, value);
        }
        return field;
    }

    static boolean[][] support(boolean value) {
        boolean[][] support = new boolean[SIZE][SIZE];
        for (boolean[] row : support) {
            java.util.Arrays.fill(row, value);
        }
        return support;
    }

    // Random number generator that always returns 0: every draw passes, first allowed direction.
    static MersenneTwisterFast zero() {
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        doReturn(0.0).when(random).nextDouble();
        return random;
    }

    // A perfused vessel along x at y = 50 um, z = 0, between two roots, with interior nodes every
    // 20 um.
    static PottsVesselNetwork line() {
        PottsVesselNetwork network = new PottsVesselNetwork(T);
        PottsVesselNode previous = network.addNode(0, 50, 0, Root.ARTERIAL);
        for (int x = 20; x <= 80; x += 20) {
            PottsVesselNode node = network.addNode(x, 50, 0, Root.NONE);
            network.addSegment(previous, node, 1);
            previous = node;
        }
        PottsVesselNode end = network.addNode(99, 50, 0, Root.VENOUS);
        network.addSegment(previous, end, 1);
        return network;
    }

    @Test
    public void constructor_invalidParameters_throws() {
        MiniBox bad = parameters();
        bad.put("V_TIP", 0);
        assertThrows(IllegalArgumentException.class, () -> growth(bad));
        MiniBox noVessels = parameters();
        noVessels.put("VESSELS", "");
        assertThrows(IllegalArgumentException.class, () -> growth(noVessels));
        MiniBox badIntegrity = parameters();
        badIntegrity.put("I_SPROUT", 2);
        assertThrows(IllegalArgumentException.class, () -> growth(badIntegrity));
    }

    @Test
    public void schedule_called_usesInterval() {
        Schedule schedule = mock(Schedule.class);
        PottsComponentVesselGrowth growth = growth(parameters());
        growth.schedule(schedule);
        verify(schedule).scheduleRepeating(growth, Ordering.COMPONENTS.ordinal(), 4);
    }

    @Test
    public void register_twice_throws() {
        PottsComponentVesselGrowth growth = growth(parameters());
        growth.register(null, "VEGF");
        assertThrows(IllegalStateException.class, () -> growth.register(null, "VEGF"));
    }

    @Test
    public void fibonacci_called_returnsUnitVectors() {
        for (double[] u : PottsComponentVesselGrowth.UNIT_DIRECTIONS) {
            assertEquals(1, Math.sqrt(u[0] * u[0] + u[1] * u[1] + u[2] * u[2]), EPSILON);
        }
        assertEquals(32, PottsComponentVesselGrowth.UNIT_DIRECTIONS.length);
    }

    @Test
    public void update_noVegf_makesNoSprouts() {
        MiniBox parameters = parameters();
        parameters.put("P_MAX", 1000);
        PottsComponentVesselGrowth growth = growth(parameters);
        PottsVesselNetwork network = line();
        boolean changed =
                growth.update(
                        network, field(0), support(true), DS, 1, 0, new MersenneTwisterFast(1));
        assertFalse(changed);
        assertEquals(5, network.getSegments().size());
        assertTrue(growth.getSprouts().isEmpty());
    }

    @Test
    public void update_withVegf_sproutsAndExtendsOneSegment() {
        MiniBox parameters = parameters();
        parameters.put("P_MAX", 1);
        parameters.put("D_TIP", 0);
        PottsComponentVesselGrowth growth = growth(parameters);
        PottsVesselNetwork network = line();
        boolean changed = growth.update(network, field(1), support(true), DS, 1, 0, zero());
        assertTrue(changed);
        // Four interior nodes, each sprouts once (no tip spacing): four new segments.
        assertEquals(9, network.getSegments().size());
        for (PottsVesselEdge segment : network.getSegments().subList(5, 9)) {
            assertEquals(0.2, segment.getIntegrity(), EPSILON);
            assertFalse(segment.isPerfused());
            assertEquals(
                    10,
                    PottsVesselGeometry.length(
                            segment.getStart().getPosition(), segment.getEnd().getPosition(), T),
                    0.1);
        }
    }

    @Test
    public void update_activeTipNearby_blocksNewSprout() {
        MiniBox parameters = parameters();
        parameters.put("P_MAX", 1);
        parameters.put("D_TIP", 30);
        PottsComponentVesselGrowth growth = growth(parameters);
        PottsVesselNetwork network = line();
        growth.update(network, field(1), support(true), DS, 1, 0, zero());
        // Nodes are visited in x order and the first direction is mostly +y, so each tip ends
        // about 21 um from the next node: x = 20 sprouts, 40 is blocked, 60 sprouts, 80 is blocked.
        assertEquals(7, network.getSegments().size());
    }

    @Test
    public void update_refractory_blocksSecondSprout() {
        MiniBox parameters = parameters();
        parameters.put("P_MAX", 1);
        parameters.put("D_TIP", 0);
        PottsComponentVesselGrowth growth = growth(parameters);
        PottsVesselNetwork network = line();
        growth.update(network, field(1), support(false), DS, 1, 0, zero());
        int sprouts = growth.getSprouts().size();
        assertEquals(4, sprouts);
        growth.update(network, field(1), support(false), DS, 1, 1, zero());
        assertEquals(sprouts, growth.getSprouts().size());
    }

    @Test
    public void update_noSupport_stallsThenDrops() {
        MiniBox parameters = parameters();
        parameters.put("P_MAX", 1);
        PottsComponentVesselGrowth growth = growth(parameters);
        PottsVesselNetwork network = line();
        assertFalse(growth.update(network, field(1), support(false), DS, 1, 0, zero()));
        assertEquals(5, network.getSegments().size());
        assertFalse(growth.getSprouts().isEmpty());
        growth.update(network, field(0), support(false), DS, 1, 24, zero());
        assertEquals(5, network.getSegments().size());
        assertTrue(growth.getSprouts().isEmpty());
    }

    @Test
    public void extend_largeBeta_climbsGradient() {
        MiniBox parameters = parameters();
        parameters.put("BETA", 1000);
        PottsComponentVesselGrowth growth = growth(parameters);
        double[][] field = new double[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                field[i][j] = 1 + i;
            }
        }
        MersenneTwisterFast random = new MersenneTwisterFast(3);
        for (int trial = 0; trial < 20; trial++) {
            PottsVesselNetwork network = line();
            PottsVesselNode base = network.findNode(40, 50, 0);
            Sprout sprout = new Sprout(base, null, 0, false);
            assertNotNull(growth.extend(network, sprout, field, support(true), DS, 1, random));
            assertTrue(sprout.heading[0] > 0.8);
        }
    }

    @Test
    public void extend_noBetaNoKappa_choosesAllDirections() {
        PottsComponentVesselGrowth growth = growth(parameters());
        MersenneTwisterFast random = new MersenneTwisterFast(3);
        int negative = 0;
        for (int trial = 0; trial < 100; trial++) {
            PottsVesselNetwork network = line();
            Sprout sprout = new Sprout(network.findNode(40, 50, 0), null, 0, false);
            growth.extend(network, sprout, field(1), support(true), DS, 1, random);
            if (sprout.heading[0] < 0) {
                negative++;
            }
        }
        assertTrue(negative > 25 && negative < 75);
    }

    @Test
    public void extend_outsideSlice_excluded() {
        PottsComponentVesselGrowth growth = growth(parameters());
        MersenneTwisterFast random = new MersenneTwisterFast(5);
        for (int trial = 0; trial < 30; trial++) {
            PottsVesselNetwork network = new PottsVesselNetwork(T);
            PottsVesselNode a = network.addNode(50, 0.5, 0, Root.ARTERIAL);
            PottsVesselNode b = network.addNode(60, 0.5, 0, Root.NONE);
            network.addSegment(a, b, 1);
            Sprout sprout = new Sprout(b, null, 0, false);
            PottsVesselNode tip =
                    growth.extend(network, sprout, field(1), support(true), DS, 1, random);
            assertTrue(tip.getPosition()[1] >= 0);
        }
    }

    @Test
    public void update_lengthCap_stopsExtension() {
        MiniBox parameters = parameters();
        parameters.put("P_MAX", 1);
        parameters.put("L_MAX", 10);
        parameters.put("D_TIP", 0);
        parameters.put("D_ANAST", 0);
        PottsComponentVesselGrowth growth = growth(parameters);
        PottsVesselNetwork network = line();
        growth.update(network, field(1), support(true), DS, 1, 0, zero());
        int segments = network.getSegments().size();
        for (Sprout sprout : growth.getSprouts()) {
            assertTrue(sprout.capped);
        }
        growth.update(network, field(1), support(true), DS, 1, 1, zero());
        // Refractory nodes do not sprout again and capped tips do not extend.
        assertEquals(segments, network.getSegments().size());
    }

    @Test
    public void anastomose_nearOtherVessel_joinsAndPerfuses() {
        PottsComponentVesselGrowth growth = growth(parameters());
        PottsVesselNetwork network = line();
        PottsVesselNode base = network.findNode(40, 50, 0);
        PottsVesselNode s1 = network.addNode(40, 60, 0, Root.NONE);
        PottsVesselNode s2 = network.addNode(40, 70, 0, Root.NONE);
        PottsVesselNode s3 = network.addNode(40, 80, 0, Root.NONE);
        network.addSegment(base, s1, 0.2);
        network.addSegment(s1, s2, 0.2);
        network.addSegment(s2, s3, 0.2);
        PottsVesselNode a = network.addNode(0, 85, 0, Root.ARTERIAL);
        PottsVesselNode m = network.addNode(45, 85, 0, Root.NONE);
        PottsVesselNode b = network.addNode(99, 85, 0, Root.VENOUS);
        network.addSegment(a, m, 1);
        network.addSegment(m, b, 1);

        Sprout sprout = new Sprout(s3, new double[] {0, 1, 0}, 0, false);
        sprout.excluded.addAll(PottsComponentVesselGrowth.nearby(network, base, 2));
        sprout.excluded.addAll(List.of(s1, s2, s3));
        assertTrue(growth.anastomose(network, sprout));
        PottsVesselPerfusion.update(network);
        for (PottsVesselEdge segment : network.getSegments(s2)) {
            assertTrue(segment.isPerfused());
        }
        assertEquals(2, network.getDegree(s3));
        assertEquals(3, network.getDegree(m));
    }

    @Test
    public void anastomose_onlyParentNearby_doesNotJoin() {
        PottsComponentVesselGrowth growth = growth(parameters());
        PottsVesselNetwork network = line();
        PottsVesselNode base = network.findNode(40, 50, 0);
        PottsVesselNode s1 = network.addNode(40, 55, 0, Root.NONE);
        network.addSegment(base, s1, 0.2);
        Sprout sprout = new Sprout(s1, new double[] {0, 1, 0}, 0, false);
        sprout.excluded.addAll(PottsComponentVesselGrowth.nearby(network, base, 2));
        assertFalse(growth.anastomose(network, sprout));
    }

    @Test
    public void update_deadEnd_regressesBackToBranch() {
        PottsComponentVesselGrowth growth = growth(parameters());
        PottsVesselNetwork network = line();
        PottsVesselNode base = network.findNode(40, 50, 0);
        PottsVesselNode s1 = network.addNode(40, 60, 0, Root.NONE);
        PottsVesselNode s2 = network.addNode(40, 70, 0, Root.NONE);
        network.addSegment(base, s1, 1);
        network.addSegment(s1, s2, 1);
        growth.update(network, field(0), support(true), DS, 1, 0, zero());
        assertEquals(1, growth.getSprouts().size());
        assertTrue(growth.getSprouts().get(0).dormant);
        assertEquals(7, network.getSegments().size());
        growth.update(network, field(0), support(true), DS, 1, 23, zero());
        assertEquals(7, network.getSegments().size());
        assertTrue(growth.update(network, field(0), support(true), DS, 1, 24, zero()));
        assertEquals(5, network.getSegments().size());
        assertFalse(network.contains(s1));
        assertTrue(network.contains(base));
        assertTrue(growth.getSprouts().isEmpty());
    }

    @Test
    public void update_dormantTip_activatesWithVegf() {
        MiniBox parameters = parameters();
        parameters.put("P_MAX", 1);
        parameters.put("T_REFRACT", 1000);
        PottsComponentVesselGrowth growth = growth(parameters);
        PottsVesselNetwork network = line();
        PottsVesselNode base = network.findNode(40, 50, 0);
        PottsVesselNode s1 = network.addNode(40, 60, 0, Root.NONE);
        network.addSegment(base, s1, 1);
        growth.update(network, field(1), support(true), DS, 1, 0, zero());
        assertFalse(network.contains(s1) && network.getDegree(s1) == 1);
    }

    @Test
    public void update_maturation_raisesPerfusedIntegrity() {
        MiniBox parameters = parameters();
        parameters.put("K_MAT", 0.1);
        PottsComponentVesselGrowth growth = growth(parameters);
        PottsVesselNetwork network = line();
        PottsVesselEdge segment = network.getSegments().get(0);
        segment.setIntegrity(0.2);
        assertTrue(growth.update(network, field(0), support(true), DS, 2, 0, zero()));
        assertEquals(1 - 0.8 * Math.exp(-0.2), segment.getIntegrity(), EPSILON);
    }

    @Test
    public void update_lowVegfImmature_prunes() {
        MiniBox parameters = parameters();
        parameters.put("K_PRUNE", 1000);
        PottsComponentVesselGrowth growth = growth(parameters);
        PottsVesselNetwork network = line();
        network.getSegments().get(2).setIntegrity(0.5);
        growth.update(network, field(0), support(true), DS, 1, 0, zero());
        // The immature segment is removed; stable segments stay.
        assertEquals(4, network.getSegments().size());
        for (PottsVesselEdge segment : network.getSegments()) {
            assertEquals(1, segment.getIntegrity(), EPSILON);
        }
    }

    @Test
    public void update_highVegfImmature_keeps() {
        MiniBox parameters = parameters();
        parameters.put("K_PRUNE", 1000);
        PottsComponentVesselGrowth growth = growth(parameters);
        PottsVesselNetwork network = line();
        network.getSegments().get(2).setIntegrity(0.5);
        growth.update(network, field(10), support(true), DS, 1, 0, zero());
        assertEquals(5, network.getSegments().size());
    }

    @Test
    public void isSupported_withinDistance_returnsTrue() {
        boolean[][] support = new boolean[SIZE][SIZE];
        support[10][10] = true;
        assertTrue(
                PottsComponentVesselGrowth.isSupported(support, DS, new double[] {15, 10.5, 0}, 5));
        assertFalse(
                PottsComponentVesselGrowth.isSupported(
                        support, DS, new double[] {16.5, 10.5, 0}, 5));
    }

    @Test
    public void supportMap_noCells_returnsFalse() {
        Potts potts = mock(Potts.class);
        potts.ids = new int[][][] {{{0, 1}, {2, 0}}};
        boolean[][] support = PottsComponentVesselGrowth.supportMap(potts);
        assertFalse(support[0][1]);
        assertFalse(support[1][0]);
    }

    @Test
    public void step_noVesselComponent_throws() {
        PottsSimulation sim = mock(PottsSimulation.class);
        PottsComponentVesselGrowth growth = growth(parameters());
        growth.register(sim, "VEGF");
        assertThrows(IllegalStateException.class, () -> growth.step(sim));
    }

    @Test
    public void distance_halfSlabApart_returnsHalfThickness() {
        assertEquals(
                T / 2,
                PottsComponentVesselGrowth.distance(
                        new double[] {0, 0, -T / 4}, new double[] {0, 0, T / 4}, T),
                EPSILON);
        assertEquals(
                5,
                PottsComponentVesselGrowth.distance(
                        new double[] {0, 0, T / 2 - 1}, new double[] {3, 0, -T / 2 + 3}, T),
                EPSILON);
    }

    @Test
    public void update_isolatedFragment_regressesWithoutError() {
        PottsComponentVesselGrowth growth = growth(parameters());
        PottsVesselNetwork network = line();
        PottsVesselNode a = network.addNode(40, 80, 0, Root.NONE);
        PottsVesselNode b = network.addNode(50, 80, 0, Root.NONE);
        PottsVesselNode c = network.addNode(60, 80, 0, Root.NONE);
        network.addSegment(a, b, 0.2);
        network.addSegment(b, c, 0.2);
        growth.update(network, field(0), support(true), DS, 1, 0, zero());
        assertEquals(2, growth.getSprouts().size());
        growth.update(network, field(1), support(true), DS, 1, 24, zero());
        assertFalse(network.contains(a));
        assertFalse(network.contains(c));
        assertTrue(growth.getSprouts().isEmpty());
        assertEquals(5, network.getSegments().size());
    }
}
