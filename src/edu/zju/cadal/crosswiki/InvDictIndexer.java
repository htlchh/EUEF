package edu.zju.cadal.crosswiki;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

import edu.zju.cadal.utils.BZ2;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年9月25日
 */
public class InvDictIndexer {

	private Analyzer analyzer;
	private IndexWriterConfig iwc;
	private IndexWriter writer;
	private BZ2 reader;	
		

	/**
	 * 
	 * @param resource, files to indexed
	 * @param destination, directory where stores the index files
	 */
	public InvDictIndexer(String indexDirectory, String dictPath) {
		try {
			reader = new BZ2(dictPath);
			this.analyzer = new CrossWikiAnalyzer();
			this.iwc = new IndexWriterConfig(analyzer);
			this.writer = new IndexWriter(FSDirectory.open(Paths.get(indexDirectory)), iwc);			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("deprecation")
	public void index() {
		String line = "";
		int i = 0;
		try {
			while ((line = reader.readLine()) != null) {
				int tabPos = line.indexOf('\t');
				int spacePos = line.indexOf(' ', tabPos+1);
				String url = line.substring(0, tabPos);
				String cprob = line.substring(tabPos+1, spacePos);
				int secondTabPos = line.indexOf('\t', spacePos+1);
				
				if (secondTabPos != -1) {
					String mention = line.substring(spacePos+1, secondTabPos);
					String[] scores = line.substring(secondTabPos+1, line.length()).split(" ");
					
					Document document = new Document();
					document.add(new Field("url", url, Field.Store.YES, Field.Index.ANALYZED));
					document.add(new Field("cprob", cprob, Field.Store.YES, Field.Index.NOT_ANALYZED));
					document.add(new Field("mention", mention, Field.Store.YES, Field.Index.ANALYZED));
					for (String label : scores) {
						String[] t = label.split(":");
						if (t.length == 1) 
							document.add(new Field(t[0], t[0], Field.Store.YES, Field.Index.NOT_ANALYZED));
						else
							document.add(new Field(t[0], t[1], Field.Store.YES, Field.Index.NOT_ANALYZED));
					}
					writer.addDocument(document);	
					if (i++ % 50000 == 0) 
						System.out.println(i + " lines processed");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			writer.close();
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
