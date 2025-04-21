package arcade.patch.agent.cell;

import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sim.engine.Schedule;
import sim.engine.Steppable;
import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.core.util.Parameters;
import arcade.patch.agent.module.PatchModule;
import arcade.patch.agent.process.PatchProcessInflammation;
import arcade.patch.agent.process.PatchProcessMetabolism;
import arcade.patch.agent.process.PatchProcessSignaling;
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

    private Parameters parametersMock;

    private PatchLocation locationMock;

    private PatchCellContainer container;

    private PatchCellCARTCD4 cellMock;

    private PatchSimulation simMock;

    @BeforeEach
    public final void setUp() {
        parametersMock = spy(new Parameters(new MiniBox(), null, null));
        locationMock = mock(PatchLocation.class);

        int id = 1;
        int parentId = 1;
        int pop = 1;
        int age = randomIntBetween(1, 100);
        int divisions = 0;
        double volume = randomDoubleBetween(100, 200);
        double height = randomDoubleBetween(4, 10);
        double criticalVolume = randomDoubleBetween(100, 200);
        double criticalHeight = randomDoubleBetween(4, 10);
        State state = State.UNDEFINED;

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

        doReturn(1.0).when(parametersMock).getDouble(any(String.class));
        doReturn(1).when(parametersMock).getInt(any(String.class));
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        simMock = mock(PatchSimulation.class);
        simMock.random = random;
    }

    @Test
    public void step_called_increasesAge() {
        cellMock = spy(new PatchCellCARTCD4(container, locationMock, parametersMock));
        cellMock.processes.put(Domain.METABOLISM, mock(PatchProcessMetabolism.class));
        cellMock.processes.put(Domain.SIGNALING, mock(PatchProcessSignaling.class));
        cellMock.processes.put(Domain.INFLAMMATION, mock(PatchProcessInflammation.class));
        PatchModule module = mock(PatchModule.class);
        doAnswer(
                        invocationOnMock -> {
                            cellMock.state = invocationOnMock.getArgument(0);
                            cellMock.module = module;
                            return null;
                        })
                .when(cellMock)
                .setState(any(State.class));
        doReturn(new PatchCellTissue(container, locationMock, parametersMock))
                .when(cellMock)
                .bindTarget(
                        any(Simulation.class),
                        any(PatchLocation.class),
                        any(MersenneTwisterFast.class));
        int initialAge = cellMock.getAge();

        cellMock.step(simMock);

        assertEquals(initialAge + 1, cellMock.getAge());
    }

    @Test
    public void step_whenEnergyIsLow_setsStateToApoptotic() {
        when(parametersMock.getDouble("APOPTOSIS_AGE")).thenReturn(100.0);
        when(parametersMock.getDouble("ENERGY_THRESHOLD")).thenReturn(1.0);
        cellMock = spy(new PatchCellCARTCD4(container, locationMock, parametersMock));
        cellMock.processes.put(Domain.METABOLISM, mock(PatchProcessMetabolism.class));
        cellMock.processes.put(Domain.SIGNALING, mock(PatchProcessSignaling.class));
        cellMock.processes.put(Domain.INFLAMMATION, mock(PatchProcessInflammation.class));
        PatchModule module = mock(PatchModule.class);
        doAnswer(
                        invocationOnMock -> {
                            cellMock.state = invocationOnMock.getArgument(0);
                            cellMock.module = module;
                            return null;
                        })
                .when(cellMock)
                .setState(any(State.class));
        doReturn(new PatchCellTissue(container, locationMock, parametersMock))
                .when(cellMock)
                .bindTarget(
                        any(Simulation.class),
                        any(PatchLocation.class),
                        any(MersenneTwisterFast.class));
        cellMock.setEnergy(-1 * randomIntBetween(2, 5));

        cellMock.step(simMock);

        assertEquals(State.APOPTOTIC, cellMock.getState());
        assertEquals(AntigenFlag.UNBOUND, cellMock.getBindingFlag());
        assertFalse(cellMock.getActivationStatus());
    }

    @Test
    public void step_whenEnergyIsNegativeAndMoreThanThreshold_setsStateToStarved() {
        when(parametersMock.getDouble("APOPTOSIS_AGE")).thenReturn(100.0);
        when(parametersMock.getDouble("ENERGY_THRESHOLD")).thenReturn(1.0);
        when(parametersMock.getDouble("EXHAUSTED_FRAC")).thenReturn(0.5);
        cellMock = spy(new PatchCellCARTCD4(container, locationMock, parametersMock));
        cellMock.processes.put(Domain.METABOLISM, mock(PatchProcessMetabolism.class));
        cellMock.processes.put(Domain.SIGNALING, mock(PatchProcessSignaling.class));
        cellMock.processes.put(Domain.INFLAMMATION, mock(PatchProcessInflammation.class));
        PatchModule module = mock(PatchModule.class);
        doAnswer(
                        invocationOnMock -> {
                            cellMock.state = invocationOnMock.getArgument(0);
                            cellMock.module = module;
                            return null;
                        })
                .when(cellMock)
                .setState(any(State.class));
        doReturn(new PatchCellTissue(container, locationMock, parametersMock))
                .when(cellMock)
                .bindTarget(
                        any(Simulation.class),
                        any(PatchLocation.class),
                        any(MersenneTwisterFast.class));
        cellMock.setEnergy(-0.5);

        cellMock.step(simMock);

        assertEquals(State.STARVED, cellMock.getState());
        assertEquals(AntigenFlag.UNBOUND, cellMock.getBindingFlag());
    }

    @Test
    public void step_whenEnergyIsNegativeAndLessThanThreshold_setsStateToApoptotic() {
        when(parametersMock.getDouble("APOPTOSIS_AGE")).thenReturn(100.0);
        when(parametersMock.getDouble("ENERGY_THRESHOLD")).thenReturn(1.0);
        cellMock = spy(new PatchCellCARTCD4(container, locationMock, parametersMock));
        cellMock.processes.put(Domain.METABOLISM, mock(PatchProcessMetabolism.class));
        cellMock.processes.put(Domain.SIGNALING, mock(PatchProcessSignaling.class));
        cellMock.processes.put(Domain.INFLAMMATION, mock(PatchProcessInflammation.class));
        PatchModule module = mock(PatchModule.class);
        doAnswer(
                        invocationOnMock -> {
                            cellMock.state = invocationOnMock.getArgument(0);
                            cellMock.module = module;
                            return null;
                        })
                .when(cellMock)
                .setState(any(State.class));
        doReturn(new PatchCellTissue(container, locationMock, parametersMock))
                .when(cellMock)
                .bindTarget(
                        any(Simulation.class),
                        any(PatchLocation.class),
                        any(MersenneTwisterFast.class));
        cellMock.setEnergy(-1.5);

        cellMock.step(simMock);

        assertEquals(State.APOPTOTIC, cellMock.getState());
    }

    @Test
    public void step_whenDivisionPotentialMet_setsStateToApoptotic()
            throws NoSuchFieldException, IllegalAccessException {
        when(parametersMock.getDouble("APOPTOSIS_AGE")).thenReturn(100.0);
        when(parametersMock.getInt("DIVISION_POTENTIAL")).thenReturn(10);
        when(parametersMock.getDouble("SENESCENT_FRACTION")).thenReturn(0.5);
        cellMock = spy(new PatchCellCARTCD4(container, locationMock, parametersMock));
        cellMock.processes.put(Domain.METABOLISM, mock(PatchProcessMetabolism.class));
        cellMock.processes.put(Domain.SIGNALING, mock(PatchProcessSignaling.class));
        cellMock.processes.put(Domain.INFLAMMATION, mock(PatchProcessInflammation.class));
        PatchModule module = mock(PatchModule.class);
        doAnswer(
                        invocationOnMock -> {
                            cellMock.state = invocationOnMock.getArgument(0);
                            cellMock.module = module;
                            return null;
                        })
                .when(cellMock)
                .setState(any(State.class));
        doReturn(new PatchCellTissue(container, locationMock, parametersMock))
                .when(cellMock)
                .bindTarget(
                        any(Simulation.class),
                        any(PatchLocation.class),
                        any(MersenneTwisterFast.class));
        Field div = PatchCell.class.getDeclaredField("divisions");
        div.setAccessible(true);
        div.set(cellMock, cellMock.divisionPotential);
        when(simMock.random.nextDouble()).thenReturn(0.51);

        cellMock.step(simMock);

        assertTrue(cellMock.getState() == State.APOPTOTIC);
        assertEquals(AntigenFlag.UNBOUND, cellMock.getBindingFlag());
        assertFalse(cellMock.getActivationStatus());
    }

    @Test
    public void step_whenDivisionPotentialMet_setsStateToSenescent()
            throws NoSuchFieldException, IllegalAccessException {
        when(parametersMock.getDouble("APOPTOSIS_AGE")).thenReturn(100.0);
        when(parametersMock.getDouble("SENESCENT_FRACTION")).thenReturn(0.5);
        cellMock = spy(new PatchCellCARTCD4(container, locationMock, parametersMock));
        cellMock.processes.put(Domain.METABOLISM, mock(PatchProcessMetabolism.class));
        cellMock.processes.put(Domain.SIGNALING, mock(PatchProcessSignaling.class));
        cellMock.processes.put(Domain.INFLAMMATION, mock(PatchProcessInflammation.class));
        PatchModule module = mock(PatchModule.class);
        doAnswer(
                        invocationOnMock -> {
                            cellMock.state = invocationOnMock.getArgument(0);
                            cellMock.module = module;
                            return null;
                        })
                .when(cellMock)
                .setState(any(State.class));
        doReturn(new PatchCellTissue(container, locationMock, parametersMock))
                .when(cellMock)
                .bindTarget(
                        any(Simulation.class),
                        any(PatchLocation.class),
                        any(MersenneTwisterFast.class));
        Field div = PatchCell.class.getDeclaredField("divisions");
        div.setAccessible(true);
        div.set(cellMock, cellMock.divisionPotential);
        when(simMock.random.nextDouble()).thenReturn(0.49);

        cellMock.step(simMock);

        assertTrue(cellMock.getState() == State.SENESCENT);
        assertEquals(AntigenFlag.UNBOUND, cellMock.getBindingFlag());
        assertFalse(cellMock.getActivationStatus());
    }

    @Test
    public void step_whenBoundToBothAntigenAndSelf_setsStateToAnergic() {
        when(parametersMock.getDouble("APOPTOSIS_AGE")).thenReturn(100.0);
        when(parametersMock.getDouble("ANERGIC_FRACTION")).thenReturn(0.5);
        when(simMock.random.nextDouble()).thenReturn(0.49);
        cellMock = spy(new PatchCellCARTCD4(container, locationMock, parametersMock));
        cellMock.processes.put(Domain.METABOLISM, mock(PatchProcessMetabolism.class));
        cellMock.processes.put(Domain.SIGNALING, mock(PatchProcessSignaling.class));
        cellMock.processes.put(Domain.INFLAMMATION, mock(PatchProcessInflammation.class));
        PatchModule module = mock(PatchModule.class);
        doAnswer(
                        invocationOnMock -> {
                            cellMock.state = invocationOnMock.getArgument(0);
                            cellMock.module = module;
                            return null;
                        })
                .when(cellMock)
                .setState(any(State.class));
        doReturn(new PatchCellTissue(container, locationMock, parametersMock))
                .when(cellMock)
                .bindTarget(
                        any(Simulation.class),
                        any(PatchLocation.class),
                        any(MersenneTwisterFast.class));
        cellMock.setBindingFlag(AntigenFlag.BOUND_ANTIGEN_CELL_RECEPTOR);

        cellMock.step(simMock);

        assertTrue(cellMock.getState() == State.ANERGIC);
        assertEquals(AntigenFlag.UNBOUND, cellMock.getBindingFlag());
        assertFalse(cellMock.getActivationStatus());
    }

    @Test
    public void step_whenBoundToAntigen_setsStateToStimulatory()
            throws NoSuchFieldException, IllegalAccessException {
        when(parametersMock.getDouble("APOPTOSIS_AGE")).thenReturn(100.0);
        when(parametersMock.getInt("SELF_RECEPTORS")).thenReturn(randomIntBetween(100, 200));
        when(parametersMock.getDouble("SEARCH_ABILITY")).thenReturn(1.0);
        when(parametersMock.getDouble("CAR_AFFINITY")).thenReturn(10 * Math.pow(10, -7));
        when(parametersMock.getDouble("CAR_ALPHA")).thenReturn(3.0);
        when(parametersMock.getDouble("CAR_BETA")).thenReturn(0.01);
        when(parametersMock.getDouble("SELF_RECEPTOR_AFFINITY")).thenReturn(7.8E-6);
        when(parametersMock.getDouble("SELF_ALPHA")).thenReturn(3.0);
        when(parametersMock.getDouble("SELF_BETA")).thenReturn(0.02);
        when(parametersMock.getDouble("CONTACT_FRAC")).thenReturn(7.8E-6);
        when(parametersMock.getInt("MAX_ANTIGEN_BINDING")).thenReturn(10);
        when(parametersMock.getInt("CARS")).thenReturn(50000);
        cellMock = spy(new PatchCellCARTCD4(container, locationMock, parametersMock));
        cellMock.processes.put(Domain.METABOLISM, mock(PatchProcessMetabolism.class));
        cellMock.processes.put(Domain.SIGNALING, mock(PatchProcessSignaling.class));
        cellMock.processes.put(Domain.INFLAMMATION, mock(PatchProcessInflammation.class));
        PatchModule module = mock(PatchModule.class);
        doAnswer(
                        invocationOnMock -> {
                            cellMock.state = invocationOnMock.getArgument(0);
                            cellMock.module = module;
                            return null;
                        })
                .when(cellMock)
                .setState(any(State.class));
        doReturn(new PatchCellTissue(container, locationMock, parametersMock))
                .when(cellMock)
                .bindTarget(
                        any(Simulation.class),
                        any(PatchLocation.class),
                        any(MersenneTwisterFast.class));
        Field boundAntigens = PatchCellCART.class.getDeclaredField("boundCARAntigensCount");
        boundAntigens.setAccessible(true);
        boundAntigens.set(cellMock, 0);
        cellMock.setBindingFlag(AntigenFlag.BOUND_ANTIGEN);
        Schedule schedule = mock(Schedule.class);
        doReturn(true).when(schedule).scheduleOnce(any(Steppable.class));
        doReturn(schedule).when(simMock).getSchedule();

        cellMock.step(simMock);

        assertEquals(State.STIMULATORY, cellMock.getState());
        assertTrue(cellMock.getActivationStatus());
    }

    @Test
    public void step_whenActivated_setsStateToProliferative()
            throws NoSuchFieldException, IllegalAccessException {
        when(parametersMock.getDouble("APOPTOSIS_AGE")).thenReturn(100.0);
        when(parametersMock.getDouble("PROLIFERATIVE_FRACTION")).thenReturn(0.5);
        cellMock = spy(new PatchCellCARTCD4(container, locationMock, parametersMock));
        cellMock.processes.put(Domain.METABOLISM, mock(PatchProcessMetabolism.class));
        cellMock.processes.put(Domain.SIGNALING, mock(PatchProcessSignaling.class));
        cellMock.processes.put(Domain.INFLAMMATION, mock(PatchProcessInflammation.class));
        PatchModule module = mock(PatchModule.class);
        doAnswer(
                        invocationOnMock -> {
                            cellMock.state = invocationOnMock.getArgument(0);
                            cellMock.module = module;
                            return null;
                        })
                .when(cellMock)
                .setState(any(State.class));
        doReturn(new PatchCellTissue(container, locationMock, parametersMock))
                .when(cellMock)
                .bindTarget(
                        any(Simulation.class),
                        any(PatchLocation.class),
                        any(MersenneTwisterFast.class));
        Field active = PatchCellCART.class.getDeclaredField("activated");
        active.setAccessible(true);
        active.set(cellMock, true);

        cellMock.step(simMock);

        assertEquals(State.PROLIFERATIVE, cellMock.getState());
    }

    @Test
    public void step_whenNotActivated_setsStateToMigratory()
            throws NoSuchFieldException, IllegalAccessException {
        when(parametersMock.getDouble("APOPTOSIS_AGE")).thenReturn(100.0);
        when(parametersMock.getDouble("PROLIFERATIVE_FRACTION")).thenReturn(0.5);
        cellMock = spy(new PatchCellCARTCD4(container, locationMock, parametersMock));
        cellMock.processes.put(Domain.METABOLISM, mock(PatchProcessMetabolism.class));
        cellMock.processes.put(Domain.SIGNALING, mock(PatchProcessSignaling.class));
        cellMock.processes.put(Domain.INFLAMMATION, mock(PatchProcessInflammation.class));
        PatchModule module = mock(PatchModule.class);
        doAnswer(
                        invocationOnMock -> {
                            cellMock.state = invocationOnMock.getArgument(0);
                            cellMock.module = module;
                            return null;
                        })
                .when(cellMock)
                .setState(any(State.class));
        doReturn(new PatchCellTissue(container, locationMock, parametersMock))
                .when(cellMock)
                .bindTarget(
                        any(Simulation.class),
                        any(PatchLocation.class),
                        any(MersenneTwisterFast.class));
        when(simMock.random.nextDouble()).thenReturn(0.51);
        Field active = PatchCellCART.class.getDeclaredField("activated");
        active.setAccessible(true);
        active.set(cellMock, false);

        cellMock.step(simMock);

        assertTrue(cellMock.getState() == State.MIGRATORY);
    }

    @Test
    public void step_whenOverstimulated_setsStateToExhausted()
            throws NoSuchFieldException, IllegalAccessException {
        when(parametersMock.getDouble("APOPTOSIS_AGE")).thenReturn(100.0);
        when(parametersMock.getDouble("EXHAUSTED_FRAC")).thenReturn(0.5);
        cellMock = spy(new PatchCellCARTCD4(container, locationMock, parametersMock));
        cellMock.processes.put(Domain.METABOLISM, mock(PatchProcessMetabolism.class));
        cellMock.processes.put(Domain.SIGNALING, mock(PatchProcessSignaling.class));
        cellMock.processes.put(Domain.INFLAMMATION, mock(PatchProcessInflammation.class));
        PatchModule module = mock(PatchModule.class);
        doAnswer(
                        invocationOnMock -> {
                            cellMock.state = invocationOnMock.getArgument(0);
                            cellMock.module = module;
                            return null;
                        })
                .when(cellMock)
                .setState(any(State.class));
        doReturn(new PatchCellTissue(container, locationMock, parametersMock))
                .when(cellMock)
                .bindTarget(
                        any(Simulation.class),
                        any(PatchLocation.class),
                        any(MersenneTwisterFast.class));
        when(simMock.random.nextDouble()).thenReturn(0.49);
        Field boundAntigens = PatchCellCART.class.getDeclaredField("boundCARAntigensCount");
        boundAntigens.setAccessible(true);
        boundAntigens.set(cellMock, cellMock.maxAntigenBinding + 1);
        cellMock.setBindingFlag(AntigenFlag.BOUND_ANTIGEN);

        cellMock.step(simMock);

        assertTrue(cellMock.getState() == State.EXHAUSTED);
        assertEquals(AntigenFlag.UNBOUND, cellMock.getBindingFlag());
        assertFalse(cellMock.getActivationStatus());
    }
}
