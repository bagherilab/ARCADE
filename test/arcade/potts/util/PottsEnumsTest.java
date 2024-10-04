package arcade.potts.util;

import java.util.ArrayList;
import java.util.EnumSet;
import org.junit.Test;
import ec.util.MersenneTwisterFast;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.potts.util.PottsEnums.Direction;
import static arcade.potts.util.PottsEnums.Phase;
import static arcade.potts.util.PottsEnums.Term;

public class PottsEnumsTest {
    @Test(expected = UnsupportedOperationException.class)
    public void constructor_called_throwsException() {
        PottsEnums enums = new PottsEnums();
    }
    
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
    public void Term_random_returnsTerm() {
        // Create set of all values.
        EnumSet<Term> enumSet = EnumSet.allOf(Term.class);
        enumSet.remove(Term.UNDEFINED);
        
        // Create set of all random values.
        ArrayList<Term> enumRandom = new ArrayList<>();
        
        int n = Term.values().length - 1;
        for (int i = 0; i < n; i++) {
            MersenneTwisterFast rng = mock(MersenneTwisterFast.class);
            doReturn(i).when(rng).nextInt(n);
            enumRandom.add(Term.random(rng));
        }
        
        // Compare resulting sets.
        EnumSet<Term> enumSetRandom = EnumSet.copyOf(enumRandom);
        assertEquals(enumSet, enumSetRandom);
    }
    
    @Test
    public void Direction_random_returnsDirection() {
        // Create a set of all Direction values excluding UNDEFINED
        EnumSet<Direction> expectedDirections = EnumSet.complementOf(EnumSet.of(Direction.UNDEFINED));

        // Create a set to hold the Directions returned by Direction.random
        EnumSet<Direction> randomDirections = EnumSet.noneOf(Direction.class);

        int n = Direction.values().length - 1; // Excluding UNDEFINED

        // Loop over all possible values that rng.nextInt(n) can return
        for (int i = 0; i < n; i++) {
            MersenneTwisterFast rng = mock(MersenneTwisterFast.class);
            doReturn(i).when(rng).nextInt(n);

            Direction direction = Direction.random(rng);
            randomDirections.add(direction);
        }

        // Assert that the set of random directions equals the expected set
        assertEquals(expectedDirections, randomDirections);
    }
}
