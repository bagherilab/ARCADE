package arcade.patch.agent.cell;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sim.engine.Schedule;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellState;
import arcade.core.env.location.*;
import arcade.core.util.MiniBox;
import arcade.core.util.Parameters;
import arcade.core.util.distributions.Distribution;
import arcade.core.util.distributions.NormalDistribution;
import arcade.patch.env.location.PatchLocation;
import arcade.patch.sim.PatchSimulation;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.patch.util.PatchEnums.State;

public class PatchCellContainerTest {
    static PatchSimulation simMock;

    static PatchLocation locationMock;

    static Parameters parametersMock;

    static MersenneTwisterFast randomMock;

    static int cellID = randomIntBetween(1, 10);

    static int cellParent = randomIntBetween(1, 10);

    static int cellPop = randomIntBetween(1, 10);

    static int cellAge = randomIntBetween(1, 1000);

    static int cellDivisions = randomIntBetween(1, 100);

    static double cellVolume = randomDoubleBetween(10, 100);

    static double cellHeight = randomDoubleBetween(10, 100);

    static double cellCriticalVolume = randomDoubleBetween(10, 100);

    static double cellCriticalHeight = randomDoubleBetween(10, 100);

    static State cellState = State.QUIESCENT;

    static PatchCellContainer baseContainer =
            new PatchCellContainer(
                    cellID,
                    cellParent,
                    cellPop,
                    cellAge,
                    cellDivisions,
                    cellState,
                    cellVolume,
                    cellHeight,
                    cellCriticalVolume,
                    cellCriticalHeight);

    static class PatchCellMock extends PatchCellTissue {
        PatchCellMock(PatchCellContainer container, Location location, Parameters parameters) {
            super(container, location, parameters, null);
        }

        @Override
        public PatchCellContainer make(int newID, CellState newState, MersenneTwisterFast random) {
            return new PatchCellContainer(
                    newID,
                    id,
                    pop,
                    age,
                    divisions,
                    newState,
                    volume,
                    height,
                    criticalVolume,
                    criticalHeight);
        }
    }

    @BeforeAll
    public static void setupMocks() {
        simMock = mock(PatchSimulation.class);
        locationMock = mock(PatchLocation.class);
        parametersMock = spy(new Parameters(new MiniBox(), null, null));
        randomMock = mock(MersenneTwisterFast.class);
    }

    @Test
    public void setState_proliferateWithIC_drawsFromDistribution() {
        Schedule scheduleMock = mock(Schedule.class);
        doReturn(cellID + 1).when(simMock).getID();
        doReturn(scheduleMock).when(simMock).getSchedule();
        doReturn(null).when(scheduleMock).scheduleRepeating(anyInt(), anyInt(), any());
        PatchCellFactory factoryMock = mock(PatchCellFactory.class);
        doReturn(null).when(factoryMock).getLinks(anyInt());
        doReturn(factoryMock).when(simMock).getCellFactory();
        doReturn(0.5).when(randomMock).nextDouble();

        MiniBox population = new MiniBox();
        double volume = 500;
        population.put("CLASS", "tissue");
        population.put("COMPRESSION_TOLERANCE", 0);
        population.put("(DISTRIBUTION)/NECROTIC_FRACTION", "NORMAL");
        population.put("NECROTIC_FRACTION_MU", 0.5);
        population.put("NECROTIC_FRACTION_SIGMA", 0.1);
        population.put("NECROTIC_FRACTION_IC", "MU");
        population.put("proliferation/SYNTHESIS_DURATION", 1);
        population.put("SENESCENT_FRACTION", 0);
        population.put("ENERGY_THRESHOLD", 0);
        population.put("APOPTOSIS_AGE", 0);
        population.put("ACCURACY", 0);
        population.put("AFFINITY", 0);
        population.put("DIVISION_POTENTIAL", 50);
        population.put("MAX_DENSITY", 0);
        population.put("CAR_ANTIGENS", 0);
        population.put("SELF_TARGETS", 0);
        doReturn(population).when(factoryMock).getParameters(anyInt());
        Parameters parameters = new Parameters(population, null, randomMock);
        double critHeight = 10;
        double critVolume = 250;
        PatchCellContainer daughterContainer =
                new PatchCellContainer(
                        cellID + 1,
                        cellParent,
                        cellPop,
                        cellAge,
                        cellDivisions,
                        State.UNDEFINED,
                        volume,
                        cellHeight,
                        critVolume,
                        critHeight);
        PatchCell daughter =
                (PatchCell)
                        daughterContainer.convert(
                                factoryMock, locationMock, randomMock, parameters);
        Distribution dist = daughter.getParameters().getDistribution("NECROTIC_FRACTION");
        assertTrue(dist instanceof NormalDistribution);
    }
}
