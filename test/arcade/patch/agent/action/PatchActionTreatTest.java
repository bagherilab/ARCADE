package arcade.patch.agent.action;

import java.lang.reflect.Field;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sim.engine.Schedule;
import sim.util.Bag;
import ec.util.MersenneTwisterFast;
import arcade.core.env.location.LocationContainer;
import arcade.core.util.MiniBox;
import arcade.patch.agent.cell.PatchCell;
import arcade.patch.agent.cell.PatchCellContainer;
import arcade.patch.agent.cell.PatchCellFactory;
import arcade.patch.env.component.PatchComponentSitesSource;
import arcade.patch.env.grid.PatchGrid;
import arcade.patch.env.location.*;
import arcade.patch.sim.PatchSeries;
import arcade.patch.sim.PatchSimulation;
import arcade.patch.util.PatchEnums;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PatchActionTreatTest {

    private PatchActionTreat action;
    private PatchSimulation sim;
    private PatchGrid gridMock;
    private PatchCell cellMock;
    MiniBox parameters;
    PatchSeries series;

    @BeforeEach
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        parameters = mock(MiniBox.class);
        when(parameters.getInt("TIME_DELAY")).thenReturn(10);
        when(parameters.getInt("DOSE")).thenReturn(10);
        when(parameters.getDouble("RATIO")).thenReturn(0.5);
        when(parameters.getDouble("MAX_DAMAGE_SEED")).thenReturn(0.1);
        when(parameters.getDouble("MIN_RADIUS_SEED")).thenReturn(0.01);
        when(parameters.getDouble("T_CELL_VOL_AVG")).thenReturn(175.0);
        series = mock(PatchSeries.class);
        MiniBox patchMock = mock(MiniBox.class);
        series.patch = patchMock;
        gridMock = mock(PatchGrid.class);
        Schedule mockSchedule = mock(Schedule.class);
        when(patchMock.get("GEOMETRY")).thenReturn("HEX");
        sim = mock(PatchSimulation.class);
        when(sim.getSeries()).thenReturn(series);
        when(sim.getGrid()).thenReturn(gridMock);
        when(sim.getSchedule()).thenReturn(mockSchedule);
        PatchComponentSitesSource sources = mock(PatchComponentSitesSource.class);
        double[][][] damage = new double[20][20][20];
        boolean[][][] sitesLat = new boolean[20][20][20];
        damage[0][0][0] = 0.05;
        sitesLat[0][0][0] = true;
        when(sources.getDamage()).thenReturn(damage);
        when(sources.getSources()).thenReturn(sitesLat);
        when(sim.getComponent("SITES")).thenReturn(sources);
        ArrayList<LocationContainer> locations = new ArrayList<>();
        PatchLocationContainer container = mock(PatchLocationContainer.class);
        PatchLocation loc = mock(PatchLocation.class);
        CoordinateXYZ coord = mock(CoordinateXYZ.class);
        Field x = CoordinateXYZ.class.getDeclaredField("x");
        x.setAccessible(true);
        x.set(coord, 0);
        Field y = CoordinateXYZ.class.getDeclaredField("y");
        y.setAccessible(true);
        y.set(coord, 0);
        Field z = Coordinate.class.getDeclaredField("z");
        z.setAccessible(true);
        z.set(coord, 0);
        CoordinateUVWZ c = mock(CoordinateUVWZ.class);
        z.set(c, 0);
        when(loc.getSubcoordinate()).thenReturn(coord);
        when(loc.getCoordinate()).thenReturn(c);
        when(container.convert(any(), any())).thenReturn(loc);
        locations.add(container);
        when(sim.getLocations()).thenReturn(locations);
        sim.random = mock(MersenneTwisterFast.class);

        PatchCellFactory factoryMock = mock(PatchCellFactory.class);
        PatchCellContainer patchCellContainerMock = mock(PatchCellContainer.class);
        when(factoryMock.createCellForPopulation(any(Integer.class), any(Integer.class)))
                .thenReturn(patchCellContainerMock);
        cellMock = mock(PatchCell.class);
        when(patchCellContainerMock.convert(any(), any(), any())).thenReturn(cellMock);

        Field factory = PatchSimulation.class.getDeclaredField("cellFactory");
        factory.setAccessible(true);
        factory.set(sim, factoryMock);
    }

    final Bag createPatchCellsWithVolumeAndCriticalHeight(int n, double volume, double critHeight) {
        Bag bag = new Bag();
        for (int i = 0; i < n; i++) {
            PatchCell cell = mock(PatchCell.class);
            when(cell.getVolume()).thenReturn(volume);
            when(cell.getHeight()).thenReturn(critHeight);
            bag.add(cell);
        }
        return bag;
    }

    @Test
    public void testSchedule() throws NoSuchFieldException, IllegalAccessException {
        action = new PatchActionTreat(series, parameters);

        ArrayList<MiniBox> populations = new ArrayList<>();
        MiniBox populationMock = mock(MiniBox.class);
        when(populationMock.getInt("CODE")).thenReturn(4);
        when(populationMock.get("CLASS")).thenReturn("cart_cd4");
        populations.add(populationMock);
        Field pops = PatchActionTreat.class.getDeclaredField("populations");
        pops.setAccessible(true);
        pops.set(action, populations);

        Schedule schedule = mock(Schedule.class);
        action.schedule(schedule);
        verify(schedule)
                .scheduleOnce(anyDouble(), eq(PatchEnums.Ordering.ACTIONS.ordinal()), eq(action));
    }

    @Test
    public void testStep() throws NoSuchFieldException, IllegalAccessException {
        action = new PatchActionTreat(series, parameters);

        ArrayList<MiniBox> populations = new ArrayList<>();
        MiniBox populationMock = mock(MiniBox.class);
        when(populationMock.getInt("CODE")).thenReturn(4);
        when(populationMock.get("CLASS")).thenReturn("cart_cd4");
        populations.add(populationMock);
        Field pops = PatchActionTreat.class.getDeclaredField("populations");
        pops.setAccessible(true);
        pops.set(action, populations);

        action.step(sim);
        assertFalse(action.getSiteLocs().isEmpty());
        verify(gridMock, times(action.getSiteLocs().size() - 1)).addObject(any(), any());
        verify(cellMock, times(action.getSiteLocs().size() - 1)).schedule(any());
    }

    @Test
    public void testZeroDose() throws NoSuchFieldException, IllegalAccessException {
        when(parameters.getInt("DOSE")).thenReturn(0);
        action = new PatchActionTreat(series, parameters);

        ArrayList<MiniBox> populations = new ArrayList<>();
        MiniBox populationMock = mock(MiniBox.class);
        when(populationMock.getInt("CODE")).thenReturn(4);
        when(populationMock.get("CLASS")).thenReturn("cart_cd4");
        populations.add(populationMock);
        Field pops = PatchActionTreat.class.getDeclaredField("populations");
        pops.setAccessible(true);
        pops.set(action, populations);

        action.step(sim);
        verify(gridMock, times(0)).addObject(any(), any());
        verify(cellMock, times(0)).schedule(any());
    }

    @Test
    public void testCheckLocationSpace() throws NoSuchFieldException, IllegalAccessException {
        action = new PatchActionTreat(series, parameters);
        ArrayList<MiniBox> populations = new ArrayList<>();
        MiniBox populationMock = mock(MiniBox.class);
        when(populationMock.getInt("CODE")).thenReturn(4);
        when(populationMock.get("CLASS")).thenReturn("cart_cd4");
        populations.add(populationMock);
        Field pops = PatchActionTreat.class.getDeclaredField("populations");
        pops.setAccessible(true);
        pops.set(action, populations);

        PatchLocation locationMock = mock(PatchLocation.class);
        when(locationMock.getArea()).thenReturn(3.0 / 2.0 / Math.sqrt(3.0) * 30 * 30);
        when(locationMock.getVolume()).thenReturn(3.0 / 2.0 / Math.sqrt(3.0) * 30 * 30 * 8.7);
        Bag testBag = createPatchCellsWithVolumeAndCriticalHeight(2, 10, 12.5);
        when(gridMock.getObjectsAtLocation(locationMock)).thenReturn(testBag);
        boolean result = action.checkLocationSpace(locationMock, gridMock);
        assertTrue(result);
    }

    @Test
    public void testMaxConfluency() throws NoSuchFieldException, IllegalAccessException {
        action = new PatchActionTreat(series, parameters);
        Field density = PatchActionTreat.class.getDeclaredField("maxConfluency");
        density.setAccessible(true);
        density.set(action, 1);

        ArrayList<MiniBox> populations = new ArrayList<>();
        MiniBox populationMock = mock(MiniBox.class);
        when(populationMock.getInt("CODE")).thenReturn(4);
        when(populationMock.get("CLASS")).thenReturn("cart_cd4");
        populations.add(populationMock);
        Field pops = PatchActionTreat.class.getDeclaredField("populations");
        pops.setAccessible(true);
        pops.set(action, populations);

        PatchLocation locationMock = mock(PatchLocation.class);
        when(locationMock.getArea()).thenReturn(3.0 / 2.0 / Math.sqrt(3.0) * 30 * 30);
        when(locationMock.getVolume()).thenReturn(3.0 / 2.0 / Math.sqrt(3.0) * 30 * 30 * 8.7);
        Bag testBag = createPatchCellsWithVolumeAndCriticalHeight(2, 10, 12.5);
        when(gridMock.getObjectsAtLocation(locationMock)).thenReturn(testBag);
        boolean result = action.checkLocationSpace(locationMock, gridMock);
        assertFalse(result);
    }
}
