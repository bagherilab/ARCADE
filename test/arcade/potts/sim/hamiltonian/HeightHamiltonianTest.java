package arcade.potts.sim.hamiltonian;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import org.junit.jupiter.api.Test;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.env.location.PottsLocation;
import arcade.potts.env.location.Voxel;
import arcade.potts.sim.PottsSeries;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.core.util.MiniBox.TAG_SEPARATOR;
import static arcade.potts.sim.PottsSeries.TARGET_SEPARATOR;
import static arcade.potts.util.PottsEnums.Region;

public class HeightHamiltonianTest {
    private static final double EPSILON = 1E-10;

    @Test
    public void constructor_called_initializesMaps() {
        HeightHamiltonian hh = new HeightHamiltonian(mock(PottsSeries.class));
        assertNotNull(hh.configs);
        assertNotNull(hh.popToLambda);
        assertNotNull(hh.popToLambdasRegion);
    }

    @Test
    public void constructor_called_initializesParameters() {
        PottsSeries series = mock(PottsSeries.class);

        series.potts = new MiniBox();
        series.populations = new HashMap<>();

        String key1 = randomString();
        int code1 = randomIntBetween(1, 10);
        MiniBox population1 = new MiniBox();
        population1.put("CODE", code1);
        series.populations.put(key1, population1);

        String key2 = randomString();
        int code2 = code1 + randomIntBetween(1, 10);
        MiniBox population2 = new MiniBox();
        population2.put("CODE", code2);
        population2.put("(REGION)" + TAG_SEPARATOR + Region.DEFAULT.name(), 0);
        population2.put("(REGION)" + TAG_SEPARATOR + Region.NUCLEUS.name(), 0);
        series.populations.put(key2, population2);

        double lambda1 = randomDoubleBetween(1, 100);
        double lambda2 = randomDoubleBetween(1, 100);

        series.potts.put("height/LAMBDA" + TARGET_SEPARATOR + key1, lambda1);
        series.potts.put("height/LAMBDA" + TARGET_SEPARATOR + key2, lambda2);

        double lambdaNucleus = randomDoubleBetween(1, 100);
        double lambdaDefault = randomDoubleBetween(1, 100);
        series.potts.put(
                "height/LAMBDA_" + Region.DEFAULT.name() + TARGET_SEPARATOR + key2, lambdaDefault);
        series.potts.put(
                "height/LAMBDA_" + Region.NUCLEUS.name() + TARGET_SEPARATOR + key2, lambdaNucleus);

        HeightHamiltonian hh = new HeightHamiltonian(series);

        assertEquals(2, hh.popToLambda.size());
        assertTrue(hh.popToLambda.containsKey(code1));
        assertTrue(hh.popToLambda.containsKey(code2));
        assertEquals(lambda1, hh.popToLambda.get(code1), EPSILON);
        assertEquals(lambda2, hh.popToLambda.get(code2), EPSILON);
        assertNull(hh.popToLambdasRegion.get(code1));
        assertEquals(lambdaDefault, hh.popToLambdasRegion.get(code2).get(Region.DEFAULT), EPSILON);
        assertEquals(lambdaNucleus, hh.popToLambdasRegion.get(code2).get(Region.NUCLEUS), EPSILON);
    }

    @Test
    public void register_noRegions_addsConfig() {
        HeightHamiltonian hh = new HeightHamiltonian(mock(PottsSeries.class));
        PottsCell cell = mock(PottsCell.class);

        int id = randomIntBetween(1, 10);
        int pop = randomIntBetween(1, 10);

        doReturn(id).when(cell).getID();
        doReturn(pop).when(cell).getPop();

        double lambda = randomDoubleBetween(1, 100);
        EnumMap<Region, Double> lambdasRegion = null;
        hh.popToLambda.put(pop, lambda);
        hh.popToLambdasRegion.put(pop, lambdasRegion);

        hh.register(cell);
        HeightHamiltonianConfig config = hh.configs.get(id);

        assertNotNull(config);
        assertEquals(cell, config.cell);
        assertEquals(lambda, config.getLambda(), EPSILON);
        assertEquals(Double.NaN, config.getLambda(Region.UNDEFINED), EPSILON);
    }

    @Test
    public void register_withRegions_addsConfig() {
        HeightHamiltonian hh = new HeightHamiltonian(mock(PottsSeries.class));
        PottsCell cell = mock(PottsCell.class);

        int id = randomIntBetween(1, 10);
        int pop = randomIntBetween(1, 10);

        doReturn(id).when(cell).getID();
        doReturn(pop).when(cell).getPop();

        double lambda = randomDoubleBetween(1, 100);
        double lambdaNucleus = randomDoubleBetween(1, 100);
        EnumMap<Region, Double> lambdasRegion = new EnumMap<>(Region.class);
        lambdasRegion.put(Region.NUCLEUS, lambdaNucleus);

        hh.popToLambda.put(pop, lambda);
        hh.popToLambdasRegion.put(pop, lambdasRegion);

        hh.register(cell);
        HeightHamiltonianConfig config = hh.configs.get(id);

        assertNotNull(config);
        assertEquals(cell, config.cell);
        assertEquals(lambda, config.getLambda(), EPSILON);
        assertEquals(lambdaNucleus, config.getLambda(Region.NUCLEUS), EPSILON);
    }

