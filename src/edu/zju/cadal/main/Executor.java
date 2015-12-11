package edu.zju.cadal.main;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.zju.cadal.cache.EvaluationResult;
import edu.zju.cadal.cache.SystemResult;
import edu.zju.cadal.cache.SystemResultDumper;
import edu.zju.cadal.dataset.AbstractDataset;
import edu.zju.cadal.exception.UnknowMatchingException;
import edu.zju.cadal.matching.AnnotationMatching;
import edu.zju.cadal.matching.CandidateMatching;
import edu.zju.cadal.matching.Evaluation;
import edu.zju.cadal.matching.Matching;
import edu.zju.cadal.matching.MentionMatching;
import edu.zju.cadal.matching.NILMatching;
import edu.zju.cadal.system.AbstractERDSystem;
import edu.zju.cadal.utils.Pair;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月14日
 */
public class Executor<T> {
	
	private static float THRESHOLD_STEP = 1f/128;
	
	public static <T> void run(AbstractDataset ds, AbstractERDSystem s, Matching<T> m, String outputFile) {
		SystemResult result = s.erd(ds);
		evaluate(result, ds, s, m, outputFile);
	}
	
	private static <T> void evaluate(SystemResult result, AbstractDataset ds, AbstractERDSystem s, Matching<T> m, String outputFile) {
		
		//设置结果是否输出到文件
		try {
			if (outputFile != null)
				SystemResultDumper.setPrintStream(new PrintStream(outputFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		if (m instanceof MentionMatching) {
			MentionMatching mwm = (MentionMatching)m;
			
			EvaluationResult evaluationResult =
						Evaluation.getResult(
						result.getMentionCache(s.getName(), ds.getName()), //选择全部的输出结果
						ds.getGoldMention(), 
						mwm);				
			System.out.println(evaluationResult);
			evaluationResult.detailPRF();
			SystemResultDumper.compare(result, ds, s, mwm);
		}
		else if (m instanceof CandidateMatching) {
			CandidateMatching cwm = (CandidateMatching)m;
			
			EvaluationResult evaluationResult = Evaluation.getResult(
					result.getCandidateCache(s.getName(), ds.getName()), 
					ds.getGoldCandidate(), 
					cwm);
			System.out.println(evaluationResult);
			evaluationResult.detailPRF();
			SystemResultDumper.compare(result, ds, s, cwm);				
		}
		else if (m instanceof AnnotationMatching) {
			AnnotationMatching awm = (AnnotationMatching)m;
			Map<Float, EvaluationResult> evaluationResult = new HashMap<Float, EvaluationResult>();
			
			for (float threshold = 0; threshold <= 1; threshold += THRESHOLD_STEP) {
				evaluationResult.put(threshold, Evaluation.getResult(
//						result.getAnnotationCache(s.getName(), ds.getName()),
						result.getAnnotationCache(s.getName(), ds.getName(), threshold), 
						ds.getGoldAnnotation(),
						awm));
			}
			for (Float key : evaluationResult.keySet()) {
				System.out.println(key + " " + evaluationResult.get(key).getMicroF1());
			}
			Pair<Float, EvaluationResult> bestResult = getBestResult(evaluationResult);
			System.out.println(bestResult.second);
			bestResult.second.detailPRF();
			SystemResultDumper.compare(result, ds, s, awm);				
		}
		else if (m instanceof NILMatching) {
			NILMatching nfm = (NILMatching)m;
			Map<Float, EvaluationResult> evaluationResult = new HashMap<Float, EvaluationResult>();
			
			for (float threshold = 0; threshold <= 1; threshold += THRESHOLD_STEP) {
				evaluationResult.put(threshold, Evaluation.getResult(
//						result.getNILCache(s.getName(), ds.getName()),
						result.getNILCache(s.getName(), ds.getName(), threshold), 
						ds.getGoldNIL(), 
						nfm));
			}
			Pair<Float, EvaluationResult> bestResult = getBestResult(evaluationResult);
			System.out.println(bestResult.second);
			bestResult.second.detailPRF();
			SystemResultDumper.compare(result, ds, s, nfm);
		}
		else {
			throw new UnknowMatchingException();
		}
	}
	
	/**
	 * 取得最好的threshold以及对应的最好的结果
	 * @param resultMap
	 * @return
	 */
	private static Pair<Float, EvaluationResult> getBestResult(Map<Float, EvaluationResult> resultMap) {
		List<Float> thresholds = new ArrayList<Float>(resultMap.keySet());
		Collections.sort(thresholds);
		Pair<Float, EvaluationResult> bestResult = null;
		for (Float t : thresholds) 
			if (bestResult == null || resultMap.get(t).getMacroF1() > bestResult.second.getMacroF1())
				bestResult = new Pair<Float, EvaluationResult>(t, resultMap.get(t));
		
		return bestResult;
	}
}
