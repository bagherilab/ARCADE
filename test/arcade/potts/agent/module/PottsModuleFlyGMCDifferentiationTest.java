package arcade.potts.agent.module;

import java.util.EnumMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import sim.engine.Schedule;
import ec.util.MersenneTwisterFast;
import arcade.core.env.grid.Grid;
import arcade.core.sim.Simulation;
import arcade.core.util.GrabBag;
import arcade.core.util.Parameters;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.agent.cell.PottsCellContainer;
import arcade.potts.agent.cell.PottsCellFactory;
import arcade.potts.agent.cell.PottsCellFlyGMC;
import arcade.potts.agent.cell.PottsCellFlyNeuron;
import arcade.potts.env.location.PottsLocation;
import arcade.potts.env.location.PottsLocation2D;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static arcade.potts.util.PottsEnums.Region;
import static arcade.potts.util.PottsEnums.State;

public class PottsModuleFlyGMCDifferentiationTest {
    private int[][][] dummyIDs;

    private int[][][] dummyRegions;

    private Simulation sim;

    private Potts potts;

    private Grid grid;

    private PottsCellFactory cellFactory;

    private Schedule schedule;

    private PottsCellFlyGMC gmcCell;

    private PottsLocation2D location;

    private PottsLocation newLocation;

    private PottsCellContainer container;

    private PottsCell newCell;

    private GrabBag links;

    private Parameters parameters;

    private MersenneTwisterFast random;

    @BeforeEach
    public final void setupMocks() {
        dummyIDs = new int[1][1][1];
        dummyRegions = new int[0][0][0];

        sim = mock(PottsSimulation.class);
        potts = mock(Potts.class);
        when(((PottsSimulation) sim).getPotts()).thenReturn(potts);
        potts.ids = dummyIDs;
        potts.regions = dummyRegions;

        grid = mock(Grid.class);
        when(sim.getGrid()).thenReturn(grid);
        cellFactory = mock(PottsCellFactory.class);
        when(sim.getCellFactory()).thenReturn(cellFactory);
        schedule = mock(Schedule.class);
        when(sim.getSchedule()).thenReturn(schedule);
        when(sim.getID()).thenReturn(123);

        gmcCell = mock(PottsCellFlyGMC.class);
        location = mock(PottsLocation2D.class);
        when(gmcCell.getLocation()).thenReturn(location);
        newLocation = mock(PottsLocation.class);
        when(location.split(any(MersenneTwisterFast.class))).thenReturn(newLocation);

        links = mock(GrabBag.class);
        when(gmcCell.getLinks()).thenReturn(links);
        int newPop = 2;
        when(links.next(any(MersenneTwisterFast.class))).thenReturn(newPop);

        // Stub getters on the GMC cell for differentiated cell creation
        when(gmcCell.getID()).thenReturn(100);
        int parent = 0;
        when(gmcCell.getParent()).thenReturn(parent);
        when(gmcCell.getAge()).thenReturn(5);
        when(gmcCell.getDivisions()).thenReturn(2);
        when(gmcCell.getCriticalVolume()).thenReturn(1.0);
        when(gmcCell.getCriticalHeight()).thenReturn(2.0);
        EnumMap<Region, Double> critRegionVolumes = new EnumMap<>(Region.class);
        EnumMap<Region, Double> critRegionHeights = new EnumMap<>(Region.class);
        when(gmcCell.getCriticalRegionVolumes()).thenReturn(critRegionVolumes);
        when(gmcCell.getCriticalRegionHeights()).thenReturn(critRegionHeights);

        // Stub parameters
        parameters = mock(Parameters.class);
        when(parameters.getDouble("proliferation/RATE_G1")).thenReturn(1.0);
        when(parameters.getDouble("proliferation/RATE_S")).thenReturn(1.0);
        when(parameters.getDouble("proliferation/RATE_G2")).thenReturn(1.0);
        when(parameters.getDouble("proliferation/RATE_M")).thenReturn(1.0);
        when(parameters.getInt("proliferation/STEPS_G1")).thenReturn(1);
        when(parameters.getInt("proliferation/STEPS_S")).thenReturn(1);
        when(parameters.getInt("proliferation/STEPS_G2")).thenReturn(1);
        when(parameters.getInt("proliferation/STEPS_M")).thenReturn(1);
        when(parameters.getDouble("proliferation/CELL_GROWTH_RATE")).thenReturn(1.0);
        when(parameters.getDouble("proliferation/NUCLEUS_GROWTH_RATE")).thenReturn(1.0);
        when(parameters.getDouble("proliferation/BASAL_APOPTOSIS_RATE")).thenReturn(0.1);
        when(parameters.getDouble("proliferation/NUCLEUS_CONDENSATION_FRACTION")).thenReturn(0.5);
        when(gmcCell.getParameters()).thenReturn(parameters);

        random = mock(MersenneTwisterFast.class);
    }

    @Test
    public void addCell_called_callsExpectedMethods() {
        // Intercept construction of the differentiated cell container
        try (MockedConstruction<PottsCellContainer> mockedConstruction =
                mockConstruction(
                        PottsCellContainer.class,
                        (mockContainer, context) -> {
                            // When convert() is called on this new container, return a
                            // differentiated cell
                            PottsCellFlyNeuron diffCell = mock(PottsCellFlyNeuron.class);
                            when(mockContainer.convert(
                                            eq(cellFactory),
                                            eq(location),
                                            any(MersenneTwisterFast.class)))
                                    .thenReturn(diffCell);
                        })) {

            // When the module calls make() on the cell, return Quiescent PottsCellContainer mock
            container = mock(PottsCellContainer.class);
            when(gmcCell.make(eq(123), eq(State.QUIESCENT), any(MersenneTwisterFast.class)))
                    .thenReturn(container);
            newCell = mock(PottsCell.class);
            when(container.convert(
                            eq(cellFactory), eq(newLocation), any(MersenneTwisterFast.class)))
                    .thenReturn(newCell);

            PottsModuleFlyGMCDifferentiation module = new PottsModuleFlyGMCDifferentiation(gmcCell);
            module.addCell(random, sim);
            verify(location).split(random);
            verify(gmcCell).reset(dummyIDs, dummyRegions);
            verify(gmcCell).make(123, State.QUIESCENT, random);

            verify(grid).addObject(newCell, null);
            verify(potts).register(newCell);
            verify(newCell).reset(dummyIDs, dummyRegions);
            verify(newCell).schedule(schedule);

            verify(grid).removeObject(gmcCell, location);
            verify(gmcCell).stop();

            PottsCellContainer constructed = mockedConstruction.constructed().get(0);
            PottsCellFlyNeuron diffCell =
                    (PottsCellFlyNeuron) constructed.convert(cellFactory, location, random);
            verify(grid).addObject(diffCell, location);
            verify(potts).register(diffCell);
            verify(diffCell).reset(dummyIDs, dummyRegions);
            verify(diffCell).schedule(schedule);
        }
    }
}
