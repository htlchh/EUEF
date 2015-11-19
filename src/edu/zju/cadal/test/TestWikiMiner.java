package edu.zju.cadal.test;

import org.junit.Test;

import edu.zju.cadal.dataset.MSNBC;
import edu.zju.cadal.system.WikiMiner;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月16日
 */
public class TestWikiMiner {

	@Test
	public void test_erd() {
		WikiMiner wm = new WikiMiner(false);
		MSNBC msnbc = new MSNBC(
				"dataset/MSNBC/RawTextsSimpleChars_utf8", 
				"dataset/MSNBC/Problems");
		wm.erd(msnbc);
	}
}
