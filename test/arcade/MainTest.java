package arcade;

import java.io.*;
import java.util.Random;

public class MainTest {
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
	
	public static String randomString() {
		return new Random().ints(97, 123)
				.limit(10)
				.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
				.toString();
	}
	
	public static int randomInt() { return (int)(Math.random()*10) + 2; }
	
	public static String[] randomValues(int n) {
		String[] strings = new String[n];
		for (int i = 0; i < n; i++) { strings[i] = randomString(); }
		return strings;
	}
}
