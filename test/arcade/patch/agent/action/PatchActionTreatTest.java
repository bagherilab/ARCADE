package arcade.patch.agent.action;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sim.engine.Schedule;
import sim.util.Bag;
import ec.util.MersenneTwisterFast;
import arcade.core.env.location.Location;
import arcade.core.env.location.LocationContainer;
import arcade.core.sim.Series;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.patch.agent.cell.PatchCell;
import arcade.patch.agent.cell.PatchCellContainer;
import arcade.patch.agent.cell.PatchCellFactory;
import arcade.patch.env.component.PatchComponentSitesSource;
import arcade.patch.env.grid.PatchGrid;
import arcade.patch.env.location.CoordinateUVWZ;
import arcade.patch.env.location.CoordinateXYZ;
import arcade.patch.env.location.PatchLocation;
import arcade.patch.env.location.PatchLocationContainer;
import arcade.patch.env.location.PatchLocationHex;
import arcade.patch.sim.PatchSeries;
import arcade.patch.sim.PatchSimulation;
import arcade.patch.util.PatchEnums;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class PatchActionTreatTest {

    private PatchActionTreat action;

    private PatchSimulation sim;

    private PatchGrid gridMock;

    private PatchCell cellMock;

    private PatchLocation locationMock;

    MiniBox parameters;

    PatchSeries series;

    @BeforeEach
    public final void setUp()
            throws NoSuchFieldException,
                    SecurityException,
                    IllegalArgumentException,
                    IllegalAccessException {
        // set up mocks
        parameters = mock(MiniBox.class);
        when(parameters.getInt(anyString())).thenReturn(1);
        when(parameters.getDouble(anyString())).thenReturn(1.0);

        series = mock(PatchSeries.class);
        MiniBox patchMock = mock(MiniBox.class);
        series.patch = patchMock;
        gridMock = mock(PatchGrid.class);
        cellMock = mock(PatchCell.class);
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
        damage[1][0][0] = 0.05;
        sitesLat[1][0][0] = true;
        when(sources.getDamage()).thenReturn(damage);
        when(sources.getSources()).thenReturn(sitesLat);
        when(sim.getComponent("SITES")).thenReturn(sources);
        ArrayList<LocationContainer> locations = new ArrayList<>();
        PatchLocationContainer container = mock(PatchLocationContainer.class);
        PatchLocationContainer container2 = mock(PatchLocationContainer.class);
        PatchLocation loc = mock(PatchLocation.class);
        locationMock = loc;
        PatchLocation loc2 = mock(PatchLocation.class);
        CoordinateXYZ coord = new CoordinateXYZ(0, 0, 0);
        CoordinateUVWZ c = new CoordinateUVWZ(0, 0, 0, 0);
        when(loc.getSubcoordinate()).thenReturn(coord);
        when(loc.getCoordinate()).thenReturn(c);
        Bag mockBag1 = new Bag();
        when(container.convert(any(), any())).thenReturn(loc);
        CoordinateXYZ coord2 = new CoordinateXYZ(0, 0, 1);
        CoordinateUVWZ c2 = new CoordinateUVWZ(0, 0, 0, 1);
        when(loc2.getSubcoordinate()).thenReturn(coord2);
        when(loc2.getCoordinate()).thenReturn(c2);
        when(container2.convert(any(), any())).thenReturn(loc2);
        Bag mockBag2 = new Bag();
        mockBag2.add(cellMock);
        locations.add(container);
        locations.add(container2);
        when(gridMock.getObjectsAtLocation(loc)).thenReturn(mockBag1);
        when(gridMock.getObjectsAtLocation(loc2)).thenReturn(mockBag2);
        when(sim.getLocations()).thenReturn(locations);
        Set<Location> newLocations = new java.util.HashSet<>();
        newLocations.add(new PatchLocationHex(c2));
        when(sim.getAllLocations()).thenReturn(newLocations);
        sim.random = mock(MersenneTwisterFast.class);
        PatchCellFactory factoryMock = mock(PatchCellFactory.class);
        PatchCellContainer patchCellContainerMock = mock(PatchCellContainer.class);
        when(factoryMock.createCellForPopulation(any(Integer.class), any(Integer.class)))
                .thenReturn(patchCellContainerMock);
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

    private static class TestPatchActionTreat extends PatchActionTreat {

        private final ArrayList<MiniBox> testPopulations;

        TestPatchActionTreat(Series series, MiniBox parameters, ArrayList<MiniBox> populations) {
            super(series, parameters);
            this.testPopulations = populations;

            HashMap<String, MiniBox> populationsMap = new HashMap<>();
            for (MiniBox pop : populations) {
                String className = pop.get("CLASS").toString();
                populationsMap.put(className, pop);
            }
            series.populations = populationsMap;
            for (MiniBox pop : populations) {
                Simulation mockSim = mock(Simulation.class);
                when(mockSim.getSeries()).thenReturn(series);
                String className = pop.get("CLASS").toString();
                register(mockSim, className);
            }
        }
    }

    @Test
    public void schedule_called_schedulesOnAction() {
        ArrayList<MiniBox> populations = new ArrayList<>();
        MiniBox populationMock = mock(MiniBox.class);
        when(populationMock.getInt("CODE")).thenReturn(4);
        when(populationMock.get("CLASS")).thenReturn("cart_cd4");
        populations.add(populationMock);

        action = new TestPatchActionTreat(series, parameters, populations);

        Schedule schedule = mock(Schedule.class);
        action.schedule(schedule);

        verify(schedule)
                .scheduleOnce(anyDouble(), eq(PatchEnums.Ordering.ACTIONS.ordinal()), eq(action));
    }

    @Test
    public void step_called_addsObjectsToSim() {
        ArrayList<MiniBox> populations = new ArrayList<>();
        MiniBox populationMock1 = new MiniBox();
        populationMock1.put("CLASS", "cart_cd4");
        populationMock1.put("CODE", 4);
        MiniBox populationMock2 = new MiniBox();
        populationMock2.put("CLASS", "cart_cd8");
        populationMock2.put("CODE", 8);
        populations.add(populationMock1);
        populations.add(populationMock2);

        action = new TestPatchActionTreat(series, parameters, populations);

        action.step(sim);

        verify(gridMock, atLeastOnce()).addObject(any(), any());
        verify(cellMock, atLeastOnce()).schedule(any());
    }

    @Test
    public void step_zeroDose_doesNotAddObjects() {
        when(parameters.getInt("DOSE")).thenReturn(0);

        ArrayList<MiniBox> populations = new ArrayList<>();
        MiniBox populationMock1 = new MiniBox();
        populationMock1.put("CLASS", "cart_cd4");
        populationMock1.put("CODE", 4);
        MiniBox populationMock2 = new MiniBox();
        populationMock2.put("CLASS", "cart_cd8");
        populationMock2.put("CODE", 8);
        populations.add(populationMock1);
        populations.add(populationMock2);

        action = new TestPatchActionTreat(series, parameters, populations);

        action.step(sim);

        verify(gridMock, times(0)).addObject(any(), any());
        verify(cellMock, times(0)).schedule(any());
    }

    @Test
    public void checkLocationSpace_withEmptySpaces_addsObject() {
        when(parameters.getDouble("T_CELL_VOL_AVG")).thenReturn(175.0);

        ArrayList<MiniBox> populations = new ArrayList<>();
        MiniBox populationMock = mock(MiniBox.class);
        when(populationMock.getInt("CODE")).thenReturn(4);
        when(populationMock.get("CLASS")).thenReturn("cart_cd4");
        when(populationMock.getInt("MAX_DENSITY")).thenReturn(54);
        when(populationMock.getDouble("T_CELL_VOL_AVG")).thenReturn(175.0);
        populations.add(populationMock);
        when(locationMock.getArea()).thenReturn(3.0 / 2.0 / Math.sqrt(3.0) * 30 * 30);
        when(locationMock.getVolume()).thenReturn(3.0 / 2.0 / Math.sqrt(3.0) * 30 * 30 * 8.7);
        Bag testBag = createPatchCellsWithVolumeAndCriticalHeight(2, 10, 12.5);
        when(gridMock.getObjectsAtLocation(locationMock)).thenReturn(testBag);

        action = new TestPatchActionTreat(series, parameters, populations);

        action.step(sim);

        verify(gridMock, times(1)).addObject(any(), any());
        verify(cellMock, times(1)).schedule(any());
    }

    @Test
    public void checkLocation_maxConfluency_doesNotAddObject() {
        when(parameters.getDouble("T_CELL_VOL_AVG")).thenReturn(175.0);

        ArrayList<MiniBox> populations = new ArrayList<>();
        MiniBox populationMock = mock(MiniBox.class);
        when(populationMock.getInt("CODE")).thenReturn(4);
        when(populationMock.get("CLASS")).thenReturn("cart_cd4");
        when(populationMock.getInt("MAX_DENSITY")).thenReturn(1);
        populations.add(populationMock);
        when(locationMock.getArea()).thenReturn(3.0 / 2.0 / Math.sqrt(3.0) * 30 * 30);
        when(locationMock.getVolume()).thenReturn(3.0 / 2.0 / Math.sqrt(3.0) * 30 * 30 * 8.7);
        Bag testBag = createPatchCellsWithVolumeAndCriticalHeight(2, 10, 12.5);
        when(gridMock.getObjectsAtLocation(locationMock)).thenReturn(testBag);

        action = new TestPatchActionTreat(series, parameters, populations);

        action.step(sim);

        verify(gridMock, times(0)).addObject(any(), any());
        verify(cellMock, times(0)).schedule(any());
    }
}
