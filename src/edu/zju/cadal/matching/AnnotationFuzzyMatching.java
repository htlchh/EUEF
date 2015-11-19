package edu.zju.cadal.matching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.zju.cadal.model.Annotation;
import edu.zju.cadal.model.Entity;
import edu.zju.cadal.system.PreProcessor;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月17日
 */
public class AnnotationFuzzyMatching implements Matching<Annotation>{

	@Override
	public boolean match(Annotation a1, Annotation a2) {
		MentionFuzzyMatching mwm = new MentionFuzzyMatching();
		if (mwm.match(a1.getMention(), a2.getMention()) == false)
			return false;
		
		Entity e1 = a1.getEntity();
		Entity e2 = a2.getEntity();
		if (PreProcessor.dereference(e1.getId()) != PreProcessor.dereference(e2.getId()))
			return false;
		
		return true;
	}

	@Override
	public Map<String, Set<Annotation>> preprocessSystemResult(Map<String, Set<Annotation>> systemResult) {
		//预先查询结果中的id，加快比较速度
		List<Integer> idList = new ArrayList<Integer>();
		for (Set<Annotation> aSet : systemResult.values()) 
			for (Annotation a : aSet)
				idList.add(a.getEntity().getId());
		PreProcessor.prefetchWId(idList);
		
		//过滤掉mention重叠的annotation
		Map<String, Set<Annotation>> filterSystemResult = new HashMap<String, Set<Annotation>>();
		for (String title : systemResult.keySet()) {
			filterSystemResult.put(title, PreProcessor.filterOverlapAnnotation(systemResult.get(title)));
		}
		return filterSystemResult;
	}

	@Override
	public Map<String, Set<Annotation>> preprocessGoldStandard(Map<String, Set<Annotation>> goldStandard) {
		List<Integer> idList = new ArrayList<Integer>();
		for (Set<Annotation> aSet : goldStandard.values()) 
			for (Annotation a : aSet)
				idList.add(a.getEntity().getId());
		PreProcessor.prefetchWId(idList);
		return goldStandard;
	}

	@Override
	public String getName() {
		return "Annotation Weak Matching";
	}

}
