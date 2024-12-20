package arcade.patch.agent.cell;

import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sim.engine.Schedule;
import sim.engine.Steppable;
import sim.util.Bag;
import ec.util.MersenneTwisterFast;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.Parameters;          
import arcade.patch.agent.module.PatchModule;
import arcade.patch.agent.process.PatchProcessInflammation;
import arcade.patch.agent.process.PatchProcessMetabolism;
import arcade.patch.agent.process.PatchProcessSignaling;
import arcade.patch.env.grid.PatchGrid;
import arcade.patch.env.location.PatchLocation;
import arcade.patch.sim.PatchSimulation;
import arcade.patch.util.PatchEnums.Domain;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.randomDoubleBetween;
import static arcade.core.ARCADETestUtilities.randomIntBetween;
import static arcade.patch.util.PatchEnums.AntigenFlag;
import static arcade.patch.util.PatchEnums.State;

public class PatchCellCARTCD4Test {

    private PatchCellCARTCD4 patchCellCART;
    private Parameters parameters;
    private PatchLocation location;
    private PatchCellContainer container;

    @BeforeEach
    public void setUp() {
        parameters = mock(Parameters.class);
        location = mock(PatchLocation.class);

        int id = 1;
        int parentId = 1;
        int pop = 1;
        int age = randomIntBetween(1, 100800);
        int divisions = 10;
        double volume = randomDoubleBetween(100, 200);
        double height = randomDoubleBetween(4, 10);
        double criticalVolume = randomDoubleBetween(100, 200);
        double criticalHeight = randomDoubleBetween(4, 10);
        State state = State.UNDEFINED;
        ;

        container =
                new PatchCellContainer(
                        id,
                        parentId,
                        pop,
                        age,
                        divisions,
                        state,
                        volume,
                        height,
                        criticalVolume,
                        criticalHeight);

        when(parameters.getDouble("HETEROGENEITY")).thenReturn(0.0);
        when(parameters.getDouble("ENERGY_THRESHOLD")).thenReturn(1.0);

        when(parameters.getDouble("NECROTIC_FRACTION"))
                .thenReturn(randomIntBetween(40, 100) / 100.0);
        when(parameters.getDouble("EXHAU_FRAC")).thenReturn(randomIntBetween(40, 100) / 100.0);
        when(parameters.getDouble("SENESCENT_FRACTION"))
                .thenReturn(randomIntBetween(40, 100) / 100.0);
        when(parameters.getDouble("ANERGIC_FRACTION"))
                .thenReturn(randomIntBetween(40, 100) / 100.0);
        when(parameters.getDouble("PROLIFERATIVE_FRACTION"))
                .thenReturn(randomIntBetween(40, 100) / 100.0);
        when(parameters.getInt("SELF_RECEPTORS")).thenReturn(randomIntBetween(100, 200));
        when(parameters.getDouble("SEARCH_ABILITY")).thenReturn(1.0);
        when(parameters.getDouble("CAR_AFFINITY")).thenReturn(10 * Math.pow(10, -7));
        when(parameters.getDouble("CAR_ALPHA")).thenReturn(3.0);
        when(parameters.getDouble("CAR_BETA")).thenReturn(0.01);
        when(parameters.getDouble("SELF_RECEPTOR_AFFINITY")).thenReturn(7.8E-6);
        when(parameters.getDouble("SELF_ALPHA")).thenReturn(3.0);
        when(parameters.getDouble("SELF_BETA")).thenReturn(0.02);
        when(parameters.getDouble("CONTACT_FRAC")).thenReturn(7.8E-6);
        when(parameters.getInt("MAX_ANTIGEN_BINDING")).thenReturn(10);
        when(parameters.getInt("CARS")).thenReturn(50000);

        patchCellCART = new PatchCellCARTCD4(container, location, parameters);
    }

    @Test
    public void testSetAntigenFlag() {
        patchCellCART.setAntigenFlag(AntigenFlag.BOUND_ANTIGEN);
        assertEquals(AntigenFlag.BOUND_ANTIGEN, patchCellCART.getAntigenFlag());
    }

    @Test
    public void testGetAntigenFlag() {
        patchCellCART.setAntigenFlag(AntigenFlag.BOUND_ANTIGEN);
        assertEquals(AntigenFlag.BOUND_ANTIGEN, patchCellCART.getAntigenFlag());
    }

