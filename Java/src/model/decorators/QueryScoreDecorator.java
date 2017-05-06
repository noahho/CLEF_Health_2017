package model.decorators;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class QueryScoreDecorator {
	private Map<String, QueryScoreEntry> queryScoreList;
	
	public QueryScoreDecorator()
	{
		this.queryScoreList = new HashMap<String, QueryScoreEntry>();
	}
	
	public void addScore(String topicId, String type, float score)
	{
		if (this.queryScoreList.containsKey(topicId))
			this.queryScoreList.get(topicId).scores.put(type, score);
		else
		{
			HashMap<String, Float> e = new HashMap<String, Float>();
			e.put(type, score);
			this.queryScoreList.put(topicId, new QueryScoreEntry(e));
		}
	}
	
	public QueryScoreEntry getScore(String topicId)
	{
		if (this.queryScoreList.containsKey(topicId))
			return this.queryScoreList.get(topicId);
		else
			return new QueryScoreEntry(new HashMap<String, Float>());
	}
	
	public static class QueryScoreEntry {
		public Map<String, Float> scores;
		
		public QueryScoreEntry(Map<String, Float> scores)
		{
			this.scores = scores;
		}
		
		public float get (String type)
		{
			if (this.scores.containsKey(type))
				return this.scores.get(type);
			else
				return 0;
		}
	}
}
