package arcade.potts.sim;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.junit.jupiter.api.Test;
import sim.engine.SimState;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.Cell;
import arcade.core.env.grid.Grid;
import arcade.core.env.location.Location;
import arcade.core.sim.Series;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.env.location.PottsLocation;
import arcade.potts.sim.hamiltonian.Hamiltonian;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.core.util.MiniBox.TAG_SEPARATOR;
import static arcade.potts.util.PottsEnums.Region;
import static arcade.potts.util.PottsEnums.Term;

public class PottsTest {
    private static final double EPSILON = 1E-10;
    
    private static final double TEMPERATURE = 10;
    
    private static final double R = randomDoubleBetween(0, 1);
    
    private static final double R_PLUS = Math.exp(-3 / TEMPERATURE) + EPSILON;
    
    private static final double R_MINUS = Math.exp(-3 / TEMPERATURE) - EPSILON;
    
    static class PottsMock extends Potts {
        PottsMock(PottsSeries series) { super(series); }
        
        @Override
        Hamiltonian getHamiltonian(Term term, PottsSeries series) {
            Hamiltonian hamiltonian = mock(Hamiltonian.class);
            doReturn(term.name()).when(hamiltonian).toString();
            return hamiltonian;
        }
        
        @Override
        boolean[][][] getNeighborhood(int id, int x, int y, int z) {
            return new boolean[][][] { { { x != 0 } } };
        }
        
        @Override
        boolean[][][] getNeighborhood(int id, int region, int x, int y, int z) {
            return new boolean[][][] { { { y != 0 } } };
        }
        
        @Override
        boolean getConnectivity(boolean[][][] array, boolean zero) {
            return array[0][0][0];
        }
        
        @Override
        HashSet<Integer> getUniqueIDs(int x, int y, int z) {
            HashSet<Integer> set = new HashSet<>();
            if (x == 0 && y == 0) {
                set.add(1);
                set.add(2);
            }
            return set;
        }
        
        @Override
        HashSet<Integer> getUniqueRegions(int x, int y, int z) {
            HashSet<Integer> set = new HashSet<>();
            if (x == 1 && y == 0) {
                set.add(Region.DEFAULT.ordinal());
                set.add(Region.NUCLEUS.ordinal());
            }
            return set;
        }
    }
    
    static PottsSeries makeSeries() {
        PottsSeries series = mock(PottsSeries.class);
        series.potts = mock(MiniBox.class);
        series.populations = mock(HashMap.class);
        doReturn(1.0).when(series.potts).getDouble("MCS");
        return series;
    }
    
    static PottsSeries makeSeries(int length, int width, int height) {
        return makeSeries(length, width, height, 1, 1);
    }
    
    static PottsSeries makeSeries(int length, int width, int height, double ds, double dt) {
        PottsSeries series = makeSeries();
        series.terms = new ArrayList<>();
        
        try {
            Field lengthField = Series.class.getDeclaredField("length");
            lengthField.setAccessible(true);
            lengthField.setInt(series, length);
            
            Field widthField = Series.class.getDeclaredField("width");
            widthField.setAccessible(true);
            widthField.setInt(series, width);
            
            Field heightField = Series.class.getDeclaredField("height");
            heightField.setAccessible(true);
            heightField.setInt(series, height);
            
            Field dsField = Series.class.getDeclaredField("ds");
            dsField.setAccessible(true);
            dsField.setDouble(series, ds);
            
            Field dtField = Series.class.getDeclaredField("dt");
            dtField.setAccessible(true);
            dtField.setDouble(series, dt);
        } catch (Exception ignored) { }
        
        return series;
    }
    
    static PottsMock makeFlipMock() {
        PottsSeries series = makeSeries(4, 4, 1);
        PottsMock spy = spy(new PottsMock(series));
        
        spy.regions[0][0][0] = Region.NUCLEUS.ordinal();
        spy.regions[0][0][1] = Region.NUCLEUS.ordinal();
        spy.regions[0][1][0] = Region.NUCLEUS.ordinal();
        spy.regions[0][1][1] = Region.NUCLEUS.ordinal();
        
        doNothing().when(spy).change(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble());
        doNothing().when(spy).change(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble());
        return spy;
    }
    
    static PottsMock makeChangeMock(int source, int target, double[] values, boolean hasRegions) {
        Grid grid = mock(Grid.class);
        PottsSeries series = makeSeries(3, 3, 1);
        PottsMock spy = spy(new PottsMock(series));
        spy.grid = grid;
        
        try {
            Field regionField = Potts.class.getDeclaredField("hasRegions");
            regionField.setAccessible(true);
            regionField.setBoolean(spy, hasRegions);
            
            Field tempField = Potts.class.getDeclaredField("temperature");
            tempField.setAccessible(true);
            tempField.setDouble(spy, TEMPERATURE);
        } catch (Exception ignored) { }
        
        for (double v : values) {
            Hamiltonian hamiltonian = mock(Hamiltonian.class);
            doReturn(v).when(hamiltonian).getDelta(source, target, 0, 0, 0);
            spy.hamiltonian.add(hamiltonian);
        }
        
        if (source != 0) {
            Cell cellSource = mock(PottsCell.class);
            Location locationSource = mock(PottsLocation.class);
            doReturn(locationSource).when(cellSource).getLocation();
            doReturn(cellSource).when(spy).getCell(source);
            doReturn(cellSource).when(grid).getObjectAt(source);
        }
        
        if (target != 0) {
            Cell cellTarget = mock(PottsCell.class);
            Location locationTarget = mock(PottsLocation.class);
            doReturn(locationTarget).when(cellTarget).getLocation();
            doReturn(cellTarget).when(spy).getCell(target);
            doReturn(cellTarget).when(grid).getObjectAt(target);
        }
        
        return spy;
    }
    
