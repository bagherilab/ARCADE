package arcade.potts.agent.cell;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellState;
import arcade.core.util.MiniBox;
import arcade.core.util.Parameters;
import arcade.potts.agent.module.PottsModuleProliferationSimple;
import arcade.potts.env.location.PottsLocation;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.potts.util.PottsEnums.Phase;
import static arcade.potts.util.PottsEnums.State;

public class PottsCellFlyNeuronTest {

    static PottsLocation locationMock;

    static Parameters parametersMock;

    static int cellID = randomIntBetween(1, 10);

    static int cellParent = randomIntBetween(1, 10);

    static int cellPop = randomIntBetween(1, 10);

    static int cellAge = randomIntBetween(1, 1000);

    static int cellDivisions = randomIntBetween(1, 100);

    static double cellCriticalVolume = randomDoubleBetween(10, 100);

    static double cellCriticalHeight = randomDoubleBetween(10, 100);

    static State cellState = State.UNDEFINED;

    static Phase cellPhase = Phase.UNDEFINED;

    static PottsCellContainer baseContainer =
            new PottsCellContainer(
                    cellID,
                    cellParent,
                    cellPop,
                    cellAge,
                    cellDivisions,
                    cellState,
                    cellPhase,
                    0,
                    cellCriticalVolume,
                    cellCriticalHeight);

    @BeforeEach
    public final void setupMocks() {
        locationMock = mock(PottsLocation.class);
        parametersMock = spy(new Parameters(new MiniBox(), null, null));
        doReturn(0.0).when(parametersMock).getDouble(any(String.class));
        doReturn(0).when(parametersMock).getInt(any(String.class));
    }

    @Test
    public void constructor_validParameters_createsInstance() {
        PottsCellFlyNeuron neuron =
                new PottsCellFlyNeuron(baseContainer, locationMock, parametersMock);
        assertNotNull(neuron);
    }

    @Test
    public void constructor_invalidApoptosisRate_throwsUnsupportedOperationException() {
        doReturn(1.0).when(parametersMock).getDouble("proliferation/BASAL_APOPTOSIS_RATE");
        assertThrows(
                UnsupportedOperationException.class,
                () -> {
                    new PottsCellFlyNeuron(baseContainer, locationMock, parametersMock);
                });
    }

    @Test
    public void constructor_invalidGrowthRate_throwsUnsupportedOperationException() {
        doReturn(1).when(parametersMock).getInt("proliferation/CELL_GROWTH_RATE");
        assertThrows(
                UnsupportedOperationException.class,
                () -> {
                    new PottsCellFlyNeuron(baseContainer, locationMock, parametersMock);
                });
    }

    @Test
    public void make_called_throwUnsupportedOperationException() {
        PottsCellFlyNeuron neuron =
                new PottsCellFlyNeuron(baseContainer, locationMock, parametersMock);
        assertThrows(
                UnsupportedOperationException.class,
                () -> {
                    neuron.make(cellID + 1, State.PROLIFERATIVE, mock(MersenneTwisterFast.class));
                });
    }

    @Test
    public void setState_proliferative_setsProliferationModule() {
        PottsCellFlyNeuron neuron =
                new PottsCellFlyNeuron(baseContainer, locationMock, parametersMock);
        neuron.setState(State.PROLIFERATIVE);
        assertTrue(neuron.module instanceof PottsModuleProliferationSimple);
    }

    @Test
    public void setState_otherStates_setsNullModule() {
        PottsCellFlyNeuron neuron =
                new PottsCellFlyNeuron(baseContainer, locationMock, parametersMock);
        neuron.setState(State.QUIESCENT);
        assertNull(neuron.getModule());
        neuron.setState(State.NECROTIC);
        assertNull(neuron.getModule());
    }

    @Test
    public void setState_invalidType_throwsIllegalArgumentException() {
        PottsCellFlyNeuron neuron =
                new PottsCellFlyNeuron(baseContainer, locationMock, parametersMock);
        CellState invalidState = new CellState() {};
        assertThrows(IllegalArgumentException.class, () -> neuron.setState(invalidState));
    }
}
