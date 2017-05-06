package common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {
	public static final String dataDir = "../data";

	public static final String trecEvalExe = "resources\\trec_eval-win-x86.exe";
	public static final String rankLibJar = "resources\\RankLib-2.1-patched.jar";
	
	// INDEXING
	public static final String articlesIndexDir = dataDir + "\\indexed";

	public static final String originalDir = dataDir + "\\original";
	public static final String originalArticleXmlDir = originalDir + "\\pubmed";
	public static final String originalTopicsDir = originalDir+"\\topics";
	public static final String originalTopicsByAbstractsQrelFile = originalDir
			+ "\\qrel_abs_train";
	public static final String originalTopicsByContentQrelFile = originalDir
			+ "\\qrel_content_train";

	public static final String learningDir = dataDir + "\\learning";
	public static final String learningTestFeatures = "test_feats";
	public static final String learningTrainFeatures = "train_feats";
	public static final String learningFeaturesDir = learningDir + "\\features";
	public static final String learningFeaturesFile = learningDir + "\\features\\features_no_metamap.txt";
	public static final String learningFeaturesFileGensim = learningDir + "\\features\\features_gensim.txt";
	public static final String learningFeaturesFileLucene = learningDir + "\\features\\features_lucene.txt";
	public static final String learningFeaturesFileMetadata = learningDir + "\\features\\features_metadata.txt";
	public static final String learningFeaturesFileMetamap = learningDir + "\\features\\features_metamap.txt";
	public static final String learningScoresDir = learningDir + "\\scores";
	public static final String learningModelFile = learningDir + "\\models\\model";

	public static final String trec = dataDir + "\\trec";

	public static final String preprocessingDir = dataDir + "\\preprocessing";
	
	public static final String preprocessingTopicsByAbstractsQrelFile = preprocessingDir
			+ "\\qrel_abs_train";

	public static final String metaDataDir = learningDir + "\\metadata";
	public static final String relevantSource = metaDataDir
			+ "\\relevant_source";
	public static final String relevantLanguage = metaDataDir
			+ "\\relevant_language";

	public static final String gensimDir = learningDir + "\\gensim";
	public static final String gensimInputArticlesTitlesFile = gensimDir
			+ "\\gensimInput\\articles_titles.txt";
	public static final String gensimInputArticlesAbstractsFile = gensimDir
			+ "\\gensimInput\\articles_abstracts.txt";
	public static final String gensimInputArticlesMeshFile = gensimDir
			+ "\\gensimInput\\articles_mesh.txt";
	public static final String gensimInputTopicsTitlesFile = gensimDir
			+ "\\gensimInput\\topics_titles.txt";
	public static final String gensimInputTopicsQueriesFile = gensimDir
			+ "\\gensimInput\\topics_queries.txt";
	public static final String gensimOutputArticlesAbstractsDir = gensimDir
			+ "\\gensimOutput\\articles_abstracts_vectors";
	public static final String gensimOutputArticlesTitlesDir = gensimDir
			+ "\\gensimOutput\\articles_titles_vectors";
	public static final String gensimOutputArticlesMeshDir = gensimDir
			+ "\\gensimOutput\\articles_mesh_vectors";
	public static final String gensimOutputRelevanceAbstractsFile = gensimDir
			+ "\\gensimOutput\\articles_abstracts_relevance_vector.txt";
	public static final String gensimOutputRelevanceTitlesFile = gensimDir
			+ "\\gensimOutput\\articles_titles_relevance_vector.txt";
	public static final String gensimOutputRelevanceMeshFile = gensimDir
			+ "\\gensimOutput\\articles_mesh_relevance_vector.txt";
	public static final String gensimOutputTopicsQueriesDir = gensimDir
			+ "\\gensimOutput\\topics_queries";
	public static final String gensimOutputTopicsTitlesDir = gensimDir
			+ "\\gensimOutput\\topics_titles";

	public static final String metaMapDir = preprocessingDir+"\\metamap";
	public static final String metaMapInputTopicsSeparateDir = metaMapDir
			+ "\\metamapInput\\topicsSeparate";
	public static final String metaMapInputArticleAbstractsFile = metaMapDir
			+ "\\metamapInput\\abstracts.txt";
	public static final String metaMapInputArticleTitlesFile = metaMapDir
			+ "\\metamapInput\\titles.txt";
	public static final String metaMapOutputArticlesFile = metaMapDir
			+ "\\metamapOutput\\abstracts.txt";
	public static final String metaMapOutputTitlesFile = metaMapDir
			+ "\\metamapOutput\\titles.txt";
	public static final String metaMapOutputArticlesDir = metaMapDir
			+ "\\metamapOutput\\abstracts";
	public static final String metaMapOutputTopicsDir = metaMapDir
			+ "\\metamapOutput\\topics";
	public static final String metaMapOutputTitlesDir = metaMapDir
			+ "\\metamapOutput\\titles";

	public static int database_workset_max_size = 1500;

	public static List<String> staticSet = Arrays.asList("CD009593", "CD007427",
			"CD007394", "CD008686");
	
	public static List<String> testSet = Arrays.asList("CD008643", "CD010632",
			"CD010771", "CD009323", "CD008691", "CD009944", "CD011548"); // , "CD010438", "CD010771", "CD011134", "CD009323"

	public static boolean RECREATE_TREC = true;
	
	public static boolean RECREATE_GENSIM_INPUT = false;
	
	public static boolean RECREATE_METADATA = false;

	public static boolean RECREATE_PRED = true;

	public static boolean RECREATE_INDEX = false; // Should the index be
	
												// recreated?
	public static boolean RECREATE_MODEL = false; // Should the model be trained
												// with the feature vector
												// again?
	public static boolean RECREATE_FEATS = false; // Should the feature vectors
													// be saved in a separate
													// file again?
	public static boolean RECREATE_METAMAP_TOPIC_INPUT = false; // Should the topic queries
													// and titles be saved in
													// separate files for
													// parsing with metamap?
	public static boolean RECREATE_METAMAP_ARTICLE_INPUT = false; // Should the abstracts
														// and titles be saved
														// in one file for
														// parsing with metamap?
	public static boolean RECREATE_METAMAP_OUTPUT = false; // Should the output of
														// metamap which is one
														// big file be split up
														// into one file per
														// article?

	public static boolean DOWNLOADPUBMED = false;

	public static String RANKLIB = "RankLib";

	public static boolean isRelatedType(String[] type, String cat) {
		boolean r = false;
		for (String s : type) {
			if (cat == "patient")
				r = r || isPatientRelatedType(s);
			else if (cat == "test")
				r = r || isTestRelatedType(s);
			else if (cat == "target")
				r = r || isTargetRelatedType(s);
			else if (cat == "general")
				r = r || isGeneralType(s);
			else
				throw new RuntimeException("Invalid Type");
		}
		return r;
	}

	public static boolean isPatientRelatedType(String type) {
		return false;
		/*if (type.equals("podg")
				|| // Patient or diabled group
				type.equals("popg")
				|| // Population group
				type.equals("prog")
				|| // Professional group
				type.equals("aggp")
				|| // Age group
				type.equals("famg")
				|| // Family group
				type.equals("humn")
				|| // Human
				type.equals("eehu")
				|| // Environmental effect on humans
				type.equals("hops") || type.equals("inbe")
				|| // Individual behavior
				type.equals("ocac")
				|| // Occupational activity
				type.equals("ocdi")
				|| // Occupation or discipline
				type.equals("pros")
				|| // Professional society
				type.equals("socb")
				|| // Societal behavior
				type.equals("anim") || type.equals("bird")
				|| type.equals("amph") || // Amphibian
				type.equals("acab") || // Acquired abnormality
				type.equals("dora") || // Daily or recreational activity
				type.equals("geoa") || // Geographic area
				type.equals("mamm")// Mammal
		)
			return true;
		return false;*/
	}

	public static boolean isTestRelatedType(String type) {
		if (type.equals("diap") || // Diagnostic procedure
				type.equals("aapp") || // Amino acid or protein
				type.equals("amas") || // Amino sequence
				type.equals("lbpr") || // Laboratory procedure
				type.equals("lbtr") || // Lab result
				type.equals("medd") || // Medical device
				type.equals("nnon") || // Nucleic Acid, Nucleoside, or
										// Nucleotide
				type.equals("nusq") || // Nucleotide sequence
				type.equals("resd") || // Research device
				type.equals("sosy")// Sign or symptom
		)
			return true;
		return false;
	}

	public static boolean isTargetRelatedType(String type) {
		if (type.equals("dsyn")
				|| // Disease or syndrome
				type.equals("aapp") || type.equals("amas")
				|| type.equals("cgab") || // Congenital
				// Abnormality
				type.equals("comd") || // Cell or molecular disfunction
				type.equals("inpo") || // Injury or posioning
				type.equals("mobd") || // Mental disorder
				type.equals("orgm") || // Organism
				type.equals("ortf") || // Organ or tissue function
				type.equals("virs") || // Virus
				type.equals("bact")// Bacteria
		)
			return true;
		return false;
	}

	public static boolean isGeneralType(String type) {
		return true;
	}

	public static Map<String, Integer> getSourcesTrust() {
		Map<String, Integer> sourcesTrust = new HashMap<String, Integer>();
		sourcesTrust.put("Addresses", 0);
		sourcesTrust.put("Autobiography", -2);
		sourcesTrust.put("Bibliography", 1);
		sourcesTrust.put("Bibliography", -2);
		sourcesTrust.put("Bibliography", 1);
		sourcesTrust.put("Classical Article", 1);
		sourcesTrust.put("Clinical Conference", 1);
		sourcesTrust.put("Clinical Study", 3);
		/*
		 * Clinical Trial Clinical Trial, Phase I Clinical Trial, Phase II
		 * Clinical Trial, Phase III Clinical Trial, Phase IV Collected Works
		 * Comparative Study Congresses Consensus Development Conference
		 * Consensus Development Conference, NIH Controlled Clinical Trial
		 * Dataset Dictionary Directory Duplicate Publication Editorial English
		 * Abstract Evaluation Studies Festschrift Government Publications
		 * Guideline Historical Article Interactive Tutorial Interview
		 * Introductory Journal Article Journal Article Lectures Legal Cases
		 * Legislation Letter Meta-Analysis Multicenter Study News Newspaper
		 * Article Observational Study Overall Patient Education Handout
		 * Periodical Index Personal Narratives Portraits Practice Guideline
		 * Pragmatic Clinical Trial Publication Components Publication Formats
		 * Publication Type Category Randomized Controlled Trial Research
		 * Support, American Recovery and Reinvestment Act Research Support,
		 * N.I.H., Extramural Research Support, N.I.H., Intramural Research
		 * Support, Non-U.S. Gov't Research Support, U.S. Gov't, Non-P.H.S.
		 * Research Support, U.S. Gov't, P.H.S. Review Scientific Integrity
		 * Review Study Characteristics Support of Research Technical Report
		 * Twin Study Validation Studies Video-Audio Media Webcasts
		 */
		return sourcesTrust;
	}
}