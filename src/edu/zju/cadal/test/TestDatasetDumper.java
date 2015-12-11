package edu.zju.cadal.test;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.junit.Test;

import edu.zju.cadal.dataset.ACE2004;
import edu.zju.cadal.dataset.AQUAINT;
import edu.zju.cadal.dataset.DatasetDumper;
import edu.zju.cadal.dataset.IITB;
import edu.zju.cadal.dataset.MSNBC;
import edu.zju.cadal.dataset.TestA;
import edu.zju.cadal.dataset.TestB;
import edu.zju.cadal.dataset.Training;

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
		ACE2004 ace = new ACE2004("dataset/ACE2004_Coref_Turking/Dev/RawTextsNoTranscripts", 
				"dataset/ACE2004_Coref_Turking/Dev/ProblemsNoTranscripts");
		AQUAINT aquaint = new AQUAINT("dataset/AQUAINT/RawTexts", "dataset/AQUAINT/Problems");
		TestA testa = new TestA("dataset/aida/Conll-AIDA-TestA.tsv");
		TestB testb = new TestB("dataset/aida/Conll-AIDA-TestB.tsv");
		Training training = new Training("dataset/aida/Conll-AIDA-Training.tsv");
		IITB iitb = new IITB("dataset/iitb/crawledDocs", "dataset/iitb/CSAW_Annotations.xml");
		DatasetDumper.statistics(iitb);
	}
	
	@Test
	public void createRawText() {
//		TestA testa = new TestA("dataset/aida/Conll-AIDA-TestA.tsv");
//		TestB testb = new TestB("dataset/aida/Conll-AIDA-TestB.tsv");
//		Training training = new Training("dataset/aida/Conll-AIDA-Training.tsv");
		IITB iitb = new IITB("dataset/iitb/crawledDocs", "dataset/iitb/CSAW_Annotations.xml");		
		Map<String, String> rawText = iitb.getRawText();
		for (String title : rawText.keySet()) {
			writeDocument("/home/chenhui/workspace/evalution-framework/wikifier/IITB/"+title, rawText.get(title));
		}
		
	}
	
	/**
	 * 
	 * @Title: FileAccessor
	 * @Desciption: 写文件到指定路径
	 * @param documentPath 给定的写文件的路径
	 * @param document 要写入的内容
	 * @return 写成功，返回true，否则返回false
	 */
	static private boolean writeDocument(String documentPath, String document)
	{
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(documentPath), "UTF-8"));
			bw.write(document);
			bw.close();
			return true;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}	
}
