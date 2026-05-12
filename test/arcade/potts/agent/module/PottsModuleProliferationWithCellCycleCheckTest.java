package arcade.potts.agent.module;

import org.junit.jupiter.api.Test;
import sim.engine.Schedule;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellFactory;
import arcade.core.env.grid.Grid;
import arcade.core.sim.Simulation;
import arcade.core.util.Parameters;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.agent.cell.PottsCellContainer;
import arcade.potts.env.location.PottsLocation;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.potts.util.PottsEnums.Phase;
import static arcade.potts.util.PottsEnums.State;

public class PottsModuleProliferationWithCellCycleCheckTest {
    static MersenneTwisterFast randomMock = new MersenneTwisterFast();

    static PottsSimulation simMock = mock(PottsSimulation.class);

    static PottsCell cellMock = mock(PottsCell.class);

    static class PottsModuleProliferationwithCellCycleCheckMock
            extends PottsModuleProliferationWithCellCycleCheck {
        PottsModuleProliferationwithCellCycleCheckMock(PottsCell cell) {
            super(cell);
        }

        @Override
        void stepG1(MersenneTwisterFast random) {
            setPhase(Phase.PROLIFERATIVE_S);
        }

        @Override
        void stepS(MersenneTwisterFast random) {
            setPhase(Phase.PROLIFERATIVE_G2);
        }

        @Override
        void stepG2(MersenneTwisterFast random) {
            setPhase(Phase.PROLIFERATIVE_M);
        }

        @Override
        void stepM(MersenneTwisterFast random, Simulation sim) {
            setPhase(Phase.PROLIFERATIVE_G1);
        }
    }

    @Test
    public void constructor_initializesFactory() {
        PottsModuleProliferationWithCellCycleCheck module =
                new PottsModuleProliferationwithCellCycleCheckMock(cellMock);
        assertNotNull(module.poissonFactory);
    }

    @Test
    public void getPhase_defaultConstructor_returnsValue() {
        PottsModuleProliferationWithCellCycleCheck module =
                new PottsModuleProliferationwithCellCycleCheckMock(cellMock);
        assertEquals(Phase.PROLIFERATIVE_G1, module.getPhase());
    }

    @Test
    public void setPhase_givenValue_setsValue() {
        Phase phase = Phase.random(randomMock);
        PottsModuleProliferationWithCellCycleCheck module =
                new PottsModuleProliferationwithCellCycleCheckMock(cellMock);
        module.setPhase(phase);
        assertEquals(phase, module.phase);
    }

    @Test
    public void setPhase_givenValue_resetsSteps() {
        Phase phase = Phase.random(randomMock);
        PottsModuleProliferationWithCellCycleCheck module =
                new PottsModuleProliferationwithCellCycleCheckMock(cellMock);
        module.currentSteps = randomIntBetween(1, 10);
        module.setPhase(phase);
        assertEquals(0, module.currentSteps);
    }

    @Test
    public void step_givenPhaseG1_callsMethod() {
        PottsModuleProliferationWithCellCycleCheck module =
                spy(new PottsModuleProliferationwithCellCycleCheckMock(cellMock));
        module.phase = Phase.PROLIFERATIVE_G1;

        module.step(randomMock, simMock);
        verify(module).stepG1(randomMock);
        verify(module, never()).stepS(randomMock);
        verify(module, never()).stepG2(randomMock);
        verify(module, never()).stepM(randomMock, simMock);
    }

    @Test
    public void step_givenPhaseS_callsMethod() {
        PottsModuleProliferationWithCellCycleCheck module =
                spy(new PottsModuleProliferationwithCellCycleCheckMock(cellMock));
        module.phase = Phase.PROLIFERATIVE_S;

        module.step(randomMock, simMock);
        verify(module).stepS(randomMock);
        verify(module, never()).stepG1(randomMock);
        verify(module, never()).stepG2(randomMock);
        verify(module, never()).stepM(randomMock, simMock);
    }

    @Test
    public void step_givenPhaseG2_callsMethod() {
        PottsModuleProliferationWithCellCycleCheck module =
                spy(new PottsModuleProliferationwithCellCycleCheckMock(cellMock));
        module.phase = Phase.PROLIFERATIVE_G2;

        module.step(randomMock, simMock);
        verify(module).stepG2(randomMock);
        verify(module, never()).stepG1(randomMock);
        verify(module, never()).stepS(randomMock);
        verify(module, never()).stepM(randomMock, simMock);
    }

    @Test
    public void step_givenPhaseM_callsMethod() {
        PottsModuleProliferationWithCellCycleCheck module =
                spy(new PottsModuleProliferationwithCellCycleCheckMock(cellMock));
        doNothing().when(module).addCell(randomMock, simMock);
        module.phase = Phase.PROLIFERATIVE_M;

        module.step(randomMock, simMock);
        verify(module).stepM(randomMock, simMock);
        verify(module, never()).stepG1(randomMock);
        verify(module, never()).stepS(randomMock);
        verify(module, never()).stepG2(randomMock);
    }

    @Test
    public void step_invalidPhase_doesNothing() {
        PottsModuleProliferationWithCellCycleCheck module =
                spy(new PottsModuleProliferationwithCellCycleCheckMock(cellMock));
        module.phase = Phase.UNDEFINED;

        module.step(randomMock, simMock);
        verify(module, never()).stepG1(randomMock);
        verify(module, never()).stepS(randomMock);
        verify(module, never()).stepG2(randomMock);
        verify(module, never()).stepM(randomMock, simMock);
    }

    @Test
    public void addCell_called_addsObject() {
        PottsCell cell = mock(PottsCell.class);
        Parameters parameters = mock(Parameters.class);
        doReturn(0.).when(parameters).getDouble(anyString());
        doReturn(parameters).when(cell).getParameters();

        PottsLocation location = mock(PottsLocation.class);
        Potts potts = mock(Potts.class);
        Grid grid = mock(Grid.class);
        PottsSimulation sim = mock(PottsSimulation.class);
        CellFactory cellFactory = mock(CellFactory.class);
        Schedule schedule = mock(Schedule.class);

        int id = randomIntBetween(1, 100);
        doReturn(potts).when(sim).getPotts();
        doReturn(id).when(sim).getID();
        doReturn(grid).when(sim).getGrid();
        doReturn(schedule).when(sim).getSchedule();
        doReturn(cellFactory).when(sim).getCellFactory();

        potts.ids = new int[][][] {{{}}};
        potts.regions = new int[][][] {{{}}};

        PottsLocation newLocation = mock(PottsLocation.class);
        PottsCellContainer newContainer = mock(PottsCellContainer.class);
        PottsCell newCell = mock(PottsCell.class);

        doReturn(newContainer).when(cell).make(eq(id), any(State.class), eq(randomMock));
        doReturn(newCell)
                .when(newContainer)
                .convert(eq(cellFactory), eq(newLocation), eq(randomMock), eq(parameters));
        doReturn(location).when(cell).getLocation();
        doReturn(newLocation).when(location).split(randomMock);
        doNothing().when(cell).reset(any(), any());
        doNothing().when(newCell).reset(any(), any());

        PottsModuleProliferationWithCellCycleCheck module =
                new PottsModuleProliferationwithCellCycleCheckMock(cell);
        module.addCell(randomMock, sim);

        verify(cell).reset(potts.ids, potts.regions);
        verify(newCell).reset(potts.ids, potts.regions);
        verify(grid).addObject(newCell, null);
        verify(potts).register(newCell);
        verify(newCell).schedule(schedule);
    }
}
