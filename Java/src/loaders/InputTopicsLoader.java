package loaders;

import helpers.Gensim;
import helpers.KeyValuePair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;

import query.QueryHelpers;
import metamap.MetaMapEntry;
import model.PubMedFile;
import model.database.PubMedDatabase;
import model.database.TopicsDatabase;
import model.input.TopicInputFile;
import common.Config;
import common.Utils;

public class InputTopicsLoader extends Loader {

	public InputTopicsLoader(PubMedDatabase pmDatabase,
			TopicsDatabase tpDatabase) {
		super(pmDatabase, tpDatabase);
	}

	public void loadMethod() {
		// Read All the input and parse it
		File folder = new File(Config.originalTopicsDir);
		for (File fileEntry : folder.listFiles()) {
			if (!fileEntry.isDirectory()) {
				KeyValuePair<String, TopicInputFile> entry = readFile(fileEntry);
				if (entry != null)
					this.tpDatabase.add(entry);
			}
		}
	}

	// TODO: There might be other information in the input files, eg there was
	// review_id: xx in file 38
	private KeyValuePair<String, TopicInputFile> readFile(File file) {
		TopicInputFile myDoc = new TopicInputFile();
		int state = 0;
		try (BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(file), "UTF-8"))) {
			for (String line; (line = br.readLine()) != null;) {
				if (line == "")
					continue;
				String pattern = "";
				switch (state) {
				case 0:
					pattern = "Topic: ([a-zA-Z0-9]{8})";
					break;
				case 1:
					pattern = "Title: (.+)";
					break;
				case 2:
					pattern = "Query: (.*)";
					break;
				case 3:
					pattern = "Pids: (.*)";
					break;
				default:
					pattern = " *([0-9]+)";
					break;
				}

				Pattern r = Pattern.compile(pattern);
				Matcher m = r.matcher(line);
				if (pattern != "" && m.find()) {
					String value = m.group(1);
					switch (state) {
					case 0:
						myDoc.setTopicID(value);
						break;
					case 1:
						myDoc.setTitle(value);
						break;
					case 2:
						myDoc.setQuery(value);
						break;
					case 3:
						break;
					default:
						int pid = Integer.parseInt(value);
						this.pmDatabase.addIfNotExists(new PubMedFile(pid));
						this.pmDatabase.access(pid).retrievedFileTopicList
								.add(myDoc.getTopicID());
						break;
					}
					state++;
				} else {
					if (state == 3)
						myDoc.setQuery(myDoc.getQuery() + line);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
		if (state <= 3)
			return null;

		// Read the metamap Data
		String path = Config.metaMapOutputTopicsDir + "//"
				+ myDoc.getTopicID() + "_title.txt";
		MetaMapEntry mmEntryTitle = new MetaMapEntry(path, true);
		myDoc.titleMetaMap = mmEntryTitle;

		path = Config.metaMapOutputTopicsDir + "//" + myDoc.getTopicID()
				+ "_query.txt";
		MetaMapEntry mmEntryQuery = new MetaMapEntry(path, true);
		myDoc.queryMetaMap = mmEntryQuery;

		// Read the gensimData
		String s;
		try {
			path = Config.gensimOutputTopicsQueriesDir + "//" + myDoc.getTopicID();
			s = Utils.readFile(new File(path), StandardCharsets.ISO_8859_1);
			myDoc.gensimQueryVector = Gensim.getGensimFileVector(s);
			
			path = Config.gensimOutputTopicsTitlesDir + "//" + myDoc.getTopicID();
			s = Utils.readFile(new File(path), StandardCharsets.ISO_8859_1);
			myDoc.gensimTitleVector = Gensim.getGensimFileVector(s);
		} catch (IOException e) {
			throw new RuntimeException("The Gensim vectors for all topics should exist!");
		}
		return new KeyValuePair<String, TopicInputFile>(myDoc.getTopicID(),
				myDoc);
	}
}
