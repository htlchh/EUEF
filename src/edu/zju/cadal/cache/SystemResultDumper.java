package edu.zju.cadal.cache;

import java.io.PrintStream;
import java.util.Map;
import java.util.Set;

import edu.zju.cadal.dataset.AbstractDataset;
import edu.zju.cadal.matching.AnnotationExactMatching;
import edu.zju.cadal.matching.AnnotationFuzzyMatching;
import edu.zju.cadal.matching.CandidateExactMatching;
import edu.zju.cadal.matching.CandidateFuzzyMatching;
import edu.zju.cadal.matching.Matching;
import edu.zju.cadal.matching.MentionExactMatching;
import edu.zju.cadal.matching.MentionFuzzyMatching;
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
	
	public static <T> void compare(SystemResult result, AbstractDataset ds, AbstractERDSystem s, Matching<T> m) {
		Map<String, String> rawTextMap = ds.getRawText();
		
		if (m instanceof MentionExactMatching) {
			MentionExactMatching msm = (MentionExactMatching)m;
			Map<String, Set<Mention>> systemMentionMap = result.getMentionCache(s.getName(), ds.getName());
			Map<String, Set<Mention>> goldMentionMap = ds.getGoldMention();
			
			Map<String, Set<Mention>> filterSystemMentionMap = msm.preprocessSystemResult(systemMentionMap);
			Map<String, Set<Mention>> filterGoldMentionMap = msm.preprocessGoldStandard(goldMentionMap);
			for (String title : rawTextMap.keySet())
				compare(
						title, 
						rawTextMap.get(title), 
						filterGoldMentionMap.get(title), 
						filterSystemMentionMap.get(title), 
						msm);			
		}
		
		if (m instanceof MentionFuzzyMatching) {
			MentionFuzzyMatching mwm = (MentionFuzzyMatching)m;
			Map<String, Set<Mention>> systemMentionMap = result.getMentionCache(s.getName(), ds.getName());
			Map<String, Set<Mention>> goldMentionMap = ds.getGoldMention();
			
			Map<String, Set<Mention>> filterSystemMentionMap = mwm.preprocessSystemResult(systemMentionMap);
			Map<String, Set<Mention>> filterGoldMentionMap = mwm.preprocessGoldStandard(goldMentionMap);
			for (String title : rawTextMap.keySet())
				compare(
						title, 
						rawTextMap.get(title), 
						filterGoldMentionMap.get(title), 
						filterSystemMentionMap.get(title), 
						mwm);				
		}
		
		if (m instanceof AnnotationExactMatching) {
			AnnotationExactMatching asm = (AnnotationExactMatching)m;
			Map<String, Set<Annotation>> systemAnnotationMap = result.getAnnotationCache(s.getName(), ds.getName());
			Map<String, Set<Annotation>> goldAnnotationMap = ds.getGoldAnnotation();
			
			Map<String, Set<Annotation>> filterSystemAnnotationMap = asm.preprocessSystemResult(systemAnnotationMap);
			Map<String, Set<Annotation>> filterGoldAnnotationMap = asm.preprocessGoldStandard(goldAnnotationMap);
			for (String title : rawTextMap.keySet()) {
				compare(title, rawTextMap.get(title), filterGoldAnnotationMap.get(title), filterSystemAnnotationMap.get(title), asm);
			}
		}
		
		if (m instanceof AnnotationFuzzyMatching) {
			AnnotationFuzzyMatching awm = (AnnotationFuzzyMatching)m;
			Map<String, Set<Annotation>> systemAnnotationMap = result.getAnnotationCache(s.getName(), ds.getName());
			Map<String, Set<Annotation>> goldAnnotationMap = ds.getGoldAnnotation();
			
			Map<String, Set<Annotation>> filterSystemAnnotationMap = awm.preprocessSystemResult(systemAnnotationMap);
			Map<String, Set<Annotation>> filterGoldAnnotationMap = awm.preprocessGoldStandard(goldAnnotationMap);
			for (String title : rawTextMap.keySet()) {
				compare(title, rawTextMap.get(title), filterGoldAnnotationMap.get(title), filterSystemAnnotationMap.get(title), awm);
			}			
		}
		
		if (m instanceof CandidateExactMatching) {
			CandidateExactMatching csm = (CandidateExactMatching)m;
			Map<String, Set<Candidate>> systemCandidateMap = result.getCandidateCache(s.getName(), ds.getName());
			Map<String, Set<Candidate>> goldCandidateMap = ds.getGoldCandidate();
			
			Map<String, Set<Candidate>> filterSystemCandidateMap = csm.preprocessSystemResult(systemCandidateMap);
			Map<String, Set<Candidate>> filterGoldCandidateMap = csm.preprocessGoldStandard(goldCandidateMap);
			for (String title : rawTextMap.keySet())
				compare(title, rawTextMap.get(title), filterGoldCandidateMap.get(title), filterSystemCandidateMap.get(title), csm);
		}
		
		if (m instanceof CandidateFuzzyMatching) {
			CandidateFuzzyMatching cwm = (CandidateFuzzyMatching)m;
			Map<String, Set<Candidate>> systemCandidateMap = result.getCandidateCache(s.getName(), ds.getName());
			Map<String, Set<Candidate>> goldCandidateMap = ds.getGoldCandidate();
			
			Map<String, Set<Candidate>> filterSystemCandidateMap = cwm.preprocessSystemResult(systemCandidateMap);
			Map<String, Set<Candidate>> filterGoldCandidateMap = cwm.preprocessGoldStandard(goldCandidateMap);
			for (String title : rawTextMap.keySet())
				compare(title, rawTextMap.get(title), filterGoldCandidateMap.get(title), filterSystemCandidateMap.get(title), cwm);
		}

	}

	public static <T> void compare(
			String title, 
			String text, 
			Set<T> gold,
			Set<T> systemResult,
			Matching<T> m) 
	{
		float fn = 0;
		float tp = 0;
		float fp = 0;
		
		//out.println("********************** " + title + " *****************");
		//out.println(text);
		//out.println("******************************************************");
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
				printMention(g, text, note);
			else if (g instanceof Candidate)
				printCandidate(g, text, note);
			else if (g instanceof Annotation)
				printAnnotation(g, text, note);
			else if (g instanceof NIL)
				printNIL(g, text, note);
			else if (g instanceof Entity)
				printEntity(g, text, note);
			else ;
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
				printMention(s, text, note);
			else if (s instanceof Candidate)
				printCandidate(s, text, note);
			else if (s instanceof Annotation)
				printAnnotation(s, text, note);
			else if (s instanceof NIL)
				printNIL(s, text, note);
			else if (s instanceof Entity)
				printEntity(s, text, note);
			else ;			
		}
		
		float p = tp + fp == 0 ? 1 : tp / (tp + fp);
		float r = tp + fn == 0 ? 1 : tp / (tp + fn);
		float f1 = p + r == 0 ? 0 : 2 * p * r / (p + r);
		out.println("precision:" + p + "; recall:" + r + "; f1:" + f1 + "; tpCount:" + tp + "; fpCount:" + fp + "; fnCount:" + fn);
	}
	
	
	private static <T> void printMention(T t, String text, String note) {
		Mention m = (Mention)t;
		out.printf("%s => %s:[%d,%d]; score:%f%n", 
				note, 
				text.substring(m.getPosition(), m.getPosition()+m.getLength()), 
				m.getPosition(),
				m.getPosition()+m.getLength(),
				m.getScore());
	}
	
	private static <T> void printCandidate(T t, String text, String note) {
		Candidate c = (Candidate)t;
		int p = c.getMention().getPosition();
		int l = c.getMention().getLength();
		int e = p + l;
		float score = c.getMention().getScore();
		out.printf("%s => %s:[%d,%d]; score:%f%n", 
				note,
				text.substring(p, e),
				p,
				l,
				score
				);
	}
	
	private static <T> void printAnnotation(T t, String text, String note) {
		Annotation a = (Annotation)t;
		int s = a.getMention().getPosition();
		int e = a.getMention().getLength() + a.getMention().getPosition();
		String mention = text.substring(s, e);
		float score = a.getMention().getScore();
		out.printf("%s => %s:[%d,%d]; score:%f; title:%s; score:%f%n", 
							note, 
							mention, 
							s,
							e,
							score, 
							a.getEntity().getTitle(), 
							a.getScore());
	}
	
	private static <T> void printNIL(T t, String text, String note) {
		
	}
	
	private static <T> void printEntity(T t, String text, String note) {
		
	}
}
