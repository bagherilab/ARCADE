package arcade.agent.module;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import sim.engine.Stoppable;
import ec.util.MersenneTwisterFast;
import arcade.sim.Simulation;
import arcade.sim.Potts;
import arcade.env.grid.Grid;
import arcade.env.loc.Location;
import static arcade.agent.cell.Cell.*;
import static arcade.agent.module.ApoptosisModule.*;
import static arcade.agent.cell.PottsCellTest.PottsCellMock;

public class ApoptosisModuleSimpleTest {
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
		
		cell = new PottsCellMock(1, 1, location, criticals, lambdas, adhesion, 2, criticalsTag, lambdasTag, adhesionsTag);
		
		sim = mock(Simulation.class);
	}
	
	@Test
	public void getPhase_defaultConstructor_returnsValue() {
		ApoptosisModule module = new ApoptosisModule.Simple(cell);
		assertEquals(PHASE_EARLY_APOPTOSIS, module.getPhase());
	}
	
	@Test
	public void step_givenPhaseEarly_callsMethod() {
		ApoptosisModule module = new ApoptosisModule.Simple(cell);
		ApoptosisModule spy = spy(module);
		spy.phase = PHASE_EARLY_APOPTOSIS;
		
		spy.step(random, sim);
		verify(spy).stepEarly(r);
		verify(spy, never()).stepLate(r, sim);
	}
	
	@Test
	public void step_givenPhaseS_callsMethod() {
		ApoptosisModule module = new ApoptosisModule.Simple(cell);
		ApoptosisModule spy = spy(module);
		spy.phase = PHASE_LATE_APOPTOSIS;
		
		spy.step(random, sim);
		verify(spy).stepLate(r, sim);
		verify(spy, never()).stepEarly(r);
	}
	
	@Test
	public void stepEarly_noTransition_callsMethods() {
		PottsCellMock cell = spy(new PottsCellMock(1, 1, location, criticals, lambdas, adhesion, 2, criticalsTag, lambdasTag, adhesionsTag));
		ApoptosisModule module = new ApoptosisModule.Simple(cell);
		ApoptosisModule spy = spy(module);
		spy.phase = PHASE_EARLY_APOPTOSIS;
		
		spy.stepEarly(1);
		verify(cell).updateTarget(TAG_CYTOPLASM, RATE_CYTOPLASM_LOSS, 0.5);
		verify(cell).updateTarget(TAG_NUCLEUS, RATE_NUCLEUS_PYKNOSIS, 0.5);
		assertEquals(PHASE_EARLY_APOPTOSIS, spy.phase);
	}
	
	@Test
	public void stepEarly_withTransition_callsMethods() {
		PottsCellMock cell = spy(new PottsCellMock(1, 1, location, criticals, lambdas, adhesion, 2, criticalsTag, lambdasTag, adhesionsTag));
		ApoptosisModule module = new ApoptosisModule.Simple(cell);
		ApoptosisModule spy = spy(module);
		spy.phase = PHASE_EARLY_APOPTOSIS;
		
		spy.stepEarly(0);
		verify(cell).updateTarget(TAG_CYTOPLASM, RATE_CYTOPLASM_LOSS, 0.5);
		verify(cell).updateTarget(TAG_NUCLEUS, RATE_NUCLEUS_PYKNOSIS, 0.5);
		assertEquals(PHASE_LATE_APOPTOSIS, spy.phase);
	}
	
	@Test
	public void stepLate_noTransition_callsMethods() {
		Location location = mock(Location.class);
		Stoppable stopper = mock(Stoppable.class);
		PottsCellMock cell = spy(new PottsCellMock(1, 1, location, criticals, lambdas, adhesion, 2, criticalsTag, lambdasTag, adhesionsTag));
		cell.stopper = stopper;
		Potts potts = mock(Potts.class);
		Grid grid = mock(Grid.class);
		Simulation sim = mock(Simulation.class);
		when(sim.getPotts()).thenReturn(potts);
		when(sim.getAgents()).thenReturn(grid);
		ApoptosisModule module = new ApoptosisModule.Simple(cell);
		ApoptosisModule spy = spy(module);
		spy.phase = PHASE_LATE_APOPTOSIS;
		
		spy.stepLate(1, sim);
		verify(cell).updateTarget(TAG_CYTOPLASM, RATE_CYTOPLASM_BLEBBING, 0);
		verify(cell).updateTarget(TAG_NUCLEUS, RATE_NUCLEUS_FRAGMENTATION, 0);
		verify(sim, never()).getAgents();
		verify(sim, never()).getPotts();
		verify(location, never()).clear(null, null);
		verify(stopper, never()).stop();
		assertEquals(PHASE_LATE_APOPTOSIS, spy.phase);
	}
	
	@Test
	public void stepLate_withTransitionProbability_callsMethods() {
		Location location = mock(Location.class);
		Stoppable stopper = mock(Stoppable.class);
		PottsCellMock cell = spy(new PottsCellMock(1, 1, location, criticals, lambdas, adhesion, 2, criticalsTag, lambdasTag, adhesionsTag));
		cell.stopper = stopper;
		Potts potts = mock(Potts.class);
		Grid grid = mock(Grid.class);
		Simulation sim = mock(Simulation.class);
		when(sim.getPotts()).thenReturn(potts);
		when(sim.getAgents()).thenReturn(grid);
		ApoptosisModule module = new ApoptosisModule.Simple(cell);
		ApoptosisModule spy = spy(module);
		spy.phase = PHASE_LATE_APOPTOSIS;
		
		spy.stepLate(0, sim);
		verify(cell).updateTarget(TAG_CYTOPLASM, RATE_CYTOPLASM_BLEBBING, 0);
		verify(cell).updateTarget(TAG_NUCLEUS, RATE_NUCLEUS_FRAGMENTATION, 0);
		verify(sim).getAgents();
		verify(sim).getPotts();
		verify(location).clear(null, null);
		verify(stopper).stop();
		assertEquals(PHASE_LATE_APOPTOSIS, spy.phase);
	}
	
	@Test
	public void stepLate_withTransitionSize_callsMethods() {
		Location location = mock(Location.class);
		Stoppable stopper = mock(Stoppable.class);
		PottsCellMock cell = spy(new PottsCellMock(1, 1, location, criticals, lambdas, adhesion, 2, criticalsTag, lambdasTag, adhesionsTag));
		cell.stopper = stopper;
		when(cell.getVolume()).thenReturn((int)(APOPTOSIS_CHECKPOINT*100.) - 1);
		when(cell.getCriticalVolume()).thenReturn(100.);
		Potts potts = mock(Potts.class);
		Grid grid = mock(Grid.class);
		Simulation sim = mock(Simulation.class);
		when(sim.getPotts()).thenReturn(potts);
		when(sim.getAgents()).thenReturn(grid);
		ApoptosisModule module = new ApoptosisModule.Simple(cell);
		ApoptosisModule spy = spy(module);
		spy.phase = PHASE_LATE_APOPTOSIS;
		
		spy.stepLate(1, sim);
		verify(cell).updateTarget(TAG_CYTOPLASM, RATE_CYTOPLASM_BLEBBING, 0);
		verify(cell).updateTarget(TAG_NUCLEUS, RATE_NUCLEUS_FRAGMENTATION, 0);
		verify(sim).getAgents();
		verify(sim).getPotts();
		verify(location).clear(null, null);
		verify(stopper).stop();
		assertEquals(PHASE_LATE_APOPTOSIS, spy.phase);
	}
}
