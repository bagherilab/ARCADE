package arcade.potts.sim.hamiltonian;

import java.util.EnumMap;
import java.util.HashMap;
import org.junit.Test;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSeries;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.core.util.Enums.Region;
import static arcade.core.util.MiniBox.TAG_SEPARATOR;
import static arcade.potts.sim.PottsSeries.TARGET_SEPARATOR;

public class AdhesionHamiltonianTest {
    private static final double EPSILON = 1E-10;
    
    private static final double[] ADHESION_ID = {
            0,
            randomDoubleBetween(0, 10),
            randomDoubleBetween(0, 10)
    };
    
    private static final double[] ADHESION_REGION = {
            0,
            randomDoubleBetween(0, 10),
            randomDoubleBetween(0, 10)
    };
    
    static class AdhesionHamiltonianMock extends AdhesionHamiltonian {
        AdhesionHamiltonianMock(PottsSeries series, Potts potts) { super(series, potts); }
        
        @Override
        double getAdhesion(int id, int x, int y, int z) { return ADHESION_ID[id]; }
        
        @Override
        double getAdhesion(int id, int region, int x, int y, int z) { return ADHESION_REGION[region]; }
    }
    
    @Test
    public void constructor_called_initializesMaps() {
        AdhesionHamiltonianMock ahm = new AdhesionHamiltonianMock(mock(PottsSeries.class), mock(Potts.class));
        assertNotNull(ahm.configs);
        assertNotNull(ahm.popToAdhesion);
        assertNotNull(ahm.popToAdhesionRegion);
    }
    
    @Test
    public void constructor_called_setsArrays() {
        Potts potts = mock(Potts.class);
        int[][][] ids = new int[0][0][0];
        int[][][] regions = new int[0][0][0];
        potts.ids = ids;
        potts.regions = regions;
        
        AdhesionHamiltonianMock ahm = new AdhesionHamiltonianMock(mock(PottsSeries.class), potts);
        
        assertSame(ids, ahm.ids);
        assertSame(regions, ahm.regions);
    }
    
    @Test
    public void constructor_called_setsConfig() {
        AdhesionHamiltonianMock ahm = new AdhesionHamiltonianMock(mock(PottsSeries.class), mock(Potts.class));
        assertEquals(1, ahm.configs.size());
        assertNull(ahm.configs.get(0));
    }
    
