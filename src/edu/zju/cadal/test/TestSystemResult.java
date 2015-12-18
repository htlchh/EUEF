package edu.zju.cadal.test;

import java.util.Map;
import java.util.Set;

import org.junit.Test;

import edu.zju.cadal.cache.SystemResult;
import edu.zju.cadal.dataset.ACE2004;
import edu.zju.cadal.model.Mention;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月30日
 */
public class TestSystemResult {

	@Test
	public void test_remove() {
		SystemResult result = SystemResult.getInstance(true);
		result.remove("Priorer", "IITB");
		result.flush();
	}
	
}
