package evaluation;

import java.util.List;

import common.Config;

public class SimpleEvaluator {
	private List<RelevanceResult> results;
	
	public SimpleEvaluator (List<RelevanceResult> results)
	{
		this.results = results;
	}
	
	
	
	public double precisionRecall (boolean precision)
	{
		int hits = 0;
		int neghits = 0;
		int numDocs = 0;
		for (RelevanceResult result : results)
		{
			if (result.trueLabel == 1 && result.action == 1)
				hits++;
			if (result.trueLabel == 0 && result.action == 0)
				neghits++;
			numDocs++;
		}
		return (precision)? hits/numDocs : neghits/numDocs;
	}
	
	public class RelevanceResult
	{
		
		public int trueLabel; // 0 is not relevant, 1 in abs, 2 in content
		public double score;
		public double action; // 0 is dont use, 1 use relevance feedback, 2 dont use relevance feedback
		
		public RelevanceResult(int trueLabel, double score, double action)
		{
			this.trueLabel = trueLabel;
			this.score = score;
			this.action = action;
		}
	}
}
