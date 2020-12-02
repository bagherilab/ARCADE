package arcade.potts.util;

import org.junit.*;
import static org.mockito.Mockito.*;
import java.util.EnumSet;
import java.util.ArrayList;
import ec.util.MersenneTwisterFast;
import static org.junit.Assert.*;
import static arcade.potts.util.PottsEnums.*;

public class PottsEnumTest {
    @Test
    public void Phase_random_returnsPhase() {
        // Create set of all values.
        EnumSet<Phase> enumSet = EnumSet.allOf(Phase.class);
        enumSet.remove(Phase.UNDEFINED);
        
        // Create set of all random values.
        ArrayList<Phase> enumRandom = new ArrayList<>();
        
        int n = Phase.values().length - 1;
        for (int i = 0; i < n; i++) {
            MersenneTwisterFast rng = mock(MersenneTwisterFast.class);
            doReturn(i).when(rng).nextInt(n);
            enumRandom.add(Phase.random(rng));
        }
        
        // Compare resulting sets.
        EnumSet<Phase> enumSetRandom = EnumSet.copyOf(enumRandom);
        assertEquals(enumSet, enumSetRandom);
    }
    
    @Test
    public void Direction_random_returnsDirection() {
        // Create set of all values.
        EnumSet<Direction> enumSet = EnumSet.allOf(Direction.class);
        enumSet.remove(Direction.UNDEFINED);
        
        // Create set of all random values.
        ArrayList<Direction> enumRandom = new ArrayList<>();
        
        int n = Direction.values().length - 1;
        for (int i = 0; i < n; i++) {
            MersenneTwisterFast rng = mock(MersenneTwisterFast.class);
            doReturn(i).when(rng).nextInt(n);
            enumRandom.add(Direction.random(rng));
        }
        
        // Compare resulting sets.
        EnumSet<Direction> enumSetRandom = EnumSet.copyOf(enumRandom);
        assertEquals(enumSet, enumSetRandom);
    }
}
