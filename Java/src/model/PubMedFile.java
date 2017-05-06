package model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IllegalFormatException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import model.decorators.ContentDecorator;
import model.decorators.QueryScoreDecorator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import common.Config;
import common.Utils;

public class PubMedFile {
	public final int PID;
	
	public ContentDecorator content;
	public QueryScoreDecorator queryScore;
	
	public List<String> retrievedFileTopicList;
	public List<String> retrievedQrelTopicList;
	public List<String> relevantTopicList;
	
	public PubMedFile(Integer PID)
	{
		this.PID = PID;
		this.relevantTopicList = new ArrayList<String>();
		this.retrievedFileTopicList = new ArrayList<String>();
		this.retrievedQrelTopicList = new ArrayList<String>();
		this.queryScore = new QueryScoreDecorator();
		this.content = new ContentDecorator(this);
	}
	
	public int getPID() {
		return PID;
	}
	
	public boolean isOffline() {
		File file = new File(Config.originalArticleXmlDir + "\\" + this.PID + ".xml");
		return file.exists();
	}
}
