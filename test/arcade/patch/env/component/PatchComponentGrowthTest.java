package arcade.patch.env.component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sim.engine.Schedule;
import ec.util.MersenneTwisterFast;
import arcade.core.util.MiniBox;
import arcade.core.util.Parameters;
import arcade.core.util.exceptions.IncompatibleFeatureException;
import arcade.core.util.exceptions.MissingSpecificationException;
import arcade.patch.agent.process.PatchProcessMetabolism;
import arcade.patch.agent.process.PatchProcessSignaling;
import arcade.patch.env.component.PatchComponentSitesGraphFactory.EdgeDirection;
import arcade.patch.env.grid.PatchGrid;
import arcade.patch.env.lattice.PatchLattice;
import arcade.patch.sim.PatchSeries;
import arcade.patch.sim.PatchSimulation;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PatchComponentGrowthTest {
    static final double EPSILON = 1E-8;

    static PatchSeries seriesMock;

    static PatchSimulation simMock;

    static Parameters parametersMock;

    static PatchProcessMetabolism metabolismMock;

    static PatchProcessSignaling signalingMock;

    static PatchGrid gridMock;

    static MersenneTwisterFast randomMock;

    static PatchLattice latticeMock;

    @BeforeAll
    public static void setup() {
        seriesMock = mock(PatchSeries.class, CALLS_REAL_METHODS);
        simMock = mock(PatchSimulation.class);
        doReturn(seriesMock).when(simMock).getSeries();
        parametersMock = spy(new Parameters(new MiniBox(), null, null));
        metabolismMock = mock(PatchProcessMetabolism.class);
        signalingMock = mock(PatchProcessSignaling.class);
        gridMock = mock(PatchGrid.class);
        latticeMock = mock(PatchLattice.class);
        doReturn(gridMock).when(simMock).getGrid();
        randomMock = mock(MersenneTwisterFast.class);
        simMock.random = randomMock;
    }

    @Test
    public void schedule_calledWithMigrationRateLowerThanEdgeSize_setsScheduleCorrectly() {
        MiniBox parameters = new MiniBox();
        parameters.put("MIGRATION_RATE", 10);
        parameters.put("VEGF_THRESHOLD", 0.5);
        parameters.put("WALK_TYPE", "BIASED");
        parameters.put("MAX_LENGTH", 100);
        parameters.put("CALCULATION_STRATEGY", "DIVERT");

        Schedule schedule = mock(Schedule.class);

        PatchComponentGrowth component = new PatchComponentGrowth(seriesMock, parameters);

        int ds = 30;

        try {
            Field field = PatchComponentGrowth.class.getDeclaredField("edgeSize");
            field.setAccessible(true);
            field.set(component, ds);
        } catch (Exception ignored) {
        }

        component.schedule(schedule);

        verify(schedule).scheduleRepeating(component, 10, 60);
    }

    @Test
    public void schedule_calledWithMigrationRateGreaterThanEdgeSize_setsScheduleCorrectly() {
        MiniBox parameters = new MiniBox();
        parameters.put("MIGRATION_RATE", 60);
        parameters.put("VEGF_THRESHOLD", 0.5);
        parameters.put("WALK_TYPE", "BIASED");
        parameters.put("MAX_LENGTH", 100);
        parameters.put("CALCULATION_STRATEGY", "DIVERT");

        Schedule schedule = mock(Schedule.class);

        PatchComponentGrowth component = new PatchComponentGrowth(seriesMock, parameters);

        int ds = 30;

        try {
            Field field = PatchComponentGrowth.class.getDeclaredField("edgeSize");
            field.setAccessible(true);
            field.set(component, ds);
        } catch (Exception ignored) {
        }

        component.schedule(schedule);

        verify(schedule).scheduleRepeating(component, 10, 30);
    }

    @Test
    public void register_calledWithIncompatibleFeature_throwsException() {
        MiniBox parameters = new MiniBox();
        parameters.put("MIGRATION_RATE", 60);
        parameters.put("VEGF_THRESHOLD", 0.5);
        parameters.put("WALK_TYPE", "BIASED");
        parameters.put("MAX_LENGTH", 100);
        parameters.put("CALCULATION_STRATEGY", "DIVERT");

        PatchComponentSitesPattern pattern = mock(PatchComponentSitesPattern.class);
        doReturn(pattern).when(simMock).getComponent("INCOMPATIBLE");

        PatchComponentGrowth component = new PatchComponentGrowth(seriesMock, parameters);

        assertThrows(
                IncompatibleFeatureException.class,
                () -> component.register(simMock, "INCOMPATIBLE"));
    }

    @Test
    public void register_calledWithoutVEGFLattice_throwsException() {
        MiniBox parameters = new MiniBox();
        parameters.put("MIGRATION_RATE", 60);
        parameters.put("VEGF_THRESHOLD", 0.5);
        parameters.put("WALK_TYPE", "BIASED");
        parameters.put("MAX_LENGTH", 100);
        parameters.put("CALCULATION_STRATEGY", "DIVERT");

        PatchComponentSitesGraph graph = mock(PatchComponentSitesGraph.class);
        when(simMock.getLattice("VEGF")).thenReturn(null);

        try {
            Field field = PatchComponentSitesGraph.class.getDeclaredField("graphFactory");
            field.setAccessible(true);
            field.set(graph, mock(PatchComponentSitesGraphFactory.class));
        } catch (Exception ignored) {
        }

        doReturn(new EnumMap<EdgeDirection, int[]>(EdgeDirection.class))
                .when(graph.graphFactory)
                .getOffsets();

        PatchComponentGrowth component = new PatchComponentGrowth(seriesMock, parameters);
        assertThrows(
                MissingSpecificationException.class,
                () -> component.register(simMock, "COMPATIBLE"));
    }

    @Test
    public void register_calledWithSitesGraphObject_doesNotThrowException() {
        MiniBox parameters = new MiniBox();
        parameters.put("MIGRATION_RATE", 60);
        parameters.put("VEGF_THRESHOLD", 0.5);
        parameters.put("WALK_TYPE", "BIASED");
        parameters.put("MAX_LENGTH", 100);
        parameters.put("CALCULATION_STRATEGY", "DIVERT");

        PatchComponentSitesGraph graph = mock(PatchComponentSitesGraph.class);
        doReturn(graph).when(simMock).getComponent("COMPATIBLE");
        when(simMock.getLattice("VEGF")).thenReturn(latticeMock);

        try {
            Field field = PatchComponentSitesGraph.class.getDeclaredField("graphFactory");
            field.setAccessible(true);
            field.set(graph, mock(PatchComponentSitesGraphFactory.class));
        } catch (Exception ignored) {
        }

        doReturn(new EnumMap<EdgeDirection, int[]>(EdgeDirection.class))
                .when(graph.graphFactory)
                .getOffsets();
        PatchComponentGrowth component = new PatchComponentGrowth(seriesMock, parameters);
        assertDoesNotThrow(() -> component.register(simMock, "COMPATIBLE"));
    }

    @Test
    public void getDirectionalAverages_givenMap_returnsCorrectMap() {
        EnumMap<EdgeDirection, ArrayList<Double>> map = new EnumMap<>(EdgeDirection.class);
        map.put(EdgeDirection.UP, new ArrayList<>(Arrays.asList(1.0, 2.0, 3.0)));
        map.put(EdgeDirection.DOWN, new ArrayList<>(Arrays.asList(4.0, 5.0, 6.0)));
        map.put(EdgeDirection.LEFT, new ArrayList<>(Arrays.asList(7.0, 8.0, 9.0)));
        map.put(EdgeDirection.RIGHT, new ArrayList<>(Arrays.asList(10.0, 11.0, 12.0)));
        EnumMap<EdgeDirection, Double> averageMap =
                PatchComponentGrowth.getDirectionalAverages(map);
        assertEquals(averageMap.get(EdgeDirection.UP), 2.0);
        assertEquals(averageMap.get(EdgeDirection.DOWN), 5.0);
        assertEquals(averageMap.get(EdgeDirection.LEFT), 8.0);
        assertEquals(averageMap.get(EdgeDirection.RIGHT), 11.0);
    }

    @Test
    public void getMaxKey_givenMap_returnsCorrectDirection() {
        EnumMap<EdgeDirection, Double> map = new EnumMap<>(EdgeDirection.class);
        map.put(EdgeDirection.UP, 1.0);
        map.put(EdgeDirection.DOWN, 2.0);
        map.put(EdgeDirection.LEFT, 3.0);
        map.put(EdgeDirection.RIGHT, 4.0);
        assertEquals(EdgeDirection.RIGHT, PatchComponentGrowth.getMaxKey(map));
    }

    @Test
    public void normalizeDirectionalMap_givenMap_returnsNormalizedMap() {
        EnumMap<EdgeDirection, Double> map = new EnumMap<>(EdgeDirection.class);
        map.put(EdgeDirection.UP, 0.5);
        map.put(EdgeDirection.DOWN, 0.5);
        map.put(EdgeDirection.LEFT, 0.5);
        map.put(EdgeDirection.RIGHT, 0.5);
        EnumMap<EdgeDirection, Double> normalizedMap =
                PatchComponentGrowth.normalizeDirectionalMap(map);
        assertEquals(normalizedMap.get(EdgeDirection.UP), 0.25);
        assertEquals(normalizedMap.get(EdgeDirection.RIGHT), 0.5);
        assertEquals(normalizedMap.get(EdgeDirection.DOWN), 0.75);
        assertEquals(normalizedMap.get(EdgeDirection.LEFT), 1.0);
        assertEquals(normalizedMap.keySet().size(), 4);
        assertNull(normalizedMap.get(EdgeDirection.UNDEFINED));
    }

    @Test
    public void sumMap_givenMap_returnsCorrectSum() {
        EnumMap<EdgeDirection, Double> map = new EnumMap<>(EdgeDirection.class);
        map.put(EdgeDirection.UP, 1.0);
        map.put(EdgeDirection.DOWN, 2.0);
        map.put(EdgeDirection.LEFT, 3.0);
        map.put(EdgeDirection.RIGHT, 4.0);
        assertEquals(10.0, PatchComponentGrowth.sumMap(map));
    }

    @Test
    public void averageDirectionalMap_givenMap_returnsCorrectAverage() {
        EnumMap<EdgeDirection, ArrayList<Double>> map = new EnumMap<>(EdgeDirection.class);
        map.put(EdgeDirection.UP, new ArrayList<>(Arrays.asList(1.0, 2.0, 3.0)));
        map.put(EdgeDirection.DOWN, new ArrayList<>(Arrays.asList(4.0, 5.0, 6.0)));
        map.put(EdgeDirection.LEFT, new ArrayList<>(Arrays.asList(7.0, 8.0, 9.0)));
        map.put(EdgeDirection.RIGHT, new ArrayList<>(Arrays.asList(10.0, 11.0, 12.0)));
        assertEquals(6.5, PatchComponentGrowth.averageDirectionalMap(map));
    }
}
