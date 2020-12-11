package arcade.potts.agent.cell;

import java.util.EnumMap;
import java.util.EnumSet;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.stubbing.Answer;
import sim.engine.Schedule;
import sim.engine.Stoppable;
import arcade.core.agent.module.*;
import arcade.core.env.loc.*;
import arcade.core.util.MiniBox;
import arcade.potts.agent.module.PottsModule;
import arcade.potts.agent.module.PottsModuleApoptosis;
import arcade.potts.agent.module.PottsModuleAutosis;
import arcade.potts.agent.module.PottsModuleNecrosis;
import arcade.potts.agent.module.PottsModuleProliferation;
import arcade.potts.agent.module.PottsModuleQuiescence;
import arcade.potts.env.loc.PottsLocation;
import arcade.potts.sim.PottsSimulation;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.TestUtilities.*;
import static arcade.core.util.Enums.Region;
import static arcade.core.util.Enums.State;
import static arcade.potts.agent.cell.PottsCellFactoryTest.*;
import static arcade.potts.util.PottsEnums.Ordering;
import static arcade.potts.util.PottsEnums.Phase;
import static arcade.potts.util.PottsEnums.Term;

public class PottsCellTest {
    private static final double VOLUME_SURFACE_RATIO = Math.random();
    static double lambdaVolume;
    static double lambdaSurface;
    static double adhesionTo0;
    static double adhesionTo1;
    static double adhesionTo2;
    static EnumMap<Term, Double> criticalsMock;
    static EnumMap<Term, Double> lambdasMock;
    static double[] adhesionMock;
    static EnumMap<Region, EnumMap<Term, Double>> criticalsRegionMock;
    static EnumMap<Region, EnumMap<Term, Double>> lambdasRegionMock;
    static EnumMap<Region, EnumMap<Region, Double>> adhesionRegionMock;
    static Location locationMock;
    static int locationVolume;
    static int locationSurface;
    static EnumMap<Region, Integer> locationRegionVolumes;
    static EnumMap<Region, Integer> locationRegionSurfaces;
    static int cellID = (int) (Math.random() * 10) + 1;
    static int cellPop = (int) (Math.random() * 10) + 1;
    static PottsCellMock cellDefault;
    static PottsCellMock cellWithRegions;
    static PottsCellMock cellWithoutRegions;
    static EnumSet<Region> regionList;
    static MiniBox parametersMock;
    
    @BeforeClass
    public static void setupMocks() {
        // Random parameters.
        parametersMock = mock(MiniBox.class);
        
        // Random lambda values.
        lambdaVolume = Math.random();
        lambdaSurface = Math.random();
        
        // Random adhesion values.
        adhesionTo0 = Math.random();
        adhesionTo1 = Math.random();
        adhesionTo2 = Math.random();
        
        locationMock = mock(PottsLocation.class);
        regionList = EnumSet.of(Region.DEFAULT, Region.NUCLEUS);
        when(locationMock.getRegions()).thenReturn(regionList);
        
        Answer<Double> answer = invocation -> {
            Double value = invocation.getArgument(0);
            return value * VOLUME_SURFACE_RATIO;
        };
        when(((PottsLocation) locationMock).convertVolume(anyDouble())).thenAnswer(answer);
        
        locationRegionVolumes = new EnumMap<>(Region.class);
        locationRegionSurfaces = new EnumMap<>(Region.class);
        
        // Random volumes and surfaces for regions.
        for (Region region : regionList) {
            locationRegionVolumes.put(region, (int) (Math.random() * 100));
            locationRegionSurfaces.put(region, (int) (Math.random() * 100));
            
            when(locationMock.getVolume(region)).thenReturn(locationRegionVolumes.get(region));
            when(locationMock.getSurface(region)).thenReturn(locationRegionSurfaces.get(region));
            
            locationVolume += locationRegionVolumes.get(region);
            locationSurface += locationRegionSurfaces.get(region);
        }
        
        when(locationMock.getVolume()).thenReturn(locationVolume);
        when(locationMock.getSurface()).thenReturn(locationSurface);
        
        criticalsMock = new EnumMap<>(Term.class);
        criticalsMock.put(Term.VOLUME, (double) locationVolume);
        criticalsMock.put(Term.SURFACE, (double) locationSurface);
        
        // Region criticals.
        criticalsRegionMock = new EnumMap<>(Region.class);
        for (Region region : regionList) {
            EnumMap<Term, Double> criticalsRegionTerms = new EnumMap<>(Term.class);
            criticalsRegionTerms.put(Term.VOLUME, (double) locationRegionVolumes.get(region));
            criticalsRegionTerms.put(Term.SURFACE, (double) locationRegionSurfaces.get(region));
            criticalsRegionMock.put(region, criticalsRegionTerms);
        }
        
        lambdasMock = new EnumMap<>(Term.class);
        lambdasMock.put(Term.VOLUME, lambdaVolume);
        lambdasMock.put(Term.SURFACE, lambdaSurface);
        
        // Random lambda values for regions.
        lambdasRegionMock = new EnumMap<>(Region.class);
        for (Region region : regionList) {
            EnumMap<Term, Double> lambdasRegionTerms = new EnumMap<>(Term.class);
            lambdasRegionTerms.put(Term.VOLUME, Math.random() * 100);
            lambdasRegionTerms.put(Term.SURFACE, Math.random() * 100);
            lambdasRegionMock.put(region, lambdasRegionTerms);
        }
        
        adhesionMock = new double[] { adhesionTo0, adhesionTo1, adhesionTo2 };
        
        // Random adhesion values for regions.
        adhesionRegionMock = new EnumMap<>(Region.class);
        for (Region region : regionList) {
            EnumMap<Region, Double> adhesionRegionTarget = new EnumMap<>(Region.class);
            for (Region target : regionList) {
                adhesionRegionTarget.put(target, Math.random() * 100);
            }
            adhesionRegionMock.put(region, adhesionRegionTarget);
        }
        
        cellDefault = make(cellID, cellPop, false);
        cellWithRegions = make(cellID, 1, true);
        cellWithoutRegions = make(cellID, 1, false);
    }
    
    public static class PottsCellMock extends PottsCell {
        public PottsCellMock(int id, int pop, Location location,
                             MiniBox parameters, double[] adhesion,
                             EnumMap<Term, Double> criticals, EnumMap<Term, Double> lambdas) {
            super(id, pop, location, parameters, adhesion, criticals, lambdas);
        }
        
        public PottsCellMock(int id, int pop, Location location,
                             MiniBox parameters, double[] adhesion,
                             EnumMap<Term, Double> criticals, EnumMap<Term, Double> lambdas,
                             EnumMap<Region, EnumMap<Term, Double>> criticalsRegion,
                             EnumMap<Region, EnumMap<Term, Double>> lambdasRegion,
                             EnumMap<Region, EnumMap<Region, Double>> adhesionRegion) {
            super(id, pop, location, parameters, adhesion, criticals, lambdas,
                    criticalsRegion, lambdasRegion, adhesionRegion);
        }
        