    @Test
    public void constructor_called_initializesParameters() {
        PottsSeries series = mock(PottsSeries.class);
        
        series.potts = new MiniBox();
        series.populations = new HashMap<>();
        
        String key1 = randomString();
        MiniBox population1 = new MiniBox();
        population1.put("CODE", 1);
        series.populations.put(key1, population1);
        
        String key2 = randomString();
        MiniBox population2 = new MiniBox();
        population2.put("CODE", 2);
        population2.put("(REGION)" + TAG_SEPARATOR + Region.DEFAULT.name(), 0);
        population2.put("(REGION)" + TAG_SEPARATOR + Region.NUCLEUS.name(), 0);
        series.populations.put(key2, population2);
        
        double[] adhesion1 = new double[] {
                randomDoubleBetween(1, 100),
                randomDoubleBetween(1, 100),
                randomDoubleBetween(1, 100),
        };
        double[] adhesion2 = new double[] {
                randomDoubleBetween(1, 100),
                randomDoubleBetween(1, 100),
                randomDoubleBetween(1, 100),
        };
        
        series.potts.put("adhesion/ADHESION" + TARGET_SEPARATOR + key1 + TARGET_SEPARATOR + "*", adhesion1[0]);
        series.potts.put("adhesion/ADHESION" + TARGET_SEPARATOR + key1 + TARGET_SEPARATOR + key1, adhesion1[1]);
        series.potts.put("adhesion/ADHESION" + TARGET_SEPARATOR + key1 + TARGET_SEPARATOR + key2, adhesion1[2]);
        
        series.potts.put("adhesion/ADHESION" + TARGET_SEPARATOR + key2 + TARGET_SEPARATOR + "*", adhesion2[0]);
        series.potts.put("adhesion/ADHESION" + TARGET_SEPARATOR + key2 + TARGET_SEPARATOR + key1, adhesion2[1]);
        series.potts.put("adhesion/ADHESION" + TARGET_SEPARATOR + key2 + TARGET_SEPARATOR + key2, adhesion2[2]);
        
        double[] adhesionDefault = new double[] {
                randomDoubleBetween(1, 100),
                randomDoubleBetween(1, 100),
        };
        double[] adhesionNucleus = new double[] {
                randomDoubleBetween(1, 100),
                randomDoubleBetween(1, 100),
        };
        
        series.potts.put("adhesion/ADHESION_" + Region.DEFAULT.name() + TARGET_SEPARATOR
                + key2 + TARGET_SEPARATOR + Region.DEFAULT.name(), adhesionDefault[0]);
        series.potts.put("adhesion/ADHESION_" + Region.DEFAULT.name() + TARGET_SEPARATOR
                + key2 + TARGET_SEPARATOR + Region.NUCLEUS.name(), adhesionDefault[1]);
        series.potts.put("adhesion/ADHESION_" + Region.NUCLEUS.name() + TARGET_SEPARATOR
                + key2 + TARGET_SEPARATOR + Region.DEFAULT.name(), adhesionNucleus[0]);
        series.potts.put("adhesion/ADHESION_" + Region.NUCLEUS.name() + TARGET_SEPARATOR
                + key2 + TARGET_SEPARATOR + Region.NUCLEUS.name(), adhesionNucleus[1]);
        
        AdhesionHamiltonianMock ahm = new AdhesionHamiltonianMock(series, mock(Potts.class));
        
        assertEquals(2, ahm.popToAdhesion.size());
        assertTrue(ahm.popToAdhesion.containsKey(1));
        assertTrue(ahm.popToAdhesion.containsKey(2));
        assertEquals(adhesion1[0], ahm.popToAdhesion.get(1)[0], EPSILON);
        assertEquals(adhesion1[1], ahm.popToAdhesion.get(1)[1], EPSILON);
        assertEquals(adhesion1[2], ahm.popToAdhesion.get(1)[2], EPSILON);
        assertEquals(adhesion2[0], ahm.popToAdhesion.get(2)[0], EPSILON);
        assertEquals(adhesion2[1], ahm.popToAdhesion.get(2)[1], EPSILON);
        assertEquals(adhesion2[2], ahm.popToAdhesion.get(2)[2], EPSILON);
        assertNull(ahm.popToAdhesionRegion.get(1));
        assertEquals(adhesionDefault[0],
                ahm.popToAdhesionRegion.get(2).get(Region.DEFAULT).get(Region.DEFAULT), EPSILON);
        assertEquals(adhesionDefault[1],
                ahm.popToAdhesionRegion.get(2).get(Region.DEFAULT).get(Region.NUCLEUS), EPSILON);
        assertEquals(adhesionNucleus[0],
                ahm.popToAdhesionRegion.get(2).get(Region.NUCLEUS).get(Region.DEFAULT), EPSILON);
        assertEquals(adhesionNucleus[1],
                ahm.popToAdhesionRegion.get(2).get(Region.NUCLEUS).get(Region.NUCLEUS), EPSILON);
    }
    
