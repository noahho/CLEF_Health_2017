package loaders.ml;

import helpers.KeyValuePair;

import java.io.File;

import loaders.Loader;
import model.database.PubMedDatabase;
import model.database.TopicsDatabase;
import model.wrappers.PubMedFileIndexingWrapper;
import model.wrappers.PubMedFileLearningWrapper;
import model.wrappers.PubMedFileLearningWrapper.LabeledAndTaggedDp;

import org.apache.log4j.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Collectors;

import scala.Tuple2;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.apache.log4j.spi.Configurator;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator;
import org.apache.spark.ml.tuning.CrossValidator;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.tree.RandomForest;
import org.apache.spark.mllib.tree.configuration.BoostingStrategy;
import org.apache.spark.mllib.tree.model.RandomForestModel;
import org.apache.spark.mllib.util.MLUtils;

public class MlLoader {
	SparkConf sparkConf;
	JavaSparkContext jsc;
	private static org.apache.log4j.Logger log = Logger
			.getLogger(MlLoader.class);
	private RandomForestModel model;
	private PubMedDatabase pmDatabase;
	private TopicsDatabase tpDatabase;
	private PubMedDatabase pmWorkingSet;
	private IndexSearcher searcher;

	public MlLoader(PubMedDatabase pmDatabase, TopicsDatabase tpDatabase,
			IndexSearcher searcher) {
		this.pmDatabase = pmDatabase;
		this.tpDatabase = tpDatabase;
		this.pmWorkingSet = pmDatabase;

		this.searcher = searcher;

		this.sparkConf = new SparkConf().setAppName(
				"JavaRandomForestClassification").setMaster("local[1]");
		this.jsc = new JavaSparkContext(sparkConf);
		
	}

	public List<PredictionResult> predict(List<LabeledAndTaggedDp> dps) {

		return dps
				.stream()
				.map(s -> new PredictionResult(s.pid, s.topicId, model
						.predict(s.dp.features())))
				.collect(Collectors.toList());
	}

	public class PredictionResult {
		public int pid;
		public String topicId;
		public double score;

		public PredictionResult(int pid, String topicId, double score) {
			this.pid = pid;
			this.topicId = topicId;
			this.score = score;
		}
	}

	public double crossValidation() {
		/*
		 * int trainSetSize = ((int) Math.ceil(this.pmWorkingSet.size() / 5)) *
		 * 4; List<PubMedDatabase> pmChunks = this.pmWorkingSet.splitDatabase(
		 * trainSetSize, 2); train(pmChunks.get(0)); List<PredictionResult>
		 * predictions = predict(pmChunks.get(1));
		 */
		return 1;
		/*
		 * predictions.stream().forEach( s -> s.value = (double)
		 * this.binaryClass(s.value));
		 * 
		 * System.out.println("N " + pmChunks.get(1).size() + "; unprecise " +
		 * predictions.stream() .mapToDouble(this::evaluatePointPrecision).sum()
		 * + " ; not found:" +
		 * predictions.stream().mapToDouble(this::evaluatePointRecall) .sum());
		 * 
		 * return predictions.stream().mapToDouble(this::evaluatePointRecall)
		 * .sum();
		 */
	}

	public int binaryClass(double score) {
		return (score > 0.006) ? 1 : 0;
	}

	/*
	 * public double evaluatePointRecall(KeyValuePair<Integer, Double> point) {
	 * int relevance = 0; if
	 * (this.pmDatabase.access(point.key).relevantTopicList
	 * .contains(this.topic)) relevance = 1;
	 * 
	 * return (relevance - point.value == 1) ? 1 : 0; }
	 * 
	 * public double evaluatePointPrecision(KeyValuePair<Integer, Double> point)
	 * { int relevance = 0; if
	 * (this.pmDatabase.access(point.key).relevantTopicList
	 * .contains(this.topic)) relevance = 1;
	 * 
	 * return (point.value - relevance == 1) ? 1 : 0; }
	 */

	public void train(List<LabeledAndTaggedDp> dps) {
		JavaRDD<LabeledPoint> data = jsc.parallelize(dps.stream()
				.map(s -> s.dp).collect(Collectors.toList()));

		// Train a RandomForest model.
		// Empty categoricalFeaturesInfo indicates all features are continuous.
		Integer numClasses = 2;
		HashMap<Integer, Integer> categoricalFeaturesInfo = new HashMap<>();
		Integer numTrees = 3; // Use more in practice.
		String featureSubsetStrategy = "auto"; // Let the algorithm choose.
		String impurity = "variance";
		Integer maxDepth = 5;
		Integer maxBins = 32;
		Integer seed = 12345;

		this.model = RandomForest.trainRegressor(data, categoricalFeaturesInfo,
				numTrees, featureSubsetStrategy, impurity, maxDepth, maxBins,
				seed);

		// Save and load model
		// FileUtils.deleteDirectory(new
		// File("target/tmp/myRandomForestClassificationModel"));

		// model.save(jsc.sc(), "target/tmp/myRandomForestClassificationModel");
		// RandomForestModel sameModel = RandomForestModel.load(jsc.sc(),
		// "target/tmp/myRandomForestClassificationModel");
	}
}
