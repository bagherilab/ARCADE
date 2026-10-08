package arcade.patch.agent.process;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ec.util.MersenneTwisterFast;
import arcade.core.util.Parameters;
import arcade.patch.agent.cell.PatchCell;
import arcade.patch.env.lattice.PatchLattice;
import arcade.patch.env.location.PatchLocation;
import arcade.patch.sim.PatchSimulation;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;

public class PatchProcessSensingSimpleTest {

    static PatchCell cellMock;

    static PatchSimulation simMock;

    static MersenneTwisterFast randomMock;

    static Parameters parametersMock;

    static PatchLocation locationMock;

    static PatchLattice latticeMock;

    @BeforeAll
    public static void setupMocks() {
        cellMock = mock(PatchCell.class);
        simMock = mock(PatchSimulation.class);
        randomMock = mock(MersenneTwisterFast.class);
        parametersMock = mock(Parameters.class);
        locationMock = mock(PatchLocation.class);
        doReturn(parametersMock).when(cellMock).getParameters();
        doReturn(locationMock).when(cellMock).getLocation();
        latticeMock = mock(PatchLattice.class);
        doReturn(latticeMock).when(simMock).getLattice("VEGF");
    }

    @Test
    public void step_calledWithNegativeEnergyCell_updatesLattice() {
        double energy = randomDoubleBetween(-100, -0.1);
        double secretionRate = randomDoubleBetween(1, 100);
        double currentVEGF = randomDoubleBetween(0, 100);
        doReturn(energy).when(cellMock).getEnergy();
        doReturn(secretionRate).when(parametersMock).getDouble("sensing/VEGF_SECRETION_RATE");
        doReturn(currentVEGF).when(latticeMock).getAverageValue(locationMock);

        PatchProcessSensingSimple sensing = new PatchProcessSensingSimple(cellMock);
        sensing.step(randomMock, simMock);

        verify(latticeMock).setValue(locationMock, currentVEGF + secretionRate);
    }

    @Test
    public void step_calledWithPositiveEnergyCell_doesNotUpdateLattice() {
        double energy = randomDoubleBetween(0.1, 100);
        double secretionRate = randomDoubleBetween(1, 100);
        double currentVEGF = randomDoubleBetween(0, 100);
        doReturn(energy).when(cellMock).getEnergy();
        doReturn(secretionRate).when(parametersMock).getDouble("sensing/VEGF_SECRETION_RATE");
        doReturn(currentVEGF).when(latticeMock).getAverageValue(locationMock);

        PatchProcessSensingSimple sensing = new PatchProcessSensingSimple(cellMock);
        sensing.step(randomMock, simMock);

        verify(latticeMock, never()).setValue(locationMock, currentVEGF + secretionRate);
    }
}
