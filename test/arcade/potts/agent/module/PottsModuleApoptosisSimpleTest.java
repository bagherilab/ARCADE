package arcade.potts.agent.module;

import org.junit.BeforeClass;
import org.junit.Test;
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
import static arcade.potts.agent.module.PottsModuleApoptosis.*;
import static arcade.potts.util.PottsEnums.Phase;

public class PottsModuleApoptosisSimpleTest {
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
        PottsModuleApoptosis module = new PottsModuleApoptosis.Simple(cell);
        
        assertEquals(parameters.getDouble("apoptosis/RATE_EARLY"), module.rateEarly, EPSILON);
        assertEquals(parameters.getDouble("apoptosis/RATE_LATE"), module.rateLate, EPSILON);
        assertEquals(parameters.getDouble("apoptosis/STEPS_EARLY"), module.stepsEarly, EPSILON);
        assertEquals(parameters.getDouble("apoptosis/STEPS_LATE"), module.stepsLate, EPSILON);
    }
    
    @Test
    public void constructor_initializesFactory() {
        PottsModuleApoptosis module = new PottsModuleApoptosis.Simple(cellMock);
        assertNotNull(module.poissonFactory);
    }
    
    @Test
    public void getPhase_defaultConstructor_returnsValue() {
        PottsModuleApoptosis module = new PottsModuleApoptosis.Simple(cellMock);
        assertEquals(Phase.APOPTOTIC_EARLY, module.getPhase());
    }
    
    @Test
    public void setPhase_givenValue_setsValue() {
        Phase phase = Phase.random(RANDOM);
        PottsModuleApoptosis module = new PottsModuleApoptosis.Simple(cellMock);
        module.setPhase(phase);
        assertEquals(phase, module.phase);
    }
    
    @Test
    public void setPhase_givenValue_resetsSteps() {
        Phase phase = Phase.random(RANDOM);
        PottsModuleApoptosis module = new PottsModuleApoptosis.Simple(cellMock);
        module.currentSteps = randomIntBetween(1, 10);
        module.setPhase(phase);
        assertEquals(0, module.currentSteps);
    }
    
    @Test
    public void step_givenPhaseEarly_callsMethod() {
        PottsModuleApoptosis module = spy(new PottsModuleApoptosis.Simple(cellMock));
        module.phase = Phase.APOPTOTIC_EARLY;
        
        module.step(random, simMock);
        verify(module).stepEarly(random);
        verify(module, never()).stepLate(random, simMock);
    }
    
    @Test
    public void step_givenPhaseLate_callsMethod() {
        PottsModuleApoptosis module = spy(new PottsModuleApoptosis.Simple(cellMock));
        doNothing().when(module).removeCell(simMock);
        module.phase = Phase.APOPTOTIC_LATE;
        
        module.step(random, simMock);
        verify(module).stepLate(random, simMock);
        verify(module, never()).stepEarly(random);
    }
    
    @Test
    public void step_invalidPhase_doesNothing() {
        PottsModuleApoptosis module = spy(new PottsModuleApoptosis.Simple(cellMock));
        module.phase = Phase.UNDEFINED;
        
        module.step(random, simMock);
        verify(module, never()).stepLate(random, simMock);
        verify(module, never()).stepEarly(random);
    }
    
    @Test
    public void stepEarly_withTransition_updatesPhase() {
        int steps = randomIntBetween(1, parameters.getInt("apoptosis/STEPS_EARLY"));
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        PottsModuleApoptosis module = spy(new PottsModuleApoptosis.Simple(cell));
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
        PottsModuleApoptosis module = spy(new PottsModuleApoptosis.Simple(cell));
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
        PottsModuleApoptosis module = spy(new PottsModuleApoptosis.Simple(cell));
        
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
    public void stepLate_withTransitionNotArrested_updatesPhase() {
        int steps = randomIntBetween(1, parameters.getInt("apoptosis/STEPS_LATE"));
        PottsCell cell = mock(PottsCell.class);
        doReturn(parameters).when(cell).getParameters();
        double volume = randomDoubleBetween(0, 100);
        doReturn((int) (volume * LATE_SIZE_CHECKPOINT) - 1).when(cell).getVolume();
        doReturn(volume).when(cell).getCriticalVolume();
        
        PottsModuleApoptosis module = spy(new PottsModuleApoptosis.Simple(cell));
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
        doReturn((int) (volume * LATE_SIZE_CHECKPOINT) - 1).when(cell).getVolume();
        doReturn(volume).when(cell).getCriticalVolume();
        
        PottsModuleApoptosis module = spy(new PottsModuleApoptosis.Simple(cell));
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
        doReturn((int) (volume * LATE_SIZE_CHECKPOINT) + 1).when(cell).getVolume();
        doReturn(volume).when(cell).getCriticalVolume();
        
        PottsModuleApoptosis module = spy(new PottsModuleApoptosis.Simple(cell));
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
        doReturn((int) (volume * LATE_SIZE_CHECKPOINT) + 1).when(cell).getVolume();
        doReturn(volume).when(cell).getCriticalVolume();
        
        PottsModuleApoptosis module = spy(new PottsModuleApoptosis.Simple(cell));
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
        PottsModuleApoptosis module = spy(new PottsModuleApoptosis.Simple(cell));
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
    public void removeCell_called_removeObject() {
        PottsCell cell = mock(PottsCell.class);
        MiniBox box = mock(MiniBox.class);
        doReturn(0.).when(box).getDouble(anyString());
        doReturn(box).when(cell).getParameters();
        
        PottsLocation location = mock(PottsLocation.class);
        Potts potts = mock(Potts.class);
        Grid grid = mock(Grid.class);
        PottsSimulation sim = mock(PottsSimulation.class);
        
        int id = randomIntBetween(1, 100);
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
        verify(potts).deregister(cell);
        verify(cell).stop();
    }
}