    @Test
    public void deregister_exists_removesConfig() {
        HeightHamiltonian hh = new HeightHamiltonian(mock(PottsSeries.class));
        PottsCell cell = mock(PottsCell.class);

        int id = randomIntBetween(1, 10);
        doReturn(id).when(cell).getID();

        HeightHamiltonianConfig config = mock(HeightHamiltonianConfig.class);
        hh.configs.put(id, config);

        hh.deregister(cell);

        assertFalse(hh.configs.containsKey(id));
    }

    @Test
    public void getDelta_validIDs_calculatesValue() {
        HeightHamiltonian hh = spy(new HeightHamiltonian(mock(PottsSeries.class)));
        Voxel voxel = new Voxel(0, 0, 0);
        int id1 = randomIntBetween(1, 100);
        int id2 = id1 + randomIntBetween(1, 10);

        double cell1 = randomDoubleBetween(1, 100);
        doReturn(cell1).when(hh).getHeight(id1, voxel, 0);

        double cell1plus1 = randomDoubleBetween(1, 100);
        doReturn(cell1plus1).when(hh).getHeight(id1, voxel, 1);

        double cell1minus1 = randomDoubleBetween(1, 100);
        doReturn(cell1minus1).when(hh).getHeight(id1, voxel, -1);

        double cell2 = randomDoubleBetween(1, 100);
        doReturn(cell2).when(hh).getHeight(id2, voxel, 0);

        double cell2plus1 = randomDoubleBetween(1, 100);
        doReturn(cell2plus1).when(hh).getHeight(id2, voxel, 1);

        double cell2minus1 = randomDoubleBetween(1, 100);
        doReturn(cell2minus1).when(hh).getHeight(id2, voxel, -1);

        assertEquals(
                (cell1minus1 - cell1 + cell2plus1 - cell2),
                hh.getDelta(id1, id2, 0, 0, 0),
                EPSILON);
        assertEquals(
                (cell2minus1 - cell2 + cell1plus1 - cell1),
                hh.getDelta(id2, id1, 0, 0, 0),
                EPSILON);
    }

    @Test
    public void getDelta_validRegions_calculatesValue() {
        HeightHamiltonian hh = spy(new HeightHamiltonian(mock(PottsSeries.class)));
        Voxel voxel = new Voxel(0, 0, 0);
        int id = randomIntBetween(1, 100);

        double region = randomDoubleBetween(1, 100);
        doReturn(0.0).when(hh).getHeight(id, voxel, Region.DEFAULT.ordinal(), 0);
        doReturn(region).when(hh).getHeight(id, voxel, Region.NUCLEUS.ordinal(), 0);

        double regionplus1 = randomDoubleBetween(1, 100);
        doReturn(0.0).when(hh).getHeight(id, voxel, Region.DEFAULT.ordinal(), 1);
        doReturn(regionplus1).when(hh).getHeight(id, voxel, Region.NUCLEUS.ordinal(), 1);

        double regionminus1 = randomDoubleBetween(1, 100);
        doReturn(0.0).when(hh).getHeight(id, voxel, Region.DEFAULT.ordinal(), -1);
        doReturn(regionminus1).when(hh).getHeight(id, voxel, Region.NUCLEUS.ordinal(), -1);

        assertEquals(
                (regionplus1 - region),
                hh.getDelta(id, Region.DEFAULT.ordinal(), Region.NUCLEUS.ordinal(), 0, 0, 0),
                EPSILON);
        assertEquals(
                (regionminus1 - region),
                hh.getDelta(id, Region.NUCLEUS.ordinal(), Region.DEFAULT.ordinal(), 0, 0, 0),
                EPSILON);
    }

    @Test
    public void getHeight_validID_calculatesValue() {
        HeightHamiltonian hh = new HeightHamiltonian(mock(PottsSeries.class));
        int id = randomIntBetween(1, 100);

        int height = randomIntBetween(10, 20);
        double criticalHeight = randomDoubleBetween(10, 20);

        ArrayList<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(0, 0, 0));
        voxels.add(new Voxel(0, 0, 1));
        voxels.add(new Voxel(0, 0, height - 1));
        voxels.add(new Voxel(0, 1, 0));
        voxels.add(new Voxel(1, 0, 0));
        voxels.add(new Voxel(1, 1, 0));
        voxels.add(new Voxel(1, 1, 2));

        PottsCell cell = mock(PottsCell.class);
        PottsLocation location = mock(PottsLocation.class);
        doReturn(criticalHeight).when(cell).getCriticalHeight();
        doReturn(voxels).when(location).getVoxels();

        HeightHamiltonianConfig config = mock(HeightHamiltonianConfig.class);

        try {
            Field cellField = HeightHamiltonianConfig.class.getDeclaredField("cell");
            cellField.setAccessible(true);
            cellField.set(config, cell);

            Field locationField = HeightHamiltonianConfig.class.getDeclaredField("location");
            locationField.setAccessible(true);
            locationField.set(config, location);
        } catch (Exception ignored) {
        }

