package edu.zju.cadal.matching;

import java.util.Map;
import java.util.Set;

import edu.zju.cadal.model.Mention;

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
	public void preProcessSystemResult(Map<String, Set<Mention>> systemResult) {
	}

	@Override
	public void preProcessGoldStandard(Map<String, Set<Mention>> goldStandard) {
	}

	@Override
	public String getName() {
		return "Mention Exact Matching";
	}

}
