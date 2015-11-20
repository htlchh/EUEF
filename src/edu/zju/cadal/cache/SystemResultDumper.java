package edu.zju.cadal.cache;

import java.io.PrintStream;
import java.util.Map;
import java.util.Set;

import edu.zju.cadal.dataset.AbstractDataset;
import edu.zju.cadal.exception.UnknowMatchingException;
import edu.zju.cadal.matching.AnnotationExactMatching;
import edu.zju.cadal.matching.AnnotationFuzzyMatching;
import edu.zju.cadal.matching.CandidateExactMatching;
import edu.zju.cadal.matching.CandidateFuzzyMatching;
import edu.zju.cadal.matching.Matching;
import edu.zju.cadal.matching.MentionExactMatching;
import edu.zju.cadal.matching.MentionFuzzyMatching;
import edu.zju.cadal.matching.NILExactMathcing;
import edu.zju.cadal.matching.NILFuzzyMatching;
import edu.zju.cadal.model.Annotation;
import edu.zju.cadal.model.Candidate;
import edu.zju.cadal.model.Entity;
import edu.zju.cadal.model.Mention;
import edu.zju.cadal.model.NIL;
import edu.zju.cadal.system.AbstractERDSystem;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月16日
 */
public class SystemResultDumper<T> {
	
	private static PrintStream out = System.out;
	public static void setPrintStream(PrintStream out) {
		SystemResultDumper.out = out;
	}
	
	public static <T> void compare(SystemResult result, AbstractDataset ds, AbstractERDSystem s, Matching<T> m) throws UnknowMatchingException {
		Map<String, String> rawTextMap = ds.getRawText();
		
//		if (m instanceof MentionExactMatching) {
//			MentionExactMatching msm = (MentionExactMatching)m;
//			Map<String, Set<Mention>> systemMentionMap = result.getMentionCache(s.getName(), ds.getName());
//			Map<String, Set<Mention>> goldMentionMap = ds.getGoldMention();
//			
//			for (String title : rawTextMap.keySet())
//				compare(
//						title, 
//						systemMentionMap.get(title), 
//						goldMentionMap.get(title), 
//						msm);			
//		}
		
		if (m instanceof MentionFuzzyMatching) {
			MentionFuzzyMatching mwm = (MentionFuzzyMatching)m;
			Map<String, Set<Mention>> systemMentionMap = result.getMentionCache(s.getName(), ds.getName());
			Map<String, Set<Mention>> goldMentionMap = ds.getGoldMention();
			
			for (String title : rawTextMap.keySet()) {
				compare(
						title, 
						systemMentionMap.get(title), 
						goldMentionMap.get(title), 
						mwm);		
			}
		}
		
//		else if (m instanceof AnnotationExactMatching) {
//			AnnotationExactMatching asm = (AnnotationExactMatching)m;
//			Map<String, Set<Annotation>> systemAnnotationMap = result.getAnnotationCache(s.getName(), ds.getName());
//			Map<String, Set<Annotation>> goldAnnotationMap = ds.getGoldAnnotation();
//			
//			for (String title : rawTextMap.keySet()) {
//				compare(title, systemAnnotationMap.get(title), goldAnnotationMap.get(title), asm);
//			}
//		}
		
		else if (m instanceof AnnotationFuzzyMatching) {
			AnnotationFuzzyMatching awm = (AnnotationFuzzyMatching)m;
			Map<String, Set<Annotation>> systemAnnotationMap = result.getAnnotationCache(s.getName(), ds.getName());
			Map<String, Set<Annotation>> goldAnnotationMap = ds.getGoldAnnotation();
			
			for (String title : rawTextMap.keySet()) {
				compare(title, systemAnnotationMap.get(title), goldAnnotationMap.get(title), awm);
			}			
		}
		
//		else if (m instanceof CandidateExactMatching) {
//			CandidateExactMatching csm = (CandidateExactMatching)m;
//			Map<String, Set<Candidate>> systemCandidateMap = result.getCandidateCache(s.getName(), ds.getName());
//			Map<String, Set<Candidate>> goldCandidateMap = ds.getGoldCandidate();
//			
//			for (String title : rawTextMap.keySet())
//				compare(title, systemCandidateMap.get(title), goldCandidateMap.get(title), csm);
//		}
		
		else if (m instanceof CandidateFuzzyMatching) {
			CandidateFuzzyMatching cwm = (CandidateFuzzyMatching)m;
			Map<String, Set<Candidate>> systemCandidateMap = result.getCandidateCache(s.getName(), ds.getName());
			Map<String, Set<Candidate>> goldCandidateMap = ds.getGoldCandidate();
			
			for (String title : rawTextMap.keySet())
				compare(title, systemCandidateMap.get(title), goldCandidateMap.get(title), cwm);
		} 
//		else if (m instanceof NILExactMathcing) {
//			NILExactMathcing nem = (NILExactMathcing)m;
//			Map<String, Set<NIL>> systemNILMap = result.getNILCache(s.getName(), ds.getName());
//			Map<String, Set<NIL>> goldNILMap = ds.getGoldNIL();
//			
//			for (String title : rawTextMap.keySet()) 
//				compare(title, systemNILMap.get(title), goldNILMap.get(title), nem);
//		}
		else if (m instanceof NILFuzzyMatching) {
			NILFuzzyMatching nfm = (NILFuzzyMatching)m;
			Map<String, Set<NIL>> systemNILMap = result.getNILCache(s.getName(), ds.getName());
			Map<String, Set<NIL>> goldNILMap = ds.getGoldNIL();
			
			for (String title : rawTextMap.keySet()) 
				compare(title, systemNILMap.get(title), goldNILMap.get(title), nfm);
		}
		else {
			throw new UnknowMatchingException();
		}

	}

