package model.wrappers;

import loaders.StaticMetaDataProcessingLoader;
import model.PubMedFile;
import model.database.TopicsDatabase;
import model.decorators.QueryScoreDecorator.QueryScoreEntry;
import model.input.TopicInputFile;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;

import common.Utils;
import helpers.Gensim;
import helpers.KeyValuePair;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class PubMedFileLearningWrapper {

	private PubMedFile file;
	private double prediction;

	public double getPrediction() {
		return prediction;
	}

	public void setPrediction(double prediction) {
		this.prediction = prediction;
	}

	private final static Logger LOGGER = Logger
			.getLogger(PubMedFileLearningWrapper.class.getName());

	public PubMedFileLearningWrapper(PubMedFile file) {
		this.file = file;
	}

	public List<LabeledAndTaggedDp> getDataPoint(IndexSearcher searcher,
			TopicsDatabase tpDatabase, StaticMetaDataProcessingLoader smdpLoader) {

		Query q1 = new TermQuery(new Term("id", String.valueOf(this.file.PID)));
		TopDocs td1;
		int pubYear;
		Vector gensimAbstract, gensimMesh, gensimTitle;
		String abstractText, titleText, meshHeadings;
		String metaMapGeneralCuis, metaMapTargetCuis, metaMapTestCuis;
		String language, sources;
		List<LabeledAndTaggedDp> result = new ArrayList<LabeledAndTaggedDp>();
		StaticMetaDataProcessingLoader.LanguageResult langResult = null;
		StaticMetaDataProcessingLoader.SourcesResult sourceResult = null;
		
		double abstractGensimRelevancy, titleGensimRelevancy, meshHeadingsGensimRelevancy;
		double abstractGensimRelevancy_2, titleGensimRelevancy_2, meshHeadingsGensimRelevancy_2;
		
		try {
			td1 = searcher.search(q1, 1);
			int docId = td1.scoreDocs[0].doc;
			Document d;
			d = searcher.doc(docId);
			try {
				pubYear = Integer.parseInt(d.get("pubYear"));
			} catch (NumberFormatException e) {
				pubYear = 0;
			}
			
			gensimAbstract = Gensim.getGensimFileVector(d.get("gensimAbstract"));
			gensimTitle = Gensim.getGensimFileVector(d.get("gensimTitle"));
			gensimMesh = Gensim.getGensimFileVector(d.get("gensimMesh"));
			
			abstractGensimRelevancy = Gensim.vectorSimilarity(gensimAbstract, smdpLoader.getAbstractRelevanceVector());
			titleGensimRelevancy = Gensim.vectorSimilarity(gensimTitle, smdpLoader.getTitleRelevanceVector());
			meshHeadingsGensimRelevancy = Gensim.vectorSimilarity(gensimMesh, smdpLoader.getMeshHeadingsRelevanceVector());
			
			abstractGensimRelevancy_2 = Gensim.vectorSimilarity(gensimAbstract, smdpLoader.getAbstractRelevanceVector_2());
			titleGensimRelevancy_2 = Gensim.vectorSimilarity(gensimTitle, smdpLoader.getTitleRelevanceVector_2());
			meshHeadingsGensimRelevancy_2 = Gensim.vectorSimilarity(gensimMesh, smdpLoader.getMeshHeadingsRelevanceVector_2());
			
			metaMapGeneralCuis = d.get("abstract_metamap_general");
			metaMapTargetCuis = d.get("abstract_metamap_target");
			metaMapTestCuis = d.get("abstract_metamap_test");
			
			language = d.get("language");
			sources = d.get("sources");
			langResult = smdpLoader.scoreLanguage(language);
			sourceResult = smdpLoader.scoreSources(sources);
			
			abstractText = d.get("abstract");
			titleText = d.get("title");
			meshHeadings = d.get("meshHeadings");
			
			
		} catch (IOException e) {
			throw new RuntimeException("Error retrieving a datapoint");
		}

		for (String topic : this.file.retrievedQrelTopicList) {
			TopicInputFile topicFile = tpDatabase.access(topic);
			
			String topicTitle = topicFile.getTitle();
			String topicQuery = topicFile.getQuery();
			
			double gensimAbstractQuerySimilarity = Gensim.vectorSimilarity(gensimAbstract, topicFile.gensimQueryVector);
			double gensimAbstractTitleSimilarity = Gensim.vectorSimilarity(gensimAbstract, topicFile.gensimTitleVector);
			
			double gensimTitleQuerySimilarity = Gensim.vectorSimilarity(gensimTitle, topicFile.gensimQueryVector);
			double gensimTitleTitleSimilarity = Gensim.vectorSimilarity(gensimTitle, topicFile.gensimTitleVector);
			
			double gensimMeshHeadingQuerySimilarity = Gensim.vectorSimilarity(gensimMesh, topicFile.gensimQueryVector);
			double gensimMeshHeadingTitleSimilarity = Gensim.vectorSimilarity(gensimMesh, topicFile.gensimTitleVector);
			
			int label = 0;
			if (this.file.relevantTopicList.contains(topic))
				label = 1;

			QueryScoreEntry queryResults = this.file.queryScore.getScore(topic);
			LabeledPoint dp = new LabeledPoint(label, Vectors.dense(
					queryResults.get("query_in_abstract"),
					queryResults.get("title_in_title"),
					queryResults.get("query_in_title"),
					queryResults.get("title_in_abstract"),
					queryResults.get("query_in_meshHeading"),
					queryResults.get("title_in_meshHeading"),
					
					//7
					queryResults.get("query_in_title_fuzzy"),
					queryResults.get("title_in_title_fuzzy"),
					queryResults.get("query_in_abstract_fuzzy"),
					queryResults.get("title_in_abstract_fuzzy"),
					queryResults.get("query_in_meshHeading_fuzzy"),
					queryResults.get("title_in_meshHeading_fuzzy"),
					
					// 13
					queryResults.get("title_in_abstract_metamap_test"),
					queryResults.get("title_in_abstract_metamap_target"), // 93.5% ZEROS
					queryResults.get("title_in_abstract_metamap_general"),

					//16
					queryResults.get("query_in_abstract_metamap_test"),
					queryResults.get("query_in_abstract_metamap_target"),
					queryResults.get("query_in_abstract_metamap_general"),

					//19
					queryResults.get("title_in_title_metamap_test"),
					queryResults.get("title_in_title_metamap_target"),
					queryResults.get("title_in_title_metamap_general"),

					//22
					queryResults.get("query_in_title_metamap_test"),
					queryResults.get("query_in_title_metamap_target"),
					queryResults.get("query_in_title_metamap_general"),

					//25
					Utils.countWords(abstractText),
					Utils.countWords(titleText),
					Utils.countWords(meshHeadings),
					
					//28
					Utils.countWords(topicTitle),
					Utils.countWords(topicQuery),
					
					//30
					Utils.countWords(metaMapGeneralCuis), 
					Utils.countWords(metaMapTargetCuis), 
					Utils.countWords(metaMapTestCuis), 

					//TODO THE SAME COUNTS FOR TITLE
					//33
					pubYear,

					Utils.makeSafeDouble(sourceResult.avgAvg), Utils.makeSafeDouble(sourceResult.avgMax),
					Utils.makeSafeDouble(sourceResult.scoreAvg), Utils.makeSafeDouble(sourceResult.scoreMax),
					Utils.makeSafeDouble(sourceResult.scoreSum),

					//39
					Utils.makeSafeDouble(langResult.avg), Utils.makeSafeDouble(langResult.score),
					
					//41
					gensimAbstractQuerySimilarity, gensimAbstractTitleSimilarity,
					gensimTitleQuerySimilarity, gensimTitleTitleSimilarity,
					gensimMeshHeadingQuerySimilarity, gensimMeshHeadingTitleSimilarity,
					
					//47
					abstractGensimRelevancy,
					titleGensimRelevancy,
					meshHeadingsGensimRelevancy,
					
					abstractGensimRelevancy_2,
					titleGensimRelevancy_2,
					meshHeadingsGensimRelevancy_2
					));

			result.add(new LabeledAndTaggedDp(this.file.PID, topic, dp));
		}
		return result;
	}

	public Integer getPid() {
		return this.file.getPID();
	}

	public class LabeledAndTaggedDp {
		public int pid;
		public String topicId;
		public LabeledPoint dp;

		public LabeledAndTaggedDp(int pid, String topicId, LabeledPoint dp) {
			this.pid = pid;
			this.topicId = topicId;
			this.dp = dp;
		}

		public String toRanklibFeatureString() {
			String r = (int) this.dp.label() + " qid:"
					+ this.topicId.substring(2, 8) + " ";
			int i = 0;
			for (Double f : this.dp.features().toArray()) {
				i++;
				r += i + ":" + f + " ";
			}
			return r + "# pid:" + this.pid;

		}
	}
}
