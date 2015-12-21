package edu.zju.cadal.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.zju.cadal.matching.Matching;
import edu.zju.cadal.model.Model;

/**
 * Class for prediction and gold standards preprocessing 
 *
 */
public class Filter<T extends Model> {
	
	private Matching<T> m;
	
	public Filter(Matching<T> m) {
		this.m = m;
	}
	
	
	/**
	 * If two mentions are both TPs compared with a same gold mention,
	 * then delete these mentions but the mention with largest length.
	 * e.g.
	 * two predicted mentions:
	 *    Washington[2,10]
	 *    D.C.[11,4]
	 * gold mention
	 *    Washington D.C.[2,15]
	 * This function would filter D.C., but retain Washington. 
	 * */	
	public void filterMany2One(Map<String, Set<T>> prediction, Map<String, Set<T>> goldStandard) {
		Map<String, Set<T>> retainedMap = new HashMap<String, Set<T>>();
		for (String title : prediction.keySet()) {
			Map<Integer, Set<T>> gpMap = new HashMap<Integer, Set<T>>();
			Set<T> retainedSet = new HashSet<T>();
			for (T p : prediction.get(title)) 
			{
				boolean matched = false;
				for (T g : goldStandard.get(title)) 
				{
					if (gpMap.containsKey(g.hashCode()) == false)
						gpMap.put(g.hashCode(), new HashSet<T>());
					if (m.match(p, g) == true) {
						gpMap.get(g.hashCode()).add(p);
						matched = true;
						break;
					}
				}
				if (matched == false) {
					if (gpMap.containsKey(new String("missed").hashCode()) == false)
						gpMap.put(new String("missed").hashCode(), new HashSet<T>());
					gpMap.get(new String("missed").hashCode()).add(p);
				}
			}
			/** Choose the mention with largest length */
			for (Integer integer : gpMap.keySet()) {
				/** Add all mentions with the key 'missed'*/
				if (integer.equals(new String("missed").hashCode()) == true)
					retainedSet.addAll(gpMap.get(integer));
				else if (gpMap.get(integer).size() > 1) {
					T best = null;
					for (T t : gpMap.get(integer))
						if (best == null || t.getLength() > best.getLength())
							best = t;
					retainedSet.add(best);
				}
				else 
					retainedSet.addAll(gpMap.get(integer));
			}
			retainedMap.put(title, retainedSet);
		}
		prediction.clear();
		prediction.putAll(retainedMap);
	}
	
