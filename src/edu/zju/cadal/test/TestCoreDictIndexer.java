package edu.zju.cadal.test;

import org.junit.Test;

import edu.zju.cadal.crosswiki.CoreDictIndexer;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年12月6日
 */
public class TestCoreDictIndexer {

	@Test
	public void test_index() {
		CoreDictIndexer indexer = new CoreDictIndexer(
				"/home/chenhui/Data/lucene_index/crosswikis_index/core_dict_index", 
				"/home/chenhui/Data/data/CrossWikis/dictionary");
		indexer.index();
		indexer.close();
	}

}
