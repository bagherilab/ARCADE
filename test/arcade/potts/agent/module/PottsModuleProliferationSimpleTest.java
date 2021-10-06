package arcade.potts.agent.module;

import org.junit.BeforeClass;
import org.junit.Test;
import sim.engine.Schedule;
import sim.util.distribution.Poisson;
import ec.util.MersenneTwisterFast;
import arcade.core.env.grid.Grid;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.env.loc.PottsLocation;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.core.util.Enums.Region;
import static arcade.core.util.Enums.State;
import static arcade.potts.agent.module.PottsModuleProliferation.*;
import static arcade.potts.util.PottsEnums.Phase;

public class PottsModuleProliferationSimpleTest {
    private static final double EPSILON = 1E-10;
    private static final MersenneTwisterFast RANDOM = new MersenneTwisterFast(randomSeed());
    private static final double R = 1.0;
    private static final int N = 0;
    static MersenneTwisterFast random;
    static Poisson poissonMock;
    static PottsSimulation simMock;
    static PottsCell cellMock;
    static MiniBox parameters;
    
    @BeforeClass
    public static void setupMocks() {
        random = mock(MersenneTwisterFast.class);
        doReturn(R).when(random).nextDouble();
        
        poissonMock = mock(Poisson.class);
        doReturn(N).when(poissonMock).nextInt();
        
        simMock = mock(PottsSimulation.class);
        
        cellMock = mock(PottsCell.class);
        MiniBox box = mock(MiniBox.class);
        doReturn(0.).when(box).getDouble(anyString());
        doReturn(box).when(cellMock).getParameters();
        
        parameters = new MiniBox();
        parameters.put("proliferation/RATE_G1", randomDoubleBetween(1, 10));
        parameters.put("proliferation/RATE_S", randomDoubleBetween(1, 10));
        parameters.put("proliferation/RATE_G2", randomDoubleBetween(1, 10));
        parameters.put("proliferation/RATE_M", randomDoubleBetween(1, 10));
        parameters.put("proliferation/STEPS_G1", randomIntBetween(1, 100));
        parameters.put("proliferation/STEPS_S", randomIntBetween(1, 100));
        parameters.put("proliferation/STEPS_G2", randomIntBetween(1, 100));
        parameters.put("proliferation/STEPS_M", randomIntBetween(1, 100));
        parameters.put("proliferation/CELL_GROWTH_RATE", randomDoubleBetween(1, 100));
        parameters.put("proliferation/BASAL_APOPTOSIS_RATE", randomDoubleBetween(0, 0.5));
    }
    
    @Test
    public void constructor_setsParameters() {
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        PottsModuleProliferation module = new PottsModuleProliferation.Simple(cell);
        
        assertEquals(parameters.getDouble("proliferation/RATE_G1"), module.rateG1, EPSILON);
        assertEquals(parameters.getDouble("proliferation/RATE_S"), module.rateS, EPSILON);
        assertEquals(parameters.getDouble("proliferation/RATE_G2"), module.rateG2, EPSILON);
        assertEquals(parameters.getDouble("proliferation/RATE_M"), module.rateM, EPSILON);
        assertEquals(parameters.getInt("proliferation/STEPS_G1"), module.stepsG1);
        assertEquals(parameters.getInt("proliferation/STEPS_S"), module.stepsS);
        assertEquals(parameters.getInt("proliferation/STEPS_G2"), module.stepsG2);
        assertEquals(parameters.getInt("proliferation/STEPS_M"), module.stepsM);
        assertEquals(parameters.getDouble("proliferation/CELL_GROWTH_RATE"), module.cellGrowthRate, EPSILON);
        assertEquals(parameters.getDouble("proliferation/BASAL_APOPTOSIS_RATE"), module.basalApoptosisRate, EPSILON);
    }
    
    @Test
    public void constructor_initializesFactory() {
        PottsModuleProliferation module = new PottsModuleProliferation.Simple(cellMock);
        assertNotNull(module.poissonFactory);
    }
    
    @Test
    public void getPhase_defaultConstructor_returnsValue() {
        PottsModuleProliferation module = new PottsModuleProliferation.Simple(cellMock);
        assertEquals(Phase.PROLIFERATIVE_G1, module.getPhase());
    }
    
    @Test
    public void setPhase_givenValue_setsValue() {
        Phase phase = Phase.random(RANDOM);
        PottsModuleProliferation module = new PottsModuleProliferation.Simple(cellMock);
        module.setPhase(phase);
        assertEquals(phase, module.phase);
    }
    
    @Test
    public void setPhase_givenValue_resetsSteps() {
        Phase phase = Phase.random(RANDOM);
        PottsModuleProliferation module = new PottsModuleProliferation.Simple(cellMock);
        module.currentSteps = randomIntBetween(1, 10);
        module.setPhase(phase);
        assertEquals(0, module.currentSteps);
    }
    
