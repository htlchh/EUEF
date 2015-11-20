package edu.zju.cadal.test;

import org.junit.Test;

import edu.zju.cadal.priorer.NER;
import edu.zju.cadal.utils.Timer;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月19日
 */
public class TestNER {

	@Test
	public void test_recognize() {
		NER ner = new NER();
		ner.recognize("NEW YORK - Stocks moved soundly higher in the first session of 2007 as investors cheered mostly solid readings on the economy and found reason for increased prospects for big-name retailers Home Depot Inc. and Wal-Mart Stores Inc. ", new Timer());
	}
	
}