        public PottsCellMock(int id, int pop, State state, int age, Location location,
                             boolean regions, MiniBox parameters, double[] adhesion,
                             EnumMap<Term, Double> criticals, EnumMap<Term, Double> lambdas,
                             EnumMap<Region, EnumMap<Term, Double>> criticalsRegion,
                             EnumMap<Region, EnumMap<Term, Double>> lambdasRegion,
                             EnumMap<Region, EnumMap<Region, Double>> adhesionRegion) {
            super(id, pop, state, age, location, regions, parameters, adhesion,
                    criticals, lambdas, criticalsRegion, lambdasRegion, adhesionRegion);
        }
        
        
        
        public PottsCell make(int id, State state, Location location) {
            return new PottsCellMock(id, pop, state, 0, location, hasRegions, parameters,
                    adhesion, criticals, lambdas, criticalsRegion, lambdasRegion, adhesionRegion);
        }
    }
    
    static PottsCellMock make(int id, int pop, boolean regions) {
        return make(id, pop, locationMock, regions);
    }
    
    static PottsCellMock make(int id, int pop, Location location, boolean regions) {
        if (!regions) {
            return new PottsCellMock(id, pop, location,
                parametersMock, adhesionMock, criticalsMock, lambdasMock);
        } else {
            return new PottsCellMock(id, pop, location,
                parametersMock, adhesionMock, criticalsMock, lambdasMock,
                criticalsRegionMock, lambdasRegionMock, adhesionRegionMock);
        }
    }
    
    @Test
    public void getID_defaultConstructor_returnsValue() {
        assertEquals(cellID, cellDefault.getID());
    }
    
    @Test
    public void getPop_defaultConstructor_returnsValue() {
        assertEquals(cellPop, cellDefault.getPop());
    }
    
    @Test
    public void getPop_valueAssigned_returnsValue() {
        int pop = (int) (Math.random() * 100);
        PottsCellMock cell = make(cellID, pop, false);
        assertEquals(pop, cell.getPop());
    }
    
    @Test
    public void getState_defaultConstructor_returnsValue() {
        assertEquals(State.PROLIFERATIVE, cellDefault.getState());
    }
    
    @Test
    public void getState_valueAssigned_returnsValue() {
        State cellState = State.random(RANDOM);
        PottsCellMock cell = new PottsCellMock(cellID, 0, cellState, 0, locationMock,
                false, parametersMock, adhesionMock, criticalsMock, lambdasMock, null, null, null);
        assertEquals(cellState, cell.getState());
    }
    
    @Test
    public void getAge_defaultConstructor_returnsValue() {
        assertEquals(0, cellDefault.getAge());
    }
    
    @Test
    public void getAge_valueAssigned_returnsValue() {
        int cellAge = (int) (Math.random() * 100);
        PottsCellMock cell = new PottsCellMock(cellID, 0, State.QUIESCENT, cellAge, locationMock,
                false, parametersMock, adhesionMock, criticalsMock, lambdasMock, null, null, null);
        assertEquals(cellAge, cell.getAge());
    }
    
    @Test
    public void hasRegions_withoutRegions_returnsFalse() {
        assertFalse(cellWithoutRegions.hasRegions());
    }
    
    @Test
    public void hasRegions_withRegions_returnsTrue() {
        assertTrue(cellWithRegions.hasRegions());
    }
    
    @Test
    public void getLocation_defaultConstructor_returnsObject() {
        assertSame(locationMock, cellDefault.getLocation());
    }
    
    @Test
    public void getModule_defaultConstructor_returnsObject() {
        assertTrue(cellDefault.getModule() instanceof PottsModuleProliferation);
    }
    
    @Test
    public void getParameters_defaultConstructor_returnsObject() {
        assertSame(parametersMock, cellDefault.getParameters());
    }
    
    @Test
    public void getVolume_defaultConstructor_returnsValue() {
        assertEquals(locationVolume, cellDefault.getVolume());
    }
    
    @Test
    public void getVolume_validRegions_returnsValue() {
        for (Region region : regionList) {
            assertEquals((int) locationRegionVolumes.get(region), cellWithRegions.getVolume(region));
        }
    }
    
    @Test
    public void getVolume_nullRegion_returnsZero() {
        assertEquals(0, cellWithRegions.getVolume(null));
    }
    
    @Test
    public void getVolume_noRegions_returnsZero() {
        for (Region region : regionList) {
            assertEquals(0, cellWithoutRegions.getVolume(region));
        }
    }
    
    @Test
    public void getSurface_defaultConstructor_returnsValue() {
        assertEquals(locationSurface, cellDefault.getSurface());
    }
    
    @Test
    public void getSurface_validRegions_returnsValue() {
        for (Region region : regionList) {
            assertEquals((int) locationRegionSurfaces.get(region), cellWithRegions.getSurface(region));
        }
    }
    
    @Test
    public void getSurface_nullRegion_returnsZero() {
        assertEquals(0, cellWithRegions.getSurface(null));
    }
    
    @Test
    public void getSurface_noRegions_returnsZero() {
        for (Region region : regionList) {
            assertEquals(0, cellWithoutRegions.getSurface(region));
        }
    }
    
    @Test
    public void getTargetVolume_beforeInitialize_returnsZero() {
        assertEquals(0, cellDefault.getTargetVolume(), EPSILON);
    }
    
    @Test
    public void getTargetVolume_beforeInitializeValidRegion_returnsZero() {
        for (Region region : regionList) {
            assertEquals(0, cellWithRegions.getTargetVolume(region), EPSILON);
        }
    }
    
    @Test
    public void getTargetVolume_beforeInitializeInvalidRegion_returnsZero() {
        assertEquals(0, cellWithRegions.getTargetVolume(null), EPSILON);
    }
    
    @Test
    public void getTargetVolume_beforeInitializeNoRegions_returnsZero() {
        for (Region region : regionList) {
            assertEquals(0, cellWithoutRegions.getTargetVolume(region), EPSILON);
        }
    }
    
    @Test
    public void getTargetVolume_afterInitialize_returnsValue() {
        PottsCellMock cell = make(cellID, cellPop, false);
        cell.initialize(null, null);
        assertEquals(locationVolume, cell.getTargetVolume(), EPSILON);
    }
    
    @Test
    public void getTargetVolume_afterInitializeValidRegion_returnsValue() {
        PottsCellMock cell = make(cellID, 1, true);
        cell.initialize(null, null);
        for (Region region : regionList) {
            assertEquals(locationRegionVolumes.get(region), cell.getTargetVolume(region), EPSILON);
        }
    }
    
    @Test
    public void getTargetVolume_afterInitializeInvalidRegion_returnsZero() {
        PottsCellMock cell = make(cellID, 1, true);
        cell.initialize(null, null);
        assertEquals(0, cell.getTargetVolume(null), EPSILON);
        assertEquals(0, cell.getTargetVolume(Region.UNDEFINED), EPSILON);
    }
    
    @Test
    public void getTargetVolume_afterInitializeNoRegion_returnsZero() {
        PottsCellMock cell = make(cellID, 1, false);
        cell.initialize(null, null);
        for (Region region : regionList) {
            assertEquals(0, cell.getTargetVolume(region), EPSILON);
        }
    }
    
    @Test
    public void getTargetSurface_beforeInitialize_returnsZero() {
        assertEquals(0, cellDefault.getTargetSurface(), EPSILON);
    }
    