    @Test
    public void register_noRegions_addsConfig() {
        AdhesionHamiltonianMock ahm = new AdhesionHamiltonianMock(mock(PottsSeries.class), mock(Potts.class));
        PottsCell cell = mock(PottsCell.class);
        
        int id = randomIntBetween(1, 10);
        int pop = randomIntBetween(1, 10);
        
        doReturn(id).when(cell).getID();
        doReturn(pop).when(cell).getPop();
    
        double[] adhesion = new double[] {
                randomDoubleBetween(1, 100),
                randomDoubleBetween(1, 100),
        };
        EnumMap<Region, EnumMap<Region, Double>> adhesionRegion = null;
        ahm.popToAdhesion.put(pop, adhesion);
        ahm.popToAdhesionRegion.put(pop, adhesionRegion);
    
        ahm.register(cell);
        AdhesionHamiltonianConfig config = ahm.configs.get(id);
        
        assertNotNull(config);
        assertEquals(cell, config.cell);
        assertEquals(adhesion[0], config.getAdhesion(0), EPSILON);
        assertEquals(adhesion[1], config.getAdhesion(1), EPSILON);
        assertEquals(Double.NaN, config.getAdhesion(Region.UNDEFINED, Region.UNDEFINED), EPSILON);
    }
    
    @Test
    public void register_withRegions_addsConfig() {
        AdhesionHamiltonianMock ahm = new AdhesionHamiltonianMock(mock(PottsSeries.class), mock(Potts.class));
        PottsCell cell = mock(PottsCell.class);
        
        int id = randomIntBetween(1, 10);
        int pop = randomIntBetween(1, 10);
        
        doReturn(id).when(cell).getID();
        doReturn(pop).when(cell).getPop();
    
        double[] adhesion = new double[] {
                randomDoubleBetween(1, 100),
                randomDoubleBetween(1, 100),
        };
        EnumMap<Region, Double> adhesionNucleus = new EnumMap<>(Region.class);
        adhesionNucleus.put(Region.NUCLEUS, randomDoubleBetween(1, 100));
        
        EnumMap<Region, EnumMap<Region, Double>> adhesionRegion = new EnumMap<>(Region.class);
        adhesionRegion.put(Region.NUCLEUS, adhesionNucleus);
    
        ahm.popToAdhesion.put(pop, adhesion);
        ahm.popToAdhesionRegion.put(pop, adhesionRegion);
    
        ahm.register(cell);
        AdhesionHamiltonianConfig config = ahm.configs.get(id);
        
        assertNotNull(config);
        assertEquals(cell, config.cell);
        assertEquals(adhesion[0], config.getAdhesion(0), EPSILON);
        assertEquals(adhesion[1], config.getAdhesion(1), EPSILON);
        assertEquals(adhesionNucleus.get(Region.NUCLEUS), config.getAdhesion(Region.NUCLEUS, Region.NUCLEUS), EPSILON);
    }
    
    @Test
    public void deregister_exists_removesConfig() {
        AdhesionHamiltonianMock ahm = new AdhesionHamiltonianMock(mock(PottsSeries.class), mock(Potts.class));
        PottsCell cell = mock(PottsCell.class);
        
        int id = randomIntBetween(1, 10);
        doReturn(id).when(cell).getID();
        
        AdhesionHamiltonianConfig config = mock(AdhesionHamiltonianConfig.class);
        ahm.configs.put(id, config);
        
        ahm.deregister(cell);
        
        assertFalse(ahm.configs.containsKey(id));
    }
    
    @Test
    public void getDelta_validIDs_calculatesValue() {
        AdhesionHamiltonianMock ahm = new AdhesionHamiltonianMock(mock(PottsSeries.class), mock(Potts.class));
        assertEquals(ADHESION_ID[1] - ADHESION_ID[2], ahm.getDelta(2, 1, 0, 0, 0), EPSILON);
        assertEquals(ADHESION_ID[2] - ADHESION_ID[1], ahm.getDelta(1, 2, 0, 0, 0), EPSILON);
    }
    
    @Test
    public void getDelta_validRegions_calculatesValue() {
        AdhesionHamiltonianMock ahm = new AdhesionHamiltonianMock(mock(PottsSeries.class), mock(Potts.class));
        assertEquals(ADHESION_REGION[1] - ADHESION_REGION[2], ahm.getDelta(1, 2, 1, 0, 0, 0), EPSILON);
        assertEquals(ADHESION_REGION[2] - ADHESION_REGION[1], ahm.getDelta(1, 1, 2, 0, 0, 0), EPSILON);
    }
}
