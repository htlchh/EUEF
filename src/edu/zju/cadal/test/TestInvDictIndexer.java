package edu.zju.cadal.test;

import org.junit.Test;

import edu.zju.cadal.crosswiki.InvDictIndexer;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年12月6日
 */
public class TestInvDictIndexer {
	@Test
	public void test_index() {
		InvDictIndexer indexer = new InvDictIndexer(
				"/home/chenhui/Data/lucene_index/crosswikis_index/inv_dict_index", 
				"/home/chenhui/Data/data/CrossWikis/inv.dict.bz2");
		indexer.index();
		indexer.close();
	}
}
