package common;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import loaders.ContentLoader;
import loaders.FeatureLoader;
import loaders.GensimSavingLoader;
import loaders.InputQrelLoader;
import loaders.InputTopicsLoader;
import loaders.StaticMetaDataLoader;
import loaders.lucene.IndexLoader;
import loaders.lucene.QuerySearchLoader;
import model.database.PubMedDatabase;
import model.database.TopicsDatabase;
import model.input.TopicInputFile;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;

public class EvaluationRun {
	public static void run() {
		System.out.println("Reading the input topic files...");

		PubMedDatabase pmDatabase = new PubMedDatabase();
		TopicsDatabase tpDatabase = new TopicsDatabase();

		InputQrelLoader qrelLoader = new InputQrelLoader(pmDatabase, tpDatabase);
		InputTopicsLoader iTLoader = new InputTopicsLoader(pmDatabase,
				tpDatabase);

		iTLoader.load();
		qrelLoader.load();

		for (String topicI : tpDatabase.getKeys()) {
			System.out.println(tpDatabase.access(topicI).numTopics());
		}
		System.out.println("Overall " + pmDatabase.size() + " articles");

		pmDatabase = pmDatabase.filterNotRetrievedFromTopic();

		System.out.println("Working with " + pmDatabase.size() + " articles");

		// Specify the analyzer for tokenizing text.
		// The same analyzer should be used for indexing and searching
		StandardAnalyzer analyzer = new StandardAnalyzer();

		if (Config.RECREATE_MODEL) {
			TrainAndPredict.Train(tpDatabase);
		} else {
			System.out.println("Skipped training model");
		}

		// APPLY THE PREDICTION ALGORITHM AND SAVE THE PREDICTIONS
		if (Config.RECREATE_PRED) {
			TrainAndPredict.Predict(tpDatabase);
		} else {
			System.out.println("Skipped creating predictions");
		}

		if (Config.RECREATE_TREC)
		{
			System.out.println("Creating trec files");
			TrainAndPredict.CreateTREC(pmDatabase, tpDatabase);
		}
		
		TrainAndPredict.Evaluate(pmDatabase, tpDatabase);
		System.out.println("Done");
	}
}
