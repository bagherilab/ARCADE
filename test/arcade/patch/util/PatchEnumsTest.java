package arcade.patch.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import org.junit.Test;

import arcade.patch.util.PatchEnums.Category;
import arcade.patch.util.PatchEnums.Domain;
import arcade.patch.util.PatchEnums.Flag;
import arcade.patch.util.PatchEnums.Ordering;
import arcade.patch.util.PatchEnums.State;
import ec.util.MersenneTwisterFast;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.patch.util.PatchEnums.Flag;
import static arcade.patch.util.PatchEnums.Domain;
import static arcade.patch.util.PatchEnums.Ordering;
import static arcade.patch.util.PatchEnums.State;
import static arcade.patch.util.PatchEnums.Category;
import static arcade.patch.util.PatchEnums.AntigenFlag;

public class PatchEnumsTest {
    @Test(expected = UnsupportedOperationException.class)
    public void constructor_called_throwsException() {
        PatchEnums enums = new PatchEnums();
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

    @Test
    public void Domain_random_returnsDomain() {
        // Create set of all values.
        EnumSet<Domain> enumSet = EnumSet.allOf(Domain.class);
        enumSet.remove(Domain.UNDEFINED);
        
        // Create set of all random values.
        ArrayList<Domain> enumRandom = new ArrayList<>();
        
        int n = Domain.values().length - 1;
        for (int i = 0; i < n; i++) {
            MersenneTwisterFast rng = mock(MersenneTwisterFast.class);
            doReturn(i).when(rng).nextInt(n);
            enumRandom.add(Domain.random(rng));
        }
        
        // Compare resulting sets.
        EnumSet<Domain> enumSetRandom = EnumSet.copyOf(enumRandom);
        assertEquals(enumSet, enumSetRandom);
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
    public void Category_random_returnsCategory() {
        // Create set of all values.
        EnumSet<Category> enumSet = EnumSet.allOf(Category.class);
        enumSet.remove(Category.UNDEFINED);
        
        // Create set of all random values.
        ArrayList<Category> enumRandom = new ArrayList<>();
        
        int n = Category.values().length - 1;
        for (int i = 0; i < n; i++) {
            MersenneTwisterFast rng = mock(MersenneTwisterFast.class);
            doReturn(i).when(rng).nextInt(n);
            enumRandom.add(Category.random(rng));
        }
        
        // Compare resulting sets.
        EnumSet<Category> enumSetRandom = EnumSet.copyOf(enumRandom);
        assertEquals(enumSet, enumSetRandom);
    }

    @Test
    public void AntigenFlag_random_returnsAntigenFlag() {
        // Create set of all values.
        EnumSet<AntigenFlag> enumSet = EnumSet.allOf(AntigenFlag.class);
        enumSet.remove(AntigenFlag.UNDEFINED);
        
        // Create set of all random values.
        ArrayList<AntigenFlag> enumRandom = new ArrayList<>();
        
        int n = AntigenFlag.values().length - 1;
        for (int i = 0; i < n; i++) {
            MersenneTwisterFast rng = mock(MersenneTwisterFast.class);
            doReturn(i).when(rng).nextInt(n);
            enumRandom.add(AntigenFlag.random(rng));
        }
        
        // Compare resulting sets.
        EnumSet<AntigenFlag> enumSetRandom = EnumSet.copyOf(enumRandom);
        assertEquals(enumSet, enumSetRandom);
    }

    @Test
    public void Ordering_in_expected_order() {
        // Create list of all values.
        ArrayList<Ordering> enumList = new ArrayList<Ordering>(Arrays.asList(Ordering.values()));
        
        int n = -1;
        int verify = -2;
        // Grabbing order of items in enum
        for (Ordering x: enumList){
            verify = x.ordinal();
            n++;
            // Verify order of enum
            assertEquals(n, verify);
        }
    }
}
