package edu.zju.cadal.test;

import org.junit.Test;

import edu.zju.cadal.dataset.DatasetDumper;
import edu.zju.cadal.dataset.MSNBC;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月16日
 */
public class TestDatasetDumper {

	@Test
	public void test_statistics() {
		MSNBC msnbc = new MSNBC(
				"dataset/MSNBC/RawTextsSimpleChars_utf8", 
				"dataset/MSNBC/Problems");
		DatasetDumper.statistics(msnbc);
	}
}
