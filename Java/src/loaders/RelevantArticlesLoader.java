/*package loaders;

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

public class RelevantArticlesLoader extends Loader {
	private static org.apache.log4j.Logger log = Logger
			.getLogger(RelevantArticlesLoader.class);

	public RelevantArticlesLoader(PubMedDatabase pmDatabase,
			TopicsDatabase tpDatabase) {
		super(pmDatabase, tpDatabase);

	}

	public void loadMethod() {
		File relevantSourceFile = new File(Config.relevantSource);
		String relevantSources = "";
		for (String topic : tpDatabase.getKeys()) {
			File relevantFile = new File(Config.topicRelevantInputDir + "\\"
					+ topic);
			File relevantMetamapFile = new File(Config.topicRelevantInputDir
					+ "\\" + topic);
			relevantFile.delete();
			relevantMetamapFile.delete();
			PubMedDatabase relevantDatabase = pmDatabase
					.filterRelevantToTopic(topic);
			String relevantText = "";
			String relevantMetamapText = "";

			for (PubMedFile file : relevantDatabase.getValues()) {
				file.content.cache();
				relevantText += file.content.getAbstractText();
				relevantMetamapText += file.content.abstractMetaMap;
				relevantSources += file.content.sources;
				file.content.uncache();
			}

			Utils.saveFile(relevantFile, relevantText, StandardCharsets.UTF_8,
					true);
			Utils.saveFile(relevantMetamapFile, relevantMetamapText, StandardCharsets.UTF_8,
					true);
		}
		Utils.saveFile(relevantSourceFile, relevantSources, StandardCharsets.UTF_8,
				false);
	}
}*/
