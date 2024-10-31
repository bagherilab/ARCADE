package arcade.core.util;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import org.junit.jupiter.api.Test;
import ec.util.MersenneTwisterFast;
import arcade.core.util.distributions.Distribution;
import arcade.core.util.distributions.NormalDistribution;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.core.util.MiniBox.TAG_SEPARATOR;

public class ParametersTest {
    private static final MersenneTwisterFast RANDOM = new MersenneTwisterFast();

    @Test
    public void constructor_noDistributionsNoParent_skipsDistributions() {
        MiniBox popParameters = new MiniBox();

        Parameters parameters = new Parameters(popParameters, null, RANDOM);

        assertTrue(parameters.distributions.isEmpty());
        assertSame(popParameters, parameters.popParameters);
    }

    @Test
    public void constructor_withDistributionsNoParent_usesPopulationDistributions() {
        MiniBox popParameters = spy(new MiniBox());

        String key = randomString();
        popParameters.put("(DISTRIBUTION)" + TAG_SEPARATOR + key, "X");
        Distribution distribution = mock(Distribution.class);
        doReturn(distribution).when(popParameters).getDistribution(key, RANDOM);

        Parameters parameters = new Parameters(popParameters, null, RANDOM);

        assertEquals(distribution, parameters.distributions.get(key));
        assertSame(popParameters, parameters.popParameters);
    }

    @Test
    public void constructor_noDistributionsWithParent_skipsDistributions() {
        MiniBox popParameters1 = new MiniBox();
        MiniBox popParameters2 = new MiniBox();

        Parameters cellParameters = new Parameters(popParameters1, null, RANDOM);

        Parameters parameters = new Parameters(popParameters2, cellParameters, RANDOM);

        assertTrue(parameters.distributions.isEmpty());
        assertSame(popParameters2, parameters.popParameters);
    }

    @Test
    public void constructor_withDistributionsWithParent_usesParentDistributions() {
        MiniBox popParameters1 = spy(new MiniBox());
        MiniBox popParameters2 = new MiniBox();

        String key = randomString();
        popParameters1.put("(DISTRIBUTION)" + TAG_SEPARATOR + key, "X");
        popParameters2.put("(DISTRIBUTION)" + TAG_SEPARATOR + key, "Y");
        Distribution distribution1 = mock(Distribution.class);
        Distribution distribution2 = mock(Distribution.class);
        doReturn(distribution1).when(popParameters1).getDistribution(key, RANDOM);
        doReturn(distribution2).when(distribution1).rebase(RANDOM);

        Parameters cellParameters = new Parameters(popParameters1, null, RANDOM);

        Parameters parameters = new Parameters(popParameters2, cellParameters, RANDOM);

        assertEquals(distribution2, parameters.distributions.get(key));
        assertSame(popParameters2, parameters.popParameters);
    }

    @Test
    public void constructor_withDistributionsWithParentDifferentKey_usesPopulationDistributions() {
        MiniBox popParameters1 = spy(new MiniBox());
        MiniBox popParameters2 = spy(new MiniBox());

        String key1 = randomString();
        String key2 = randomString();
        popParameters1.put("(DISTRIBUTION)" + TAG_SEPARATOR + key1, "X");
        popParameters2.put("(DISTRIBUTION)" + TAG_SEPARATOR + key2, "Y");
        Distribution distribution1 = mock(Distribution.class);
        Distribution distribution2 = mock(Distribution.class);
        doReturn(distribution1).when(popParameters1).getDistribution(key1, RANDOM);
        doReturn(distribution2).when(popParameters2).getDistribution(key2, RANDOM);

        Parameters cellParameters = new Parameters(popParameters1, null, RANDOM);

        Parameters parameters = new Parameters(popParameters2, cellParameters, RANDOM);

        assertFalse(parameters.distributions.containsKey(key1));
        assertEquals(distribution2, parameters.distributions.get(key2));
        assertSame(popParameters2, parameters.popParameters);
    }

    @Test
    public void getDouble_keyHasDistribution_returnsValue() {
        MiniBox box = new MiniBox();
        String key = randomString();

        double value = randomDoubleBetween(0, 100);
        Distribution distribution = mock(Distribution.class);
        doReturn(value).when(distribution).getDoubleValue();

        Parameters parameters = new Parameters(box, null, RANDOM);
        parameters.distributions.put(key, distribution);

        assertEquals(value, parameters.getDouble(key));
    }

    @Test
    public void getDouble_keyDoesNotHaveDistribution_returnsValue() {
        MiniBox box = new MiniBox();
        String key = randomString();

        double value = randomDoubleBetween(0, 100);
        box.put(key, value);

        Parameters parameters = new Parameters(box, null, RANDOM);

        assertEquals(value, parameters.getDouble(key));
    }

