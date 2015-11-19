package edu.zju.cadal.matching;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.zju.cadal.model.Mention;
import edu.zju.cadal.system.PreProcessor;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月16日
 */
public class MentionFuzzyMatching implements Matching<Mention>{

	@Override
	public boolean match(Mention m1, Mention m2) {
		return overlap(m1, m2);
	}

	@Override
	public Map<String, Set<Mention>> preprocessSystemResult(Map<String, Set<Mention>> systemResult) {
		Map<String, Set<Mention>> filteredResult = new HashMap<String, Set<Mention>>();
		for (String title : systemResult.keySet()) {
			filteredResult.put(title, PreProcessor.coreference(systemResult.get(title)));
		}
		return filteredResult;
	}

	@Override
	public Map<String, Set<Mention>> preprocessGoldStandard(Map<String, Set<Mention>> goldStandard) {
		return goldStandard;
	}

	@Override
	public String getName() {
		return "Mention Weak Matching";
	}

	private boolean overlap(Mention m1, Mention m2) {
		int p1 = m1.getPosition();
		int l1 = m1.getLength();
		int e1 = p1 + l1 - 1;
		int p2 = m2.getPosition();
		int l2 = m2.getLength();
		int e2 = p2 + l2 - 1;
		return (
				(p1 <= p2 && p2 <= e1) ||
				(p2 <= p1 && p1 <= e2) ||
				(p1 >= p2 && e1 <= e2) ||
				(p1 <= p2 && e1 >= e2)
				);		
	}
}
