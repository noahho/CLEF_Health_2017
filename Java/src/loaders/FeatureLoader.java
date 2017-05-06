package loaders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;

import query.TopicQuery;
import model.PubMedFile;
import model.database.PubMedDatabase;
import model.database.TopicsDatabase;
import model.decorators.QueryScoreDecorator;
import model.input.TopicInputFile;
import model.wrappers.PubMedFileLearningWrapper;
import model.wrappers.PubMedFileLearningWrapper.LabeledAndTaggedDp;
import common.Config;

public class FeatureLoader extends Loader {
	private static org.apache.log4j.Logger log = Logger
			.getLogger(FeatureLoader.class);
	private IndexSearcher searcher;
	private int num;
	StaticMetaDataProcessingLoader smdp;

	public FeatureLoader(PubMedDatabase pmDatabase, TopicsDatabase tpDatabase,
			IndexSearcher searcher) {
		super(pmDatabase, tpDatabase);
		this.searcher = searcher;
		smdp = new StaticMetaDataProcessingLoader(pmDatabase, tpDatabase);
		smdp.load();
	}

	public void clearWorkingDirectory() {
		File featureFileTest = new File(Config.learningFeaturesDir
				+ "\\feats_test");
		File featureFileTrain = new File(Config.learningFeaturesDir
				+ "\\feats_train");
		featureFileTest.delete();
		featureFileTrain.delete();
	}

	public void loadMethod() {
		// Collect the feature vectors from all pubmedfiles in combination
		// with each topic it was retrieved from

		List<LabeledAndTaggedDp> dataset = pmDatabase.stream()
				.map(s -> new PubMedFileLearningWrapper(s))
				.map(s -> s.getDataPoint(searcher, tpDatabase, smdp))
				.flatMap(List::stream).collect(Collectors.toList());

		Collections.sort(dataset, new Comparator<LabeledAndTaggedDp>() {
			@Override
			public int compare(LabeledAndTaggedDp a1, LabeledAndTaggedDp a2) {
				int i1 = Integer.parseInt(a1.topicId.substring(2, 8));
				int i2 = Integer.parseInt(a2.topicId.substring(2, 8));
				return Integer.compare(i1, i2);
			}
		});

		// for (String topic : tpDatabase.getKeys()) {
		String featureFileTest = Config.learningFeaturesDir + "\\feats_test";
		String featureFileTrain = Config.learningFeaturesDir + "\\feats_train";
		List<LabeledAndTaggedDp> trainSet = new ArrayList<LabeledAndTaggedDp>();
		List<LabeledAndTaggedDp> testSet = new ArrayList<LabeledAndTaggedDp>();

		dataset.stream().forEach(s -> {
			//if (!Config.staticSet.contains(s.topicId)) {
				if (Config.testSet.contains(s.topicId))
					testSet.add(s);
				else
					trainSet.add(s);
			//}
		});
		
		createFeatsForList(trainSet, featureFileTrain);
		createFeatsForList(testSet, featureFileTest);
		// }
	}

	public static void createFeatsForList(List<LabeledAndTaggedDp> dataset,
			String featureFile) {
		// Does the old vector get correctly erased? Have to
		// test this!
		// Save the vector for the training file
		try (PrintWriter out = new PrintWriter(new FileOutputStream(
				featureFile, false))) {
			for (LabeledAndTaggedDp dp : dataset) {
				out.write(dp.toRanklibFeatureString() + System.lineSeparator());
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Could not save the feature vector!");
		}
	}
}
