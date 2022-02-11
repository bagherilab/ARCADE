package arcade.potts.agent.module;

import org.junit.BeforeClass;
import org.junit.Test;
import sim.util.distribution.Poisson;
import ec.util.MersenneTwisterFast;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.env.loc.PottsLocations;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.core.util.Enums.Region;
import static arcade.core.util.Enums.State;
import static arcade.potts.agent.module.PottsModule.PoissonFactory;
import static arcade.potts.agent.module.PottsModuleProliferationSimple.*;
import static arcade.potts.util.PottsEnums.Phase;

public class PottsModuleProliferationSimpleTest {
    private static final double EPSILON = 1E-10;
    private static final double R = 1.0;
    private static final int N = 0;
    static MersenneTwisterFast random;
    static Poisson poissonMock;
    static PottsSimulation simMock;
    static MiniBox parameters;
    static Potts pottsMock;
    
    @BeforeClass
    public static void setupMocks() {
        random = mock(MersenneTwisterFast.class);
        doReturn(R).when(random).nextDouble();
        
        poissonMock = mock(Poisson.class);
        doReturn(N).when(poissonMock).nextInt();
    
        pottsMock = mock(Potts.class);
        
        simMock = mock(PottsSimulation.class);
        doReturn(pottsMock).when(simMock).getPotts();
        
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
        parameters.put("proliferation/NUCLEAR_GROWTH_RATE", randomDoubleBetween(1, 100));
        parameters.put("proliferation/BASAL_APOPTOSIS_RATE", randomDoubleBetween(0, 0.5));
        parameters.put("proliferation/NUCLEUS_CONDENSATION_FRACTION", randomDoubleBetween(0.25, 0.75));
    }
    
    @Test
    public void constructor_setsParameters() {
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        PottsModuleProliferationSimple module = new PottsModuleProliferationSimple(cell);
        
        assertEquals(parameters.getDouble("proliferation/RATE_G1"), module.rateG1, EPSILON);
        assertEquals(parameters.getDouble("proliferation/RATE_S"), module.rateS, EPSILON);
        assertEquals(parameters.getDouble("proliferation/RATE_G2"), module.rateG2, EPSILON);
        assertEquals(parameters.getDouble("proliferation/RATE_M"), module.rateM, EPSILON);
        assertEquals(parameters.getInt("proliferation/STEPS_G1"), module.stepsG1);
        assertEquals(parameters.getInt("proliferation/STEPS_S"), module.stepsS);
        assertEquals(parameters.getInt("proliferation/STEPS_G2"), module.stepsG2);
        assertEquals(parameters.getInt("proliferation/STEPS_M"), module.stepsM);
        assertEquals(parameters.getDouble("proliferation/CELL_GROWTH_RATE"), module.cellGrowthRate, EPSILON);
        assertEquals(parameters.getDouble("proliferation/NUCLEUS_GROWTH_RATE"), module.nucleusGrowthRate, EPSILON);
        assertEquals(parameters.getDouble("proliferation/BASAL_APOPTOSIS_RATE"), module.basalApoptosisRate, EPSILON);
        assertEquals(parameters.getDouble("proliferation/NUCLEUS_CONDENSATION_FRACTION"),
                module.nucleusCondFraction, EPSILON);
    }
    