	/**
	 * Coreference the models (mentions, canddiates, annotations and NILs),
	 * If the mentions embedded in models are matched according to the matching metric,
	 * then choose the one with largest length, and discard others 
	 * */
	public void coreference(Map<String, Set<T>> origin) {
		Map<String, Set<T>> retainedMap = new HashMap<String, Set<T>>();
		for (String title : origin.keySet()) {
			List<T> tList = new ArrayList<T>(origin.get(title));
			Set<T> retainedSet = new HashSet<T>();
			for (int i = 0; i < tList.size(); i++) {
				T best = tList.get(i);
				for (int j = 0; j < tList.size(); j++) {
					if (j != i && m.getBaseMentionMatching().match(best.getMention(), tList.get(j).getMention()) == true)
						best = best.getLength() > tList.get(j).getLength() ? best : tList.get(j);
				}
				retainedSet.add(best);
			}
			retainedMap.put(title, retainedSet);
		}
		origin.clear();
		origin.putAll(retainedMap);
	}
//	/**
//	 * If two mentions are both TPs compared with a same gold mention,
//	 * then delete these mentions but the mention with largest length.
//	 * e.g.
//	 * two predicted mentions:
//	 *    Washington[2,10]
//	 *    D.C.[11,4]
//	 * gold mention
//	 *    Washington D.C.[2,15]
//	 * This function would filter D.C., but retain Washington. 
//	 * */
//	public void filterDuplicatedMention(Map<String, Set<Mention>> prediction, Map<String, Set<Mention>> goldStandard) {
//		Map<String, Set<Mention>> retainedMap = new HashMap<String, Set<Mention>>();
//	
//		for (String title : prediction.keySet()) {
//			/** Map to store gold mention and its predicted mentions*/
//			Map<Integer, Set<Mention>> gpMap = new HashMap<Integer, Set<Mention>>();
//			Set<Mention> mentionSet = new HashSet<Mention>();
//			
//			/** If a predicted mention has a gold mention, then it is added in the gpMap with key of the gold mention
//			 *  Else the predicted mention is added with a of "missed" */
//			for (Mention pMention : prediction.get(title)) 
//			{
//				boolean matched = false;
//				for (Mention goldMention : goldStandard.get(title)) 
//				{
//					if (gpMap.containsKey(goldMention.hashCode()) == false)
//						gpMap.put(goldMention.hashCode(), new HashSet<Mention>());
//					
//					if (mm.match(pMention, goldMention) == true) {
//						gpMap.get(goldMention.hashCode()).add(pMention);
//						matched = true;
//						break;
//					}
//				}
//			
//				if (matched == false) {
//					if (gpMap.containsKey(new String("missed").hashCode()) == false)
//						gpMap.put(new String("missed").hashCode(), new HashSet<Mention>());
//					gpMap.get(new String("missed").hashCode()).add(pMention);
//				}
//			}
//			
//			/** Choose the mention with largest length */
//			for (Integer integer : gpMap.keySet()) {
//				/** Add all mentions with the key "missed" */
//				if (integer.equals(new String("missed").hashCode()) == true)
//					mentionSet.addAll(gpMap.get(integer));
//				
//				else if (gpMap.get(integer).size() > 1) {
//					Mention bestMention = null;
//					for (Mention m : gpMap.get(integer)) {
//						if (bestMention == null || m.getSurfaceForm().length() > bestMention.getSurfaceForm().length())
//							bestMention = m;
//					}
//					mentionSet.add(bestMention);
//				} 
//				else
//					mentionSet.addAll(gpMap.get(integer));
//			}
//			retainedMap.put(title, mentionSet);
//		}
//		
//		for (String title : prediction.keySet()) {
//			prediction.get(title).clear();
//			prediction.get(title).addAll(retainedMap.get(title));
//		}		
//	}
//	
//	
//	/**
//	 * 根据gold standard,过滤mention重叠的candidate,只保留mention长度最长的candidate
//	 * @param systemResult
//	 * @param goldStandard
//	 */
//	public void filterDuplicatedCandidate(Map<String, Set<Candidate>> systemResult, Map<String, Set<Candidate>> goldStandard) {
//		Map<String, Set<Candidate>> resultMap = new HashMap<String, Set<Candidate>>();
//		
//		for (String title : systemResult.keySet()) {
//			Map<Integer, Set<Candidate>> filterMap = new HashMap<Integer, Set<Candidate>>();
//			Set<Candidate> cSet = new HashSet<Candidate>();
//			
//			for (Candidate sc : systemResult.get(title)) 
//			{
//				boolean matched = false;
//				for (Candidate gc : goldStandard.get(title)) 
//				{
//					if (filterMap.containsKey(gc.hashCode()) == false)
//						filterMap.put(gc.hashCode(), new HashSet<Candidate>());
//					
//					if (mm.match(sc.getMention(), gc.getMention()) == true) {
//						filterMap.get(gc.hashCode()).add(sc);
//						matched = true;
//						break;
//					}
//				}
//				
//				if (matched == false) {
//					if (filterMap.containsKey(new String("missed").hashCode()) == false)
//						filterMap.put(new String("missed").hashCode(), new HashSet<Candidate>());
//					
//					filterMap.get(new String("missed").hashCode()).add(sc);
//				}
//			}
//			//选择每个gold mention的多个system mention中长度最长的一个
//			for (Integer integer : filterMap.keySet()) {
//				if (integer.equals(new String("missed").hashCode()) == true)
//					cSet.addAll(filterMap.get(integer));
//				
//				else if (filterMap.get(integer).size() > 1) {
//					Candidate best = null;
//					for (Candidate c : filterMap.get(integer)) {
//						if (best == null || c.getMention().getSurfaceForm().length() > best.getMention().getSurfaceForm().length())
//							best = c;
//					}
//					cSet.add(best);
//				} 
//				else
//					cSet.addAll(filterMap.get(integer));
//			}
//			
//			resultMap.put(title, cSet);
//		}
//		
//		for (String title : systemResult.keySet()) {
//			systemResult.get(title).clear();
//			systemResult.get(title).addAll(resultMap.get(title));
//		}			
//	}
//	
//	/**
//	 * 
//	 * @param systemResult
//	 * @param goldStandard
//	 */
//	public void filterDuplicatedAnnotation(Map<String, Set<Annotation>> systemResult, Map<String, Set<Annotation>> goldStandard) {
//		Map<String, Set<Annotation>> resultMap = new HashMap<String, Set<Annotation>>();
//		
//		for (String title : systemResult.keySet()) {
//			Map<Integer, Set<Annotation>> filterMap = new HashMap<Integer, Set<Annotation>>();
//			Set<Annotation> aSet = new HashSet<Annotation>();
//			
//			for (Annotation sa : systemResult.get(title)) 
//			{
//				boolean matched = false;
//				for (Annotation ga : goldStandard.get(title)) 
//				{
//					if (filterMap.containsKey(ga.hashCode()) == false)
//						filterMap.put(ga.hashCode(), new HashSet<Annotation>());
//					
//					if (mm.match(sa.getMention(), ga.getMention()) == true) {
//						filterMap.get(ga.hashCode()).add(sa);
//						matched = true;
//						break;
//					}
//				}
//				
//				if (matched == false) {
//					if (filterMap.containsKey(new String("missed").hashCode()) == false)
//						filterMap.put(new String("missed").hashCode(), new HashSet<Annotation>());
//					
//					filterMap.get(new String("missed").hashCode()).add(sa);
//				}
//			}
//			
//			//选择每个gold mention的多个system mention中长度最长的一个
//			for (Integer integer : filterMap.keySet()) {
//				if (integer.equals(new String("missed").hashCode()) == true)
//					aSet.addAll(filterMap.get(integer));
//				
//				else if (filterMap.get(integer).size() > 1) {
//					Annotation best = null;
//					for (Annotation c : filterMap.get(integer)) {
//						if (best == null || c.getMention().getSurfaceForm().length() > best.getMention().getSurfaceForm().length())
//							best = c;
//					}
//					aSet.add(best);
//				} 
//				else
//					aSet.addAll(filterMap.get(integer));
//			}
//			
//			resultMap.put(title, aSet);
//		}
//		
//		for (String title : systemResult.keySet()) {
//			systemResult.get(title).clear();
//			systemResult.get(title).addAll(resultMap.get(title));
//		}			
//	}
//	
//	
//	public void filterDuplicatedNIL(Map<String, Set<NIL>> systemResult, Map<String, Set<NIL>> goldStandard) {
//		Map<String, Set<NIL>> resultMap = new HashMap<String, Set<NIL>>();
//		for (String title : systemResult.keySet()) {
//			Map<Integer, Set<NIL>> filterMap = new HashMap<Integer, Set<NIL>>();
//			Set<NIL> nSet = new HashSet<NIL>();
//			
//			for (NIL sn : systemResult.get(title)) 
//			{
//				boolean matched = false;
//				for (NIL gn : goldStandard.get(title)) 
//				{
//					if (filterMap.containsKey(gn.hashCode()) == false)
//						filterMap.put(gn.hashCode(), new HashSet<NIL>());
//					
//					if (mm.match(sn.getMention(), gn.getMention()) == true) {
//						filterMap.get(gn.hashCode()).add(sn);
//						matched = true;
//						break;
//					}
//				}
//				
//				if (matched == false) {
//					if (filterMap.containsKey(new String("missed").hashCode()) == false)
//						filterMap.put(new String("missed").hashCode(), new HashSet<NIL>());
//					
//					filterMap.get(new String("missed").hashCode()).add(sn);
//				}
//			}
//			
//			//选择每个gold mention的多个system mention中长度最长的一个
//			for (Integer integer : filterMap.keySet()) {
//				if (integer.equals(new String("missed").hashCode()) == true)
//					nSet.addAll(filterMap.get(integer));
//				
//				else if (filterMap.get(integer).size() > 1) {
//					NIL best = null;
//					for (NIL c : filterMap.get(integer)) {
//						if (best == null || c.getMention().getSurfaceForm().length() > best.getMention().getSurfaceForm().length())
//							best = c;
//					}
//					nSet.add(best);
//				} 
//				else
//					nSet.addAll(filterMap.get(integer));
//			}
//			
//			resultMap.put(title, nSet);
//		}
//		
//		for (String title : systemResult.keySet()) {
//			systemResult.get(title).clear();
//			systemResult.get(title).addAll(resultMap.get(title));
//		}			
//	}
//	
	
//	/**
//	 * 根据给的MentionMatching类过滤结果中有重叠的mention
//	 * 如果两个mention匹配，则选择长度大的mention
//	 * @param mentionMap
//	 */
//	public void mentionCoreference(Map<String, Set<Mention>> mentionMap) {
//		Map<String, Set<Mention>> resultMap = new HashMap<String, Set<Mention>>();
//		for (String title : mentionMap.keySet()) {
//			List<Mention> mentionList = new ArrayList<Mention>(mentionMap.get(title));
//			Set<Mention> mentionSet = new HashSet<Mention>();
////			if (mentionList.size() == 0) {
////				System.out.println(title);
////				System.out.println(mentionMap.get(title).isEmpty());
////			}
//			for (int i = 0; i < mentionList.size(); i++) {
//				Mention bestMention = mentionList.get(i);
////				if (bestMention.getSurfaceForm() == null || bestMention.getSurfaceForm().equals("null"))
////					System.out.println(title + " " + bestMention.getSurfaceForm() + " " + bestMention.getPosition() + " " + bestMention.getLength());
//				for (int j = 0; j < mentionList.size(); j++) {
//					if (coMention(bestMention, mentionList.get(j)) != null)
//						bestMention = coMention(bestMention, mentionList.get(j));
//				}
//				mentionSet.add(bestMention);
//			}
//			resultMap.put(title, mentionSet);
//		}
//		
//		for (String title : mentionMap.keySet()) {
//			mentionMap.get(title).clear();
//			mentionMap.get(title).addAll(resultMap.get(title));
//		}
//	}
//	
//	
//	/**
//	 * 根据给定的mention匹配方式，过滤结果中mention重叠的candidate
//	 * @param candidateMap
//	 */
//	public void candidateCoreference(Map<String, Set<Candidate>> candidateMap) {
//		Map<String, Set<Candidate>> resultMap = new HashMap<String, Set<Candidate>>();
//		for (String title : candidateMap.keySet()) {
//			List<Candidate> candidateList = new ArrayList<Candidate>(candidateMap.get(title));
//			Set<Candidate> candidateSet = new HashSet<Candidate>();
//			for (int i = 0; i < candidateList.size(); i++) {
//				Candidate bestCandidate = candidateList.get(i);
//				for (int j = 0; j < candidateList.size(); j++) {
//					if (coCandidate(bestCandidate, candidateList.get(j)) != null)
//						bestCandidate = coCandidate(bestCandidate, candidateList.get(j));
//				}
//				candidateSet.add(bestCandidate);
//			}
//			resultMap.put(title, candidateSet);
//		}
//		
//		for (String title : candidateMap.keySet()) {
//			candidateMap.get(title).clear();
//			candidateMap.get(title).addAll(resultMap.get(title));
//		}
//	}
//	
//	
//	public void annotationCoreference(Map<String, Set<Annotation>> annotationMap) {
//		Map<String, Set<Annotation>> resultMap = new HashMap<String, Set<Annotation>>();
//		for (String title : annotationMap.keySet()) {
//			List<Annotation> annotationList = new ArrayList<Annotation>(annotationMap.get(title));
//			Set<Annotation> annotationSet = new HashSet<Annotation>();
//			for (int i = 0; i < annotationList.size(); i++) {
//				Annotation bestAnnotation = annotationList.get(i);
//				for (int j = 0; j < annotationList.size(); j++) {
//					if (coAnnotation(bestAnnotation, annotationList.get(j)) != null)
//						bestAnnotation = coAnnotation(bestAnnotation, annotationList.get(j));
//				}
//				annotationSet.add(bestAnnotation);
//			}
//			resultMap.put(title, annotationSet);
//		}
//		for (String title : annotationMap.keySet()) {
//			annotationMap.get(title).clear();
//			annotationMap.get(title).addAll(resultMap.get(title));
//		}
//	}
//	
//	public void NILCoreference(Map<String, Set<NIL>> nILMap) {
//		Map<String, Set<NIL>> resultMap = new HashMap<String, Set<NIL>>();
//		for (String title : nILMap.keySet()) {
//			List<NIL> nILList = new ArrayList<NIL>(nILMap.get(title));
//			Set<NIL> nILSet = new HashSet<NIL>();
//			for (int i = 0; i < nILList.size(); i++) {
//				NIL bestNIL = nILList.get(i);
//				for (int j = 0; j < nILList.size(); j++) {
//					if (coNIL(bestNIL, nILList.get(j)) != null)
//						bestNIL = coNIL(bestNIL, nILList.get(j));
//				}
//				nILSet.add(bestNIL);
//			}
//			resultMap.put(title, nILSet);
//		}
//		for (String title : nILMap.keySet()) {
//			nILMap.get(title).clear();
//			nILMap.get(title).addAll(resultMap.get(title));
//		}
//	}
//	
//	
//	private Mention coMention(Mention m1, Mention m2) {
//		if (mm.match(m1, m2) == false)
//			return null;
//		if (m1.getLength() > m2.getLength())
//			return m1;
//		else
//			return m2;
//	}
//	
//	private Annotation coAnnotation(Annotation a1, Annotation a2) {
//		Mention m1 = a1.getMention();
//		Mention m2 = a2.getMention();
//		
//		if (coMention(m1, m2) == null)
//			return null;
//		if (coMention(m1, m2) == m1)
//			return a1;
//		else
//			return a2;
//	}
//	
//	private Candidate coCandidate(Candidate c1, Candidate c2) {
//		Mention m1 = c1.getMention();
//		Mention m2 = c2.getMention();
//		
//		if (coMention(m1, m2) == null)
//			return null;
//		if (coMention(m1, m2) == m1)
//			return c1;
//		else
//			return c2;
//	}
//	
//	private NIL coNIL(NIL n1, NIL n2) {
//		Mention m1 = n1.getMention();
//		Mention m2 = n2.getMention();
//		
//		if (mm.match(m1, m2) == false)
//			return null;
//		if (m1.getLength() > m2.getLength())
//			return n1;
//		else 
//			return n2;
//	}
	
}
