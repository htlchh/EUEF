package edu.zju.cadal.main;

import edu.zju.cadal.dataset.MSNBC;
import edu.zju.cadal.matching.AnnotationFuzzyMatching;
import edu.zju.cadal.matching.CandidateFuzzyMatching;
import edu.zju.cadal.matching.MentionFuzzyMatching;
import edu.zju.cadal.matching.NILFuzzyMatching;
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
		
		
		MentionFuzzyMatching mm = new MentionFuzzyMatching();
		mm.setDistanceThreshold(0.000f);
		CandidateFuzzyMatching cm = new CandidateFuzzyMatching(mm);
		AnnotationFuzzyMatching am = new AnnotationFuzzyMatching(mm);
		NILFuzzyMatching nm = new NILFuzzyMatching(mm);
		
		Executor.run(msnbc, wm, am, "output/wikimier-msnbc-mfm.out");
	}

}