    @Test
    public void stepG1_withStateChange_callsMethods() {
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        PottsModuleProliferationSimple module = spy(new PottsModuleProliferationSimple(cell));
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
        PottsModuleProliferationSimple module = spy(new PottsModuleProliferationSimple(cell));
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
        PottsModuleProliferationSimple module = spy(new PottsModuleProliferationSimple(cell));
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
        PottsModuleProliferationSimple module = spy(new PottsModuleProliferationSimple(cell));
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
        PottsModuleProliferationSimple module = spy(new PottsModuleProliferationSimple(cell));
        
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
    public void stepG1_anyTransitionWithRegionOverThreshold_updatesCell() {
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        doReturn(true).when(cell).hasRegions();
        
        int criticalVolume = randomIntBetween(100, 1000);
        doReturn((double) criticalVolume).when(cell).getCriticalVolume(Region.NUCLEUS);
        doReturn(criticalVolume + 1).when(cell).getVolume(Region.NUCLEUS);
        
        PottsModuleProliferationSimple module = spy(new PottsModuleProliferationSimple(cell));
        
        PoissonFactory poissonFactory = mock(PoissonFactory.class);
        doReturn(poissonMock).when(poissonFactory).createPoisson(anyDouble(), eq(random));
        module.poissonFactory = poissonFactory;
        
        module.currentSteps = Integer.MAX_VALUE;
        module.stepG1(random);
        module.currentSteps = 0;
        module.stepG1(random);
        
        verify(cell, times(2)).updateTarget(Region.NUCLEUS, module.nucleusGrowthRate, 2);
    }
    
    @Test
    public void stepG1_anyTransitionWithRegionUnderThreshold_updatesCell() {
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        doReturn(true).when(cell).hasRegions();
        
        int criticalVolume = randomIntBetween(100, 1000);
        doReturn((double) criticalVolume).when(cell).getCriticalVolume(Region.NUCLEUS);
        doReturn(criticalVolume - 1).when(cell).getVolume(Region.NUCLEUS);
        
        PottsModuleProliferationSimple module = spy(new PottsModuleProliferationSimple(cell));
        
        PoissonFactory poissonFactory = mock(PoissonFactory.class);
        doReturn(poissonMock).when(poissonFactory).createPoisson(anyDouble(), eq(random));
        module.poissonFactory = poissonFactory;
        
        module.currentSteps = Integer.MAX_VALUE;
        module.stepG1(random);
        module.currentSteps = 0;
        module.stepG1(random);
    
        verify(cell, never()).updateTarget(eq(Region.NUCLEUS), anyDouble(), anyDouble());
    }
    
    @Test
    public void stepS_withTransition_updatesPhase() {
        int steps = randomIntBetween(1, parameters.getInt("proliferation/STEPS_S"));
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        PottsModuleProliferationSimple module = spy(new PottsModuleProliferationSimple(cell));
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
        PottsModuleProliferationSimple module = spy(new PottsModuleProliferationSimple(cell));
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
        PottsModuleProliferationSimple module = spy(new PottsModuleProliferationSimple(cell));
        
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
    public void stepS_anyTransitionWithRegion_updatesCell() {
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        doReturn(true).when(cell).hasRegions();
        PottsModuleProliferationSimple module = spy(new PottsModuleProliferationSimple(cell));
        
        PoissonFactory poissonFactory = mock(PoissonFactory.class);
        doReturn(poissonMock).when(poissonFactory).createPoisson(anyDouble(), eq(random));
        module.poissonFactory = poissonFactory;
        
        module.currentSteps = Integer.MAX_VALUE;
        module.stepS(random);
        module.currentSteps = 0;
        module.stepS(random);
        
        verify(cell, times(2)).updateTarget(Region.NUCLEUS, module.nucleusGrowthRate, 2);
    }
    
    @Test
    public void stepG2_withStateChange_callsMethods() {
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        PottsModuleProliferationSimple module = spy(new PottsModuleProliferationSimple(cell));
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
        PottsModuleProliferationSimple module = spy(new PottsModuleProliferationSimple(cell));
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
    public void stepG2_withTransitionNotArrested_updatesPhase() {
        int steps = randomIntBetween(1, parameters.getInt("proliferation/STEPS_G2"));
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        double volume = randomDoubleBetween(0, 100);
        doReturn((int) (volume * SIZE_CHECKPOINT) + 1).when(cell).getVolume();
        doReturn(volume).when(cell).getCriticalVolume();
        
        PottsModuleProliferationSimple module = spy(new PottsModuleProliferationSimple(cell));
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
    public void stepG2_withoutTransitionNotArrested_maintainsPhase() {
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        double volume = randomDoubleBetween(0, 100);
        doReturn((int) (volume * SIZE_CHECKPOINT) + 1).when(cell).getVolume();
        doReturn(volume).when(cell).getCriticalVolume();
        
        PottsModuleProliferationSimple module = spy(new PottsModuleProliferationSimple(cell));
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
    public void stepG2_withTransitionArrested_maintainsPhase() {
        int steps = randomIntBetween(1, parameters.getInt("proliferation/STEPS_G2"));
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        double volume = randomDoubleBetween(0, 100);
        doReturn((int) (volume * SIZE_CHECKPOINT) - 1).when(cell).getVolume();
        doReturn(volume).when(cell).getCriticalVolume();
        
        PottsModuleProliferationSimple module = spy(new PottsModuleProliferationSimple(cell));
        module.phase = Phase.PROLIFERATIVE_G2;
        module.currentSteps = module.stepsG2 - steps;
        
        Poisson poisson = mock(Poisson.class);
        doReturn(steps).when(poisson).nextInt();
        PoissonFactory poissonFactory = mock(PoissonFactory.class);
        doReturn(poisson).when(poissonFactory).createPoisson(eq(module.rateG2), eq(random));
        module.poissonFactory = poissonFactory;
        
        module.stepG2(random);
        
        verify(module, never()).setPhase(any(Phase.class));
        assertEquals(Phase.PROLIFERATIVE_G2, module.phase);
    }
    
    @Test
    public void stepM_withoutTransitionArrested_maintainPhase() {
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        double volume = randomDoubleBetween(0, 100);
        doReturn((int) (volume * SIZE_CHECKPOINT) - 1).when(cell).getVolume();
        doReturn(volume).when(cell).getCriticalVolume();
        
        PottsModuleProliferationSimple module = spy(new PottsModuleProliferationSimple(cell));
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
        PottsModuleProliferationSimple module = spy(new PottsModuleProliferationSimple(cell));
        
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
    public void stepG2_anyTransitionWithRegion_updatesCell() {
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        doReturn(true).when(cell).hasRegions();
        PottsModuleProliferationSimple module = spy(new PottsModuleProliferationSimple(cell));
        
        PoissonFactory poissonFactory = mock(PoissonFactory.class);
        doReturn(poissonMock).when(poissonFactory).createPoisson(anyDouble(), eq(random));
        module.poissonFactory = poissonFactory;
        
        module.currentSteps = Integer.MAX_VALUE;
        module.stepG2(random);
        module.currentSteps = 0;
        module.stepG2(random);
        
        verify(cell, times(2)).updateTarget(Region.NUCLEUS, module.nucleusGrowthRate, 2);
    }
    
    @Test
    public void stepM_withTransition_updatesPhase() {
        int steps = randomIntBetween(1, parameters.getInt("proliferation/STEPS_M"));
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        PottsModuleProliferationSimple module = spy(new PottsModuleProliferationSimple(cell));
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
        PottsModuleProliferationSimple module = spy(new PottsModuleProliferationSimple(cell));
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
    public void stepM_anyTransition_updatesCell() {
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        PottsModuleProliferationSimple module = spy(new PottsModuleProliferationSimple(cell));
        doNothing().when(module).addCell(random, simMock);
        
        PoissonFactory poissonFactory = mock(PoissonFactory.class);
        doReturn(poissonMock).when(poissonFactory).createPoisson(anyDouble(), eq(random));
        module.poissonFactory = poissonFactory;
        
        module.currentSteps = Integer.MAX_VALUE;
        module.stepM(random, simMock);
        module.currentSteps = 0;
        module.stepM(random, simMock);
        
        verify(cell, times(2)).updateTarget(module.cellGrowthRate, 2);
    }
    
    @Test
    public void stepM_withRegionOverThreshold_doesNotUpdateCell() {
        PottsCell cell = mock(PottsCell.class);
        int cellID = randomIntBetween(1, 10);
        doReturn(cellID).when(cell).getID();
        doReturn(parameters).when(cell).getParameters();
        doReturn(true).when(cell).hasRegions();
        
        int criticalVolume = randomIntBetween(100, 1000);
        doReturn((double) criticalVolume).when(cell).getCriticalVolume(Region.NUCLEUS);
        doReturn(criticalVolume - 1).when(cell).getVolume(Region.NUCLEUS);
        
        PottsLocations loc = mock(PottsLocations.class);
        doReturn(loc).when(cell).getLocation();
        
        int[][][] ids = new int[0][0][0];
        int[][][] regions = new int[0][0][0];
        pottsMock.ids = ids;
        pottsMock.regions = regions;
        
        PottsModuleProliferationSimple module = spy(new PottsModuleProliferationSimple(cell));
        doNothing().when(module).addCell(random, simMock);
        
        PoissonFactory poissonFactory = mock(PoissonFactory.class);
        doReturn(poissonMock).when(poissonFactory).createPoisson(anyDouble(), eq(random));
        module.poissonFactory = poissonFactory;
        
        module.currentSteps = Integer.MAX_VALUE;
        module.stepM(random, simMock);
        module.currentSteps = 0;
        module.stepM(random, simMock);
        
        double nucleusCondFraction = parameters.getDouble("proliferation/NUCLEUS_CONDENSATION_FRACTION");
        int target = (int) (nucleusCondFraction * criticalVolume);
        
        verify(loc, never()).distribute(Region.NUCLEUS, target, random);
        verify(loc, never()).update(cellID, ids, regions);
        verify(cell, never()).setTargets(eq(Region.DEFAULT), anyDouble(), anyDouble());
        verify(cell, never()).setTargets(eq(Region.NUCLEUS), anyDouble(), anyDouble());
    }
    
    @Test
    public void stepM_withRegionUnderThreshold_updatesCell() {
        PottsCell cell = mock(PottsCell.class);
        int cellID = randomIntBetween(1, 10);
        doReturn(cellID).when(cell).getID();
        doReturn(parameters).when(cell).getParameters();
        doReturn(true).when(cell).hasRegions();
        
        int criticalVolume = randomIntBetween(100, 1000);
        doReturn((double) criticalVolume).when(cell).getCriticalVolume(Region.NUCLEUS);
        doReturn(criticalVolume + 1).when(cell).getVolume(Region.NUCLEUS);
        
        PottsLocations loc = mock(PottsLocations.class);
        doReturn(loc).when(cell).getLocation();
        
        int[][][] ids = new int[0][0][0];
        int[][][] regions = new int[0][0][0];
        pottsMock.ids = ids;
        pottsMock.regions = regions;
        
        PottsModuleProliferationSimple module = spy(new PottsModuleProliferationSimple(cell));
        doNothing().when(module).addCell(random, simMock);
        
        PoissonFactory poissonFactory = mock(PoissonFactory.class);
        doReturn(poissonMock).when(poissonFactory).createPoisson(anyDouble(), eq(random));
        module.poissonFactory = poissonFactory;
        
        module.currentSteps = Integer.MAX_VALUE;
        module.stepM(random, simMock);
        module.currentSteps = 0;
        module.stepM(random, simMock);
        
        double nucleusCondFraction = parameters.getDouble("proliferation/NUCLEUS_CONDENSATION_FRACTION");
        int target = (int) (nucleusCondFraction * criticalVolume);
        
        verify(loc, times(2)).distribute(Region.NUCLEUS, target, random);
        verify(loc, times(2)).update(cellID, ids, regions);
        verify(cell, times(2)).setTargets(eq(Region.DEFAULT), anyDouble(), anyDouble());
        verify(cell, times(2)).setTargets(eq(Region.NUCLEUS), anyDouble(), anyDouble());
    }
}
