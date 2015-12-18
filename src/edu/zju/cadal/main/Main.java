package edu.zju.cadal.main;

import edu.zju.cadal.dataset.ACE2004;
import edu.zju.cadal.dataset.AQUAINT;
import edu.zju.cadal.dataset.IITB;
import edu.zju.cadal.dataset.MSNBC;
import edu.zju.cadal.dataset.TestA;
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
		ACE2004 ace = new ACE2004("dataset/ACE2004_Coref_Turking/Dev/RawTextsNoTranscripts", 
				"dataset/ACE2004_Coref_Turking/Dev/ProblemsNoTranscripts");		
		AQUAINT aquaint = new AQUAINT("dataset/AQUAINT/RawTexts", "dataset/AQUAINT/Problems");
		MSNBC test = new MSNBC("dataset/MSNBC1/RawTextsSimpleChars_utf8", "dataset/MSNBC1/Problems");
		TestA testa = new TestA("dataset/aida/Conll-AIDA-TestA.tsv");
//		TestB testb = new TestB("dataset/aida/Conll-AIDA-TestB.tsv");
//		Training training = new Training("dataset/aida/Conll-AIDA-Training.tsv");
		IITB iitb = new IITB("dataset/iitb/crawledDocs", "dataset/iitb/CSAW_Annotations.xml");
		
		MentionMatching mm = new MentionMatching();
		mm.setDistanceThreshold(0.00f);
		CandidateMatching cm = new CandidateMatching(mm);
		AnnotationMatching am = new AnnotationMatching(mm);
		NILMatching nm = new NILMatching(mm);
//		System.out.println("+++++++++++++++ Mention Matching Result ++++++++++++++++++++");
//		Executor.run(ace, priorer, mm, "output/priorer-ace-mm-000.out");
//		System.out.println("+++++++++++++++ Candidate Matching Result ++++++++++++++++++++");
//		Executor.run(ace, priorer, cm, "output/priorer-ace-cm-000.out");
//		System.out.println("+++++++++++++++ Annotation Matching Result ++++++++++++++++++++");
//		Executor.run(aquaint, priorer, am, "output/priorer-aquaint-am-000.out");
		System.out.println("+++++++++++++++ NIL Matching Result ++++++++++++++++++++");
		Executor.run(iitb, priorer, mm, "output/priorer-iitb-mm-000.out");		
		
//		DatasetDumper.statistics(ace);
	}

}
