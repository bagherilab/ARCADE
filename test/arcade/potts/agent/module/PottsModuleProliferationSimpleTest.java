package arcade.potts.agent.module;

import java.lang.reflect.Field;
import org.junit.BeforeClass;
import org.junit.Test;
import sim.engine.Schedule;
import ec.util.MersenneTwisterFast;
import arcade.core.env.grid.Grid;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.env.loc.PottsLocation;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.TestUtilities.*;
import static arcade.core.util.Enums.Region;
import static arcade.core.util.Enums.State;
import static arcade.potts.agent.module.PottsModuleProliferation.*;
import static arcade.potts.util.PottsEnums.Phase;

public class PottsModuleProliferationSimpleTest {
    private static final double R = 1.0;
    static MersenneTwisterFast random;
    static PottsSimulation simMock;
    static PottsCell cellMock;
    static MiniBox parameters;
    
    @BeforeClass
    public static void setupMocks() {
        random = mock(MersenneTwisterFast.class);
        when(random.nextDouble()).thenReturn(R);
        simMock = mock(PottsSimulation.class);
        cellMock = mock(PottsCell.class);
        
        MiniBox box = mock(MiniBox.class);
        doReturn(0.).when(box).getDouble(anyString());
        doReturn(box).when(cellMock).getParameters();
        
        parameters = new MiniBox();
        parameters.put("proliferation/DURATION_G1", randomDoubleBetween(1, 10));
        parameters.put("proliferation/DURATION_S", randomDoubleBetween(1, 10));
        parameters.put("proliferation/DURATION_G2", randomDoubleBetween(1, 10));
        parameters.put("proliferation/DURATION_M", randomDoubleBetween(1, 10));
        parameters.put("proliferation/DURATION_CHECKPOINT", randomDoubleBetween(1, 10));
        parameters.put("proliferation/BASAL_APOPTOSIS_RATE", randomDoubleBetween(0, 0.5));
    }
    
    @Test
    public void constructor_setsParameters() {
        PottsCell cell = mock(PottsCell.class);
        when(cell.getParameters()).thenReturn(parameters);
        PottsModuleProliferation module = new PottsModuleProliferation.Simple(cell);
        
        assertEquals(parameters.getDouble("proliferation/DURATION_G1"), module.durationG1, EPSILON);
        assertEquals(parameters.getDouble("proliferation/DURATION_S"), module.durationS, EPSILON);
        assertEquals(parameters.getDouble("proliferation/DURATION_G2"), module.durationG2, EPSILON);
        assertEquals(parameters.getDouble("proliferation/DURATION_M"), module.durationM, EPSILON);
        assertEquals(parameters.getDouble("proliferation/DURATION_CHECKPOINT"), module.durationCheckpoint, EPSILON);
        assertEquals(parameters.getDouble("proliferation/BASAL_APOPTOSIS_RATE"), module.basalApoptosisRate, EPSILON);
    }
    
    @Test
    public void constructor_calculatesParameters() {
        PottsCell cell = mock(PottsCell.class);
        when(cell.getParameters()).thenReturn(parameters);
        PottsModuleProliferation module = new PottsModuleProliferation.Simple(cell);
        
        double durationG1 = parameters.getDouble("proliferation/DURATION_G1");
        double durationS = parameters.getDouble("proliferation/DURATION_S");
        double durationG2 = parameters.getDouble("proliferation/DURATION_G2");
        
        assertEquals(-Math.log(0.05) / durationG1, module.rateG1, EPSILON);
        assertEquals(-Math.log(0.01) / durationS, module.rateS, EPSILON);
        assertEquals(-Math.log(0.01) / durationG2, module.rateG2, EPSILON);
    }

    @Test
    public void getPhase_defaultConstructor_returnsValue() {
        PottsModuleProliferation module = new PottsModuleProliferation.Simple(cellMock);
        assertEquals(Phase.PROLIFERATIVE_G1, module.getPhase());
    }
    
    @Test
    public void setPhase_givenValue_setsValue() {
        Phase phase = Phase.values()[(int) (Math.random() * Phase.values().length)];
        PottsModuleProliferation module = new PottsModuleProliferation.Simple(cellMock);
        module.setPhase(phase);
        assertEquals(phase, module.phase);
    }
    
