package loaders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.Normalizer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import loaders.ml.MlLoader;
import model.PubMedFile;
import model.database.PubMedDatabase;
import model.database.TopicsDatabase;
import model.decorators.ContentDecorator;

import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import common.Config;
import common.Constants;
import common.Utils;

public class GensimSavingLoader extends Loader {
	private static org.apache.log4j.Logger log = Logger
			.getLogger(GensimSavingLoader.class);

	private File articleFile, titleFile, meshFile;
	
	public GensimSavingLoader(PubMedDatabase pmDatabase, TopicsDatabase tpDatabase) {
		super(pmDatabase, tpDatabase);
		this.articleFile = new File(Config.gensimInputArticlesAbstractsFile);
		this.titleFile = new File(Config.gensimInputArticlesTitlesFile);
		this.meshFile = new File(Config.gensimInputArticlesMeshFile);
	}

	public void clearFile()
	{
		this.articleFile.delete();
		this.titleFile.delete();
		this.meshFile.delete();
	}
	
	
	public void loadMethod() {
		String cText = "";
		String tText = "";
		String mText = "";
		for (PubMedFile file : pmWorkingSet.getValues())
		{
			for (String topic : file.retrievedFileTopicList)
			{
				int relevant = (file.relevantTopicList.contains(topic))? 1 : 0;
				cText += relevant + ":" + topic + ":" + file.PID +":"+ file.content.getAbstractText()+"\n";
				tText += relevant + ":" + topic + ":" + file.PID +":" + file.content.getTitle()+"\n";
				mText += relevant + ":" + topic + ":" + file.PID +":" + file.content.meshHeadings+"\n";
			}
		}
		Utils.saveFile(articleFile, cText,
				StandardCharsets.ISO_8859_1, true);
		Utils.saveFile(titleFile, tText,
				StandardCharsets.ISO_8859_1, true);
		Utils.saveFile(meshFile, mText,
				StandardCharsets.ISO_8859_1, true);
	}
}