    @Test
    public void getTargetSurface_beforeInitializeValidRegion_returnsZero() {
        for (Region region : regionList) {
            assertEquals(0, cellWithRegions.getTargetSurface(region), EPSILON);
        }
    }
    
    @Test
    public void getTargetSurface_beforeInitializeInvalidRegion_returnsZero() {
        assertEquals(0, cellWithRegions.getTargetSurface(null), EPSILON);
    }
    
    @Test
    public void getTargetSurface_beforeInitializeNoRegions_returnsZero() {
        for (Region region : regionList) {
            assertEquals(0, cellWithoutRegions.getTargetSurface(region), EPSILON);
        }
    }
    
    @Test
    public void getTargetSurface_afterInitialize_returnsValue() {
        PottsCellMock cell = make(cellID, cellPop, false);
        cell.initialize(null, null);
        assertEquals(locationSurface, cell.getTargetSurface(), EPSILON);
    }
    
    @Test
    public void getTargetSurface_afterInitializeValidRegion_returnsValue() {
        PottsCellMock cell = make(cellID, 1, true);
        cell.initialize(null, null);
        for (Region region : regionList) {
            assertEquals(locationRegionSurfaces.get(region), cell.getTargetSurface(region), EPSILON);
        }
    }
    
    @Test
    public void getTargetSurface_afterInitializeInvalidRegion_returnsZero() {
        PottsCellMock cell = make(cellID, 1, true);
        cell.initialize(null, null);
        assertEquals(0, cell.getTargetSurface(null), EPSILON);
        assertEquals(0, cell.getTargetSurface(Region.UNDEFINED), EPSILON);
    }
    
    @Test
    public void getTargetSurface_afterInitializeNoRegion_returnsZero() {
        PottsCellMock cell = make(cellID, 1, false);
        cell.initialize(null, null);
        for (Region region : regionList) {
            assertEquals(0, cell.getTargetSurface(region), EPSILON);
        }
    }
    
    @Test
    public void getCriticalVolume_beforeInitialize_returnsValue() {
        assertEquals(locationVolume, cellDefault.getCriticalVolume(), EPSILON);
    }
    
    @Test
    public void getCriticalVolume_beforeInitializeValidRegion_returnsValue() {
        for (Region region : regionList) {
            assertEquals(locationRegionVolumes.get(region), cellWithRegions.getCriticalVolume(region), EPSILON);
        }
    }
    
    @Test
    public void getCriticalVolume_beforeInitializeInvalidRegion_returnsZero() {
        assertEquals(0, cellWithRegions.getCriticalVolume(null), EPSILON);
        assertEquals(0, cellWithRegions.getCriticalVolume(Region.UNDEFINED), EPSILON);
    }
    
    @Test
    public void getCriticalVolume_beforeInitializeNoRegions_returnsZero() {
        for (Region region : regionList) {
            assertEquals(0, cellWithoutRegions.getCriticalVolume(region), EPSILON);
        }
    }
    
    @Test
    public void getCriticalVolume_afterInitialize_returnsValue() {
        PottsCellMock cell = make(cellID, cellPop, false);
        cell.initialize(null, null);
        assertEquals(locationVolume, cell.getCriticalVolume(), EPSILON);
    }
    
    @Test
    public void getCriticalVolume_afterInitializeValidRegion_returnsValue() {
        PottsCellMock cell = make(cellID, 1, true);
        cell.initialize(null, null);
        for (Region region : regionList) {
            assertEquals(locationRegionVolumes.get(region), cell.getCriticalVolume(region), EPSILON);
        }
    }
    
    @Test
    public void getCriticalVolume_afterInitializeInvalidRegion_returnsZero() {
        PottsCellMock cell = make(cellID, 1, true);
        cell.initialize(null, null);
        assertEquals(0, cell.getCriticalVolume(null), EPSILON);
    }
    
    @Test
    public void getCriticalVolume_afterInitializeNoRegion_returnsZero() {
        PottsCellMock cell = make(cellID, 1, false);
        cell.initialize(null, null);
        for (Region region : regionList) {
            assertEquals(0, cell.getCriticalVolume(region), EPSILON);
        }
    }
    
    @Test
    public void getCriticalSurface_beforeInitialize_returnsZero() {
        assertEquals(locationSurface, cellDefault.getCriticalSurface(), EPSILON);
    }
    
    @Test
    public void getCriticalSurface_beforeInitializeValidRegion_returnsZero() {
        for (Region region : regionList) {
            assertEquals(locationRegionSurfaces.get(region), cellWithRegions.getCriticalSurface(region), EPSILON);
        }
    }
    
    @Test
    public void getCriticalSurface_beforeInitializeInvalidRegion_returnsZero() {
        assertEquals(0, cellWithRegions.getCriticalSurface(null), EPSILON);
        assertEquals(0, cellWithRegions.getCriticalSurface(Region.UNDEFINED), EPSILON);
    }
    
    @Test
    public void getCriticalSurface_beforeInitializeNoRegions_returnsZero() {
        for (Region region : regionList) {
            assertEquals(0, cellWithoutRegions.getCriticalSurface(region), EPSILON);
        }
    }
    
    @Test
    public void getCriticalSurface_afterInitialize_returnsValue() {
        PottsCellMock cell = make(cellID, cellPop, false);
        cell.initialize(null, null);
        assertEquals(locationSurface, cell.getCriticalSurface(), EPSILON);
    }
    
    @Test
    public void getCriticalSurface_afterInitializeValidRegion_returnsValue() {
        PottsCellMock cell = make(cellID, 1, true);
        cell.initialize(null, null);
        for (Region region : regionList) {
            assertEquals(locationRegionSurfaces.get(region), cell.getCriticalSurface(region), EPSILON);
        }
    }
    
    @Test
    public void getCriticalSurface_afterInitializeInvalidRegion_returnsZero() {
        PottsCellMock cell = make(cellID, 1, true);
        cell.initialize(null, null);
        assertEquals(0, cell.getCriticalSurface(null), EPSILON);
    }
    
    @Test
    public void getCriticalSurface_afterInitializeNoRegion_returnsZero() {
        PottsCellMock cell = make(cellID, 1, false);
        cell.initialize(null, null);
        for (Region region : regionList) {
            assertEquals(0, cell.getCriticalSurface(region), EPSILON);
        }
    }
    
    @Test
    public void getLambda_givenTerm_returnsValue() {
        assertEquals(lambdaVolume, cellDefault.getLambda(Term.VOLUME), EPSILON);
        assertEquals(lambdaSurface, cellDefault.getLambda(Term.SURFACE), EPSILON);
    }
    
    @Test
    public void getLambda_givenTermValidRegions_returnsValue() {
        for (Region region : regionList) {
            assertEquals(lambdasRegionMock.get(region).get(Term.VOLUME),
                    cellWithRegions.getLambda(Term.VOLUME, region), EPSILON);
            assertEquals(lambdasRegionMock.get(region).get(Term.SURFACE),
                    cellWithRegions.getLambda(Term.SURFACE, region), EPSILON);
        }
    }
    
    @Test
    public void getLambda_givenTermInvalidRegions_returnsNaN() {
        assertEquals(Double.NaN, cellWithRegions.getLambda(Term.VOLUME, null), EPSILON);
        assertEquals(Double.NaN, cellWithRegions.getLambda(Term.SURFACE, null), EPSILON);
        assertEquals(Double.NaN, cellWithRegions.getLambda(Term.VOLUME, Region.UNDEFINED), EPSILON);
        assertEquals(Double.NaN, cellWithRegions.getLambda(Term.SURFACE, Region.UNDEFINED), EPSILON);
    }
    
