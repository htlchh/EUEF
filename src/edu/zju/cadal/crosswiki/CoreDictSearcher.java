package edu.zju.cadal.crosswiki;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年8月27日
 */
public class CoreDictSearcher {
	private IndexReader reader;
	private IndexSearcher searcher;
	
	
	public IndexSearcher getSearcher() {
		return searcher;
	}

	public void setSearcher(IndexSearcher searcher) {
		this.searcher = searcher;
	}

	public CoreDictSearcher(String indexPath) {
		System.out.println("load cross-wikis core dictionary index files ... ");
		try {
			this.reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
			this.searcher = new IndexSearcher(reader);	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public TopDocs search(String field, String queryString) 
	{
		try {
			BooleanQuery bq = new BooleanQuery();
			String[] terms = queryString.toLowerCase().replaceAll("_", " ").split(" ");
			for (String t : terms) {
				bq.add(new TermQuery(new Term(field, t)), BooleanClause.Occur.MUST);
			}
			return searcher.search(bq, 100);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void close() {
		try {
			this.reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
