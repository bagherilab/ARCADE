package arcade.potts.agent.module;

import org.junit.BeforeClass;
import org.junit.Test;
import sim.util.distribution.Poisson;
import ec.util.MersenneTwisterFast;
import arcade.core.env.grid.Grid;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.core.util.Enums.Region;
import static arcade.potts.agent.module.PottsModuleApoptosisSimple.*;
import static arcade.potts.util.PottsEnums.Phase;

public class PottsModuleApoptosisSimpleTest {
    private static final double EPSILON = 1E-10;
    
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
        when(simMock.getPotts()).thenReturn(mock(Potts.class));
        when(simMock.getGrid()).thenReturn(mock(Grid.class));
        
        cellMock = mock(PottsCell.class);
        MiniBox box = mock(MiniBox.class);
        doReturn(0.).when(box).getDouble(anyString());
        doReturn(box).when(cellMock).getParameters();
        
        parameters = new MiniBox();
        parameters.put("apoptosis/RATE_EARLY", randomDoubleBetween(1, 10));
        parameters.put("apoptosis/RATE_LATE", randomDoubleBetween(1, 10));
        parameters.put("apoptosis/STEPS_EARLY", randomIntBetween(1, 100));
        parameters.put("apoptosis/STEPS_LATE", randomIntBetween(1, 100));
    }
    
    @Test
    public void constructor_setsParameters() {
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        PottsModuleApoptosisSimple module = new PottsModuleApoptosisSimple(cell);
        
        assertEquals(parameters.getDouble("apoptosis/RATE_EARLY"), module.rateEarly, EPSILON);
        assertEquals(parameters.getDouble("apoptosis/RATE_LATE"), module.rateLate, EPSILON);
        assertEquals(parameters.getDouble("apoptosis/STEPS_EARLY"), module.stepsEarly, EPSILON);
        assertEquals(parameters.getDouble("apoptosis/STEPS_LATE"), module.stepsLate, EPSILON);
    }
    
    @Test
    public void stepEarly_withTransition_updatesPhase() {
        int steps = randomIntBetween(1, parameters.getInt("apoptosis/STEPS_EARLY"));
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        PottsModuleApoptosisSimple module = spy(new PottsModuleApoptosisSimple(cell));
        module.phase = Phase.APOPTOTIC_EARLY;
        module.currentSteps = module.stepsEarly - steps;
        
        Poisson poisson = mock(Poisson.class);
        doReturn(steps).when(poisson).nextInt();
        PoissonFactory poissonFactory = mock(PoissonFactory.class);
        doReturn(poisson).when(poissonFactory).createPoisson(eq(module.rateEarly), eq(random));
        module.poissonFactory = poissonFactory;
        
        module.stepEarly(random);
        
        verify(module).setPhase(Phase.APOPTOTIC_LATE);
        assertEquals(Phase.APOPTOTIC_LATE, module.phase);
    }
    
    @Test
    public void stepEarly_withoutTransition_maintainsPhase() {
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        PottsModuleApoptosisSimple module = spy(new PottsModuleApoptosisSimple(cell));
        module.phase = Phase.APOPTOTIC_EARLY;
        module.currentSteps = module.stepsEarly;
        
        Poisson poisson = mock(Poisson.class);
        doReturn(-1).when(poisson).nextInt();
        PoissonFactory poissonFactory = mock(PoissonFactory.class);
        doReturn(poisson).when(poissonFactory).createPoisson(anyDouble(), eq(random));
        module.poissonFactory = poissonFactory;
        
        module.stepEarly(random);
        
        verify(module, never()).setPhase(any(Phase.class));
        assertEquals(Phase.APOPTOTIC_EARLY, module.phase);
    }
    
    @Test
    public void stepEarly_anyTransition_updatesCell() {
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        PottsModuleApoptosisSimple module = spy(new PottsModuleApoptosisSimple(cell));
        
        PoissonFactory poissonFactory = mock(PoissonFactory.class);
        doReturn(poissonMock).when(poissonFactory).createPoisson(anyDouble(), eq(random));
        module.poissonFactory = poissonFactory;
        
        module.currentSteps = Integer.MAX_VALUE;
        module.stepEarly(random);
        module.currentSteps = 0;
        module.stepEarly(random);
        
        verify(cell, times(2)).updateTarget(module.waterLossRate, EARLY_SIZE_CHECKPOINT);
    }
    
    @Test
    public void stepEarly_anyTransitionWithRegion_updatesCell() {
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        doReturn(true).when(cell).hasRegions();
        PottsModuleApoptosisSimple module = spy(new PottsModuleApoptosisSimple(cell));
        
        PoissonFactory poissonFactory = mock(PoissonFactory.class);
        doReturn(poissonMock).when(poissonFactory).createPoisson(anyDouble(), eq(random));
        module.poissonFactory = poissonFactory;
        
        module.currentSteps = Integer.MAX_VALUE;
        module.stepEarly(random);
        module.currentSteps = 0;
        module.stepEarly(random);
        
        verify(cell, times(2)).updateTarget(Region.NUCLEUS, module.nucleusPyknosisRate, EARLY_SIZE_CHECKPOINT);
    }
    
    @Test
    public void stepLate_withTransitionNotArrested_updatesPhase() {
        int steps = randomIntBetween(1, parameters.getInt("apoptosis/STEPS_LATE"));
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        double volume = randomDoubleBetween(0, 100);
        doReturn((volume * LATE_SIZE_CHECKPOINT) - 1).when(cell).getVolume();
        doReturn(volume).when(cell).getCriticalVolume();
        
        PottsModuleApoptosisSimple module = spy(new PottsModuleApoptosisSimple(cell));
        module.phase = Phase.APOPTOTIC_LATE;
        module.currentSteps = module.stepsLate - steps;
        doNothing().when(module).removeCell(simMock);
        
        Poisson poisson = mock(Poisson.class);
        doReturn(steps).when(poisson).nextInt();
        PoissonFactory poissonFactory = mock(PoissonFactory.class);
        doReturn(poisson).when(poissonFactory).createPoisson(eq(module.rateLate), eq(random));
        module.poissonFactory = poissonFactory;
        
        module.stepLate(random, simMock);
        
        verify(module).removeCell(simMock);
        verify(module).setPhase(Phase.APOPTOSED);
        assertEquals(Phase.APOPTOSED, module.phase);
    }
    
    @Test
    public void stepLate_withoutTransitionNotArrested_maintainsPhase() {
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        double volume = randomDoubleBetween(0, 100);
        doReturn((volume * LATE_SIZE_CHECKPOINT) - 1).when(cell).getVolume();
        doReturn(volume).when(cell).getCriticalVolume();
        
        PottsModuleApoptosisSimple module = spy(new PottsModuleApoptosisSimple(cell));
        module.phase = Phase.APOPTOTIC_LATE;
        module.currentSteps = module.stepsLate;
        doNothing().when(module).removeCell(simMock);
        
        Poisson poisson = mock(Poisson.class);
        doReturn(-1).when(poisson).nextInt();
        PoissonFactory poissonFactory = mock(PoissonFactory.class);
        doReturn(poisson).when(poissonFactory).createPoisson(anyDouble(), eq(random));
        module.poissonFactory = poissonFactory;
        
        module.stepLate(random, simMock);
        
        verify(module, never()).removeCell(simMock);
        verify(module, never()).setPhase(any(Phase.class));
        assertEquals(Phase.APOPTOTIC_LATE, module.phase);
    }
    
    @Test
    public void stepLate_withTransitionArrested_maintainsPhase() {
        int steps = randomIntBetween(1, parameters.getInt("apoptosis/STEPS_LATE"));
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        double volume = randomDoubleBetween(0, 100);
        doReturn((volume * LATE_SIZE_CHECKPOINT) + 1).when(cell).getVolume();
        doReturn(volume).when(cell).getCriticalVolume();
        
        PottsModuleApoptosisSimple module = spy(new PottsModuleApoptosisSimple(cell));
        module.phase = Phase.APOPTOTIC_LATE;
        module.currentSteps = module.stepsLate - steps;
        doNothing().when(module).removeCell(simMock);
        
        Poisson poisson = mock(Poisson.class);
        doReturn(steps).when(poisson).nextInt();
        PoissonFactory poissonFactory = mock(PoissonFactory.class);
        doReturn(poisson).when(poissonFactory).createPoisson(eq(module.rateLate), eq(random));
        module.poissonFactory = poissonFactory;
        
        module.stepLate(random, simMock);
        
        verify(module, never()).removeCell(simMock);
        verify(module, never()).setPhase(any(Phase.class));
        assertEquals(Phase.APOPTOTIC_LATE, module.phase);
    }
    
    @Test
    public void stepLate_withoutTransitionArrested_maintainsPhase() {
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        double volume = randomDoubleBetween(0, 100);
        doReturn((volume * LATE_SIZE_CHECKPOINT) + 1).when(cell).getVolume();
        doReturn(volume).when(cell).getCriticalVolume();
        
        PottsModuleApoptosisSimple module = spy(new PottsModuleApoptosisSimple(cell));
        module.phase = Phase.APOPTOTIC_LATE;
        module.currentSteps = module.stepsLate;
        doNothing().when(module).removeCell(simMock);
        
        Poisson poisson = mock(Poisson.class);
        doReturn(-1).when(poisson).nextInt();
        PoissonFactory poissonFactory = mock(PoissonFactory.class);
        doReturn(poisson).when(poissonFactory).createPoisson(anyDouble(), eq(random));
        module.poissonFactory = poissonFactory;
        
        module.stepLate(random, simMock);
        
        verify(module, never()).removeCell(simMock);
        verify(module, never()).setPhase(any(Phase.class));
        assertEquals(Phase.APOPTOTIC_LATE, module.phase);
    }
    
    @Test
    public void stepLate_anyTransition_updatesCell() {
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        PottsModuleApoptosisSimple module = spy(new PottsModuleApoptosisSimple(cell));
        doNothing().when(module).removeCell(simMock);
        
        PoissonFactory poissonFactory = mock(PoissonFactory.class);
        doReturn(poissonMock).when(poissonFactory).createPoisson(anyDouble(), eq(random));
        module.poissonFactory = poissonFactory;
        
        module.currentSteps = Integer.MAX_VALUE;
        module.stepLate(random, simMock);
        module.currentSteps = 0;
        module.stepLate(random, simMock);
        
        verify(cell, times(2)).updateTarget(module.cytoBlebbingRate, LATE_SIZE_CHECKPOINT);
    }
    
    @Test
    public void stepLate_anyTransitionWithRegion_updatesCell() {
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        doReturn(true).when(cell).hasRegions();
        PottsModuleApoptosisSimple module = spy(new PottsModuleApoptosisSimple(cell));
        doNothing().when(module).removeCell(simMock);
        
        PoissonFactory poissonFactory = mock(PoissonFactory.class);
        doReturn(poissonMock).when(poissonFactory).createPoisson(anyDouble(), eq(random));
        module.poissonFactory = poissonFactory;
        
        module.currentSteps = Integer.MAX_VALUE;
        module.stepLate(random, simMock);
        module.currentSteps = 0;
        module.stepLate(random, simMock);
        
        verify(cell, times(2)).updateTarget(Region.NUCLEUS, module.nucleusFragmentationRate, 0);
    }
}
