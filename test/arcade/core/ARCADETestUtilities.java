package arcade.core;

public final class ARCADETestUtilities {
    protected ARCADETestUtilities() {
        throw new UnsupportedOperationException();
    }

    public static int randomSeed() {
        return randomIntBetween(1, 1000);
    }

    public static int randomIntBetween(int lower, int upper) {
        return (int) randomDoubleBetween(lower, upper);
    }

    public static double randomDoubleBetween(double lower, double upper) {
        return Math.random() * (upper - lower) + lower;
    }

    public static String randomString() {
        int stringSize = 10;
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvxyz";
        StringBuilder sb = new StringBuilder(stringSize);

        for (int i = 0; i < stringSize; i++) {
            int index = (int) (alphabet.length() * Math.random());
            sb.append(alphabet.charAt(index));
        }

        return sb.toString();
    }

    public static String[] randomStringArray(int n) {
        String[] strings = new String[n];
        for (int i = 0; i < n; i++) {
            strings[i] = randomString();
        }
        return strings;
    }
}