    static PottsMock makeChangeMock(int source, int target, double[] values) {
        Grid grid = mock(Grid.class);
        PottsSeries series = makeSeries(3, 3, 1);
        PottsMock spy = spy(new PottsMock(series));
        spy.grid = grid;
        
        try {
            Field tempField = Potts.class.getDeclaredField("temperature");
            tempField.setAccessible(true);
            tempField.setDouble(spy, TEMPERATURE);
        } catch (Exception ignored) { }
        
        for (double v : values) {
            Hamiltonian hamiltonian = mock(Hamiltonian.class);
            doReturn(v).when(hamiltonian).getDelta(1, source, target, 0, 0, 0);
            spy.hamiltonian.add(hamiltonian);
        }
        
        Cell cellSource = mock(PottsCell.class);
        Location locationSource = mock(PottsLocation.class);
        doReturn(locationSource).when(cellSource).getLocation();
        doReturn(cellSource).when(spy).getCell(1);
        doReturn(cellSource).when(grid).getObjectAt(1);
        
        return spy;
    }
    
    @Test
    public void constructor_2D_assignsValues() {
        int length = randomIntBetween(1, 100);
        int width = randomIntBetween(1, 100);
        PottsSeries series = makeSeries(length + 2, width + 2, 1);
        PottsMock pottsMock = new PottsMock(series);
        
        assertEquals(length, pottsMock.length);
        assertEquals(width, pottsMock.width);
        assertEquals(1, pottsMock.height);
        
        assertEquals(1, pottsMock.ids.length);
        assertEquals(1, pottsMock.regions.length);
        assertEquals(length + 2, pottsMock.ids[0].length);
        assertEquals(length + 2, pottsMock.regions[0].length);
        assertEquals(width + 2, pottsMock.ids[0][0].length);
        assertEquals(width + 2, pottsMock.regions[0][0].length);
    }
    
    @Test
    public void constructor_3D_assignsValues() {
        int length = randomIntBetween(1, 100);
        int width = randomIntBetween(1, 100);
        PottsSeries series = makeSeries(length + 2, width + 2, 4);
        PottsMock pottsMock = new PottsMock(series);
        
        assertEquals(length, pottsMock.length);
        assertEquals(width, pottsMock.width);
        assertEquals(2, pottsMock.height);
        
        assertEquals(4, pottsMock.ids.length);
        assertEquals(4, pottsMock.regions.length);
        assertEquals(length + 2, pottsMock.ids[0].length);
        assertEquals(length + 2, pottsMock.regions[0].length);
        assertEquals(width + 2, pottsMock.ids[0][0].length);
        assertEquals(width + 2, pottsMock.regions[0][0].length);
    }
    
    @Test
    public void constructor_givenSeries_setsFields() {
        int length = randomIntBetween(1, 100);
        int width = randomIntBetween(1, 100);
        int height = randomIntBetween(2, 100);
        int temperature = randomIntBetween(1, 100);
        double mcs = randomDoubleBetween(0, 10);
        double ds = randomDoubleBetween(2, 10);
        double dt = randomDoubleBetween(2, 10);
        
        PottsSeries series = makeSeries(length, width, height, ds, dt);
        series.potts = new MiniBox();
        series.potts.put("TEMPERATURE", temperature);
        series.potts.put("MCS", mcs);
        
        PottsMock pottsMock = new PottsMock(series);
        
        assertEquals((int) (mcs * (length - 2) * (width - 2) * (height - 2)), pottsMock.steps);
        assertEquals(temperature, pottsMock.temperature, EPSILON);
    }
    
    @Test
    public void constructor_noPopulations_setsFalse() {
        PottsSeries series = makeSeries(0, 0, 0);
        series.populations = new HashMap<>();
        
        PottsMock pottsMock = new PottsMock(series);
        assertFalse(pottsMock.hasRegions);
    }
    
    @Test
    public void constructor_noRegions_setsFalse() {
        PottsSeries series = makeSeries(0, 0, 0);
        series.populations = new HashMap<>();
        
        MiniBox popA = new MiniBox();
        MiniBox popB = new MiniBox();
        MiniBox popC = new MiniBox();
        
        series.populations.put("A", popA);
        series.populations.put("B", popB);
        series.populations.put("C", popC);
        
        PottsMock pottsMock = new PottsMock(series);
        assertFalse(pottsMock.hasRegions);
    }
    
    @Test
    public void constructor_withRegions_setsTrue() {
        PottsSeries series = makeSeries(0, 0, 0);
        series.populations = new HashMap<>();
        
        MiniBox popA = new MiniBox();
        MiniBox popB = new MiniBox();
        MiniBox popC = new MiniBox();
        
        series.populations.put("A", popA);
        series.populations.put("B", popB);
        series.populations.put("C", popC);
        
        popB.put("(REGION)" + TAG_SEPARATOR + "X", "0");
        
        PottsMock pottsMock = new PottsMock(series);
        assertTrue(pottsMock.hasRegions);
    }
    
