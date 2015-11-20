package edu.zju.cadal.test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import edu.zju.cadal.cache.EvaluationResult;
import edu.zju.cadal.matching.Evaluation;
import edu.zju.cadal.matching.Matching;
import edu.zju.cadal.matching.MentionExactMatching;
import edu.zju.cadal.matching.MentionFuzzyMatching;
import edu.zju.cadal.model.Mention;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月17日
 */
public class TestMentionMatching {

	@Test
	public void test_getResult() {
		Map<String, Set<Mention>> systemResult = new HashMap<String, Set<Mention>>();
		Map<String, Set<Mention>> goldStandard = new HashMap<String, Set<Mention>>();
		Set<Mention> sys = new HashSet<Mention>();
		Set<Mention> gold = new HashSet<Mention>();
		sys.add(new Mention("CAC", 4976, 3, 0.5f));
		sys.add(new Mention("Lennar", 4253, 6, 0.9f));
		gold.add(new Mention("Lennar Corp.", 4253, 12, 1f));
		gold.add(new Mention("CAC-40", 4976, 6, 1f));
		systemResult.put("test", sys);
		goldStandard.put("test", gold);
		MentionFuzzyMatching mfm = new MentionFuzzyMatching();
		mfm.setDistanceThreshold(0.000f);
		
		EvaluationResult result = Evaluation.getResult(systemResult, goldStandard, mfm);
		System.out.println(result);
		result.detailPRF();
		
	}
	
}
