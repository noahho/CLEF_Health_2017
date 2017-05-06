/*package loaders;

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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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

public class StaticMetaDataLoaderLeaveOneOut extends Loader {
	private static org.apache.log4j.Logger log = Logger
			.getLogger(StaticMetaDataLoaderLeaveOneOut.class);
	
	Set<StaticMetaDataLoader> loaders = new HashSet<StaticMetaDataLoader>();
	
	public StaticMetaDataLoaderLeaveOneOut(PubMedDatabase pmDatabase,
			TopicsDatabase tpDatabase) {
		super(pmDatabase, tpDatabase);
		for (String topic : tpDatabase.getKeys()) {
			loaders.add(new StaticMetaDataLoader(pmDatabase, tpDatabase, topic));
		}
	}

	public void loadMethod()
	{
		for (StaticMetaDataLoader loader : loaders) {
			loader.load();
		}
	}
	
	public void save() {
		for (StaticMetaDataLoader loader : loaders) {
			loader.save();
		}
	}
}
*/