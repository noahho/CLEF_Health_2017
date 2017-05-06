package common;

import helpers.KeyValuePair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import loaders.InputQrelLoader;
import loaders.StaticMetaDataProcessingLoader;
import model.database.PubMedDatabase;
import model.database.TopicsDatabase;
import model.wrappers.PubMedFileLearningWrapper;
import model.wrappers.PubMedFileLearningWrapper.LabeledAndTaggedDp;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.lucene.search.IndexSearcher;

import trec_eval.trec_eval;

public class TrainAndPredict {
	private static org.apache.log4j.Logger log = Logger
			.getLogger(TrainAndPredict.class);

	public static void Train(TopicsDatabase tpDatabase) {
		// for (String topic : tpDatabase.getKeys()) {
		// System.out.println("Training for " + topic);
		// Run a java app in a separate system process
		File featureTrainFile = new File(Config.learningFeaturesDir
				+ "\\feats_train_nozeros_rebalanced");
		String featureTestFile = Config.learningFeaturesDir + "\\feats_test_nozeros";
		String modelFile = Config.learningModelFile;
		try {
			String execCmd = "";
			String mart = " -ranker 6 -leaf 12";
			String adaBoost = " -ranker 7 -round 50";
			String rankNet = " -ranker 1 -round 50";
			String coordinateDescent = " -ranker 4";

			if (Config.RANKLIB.equals("RankLib")) {
				execCmd = "java -jar \"" + Config.rankLibJar + "\" "
						+ "-train \"" + featureTrainFile + "\" "
						//+ "-feature \"" + Config.learningFeaturesFile + "\" "
						+ "-validate \"" + featureTestFile + "\" "
						+ "-metric2t MAP " + "-gmax 1 " + "-save \""
						+ modelFile + "\"" + mart;
			}

			System.out.println(execCmd);
			ProcessBuilder builder = new ProcessBuilder(execCmd.split(" "));
			final Process process = builder.start();
			final Thread ioThread = new Thread() {
				@Override
				public void run() {
					try {
						final BufferedReader reader = new BufferedReader(
								new InputStreamReader(process.getInputStream()));
						String line = null;
						while ((line = reader.readLine()) != null) {
							System.out.println(line);
						}
						reader.close();
					} catch (final Exception e) {
						e.printStackTrace();
					}
				}
			};
			ioThread.start();
			
			try {
				process.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				process.destroy();
			}
		} catch (IOException e) {
			throw new RuntimeException("The jar for ranklib was not found!");
		}
		// }
	}

	public static void Predict(TopicsDatabase tpDatabase) {
		// for (String topic : tpDatabase.getKeys()) {
		System.out.println("Predicting");
		String inputFile = Config.learningFeaturesDir + "\\feats_test_nozeros";
		String outputFile = Config.learningScoresDir + "\\score";
		String modelFile = Config.learningModelFile;
		try {
			String execCmd = "";
			if (Config.RANKLIB.equals("RankLib")) {
				execCmd = "java -jar \"" + Config.rankLibJar + "\""
						// + " -feature \"" + Config.learningFeaturesFile + "\" "
						+ " -load  \"" + modelFile + "\"" + " -rank \""
						+ inputFile + "\"" + " -gmax 1" + " -score \""
						+ outputFile + "\"";
			}

			System.out.println(execCmd);
			ProcessBuilder builder = new ProcessBuilder(execCmd.split(" "));
			final Process process = builder.start();
			final Thread ioThread = new Thread() {
				@Override
				public void run() {
					try {
						final BufferedReader reader = new BufferedReader(
								new InputStreamReader(process.getInputStream()));
						String line = null;
						while ((line = reader.readLine()) != null) {
							System.out.println(line);
						}
						reader.close();
					} catch (final Exception e) {
						e.printStackTrace();
					}
				}
			};
			ioThread.start();

			try {
				process.waitFor();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e) {
			throw new RuntimeException("The jar for ranklib was not found!");
		}
		// }
	}

	public static void CreateTREC(PubMedDatabase pmDatabase,
			TopicsDatabase tpDatabase) {
		File featureFile = new File(Config.learningFeaturesDir + "\\feats_test");
		File scoreFile = new File(Config.learningScoresDir + "\\score");
		File trecFile = new File(Config.trec);
		trecFile.delete();
		try {
			String[] feats = Utils
					.readFile(featureFile, StandardCharsets.UTF_8).split(
							"[\\r\\n]+");
			String[] scores = Utils.readFile(scoreFile, StandardCharsets.UTF_8)
					.split("[\\r\\n]+");
			TreeMap<Double, KeyValuePair<Integer, String>> scoreMap = new TreeMap<Double, KeyValuePair<Integer, String>>(
					Collections.reverseOrder());

			if (scores.length != feats.length)
				throw new RuntimeException();
			for (int i = 0; i < feats.length; i++) {
				String[] d = feats[i].split(".*pid:");
				String[] d2 = scores[i].split("\\s");
				scoreMap.put(
						Double.parseDouble(d2[2]),
						new KeyValuePair<Integer, String>(Integer
								.parseInt(d[1]), d2[0]));
			}
			Set<Entry<Double, KeyValuePair<Integer, String>>> set = scoreMap
					.entrySet();
			Iterator<Entry<Double, KeyValuePair<Integer, String>>> iterator = set
					.iterator();
			String evalC = "";
			int i = 1;
			while (iterator.hasNext()) {
				Entry<Double, KeyValuePair<Integer, String>> mentry = iterator
						.next();
				/*
				 * if
				 * (pmDatabase.access(mentry.getValue().key).relevantTopicList
				 * .contains(topic)) last_relevant.put("topic",
				 * counts.get("topic"));
				 */
				evalC += "CD" + (mentry.getValue()).value + "\t0\t"
						+ (mentry.getValue()).key + "\t" + i + "\t"
						+ mentry.getKey() + "\t0" + System.lineSeparator();
				i++;
			}

			Utils.saveFile(trecFile, evalC, StandardCharsets.UTF_8, true);
		} catch (IOException e) {
			throw new RuntimeException("Feature file couldnt be read");
		}
	}

	public static void Evaluate(PubMedDatabase pmDatabase,
			TopicsDatabase tpDatabase) {
		trec_eval te = new trec_eval();
		String qrels = Config.preprocessingTopicsByAbstractsQrelFile;
		String res = Config.trec;

		String input[] = new String[] { "-q", "-c", "-m", "map", "-m", "ndcg",
				"-m", "recall.100,500,1000,2000", "-m", "P.100,500,1000,200",
				"-m", "num_ret", "-m", "num_rel", qrels, res };

		String[][] output = te.runAndGetOutput(input);
		for (String[] line : output) {
			for (String word : line)
				System.out.print(word + " ");
			System.out.println();
		}
	}
}
