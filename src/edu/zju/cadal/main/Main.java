package edu.zju.cadal.main;

import edu.zju.cadal.dataset.MSNBC;
import edu.zju.cadal.matching.AnnotationExactMatching;
import edu.zju.cadal.matching.AnnotationFuzzyMatching;
import edu.zju.cadal.matching.CandidateExactMatching;
import edu.zju.cadal.matching.CandidateFuzzyMatching;
import edu.zju.cadal.matching.MentionExactMatching;
import edu.zju.cadal.matching.MentionFuzzyMatching;
import edu.zju.cadal.system.Priorer;
import edu.zju.cadal.system.Spotlight;
import edu.zju.cadal.system.WikiMiner;
import edu.zju.cadal.system.Wikifier;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月17日
 */
public class Main {

	public static void main(String[] args) {
		WikiMiner wm = new WikiMiner(true);
		Wikifier wf = new Wikifier(true);
		Spotlight spotlight = new Spotlight(true);
		Priorer priorer = new Priorer(true);
		
		MSNBC msnbc = new MSNBC(
				"dataset/MSNBC/RawTextsSimpleChars_utf8", 
				"dataset/MSNBC/Problems");
		
		
		MentionExactMatching msm = new MentionExactMatching();
		MentionFuzzyMatching mwm = new MentionFuzzyMatching();
		CandidateExactMatching csm = new CandidateExactMatching();
		CandidateFuzzyMatching cwm = new CandidateFuzzyMatching();
		AnnotationExactMatching asm = new AnnotationExactMatching();
		AnnotationFuzzyMatching awm = new AnnotationFuzzyMatching();
		
		Executor.run(msnbc, priorer, asm, "output/priorer-msnbc-asm.out");
	}

}
