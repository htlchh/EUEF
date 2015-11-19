package edu.zju.cadal.crosswiki;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

/**
 * Index and search CrossWikis using Lucene.
 * 
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年9月25日
 */
public class CoreDictIndexer {
	
	private Analyzer analyzer;
	private IndexWriterConfig iwc;
	private IndexWriter writer;
	private File file;
	private BufferedReader br;	
	
	public CoreDictIndexer(String indexPath, String dumpPath) {
		try {
			file = new File(dumpPath);
			br = new BufferedReader(new FileReader(file));
			this.analyzer = new TitleAnalyzer();
			this.iwc = new IndexWriterConfig(analyzer);
			this.writer = new IndexWriter(FSDirectory.open(Paths.get(indexPath)), iwc);			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	@SuppressWarnings("deprecation")
	public void index() {
		String line = "";
		int i = 0;
		try {
			while ((line = br.readLine()) != null) {
				int tabPos = line.indexOf('\t');
				int spacePos = line.indexOf(' ', tabPos+1);
				String mention = line.substring(0, tabPos);
				String cprob = line.substring(tabPos+1, spacePos);
				int secondSpacePos = line.indexOf(' ', spacePos+1);
				String url = line.substring(spacePos+1, secondSpacePos);
				String[] scores = line.substring(secondSpacePos+1, line.length()).split(" ");
				
				Document document = new Document();
				document.add(new Field("mention", mention, Field.Store.NO, Field.Index.ANALYZED));
				document.add(new Field("cprob", cprob, Field.Store.YES, Field.Index.ANALYZED));
				document.add(new Field("url", url, Field.Store.YES, Field.Index.ANALYZED));
				for (String label : scores) {
					String[] t = label.split(":");
					//label is the field value itself
					if (t.length == 1) 
						document.add(new Field(t[0], t[0], Field.Store.NO, Field.Index.ANALYZED));
					else
						document.add(new Field(t[0], t[1], Field.Store.YES, Field.Index.ANALYZED));
				}
				writer.addDocument(document);	
				if (i % 50000 == 0) 
					System.out.println(i + " lines processed");
				i++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
