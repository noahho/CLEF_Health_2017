package loaders;

import helpers.Gensim;
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
import org.apache.spark.mllib.linalg.Vector;
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

public class StaticMetaDataProcessingLoader extends Loader {
	private static org.apache.log4j.Logger log = Logger
			.getLogger(StaticMetaDataProcessingLoader.class);
	Map<String, KeyValuePair<Float, Float>> sourcesTrust = new HashMap<String, KeyValuePair<Float, Float>>();
	Map<String, KeyValuePair<Float, Float>> languagesTrust = new HashMap<String, KeyValuePair<Float, Float>>();
	Vector titleRelevanceVector, abstractRelevanceVector, meshHeadingsRelevanceVector;
	Vector titleRelevanceVector_2, abstractRelevanceVector_2, meshHeadingsRelevanceVector_2;
	
	public StaticMetaDataProcessingLoader(PubMedDatabase pmDatabase,
			TopicsDatabase tpDatabase) {
		super(pmDatabase, tpDatabase);
		try {
			String titleFileString = Utils.readFile(new File(Config.gensimOutputRelevanceTitlesFile), StandardCharsets.ISO_8859_1);
			String abstractFileString = Utils.readFile(new File(Config.gensimOutputRelevanceAbstractsFile), StandardCharsets.ISO_8859_1);
			String meshFileString = Utils.readFile(new File(Config.gensimOutputRelevanceMeshFile), StandardCharsets.ISO_8859_1);
			
			String titleFileString_1 = titleFileString.split("]")[0]+"]";
			String abstractFileString_1 = abstractFileString.split("]")[0]+"]";
			String 	meshFileString_1 = meshFileString.split("]")[0]+"]";
			String titleFileString_2 = titleFileString.split("]")[1]+"]";
			String abstractFileString_2 = abstractFileString.split("]")[1]+"]";
			String 	meshFileString_2 = meshFileString.split("]")[1]+"]";
			
			this.titleRelevanceVector = Gensim.getGensimFileVector(titleFileString_1);
			this.abstractRelevanceVector = Gensim.getGensimFileVector(abstractFileString_1);
			this.meshHeadingsRelevanceVector = Gensim.getGensimFileVector(meshFileString_1);
			this.titleRelevanceVector_2 = Gensim.getGensimFileVector(titleFileString_2);
			this.abstractRelevanceVector_2 = Gensim.getGensimFileVector(abstractFileString_2);
			this.meshHeadingsRelevanceVector_2 = Gensim.getGensimFileVector(meshFileString_2);
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}
	
	public Vector getTitleRelevanceVector()
	{
		return this.titleRelevanceVector;
	}
	public Vector getAbstractRelevanceVector()
	{
		return this.abstractRelevanceVector;
	}
	public Vector getMeshHeadingsRelevanceVector()
	{
		return this.meshHeadingsRelevanceVector;
	}
	
	public Vector getTitleRelevanceVector_2()
	{
		return this.titleRelevanceVector_2;
	}
	public Vector getAbstractRelevanceVector_2()
	{
		return this.abstractRelevanceVector_2;
	}
	public Vector getMeshHeadingsRelevanceVector_2()
	{
		return this.meshHeadingsRelevanceVector_2;
	}
	
	public LanguageResult scoreLanguage(String language)
	{
		if (this.languagesTrust.containsKey(language)) {
			KeyValuePair<Float, Float> r = this.languagesTrust.get(language);
			return new LanguageResult(r.key,r.value);
		} else
			return new LanguageResult(0,0);
	}
	
	public SourcesResult scoreSources(String sources)
	{
		double sumScore = 0;
		double sumAvg = 0;
		double c = 0;
		double maxScore = 0;
		double avgScore = 0;
		double maxAvg = 0;
		double avgAvg = 0;
		String[] sourcesList = sources.split("\\|\\|");
		for (String source : sourcesList)
		{
			KeyValuePair<Float, Float> r = this.sourcesTrust.get(source);
			if (this.sourcesTrust.containsKey(source)) {
				if (r.value > maxAvg)
					maxAvg = r.value;
				if (r.key > maxScore)
					maxScore = r.key;
				sumAvg += r.value;
				sumScore += r.key;
				c++;
			}
		}
		avgScore = sumScore / c;
		avgAvg = sumAvg / c;
		return new SourcesResult(maxScore, avgScore, sumScore, avgAvg, maxAvg);
	}
	
	public class SourcesResult {
		public SourcesResult(double scoreMax, double scoreAvg, double scoreSum,
				double avgAvg, double avgMax) {
			super();
			this.scoreMax = scoreMax;
			this.scoreAvg = scoreAvg;
			this.scoreSum = scoreSum;
			this.avgAvg = avgAvg;
			this.avgMax = avgMax;
		}
		public double scoreMax;
		public double scoreAvg;
		public double scoreSum;
		public double avgAvg;
		public double avgMax;
	}
	
	public class LanguageResult {
		public LanguageResult(double score, double avg) {
			super();
			this.score = score;
			this.avg = avg;
		}
		public double score;
		public double avg;
	}
	
	public void loadMethod() {
		File relevantSourceFile = new File(Config.relevantSource);
		File relevantLanguageFile = new File(Config.relevantLanguage);
		String relevantSource="";
		String relevantLanguage = "";
		try {
			relevantSource = Utils.readFile(relevantSourceFile, StandardCharsets.ISO_8859_1);
			relevantLanguage = Utils.readFile(relevantLanguageFile, StandardCharsets.ISO_8859_1);
		} catch (IOException e) {
			throw new RuntimeException("Could not load metadata");
		}
		
		String[] relevantLanguageLines = relevantLanguage.split("\n");
		String[] relevantSourceLines = relevantSource.split("\n");
		for (String line : relevantLanguageLines)
		{
			String[] s = line.split(":");
			this.languagesTrust.put(s[0], new KeyValuePair<Float, Float>(Float.parseFloat(s[2]), Float.parseFloat(s[1])));
		}
		for (String line : relevantSourceLines)
		{
			String[] s = line.split(":");
			this.sourcesTrust.put(s[0], new KeyValuePair<Float, Float>(Float.parseFloat(s[2]), Float.parseFloat(s[1])));
		}
	}
}
