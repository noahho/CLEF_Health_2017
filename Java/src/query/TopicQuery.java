package query;

import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.Query;
import org.apache.commons.cli.*;

import common.Config;
import model.PubMedFile;
import model.input.TopicInputFile;

public class TopicQuery {
	TopicInputFile topic;
	Analyzer analyzer;
	List<PubMedFile> relevantFiles;

	public TopicQuery(TopicInputFile topic, Analyzer analyzer,
			List<PubMedFile> relevantFiles) {
		this.topic = topic;
		this.analyzer = analyzer;
		this.relevantFiles = relevantFiles;
	}

	public Query generateQuery(String sourcefield, String destfield,
			String metamap, float boost, boolean fuzzy) {
		String querystr = "";
		String field = "";

		Query q;

		if (destfield.equals("title"))
			field = "title";
		else if (destfield.equals("abstract"))
			field = "abstract";
		else if (destfield.equals("meshHeadings"))
			field = "meshHeadings";
		else
			throw new RuntimeException("The destfield option does not exist: "
					+ destfield);

		if (metamap == null) {
			if (sourcefield.equals("title"))
				querystr = (fuzzy)?  QueryHelpers.parseQuery(this.topic.getTitle()) : this.topic.getTitle();
			else if (sourcefield.equals("query"))
				querystr = (fuzzy)?  QueryHelpers.parseQuery(QueryHelpers.reduceQuery(this.topic.getQuery())) : QueryHelpers.reduceQuery(this.topic.getQuery()
						.replaceAll("\\.", " "));
			else
				throw new RuntimeException(
						"The sourcefield option does not exist: " + sourcefield);
		} else {
			field += "_metamap_" + metamap;
			if (sourcefield.equals("title"))
				if (metamap.equals("general"))
					querystr = "patient: "
							+ QueryHelpers.getMetaMapOption(
									this.topic.titleMetaMap, metamap, true)
							+ " general:"
							+ QueryHelpers.getMetaMapOption(
									this.topic.titleMetaMap, metamap, true);
				else
					querystr = QueryHelpers.getMetaMapOption(
							this.topic.titleMetaMap, metamap, true);
			else if (sourcefield.equals("query"))
				querystr = QueryHelpers.getMetaMapOption(
						this.topic.queryMetaMap, metamap, true);
			else
				throw new RuntimeException(
						"The sourcefield option does not exist: " + sourcefield);

		}

		if (querystr == "")
			return null;

		try {
			if (metamap == null)
				querystr = QueryParser.escape(querystr);

			q = new QueryParser(field, this.analyzer).parse(querystr+" +topics:"+this.topic.getTopicID()+"^0");

		} catch (ParseException e) {
			System.out.println(querystr);
			throw new RuntimeException("Could not generate query!");
		}
		q = new BoostQuery(q, boost);

		return q;
	}
}