    @Test
    public void getDouble_keyDoesNotExist_throwsException() {
        MiniBox box = new MiniBox();
        String key = randomString();

        Parameters parameters = new Parameters(box, null, RANDOM);

        assertThrows(InvalidParameterException.class, () -> parameters.getDouble(key));
    }

    @Test
    public void getInt_keyHasDistribution_returnsValue() {
        MiniBox box = new MiniBox();
        String key = randomString();

        int value = randomIntBetween(0, 100);
        Distribution distribution = mock(Distribution.class);
        doReturn(value).when(distribution).getIntValue();

        Parameters parameters = new Parameters(box, null, RANDOM);
        parameters.distributions.put(key, distribution);

        assertEquals(value, parameters.getInt(key));
    }

    @Test
    public void getInt_keyDoesNotHaveDistribution_returnsValue() {
        MiniBox box = new MiniBox();
        String key = randomString();

        int value = randomIntBetween(0, 100);
        box.put(key, value);

        Parameters parameters = new Parameters(box, null, RANDOM);

        assertEquals(value, parameters.getInt(key));
    }

    @Test
    public void getInt_keyDoesNotExist_throwsException() {
        MiniBox box = new MiniBox();
        String key = randomString();

        Parameters parameters = new Parameters(box, null, RANDOM);

        assertThrows(InvalidParameterException.class, () -> parameters.getInt(key));
    }

    @Test
    public void getDistribution_keyHasDistribution_returnsDistribution() {
        MiniBox box = new MiniBox();
        String key = randomString();

        Distribution distribution = mock(Distribution.class);
        Parameters parameters = new Parameters(box, null, RANDOM);
        parameters.distributions.put(key, distribution);

        assertSame(distribution, parameters.getDistribution(key));
    }

    @Test
    public void getDistribution_keyDoesNotExist_throwsException() {
        MiniBox box = new MiniBox();
        String key = randomString();

        Parameters parameters = new Parameters(box, null, RANDOM);

        assertThrows(InvalidParameterException.class, () -> parameters.getDouble(key));
    }

    @Test
    public void filter_invalidCode_returnsEmpty() {
        MiniBox box = new MiniBox();
        String code = randomString();
        String key = randomString();
        String value = randomString();
        box.put(key, value);

        Parameters parameters = new Parameters(box, null, RANDOM);

        MiniBox filtered = parameters.filter(code);
        assertEquals(new ArrayList<>(), filtered.keys);
        assertEquals(new HashMap<>(), filtered.contents);
    }

    @Test
    public void filter_validCode_returnsFiltered() {
        MiniBox box = new MiniBox();
        String code = randomString();
        String key1 = randomString();
        String key2 = randomString();
        String key3 = randomString();
        String value1 = randomString();
        String value2 = randomString();
        String value3 = randomString();

        box.put(key1, value1);
        box.put(code + TAG_SEPARATOR + key2, value2);
        box.put(code + TAG_SEPARATOR + key3, value3);

        ArrayList<String> filteredKeys = new ArrayList<>();
        filteredKeys.add(key2);
        filteredKeys.add(key3);

        HashMap<String, String> filteredMap = new HashMap<>();
        filteredMap.put(key2, value2);
        filteredMap.put(key3, value3);

        Parameters parameters = new Parameters(box, null, RANDOM);

        MiniBox filtered = parameters.filter(code);
        assertEquals(filteredKeys, filtered.keys);
        assertEquals(filteredMap, filtered.contents);
    }

    @Test
    public void filter_multipleCodes_returnsFiltered() {
        MiniBox box = new MiniBox();
        String code1 = randomString();
        String code2 = randomString();
        String key1 = randomString();
        String key2 = randomString();
        String key3 = randomString();
        String value1 = randomString();
        String value2 = randomString();
        String value3 = randomString();

        box.put(key1, value1);
        box.put(code1 + TAG_SEPARATOR + key2, value2);
        box.put(code2 + TAG_SEPARATOR + key3, value3);

        ArrayList<String> filteredKeys = new ArrayList<>();
        filteredKeys.add(key3);

        HashMap<String, String> filteredMap = new HashMap<>();
        filteredMap.put(key3, value3);

        Parameters parameters = new Parameters(box, null, RANDOM);

        MiniBox filtered = parameters.filter(code2);
        assertEquals(filteredKeys, filtered.keys);
        assertEquals(filteredMap, filtered.contents);
    }

