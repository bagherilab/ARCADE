package arcade.potts.agent.module;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import ec.util.MersenneTwisterFast;
import arcade.core.env.grid.Grid;
import arcade.core.util.MiniBox;
import arcade.potts.sim.*;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.env.loc.PottsLocation;
import static arcade.core.util.Enums.Region;
import static arcade.potts.util.PottsEnums.Phase;
import static arcade.potts.agent.module.PottsModuleApoptosis.*;
import static arcade.core.TestUtilities.*;

public class PottsModuleApoptosisSimpleTest {
    private static final double R = 1.0;
    static MersenneTwisterFast random;
    static PottsSimulation sim;
    static PottsCell cell;
    static MiniBox parameters;
    
    @BeforeClass
    public static void setupMocks() {
        random = mock(MersenneTwisterFast.class);
        when(random.nextDouble()).thenReturn(R);
        sim = mock(PottsSimulation.class);
        when(sim.getPotts()).thenReturn(mock(Potts.class));
        when(sim.getGrid()).thenReturn(mock(Grid.class));
        cell = mock(PottsCell.class);
        
        MiniBox box = mock(MiniBox.class);
        doReturn(0.).when(box).getDouble(anyString());
        doReturn(box).when(cell).getParameters();
        
        parameters = new MiniBox();
        parameters.put("apoptosis/DURATION_EARLY", randomDoubleBetween(1, 100));
        parameters.put("apoptosis/DURATION_LATE", randomDoubleBetween(1, 100));
    }
    
    @Test
    public void constructor_setsParameters() {
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        PottsModuleApoptosis module = new PottsModuleApoptosis.Simple(cell);
        
        assertEquals(parameters.getDouble("apoptosis/DURATION_EARLY"), module.durationEarly, EPSILON);
        assertEquals(parameters.getDouble("apoptosis/DURATION_LATE"), module.durationLate, EPSILON);
    }
    
    @Test
    public void constructor_calculatesParameters() {
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        PottsModuleApoptosis module = new PottsModuleApoptosis.Simple(cell);
        
        double durationEarly = parameters.getDouble("apoptosis/DURATION_EARLY");
        double durationLate = parameters.getDouble("apoptosis/DURATION_LATE");
        
        assertEquals(-Math.log(0.05) / durationEarly, module.rateCytoplasmLoss, EPSILON);
        assertEquals(-Math.log(0.01) / durationEarly, module.rateNucleusPyknosis, EPSILON);
        assertEquals(-Math.log(0.01) / durationLate, module.rateCytoplasmBlebbing, EPSILON);
        assertEquals(-Math.log(0.01) / durationLate, module.rateNucleusFragmentation, EPSILON);
    }
    
    @Test
    public void getPhase_defaultConstructor_returnsValue() {
        PottsModuleApoptosis module = new PottsModuleApoptosis.Simple(cell);
        assertEquals(Phase.APOPTOTIC_EARLY, module.getPhase());
    }
    
    @Test
    public void setPhase_givenValue_setsValue() {
        Phase phase = Phase.values()[(int) (Math.random() * Phase.values().length)];
        PottsModuleApoptosis module = new PottsModuleApoptosis.Simple(cell);
        module.setPhase(phase);
        assertEquals(phase, module.phase);
    }
    
    @Test
    public void step_givenPhaseEarly_callsMethod() {
        PottsModuleApoptosis module = spy(new PottsModuleApoptosis.Simple(cell));
        module.phase = Phase.APOPTOTIC_EARLY;
        
        module.step(random, sim);
        verify(module).stepEarly(R);
        verify(module, never()).stepLate(R, sim);
    }
    
    @Test
    public void step_givenPhaseLate_callsMethod() {
        PottsModuleApoptosis module = spy(new PottsModuleApoptosis.Simple(cell));
        doNothing().when(module).removeCell(sim);
        module.phase = Phase.APOPTOTIC_LATE;
        
        module.step(random, sim);
        verify(module).stepLate(R, sim);
        verify(module, never()).stepEarly(R);
    }
    
