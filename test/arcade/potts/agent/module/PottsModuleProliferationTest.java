package arcade.potts.agent.module;

import org.junit.Test;
import sim.engine.Schedule;
import ec.util.MersenneTwisterFast;
import arcade.core.env.grid.Grid;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.env.location.PottsLocation;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.core.util.Enums.State;
import static arcade.potts.util.PottsEnums.Phase;

public class PottsModuleProliferationTest {
    static MersenneTwisterFast randomMock = new MersenneTwisterFast(randomSeed());
    
    static PottsSimulation simMock = mock(PottsSimulation.class);
    
    static PottsCell cellMock = mock(PottsCell.class);
    
    static class PottsModuleProliferationMock extends PottsModuleProliferation {
        PottsModuleProliferationMock(PottsCell cell) { super(cell); }
        
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
        PottsModuleProliferationMock module = new PottsModuleProliferationMock(cellMock);
        assertNotNull(module.poissonFactory);
    }
    
    @Test
    public void getPhase_defaultConstructor_returnsValue() {
        PottsModuleProliferation module = new PottsModuleProliferationMock(cellMock);
        assertEquals(Phase.PROLIFERATIVE_G1, module.getPhase());
    }
    
    @Test
    public void setPhase_givenValue_setsValue() {
        Phase phase = Phase.random(randomMock);
        PottsModuleProliferation module = new PottsModuleProliferationMock(cellMock);
        module.setPhase(phase);
        assertEquals(phase, module.phase);
    }
    
    @Test
    public void setPhase_givenValue_resetsSteps() {
        Phase phase = Phase.random(randomMock);
        PottsModuleProliferation module = new PottsModuleProliferationMock(cellMock);
        module.currentSteps = randomIntBetween(1, 10);
        module.setPhase(phase);
        assertEquals(0, module.currentSteps);
    }
    
    @Test
    public void step_givenPhaseG1_callsMethod() {
        PottsModuleProliferation module = spy(new PottsModuleProliferationMock(cellMock));
        module.phase = Phase.PROLIFERATIVE_G1;
        
        module.step(randomMock, simMock);
        verify(module).stepG1(randomMock);
        verify(module, never()).stepS(randomMock);
        verify(module, never()).stepG2(randomMock);
        verify(module, never()).stepM(randomMock, simMock);
    }
    
    @Test
    public void step_givenPhaseS_callsMethod() {
        PottsModuleProliferation module = spy(new PottsModuleProliferationMock(cellMock));
        module.phase = Phase.PROLIFERATIVE_S;
        
        module.step(randomMock, simMock);
        verify(module).stepS(randomMock);
        verify(module, never()).stepG1(randomMock);
        verify(module, never()).stepG2(randomMock);
        verify(module, never()).stepM(randomMock, simMock);
    }
    
    @Test
    public void step_givenPhaseG2_callsMethod() {
        PottsModuleProliferation module = spy(new PottsModuleProliferationMock(cellMock));
        module.phase = Phase.PROLIFERATIVE_G2;
        
        module.step(randomMock, simMock);
        verify(module).stepG2(randomMock);
        verify(module, never()).stepG1(randomMock);
        verify(module, never()).stepS(randomMock);
        verify(module, never()).stepM(randomMock, simMock);
    }
    
    @Test
    public void step_givenPhaseM_callsMethod() {
        PottsModuleProliferation module = spy(new PottsModuleProliferationMock(cellMock));
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
        PottsModuleProliferation module = spy(new PottsModuleProliferationMock(cellMock));
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
        MiniBox box = mock(MiniBox.class);
        doReturn(0.).when(box).getDouble(anyString());
        doReturn(box).when(cell).getParameters();
        
        PottsLocation location = mock(PottsLocation.class);
        Potts potts = mock(Potts.class);
        Grid grid = mock(Grid.class);
        PottsSimulation sim = mock(PottsSimulation.class);
        Schedule schedule = mock(Schedule.class);
        
        int id = randomIntBetween(1, 100);
        doReturn(potts).when(sim).getPotts();
        doReturn(id).when(sim).getID();
        doReturn(grid).when(sim).getGrid();
        doReturn(schedule).when(sim).getSchedule();
        
        potts.ids = new int[][][] { { { } } };
        potts.regions = new int[][][] { { { } } };
        
        PottsLocation newLocation = mock(PottsLocation.class);
        PottsCell newCell = mock(PottsCell.class);
        
        doReturn(newCell).when(cell).make(eq(id), any(State.class), eq(newLocation), eq(randomMock));
        doReturn(location).when(cell).getLocation();
        doReturn(newLocation).when(location).split(randomMock);
        doNothing().when(cell).reset(any(), any());
        doNothing().when(newCell).reset(any(), any());
        
        PottsModuleProliferation module = new PottsModuleProliferationMock(cell);
        module.addCell(randomMock, sim);
        
        verify(cell).reset(potts.ids, potts.regions);
        verify(newCell).reset(potts.ids, potts.regions);
        verify(grid).addObject(newCell, null);
        verify(potts).register(newCell);
        verify(newCell).schedule(schedule);
    }
}
