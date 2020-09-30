package arcade.agent.module;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import sim.engine.Schedule;
import ec.util.MersenneTwisterFast;
import arcade.agent.cell.PottsCell;
import arcade.sim.Simulation;
import arcade.sim.Potts;
import arcade.env.grid.Grid;
import arcade.env.loc.Location;
import static arcade.agent.cell.Cell.*;
import static arcade.agent.module.ProliferationModule.*;
import static arcade.agent.cell.PottsCellTest.PottsCellMock;

public class ProliferationModuleSimpleTest {
	private static final double EPSILON = 1E-5;
	private static final double r = 1.0;
	static MersenneTwisterFast random;
	static Simulation sim;
	
	@BeforeClass
	public static void setupMocks() {
		random = mock(MersenneTwisterFast.class);
		when(random.nextDouble()).thenReturn(r);
		sim = mock(Simulation.class);
	}
	
	@Test
	public void getPhase_defaultConstructor_returnsValue() {
		ProliferationModule module = new ProliferationModule.Simple(mock(PottsCell.class));
		assertEquals(PHASE_G1, module.getPhase());
	}
	
	@Test
	public void step_givenPhaseG1_callsMethod() {
		ProliferationModule module = spy(new ProliferationModule.Simple(mock(PottsCell.class)));
		module.phase = PHASE_G1;
		
		module.step(random, sim);
		verify(module).stepG1(r);
		verify(module, never()).stepS(r);
		verify(module, never()).stepG2(r);
		verify(module, never()).stepM(r, random, sim);
	}
	
	@Test
	public void step_givenPhaseS_callsMethod() {
		ProliferationModule module = spy(new ProliferationModule.Simple(mock(PottsCell.class)));
		module.phase = PHASE_S;
		
		module.step(random, sim);
		verify(module).stepS(r);
		verify(module, never()).stepG1(r);
		verify(module, never()).stepG2(r);
		verify(module, never()).stepM(r, random, sim);
	}
	
	@Test
	public void step_givenPhaseG2_callsMethod() {
		ProliferationModule module = spy(new ProliferationModule.Simple(mock(PottsCell.class)));
		module.phase = PHASE_G2;
		
		module.step(random, sim);
		verify(module).stepG2(r);
		verify(module, never()).stepG1(r);
		verify(module, never()).stepS(r);
		verify(module, never()).stepM(r, random, sim);
	}
	
	@Test
	public void step_givenPhaseM_callsMethod() {
		ProliferationModule module = spy(new ProliferationModule.Simple(mock(PottsCell.class)));
		module.phase = PHASE_M;
		
		module.step(random, sim);
		verify(module).stepM(r, random, sim);
		verify(module, never()).stepG1(r);
		verify(module, never()).stepS(r);
		verify(module, never()).stepG2(r);
	}
	
	@Test
	public void stepG1_withStateChange_callsMethods() {
		PottsCell cell = spy(mock(PottsCell.class));
		ProliferationModule module = spy(new ProliferationModule.Simple(cell));
		
		module.phase = PHASE_G1;
		module.stepG1(BASAL_APOPTOSIS_RATE*Simulation.DT - EPSILON);
		
		verify(cell, never()).updateTarget(RATE_G1, 2);
		verify(module, never()).checkpointG1();
		verify(cell).setState(STATE_APOPTOTIC);
	}
	
	@Test
	public void stepG1_withoutStateChange_updatesCell() {
		for (int i = 0; i < 10; i++) {
			PottsCell cell = mock(PottsCell.class);
			ProliferationModule module = spy(new ProliferationModule.Simple(cell));
			
			module.phase = PHASE_G1;
			module.stepG1(i/10. + BASAL_APOPTOSIS_RATE*Simulation.DT);
			verify(cell).updateTarget(RATE_G1, 2);
		}
	}
	
	@Test
	public void stepG1_noTransitionPhaseNotArrested_maintainsPhase() {
		PottsCell cell = mock(PottsCell.class);
		ProliferationModule module = spy(new ProliferationModule.Simple(cell));
		module.phase = PHASE_G1;
		module.isArrested = false;
		module.stepG1(Simulation.DT/DURATION_G1 + EPSILON);
		verify(module, never()).checkpointG1();
		assertEquals(PHASE_G1, module.phase);
	}
	
	@Test
	public void stepG1_withTransitionPhaseNotArrested_updatesPhase() {
		PottsCell cell = mock(PottsCell.class);
		ProliferationModule module = spy(new ProliferationModule.Simple(cell));
		module.phase = PHASE_G1;
		module.isArrested = false;
		module.stepG1(Simulation.DT/DURATION_G1 - EPSILON);
		verify(module).checkpointG1();
		assertEquals(PHASE_S, module.phase);
	}
	
	@Test
	public void stepG1_noTransitionPhaseArrested_maintainsPhase() {
		PottsCell cell = mock(PottsCell.class);
		ProliferationModule module = spy(new ProliferationModule.Simple(cell));
		module.phase = PHASE_G1;
		module.isArrested = true;
		module.stepG1(Simulation.DT/DURATION_CHECKPOINT + EPSILON);
		verify(module, never()).checkpointG1();
		assertEquals(PHASE_G1, module.phase);
	}
	
