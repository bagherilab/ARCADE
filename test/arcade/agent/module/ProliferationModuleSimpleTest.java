package arcade.agent.module;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import sim.engine.Schedule;
import ec.util.MersenneTwisterFast;
import arcade.agent.cell.Cell;
import arcade.sim.Simulation;
import arcade.sim.Potts;
import arcade.env.grid.Grid;
import arcade.env.loc.Location;
import arcade.util.MiniBox;
import static arcade.agent.cell.Cell.Tag;
import static arcade.agent.cell.Cell.State;
import static arcade.agent.module.Module.Phase;
import static arcade.agent.module.ProliferationModule.*;
import static arcade.MainTest.*;

public class ProliferationModuleSimpleTest {
	private static final double EPSILON = 1E-5;
	private static final double r = 1.0;
	static MersenneTwisterFast random;
	static Simulation sim;
	static Cell cell;
	static MiniBox parameters;
	
	@BeforeClass
	public static void setupMocks() {
		random = mock(MersenneTwisterFast.class);
		when(random.nextDouble()).thenReturn(r);
		sim = mock(Simulation.class);
		cell = mock(Cell.class);
		
		MiniBox box = mock(MiniBox.class);
		doReturn(0.).when(box).getDouble(anyString());
		doReturn(box).when(cell).getParameters();
		
		parameters = new MiniBox();
		parameters.put("DURATION_PROLIFERATION_G1", randomDouble());
		parameters.put("DURATION_PROLIFERATION_S", randomDouble());
		parameters.put("DURATION_PROLIFERATION_G2", randomDouble());
		parameters.put("DURATION_PROLIFERATION_M", randomDouble());
		parameters.put("DURATION_PROLIFERATION_CHECKPOINT", randomDouble());
		parameters.put("BASAL_APOPTOSIS_RATE", Math.random()/2);
	}
	
	@Test
	public void constructor_setsParameters() {
		Cell cell = mock(Cell.class);
		doReturn(parameters).when(cell).getParameters();
		ProliferationModule module = new ProliferationModule.Simple(cell);
		
		assertEquals(parameters.getDouble("DURATION_PROLIFERATION_G1"), module.DURATION_G1, EPSILON);
		assertEquals(parameters.getDouble("DURATION_PROLIFERATION_S"), module.DURATION_S, EPSILON);
		assertEquals(parameters.getDouble("DURATION_PROLIFERATION_G2"), module.DURATION_G2, EPSILON);
		assertEquals(parameters.getDouble("DURATION_PROLIFERATION_M"), module.DURATION_M, EPSILON);
		assertEquals(parameters.getDouble("DURATION_PROLIFERATION_CHECKPOINT"), module.DURATION_CHECKPOINT, EPSILON);
		assertEquals(parameters.getDouble("BASAL_APOPTOSIS_RATE"), module.BASAL_APOPTOSIS_RATE, EPSILON);
	}
	
	@Test
	public void constructor_calculatesParameters() {
		Cell cell = mock(Cell.class);
		doReturn(parameters).when(cell).getParameters();
		ProliferationModule module = new ProliferationModule.Simple(cell);
		
		double durationG1 = parameters.getDouble("DURATION_PROLIFERATION_G1");
		double durationS = parameters.getDouble("DURATION_PROLIFERATION_S");
		double durationG2 = parameters.getDouble("DURATION_PROLIFERATION_G2");
		
		assertEquals(-Math.log(0.05)/durationG1, module.RATE_G1, EPSILON);
		assertEquals(-Math.log(0.01)/durationS, module.RATE_S, EPSILON);
		assertEquals(-Math.log(0.01)/durationG2, module.RATE_G2, EPSILON);
	}

	@Test
	public void getPhase_defaultConstructor_returnsValue() {
		ProliferationModule module = new ProliferationModule.Simple(cell);
		assertEquals(Phase.PROLIFERATIVE_G1, module.getPhase());
	}
	
