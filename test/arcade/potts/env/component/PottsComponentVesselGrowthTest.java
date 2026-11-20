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

    // Largest sideways offset of a front from its aim line (R_OUT of a new sprout) [um].
    private static final double R = 7.4;

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
    public void constructor_negativeInjuryWeight_throws() {
        MiniBox bad = parameters();
        bad.put("INJURY_WEIGHT", -1);
        assertThrows(IllegalArgumentException.class, () -> growth(bad));
    }

    @Test
    public void signal_zeroWeight_returnsVegfFieldItself() {
        double[][] vegf = field(0.1);
        assertSame(vegf, PottsComponentVesselGrowth.signal(vegf, field(1), 0));
        assertSame(vegf, PottsComponentVesselGrowth.signal(vegf, null, 0));
    }

    @Test
    public void signal_positiveWeight_addsWeightedInjury() {
        double[][] vegf = field(0.1);
        double[][] injury = field(0.5);
        injury[3][4] = 2;
        double[][] signal = PottsComponentVesselGrowth.signal(vegf, injury, 0.3);
        assertNotSame(vegf, signal);
        assertEquals(0.1 + 0.3 * 0.5, signal[0][0], EPSILON);
        assertEquals(0.1 + 0.3 * 2, signal[3][4], EPSILON);
        assertEquals(0.1, vegf[3][4], EPSILON);
    }

    @Test
    public void signal_positiveWeightNoInjuryLayer_throws() {
        assertThrows(
                IllegalStateException.class,
                () -> PottsComponentVesselGrowth.signal(field(0.1), null, 0.3));
    }

    @Test
    public void update_injuryOnly_startsSprouts() {
        MiniBox parameters = parameters();
        parameters.put("P_MAX", 1000);
        parameters.put("INJURY_WEIGHT", 1);
        PottsComponentVesselGrowth growth = growth(parameters);
        PottsVesselNetwork network = line();
        double[][] signal = PottsComponentVesselGrowth.signal(field(0), field(1), 1);
        growth.update(network, signal, support(true), DS, 1, 0, zero());
        assertFalse(growth.getSprouts().isEmpty());
    }

    @Test
    public void constructor_negativeFibrinSupport_throws() {
        MiniBox bad = parameters();
        bad.put("FIBRIN_SUPPORT", -0.1);
        assertThrows(IllegalArgumentException.class, () -> growth(bad));
    }

    @Test
    public void addFibrin_zeroThreshold_leavesSupportUnchanged() {
        boolean[][] support = support(false);
        support[1][1] = true;
        PottsComponentVesselGrowth.addFibrin(support, field(1), 0);
        PottsComponentVesselGrowth.addFibrin(support, null, 0);
        assertTrue(support[1][1]);
        assertFalse(support[2][2]);
    }

    @Test
    public void addFibrin_positiveThreshold_marksVoxelsAboveIt() {
        boolean[][] support = support(false);
        support[0][0] = true;
        double[][] matrix = field(0.4);
        matrix[5][6] = 0.6;
        matrix[7][8] = 0.5;
        PottsComponentVesselGrowth.addFibrin(support, matrix, 0.5);
        assertTrue(support[5][6]);
        assertFalse(support[7][8]);
        assertFalse(support[1][1]);
        assertTrue(support[0][0]);
    }

    @Test
    public void addFibrin_positiveThresholdNoLayer_throws() {
        assertThrows(
                IllegalStateException.class,
                () -> PottsComponentVesselGrowth.addFibrin(support(false), null, 0.5));
    }

    @Test
    public void update_fibrinSupportOnly_tipExtends() {
        PottsComponentVesselGrowth growth = growth(parameters());
        PottsVesselNetwork network = line();
        Sprout sprout = sprout(growth, network, 40, 0);
        int before = network.getSegments().size();
        boolean[][] support = support(false);
        PottsComponentVesselGrowth.addFibrin(support, field(1), 0.5);
        growth.update(network, field(1), support, DS, 1, 0, zero());
        assertEquals(before + 1, network.getSegments().size());
        assertNotNull(sprout.heading);
    }

    @Test
    public void constructor_negativeInjuryTau_throws() {
        MiniBox bad = parameters();
        bad.put("INJURY_TAU", -1);
        assertThrows(IllegalArgumentException.class, () -> growth(bad));
    }

    @Test
    public void injuryWeightAt_zeroTau_isConstant() {
        MiniBox parameters = parameters();
        parameters.put("INJURY_WEIGHT", 0.5);
        PottsComponentVesselGrowth growth = growth(parameters);
        assertEquals(0.5, growth.injuryWeightAt(0), EPSILON);
        assertEquals(0.5, growth.injuryWeightAt(1000), EPSILON);
    }

    @Test
    public void injuryWeightAt_positiveTau_decaysExponentially() {
        MiniBox parameters = parameters();
        parameters.put("INJURY_WEIGHT", 2);
        parameters.put("INJURY_TAU", 48);
        PottsComponentVesselGrowth growth = growth(parameters);
        assertEquals(2, growth.injuryWeightAt(0), EPSILON);
        assertEquals(2 * Math.exp(-1), growth.injuryWeightAt(48), EPSILON);
        assertEquals(2 * Math.exp(-3), growth.injuryWeightAt(144), EPSILON);
    }

    @Test
    public void constructor_badSproutIntegrity_throws() {
        MiniBox bad = parameters();
        bad.put("SPROUT_MIN_INTEGRITY", 1.5);
        assertThrows(IllegalArgumentException.class, () -> growth(bad));
    }

    // A perfused line like line() whose segments have the given integrity.
    static PottsVesselNetwork line(double integrity) {
        PottsVesselNetwork network = new PottsVesselNetwork(T);
        PottsVesselNode previous = network.addNode(0, 50, 0, Root.ARTERIAL);
        for (int x = 20; x <= 80; x += 20) {
            PottsVesselNode node = network.addNode(x, 50, 0, Root.NONE);
            network.addSegment(previous, node, integrity);
            previous = node;
        }
        network.addSegment(previous, network.addNode(99, 50, 0, Root.VENOUS), integrity);
        return network;
    }

    @Test
    public void update_immatureVessel_doesNotSprout() {
        MiniBox parameters = parameters();
        parameters.put("P_MAX", 1000);
        parameters.put("SPROUT_MIN_INTEGRITY", 0.8);
        PottsComponentVesselGrowth growth = growth(parameters);
        growth.update(line(0.5), field(1), support(true), DS, 1, 0, zero());
        assertTrue(growth.getSprouts().isEmpty());
    }

    @Test
    public void update_matureVessel_sprouts() {
        MiniBox parameters = parameters();
        parameters.put("P_MAX", 1000);
        parameters.put("SPROUT_MIN_INTEGRITY", 0.8);
        PottsComponentVesselGrowth growth = growth(parameters);
        growth.update(line(0.8), field(1), support(true), DS, 1, 0, zero());
        assertFalse(growth.getSprouts().isEmpty());
    }

    @Test
    public void update_immatureVesselNoThreshold_sprouts() {
        MiniBox parameters = parameters();
        parameters.put("P_MAX", 1000);
        PottsComponentVesselGrowth growth = growth(parameters);
        growth.update(line(0.5), field(1), support(true), DS, 1, 0, zero());
        assertFalse(growth.getSprouts().isEmpty());
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

    // Growth coupled to tip cells, with every proposed segment in the plane or none.
    static PottsComponentVesselGrowth coupled(MiniBox parameters, boolean inPlane) {
        PottsComponentVesselGrowth growth = growth(parameters);
        growth.couple((a, b) -> inPlane);
        return growth;
    }

    // Field increasing along x (axis 0) or y (axis 1).
    static double[][] gradient(int axis) {
        double[][] field = new double[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                field[i][j] = 1 + (axis == 0 ? i : j);
            }
        }
        return field;
    }

    // Adds an active sprout at a node of the line, created at a time.
    static Sprout sprout(
            PottsComponentVesselGrowth growth, PottsVesselNetwork network, double x, double t) {
        PottsVesselNode node = network.findNode(x, 50, 0);
        Sprout sprout = new Sprout(node, null, t, false);
        sprout.excluded.addAll(PottsComponentVesselGrowth.nearby(network, node, 2));
        growth.getSprouts().add(sprout);
        return sprout;
    }

    @Test
    public void propose_inPlane_storesAimWithoutSegment() {
        PottsComponentVesselGrowth growth = coupled(parameters(), true);
        PottsVesselNetwork network = line();
        Sprout sprout = sprout(growth, network, 40, 0);
        assertFalse(growth.update(network, field(1), support(true), DS, 1, 0, zero()));
        assertEquals(5, network.getSegments().size());
        assertArrayEquals(new double[] {40, 50, 0}, sprout.aimStart, EPSILON);
        assertEquals(
                10, PottsComponentVesselGrowth.distance(sprout.aimStart, sprout.aimEnd, T), 1E-6);
        assertEquals(0, sprout.length, EPSILON);
        assertEquals(List.of(sprout), growth.getActiveInPlaneSprouts());
        assertTrue(growth.isCoupled());
        assertFalse(growth(parameters()).isCoupled());
    }

    @Test
    public void tip_reaimsHourly() {
        MiniBox parameters = parameters();
        parameters.put("BETA", 1000);
        PottsComponentVesselGrowth growth = coupled(parameters, true);
        PottsVesselNetwork network = line();
        Sprout sprout = sprout(growth, network, 40, 0);
        MersenneTwisterFast random = new MersenneTwisterFast(3);

        growth.update(network, gradient(0), support(true), DS, 1, 0, random);
        assertTrue(sprout.aimEnd[0] - sprout.aimStart[0] > 8, "aim " + sprout.aimEnd[0]);

        // The gradient turns to +y and the tip cell's front moved: the next hour re-aims from it.
        growth.setFront(sprout, 45, 52);
        growth.update(network, gradient(1), support(true), DS, 1, 1, random);
        assertArrayEquals(new double[] {45, 52, 0}, sprout.aimStart, EPSILON);
        assertTrue(sprout.aimEnd[1] - sprout.aimStart[1] > 8, "aim " + sprout.aimEnd[1]);
        assertEquals(5, network.getSegments().size());
    }

    @Test
    public void outOfPlane_unchanged() {
        // With every proposal out of the plane, coupled growth is exactly uncoupled growth.
        MiniBox parameters = parameters();
        parameters.put("P_MAX", 1);
        parameters.put("D_TIP", 0);
        parameters.put("KAPPA", 2);
        parameters.put("BETA", 1);
        PottsComponentVesselGrowth plain = growth(parameters);
        PottsComponentVesselGrowth outOfPlane = coupled(parameters, false);
        PottsVesselNetwork a = line();
        PottsVesselNetwork b = line();
        MersenneTwisterFast randomA = new MersenneTwisterFast(7);
        MersenneTwisterFast randomB = new MersenneTwisterFast(7);
        for (int hour = 0; hour < 5; hour++) {
            plain.update(a, gradient(1), support(true), DS, 1, hour, randomA);
            outOfPlane.update(b, gradient(1), support(true), DS, 1, hour, randomB);
        }
        assertTrue(a.getSegments().size() > 9);
        assertEquals(a.toJson(), b.toJson());
        assertEquals(randomA.nextDouble(), randomB.nextDouble());
        assertTrue(outOfPlane.getActiveInPlaneSprouts().isEmpty());
    }

    @Test
    public void appendSpeed_capped() {
        PottsComponentVesselGrowth growth = coupled(parameters(), true);
        PottsVesselNetwork network = line();
        Sprout sprout = sprout(growth, network, 40, 0);
        growth.propose(sprout, new double[] {40, 50, 0}, new double[] {0, 1, 0}, 10);

        // No aim, no segment; half an hour credits 5 um, less than the aim length (10 um).
        assertNull(growth.appendPoint(new Sprout(sprout.tip, null, 0, false), 40, 80, 5, 0, R));
        assertNull(growth.appendPoint(sprout, 40, 80, 0.5, 0, R));
        // After one hour, a front 30 um away is credited 10 um; after two hours, the credit is
        // still capped at the aim's end.
        assertArrayEquals(
                new double[] {40, 60, 0}, growth.appendPoint(sprout, 40, 80, 1, 0, R), 1E-9);
        assertArrayEquals(
                new double[] {40, 60, 0}, growth.appendPoint(sprout, 40, 80, 2, 0, R), 1E-9);
        // A front closer than the aim length is not due, unless within the tolerance; one farther
        // is appended along the aim, not toward the front.
        assertNull(growth.appendPoint(sprout, 40, 55, 5, 0, R));
        assertArrayEquals(
                new double[] {40, 59.5, 0}, growth.appendPoint(sprout, 40, 59.5, 5, 1, R), 1E-9);
        assertArrayEquals(
                new double[] {40, 60, 0}, growth.appendPoint(sprout, 46, 61, 5, 0, R), 1E-9);

        // An aim with a z part reaches less far in the plane; reaching its end is enough.
        growth.propose(sprout, new double[] {40, 50, 0}, new double[] {0, 0.6, 0.8}, 10);
        assertNull(growth.appendPoint(sprout, 40, 55.9, 5, 0, R));
        assertArrayEquals(
                new double[] {40, 56, 8}, growth.appendPoint(sprout, 40, 56, 5, 0, R), 1E-9);
    }

    @Test
    public void appendPoint_frontBehindOrBeside_noSegment() {
        PottsComponentVesselGrowth growth = coupled(parameters(), true);
        PottsVesselNetwork network = line();
        Sprout sprout = sprout(growth, network, 40, 0);
        growth.propose(sprout, new double[] {40, 50, 0}, new double[] {0, 1, 0}, 10);

        // A front far behind the node (through the parent vessel) is not credited.
        assertNull(growth.appendPoint(sprout, 40, 20, 5, 1, R));
        // Nor is one far enough forward but more than R_OUT to the side of the aim line.
        assertNull(growth.appendPoint(sprout, 40 + R + 0.1, 65, 5, 1, R));
        assertArrayEquals(
                new double[] {40, 60, 0},
                growth.appendPoint(sprout, 40 + R - 0.1, 65, 5, 1, R),
                1E-9);
        // A vertical aim has no in-plane direction and gets no point.
        growth.propose(sprout, new double[] {40, 50, 0}, new double[] {0, 0, 1}, 10);
        assertNull(growth.appendPoint(sprout, 40, 50, 5, 1, R));
    }

    @Test
    public void credit_forwardOnly() {
        double[] node = {40, 50, 0};
        double[] start = {40, 50, 0};
        double[] end = {40, 60, 0};
        // Behind the node: no credit.
        assertEquals(0, PottsComponentVesselGrowth.credit(node, start, end, 40, 45, 100, R), 0);
        assertEquals(0, PottsComponentVesselGrowth.credit(node, start, end, 40, 0, 100, R), 0);
        // Beyond R_OUT to the side: no credit.
        assertEquals(
                0, PottsComponentVesselGrowth.credit(node, start, end, 40 + R + 1, 58, 100, R), 0);
        // Forward: the forward projection, capped.
        assertEquals(8, PottsComponentVesselGrowth.credit(node, start, end, 43, 58, 100, R), 1E-12);
        assertEquals(5, PottsComponentVesselGrowth.credit(node, start, end, 43, 58, 5, R), 1E-12);
        // A diagonal aim projects the front onto its direction.
        double[] diagonal = {50, 60, 0};
        assertEquals(
                Math.sqrt(2) * 5,
                PottsComponentVesselGrowth.credit(node, start, diagonal, 45, 55, 100, R),
                1E-12);
        // An aim with no in-plane part (vertical) gets no credit.
        double[] vertical = {40, 50, 10};
        assertEquals(
                0, PottsComponentVesselGrowth.credit(node, start, vertical, 40, 60, 100, R), 0);
    }

    @Test
    public void append_atPoint_setsBookkeeping() {
        PottsComponentVesselGrowth growth = coupled(parameters(), true);
        PottsVesselNetwork network = line();
        Sprout sprout = sprout(growth, network, 40, 0);
        PottsVesselNode base = sprout.tip;
        growth.propose(sprout, new double[] {40, 50, 0}, new double[] {0, 1, 0}, 10);

        PottsVesselNode node = growth.append(network, sprout, 40, 62, 0, 3);
        assertNotNull(node);
        assertSame(node, sprout.tip);
        assertEquals(6, network.getSegments().size());
        assertEquals(3, network.getDegree(base));
        assertEquals(12, sprout.length, EPSILON);
        assertEquals(3, sprout.lastGrowth, EPSILON);
        assertArrayEquals(new double[] {0, 1, 0}, sprout.heading, EPSILON);
        assertEquals(0.2, network.getSegments(node).get(0).getIntegrity(), EPSILON);
        assertFalse(sprout.joined);
        assertFalse(sprout.capped);
        // A node already there, or an inactive sprout, appends nothing.
        assertNull(growth.append(network, sprout, 40, 62, 0, 4));
        assertNull(growth.append(network, new Sprout(node, null, 0, false), 40, 70, 0, 4));
    }

    @Test
    public void append_pastMaxLength_caps() {
        MiniBox parameters = parameters();
        parameters.put("L_MAX", 10);
        PottsComponentVesselGrowth growth = coupled(parameters, true);
        PottsVesselNetwork network = line();
        Sprout sprout = sprout(growth, network, 40, 0);
        growth.propose(sprout, new double[] {40, 50, 0}, new double[] {0, 1, 0}, 10);
        assertNotNull(growth.append(network, sprout, 40, 61, 0, 1));
        assertTrue(sprout.capped);
        assertTrue(growth.getActiveInPlaneSprouts().isEmpty());
    }

    @Test
    public void append_nearOtherVessel_joins() {
        PottsComponentVesselGrowth growth = coupled(parameters(), true);
        PottsVesselNetwork network = line();
        PottsVesselNode a = network.addNode(0, 85, 0, Root.ARTERIAL);
        PottsVesselNode m = network.addNode(45, 85, 0, Root.NONE);
        PottsVesselNode b = network.addNode(99, 85, 0, Root.VENOUS);
        network.addSegment(a, m, 1);
        network.addSegment(m, b, 1);
        Sprout sprout = sprout(growth, network, 40, 0);
        growth.propose(sprout, new double[] {40, 50, 0}, new double[] {0, 1, 0}, 10);
        assertNotNull(growth.append(network, sprout, 40, 60, 0, 1));
        assertFalse(sprout.joined);

        PottsVesselNode node = growth.append(network, sprout, 40, 78, 0, 2);
        assertNotNull(node);
        assertTrue(sprout.joined);
        assertFalse(growth.getSprouts().contains(sprout));
        assertEquals(2, network.getDegree(node));
        assertEquals(3, network.getDegree(m));
        for (PottsVesselEdge segment : network.getSegments(node)) {
            assertTrue(segment.isPerfused());
        }
    }

    @Test
    public void update_coupledNoAppend_regressesAfterTRegress() {
        PottsComponentVesselGrowth growth = coupled(parameters(), true);
        PottsVesselNetwork network = line();
        Sprout sprout = sprout(growth, network, 40, 0);
        for (int hour = 0; hour < 24; hour++) {
            growth.update(network, field(1), support(true), DS, 1, hour, zero());
            assertTrue(growth.getSprouts().contains(sprout), "hour " + hour);
        }
        growth.update(network, field(1), support(true), DS, 1, 24, zero());
        assertFalse(growth.getSprouts().contains(sprout));
        assertEquals(5, network.getSegments().size());
    }

    @Test
    public void update_coupledNoDirection_keepsAim() {
        PottsComponentVesselGrowth growth = coupled(parameters(), true);
        PottsVesselNetwork network = line();
        Sprout sprout = sprout(growth, network, 40, 0);
        growth.update(network, field(1), support(true), DS, 1, 0, zero());
        double[] aim = sprout.aimEnd.clone();
        growth.update(network, field(1), support(false), DS, 1, 1, zero());
        assertArrayEquals(aim, sprout.aimEnd, EPSILON);
    }

    // A line with an out-of-plane branch (40, 50, 0) -> (40, 60, 40) and an active sprout at its
    // end, whose front (if set) is offset from the node.
    static Sprout outOfPlaneSprout(
            PottsComponentVesselGrowth growth, PottsVesselNetwork network, boolean front) {
        PottsVesselNode base = network.findNode(40, 50, 0);
        PottsVesselNode tip = network.addNode(40, 60, 40, Root.NONE);
        network.addSegment(base, tip, 0.2);
        Sprout sprout = new Sprout(tip, null, 0, false);
        sprout.length = 10;
        sprout.excluded.addAll(PottsComponentVesselGrowth.nearby(network, tip, 2));
        growth.getSprouts().add(sprout);
        if (front) {
            growth.setFront(sprout, 60, 80);
        }
        return sprout;
    }

    @Test
    public void aim_outOfPlaneWithOffsetFront_extendsFromTipOneStep() {
        // In the plane only near z = 0; the tip is at z = 40, so every proposal is out of it.
        PottsComponentVesselGrowth coupled = growth(parameters());
        coupled.couple((a, b) -> Math.min(Math.abs(a[2]), Math.abs(b[2])) < 5);
        PottsComponentVesselGrowth plain = growth(parameters());
        PottsVesselNetwork a = line();
        PottsVesselNetwork b = line();
        Sprout sprout = outOfPlaneSprout(coupled, a, true);
        Sprout reference = outOfPlaneSprout(plain, b, false);
        PottsVesselNode tip = sprout.tip;
        MersenneTwisterFast randomA = new MersenneTwisterFast(11);
        MersenneTwisterFast randomB = new MersenneTwisterFast(11);

        coupled.update(a, field(1), support(true), DS, 1, 0, randomA);
        plain.update(b, field(1), support(true), DS, 1, 0, randomB);

        // One step of V_TIP dt from the tip node, not from the front: exactly as without coupling,
        // with the same number of random draws.
        assertEquals(b.toJson(), a.toJson());
        assertEquals(randomB.nextDouble(), randomA.nextDouble());
        assertNotSame(tip, sprout.tip);
        assertEquals(1, a.getSegments(sprout.tip).size());
        double[] start = tip.getPosition();
        double[] end = sprout.tip.getPosition();
        assertEquals(10, PottsVesselGeometry.length(start, end, T), 0.1);
        assertEquals(20, sprout.length, EPSILON);
        assertEquals(reference.length, sprout.length, EPSILON);
        assertArrayEquals(reference.heading, sprout.heading, EPSILON);
        assertNull(sprout.front);
        assertNull(sprout.aimEnd);
    }

    @Test
    public void append_tipNoLongerDeadEnd_returnsNull() {
        PottsComponentVesselGrowth growth = coupled(parameters(), true);
        PottsVesselNetwork network = line();
        Sprout sprout = sprout(growth, network, 40, 0);
        growth.propose(sprout, new double[] {40, 50, 0}, new double[] {0, 1, 0}, 10);
        PottsVesselNode node = growth.append(network, sprout, 40, 62, 0, 1);
        assertNotNull(node);
        PottsVesselNode other = network.addNode(50, 70, 0, Root.NONE);
        network.addSegment(node, other, 0.2);
        int segments = network.getSegments().size();
        assertNull(growth.append(network, sprout, 40, 75, 0, 3));
        assertEquals(segments, network.getSegments().size());
    }
}
