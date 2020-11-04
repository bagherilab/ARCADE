package arcade.agent.module;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import sim.engine.Stoppable;
import ec.util.MersenneTwisterFast;
import arcade.agent.cell.PottsCell;
import arcade.sim.Simulation;
import arcade.sim.Potts;
import arcade.env.grid.Grid;
import arcade.env.loc.Location;
import static arcade.agent.cell.Cell.*;
import static arcade.agent.module.ApoptosisModule.*;

public class ApoptosisModuleSimpleTest {
	private static final double EPSILON = 1E-5;
	private static final double r = 1.0;
	static MersenneTwisterFast random;
	static Simulation sim;
	
	@BeforeClass
	public static void setupMocks() {
		random = mock(MersenneTwisterFast.class);
		when(random.nextDouble()).thenReturn(r);
		sim = mock(Simulation.class);
		when(sim.getPotts()).thenReturn(mock(Potts.class));
		when(sim.getAgents()).thenReturn(mock(Grid.class));
	}
	
	@Test
	public void getPhase_defaultConstructor_returnsString() {
		ApoptosisModule module = new ApoptosisModule.Simple(mock(PottsCell.class));
		assertEquals("apoptotic", module.getName());
	}
	
	@Test
	public void getPhase_defaultConstructor_returnsValue() {
		ApoptosisModule module = new ApoptosisModule.Simple(mock(PottsCell.class));
		assertEquals(Phase.APOPTOTIC_EARLY, module.getPhase());
	}
	
	@Test
	public void setPhase_givenValue_setsValue() {
		Phase phase = Phase.values()[(int)(Math.random()*Phase.values().length)];
		ApoptosisModule module = new ApoptosisModule.Simple(mock(PottsCell.class));
		module.setPhase(phase);
		assertEquals(phase, module.phase);
	}
	
	@Test
	public void step_givenPhaseEarly_callsMethod() {
		ApoptosisModule module = spy(new ApoptosisModule.Simple(mock(PottsCell.class)));
		module.phase = Phase.APOPTOTIC_EARLY;
		
		module.step(random, sim);
		verify(module).stepEarly(r);
		verify(module, never()).stepLate(r, sim);
	}
	
	@Test
	public void step_givenPhaseLate_callsMethod() {
		ApoptosisModule module = spy(new ApoptosisModule.Simple(mock(PottsCell.class)));
		module.phase = Phase.APOPTOTIC_LATE;
		
		module.step(random, sim);
		verify(module).stepLate(r, sim);
		verify(module, never()).stepEarly(r);
	}
	
	@Test
	public void step_invalidPhase_doesNothing() {
		ApoptosisModule module = spy(new ApoptosisModule.Simple(mock(PottsCell.class)));
		module.phase = Phase.UNDEFINED;
		
		module.step(random, sim);
		verify(module, never()).stepLate(r, sim);
		verify(module, never()).stepEarly(r);
	}
	
	@Test
	public void stepEarly_anyTransitionTagged_updatesCell() {
		for (int i = 0; i < 10; i++) {
			PottsCell cell = mock(PottsCell.class);
			ApoptosisModule module = spy(new ApoptosisModule.Simple(cell));
			
			cell.hasTags = true;
			module.phase = Phase.APOPTOTIC_EARLY;
			module.stepEarly(i/10.);
			verify(cell).updateTarget(Tag.DEFAULT, RATE_CYTOPLASM_LOSS, 0.5);
			verify(cell).updateTarget(Tag.NUCLEUS, RATE_NUCLEUS_PYKNOSIS, 0.5);
			verify(cell, never()).updateTarget(RATE_CYTOPLASM_LOSS, 0.5);
		}
	}
	
	@Test
	public void stepEarly_anyTransitionUntagged_updatesCell() {
		for (int i = 0; i < 10; i++) {
			PottsCell cell = mock(PottsCell.class);
			ApoptosisModule module = spy(new ApoptosisModule.Simple(cell));
			
			module.phase = Phase.APOPTOTIC_EARLY;
			module.stepEarly(i/10.);
			verify(cell, never()).updateTarget(Tag.DEFAULT, RATE_CYTOPLASM_LOSS, 0.5);
			verify(cell, never()).updateTarget(Tag.NUCLEUS, RATE_NUCLEUS_PYKNOSIS, 0.5);
			verify(cell).updateTarget(RATE_CYTOPLASM_LOSS, 0.5);
		}
	}
	
	@Test
	public void stepEarly_noTransition_maintainsPhase() {
		PottsCell cell = mock(PottsCell.class);
		ApoptosisModule module = spy(new ApoptosisModule.Simple(cell));
		module.phase = Phase.APOPTOTIC_EARLY;
		module.stepEarly(Simulation.DT/DURATION_EARLY + EPSILON);
		assertEquals(Phase.APOPTOTIC_EARLY, module.phase);
	}
	
	@Test
	public void stepEarly_withTransition_updatesPhase() {
		PottsCell cell = mock(PottsCell.class);
		ApoptosisModule module = spy(new ApoptosisModule.Simple(cell));
		module.phase = Phase.APOPTOTIC_EARLY;
		module.stepEarly(Simulation.DT/DURATION_EARLY - EPSILON);
		assertEquals(Phase.APOPTOTIC_LATE, module.phase);
	}
	