    @Test
    public void step_invalidPhase_doesNothing() {
        PottsModuleApoptosis module = spy(new PottsModuleApoptosis.Simple(cell));
        module.phase = Phase.UNDEFINED;
        
        module.step(random, sim);
        verify(module, never()).stepLate(R, sim);
        verify(module, never()).stepEarly(R);
    }
    
    @Test
    public void stepEarly_anyTransitionRegionsupdatesCell() {
        for (int i = 0; i < 10; i++) {
            PottsCell cell = mock(PottsCell.class);
            doReturn(parameters).when(cell).getParameters();
            doReturn(true).when(cell).hasRegions();
            
            PottsModuleApoptosis module = spy(new PottsModuleApoptosis.Simple(cell));
            module.phase = Phase.APOPTOTIC_EARLY;
            module.stepEarly(i / 10.);
            
            verify(cell).updateTarget(Region.DEFAULT, module.rateCytoplasmLoss, 0.5);
            verify(cell).updateTarget(Region.NUCLEUS, module.rateNucleusPyknosis, 0.5);
            verify(cell, never()).updateTarget(module.rateCytoplasmLoss, 0.5);
        }
    }
    
    @Test
    public void stepEarly_anyTransitionNoRegions_updatesCell() {
        for (int i = 0; i < 10; i++) {
            PottsCell cell = mock(PottsCell.class);
            doReturn(parameters).when(cell).getParameters();
            
            PottsModuleApoptosis module = spy(new PottsModuleApoptosis.Simple(cell));
            module.phase = Phase.APOPTOTIC_EARLY;
            module.stepEarly(i / 10.);
            
            verify(cell, never()).updateTarget(Region.DEFAULT, module.rateCytoplasmLoss, 0.5);
            verify(cell, never()).updateTarget(Region.NUCLEUS, module.rateNucleusPyknosis, 0.5);
            verify(cell).updateTarget(module.rateCytoplasmLoss, 0.5);
        }
    }
    
    @Test
    public void stepEarly_noTransition_maintainsPhase() {
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        
        PottsModuleApoptosis module = spy(new PottsModuleApoptosis.Simple(cell));
        module.phase = Phase.APOPTOTIC_EARLY;
        module.stepEarly(1.0 / module.durationEarly + EPSILON);
        
        assertEquals(Phase.APOPTOTIC_EARLY, module.phase);
    }
    
    @Test
    public void stepEarly_withTransition_updatesPhase() {
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        
        PottsModuleApoptosis module = spy(new PottsModuleApoptosis.Simple(cell));
        module.phase = Phase.APOPTOTIC_EARLY;
        module.stepEarly(1.0 / module.durationEarly - EPSILON);
        
        assertEquals(Phase.APOPTOTIC_LATE, module.phase);
    }
    
    @Test
    public void stepLate_anyTransitionRegions_updatesCell() {
        for (int i = 0; i < 10; i++) {
            PottsCell cell = mock(PottsCell.class);
            doReturn(parameters).when(cell).getParameters();
            doReturn(true).when(cell).hasRegions();
            
            PottsLocation location = mock(PottsLocation.class);
            doReturn(location).when(cell).getLocation();
            doNothing().when(location).clear(any(), any());
            
            PottsModuleApoptosis module = spy(new PottsModuleApoptosis.Simple(cell));
            doNothing().when(module).removeCell(sim);
            module.phase = Phase.APOPTOTIC_LATE;
            module.stepLate(i / 10., sim);
            
            verify(cell).updateTarget(Region.DEFAULT, module.rateCytoplasmBlebbing, 0);
            verify(cell).updateTarget(Region.NUCLEUS, module.rateNucleusFragmentation, 0);
            verify(cell, never()).updateTarget(module.rateCytoplasmBlebbing, 0);
        }
    }
    