    @Test
    public void step_givenPhaseG1_callsMethod() {
        PottsModuleProliferation module = spy(new PottsModuleProliferation.Simple(cellMock));
        module.phase = Phase.PROLIFERATIVE_G1;
        
        module.step(random, simMock);
        verify(module).stepG1(R);
        verify(module, never()).stepS(R);
        verify(module, never()).stepG2(R);
        verify(module, never()).stepM(R, random, simMock);
    }
    
    @Test
    public void step_givenPhaseS_callsMethod() {
        PottsModuleProliferation module = spy(new PottsModuleProliferation.Simple(cellMock));
        module.phase = Phase.PROLIFERATIVE_S;
        
        module.step(random, simMock);
        verify(module).stepS(R);
        verify(module, never()).stepG1(R);
        verify(module, never()).stepG2(R);
        verify(module, never()).stepM(R, random, simMock);
    }
    
    @Test
    public void step_givenPhaseG2_callsMethod() {
        PottsModuleProliferation module = spy(new PottsModuleProliferation.Simple(cellMock));
        module.phase = Phase.PROLIFERATIVE_G2;
        
        module.step(random, simMock);
        verify(module).stepG2(R);
        verify(module, never()).stepG1(R);
        verify(module, never()).stepS(R);
        verify(module, never()).stepM(R, random, simMock);
    }
    
    @Test
    public void step_givenPhaseM_callsMethod() {
        PottsModuleProliferation module = spy(new PottsModuleProliferation.Simple(cellMock));
        doNothing().when(module).addCell(random, simMock);
        module.phase = Phase.PROLIFERATIVE_M;
        
        module.step(random, simMock);
        verify(module).stepM(R, random, simMock);
        verify(module, never()).stepG1(R);
        verify(module, never()).stepS(R);
        verify(module, never()).stepG2(R);
    }
    
    @Test
    public void step_invalidPhase_doesNothing() {
        PottsModuleProliferation module = spy(new PottsModuleProliferation.Simple(cellMock));
        module.phase = Phase.UNDEFINED;
        
        module.step(random, simMock);
        verify(module, never()).stepM(R, random, simMock);
        verify(module, never()).stepG1(R);
        verify(module, never()).stepS(R);
        verify(module, never()).stepG2(R);
    }
    
    @Test
    public void stepG1_withStateChange_callsMethods() {
        PottsCell cell = mock(PottsCell.class);
        when(cell.getParameters()).thenReturn(parameters);
        
        PottsModuleProliferation module = spy(new PottsModuleProliferation.Simple(cell));
        module.phase = Phase.PROLIFERATIVE_G1;
        module.stepG1(module.basalApoptosisRate - EPSILON);
        
        verify(cell, never()).updateTarget(module.rateG1, 2);
        verify(module, never()).checkpointG1();
        verify(cell).setState(State.APOPTOTIC);
    }
    
    @Test
    public void stepG1_withoutStateChange_updatesCell() {
        for (int i = 0; i < 10; i++) {
            PottsCell cell = mock(PottsCell.class);
            when(cell.getParameters()).thenReturn(parameters);
            
            PottsModuleProliferation module = spy(new PottsModuleProliferation.Simple(cell));
            module.phase = Phase.PROLIFERATIVE_G1;
            module.stepG1(i / 10. + module.basalApoptosisRate);
            
            verify(cell).updateTarget(module.rateG1, 2);
        }
    }
    
    @Test
    public void stepG1_noTransitionPhaseNotArrested_maintainsPhase() {
        PottsCell cell = mock(PottsCell.class);
        when(cell.getParameters()).thenReturn(parameters);
        
        PottsModuleProliferation module = spy(new PottsModuleProliferation.Simple(cell));
        module.phase = Phase.PROLIFERATIVE_G1;
        module.isArrested = false;
        module.stepG1(1.0 / module.durationG1 + EPSILON);
        
        verify(module, never()).checkpointG1();
        assertEquals(Phase.PROLIFERATIVE_G1, module.phase);
    }
    
    @Test
    public void stepG1_withTransitionPhaseNotArrested_updatesPhase() {
        PottsCell cell = mock(PottsCell.class);
        when(cell.getParameters()).thenReturn(parameters);
        
        PottsModuleProliferation module = spy(new PottsModuleProliferation.Simple(cell));
        
        try {
            Field field = PottsModuleProliferation.class.getDeclaredField("basalApoptosisRate");
            field.setAccessible(true);
            field.setDouble(module, 0);
        } catch (Exception ignored) { }
        
        module.phase = Phase.PROLIFERATIVE_G1;
        module.isArrested = false;
        module.stepG1(1.0 / module.durationG1 - EPSILON);
        
        verify(module).checkpointG1();
        assertEquals(Phase.PROLIFERATIVE_S, module.phase);
    }
    
