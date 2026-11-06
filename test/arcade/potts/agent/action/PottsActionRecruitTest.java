package arcade.potts.agent.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import sim.engine.Schedule;
import ec.util.MersenneTwisterFast;
import arcade.core.sim.Series;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.env.lattice.PottsLattice;
import arcade.potts.env.location.Voxel;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static arcade.potts.util.PottsEnums.Ordering;

public class PottsActionRecruitTest {
    private static final double EPSILON = 1E-12;

    // Lattice size in x and y [voxels].
    private static final int SIZE = 12;

    // Voxel size [um].
    private static final double DS = 10;

    // Tick length [hours].
    private static final double DT = 1;

    static MiniBox parameters(double rate, String signals, double k, double distance) {
        MiniBox parameters = new MiniBox();
        parameters.put("R_BASE", rate);
        parameters.put("SIGNALS", signals);
        parameters.put("K_RECRUIT", k);
        parameters.put("D_RECRUIT", distance);
        parameters.put("MASK", "SUPPLY");
        return parameters;
    }

    static PottsLattice makeLattice() {
        return new PottsLattice(SIZE, SIZE, 1, DS, DT, new MiniBox());
    }

    // Creates a fake simulation with population MAC (code 1), an empty grid, and the layers.
    static PottsSimulation makeSim(PottsLattice mask, PottsLattice signal) {
        Series series = mock(Series.class);
        series.populations = new HashMap<>();
        MiniBox population = new MiniBox();
        population.put("CODE", 1);
        series.populations.put("MAC", population);

        Potts potts = mock(Potts.class);
        potts.ids = new int[1][SIZE][SIZE];

        PottsSimulation sim = mock(PottsSimulation.class);
        sim.random = new MersenneTwisterFast(3);
        doReturn(series).when(sim).getSeries();
        doReturn(potts).when(sim).getPotts();
        doReturn(mask).when(sim).getLattice("SUPPLY");
        doReturn(signal).when(sim).getLattice("VEGF");
        doReturn(mock(PottsCell.class)).when(sim).insertCell(anyInt(), any());
        return sim;
    }

    // A mask of 1 on column x = 5 only.
    static PottsLattice columnMask() {
        PottsLattice mask = makeLattice();
        for (int j = 0; j < SIZE; j++) {
            mask.getField()[0][5][j] = 1;
        }
        return mask;
    }

    @Test
    public void schedule_called_schedulesRepeatingAtActions() {
        Schedule schedule = mock(Schedule.class);
        PottsActionRecruit action = new PottsActionRecruit(parameters(1, "NONE", 1, 10));
        action.schedule(schedule);
        verify(schedule).scheduleRepeating(action, Ordering.ACTIONS.ordinal(), 1);
    }

    @Test
    public void expected_noSignals_isRateTimesSuppliedAreaTimesTick() {
        PottsLattice mask = columnMask();
        mask.getField()[0][5][0] = 0.5;
        PottsSimulation sim = makeSim(mask, null);
        PottsActionRecruit action = new PottsActionRecruit(parameters(200, "NONE", 1, 10));

        // 11.5 supplied voxels of 1E-4 mm^2 each, for 1 hour.
        assertEquals(200 * 11.5 * DS * DS * 1E-6 * DT, action.expected(sim), EPSILON);
    }

    @Test
    public void expected_withSignal_saturatesAtFullRate() {
        PottsLattice signal = makeLattice();
        PottsActionRecruit action = new PottsActionRecruit(parameters(200, "VEGF", 2, 10));
        double full = 200 * SIZE * DS * DS * 1E-6 * DT;

        signal.setField(0);
        assertEquals(0, action.expected(makeSim(columnMask(), signal)), EPSILON);
        signal.setField(2);
        assertEquals(full / 2, action.expected(makeSim(columnMask(), signal)), EPSILON);
        signal.setField(1E9);
        assertEquals(full, action.expected(makeSim(columnMask(), signal)), full * 1E-6);
    }

