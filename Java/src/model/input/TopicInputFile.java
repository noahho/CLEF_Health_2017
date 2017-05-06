package model.input;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.spark.mllib.linalg.Vector;

import query.QueryHelpers;
import common.Config;
import common.Utils;
import metamap.MetaMapEntry;
import model.PubMedFile;

public class TopicInputFile {
	private List<Integer> pids;
	private String topicID;
	private String query;
	private String title;
	public Vector gensimTitleVector, gensimQueryVector;
	public MetaMapEntry queryMetaMap;
	public MetaMapEntry titleMetaMap;
	
	private String titleSuffixQuery = null, querySuffixQuery = null;

	public TopicInputFile() {
		this.pids = new ArrayList<Integer>();
	}
	
	public void add(int pid)
	{
		this.pids.add(pid);
	}
	
	public int numTopics()
	{
		return this.pids.size();
	}
	
	public String getTitleSuffix()
	{
		if (this.titleSuffixQuery == null)
			this.titleSuffixQuery = QueryHelpers.parseQuery(title);
		return this.titleSuffixQuery;
	}
	
	public String getQuerySuffix()
	{
		if (this.querySuffixQuery == null)
			this.querySuffixQuery = QueryHelpers.parseQuery(query);
		return this.querySuffixQuery;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTopicID() {
		return topicID;
	}

	public String getQuery() {
		return query;
	}

	public void setTopicID(String topicID) {
		this.topicID = topicID;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	// Save the title and query in the file for gensim input
	public void saveTopicGenSim() {
		File file = new File(Config.gensimInputTopicsTitlesFile);
		Utils.saveFile(file, this.getTopicID()+":"+this.getTitle()+"\n", StandardCharsets.UTF_8, true);

		file = new File(Config.gensimInputTopicsQueriesFile);
		Utils.saveFile(file, this.getTopicID()+":"+QueryHelpers.reduceQuery(this.getQuery())+"\n",
				StandardCharsets.UTF_8, true);
	}

	// Save the title and query in the file for metamap input
	public void saveTopicMetaMap() {
		File file = new File(Config.metaMapInputTopicsSeparateDir + "\\"
				+ this.getTopicID() + "_topic_title.txt");
		file.delete();
		Utils.saveFile(file, this.getTitle(), StandardCharsets.UTF_8);

		file = new File(Config.metaMapInputTopicsSeparateDir + "\\"
				+ this.getTopicID() + "_topic_query.txt");
		file.delete();
		Utils.saveFile(file, QueryHelpers.reduceQuery(this.getQuery()),
				StandardCharsets.UTF_8);
	}
}
