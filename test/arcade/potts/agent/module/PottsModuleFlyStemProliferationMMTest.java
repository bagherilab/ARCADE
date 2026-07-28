package arcade.potts.agent.module;

import java.util.ArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sim.util.Double3D;
import ec.util.MersenneTwisterFast;
import arcade.core.env.grid.Grid;
import arcade.core.util.GrabBag;
import arcade.core.util.Parameters;
import arcade.core.util.Plane;
import arcade.core.util.Vector;
import arcade.core.util.distributions.NormalDistribution;
import arcade.potts.agent.cell.PottsCellFactory;
import arcade.potts.agent.cell.PottsCellFlyStem;
import arcade.potts.env.location.PottsLocation;
import arcade.potts.env.location.PottsLocation2D;
import arcade.potts.env.location.Voxel;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class PottsModuleFlyStemProliferationMMTest {
    PottsCellFlyStem stemCell;

    PottsModuleFlyStemProliferationMM module;

    PottsLocation2D stemLoc;

    PottsLocation daughterLoc;

    Parameters parameters;

    PottsSimulation sim;

    Potts potts;

    Grid grid;

    PottsCellFactory factory;

    MersenneTwisterFast random;

    NormalDistribution dist;

    float EPSILON = 1e-6f;

    int stemCellPop;

    @BeforeEach
    public final void setup() {
        // Core mocks
        stemCell = mock(PottsCellFlyStem.class);
        parameters = mock(Parameters.class);
        dist = mock(NormalDistribution.class);
        sim = mock(PottsSimulation.class);
        potts = mock(Potts.class);
        grid = mock(Grid.class);
        factory = mock(PottsCellFactory.class);
        random = mock(MersenneTwisterFast.class);

        // Location mocks
        stemLoc = mock(PottsLocation2D.class);
        daughterLoc = mock(PottsLocation.class);

        // Wire simulation
        when(((PottsSimulation) sim).getPotts()).thenReturn(potts);
        potts.ids = new int[1][1][1];
        potts.regions = new int[1][1][1];
        when(sim.getGrid()).thenReturn(grid);
        when(sim.getCellFactory()).thenReturn(factory);
        when(sim.getSchedule()).thenReturn(mock(sim.engine.Schedule.class));
        when(sim.getID()).thenReturn(42);

        // Wire cell
        when(stemCell.getLocation()).thenReturn(stemLoc);
        when(stemCell.getParameters()).thenReturn(parameters);
        when(stemLoc.split(eq(random), any(Plane.class))).thenReturn(daughterLoc);

        // Default centroid and volume values (sometimes overridden in tests)
        when(stemLoc.getVolume()).thenReturn(10.0);
        when(daughterLoc.getVolume()).thenReturn(5.0);
        when(stemLoc.getCentroid()).thenReturn(new double[] {0, 1.0, 0});
        when(daughterLoc.getCentroid()).thenReturn(new double[] {0, 1.6, 0});

        // Parameter stubs (sometimes overridden in tests)
        when(parameters.getDistribution("proliferation/DIV_ROTATION_DISTRIBUTION"))
                .thenReturn(dist);
        when(dist.nextDouble()).thenReturn(0.1);
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET")).thenReturn("volume");
        when(parameters.getDouble("proliferation/DIFFERENTIATION_RULESET_EQUALITY_RANGE"))
                .thenReturn(0.5);
        when(parameters.getString("proliferation/HAS_DETERMINISTIC_DIFFERENTIATION"))
                .thenReturn("TRUE");

        // Link selection
        GrabBag links = mock(GrabBag.class);
        when(stemCell.getLinks()).thenReturn(links);
        when(links.next(random)).thenReturn(2);

        // Other defaults
        stemCellPop = 3;
        when(stemCell.getPop()).thenReturn(stemCellPop);
        when(stemCell.getCriticalVolume()).thenReturn(100.0);
    }

    @AfterEach
    final void tearDown() {
        Mockito.framework().clearInlineMocks();
    }

    // Constructor tests

    @Test
    public void getMUDDivisionPlane_returnsRotatedPlaneWithCorrectNormal() {
        Vector apicalAxis = new Vector(0, 1, 0);
        when(stemCell.getApicalAxis()).thenReturn(apicalAxis);

        Vector expectedNormal = new Vector(1.0, 0.0, 0.0);

        Voxel splitVoxel = new Voxel(7, 8, 9);
        ArrayList<Integer> expectedOffset = new ArrayList<>();
        expectedOffset.add(50); // MUDMUT x offset percent
        expectedOffset.add(50); // MUDMUT y offset percent
        when(stemLoc.getOffsetInApicalFrame(any(), any())).thenReturn(splitVoxel);

        module = new PottsModuleFlyStemProliferationMM(stemCell);
        Plane result = module.getMUDDivisionPlane(stemCell);

        assertEquals(new Double3D(7, 8, 9), result.getReferencePoint());
        Vector resultNormal = result.getUnitNormalVector();
        assertEquals(expectedNormal.getX(), resultNormal.getX(), EPSILON);
        assertEquals(expectedNormal.getY(), resultNormal.getY(), EPSILON);
        assertEquals(expectedNormal.getZ(), resultNormal.getZ(), EPSILON);
    }

    @Test
    public void chooseDivisionPlane_MUDMUT_withLowOffset_callsWTLikeVariant() {
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.MUDMUT);
        when(dist.nextDouble()).thenReturn(10.0); // abs(offset) < 45 → WT logic

        module = spy(new PottsModuleFlyStemProliferationMM(stemCell));

        Plane expectedPlane = mock(Plane.class);
        doReturn(expectedPlane)
                .when(module)
                .getWTLikeDivisionPlaneWithRotationalVariance(stemCell, 10.0);

        Plane result = module.chooseDivisionPlane(stemCell);

        assertEquals(expectedPlane, result);
        verify(module).getWTLikeDivisionPlaneWithRotationalVariance(stemCell, 10.0);
        verify(module, never()).getMUDDivisionPlane(any());
    }

    @Test
    public void chooseDivisionPlane_MUDMUT_withHighOffset_callsMUDVariant() {
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.MUDMUT);
        when(dist.nextDouble()).thenReturn(60.0); // abs(offset) ≥ 45 → MUD logic

        module = spy(new PottsModuleFlyStemProliferationMM(stemCell));

        Plane expectedPlane = mock(Plane.class);
        doReturn(expectedPlane).when(module).getMUDDivisionPlane(stemCell);

        Plane result = module.chooseDivisionPlane(stemCell);

        assertEquals(expectedPlane, result);
        verify(module).getMUDDivisionPlane(stemCell);
        verify(module, never()).getWTLikeDivisionPlaneWithRotationalVariance(any(), anyDouble());
    }

    @Test
    void testDaughterStem_RuleBased_VolumeTrue() {
        when(parameters.getString("proliferation/HAS_DETERMINISTIC_DIFFERENTIATION"))
                .thenReturn("FALSE");
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET")).thenReturn("volume");
        when(parameters.getDouble("proliferation/DIFFERENTIATION_RULESET_EQUALITY_RANGE"))
                .thenReturn(10.0); // large enough for |10 - 5| < 10

        PottsModuleFlyStemProliferation module = new PottsModuleFlyStemProliferationMM(stemCell);

        boolean result = module.daughterStem(stemLoc, daughterLoc, mock(Plane.class), 0, 0);

        assertTrue(result, "Expected true since |10-5| < range");
    }

    @Test
    void testDaughterStem_RuleBased_VolumeFalse() {
        when(parameters.getString("proliferation/HAS_DETERMINISTIC_DIFFERENTIATION"))
                .thenReturn("FALSE");
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET")).thenReturn("volume");
        when(parameters.getDouble("proliferation/DIFFERENTIATION_RULESET_EQUALITY_RANGE"))
                .thenReturn(1.0); // |10 - 5| = 5 > 1

        PottsModuleFlyStemProliferation module = new PottsModuleFlyStemProliferationMM(stemCell);

        boolean result = module.daughterStem(stemLoc, daughterLoc, mock(Plane.class), 0, 0);

        assertFalse(result, "Expected false since |10-5| > range");
    }
}
