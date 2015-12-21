package edu.zju.cadal.matching;

import java.util.Map;
import java.util.Set;

import edu.zju.cadal.main.Filter;
import edu.zju.cadal.model.Mention;
import edu.zju.cadal.model.NIL;


public class NILMatching implements Matching<NIL>{
	private MentionMatching mm;
	private Filter<NIL> filter;
	
	public NILMatching(MentionMatching mm) {
		this.mm = mm;
		filter = new Filter<NIL>(this);
	}
	
	@Override
	public boolean match(NIL n1, NIL n2) {
		Mention m1 = n1.getMention();
		Mention m2 = n2.getMention();
		if (mm.match(m1, m2) == false)
			return false;
			
		return true;
	}

	@Override
	public String getName() {
		return "NIL Matching";
	}

	@Override
	public void preProcessing(Map<String, Set<NIL>> prediction, Map<String, Set<NIL>> goldStandard) {
		filter.coreference(prediction);
		filter.coreference(goldStandard);
		filter.filterMany2One(prediction, goldStandard);
	}

	@Override
	public MentionMatching getBaseMentionMatching() {
		return this.mm;
	}

}
