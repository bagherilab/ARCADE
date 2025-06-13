package arcade.patch.env.component;

import java.lang.reflect.Field;
import java.util.EnumMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sim.engine.Schedule;
import ec.util.MersenneTwisterFast;
import arcade.core.util.MiniBox;
import arcade.core.util.Parameters;
import arcade.core.util.exceptions.IncompatibleFeatureException;
import arcade.patch.agent.process.PatchProcessMetabolism;
import arcade.patch.agent.process.PatchProcessSignaling;
import arcade.patch.env.component.PatchComponentSitesGraphFactory.EdgeDirection;
import arcade.patch.env.grid.PatchGrid;
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

    @BeforeAll
    public static void setup() {

        seriesMock = mock(PatchSeries.class, CALLS_REAL_METHODS);
        simMock = mock(PatchSimulation.class);
        doReturn(seriesMock).when(simMock).getSeries();
        parametersMock = spy(new Parameters(new MiniBox(), null, null));
        metabolismMock = mock(PatchProcessMetabolism.class);
        signalingMock = mock(PatchProcessSignaling.class);
        gridMock = mock(PatchGrid.class);
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

        verify(schedule).scheduleRepeating(component, 6, 60);
    }

    @Test
    public void schedule_calledWithMigrationRateGreaterThanEdgeSize_setsScheduleCorrectly() {
        MiniBox parameters = new MiniBox();
        parameters.put("MIGRATION_RATE", 60);
        parameters.put("VEGF_THRESHOLD", 0.5);
        parameters.put("WALK_TYPE", "BIASED");
        parameters.put("MAX_LENGTH", 100);

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

        verify(schedule).scheduleRepeating(component, 6, 30);
    }

    @Test
    public void register_calledWithIncompatibleFeature_throwsException() {
        MiniBox parameters = new MiniBox();
        parameters.put("MIGRATION_RATE", 60);
        parameters.put("VEGF_THRESHOLD", 0.5);
        parameters.put("WALK_TYPE", "BIASED");
        parameters.put("MAX_LENGTH", 100);

        PatchComponentSitesPattern pattern = mock(PatchComponentSitesPattern.class);
        doReturn(pattern).when(simMock).getComponent("INCOMPATIBLE");

        PatchComponentGrowth component = new PatchComponentGrowth(seriesMock, parameters);

        assertThrows(
                IncompatibleFeatureException.class,
                () -> component.register(simMock, "INCOMPATIBLE"));
    }

    @Test
    public void register_calledWithSitesGraphObject_doesNotThrowException() {
        MiniBox parameters = new MiniBox();
        parameters.put("MIGRATION_RATE", 60);
        parameters.put("VEGF_THRESHOLD", 0.5);
        parameters.put("WALK_TYPE", "BIASED");
        parameters.put("MAX_LENGTH", 100);

        PatchComponentSitesGraph graph = mock(PatchComponentSitesGraph.class);
        doReturn(graph).when(simMock).getComponent("COMPATIBLE");

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
}
