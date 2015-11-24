package edu.zju.cadal.main;

import java.io.FileNotFoundException;
import java.io.PrintStream;

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

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月14日
 */
public class Executor<T> {
	
	
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
			EvaluationResult evaluationResult = Evaluation.getResult(
					result.getMentionCache(s.getName(), ds.getName()), 
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
			EvaluationResult evaluationResult = Evaluation.getResult(
					result.getAnnotationCache(s.getName(), ds.getName()), 
					ds.getGoldAnnotation(),
					awm);
			System.out.println(evaluationResult);
			evaluationResult.detailPRF();
			SystemResultDumper.compare(result, ds, s, awm);				
		}
		else if (m instanceof NILMatching) {
			NILMatching nfm = (NILMatching)m;
			EvaluationResult evaluationResult = Evaluation.getResult(
					result.getNILCache(s.getName(), ds.getName()), 
					ds.getGoldNIL(), 
					nfm);
			System.out.println(evaluationResult);
			evaluationResult.detailPRF();
			SystemResultDumper.compare(result, ds, s, nfm);
		}
		else {
			throw new UnknowMatchingException();
		}
	}
}