	@Test
	public void stepG1_withTransitionPhaseArrested_updatesPhase() {
		PottsCell cell = mock(PottsCell.class);
		ProliferationModule module = spy(new ProliferationModule.Simple(cell));
		module.phase = PHASE_G1;
		module.isArrested = true;
		module.stepG1(Simulation.DT/DURATION_CHECKPOINT - EPSILON);
		verify(module).checkpointG1();
		assertEquals(PHASE_S, module.phase);
	}
	
	@Test
	public void checkpointG1_checkpointPassed_updatesState() {
		PottsCellMock cell = mock(PottsCellMock.class);
		double volume = Math.random()*100;
		when(cell.getVolume()).thenReturn((int)(volume*GROWTH_CHECKPOINT_G1) + 1);
		when(cell.getCriticalVolume()).thenReturn(volume);
		ProliferationModule module = new ProliferationModule.Simple(cell);
		module.phase = PHASE_G1;
		
		module.checkpointG1();
		assertEquals(PHASE_S, module.phase);
		assertFalse(module.isArrested);
	}
	
	@Test
	public void checkpointG1_checkpointNotPassed_updatesState() {
		PottsCellMock cell = mock(PottsCellMock.class);
		double volume = Math.random()*100;
		when(cell.getVolume()).thenReturn((int)(volume*GROWTH_CHECKPOINT_G1) - 1);
		when(cell.getCriticalVolume()).thenReturn(volume);
		ProliferationModule module = new ProliferationModule.Simple(cell);
		module.phase = PHASE_G1;
		
		module.checkpointG1();
		assertEquals(PHASE_G1, module.phase);
		assertTrue(module.isArrested);
	}
	
	@Test
	public void stepS_anyTransitionTagged_updatesCell() {
		for (int i = 0; i < 10; i++) {
			PottsCell cell = mock(PottsCell.class);
			ProliferationModule module = spy(new ProliferationModule.Simple(cell));
			
			cell.tags = 2;
			module.phase = PHASE_S;
			module.stepS(i/10.);
			verify(cell).updateTarget(TAG_NUCLEUS, RATE_S, 2);
		}
	}
	
	@Test
	public void stepS_noTransitionTagged_maintainsPhase() {
		PottsCellMock cell = mock(PottsCellMock.class);
		double volume = Math.random()*100;
		when(cell.getVolume(TAG_NUCLEUS)).thenReturn((int)(volume*GROWTH_CHECKPOINT_S) - 1);
		when(cell.getCriticalVolume(TAG_NUCLEUS)).thenReturn(volume);
		cell.tags = 2;
		
		ProliferationModule module = spy(new ProliferationModule.Simple(cell));
		module.phase = PHASE_S;
		module.stepS(Math.random());
		assertEquals(PHASE_S, module.getPhase());
	}
	
	@Test
	public void stepS_withTransitionTagged_updatesPhase() {
		PottsCellMock cell = mock(PottsCellMock.class);
		double volume = Math.random()*100;
		when(cell.getVolume(TAG_NUCLEUS)).thenReturn((int)(volume*GROWTH_CHECKPOINT_S) + 1);
		when(cell.getCriticalVolume(TAG_NUCLEUS)).thenReturn(volume);
		cell.tags = 2;
		
		ProliferationModule module = spy(new ProliferationModule.Simple(cell));
		module.phase = PHASE_S;
		module.stepS(Math.random());
		assertEquals(PHASE_G2, module.getPhase());
	}
	
	@Test
	public void stepS_noTransitionUntagged_maintainsPhase() {
		PottsCellMock cell = mock(PottsCellMock.class);
		ProliferationModule module = spy(new ProliferationModule.Simple(cell));
		module.phase = PHASE_S;
		module.stepS(Simulation.DT/DURATION_S + EPSILON);
		assertEquals(PHASE_S, module.phase);
	}
	
	@Test
	public void stepS_withTransitionUntagged_updatesPhase() {
		PottsCellMock cell = mock(PottsCellMock.class);
		ProliferationModule module = spy(new ProliferationModule.Simple(cell));
		module.phase = PHASE_S;
		module.stepS(Simulation.DT/DURATION_S - EPSILON);
		assertEquals(PHASE_G2, module.phase);
	}
	
	@Test
	public void stepG2_anyTransition_updatesCell() {
		for (int i = 0; i < 10; i++) {
			PottsCell cell = mock(PottsCell.class);
			ProliferationModule module = spy(new ProliferationModule.Simple(cell));
			
			module.phase = PHASE_G2;
			module.stepG2(i/10.);
			verify(cell).updateTarget(RATE_G2, 2);
		}
	}
	
	@Test
	public void stepG2_noTransitionPhaseNotArrested_maintainsPhase() {
		PottsCell cell = mock(PottsCell.class);
		ProliferationModule module = spy(new ProliferationModule.Simple(cell));
		module.phase = PHASE_G2;
		module.isArrested = false;
		module.stepG2(Simulation.DT/DURATION_G2 + EPSILON);
		verify(module, never()).checkpointG2();
		assertEquals(PHASE_G2, module.phase);
	}
	
