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
 * + 对系统产生的多个mention对应一个相同的gold mention的情况进行预处理
 * + 对系统产生的多个互相重叠mention的情况进行预处理 - coreference mention
 * + 对重叠的gold mention进行预处理 - coreference gold mention
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月16日
 */
public class PreProcessor {
	
	private MentionMatching mfm;
	
	public PreProcessor(MentionMatching mfm) {
		this.mfm = mfm;
	}
	
	/**
	 * 系统产生的多个mention如果对应到同一个gold mention,那么只保留长度最长的那个mention
	 * @param systemResult
	 * @param goldStandard
	 */
	public void filterDuplicatedMention(Map<String, Set<Mention>> systemResult, Map<String, Set<Mention>> goldStandard) {
		Map<String, Set<Mention>> resultMap = new HashMap<String, Set<Mention>>();
		
		for (String title : systemResult.keySet()) {
			Map<Integer, Set<Mention>> filterMap = new HashMap<Integer, Set<Mention>>();
			Set<Mention> mentionSet = new HashSet<Mention>();
			
			//把每个system mention分别对应到正确的gold mention上
			//如果没有对应的gold mention，则附加到新定义的missed的key上
			for (Mention systemMention : systemResult.get(title)) 
			{
				boolean matched = false;
				for (Mention goldMention : goldStandard.get(title)) 
				{
					if (filterMap.containsKey(goldMention.hashCode()) == false)
						filterMap.put(goldMention.hashCode(), new HashSet<Mention>());
					
					if (mfm.match(systemMention, goldMention) == true) {
						filterMap.get(goldMention.hashCode()).add(systemMention);
						matched = true;
						break;
					}
				}
				
				if (matched == false) {
					if (filterMap.containsKey(new String("missed").hashCode()) == false)
						filterMap.put(new String("missed").hashCode(), new HashSet<Mention>());
					
					filterMap.get(new String("missed").hashCode()).add(systemMention);
				}
			}
			
			//选择每个gold mention的多个system mention中长度最长的一个
			for (Integer integer : filterMap.keySet()) {
				if (integer.equals(new String("missed").hashCode()) == true)
					mentionSet.addAll(filterMap.get(integer));
				
				else if (filterMap.get(integer).size() > 1) {
					Mention bestMention = null;
					for (Mention m : filterMap.get(integer)) {
						if (bestMention == null || m.getSurfaceForm().length() > bestMention.getSurfaceForm().length())
							bestMention = m;
					}
					mentionSet.add(bestMention);
				} 
				else
					mentionSet.addAll(filterMap.get(integer));
			}
			
			resultMap.put(title, mentionSet);
		}
		
		for (String title : systemResult.keySet()) {
			systemResult.get(title).clear();
			systemResult.get(title).addAll(resultMap.get(title));
		}		
	}
	
	
	/**
	 * 根据gold standard,过滤mention重叠的candidate,只保留mention长度最长的candidate
	 * @param systemResult
	 * @param goldStandard
	 */
	public void filterDuplicatedCandidate(Map<String, Set<Candidate>> systemResult, Map<String, Set<Candidate>> goldStandard) {
		Map<String, Set<Candidate>> resultMap = new HashMap<String, Set<Candidate>>();
		
		for (String title : systemResult.keySet()) {
			Map<Integer, Set<Candidate>> filterMap = new HashMap<Integer, Set<Candidate>>();
			Set<Candidate> cSet = new HashSet<Candidate>();
			
			for (Candidate sc : systemResult.get(title)) 
			{
				boolean matched = false;
				for (Candidate gc : goldStandard.get(title)) 
				{
					if (filterMap.containsKey(gc.hashCode()) == false)
						filterMap.put(gc.hashCode(), new HashSet<Candidate>());
					
					if (mfm.match(sc.getMention(), gc.getMention()) == true) {
						filterMap.get(gc.hashCode()).add(sc);
						matched = true;
						break;
					}
				}
				
				if (matched == false) {
					if (filterMap.containsKey(new String("missed").hashCode()) == false)
						filterMap.put(new String("missed").hashCode(), new HashSet<Candidate>());
					
					filterMap.get(new String("missed").hashCode()).add(sc);
				}
			}
			
			//选择每个gold mention的多个system mention中长度最长的一个
			for (Integer integer : filterMap.keySet()) {
				if (integer.equals(new String("missed").hashCode()) == true)
					cSet.addAll(filterMap.get(integer));
				
				else if (filterMap.get(integer).size() > 1) {
					Candidate best = null;
					for (Candidate c : filterMap.get(integer)) {
						if (best == null || c.getMention().getSurfaceForm().length() > best.getMention().getSurfaceForm().length())
							best = c;
					}
					cSet.add(best);
				} 
				else
					cSet.addAll(filterMap.get(integer));
			}
			
			resultMap.put(title, cSet);
		}
		
		for (String title : systemResult.keySet()) {
			systemResult.get(title).clear();
			systemResult.get(title).addAll(resultMap.get(title));
		}			
	}
	
