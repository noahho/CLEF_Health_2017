package loaders;

import java.io.File;
import java.io.FileOutputStream;
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

public class ContentLoader extends Loader {
	private static org.apache.log4j.Logger log = Logger
			.getLogger(ContentLoader.class);

	private DocumentBuilderFactory dBfactory;
	private DocumentBuilder dBuilder;

	public ContentLoader(PubMedDatabase pmDatabase, TopicsDatabase tpDatabase) {
		super(pmDatabase, tpDatabase);

		this.dBfactory = DocumentBuilderFactory.newInstance();
		try {
			this.dBuilder = this.dBfactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			throw new RuntimeException(
					"Document builder could not be initialized");
		}
	}

	public void loadMethod() {
		// First filter for not downloaded files
		List<PubMedFile> downloadFileList = pmWorkingSet.getValues()
				.parallelStream().filter(s -> !s.isOffline())
				.collect(Collectors.toList());

		// Download the missing files
		System.out.println(pmWorkingSet.size() - downloadFileList.size()
				+ " documents were retrieved from disk");

		this.downloadPubMedFiles(downloadFileList);


		System.out.println(downloadFileList.size()
				+ " documents were downloaded from the web");

		// Mark that the files are downloaded and load content
		// pmWorkingSet.getValues().parallelStream().forEach(s -> s.content =
		// new ContentDecorator(s));
	}

	public void saveAbstractsAndTitles() {
		File absfile = new File(Config.metaMapInputArticleAbstractsFile);
		File titfile = new File(Config.metaMapInputArticleTitlesFile);
		try (FileWriter fw = new FileWriter(absfile, true);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter outAbs = new PrintWriter(bw);
				FileWriter fw2 = new FileWriter(titfile, true);
				BufferedWriter bw2 = new BufferedWriter(fw2);
				PrintWriter outTit = new PrintWriter(bw2)) {
			for (PubMedFile pmFile : this.pmWorkingSet.getValues()) {
				String abstractText = pmFile.content.getAbstractText();
				String titleText = pmFile.content.getTitle();
				if (!(abstractText == null) && !abstractText.equals("")) {
					abstractText = Normalizer.normalize(abstractText,
							Normalizer.Form.NFD)
							.replaceAll("[^\\p{ASCII}]", "");
					abstractText = abstractText.replaceAll("\n", "");
					abstractText = abstractText.replaceAll("\\s+", " ");
					outAbs.println((pmFile.PID + "|" + abstractText));
				}
				if (!(titleText == null) && !titleText.equals("")) {
					titleText = Normalizer.normalize(titleText,
							Normalizer.Form.NFD)
							.replaceAll("[^\\p{ASCII}]", "");
					titleText = titleText.replaceAll("\\s+", " ");
					outTit.println((pmFile.PID + "|" + titleText));
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("Could not save the abstracts and titles");
			// exception handling left as an exercise for the reader
		}
	}

	private void downloadPubMedFiles(List<PubMedFile> downloadFileList) {
		int downloadBatchSize = Constants.pubmed_max_articles;

		for (int i = 0; i * downloadBatchSize < downloadFileList.size(); i++) {
			try {
				String listString = downloadFileList
						.subList(
								i * downloadBatchSize,
								Math.min((i + 1) * downloadBatchSize,
										downloadFileList.size())).stream()
						.map(s -> s.getPID()).map(s -> s.toString())
						.collect(Collectors.joining(","));

				Content content = Request
						.Post("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi")
						.bodyForm(
								Form.form().add("db", "pubmed")
										.add("rettype", "xml")
										.add("id", listString).build())
						.execute().returnContent();

				int articlesExpected = Math.min((i + 1) * downloadBatchSize,
						downloadFileList.size());
				parseResult(content, articlesExpected);
			} catch (IOException e) {
				System.out.println(e.toString());
			}
		}
	}

	private void parseResult(Content content, int documentsExpected) {
		int documentsSeen = 0;
		try {
			StringBuilder xmlStringBuilder = new StringBuilder();
			xmlStringBuilder.append(content);
			ByteArrayInputStream input = new ByteArrayInputStream(
					xmlStringBuilder.toString().getBytes("UTF-8"));
			Document doc = this.dBuilder.parse(input);

			doc.getDocumentElement().normalize();

			Node childNode = doc.getDocumentElement().getFirstChild();
			while (childNode.getNextSibling() != null) {
				childNode = childNode.getNextSibling();

				if (childNode.getNodeType() == Node.ELEMENT_NODE) {
					int pid = -1;
					int contentType = -1;
					Element eElement = (Element) childNode;
					if (childNode.getNodeName() == "PubmedArticle") {
						contentType = 1;
						Element medline = (Element) eElement
								.getElementsByTagName("MedlineCitation")
								.item(0);
						pid = Integer.parseInt(medline
								.getElementsByTagName("PMID").item(0)
								.getTextContent());
					} else if (childNode.getNodeName() == "PubmedBookArticle") {
						contentType = 0;
						Element bookDocument = (Element) eElement
								.getElementsByTagName("BookDocument").item(0);
						pid = Integer.parseInt(bookDocument
								.getElementsByTagName("PMID").item(0)
								.getTextContent());
					}

					if (contentType == -1)
						log.log(Priority.WARN,
								"An unrecognized Node type was received "
										+ childNode.getNodeName());
					else if (!this.pmDatabase.containsKey(pid))
						log.log(Priority.WARN,
								"A pid was received that was not in the library "
										+ pid);
					else // The file is valid
					{
						// Save the file
						Transformer transformer = TransformerFactory
								.newInstance().newTransformer();
						Result output = new StreamResult(new File(
								Config.originalArticleXmlDir + "\\" + pid + ".xml"));
						Source xml = new DOMSource(eElement);

						transformer.transform(xml, output);
					}
				}
			}

		} catch (TransformerException | SAXException | IOException e) {
			throw new RuntimeException(e.toString());
		}

		if (documentsSeen != documentsExpected)
			log.log(Priority.WARN, "Too few results were retrieved!");
	}
}
