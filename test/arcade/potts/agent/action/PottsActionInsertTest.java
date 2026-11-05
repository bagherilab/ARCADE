package arcade.potts.agent.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import sim.engine.Schedule;
import ec.util.MersenneTwisterFast;
import arcade.core.sim.Series;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.env.location.Voxel;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import arcade.potts.util.PottsEnums.Ordering;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class PottsActionInsertTest {

    static PottsSimulation makeSim() {
        Series series = mock(Series.class);
        series.populations = new HashMap<>();
        MiniBox population = new MiniBox();
        population.put("CODE", 1);
        series.populations.put("MAC", population);

        Potts potts = mock(Potts.class);
        potts.ids = new int[1][5][5];

        PottsSimulation sim = mock(PottsSimulation.class);
        sim.random = new MersenneTwisterFast(1);
        doReturn(series).when(sim).getSeries();
        doReturn(potts).when(sim).getPotts();
        return sim;
    }

    @Test
    public void step_emptyLattice_insertsOnInteriorVoxels() {
        MiniBox parameters = new MiniBox();
        parameters.put("TIME_DELAY", 3);
        parameters.put("INSERT_NUMBER", 2);

        PottsSimulation sim = makeSim();
        doReturn(mock(PottsCell.class)).when(sim).insertCell(anyInt(), any());

        PottsActionInsert action = new PottsActionInsert(parameters);
        action.register(sim, "MAC");
        action.step(sim);

        ArgumentCaptor<ArrayList<Voxel>> captor = ArgumentCaptor.forClass(ArrayList.class);
        verify(sim, times(2)).insertCell(eq(1), captor.capture());
        HashSet<Voxel> seen = new HashSet<>();
        for (ArrayList<Voxel> voxels : captor.getAllValues()) {
            assertEquals(1, voxels.size());
            Voxel v = voxels.get(0);
            assertTrue(v.x >= 1 && v.x <= 3 && v.y >= 1 && v.y <= 3 && v.z == 0);
            seen.add(v);
        }
        assertEquals(2, seen.size());
        assertEquals(0, action.getFailures());
    }

    @Test
    public void schedule_givenDelay_schedulesOnceAtDelay() {
        Schedule schedule = mock(Schedule.class);

        MiniBox parameters = new MiniBox();
        parameters.put("TIME_DELAY", 3);
        parameters.put("INSERT_NUMBER", 2);

        PottsActionInsert action = new PottsActionInsert(parameters);
        action.schedule(schedule);

        verify(schedule).scheduleOnce(3, Ordering.ACTIONS.ordinal(), action);
    }

    @Test
    public void step_insertCellFails_countsFailures() {
        MiniBox parameters = new MiniBox();
        parameters.put("TIME_DELAY", 3);
        parameters.put("INSERT_NUMBER", 2);

        PottsSimulation sim = makeSim();
        doReturn(null).when(sim).insertCell(anyInt(), any());

        PottsActionInsert action = new PottsActionInsert(parameters);
        action.register(sim, "MAC");
        action.step(sim);

        assertEquals(2, action.getFailures());
    }

    @Test
    public void step_fewerEmptyVoxelsThanCells_insertsWhatFitsAndCountsRest() {
        MiniBox parameters = new MiniBox();
        parameters.put("TIME_DELAY", 3);
        parameters.put("INSERT_NUMBER", 2);

        // 3x3 lattice: the edge is border, so (1, 1, 0) is the only interior voxel.
        PottsSimulation sim = makeSim();
        sim.getPotts().ids = new int[1][3][3];
        doReturn(mock(PottsCell.class)).when(sim).insertCell(anyInt(), any());

        PottsActionInsert action = new PottsActionInsert(parameters);
        action.register(sim, "MAC");
        action.step(sim);

        // One cell fits; the second has no voxel left.
        verify(sim, times(1)).insertCell(eq(1), any());
        assertEquals(1, action.getFailures());
    }
}