    @Test
    public void getLambda_givenTermNoRegions_returnsNaN() {
        for (Region region : regionList) {
            assertEquals(Double.NaN, cellWithoutRegions.getLambda(Term.VOLUME, region), EPSILON);
            assertEquals(Double.NaN, cellWithoutRegions.getLambda(Term.SURFACE, region), EPSILON);
        }
    }
    
    @Test
    public void getAdhesion_givenPop_returnsValue() {
        assertEquals(adhesionTo0, cellDefault.getAdhesion(0), EPSILON);
        assertEquals(adhesionTo1, cellDefault.getAdhesion(1), EPSILON);
        assertEquals(adhesionTo2, cellDefault.getAdhesion(2), EPSILON);
    }
    
    @Test
    public void getAdhesion_validRegions_returnsValue() {
        for (Region region : regionList) {
            for (Region target : regionList) {
                assertEquals(adhesionRegionMock.get(region).get(target),
                        cellWithRegions.getAdhesion(region, target), EPSILON);
            }
        }
    }
    
    @Test
    public void getAdhesion_nullRegions_returnsNaN() {
        assertEquals(Double.NaN, cellWithRegions.getAdhesion(null, null), EPSILON);
        for (Region region : regionList) {
            assertEquals(Double.NaN, cellWithRegions.getAdhesion(region, null), EPSILON);
            assertEquals(Double.NaN, cellWithRegions.getAdhesion(null, region), EPSILON);
        }
    }
    
    @Test
    public void getAdhesion_invalidRegions_returnsNaN() {
        assertEquals(Double.NaN, cellWithRegions.getAdhesion(Region.UNDEFINED, Region.UNDEFINED), EPSILON);
        for (Region region : regionList) {
            assertEquals(Double.NaN, cellWithRegions.getAdhesion(region, Region.UNDEFINED), EPSILON);
            assertEquals(Double.NaN, cellWithRegions.getAdhesion(Region.UNDEFINED, region), EPSILON);
        }
    }
    
    @Test
    public void getAdhesion_noRegions_returnsNaN() {
        for (Region region : regionList) {
            for (Region target : regionList) {
                assertEquals(Double.NaN, cellWithoutRegions.getAdhesion(region, target), EPSILON);
            }
        }
    }
    
    @Test
    public void setState_givenState_assignsValue() {
        PottsCellMock cell = make(cellID, cellPop, false);
        
        cell.setState(State.QUIESCENT);
        assertEquals(State.QUIESCENT, cell.getState());
        
        cell.setState(State.PROLIFERATIVE);
        assertEquals(State.PROLIFERATIVE, cell.getState());
        
        cell.setState(State.APOPTOTIC);
        assertEquals(State.APOPTOTIC, cell.getState());
        
        cell.setState(State.NECROTIC);
        assertEquals(State.NECROTIC, cell.getState());
        
        cell.setState(State.AUTOTIC);
        assertEquals(State.AUTOTIC, cell.getState());
    }
    
    @Test
    public void setState_givenState_updatesModule() {
        PottsCellMock cell = make(cellID, cellPop, false);
        
        cell.setState(State.QUIESCENT);
        assertTrue(cell.module instanceof PottsModuleQuiescence);
        
        cell.setState(State.PROLIFERATIVE);
        assertTrue(cell.module instanceof PottsModuleProliferation);
        
        cell.setState(State.APOPTOTIC);
        assertTrue(cell.module instanceof PottsModuleApoptosis);
        
        cell.setState(State.NECROTIC);
        assertTrue(cell.module instanceof PottsModuleNecrosis);
        
        cell.setState(State.AUTOTIC);
        assertTrue(cell.module instanceof PottsModuleAutosis);
    }
    
    @Test
    public void setState_invalidState_setsNull() {
        PottsCellMock cell = make(cellID, cellPop, false);
        cell.setState(State.UNDEFINED);
        assertNull(cell.getModule());
    }
    
    @Test
    public void stop_called_callsMethod() {
        PottsCellMock cell = make(cellID, cellPop, false);
        cell.stopper = mock(Stoppable.class);
        cell.stop();
        verify(cell.stopper).stop();
    }
    
    @Test
    public void make_noRegions_setsFields() {
        int n = (int) (Math.random() * 10) + 2;
        EnumMap<Term, Double> criticals = new EnumMap<>(Term.class);
        EnumMap<Term, Double> lambdas = new EnumMap<>(Term.class);
        double[] adhesion = new double[n];
        MiniBox parameters = mock(MiniBox.class);
        Location location1 = mock(PottsLocation.class);
        Location location2 = mock(PottsLocation.class);
        
        PottsCell cell1 = new PottsCell(cellID, cellPop, location1, parameters,
                adhesion, criticals, lambdas);
        PottsCell cell2 = cell1.make(cellID + 1, State.QUIESCENT, location2);
        
        assertEquals(cellID + 1, cell2.id);
        assertEquals(cellPop, cell2.pop);
        assertEquals(0, cell2.getAge());
        assertFalse(cell2.hasRegions);
        assertEquals(location2, cell2.getLocation());
        assertEquals(cell2.parameters, parameters);
        assertEquals(cell2.criticals, criticals);
        assertEquals(cell2.lambdas, lambdas);
        assertArrayEquals(cell2.adhesion, adhesion, EPSILON);
    }
    
    @Test
    public void make_hasRegions_setsFields() {
        int n = (int) (Math.random() * 10) + 2;
        EnumMap<Term, Double> criticals = new EnumMap<>(Term.class);
        EnumMap<Term, Double> lambdas = new EnumMap<>(Term.class);
        double[] adhesion = new double[n];
        MiniBox parameters = mock(MiniBox.class);
        Location location1 = mock(PottsLocation.class);
        Location location2 = mock(PottsLocation.class);
        EnumMap<Region, EnumMap<Term, Double>> criticalsRegion = new EnumMap<>(Region.class);
        EnumMap<Region, EnumMap<Term, Double>> lambdasRegion = new EnumMap<>(Region.class);
        EnumMap<Region, EnumMap<Region, Double>> adhesionRegion = new EnumMap<>(Region.class);
        
        for (int i = 0; i < n; i++) {
            adhesion[i] = Math.random();
        }
        
        for (Region region : Region.values()) {
            criticalsRegion.put(region, new EnumMap<>(Term.class));
            lambdasRegion.put(region, new EnumMap<>(Term.class));
            adhesionRegion.put(region, new EnumMap<>(Region.class));
        }
        
        EnumSet<Region> allRegions = EnumSet.allOf(Region.class);
        doReturn(allRegions).when(location1).getRegions();
        doReturn(allRegions).when(location2).getRegions();
        
        PottsCell cell1 = new PottsCell(cellID, cellPop, location1, parameters,
                adhesion, criticals, lambdas, criticalsRegion, lambdasRegion, adhesionRegion);
        PottsCell cell2 = cell1.make(cellID + 1, State.QUIESCENT, location2);
        
        assertEquals(cellID + 1, cell2.id);
        assertEquals(cellPop, cell2.pop);
        assertEquals(0, cell2.getAge());
        assertTrue(cell2.hasRegions);
        assertEquals(location2, cell2.getLocation());
        assertEquals(cell2.parameters, parameters);
        assertEquals(cell2.criticals, criticals);
        assertEquals(cell2.lambdas, lambdas);
        assertArrayEquals(cell2.adhesion, adhesion, EPSILON);
        assertEquals(cell2.criticalsRegion, criticalsRegion);
        assertEquals(cell2.lambdasRegion, lambdasRegion);
        assertEquals(cell2.adhesionRegion, adhesionRegion);
    }
    
