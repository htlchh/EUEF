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
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年9月25日
 */
public class InvDictSearcher {

	private IndexReader reader;
	private IndexSearcher searcher;
	

	/**
	 * 
	 * @param indexPath, the directory path where index files located
	 */
	public InvDictSearcher(String indexPath) {
		System.err.println("load cross-wikis inv dictionary index files ... ");
		try {
			this.reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.searcher = new IndexSearcher(reader);			
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