    @Test
    public void testBindTargetNoNeighbors() {
        Simulation sim = mock(Simulation.class);
        PatchLocation loc = mock(PatchLocation.class);
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        PatchGrid grid = mock(PatchGrid.class);
        Bag bag = new Bag();

        when(sim.getGrid()).thenReturn(grid);
        when(grid.getObjectsAtLocation(loc)).thenReturn(bag);
        when(loc.getNeighbors()).thenReturn(new ArrayList<Location>());

        PatchCellTissue result = patchCellCART.bindTarget(sim, loc, random);
        assertNull(result);
        assertEquals(AntigenFlag.UNBOUND, patchCellCART.getAntigenFlag());
    }

    @Test
    public void testBindTargetWithSelfBinding() {
        // lots of self antigens, not a lot of car antigens
        Simulation sim = mock(Simulation.class);
        PatchLocation loc = mock(PatchLocation.class);
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        PatchGrid grid = mock(PatchGrid.class);
        Bag bag = new Bag();

        when(sim.getGrid()).thenReturn(grid);
        when(grid.getObjectsAtLocation(loc)).thenReturn(bag);
        when(loc.getNeighbors()).thenReturn(new ArrayList<Location>());
        when(loc.getVolume()).thenReturn(6000.0);

        when(parameters.getInt("CAR_ANTIGENS_HEALTHY")).thenReturn(10);
        when(parameters.getInt("CAR_ANTIGENS_CANCER")).thenReturn(10);
        when(parameters.getInt("SELF_TARGETS")).thenReturn(10000000);
        PatchCellTissue tissueCell = new PatchCellTissue(container, location, parameters);

        bag.add(tissueCell);

        when(random.nextDouble()).thenReturn(0.0000005);

        PatchCellTissue result = patchCellCART.bindTarget(sim, loc, random);
        assertNotNull(result);
        assertEquals(AntigenFlag.BOUND_CELL_RECEPTOR, patchCellCART.getAntigenFlag());
    }

    @Test
    public void testBindTargetWithSelfAndAntigenBinding() {
        Simulation sim = mock(Simulation.class);
        PatchLocation loc = mock(PatchLocation.class);
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        PatchGrid grid = mock(PatchGrid.class);
        Bag bag = new Bag();

        when(sim.getGrid()).thenReturn(grid);
        when(grid.getObjectsAtLocation(loc)).thenReturn(bag);
        when(loc.getNeighbors()).thenReturn(new ArrayList<Location>());
        when(loc.getVolume()).thenReturn(6000.0);

        when(parameters.getInt("CAR_ANTIGENS_HEALTHY")).thenReturn(5000);
        when(parameters.getInt("CAR_ANTIGENS_CANCER")).thenReturn(5000);
        when(parameters.getInt("SELF_TARGETS")).thenReturn(50000000);

        PatchCellTissue tissueCell = new PatchCellTissue(container, location, parameters);

        bag.add(tissueCell);

        when(random.nextDouble()).thenReturn(0.0000005);

        PatchCellTissue result = patchCellCART.bindTarget(sim, loc, random);
        assertNotNull(result);
        assertEquals(AntigenFlag.BOUND_ANTIGEN_CELL_RECEPTOR, patchCellCART.getAntigenFlag());
    }

    @Test
    public void testBindTarget() {
        Simulation sim = mock(Simulation.class);
        PatchLocation loc = mock(PatchLocation.class);
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        PatchGrid grid = mock(PatchGrid.class);
        Bag bag = new Bag();

        when(sim.getGrid()).thenReturn(grid);
        when(grid.getObjectsAtLocation(loc)).thenReturn(bag);
        when(loc.getNeighbors()).thenReturn(new ArrayList<Location>());
        when(loc.getVolume()).thenReturn(6000.0);

        when(parameters.getInt("CAR_ANTIGENS_HEALTHY")).thenReturn(5000);
        when(parameters.getInt("CAR_ANTIGENS_CANCER")).thenReturn(5000);
        when(parameters.getInt("SELF_TARGETS")).thenReturn(5000);

        PatchCellTissue tissueCell = new PatchCellTissue(container, location, parameters);

        bag.add(tissueCell);

        when(random.nextDouble()).thenReturn(0.0000005);

        bag.add(tissueCell);

        PatchCellTissue result = patchCellCART.bindTarget(sim, loc, random);
        assertNotNull(result);
        assertEquals(AntigenFlag.BOUND_ANTIGEN, patchCellCART.getAntigenFlag());
    }