        double lambda = randomDoubleBetween(10, 100);
        doReturn(lambda).when(config).getLambda();

        hh.configs.put(id, config);

        assertEquals(
                lambda * Math.pow(height - criticalHeight, 2),
                hh.getHeight(id, new Voxel(0, 0, 0), 0),
                EPSILON);
        assertEquals(
                lambda * Math.pow(3 - criticalHeight, 2),
                hh.getHeight(id, new Voxel(1, 1, 1), 1),
                EPSILON);
        assertEquals(
                lambda * Math.pow(3 - criticalHeight, 2),
                hh.getHeight(id, new Voxel(1, 1, 1), -1),
                EPSILON);
        assertEquals(
                lambda * Math.pow(height - criticalHeight + 1, 2),
                hh.getHeight(id, new Voxel(0, 0, height), 1),
                EPSILON);
        assertEquals(
                lambda * Math.pow(height - criticalHeight - 1, 2),
                hh.getHeight(id, new Voxel(0, 0, 0), -1),
                EPSILON);
    }

    @Test
    public void getHeight_validRegions_calculatesValue() {
        HeightHamiltonian hh = new HeightHamiltonian(mock(PottsSeries.class));
        int id = randomIntBetween(1, 100);
        Region region = Region.NUCLEUS;

        int height = randomIntBetween(10, 20);
        double criticalHeight = randomDoubleBetween(10, 20);

        ArrayList<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(0, 0, 0));
        voxels.add(new Voxel(0, 0, 1));
        voxels.add(new Voxel(0, 0, height - 1));
        voxels.add(new Voxel(0, 1, 0));
        voxels.add(new Voxel(1, 0, 0));
        voxels.add(new Voxel(1, 1, 0));
        voxels.add(new Voxel(1, 1, 2));

        PottsCell cell = mock(PottsCell.class);
        PottsLocation location = mock(PottsLocation.class);
        doReturn(criticalHeight).when(cell).getCriticalHeight(region);
        doReturn(voxels).when(location).getVoxels(region);

        HeightHamiltonianConfig config = mock(HeightHamiltonianConfig.class);

        try {
            Field cellField = HeightHamiltonianConfig.class.getDeclaredField("cell");
            cellField.setAccessible(true);
            cellField.set(config, cell);

            Field locationField = HeightHamiltonianConfig.class.getDeclaredField("location");
            locationField.setAccessible(true);
            locationField.set(config, location);
        } catch (Exception ignored) {
        }

        double lambda = randomDoubleBetween(10, 100);
        doReturn(lambda).when(config).getLambda(region);

        hh.configs.put(id, config);

        assertEquals(
                lambda * Math.pow(height - criticalHeight, 2),
                hh.getHeight(id, new Voxel(0, 0, 0), region.ordinal(), 0),
                EPSILON);
        assertEquals(
                lambda * Math.pow(3 - criticalHeight, 2),
                hh.getHeight(id, new Voxel(1, 1, 1), region.ordinal(), 1),
                EPSILON);
        assertEquals(
                lambda * Math.pow(3 - criticalHeight, 2),
                hh.getHeight(id, new Voxel(1, 1, 1), region.ordinal(), -1),
                EPSILON);
        assertEquals(
                lambda * Math.pow(height - criticalHeight + 1, 2),
                hh.getHeight(id, new Voxel(0, 0, height), region.ordinal(), 1),
                EPSILON);
        assertEquals(
                lambda * Math.pow(height - criticalHeight - 1, 2),
                hh.getHeight(id, new Voxel(0, 0, 0), region.ordinal(), -1),
                EPSILON);
    }

    @Test
    public void getHeight_zeroID_returnsZero() {
        HeightHamiltonian hh = new HeightHamiltonian(mock(PottsSeries.class));
        Voxel voxel = new Voxel(0, 0, 0);
        assertEquals(0, hh.getHeight(0, voxel, 1), EPSILON);
        assertEquals(0, hh.getHeight(0, voxel, 0), EPSILON);
        assertEquals(0, hh.getHeight(0, voxel, -1), EPSILON);
    }

    @Test
    public void getHeight_defaultRegion_returnsZero() {
        HeightHamiltonian hh = new HeightHamiltonian(mock(PottsSeries.class));
        Voxel voxel = new Voxel(0, 0, 0);
        int id = randomIntBetween(1, 100);
        assertEquals(0, hh.getHeight(0, voxel, Region.DEFAULT.ordinal(), 1), EPSILON);
        assertEquals(0, hh.getHeight(0, voxel, Region.DEFAULT.ordinal(), 0), EPSILON);
        assertEquals(0, hh.getHeight(0, voxel, Region.DEFAULT.ordinal(), -1), EPSILON);
        assertEquals(0, hh.getHeight(id, voxel, Region.DEFAULT.ordinal(), 1), EPSILON);
        assertEquals(0, hh.getHeight(id, voxel, Region.DEFAULT.ordinal(), 0), EPSILON);
        assertEquals(0, hh.getHeight(id, voxel, Region.DEFAULT.ordinal(), -1), EPSILON);
    }
}
