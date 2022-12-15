package arcade.potts.agent.module;

import org.junit.Test;
import ec.util.MersenneTwisterFast;
import arcade.core.env.grid.Grid;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.env.loc.PottsLocation;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.potts.util.PottsEnums.Phase;

public class PottsModuleApoptosisTest {
    static MersenneTwisterFast randomMock = new MersenneTwisterFast(randomSeed());
    
    static PottsSimulation simMock;
    
    static PottsCell cellMock;
    
    static class PottsModuleApoptosisMock extends PottsModuleApoptosis {
        PottsModuleApoptosisMock(PottsCell cell) { super(cell); }
        
        @Override
        void stepEarly(MersenneTwisterFast random) {
            setPhase(Phase.APOPTOTIC_LATE);
        }
        
        @Override
        void stepLate(MersenneTwisterFast random, Simulation sim) {
            setPhase(Phase.APOPTOSED);
        }
    }
    
    @Test
    public void constructor_initializesFactory() {
        PottsModuleApoptosis module = new PottsModuleApoptosisMock(cellMock);
        assertNotNull(module.poissonFactory);
    }
    
    @Test
    public void getPhase_defaultConstructor_returnsValue() {
        PottsModuleApoptosis module = new PottsModuleApoptosisMock(cellMock);
        assertEquals(Phase.APOPTOTIC_EARLY, module.getPhase());
    }
    
    @Test
    public void setPhase_givenValue_setsValue() {
        Phase phase = Phase.random(randomMock);
        PottsModuleApoptosis module = new PottsModuleApoptosisMock(cellMock);
        module.setPhase(phase);
        assertEquals(phase, module.phase);
    }
    
    @Test
    public void setPhase_givenValue_resetsSteps() {
        Phase phase = Phase.random(randomMock);
        PottsModuleApoptosis module = new PottsModuleApoptosisMock(cellMock);
        module.currentSteps = randomIntBetween(1, 10);
        module.setPhase(phase);
        assertEquals(0, module.currentSteps);
    }
    
    @Test
    public void step_givenPhaseEarly_callsMethod() {
        PottsModuleApoptosis module = spy(new PottsModuleApoptosisMock(cellMock));
        module.phase = Phase.APOPTOTIC_EARLY;
        
        module.step(randomMock, simMock);
        verify(module).stepEarly(randomMock);
        verify(module, never()).stepLate(randomMock, simMock);
    }
    
    @Test
    public void step_givenPhaseLate_callsMethod() {
        PottsModuleApoptosis module = spy(new PottsModuleApoptosisMock(cellMock));
        doNothing().when(module).removeCell(simMock);
        module.phase = Phase.APOPTOTIC_LATE;
        
        module.step(randomMock, simMock);
        verify(module).stepLate(randomMock, simMock);
        verify(module, never()).stepEarly(randomMock);
    }
    
    @Test
    public void step_invalidPhase_doesNothing() {
        PottsModuleApoptosis module = spy(new PottsModuleApoptosisMock(cellMock));
        module.phase = Phase.UNDEFINED;
        
        module.step(randomMock, simMock);
        verify(module, never()).stepLate(randomMock, simMock);
        verify(module, never()).stepEarly(randomMock);
    }
    
    @Test
    public void removeCell_called_removeObject() {
        PottsCell cell = mock(PottsCell.class);
        MiniBox box = mock(MiniBox.class);
        doReturn(0.).when(box).getDouble(anyString());
        doReturn(box).when(cell).getParameters();
        
        PottsLocation location = mock(PottsLocation.class);
        Potts potts = mock(Potts.class);
        Grid grid = mock(Grid.class);
        PottsSimulation sim = mock(PottsSimulation.class);
        
        int id = randomIntBetween(1, 100);
        doReturn(potts).when(sim).getPotts();
        doReturn(id).when(cell).getID();
        doReturn(grid).when(sim).getGrid();
        doReturn(location).when(cell).getLocation();
        
        potts.ids = new int[][][] { { { } } };
        potts.regions = new int[][][] { { { } } };
        
        PottsModuleApoptosis module = new PottsModuleApoptosisMock(cell);
        module.removeCell(sim);
        
        verify(location).clear(potts.ids, potts.regions);
        verify(grid).removeObject(cell, null);
        verify(potts).deregister(cell);
        verify(cell).stop();
    }
}
