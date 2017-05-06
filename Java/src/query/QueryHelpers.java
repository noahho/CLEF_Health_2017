package query;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import metamap.MetaMapEntry;

public class QueryHelpers {
	public static String reduceQuery(String q) {
		q = q.replaceAll(
				"\"|”|\\(|\\)|“|\u2018|\u2019|\u201a|\u201b|\u275b|\u275c|\u201c|\u201d|\u201e|\u201f|\u275d|\u275e|/",
				".");
		q = Normalizer.normalize(q, Normalizer.Form.NFD).replaceAll(
				"[^\\p{ASCII}]", "");
		q = q.toLowerCase();

		q = q.replaceAll("not\\s?\\([^\\)]*\\)", "");
		q = q.replaceAll("[^a-zA-Z]not\\s?[a-z]*", "");
		q = q.replaceAll("([a-zA-Z]+)[ ]?([\\$,\\*]){1}", "$1*");
		q = q.replaceAll("(\\[[a-zA-Z0-9]+\\])", ".");
		q = q.replaceAll("\\.([a-z][,]?)+\\.", ".");
		q = q.replaceAll(
				"[\\. ]+(adj5|adj4|adj3|ot|[a-z]{3}|[0-9]{1,2}|not|and|or|exp|adj|adj2|#[0-9]+|[0-9]+[-,\",\"][0.9]+)[\\. ]+",
				".");
		q = q.replaceAll(
				"[\\. ]+(adj5|adj4|adj3|ot|[a-z]{3}|[0-9]{1,2}|not|and|or|exp|adj|adj|twis|#[0-9]+|[0-9]+[-,\",\"][0.9]+)[\\. ]+",
				".");
		q = q.replaceAll("\\.+", ".");
		q = q.replaceAll("(\\.\\s+)+", ".");
		q = q.replaceAll("\\s+", " ");
		// q = String.join(" ", q.split("[\\s+, \".\",\",\",\"|\"]"));

		return q;
	}

	
	public static String[] stopwords = {"a", "as", "able", "about", "above", "according", "accordingly", "across", "actually", "after", "afterwards", "again", "against", "aint", "all", "allow", "allows", "almost", "alone", "along", "already", "also", "although", "always", "am", "among", "amongst", "an", "and", "another", "any", "anybody", "anyhow", "anyone", "anything", "anyway", "anyways", "anywhere", "apart", "appear", "appreciate", "appropriate", "are", "arent", "around", "as", "aside", "ask", "asking", "associated", "at", "available", "away", "awfully", "be", "became", "because", "become", "becomes", "becoming", "been", "before", "beforehand", "behind", "being", "believe", "below", "beside", "besides", "best", "better", "between", "beyond", "both", "brief", "but", "by", "cmon", "cs", "came", "can", "cant", "cannot", "cant", "cause", "causes", "certain", "certainly", "changes", "clearly", "co", "com", "come", "comes", "concerning", "consequently", "consider", "considering", "contain", "containing", "contains", "corresponding", "could", "couldnt", "course", "currently", "definitely", "described", "despite", "did", "didnt", "different", "do", "does", "doesnt", "doing", "dont", "done", "down", "downwards", "during", "each", "edu", "eg", "eight", "either", "else", "elsewhere", "enough", "entirely", "especially", "et", "etc", "even", "ever", "every", "everybody", "everyone", "everything", "everywhere", "ex", "exactly", "example", "except", "far", "few", "ff", "fifth", "first", "five", "followed", "following", "follows", "for", "former", "formerly", "forth", "four", "from", "further", "furthermore", "get", "gets", "getting", "given", "gives", "go", "goes", "going", "gone", "got", "gotten", "greetings", "had", "hadnt", "happens", "hardly", "has", "hasnt", "have", "havent", "having", "he", "hes", "hello", "help", "hence", "her", "here", "heres", "hereafter", "hereby", "herein", "hereupon", "hers", "herself", "hi", "him", "himself", "his", "hither", "hopefully", "how", "howbeit", "however", "i", "id", "ill", "im", "ive", "ie", "if", "ignored", "immediate", "in", "inasmuch", "inc", "indeed", "indicate", "indicated", "indicates", "inner", "insofar", "instead", "into", "inward", "is", "isnt", "it", "itd", "itll", "its", "its", "itself", "just", "keep", "keeps", "kept", "know", "knows", "known", "last", "lately", "later", "latter", "latterly", "least", "less", "lest", "let", "lets", "like", "liked", "likely", "little", "look", "looking", "looks", "ltd", "mainly", "many", "may", "maybe", "me", "mean", "meanwhile", "merely", "might", "more", "moreover", "most", "mostly", "much", "must", "my", "myself", "name", "namely", "nd", "near", "nearly", "necessary", "need", "needs", "neither", "never", "nevertheless", "new", "next", "nine", "no", "nobody", "non", "none", "noone", "nor", "normally", "not", "nothing", "novel", "now", "nowhere", "obviously", "of", "off", "often", "oh", "ok", "okay", "old", "on", "once", "one", "ones", "only", "onto", "or", "other", "others", "otherwise", "ought", "our", "ours", "ourselves", "out", "outside", "over", "overall", "own", "particular", "particularly", "per", "perhaps", "placed", "please", "plus", "possible", "presumably", "probably", "provides", "que", "quite", "qv", "rather", "rd", "re", "really", "reasonably", "regarding", "regardless", "regards", "relatively", "respectively", "right", "said", "same", "saw", "say", "saying", "says", "second", "secondly", "see", "seeing", "seem", "seemed", "seeming", "seems", "seen", "self", "selves", "sensible", "sent", "serious", "seriously", "seven", "several", "shall", "she", "should", "shouldnt", "since", "six", "so", "some", "somebody", "somehow", "someone", "something", "sometime", "sometimes", "somewhat", "somewhere", "soon", "sorry", "specified", "specify", "specifying", "still", "sub", "such", "sup", "sure", "ts", "take", "taken", "tell", "tends", "th", "than", "thank", "thanks", "thanx", "that", "thats", "thats", "the", "their", "theirs", "them", "themselves", "then", "thence", "there", "theres", "thereafter", "thereby", "therefore", "therein", "theres", "thereupon", "these", "they", "theyd", "theyll", "theyre", "theyve", "think", "third", "this", "thorough", "thoroughly", "those", "though", "three", "through", "throughout", "thru", "thus", "to", "together", "too", "took", "toward", "towards", "tried", "tries", "truly", "try", "trying", "twice", "two", "un", "under", "unfortunately", "unless", "unlikely", "until", "unto", "up", "upon", "us", "use", "used", "useful", "uses", "using", "usually", "value", "various", "very", "via", "viz", "vs", "want", "wants", "was", "wasnt", "way", "we", "wed", "well", "were", "weve", "welcome", "well", "went", "were", "werent", "what", "whats", "whatever", "when", "whence", "whenever", "where", "wheres", "whereafter", "whereas", "whereby", "wherein", "whereupon", "wherever", "whether", "which", "while", "whither", "who", "whos", "whoever", "whole", "whom", "whose", "why", "will", "willing", "wish", "with", "within", "without", "wont", "wonder", "would", "would", "wouldnt", "yes", "yet", "you", "youd", "youll", "youre", "youve", "your", "yours", "yourself", "yourselves", "zero"};
	public static Set stopSet = new HashSet<String>(Arrays.asList(stopwords));
	
