package common;

import query.QueryHelpers;

public class Main {
	public static void main(String[] args) {
		if (args.length > 0) {
			if (args[0].equals("download"))
				Config.DOWNLOADPUBMED = true;
			if (args[0].equals("index"))
				Config.RECREATE_INDEX = true;
			if (args[0].equals("feats"))
				Config.RECREATE_FEATS = true;
		}
		
		Preprocessing.run();
		EvaluationRun.run();
		
	}
}