	@Test
	public void stepLate_anyTransitionTagged_updatesCell() {
		for (int i = 0; i < 10; i++) {
			PottsCell cell = mock(PottsCell.class);
			Location location = mock(Location.class);
			ApoptosisModule module = spy(new ApoptosisModule.Simple(cell));
			
			doReturn(location).when(cell).getLocation();
			doNothing().when(location).clear(any(), any());
			cell.stopper = mock(Stoppable.class);
			
			cell.hasTags = true;
			module.phase = Phase.APOPTOTIC_LATE;
			module.stepLate(i/10., sim);
			verify(cell).updateTarget(Tag.DEFAULT, RATE_CYTOPLASM_BLEBBING, 0);
			verify(cell).updateTarget(Tag.NUCLEUS, RATE_NUCLEUS_FRAGMENTATION, 0);
			verify(cell, never()).updateTarget(RATE_CYTOPLASM_BLEBBING, 0);
		}
	}
	
	@Test
	public void stepLate_anyTransitionUntagged_updatesCell() {
		for (int i = 0; i < 10; i++) {
			PottsCell cell = mock(PottsCell.class);
			Location location = mock(Location.class);
			ApoptosisModule module = spy(new ApoptosisModule.Simple(cell));
			
			doReturn(location).when(cell).getLocation();
			doNothing().when(location).clear(any(), any());
			cell.stopper = mock(Stoppable.class);
			
			module.phase = Phase.APOPTOTIC_LATE;
			module.stepLate(i/10., sim);
			verify(cell, never()).updateTarget(Tag.DEFAULT, RATE_CYTOPLASM_BLEBBING, 0);
			verify(cell, never()).updateTarget(Tag.NUCLEUS, RATE_NUCLEUS_FRAGMENTATION, 0);
			verify(cell).updateTarget(RATE_CYTOPLASM_BLEBBING, 0);
		}
	}
	
	@Test
	public void stepLate_noTransitionProbability_doesNothing() {
		PottsCell cell = mock(PottsCell.class);
		ApoptosisModule module = spy(new ApoptosisModule.Simple(cell));
		doNothing().when(module).removeCell(sim);
		module.phase = Phase.APOPTOTIC_LATE;
		module.stepLate(Simulation.DT/DURATION_LATE + EPSILON, sim);
		verify(module, never()).removeCell(sim);
		assertEquals(Phase.APOPTOTIC_LATE, module.phase);
	}
	
	@Test
	public void stepLate_withTransitionProbability_updatesPhase() {
		PottsCell cell = mock(PottsCell.class);
		ApoptosisModule module = spy(new ApoptosisModule.Simple(cell));
		doNothing().when(module).removeCell(sim);
		module.phase = Phase.APOPTOTIC_LATE;
		module.stepLate(Simulation.DT/DURATION_LATE - EPSILON, sim);
		verify(module).removeCell(sim);
		assertEquals(Phase.APOPTOSED, module.phase);
	}
	
	@Test
	public void stepLate_noTransitionSize_doesNothing() {
		PottsCell cell = mock(PottsCell.class);
		double volume = Math.random()*100;
		when(cell.getVolume()).thenReturn((int)(volume*APOPTOSIS_CHECKPOINT) + 1);
		when(cell.getCriticalVolume()).thenReturn(volume);
		ApoptosisModule module = spy(new ApoptosisModule.Simple(cell));
		doNothing().when(module).removeCell(sim);
		module.phase = Phase.APOPTOTIC_LATE;
		module.stepLate(1, sim);
		verify(module, never()).removeCell(sim);
		assertEquals(Phase.APOPTOTIC_LATE, module.phase);
	}
	
	@Test
	public void stepLate_withTransitionSize_updatesPhase() {
		PottsCell cell = mock(PottsCell.class);
		double volume = Math.random()*100;
		when(cell.getVolume()).thenReturn((int)(volume*APOPTOSIS_CHECKPOINT) - 1);
		when(cell.getCriticalVolume()).thenReturn(volume);
		ApoptosisModule module = spy(new ApoptosisModule.Simple(cell));
		doNothing().when(module).removeCell(sim);
		module.phase = Phase.APOPTOTIC_LATE;
		module.stepLate(1, sim);
		verify(module).removeCell(sim);
		assertEquals(Phase.APOPTOSED, module.phase);
	}
	
	@Test
	public void removeCell_called_removeObject() {
		Location location = mock(Location.class);
		PottsCell cell = spy(mock(PottsCell.class));
		Potts potts = mock(Potts.class);
		Grid grid = mock(Grid.class);
		Simulation sim = mock(Simulation.class);
		Stoppable stoppable = mock(Stoppable.class);
		
		int id = (int)(Math.random()*100) + 1;
		doReturn(potts).when(sim).getPotts();
		doReturn(id).when(cell).getID();
		doReturn(grid).when(sim).getAgents();
		doReturn(location).when(cell).getLocation();
		
		potts.IDS = new int[][][] { { { } } };
		potts.TAGS = new int[][][] { { { } } };
		cell.stopper = stoppable;
		
		ApoptosisModule module = new ApoptosisModule.Simple(cell);
		
		module.removeCell(sim);
		verify(location).clear(potts.IDS, potts.TAGS);
		verify(grid).removeObject(id);
		verify(stoppable).stop();
	}
}