    @Test
    public void step_givenPhaseG1_callsMethod() {
        PottsModuleProliferation module = spy(new PottsModuleProliferation.Simple(cellMock));
        module.phase = Phase.PROLIFERATIVE_G1;
        
        module.step(random, simMock);
        verify(module).stepG1(random);
        verify(module, never()).stepS(random);
        verify(module, never()).stepG2(random);
        verify(module, never()).stepM(random, simMock);
    }
    
    @Test
    public void step_givenPhaseS_callsMethod() {
        PottsModuleProliferation module = spy(new PottsModuleProliferation.Simple(cellMock));
        module.phase = Phase.PROLIFERATIVE_S;
        
        module.step(random, simMock);
        verify(module).stepS(random);
        verify(module, never()).stepG1(random);
        verify(module, never()).stepG2(random);
        verify(module, never()).stepM(random, simMock);
    }
    
    @Test
    public void step_givenPhaseG2_callsMethod() {
        PottsModuleProliferation module = spy(new PottsModuleProliferation.Simple(cellMock));
        module.phase = Phase.PROLIFERATIVE_G2;
        
        module.step(random, simMock);
        verify(module).stepG2(random);
        verify(module, never()).stepG1(random);
        verify(module, never()).stepS(random);
        verify(module, never()).stepM(random, simMock);
    }
    
    @Test
    public void step_givenPhaseM_callsMethod() {
        PottsModuleProliferation module = spy(new PottsModuleProliferation.Simple(cellMock));
        doNothing().when(module).addCell(random, simMock);
        module.phase = Phase.PROLIFERATIVE_M;
        
        module.step(random, simMock);
        verify(module).stepM(random, simMock);
        verify(module, never()).stepG1(random);
        verify(module, never()).stepS(random);
        verify(module, never()).stepG2(random);
    }
    
    @Test
    public void step_invalidPhase_doesNothing() {
        PottsModuleProliferation module = spy(new PottsModuleProliferation.Simple(cellMock));
        module.phase = Phase.UNDEFINED;
        
        module.step(random, simMock);
        verify(module, never()).stepG1(random);
        verify(module, never()).stepS(random);
        verify(module, never()).stepG2(random);
        verify(module, never()).stepM(random, simMock);
    }
    
    @Test
    public void stepG1_withStateChange_callsMethods() {
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        PottsModuleProliferation module = spy(new PottsModuleProliferation.Simple(cell));
        module.phase = Phase.PROLIFERATIVE_G1;
        module.currentSteps = Integer.MAX_VALUE;
        
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        doReturn(module.basalApoptosisRate - EPSILON).when(random).nextDouble();
        PoissonFactory poissonFactory = mock(PoissonFactory.class);
        module.poissonFactory = poissonFactory;
        
        module.stepG1(random);
        
        verify(cell).setState(State.APOPTOTIC);
        verify(poissonFactory, never()).createPoisson(anyDouble(), eq(random));
        verify(module, never()).setPhase(any(Phase.class));
    }
    
    @Test
    public void stepG1_withoutStateChange_callsMethods() {
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        PottsModuleProliferation module = spy(new PottsModuleProliferation.Simple(cell));
        module.phase = Phase.PROLIFERATIVE_G1;
        module.currentSteps = Integer.MAX_VALUE;
        
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        doReturn(module.basalApoptosisRate + EPSILON).when(random).nextDouble();
        PoissonFactory poissonFactory = mock(PoissonFactory.class);
        doReturn(poissonMock).when(poissonFactory).createPoisson(anyDouble(), eq(random));
        module.poissonFactory = poissonFactory;
        
        module.stepG1(random);
        
        verify(cell, never()).setState(State.APOPTOTIC);
        verify(poissonFactory).createPoisson(module.rateG1, random);
        verify(module).setPhase(any(Phase.class));
    }
    
    @Test
    public void stepG1_withTransition_updatesPhase() {
        int steps = randomIntBetween(1, parameters.getInt("proliferation/STEPS_G1"));
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        PottsModuleProliferation module = spy(new PottsModuleProliferation.Simple(cell));
        module.phase = Phase.PROLIFERATIVE_G1;
        module.currentSteps = module.stepsG1 - steps;
        
        Poisson poisson = mock(Poisson.class);
        doReturn(steps).when(poisson).nextInt();
        PoissonFactory poissonFactory = mock(PoissonFactory.class);
        doReturn(poisson).when(poissonFactory).createPoisson(eq(module.rateG1), eq(random));
        module.poissonFactory = poissonFactory;
        
        module.stepG1(random);
        
        verify(module).setPhase(Phase.PROLIFERATIVE_S);
        assertEquals(Phase.PROLIFERATIVE_S, module.phase);
    }
    
