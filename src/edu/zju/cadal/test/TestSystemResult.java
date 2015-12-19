package edu.zju.cadal.test;

import java.util.Map;
import java.util.Set;

import org.junit.Test;

import edu.zju.cadal.cache.Prediction;
import edu.zju.cadal.model.Candidate;
import edu.zju.cadal.model.Entity;
import edu.zju.cadal.utils.Pair;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月30日
 */
public class TestSystemResult {

	@Test
	public void test_remove() {
		Prediction result = Prediction.getInstance(true);
		result.remove("Spotlight", "MSNBC");
		result.flush();
	}
	
//	@Test
//	public void test() {
//		SystemResult result = SystemResult.getInstance(true);
//		Map<String, Set<Candidate>> cMap = result.getCandidateCache("Priorer", "MSNBC");
//		for (Candidate c : cMap.get("Bus3683270.txt")) {
////			System.out.println(c.getMention());
//			if (c.getMention().getSurfaceForm().equals("Lennar Corp.")) {
//				Set<Pair<Entity, Float>> ps = c.getPairSet();
//				for (Pair<Entity, Float> p : ps) {
//					System.out.println(p.first.getId() + " " + p.second);
//				}
//			}
//			
//		}
//	}
}