    @Test
    public void stepG1_noTransitionPhaseArrested_maintainsPhase() {
        PottsCell cell = mock(PottsCell.class);
        when(cell.getParameters()).thenReturn(parameters);
        
        PottsModuleProliferation module = spy(new PottsModuleProliferation.Simple(cell));
        
        try {
            Field field = PottsModuleProliferation.class.getDeclaredField("basalApoptosisRate");
            field.setAccessible(true);
            field.setDouble(module, 0);
        } catch (Exception ignored) { }
        
        module.phase = Phase.PROLIFERATIVE_G1;
        module.isArrested = true;
        module.stepG1(1.0 / module.durationCheckpoint + EPSILON);
        
        verify(module, never()).checkpointG1();
        assertEquals(Phase.PROLIFERATIVE_G1, module.phase);
    }
    
    @Test
    public void stepG1_withTransitionPhaseArrested_updatesPhase() {
        PottsCell cell = mock(PottsCell.class);
        when(cell.getParameters()).thenReturn(parameters);
        
        PottsModuleProliferation module = spy(new PottsModuleProliferation.Simple(cell));
        
        try {
            Field field = PottsModuleProliferation.class.getDeclaredField("basalApoptosisRate");
            field.setAccessible(true);
            field.setDouble(module, 0);
        } catch (Exception ignored) { }
        
        module.phase = Phase.PROLIFERATIVE_G1;
        module.isArrested = true;
        
        module.stepG1(1.0 / module.durationCheckpoint - EPSILON);
        verify(module).checkpointG1();
        assertEquals(Phase.PROLIFERATIVE_S, module.phase);
    }
    
    @Test
    public void checkpointG1_checkpointPassed_updatesState() {
        PottsCell cell = mock(PottsCell.class);
        when(cell.getParameters()).thenReturn(parameters);
        
        double volume = Math.random() * 100;
        when(cell.getVolume()).thenReturn((int) (volume * GROWTH_CHECKPOINT_G1) + 1);
        when(cell.getCriticalVolume()).thenReturn(volume);
        
        PottsModuleProliferation module = new PottsModuleProliferation.Simple(cell);
        module.phase = Phase.PROLIFERATIVE_G1;
        module.checkpointG1();
        
        assertEquals(Phase.PROLIFERATIVE_S, module.phase);
        assertFalse(module.isArrested);
    }
    
    @Test
    public void checkpointG1_checkpointNotPassed_updatesState() {
        PottsCell cell = mock(PottsCell.class);
        when(cell.getParameters()).thenReturn(parameters);
        
        double volume = Math.random() * 100;
        when(cell.getVolume()).thenReturn((int) (volume * GROWTH_CHECKPOINT_G1) - 1);
        when(cell.getCriticalVolume()).thenReturn(volume);
        
        PottsModuleProliferation module = new PottsModuleProliferation.Simple(cell);
        module.phase = Phase.PROLIFERATIVE_G1;
        module.checkpointG1();
        
        assertEquals(Phase.PROLIFERATIVE_G1, module.phase);
        assertTrue(module.isArrested);
    }
    
    @Test
    public void stepS_anyTransitionRegions_updatesCell() {
        for (int i = 0; i < 10; i++) {
            PottsCell cell = mock(PottsCell.class);
            when(cell.getParameters()).thenReturn(parameters);
            doReturn(true).when(cell).hasRegions();
            
            PottsModuleProliferation module = spy(new PottsModuleProliferation.Simple(cell));
            module.phase = Phase.PROLIFERATIVE_S;
            module.stepS(i / 10.);
            
            verify(cell).updateTarget(Region.NUCLEUS, module.rateS, 2);
        }
    }
    
    @Test
    public void stepS_noTransitionRegions_maintainsPhase() {
        PottsCell cell = mock(PottsCell.class);
        when(cell.getParameters()).thenReturn(parameters);
        doReturn(true).when(cell).hasRegions();
        
        double volume = Math.random() * 100;
        when(cell.getVolume(Region.NUCLEUS)).thenReturn((int) (volume * GROWTH_CHECKPOINT_S) - 1);
        when(cell.getCriticalVolume(Region.NUCLEUS)).thenReturn(volume);
        
        PottsModuleProliferation module = spy(new PottsModuleProliferation.Simple(cell));
        module.phase = Phase.PROLIFERATIVE_S;
        module.stepS(Math.random());
        
        assertEquals(Phase.PROLIFERATIVE_S, module.getPhase());
    }
    