	public static String removeStopWords(String string) {
		String[] words = string.split("[\\s\\.\\,]");
		String result = "";
		for (String word : words)
		{
			if (stopSet.contains(word))
				continue;
			result += " "+word;
		}
		return result;
	}
	
	public static String weightTerms(String text, double weight) {
		return text.replaceAll("[\\w\\-\\*]+", "($0)^"+weight);
	}
	
	public static String removeSuffix(String text, int minLength, double weight) {
		text = " "+text+" ";
		String suffixes = "a|ability|able|ably|ac|acean|aceous|ad|ade|aemia|age|agog|agogue|aholic|al|algia|all|an|ana|ance|ancy|androus|andry|ane|ant|ar|arch|archy|ard|arian|arium|art|ary|ase|ate|athon|ation|ative|ator|atory|bot|cade|caine|carp|carpic|carpous|cele|cene|centric|cephalic|cephalous|cephaly|chore|chory|chrome|cide|clast|clinal|cline|clinic|coccus|coel|coele|colous|cracy|crat|cratic|cratical|cy|cyte|dale|derm|derma|dermatous|dom|drome|dromous|ean|eaux|ectomy|ed|ee|eer|ein|eme|emia|en|ence|enchyma|ency|ene|ent|eous|er|ergic|ergy|es|escence|escent|ese|esque|ess|est|et|eth|etic|ette|ey|facient|faction|fer|ferous|fic|fication|fid|florous|fold|foliate|foliolate|form|fuge|ful|fy|gamous|gamy|gate|gen|gene|genesis|genetic|genic|genous|geny|gnathous|gon|gony|gram|graph|grapher|graphy|gyne|gynous|gyny|hood|ia|ial|ian|iana|iasis|iatric|iatrics|iatry|ibility|ible|ic|icide|ician|ick|ics|id|ide|ie|ify|ile|in|ine|ing|ion|ious|isation|ise|ish|ism|ist|istic|istical|istically|ite|itious|itis|ity|ium|ive|ix|ization|ize|kin|kinesis|kins|land|latry|le|lepry|less|let|like|ling|lite|lith|lithic|log|logue|logic|logical|logist|logy|ly|lyse|lysis|lyte|lytic|lyze|mancy|mania|meister|ment|mer|mere|merous|meter|metric|metrics|metry|mire|mo|morph|morphic|morphism|morphous|most|mycete|mycin|n't|nasty|ness|nik|nomy|nomics|o|ode|odon|odont|odontia|oholic|oic|oid|ol|ole|oma|ome|omics|on|one|ont|onym|onymy|opia|opsis|opsy|or|orama|ory|ose|osis|otic|otomy|ous|para|parous|path|pathy|ped|pede|penia|petal|phage|phagia|phagous|phagy|phane|phasia|phil|phile|philia|philiac|philic|philous|phobe|phobia|phobic|phone|phony|phore|phoresis|phorous|phrenia|phyll|phyllous|plasia|plasm|plast|plastic|plasty|plegia|plex|ploid|pod|pode|podous|poieses|poietic|pter|punk|rrhagia|rrhea|ric|ry|s|scape|scope|scopy|scribe|script|sect|sepalous|ship|some|speak|sporous|st|stasis|stat|ster|stome|stomy|taxis|taxy|tend|th|therm|thermal|thermic|thermy|thon|thymia|tion|tome|tomy|tonia|trichous|trix|tron|trophic|trophy|tropic|tropism|tropous|tropy|tude|ture|ty|us|ular|ule|ure|urgy|uria|uronic|urous|valent|virile|vorous|ward|wards|ware|ways|wear|wide|wise|worthy|xor|y|yl|yne|zilla|zoic|zoon|zygous|zyme";
		text = text.replaceAll("([\\w]{"+minLength+",})("+suffixes+")[\\.\\s,]", "$1* ");
		return text;
	}
	
	public static String makeFuzzy(String text) {
		text = text.replaceAll("(\\w)[\\.\\s,]", "$1~ ");
		return text;
	}
	
	public static String parseQuery(String text) {
		text = removeNonText(text);
		return QueryHelpers.removeSuffix(QueryHelpers.removeStopWords(text), 3, 0.3) + QueryHelpers.removeSuffix(QueryHelpers.removeStopWords(text), 5, 0.3) + QueryHelpers.removeSuffix(QueryHelpers.removeStopWords(text), 7, 0.3);
	}
	
	public static String removeNonText(String text) {
		return text.replaceAll("[^\\w\\s\\,\\*]", " ");
	}

	public static String getMetaMapOption(MetaMapEntry mme, String option,
			boolean weights) {
		if (option.equals("patient"))
			return mme.getPatientCuiField();
		else if (option.equals("target"))
			return mme.getTargetCuiField();
		else if (option.equals("test"))
			return mme.getTestCuiField();
		else if (option.equals("general"))
			return mme.getGeneralCuiField();
		else
			throw new RuntimeException("The chosen option does not exist :"
					+ option);
	}
}
