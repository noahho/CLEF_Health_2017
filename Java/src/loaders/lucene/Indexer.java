package loaders.lucene;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;

public abstract class Indexer {
	protected IndexWriter index;
	protected Analyzer analyzer;
	protected Directory indexDirectory;
	
	public abstract void createIndex() throws Exception;
	
	public Directory getIndexDirectory()
	{
		return this.indexDirectory;
	}
	
	public Indexer()
	{
		
	}
	
	
}