	/**
	 * 
	 * @param systemResult
	 * @param goldStandard
	 */
	public void filterDuplicatedAnnotation(Map<String, Set<Annotation>> systemResult, Map<String, Set<Annotation>> goldStandard) {
		Map<String, Set<Annotation>> resultMap = new HashMap<String, Set<Annotation>>();
		
		for (String title : systemResult.keySet()) {
			Map<Integer, Set<Annotation>> filterMap = new HashMap<Integer, Set<Annotation>>();
			Set<Annotation> aSet = new HashSet<Annotation>();
			
			for (Annotation sa : systemResult.get(title)) 
			{
				boolean matched = false;
				for (Annotation ga : goldStandard.get(title)) 
				{
					if (filterMap.containsKey(ga.hashCode()) == false)
						filterMap.put(ga.hashCode(), new HashSet<Annotation>());
					
					if (mfm.match(sa.getMention(), ga.getMention()) == true) {
						filterMap.get(ga.hashCode()).add(sa);
						matched = true;
						break;
					}
				}
				
				if (matched == false) {
					if (filterMap.containsKey(new String("missed").hashCode()) == false)
						filterMap.put(new String("missed").hashCode(), new HashSet<Annotation>());
					
					filterMap.get(new String("missed").hashCode()).add(sa);
				}
			}
			
			//选择每个gold mention的多个system mention中长度最长的一个
			for (Integer integer : filterMap.keySet()) {
				if (integer.equals(new String("missed").hashCode()) == true)
					aSet.addAll(filterMap.get(integer));
				
				else if (filterMap.get(integer).size() > 1) {
					Annotation best = null;
					for (Annotation c : filterMap.get(integer)) {
						if (best == null || c.getMention().getSurfaceForm().length() > best.getMention().getSurfaceForm().length())
							best = c;
					}
					aSet.add(best);
				} 
				else
					aSet.addAll(filterMap.get(integer));
			}
			
			resultMap.put(title, aSet);
		}
		
		for (String title : systemResult.keySet()) {
			systemResult.get(title).clear();
			systemResult.get(title).addAll(resultMap.get(title));
		}			
	}
	
	
	public void filterDuplicatedNIL(Map<String, Set<NIL>> systemResult, Map<String, Set<NIL>> goldStandard) {
		Map<String, Set<NIL>> resultMap = new HashMap<String, Set<NIL>>();
		for (String title : systemResult.keySet()) {
			Map<Integer, Set<NIL>> filterMap = new HashMap<Integer, Set<NIL>>();
			Set<NIL> nSet = new HashSet<NIL>();
			
			for (NIL sn : systemResult.get(title)) 
			{
				boolean matched = false;
				for (NIL gn : goldStandard.get(title)) 
				{
					if (filterMap.containsKey(gn.hashCode()) == false)
						filterMap.put(gn.hashCode(), new HashSet<NIL>());
					
					if (mfm.match(sn.getMention(), gn.getMention()) == true) {
						filterMap.get(gn.hashCode()).add(sn);
						matched = true;
						break;
					}
				}
				
				if (matched == false) {
					if (filterMap.containsKey(new String("missed").hashCode()) == false)
						filterMap.put(new String("missed").hashCode(), new HashSet<NIL>());
					
					filterMap.get(new String("missed").hashCode()).add(sn);
				}
			}
			
			//选择每个gold mention的多个system mention中长度最长的一个
			for (Integer integer : filterMap.keySet()) {
				if (integer.equals(new String("missed").hashCode()) == true)
					nSet.addAll(filterMap.get(integer));
				
				else if (filterMap.get(integer).size() > 1) {
					NIL best = null;
					for (NIL c : filterMap.get(integer)) {
						if (best == null || c.getMention().getSurfaceForm().length() > best.getMention().getSurfaceForm().length())
							best = c;
					}
					nSet.add(best);
				} 
				else
					nSet.addAll(filterMap.get(integer));
			}
			
			resultMap.put(title, nSet);
		}
		
		for (String title : systemResult.keySet()) {
			systemResult.get(title).clear();
			systemResult.get(title).addAll(resultMap.get(title));
		}			
	}
	
	
	/**
	 * 根据给的MentionMatching类过滤结果中有重叠的mention
	 * 如果两个mention匹配，则选择长度大的mention
	 * @param mentionMap
	 */
	public void mentionCoreference(Map<String, Set<Mention>> mentionMap) {
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
	
	
	/**
	 * 根据给定的mention匹配方式，过滤结果中mention重叠的candidate
	 * @param candidateMap
	 */
	public void candidateCoreference(Map<String, Set<Candidate>> candidateMap) {
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
	
	
	public void annotationCoreference(Map<String, Set<Annotation>> annotationMap) {
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
	
	public void NILCoreference(Map<String, Set<NIL>> nILMap) {
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
	
	
	private Mention coMention(Mention m1, Mention m2) {
		if (mfm.match(m1, m2) == false)
			return null;
		if (m1.getLength() > m2.getLength())
			return m1;
		else
			return m2;
	}
	
	private Annotation coAnnotation(Annotation a1, Annotation a2) {
		Mention m1 = a1.getMention();
		Mention m2 = a2.getMention();
		
		if (coMention(m1, m2) == null)
			return null;
		if (coMention(m1, m2) == m1)
			return a1;
		else
			return a2;
	}
	
	private Candidate coCandidate(Candidate c1, Candidate c2) {
		Mention m1 = c1.getMention();
		Mention m2 = c2.getMention();
		
		if (coMention(m1, m2) == null)
			return null;
		if (coMention(m1, m2) == m1)
			return c1;
		else
			return c2;
	}
	
	private NIL coNIL(NIL n1, NIL n2) {
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
