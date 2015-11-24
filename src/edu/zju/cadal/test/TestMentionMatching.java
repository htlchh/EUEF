package edu.zju.cadal.test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import edu.zju.cadal.cache.EvaluationResult;
import edu.zju.cadal.exception.UnknowMatchingException;
import edu.zju.cadal.matching.Evaluation;
import edu.zju.cadal.matching.Matching;
import edu.zju.cadal.matching.MentionExactMatching;
import edu.zju.cadal.matching.MentionFuzzyMatching;
import edu.zju.cadal.model.Annotation;
import edu.zju.cadal.model.Candidate;
import edu.zju.cadal.model.Entity;
import edu.zju.cadal.model.Mention;
import edu.zju.cadal.model.NIL;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月17日
 */
public class TestMentionMatching {

	@Test
	public void test_getResult() {
		Set<Mention> sys = new HashSet<Mention>();
		Set<Mention> gold = new HashSet<Mention>();
		sys.add(new Mention("CAC", 4976, 3, 0.5f));
		sys.add(new Mention("Lennar", 4253, 6, 0.9f));
		gold.add(new Mention("Lennar Corp.", 4253, 12, 1f));
		gold.add(new Mention("CAC-40", 4976, 6, 1f));
		MentionFuzzyMatching mfm = new MentionFuzzyMatching();
		mfm.setDistanceThreshold(0.000f);
		
		System.out.println(new Mention("CAC", 4976, 3, 0.5f) + "|" + new Mention("CAC-40", 4976, 6, 1f) + ":" +
				mfm.match(new Mention("CAC", 4976, 3, 0.5f), new Mention("CAC-40", 4976, 6, 1f)));
		System.out.println(new Mention("Lennar", 4253, 6, 0.9f) + "|" + new Mention("Lennar Corp.", 4253, 12, 1f) + ":" +
				mfm.match(new Mention("Lennar", 4253, 6, 0.9f), new Mention("Lennar Corp.", 4253, 12, 1f)));
		int tp = 0;
		int fn = 0;
		for (Mention g : gold) {
			boolean matched = false;
			for (Mention s : sys) {
				System.out.println(g + "|" + s + ":" + mfm.match(s, g));
				if (mfm.match(g, s) == true)
					matched = true;
			}
			if (matched == true)
				tp++;
			else
				fn++;
		}
		System.out.println("tp:" + tp);
		System.out.println("fn:" + fn);
	}
	
}
