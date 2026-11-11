package arcade.potts.env.component;

import java.util.List;
import org.junit.jupiter.api.Test;
import sim.engine.Schedule;
import arcade.core.env.operation.Operation;
import arcade.core.util.MiniBox;
import arcade.potts.env.lattice.PottsLattice;
import arcade.potts.env.location.Voxel;
import arcade.potts.sim.PottsSimulation;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.potts.util.PottsEnums.Category;
import static arcade.potts.util.PottsEnums.Ordering;

public class PottsComponentReleaseTest {
    private static final double EPSILON = 1E-12;

    private static final double DT = 0.25;

    static MiniBox parameters(double rate, String source) {
        MiniBox parameters = new MiniBox();
        parameters.put("RATE", rate);
        if (source != null) {
            parameters.put("SOURCE", source);
        }
        return parameters;
    }

    // A 3 x 2 x 1 source layer with no operations.
    static PottsLattice makeSource() {
        return new PottsLattice(3, 2, 1, 3.0, DT, new MiniBox());
    }

    // A mock target layer with a diffuser (or none) and the tick length.
    static PottsLattice makeTarget(boolean withDiffuser) {
        PottsLattice target = mock(PottsLattice.class);
        doReturn(DT).when(target).getDt();
        doReturn(withDiffuser ? mock(Operation.class) : null)
                .when(target)
                .getOperation(Category.DIFFUSER);
        return target;
    }

    static PottsSimulation makeSim(PottsLattice source, PottsLattice target) {
        PottsSimulation sim = mock(PottsSimulation.class);
        doReturn(source).when(sim).getLattice("SRC");
        doReturn(target).when(sim).getLattice("SIG");
        return sim;
    }

    @Test
    public void constructor_negativeRate_throws() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PottsComponentRelease(parameters(-1, "SRC")));
    }

    @Test
    public void constructor_noSource_throws() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PottsComponentRelease(parameters(1, null)));
    }

    @Test
    public void schedule_called_schedulesAtComponents() {
        Schedule schedule = mock(Schedule.class);
        PottsComponentRelease release = new PottsComponentRelease(parameters(1, "SRC"));
        release.schedule(schedule);
        verify(schedule).scheduleRepeating(release, Ordering.COMPONENTS.ordinal(), 1);
    }

    @Test
    public void step_zeroRate_addsNothing() {
        PottsLattice source = makeSource();
        source.getField()[0][0][0] = 1.0;
        PottsLattice target = makeTarget(true);
        PottsSimulation sim = makeSim(source, target);
        PottsComponentRelease release = new PottsComponentRelease(parameters(0, "SRC"));
        release.register(sim, "SIG");

        release.step(sim);

        verify(target, never()).addSource(any(), anyDouble());
    }

    @Test
    public void step_givenSource_addsRatePerSecondTimesSource() {
        PottsLattice source = makeSource();
        source.getField()[0][0][0] = 1.0;
        source.getField()[0][2][1] = 0.5;
        PottsLattice target = makeTarget(true);
        PottsSimulation sim = makeSim(source, target);
        PottsComponentRelease release = new PottsComponentRelease(parameters(4, "SRC"));
        release.register(sim, "SIG");

        release.step(sim);

        verify(target).addSource(List.of(new Voxel(0, 0, 0)), 4 / 3600.0 * 1.0);
        verify(target).addSource(List.of(new Voxel(2, 1, 0)), 4 / 3600.0 * 0.5);
        verify(target, times(2)).addSource(any(), anyDouble());
    }

    @Test
    public void step_sourceChanges_followsSourceEachTick() {
        PottsLattice source = makeSource();
        source.getField()[0][1][0] = 1.0;
        PottsLattice target = makeTarget(true);
        PottsSimulation sim = makeSim(source, target);
        PottsComponentRelease release = new PottsComponentRelease(parameters(2, "SRC"));
        release.register(sim, "SIG");

        release.step(sim);
        source.getField()[0][1][0] = 0.25;
        release.step(sim);
        source.getField()[0][1][0] = 0;
        release.step(sim);

        verify(target).addSource(List.of(new Voxel(1, 0, 0)), 2 / 3600.0 * 1.0);
        verify(target).addSource(List.of(new Voxel(1, 0, 0)), 2 / 3600.0 * 0.25);
        verify(target, times(2)).addSource(any(), anyDouble());
    }

    @Test
    public void step_missingSourceLayer_throws() {
        PottsLattice target = makeTarget(true);
        PottsSimulation sim = makeSim(null, target);
        PottsComponentRelease release = new PottsComponentRelease(parameters(1, "SRC"));
        release.register(sim, "SIG");
        assertThrows(IllegalStateException.class, () -> release.step(sim));
    }

    @Test
    public void step_missingTargetLayer_throws() {
        PottsSimulation sim = makeSim(makeSource(), null);
        PottsComponentRelease release = new PottsComponentRelease(parameters(1, "SRC"));
        release.register(sim, "SIG");
        assertThrows(IllegalStateException.class, () -> release.step(sim));
    }

    @Test
    public void step_targetWithoutDiffuser_throws() {
        PottsSimulation sim = makeSim(makeSource(), makeTarget(false));
        PottsComponentRelease release = new PottsComponentRelease(parameters(1, "SRC"));
        release.register(sim, "SIG");
        assertThrows(IllegalStateException.class, () -> release.step(sim));
    }

    @Test
    public void step_realLattice_amountEntersFieldAtLatticeStep() {
        PottsLattice source = makeSource();
        source.getField()[0][0][1] = 2.0;
        MiniBox ops = new MiniBox();
        ops.put("(OPERATION)" + MiniBox.TAG_SEPARATOR + "DIFFUSER", "");
        ops.put("diffuser/DIFFUSIVITY", 0.0);
        ops.put("diffuser/MAX_SUBSTEP", 30.0);
        PottsLattice target = new PottsLattice(3, 2, 1, 3.0, DT, ops);
        PottsSimulation sim = makeSim(source, target);
        PottsComponentRelease release = new PottsComponentRelease(parameters(3, "SRC"));
        release.register(sim, "SIG");

        release.step(sim);
        assertEquals(0, target.getField()[0][0][1], EPSILON);
        target.step(sim);

        assertEquals(3 * DT * 2.0, target.getField()[0][0][1], EPSILON);
        assertEquals(0, target.getField()[0][1][1], EPSILON);
    }
}
