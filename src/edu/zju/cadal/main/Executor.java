package edu.zju.cadal.main;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Map;
import java.util.Set;

import edu.zju.cadal.cache.EvaluationResult;
import edu.zju.cadal.cache.SystemResult;
import edu.zju.cadal.cache.SystemResultDumper;
import edu.zju.cadal.dataset.AbstractDataset;
import edu.zju.cadal.matching.AnnotationExactMatching;
import edu.zju.cadal.matching.AnnotationFuzzyMatching;
import edu.zju.cadal.matching.CandidateExactMatching;
import edu.zju.cadal.matching.CandidateFuzzyMatching;
import edu.zju.cadal.matching.Evaluation;
import edu.zju.cadal.matching.Matching;
import edu.zju.cadal.matching.MentionExactMatching;
import edu.zju.cadal.matching.MentionFuzzyMatching;
import edu.zju.cadal.model.Candidate;
import edu.zju.cadal.model.Mention;
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
		
		/** mention模糊匹配 */
		if (m instanceof MentionFuzzyMatching) {
			MentionFuzzyMatching mwm = (MentionFuzzyMatching)m;
			EvaluationResult evaluationResult = Evaluation.getResult(
					result.getMentionCache(s.getName(), ds.getName()), 
					ds.getGoldMention(), 
					mwm);			
			System.out.println(evaluationResult);
			evaluationResult.detailPRF();
			SystemResultDumper.compare(result, ds, s, mwm);
		}
		
		/** mention强匹配 */
		if (m instanceof MentionExactMatching) {
			MentionExactMatching msm = (MentionExactMatching)m;
			EvaluationResult evaluationResult = Evaluation.getResult(
					result.getMentionCache(s.getName(), ds.getName()), 
					ds.getGoldMention(), 
					msm);			
			System.out.println(evaluationResult);
			evaluationResult.detailPRF();
			SystemResultDumper.compare(result, ds, s, msm);			
		}
		
		/** candidate 模糊匹配 */
		if (m instanceof CandidateFuzzyMatching) {
			CandidateFuzzyMatching cwm = (CandidateFuzzyMatching)m;
			EvaluationResult evaluationResult = Evaluation.getResult(
					result.getCandidateCache(s.getName(), ds.getName()), 
					ds.getGoldCandidate(), 
					cwm);
			System.out.println(evaluationResult);
			evaluationResult.detailPRF();
			SystemResultDumper.compare(result, ds, s, cwm);				
		}
		
		/** candidate 强匹配 */
		if (m instanceof CandidateExactMatching) {
			CandidateExactMatching csm = (CandidateExactMatching)m;
			EvaluationResult evaluationResult = Evaluation.getResult(
					result.getCandidateCache(s.getName(), ds.getName()), 
					ds.getGoldCandidate(), 
					csm);
			System.out.println(evaluationResult);
			evaluationResult.detailPRF();
			SystemResultDumper.compare(result, ds, s, csm);				
		}		
		
		/** annotation 模糊匹配 */
		if (m instanceof AnnotationFuzzyMatching) {
			AnnotationFuzzyMatching awm = (AnnotationFuzzyMatching)m;
			EvaluationResult evaluationResult = Evaluation.getResult(
					result.getAnnotationCache(s.getName(), ds.getName()), 
					ds.getGoldAnnotation(),
					awm);
			System.out.println(evaluationResult);
			evaluationResult.detailPRF();
			SystemResultDumper.compare(result, ds, s, awm);				
		}
		
		/** annotation 强匹配 */
		if (m instanceof AnnotationExactMatching) {
			AnnotationExactMatching asm = (AnnotationExactMatching)m;
			EvaluationResult evaluationResult = Evaluation.getResult(
					result.getAnnotationCache(s.getName(), ds.getName()), 
					ds.getGoldAnnotation(),
					asm);
			System.out.println(evaluationResult);
			evaluationResult.detailPRF();
			SystemResultDumper.compare(result, ds, s, asm);				
		}
	}
}