    @Test
    public void schedule_validInput_callsMethod() {
        Schedule schedule = spy(mock(Schedule.class));
        PottsCellMock cell = make(cellID, cellPop, false);
        doReturn(mock(Stoppable.class)).when(schedule).scheduleRepeating(cell, Ordering.CELLS.ordinal(), 1);
        cell.schedule(schedule);
        verify(schedule).scheduleRepeating(cell, Ordering.CELLS.ordinal(), 1);
    }
    
    @Test
    public void schedule_validInput_assignStopper() {
        Schedule schedule = spy(mock(Schedule.class));
        PottsCellMock cell = make(cellID, cellPop, false);
        doReturn(mock(Stoppable.class)).when(schedule).scheduleRepeating(cell, Ordering.CELLS.ordinal(), 1);
        cell.schedule(schedule);
        assertNotNull(cell.stopper);
    }
    
    @Test
    public void initialize_withoutRegions_callsMethod() {
        PottsLocation location = mock(PottsLocation.class);
        PottsCellMock cell = make(cellID, cellPop, location, false);
        int[][][] array = new int[1][3][3];
        cell.initialize(array, null);
        
        verify(location).update(cellID, array, null);
    }
    
    @Test
    public void initialize_withRegions_callsMethod() {
        PottsLocation location = mock(PottsLocation.class);
        when(location.getRegions()).thenReturn(regionList);
        PottsCellMock cell = make(cellID, 1, location, true);
        int[][][] array1 = new int[1][3][3];
        int[][][] array2 = new int[1][3][3];
        cell.initialize(array1, array2);
        
        verify(location).update(cellID, array1, array2);
    }
    
    @Test
    public void initialize_withoutRegions_updatesTargets() {
        int volume = (int) (Math.random() * 100);
        int surface = (int) (Math.random() * 100);
        PottsLocation location = mock(PottsLocation.class);
        when(location.getVolume()).thenReturn(volume);
        when(location.getSurface()).thenReturn(surface);
        
        PottsCellMock cell = make(cellID, cellPop, location, false);
        cell.initialize(new int[1][3][3], null);
        
        assertEquals(volume, cell.getTargetVolume(), EPSILON);
        assertEquals(surface, cell.getTargetSurface(), EPSILON);
        assertEquals(0, cell.getTargetVolume(Region.DEFAULT), EPSILON);
        assertEquals(0, cell.getTargetSurface(Region.DEFAULT), EPSILON);
    }
    
    @Test
    public void initialize_withRegions_updatesTargets() {
        int volume1 = (int) (Math.random() * 100);
        int volume2 = (int) (Math.random() * 100);
        int surface1 = (int) (Math.random() * 100);
        int surface2 = (int) (Math.random() * 100);
        Location location = mock(PottsLocation.class);
        when(location.getVolume()).thenReturn(volume1 + volume2);
        when(location.getSurface()).thenReturn(surface1 + surface2);
        when(location.getVolume(Region.DEFAULT)).thenReturn(volume1);
        when(location.getSurface(Region.DEFAULT)).thenReturn(surface1);
        when(location.getVolume(Region.NUCLEUS)).thenReturn(volume2);
        when(location.getSurface(Region.NUCLEUS)).thenReturn(surface2);
        when(location.getRegions()).thenReturn(regionList);
        
        PottsCellMock cell = make(cellID, 1, location, true);
        cell.initialize(new int[1][3][3], new int[1][3][3]);
        
        assertEquals(volume1 + volume2, cell.getTargetVolume(), EPSILON);
        assertEquals(surface1 + surface2, cell.getTargetSurface(), EPSILON);
        assertEquals(volume1, cell.getTargetVolume(Region.DEFAULT), EPSILON);
        assertEquals(surface1, cell.getTargetSurface(Region.DEFAULT), EPSILON);
        assertEquals(volume2, cell.getTargetVolume(Region.NUCLEUS), EPSILON);
        assertEquals(surface2, cell.getTargetSurface(Region.NUCLEUS), EPSILON);
        assertEquals(0, cell.getTargetVolume(Region.UNDEFINED), EPSILON);
        assertEquals(0, cell.getTargetSurface(Region.UNDEFINED), EPSILON);
    }
    
    @Test
    public void initialize_targetsSetWithoutRegions_doesNothing() {
        int volume = (int) (Math.random() * 100);
        int surface = (int) (Math.random() * 100);
        Location location = mock(PottsLocation.class);
        when(location.getVolume()).thenReturn(volume);
        when(location.getSurface()).thenReturn(surface);
        
        int targetVolume = (int) (Math.random() * 100) + 1;
        int targetSurface = (int) (Math.random() * 100) + 1;
        
        PottsCellMock cell = make(cellID, cellPop, location, false);
        cell.setTargets(targetVolume, targetSurface);
        cell.initialize(new int[1][3][3], null);
        
        assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
        assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
        assertEquals(0, cell.getTargetVolume(Region.DEFAULT), EPSILON);
        assertEquals(0, cell.getTargetSurface(Region.DEFAULT), EPSILON);
    }
    
    @Test
    public void initialize_targetsSetWithRegions_updatesTargets() {
        int volume1 = (int) (Math.random() * 100);
        int volume2 = (int) (Math.random() * 100);
        int surface1 = (int) (Math.random() * 100);
        int surface2 = (int) (Math.random() * 100);
        Location location = mock(PottsLocation.class);
        when(location.getVolume()).thenReturn(volume1 + volume2);
        when(location.getSurface()).thenReturn(surface1 + surface2);
        when(location.getVolume(Region.DEFAULT)).thenReturn(volume1);
        when(location.getSurface(Region.DEFAULT)).thenReturn(surface1);
        when(location.getVolume(Region.NUCLEUS)).thenReturn(volume2);
        when(location.getSurface(Region.NUCLEUS)).thenReturn(surface2);
        when(location.getRegions()).thenReturn(regionList);
        
        int targetVolume = (int) (Math.random() * 100) + 1;
        int targetSurface = (int) (Math.random() * 100) + 1;
        int targetRegionVolume1 = (int) (Math.random() * 100) + 1;
        int targetRegionSurface1 = (int) (Math.random() * 100) + 1;
        int targetRegionVolume2 = (int) (Math.random() * 100) + 1;
        int targetRegionSurface2 = (int) (Math.random() * 100) + 1;
        
        PottsCellMock cell = make(cellID, 1, location, true);
        cell.setTargets(targetVolume, targetSurface);
        cell.setTargets(Region.DEFAULT, targetRegionVolume1, targetRegionSurface1);
        cell.setTargets(Region.NUCLEUS, targetRegionVolume2, targetRegionSurface2);
        cell.initialize(new int[1][3][3], new int[1][3][3]);
        
        assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
        assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
        assertEquals(targetRegionVolume1, cell.getTargetVolume(Region.DEFAULT), EPSILON);
        assertEquals(targetRegionSurface1, cell.getTargetSurface(Region.DEFAULT), EPSILON);
        assertEquals(targetRegionVolume2, cell.getTargetVolume(Region.NUCLEUS), EPSILON);
        assertEquals(targetRegionSurface2, cell.getTargetSurface(Region.NUCLEUS), EPSILON);
        assertEquals(0, cell.getTargetVolume(Region.UNDEFINED), EPSILON);
        assertEquals(0, cell.getTargetSurface(Region.UNDEFINED), EPSILON);
    }
    