    @Test
    public void stepG1_withoutTransition_maintainsPhase() {
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        PottsModuleProliferation module = spy(new PottsModuleProliferation.Simple(cell));
        module.phase = Phase.PROLIFERATIVE_G1;
        module.currentSteps = module.stepsG1;
        
        Poisson poisson = mock(Poisson.class);
        doReturn(-1).when(poisson).nextInt();
        PoissonFactory poissonFactory = mock(PoissonFactory.class);
        doReturn(poisson).when(poissonFactory).createPoisson(anyDouble(), eq(random));
        module.poissonFactory = poissonFactory;
        
        module.stepG1(random);
        
        verify(module, never()).setPhase(any(Phase.class));
        assertEquals(Phase.PROLIFERATIVE_G1, module.phase);
    }
    
    @Test
    public void stepG1_anyTransition_updatesCell() {
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        PottsModuleProliferation module = spy(new PottsModuleProliferation.Simple(cell));
        
        PoissonFactory poissonFactory = mock(PoissonFactory.class);
        doReturn(poissonMock).when(poissonFactory).createPoisson(anyDouble(), eq(random));
        module.poissonFactory = poissonFactory;
        
        module.currentSteps = Integer.MAX_VALUE;
        module.stepG1(random);
        module.currentSteps = 0;
        module.stepG1(random);
        
        verify(cell, times(2)).updateTarget(module.cellGrowthRate, 2);
    }
    
    @Test
    public void stepS_withTransition_updatesPhase() {
        int steps = randomIntBetween(1, parameters.getInt("proliferation/STEPS_S"));
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        PottsModuleProliferation module = spy(new PottsModuleProliferation.Simple(cell));
        module.phase = Phase.PROLIFERATIVE_S;
        module.currentSteps = module.stepsS - steps;
        
        Poisson poisson = mock(Poisson.class);
        doReturn(steps).when(poisson).nextInt();
        PoissonFactory poissonFactory = mock(PoissonFactory.class);
        doReturn(poisson).when(poissonFactory).createPoisson(eq(module.rateS), eq(random));
        module.poissonFactory = poissonFactory;
        
        module.stepS(random);
        
        verify(module).setPhase(Phase.PROLIFERATIVE_G2);
        assertEquals(Phase.PROLIFERATIVE_G2, module.phase);
    }
    
    @Test
    public void stepS_withoutTransition_maintainsPhase() {
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        PottsModuleProliferation module = spy(new PottsModuleProliferation.Simple(cell));
        module.phase = Phase.PROLIFERATIVE_S;
        module.currentSteps = module.stepsS;
        
        Poisson poisson = mock(Poisson.class);
        doReturn(-1).when(poisson).nextInt();
        PoissonFactory poissonFactory = mock(PoissonFactory.class);
        doReturn(poisson).when(poissonFactory).createPoisson(anyDouble(), eq(random));
        module.poissonFactory = poissonFactory;
        
        module.stepS(random);
        
        verify(module, never()).setPhase(any(Phase.class));
        assertEquals(Phase.PROLIFERATIVE_S, module.phase);
    }
    
    @Test
    public void stepS_anyTransition_updatesCell() {
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        PottsModuleProliferation module = spy(new PottsModuleProliferation.Simple(cell));
        
        PoissonFactory poissonFactory = mock(PoissonFactory.class);
        doReturn(poissonMock).when(poissonFactory).createPoisson(anyDouble(), eq(random));
        module.poissonFactory = poissonFactory;
        
        module.currentSteps = Integer.MAX_VALUE;
        module.stepS(random);
        module.currentSteps = 0;
        module.stepS(random);
        
        verify(cell, times(2)).updateTarget(module.cellGrowthRate, 2);
    }
    
    @Test
    public void stepG2_withStateChange_callsMethods() {
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        PottsModuleProliferation module = spy(new PottsModuleProliferation.Simple(cell));
        module.phase = Phase.PROLIFERATIVE_G2;
        module.currentSteps = Integer.MAX_VALUE;
        
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        doReturn(module.basalApoptosisRate - EPSILON).when(random).nextDouble();
        PoissonFactory poissonFactory = mock(PoissonFactory.class);
        module.poissonFactory = poissonFactory;
        
        module.stepG2(random);
        
        verify(cell).setState(State.APOPTOTIC);
        verify(poissonFactory, never()).createPoisson(anyDouble(), eq(random));
        verify(module, never()).setPhase(any(Phase.class));
    }
    
