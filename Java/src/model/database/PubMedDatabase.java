package model.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;


import model.PubMedFile;

public class PubMedDatabase {
	private Map<Integer, PubMedFile> pubmed_files;
	
	public PubMedDatabase()
	{
		this.pubmed_files = new HashMap<Integer, PubMedFile>();
	}
	
	public PubMedDatabase filterTopic(String topicId)
	{
		PubMedDatabase filteredDatabase = new PubMedDatabase();
		this.pubmed_files.values().stream().filter(s -> s.retrievedFileTopicList.contains(topicId)).forEach(s -> filteredDatabase.add(s));
		return filteredDatabase;
	}
	
	public PubMedDatabase filterRelevantToTopic(String topic)
	{
		PubMedDatabase filteredDatabase = new PubMedDatabase();
		this.pubmed_files.values().stream().filter(s -> s.relevantTopicList.contains(topic)).forEach(s -> filteredDatabase.add(s));
		return filteredDatabase;
	}
	
	public PubMedDatabase filterNotRetrievedFromTopic()
	{
		PubMedDatabase filteredDatabase = new PubMedDatabase();
		this.pubmed_files.values().stream().filter(s -> s.retrievedFileTopicList.size() > 0 && s.retrievedQrelTopicList.size() > 0).forEach(s -> filteredDatabase.add(s));
		return filteredDatabase;
	}
	
	public List<PubMedDatabase> splitDatabase(int maxSize)
	{
		return this.splitDatabase(maxSize, this.pubmed_files.size()/maxSize + 1);
	}
	
	public PubMedDatabase randomShrink()
	{
		PubMedDatabase filteredDatabase = new PubMedDatabase();
		Random random = new Random();
		for (PubMedFile file : this.pubmed_files.values())
			if (random.nextInt(200) <= 1)
				filteredDatabase.add(file);
		return filteredDatabase;
	}
	
	public List<PubMedDatabase> splitDatabase(int maxSize, int numChunks)
	{
		List<PubMedDatabase> splits = new ArrayList<PubMedDatabase>();
		PubMedDatabase currentChunk = new PubMedDatabase();
		
		for (PubMedFile file : this.pubmed_files.values())
		{
			if (currentChunk == null)
				currentChunk = new PubMedDatabase();
			currentChunk.add(file);
			if (currentChunk.size() == maxSize && splits.size() <= numChunks)
			{
				splits.add(currentChunk);
				currentChunk = null;
			}
		}
		splits.add(currentChunk);
		
		return splits;
	}
	
	public Stream<PubMedFile> stream()
	{
		return this.pubmed_files.values().stream();
	}

	
	public Stream<PubMedFile> parallelStream()
	{
		return this.pubmed_files.values().parallelStream();
	}
	
	public int size()
	{
		return this.pubmed_files.size();
	}
	
	public boolean containsKey(int key)
	{
		return this.pubmed_files.containsKey(key);
	}
	
	public Collection<PubMedFile> getValues()
	{
		return this.pubmed_files.values();
	}
	
	public PubMedFile access(Integer key)
	{
		return this.pubmed_files.get(key);
	}
	
	public void add(PubMedFile file)
	{
		this.pubmed_files.put(file.PID, file);
	}
	
	public void addIfNotExists(PubMedFile file)
	{
		if (!this.containsKey(file.PID))
			this.pubmed_files.put(file.PID, file);
	}
}
