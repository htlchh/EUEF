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
		sys.add(new Mention(1, 10, 0.8f));
		sys.add(new Mention(5, 6, 0.9f));
		gold.add(new Mention(2, 10));
		gold.add(new Mention(20, 5));
		systemResult.put("test", sys);
		goldStandard.put("test", gold);
		MentionExactMatching msm = new MentionExactMatching();
		EvaluationResult result = Evaluation.getResult(systemResult, goldStandard, msm);
		System.out.println(result);
		result.detailPRF();
		
		MentionFuzzyMatching mwm = new MentionFuzzyMatching();
		result = Evaluation.getResult(systemResult, goldStandard, mwm);
		System.out.println(result);
		result.detailPRF();
	}
}