    @Test
    public void stepG2_withoutStateChange_callsMethods() {
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        PottsModuleProliferation module = spy(new PottsModuleProliferation.Simple(cell));
        module.phase = Phase.PROLIFERATIVE_G2;
        module.currentSteps = Integer.MAX_VALUE;
        
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        doReturn(module.basalApoptosisRate + EPSILON).when(random).nextDouble();
        PoissonFactory poissonFactory = mock(PoissonFactory.class);
        doReturn(poissonMock).when(poissonFactory).createPoisson(anyDouble(), eq(random));
        module.poissonFactory = poissonFactory;
        
        module.stepG2(random);
        
        verify(cell, never()).setState(State.APOPTOTIC);
        verify(poissonFactory).createPoisson(module.rateG2, random);
        verify(module).setPhase(any(Phase.class));
    }
    
    @Test
    public void stepG2_withTransition_updatesPhase() {
        int steps = randomIntBetween(1, parameters.getInt("proliferation/STEPS_G2"));
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        PottsModuleProliferation module = spy(new PottsModuleProliferation.Simple(cell));
        module.phase = Phase.PROLIFERATIVE_G2;
        module.currentSteps = module.stepsG2 - steps;
        
        Poisson poisson = mock(Poisson.class);
        doReturn(steps).when(poisson).nextInt();
        PoissonFactory poissonFactory = mock(PoissonFactory.class);
        doReturn(poisson).when(poissonFactory).createPoisson(eq(module.rateG2), eq(random));
        module.poissonFactory = poissonFactory;
        
        module.stepG2(random);
        
        verify(module).setPhase(Phase.PROLIFERATIVE_M);
        assertEquals(Phase.PROLIFERATIVE_M, module.phase);
    }
    
    @Test
    public void stepG2_withoutTransition_maintainsPhase() {
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        PottsModuleProliferation module = spy(new PottsModuleProliferation.Simple(cell));
        module.phase = Phase.PROLIFERATIVE_G2;
        module.currentSteps = module.stepsG2;
        
        Poisson poisson = mock(Poisson.class);
        doReturn(-1).when(poisson).nextInt();
        PoissonFactory poissonFactory = mock(PoissonFactory.class);
        doReturn(poisson).when(poissonFactory).createPoisson(anyDouble(), eq(random));
        module.poissonFactory = poissonFactory;
        
        module.stepG2(random);
        
        verify(module, never()).setPhase(any(Phase.class));
        assertEquals(Phase.PROLIFERATIVE_G2, module.phase);
    }
    
    @Test
    public void stepG2_anyTransition_updatesCell() {
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        PottsModuleProliferation module = spy(new PottsModuleProliferation.Simple(cell));
        
        PoissonFactory poissonFactory = mock(PoissonFactory.class);
        doReturn(poissonMock).when(poissonFactory).createPoisson(anyDouble(), eq(random));
        module.poissonFactory = poissonFactory;
        
        module.currentSteps = Integer.MAX_VALUE;
        module.stepG2(random);
        module.currentSteps = 0;
        module.stepG2(random);
        
        verify(cell, times(2)).updateTarget(module.cellGrowthRate, 2);
    }
    
    @Test
    public void stepM_withTransition_updatesPhase() {
        int steps = randomIntBetween(1, parameters.getInt("proliferation/STEPS_M"));
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        PottsModuleProliferation module = spy(new PottsModuleProliferation.Simple(cell));
        module.phase = Phase.PROLIFERATIVE_M;
        module.currentSteps = module.stepsM - steps;
        doNothing().when(module).addCell(random, simMock);
        
        Poisson poisson = mock(Poisson.class);
        doReturn(steps).when(poisson).nextInt();
        PoissonFactory poissonFactory = mock(PoissonFactory.class);
        doReturn(poisson).when(poissonFactory).createPoisson(eq(module.rateM), eq(random));
        module.poissonFactory = poissonFactory;
        
        module.stepM(random, simMock);
        
        verify(module).addCell(random, simMock);
        verify(module).setPhase(Phase.PROLIFERATIVE_G1);
        assertEquals(Phase.PROLIFERATIVE_G1, module.phase);
    }
    
    @Test
    public void stepM_withoutTransition_maintainsPhase() {
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        PottsModuleProliferation module = spy(new PottsModuleProliferation.Simple(cell));
        module.phase = Phase.PROLIFERATIVE_M;
        module.currentSteps = module.stepsM;
        doNothing().when(module).addCell(random, simMock);
        
        Poisson poisson = mock(Poisson.class);
        doReturn(-1).when(poisson).nextInt();
        PoissonFactory poissonFactory = mock(PoissonFactory.class);
        doReturn(poisson).when(poissonFactory).createPoisson(anyDouble(), eq(random));
        module.poissonFactory = poissonFactory;
        
        module.stepM(random, simMock);
    
        verify(module, never()).addCell(random, simMock);
        verify(module, never()).setPhase(any(Phase.class));
        assertEquals(Phase.PROLIFERATIVE_M, module.phase);
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
