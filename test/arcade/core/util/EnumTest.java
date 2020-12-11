package arcade.core.util;

import java.util.ArrayList;
import java.util.EnumSet;
import org.junit.Test;
import ec.util.MersenneTwisterFast;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.util.Enums.*;

public class EnumTest {
    @Test(expected = UnsupportedOperationException.class)
    public void constructor_called_throwsException() {
        Enums enums = new Enums();
    }
    
    @Test
    public void State_random_returnsState() {
        // Create set of all values.
        EnumSet<State> enumSet = EnumSet.allOf(State.class);
        enumSet.remove(State.UNDEFINED);
        
        // Create set of all random values.
        ArrayList<State> enumRandom = new ArrayList<>();
        
        int n = State.values().length - 1;
        for (int i = 0; i < n; i++) {
            MersenneTwisterFast rng = mock(MersenneTwisterFast.class);
            doReturn(i).when(rng).nextInt(n);
            enumRandom.add(State.random(rng));
        }
        
        // Compare resulting sets.
        EnumSet<State> enumSetRandom = EnumSet.copyOf(enumRandom);
        assertEquals(enumSet, enumSetRandom);
    }
    
    @Test
    public void Region_random_returnsRegion() {
        // Create set of all values.
        EnumSet<Region> enumSet = EnumSet.allOf(Region.class);
        enumSet.remove(Region.UNDEFINED);
        
        // Create set of all random values.
        ArrayList<Region> enumRandom = new ArrayList<>();
        
        int n = Region.values().length - 1;
        for (int i = 0; i < n; i++) {
            MersenneTwisterFast rng = mock(MersenneTwisterFast.class);
            doReturn(i).when(rng).nextInt(n);
            enumRandom.add(Region.random(rng));
        }
        
        // Compare resulting sets.
        EnumSet<Region> enumSetRandom = EnumSet.copyOf(enumRandom);
        assertEquals(enumSet, enumSetRandom);
    }
}
