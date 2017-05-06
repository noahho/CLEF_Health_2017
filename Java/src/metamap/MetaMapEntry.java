package metamap;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

import common.Config;
import common.Utils;

public class MetaMapEntry {
	String path;
	Map<String, List<String>> cuiList;
	boolean weights;
	
	public MetaMapEntry(String path) {
		this(path, false);
	}

	public MetaMapEntry(String path, boolean weights) {
		this.path = path;
		this.weights = weights;
		cuiList = new HashMap<String, List<String>>();
		parseMetaMap();
	}

	public void parseMetaMap() {
		String input;
		try {
			input = Utils.readFile(new File(path), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException("Could not open the metamap file!");
		}

		String[] inputSplit = input.split(">>>>> Mappings");
		for (String split : inputSplit) {
			Pattern MY_PATTERN = Pattern
					.compile("\\s+([0-9]+)\\s+C([0-9]{7}).*\\[(([a-z]*[,]?)+)\\]");
			Matcher m = MY_PATTERN.matcher(split);
			int num_mappings = StringUtils.countMatches(split, "Meta Mapping (");
		
			while (m.find()) {
				int rank = Integer.parseInt(m.group(1));
				String cui = m.group(2);
				String[] types = m.group(3).split(",");
				if (rank > 500) {
					String weightsStr = "^"+1/(float)num_mappings;
					if (!weights)
						weightsStr = "";
					if (Config.isRelatedType(types, "patient"))
						this.addCui("patient", cui+weightsStr);
					if (Config.isRelatedType(types, "test"))
						this.addCui("test", cui+weightsStr);
					if (Config.isRelatedType(types, "target"))
						this.addCui("target", cui+weightsStr);
					if (Config.isRelatedType(types, "general"))
						this.addCui("general", cui+weightsStr);
				}
			}
		}
	}

	public void addCui(String type, String cui) {
		if (!this.cuiList.containsKey(type))
			this.cuiList.put(type, new ArrayList<String>());
		this.cuiList.get(type).add(cui);
	}

	public String getPatientCuiField() {
		if (this.cuiList.get("patient") == null)
			return "";
		return "C"+this.cuiList.get("patient").stream()
				.collect(Collectors.joining(" C"));
	}

	public String getTestCuiField() {
		if (this.cuiList.get("test") == null)
			return "";
		return "C"+this.cuiList.get("test").stream()
				.collect(Collectors.joining(" C"));
	}

	public String getTargetCuiField() {
		if (this.cuiList.get("target") == null)
			return "";
		return "C"+this.cuiList.get("target").stream()
				.collect(Collectors.joining(" C"));
	}

	public String getGeneralCuiField() {
		if (this.cuiList.get("general") == null)
			return "";
		return "C"+this.cuiList.get("general").stream()
				.collect(Collectors.joining(" C"));
	}
}