    @Test
    public void testStepIncreasesAge() {
        PatchSimulation sim = mock(PatchSimulation.class);
        PatchCellCARTCD4 cell = spy(new PatchCellCARTCD4(container, location, parameters));
        cell.processes.put(Domain.METABOLISM, mock(PatchProcessMetabolism.class));
        cell.processes.put(Domain.SIGNALING, mock(PatchProcessSignaling.class));
        cell.processes.put(Domain.INFLAMMATION, mock(PatchProcessInflammation.class));
        PatchModule module = mock(PatchModule.class);
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        doAnswer(
                        invocationOnMock -> {
                            cell.state = invocationOnMock.getArgument(0);
                            cell.module = module;
                            return null;
                        })
                .when(cell)
                .setState(any(State.class));
        doReturn(new PatchCellTissue(container, location, parameters))
                .when(cell)
                .bindTarget(
                        any(Simulation.class),
                        any(PatchLocation.class),
                        any(MersenneTwisterFast.class));
        sim.random = random;
        cell.setState(State.UNDEFINED);
        int initialAge = cell.getAge();
        cell.step(sim);
        assertEquals(initialAge + 1, cell.getAge());
    }

    @Test
    public void testStepSetsStateToApoptoticWhenEnergyIsLow() {
        PatchSimulation sim = mock(PatchSimulation.class);
        PatchCellCARTCD4 cell = spy(new PatchCellCARTCD4(container, location, parameters));
        cell.processes.put(Domain.METABOLISM, mock(PatchProcessMetabolism.class));
        cell.processes.put(Domain.SIGNALING, mock(PatchProcessSignaling.class));
        cell.processes.put(Domain.INFLAMMATION, mock(PatchProcessInflammation.class));
        PatchModule module = mock(PatchModule.class);
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        doAnswer(
                        invocationOnMock -> {
                            cell.state = invocationOnMock.getArgument(0);
                            cell.module = module;
                            return null;
                        })
                .when(cell)
                .setState(any(State.class));
        doReturn(new PatchCellTissue(container, location, parameters))
                .when(cell)
                .bindTarget(
                        any(Simulation.class),
                        any(PatchLocation.class),
                        any(MersenneTwisterFast.class));
        sim.random = random;
        cell.setState(State.UNDEFINED);

        cell.setEnergy(-1 * randomIntBetween(1, 5));
        cell.step(sim);

        assertEquals(State.APOPTOTIC, cell.getState());
        assertEquals(AntigenFlag.UNBOUND, cell.getAntigenFlag());
        assertFalse(cell.getActivationStatus());
    }

    @Test
    public void testStepSetsStateToStarvedWhenEnergyIsNegativeAndMoreThanThreshold() {
        PatchSimulation sim = mock(PatchSimulation.class);
        PatchCellCARTCD4 cell = spy(new PatchCellCARTCD4(container, location, parameters));
        cell.processes.put(Domain.METABOLISM, mock(PatchProcessMetabolism.class));
        cell.processes.put(Domain.SIGNALING, mock(PatchProcessSignaling.class));
        cell.processes.put(Domain.INFLAMMATION, mock(PatchProcessInflammation.class));
        PatchModule module = mock(PatchModule.class);
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        doAnswer(
                        invocationOnMock -> {
                            cell.state = invocationOnMock.getArgument(0);
                            cell.module = module;
                            return null;
                        })
                .when(cell)
                .setState(any(State.class));
        doReturn(new PatchCellTissue(container, location, parameters))
                .when(cell)
                .bindTarget(
                        any(Simulation.class),
                        any(PatchLocation.class),
                        any(MersenneTwisterFast.class));
        sim.random = random;
        cell.setState(State.UNDEFINED);

        cell.setEnergy(-0.5);
        cell.step(sim);

        assertEquals(State.STARVED, cell.getState());
        assertEquals(AntigenFlag.UNBOUND, cell.getAntigenFlag());
    }

    @Test
    public void testStepSetsStateToApoptoticWhenEnergyIsNegativeAndLessThanThreshold() {
        PatchSimulation sim = mock(PatchSimulation.class);
        PatchCellCARTCD4 cell = spy(new PatchCellCARTCD4(container, location, parameters));
        cell.processes.put(Domain.METABOLISM, mock(PatchProcessMetabolism.class));
        cell.processes.put(Domain.SIGNALING, mock(PatchProcessSignaling.class));
        cell.processes.put(Domain.INFLAMMATION, mock(PatchProcessInflammation.class));
        PatchModule module = mock(PatchModule.class);
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        doAnswer(
                        invocationOnMock -> {
                            cell.state = invocationOnMock.getArgument(0);
                            cell.module = module;
                            return null;
                        })
                .when(cell)
                .setState(any(State.class));
        doReturn(new PatchCellTissue(container, location, parameters))
                .when(cell)
                .bindTarget(
                        any(Simulation.class),
                        any(PatchLocation.class),
                        any(MersenneTwisterFast.class));
        sim.random = random;
        cell.setState(State.UNDEFINED);

        cell.setEnergy(-1.5);
        cell.step(sim);

        assertEquals(State.APOPTOTIC, cell.getState());
    }