	@Test
	public void setPhase_givenValue_setsValue() {
		Phase phase = Phase.values()[(int)(Math.random()*Phase.values().length)];
		ProliferationModule module = new ProliferationModule.Simple(cell);
		module.setPhase(phase);
		assertEquals(phase, module.phase);
	}
	
	@Test
	public void step_givenPhaseG1_callsMethod() {
		ProliferationModule module = spy(new ProliferationModule.Simple(cell));
		module.phase = Phase.PROLIFERATIVE_G1;
		
		module.step(random, sim);
		verify(module).stepG1(r);
		verify(module, never()).stepS(r);
		verify(module, never()).stepG2(r);
		verify(module, never()).stepM(r, random, sim);
	}
	
	@Test
	public void step_givenPhaseS_callsMethod() {
		ProliferationModule module = spy(new ProliferationModule.Simple(cell));
		module.phase = Phase.PROLIFERATIVE_S;
		
		module.step(random, sim);
		verify(module).stepS(r);
		verify(module, never()).stepG1(r);
		verify(module, never()).stepG2(r);
		verify(module, never()).stepM(r, random, sim);
	}
	
	@Test
	public void step_givenPhaseG2_callsMethod() {
		ProliferationModule module = spy(new ProliferationModule.Simple(cell));
		module.phase = Phase.PROLIFERATIVE_G2;
		
		module.step(random, sim);
		verify(module).stepG2(r);
		verify(module, never()).stepG1(r);
		verify(module, never()).stepS(r);
		verify(module, never()).stepM(r, random, sim);
	}
	
	@Test
	public void step_givenPhaseM_callsMethod() {
		ProliferationModule module = spy(new ProliferationModule.Simple(cell));
		doNothing().when(module).addCell(random, sim);
		module.phase = Phase.PROLIFERATIVE_M;
		
		module.step(random, sim);
		verify(module).stepM(r, random, sim);
		verify(module, never()).stepG1(r);
		verify(module, never()).stepS(r);
		verify(module, never()).stepG2(r);
	}
	
	@Test
	public void step_invalidPhase_doesNothing() {
		ProliferationModule module = spy(new ProliferationModule.Simple(cell));
		module.phase = Phase.UNDEFINED;
		
		module.step(random, sim);
		verify(module, never()).stepM(r, random, sim);
		verify(module, never()).stepG1(r);
		verify(module, never()).stepS(r);
		verify(module, never()).stepG2(r);
	}
	
	@Test
	public void stepG1_withStateChange_callsMethods() {
		Cell cell = mock(Cell.class);
		doReturn(parameters).when(cell).getParameters();
		
		ProliferationModule module = spy(new ProliferationModule.Simple(cell));
		module.phase = Phase.PROLIFERATIVE_G1;
		module.stepG1(module.BASAL_APOPTOSIS_RATE - EPSILON);
		
		verify(cell, never()).updateTarget(module.RATE_G1, 2);
		verify(module, never()).checkpointG1();
		verify(cell).setState(State.APOPTOTIC);
	}
	
	@Test
	public void stepG1_withoutStateChange_updatesCell() {
		for (int i = 0; i < 10; i++) {
			Cell cell = mock(Cell.class);
			doReturn(parameters).when(cell).getParameters();
			
			ProliferationModule module = spy(new ProliferationModule.Simple(cell));
			module.phase = Phase.PROLIFERATIVE_G1;
			module.stepG1(i/10. + module.BASAL_APOPTOSIS_RATE);
			
			verify(cell).updateTarget(module.RATE_G1, 2);
		}
	}
	
	@Test
	public void stepG1_noTransitionPhaseNotArrested_maintainsPhase() {
		Cell cell = mock(Cell.class);
		doReturn(parameters).when(cell).getParameters();
		
		ProliferationModule module = spy(new ProliferationModule.Simple(cell));
		module.phase = Phase.PROLIFERATIVE_G1;
		module.isArrested = false;
		module.stepG1(1.0/module.DURATION_G1 + EPSILON);
		
		verify(module, never()).checkpointG1();
		assertEquals(Phase.PROLIFERATIVE_G1, module.phase);
	}
	
