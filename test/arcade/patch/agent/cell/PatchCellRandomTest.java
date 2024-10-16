package arcade.patch.agent.cell;

import org.junit.jupiter.api.Test;
import ec.util.MersenneTwisterFast;
import arcade.core.env.location.*;
import arcade.core.util.MiniBox;
import arcade.patch.agent.module.PatchModule;
import arcade.patch.agent.process.PatchProcessMetabolism;
import arcade.patch.agent.process.PatchProcessMetabolismMedium;
import arcade.patch.agent.process.PatchProcessSignalingSimple;
import arcade.patch.env.location.PatchLocation;
import arcade.patch.sim.PatchSimulation;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.patch.util.PatchEnums.Domain;
import static arcade.patch.util.PatchEnums.State;

public class PatchCellRandomTest {
    private static final double EPSILON = 1E-8;
    
    static PatchLocation locationMock = mock(PatchLocation.class);
    
    static int cellID = randomIntBetween(1, 10);
    
    static int cellParent = randomIntBetween(1, 10);
    
    static int cellPop = randomIntBetween(1, 10);
    
    static int cellAge = randomIntBetween(1, 1000);
    
    static int cellDivisions = randomIntBetween(1, 100);
    
    static double cellVolume = randomDoubleBetween(10, 100);
    
    static double cellHeight = randomDoubleBetween(10, 100);
    
    static double cellCriticalVolume = randomDoubleBetween(10, 100);
    
    static double cellCriticalHeight = randomDoubleBetween(10, 100);
    
    static State cellState = State.QUIESCENT;
    
    static MiniBox parametersMock = new MiniBox();
    
    @Test
    public void make_called_setsFields() {
        double volume = randomDoubleBetween(10, 100);
        double height = randomDoubleBetween(10, 100);
        double criticalVolume = randomDoubleBetween(10, 100);
        double criticalHeight = randomDoubleBetween(10, 100);
        MiniBox parameters = new MiniBox();
        
        String metabolism = "medium";
        String signaling = "simple";
        parameters.put("(PROCESS)/" + Domain.METABOLISM, metabolism);
        parameters.put("(PROCESS)/" + Domain.SIGNALING, signaling);
        
        State state1 = State.QUIESCENT;
        State state2 = State.PROLIFERATIVE;
        Location location1 = mock(PatchLocation.class);
        Location location2 = mock(PatchLocation.class);
        
        PatchCellRandom cell1 = new PatchCellRandom(cellID, cellParent, cellPop, state1, cellAge, cellDivisions,
                location1, parameters, volume, height, criticalVolume, criticalHeight);
        PatchCellRandom cell2 = (PatchCellRandom) cell1.make(cellID + 1, state2, location2, null);
        
        assertEquals(cellID + 1, cell2.id);
        assertEquals(cellID, cell2.parent);
        assertEquals(cellPop, cell2.pop);
        assertEquals(cellAge, cell2.getAge());
        assertEquals(cellDivisions - 1, cell1.getDivisions());
        assertEquals(cellDivisions - 1, cell2.getDivisions());
        assertEquals(location2, cell2.getLocation());
        assertEquals(cell2.parameters, parameters);
        assertEquals(volume, cell2.getVolume(), EPSILON);
        assertEquals(height, cell2.getHeight(), EPSILON);
        assertEquals(criticalVolume, cell2.getCriticalVolume(), EPSILON);
        assertEquals(criticalHeight, cell2.getCriticalHeight(), EPSILON);
        assertTrue(cell2.getProcess(Domain.METABOLISM) instanceof PatchProcessMetabolismMedium);
        assertTrue(cell2.getProcess(Domain.SIGNALING) instanceof PatchProcessSignalingSimple);
    }
    
    @Test
    public void step_calledWithUndefinedState_setsRandomState() {
        PatchSimulation sim = mock(PatchSimulation.class);
        PatchCellRandom cell = spy(new PatchCellRandom(cellID, cellParent, cellPop,
                cellState, cellAge, cellDivisions, locationMock, parametersMock,
                cellVolume, cellHeight, cellCriticalVolume, cellCriticalHeight));
        PatchModule module = mock(PatchModule.class);
        
        cell.processes.put(Domain.METABOLISM, mock(PatchProcessMetabolism.class));
        cell.processes.put(Domain.SIGNALING, mock(PatchProcessMetabolism.class));
        
        int numValidStates = State.values().length - 1;
        
        for (int i = 1; i < numValidStates + 1; i++) {
            MersenneTwisterFast random = mock(MersenneTwisterFast.class);
            doReturn(i - 1).when(random).nextInt(numValidStates);
            doAnswer(invocationOnMock -> {
                cell.state = invocationOnMock.getArgument(0);
                cell.module = module;
                return null;
            }).when(cell).setState(any(State.class));
            
            State state = State.values()[i];
            sim.random = random;
            cell.setState(State.UNDEFINED);
            cell.step(sim);
            
            assertEquals(state, cell.getState());
        }
    }
    
    @Test
    public void step_calledWithDefinedState_keepsDefinedState() {
        PatchSimulation sim = mock(PatchSimulation.class);
        PatchCellRandom cell = spy(new PatchCellRandom(cellID, cellParent, cellPop,
                cellState, cellAge, cellDivisions, locationMock, parametersMock,
                cellVolume, cellHeight, cellCriticalVolume, cellCriticalHeight));
        
        cell.processes.put(Domain.METABOLISM, mock(PatchProcessMetabolism.class));
        cell.processes.put(Domain.SIGNALING, mock(PatchProcessMetabolism.class));
        
        int numValidStates = State.values().length - 1;
        
        for (int i = 1; i < numValidStates + 1; i++) {
            MersenneTwisterFast random = mock(MersenneTwisterFast.class);
            doReturn(i).when(random).nextInt(numValidStates);
            doAnswer(invocationOnMock -> {
                cell.state = invocationOnMock.getArgument(0);
                cell.module = null;
                return null;
            }).when(cell).setState(any(State.class));
            
            State state = State.values()[i];
            sim.random = random;
            cell.setState(state);
            cell.step(sim);
            
            assertEquals(state, cell.getState());
        }
    }
}
