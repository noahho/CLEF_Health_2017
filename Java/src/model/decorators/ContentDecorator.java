package model.decorators;

import helpers.Gensim;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import metamap.MetaMapEntry;
import model.PubMedFile;

import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import common.Config;
import common.Constants;
import common.Utils;

public class ContentDecorator {
	protected Element xml;
	protected String abstractText;
	protected String articleTitle;
	public String meshHeadings = "";

	// public int sourceTrustLevel;
	public List<String> sources;
	public String language;

	public int pubYear;
	private boolean cached = false;
	PubMedFile parent;
	public MetaMapEntry titleMetaMap;
	public MetaMapEntry abstractMetaMap;

	public String gensimAbstractVector = "";
	public String gensimTitleVector = "";
	public String gensimMeshVector = "";

	public ContentDecorator(PubMedFile parent) {
		this.parent = parent;
		this.sources = new ArrayList<String>();
	}

	public void parseXml() {
		if (this.xml.getNodeName() == "PubmedArticle") {
			this.parseMedlineXml();
		} else if (this.xml.getNodeName() == "PubmedBookArticle") {
			this.parseBookXml();
		} else
			throw new RuntimeException("Unrecognized file format \n");
		this.abstractText = abstractText.replaceAll("[\n\r]", "").replaceAll("\\s+", " ");
	}

