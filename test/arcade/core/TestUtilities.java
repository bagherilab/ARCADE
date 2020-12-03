package arcade.core;

import java.io.*;
import java.util.Random;
import ec.util.MersenneTwisterFast;

public final class TestUtilities {
    public static final double EPSILON = 1E-10;
    
    public static final MersenneTwisterFast RANDOM = new MersenneTwisterFast((long) (Math.random()*1000));
    
    public static int randomSeed() { return randomIntBetween(1, 1000); }
    
    public static int randomIntBetween(int lower, int upper) { return (int)(Math.random()*(upper - lower)) + lower; }
    
    public static double randomDoubleBetween(double lower, double upper) { return Math.random()*(upper - lower) + lower; }
    
    public static String[] randomStrings(int n) {
        String[] strings = new String[n];
        for (int i = 0; i < n; i++) { strings[i] = randomString(); }
        return strings;
    }
    
    public static String randomString() {
        return new Random().ints(97, 123)
                .limit(10)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
    
    public static void write(File file, String contents) {
        FileWriter fw = null;
        BufferedWriter bw = null;
        PrintWriter pw = null;
        
        try {
            fw = new FileWriter(file, true);
            bw = new BufferedWriter(fw);
            pw = new PrintWriter(bw);
            pw.print(contents);
        } catch (IOException e) { e.printStackTrace(); }
        finally {
            try {
                if (pw != null) { pw.close(); }
                else if (bw != null) { bw.close(); }
                else if (fw != null) {fw.close(); }
            } catch (IOException e) { e.printStackTrace(); }
        }
    }
}
