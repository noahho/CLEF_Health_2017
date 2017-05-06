package helpers;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;

public class Gensim {
	public static Vector getGensimFileVector(String content)
	{
		content = content.replaceAll("(?s)[0-9]*:?\\[(.*)\\]", "$1");
		List<String> split = Arrays.asList(content.split("\\s+")).stream().filter(a -> !a.equals("")).collect(Collectors.toList());
		Vector v = Vectors.dense(split.stream()
				.mapToDouble(a -> Double.parseDouble(a)).toArray());
		return v;
	}
	
	public static double vectorSimilarity(Vector v1, Vector v2)
	{
		if (v1.size() != v2.size())
			return 0;
		double dot = org.apache.spark.mllib.linalg.BLAS.dot(v1, v2);
		double norms = Vectors.norm(v1, 2) * Vectors.norm(v2, 2);
		return dot / norms;
	}
}