    @Test
    public void initialize_targetsMixed_updatesTargets() {
        int volume = (int) (Math.random() * 100);
        int surface = (int) (Math.random() * 100);
        Location location = mock(PottsLocation.class);
        when(location.getVolume()).thenReturn(volume);
        when(location.getSurface()).thenReturn(surface);
        
        PottsCellMock cell1 = make(cellID, cellPop, location, false);
        cell1.setTargets(0, (int) (Math.random() * 100));
        cell1.initialize(new int[1][3][3], null);
        
        assertEquals(volume, cell1.getTargetVolume(), EPSILON);
        assertEquals(surface, cell1.getTargetSurface(), EPSILON);
        
        PottsCellMock cell2 = make(cellID, cellPop, location, false);
        cell2.setTargets((int) (Math.random() * 100), 0);
        cell2.initialize(new int[1][3][3], null);
        
        assertEquals(volume, cell2.getTargetVolume(), EPSILON);
        assertEquals(surface, cell2.getTargetSurface(), EPSILON);
    }
    
    @Test
    public void reset_withoutRegions_callsMethod() {
        PottsLocation location = mock(PottsLocation.class);
        PottsCellMock cell = make(cellID, cellPop, location, false);
        int[][][] array = new int[1][3][3];
        cell.initialize(array, null);
        cell.reset(array, null);
        
        verify(location, times(2)).update(cellID, array, null);
    }
    
    @Test
    public void reset_withRegions_callsMethod() {
        PottsLocation location = mock(PottsLocation.class);
        when(location.getRegions()).thenReturn(regionList);
        PottsCellMock cell = make(cellID, 1, location, true);
        int[][][] array1 = new int[1][3][3];
        int[][][] array2 = new int[1][3][3];
        cell.initialize(array1, array2);
        cell.reset(array1, array2);
        
        verify(location, times(2)).update(cellID, array1, array2);
    }
    
    @Test
    public void reset_withoutRegions_updatesTargets() {
        PottsCellMock cell = make(cellID, cellPop, false);
        cell.initialize(new int[1][3][3], new int[1][3][3]);
        cell.updateTarget(Math.random(), Math.random());
        cell.reset(new int[1][3][3], null);
        
        assertEquals(locationVolume, cell.getTargetVolume(), EPSILON);
        assertEquals(locationSurface, cell.getTargetSurface(), EPSILON);
        assertEquals(0, cell.getTargetVolume(Region.DEFAULT), EPSILON);
        assertEquals(0, cell.getTargetSurface(Region.DEFAULT), EPSILON);
    }
    
    @Test
    public void reset_withRegions_updatesTargets() {
        PottsCellMock cell = make(cellID, 1, true);
        cell.initialize(new int[1][3][3], new int[1][3][3]);
        cell.updateTarget(Region.DEFAULT, Math.random(), Math.random());
        cell.updateTarget(Region.NUCLEUS, Math.random(), Math.random());
        cell.reset(new int[1][3][3], new int[1][3][3]);
        
        assertEquals(locationVolume, cell.getTargetVolume(), EPSILON);
        assertEquals(locationSurface, cell.getTargetSurface(), EPSILON);
        assertEquals(locationRegionVolumes.get(Region.DEFAULT), cell.getTargetVolume(Region.DEFAULT), EPSILON);
        assertEquals(locationRegionSurfaces.get(Region.DEFAULT), cell.getTargetSurface(Region.DEFAULT), EPSILON);
        assertEquals(locationRegionVolumes.get(Region.NUCLEUS), cell.getTargetVolume(Region.NUCLEUS), EPSILON);
        assertEquals(locationRegionSurfaces.get(Region.NUCLEUS), cell.getTargetSurface(Region.NUCLEUS), EPSILON);
    }
    
    @Test
    public void step_singleStep_updatesAge() {
        PottsCellMock cell = make(cellID, cellPop, false);
        PottsSimulation sim = mock(PottsSimulation.class);
        cell.module = mock(Module.class);
        
        cell.step(sim);
        assertEquals(1, cell.getAge(), EPSILON);
    }
    
    @Test
    public void setTargets_noRegions_updateValues() {
        double targetVolume = Math.random();
        double targetSurface = Math.random();
        PottsCellMock cell = make(cellID, cellPop, false);
        cell.setTargets(targetVolume, targetSurface);
        assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
        assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
    }
    
    @Test
    public void setTargets_withRegions_updateValues() {
        double targetVolume = Math.random();
        double targetSurface = Math.random();
        PottsCellMock cell = make(cellID, 1, true);
        cell.setTargets(Region.NUCLEUS, targetVolume, targetSurface);
        
        assertEquals(targetVolume, cell.getTargetVolume(Region.NUCLEUS), EPSILON);
        assertEquals(targetSurface, cell.getTargetSurface(Region.NUCLEUS), EPSILON);
        assertEquals(0, cell.getTargetVolume(Region.UNDEFINED), EPSILON);
        assertEquals(0, cell.getTargetSurface(Region.UNDEFINED), EPSILON);
        assertEquals(0, cell.getTargetVolume(Region.DEFAULT), EPSILON);
        assertEquals(0, cell.getTargetSurface(Region.DEFAULT), EPSILON);
    }
    
    @Test
    public void updateTarget_scaleTwoNoRegion_updatesValues() {
        double rate = Math.random();
        PottsCellMock cell = make(cellID, cellPop, false);
        cell.initialize(null, null);
        cell.updateTarget(rate, 2);
        
        double targetVolume = locationVolume + rate * (locationVolume);
        assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
        
        double targetSurface = VOLUME_SURFACE_RATIO * targetVolume;
        assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
    }
    
    @Test
    public void updateTarget_scaleTwoWithRegions_updatesValues() {
        double rate = Math.random();
        PottsCellMock cell = make(cellID, 1, true);
        cell.initialize(null, null);
        cell.updateTarget(rate, 2);
        
        double targetVolume = locationVolume + rate * (locationVolume);
        assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
        
        double targetSurface = VOLUME_SURFACE_RATIO * targetVolume;
        assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
    
        double criticalRegionVolume = criticalsRegionMock.get(Region.DEFAULT).get(Term.VOLUME);
        double targetRegionVolume = criticalRegionVolume - criticalsMock.get(Term.VOLUME) + targetVolume;
        assertEquals(targetRegionVolume, cell.getTargetVolume(Region.DEFAULT), EPSILON);
        
        double targetRegionSurface = VOLUME_SURFACE_RATIO * targetRegionVolume;
        assertEquals(targetRegionSurface, cell.getTargetSurface(Region.DEFAULT), EPSILON);
    }
    
