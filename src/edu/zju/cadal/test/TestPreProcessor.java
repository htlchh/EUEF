package edu.zju.cadal.test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import edu.zju.cadal.cache.PRF;
import edu.zju.cadal.main.Evaluator;
import edu.zju.cadal.matching.MentionMatching;
import edu.zju.cadal.model.Candidate;
import edu.zju.cadal.model.Entity;
import edu.zju.cadal.model.Mention;
import edu.zju.cadal.utils.Pair;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月18日
 */
public class TestPreProcessor {

	@Test
	public void test_deference() {
		Set<Mention> mentionSet = new HashSet<Mention>();
		mentionSet.add(new Mention("", 2, 5, 0.3f));
		mentionSet.add(new Mention("", 3, 8, 0.7f));
		mentionSet.add(new Mention("", 15, 10, 0.9f));
		System.out.println(mentionSet);
		MentionMatching mwm = new MentionMatching();
		System.out.println(mwm.match(new Mention("", 1, 4), new Mention("", 1, 4)));
	}
	
	
	@Test
	public void test_filterOverlapCandidate() {
		Mention m1 = new Mention("", 2, 5, 0.3f);
		Mention m2 = new Mention("", 3, 8, 0.7f);
		Mention m3 = new Mention("", 15, 10, 0.9f);
		
		Pair<Entity, Float> p1 = new Pair<Entity, Float>(new Entity(10), 0.5f);
		Pair<Entity, Float> p2 = new Pair<Entity, Float>(new Entity(11), 0.6f);
		Pair<Entity, Float> p3 = new Pair<Entity, Float>(new Entity(19), 0.6f);
		Pair<Entity, Float> p4 = new Pair<Entity, Float>(new Entity(0), 0.9f);
		
		Set<Pair<Entity, Float>> s1 = new HashSet<Pair<Entity,Float>>();
		s1.add(p1);
		s1.add(p2);
		
		Set<Pair<Entity, Float>> s2 = new HashSet<Pair<Entity,Float>>();
		s2.add(p1);
		s2.add(p2);
		
		Set<Pair<Entity, Float>> s3 = new HashSet<Pair<Entity,Float>>();
		s3.add(p3);
		s3.add(p4);
		
		Set<Pair<Entity, Float>> empty = new HashSet<Pair<Entity,Float>>();
		
		Set<Candidate> sys = new HashSet<Candidate>();
		sys.add(new Candidate(m1, s1));
		sys.add(new Candidate(m2, s2));
		sys.add(new Candidate(m3, s3));
		sys.add(new Candidate(m3, empty));
		
		Set<Candidate> gold = new HashSet<Candidate>();
		Set<Pair<Entity, Float>> s4 = new HashSet<Pair<Entity,Float>>();
		s4.add(p1);
		Set<Pair<Entity, Float>> s5 = new HashSet<Pair<Entity,Float>>();
		s5.add(p4);
		gold.add(new Candidate(m1, s4));
		gold.add(new Candidate(m3, s5));
		
		Map<String, Set<Candidate>> systemResult = new HashMap<String, Set<Candidate>>();
		Map<String, Set<Candidate>> goldStandard = new HashMap<String, Set<Candidate>>();
		systemResult.put("test", sys);
		goldStandard.put("test", gold);
//		for (Candidate c : sys) {
//			System.out.print(c.getMention() + " ");
//		}
//		System.out.println();
//		for (Candidate c : PreProcessor.filterOverlapCandidate(sys))
//			System.out.print(c.getMention() + " ");
		
	}
	
	
}
