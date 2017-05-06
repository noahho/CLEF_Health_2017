package model.database;

import helpers.KeyValuePair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import model.PubMedFile;
import model.input.TopicInputFile;

public class TopicsDatabase {
	Map<String, TopicInputFile> topic_files;
	
	public TopicsDatabase()
	{
		this.topic_files = new HashMap<String, TopicInputFile>();
	}
	
	public void add(KeyValuePair<String, TopicInputFile> kvPair)
	{
		this.topic_files.put(kvPair.key, kvPair.value);
	}
	
	public TopicInputFile access(String key)
	{
		return this.topic_files.get(key);
	}
	
	public Set<String> getKeys()
	{
		return this.topic_files.keySet();
	}
	
	public Collection<TopicInputFile> getValues()
	{
		return this.topic_files.values();
	}
	
	public Set<Entry<String, TopicInputFile>> entrySet()
	{
		return this.topic_files.entrySet();
	}
	
	public void add(String key, TopicInputFile file)
	{
		this.topic_files.put(key, file);
	}
	
	public void testTopic()
	{
		TopicInputFile t = this.topic_files.get("CD010771");
		this.topic_files = new HashMap<String, TopicInputFile>();
		this.topic_files.put(t.getTopicID(),t );
	}
	
	public String getRandomKey()
	{
		Random       random    = new Random();
		List<String> keys      = new ArrayList<String>(this.topic_files.keySet());
		String       randomKey = keys.get( random.nextInt(keys.size()) );
		return randomKey;
	}
}
