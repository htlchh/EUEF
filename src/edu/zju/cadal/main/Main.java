package edu.zju.cadal.main;

import edu.zju.cadal.dataset.MSNBC;
import edu.zju.cadal.matching.AnnotationMatching;
import edu.zju.cadal.matching.CandidateMatching;
import edu.zju.cadal.matching.MentionMatching;
import edu.zju.cadal.matching.NILMatching;
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
		
		
		MentionMatching mm = new MentionMatching();
		mm.setDistanceThreshold(0.000f);
		CandidateMatching cm = new CandidateMatching(mm);
		AnnotationMatching am = new AnnotationMatching(mm);
		NILMatching nm = new NILMatching(mm);
		
		Executor.run(msnbc, wm, am, "output/wikipedia-miner-msnbc-am000.out");
	}

}
