package arcade.patch.agent.cell;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalMatchers;
import arcade.core.env.lattice.Lattice;
import arcade.core.util.MiniBox;
import arcade.core.util.Parameters;
import arcade.patch.env.location.PatchLocation;
import arcade.patch.sim.PatchSimulation;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.patch.util.PatchEnums.State;

public class PatchCellCAFTest {
    static final double EPSILON = 1E-8;

    static final double DEPOSITION_RATE = 0.01;

    static final double MAX_DENSITY = 1.0;

    PatchSimulation simMock;

    PatchLocation locationMock;

    Lattice ecmMock;

    PatchCellCAF cell;

    @BeforeEach
    public void setupMocks() {
        simMock = mock(PatchSimulation.class);
        locationMock = mock(PatchLocation.class);
        ecmMock = mock(Lattice.class);

        Parameters parameters = spy(new Parameters(new MiniBox(), null, null));
        doReturn(0.0).when(parameters).getDouble(anyString());
        doReturn(0).when(parameters).getInt(anyString());
        doReturn(DEPOSITION_RATE).when(parameters).getDouble("ECM_DEPOSITION_RATE");
        doReturn(MAX_DENSITY).when(parameters).getDouble("ECM_MAX_DENSITY");

        PatchCellContainer container =
                new PatchCellContainer(
                        randomIntBetween(1, 10),
                        0,
                        randomIntBetween(1, 10),
                        randomIntBetween(1, 1000),
                        0,
                        State.QUIESCENT,
                        randomDoubleBetween(10, 100),
                        randomDoubleBetween(10, 100),
                        randomDoubleBetween(10, 100),
                        randomDoubleBetween(10, 100));

        cell = new PatchCellCAF(container, locationMock, parameters);
    }

    @Test
    public void depositECM_belowMaxDensity_incrementsByRate() {
        doReturn(ecmMock).when(simMock).getLattice("ECM_DENSITY");
        doReturn(0.2).when(ecmMock).getAverageValue(locationMock);

        cell.depositECM(simMock);

        verify(ecmMock).incrementValue(locationMock, DEPOSITION_RATE);
    }

    @Test
    public void depositECM_nearMaxDensity_incrementsOnlyToMax() {
        doReturn(ecmMock).when(simMock).getLattice("ECM_DENSITY");
        double current = MAX_DENSITY - DEPOSITION_RATE / 2;
        doReturn(current).when(ecmMock).getAverageValue(locationMock);

        cell.depositECM(simMock);

        verify(ecmMock)
                .incrementValue(
                        eq(locationMock), AdditionalMatchers.eq(MAX_DENSITY - current, EPSILON));
    }

    @Test
    public void depositECM_atMaxDensity_doesNotIncrement() {
        doReturn(ecmMock).when(simMock).getLattice("ECM_DENSITY");
        doReturn(MAX_DENSITY).when(ecmMock).getAverageValue(locationMock);

        cell.depositECM(simMock);

        verify(ecmMock, never()).incrementValue(any(), anyDouble());
    }

    @Test
    public void depositECM_noECMLayer_doesNothing() {
        doReturn(null).when(simMock).getLattice("ECM_DENSITY");

        assertDoesNotThrow(() -> cell.depositECM(simMock));

        verifyNoInteractions(ecmMock);
    }
}
