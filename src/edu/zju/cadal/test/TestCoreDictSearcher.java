package edu.zju.cadal.test;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.junit.Test;

import edu.zju.cadal.crosswiki.CoreDictSearcher;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月19日
 */
public class TestCoreDictSearcher {
	@Test
	public void test_search() throws IOException {
		CoreDictSearcher s = new CoreDictSearcher("/home/chenhui/Data/lucene_index/crosswikis_index/core_dict_index");
		TopDocs topDocs = s.search("mention", "Lennar");
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		for (int i = 0; i < scoreDocs.length; i++) 
		{
			Document document = s.getSearcher().doc(scoreDocs[i].doc);
			System.out.println(
					document.getField("mention").stringValue() + " " + 
					document.getField("url").stringValue() + " " + Float.parseFloat(document.getField("cprob").stringValue()));
		}
	}
}