    @Test
    public void testStepSetsStateToSenescentWhenDivisionsAreZero() {
        PatchSimulation sim = mock(PatchSimulation.class);
        PatchCellCARTCD4 cell = spy(new PatchCellCARTCD4(container, location, parameters));
        cell.processes.put(Domain.METABOLISM, mock(PatchProcessMetabolism.class));
        cell.processes.put(Domain.SIGNALING, mock(PatchProcessSignaling.class));
        cell.processes.put(Domain.INFLAMMATION, mock(PatchProcessInflammation.class));
        PatchModule module = mock(PatchModule.class);
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        doAnswer(
                        invocationOnMock -> {
                            cell.state = invocationOnMock.getArgument(0);
                            cell.divisions = 0;
                            cell.module = module;
                            return null;
                        })
                .when(cell)
                .setState(any(State.class));
        doReturn(new PatchCellTissue(container, location, parameters))
                .when(cell)
                .bindTarget(
                        any(Simulation.class),
                        any(PatchLocation.class),
                        any(MersenneTwisterFast.class));
        sim.random = random;
        cell.setState(State.UNDEFINED);

        cell.step(sim);

        assertTrue(cell.getState() == State.APOPTOTIC || cell.getState() == State.SENESCENT);
        assertEquals(AntigenFlag.UNBOUND, cell.getAntigenFlag());
        assertFalse(cell.getActivationStatus());
    }

    @Test
    public void testStepSetsStateToAnergicWhenBoundToBothAntigenAndSelf() {
        PatchSimulation sim = mock(PatchSimulation.class);
        PatchCellCARTCD4 cell = spy(new PatchCellCARTCD4(container, location, parameters));
        cell.processes.put(Domain.METABOLISM, mock(PatchProcessMetabolism.class));
        cell.processes.put(Domain.SIGNALING, mock(PatchProcessSignaling.class));
        cell.processes.put(Domain.INFLAMMATION, mock(PatchProcessInflammation.class));
        PatchModule module = mock(PatchModule.class);
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        doAnswer(
                        invocationOnMock -> {
                            cell.state = invocationOnMock.getArgument(0);
                            cell.module = module;
                            return null;
                        })
                .when(cell)
                .setState(any(State.class));
        doReturn(new PatchCellTissue(container, location, parameters))
                .when(cell)
                .bindTarget(
                        any(Simulation.class),
                        any(PatchLocation.class),
                        any(MersenneTwisterFast.class));
        sim.random = random;
        cell.setState(State.UNDEFINED);

        cell.setAntigenFlag(AntigenFlag.BOUND_ANTIGEN_CELL_RECEPTOR);
        cell.step(sim);

        assertTrue(cell.getState() == State.APOPTOTIC || cell.getState() == State.ANERGIC);
        assertEquals(AntigenFlag.UNBOUND, cell.getAntigenFlag());
        assertFalse(cell.getActivationStatus());
    }

    @Test
    public void testStepSetsStateToCytotoxicWhenBoundToAntigen() {
        PatchSimulation sim = mock(PatchSimulation.class);
        PatchCellCARTCD4 cell = spy(new PatchCellCARTCD4(container, location, parameters));
        cell.processes.put(Domain.METABOLISM, mock(PatchProcessMetabolism.class));
        cell.processes.put(Domain.SIGNALING, mock(PatchProcessSignaling.class));
        cell.processes.put(Domain.INFLAMMATION, mock(PatchProcessInflammation.class));
        PatchModule module = mock(PatchModule.class);
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        doAnswer(
                        invocationOnMock -> {
                            cell.state = invocationOnMock.getArgument(0);
                            cell.boundAntigensCount = 0;
                            cell.module = module;
                            return null;
                        })
                .when(cell)
                .setState(any(State.class));
        doReturn(new PatchCellTissue(container, location, parameters))
                .when(cell)
                .bindTarget(
                        any(Simulation.class),
                        any(PatchLocation.class),
                        any(MersenneTwisterFast.class));
        sim.random = random;
        cell.setState(State.UNDEFINED);
        cell.setAntigenFlag(AntigenFlag.BOUND_ANTIGEN);

        Schedule schedule = mock(Schedule.class);
        doReturn(true).when(schedule).scheduleOnce(any(Steppable.class));
        doReturn(schedule).when(sim).getSchedule();

        cell.step(sim);

        assertEquals(State.STIMULATORY, cell.getState());
        assertEquals(AntigenFlag.UNBOUND, cell.getAntigenFlag());
        assertTrue(cell.getActivationStatus());
    }