	private static <T> void compare(
			String title, 
			Set<T> systemResult,
			Set<T> gold,
			Matching<T> m) 
	{
		float fn = 0;
		float tp = 0;
		float fp = 0;
		
		out.println("[" + title + "]");
		out.println("***************** Gold Standard: ***********************");
		for (T g : gold) {
			boolean matched = false;
			String note = "\tFN";
			for (T s : systemResult) 
				if (m.match(g, s) == true) {
					note = "";
					matched = true;
				}
			if (matched == true)
				tp++;
			else
				fn++;
			if (g instanceof Mention)
				printMention(g, note);
			else if (g instanceof Candidate)
				printCandidate(g, note);
			else if (g instanceof Annotation)
				printAnnotation(g, note);
			else if (g instanceof NIL)
				printNIL(g, note);
			else if (g instanceof Entity)
				printEntity(g, note);
			else 
				throw new UnknowMatchingException();
				;
		}
		
		out.println("\n************************ System Result: ***************************");
		for (T s : systemResult) {
			boolean matched = false;
			String note = "\tFP";
			for (T g : gold) 
				if (m.match(g, s) == true) {
					note = "";
					matched = true;
				}
			if (matched == false)
				fp++;
			if (s instanceof Mention)
				printMention(s, note);
			else if (s instanceof Candidate)
				printCandidate(s, note);
			else if (s instanceof Annotation)
				printAnnotation(s, note);
			else if (s instanceof NIL)
				printNIL(s, note);
			else if (s instanceof Entity)
				printEntity(s, note);
			else 
				throw new UnknowMatchingException();			
		}
		
		float p = tp + fp == 0 ? 1 : tp / (tp + fp);
		float r = tp + fn == 0 ? 1 : tp / (tp + fn);
		float f1 = p + r == 0 ? 0 : 2 * p * r / (p + r);
		out.println("precision:" + p + "; recall:" + r + "; f1:" + f1 + "; tpCount:" + tp + "; fpCount:" + fp + "; fnCount:" + fn);
	}
	
	
	private static <T> void printMention(T t, String note) {
		Mention m = (Mention)t;
		out.printf("%s => %s:[%d,%d]; score:%f%n", 
				note, 
				m.getSurfaceForm(), 
				m.getPosition(),
				m.getPosition()+m.getLength(),
				m.getScore());
	}
	
	private static <T> void printCandidate(T t, String note) {
		Candidate c = (Candidate)t;
		int p = c.getMention().getPosition();
		int l = c.getMention().getLength();
		float score = c.getMention().getScore();
		out.printf("%s => %s:[%d,%d]; score:%f%n", 
				note,
				c.getMention().getSurfaceForm(),
				p,
				l,
				score
				);
	}
	
	private static <T> void printAnnotation(T t, String note) {
		Annotation a = (Annotation)t;
		int s = a.getMention().getPosition();
		int e = a.getMention().getLength() + a.getMention().getPosition();
		float score = a.getMention().getScore();
		out.printf("%s => %s:[%d,%d]; score:%f; title:%s; score:%f%n", 
							note, 
							a.getMention().getSurfaceForm(), 
							s,
							e,
							score, 
							a.getEntity().getTitle(), 
							a.getScore());
	}
	
	private static <T> void printNIL(T t, String note) {
		NIL n = (NIL)t;
		int s = n.getMention().getPosition();
		int e = n.getMention().getLength() + n.getMention().getPosition();
		float score = n.getMention().getScore();
		out.printf("%s => %s:[%d,%d] score:%f; title:%s; score:%f%n", 
				note,
				n.getMention().getSurfaceForm(),
				s,
				e,
				score,
				n.getEntity().getTitle(),
				n.getScore());
	}
	
	private static <T> void printEntity(T t, String note) {
		
	}
}