    @Test
    public void step_manyTicks_recruitsExpectedNumberOnAverage() {
        PottsSimulation sim = makeSim(columnMask(), null);
        PottsActionRecruit action = new PottsActionRecruit(parameters(2000, "NONE", 1, 10));
        action.register(sim, "MAC");
        double expected = action.expected(sim);

        int ticks = 1000;
        for (int t = 0; t < ticks; t++) {
            action.step(sim);
        }

        // Poisson total over many ticks: mean expected * ticks, sd sqrt of that.
        double mean = expected * ticks;
        assertEquals(mean, action.getRecruited(), 4 * Math.sqrt(mean));
        assertEquals(0, action.getFailures());
    }

    @Test
    public void step_givenSites_insertsOnlyNearSupply() {
        PottsSimulation sim = makeSim(columnMask(), null);
        PottsActionRecruit action = new PottsActionRecruit(parameters(2000, "NONE", 1, 10));
        action.register(sim, "MAC");

        for (int t = 0; t < 50; t++) {
            action.step(sim);
        }

        ArgumentCaptor<ArrayList<Voxel>> captor = ArgumentCaptor.forClass(ArrayList.class);
        verify(sim, atLeastOnce()).insertCell(eq(1), captor.capture());
        for (ArrayList<Voxel> voxels : captor.getAllValues()) {
            assertEquals(1, voxels.size());
            Voxel v = voxels.get(0);
            assertTrue(Math.abs(v.x - 5) <= 1, "x = " + v.x);
            assertTrue(v.y >= 1 && v.y <= SIZE - 2);
        }
    }

    @Test
    public void step_noFreeVoxels_countsFailures() {
        PottsSimulation sim = makeSim(columnMask(), null);
        int[][][] ids = sim.getPotts().ids;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                ids[0][i][j] = 9;
            }
        }
        PottsActionRecruit action = new PottsActionRecruit(parameters(2000, "NONE", 1, 10));
        action.register(sim, "MAC");

        for (int t = 0; t < 20; t++) {
            action.step(sim);
        }

        verify(sim, never()).insertCell(anyInt(), any());
        assertTrue(action.getFailures() > 0);
        assertEquals(0, action.getRecruited());
    }

    @Test
    public void freeVoxelsNear_givenDistance_returnsFreeInteriorVoxelsWithinDistance() {
        int[][][] ids = new int[1][SIZE][SIZE];
        ids[0][5][6] = 3;
        PottsActionRecruit action = new PottsActionRecruit(parameters(1, "NONE", 1, 10));

        List<Voxel> free = action.freeVoxelsNear(ids, new Voxel(5, 5, 0), DS);
        assertEquals(4, free.size());
        assertFalse(free.contains(new Voxel(5, 6, 0)));
        assertTrue(free.contains(new Voxel(5, 5, 0)));

        List<Voxel> edge = action.freeVoxelsNear(ids, new Voxel(0, 0, 0), DS);
        assertTrue(edge.isEmpty());
    }

    @Test
    public void step_missingMask_throws() {
        PottsSimulation sim = makeSim(null, null);
        PottsActionRecruit action = new PottsActionRecruit(parameters(1, "NONE", 1, 10));
        action.register(sim, "MAC");
        Exception e = assertThrows(IllegalStateException.class, () -> action.step(sim));
        assertTrue(e.getMessage().contains("SUPPLY"));
    }

    @Test
    public void step_missingSignal_throws() {
        PottsSimulation sim = makeSim(columnMask(), null);
        PottsActionRecruit action = new PottsActionRecruit(parameters(1, "VEGF", 1, 10));
        action.register(sim, "MAC");
        Exception e = assertThrows(IllegalStateException.class, () -> action.step(sim));
        assertTrue(e.getMessage().contains("VEGF"));
    }

    @Test
    public void step_zeroRate_doesNothingAndNeedsNoLayers() {
        PottsSimulation sim = makeSim(null, null);
        PottsActionRecruit action = new PottsActionRecruit(parameters(0, "VEGF", 1, 10));
        action.register(sim, "MAC");
        assertDoesNotThrow(() -> action.step(sim));
        verify(sim, never()).insertCell(anyInt(), any());
    }

    @Test
    public void constructor_invalidParameters_throws() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PottsActionRecruit(parameters(-1, "NONE", 1, 10)));
        assertThrows(
                IllegalArgumentException.class,
                () -> new PottsActionRecruit(parameters(1, "NONE", 1, -1)));
        assertThrows(
                IllegalArgumentException.class,
                () -> new PottsActionRecruit(parameters(1, "VEGF", 0, 10)));
    }
}
