package edu.zju.cadal.test;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import edu.zju.cadal.model.Candidate;
import edu.zju.cadal.model.Entity;
import edu.zju.cadal.model.Mention;
import edu.zju.cadal.utils.Pair;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月14日
 */
public class TestCandidate {

	@Test
	public void test_equal() {
		Mention m1 = new Mention(0, 1, 1.0f);
		Mention m2 = new Mention(2, 1, 0.5f);
		
		Entity e1 = new Entity(10);
		Entity e2 = new Entity(11);
		
		Pair<Entity, Float> p1 = new Pair<Entity, Float>(e1, 0.5f);
		Pair<Entity, Float> p2 = new Pair<Entity, Float>(e1, 0.6f);
		Pair<Entity, Float> p3 = new Pair<Entity, Float>(e2, 0.6f);
		
		Set<Pair<Entity, Float>> s1 = new HashSet<Pair<Entity,Float>>();
		s1.add(p1);
		s1.add(p2);
		Candidate c1 = new Candidate(m1, s1);
		
		Set<Pair<Entity, Float>> s2 = new HashSet<Pair<Entity,Float>>();
		s2.add(p3);
		s2.add(p2);
		Candidate c2 = new Candidate(m1, s2);
		Assert.assertEquals(c1.equals(c2), true);
		
		Candidate c3 = new Candidate(m2, s1);
		Assert.assertEquals(c1.equals(c3), false);
		
		Set<Pair<Entity, Float>> s3 = new HashSet<Pair<Entity,Float>>();
		s3.add(p3);
		Candidate c4 = new Candidate(m1, s3);
		Assert.assertEquals(c1.equals(c4), false);
		
		Set<Candidate> cSet = new HashSet<Candidate>();
		cSet.add(c1);
		cSet.add(c2);
		Assert.assertEquals(cSet.size(), 1);
	}
}












