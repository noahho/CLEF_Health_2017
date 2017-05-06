package model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import model.wrappers.PubMedFileIndexingWrapper;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import common.Config;
import common.Utils;

public class PubMedMedlineContent extends PubMedContent {
	
	private final static Logger LOGGER = Logger.getLogger(PubMedFileIndexingWrapper.class.getName());
	
	public PubMedMedlineContent(Element xml)
	{
		super(xml);
	}
	
	public boolean loadXml(Element xml) throws ParseException 
	{
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		this.xml = xml;
		try {
    		Element medline = (Element) this.xml.getElementsByTagName("MedlineCitation").item(0);
    		Element article = (Element)medline.getElementsByTagName("Article").item(0);
    		
    		Element articleAbstract = (Element)article.getElementsByTagName("Abstract").item(0);
    		if (articleAbstract != null) // Abstract is optional for Medline articles
    			this.abstractText = articleAbstract.getTextContent();
    		else
    			this.abstractText = "";
    		
    		 Element pubDate = (Element)article.getElementsByTagName("DateCreated").item(0);
             if (pubDate != null) // pubDate is optional for Medline articles
             	this.PubYear = Integer.parseInt(pubDate.getElementsByTagName("Year").item(0).getTextContent());
    		
    		Element articleJournal = (Element)article.getElementsByTagName("Journal").item(0);
    		Element articleTitle = (Element)article.getElementsByTagName("ArticleTitle").item(0);
    		Element articleAuthorList = (Element)article.getElementsByTagName("AuthorList").item(0);
    		Element articlePublicationTypeList = (Element)article.getElementsByTagName("PublicationTypeList").item(0);
    		Element articleMeshHeadingList = (Element)article.getElementsByTagName("MeshHeadingList").item(0);
		} catch (Exception e) {
			throw new ParseException("Error Parsing document"+e, 0);
		}
		return true;
	}
}
