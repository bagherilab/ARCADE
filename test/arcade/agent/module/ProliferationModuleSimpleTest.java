package arcade.agent.module;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import ec.util.MersenneTwisterFast;
import arcade.sim.Simulation;
import arcade.agent.cell.PottsCell;
import static arcade.agent.cell.Cell.*;
import static arcade.agent.module.ProliferationModule.*;

public class ProliferationModuleSimpleTest {
	private static final double EPSILON = 1E-5;
	private static final double r = 1.0;
	PottsCell cell;
	MersenneTwisterFast random;
	Simulation sim;
	
	@Before
	public void setupMocks() {
		cell = mock(PottsCell.class);
		
		random = mock(MersenneTwisterFast.class);
		when(random.nextDouble()).thenReturn(r);
		
		sim = mock(Simulation.class);
	}
	
	@Test
	public void getPhase_defaultConstructor_returnsValue() {
		ProliferationModule module = new ProliferationModule.Simple(cell);
		assertEquals(ProliferationModule.PHASE_G1, module.getPhase());
	}
	
	@Test
	public void step_givenPhaseG1_callsMethod() {
		ProliferationModule module = new ProliferationModule.Simple(cell);
		ProliferationModule spy = spy(module);
		
		spy.step(random, sim);
		verify(spy).stepG1(r);
		verify(spy, never()).stepS();
		verify(spy, never()).stepG2(r);
		verify(spy, never()).stepM(r);
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
		verify(spy, never()).stepM(r);
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
		verify(spy, never()).stepM(r);
	}
	
	@Test
	public void step_givenPhaseM_callsMethod() {
		ProliferationModule module = new ProliferationModule.Simple(cell);
		ProliferationModule spy = spy(module);
		
		spy.phase = PHASE_M;
		spy.step(random, sim);
		verify(spy).stepM(r);
		verify(spy, never()).stepG1(r);
		verify(spy, never()).stepS();
		verify(spy, never()).stepG2(r);
	}
	
	@Test
	public void stepG1_noTransition_callMethods() {
		ProliferationModule module = new ProliferationModule.Simple(cell);
		ProliferationModule spy = spy(module);
		
		spy.stepG1(1);
		verify(cell).updateTarget(RATE_G1, 2);
		verify(spy, never()).checkpointG1();
	}
	
	@Test
	public void stepG1_withTransition_callMethods() {
		ProliferationModule module = new ProliferationModule.Simple(cell);
		ProliferationModule spy = spy(module);
		
		spy.stepG1(0);
		verify(cell).updateTarget(RATE_G1, 2);
		verify(spy).checkpointG1();
	}
	
	@Test
	public void stepG1_phaseNotArrested_callMethods() {
		ProliferationModule module = new ProliferationModule.Simple(cell);
		ProliferationModule spy = spy(module);
		
		spy.isArrested = false;
		double transition = Simulation.DT*DURATION_CHECKPOINT;
		
		spy.stepG1(transition);
		verify(cell).updateTarget(RATE_G1, 2);
		verify(spy, never()).checkpointG1();
		
		spy.stepG1(transition + EPSILON);
		verify(cell, times(2)).updateTarget(RATE_G1, 2);
		verify(spy, never()).checkpointG1();
		
		spy.stepG1(transition - EPSILON);
		verify(cell, times(3)).updateTarget(RATE_G1, 2);
		verify(spy, never()).checkpointG1();
	}
	
	@Test
	public void stepG1_phaseArrested_callMethods() {
		ProliferationModule module = new ProliferationModule.Simple(cell);
		ProliferationModule spy = spy(module);
		
		spy.isArrested = true;
		double transition = Simulation.DT*DURATION_CHECKPOINT;
		
		spy.stepG1(transition);
		verify(cell).updateTarget(RATE_G1, 2);
		verify(spy, never()).checkpointG1();
		
		spy.stepG1(transition + EPSILON);
		verify(cell, times(2)).updateTarget(RATE_G1, 2);
		verify(spy, never()).checkpointG1();
		
		spy.stepG1(transition - EPSILON);
		verify(cell, times(3)).updateTarget(RATE_G1, 2);
		verify(spy).checkpointG1();
	}
	
	@Test
	public void checkpointG1_checkpointPassed_updatesState() {
		PottsCell cell = mock(PottsCell.class);
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
		PottsCell cell = mock(PottsCell.class);
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
	public void stepS_noTransition_callMethods() {
		PottsCell cell = mock(PottsCell.class);
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
	public void stepS_withTransition_callMethods() {
		PottsCell cell = mock(PottsCell.class);
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
	public void stepG2_noTransition_callMethods() {
		ProliferationModule module = new ProliferationModule.Simple(cell);
		ProliferationModule spy = spy(module);
		
		spy.stepG2(1);
		verify(cell).updateTarget(RATE_G2, 2);
		verify(spy, never()).checkpointG2();
	}
	
	@Test
	public void stepG2_withTransition_callMethods() {
		ProliferationModule module = new ProliferationModule.Simple(cell);
		ProliferationModule spy = spy(module);
		
		spy.stepG2(0);
		verify(cell).updateTarget(RATE_G2, 2);
		verify(spy).checkpointG2();
	}
	
	@Test
	public void stepG2_phaseNotArrested_callMethods() {
		ProliferationModule module = new ProliferationModule.Simple(cell);
		ProliferationModule spy = spy(module);
		
		spy.isArrested = false;
		double transition = Simulation.DT*DURATION_CHECKPOINT;
		
		spy.stepG2(transition);
		verify(cell).updateTarget(RATE_G2, 2);
		verify(spy, never()).checkpointG1();
		
		spy.stepG2(transition + EPSILON);
		verify(cell, times(2)).updateTarget(RATE_G2, 2);
		verify(spy, never()).checkpointG2();
		
		spy.stepG2(transition - EPSILON);
		verify(cell, times(3)).updateTarget(RATE_G2, 2);
		verify(spy, never()).checkpointG2();
	}
	
	@Test
	public void stepG2_phaseArrested_callMethods() {
		ProliferationModule module = new ProliferationModule.Simple(cell);
		ProliferationModule spy = spy(module);
		
		spy.isArrested = true;
		double transition = Simulation.DT*DURATION_CHECKPOINT;
		
		spy.stepG2(transition);
		verify(cell).updateTarget(RATE_G2, 2);
		verify(spy, never()).checkpointG2();
		
		spy.stepG2(transition + EPSILON);
		verify(cell, times(2)).updateTarget(RATE_G2, 2);
		verify(spy, never()).checkpointG2();
		
		spy.stepG2(transition - EPSILON);
		verify(cell, times(3)).updateTarget(RATE_G2, 2);
		verify(spy).checkpointG2();
	}
	
	@Test
	public void checkpointG2_checkpointPassed_updatesState() {
		PottsCell cell = mock(PottsCell.class);
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
		PottsCell cell = mock(PottsCell.class);
		double volume = Math.random()*100;
		when(cell.getVolume()).thenReturn((int)(volume*GROWTH_CHECKPOINT_G2) - 1);
		when(cell.getCriticalVolume()).thenReturn(volume);
		ProliferationModule module = new ProliferationModule.Simple(cell);
		module.phase = PHASE_G2;
		
		module.checkpointG2();
		assertEquals(PHASE_G2, module.phase);
		assertTrue(module.isArrested);
	}
}
