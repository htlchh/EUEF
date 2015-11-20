package edu.zju.cadal.matching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.zju.cadal.model.Annotation;
import edu.zju.cadal.model.Candidate;
import edu.zju.cadal.model.Mention;
import edu.zju.cadal.model.NIL;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月16日
 */
public class PreProcessor {
	
	private MentionFuzzyMatching mfm;
	
	public PreProcessor(MentionFuzzyMatching mfm) {
		this.mfm = mfm;
	}
	
	/**
	 * 根据给的MentionFuzzyMatching类过滤结果中有重叠的mention
	 * 如果两个mention模糊匹配，则选择长度大的mention
	 * @param mentionMap
	 */
	public void filterFuzzyMatchedMention(Map<String, Set<Mention>> mentionMap) {
		Map<String, Set<Mention>> resultMap = new HashMap<String, Set<Mention>>();
		for (String title : mentionMap.keySet()) {
			List<Mention> mentionList = new ArrayList<Mention>(mentionMap.get(title));
			Set<Mention> mentionSet = new HashSet<Mention>();
			for (int i = 0; i < mentionList.size(); i++) {
				Mention bestMention = mentionList.get(i);
				for (int j = 0; j < mentionList.size(); j++) {
					if (coMention(bestMention, mentionList.get(j)) != null)
						bestMention = coMention(bestMention, mentionList.get(j));
				}
				mentionSet.add(bestMention);
			}
			resultMap.put(title, mentionSet);
		}
		
		for (String title : mentionMap.keySet()) {
			mentionMap.get(title).clear();
			mentionMap.get(title).addAll(resultMap.get(title));
		}
	}
	
	
	public void filterFuzzyMatchCandidate(Map<String, Set<Candidate>> candidateMap) {
		Map<String, Set<Candidate>> resultMap = new HashMap<String, Set<Candidate>>();
		for (String title : candidateMap.keySet()) {
			List<Candidate> candidateList = new ArrayList<Candidate>(candidateMap.get(title));
			Set<Candidate> candidateSet = new HashSet<Candidate>();
			for (int i = 0; i < candidateList.size(); i++) {
				Candidate bestCandidate = candidateList.get(i);
				for (int j = 0; j < candidateList.size(); j++) {
					if (coCandidate(bestCandidate, candidateList.get(j)) != null)
						bestCandidate = coCandidate(bestCandidate, candidateList.get(j));
				}
				candidateSet.add(bestCandidate);
			}
			resultMap.put(title, candidateSet);
		}
		
		for (String title : candidateMap.keySet()) {
			candidateMap.get(title).clear();
			candidateMap.get(title).addAll(resultMap.get(title));
		}
	}
	
	
	public void filterFuzzyMatchAnnotation(Map<String, Set<Annotation>> annotationMap) {
		Map<String, Set<Annotation>> resultMap = new HashMap<String, Set<Annotation>>();
		for (String title : annotationMap.keySet()) {
			List<Annotation> annotationList = new ArrayList<Annotation>(annotationMap.get(title));
			Set<Annotation> annotationSet = new HashSet<Annotation>();
			for (int i = 0; i < annotationList.size(); i++) {
				Annotation bestAnnotation = annotationList.get(i);
				for (int j = 0; j < annotationList.size(); j++) {
					if (coAnnotation(bestAnnotation, annotationList.get(j)) != null)
						bestAnnotation = coAnnotation(bestAnnotation, annotationList.get(j));
				}
				annotationSet.add(bestAnnotation);
			}
			resultMap.put(title, annotationSet);
		}
		for (String title : annotationMap.keySet()) {
			annotationMap.get(title).clear();
			annotationMap.get(title).addAll(resultMap.get(title));
		}
	}
	
	public void filterFuzzyMatchNIL(Map<String, Set<NIL>> nILMap) {
		Map<String, Set<NIL>> resultMap = new HashMap<String, Set<NIL>>();
		for (String title : nILMap.keySet()) {
			List<NIL> nILList = new ArrayList<NIL>(nILMap.get(title));
			Set<NIL> nILSet = new HashSet<NIL>();
			for (int i = 0; i < nILList.size(); i++) {
				NIL bestNIL = nILList.get(i);
				for (int j = 0; j < nILList.size(); j++) {
					if (coNIL(bestNIL, nILList.get(j)) != null)
						bestNIL = coNIL(bestNIL, nILList.get(j));
				}
				nILSet.add(bestNIL);
			}
			resultMap.put(title, nILSet);
		}
		for (String title : nILMap.keySet()) {
			nILMap.get(title).clear();
			nILMap.get(title).addAll(resultMap.get(title));
		}
	}
	
	
	public Mention coMention(Mention m1, Mention m2) {
		if (mfm.match(m1, m2) == false)
			return null;
		if (m1.getLength() > m2.getLength())
			return m1;
		else
			return m2;
	}
	
	public Annotation coAnnotation(Annotation a1, Annotation a2) {
		Mention m1 = a1.getMention();
		Mention m2 = a2.getMention();
		
		if (coMention(m1, m2) == null)
			return null;
		if (coMention(m1, m2) == m1)
			return a1;
		else
			return a2;
	}
	
	public Candidate coCandidate(Candidate c1, Candidate c2) {
		Mention m1 = c1.getMention();
		Mention m2 = c2.getMention();
		
		if (coMention(m1, m2) == null)
			return null;
		if (coMention(m1, m2) == m1)
			return c1;
		else
			return c2;
	}
	
	public NIL coNIL(NIL n1, NIL n2) {
		Mention m1 = n1.getMention();
		Mention m2 = n2.getMention();
		
		if (mfm.match(m1, m2) == false)
			return null;
		if (m1.getLength() > m2.getLength())
			return n1;
		else 
			return n2;
	}
	
}
