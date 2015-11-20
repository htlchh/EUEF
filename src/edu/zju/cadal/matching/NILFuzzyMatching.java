package edu.zju.cadal.matching;

import java.util.Map;
import java.util.Set;

import edu.zju.cadal.model.Mention;
import edu.zju.cadal.model.NIL;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月19日
 */
public class NILFuzzyMatching implements Matching<NIL>{
	private MentionFuzzyMatching mfm;
	private PreProcessor preProcessor;
	
	public NILFuzzyMatching(MentionFuzzyMatching mfm) {
		this.mfm = mfm;
		preProcessor = new PreProcessor(mfm);
	}
	
	@Override
	public boolean match(NIL n1, NIL n2) {
		Mention m1 = n1.getMention();
		Mention m2 = n2.getMention();
		if (mfm.match(m1, m2) == false)
			return false;
			
		return true;
	}

	@Override
	public void preProcessSystemResult(Map<String, Set<NIL>> systemResult) {
		preProcessor.filterFuzzyMatchNIL(systemResult);
	}

	@Override
	public void preProcessGoldStandard(Map<String, Set<NIL>> goldStandard) {
	}

	@Override
	public String getName() {
		return "NIL Fuzzy Matching";
	}

}