	@Test
	public void stepG1_withTransitionPhaseNotArrested_updatesPhase() {
		Cell cell = mock(Cell.class);
		doReturn(parameters).when(cell).getParameters();
		
		ProliferationModule module = spy(new ProliferationModule.Simple(cell));
		module.phase = Phase.PROLIFERATIVE_G1;
		module.isArrested = false;
		module.stepG1(1.0/module.DURATION_G1 - EPSILON);
		
		verify(module).checkpointG1();
		assertEquals(Phase.PROLIFERATIVE_S, module.phase);
	}
	
	@Test
	public void stepG1_noTransitionPhaseArrested_maintainsPhase() {
		Cell cell = mock(Cell.class);
		doReturn(parameters).when(cell).getParameters();
		
		ProliferationModule module = spy(new ProliferationModule.Simple(cell));
		module.phase = Phase.PROLIFERATIVE_G1;
		module.isArrested = true;
		module.stepG1(1.0/module.DURATION_CHECKPOINT + EPSILON);
		
		verify(module, never()).checkpointG1();
		assertEquals(Phase.PROLIFERATIVE_G1, module.phase);
	}
	
	@Test
	public void stepG1_withTransitionPhaseArrested_updatesPhase() {
		Cell cell = mock(Cell.class);
		doReturn(parameters).when(cell).getParameters();
		
		ProliferationModule module = spy(new ProliferationModule.Simple(cell));
		module.phase = Phase.PROLIFERATIVE_G1;
		module.isArrested = true;
		
		module.stepG1(1.0/module.DURATION_CHECKPOINT - EPSILON);
		verify(module).checkpointG1();
		assertEquals(Phase.PROLIFERATIVE_S, module.phase);
	}
	
	@Test
	public void checkpointG1_checkpointPassed_updatesState() {
		Cell cell = mock(Cell.class);
		doReturn(parameters).when(cell).getParameters();
		
		double volume = Math.random()*100;
		when(cell.getVolume()).thenReturn((int)(volume*GROWTH_CHECKPOINT_G1) + 1);
		when(cell.getCriticalVolume()).thenReturn(volume);
		
		ProliferationModule module = new ProliferationModule.Simple(cell);
		module.phase = Phase.PROLIFERATIVE_G1;
		module.checkpointG1();
		
		assertEquals(Phase.PROLIFERATIVE_S, module.phase);
		assertFalse(module.isArrested);
	}
	
	@Test
	public void checkpointG1_checkpointNotPassed_updatesState() {
		Cell cell = mock(Cell.class);
		doReturn(parameters).when(cell).getParameters();
		
		double volume = Math.random()*100;
		when(cell.getVolume()).thenReturn((int)(volume*GROWTH_CHECKPOINT_G1) - 1);
		when(cell.getCriticalVolume()).thenReturn(volume);
		
		ProliferationModule module = new ProliferationModule.Simple(cell);
		module.phase = Phase.PROLIFERATIVE_G1;
		module.checkpointG1();
		
		assertEquals(Phase.PROLIFERATIVE_G1, module.phase);
		assertTrue(module.isArrested);
	}
	
	@Test
	public void stepS_anyTransitionTagged_updatesCell() {
		for (int i = 0; i < 10; i++) {
			Cell cell = mock(Cell.class);
			doReturn(parameters).when(cell).getParameters();
			doReturn(true).when(cell).hasTags();
			
			ProliferationModule module = spy(new ProliferationModule.Simple(cell));
			module.phase = Phase.PROLIFERATIVE_S;
			module.stepS(i/10.);
			
			verify(cell).updateTarget(Tag.NUCLEUS, module.RATE_S, 2);
		}
	}
	
