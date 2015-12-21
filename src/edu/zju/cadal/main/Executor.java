package edu.zju.cadal.main;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.zju.cadal.cache.PRF;
import edu.zju.cadal.cache.Prediction;
import edu.zju.cadal.cache.PredictionDumper;
import edu.zju.cadal.dataset.AbstractDataset;
import edu.zju.cadal.exception.UnknowMatchingException;
import edu.zju.cadal.matching.AnnotationMatching;
import edu.zju.cadal.matching.CandidateMatching;
import edu.zju.cadal.matching.Matching;
import edu.zju.cadal.matching.MentionMatching;
import edu.zju.cadal.matching.NILMatching;
import edu.zju.cadal.model.Annotation;
import edu.zju.cadal.model.Candidate;
import edu.zju.cadal.model.Mention;
import edu.zju.cadal.model.NIL;
import edu.zju.cadal.system.AbstractERDSystem;
import edu.zju.cadal.utils.Pair;


public class Executor<T> {
	
	/** threshold iterative step */
	private static float THRESHOLD_STEP = 1.1f;
	private static boolean printDetail = true;
	private static boolean iteration = false;
	private static Map<Float, PRF> prfMap = new HashMap<Float, PRF>();
	
	
	/**
	 * @param printDetail
	 * @param iteration
	 * @param thresholdStep
	 */
	public static void setExecutor(boolean printDetail, boolean iteration, float thresholdStep) {
		Executor.printDetail = printDetail;
		Executor.iteration = iteration;
		Executor.THRESHOLD_STEP = thresholdStep;
	}
	
	/**  
	 * Get PRF of a specific threshold
	 * */
	public static Map<Float, PRF> getIterativePRF() {
		if (iteration == false) {
			throw new RuntimeException("Iteration is forbidden. Please open the iteration.");
		}
		return prfMap;
	}
	
	public static <T> void run(AbstractDataset ds, AbstractERDSystem s, Matching<T> m, String outputFile) {
		Prediction prediction = s.erd(ds);
		evaluate(prediction, ds, s, m, outputFile);
	}
	
	/**
	 * Evaluate the prediction compared with the given gold standards.
	 * 
	 * @param prediction
	 * @param ds
	 * @param s
	 * @param m
	 * @param outputFile
	 *  + print in screen if null
	 *  + output to a specified file
	 */
	private static <T> void evaluate(Prediction prediction, AbstractDataset ds, AbstractERDSystem s, Matching<T> m, String outputFile) {
		
		/** check if output to file */
		try {
			if (outputFile != null)
				PredictionDumper.setPrintStream(new PrintStream(outputFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		if (m instanceof MentionMatching) {
			MentionMatching mwm = (MentionMatching)m;
			Map<String, Set<Mention>> pMention = prediction.getMentionCache(s.getName(), ds.getName());
			Map<String, Set<Mention>> gMention = ds.getGoldMention();
			
			PRF prf = Evaluator.getResult(pMention,	gMention, mwm);				
			System.out.println(prf);
			if (printDetail) prf.detailPRF();
			PredictionDumper.compare(pMention, gMention, mwm);
		}
		else if (m instanceof CandidateMatching) {
			CandidateMatching cwm = (CandidateMatching)m;
			Map<String, Set<Candidate>> pCandidate = prediction.getCandidate(s.getName(), ds.getName());
			Map<String, Set<Candidate>> gCandidate = ds.getGoldCandidate();
			
			PRF prf = Evaluator.getResult(pCandidate, gCandidate, cwm);
			System.out.println(prf);
			if (printDetail) prf.detailPRF();
			PredictionDumper.compare(pCandidate, gCandidate, cwm);
		}
		else if (m instanceof AnnotationMatching) {
			AnnotationMatching awm = (AnnotationMatching)m;
			Map<String, Set<Annotation>> gAnnotation = ds.getGoldAnnotation();
			
			for (float threshold = 0; threshold <= 1; threshold += THRESHOLD_STEP)
				prfMap.put(threshold, Evaluator.getResult(
						prediction.getAnnotation(s.getName(), ds.getName(), threshold), 
						gAnnotation,
						awm));
			Pair<Float, PRF> best = bestPRF(prfMap);
			System.out.println(best.second);
			if (printDetail) best.second.detailPRF();
			PredictionDumper.compare(prediction.getAnnotation(s.getName(), ds.getName(), best.first), gAnnotation, awm);
		}
		else if (m instanceof NILMatching) {
			NILMatching nfm = (NILMatching)m;
			Map<String, Set<NIL>> goldNIL = ds.getGoldNIL();
			
			for (float threshold = 0; threshold <= 1; threshold += THRESHOLD_STEP)
				prfMap.put(threshold, Evaluator.getResult(
						prediction.getNIL(s.getName(), ds.getName(), threshold), 
						goldNIL, 
						nfm));
			Pair<Float, PRF> best = bestPRF(prfMap);
			System.out.println(best.second);
			if (printDetail) best.second.detailPRF();
			PredictionDumper.compare(prediction.getNIL(s.getName(), ds.getName(), best.first), goldNIL, nfm);
		}
		else {
			throw new UnknowMatchingException();
		}
	}
	
	/**
	 * Find the best PRF
	 */
	private static Pair<Float, PRF> bestPRF(Map<Float, PRF> prfMap) {
		List<Float> thresholds = new ArrayList<Float>(prfMap.keySet());
		Collections.sort(thresholds);
		Pair<Float, PRF> bestResult = null;
		for (Float t : thresholds) 
			if (bestResult == null || prfMap.get(t).getMacroF1() > bestResult.second.getMacroF1())
				bestResult = new Pair<Float, PRF>(t, prfMap.get(t));
		return bestResult;
	}
}