    @Test
    public void updateTarget_scaleZeroNoRegion_updatesValues() {
        double rate = Math.random();
        PottsCellMock cell = make(cellID, cellPop, false);
        cell.initialize(null, null);
        cell.updateTarget(rate, 0);
        
        double targetVolume = locationVolume + rate * (-locationVolume);
        assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
        
        double targetSurface = VOLUME_SURFACE_RATIO * targetVolume;
        assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
    }
    
    @Test
    public void updateTarget_scaleZeroWithRegion_updatesValues() {
        double rate = Math.random();
        PottsCellMock cell = make(cellID, 1, true);
        cell.initialize(null, null);
        cell.updateTarget(rate, 0);
        
        double targetVolume = locationVolume + rate * (-locationVolume);
        assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
        
        double targetSurface = VOLUME_SURFACE_RATIO * targetVolume;
        assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
        
        double criticalRegionVolume = criticalsRegionMock.get(Region.DEFAULT).get(Term.VOLUME);
        double targetRegionVolume = criticalRegionVolume - criticalsMock.get(Term.VOLUME) + targetVolume;
        assertEquals(targetRegionVolume, cell.getTargetVolume(Region.DEFAULT), EPSILON);
        
        double targetRegionSurface = VOLUME_SURFACE_RATIO * targetRegionVolume;
        assertEquals(targetRegionSurface, cell.getTargetSurface(Region.DEFAULT), EPSILON);
    }
    
    @Test
    public void updateTarget_scaleTwoNoRegionModified_updatesValues() {
        double rate = Math.random();
        double delta = Math.random();
        PottsCellMock cell = make(cellID, cellPop, false);
        cell.initialize(null, null);
        cell.updateTarget(rate + delta, 2);
        cell.updateTarget(rate, 2);
        
        double targetVolume = locationVolume + (rate + delta) * (locationVolume);
        assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
        
        double targetSurface = VOLUME_SURFACE_RATIO * targetVolume;
        assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
    }
    
    @Test
    public void updateTarget_scaleTwoWithRegionsModified_updatesValues() {
        double rate = Math.random();
        double delta = Math.random();
        PottsCellMock cell = make(cellID, 1, true);
        cell.initialize(null, null);
        cell.updateTarget(rate + delta, 2);
        cell.updateTarget(rate, 2);
        
        double targetVolume = locationVolume + (rate + delta) * (locationVolume);
        assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
        
        double targetSurface = VOLUME_SURFACE_RATIO * targetVolume;
        assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
        
        double criticalRegionVolume = criticalsRegionMock.get(Region.DEFAULT).get(Term.VOLUME);
        double targetRegionVolume = criticalRegionVolume - criticalsMock.get(Term.VOLUME) + targetVolume;
        assertEquals(targetRegionVolume, cell.getTargetVolume(Region.DEFAULT), EPSILON);
        
        double targetRegionSurface = VOLUME_SURFACE_RATIO * targetRegionVolume;
        assertEquals(targetRegionSurface, cell.getTargetSurface(Region.DEFAULT), EPSILON);
    }
    
    @Test
    public void updateTarget_scaleZeroNoRegionModified_updatesValues() {
        double rate = Math.random();
        double delta = Math.random();
        PottsCellMock cell = make(cellID, cellPop, false);
        cell.initialize(null, null);
        cell.updateTarget(rate + delta, 0);
        cell.updateTarget(rate, 0);
        
        double targetVolume = locationVolume + (rate + delta) * (-locationVolume);
        assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
        
        double targetSurface = VOLUME_SURFACE_RATIO * targetVolume;
        assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
    }
    
    @Test
    public void updateTarget_scaleZeroWithRegionModified_updatesValues() {
        double rate = Math.random() + 1;
        double delta = Math.random() + 1;
        PottsCellMock cell = make(cellID, 1, true);
        cell.initialize(null, null);
        cell.updateTarget(rate + delta, 0);
        cell.updateTarget(rate, 0);
        
        double targetVolume = locationVolume + (rate + delta) * (-locationVolume);
        assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
        
        double targetSurface = VOLUME_SURFACE_RATIO * targetVolume;
        assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
        
        double criticalRegionVolume = criticalsRegionMock.get(Region.DEFAULT).get(Term.VOLUME);
        double targetRegionVolume = criticalRegionVolume - criticalsMock.get(Term.VOLUME) + targetVolume;
        assertEquals(targetRegionVolume, cell.getTargetVolume(Region.DEFAULT), EPSILON);
        
        double targetRegionSurface = VOLUME_SURFACE_RATIO * targetRegionVolume;
        assertEquals(targetRegionSurface, cell.getTargetSurface(Region.DEFAULT), EPSILON);
    }
    
    @Test
    public void updateTarget_regionScaleTwoWithRegion_updatesValues() {
        double rate = Math.random();
        PottsCellMock cell = make(cellID, 0, true);
        cell.initialize(null, null);
        cell.updateTarget(Region.DEFAULT, rate, 2);
        
        double locationRegionVolume = locationRegionVolumes.get(Region.DEFAULT);
        double targetRegionVolume = locationRegionVolume + rate * (locationRegionVolume);
        assertEquals(targetRegionVolume, cell.getTargetVolume(Region.DEFAULT), EPSILON);
        
        double targetRegionSurface = VOLUME_SURFACE_RATIO * targetRegionVolume;
        assertEquals(targetRegionSurface, cell.getTargetSurface(Region.DEFAULT), EPSILON);
        
        double criticalRegionVolume = criticalsRegionMock.get(Region.DEFAULT).get(Term.VOLUME);
        double targetVolume = criticalsMock.get(Term.VOLUME) - criticalRegionVolume + targetRegionVolume;
        assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
        
        double targetSurface = VOLUME_SURFACE_RATIO * targetVolume;
        assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
    }
    
    @Test
    public void updateTarget_regionScaleZeroWithRegion_updatesValues() {
        double rate = Math.random();
        PottsCellMock cell = make(cellID, 0, true);
        cell.initialize(null, null);
        cell.updateTarget(Region.DEFAULT, rate, 0);
        
        double locationRegionVolume = locationRegionVolumes.get(Region.DEFAULT);
        double targetRegionVolume = locationRegionVolume + rate * (-locationRegionVolume);
        assertEquals(targetRegionVolume, cell.getTargetVolume(Region.DEFAULT), EPSILON);
        
        double targetRegionSurface = VOLUME_SURFACE_RATIO * targetRegionVolume;
        assertEquals(targetRegionSurface, cell.getTargetSurface(Region.DEFAULT), EPSILON);
        
        double criticalRegionVolume = criticalsRegionMock.get(Region.DEFAULT).get(Term.VOLUME);
        double targetVolume = criticalsMock.get(Term.VOLUME) - criticalRegionVolume + targetRegionVolume;
        assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
        
        double targetSurface = VOLUME_SURFACE_RATIO * targetVolume;
        assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
    }
    
