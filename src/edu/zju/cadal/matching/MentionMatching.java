package edu.zju.cadal.matching;

import java.util.Map;
import java.util.Set;

import edu.zju.cadal.main.Filter;
import edu.zju.cadal.model.Mention;
import edu.zju.cadal.utils.EditDistance;

public class MentionMatching implements Matching<Mention>{

	private Filter<Mention> filter;
	private float distanceThreshold = 0.85f;

	public MentionMatching() {
		this.filter = new Filter<Mention>(this);
	}
	
	public MentionMatching(float distanceThreshold) {
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
	public void preProcessing(Map<String, Set<Mention>> prediction, Map<String, Set<Mention>> goldStandard) {
		filter.coreference(prediction);
		filter.coreference(goldStandard);
		filter.filterMany2One(prediction, goldStandard);
	}


	@Override
	public String getName() {
		return "Mention Matching";
	}

	/**
	 * Check if two mentions are matched or not,
	 * If the distance >= distanceThreshold, then true; Otherwise, false
	 * */
	private boolean editDistanceFuzzyMatching(Mention m1, Mention m2) {
		if (m1.overlap(m2) == false)
			return false;
		
		float similarity = EditDistance.distance(m1.getSurfaceForm(), m2.getSurfaceForm());
//		System.out.println(m1.getSurfaceForm() + "|" + m2.getSurfaceForm() + ":" + similarity);
		float length = m1.getSurfaceForm().length() > m2.getSurfaceForm().length() 
						? m1.getSurfaceForm().length() 
						: m2.getSurfaceForm().length();
		
//		System.out.println(m1.getSurfaceForm() + " | " + m2.getSurfaceForm() + similarity/length);
		float confidence = 1 - similarity/length;
		if (confidence > this.distanceThreshold)
			return true;
		else 
			return false;
	}

	@Override
	public MentionMatching getBaseMentionMatching() {
		return this;
	}
	
}
