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

public class MetaMapLoader extends Loader {
	private static org.apache.log4j.Logger log = Logger
			.getLogger(MetaMapLoader.class);

	public MetaMapLoader(PubMedDatabase pmDatabase, TopicsDatabase tpDatabase) {
		super(pmDatabase, tpDatabase);

	}

	public void loadMethod() {
		splitMetaMapFile(Config.metaMapOutputTitlesFile,
				Config.metaMapOutputTitlesDir);
		splitMetaMapFile(Config.metaMapOutputArticlesFile,
				Config.metaMapOutputArticlesDir);
	}

	public void splitMetaMapFile(String pathInput, String pathOutput) {
		File qrelAbs = new File(Config.originalTopicsByContentQrelFile);
		String pid = null;
		String cText = "";
		try (BufferedReader br = new BufferedReader(new FileReader(pathInput))) {
			for (String line; (line = br.readLine()) != null;) {
				if (line.substring(0, 10).equals("Processing")) {
					if (line.matches("Processing [0-9]*\\.ti\\.1:.*")) {
						String[] words = line.split(" ");
						if (pid != null) {
							Utils.saveFile(new File(pathOutput + pid+".txt"), cText,
									StandardCharsets.UTF_8);
						}
						String[] wSplit = words[1].split("\\.");
						pid = wSplit[0];
						cText = "";
					}
				}
				cText += line + System.getProperty("line.separator");
			}
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}
}