    @Test
    public void stepS_withTransitionRegions_updatesPhase() {
        PottsCell cell = mock(PottsCell.class);
        when(cell.getParameters()).thenReturn(parameters);
        doReturn(true).when(cell).hasRegions();
        
        double volume = Math.random() * 100;
        when(cell.getVolume(Region.NUCLEUS)).thenReturn((int) (volume * GROWTH_CHECKPOINT_S) + 1);
        when(cell.getCriticalVolume(Region.NUCLEUS)).thenReturn(volume);
        
        PottsModuleProliferation module = spy(new PottsModuleProliferation.Simple(cell));
        module.phase = Phase.PROLIFERATIVE_S;
        module.stepS(Math.random());
        
        assertEquals(Phase.PROLIFERATIVE_G2, module.getPhase());
    }
    
    @Test
    public void stepS_noTransitionNoRegions_maintainsPhase() {
        PottsCell cell = mock(PottsCell.class);
        when(cell.getParameters()).thenReturn(parameters);
        
        PottsModuleProliferation module = spy(new PottsModuleProliferation.Simple(cell));
        module.phase = Phase.PROLIFERATIVE_S;
        module.stepS(1.0 / module.durationS + EPSILON);
        
        assertEquals(Phase.PROLIFERATIVE_S, module.phase);
    }
    
    @Test
    public void stepS_withTransitionNoRegions_updatesPhase() {
        PottsCell cell = mock(PottsCell.class);
        when(cell.getParameters()).thenReturn(parameters);
        
        PottsModuleProliferation module = spy(new PottsModuleProliferation.Simple(cell));
        module.phase = Phase.PROLIFERATIVE_S;
        module.stepS(1.0 / module.durationS - EPSILON);
        
        assertEquals(Phase.PROLIFERATIVE_G2, module.phase);
    }
    
    @Test
    public void stepG2_anyTransition_updatesCell() {
        for (int i = 0; i < 10; i++) {
            PottsCell cell = mock(PottsCell.class);
            when(cell.getParameters()).thenReturn(parameters);
            
            PottsModuleProliferation module = spy(new PottsModuleProliferation.Simple(cell));
            module.phase = Phase.PROLIFERATIVE_G2;
            module.stepG2(i / 10.);
            
            verify(cell).updateTarget(module.rateG2, 2);
        }
    }
    
    @Test
    public void stepG2_noTransitionPhaseNotArrested_maintainsPhase() {
        PottsCell cell = mock(PottsCell.class);
        when(cell.getParameters()).thenReturn(parameters);
        
        PottsModuleProliferation module = spy(new PottsModuleProliferation.Simple(cell));
        module.phase = Phase.PROLIFERATIVE_G2;
        module.isArrested = false;
        module.stepG2(1.0 / module.durationG2 + EPSILON);
        
        verify(module, never()).checkpointG2();
        assertEquals(Phase.PROLIFERATIVE_G2, module.phase);
    }
    
    @Test
    public void stepG2_withTransitionPhaseNotArrested_updatesPhase() {
        PottsCell cell = mock(PottsCell.class);
        when(cell.getParameters()).thenReturn(parameters);
        
        PottsModuleProliferation module = spy(new PottsModuleProliferation.Simple(cell));
        module.phase = Phase.PROLIFERATIVE_G2;
        module.isArrested = false;
        module.stepG2(1.0 / module.durationG2 - EPSILON);
        
        verify(module).checkpointG2();
        assertEquals(Phase.PROLIFERATIVE_M, module.phase);
    }
    
    @Test
    public void stepG2_noTransitionPhaseArrested_maintainsPhase() {
        PottsCell cell = mock(PottsCell.class);
        when(cell.getParameters()).thenReturn(parameters);
        
        PottsModuleProliferation module = spy(new PottsModuleProliferation.Simple(cell));
        module.phase = Phase.PROLIFERATIVE_G2;
        module.isArrested = true;
        module.stepG2(1.0 / module.durationCheckpoint + EPSILON);
        
        verify(module, never()).checkpointG2();
        assertEquals(Phase.PROLIFERATIVE_G2, module.phase);
    }
    
