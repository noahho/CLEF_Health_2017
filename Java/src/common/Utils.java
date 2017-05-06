package common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;

public class Utils {
	public static String readFile(File file, Charset encoding)
			throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
		return new String(encoded, encoding);
	}

	public static boolean saveFile(File file, String content, Charset encoding) {
		return saveFile(file, content, encoding, false);
	}

	public static double makeSafeDouble(double i) {
		if (Double.isNaN(i))
			  return 0;
		return i;
	}

	public static boolean saveFile(File file, String content, Charset encoding,
			boolean append) {
		try {
			PrintWriter writer = new PrintWriter(new FileOutputStream(new File(
					file.getAbsolutePath()), append));
			writer.print(content);
			writer.close();
		} catch (IOException e) {
			System.out.println(e.toString());
			return false;
		}
		return true;
	}

	public static Vector StringToVector(String v) {
		return Vectors.zeros(300);
	}

	public static int countWords(String s) {

		int wordCount = 0;

		boolean word = false;
		int endOfLine = s.length() - 1;

		for (int i = 0; i < s.length(); i++) {
			// if the char is a letter, word = true.
			if (Character.isLetter(s.charAt(i)) && i != endOfLine) {
				word = true;
				// if char isn't a letter and there have been letters before,
				// counter goes up.
			} else if (!Character.isLetter(s.charAt(i)) && word) {
				wordCount++;
				word = false;
				// last word of String; if it doesn't end with a non letter, it
				// wouldn't count without this.
			} else if (Character.isLetter(s.charAt(i)) && i == endOfLine) {
				wordCount++;
			}
		}
		return wordCount;
	}

	public static String convertToUTF8(String s) {
		String out = null;
		try {
			out = new String(s.getBytes("UTF-8"), "ISO-8859-1");
		} catch (java.io.UnsupportedEncodingException e) {
			return null;
		}
		return out;
	}

}
