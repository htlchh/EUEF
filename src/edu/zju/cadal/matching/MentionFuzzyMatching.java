package edu.zju.cadal.matching;

import java.util.Map;
import java.util.Set;

import edu.zju.cadal.model.Candidate;
import edu.zju.cadal.model.Mention;
import edu.zju.cadal.utils.EditDistance;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月16日
 */
public class MentionFuzzyMatching implements Matching<Mention>{

	private PreProcessor preProcessor;
	private float distanceThreshold = 0.85f;

	public MentionFuzzyMatching() {
		this.preProcessor = new PreProcessor(this);
	}
	
	public MentionFuzzyMatching(float distanceThreshold) {
		this.distanceThreshold = distanceThreshold;
	}


	@Override
	public boolean match(Mention m1, Mention m2) {
		return editDistanceFuzzyMatching(m1, m2);
	}
	
	
	public float getDistanceThreshold() {
		return distanceThreshold;
	}
	
	public void setDistanceThreshold(float distanceThreshold) {
		this.distanceThreshold = distanceThreshold;
	}

	@Override
	public void preProcessSystemResult(Map<String, Set<Mention>> systemResult) {
		preProcessor.filterFuzzyMatchedMention(systemResult);

//		for (String title : systemResult.keySet()) {
//			Set<Mention> cSet1 = systemResult.get(title);
//			Set<Mention> cSet2 = systemResult.get(title);
//			for (Mention c1 : cSet1) {
//				for (Mention c2 : cSet2) {
//					if (match(c1, c2) && c1 != c2)
//						System.out.println(c1 + "|" + c2);
//				}
//			}
//		}		
	}

	@Override
	public void preProcessGoldStandard(Map<String, Set<Mention>> goldStandard) {
		
	}

	@Override
	public String getName() {
		return "Mention Fuzzy Matching";
	}

	private boolean editDistanceFuzzyMatching(Mention m1, Mention m2) {
		if (m1.overlap(m2) == false)
			return false;
		
		float similarity = EditDistance.distance(m1.getSurfaceForm(), m2.getSurfaceForm());
//		//取两个词中较短的一个词的长度
//		float length = m1.getSurfaceForm().length() < m2.getSurfaceForm().length() 
//						? m2.getSurfaceForm().length() 
//						: m1.getSurfaceForm().length();
		//取数据集的词作为标准长度
		float length = m2.getSurfaceForm().length();
		
//		System.out.println(m1.getSurfaceForm() + " | " + m2.getSurfaceForm() + similarity/length);
		float confidence = 1 - similarity/length;
		if (confidence > this.distanceThreshold)
			return true;
		else 
			return false;
	}
	
}
