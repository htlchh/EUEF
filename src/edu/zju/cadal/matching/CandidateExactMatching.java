package edu.zju.cadal.matching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.text.html.CSS;

import edu.zju.cadal.model.Candidate;
import edu.zju.cadal.model.Entity;
import edu.zju.cadal.system.PreProcessor;
import edu.zju.cadal.utils.Pair;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月17日
 */
public class CandidateExactMatching implements Matching<Candidate> {

	/**
	 * 两个candidate匹配当且仅当mention相同，且有一个共同的消歧候选项
	 */
	@Override
	public boolean match(Candidate c1, Candidate c2) {
		if (c1.getMention().equals(c2.getMention()) == false)
			return false;
		Set<Pair<Entity, Float>> s1 = c1.getPairSet();
		Set<Pair<Entity, Float>> s2 = c2.getPairSet();
		
		//提取并比较所有的候选消歧项的id
		Set<Integer> s3 = new HashSet<Integer>();
		Set<Integer> s4 = new HashSet<Integer>();
		for (Pair<Entity, Float> p : s1)
			s3.add(PreProcessor.dereference(p.first.getId()));
		for (Pair<Entity, Float> p : s2)
			s4.add(PreProcessor.dereference(p.first.getId()));
		s3.retainAll(s4);
		if (s3.size() == 0)
			return false;
		return true;
	}

	@Override
	public Map<String, Set<Candidate>> preprocessSystemResult(Map<String, Set<Candidate>> systemResult) {
		//预处理id，加快比较速度
		List<Integer> idList = new ArrayList<Integer>();
		for (String title : systemResult.keySet()) {
			for (Candidate c : systemResult.get(title))
				for (Pair<Entity, Float> p : c.getPairSet())
					idList.add(p.first.getId());
		}
		PreProcessor.prefetchWId(idList);
		
		Map<String, Set<Candidate>> filterSystemResult = new HashMap<String, Set<Candidate>>();
		for (String title : systemResult.keySet()) {
			filterSystemResult.put(title, PreProcessor.filterOverlapCandidate(systemResult.get(title)));
		}
		return filterSystemResult;
	}

	@Override
	public Map<String, Set<Candidate>> preprocessGoldStandard(Map<String, Set<Candidate>> goldStandard) {
		List<Integer> idList = new ArrayList<Integer>();
		for (String title : goldStandard.keySet()) {
			for (Candidate c : goldStandard.get(title))
				for (Pair<Entity, Float> p : c.getPairSet())
					idList.add(p.first.getId());
		}
		PreProcessor.prefetchWId(idList);
		
		return goldStandard;
	}

	@Override
	public String getName() {
		return "Candidate Strong Matching";
	}
	
}