    @Test
    public void updateTarget_regionScaleTwoWithRegionModified_updatesValues() {
        double rate = Math.random();
        double delta = Math.random();
        PottsCellMock cell = make(cellID, 0, true);
        cell.initialize(null, null);
        cell.updateTarget(Region.DEFAULT, rate + delta, 2);
        cell.updateTarget(Region.DEFAULT, rate, 2);
        
        double locationRegionVolume = locationRegionVolumes.get(Region.DEFAULT);
        double targetRegionVolume = locationRegionVolume + (rate + delta) * locationRegionVolume;
        assertEquals(targetRegionVolume, cell.getTargetVolume(Region.DEFAULT), EPSILON);
        
        double targetRegionSurface = VOLUME_SURFACE_RATIO * targetRegionVolume;
        assertEquals(targetRegionSurface, cell.getTargetSurface(Region.DEFAULT), EPSILON);
        
        double criticalRegionVolume = criticalsRegionMock.get(Region.DEFAULT).get(Term.VOLUME);
        double targetVolume = criticalsMock.get(Term.VOLUME) - criticalRegionVolume + targetRegionVolume;
        assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
        
        double targetSurface = VOLUME_SURFACE_RATIO * targetVolume;
        assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
    }
    
    @Test
    public void updateTarget_regionScaleZeroWithRegionModified_updatesValues() {
        double rate = Math.random();
        double delta = Math.random();
        PottsCellMock cell = make(cellID, 0, true);
        cell.initialize(null, null);
        cell.updateTarget(Region.DEFAULT, rate + delta, 0);
        cell.updateTarget(Region.DEFAULT, rate, 0);
        
        double locationRegionVolume = locationRegionVolumes.get(Region.DEFAULT);
        double targetRegionVolume = locationRegionVolume + (rate + delta) * (-locationRegionVolume);
        assertEquals(targetRegionVolume, cell.getTargetVolume(Region.DEFAULT), EPSILON);
        
        double targetRegionSurface = VOLUME_SURFACE_RATIO * targetRegionVolume;
        assertEquals(targetRegionSurface, cell.getTargetSurface(Region.DEFAULT), EPSILON);
        
        double criticalRegionVolume = criticalsRegionMock.get(Region.DEFAULT).get(Term.VOLUME);
        double targetVolume = criticalsMock.get(Term.VOLUME) - criticalRegionVolume + targetRegionVolume;
        assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
        
        double targetSurface = VOLUME_SURFACE_RATIO * targetVolume;
        assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
    }
    
    @Test
    public void convert_noRegions_createsContainer() {
        Location location = mock(PottsLocation.class);
        MiniBox parameters = mock(MiniBox.class);
        
        int id = randomIntBetween(1, 10);
        int pop = randomIntBetween(1, 10);
        int age = randomIntBetween(1, 100);
        State state = State.random(RANDOM);
        Phase phase = Phase.random(RANDOM);
        EnumMap<Term, Double> criticals = makeEnumMap();
        EnumMap<Term, Double> lambdas = makeEnumMap();
        double[] adhesion = new double[] {
                randomDoubleBetween(0, 10),
                randomDoubleBetween(0, 10)
        };
        
        PottsCell cell = new PottsCell(id, pop, state, age, location,
                false, parameters, adhesion, criticals, lambdas, null, null, null);
        ((PottsModule) cell.getModule()).setPhase(phase);
        
        int voxels = randomIntBetween(1, 100);
        doReturn(voxels).when(location).getVolume();
        
        int targetVolume = randomIntBetween(1, 100);
        int targetSurface = randomIntBetween(1, 100);
        cell.setTargets(targetVolume, targetSurface);
        
        PottsCellContainer container = (PottsCellContainer) cell.convert();
        
        assertEquals(id, container.id);
        assertEquals(pop, container.pop);
        assertEquals(age, container.age);
        assertEquals(state, container.state);
        assertEquals(phase, container.phase);
        assertEquals(voxels, container.voxels);
        assertNull(container.regionVoxels);
        assertEquals(targetVolume, container.targetVolume, EPSILON);
        assertEquals(targetSurface, container.targetSurface, EPSILON);
        assertNull(container.regionTargetVolume);
        assertNull(container.regionTargetSurface);
    }
    
    @Test
    public void convert_withRegions_createsContainer() {
        Location location = mock(PottsLocation.class);
        MiniBox parameters = mock(MiniBox.class);
        
        int id = randomIntBetween(1, 10);
        int pop = randomIntBetween(1, 10);
        int age = randomIntBetween(1, 100);
        State state = State.random(RANDOM);
        Phase phase = Phase.random(RANDOM);
        EnumMap<Term, Double> criticals = makeEnumMap();
        EnumMap<Term, Double> lambdas = makeEnumMap();
        double[] adhesion = new double[] {
                randomDoubleBetween(0, 10),
                randomDoubleBetween(0, 10)
        };
        
        EnumSet<Region> regions = EnumSet.of(Region.NUCLEUS, Region.UNDEFINED);
        doReturn(regions).when(location).getRegions();
        
        EnumMap<Region, EnumMap<Term, Double>> criticalsRegion = makeEnumMapRegion(regions);
        EnumMap<Region, EnumMap<Term, Double>> lambdasRegion = makeEnumMapRegion(regions);
        EnumMap<Region, EnumMap<Region, Double>> adhesionRegion = makeEnumMapTarget(regions);
        
        PottsCell cell = new PottsCell(id, pop, state, age, location, true,
                parameters, adhesion, criticals, lambdas, criticalsRegion, lambdasRegion, adhesionRegion);
        ((PottsModule) cell.getModule()).setPhase(phase);
        
        int voxels = randomIntBetween(1, 100);
        doReturn(voxels).when(location).getVolume();
        
        EnumMap<Region, Integer> regionVoxels = new EnumMap<>(Region.class);
        for (Region region : regions) {
            int value = randomIntBetween(1, 100);
            regionVoxels.put(region, value);
            doReturn(value).when(location).getVolume(region);
        }
        
        int targetVolume = randomIntBetween(1, 100);
        int targetSurface = randomIntBetween(1, 100);
        cell.setTargets(targetVolume, targetSurface);
        
        EnumMap<Region, Integer> regionTargetVolume = new EnumMap<>(Region.class);
        EnumMap<Region, Integer> regionTargetSurface = new EnumMap<>(Region.class);
        for (Region region : regions) {
            int volume = randomIntBetween(1, 100);
            int surface = randomIntBetween(1, 100);
            regionTargetVolume.put(region, volume);
            regionTargetSurface.put(region, surface);
            cell.setTargets(region, volume, surface);
        }
        
        PottsCellContainer container = (PottsCellContainer) cell.convert();
        
        assertEquals(id, container.id);
        assertEquals(pop, container.pop);
        assertEquals(age, container.age);
        assertEquals(state, container.state);
        assertEquals(phase, container.phase);
        assertEquals(voxels, container.voxels);
        assertEquals(targetVolume, container.targetVolume, EPSILON);
        assertEquals(targetSurface, container.targetSurface, EPSILON);
        
        for (Region region : regions) {
            assertEquals(regionVoxels.get(region), container.regionVoxels.get(region));
            assertEquals(regionTargetVolume.get(region), container.regionTargetVolume.get(region), EPSILON);
            assertEquals(regionTargetSurface.get(region), container.regionTargetSurface.get(region), EPSILON);
        }
    }
}