	@Test
	public void stepS_noTransitionTagged_maintainsPhase() {
		Cell cell = mock(Cell.class);
		doReturn(parameters).when(cell).getParameters();
		doReturn(true).when(cell).hasTags();
		
		double volume = Math.random()*100;
		when(cell.getVolume(Tag.NUCLEUS)).thenReturn((int)(volume*GROWTH_CHECKPOINT_S) - 1);
		when(cell.getCriticalVolume(Tag.NUCLEUS)).thenReturn(volume);
		
		ProliferationModule module = spy(new ProliferationModule.Simple(cell));
		module.phase = Phase.PROLIFERATIVE_S;
		module.stepS(Math.random());
		
		assertEquals(Phase.PROLIFERATIVE_S, module.getPhase());
	}
	
	@Test
	public void stepS_withTransitionTagged_updatesPhase() {
		Cell cell = mock(Cell.class);
		doReturn(parameters).when(cell).getParameters();
		doReturn(true).when(cell).hasTags();
		
		double volume = Math.random()*100;
		when(cell.getVolume(Tag.NUCLEUS)).thenReturn((int)(volume*GROWTH_CHECKPOINT_S) + 1);
		when(cell.getCriticalVolume(Tag.NUCLEUS)).thenReturn(volume);
		
		ProliferationModule module = spy(new ProliferationModule.Simple(cell));
		module.phase = Phase.PROLIFERATIVE_S;
		module.stepS(Math.random());
		
		assertEquals(Phase.PROLIFERATIVE_G2, module.getPhase());
	}
	
	@Test
	public void stepS_noTransitionUntagged_maintainsPhase() {
		Cell cell = mock(Cell.class);
		doReturn(parameters).when(cell).getParameters();
		
		ProliferationModule module = spy(new ProliferationModule.Simple(cell));
		module.phase = Phase.PROLIFERATIVE_S;
		module.stepS(1.0/module.DURATION_S + EPSILON);
		
		assertEquals(Phase.PROLIFERATIVE_S, module.phase);
	}
	
	@Test
	public void stepS_withTransitionUntagged_updatesPhase() {
		Cell cell = mock(Cell.class);
		doReturn(parameters).when(cell).getParameters();
		
		ProliferationModule module = spy(new ProliferationModule.Simple(cell));
		module.phase = Phase.PROLIFERATIVE_S;
		module.stepS(1.0/module.DURATION_S - EPSILON);
		
		assertEquals(Phase.PROLIFERATIVE_G2, module.phase);
	}
	
	@Test
	public void stepG2_anyTransition_updatesCell() {
		for (int i = 0; i < 10; i++) {
			Cell cell = mock(Cell.class);
			doReturn(parameters).when(cell).getParameters();
			
			ProliferationModule module = spy(new ProliferationModule.Simple(cell));
			module.phase = Phase.PROLIFERATIVE_G2;
			module.stepG2(i/10.);
			
			verify(cell).updateTarget(module.RATE_G2, 2);
		}
	}
	
	@Test
	public void stepG2_noTransitionPhaseNotArrested_maintainsPhase() {
		Cell cell = mock(Cell.class);
		doReturn(parameters).when(cell).getParameters();
		
		ProliferationModule module = spy(new ProliferationModule.Simple(cell));
		module.phase = Phase.PROLIFERATIVE_G2;
		module.isArrested = false;
		module.stepG2(1.0/module.DURATION_G2 + EPSILON);
		
		verify(module, never()).checkpointG2();
		assertEquals(Phase.PROLIFERATIVE_G2, module.phase);
	}
	
	@Test
	public void stepG2_withTransitionPhaseNotArrested_updatesPhase() {
		Cell cell = mock(Cell.class);
		doReturn(parameters).when(cell).getParameters();
		
		ProliferationModule module = spy(new ProliferationModule.Simple(cell));
		module.phase = Phase.PROLIFERATIVE_G2;
		module.isArrested = false;
		module.stepG2(1.0/module.DURATION_G2 - EPSILON);
		
		verify(module).checkpointG2();
		assertEquals(Phase.PROLIFERATIVE_M, module.phase);
	}
	