	public void parseBookXml() {
		XPath xpath = Constants.xPathfactory.newXPath();

		Element bookDocument = (Element) this.xml.getElementsByTagName(
				"BookDocument").item(0);
		Element book = (Element) bookDocument.getElementsByTagName("Book")
				.item(0);

		Element pubDate = (Element) book.getElementsByTagName("PubDate")
				.item(0);
		if (pubDate != null) // pubDate is optional for Medline articles
			this.pubYear = Integer.parseInt(pubDate
					.getElementsByTagName("Year").item(0).getTextContent());
		if (book.getElementsByTagName("PublicationType").getLength() > 0) {
			Element pubType = (Element) book.getElementsByTagName(
					"PublicationType").item(0);
		}

		Element articleAbstract = (Element) bookDocument.getElementsByTagName(
				"Abstract").item(0);
		if (articleAbstract != null) // Abstract is optional for Medline
										// articles
			this.abstractText = articleAbstract.getTextContent()
					.replaceAll("[\n\r]", "").replaceAll("\\s+", " ");
		else
			this.abstractText = "";

		Element articleTitle = (Element) book.getElementsByTagName(
				"ArticleTitle").item(0);
		if (articleTitle != null) // Abstract is optional for Medline
									// articles
			this.articleTitle = articleTitle.getTextContent();
		else
			this.articleTitle = "";

		XPathExpression expr;
		try {
			expr = xpath.compile("/ItemList[@ListType='Synonyms']/Item");
			NodeList synonymeList = (NodeList) expr.evaluate(book,
					XPathConstants.NODESET);
			for (int i = 0; i < synonymeList.getLength(); i++) {
				Node currentNode = synonymeList.item(i);
				this.meshHeadings += ", " + currentNode;
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
	}

	public void parseMedlineXml() {
		Element medline = (Element) this.xml.getElementsByTagName(
				"MedlineCitation").item(0);
		Element article = (Element) medline.getElementsByTagName("Article")
				.item(0);

		Element articleAbstract = (Element) article.getElementsByTagName(
				"Abstract").item(0);
		if (articleAbstract != null) // Abstract is optional for Medline
										// articles
			this.abstractText = articleAbstract.getTextContent();
		else
			this.abstractText = "";

		Element articleTitle = (Element) article.getElementsByTagName(
				"ArticleTitle").item(0);
		if (articleTitle != null) // Abstract is optional for Medline //
									// articles
			this.articleTitle = articleTitle.getTextContent();
		else
			this.articleTitle = "";

		Element pubDate = (Element) medline.getElementsByTagName("DateCreated")
				.item(0);
		if (pubDate != null) { // pubDate is optional for Medline articles
			Element year = (Element) pubDate.getElementsByTagName("Year").item(
					0);
			this.pubYear = Integer.parseInt(year.getTextContent());
		}

		if (article.getElementsByTagName("PublicationTypeList").getLength() > 0) {
			NodeList articlePublicationTypeList = article
					.getElementsByTagName("PublicationTypeList").item(0)
					.getChildNodes();
			for (int i = 0; i < articlePublicationTypeList.getLength(); i++) {
				Node a = (Node) articlePublicationTypeList.item(i);
				if (a.getNodeName() == "PublicationType")
					this.sources.add(articlePublicationTypeList.item(i)
							.getTextContent());

			}
		}

		Element articleLanguage = (Element) article.getElementsByTagName(
				"Language").item(0);
		this.language = articleLanguage.getTextContent();

		if (medline.getElementsByTagName("MeshHeadingList").getLength() > 0) {
			NodeList articleMeshHeadingList = medline
					.getElementsByTagName("MeshHeadingList").item(0)
					.getChildNodes();
			for (int i = 0; i < articleMeshHeadingList.getLength(); i++) {
				Node currentNode = articleMeshHeadingList.item(i);
				if (currentNode.getNodeName() == "MeshHeading") {
					NodeList subarticleMeshHeadingList = currentNode
							.getChildNodes();
					if (subarticleMeshHeadingList.getLength() > 0) {
						for (int j = 0; j < subarticleMeshHeadingList
								.getLength(); j++) {
							Node currentNode2 = subarticleMeshHeadingList
									.item(j);
							if (currentNode2.getNodeName() == "QualifierName"
									|| currentNode2.getNodeName() == "DescriptorName") {
								this.meshHeadings += ","
										+ currentNode2.getTextContent();
							}
						}
					}
				}
			}
		}
	}

	public Element getAuthorList(Element book) {
		NodeList authorListList = book.getElementsByTagName("AuthorList");
		if (authorListList.getLength() > 1)
			for (int i = 0; i < authorListList.getLength(); i++) {
				Node node = authorListList.item(i);

				if (node.hasAttributes()) {
					Attr attr = (Attr) node.getAttributes()
							.getNamedItem("type");
					if (attr.equals("authors"))
						return (Element) node;
				}
			}
		return (Element) authorListList.item(0);
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

	public String getTitle() {
		return this.articleTitle;
	}

	public void setAbstractText(String abstractText) {
		this.abstractText = abstractText;
	}

	public void uncache() {
		this.xml = null;
		this.abstractText = "";
		this.articleTitle = "";
		this.language = "";
		this.meshHeadings = "";
		this.titleMetaMap = null;
		this.abstractMetaMap = null;
		this.cached = false;
		this.gensimAbstractVector = "";
		this.gensimMeshVector = "";
		this.gensimTitleVector = "";
	}

	public boolean isOffline() {
		File file = new File(Config.originalArticleXmlDir + "\\"
				+ this.parent.PID + ".xml");
		return file.exists();
	}

	public boolean hasMetaMapAbstract() {
		File file = new File(Config.metaMapOutputArticlesDir + "\\"
				+ this.parent.PID + ".txt");
		return file.exists();
	}

	public boolean hasMetaMapTitle() {
		File file = new File(Config.metaMapOutputTitlesDir + "\\"
				+ this.parent.PID + ".txt");
		return file.exists();
	}

	public String getGensim(String dir) {
		String line;
		try {
			line = Utils.readFile(new File(
					dir + "/"
							+ this.parent.PID), StandardCharsets.ISO_8859_1);
			return line;
		} catch (IOException e) {
			return "";
		}
	}

	public void cache() {
		// Load Gensim vector
		this.gensimAbstractVector = getGensim(Config.gensimOutputArticlesAbstractsDir);
		this.gensimTitleVector = getGensim(Config.gensimOutputArticlesTitlesDir);
		this.gensimMeshVector = getGensim(Config.gensimOutputArticlesMeshDir);
		
		// TODO get the files metamap that had errors!
		if (this.hasMetaMapTitle()) {
			this.titleMetaMap = new MetaMapEntry(Config.metaMapOutputTitlesDir
					+ "\\" + this.parent.PID + ".txt");
		}
		if (this.hasMetaMapAbstract()) {
			this.abstractMetaMap = new MetaMapEntry(
					Config.metaMapOutputArticlesDir + "\\" + this.parent.PID
							+ ".txt");
		}

		if (this.isOffline()) {
			try {
				File file = new File(Config.originalArticleXmlDir + "\\"
						+ this.parent.PID + ".xml");
				String xml = Utils.readFile(file, StandardCharsets.UTF_8);

				DocumentBuilderFactory factory = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();

				StringBuilder xmlStringBuilder = new StringBuilder();
				xmlStringBuilder.append(xml);
				ByteArrayInputStream input = new ByteArrayInputStream(
						xmlStringBuilder.toString().getBytes("UTF-8"));
				Document doc = builder.parse(input);

				Element root = doc.getDocumentElement();

				doc.getDocumentElement().normalize();

				this.xml = root;

				this.parseXml();

				this.cached = true;
			} catch (ParserConfigurationException | SAXException | IOException e) {
				e.printStackTrace();
				throw new RuntimeException(
						"Document on disk is corrupted or not available: PID ("
								+ parent.PID + ") " + e.getMessage());
			}
		} else {
			throw new RuntimeException("Document was not downloaded yet, PID ("
					+ parent.PID + ")");
		}
	}
}
