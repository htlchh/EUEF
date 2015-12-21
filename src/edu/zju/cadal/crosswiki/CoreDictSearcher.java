package edu.zju.cadal.crosswiki;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

/**
 * CoreDictSearcher class is used to
 * Search CrossWikis's core dictionary using Lucene.
 * 
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

	public CoreDictSearcher(String indexDirectory) {
		System.out.println("Load CrossWikis Core Dictionary Index Files ... ");
		try {
			this.reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexDirectory)));
			this.searcher = new IndexSearcher(reader);	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public TopDocs search(String field, String queryString) 
	{
		try {
			Query query = new TermQuery(new Term(field, queryString.toLowerCase()));
			return searcher.search(query, 100);
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
