package arcade.core.util;

import java.security.InvalidParameterException;
import org.junit.jupiter.api.Test;
import ec.util.MersenneTwisterFast;
import arcade.core.util.distributions.Distribution;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.core.util.MiniBox.TAG_SEPARATOR;

public class ParametersTest {
    @Test
    public void constructor_noDistributionsNoParent_skipsDistributions() {
        MersenneTwisterFast random = new MersenneTwisterFast();
        MiniBox popParameters = new MiniBox();

        Parameters parameters = new Parameters(popParameters, null, random);

        assertTrue(parameters.distributions.isEmpty());
        assertSame(popParameters, parameters.popParameters);
    }

    @Test
    public void constructor_withDistributionsNoParent_usesPopulationDistributions() {
        MersenneTwisterFast random = new MersenneTwisterFast();
        MiniBox popParameters = spy(new MiniBox());

        String key = randomString();
        popParameters.put("(DISTRIBUTION)" + TAG_SEPARATOR + key, "X");
        Distribution distribution = mock(Distribution.class);
        doReturn(distribution).when(popParameters).getDistribution(key, random);

        Parameters parameters = new Parameters(popParameters, null, random);

        assertEquals(distribution, parameters.distributions.get(key));
        assertSame(popParameters, parameters.popParameters);
    }

    @Test
    public void constructor_noDistributionsWithParent_skipsDistributions() {
        MersenneTwisterFast random = new MersenneTwisterFast();
        MiniBox popParameters1 = new MiniBox();
        MiniBox popParameters2 = new MiniBox();

        Parameters cellParameters = new Parameters(popParameters1, null, random);

        Parameters parameters = new Parameters(popParameters2, cellParameters, random);

        assertTrue(parameters.distributions.isEmpty());
        assertSame(popParameters2, parameters.popParameters);
    }

    @Test
    public void constructor_withDistributionsWithParent_usesParentDistributions() {
        MersenneTwisterFast random = new MersenneTwisterFast();
        MiniBox popParameters1 = spy(new MiniBox());
        MiniBox popParameters2 = new MiniBox();

        String key = randomString();
        popParameters1.put("(DISTRIBUTION)" + TAG_SEPARATOR + key, "X");
        popParameters2.put("(DISTRIBUTION)" + TAG_SEPARATOR + key, "Y");
        Distribution distribution1 = mock(Distribution.class);
        Distribution distribution2 = mock(Distribution.class);
        doReturn(distribution1).when(popParameters1).getDistribution(key, random);
        doReturn(distribution2).when(distribution1).rebase(random);

        Parameters cellParameters = new Parameters(popParameters1, null, random);

        Parameters parameters = new Parameters(popParameters2, cellParameters, random);

        assertEquals(distribution2, parameters.distributions.get(key));
        assertSame(popParameters2, parameters.popParameters);
    }

    @Test
    public void constructor_withDistributionsWithParentDifferentKey_usesPopulationDistributions() {
        MersenneTwisterFast random = new MersenneTwisterFast();
        MiniBox popParameters1 = spy(new MiniBox());
        MiniBox popParameters2 = spy(new MiniBox());

        String key1 = randomString();
        String key2 = randomString();
        popParameters1.put("(DISTRIBUTION)" + TAG_SEPARATOR + key1, "X");
        popParameters2.put("(DISTRIBUTION)" + TAG_SEPARATOR + key2, "Y");
        Distribution distribution1 = mock(Distribution.class);
        Distribution distribution2 = mock(Distribution.class);
        doReturn(distribution1).when(popParameters1).getDistribution(key1, random);
        doReturn(distribution2).when(popParameters2).getDistribution(key2, random);

        Parameters cellParameters = new Parameters(popParameters1, null, random);

        Parameters parameters = new Parameters(popParameters2, cellParameters, random);

        assertFalse(parameters.distributions.containsKey(key1));
        assertEquals(distribution2, parameters.distributions.get(key2));
        assertSame(popParameters2, parameters.popParameters);
    }

    @Test
    public void getDouble_keyHasDistribution_returnsValue() {
        MersenneTwisterFast random = new MersenneTwisterFast();
        MiniBox box = new MiniBox();
        String key = randomString();

        double value = randomDoubleBetween(0, 100);
        Distribution distribution = mock(Distribution.class);
        doReturn(value).when(distribution).getDoubleValue();

        Parameters parameters = new Parameters(box, null, random);
        parameters.distributions.put(key, distribution);

        assertEquals(value, parameters.getDouble(key));
    }

    @Test
    public void getDouble_keyDoesNotHaveDistribution_returnsValue() {
        MersenneTwisterFast random = new MersenneTwisterFast();
        MiniBox box = new MiniBox();
        String key = randomString();

        double value = randomDoubleBetween(0, 100);
        box.put(key, value);

        Parameters parameters = new Parameters(box, null, random);

        assertEquals(value, parameters.getDouble(key));
    }

    @Test
    public void getDouble_keyDoesNotExist_throwsException() {
        MersenneTwisterFast random = new MersenneTwisterFast();
        MiniBox box = new MiniBox();
        String key = randomString();

        Parameters parameters = new Parameters(box, null, random);

        assertThrows(InvalidParameterException.class, () -> parameters.getDouble(key));
    }

    @Test
    public void getInt_keyHasDistribution_returnsValue() {
        MersenneTwisterFast random = new MersenneTwisterFast();
        MiniBox box = new MiniBox();
        String key = randomString();

        int value = randomIntBetween(0, 100);
        Distribution distribution = mock(Distribution.class);
        doReturn(value).when(distribution).getIntValue();

        Parameters parameters = new Parameters(box, null, random);
        parameters.distributions.put(key, distribution);

        assertEquals(value, parameters.getInt(key));
    }

    @Test
    public void getInt_keyDoesNotHaveDistribution_returnsValue() {
        MersenneTwisterFast random = new MersenneTwisterFast();
        MiniBox box = new MiniBox();
        String key = randomString();

        int value = randomIntBetween(0, 100);
        box.put(key, value);

        Parameters parameters = new Parameters(box, null, random);

        assertEquals(value, parameters.getInt(key));
    }

    @Test
    public void getInt_keyDoesNotExist_throwsException() {
        MersenneTwisterFast random = new MersenneTwisterFast();
        MiniBox box = new MiniBox();
        String key = randomString();

        Parameters parameters = new Parameters(box, null, random);

        assertThrows(InvalidParameterException.class, () -> parameters.getInt(key));
    }

    @Test
    public void getDistribution_keyHasDistribution_returnsDistribution() {
        MersenneTwisterFast random = new MersenneTwisterFast();
        MiniBox box = new MiniBox();
        String key = randomString();

        Distribution distribution = mock(Distribution.class);
        Parameters parameters = new Parameters(box, null, random);
        parameters.distributions.put(key, distribution);

        assertSame(distribution, parameters.getDistribution(key));
    }

    @Test
    public void getDistribution_keyDoesNotExist_throwsException() {
        MersenneTwisterFast random = new MersenneTwisterFast();
        MiniBox box = new MiniBox();
        String key = randomString();

        Parameters parameters = new Parameters(box, null, random);

        assertThrows(InvalidParameterException.class, () -> parameters.getDouble(key));
    }
}
