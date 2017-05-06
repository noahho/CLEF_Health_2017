package model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;

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

import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import common.Config;
import common.Utils;

public abstract class PubMedContent {
	protected Element xml;
	protected String abstractText;
	public int PubYear;
	
	public PubMedContent(Element xml) {
	}

	public Element getXml() {
		return xml;
	}

	public void setXml(Element xml) {
		this.xml = xml;
	}
	
	public String getAbstractText() {
		return abstractText;
	}

	public void setAbstractText(String abstractText) {
		this.abstractText = abstractText;
	}
	
	public boolean loadXml(Element xml) throws ParseException
	{
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		this.xml = xml;
		try {
		
            Element bookDocument = (Element) this.xml.getElementsByTagName("BookDocument").item(0);
            Element book = (Element)bookDocument.getElementsByTagName("Book").item(0);
            
            Element pubDate = (Element)book.getElementsByTagName("PubDate").item(0);
            if (pubDate != null) // pubDate is optional for Medline articles
            	this.PubYear = Integer.parseInt(pubDate.getElementsByTagName("Year").item(0).getTextContent());
            Element authorList = getAuthorList(book);
            Element pubType = (Element)book.getElementsByTagName("PublicationType").item(0); 
            
            Element articleAbstract = (Element)book.getElementsByTagName("ArticleTitle").item(0);
            if (articleAbstract != null) // Abstract is optional for Medline articles
    			this.abstractText = articleAbstract.getTextContent();
    		else
    			this.abstractText = "";
            
            XPathExpression expr = xpath.compile("/ItemList[@ListType='Synonyms']/Item");
            NodeList synonymeList = (NodeList) expr.evaluate(book, XPathConstants.NODESET);
		} catch (Exception e) {
			throw new ParseException("Error Parsing document"+e, 0);
		}
		return true;
	}
	
	public Element getAuthorList(Element book)
	{
		NodeList authorListList = book.getElementsByTagName("AuthorList");
        if (authorListList.getLength() > 1)
        	for (int i = 0; i < authorListList.getLength(); i++) {                
                Node node = authorListList.item(i);

                if (node.hasAttributes()) {
                	Attr attr = (Attr) node.getAttributes().getNamedItem("type");
                	if (attr.equals("authors"))
                		return (Element)node;
                }
        	}
        return (Element)authorListList.item(0);
	}
	
	public void clear()
	{
		this.xml = null;
	}
}