	@Test
	public void stepG2_withTransitionPhaseNotArrested_updatesPhase() {
		PottsCell cell = mock(PottsCell.class);
		ProliferationModule module = spy(new ProliferationModule.Simple(cell));
		module.phase = PHASE_G2;
		module.isArrested = false;
		module.stepG2( Simulation.DT/DURATION_G2 - EPSILON);
		verify(module).checkpointG2();
		assertEquals(PHASE_M, module.phase);
	}
	
	@Test
	public void stepG2_noTransitionPhaseArrested_maintainsPhase() {
		PottsCell cell = mock(PottsCell.class);
		ProliferationModule module = spy(new ProliferationModule.Simple(cell));
		module.phase = PHASE_G2;
		module.isArrested = true;
		module.stepG2(Simulation.DT/DURATION_CHECKPOINT + EPSILON);
		verify(module, never()).checkpointG2();
		assertEquals(PHASE_G2, module.phase);
	}
	
	@Test
	public void stepG2_withTransitionPhaseArrested_updatesPhase() {
		PottsCell cell = mock(PottsCell.class);
		ProliferationModule module = spy(new ProliferationModule.Simple(cell));
		module.phase = PHASE_G2;
		module.isArrested = true;
		module.stepG2(Simulation.DT/DURATION_CHECKPOINT - EPSILON);
		verify(module).checkpointG2();
		assertEquals(PHASE_M, module.phase);
	}
	
	@Test
	public void checkpointG2_checkpointPassed_updatesState() {
		PottsCellMock cell = mock(PottsCellMock.class);
		double volume = Math.random()*100;
		when(cell.getVolume()).thenReturn((int)(volume*GROWTH_CHECKPOINT_G2) + 1);
		when(cell.getCriticalVolume()).thenReturn(volume);
		ProliferationModule module = new ProliferationModule.Simple(cell);
		module.phase = PHASE_G2;
		
		module.checkpointG2();
		assertEquals(PHASE_M, module.phase);
		assertFalse(module.isArrested);
	}
	
	@Test
	public void checkpointG2_checkpointNotPassed_updatesState() {
		PottsCellMock cell = mock(PottsCellMock.class);
		double volume = Math.random()*100;
		when(cell.getVolume()).thenReturn((int)(volume*GROWTH_CHECKPOINT_G2) - 1);
		when(cell.getCriticalVolume()).thenReturn(volume);
		ProliferationModule module = new ProliferationModule.Simple(cell);
		module.phase = PHASE_G2;
		
		module.checkpointG2();
		assertEquals(PHASE_G2, module.phase);
		assertTrue(module.isArrested);
	}
	
	@Test
	public void stepM_noTransition_doesNothing() {
		PottsCell cell = mock(PottsCell.class);
		ProliferationModule module = spy(new ProliferationModule.Simple(cell));
		doNothing().when(module).addCell(random, sim);
		module.phase = PHASE_M;
		module.stepM(Simulation.DT/DURATION_M + EPSILON, random, sim);
		verify(module, never()).addCell(random, sim);
		assertEquals(PHASE_M, module.phase);
	}
	
	@Test
	public void stepM_withTransition_updatesPhase() {
		PottsCell cell = mock(PottsCell.class);
		ProliferationModule module = spy(new ProliferationModule.Simple(cell));
		doNothing().when(module).addCell(random, sim);
		module.phase = PHASE_M;
		module.stepM(Simulation.DT/DURATION_M - EPSILON, random, sim);
		verify(module).addCell(random, sim);
		assertEquals(PHASE_G1, module.phase);
	}
	
	@Test
	public void addCell_called_addsObject() {
		Location location = mock(Location.class);
		PottsCell cell = spy(mock(PottsCell.class));
		Potts potts = mock(Potts.class);
		Grid grid = mock(Grid.class);
		Simulation sim = mock(Simulation.class);
		Schedule schedule = mock(Schedule.class);
		
		int id = (int)(Math.random()*100) + 1;
		doReturn(potts).when(sim).getPotts();
		doReturn(id).when(sim).getID();
		doReturn(grid).when(sim).getAgents();
		doReturn(schedule).when(sim).getSchedule();
		
		potts.IDS = new int[][][] { { { } } };
		potts.TAGS = new int[][][] { { { } } };
		
		Location newLocation = mock(Location.class);
		PottsCell newCell = spy(mock(PottsCell.class));
		
		doReturn(newCell).when(cell).make(eq(id), anyInt(), eq(newLocation));
		doReturn(location).when(cell).getLocation();
		doReturn(newLocation).when(location).split(random);
		doNothing().when(cell).reset(any(), any());
		doNothing().when(newCell).reset(any(), any());
		
		ProliferationModule module = new ProliferationModule.Simple(cell);
		
		module.addCell(random, sim);
		verify(cell).reset(potts.IDS, potts.TAGS);
		verify(newCell).reset(potts.IDS, potts.TAGS);
		verify(grid).addObject(id, newCell);
		verify(newCell).schedule(schedule);
	}
}
