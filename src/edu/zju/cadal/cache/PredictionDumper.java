package edu.zju.cadal.cache;

import java.io.PrintStream;
import java.util.Map;
import java.util.Set;

import edu.zju.cadal.exception.UnknowMatchingException;
import edu.zju.cadal.matching.Matching;
import edu.zju.cadal.model.Annotation;
import edu.zju.cadal.model.Candidate;
import edu.zju.cadal.model.Mention;
import edu.zju.cadal.model.NIL;

/**
 * PredictionDumper class is used to dump predictions generated by ERD systems.
 * 
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月16日
 */
public class PredictionDumper<T> {
	
	/** The output stream, the default output stream is the screen */
	private static PrintStream out = System.out;
	public static void setPrintStream(PrintStream out) {
		PredictionDumper.out = out;
	}
	
	public static <T> void compare(Map<String, Set<T>> prediction, Map<String, Set<T>> goldStandard, Matching<T> m) {
		for (String title : prediction.keySet()) {
			compare(title, prediction.get(title), goldStandard.get(title), m);
		}
	}


	private static <T> void compare(String title, Set<T> prediction, Set<T> gold, Matching<T> m) {
		float fn = 0;
		float tp = 0;
		float fp = 0;
		
		out.println("[" + title + "]");
		out.println("***************** Gold Standard: ***********************");
		for (T g : gold) {
			boolean matched = false;
			String note = "\tFN";
			for (T s : prediction) 
				if (m.match(s, g) == true) {
					note = "";
					matched = true;
					break;
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
			else 
				throw new UnknowMatchingException();
				;
		}
		
		out.println("\n************************ System Result: ***************************");
		for (T s : prediction) {
			boolean matched = false;
			String note = "\tFP";
			for (T g : gold) 
				if (m.match(s, g) == true) {
					note = "";
					matched = true;
					break;
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
			else 
				throw new UnknowMatchingException();			
		}
		
		float p = tp + fp == 0 ? 1 : tp / (tp + fp);
		float r = tp + fn == 0 ? 1 : tp / (tp + fn);
		float f1 = p + r == 0 ? 0 : 2 * p * r / (p + r);
		out.println("P/R/F:" + p + "/" + r + "/" + f1 + "; tpCount:" + tp + "; fpCount:" + fp + "; fnCount:" + fn);
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
}