    @Test
    public void stepLate_anyTransitionNoRegions_updatesCell() {
        for (int i = 0; i < 10; i++) {
            PottsCell cell = mock(PottsCell.class);
            doReturn(parameters).when(cell).getParameters();
            
            PottsLocation location = mock(PottsLocation.class);
            doReturn(location).when(cell).getLocation();
            doNothing().when(location).clear(any(), any());
            
            PottsModuleApoptosis module = spy(new PottsModuleApoptosis.Simple(cell));
            doNothing().when(module).removeCell(sim);
            module.phase = Phase.APOPTOTIC_LATE;
            module.stepLate(i / 10., sim);
            
            verify(cell, never()).updateTarget(Region.DEFAULT, module.rateCytoplasmBlebbing, 0);
            verify(cell, never()).updateTarget(Region.NUCLEUS, module.rateNucleusFragmentation, 0);
            verify(cell).updateTarget(module.rateCytoplasmBlebbing, 0);
        }
    }
    
    @Test
    public void stepLate_noTransitionProbability_doesNothing() {
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        
        PottsModuleApoptosis module = spy(new PottsModuleApoptosis.Simple(cell));
        doNothing().when(module).removeCell(sim);
        module.phase = Phase.APOPTOTIC_LATE;
        module.stepLate(1.0 / module.durationLate + EPSILON, sim);
        
        verify(module, never()).removeCell(sim);
        assertEquals(Phase.APOPTOTIC_LATE, module.phase);
    }
    
    @Test
    public void stepLate_withTransitionProbability_updatesPhase() {
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        
        PottsModuleApoptosis module = spy(new PottsModuleApoptosis.Simple(cell));
        doNothing().when(module).removeCell(sim);
        module.phase = Phase.APOPTOTIC_LATE;
        module.stepLate(1.0 / module.durationLate - EPSILON, sim);
        
        verify(module).removeCell(sim);
        assertEquals(Phase.APOPTOSED, module.phase);
    }
    
    @Test
    public void stepLate_noTransitionSize_doesNothing() {
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        
        double volume = Math.random() * 100;
        when(cell.getVolume()).thenReturn((int) (volume * APOPTOSIS_CHECKPOINT) + 1);
        when(cell.getCriticalVolume()).thenReturn(volume);
        
        PottsModuleApoptosis module = spy(new PottsModuleApoptosis.Simple(cell));
        doNothing().when(module).removeCell(sim);
        module.phase = Phase.APOPTOTIC_LATE;
        module.stepLate(1, sim);
        
        verify(module, never()).removeCell(sim);
        assertEquals(Phase.APOPTOTIC_LATE, module.phase);
    }
    
    @Test
    public void stepLate_withTransitionSize_updatesPhase() {
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        
        double volume = Math.random() * 100;
        when(cell.getVolume()).thenReturn((int) (volume * APOPTOSIS_CHECKPOINT) - 1);
        when(cell.getCriticalVolume()).thenReturn(volume);
        
        PottsModuleApoptosis module = spy(new PottsModuleApoptosis.Simple(cell));
        doNothing().when(module).removeCell(sim);
        module.phase = Phase.APOPTOTIC_LATE;
        module.stepLate(1, sim);
        
        verify(module).removeCell(sim);
        assertEquals(Phase.APOPTOSED, module.phase);
    }
    
    @Test
    public void removeCell_called_removeObject() {
        PottsLocation location = mock(PottsLocation.class);
        Potts potts = mock(Potts.class);
        Grid grid = mock(Grid.class);
        PottsSimulation sim = mock(PottsSimulation.class);
        
        int id = (int) (Math.random() * 100) + 1;
        doReturn(potts).when(sim).getPotts();
        doReturn(id).when(cell).getID();
        doReturn(grid).when(sim).getGrid();
        doReturn(location).when(cell).getLocation();
        
        potts.ids = new int[][][] { { { } } };
        potts.regions = new int[][][] { { { } } };
        
        PottsModuleApoptosis module = new PottsModuleApoptosis.Simple(cell);
        module.removeCell(sim);
        
        verify(location).clear(potts.ids, potts.regions);
        verify(grid).removeObject(id);
        verify(cell).stop();
    }
}
