package arcade.potts.agent.cell;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ec.util.MersenneTwisterFast;
import arcade.core.util.GrabBag;
import arcade.core.util.MiniBox;
import arcade.core.util.Parameters;
import arcade.core.util.exceptions.IncorrectlySetParameterException;
import arcade.potts.env.location.PottsLocation;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.potts.util.PottsEnums.Phase;
import static arcade.potts.util.PottsEnums.State;

public class PottsCellFlyGMCTest {

    static final double EPSILON = 1e-6;

    static MersenneTwisterFast random = new MersenneTwisterFast();

    static PottsLocation locationMock;

    static Parameters parametersMock;

    static GrabBag links;

    static int cellID = randomIntBetween(1, 10);

    static int cellParent = randomIntBetween(1, 10);

    static int cellPop = randomIntBetween(1, 10);

    static int cellAge = randomIntBetween(1, 1000);

    static int cellDivisions = randomIntBetween(1, 100);

    static double cellCriticalVolume = randomDoubleBetween(10, 100);

    static double cellCriticalHeight = randomDoubleBetween(10, 100);

    static State cellState = State.UNDEFINED;

    static Phase cellPhase = Phase.UNDEFINED;

    static PottsCellContainer baseContainer =
            new PottsCellContainer(
                    cellID,
                    cellParent,
                    cellPop,
                    cellAge,
                    cellDivisions,
                    cellState,
                    cellPhase,
                    0,
                    cellCriticalVolume,
                    cellCriticalHeight);

    @BeforeEach
    public final void setupMocks() {
        locationMock = mock(PottsLocation.class);
        parametersMock = spy(new Parameters(new MiniBox(), null, null));
        links = new GrabBag();
        links.add(1, 1);
        doReturn(0.0).when(parametersMock).getDouble(any(String.class));
        doReturn(0).when(parametersMock).getInt(any(String.class));
    }

    @Test
    public void constructor_validParameters_createsInstance() {
        PottsCellFlyGMC gmc =
                new PottsCellFlyGMC(baseContainer, locationMock, parametersMock, links);
        assertNotNull(gmc);
    }

    @Test
    public void constructor_invalidBasalApoptosisRate_throwsUnsupportedOperationException() {
        doReturn(1.0).when(parametersMock).getDouble(any(String.class));
        assertThrows(
                IncorrectlySetParameterException.class,
                () -> {
                    new PottsCellFlyGMC(baseContainer, locationMock, parametersMock, links);
                });
    }

    @Test
    public void setStateModule_Proliferative_createsModule() {
        PottsCellFlyGMC gmc =
                new PottsCellFlyGMC(baseContainer, locationMock, parametersMock, links);
        gmc.setStateModule(State.PROLIFERATIVE);
        assertNotNull(gmc.getModule());
    }

    @Test
    public void setStateModule_notProliferative_moduleNull() {
        PottsCellFlyGMC gmc =
                new PottsCellFlyGMC(baseContainer, locationMock, parametersMock, links);
        for (State state : State.values()) {
            if (state != State.PROLIFERATIVE) {
                gmc.setStateModule(state);
                assertNull(gmc.getModule());
            }
        }
    }

    @Test
    public void make_called_returnsCorrectNewContainer() {
        PottsCellFlyGMC gmc =
                new PottsCellFlyGMC(baseContainer, locationMock, parametersMock, links);
        PottsCellContainer container = gmc.make(cellID, State.QUIESCENT, random);
        assertAll(
                () -> assertNotNull(container),
                () -> assertEquals(cellID, container.parent),
                () -> assertEquals(1, container.pop),
                () -> assertEquals(cellAge, container.age),
                () -> assertEquals(cellDivisions + 1, container.divisions),
                () -> assertEquals(State.QUIESCENT, container.state),
                () -> assertNull(container.phase),
                () -> assertEquals(0, container.voxels),
                () -> assertNull(container.regionVoxels),
                () -> assertEquals(cellCriticalVolume, container.criticalVolume, EPSILON),
                () -> assertEquals(cellCriticalHeight, container.criticalHeight, EPSILON),
                () -> assertNull(container.criticalRegionVolumes),
                () -> assertNull(container.criticalRegionHeights));
    }
}
