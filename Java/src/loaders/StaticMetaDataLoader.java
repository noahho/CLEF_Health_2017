package loaders;

import helpers.KeyValuePair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.Normalizer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

public class StaticMetaDataLoader extends Loader {
	private static org.apache.log4j.Logger log = Logger
			.getLogger(StaticMetaDataLoader.class);
	Map<String, KeyValuePair<Integer, Integer>> sourcesTrust = new HashMap<String, KeyValuePair<Integer, Integer>>();
	Map<String, KeyValuePair<Integer, Integer>> languagesTrust = new HashMap<String, KeyValuePair<Integer, Integer>>();
	int pos = 0;
	int all = 0;

	public StaticMetaDataLoader(PubMedDatabase pmDatabase,
			TopicsDatabase tpDatabase) {
		super(pmDatabase, tpDatabase);

	}

	public void save() {
		float bias = ((float) pos) / all;

		File relevantSourceFile = new File(Config.relevantSource);
		File relevantLanguageFile = new File(Config.relevantLanguage);
		String sourceText = "";
		for (Entry<String, KeyValuePair<Integer, Integer>> source : sourcesTrust
				.entrySet()) {
			float avg = (float) source.getValue().key
					/ (float) source.getValue().value;
			float score = avg-bias;
			score *= source.getValue().value;
			sourceText += source.getKey() + ":" + avg + ":"+score+"\n";
		}

		String laguageText = "";
		for (Entry<String, KeyValuePair<Integer, Integer>> lang : languagesTrust
				.entrySet()) {
			float avg = (float) lang.getValue().key
					/ (float) lang.getValue().value;
			float score = avg-bias;
			score *= lang.getValue().value;
			laguageText +=lang.getKey() + ":" + avg + ":"+score+"\n";
		}

		Utils.saveFile(relevantSourceFile, sourceText,
				StandardCharsets.ISO_8859_1, false);

		Utils.saveFile(relevantLanguageFile, laguageText,
				StandardCharsets.ISO_8859_1, false);
	}

	public void addMeta(boolean relevant, String d,
			Map<String, KeyValuePair<Integer, Integer>> metadata, boolean count) {
		if (!metadata.containsKey(d))
			metadata.put(d, new KeyValuePair<Integer, Integer>(0, 0));
		if (relevant) {
			metadata.get(d).key++;
			if (count)
				pos++;
		}
		metadata.get(d).value++;
		if (count)
			all++;
	}

	public void loadMethod() {
		for (PubMedFile file : pmWorkingSet.getValues()) {
			for (String topic : file.retrievedFileTopicList) {
					boolean relevant = file.relevantTopicList.contains(topic);
					addMeta(relevant, file.content.language,
							this.languagesTrust, true);
					for (String source : file.content.sources) {
						addMeta(relevant, source, this.sourcesTrust, false);
					}
			}

		}
		save();
	}
}
