package edu.zju.cadal.matching;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.zju.cadal.model.Mention;
import edu.zju.cadal.system.PreProcessor;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月17日
 */
public class MentionExactMatching implements Matching<Mention>{

	@Override
	public boolean match(Mention m1, Mention m2) {
		return m1.equals(m2);
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
		return "Mention Strong Matching";
	}

}