    @Test
    public void testStepSetsStateToProliferativeWhenActivated() {
        PatchSimulation sim = mock(PatchSimulation.class);
        PatchCellCARTCD4 cell = spy(new PatchCellCARTCD4(container, location, parameters));
        cell.processes.put(Domain.METABOLISM, mock(PatchProcessMetabolism.class));
        cell.processes.put(Domain.SIGNALING, mock(PatchProcessSignaling.class));
        cell.processes.put(Domain.INFLAMMATION, mock(PatchProcessInflammation.class));
        PatchModule module = mock(PatchModule.class);
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        doAnswer(
                        invocationOnMock -> {
                            cell.state = invocationOnMock.getArgument(0);
                            cell.activated = true;
                            cell.module = module;
                            return null;
                        })
                .when(cell)
                .setState(any(State.class));
        doReturn(new PatchCellTissue(container, location, parameters))
                .when(cell)
                .bindTarget(
                        any(Simulation.class),
                        any(PatchLocation.class),
                        any(MersenneTwisterFast.class));
        sim.random = random;
        cell.setState(State.UNDEFINED);

        cell.step(sim);

        assertEquals(State.PROLIFERATIVE, cell.getState());
    }

    @Test
    public void testStepSetsStateToMigratoryWhenNotActivated() {
        PatchSimulation sim = mock(PatchSimulation.class);
        PatchCellCARTCD4 cell = spy(new PatchCellCARTCD4(container, location, parameters));
        cell.processes.put(Domain.METABOLISM, mock(PatchProcessMetabolism.class));
        cell.processes.put(Domain.SIGNALING, mock(PatchProcessSignaling.class));
        cell.processes.put(Domain.INFLAMMATION, mock(PatchProcessInflammation.class));
        PatchModule module = mock(PatchModule.class);
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        doAnswer(
                        invocationOnMock -> {
                            cell.state = invocationOnMock.getArgument(0);
                            cell.activated = false;
                            cell.module = module;
                            return null;
                        })
                .when(cell)
                .setState(any(State.class));
        doReturn(new PatchCellTissue(container, location, parameters))
                .when(cell)
                .bindTarget(
                        any(Simulation.class),
                        any(PatchLocation.class),
                        any(MersenneTwisterFast.class));
        sim.random = random;
        cell.setState(State.UNDEFINED);

        cell.step(sim);

        assertTrue(cell.getState() == State.MIGRATORY || cell.getState() == State.PROLIFERATIVE);
    }

    @Test
    public void testStepSetsStatetoExhaustedWhenOverstimulated() {
        PatchSimulation sim = mock(PatchSimulation.class);
        PatchCellCARTCD4 cell = spy(new PatchCellCARTCD4(container, location, parameters));
        cell.processes.put(Domain.METABOLISM, mock(PatchProcessMetabolism.class));
        cell.processes.put(Domain.SIGNALING, mock(PatchProcessSignaling.class));
        cell.processes.put(Domain.INFLAMMATION, mock(PatchProcessInflammation.class));
        PatchModule module = mock(PatchModule.class);
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        doAnswer(
                        invocationOnMock -> {
                            cell.state = invocationOnMock.getArgument(0);
                            cell.boundAntigensCount = cell.maxAntigenBinding + 1;
                            cell.module = module;
                            return null;
                        })
                .when(cell)
                .setState(any(State.class));
        doReturn(new PatchCellTissue(container, location, parameters))
                .when(cell)
                .bindTarget(
                        any(Simulation.class),
                        any(PatchLocation.class),
                        any(MersenneTwisterFast.class));
        sim.random = random;
        cell.setState(State.UNDEFINED);
        cell.setAntigenFlag(AntigenFlag.BOUND_ANTIGEN);

        cell.step(sim);

        assertTrue(cell.getState() == State.APOPTOTIC || cell.getState() == State.EXHAUSTED);
        assertEquals(AntigenFlag.UNBOUND, cell.getAntigenFlag());
        assertFalse(cell.getActivationStatus());
    }
}