    @Test
    public void constructor_withoutTerms_createEmpty() {
        PottsSeries series = makeSeries(0, 0, 0);
        PottsMock pottsMock = spy(new PottsMock(series));
        assertNotNull(pottsMock.hamiltonian);
        assertEquals(0, pottsMock.hamiltonian.size());
    }
    
    @Test
    public void constructor_withTerms_createsList() {
        PottsSeries series = makeSeries(0, 0, 0);
        
        int n = randomIntBetween(3, 10);
        ArrayList<Term> terms = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Term term = mock(Term.class);
            doReturn("term" + i).when(term).name();
            terms.add(term);
        }
        
        series.terms = terms;
        PottsMock pottsMock = spy(new PottsMock(series));
        
        assertEquals(n, pottsMock.hamiltonian.size());
        for (int i = 0; i < n; i++) {
            String termName = terms.get(i).name();
            assertEquals(termName, pottsMock.hamiltonian.get(i).toString());
        }
    }
    
    @Test
    public void register_called_callsMethods() {
        PottsSeries series = makeSeries(0, 0, 0);
        PottsMock pottsMock = new PottsMock(series);
        ArrayList<Hamiltonian> hamiltonian = new ArrayList<>();
        
        int n = randomIntBetween(3, 10);
        for (int i = 0; i < n; i++) {
            Hamiltonian h = mock(Hamiltonian.class);
            pottsMock.hamiltonian.add(h);
            hamiltonian.add(h);
        }
        
        PottsCell cell = mock(PottsCell.class);
        pottsMock.register(cell);
        
        for (int i = 0; i < n; i++) {
            verify(hamiltonian.get(i)).register(cell);
        }
    }
    
    @Test
    public void deregister_called_callsMethods() {
        PottsSeries series = makeSeries(0, 0, 0);
        PottsMock pottsMock = new PottsMock(series);
        ArrayList<Hamiltonian> hamiltonian = new ArrayList<>();
        
        int n = randomIntBetween(3, 10);
        for (int i = 0; i < n; i++) {
            Hamiltonian h = mock(Hamiltonian.class);
            pottsMock.hamiltonian.add(h);
            hamiltonian.add(h);
        }
        
        PottsCell cell = mock(PottsCell.class);
        pottsMock.deregister(cell);
        
        for (int i = 0; i < n; i++) {
            verify(hamiltonian.get(i)).deregister(cell);
        }
    }
    
    @Test
    public void step_2D_callsMethods() {
        MersenneTwisterFast random = new MersenneTwisterFast(1);
        SimState simstate = mock(SimState.class);
        simstate.random = random;
        
        int length = randomIntBetween(3, 10);
        int width = randomIntBetween(3, 10);
        
        PottsSeries series = makeSeries(length, width, 1);
        PottsMock spy = spy(new PottsMock(series));
        int steps = spy.length * spy.width * spy.height;
        
        spy.step(simstate);
        verify(spy, times(steps)).getUniqueIDs(
                intThat(i -> i < length - 1 && i > 0),
                intThat(i -> i < width - 1 && i > 0),
                eq(0));
        verify(spy, times(steps)).getUniqueRegions(
                intThat(i -> i < length - 1 && i > 0),
                intThat(i -> i < width - 1 && i > 0),
                eq(0));
    }
    
    @Test
    public void step_3D_callsMethods() {
        MersenneTwisterFast random = new MersenneTwisterFast(1);
        SimState simstate = mock(SimState.class);
        simstate.random = random;
        
        int length = randomIntBetween(3, 10);
        int width = randomIntBetween(3, 10);
        int height = randomIntBetween(4, 10);
        
        PottsSeries series = makeSeries(length, width, height);
        
        PottsMock spy = spy(new PottsMock(series));
        int steps = spy.length * spy.width * spy.height;
        
        spy.step(simstate);
        verify(spy, times(steps)).getUniqueIDs(
                intThat(i -> i < length - 1 && i > 0),
                intThat(i -> i < width - 1 && i > 0),
                intThat(i -> i < height - 1 && i > 0));
        verify(spy, times(steps)).getUniqueRegions(
                intThat(i -> i < length - 1 && i > 0),
                intThat(i -> i < width - 1 && i > 0),
                intThat(i -> i < height - 1 && i > 0));
    }
    
    @Test
    public void step_uniqueIDsHasNoRegions_callsMethods() {
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        when(random.nextInt(1)).thenReturn(-1);
        when(random.nextInt(2)).thenReturn(0);
        doReturn(R).when(random).nextDouble();
        SimState simstate = mock(SimState.class);
        simstate.random = random;
        
        PottsSeries series = makeSeries(3, 3, 1);
        
        PottsMock spy = spy(new PottsMock(series));
        spy.ids[0][0][0] = 1;
        
        PottsCell cell = mock(PottsCell.class);
        doReturn(false).when(cell).hasRegions();
        doReturn(cell).when(spy).getCell(1);
        
        doNothing().when(spy).flip(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble());
        doNothing().when(spy).flip(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble());
        
        spy.step(simstate);
        verify(spy).flip(1, 1, 0, 0, 0, R);
        verify(spy, never()).flip(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble());
    }
    
    @Test
    public void step_uniqueIDsHasRegions_callsMethods() {
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        when(random.nextInt(1)).thenReturn(-1);
        when(random.nextInt(2)).thenReturn(0);
        doReturn(R).when(random).nextDouble();
        SimState simstate = mock(SimState.class);
        simstate.random = random;
        
        PottsSeries series = makeSeries(3, 3, 1);
        
        PottsMock spy = spy(new PottsMock(series));
        spy.ids[0][0][0] = 1;
        
        PottsCell cell = mock(PottsCell.class);
        doReturn(true).when(cell).hasRegions();
        doReturn(cell).when(spy).getCell(1);
        
        doNothing().when(spy).flip(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble());
        doNothing().when(spy).flip(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble());
        
        spy.step(simstate);
        verify(spy).flip(1, 1, 0, 0, 0, R);
        verify(spy, never()).flip(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble());
    }
    
    @Test
    public void step_uniqueRegionsHasNoRegions_callsMethods() {
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        when(random.nextInt(1)).thenReturn(0).thenReturn(-1);
        when(random.nextInt(2)).thenReturn(1);
        doReturn(R).when(random).nextDouble();
        SimState simstate = mock(SimState.class);
        simstate.random = random;
        
        PottsSeries series = makeSeries(3, 3, 1);
        
        PottsMock spy = spy(new PottsMock(series));
        spy.ids[0][1][0] = 1;
        spy.regions[0][1][0] = Region.DEFAULT.ordinal();
        
        PottsCell cell = mock(PottsCell.class);
        doReturn(false).when(cell).hasRegions();
        doReturn(cell).when(spy).getCell(1);
        
        doNothing().when(spy).flip(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble());
        doNothing().when(spy).flip(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble());
        
        spy.step(simstate);
        verify(spy, never()).flip(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble());
        verify(spy, never()).flip(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble());
    }
    
    @Test
    public void step_uniqueRegionsHasRegions_callsMethods() {
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        when(random.nextInt(1)).thenReturn(0).thenReturn(-1);
        when(random.nextInt(2)).thenReturn(1);
        doReturn(R).when(random).nextDouble();
        SimState simstate = mock(SimState.class);
        simstate.random = random;
        
        PottsSeries series = makeSeries(3, 3, 1);
        
        PottsMock spy = spy(new PottsMock(series));
        spy.ids[0][1][0] = 1;
        spy.regions[0][1][0] = Region.DEFAULT.ordinal();
        
        PottsCell cell = mock(PottsCell.class);
        doReturn(true).when(cell).hasRegions();
        doReturn(cell).when(spy).getCell(1);
        
        doNothing().when(spy).flip(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble());
        doNothing().when(spy).flip(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble());
        
        spy.step(simstate);
        verify(spy, never()).flip(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble());
        verify(spy).flip(1, Region.DEFAULT.ordinal(), Region.NUCLEUS.ordinal(), 1, 0, 0, R);
    }
    
    @Test
    public void flip_unconnectedSourceID_returns() {
        PottsMock spy = makeFlipMock();
        spy.flip(1, 0, 0, 0, 0, R);
        verify(spy).getNeighborhood(1, 0, 0, 0);
        verify(spy, never()).change(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(R));
    }
    
    @Test
    public void flip_unconnectedSourceIDNeighbor_returns() {
        PottsMock spy = makeFlipMock();
        spy.ids[0][0][0] = 1;
        spy.flip(1, 0, 0, 0, 0, R);
        verify(spy).getNeighborhood(1, 0, 0, 0);
        verify(spy, never()).change(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(R));
    }
    
    @Test
    public void flip_connectedSourceUnconnectedSourceRegion_returns() {
        PottsMock spy = makeFlipMock();
        spy.flip(1, 0, 1, 0, 0, R);
        verify(spy).getNeighborhood(1, 1, 0, 0);
        verify(spy).getNeighborhood(1, Region.NUCLEUS.ordinal(), 1, 0, 0);
        verify(spy, never()).change(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(R));
    }
    
    @Test
    public void flip_connectedSourceConnectedSourceRegion_completes() {
        PottsMock spy = makeFlipMock();
        spy.flip(1, 0, 1, 1, 0, R);
        verify(spy).getNeighborhood(1, 1, 1, 0);
        verify(spy).getNeighborhood(1, Region.NUCLEUS.ordinal(), 1, 1, 0);
        verify(spy).change(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(R));
    }
    
    @Test
    public void flip_connectedSourceDefaultRegion_completes() {
        PottsMock spy = makeFlipMock();
        spy.regions[0][1][1] = Region.DEFAULT.ordinal();
        spy.flip(1, 0, 1, 1, 0, R);
        verify(spy).getNeighborhood(1, 1, 1, 0);
        verify(spy, never()).getNeighborhood(1, Region.DEFAULT.ordinal(), 1, 1, 0);
        verify(spy).change(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(R));
    }
    
    @Test
    public void flip_unconnectedTargetID_returns() {
        PottsMock spy = makeFlipMock();
        spy.flip(0, 2, 0, 0, 0, R);
        verify(spy).getNeighborhood(2, 0, 0, 0);
        verify(spy, never()).change(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(R));
    }
    
    @Test
    public void flip_unconnectedTargetIDNeighbor_returns() {
        PottsMock spy = makeFlipMock();
        spy.ids[0][0][0] = 1;
        spy.flip(0, 2, 0, 0, 0, R);
        verify(spy).getNeighborhood(2, 0, 0, 0);
        verify(spy, never()).change(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(R));
    }
    
    @Test
    public void flip_connectedTargetUnconnectedTargetRegion_returns() {
        PottsMock spy = makeFlipMock();
        spy.flip(0, 2, 1, 0, 0, R);
        verify(spy).getNeighborhood(2, 1, 0, 0);
        verify(spy).getNeighborhood(2, Region.NUCLEUS.ordinal(), 1, 0, 0);
        verify(spy, never()).change(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(R));
    }
    
    @Test
    public void flip_connectedTargetConnectedTargetRegion_completes() {
        PottsMock spy = makeFlipMock();
        spy.flip(0, 2, 1, 1, 0, R);
        verify(spy).getNeighborhood(2, 1, 1, 0);
        verify(spy).getNeighborhood(2, Region.NUCLEUS.ordinal(), 1, 1, 0);
        verify(spy).change(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(R));
    }
    
    @Test
    public void flip_connectedTargetDefaultRegion_completes() {
        PottsMock spy = makeFlipMock();
        spy.regions[0][1][1] = Region.DEFAULT.ordinal();
        spy.flip(0, 2, 1, 1, 0, R);
        verify(spy).getNeighborhood(2, 1, 1, 0);
        verify(spy, never()).getNeighborhood(2, Region.DEFAULT.ordinal(), 1, 1, 0);
        verify(spy).change(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(R));
    }
    
    @Test
    public void flip_connectedSourceZeroFalse_returns() {
        PottsMock spy = makeFlipMock();
        spy.ids[0][0][0] = 1;
        spy.flip(1, 0, 0, 0, 0, R);
        verify(spy).getConnectivity(any(), eq(false));
    }
    
    @Test
    public void flip_connectedSourceZeroTrue_completes() {
        PottsMock spy = makeFlipMock();
        spy.ids[0][0][0] = 0;
        spy.flip(1, 0, 0, 0, 0, R);
        verify(spy).getConnectivity(any(), eq(true));
    }
    
    @Test
    public void flip_connectedTargetZeroFalse_returns() {
        PottsMock spy = makeFlipMock();
        spy.ids[0][0][0] = 1;
        spy.flip(0, 1, 0, 0, 0, R);
        verify(spy).getConnectivity(any(), eq(false));
    }
    
    @Test
    public void flip_connectedTargetZeroTrue_completes() {
        PottsMock spy = makeFlipMock();
        spy.ids[0][0][0] = 0;
        spy.flip(0, 1, 0, 0, 0, R);
        verify(spy).getConnectivity(any(), eq(true));
    }
    
    @Test
    public void change_zeros_callsMethods() {
        int id1 = randomIntBetween(1, 10);
        int id2 = randomIntBetween(1, 10);
        PottsMock spy = makeChangeMock(id1, id2, new double[] { 0, 0, 0 }, false);
        spy.change(id1, id2, 0, 0, 0, 1);
        
        for (Hamiltonian h : spy.hamiltonian) {
            verify(h).getDelta(id1, id2, 0, 0, 0);
        }
    }
    
    @Test
    public void change_negativeEnergyZeroSourceNonzeroTargetRegions_updatesFields() {
        PottsMock spy = makeChangeMock(0, 1, new double[] { 1, -1, -1 }, true);
        spy.ids[0][0][0] = 0;
        spy.regions[0][0][0] = Region.UNDEFINED.ordinal();
        spy.change(0, 1, 0, 0, 0, 0);
        assertEquals(1, spy.ids[0][0][0]);
        assertEquals(Region.DEFAULT.ordinal(), spy.regions[0][0][0]);
        verify(((PottsLocation) ((Cell) spy.grid.getObjectAt(1)).getLocation())).add(0, 0, 0);
    }
    
    @Test
    public void change_negativeEnergyNonzeroSourceZeroTargetRegions_updatesFields() {
        PottsMock spy = makeChangeMock(1, 0, new double[] { 1, -1, -1 }, true);
        spy.ids[0][0][0] = 1;
        spy.regions[0][0][0] = Region.DEFAULT.ordinal();
        spy.change(1, 0, 0, 0, 0, 0);
        assertEquals(0, spy.ids[0][0][0]);
        assertEquals(Region.UNDEFINED.ordinal(), spy.regions[0][0][0]);
        verify(((PottsLocation) ((Cell) spy.grid.getObjectAt(1)).getLocation())).remove(0, 0, 0);
    }
    
    @Test
    public void change_negativeEnergyNonzeroSourceNonzeroTargetRegions_updatesFields() {
        PottsMock spy = makeChangeMock(1, 2, new double[] { 1, -1, -1 }, true);
        spy.ids[0][0][0] = 1;
        spy.regions[0][0][0] = Region.DEFAULT.ordinal();
        spy.change(1, 2, 0, 0, 0, 0);
        assertEquals(2, spy.ids[0][0][0]);
        assertEquals(Region.DEFAULT.ordinal(), spy.regions[0][0][0]);
        verify(((PottsLocation) ((Cell) spy.grid.getObjectAt(2)).getLocation())).add(0, 0, 0);
        verify(((PottsLocation) ((Cell) spy.grid.getObjectAt(1)).getLocation())).remove(0, 0, 0);
    }
    
    @Test
    public void change_positiveEnergyZeroSourceNonzeroTargetRegions_updatesFields() {
        PottsMock spy = makeChangeMock(0, 1, new double[] { -1, 1, 3 }, true);
        spy.ids[0][0][0] = 0;
        spy.regions[0][0][0] = Region.UNDEFINED.ordinal();
        
        spy.change(0, 1, 0, 0, 0, R_PLUS);
        assertEquals(0, spy.ids[0][0][0]);
        assertEquals(Region.UNDEFINED.ordinal(), spy.regions[0][0][0]);
        verify(((PottsLocation) ((Cell) spy.grid.getObjectAt(1)).getLocation()), never()).add(0, 0, 0);
        
        spy.change(0, 1, 0, 0, 0, R_MINUS);
        assertEquals(1, spy.ids[0][0][0]);
        assertEquals(Region.DEFAULT.ordinal(), spy.regions[0][0][0]);
        Cell cell2 = (Cell) spy.grid.getObjectAt(1);
        verify((PottsLocation) cell2.getLocation()).add(0, 0, 0);
    }
    
    @Test
    public void change_positiveEnergyNonzeroSourceZeroTargetRegions_updatesFields() {
        PottsMock spy = makeChangeMock(1, 0, new double[] { -1, 1, 3 }, true);
        spy.ids[0][0][0] = 1;
        spy.regions[0][0][0] = Region.DEFAULT.ordinal();
        
        spy.change(1, 0, 0, 0, 0, R_PLUS);
        assertEquals(1, spy.ids[0][0][0]);
        assertEquals(Region.DEFAULT.ordinal(), spy.regions[0][0][0]);
        verify(((PottsLocation) ((Cell) spy.grid.getObjectAt(1)).getLocation()), never()).remove(0, 0, 0);
        
        spy.change(1, 0, 0, 0, 0, R_MINUS);
        assertEquals(0, spy.ids[0][0][0]);
        assertEquals(Region.UNDEFINED.ordinal(), spy.regions[0][0][0]);
        verify(((PottsLocation) ((Cell) spy.grid.getObjectAt(1)).getLocation())).remove(0, 0, 0);
    }
    
    @Test
    public void change_positiveEnergyNonzeroSourceNonzeroTargetRegions_updatesFields() {
        PottsMock spy = makeChangeMock(1, 2, new double[] { -1, 1, 3 }, true);
        spy.ids[0][0][0] = 1;
        spy.regions[0][0][0] = Region.DEFAULT.ordinal();
        
        spy.change(1, 2, 0, 0, 0, R_PLUS);
        assertEquals(1, spy.ids[0][0][0]);
        assertEquals(Region.DEFAULT.ordinal(), spy.regions[0][0][0]);
        verify(((PottsLocation) ((Cell) spy.grid.getObjectAt(1)).getLocation()), never()).remove(0, 0, 0);
        verify(((PottsLocation) ((Cell) spy.grid.getObjectAt(2)).getLocation()), never()).add(0, 0, 0);
        
        spy.change(1, 2, 0, 0, 0, R_MINUS);
        assertEquals(2, spy.ids[0][0][0]);
        assertEquals(Region.DEFAULT.ordinal(), spy.regions[0][0][0]);
        verify(((PottsLocation) ((Cell) spy.grid.getObjectAt(1)).getLocation())).remove(0, 0, 0);
        verify(((PottsLocation) ((Cell) spy.grid.getObjectAt(2)).getLocation())).add(0, 0, 0);
    }
    
    @Test
    public void change_negativeEnergyZeroSourceNonzeroTargetNoRegions_updatesFields() {
        PottsMock spy = makeChangeMock(0, 1, new double[] { 1, -1, -1 }, false);
        spy.ids[0][0][0] = 0;
        spy.regions[0][0][0] = Region.UNDEFINED.ordinal();
        spy.change(0, 1, 0, 0, 0, 0);
        assertEquals(1, spy.ids[0][0][0]);
        assertEquals(Region.UNDEFINED.ordinal(), spy.regions[0][0][0]);
        verify(((PottsLocation) ((Cell) spy.grid.getObjectAt(1)).getLocation())).add(0, 0, 0);
    }
    
    @Test
    public void change_negativeEnergyNonzeroSourceZeroTargetNoRegions_updatesFields() {
        PottsMock spy = makeChangeMock(1, 0, new double[] { 1, -1, -1 }, false);
        spy.ids[0][0][0] = 1;
        spy.regions[0][0][0] = Region.UNDEFINED.ordinal();
        spy.change(1, 0, 0, 0, 0, 0);
        assertEquals(0, spy.ids[0][0][0]);
        assertEquals(Region.UNDEFINED.ordinal(), spy.regions[0][0][0]);
        verify(((PottsLocation) ((Cell) spy.grid.getObjectAt(1)).getLocation())).remove(0, 0, 0);
    }
    
    @Test
    public void change_negativeEnergyNonzeroSourceNonzeroTargetNoRegions_updatesFields() {
        PottsMock spy = makeChangeMock(1, 2, new double[] { 1, -1, -1 }, false);
        spy.ids[0][0][0] = 1;
        spy.regions[0][0][0] = Region.UNDEFINED.ordinal();
        spy.change(1, 2, 0, 0, 0, 0);
        assertEquals(2, spy.ids[0][0][0]);
        assertEquals(Region.UNDEFINED.ordinal(), spy.regions[0][0][0]);
        verify(((PottsLocation) ((Cell) spy.grid.getObjectAt(2)).getLocation())).add(0, 0, 0);
        verify(((PottsLocation) ((Cell) spy.grid.getObjectAt(1)).getLocation())).remove(0, 0, 0);
    }
    
    @Test
    public void change_positiveEnergyZeroSourceNonzeroTargetNoRegions_updatesFields() {
        PottsMock spy = makeChangeMock(0, 1, new double[] { -1, 1, 3 }, false);
        spy.ids[0][0][0] = 0;
        spy.regions[0][0][0] = Region.UNDEFINED.ordinal();
        
        spy.change(0, 1, 0, 0, 0, R_PLUS);
        assertEquals(0, spy.ids[0][0][0]);
        assertEquals(Region.UNDEFINED.ordinal(), spy.regions[0][0][0]);
        verify(((PottsLocation) ((Cell) spy.grid.getObjectAt(1)).getLocation()), never()).add(0, 0, 0);
        
        spy.change(0, 1, 0, 0, 0, R_MINUS);
        assertEquals(1, spy.ids[0][0][0]);
        assertEquals(Region.UNDEFINED.ordinal(), spy.regions[0][0][0]);
        verify(((PottsLocation) ((Cell) spy.grid.getObjectAt(1)).getLocation())).add(0, 0, 0);
    }
    
    @Test
    public void change_positiveEnergyNonzeroSourceZeroTargetNoRegions_updatesFields() {
        PottsMock spy = makeChangeMock(1, 0, new double[] { -1, 1, 3 }, false);
        spy.ids[0][0][0] = 1;
        spy.regions[0][0][0] = Region.UNDEFINED.ordinal();
        
        spy.change(1, 0, 0, 0, 0, R_PLUS);
        assertEquals(1, spy.ids[0][0][0]);
        assertEquals(Region.UNDEFINED.ordinal(), spy.regions[0][0][0]);
        verify(((PottsLocation) ((Cell) spy.grid.getObjectAt(1)).getLocation()), never()).remove(0, 0, 0);
        
        spy.change(1, 0, 0, 0, 0, R_MINUS);
        assertEquals(0, spy.ids[0][0][0]);
        assertEquals(Region.UNDEFINED.ordinal(), spy.regions[0][0][0]);
        verify(((PottsLocation) ((Cell) spy.grid.getObjectAt(1)).getLocation())).remove(0, 0, 0);
    }
    
    @Test
    public void change_positiveEnergyNonzeroSourceNonzeroTargetNoRegions_updatesFields() {
        PottsMock spy = makeChangeMock(1, 2, new double[] { -1, 1, 3 }, false);
        spy.ids[0][0][0] = 1;
        spy.regions[0][0][0] = Region.UNDEFINED.ordinal();
        
        spy.change(1, 2, 0, 0, 0, R_PLUS);
        assertEquals(1, spy.ids[0][0][0]);
        assertEquals(Region.UNDEFINED.ordinal(), spy.regions[0][0][0]);
        verify(((PottsLocation) ((Cell) spy.grid.getObjectAt(1)).getLocation()), never()).remove(0, 0, 0);
        verify(((PottsLocation) ((Cell) spy.grid.getObjectAt(2)).getLocation()), never()).add(0, 0, 0);
        
        spy.change(1, 2, 0, 0, 0, R_MINUS);
        assertEquals(2, spy.ids[0][0][0]);
        assertEquals(Region.UNDEFINED.ordinal(), spy.regions[0][0][0]);
        verify(((PottsLocation) ((Cell) spy.grid.getObjectAt(1)).getLocation())).remove(0, 0, 0);
        verify(((PottsLocation) ((Cell) spy.grid.getObjectAt(2)).getLocation())).add(0, 0, 0);
    }
    
    @Test
    public void flip_unconnectedSourceRegion_returns() {
        PottsMock spy = makeFlipMock();
        spy.flip(1, Region.NUCLEUS.ordinal(), Region.UNDEFINED.ordinal(), 1, 0, 0, R);
        verify(spy).getNeighborhood(1, Region.NUCLEUS.ordinal(), 1, 0, 0);
        verify(spy, never()).change(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(R));
    }
    
    @Test
    public void flip_connectedSourceRegion_completes() {
        PottsMock spy = makeFlipMock();
        spy.flip(1, Region.NUCLEUS.ordinal(), Region.UNDEFINED.ordinal(), 1, 1, 0, R);
        verify(spy).getNeighborhood(1, Region.NUCLEUS.ordinal(), 1, 1, 0);
        verify(spy).change(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(R));
    }
    
    @Test
    public void flip_unconnectedTargetRegion_returns() {
        PottsMock spy = makeFlipMock();
        spy.flip(1, Region.UNDEFINED.ordinal(), Region.NUCLEUS.ordinal(), 1, 0, 0, R);
        verify(spy).getNeighborhood(1, Region.NUCLEUS.ordinal(), 1, 0, 0);
        verify(spy, never()).change(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(R));
    }
    
    @Test
    public void flip_connectedTargetRegion_completes() {
        PottsMock spy = makeFlipMock();
        spy.flip(1, Region.UNDEFINED.ordinal(), Region.NUCLEUS.ordinal(), 1, 1, 0, R);
        verify(spy).getNeighborhood(1, Region.NUCLEUS.ordinal(), 1, 1, 0);
        verify(spy).change(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(R));
    }
    
    @Test
    public void flip_connectedSourceRegionZeroFalse_returns() {
        PottsMock spy = makeFlipMock();
        spy.regions[0][0][0] = Region.UNDEFINED.ordinal();
        spy.flip(1, Region.NUCLEUS.ordinal(), Region.UNDEFINED.ordinal(), 0, 0, 0, R);
        verify(spy).getConnectivity(any(), eq(false));
    }
    
    @Test
    public void flip_connectedSourceRegionZeroTrue_completes() {
        PottsMock spy = makeFlipMock();
        spy.regions[0][0][0] = Region.DEFAULT.ordinal();
        spy.flip(1, Region.NUCLEUS.ordinal(), Region.UNDEFINED.ordinal(), 0, 0, 0, R);
        verify(spy).getConnectivity(any(), eq(true));
    }
    
    @Test
    public void flip_connectedTargetRegionZeroFalse_returns() {
        PottsMock spy = makeFlipMock();
        spy.regions[0][0][0] = Region.UNDEFINED.ordinal();
        spy.flip(1, Region.NUCLEUS.ordinal(), Region.UNDEFINED.ordinal(), 0, 0, 0, R);
        verify(spy).getConnectivity(any(), eq(false));
    }
    
    @Test
    public void flip_connectedTargetRegionZeroTrue_completes() {
        PottsMock spy = makeFlipMock();
        spy.regions[0][0][0] = Region.DEFAULT.ordinal();
        spy.flip(1, Region.NUCLEUS.ordinal(), Region.UNDEFINED.ordinal(), 0, 0, 0, R);
        verify(spy).getConnectivity(any(), eq(true));
    }
    
    @Test
    public void change_zerosRegions_callsMethods() {
        int region1 = randomIntBetween(1, 10);
        int region2 = randomIntBetween(1, 10);
        PottsMock spy = makeChangeMock(region1, region2, new double[] { 0, 0, 0 });
        spy.change(1, region1, region2, 0, 0, 0, 1);
        for (Hamiltonian h : spy.hamiltonian) {
            verify(h).getDelta(1, region1, region2, 0, 0, 0);
        }
    }
    
    @Test
    public void change_negativeEnergyRegions_updatesFields() {
        PottsMock spy = makeChangeMock(Region.DEFAULT.ordinal(), Region.NUCLEUS.ordinal(), new double[] { 1, -1, -1 });
        spy.ids[0][0][0] = 1;
        spy.regions[0][0][0] = Region.DEFAULT.ordinal();
        Grid grid = spy.grid;
        
        spy.change(1, Region.DEFAULT.ordinal(), Region.NUCLEUS.ordinal(), 0, 0, 0, 0);
        assertEquals(Region.NUCLEUS.ordinal(), spy.regions[0][0][0]);
        verify(((PottsLocation) ((Cell) grid.getObjectAt(1)).getLocation())).remove(Region.DEFAULT, 0, 0, 0);
        verify(((PottsLocation) ((Cell) grid.getObjectAt(1)).getLocation())).add(Region.NUCLEUS, 0, 0, 0);
    }
    
    @Test
    public void change_positiveEnergyRegions_updatesFields() {
        PottsMock spy = makeChangeMock(Region.DEFAULT.ordinal(), Region.NUCLEUS.ordinal(), new double[] { -1, 1, 3 });
        spy.ids[0][0][0] = 1;
        spy.regions[0][0][0] = Region.DEFAULT.ordinal();
        Grid grid = spy.grid;
        
        spy.change(1, Region.DEFAULT.ordinal(), Region.NUCLEUS.ordinal(), 0, 0, 0, R_PLUS);
        assertEquals(Region.DEFAULT.ordinal(), spy.regions[0][0][0]);
        verify(((PottsLocation) ((Cell) grid.getObjectAt(1)).getLocation()), never()).remove(Region.DEFAULT, 0, 0, 0);
        verify(((PottsLocation) ((Cell) grid.getObjectAt(1)).getLocation()), never()).add(Region.NUCLEUS, 0, 0, 0);
        
        spy.change(1, Region.DEFAULT.ordinal(), Region.NUCLEUS.ordinal(), 0, 0, 0, R_MINUS);
        assertEquals(Region.NUCLEUS.ordinal(), spy.regions[0][0][0]);
        verify(((PottsLocation) ((Cell) grid.getObjectAt(1)).getLocation())).remove(Region.DEFAULT, 0, 0, 0);
        verify(((PottsLocation) ((Cell) grid.getObjectAt(1)).getLocation())).add(Region.NUCLEUS, 0, 0, 0);
    }
    
    @Test
    public void getCell_validID_returnsObject() {
        PottsSeries series = makeSeries(1, 1, 1);
        PottsMock potts = new PottsMock(series);
        
        Grid grid = mock(Grid.class);
        potts.grid = grid;
        
        int n = randomIntBetween(3, 10);
        Cell[] cells = new Cell[n];
        for (int i = 0; i < n; i++) {
            PottsCell c = mock(PottsCell.class);
            when(grid.getObjectAt(i + 1)).thenReturn(c);
            cells[i] = c;
        }
        
        assertEquals(cells[0], potts.getCell(1));
        assertEquals(cells[1], potts.getCell(2));
        assertEquals(cells[2], potts.getCell(3));
    }
    
    @Test
    public void getCell_invalidID_returnsNull() {
        PottsSeries series = makeSeries(1, 1, 1);
        PottsMock potts = new PottsMock(series);
        
        Grid grid = mock(Grid.class);
        potts.grid = grid;
        when(grid.getObjectAt(0)).thenReturn(null);
        
        assertNull(potts.getCell(0));
        assertNull(potts.getCell(-1));
    }
}
