package arcade.patch.util;

import java.util.ArrayList;
import java.util.EnumSet;
import org.junit.jupiter.api.Test;
import ec.util.MersenneTwisterFast;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.patch.util.PatchEnums.Flag;

public class PatchEnumsTest {
    @Test
    public void constructor_called_throwsException() {
        assertThrows(UnsupportedOperationException.class, PatchEnums::new);
    }
    
    @Test
    public void Flag_random_returnsFlag() {
        // Create set of all values.
        EnumSet<Flag> enumSet = EnumSet.allOf(Flag.class);
        enumSet.remove(Flag.UNDEFINED);
        
        // Create set of all random values.
        ArrayList<Flag> enumRandom = new ArrayList<>();
        
        int n = Flag.values().length - 1;
        for (int i = 0; i < n; i++) {
            MersenneTwisterFast rng = mock(MersenneTwisterFast.class);
            doReturn(i).when(rng).nextInt(n);
            enumRandom.add(Flag.random(rng));
        }
        
        // Compare resulting sets.
        EnumSet<Flag> enumSetRandom = EnumSet.copyOf(enumRandom);
        assertEquals(enumSet, enumSetRandom);
    }
}