    @Test
    public void stepG2_withTransitionPhaseArrested_updatesPhase() {
        PottsCell cell = mock(PottsCell.class);
        when(cell.getParameters()).thenReturn(parameters);
        
        PottsModuleProliferation module = spy(new PottsModuleProliferation.Simple(cell));
        module.phase = Phase.PROLIFERATIVE_G2;
        module.isArrested = true;
        module.stepG2(1.0 / module.durationCheckpoint - EPSILON);
        
        verify(module).checkpointG2();
        assertEquals(Phase.PROLIFERATIVE_M, module.phase);
    }
    
    @Test
    public void checkpointG2_checkpointPassed_updatesState() {
        PottsCell cell = mock(PottsCell.class);
        when(cell.getParameters()).thenReturn(parameters);
        
        double volume = Math.random() * 100;
        when(cell.getVolume()).thenReturn((int) (volume * GROWTH_CHECKPOINT_G2) + 1);
        when(cell.getCriticalVolume()).thenReturn(volume);
        
        PottsModuleProliferation module = new PottsModuleProliferation.Simple(cell);
        module.phase = Phase.PROLIFERATIVE_G2;
        module.checkpointG2();
        
        assertEquals(Phase.PROLIFERATIVE_M, module.phase);
        assertFalse(module.isArrested);
    }
    
    @Test
    public void checkpointG2_checkpointNotPassed_updatesState() {
        PottsCell cell = mock(PottsCell.class);
        when(cell.getParameters()).thenReturn(parameters);
        
        double volume = Math.random() * 100;
        when(cell.getVolume()).thenReturn((int) (volume * GROWTH_CHECKPOINT_G2) - 1);
        when(cell.getCriticalVolume()).thenReturn(volume);
        
        PottsModuleProliferation module = new PottsModuleProliferation.Simple(cell);
        module.phase = Phase.PROLIFERATIVE_G2;
        module.checkpointG2();
        
        assertEquals(Phase.PROLIFERATIVE_G2, module.phase);
        assertTrue(module.isArrested);
    }
    
    @Test
    public void stepM_noTransition_doesNothing() {
        PottsCell cell = mock(PottsCell.class);
        when(cell.getParameters()).thenReturn(parameters);
        
        PottsModuleProliferation module = spy(new PottsModuleProliferation.Simple(cell));
        doNothing().when(module).addCell(random, simMock);
        module.phase = Phase.PROLIFERATIVE_M;
        module.stepM(1.0 / module.durationM + EPSILON, random, simMock);
        
        verify(module, never()).addCell(random, simMock);
        assertEquals(Phase.PROLIFERATIVE_M, module.phase);
    }
    
    @Test
    public void stepM_withTransition_updatesPhase() {
        PottsCell cell = mock(PottsCell.class);
        when(cell.getParameters()).thenReturn(parameters);
        
        PottsModuleProliferation module = spy(new PottsModuleProliferation.Simple(cell));
        doNothing().when(module).addCell(random, simMock);
        module.phase = Phase.PROLIFERATIVE_M;
        module.stepM(1.0 / module.durationM - EPSILON, random, simMock);
        
        verify(module).addCell(random, simMock);
        assertEquals(Phase.PROLIFERATIVE_G1, module.phase);
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
        
        int id = (int) (Math.random() * 100) + 1;
        doReturn(potts).when(sim).getPotts();
        doReturn(id).when(sim).getID();
        doReturn(grid).when(sim).getGrid();
        doReturn(schedule).when(sim).getSchedule();
        
        potts.ids = new int[][][] { { { } } };
        potts.regions = new int[][][] { { { } } };
        
        PottsLocation newLocation = mock(PottsLocation.class);
        PottsCell newCell = mock(PottsCell.class);
        
        doReturn(newCell).when(cell).make(eq(id), any(State.class), eq(newLocation));
        doReturn(location).when(cell).getLocation();
        doReturn(newLocation).when(location).split(random);
        doNothing().when(cell).reset(any(), any());
        doNothing().when(newCell).reset(any(), any());
        
        PottsModuleProliferation module = new PottsModuleProliferation.Simple(cell);
        module.addCell(random, sim);
        
        verify(cell).reset(potts.ids, potts.regions);
        verify(newCell).reset(potts.ids, potts.regions);
        verify(grid).addObject(id, newCell);
        verify(newCell).schedule(schedule);
    }
}
