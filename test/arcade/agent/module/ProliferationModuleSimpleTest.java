package arcade.agent.module;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import sim.engine.Schedule;
import ec.util.MersenneTwisterFast;
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
	static PottsCellMock cell;
	static MersenneTwisterFast random;
	static Location location, newLocation;
	static Simulation sim;
	static double[] criticals = new double[] { 0, 0 };
	static double[] lambdas = new double[] { Math.random()*100, Math.random()*100 };
	static double[] adhesion = new double[] { Math.random()*100, Math.random()*100, Math.random()*100 };
	static double[][] criticalsTag = new double[][] {
			{ 0, 0 },
			{ 0, 0 }
	};
	static double[][] lambdasTag = new double[][] {
			{ Math.random()*100, Math.random()*100 },
			{ Math.random()*100, Math.random()*100 }
	};
	static double[][] adhesionsTag = new double[][] {
			{ Math.random()*100, Math.random()*100 },
			{ Math.random()*100, Math.random()*100 }
	};
	
	@BeforeClass
	public static void setupMocks() {
		random = mock(MersenneTwisterFast.class);
		when(random.nextDouble()).thenReturn(r);
		
		location = mock(Location.class);
		newLocation = mock(Location.class);
		when(location.split(random)).thenReturn(newLocation);
		
		cell = new PottsCellMock(1, 1, 0, 0, location, criticals, lambdas, adhesion, 2, criticalsTag, lambdasTag, adhesionsTag);
		
		sim = mock(Simulation.class);
	}
	
	@Test
	public void getPhase_defaultConstructor_returnsValue() {
		ProliferationModule module = new ProliferationModule.Simple(cell);
		assertEquals(PHASE_G1, module.getPhase());
	}
	
	@Test
	public void step_givenPhaseG1_callsMethod() {
		ProliferationModule module = new ProliferationModule.Simple(cell);
		ProliferationModule spy = spy(module);
		spy.phase = PHASE_G1;
		
		spy.step(random, sim);
		verify(spy).stepG1(r);
		verify(spy, never()).stepS();
		verify(spy, never()).stepG2(r);
		verify(spy, never()).stepM(r, random, sim);
	}
	
	@Test
	public void step_givenPhaseS_callsMethod() {
		ProliferationModule module = new ProliferationModule.Simple(cell);
		ProliferationModule spy = spy(module);
		spy.phase = PHASE_S;
		
		spy.step(random, sim);
		verify(spy).stepS();
		verify(spy, never()).stepG1(r);
		verify(spy, never()).stepG2(r);
		verify(spy, never()).stepM(r, random, sim);
	}
	
	@Test
	public void step_givenPhaseG2_callsMethod() {
		ProliferationModule module = new ProliferationModule.Simple(cell);
		ProliferationModule spy = spy(module);
		spy.phase = PHASE_G2;
		
		spy.step(random, sim);
		verify(spy).stepG2(r);
		verify(spy, never()).stepG1(r);
		verify(spy, never()).stepS();
		verify(spy, never()).stepM(r, random, sim);
	}
	
	@Test
	public void step_givenPhaseM_callsMethod() {
		ProliferationModule module = new ProliferationModule.Simple(cell);
		ProliferationModule spy = spy(module);
		spy.phase = PHASE_M;
		
		spy.step(random, sim);
		verify(spy).stepM(r, random, sim);
		verify(spy, never()).stepG1(r);
		verify(spy, never()).stepS();
		verify(spy, never()).stepG2(r);
	}
	
	@Test
	public void stepG1_noTransition_callsMethods() {
		PottsCellMock cell = spy(new PottsCellMock(1, 1, location, criticals, lambdas, adhesion, 2, criticalsTag, lambdasTag, adhesionsTag));
		ProliferationModule module = new ProliferationModule.Simple(cell);
		ProliferationModule spy = spy(module);
		spy.phase = PHASE_G1;
		
		spy.stepG1(1);
		verify(cell).updateTarget(RATE_G1, 2);
		verify(spy, never()).checkpointG1();
		assertEquals(PHASE_G1, spy.phase);
	}
	
	@Test
	public void stepG1_withTransition_callsMethods() {
		PottsCellMock cell = spy(new PottsCellMock(1, 1, location, criticals, lambdas, adhesion, 2, criticalsTag, lambdasTag, adhesionsTag));
		ProliferationModule module = new ProliferationModule.Simple(cell);
		ProliferationModule spy = spy(module);
		spy.phase = PHASE_G1;
		
		spy.stepG1(BASAL_APOPTOSIS_RATE*Simulation.DT + EPSILON);
		verify(cell).updateTarget(RATE_G1, 2);
		verify(spy).checkpointG1();
		assertEquals(PHASE_S, spy.phase);
	}
	
	@Test
	public void stepG1_withStateChange_callsMethods() {
		PottsCellMock cell = spy(new PottsCellMock(1, 1, location, criticals, lambdas, adhesion, 2, criticalsTag, lambdasTag, adhesionsTag));
		ProliferationModule module = new ProliferationModule.Simple(cell);
		ProliferationModule spy = spy(module);
		spy.phase = PHASE_G1;
		
		spy.stepG1(0);
		verify(cell, never()).updateTarget(RATE_G1, 2);
		verify(spy, never()).checkpointG1();
		assertEquals(STATE_APOPTOTIC, cell.getState());
		assertTrue(spy.cell.getModule() instanceof ApoptosisModule);
	}
	
	@Test
	public void stepG1_phaseNotArrested_callsMethods() {
		PottsCellMock cell = spy(new PottsCellMock(1, 1, location, criticals, lambdas, adhesion, 2, criticalsTag, lambdasTag, adhesionsTag));
		ProliferationModule module = new ProliferationModule.Simple(cell);
		ProliferationModule spy = spy(module);
		
		spy.phase = PHASE_G1;
		spy.isArrested = false;
		double transition = Simulation.DT*DURATION_CHECKPOINT;
		
		spy.stepG1(transition);
		verify(cell).updateTarget(RATE_G1, 2);
		verify(spy, never()).checkpointG1();
		assertEquals(PHASE_G1, spy.phase);
		
		spy.stepG1(transition + EPSILON);
		verify(cell, times(2)).updateTarget(RATE_G1, 2);
		verify(spy, never()).checkpointG1();
		assertEquals(PHASE_G1, spy.phase);
		
		spy.stepG1(transition - EPSILON);
		verify(cell, times(3)).updateTarget(RATE_G1, 2);
		verify(spy, never()).checkpointG1();
		assertEquals(PHASE_G1, spy.phase);
	}
	
	@Test
	public void stepG1_phaseArrested_callsMethods() {
		PottsCellMock cell = spy(new PottsCellMock(1, 1, location, criticals, lambdas, adhesion, 2, criticalsTag, lambdasTag, adhesionsTag));
		ProliferationModule module = new ProliferationModule.Simple(cell);
		ProliferationModule spy = spy(module);
		
		spy.phase = PHASE_G1;
		spy.isArrested = true;
		double transition = Simulation.DT*DURATION_CHECKPOINT;
		
		spy.stepG1(transition);
		verify(cell).updateTarget(RATE_G1, 2);
		verify(spy, never()).checkpointG1();
		assertEquals(PHASE_G1, spy.phase);
		
		spy.stepG1(transition + EPSILON);
		verify(cell, times(2)).updateTarget(RATE_G1, 2);
		verify(spy, never()).checkpointG1();
		assertEquals(PHASE_G1, spy.phase);
		
		spy.stepG1(transition - EPSILON);
		verify(cell, times(3)).updateTarget(RATE_G1, 2);
		verify(spy).checkpointG1();
		assertEquals(PHASE_S, spy.phase);
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
	public void stepS_noTransition_callsMethods() {
		PottsCellMock cell = mock(PottsCellMock.class);
		double volume = Math.random()*100;
		when(cell.getVolume(TAG_NUCLEUS)).thenReturn((int)(volume*GROWTH_CHECKPOINT_S) - 1);
		when(cell.getCriticalVolume(TAG_NUCLEUS)).thenReturn(volume);
		
		ProliferationModule module = new ProliferationModule.Simple(cell);
		ProliferationModule spy = spy(module);
		spy.phase = PHASE_S;
		
		spy.stepS();
		verify(cell).updateTarget(TAG_NUCLEUS, RATE_S, 2);
		assertEquals(PHASE_S, spy.getPhase());
	}
	
	@Test
	public void stepS_withTransition_callsMethods() {
		PottsCellMock cell = mock(PottsCellMock.class);
		double volume = Math.random()*100;
		when(cell.getVolume(TAG_NUCLEUS)).thenReturn((int)(volume*GROWTH_CHECKPOINT_S) + 1);
		when(cell.getCriticalVolume(TAG_NUCLEUS)).thenReturn(volume);
		
		ProliferationModule module = new ProliferationModule.Simple(cell);
		ProliferationModule spy = spy(module);
		spy.phase = PHASE_S;
		
		spy.stepS();
		verify(cell).updateTarget(TAG_NUCLEUS, RATE_S, 2);
		assertEquals(PHASE_G2, spy.getPhase());
	}
	
	@Test
	public void stepG2_noTransition_callsMethods() {
		PottsCellMock cell = spy(new PottsCellMock(1, 1, location, criticals, lambdas, adhesion, 2, criticalsTag, lambdasTag, adhesionsTag));
		ProliferationModule module = new ProliferationModule.Simple(cell);
		ProliferationModule spy = spy(module);
		spy.phase = PHASE_G2;
		
		spy.stepG2(1);
		verify(cell).updateTarget(RATE_G2, 2);
		verify(spy, never()).checkpointG2();
		assertEquals(PHASE_G2, spy.phase);
	}
	
	@Test
	public void stepG2_withTransition_callsMethods() {
		PottsCellMock cell = spy(new PottsCellMock(1, 1, location, criticals, lambdas, adhesion, 2, criticalsTag, lambdasTag, adhesionsTag));
		ProliferationModule module = new ProliferationModule.Simple(cell);
		ProliferationModule spy = spy(module);
		spy.phase = PHASE_G2;
		
		spy.stepG2(0);
		verify(cell).updateTarget(RATE_G2, 2);
		verify(spy).checkpointG2();
		assertEquals(PHASE_M, spy.phase);
	}
	
	@Test
	public void stepG2_phaseNotArrested_callsMethods() {
		PottsCellMock cell = spy(new PottsCellMock(1, 1, location, criticals, lambdas, adhesion, 2, criticalsTag, lambdasTag, adhesionsTag));
		ProliferationModule module = new ProliferationModule.Simple(cell);
		ProliferationModule spy = spy(module);
		
		spy.phase = PHASE_G2;
		spy.isArrested = false;
		double transition = Simulation.DT*DURATION_CHECKPOINT;
		
		spy.stepG2(transition);
		verify(cell).updateTarget(RATE_G2, 2);
		verify(spy, never()).checkpointG1();
		assertEquals(PHASE_G2, spy.phase);
		
		spy.stepG2(transition + EPSILON);
		verify(cell, times(2)).updateTarget(RATE_G2, 2);
		verify(spy, never()).checkpointG2();
		assertEquals(PHASE_G2, spy.phase);
		
		spy.stepG2(transition - EPSILON);
		verify(cell, times(3)).updateTarget(RATE_G2, 2);
		verify(spy, never()).checkpointG2();
		assertEquals(PHASE_G2, spy.phase);
	}
	
	@Test
	public void stepG2_phaseArrested_callsMethods() {
		PottsCellMock cell = spy(new PottsCellMock(1, 1, location, criticals, lambdas, adhesion, 2, criticalsTag, lambdasTag, adhesionsTag));
		ProliferationModule module = new ProliferationModule.Simple(cell);
		ProliferationModule spy = spy(module);
		
		spy.phase = PHASE_G2;
		spy.isArrested = true;
		double transition = Simulation.DT*DURATION_CHECKPOINT;
		
		spy.stepG2(transition);
		verify(cell).updateTarget(RATE_G2, 2);
		verify(spy, never()).checkpointG2();
		assertEquals(PHASE_G2, spy.phase);
		
		spy.stepG2(transition + EPSILON);
		verify(cell, times(2)).updateTarget(RATE_G2, 2);
		verify(spy, never()).checkpointG2();
		assertEquals(PHASE_G2, spy.phase);
		
		spy.stepG2(transition - EPSILON);
		verify(cell, times(3)).updateTarget(RATE_G2, 2);
		verify(spy).checkpointG2();
		assertEquals(PHASE_M, spy.phase);
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
	public void stepM_noTransition_callsMethods() {
		Location location = mock(Location.class);
		PottsCellMock cell = spy(new PottsCellMock(1, 1, location, criticals, lambdas, adhesion, 2, criticalsTag, lambdasTag, adhesionsTag));
		Potts potts = mock(Potts.class);
		Grid grid = mock(Grid.class);
		Simulation sim = mock(Simulation.class);
		Schedule schedule = spy(mock(Schedule.class));
		when(sim.getPotts()).thenReturn(potts);
		when(sim.getID()).thenReturn(2);
		when(sim.getAgents()).thenReturn(grid);
		when(sim.getSchedule()).thenReturn(schedule);
		
		ProliferationModule module = new ProliferationModule.Simple(cell);
		ProliferationModule spy = spy(module);
		spy.phase = PHASE_M;
		
		spy.stepM(1, random, sim);
		verify(location, never()).split(random);
		verify(cell, never()).reset(null, null);
		verify(sim, never()).getAgents();
		verify(sim, never()).getID();
		verify(sim, never()).getSchedule();
		verify(grid, never()).addObject(eq(2), isA(PottsCellMock.class));
		assertEquals(PHASE_M, spy.phase);
	}
	
	@Test
	public void stepM_withTransition_callsMethods() {
		Location location = mock(Location.class);
		Location newLocation = mock(Location.class);
		when(location.split(random)).thenReturn(newLocation);
		
		PottsCellMock cell = spy(new PottsCellMock(1, 1, location, criticals, lambdas, adhesion, 2, criticalsTag, lambdasTag, adhesionsTag));
		Potts potts = mock(Potts.class);
		Grid grid = mock(Grid.class);
		Simulation sim = mock(Simulation.class);
		Schedule schedule = spy(mock(Schedule.class));
		when(sim.getPotts()).thenReturn(potts);
		when(sim.getID()).thenReturn(2);
		when(sim.getAgents()).thenReturn(grid);
		when(sim.getSchedule()).thenReturn(schedule);
		
		ProliferationModule module = new ProliferationModule.Simple(cell);
		ProliferationModule spy = spy(module);
		spy.phase = PHASE_M;
		
		spy.stepM(0, random, sim);
		verify(location).split(random);
		verify(cell).reset(null, null);
		verify(sim).getAgents();
		verify(sim).getID();
		verify(sim).getSchedule();
		verify(grid).addObject(eq(2), isA(PottsCellMock.class));
		assertEquals(PHASE_G1, spy.phase);
	}
}
