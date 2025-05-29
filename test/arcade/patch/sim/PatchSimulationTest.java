package arcade.patch.sim;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.junit.jupiter.api.Test;
import sim.util.Bag;
import arcade.core.agent.cell.Cell;
import arcade.core.agent.cell.CellContainer;
import arcade.core.env.grid.Grid;
import arcade.core.env.lattice.Lattice;
import arcade.core.env.location.Location;
import arcade.core.env.location.LocationContainer;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;

public class PatchSimulationTest {
    @Test
    public void getCells_called_returnsContainers() {
        PatchSimulation sim = mock(PatchSimulation.class, CALLS_REAL_METHODS);
        Grid mockGrid = mock(Grid.class);

        try {
            Field field = PatchSimulation.class.getDeclaredField("grid");
            field.setAccessible(true);
            field.set(sim, mockGrid);
        } catch (Exception ignored) {
        }

        Bag objects = new Bag();
        ArrayList<CellContainer> cellContainers = new ArrayList<>();
        doReturn(objects).when(mockGrid).getAllObjects();

        int n = randomIntBetween(5, 10);
        for (int i = 0; i < n; i++) {
            Cell cell = mock(Cell.class);
            CellContainer cellContainer = mock(CellContainer.class);
            doReturn(i).when(cell).getID();
            doReturn(cellContainer).when(cell).convert();

            objects.add(cell);
            cellContainers.add(cellContainer);
        }

        assertEquals(cellContainers, sim.getCells());
    }

    @Test
    public void getLocations_called_returnsContainers() {
        PatchSimulation sim = mock(PatchSimulation.class, CALLS_REAL_METHODS);
        Grid mockGrid = mock(Grid.class);

        try {
            Field field = PatchSimulation.class.getDeclaredField("grid");
            field.setAccessible(true);
            field.set(sim, mockGrid);
        } catch (Exception ignored) {
        }

        Bag objects = new Bag();
        ArrayList<LocationContainer> locationContainers = new ArrayList<>();

        doReturn(objects).when(mockGrid).getAllObjects();

        int n = randomIntBetween(5, 10);
        for (int i = 0; i < n; i++) {
            Cell cell = mock(Cell.class);
            Location location = mock(Location.class);
            LocationContainer locationContainer = mock(LocationContainer.class);

            doReturn(i).when(cell).getID();
            doReturn(location).when(cell).getLocation();
            doReturn(locationContainer).when(location).convert(i);

            objects.add(cell);
            locationContainers.add(locationContainer);
        }

        assertEquals(locationContainers, sim.getLocations());
    }

    @Test
    public void getLayers_givenList_returnsContainers() {
        PatchSimulation sim = mock(PatchSimulation.class, CALLS_REAL_METHODS);
        HashMap<String, Lattice> lattices = new HashMap<>();
        Lattice latticeMock = mock(Lattice.class);
        double val = randomDoubleBetween(0, 100);
        when(latticeMock.getAverageValue(any(Location.class))).thenReturn(val);
        lattices.put("A", latticeMock);
        sim.lattices = lattices;

        Location locationMock = mock(Location.class);
        HashSet<Location> locationSet = new HashSet<>();
        locationSet.add(locationMock);
        doReturn(locationSet).when(sim).getAllLocations();

        HashMap<Location, HashMap<String, Double>> layers = new HashMap<>();
        layers.put(locationMock, new HashMap<>());
        layers.get(locationMock).put("A", val);

        assertEquals(layers, sim.getLayers());
    }
}
