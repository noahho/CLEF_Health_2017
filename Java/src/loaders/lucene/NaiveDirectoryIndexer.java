package loaders.lucene;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import common.Utils;

public class NaiveDirectoryIndexer extends Indexer {
   private String dataDirPath;
   private FileFilter filter; 
   
   private int numberOfFiles = 0;

   public NaiveDirectoryIndexer(String indexDirectoryPath, String dataDirPath, Analyzer analyzer) throws IOException{
	  super();
	  
	   //this directory will contain the indexes
      this.indexDirectory = 
         FSDirectory.open(Paths.get(indexDirectoryPath));
      
      this.analyzer = analyzer;
      
      this.dataDirPath = dataDirPath;
      //this.filter = filter;

      //create the indexer
      IndexWriterConfig config = new IndexWriterConfig(this.analyzer);
      config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
      
      index = new IndexWriter(indexDirectory, config);
   }

   public void close() throws CorruptIndexException, IOException{
	   index.close();
   }

   private Document getDocument(File file) throws IOException{
      Document document = new Document();

      //index file contents
      String text = Utils.readFile(file, StandardCharsets.UTF_8);
      Field contentField = new TextField("body", text, Field.Store.YES);
    		 // new Field("title", new FileReader(file), this.getDocumentContentType());
      //index file name
      Field fileNameField = new Field("filename",
         file.getName(),
         this.getFileNameType());
      //index file path
      Field filePathField = new Field("filepath",
         file.getCanonicalPath(),
         this.getPathType());
      
      document.add(contentField);
      document.add(fileNameField);
      document.add(filePathField);

      return document;
   }   

   private void indexFile(File file) throws IOException{
	   if (this.numberOfFiles % 5000 == 0)
		   System.out.println("Indexing 5000 more files");
      Document document = getDocument(file);
      index.addDocument(document);
   }

   public void createIndex() throws IOException{
      //get all files in the data directory
      File[] files = new File(dataDirPath).listFiles();

      for (File file : files) {
         if(!file.isDirectory()
            && !file.isHidden()
            && file.exists()
            && file.canRead()
         ){
            indexFile(file);
            this.numberOfFiles++;
         }
      }
   }
   

   private FieldType getPathType()
	{
		 FieldType pathType = new FieldType();
		 pathType.setStored(true);
		 
	     return pathType;
	}
	
	private FieldType getFileNameType()
	{
		 FieldType fileNameType = new FieldType();
		 fileNameType.setStored(true);
		 
	     return fileNameType;
	}
	
	private FieldType getDocumentContentType()
	{
		 FieldType documentContentType = new FieldType();
		 documentContentType.setStored(true);
		 documentContentType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
		 documentContentType.setTokenized(true);
		 documentContentType.setStoreTermVectors(true);
		 
	     return documentContentType;
	}
}