    @Test
    public void compare_sameParametersSameDistributions_returnsTrue() {
        MiniBox boxA = new MiniBox();
        MiniBox boxB = new MiniBox();

        String key = randomString();
        String value = randomString();

        boxA.put(key, value);
        boxB.put(key, value);

        Parameters parametersA = new Parameters(boxA, null, RANDOM);
        Parameters parametersB = new Parameters(boxB, null, RANDOM);

        parametersA.distributions.put("PARAM", new NormalDistribution(5, 3, RANDOM));
        parametersB.distributions.put("PARAM", new NormalDistribution(5, 3, RANDOM));

        assertTrue(parametersA.compare(parametersB));
        assertTrue(parametersB.compare(parametersA));
    }

    @Test
    public void compare_sameParametersDifferentDistributionContents_returnsFalse() {
        MiniBox boxA = new MiniBox();
        MiniBox boxB = new MiniBox();

        String key = randomString();
        String value = randomString();

        boxA.put(key, value);
        boxB.put(key, value);

        Parameters parametersA = new Parameters(boxA, null, RANDOM);
        Parameters parametersB = new Parameters(boxB, null, RANDOM);

        parametersA.distributions.put("PARAM", new NormalDistribution(5, 3, RANDOM));
        parametersB.distributions.put("PARAM", new NormalDistribution(3, 2, RANDOM));

        assertFalse(parametersA.compare(parametersB));
        assertFalse(parametersB.compare(parametersA));
    }

    @Test
    public void compare_sameParametersDifferentDistributionsKeys_returnsFalse() {
        MiniBox boxA = new MiniBox();
        MiniBox boxB = new MiniBox();

        String key = randomString();
        String value = randomString();

        boxA.put(key, value);
        boxB.put(key, value);

        Parameters parametersA = new Parameters(boxA, null, RANDOM);
        Parameters parametersB = new Parameters(boxB, null, RANDOM);

        parametersA.distributions.put("PARAM_A", new NormalDistribution(5, 3, RANDOM));
        parametersB.distributions.put("PARAM_A", new NormalDistribution(5, 3, RANDOM));
        parametersB.distributions.put("PARAM_B", new NormalDistribution(5, 3, RANDOM));

        assertFalse(parametersA.compare(parametersB));
        assertFalse(parametersB.compare(parametersA));
    }

    @Test
    public void compare_differentParametersSameDistributions_returnsFalse() {
        MiniBox boxA = new MiniBox();
        MiniBox boxB = new MiniBox();

        String key = randomString();
        String value1 = randomString();
        String value2 = randomString();

        boxA.put(key, value1);
        boxB.put(key, value2);

        Parameters parametersA = new Parameters(boxA, null, RANDOM);
        Parameters parametersB = new Parameters(boxB, null, RANDOM);

        parametersA.distributions.put("PARAM", new NormalDistribution(5, 3, RANDOM));
        parametersB.distributions.put("PARAM", new NormalDistribution(5, 3, RANDOM));

        assertFalse(parametersA.compare(parametersB));
        assertFalse(parametersB.compare(parametersA));
    }

    @Test
    public void compare_differentParametersDifferentDistributionContents_returnsFalse() {
        MiniBox boxA = new MiniBox();
        MiniBox boxB = new MiniBox();

        String key = randomString();
        String value1 = randomString();
        String value2 = randomString();

        boxA.put(key, value1);
        boxB.put(key, value2);

        Parameters parametersA = new Parameters(boxA, null, RANDOM);
        Parameters parametersB = new Parameters(boxB, null, RANDOM);

        parametersA.distributions.put("PARAM", new NormalDistribution(5, 3, RANDOM));
        parametersB.distributions.put("PARAM", new NormalDistribution(3, 2, RANDOM));

        assertFalse(parametersA.compare(parametersB));
        assertFalse(parametersB.compare(parametersA));
    }

    @Test
    public void compare_differentParametersDifferentDistributionKeys_returnsFalse() {
        MiniBox boxA = new MiniBox();
        MiniBox boxB = new MiniBox();

        String key = randomString();
        String value1 = randomString();
        String value2 = randomString();

        boxA.put(key, value1);
        boxB.put(key, value2);

        Parameters parametersA = new Parameters(boxA, null, RANDOM);
        Parameters parametersB = new Parameters(boxB, null, RANDOM);

        parametersA.distributions.put("PARAM_A", new NormalDistribution(5, 3, RANDOM));
        parametersB.distributions.put("PARAM_A", new NormalDistribution(5, 3, RANDOM));
        parametersB.distributions.put("PARAM_B", new NormalDistribution(5, 3, RANDOM));

        assertFalse(parametersA.compare(parametersB));
        assertFalse(parametersB.compare(parametersA));
    }
}
