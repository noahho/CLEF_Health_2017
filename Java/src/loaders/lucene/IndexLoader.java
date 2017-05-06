package loaders.lucene;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Map;

import loaders.ContentLoader;
import loaders.Loader;
import model.database.PubMedDatabase;
import model.database.TopicsDatabase;
import model.wrappers.PubMedFileIndexingWrapper;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import common.Config;

public class IndexLoader extends Loader {
	PubMedDatabase pmDatabase;
	protected IndexWriter index;
	protected Analyzer analyzer;
	protected Directory indexDirectory;

	private static org.apache.log4j.Logger log = Logger
			.getLogger(ContentLoader.class);

	public IndexLoader(PubMedDatabase pmDatabase, TopicsDatabase tpDatabase,
			Analyzer analyzer) {
		super(pmDatabase, tpDatabase);

		// this directory will contain the indexes
		this.pmDatabase = pmDatabase;

		this.analyzer = analyzer;

		// create the indexer
		IndexWriterConfig config = new IndexWriterConfig(this.analyzer);
		config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

		try {
			index = new IndexWriter(FSDirectory.open(Paths
					.get(Config.articlesIndexDir)), config);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("The index writer could not be opened!");
		}
	}

	public void close() {
		try {
			index.close();
		} catch (IOException e) {
			log.log(Priority.WARN, "The index writer could not be closed");
		}
	}

	public Directory getIndexDirectory() {
		return this.indexDirectory;
	}


	 
	private void indexFile(PubMedFileIndexingWrapper file) {
		try {
			Document document = file.getIndexingDocument();
			index.addDocument(document);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(
					"A file had the wrong format or was not found (PID: "
							+ file.getPid() + ")");
		}

	}

	public void clearWorkingDirectory() throws IOException {
		// get all files in the data directory
		String[] files = this.indexDirectory.listAll();
		if (files != null) {
			for (String file : files) {
				if (!file.endsWith(".lock"))
					this.indexDirectory.deleteFile(file);
			}
		}
	}
	
	public void loadMethod() {
			this.pmWorkingSet.stream()
					.map(s -> new PubMedFileIndexingWrapper(s))
					.forEach(this::indexFile);
			System.out.println("Created index for the articles");
	}
}