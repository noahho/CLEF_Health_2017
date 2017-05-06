package loaders.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import loaders.Loader;
import model.PubMedFile;
import model.database.PubMedDatabase;
import model.database.TopicsDatabase;
import model.decorators.QueryScoreDecorator;
import model.input.TopicInputFile;
import model.wrappers.PubMedFileLearningWrapper;
import common.Config;

public class QuerySearchLoader extends Loader {
	private static org.apache.log4j.Logger log = Logger
			.getLogger(QuerySearchLoader.class);
	private TopicInputFile queryTopic;
	private Analyzer analyzer;
	private IndexSearcher searcher;
	private int num;

	public QuerySearchLoader(PubMedDatabase pmDatabase,
			TopicsDatabase tpDatabase, Analyzer analyzer, IndexSearcher searcher) {
		super(pmDatabase, tpDatabase);
		this.analyzer = analyzer;
		this.searcher = searcher;
		
		BooleanQuery.setMaxClauseCount(10000);
		try {
			this.num = this.searcher.getIndexReader().getDocCount("id");
		} catch (IOException e) {
			throw new RuntimeException("Could not read index");
		}
	}

	public void setQueryTopic(TopicInputFile queryTopic) {
		this.queryTopic = queryTopic;
	}

	public void loadMethod() {
		TopicQuery q = new TopicQuery(this.queryTopic, analyzer, null);
		
		try {
			this.searchFor("title_in_abstract_fuzzy", q.generateQuery("title", "abstract", null, 1, true));
			this.searchFor("query_in_abstract_fuzzy", q.generateQuery("query", "abstract", null, 1, true));
			this.searchFor("title_in_title_fuzzy", q.generateQuery("title", "title", null, 1, true));
			this.searchFor("query_in_title_fuzzy", q.generateQuery("query", "title", null, 1, true));
			this.searchFor("title_in_meshHeading_fuzzy", q.generateQuery("title", "meshHeadings", null, 1, true));
			this.searchFor("query_in_meshHeading_fuzzy", q.generateQuery("query", "meshHeadings", null, 1, true));
			
			
			this.searchFor("title_in_abstract", q.generateQuery("title", "abstract", null, 1, false));
			this.searchFor("query_in_abstract", q.generateQuery("query", "abstract", null, 1, false));
			this.searchFor("title_in_title", q.generateQuery("title", "title", null, 1, false));
			this.searchFor("query_in_title", q.generateQuery("query", "title", null, 1, false));
			this.searchFor("title_in_meshHeading", q.generateQuery("title", "meshHeadings", null, 1, false));
			this.searchFor("query_in_meshHeading", q.generateQuery("query", "meshHeadings", null, 1, false));
			
			this.searchFor("title_in_abstract_metamap_test", q.generateQuery("title", "abstract", "test", 1, false));
			this.searchFor("title_in_abstract_metamap_target", q.generateQuery("title", "abstract", "target", 1, false));
			this.searchFor("title_in_abstract_metamap_general", q.generateQuery("title", "abstract", "general", 1, false));
			
			this.searchFor("query_in_abstract_metamap_test", q.generateQuery("query", "abstract", "test", 1, false));
			this.searchFor("query_in_abstract_metamap_target", q.generateQuery("query", "abstract", "target", 1, false));
			this.searchFor("query_in_abstract_metamap_general", q.generateQuery("query", "abstract", "general", 1, false));
			
			this.searchFor("title_in_title_metamap_test", q.generateQuery("title", "title", "test", 1, false));
			this.searchFor("title_in_title_metamap_target", q.generateQuery("title", "title", "target", 1, false));
			this.searchFor("title_in_title_metamap_general", q.generateQuery("title", "title", "general", 1, false));
			
			this.searchFor("query_in_title_metamap_test", q.generateQuery("query", "title", "test", 1, false));
			this.searchFor("query_in_title_metamap_target", q.generateQuery("query", "title", "target", 1, false));
			this.searchFor("query_in_title_metamap_general", q.generateQuery("query", "title", "general", 1, false));
			
		} catch (Exception e) {
			throw new RuntimeException("Error creating the query results");
		}
		
	}
	
	// Adds the scores for a query to the documents it matters for
	public void searchFor(String type, Query query) throws IOException
	{
		if (query == null)
			return;
		// 3. search
		TopScoreDocCollector collector = TopScoreDocCollector
				.create(num);

		// TODO we want to get all documents
		TopDocs docs = searcher.search(query, num);
		ScoreDoc[] hits = docs.scoreDocs;

		searcher.search(query, collector);

		// 4. display results
		for (int i = 0; i < hits.length; ++i) {
			int docId = hits[i].doc;
			Document d = searcher.doc(docId);
			int pid = Integer.parseInt(d.get("id"));

			addScore(type, hits[i].score, pid);
		}
	}
	
	public void addScore(String type, float score, int pid) {
		if (this.pmWorkingSet.containsKey(pid))
		{
			this.pmWorkingSet.access(pid).queryScore.addScore(this.queryTopic.getTopicID(), type,
					score);
		}
	}
}