	@Test
	public void stepG2_noTransitionPhaseArrested_maintainsPhase() {
		Cell cell = mock(Cell.class);
		doReturn(parameters).when(cell).getParameters();
		
		ProliferationModule module = spy(new ProliferationModule.Simple(cell));
		module.phase = Phase.PROLIFERATIVE_G2;
		module.isArrested = true;
		module.stepG2(1.0/module.DURATION_CHECKPOINT + EPSILON);
		
		verify(module, never()).checkpointG2();
		assertEquals(Phase.PROLIFERATIVE_G2, module.phase);
	}
	
	@Test
	public void stepG2_withTransitionPhaseArrested_updatesPhase() {
		Cell cell = mock(Cell.class);
		doReturn(parameters).when(cell).getParameters();
		
		ProliferationModule module = spy(new ProliferationModule.Simple(cell));
		module.phase = Phase.PROLIFERATIVE_G2;
		module.isArrested = true;
		module.stepG2(1.0/module.DURATION_CHECKPOINT - EPSILON);
		
		verify(module).checkpointG2();
		assertEquals(Phase.PROLIFERATIVE_M, module.phase);
	}
	
	@Test
	public void checkpointG2_checkpointPassed_updatesState() {
		Cell cell = mock(Cell.class);
		doReturn(parameters).when(cell).getParameters();
		
		double volume = Math.random()*100;
		when(cell.getVolume()).thenReturn((int)(volume*GROWTH_CHECKPOINT_G2) + 1);
		when(cell.getCriticalVolume()).thenReturn(volume);
		
		ProliferationModule module = new ProliferationModule.Simple(cell);
		module.phase = Phase.PROLIFERATIVE_G2;
		module.checkpointG2();
		
		assertEquals(Phase.PROLIFERATIVE_M, module.phase);
		assertFalse(module.isArrested);
	}
	
	@Test
	public void checkpointG2_checkpointNotPassed_updatesState() {
		Cell cell = mock(Cell.class);
		doReturn(parameters).when(cell).getParameters();
		
		double volume = Math.random()*100;
		when(cell.getVolume()).thenReturn((int)(volume*GROWTH_CHECKPOINT_G2) - 1);
		when(cell.getCriticalVolume()).thenReturn(volume);
		
		ProliferationModule module = new ProliferationModule.Simple(cell);
		module.phase = Phase.PROLIFERATIVE_G2;
		module.checkpointG2();
		
		assertEquals(Phase.PROLIFERATIVE_G2, module.phase);
		assertTrue(module.isArrested);
	}
	
	@Test
	public void stepM_noTransition_doesNothing() {
		Cell cell = mock(Cell.class);
		doReturn(parameters).when(cell).getParameters();
		
		ProliferationModule module = spy(new ProliferationModule.Simple(cell));
		doNothing().when(module).addCell(random, sim);
		module.phase = Phase.PROLIFERATIVE_M;
		module.stepM(1.0/module.DURATION_M + EPSILON, random, sim);
		
		verify(module, never()).addCell(random, sim);
		assertEquals(Phase.PROLIFERATIVE_M, module.phase);
	}
	
	@Test
	public void stepM_withTransition_updatesPhase() {
		Cell cell = mock(Cell.class);
		doReturn(parameters).when(cell).getParameters();
		
		ProliferationModule module = spy(new ProliferationModule.Simple(cell));
		doNothing().when(module).addCell(random, sim);
		module.phase = Phase.PROLIFERATIVE_M;
		module.stepM(1.0/module.DURATION_M - EPSILON, random, sim);
		
		verify(module).addCell(random, sim);
		assertEquals(Phase.PROLIFERATIVE_G1, module.phase);
	}
	
	@Test
	public void addCell_called_addsObject() {
		Location location = mock(Location.class);
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
		Cell newCell = mock(Cell.class);
		
		doReturn(newCell).when(cell).make(eq(id), any(State.class), eq(newLocation));
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
