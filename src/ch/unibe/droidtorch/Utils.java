package ch.unibe.droidtorch;

import java.io.BufferedReader;
import java.io.IOException;

public class Utils {
	/**
	 * Returns {@code true} if file {@code filename} exists
	 * in filesystem, {@code false} otherwise.
	 * 
	 * @param filepath - file location
	 * @return true if file exists, false otherwise
	 */
	public static boolean isFileExists(String filepath){
		BufferedReader reader = LinuxShell.execute("if test -w "+filepath+"; then echo \"true\"; else echo \"false\"; fi");
		boolean exists = false;
		try {
			exists = Boolean.valueOf(reader.readLine());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return exists;
	}
}
