package loaders;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import model.PubMedFile;
import model.database.PubMedDatabase;
import model.database.TopicsDatabase;
import model.input.TopicInputFile;
import common.Config;
import common.Utils;

public class InputQrelLoader extends Loader {
	private static org.apache.log4j.Logger log = Logger
			.getLogger(InputQrelLoader.class);

	public InputQrelLoader(PubMedDatabase pmDatabase, TopicsDatabase tpDatabase) {
		super(pmDatabase, tpDatabase);
	}

	public void loadMethod() {
		File qrelAbs = new File(Config.originalTopicsByAbstractsQrelFile);
		File qrelAbsFiltered = new File(Config.preprocessingTopicsByAbstractsQrelFile);
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(qrelAbs));
			FileWriter fw = null;
			BufferedWriter bw = null;
			try {
				fw = new FileWriter(qrelAbsFiltered);
				bw = new BufferedWriter(fw);
				
				for (String line; (line = br.readLine()) != null;) {
					String[] sections = line.split("\\s+");
					if (sections.length == 4) {
						int pid = Integer.parseInt(sections[2]);
						String topic = sections[0];
						boolean relevant = (sections[3].equals("1"));
						
						this.pmDatabase.addIfNotExists(new PubMedFile(pid));
						this.pmDatabase.access(pid).retrievedQrelTopicList.add(topic);
						if (this.pmDatabase.access(pid).retrievedFileTopicList.contains(topic))
							bw.write(line+"\n");
						if (relevant)
							this.pmDatabase.access(pid).relevantTopicList
								.add(topic);
						
					} else {
						throw new RuntimeException("QRel File ("
								+ qrelAbs.getName()
								+ ") has wrong format in line: " + line);
					}
					
					

				}
			} catch (NumberFormatException | IOException e) {
				e.printStackTrace();
				
				throw new RuntimeException("IO error reading the qrel file");
			} finally {
				try {
					bw.flush();
					fw.flush();
					br.close();
					bw.close();
					fw.close();
				} catch (IOException e) {
					log.log(Priority.WARN,
							"The buffer reader could not be closed correctly");
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(
					"The file given in configuration does not exist "
							+ Config.originalTopicsByAbstractsQrelFile);
		}

	}
}
