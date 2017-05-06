package common;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;

import loaders.ContentLoader;
import loaders.FeatureLoader;
import loaders.GensimSavingLoader;
import loaders.InputQrelLoader;
import loaders.InputTopicsLoader;
import loaders.MetaMapLoader;
import loaders.StaticMetaDataLoader;
import loaders.lucene.IndexLoader;
import loaders.lucene.QuerySearchLoader;
import model.database.PubMedDatabase;
import model.database.TopicsDatabase;
import model.input.TopicInputFile;

public class Preprocessing {
	public static void run() {
		PubMedDatabase pmDatabase = new PubMedDatabase();
		TopicsDatabase tpDatabase = new TopicsDatabase();

		InputQrelLoader qrelLoader = new InputQrelLoader(pmDatabase, tpDatabase);
		InputTopicsLoader iTLoader = new InputTopicsLoader(pmDatabase,
				tpDatabase);

		qrelLoader.load();
		iTLoader.load();
		
		// Redownload articles from the web
		if (Config.DOWNLOADPUBMED) {
			ContentLoader contentLoader = new ContentLoader(pmDatabase,
					tpDatabase);
			contentLoader.load();
		}
		
		//Save all the abstracts and all the titles into two files for input to metamap
		if (Config.RECREATE_METAMAP_ARTICLE_INPUT) {
			File file = new File(
					Config.metaMapInputArticleAbstractsFile);
			file.delete();
			file = new File(Config.metaMapInputArticleTitlesFile);
			file.delete();
			System.out.println("Delete old abstract file");
			
			ContentLoader contentLoader = new ContentLoader(pmDatabase,
					tpDatabase);
			contentLoader.saveAbstractsAndTitles();
		}
		
		//Save the topic titles and parsed queries for input to metamap
		if (Config.RECREATE_METAMAP_TOPIC_INPUT)
		{
			File file = new File(Config.gensimInputTopicsTitlesFile);
			file.delete();
			file = new File(Config.gensimInputTopicsQueriesFile);
			file.delete();
			for (TopicInputFile topic : tpDatabase.getValues())
			{
				topic.saveTopicMetaMap();
				topic.saveTopicGenSim();
			}
		}
		
		//Process the output from metamap
		if (Config.RECREATE_METAMAP_OUTPUT) {
			MetaMapLoader metaMapLoader = new MetaMapLoader(pmDatabase,
					tpDatabase);
			metaMapLoader.load();
		}
	
		// Specify the analyzer for tokenizing text.
		// The same analyzer should be used for indexing and searching
		StandardAnalyzer analyzer = new StandardAnalyzer();
		
		/*if (Config.RECREATE_ADD_GENSIM)
		{
			GensimProcessingLoader gensimProcessingLoader = new GensimProcessingLoader(pmDatabase, tpDatabase,
					analyzer);
			gensimProcessingLoader.load();
		}*/
		
		if (Config.RECREATE_INDEX || Config.RECREATE_METADATA || Config.RECREATE_GENSIM_INPUT) {
			List<PubMedDatabase> pmChunks = pmDatabase
					.splitDatabase(Config.database_workset_max_size);
			
			IndexLoader indexLoader = new IndexLoader(pmDatabase, tpDatabase,
					analyzer);
			StaticMetaDataLoader staticMetaDataLoader = new StaticMetaDataLoader(pmDatabase,
					tpDatabase);
			GensimSavingLoader gensimLoader = new GensimSavingLoader(pmDatabase,
					tpDatabase);
			
			if (Config.RECREATE_GENSIM_INPUT)
				gensimLoader.clearFile();
			
			int done = 0;
			for (PubMedDatabase currentWorkingSet : pmChunks) {
				indexLoader.setWorkingSet(currentWorkingSet);
				gensimLoader.setWorkingSet(currentWorkingSet);
				staticMetaDataLoader.setWorkingSet(currentWorkingSet);

				long startTime = System.currentTimeMillis();
				currentWorkingSet.parallelStream().forEach(
						s -> s.content.cache());
				long endTime = System.currentTimeMillis();
			    System.out.println("Time for caching: " + (endTime-startTime) + "ms"); 

			    if (Config.RECREATE_METADATA)
					staticMetaDataLoader.load();
			    if (Config.RECREATE_GENSIM_INPUT)
			    	gensimLoader.load();
				if (Config.RECREATE_INDEX)
					indexLoader.load();

				currentWorkingSet.parallelStream().forEach(
						s -> s.content.uncache());
				done = done + Config.database_workset_max_size;
				System.out.println(done+"/"+pmDatabase.size()+" (memory: "+(float)Runtime.getRuntime().freeMemory()/Runtime.getRuntime().totalMemory()+")");
			}
			if (Config.RECREATE_METADATA)
				staticMetaDataLoader.save();
			indexLoader.close();
		}
		
		if (Config.RECREATE_FEATS) {
			System.out.println("Creating features");
			IndexReader reader;

			try {
				reader = DirectoryReader.open(FSDirectory.open(Paths
						.get(Config.articlesIndexDir)));
			} catch (IOException e1) {
				throw new RuntimeException("Index could not be read!");
			}
			IndexSearcher searcher = new IndexSearcher(reader);

			// MlLoader ml = new MlLoader(pmDatabase, tpDatabase, searcher);
			QuerySearchLoader qsLoader = new QuerySearchLoader(pmDatabase,
					tpDatabase, analyzer, searcher);

			Set<String> keys = tpDatabase.getKeys();
			for (String topicI : keys) {
				// Set to working set only to the articles that are
				// considered
				// for being related for each topic
				qsLoader.setWorkingSet(pmDatabase.filterTopic(topicI));
				// Now set the topic for the query
				qsLoader.setQueryTopic(tpDatabase.access(topicI));
				// Load the results of queries for the topic
				qsLoader.load();
			}

			System.out.println("Performed search on all documents");
			
			FeatureLoader featLoader = new FeatureLoader(pmDatabase,
					tpDatabase, searcher);
			featLoader.clearWorkingDirectory();
			featLoader.load();

			try {
				reader.close();
			} catch (IOException e) {
				throw new RuntimeException("Could not close Index reader");
			}

			/*
			 * for (String topic : tpDatabase.getKeys()) {
			 * 
			 * List<String> testTopics = new ArrayList<String>(); int testN = 0;
			 * testTopics.add(topic); testN +=
			 * tpDatabase.access("topic").getPids().size(); while (testN < 0.2 *
			 * pmDatabase.size()) { String randomKey =
			 * tpDatabase.getRandomKey(); testTopics.add(randomKey); testN +=
			 * tpDatabase.access(randomKey).getPids().size(); }
			 * 
			 * 
			 * File featureTrainFile = new File(Config.features + "\\feat_train"
			 * + topic); featureTrainFile.delete(); for (String topicI :
			 * tpDatabase.getKeys()) { if (!topicI.equals(topic)) { String c;
			 * try { c = Utils.readFile(new File(Config.features + "\\feats" +
			 * topicI), StandardCharsets.UTF_8); } catch (IOException e) { throw
			 * new RuntimeException( "Couldnt read feature file"); }
			 * Utils.saveFile(featureTrainFile, c, StandardCharsets.UTF_8,
			 * true); } } }
			 */
			System.out.println("Done creating feature vectors");
		} else {
			System.out.println("Skipped creating feature vectors");
		}
	}
}
